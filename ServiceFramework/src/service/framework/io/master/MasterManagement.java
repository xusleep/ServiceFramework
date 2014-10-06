package service.framework.io.master;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.context.ApplicationContext;

import service.framework.io.event.ServiceEvent;
import service.framework.io.server.Server;

public class MasterManagement {
	private Server objServer;
	private final ExecutorService objExecutorService;
	private final List<EventConsumer> eventConsumerList;
	
	public List<EventConsumer> getEventConsumerList() {
		return eventConsumerList;
	}

	public MasterManagement(Server objServer, int taskThreadPootSize, List<EventConsumer> eventConsumerList) throws Exception{
		this.objServer = objServer;
		this.objExecutorService = Executors.newFixedThreadPool(taskThreadPootSize);
		this.eventConsumerList = eventConsumerList;
}
	
	public void start(){
        try {
        	
        	//启动数据服务线程
			new Thread(objServer).start();
			//启动事件处理分发线程, 即将任务分发到线程池，由线程池完成任务
        	MasterHandler objMasterHandler = new MasterHandler(this);
        	objMasterHandler.start();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void stop(){
		
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
