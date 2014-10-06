package service.framework.io.listener;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import service.framework.io.event.ServiceEvent;

public class DefaultServiceEventMulticaster implements ServiceEventMulticaster {
	
	private List<ServiceListener> listeners = null;
	
	public DefaultServiceEventMulticaster()
	{
		listeners = new LinkedList<ServiceListener>();
	}
	
	public DefaultServiceEventMulticaster(List<ServiceListener> listeners){
		this.listeners = listeners;
	}
	
	public void multicastEvent(ServiceEvent event) throws IOException
	{
		for(ServiceListener listener : listeners)
		{
			listener.onApplicationEvent(event);
		}
	}

	@Override
	public void registerListener(ServiceListener listener) {
		// TODO Auto-generated method stub
		listeners.add(listener);
	}

	public List<ServiceListener> getListeners() {
		return listeners;
	}

	public void setListeners(List<ServiceListener> listeners) {
		this.listeners = listeners;
	}
	
	
}
