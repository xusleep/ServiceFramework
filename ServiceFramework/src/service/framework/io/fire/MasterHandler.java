package service.framework.io.fire;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import service.framework.io.event.ServiceEvent;
import service.framework.io.handlers.Handler;

/**
 * ��listener�У�����շ�����¼��������������processRequest���������¼����뵽������
 * �¼����У����ҵ���ע���handler�������¼��Ĵ���
 * @author zhonxu
 *
 */
public class MasterHandler extends Thread {
	public static BlockingQueue<ServiceEvent> pool = new LinkedBlockingQueue<ServiceEvent>();
	private final ExecutorService objExecutorService;
	private final List<Handler> eventHandlerList;
	
	public MasterHandler(int taskThreadPootSize, List<Handler> eventHandlerList){
		this.objExecutorService = Executors.newFixedThreadPool(taskThreadPootSize);
		this.eventHandlerList = eventHandlerList;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
        while (true) {
            try 
            {
            	final ServiceEvent event = pool.take();
            	// ���¼������������̳߳�ִ�У������߼�����������consumer�������
            	handleEvent(event);
            }
            catch (Exception e) {
            	e.printStackTrace();
                continue;
            }
        }
	}

	
	
	/**
	 * ע��˷������ѵĶ��У���ʼ
	 * @param event
	 */
	public void handleEvent(final ServiceEvent event){
		this.objExecutorService.execute(new Runnable(){
			@Override
			public void run() {
            	try {
            		for(Handler handler : eventHandlerList)
            			handler.handleRequest(null, event);
				} catch (IOException e) {
					e.printStackTrace();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
    	});
	}
}