package lk.busroute.controller;

import lk.busroute.dto.StopDto;
import lk.busroute.service.StopService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/stops")
public class StopController {

    private final StopService stopService;

    public StopController(StopService stopService) {
        this.stopService = stopService;
    }

    @GetMapping("/search")
    public ResponseEntity<List<StopDto>> searchStops(@RequestParam(name = "q", defaultValue = "") String query) {
        List<StopDto> results = stopService.searchStops(query);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/{id}")
    public ResponseEntity<StopDto> getStopById(@PathVariable("id") Long id) {
        StopDto stop = stopService.getStopById(id);
        return ResponseEntity.ok(stop);
    }
}
