package service.framework.io.server;

import java.util.List;
import java.util.LinkedList;
import java.util.Queue;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.Selector;
import java.nio.channels.SelectionKey;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import service.framework.io.entity.IOSession;
import service.framework.io.event.ServiceOnAcceptedEvent;
import service.framework.io.event.ServiceOnClosedEvent;
import service.framework.io.event.ServiceOnErrorEvent;
import service.framework.io.event.ServiceOnReadEvent;
import service.framework.io.event.ServiceOnWriteEvent;
import service.framework.io.event.ServiceStartedEvent;
import service.framework.io.event.ServiceStartingEvent;
import service.framework.io.listener.DefaultServiceEventMulticaster;
import service.framework.io.listener.ServiceEventMulticaster;
import service.framework.io.listener.DefaultServiceWriterListener;
import service.framework.io.master.MasterHandler;
import service.framework.protocol.ShareingProtocolData;
import service.framework.provide.entity.ResponseEntity;
import service.framework.serialization.SerializeUtils;
import servicecenter.service.ServiceInformation;

/**
 * <p>
 * Title: ���ط����߳�
 * </p>
 * 
 * @author starboy
 * @version 1.0
 */

public class DefaultServer implements Server {
	private static Queue<SelectionKey> wpool = new ConcurrentLinkedQueue<SelectionKey>(); // ��Ӧ��
	private final Selector selector;
	private final ServerSocketChannel sschannel;
	private final InetSocketAddress address;
	private final ServiceEventMulticaster serviceEventMulticaster;

	public DefaultServer(ServiceInformation serviceInformation, ServiceEventMulticaster serviceEventMulticaster) throws Exception {
		this.serviceEventMulticaster = serviceEventMulticaster;
		this.serviceEventMulticaster.multicastEvent(new ServiceStartingEvent());
		// ���������������׽�
		selector = Selector.open();
		sschannel = ServerSocketChannel.open();
		sschannel.configureBlocking(false);
		address = new InetSocketAddress(serviceInformation.getAddress(), serviceInformation.getPort());
		ServerSocket ss = sschannel.socket();
		ss.bind(address);
		sschannel.register(selector, SelectionKey.OP_ACCEPT);
		this.serviceEventMulticaster.multicastEvent(new ServiceStartedEvent());
	}

	public ServiceEventMulticaster getServiceEventMulticaster() {
		return serviceEventMulticaster;
	}

	public void run() {
		// ����
		while (true) {
			try {
				int num = 0;
				num = selector.select();
				if (num > 0) {
					Set selectedKeys = selector.selectedKeys();
					Iterator it = selectedKeys.iterator();
					while (it.hasNext()) {
						SelectionKey key = (SelectionKey) it.next();
						it.remove();
						// ����IO�¼�
						if (key.isAcceptable()) {
							// Accept the new connection
							serviceEventMulticaster.multicastEvent(new ServiceOnAcceptedEvent(key, this));
						} else if (key.isReadable()) {
							serviceEventMulticaster.multicastEvent(new ServiceOnReadEvent(key, this));
							key.cancel();
						} 
						else
						{
							addReadWriterRegister();
						}
					}
				} 
				else
				{
					addReadWriterRegister();
				}
			} catch (Exception e) {
				continue;
			}
		}
	}

	/**
	 * ����µ�ͨ��ע��
	 */
	public void addReadWriterRegister() {
		while (!wpool.isEmpty()) {
			SelectionKey key = wpool.poll();
			IOSession request = (IOSession)key.attachment();
			SocketChannel schannel = request.getSc();
			try {
				schannel.register(selector, request.getRegisterInterestOps(),
						request);
			} catch (Exception e) {
				try {
					schannel.finishConnect();
					schannel.close();
					schannel.socket().close();
					serviceEventMulticaster.multicastEvent(new ServiceOnClosedEvent());
				} 
				catch (Exception e1) {
				}
			}
		}
	}
	
	/**
	 * �ύ�µĿͻ���д�������������̵߳Ļ�Ӧ����
	 */
	public void submitOpeRegister(SelectionKey key) {
		wpool.offer(key);
		selector.wakeup(); // ���selector������״̬���Ա�ע���µ�ͨ��
	}
}
