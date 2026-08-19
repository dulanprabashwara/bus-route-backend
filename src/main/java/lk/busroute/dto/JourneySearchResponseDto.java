package lk.busroute.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class JourneySearchResponseDto {
    private StopDto fromStop;
    private StopDto toStop;
    private LocalDate requestedDate;
    private LocalTime requestedTime;
    private List<JourneyDto> journeys;
    private Integer totalResults;
    private String dataNote;

    public JourneySearchResponseDto() {}

    public JourneySearchResponseDto(StopDto fromStop, StopDto toStop, LocalDate requestedDate, LocalTime requestedTime, List<JourneyDto> journeys) {
        this.fromStop = fromStop;
        this.toStop = toStop;
        this.requestedDate = requestedDate;
        this.requestedTime = requestedTime;
        this.journeys = journeys;
        this.totalResults = journeys != null ? journeys.size() : 0;
        this.dataNote = "Pilot dataset (29 routable routes). Fares and timetables are derived from official NTC documents (Effective July 2026).";
    }

    public StopDto getFromStop() { return fromStop; }
    public void setFromStop(StopDto fromStop) { this.fromStop = fromStop; }

    public StopDto getToStop() { return toStop; }
    public void setToStop(StopDto toStop) { this.toStop = toStop; }

    public LocalDate getRequestedDate() { return requestedDate; }
    public void setRequestedDate(LocalDate requestedDate) { this.requestedDate = requestedDate; }

    public LocalTime getRequestedTime() { return requestedTime; }
    public void setRequestedTime(LocalTime requestedTime) { this.requestedTime = requestedTime; }

    public List<JourneyDto> getJourneys() { return journeys; }
    public void setJourneys(List<JourneyDto> journeys) {
        this.journeys = journeys;
        this.totalResults = journeys != null ? journeys.size() : 0;
    }

    public Integer getTotalResults() { return totalResults; }
    public void setTotalResults(Integer totalResults) { this.totalResults = totalResults; }

    public String getDataNote() { return dataNote; }
    public void setDataNote(String dataNote) { this.dataNote = dataNote; }
}
