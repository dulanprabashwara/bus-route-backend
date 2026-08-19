package lk.busroute.controller;

import lk.busroute.dto.RouteDetailDto;
import lk.busroute.dto.RouteDto;
import lk.busroute.dto.RouteStopDto;
import lk.busroute.service.RouteService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/routes")
public class RouteController {

    private final RouteService routeService;

    public RouteController(RouteService routeService) {
        this.routeService = routeService;
    }

    @GetMapping
    public ResponseEntity<List<RouteDto>> getRoutes(
            @RequestParam(name = "routeNumber", required = false) String routeNumber,
            @RequestParam(name = "serviceType", required = false) String serviceType) {
        List<RouteDto> routes = routeService.getAllRoutes(routeNumber, serviceType);
        return ResponseEntity.ok(routes);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RouteDetailDto> getRouteById(@PathVariable("id") Long id) {
        RouteDetailDto detail = routeService.getRouteDetail(id);
        return ResponseEntity.ok(detail);
    }

    @GetMapping("/{id}/stops")
    public ResponseEntity<List<RouteStopDto>> getRouteStops(@PathVariable("id") Long id) {
        RouteDetailDto detail = routeService.getRouteDetail(id);
        return ResponseEntity.ok(detail.getStops());
    }
}
