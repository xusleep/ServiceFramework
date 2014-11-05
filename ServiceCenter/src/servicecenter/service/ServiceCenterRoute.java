package servicecenter.service;

import java.util.List;

import service.framework.route.Route;

public class ServiceCenterRoute implements Route {
	private List<ServiceInformation> serviceList;
	
	

	public List<ServiceInformation> getServiceList() {
		return serviceList;
	}



	public void setServiceList(List<ServiceInformation> serviceList) {
		this.serviceList = serviceList;
	}



	@Override
	public ServiceInformation chooseRoute(String serviceName) {
		// TODO Auto-generated method stub
		return this.serviceList.get(0);
	}

}
