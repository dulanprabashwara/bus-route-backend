package lk.busroute.service;

import lk.busroute.domain.Route;
import lk.busroute.domain.RoutePattern;
import lk.busroute.domain.RouteStop;
import lk.busroute.dto.RouteDetailDto;
import lk.busroute.dto.RouteDto;
import lk.busroute.dto.RouteStopDto;
import lk.busroute.exception.ResourceNotFoundException;
import lk.busroute.repository.RoutePatternRepository;
import lk.busroute.repository.RouteRepository;
import lk.busroute.repository.RouteStopRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class RouteService {

    private final RouteRepository routeRepository;
    private final RoutePatternRepository routePatternRepository;
    private final RouteStopRepository routeStopRepository;

    public RouteService(RouteRepository routeRepository,
                        RoutePatternRepository routePatternRepository,
                        RouteStopRepository routeStopRepository) {
        this.routeRepository = routeRepository;
        this.routePatternRepository = routePatternRepository;
        this.routeStopRepository = routeStopRepository;
    }

    public List<RouteDto> getAllRoutes(String routeNumber, String serviceType) {
        String rNum = (routeNumber != null && !routeNumber.trim().isEmpty()) ? routeNumber.trim() : null;
        String sType = (serviceType != null && !serviceType.trim().isEmpty()) ? serviceType.trim() : null;

        List<Route> routes = routeRepository.findRoutableRoutes(rNum, sType);
        return routes.stream()
                .map(RouteDto::fromEntity)
                .collect(Collectors.toList());
    }

    public RouteDto getRouteById(Long id) {
        Route route = routeRepository.findRoutableById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ROUTE_NOT_FOUND", "Route with ID " + id + " was not found."));
        return RouteDto.fromEntity(route);
    }

    public RouteDetailDto getRouteDetail(Long routeId) {
        Route route = routeRepository.findRoutableById(routeId)
                .orElseThrow(() -> new ResourceNotFoundException("ROUTE_NOT_FOUND", "Route with ID " + routeId + " was not found."));

        List<RoutePattern> patterns = routePatternRepository.findActiveByRouteId(routeId);
        List<RouteStopDto> stopDtos = new ArrayList<>();
        List<String> serviceTypes = new ArrayList<>();

        if (!patterns.isEmpty()) {
            RoutePattern primaryPattern = patterns.get(0);
            List<RouteStop> routeStops = routeStopRepository.findByRoutePatternIdOrderByStopSequenceAsc(primaryPattern.getId());
            stopDtos = routeStops.stream()
                    .map(RouteStopDto::fromEntity)
                    .collect(Collectors.toList());
        }

        serviceTypes.add(route.getServiceType());

        return new RouteDetailDto(
                RouteDto.fromEntity(route),
                stopDtos,
                stopDtos.size(),
                serviceTypes
        );
    }
}
