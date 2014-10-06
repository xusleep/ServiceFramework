package service.framework.io.client.comsume;

import java.util.List;

import service.framework.route.RouteFilter;
import servicecenter.service.ServiceInformation;

public class ClientRouteFilter implements RouteFilter{

	@Override
	public List<ServiceInformation> filter(List<ServiceInformation> serviceList) {
		// TODO Auto-generated method stub
		System.out.println("pass the client route filter ..");
		return serviceList;
	}

}
