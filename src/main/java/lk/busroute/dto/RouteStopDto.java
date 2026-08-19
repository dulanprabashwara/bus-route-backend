package lk.busroute.dto;

import lk.busroute.domain.RouteStop;

public class RouteStopDto {
    private Integer stopSequence;
    private Integer fareStage;
    private Double distanceFromOriginKm;
    private Boolean pickupAllowed;
    private Boolean dropoffAllowed;
    private StopDto stop;

    public RouteStopDto() {}

    public static RouteStopDto fromEntity(RouteStop rs) {
        if (rs == null) return null;
        RouteStopDto dto = new RouteStopDto();
        dto.setStopSequence(rs.getStopSequence());
        dto.setFareStage(rs.getFareStage());
        dto.setDistanceFromOriginKm(rs.getDistanceFromOriginKm());
        dto.setPickupAllowed(rs.getPickupAllowed());
        dto.setDropoffAllowed(rs.getDropoffAllowed());
        dto.setStop(StopDto.fromEntity(rs.getStop()));
        return dto;
    }

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

    public StopDto getStop() { return stop; }
    public void setStop(StopDto stop) { this.stop = stop; }
}
