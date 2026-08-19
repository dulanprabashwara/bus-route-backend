package lk.busroute.dto;

import java.math.BigDecimal;
import java.time.LocalTime;

public class JourneyLegDto {
    private Integer legIndex;
    private Long routeId;
    private String routeNumber;
    private String routeName;
    private String serviceType;
    private Long tripId;
    private StopDto boardingStop;
    private StopDto dropOffStop;
    private LocalTime departureTime;
    private LocalTime arrivalTime;
    private Long durationMinutes;
    private BigDecimal fare;
    private String fareType;   // EXACT_POINT_TO_POINT, FULL_ENDPOINT_ONLY, FARE_UNAVAILABLE
    private String fareStatus; // EXACT, ENDPOINT_ONLY, UNAVAILABLE

    public JourneyLegDto() {}

    public Integer getLegIndex() { return legIndex; }
    public void setLegIndex(Integer legIndex) { this.legIndex = legIndex; }

    public Long getRouteId() { return routeId; }
    public void setRouteId(Long routeId) { this.routeId = routeId; }

    public String getRouteNumber() { return routeNumber; }
    public void setRouteNumber(String routeNumber) { this.routeNumber = routeNumber; }

    public String getRouteName() { return routeName; }
    public void setRouteName(String routeName) { this.routeName = routeName; }

    public String getServiceType() { return serviceType; }
    public void setServiceType(String serviceType) { this.serviceType = serviceType; }

    public Long getTripId() { return tripId; }
    public void setTripId(Long tripId) { this.tripId = tripId; }

    public StopDto getBoardingStop() { return boardingStop; }
    public void setBoardingStop(StopDto boardingStop) { this.boardingStop = boardingStop; }

    public StopDto getDropOffStop() { return dropOffStop; }
    public void setDropOffStop(StopDto dropOffStop) { this.dropOffStop = dropOffStop; }

    public LocalTime getDepartureTime() { return departureTime; }
    public void setDepartureTime(LocalTime departureTime) { this.departureTime = departureTime; }

    public LocalTime getArrivalTime() { return arrivalTime; }
    public void setArrivalTime(LocalTime arrivalTime) { this.arrivalTime = arrivalTime; }

    public Long getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(Long durationMinutes) { this.durationMinutes = durationMinutes; }

    public BigDecimal getFare() { return fare; }
    public void setFare(BigDecimal fare) { this.fare = fare; }

    public String getFareType() { return fareType; }
    public void setFareType(String fareType) { this.fareType = fareType; }

    public String getFareStatus() { return fareStatus; }
    public void setFareStatus(String fareStatus) { this.fareStatus = fareStatus; }
}
