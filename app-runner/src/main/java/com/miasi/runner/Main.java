package com.miasi.runner;

import com.miasi.helpdesk.application.services.TicketApplicationService;
import com.miasi.helpdesk.application.services.TicketFactory;
import com.miasi.helpdesk.domain.model.Category;
import com.miasi.helpdesk.domain.services.StaffAssignmentService;
import com.miasi.helpdesk.infrastructure.inbound.SlaCronJobAdapter;
import com.miasi.helpdesk.infrastructure.inbound.TicketRestController;
import com.miasi.helpdesk.infrastructure.outbound.AgentAvailabilityAdapter;
import com.miasi.helpdesk.infrastructure.outbound.JpaTicketRepository;
import com.miasi.helpdesk.infrastructure.outbound.NotificationAdapter;
import io.javalin.Javalin;
import io.javalin.json.JavalinJackson;
import jakarta.persistence.EntityManagerFactory;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.http.HttpClient;
import java.util.Enumeration;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.hibernate.cfg.Configuration;

public final class Main {

  private static final String PROPERTIES_RESOURCE = "application.properties";

  private Main() {}

  public static void main(String[] args) {
    try {
      Properties props = loadMergedProperties(PROPERTIES_RESOURCE);

      int port = Integer.parseInt(props.getProperty("server.port", "7070"));
      String dbUrl = props.getProperty("db.url", "jdbc:sqlite:helpdesk.db");
      long slaMinutes = Long.parseLong(props.getProperty("helpdesk.sla.defaultMinutes", "60"));
      String usersBaseUrl = props.getProperty("users.baseUrl", "http://localhost:7070");
      String defaultCategoryName = props.getProperty("helpdesk.defaultCategory", "general");

      EntityManagerFactory emf = buildEntityManagerFactory(dbUrl, props);

      JpaTicketRepository ticketRepository = new JpaTicketRepository(emf);
      AgentAvailabilityAdapter agentProvider =
          new AgentAvailabilityAdapter(HttpClient.newHttpClient(), usersBaseUrl);
      NotificationAdapter notificationSender = new NotificationAdapter();
      StaffAssignmentService staffAssignmentService = new StaffAssignmentService();
      TicketFactory ticketFactory = new TicketFactory(slaMinutes);
      Category defaultCategory = new Category(defaultCategoryName);

      TicketApplicationService ticketService =
          new TicketApplicationService(
              ticketFactory,
              ticketRepository,
              agentProvider,
              notificationSender,
              staffAssignmentService,
              defaultCategory);

      SlaCronJobAdapter slaCronJob = new SlaCronJobAdapter(ticketRepository, ticketService);
      TicketRestController ticketController =
          new TicketRestController(ticketService, ticketService, ticketService);

      Javalin app =
          Javalin.create(
              config -> {
                config.jsonMapper(new JavalinJackson());

                config.routes.apiBuilder(
                    () -> {
                      io.javalin.apibuilder.ApiBuilder.get("/health", ctx -> ctx.result("OK"));
                      ticketController.configureRoutes();
                    });
              });
      app.start(port);

      System.out.println("Helpdesk app running on http://localhost:" + port);

      ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
      scheduler.scheduleAtFixedRate(slaCronJob::checkSlaExceeded, 5, 5, TimeUnit.MINUTES);

      Runtime.getRuntime()
          .addShutdownHook(
              new Thread(
                  () -> {
                    scheduler.shutdown();
                    app.stop();
                    emf.close();
                  }));
    } catch (Exception exc) {
      System.out.println(exc);
    }
  }

  private static EntityManagerFactory buildEntityManagerFactory(String dbUrl, Properties appProps) {
    Configuration cfg = new Configuration();
    cfg.setProperty("hibernate.connection.url", dbUrl);
    cfg.setProperty(
        "hibernate.connection.driver_class", appProps.getProperty("db.driver", "org.sqlite.JDBC"));
    cfg.setProperty(
        "hibernate.dialect",
        appProps.getProperty("hibernate.dialect", "org.hibernate.community.dialect.SQLiteDialect"));
    cfg.setProperty(
        "hibernate.hbm2ddl.auto", appProps.getProperty("hibernate.hbm2ddl.auto", "update"));
    cfg.setProperty("hibernate.show_sql", appProps.getProperty("hibernate.show_sql", "false"));
    cfg.addAnnotatedClass(com.miasi.helpdesk.infrastructure.outbound.TicketEntity.class);
    cfg.addAnnotatedClass(com.miasi.helpdesk.infrastructure.outbound.CommentEmbeddable.class);
    return cfg.buildSessionFactory();
  }

  private static Properties loadMergedProperties(String resourceName) {
    Properties merged = new Properties();
    try {
      Enumeration<URL> resources = Main.class.getClassLoader().getResources(resourceName);
      while (resources.hasMoreElements()) {
        URL url = resources.nextElement();
        try (InputStream in = url.openStream()) {
          merged.load(in);
        }
      }
    } catch (IOException e) {
      throw new IllegalStateException("Failed to load properties from " + resourceName, e);
    }
    return merged;
  }
}
