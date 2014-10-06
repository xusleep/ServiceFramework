package service.framework.io.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import service.framework.io.client.comsume.ClientManagement;
import service.framework.io.client.comsume.ConsumerBean;
import service.framework.io.entity.IOSession;
import service.framework.io.event.ServiceOnAcceptedEvent;
import service.framework.io.event.ServiceOnClosedEvent;
import service.framework.io.event.ServiceOnConnectedEvent;
import service.framework.io.event.ServiceOnErrorEvent;
import service.framework.io.event.ServiceOnReadEvent;
import service.framework.io.event.ServiceOnWriteEvent;
import service.framework.io.event.ServiceStartedEvent;
import service.framework.io.event.ServiceStartingEvent;
import service.framework.io.listener.ServiceEventMulticaster;
import service.framework.io.server.Server;
import service.framework.protocol.ShareingProtocolData;
import service.framework.provide.entity.RequestEntity;
import service.framework.provide.entity.ResponseEntity;
import service.framework.serialization.SerializeUtils;

public class ClientTask implements Callable {
    private final RequestEntity objRequestEntity;
	public static AtomicLong idCounter = new AtomicLong(0);
    //ͨ��������  
    private Selector selector;  
    
    
	public ClientTask(String address, int port, RequestEntity objRequestData) throws IOException
	{
		this.objRequestEntity = objRequestData;
        // ���һ��Socketͨ��  
        SocketChannel channel = SocketChannel.open();  
        // ����ͨ��Ϊ������  
        channel.configureBlocking(false);  
        // ���һ��ͨ��������  
        this.selector = Selector.open();  
          
        // �ͻ������ӷ�����,��ʵ����ִ�в�û��ʵ�����ӣ���Ҫ��listen���������е�  
        //��channel.finishConnect();�����������  
        channel.connect(new InetSocketAddress(address, port));  
        //��ͨ���������͸�ͨ���󶨣���Ϊ��ͨ��ע��SelectionKey.OP_CONNECT�¼���  
        channel.register(selector, SelectionKey.OP_CONNECT);  
	}
	
	private void writeMessage(SocketChannel channel) throws IOException{
		String sendData = SerializeUtils.serializeRequest(this.objRequestEntity);
        byte[] data = sendData.getBytes(ShareingProtocolData.FRAMEWORK_IO_ENCODING);
        //��������Ը�����˷�����Ϣ
        ByteBuffer buffer = ByteBuffer.allocate(data.length);
        buffer.put(data, 0, data.length);
        buffer.flip();
        while (buffer.hasRemaining()) {  
            channel.write(buffer);  
        } 
	}

	@Override
	public Object call() {
		ResponseEntity result = null;
		try
		{
			boolean exitFlag = false;
			 // ��ѯ����selector  
	        while (true) {  
	        	if(exitFlag || Thread.currentThread().interrupted())
	        		break;
	            selector.select();  
	            // ���selector��ѡ�е���ĵ�����  
	            Iterator ite = this.selector.selectedKeys().iterator();  
	            while (ite.hasNext()) {  
	                SelectionKey key = (SelectionKey) ite.next();  
	                // ɾ����ѡ��key,�Է��ظ�����  
	                ite.remove();  
	                // �����¼�����  
	                if (key.isConnectable()) {  
	                    SocketChannel channel = (SocketChannel) key  
	                            .channel();  
	                    // ����������ӣ����������  
	                    if(channel.isConnectionPending()){  
	                        channel.finishConnect();  
	                    }  
	                    // ���óɷ�����  
	                    channel.configureBlocking(false);  
	                    writeMessage(channel);
	                    //��������Ը�����˷�����ϢŶ  
	                    channel.shutdownOutput();
	                    //�ںͷ�������ӳɹ�֮��Ϊ�˿��Խ��յ�����˵���Ϣ����Ҫ��ͨ�����ö���Ȩ�ޡ�  
	                    IOSession request = new IOSession(channel);
	                    channel.register(this.selector, SelectionKey.OP_READ, request); 
	                    // ����˿ɶ����¼�  
	                } else if (key.isReadable()) {
                        boolean succ = read(key); 
                        if(!succ)
                        {        	
                        	closeChanel((SocketChannel)key.channel());
                        	key.cancel();
                        	exitFlag = true;
                        	System.out.println("close connection count = " + idCounter.incrementAndGet());
                        }
                        else
                        {
	                        IOSession request = (IOSession)key.attachment();
	                        result = SerializeUtils.deserializeResponse(new String(request.getDataInput(), ShareingProtocolData.FRAMEWORK_IO_ENCODING));
                        } 
	                }  
	            }
	        }
		}
		catch(IOException ex){
			ex.printStackTrace();
		}
		return result;
	}
	
	private void closeChanel(SocketChannel channel) throws IOException{
		if(channel!=null)
		{
			channel.socket().close();
			channel.close();
		}
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
     */
    public boolean read(SelectionKey key) {
        try {
            // ��ȡ�ͻ�������
            SocketChannel sc = (SocketChannel) key.channel();
            IOSession request = (IOSession)key.attachment();
            return readRequest(sc, request);
        }
        catch (Exception e) {
        	e.printStackTrace();
        	return false;
        }
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
}
