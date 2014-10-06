package service.framework.io.listener;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.atomic.AtomicInteger;

import service.framework.io.entity.IOSession;
import service.framework.io.event.ServiceEvent;
import service.framework.io.event.ServiceOnAcceptedEvent;
import service.framework.io.event.ServiceOnErrorEvent;
import service.framework.io.event.ServiceOnReadEvent;
import service.framework.io.event.ServiceOnWriteEvent;
import service.framework.io.event.ServiceStartedEvent;
import service.framework.io.master.MasterHandler;
import service.framework.io.server.DefaultServer;
import service.framework.io.server.Server;

public class DefaultServiceWriterListener implements ServiceListener {
	private AtomicInteger aint = new AtomicInteger(0);
	
	public DefaultServiceWriterListener(){
	}
	
	@Override
	public void onApplicationEvent(ServiceEvent event) throws IOException {

		//�������ӵĴ�����д����������߳���
		if(event instanceof ServiceOnAcceptedEvent){
			ServiceOnAcceptedEvent objServiceOnAcceptedEvent = (ServiceOnAcceptedEvent)event; 
			ServerSocketChannel ssc = (ServerSocketChannel) objServiceOnAcceptedEvent.getSelectionKey()
					.channel();
			SelectionKey key = objServiceOnAcceptedEvent.getSelectionKey();
			SocketChannel sc = ssc.accept();
			sc.configureBlocking(false);
			// �������������¼�
			IOSession request = new IOSession(sc);
			request.setRegisterInterestOps(SelectionKey.OP_READ);
			key.attach(request);
			Server objServer = objServiceOnAcceptedEvent.getServer();
			// ע�������,�Խ�����һ���Ķ�����
			objServer.submitOpeRegister(key);
			System.out.println("Accepted connection ... count = " + aint.incrementAndGet());
		}
		else 
		{
			MasterHandler.processRequest(event);
		}
		
	}	
}
