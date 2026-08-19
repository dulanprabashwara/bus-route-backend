package lk.busroute.domain;

import jakarta.persistence.*;
import java.time.ZonedDateTime;

@Entity
@Table(name = "routes")
public class Route {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "route_number", nullable = false)
    private String routeNumber;

    @Column(name = "name")
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "origin_stop_id")
    private Stop originStop;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destination_stop_id")
    private Stop destinationStop;

    @Column(name = "service_type", nullable = false)
    private String serviceType = "NORMAL";

    @Column(name = "province")
    private String province;

    @Column(name = "inter_provincial")
    private Boolean interProvincial;

    @Column(name = "active", nullable = false)
    private Boolean active = true;

    @Column(name = "created_at", insertable = false, updatable = false)
    private ZonedDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private ZonedDateTime updatedAt;

    public Route() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getRouteNumber() { return routeNumber; }
    public void setRouteNumber(String routeNumber) { this.routeNumber = routeNumber; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Stop getOriginStop() { return originStop; }
    public void setOriginStop(Stop originStop) { this.originStop = originStop; }

    public Stop getDestinationStop() { return destinationStop; }
    public void setDestinationStop(Stop destinationStop) { this.destinationStop = destinationStop; }

    public String getServiceType() { return serviceType; }
    public void setServiceType(String serviceType) { this.serviceType = serviceType; }

    public String getProvince() { return province; }
    public void setProvince(String province) { this.province = province; }

    public Boolean getInterProvincial() { return interProvincial; }
    public void setInterProvincial(Boolean interProvincial) { this.interProvincial = interProvincial; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
}
