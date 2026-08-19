package lk.busroute.service;

import lk.busroute.domain.Stop;
import lk.busroute.dto.StopDto;
import lk.busroute.exception.ResourceNotFoundException;
import lk.busroute.repository.StopRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

class StopServiceTest {

    private StopRepository stopRepository;
    private StopService stopService;

    @BeforeEach
    void setUp() {
        stopRepository = Mockito.mock(StopRepository.class);
        stopService = new StopService(stopRepository);
    }

    @Test
    void searchStops_returnsMatchingStops() {
        Stop s = new Stop();
        s.setId(1L);
        s.setNameEn("Kandy");
        s.setNormalizedName("kandy");

        when(stopRepository.searchStops("kandy")).thenReturn(List.of(s));

        List<StopDto> results = stopService.searchStops("kandy");
        assertEquals(1, results.size());
        assertEquals("Kandy", results.get(0).getName());
    }

    @Test
    void searchStops_emptyQuery_returnsEmptyList() {
        List<StopDto> results = stopService.searchStops("  ");
        assertTrue(results.isEmpty());
    }

    @Test
    void getStopById_notFound_throwsException() {
        when(stopRepository.findRoutableById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> stopService.getStopById(999L));
    }
}
