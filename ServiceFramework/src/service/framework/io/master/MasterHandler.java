package service.framework.io.master;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import service.framework.io.event.ServiceEvent;

public class MasterHandler extends Thread {
	public static BlockingQueue<ServiceEvent> pool = new LinkedBlockingQueue<ServiceEvent>();
	private final MasterManagement objMasterManagement;
	
	public MasterHandler(MasterManagement masterManagement){
		this.objMasterManagement = masterManagement;
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
        while (true) {
            try 
            {
            	final ServiceEvent event = pool.take();
            	// 将事件处理任务交由线程池执行，处理逻辑独立处理在consumer里面完成
            	getMasterManagement().consumeEvent(event);
            }
            catch (Exception e) {
            	e.printStackTrace();
                continue;
            }
        }
	}
	
	 public MasterManagement getMasterManagement() {
		return objMasterManagement;
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
}
