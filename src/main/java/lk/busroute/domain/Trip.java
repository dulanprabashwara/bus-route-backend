package lk.busroute.domain;

import jakarta.persistence.*;
import java.time.ZonedDateTime;

@Entity
@Table(name = "trips")
public class Trip {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "route_pattern_id", nullable = false)
    private RoutePattern routePattern;

    @Column(name = "running_number")
    private String runningNumber;

    @Column(name = "bus_registration")
    private String busRegistration;

    @Column(name = "ntc_permit_number")
    private String ntcPermitNumber;

    @Column(name = "service_type", nullable = false)
    private String serviceType = "NORMAL";

    @Column(name = "trip_headsign")
    private String tripHeadsign;

    @Column(name = "active", nullable = false)
    private Boolean active = true;

    @Column(name = "created_at", insertable = false, updatable = false)
    private ZonedDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private ZonedDateTime updatedAt;

    public Trip() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public RoutePattern getRoutePattern() { return routePattern; }
    public void setRoutePattern(RoutePattern routePattern) { this.routePattern = routePattern; }

    public String getRunningNumber() { return runningNumber; }
    public void setRunningNumber(String runningNumber) { this.runningNumber = runningNumber; }

    public String getBusRegistration() { return busRegistration; }
    public void setBusRegistration(String busRegistration) { this.busRegistration = busRegistration; }

    public String getNtcPermitNumber() { return ntcPermitNumber; }
    public void setNtcPermitNumber(String ntcPermitNumber) { this.ntcPermitNumber = ntcPermitNumber; }

    public String getServiceType() { return serviceType; }
    public void setServiceType(String serviceType) { this.serviceType = serviceType; }

    public String getTripHeadsign() { return tripHeadsign; }
    public void setTripHeadsign(String tripHeadsign) { this.tripHeadsign = tripHeadsign; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
}
