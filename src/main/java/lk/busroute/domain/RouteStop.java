package lk.busroute.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "route_stops")
public class RouteStop {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "route_pattern_id", nullable = false)
    private RoutePattern routePattern;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "stop_id", nullable = false)
    private Stop stop;

    @Column(name = "stop_sequence", nullable = false)
    private Integer stopSequence;

    @Column(name = "fare_stage")
    private Integer fareStage;

    @Column(name = "distance_from_origin_km")
    private Double distanceFromOriginKm;

    @Column(name = "pickup_allowed", nullable = false)
    private Boolean pickupAllowed = true;

    @Column(name = "dropoff_allowed", nullable = false)
    private Boolean dropoffAllowed = true;

    public RouteStop() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public RoutePattern getRoutePattern() { return routePattern; }
    public void setRoutePattern(RoutePattern routePattern) { this.routePattern = routePattern; }

    public Stop getStop() { return stop; }
    public void setStop(Stop stop) { this.stop = stop; }

    public Integer getStopSequence() { return stopSequence; }
    public void setStopSequence(Integer stopSequence) { this.stopSequence = stopSequence; }

    public Integer getFareStage() { return fareStage; }
    public void setFareStage(Integer fareStage) { this.fareStage = fareStage; }

    public Double getDistanceFromOriginKm() { return distanceFromOriginKm; }
    public void setDistanceFromOriginKm(Double distanceFromOriginKm) { this.distanceFromOriginKm = distanceFromOriginKm; }

    public Boolean getPickupAllowed() { return pickupAllowed; }
    public void setPickupAllowed(Boolean pickupAllowed) { this.pickupAllowed = pickupAllowed; }

    public Boolean getDropoffAllowed() { return dropoffAllowed; }
    public void setDropoffAllowed(Boolean dropoffAllowed) { this.dropoffAllowed = dropoffAllowed; }
}
