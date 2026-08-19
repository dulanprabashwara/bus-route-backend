package lk.busroute.domain;

import jakarta.persistence.*;
import java.time.ZonedDateTime;

@Entity
@Table(name = "route_patterns")
public class RoutePattern {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "route_id", nullable = false)
    private Route route;

    @Column(name = "direction", nullable = false)
    private String direction = "OUTBOUND";

    @Column(name = "pattern_name")
    private String patternName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "origin_stop_id")
    private Stop originStop;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destination_stop_id")
    private Stop destinationStop;

    @Column(name = "active", nullable = false)
    private Boolean active = true;

    @Column(name = "created_at", insertable = false, updatable = false)
    private ZonedDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private ZonedDateTime updatedAt;

    public RoutePattern() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Route getRoute() { return route; }
    public void setRoute(Route route) { this.route = route; }

    public String getDirection() { return direction; }
    public void setDirection(String direction) { this.direction = direction; }

    public String getPatternName() { return patternName; }
    public void setPatternName(String patternName) { this.patternName = patternName; }

    public Stop getOriginStop() { return originStop; }
    public void setOriginStop(Stop originStop) { this.originStop = originStop; }

    public Stop getDestinationStop() { return destinationStop; }
    public void setDestinationStop(Stop destinationStop) { this.destinationStop = destinationStop; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
}
