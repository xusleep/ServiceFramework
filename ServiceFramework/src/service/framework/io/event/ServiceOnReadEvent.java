package service.framework.io.event;

import java.nio.channels.SelectionKey;

import service.framework.io.server.Server;

public class ServiceOnReadEvent implements ServiceEvent {
	private SelectionKey selectionKey;
	private Server server;
	
	public ServiceOnReadEvent(SelectionKey selectionKey, Server server)
	{
		this.selectionKey = selectionKey;
		this.server = server;
	}

	public Server getServer() {
		return server;
	}

	public void setServer(Server server) {
		this.server = server;
	}

	public SelectionKey getSelectionKey() {
		return selectionKey;
	}

	public void setSelectionKey(SelectionKey selectionKey) {
		this.selectionKey = selectionKey;
	}
}
