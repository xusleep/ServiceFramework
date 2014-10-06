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
            	// ���¼������������̳߳�ִ�У������߼�����������consumer�������
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
	 * ����ͻ�����,�����û��������,�����Ѷ����е��߳̽��д���
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
