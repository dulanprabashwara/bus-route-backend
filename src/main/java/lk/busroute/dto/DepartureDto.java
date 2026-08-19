package lk.busroute.dto;

import java.time.LocalTime;

public class DepartureDto {
    private Long routeId;
    private String routeNumber;
    private String routeName;
    private String serviceType;
    private StopDto boardingStop;
    private String destination;
    private LocalTime scheduledDeparture;
    private String tripHeadsign;
    private Long tripId;

    public DepartureDto() {}

    public Long getRouteId() { return routeId; }
    public void setRouteId(Long routeId) { this.routeId = routeId; }

    public String getRouteNumber() { return routeNumber; }
    public void setRouteNumber(String routeNumber) { this.routeNumber = routeNumber; }

    public String getRouteName() { return routeName; }
    public void setRouteName(String routeName) { this.routeName = routeName; }

    public String getServiceType() { return serviceType; }
    public void setServiceType(String serviceType) { this.serviceType = serviceType; }

    public StopDto getBoardingStop() { return boardingStop; }
    public void setBoardingStop(StopDto boardingStop) { this.boardingStop = boardingStop; }

    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }

    public LocalTime getScheduledDeparture() { return scheduledDeparture; }
    public void setScheduledDeparture(LocalTime scheduledDeparture) { this.scheduledDeparture = scheduledDeparture; }

    public String getTripHeadsign() { return tripHeadsign; }
    public void setTripHeadsign(String tripHeadsign) { this.tripHeadsign = tripHeadsign; }

    public Long getTripId() { return tripId; }
    public void setTripId(Long tripId) { this.tripId = tripId; }
}
