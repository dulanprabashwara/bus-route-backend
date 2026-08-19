package lk.busroute.domain;

import jakarta.persistence.*;
import java.time.LocalTime;

@Entity
@Table(name = "stop_times")
public class StopTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_id", nullable = false)
    private Trip trip;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "stop_id", nullable = false)
    private Stop stop;

    @Column(name = "stop_sequence", nullable = false)
    private Integer stopSequence;

    @Column(name = "arrival_time")
    private LocalTime arrivalTime;

    @Column(name = "departure_time")
    private LocalTime departureTime;

    @Column(name = "day_offset", nullable = false)
    private Integer dayOffset = 0;

    @Column(name = "time_accuracy", nullable = false)
    private String timeAccuracy = "EXACT";

    public StopTime() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Trip getTrip() { return trip; }
    public void setTrip(Trip trip) { this.trip = trip; }

    public Stop getStop() { return stop; }
    public void setStop(Stop stop) { this.stop = stop; }

    public Integer getStopSequence() { return stopSequence; }
    public void setStopSequence(Integer stopSequence) { this.stopSequence = stopSequence; }

    public LocalTime getArrivalTime() { return arrivalTime; }
    public void setArrivalTime(LocalTime arrivalTime) { this.arrivalTime = arrivalTime; }

    public LocalTime getDepartureTime() { return departureTime; }
    public void setDepartureTime(LocalTime departureTime) { this.departureTime = departureTime; }

    public Integer getDayOffset() { return dayOffset; }
    public void setDayOffset(Integer dayOffset) { this.dayOffset = dayOffset; }

    public String getTimeAccuracy() { return timeAccuracy; }
    public void setTimeAccuracy(String timeAccuracy) { this.timeAccuracy = timeAccuracy; }
}
