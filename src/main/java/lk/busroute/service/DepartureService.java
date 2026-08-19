package lk.busroute.service;

import lk.busroute.domain.StopTime;
import lk.busroute.domain.Trip;
import lk.busroute.dto.DepartureDto;
import lk.busroute.dto.DeparturesResponseDto;
import lk.busroute.dto.StopDto;
import lk.busroute.exception.ResourceNotFoundException;
import lk.busroute.repository.StopTimeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Time;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class DepartureService {

    private final StopService stopService;
    private final StopTimeRepository stopTimeRepository;
    private final Clock clock;

    public DepartureService(StopService stopService,
                            StopTimeRepository stopTimeRepository,
                            Clock clock) {
        this.stopService = stopService;
        this.stopTimeRepository = stopTimeRepository;
        this.clock = clock;
    }

    public DeparturesResponseDto getDepartures(Long stopId, LocalDate date, LocalTime time) {
        StopDto stop = stopService.getStopById(stopId);

        LocalDate reqDate = (date != null) ? date : LocalDate.now(clock);
        LocalTime reqTime = (time != null) ? time : LocalTime.now(clock);

        Time sqlTime = Time.valueOf(reqTime);
        List<StopTime> stopTimes = stopTimeRepository.findUpcomingDeparturesFromTime(stopId, sqlTime, 20);

        if (stopTimes.isEmpty()) {
            stopTimes = stopTimeRepository.findEarlyMorningDepartures(stopId, 20);
        }

        List<DepartureDto> departures = new ArrayList<>();
        for (StopTime st : stopTimes) {
            Trip t = st.getTrip();
            if (t == null || t.getRoutePattern() == null || t.getRoutePattern().getRoute() == null) {
                continue;
            }

            DepartureDto dto = new DepartureDto();
            dto.setRouteId(t.getRoutePattern().getRoute().getId());
            dto.setRouteNumber(t.getRoutePattern().getRoute().getRouteNumber());
            dto.setRouteName(t.getRoutePattern().getRoute().getName());
            dto.setServiceType(t.getServiceType());
            dto.setBoardingStop(stop);

            String destName = "Destination";
            if (t.getRoutePattern().getDestinationStop() != null) {
                destName = t.getRoutePattern().getDestinationStop().getDisplayName();
            } else if (t.getRoutePattern().getRoute().getDestinationStop() != null) {
                destName = t.getRoutePattern().getRoute().getDestinationStop().getDisplayName();
            }
            dto.setDestination(destName);
            dto.setScheduledDeparture(st.getDepartureTime());
            dto.setTripHeadsign(t.getTripHeadsign());
            dto.setTripId(t.getId());

            departures.add(dto);
        }

        return new DeparturesResponseDto(stop, reqDate, reqTime, departures);
    }
}
