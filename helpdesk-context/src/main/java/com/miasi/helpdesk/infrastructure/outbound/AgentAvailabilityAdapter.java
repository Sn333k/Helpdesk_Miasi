package com.miasi.helpdesk.infrastructure.outbound;

import com.miasi.helpdesk.application.domain.model.AgentSnapshot;
import com.miasi.helpdesk.application.domain.model.Category;
import com.miasi.helpdesk.application.ports.outbound.IAgentAvailabilityProvider;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public class AgentAvailabilityAdapter implements IAgentAvailabilityProvider {

  private final HttpClient httpClient;
  private final String usersBaseUrl;

  public AgentAvailabilityAdapter(HttpClient httpClient, String usersBaseUrl) {
    this.httpClient = httpClient;
    this.usersBaseUrl = usersBaseUrl;
  }

  @Override
  public List<AgentSnapshot> getAvailableAgentsForCategory(Category category) {
    try {
      URI uri =
          URI.create(usersBaseUrl + "/internal/users/agents?specialization=" + category.name());
      HttpRequest request = HttpRequest.newBuilder(uri).GET().build();
      HttpResponse<String> response =
          httpClient.send(request, HttpResponse.BodyHandlers.ofString());
      return parseAgentSnapshots(response.body());
    } catch (Exception e) {
      throw new RuntimeException("Failed to fetch agents for category " + category.name(), e);
    }
  }

  private List<AgentSnapshot> parseAgentSnapshots(String json) {
    // minimal hand-rolled JSON array parse: expects
    // [{"agentId":"...","currentLoad":N,"available":true}, ...]
    if (json == null || json.isBlank() || json.strip().equals("[]")) return List.of();

    List<AgentSnapshot> result = new java.util.ArrayList<>();
    String[] entries = json.strip().replaceAll("^\\[|]$", "").split("\\},\\s*\\{");

    for (String entry : entries) {
      String agentId = extractString(entry, "agentId");
      int currentLoad = Integer.parseInt(extractString(entry, "currentLoad"));
      boolean available = Boolean.parseBoolean(extractString(entry, "available"));
      result.add(new AgentSnapshot(agentId, currentLoad, available));
    }
    return result;
  }

  private String extractString(String json, String key) {
    int start = json.indexOf("\"" + key + "\"");
    if (start < 0) throw new IllegalArgumentException("Key not found: " + key);
    int colon = json.indexOf(':', start);
    String rest = json.substring(colon + 1).strip();
    if (rest.startsWith("\"")) {
      int end = rest.indexOf('"', 1);
      return rest.substring(1, end);
    }
    int end = rest.indexOf(',');
    if (end < 0) end = rest.indexOf('}');
    if (end < 0) end = rest.length();
    return rest.substring(0, end).strip();
  }
}
