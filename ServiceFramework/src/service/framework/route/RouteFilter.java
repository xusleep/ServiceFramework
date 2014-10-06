package service.framework.route;

import java.util.List;

import servicecenter.service.ServiceInformation;

public interface RouteFilter {
	public List<ServiceInformation> filter(List<ServiceInformation> serviceList);
}
