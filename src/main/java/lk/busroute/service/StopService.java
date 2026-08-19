package lk.busroute.service;

import lk.busroute.domain.Stop;
import lk.busroute.dto.StopDto;
import lk.busroute.exception.ResourceNotFoundException;
import lk.busroute.repository.StopRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class StopService {

    private final StopRepository stopRepository;

    public StopService(StopRepository stopRepository) {
        this.stopRepository = stopRepository;
    }

    public List<StopDto> searchStops(String query) {
        if (query == null || query.trim().isEmpty()) {
            return List.of();
        }
        String cleanQuery = query.trim();
        List<Stop> stops = stopRepository.searchStops(cleanQuery);
        return stops.stream()
                .map(StopDto::fromEntity)
                .collect(Collectors.toList());
    }

    public StopDto getStopById(Long id) {
        Stop stop = stopRepository.findRoutableById(id)
                .orElseThrow(() -> new ResourceNotFoundException("STOP_NOT_FOUND", "Stop with ID " + id + " was not found."));
        return StopDto.fromEntity(stop);
    }
}
