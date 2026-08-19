package lk.busroute.controller;

import lk.busroute.dto.DeparturesResponseDto;
import lk.busroute.service.DepartureService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;

@RestController
@RequestMapping("/api/v1/departures")
public class DepartureController {

    private final DepartureService departureService;

    public DepartureController(DepartureService departureService) {
        this.departureService = departureService;
    }

    @GetMapping
    public ResponseEntity<DeparturesResponseDto> getDepartures(
            @RequestParam("stopId") Long stopId,
            @RequestParam(name = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(name = "time", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime time) {
        DeparturesResponseDto departures = departureService.getDepartures(stopId, date, time);
        return ResponseEntity.ok(departures);
    }
}
