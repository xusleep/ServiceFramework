package service.framework.io.client.comsume;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;

import service.framework.route.Route;
import service.framework.route.RouteFilter;
import service.framework.serialization.SerializeUtils;
import servicecenter.service.ServiceInformation;

public class ClientRoute implements Route {
	private List<RouteFilter> filters;
	private ConsumerBean serviceCenterConsumerBean;
	
	public ClientRoute(){
		
	}
	
	public List<RouteFilter> getFilters() {
		return filters;
	}

	public void setFilters(List<RouteFilter> filters) {
		this.filters = filters;
	}

	public ConsumerBean getServiceCenterConsumerBean() {
		return serviceCenterConsumerBean;
	}

	public void setServiceCenterConsumerBean(ConsumerBean serviceCenterConsumerBean) {
		this.serviceCenterConsumerBean = serviceCenterConsumerBean;
	}

	@Override
	public ServiceInformation chooseRoute(String serviceName) throws IOException, InterruptedException, ExecutionException {
		//首先从cache中取得服务列表，cache中没有的话，再从服务中心获取
		List<String> list = new LinkedList<String>();
		list.add(serviceName);
		long id = this.getServiceCenterConsumerBean().prcessRequest(list);
		String result = this.getServiceCenterConsumerBean().getResult(id);
		List<ServiceInformation> serviceList = SerializeUtils.deserializeServiceInformationList(result);
		for(RouteFilter filter : filters){
			serviceList = filter.filter(serviceList);
		}
		if(serviceList.size() == 0)
			return null;
		Random r = new Random();
		ServiceInformation service = serviceList.get(r.nextInt(serviceList.size()));
		while(service == null)
		{
			service = serviceList.get(r.nextInt(serviceList.size()));
		}
		return service;
	}
}
