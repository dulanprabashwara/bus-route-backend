package lk.busroute.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.ZonedDateTime;

@Entity
@Table(name = "route_fares")
public class RouteFare {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "route_pattern_id")
    private RoutePattern routePattern;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "route_id")
    private Route route;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "from_stop_id", nullable = false)
    private Stop fromStop;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "to_stop_id", nullable = false)
    private Stop toStop;

    @Column(name = "service_type", nullable = false)
    private String serviceType = "NORMAL";

    @Column(name = "amount_lkr", nullable = false)
    private BigDecimal amountLkr;

    @Column(name = "fare_type", nullable = false)
    private String fareType = "EXACT_POINT_TO_POINT";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fare_version_id")
    private FareVersion fareVersion;

    @Column(name = "created_at", insertable = false, updatable = false)
    private ZonedDateTime createdAt;

    public RouteFare() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public RoutePattern getRoutePattern() { return routePattern; }
    public void setRoutePattern(RoutePattern routePattern) { this.routePattern = routePattern; }

    public Route getRoute() { return route; }
    public void setRoute(Route route) { this.route = route; }

    public Stop getFromStop() { return fromStop; }
    public void setFromStop(Stop fromStop) { this.fromStop = fromStop; }

    public Stop getToStop() { return toStop; }
    public void setToStop(Stop toStop) { this.toStop = toStop; }

    public String getServiceType() { return serviceType; }
    public void setServiceType(String serviceType) { this.serviceType = serviceType; }

    public BigDecimal getAmountLkr() { return amountLkr; }
    public void setAmountLkr(BigDecimal amountLkr) { this.amountLkr = amountLkr; }

    public String getFareType() { return fareType; }
    public void setFareType(String fareType) { this.fareType = fareType; }

    public FareVersion getFareVersion() { return fareVersion; }
    public void setFareVersion(FareVersion fareVersion) { this.fareVersion = fareVersion; }
}
