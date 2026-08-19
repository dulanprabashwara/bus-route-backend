package lk.busroute.dto;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class JourneyDto {
    private String journeyId;
    private LocalTime departureTime;
    private LocalTime arrivalTime;
    private Long durationMinutes;
    private Integer transferCount;
    private BigDecimal totalFare;
    private String fareStatus; // COMPLETE, PARTIAL, UNAVAILABLE
    private List<String> labels = new ArrayList<>();
    private List<JourneyLegDto> legs = new ArrayList<>();

    public JourneyDto() {}

    public String getJourneyId() { return journeyId; }
    public void setJourneyId(String journeyId) { this.journeyId = journeyId; }

    public LocalTime getDepartureTime() { return departureTime; }
    public void setDepartureTime(LocalTime departureTime) { this.departureTime = departureTime; }

    public LocalTime getArrivalTime() { return arrivalTime; }
    public void setArrivalTime(LocalTime arrivalTime) { this.arrivalTime = arrivalTime; }

    public Long getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(Long durationMinutes) { this.durationMinutes = durationMinutes; }

    public Integer getTransferCount() { return transferCount; }
    public void setTransferCount(Integer transferCount) { this.transferCount = transferCount; }

    public BigDecimal getTotalFare() { return totalFare; }
    public void setTotalFare(BigDecimal totalFare) { this.totalFare = totalFare; }

    public String getFareStatus() { return fareStatus; }
    public void setFareStatus(String fareStatus) { this.fareStatus = fareStatus; }

    public List<String> getLabels() { return labels; }
    public void setLabels(List<String> labels) { this.labels = labels; }

    public List<JourneyLegDto> getLegs() { return legs; }
    public void setLegs(List<JourneyLegDto> legs) { this.legs = legs; }
}
