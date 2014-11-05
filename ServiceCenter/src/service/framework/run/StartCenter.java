package service.framework.run;
import java.util.LinkedList;
import java.util.List;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import service.framework.io.fire.MasterHandler;
import service.framework.io.handlers.ReadWriteHandler;
import service.framework.io.handlers.Handler;
import service.framework.io.handlers.ServiceRegisterHandler;
import service.framework.io.master.ServiceBootStrap;
import service.framework.io.server.Server;


/**
 * <p>Title: ∆Ù∂Ø¿‡</p>
 * @author starboy
 * @version 1.0
 */

public class StartCenter {

    public static void main(String[] args) {
        try {
            ApplicationContext applicationContext = new ClassPathXmlApplicationContext("ServerServiceCenterConfig.xml");
            Server objServer = (Server)applicationContext.getBean("defaultServer");
            List<Handler> eventConsumerList = new LinkedList<Handler>();
            eventConsumerList.add(new ReadWriteHandler(applicationContext));
    		//eventConsumerList.add(new ServiceRegisterEventConsumer(applicationContext,  objServer.getServiceEventMulticaster()));
     		MasterHandler objMasterHandler = new MasterHandler(30, eventConsumerList);
            new ServiceBootStrap(objServer, objMasterHandler).start();
        }
        catch (Exception e) {
            System.out.println("Server error: " + e.getMessage());
            System.exit(-1);
        }
    }
}
