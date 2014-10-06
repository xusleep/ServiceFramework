package service.framework.io.master;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import service.framework.io.entity.IOSession;
import service.framework.io.event.ServiceEvent;
import service.framework.io.event.ServiceOnErrorEvent;
import service.framework.io.event.ServiceOnReadEvent;
import service.framework.io.event.ServiceOnWriteEvent;
import service.framework.io.event.ServiceStartedEvent;
import service.framework.io.event.ServiceStartingEvent;
import service.framework.io.listener.ServiceEventMulticaster;
import service.framework.io.server.Server;
import service.framework.protocol.ShareingProtocolData;
import service.framework.provide.ProviderBean;
import service.framework.provide.entity.RequestEntity;
import service.framework.provide.entity.ResponseEntity;
import service.framework.serialization.SerializeUtils;

public class DefaultEventConsumer implements EventConsumer {
	private AtomicInteger aint = new AtomicInteger(0);
	
	private final ApplicationContext applicationContext;
	private final ServiceEventMulticaster objServiceEventMulticaster;
	
	public DefaultEventConsumer(ApplicationContext applicationContext, ServiceEventMulticaster objServiceEventMulticaster){
		this.applicationContext = applicationContext;
		this.objServiceEventMulticaster = objServiceEventMulticaster;
	}
	
	@Override
	public void comsume(ServiceEvent event) throws IOException {
		// TODO Auto-generated method stub
		if (event instanceof ServiceOnReadEvent) {
			// TODO Auto-generated method stub
			ServiceOnReadEvent objServiceOnReadEvent = (ServiceOnReadEvent) event;
			SelectionKey key = objServiceOnReadEvent.getSelectionKey();
			boolean succ = read(key);
			if(succ)
			{
				this.objServiceEventMulticaster.multicastEvent(new ServiceOnWriteEvent(key));
			}
			//�����ǿͻ��������ر������ӣ��������ҲҪ�ر�
			else
			{
				try {
					this.closeChanel((SocketChannel) key.channel());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		} else if (event instanceof ServiceOnWriteEvent) {
			aint.incrementAndGet();
			ServiceOnWriteEvent objServiceOnReadEvent = (ServiceOnWriteEvent) event;
			SelectionKey key = objServiceOnReadEvent.getSelectionKey();
			try {
				IOSession request = (IOSession) key.attachment();
				String receiveData = new String(request.getDataInput(), ShareingProtocolData.FRAMEWORK_IO_ENCODING);
				RequestEntity objRequestEntity = SerializeUtils.deserializeRequest(receiveData);
				ProviderBean objProviderBean = (ProviderBean)applicationContext.getBean(objRequestEntity.getServiceName());
				ResponseEntity objResponseEntity = objProviderBean.prcessRequest(objRequestEntity);
				send(key, SerializeUtils.serializeResponse(objResponseEntity).getBytes(ShareingProtocolData.FRAMEWORK_IO_ENCODING));
				closeChanel((SocketChannel)key.channel());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else if(event instanceof ServiceOnErrorEvent){
			System.out.println("�����˴���" + ((ServiceOnErrorEvent)event).getMsg());
		}
		else if(event instanceof ServiceStartingEvent){
            System.out.println("Server Service starting ...");
		}
		//����������������ע�ᵽ��������ȥ
		else if(event instanceof ServiceStartedEvent){
			//���ｫ���ע�ᵽ�������ĵĴ���
            System.out.println("Server Service started.");
		}
		System.out.println("���������Ϊ:" + aint.get());
		System.out.println("���г���Ϊ:" + MasterHandler.pool.size());
	}
	
	
	/**
     * ��ͻ���д����
     * @param data byte[]������Ӧ����
     */
    public void send(SelectionKey key, byte[] data) throws IOException {
    	SocketChannel sc = (SocketChannel) key.channel();
        ByteBuffer buffer = ByteBuffer.allocate(data.length);
        buffer.put(data, 0, data.length);
        buffer.flip();
        sc.write(buffer);
        sc.shutdownOutput();
    }

	 /**
     * ��ȡ�ͻ��˷�����������
     * @param sc �׽�ͨ��
     */
    private static int BUFFER_SIZE = 1024;
    public static boolean readRequest(SocketChannel sc, IOSession request) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
        int off = 0;
        int r = 0;
        byte[] data = new byte[BUFFER_SIZE * 10];
        buffer.clear();
        r = sc.read(buffer);
        // �����ȡ��������Ϊ-1����˵�����ͻ�����ͼ�رգ���ô����Ҳ�ر�
        if (r == -1) 
        {
        	return false;
        }
        while ( true ) {
            if ( (off + r) > data.length) {
                data = grow(data, BUFFER_SIZE * 10);
            }
            byte[] buf = buffer.array();
            System.arraycopy(buf, 0, data, off, r);
            off += r;
            buffer.clear();
            r = sc.read(buffer);
            //������ĩβ���˳�
            if (r == -1) 
            {
            	break;
            }
        }
        byte[] req = new byte[off];
        System.arraycopy(data, 0, req, 0, off);
        request.setDataInput(req);
        return true;
    }

    /**
     * �����������ݶ�ȡ
     * @param key SelectionKey
     * @throws IOException 
     */
    public boolean read(SelectionKey key) throws IOException {
        // ��ȡ�ͻ�������
        SocketChannel sc = (SocketChannel) key.channel();
        IOSession request = (IOSession)key.attachment();
        return readRequest(sc, request);
    }
    
    /**
     * ��������
     * @param src byte[] Դ��������
     * @param size int ���ݵ�������
     * @return byte[] ���ݺ������
     */
    public static byte[] grow(byte[] src, int size) {
        byte[] tmp = new byte[src.length + size];
        System.arraycopy(src, 0, tmp, 0, src.length);
        return tmp;
    }
    
	private void closeChanel(SocketChannel channel) throws IOException{
		if(channel!=null)
		{
			channel.socket().close();
			channel.close();
		}
	}
}
