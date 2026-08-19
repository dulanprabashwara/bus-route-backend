package lk.busroute.service;

import lk.busroute.domain.Route;
import lk.busroute.domain.RouteFare;
import lk.busroute.dto.FareResultDto;
import lk.busroute.repository.RouteFareRepository;
import lk.busroute.repository.RouteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class FareService {

    private final RouteFareRepository routeFareRepository;
    private final RouteRepository routeRepository;

    public FareService(RouteFareRepository routeFareRepository, RouteRepository routeRepository) {
        this.routeFareRepository = routeFareRepository;
        this.routeRepository = routeRepository;
    }

    public FareResultDto calculateFare(Long routeId, Long patternId, Long fromStopId, Long toStopId, String serviceType) {
        if (fromStopId == null || toStopId == null) {
            return FareResultDto.unavailable();
        }

        // Priority 1: EXACT_POINT_TO_POINT
        List<RouteFare> exactFares = routeFareRepository.findExactFares(fromStopId, toStopId, serviceType);
        if (!exactFares.isEmpty()) {
            RouteFare exact = exactFares.get(0);
            return new FareResultDto(
                    exact.getAmountLkr(),
                    "EXACT_POINT_TO_POINT",
                    "EXACT",
                    "Official NTC Verified Point-to-Point Fare Matrix"
            );
        }

        // Priority 2: FULL_ENDPOINT_ONLY (Strict validation: MUST be exact full route endpoints)
        if (routeId != null) {
            Route route = routeRepository.findById(routeId).orElse(null);
            if (route != null && route.getOriginStop() != null && route.getDestinationStop() != null) {
                Long routeOriginId = route.getOriginStop().getId();
                Long routeDestId = route.getDestinationStop().getId();

                boolean isFullRouteJourney = (fromStopId.equals(routeOriginId) && toStopId.equals(routeDestId))
                        || (fromStopId.equals(routeDestId) && toStopId.equals(routeOriginId));

                if (isFullRouteJourney) {
                    List<RouteFare> endpointFares = routeFareRepository.findEndpointFares(fromStopId, toStopId, serviceType);
                    if (endpointFares.isEmpty()) {
                        endpointFares = routeFareRepository.findEndpointFares(fromStopId, toStopId, null);
                    }

                    if (!endpointFares.isEmpty()) {
                        RouteFare ep = endpointFares.get(0);
                        return new FareResultDto(
                                ep.getAmountLkr(),
                                "FULL_ENDPOINT_ONLY",
                                "ENDPOINT_ONLY",
                                "Official NTC Inter-Provincial Endpoint Document"
                        );
                    }
                }
            }
        }

        // Priority 3: FARE_UNAVAILABLE
        return FareResultDto.unavailable();
    }
}
