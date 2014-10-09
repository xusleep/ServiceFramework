package service.framework.io.master;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import service.framework.io.consumer.EventConsumer;
import service.framework.io.event.ServiceEvent;

public class MasterHandler extends Thread {
	public static BlockingQueue<ServiceEvent> pool = new LinkedBlockingQueue<ServiceEvent>();
	private final ExecutorService objExecutorService;
	private final List<EventConsumer> eventConsumerList;
	
	public MasterHandler(int taskThreadPootSize, List<EventConsumer> eventConsumerList){
		this.objExecutorService = Executors.newFixedThreadPool(taskThreadPootSize);
		this.eventConsumerList = eventConsumerList;
	}
	
	
	public List<EventConsumer> getEventConsumerList() {
		return eventConsumerList;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
        while (true) {
            try 
            {
            	final ServiceEvent event = pool.take();
            	// 将事件处理任务交由线程池执行，处理逻辑独立处理在consumer里面完成
            	consumeEvent(event);
            }
            catch (Exception e) {
            	e.printStackTrace();
                continue;
            }
        }
	}
	

	/**
	 * 处理客户请求,管理用户的联结池,并唤醒队列中的线程进行处理
	 */
	public static void processRequest(ServiceEvent event) {
		try {
			pool.put(event);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void consumeEvent(final ServiceEvent event){
		this.objExecutorService.execute(new Runnable(){
			@Override
			public void run() {
				
				// TODO Auto-generated method stub
            	try {
            		for(EventConsumer eventConsumer : eventConsumerList)
            			eventConsumer.comsume(event);
				} catch (IOException e) {
					
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
    	});
	}
}
