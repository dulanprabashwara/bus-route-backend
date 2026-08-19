package lk.busroute.dto;

import lk.busroute.domain.Route;

public class RouteDto {
    private Long id;
    private String routeNumber;
    private String name;
    private StopDto originStop;
    private StopDto destinationStop;
    private String serviceType;
    private String province;
    private Boolean interProvincial;

    public RouteDto() {}

    public static RouteDto fromEntity(Route route) {
        if (route == null) return null;
        RouteDto dto = new RouteDto();
        dto.setId(route.getId());
        dto.setRouteNumber(route.getRouteNumber());
        dto.setName(route.getName());
        dto.setOriginStop(StopDto.fromEntity(route.getOriginStop()));
        dto.setDestinationStop(StopDto.fromEntity(route.getDestinationStop()));
        dto.setServiceType(route.getServiceType());
        dto.setProvince(route.getProvince());
        dto.setInterProvincial(route.getInterProvincial());
        return dto;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getRouteNumber() { return routeNumber; }
    public void setRouteNumber(String routeNumber) { this.routeNumber = routeNumber; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public StopDto getOriginStop() { return originStop; }
    public void setOriginStop(StopDto originStop) { this.originStop = originStop; }

    public StopDto getDestinationStop() { return destinationStop; }
    public void setDestinationStop(StopDto destinationStop) { this.destinationStop = destinationStop; }

    public String getServiceType() { return serviceType; }
    public void setServiceType(String serviceType) { this.serviceType = serviceType; }

    public String getProvince() { return province; }
    public void setProvince(String province) { this.province = province; }

    public Boolean getInterProvincial() { return interProvincial; }
    public void setInterProvincial(Boolean interProvincial) { this.interProvincial = interProvincial; }
}
