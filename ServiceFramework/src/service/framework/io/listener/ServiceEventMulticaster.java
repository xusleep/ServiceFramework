package service.framework.io.listener;

import java.io.IOException;

import service.framework.io.event.ServiceEvent;

public interface ServiceEventMulticaster {
	public void registerListener(ServiceListener listener);
	public void multicastEvent(ServiceEvent event) throws IOException;
}
