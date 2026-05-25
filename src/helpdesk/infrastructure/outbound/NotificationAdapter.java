package helpdesk.infrastructure.outbound;

import helpdesk.application.ports.outbound.INotificationSender;

public class NotificationAdapter implements INotificationSender {

	/**
	 * 
	 * @param event
	 */
	public void send(DomainEvent event) {
		// TODO - implement NotificationAdapter.send
		throw new UnsupportedOperationException();
	}

}