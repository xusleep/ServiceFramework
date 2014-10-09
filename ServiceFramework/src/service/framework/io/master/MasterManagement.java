package service.framework.io.master;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.context.ApplicationContext;

import service.framework.io.consumer.EventConsumer;
import service.framework.io.event.ServiceEvent;
import service.framework.io.server.Server;

public class MasterManagement {
	private final Server objServer;
	private final MasterHandler objMasterHandler;

	public MasterManagement(Server objServer, MasterHandler objMasterHandler) throws Exception{
		this.objServer = objServer;
		this.objMasterHandler = objMasterHandler;
	}
	
	public void start(){
        try {
        	
        	//�������ݷ����߳�
			new Thread(objServer).start();
			//�����¼�����ַ��߳�, ��������ַ����̳߳أ����̳߳��������
        	objMasterHandler.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void stop(){
		
	}
}
