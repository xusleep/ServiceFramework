package service.framework.run;
import java.util.LinkedList;
import java.util.List;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import service.framework.io.consumer.DefaultEventConsumer;
import service.framework.io.consumer.EventConsumer;
import service.framework.io.consumer.ServiceRegisterEventConsumer;
import service.framework.io.master.MasterHandler;
import service.framework.io.master.MasterManagement;
import service.framework.io.server.Server;


/**
 * <p>Title: ������</p>
 * @author starboy
 * @version 1.0
 */

public class StartService {

    public static void main(String[] args) {
        try {
            ApplicationContext applicationContext = new ClassPathXmlApplicationContext("ServerServiceConfig.xml");
            Server objServer = (Server)applicationContext.getBean("defaultServer");
            List<EventConsumer> eventConsumerList = new LinkedList<EventConsumer>();
            eventConsumerList.add(new DefaultEventConsumer(applicationContext, objServer.getServiceEventMulticaster()));
    		eventConsumerList.add(new ServiceRegisterEventConsumer(applicationContext,  objServer.getServiceEventMulticaster()));
    		MasterHandler objMasterHandler = new MasterHandler(400, eventConsumerList);
            new MasterManagement(objServer, objMasterHandler).start();
        }
        catch (Exception e) {
            System.out.println("Server error: " + e.getMessage());
            System.exit(-1);
        }
    }
}
