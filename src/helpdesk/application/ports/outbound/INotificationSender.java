package helpdesk.application.ports.outbound;

public interface INotificationSender {

	/**
	 * 
	 * @param event
	 */
	void sendNotification(DomainEvent event);

}