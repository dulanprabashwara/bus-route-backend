package lk.busroute.dto;

import java.util.List;

public class RouteDetailDto {
    private RouteDto route;
    private List<RouteStopDto> stops;
    private Integer totalStops;
    private List<String> availableServiceTypes;

    public RouteDetailDto() {}

    public RouteDetailDto(RouteDto route, List<RouteStopDto> stops, Integer totalStops, List<String> availableServiceTypes) {
        this.route = route;
        this.stops = stops;
        this.totalStops = totalStops;
        this.availableServiceTypes = availableServiceTypes;
    }

    public RouteDto getRoute() { return route; }
    public void setRoute(RouteDto route) { this.route = route; }

    public List<RouteStopDto> getStops() { return stops; }
    public void setStops(List<RouteStopDto> stops) { this.stops = stops; }

    public Integer getTotalStops() { return totalStops; }
    public void setTotalStops(Integer totalStops) { this.totalStops = totalStops; }

    public List<String> getAvailableServiceTypes() { return availableServiceTypes; }
    public void setAvailableServiceTypes(List<String> availableServiceTypes) { this.availableServiceTypes = availableServiceTypes; }
}
