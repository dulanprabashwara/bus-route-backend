package lk.busroute.controller;

import lk.busroute.dto.JourneySearchResponseDto;
import lk.busroute.service.JourneyPlannerService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;

@RestController
@RequestMapping("/api/v1/journeys")
public class JourneyController {

    private final JourneyPlannerService journeyPlannerService;

    public JourneyController(JourneyPlannerService journeyPlannerService) {
        this.journeyPlannerService = journeyPlannerService;
    }

    @GetMapping("/search")
    public ResponseEntity<JourneySearchResponseDto> searchJourneys(
            @RequestParam("fromStopId") Long fromStopId,
            @RequestParam("toStopId") Long toStopId,
            @RequestParam(name = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(name = "time", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime time) {
        JourneySearchResponseDto response = journeyPlannerService.searchJourneys(fromStopId, toStopId, date, time);
        return ResponseEntity.ok(response);
    }
}
