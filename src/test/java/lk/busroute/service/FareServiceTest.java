package lk.busroute.service;

import lk.busroute.domain.Route;
import lk.busroute.domain.RouteFare;
import lk.busroute.domain.Stop;
import lk.busroute.dto.FareResultDto;
import lk.busroute.repository.RouteFareRepository;
import lk.busroute.repository.RouteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class FareServiceTest {

    private RouteFareRepository routeFareRepository;
    private RouteRepository routeRepository;
    private FareService fareService;

    @BeforeEach
    void setUp() {
        routeFareRepository = Mockito.mock(RouteFareRepository.class);
        routeRepository = Mockito.mock(RouteRepository.class);
        fareService = new FareService(routeFareRepository, routeRepository);
    }

    @Test
    void calculateFare_exactPointToPoint_returnsExactAmount() {
        RouteFare rf = new RouteFare();
        rf.setAmountLkr(new BigDecimal("795.00"));
        rf.setFareType("EXACT_POINT_TO_POINT");

        when(routeFareRepository.findExactFares(10L, 20L, "NORMAL"))
                .thenReturn(List.of(rf));

        FareResultDto res = fareService.calculateFare(1L, 1L, 10L, 20L, "NORMAL");
        assertEquals("EXACT", res.getStatus());
        assertEquals("EXACT_POINT_TO_POINT", res.getFareType());
        assertEquals(new BigDecimal("795.00"), res.getAmount());
    }

    @Test
    void calculateFare_intermediateStop_endpointOnlyFare_returnsUnavailable() {
        Stop colombo = new Stop(); colombo.setId(1L);
        Stop kandy = new Stop(); kandy.setId(2L);
        Stop mawanella = new Stop(); mawanella.setId(3L);

        Route r = new Route();
        r.setId(101L);
        r.setOriginStop(colombo);
        r.setDestinationStop(kandy);

        when(routeRepository.findById(101L)).thenReturn(Optional.of(r));
        when(routeFareRepository.findExactFares(1L, 3L, "NORMAL")).thenReturn(List.of());

        // Intermediate journey: Colombo -> Mawanella (not full endpoint Colombo -> Kandy)
        FareResultDto res = fareService.calculateFare(101L, 1L, 1L, 3L, "NORMAL");
        assertEquals("UNAVAILABLE", res.getStatus());
        assertEquals("FARE_UNAVAILABLE", res.getFareType());
        assertNull(res.getAmount());
    }

    @Test
    void calculateFare_fullEndpoint_returnsEndpointOnlyFare() {
        Stop colombo = new Stop(); colombo.setId(1L);
        Stop kandy = new Stop(); kandy.setId(2L);

        Route r = new Route();
        r.setId(101L);
        r.setOriginStop(colombo);
        r.setDestinationStop(kandy);

        RouteFare rf = new RouteFare();
        rf.setAmountLkr(new BigDecimal("521.00"));
        rf.setFareType("FULL_ENDPOINT_ONLY");

        when(routeRepository.findById(101L)).thenReturn(Optional.of(r));
        when(routeFareRepository.findExactFares(1L, 2L, "NORMAL")).thenReturn(List.of());
        when(routeFareRepository.findEndpointFares(1L, 2L, "NORMAL")).thenReturn(List.of(rf));

        FareResultDto res = fareService.calculateFare(101L, 1L, 1L, 2L, "NORMAL");
        assertEquals("ENDPOINT_ONLY", res.getStatus());
        assertEquals("FULL_ENDPOINT_ONLY", res.getFareType());
        assertEquals(new BigDecimal("521.00"), res.getAmount());
    }
}
