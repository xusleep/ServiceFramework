package service.framework.io.consumer;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import service.framework.io.client.comsume.ConsumerBean;
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
import servicecenter.service.ServiceCenterUtils;
import servicecenter.service.ServiceInformation;

public class ServiceRegisterEventConsumer implements EventConsumer {
	private AtomicInteger aint = new AtomicInteger(0);
	
	private final ApplicationContext applicationContext;
	private final ServiceEventMulticaster objServiceEventMulticaster;
	
	public ServiceRegisterEventConsumer(ApplicationContext applicationContext, ServiceEventMulticaster objServiceEventMulticaster){
		this.applicationContext = applicationContext;
		this.objServiceEventMulticaster = objServiceEventMulticaster;
	}
	
	@Override
	public void comsume(ServiceEvent event) throws IOException {
		// TODO Auto-generated method stub
		if (event instanceof ServiceStartedEvent) {
			// TODO Auto-generated method stub
			//执行
			ServiceStartedEvent objServiceOnReadEvent = (ServiceStartedEvent) event;
			ServiceInformation serviceInformation = (ServiceInformation)applicationContext.getBean("serviceInformation");
			List<ServiceInformation> serviceInformationList = new LinkedList<ServiceInformation>();
			//获取所有服务，将服务注册到注册中心
			Map serviceList = applicationContext.getBeansOfType(ProviderBean.class);
			Iterator keys = serviceList.keySet().iterator();
			while(keys.hasNext()){
				String beanName = (String) keys.next();
				ProviderBean bean = (ProviderBean) serviceList.get(beanName);
				String interfaceName = bean.getInterfaceName();
				try {
					Class interfaceclass = Class.forName(interfaceName);
					Method[] methods = interfaceclass.getMethods();
					for(int i = 0; i < methods.length; i++){
						ServiceInformation subServiceInformation = new ServiceInformation();
						subServiceInformation.setAddress(serviceInformation.getAddress());
						subServiceInformation.setPort(serviceInformation.getPort());
						subServiceInformation.setServiceMethod(methods[i].getName());
						subServiceInformation.setServiceName(beanName);
						subServiceInformation.setServiceVersion(bean.getVersion());
						serviceInformationList.add(subServiceInformation);
						System.out.println("service name : " + beanName + " method name : " + methods[i].getName());
					}
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
			String strServiceInformation = SerializeUtils.serializeServiceInformationList(serviceInformationList);
			ConsumerBean objConsumerBean = (ConsumerBean)applicationContext.getBean("linkToServiceCenter");
			List<String> args = new LinkedList<String>();
			args.add(strServiceInformation);
			try {
				objConsumerBean.prcessRequest(args);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	
	/**
     * 向客户端写数据
     * @param data byte[]　待回应数据
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
     * 读取客户端发出请求数据
     * @param sc 套接通道
     */
    private static int BUFFER_SIZE = 1024;
    public static boolean readRequest(SocketChannel sc, IOSession request) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
        int off = 0;
        int r = 0;
        byte[] data = new byte[BUFFER_SIZE * 10];
        buffer.clear();
        r = sc.read(buffer);
        // 如果读取到的数据为-1长度说明，客户端试图关闭，那么我们也关闭
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
            //读到了末尾，退出
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
     * 处理连接数据读取
     * @param key SelectionKey
     * @throws IOException 
     */
    public boolean read(SelectionKey key) throws IOException {
        // 读取客户端数据
        SocketChannel sc = (SocketChannel) key.channel();
        IOSession request = (IOSession)key.attachment();
        return readRequest(sc, request);
    }
    
    /**
     * 数组扩容
     * @param src byte[] 源数组数据
     * @param size int 扩容的增加量
     * @return byte[] 扩容后的数组
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
