package lk.busroute.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class DeparturesResponseDto {
    private StopDto stop;
    private LocalDate requestedDate;
    private LocalTime requestedTime;
    private List<DepartureDto> departures;
    private Integer totalCount;

    public DeparturesResponseDto() {}

    public DeparturesResponseDto(StopDto stop, LocalDate requestedDate, LocalTime requestedTime, List<DepartureDto> departures) {
        this.stop = stop;
        this.requestedDate = requestedDate;
        this.requestedTime = requestedTime;
        this.departures = departures;
        this.totalCount = departures != null ? departures.size() : 0;
    }

    public StopDto getStop() { return stop; }
    public void setStop(StopDto stop) { this.stop = stop; }

    public LocalDate getRequestedDate() { return requestedDate; }
    public void setRequestedDate(LocalDate requestedDate) { this.requestedDate = requestedDate; }

    public LocalTime getRequestedTime() { return requestedTime; }
    public void setRequestedTime(LocalTime requestedTime) { this.requestedTime = requestedTime; }

    public List<DepartureDto> getDepartures() { return departures; }
    public void setDepartures(List<DepartureDto> departures) {
        this.departures = departures;
        this.totalCount = departures != null ? departures.size() : 0;
    }

    public Integer getTotalCount() { return totalCount; }
    public void setTotalCount(Integer totalCount) { this.totalCount = totalCount; }
}
