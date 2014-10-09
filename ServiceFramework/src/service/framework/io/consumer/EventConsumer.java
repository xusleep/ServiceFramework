package service.framework.io.consumer;

import java.io.IOException;

import service.framework.io.event.ServiceEvent;

public interface EventConsumer {
	public void comsume(ServiceEvent event) throws IOException;
}
