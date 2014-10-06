package service.framework.io.listener;

import java.io.IOException;

import service.framework.io.event.ServiceEvent;

public interface ServiceListener {
	public void onApplicationEvent(ServiceEvent event) throws IOException;
}
