package service.framework.io.server;

import java.nio.channels.SelectionKey;

import service.framework.io.listener.ServiceEventMulticaster;

public interface Server extends Runnable{
	public void submitOpeRegister(SelectionKey key);
	public ServiceEventMulticaster getServiceEventMulticaster();
}
