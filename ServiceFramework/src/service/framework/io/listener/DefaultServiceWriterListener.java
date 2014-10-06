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

		//接受连接的代码需写到这里，即主线程中
		if(event instanceof ServiceOnAcceptedEvent){
			ServiceOnAcceptedEvent objServiceOnAcceptedEvent = (ServiceOnAcceptedEvent)event; 
			ServerSocketChannel ssc = (ServerSocketChannel) objServiceOnAcceptedEvent.getSelectionKey()
					.channel();
			SelectionKey key = objServiceOnAcceptedEvent.getSelectionKey();
			SocketChannel sc = ssc.accept();
			sc.configureBlocking(false);
			// 触发接受连接事件
			IOSession request = new IOSession(sc);
			request.setRegisterInterestOps(SelectionKey.OP_READ);
			key.attach(request);
			Server objServer = objServiceOnAcceptedEvent.getServer();
			// 注册读操作,以进行下一步的读操作
			objServer.submitOpeRegister(key);
			System.out.println("Accepted connection ... count = " + aint.incrementAndGet());
		}
		else 
		{
			MasterHandler.processRequest(event);
		}
		
	}	
}
