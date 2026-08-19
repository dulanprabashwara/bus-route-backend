package lk.busroute.service;

import lk.busroute.domain.Route;
import lk.busroute.domain.RouteStop;
import lk.busroute.domain.Stop;
import lk.busroute.dto.*;
import lk.busroute.exception.ResourceNotFoundException;
import lk.busroute.repository.RouteStopRepository;
import lk.busroute.repository.StopTimeRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Time;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class JourneyPlannerService {

    private final StopService stopService;
    private final StopTimeRepository stopTimeRepository;
    private final RouteStopRepository routeStopRepository;
    private final FareService fareService;
    private final Clock clock;

    @Value("${journey.minimum-transfer-minutes:10}")
    private int minimumTransferMinutes;

    @Value("${journey.max-transfers:2}")
    private int maxTransfers;

    @Value("${journey.score.duration-weight:1.0}")
    private double durationWeight;

    @Value("${journey.score.transfer-weight:30.0}")
    private double transferWeight;

    @Value("${journey.score.waiting-weight:0.5}")
    private double waitingWeight;

    @Value("${journey.score.fare-weight:0.05}")
    private double fareWeight;

    public JourneyPlannerService(StopService stopService,
                                 StopTimeRepository stopTimeRepository,
                                 RouteStopRepository routeStopRepository,
                                 FareService fareService,
                                 Clock clock) {
        this.stopService = stopService;
        this.stopTimeRepository = stopTimeRepository;
        this.routeStopRepository = routeStopRepository;
        this.fareService = fareService;
        this.clock = clock;
    }

    public JourneySearchResponseDto searchJourneys(Long fromStopId, Long toStopId, LocalDate date, LocalTime time) {
        if (fromStopId.equals(toStopId)) {
            throw new IllegalArgumentException("Origin and destination stops must be different.");
        }

        StopDto fromStop = stopService.getStopById(fromStopId);
        StopDto toStop = stopService.getStopById(toStopId);

        LocalDate reqDate = (date != null) ? date : LocalDate.now(clock);
        LocalTime reqTime = (time != null) ? time : LocalTime.now(clock);

        List<JourneyDto> candidateJourneys = new ArrayList<>();

        // 1. Direct Journeys (0 transfers)
        List<JourneyDto> directJourneys = findDirectJourneys(fromStop, toStop, reqTime);
        candidateJourneys.addAll(directJourneys);

        // 2. One-Transfer Journeys (1 transfer)
        if (maxTransfers >= 1) {
            List<JourneyDto> oneTransferJourneys = findOneTransferJourneys(fromStop, toStop, reqTime);
            candidateJourneys.addAll(oneTransferJourneys);
        }

        // 3. Two-Transfer Journeys (2 transfers) if needed
        if (maxTransfers >= 2 && candidateJourneys.size() < 5) {
            List<JourneyDto> twoTransferJourneys = findTwoTransferJourneys(fromStop, toStop, reqTime);
            candidateJourneys.addAll(twoTransferJourneys);
        }

        // Deduplicate journeys by trip IDs and times
        List<JourneyDto> uniqueJourneys = deduplicateJourneys(candidateJourneys);

        // Rank and label journeys
        labelAndRankJourneys(uniqueJourneys, reqTime);

        return new JourneySearchResponseDto(fromStop, toStop, reqDate, reqTime, uniqueJourneys);
    }

    private List<JourneyDto> findDirectJourneys(StopDto fromStop, StopDto toStop, LocalTime reqTime) {
        Time sqlTime = Time.valueOf(reqTime);
        List<Object[]> rows = stopTimeRepository.findDirectTripLegs(fromStop.getId(), toStop.getId(), sqlTime);

        if (rows.isEmpty()) {
            rows = stopTimeRepository.findDirectTripLegsNextDay(fromStop.getId(), toStop.getId());
        }

        List<JourneyDto> journeys = new ArrayList<>();
        Set<String> seenTrips = new HashSet<>();

        for (Object[] row : rows) {
            Long tripId = ((Number) row[0]).longValue();
            LocalTime depTime = ((Time) row[3]).toLocalTime();
            LocalTime arrTime = ((Time) row[4]).toLocalTime();
            Long patternId = ((Number) row[5]).longValue();
            Long routeId = ((Number) row[6]).longValue();
            String routeNumber = (String) row[7];
            String routeName = (String) row[8];
            String serviceType = (String) row[9];

            String key = routeId + "-" + tripId + "-" + depTime + "-" + arrTime;
            if (seenTrips.contains(key)) continue;
            seenTrips.add(key);

            long duration = Duration.between(depTime, arrTime).toMinutes();
            if (duration < 0) duration += 1440; // overnight wrapping

            FareResultDto fareRes = fareService.calculateFare(routeId, patternId, fromStop.getId(), toStop.getId(), serviceType);

            JourneyLegDto leg = new JourneyLegDto();
            leg.setLegIndex(1);
            leg.setRouteId(routeId);
            leg.setRouteNumber(routeNumber);
            leg.setRouteName(routeName);
            leg.setServiceType(serviceType);
            leg.setTripId(tripId);
            leg.setBoardingStop(fromStop);
            leg.setDropOffStop(toStop);
            leg.setDepartureTime(depTime);
            leg.setArrivalTime(arrTime);
            leg.setDurationMinutes(duration);
            leg.setFare(fareRes.getAmount());
            leg.setFareType(fareRes.getFareType());
            leg.setFareStatus(fareRes.getStatus());

            JourneyDto j = new JourneyDto();
            j.setJourneyId(UUID.randomUUID().toString());
            j.setDepartureTime(depTime);
            j.setArrivalTime(arrTime);
            j.setDurationMinutes(duration);
            j.setTransferCount(0);
            j.setTotalFare(fareRes.getAmount());
            j.setFareStatus(fareRes.getStatus().equals("EXACT") || fareRes.getStatus().equals("ENDPOINT_ONLY") ? "COMPLETE" : "UNAVAILABLE");
            j.getLegs().add(leg);

            journeys.add(j);
            if (journeys.size() >= 10) break;
        }

        return journeys;
    }

    private List<JourneyDto> findOneTransferJourneys(StopDto fromStop, StopDto toStop, LocalTime reqTime) {
        List<RouteStop> fromStops = routeStopRepository.findByStopId(fromStop.getId());
        List<RouteStop> toStops = routeStopRepository.findByStopId(toStop.getId());

        Set<Long> fromPatternIds = fromStops.stream().map(rs -> rs.getRoutePattern().getId()).collect(Collectors.toSet());
        Set<Long> toPatternIds = toStops.stream().map(rs -> rs.getRoutePattern().getId()).collect(Collectors.toSet());

        // Find candidate intermediate stops
        Map<Long, List<RouteStop>> patternToStops = new HashMap<>();

        for (Long pId : fromPatternIds) {
            patternToStops.put(pId, routeStopRepository.findByRoutePatternIdOrderByStopSequenceAsc(pId));
        }
        for (Long pId : toPatternIds) {
            patternToStops.put(pId, routeStopRepository.findByRoutePatternIdOrderByStopSequenceAsc(pId));
        }

        Set<Long> commonStopIds = new HashSet<>();
        for (Long p1 : fromPatternIds) {
            List<RouteStop> p1Stops = patternToStops.get(p1);
            int fromSeq = -1;
            for (RouteStop rs : p1Stops) {
                if (rs.getStop().getId().equals(fromStop.getId())) {
                    fromSeq = rs.getStopSequence();
                    break;
                }
            }
            if (fromSeq < 0) continue;

            for (RouteStop rs1 : p1Stops) {
                if (rs1.getStopSequence() > fromSeq) {
                    Long transferStopId = rs1.getStop().getId();
                    if (transferStopId.equals(toStop.getId())) continue;

                    for (Long p2 : toPatternIds) {
                        if (p1.equals(p2)) continue;
                        List<RouteStop> p2Stops = patternToStops.get(p2);
                        int toSeq = -1;
                        int transferSeq = -1;
                        for (RouteStop rs2 : p2Stops) {
                            if (rs2.getStop().getId().equals(toStop.getId())) {
                                toSeq = rs2.getStopSequence();
                            }
                            if (rs2.getStop().getId().equals(transferStopId)) {
                                transferSeq = rs2.getStopSequence();
                            }
                        }
                        if (transferSeq > 0 && toSeq > transferSeq) {
                            commonStopIds.add(transferStopId);
                        }
                    }
                }
            }
        }

        List<JourneyDto> journeys = new ArrayList<>();
        Time sqlTime = Time.valueOf(reqTime);
        Set<String> seenFamilyConnections = new HashSet<>();

        for (Long tStopId : commonStopIds) {
            StopDto transferStop = stopService.getStopById(tStopId);

            List<Object[]> leg1Rows = stopTimeRepository.findDirectTripLegs(fromStop.getId(), tStopId, sqlTime);
            for (Object[] row1 : leg1Rows) {
                Long trip1Id = ((Number) row1[0]).longValue();
                LocalTime dep1 = ((Time) row1[3]).toLocalTime();
                LocalTime arr1 = ((Time) row1[4]).toLocalTime();
                Long pattern1Id = ((Number) row1[5]).longValue();
                Long route1Id = ((Number) row1[6]).longValue();
                String r1Num = (String) row1[7];
                String r1Name = (String) row1[8];
                String s1Type = (String) row1[9];

                LocalTime minDep2 = arr1.plusMinutes(minimumTransferMinutes);
                Time sqlMinDep2 = Time.valueOf(minDep2);

                List<Object[]> leg2Rows = stopTimeRepository.findDirectTripLegs(tStopId, toStop.getId(), sqlMinDep2);
                for (Object[] row2 : leg2Rows) {
                    Long trip2Id = ((Number) row2[0]).longValue();
                    LocalTime dep2 = ((Time) row2[3]).toLocalTime();
                    LocalTime arr2 = ((Time) row2[4]).toLocalTime();
                    Long pattern2Id = ((Number) row2[5]).longValue();
                    Long route2Id = ((Number) row2[6]).longValue();
                    String r2Num = (String) row2[7];
                    String r2Name = (String) row2[8];
                    String s2Type = (String) row2[9];

                    // Dominated pruning: For a given first trip + transfer stop + second route,
                    // only take the EARLIEST valid connecting second bus!
                    String familyKey = trip1Id + "-" + tStopId + "-" + route2Id;
                    if (seenFamilyConnections.contains(familyKey)) {
                        continue;
                    }
                    seenFamilyConnections.add(familyKey);

                    long leg1Dur = Duration.between(dep1, arr1).toMinutes();
                    if (leg1Dur < 0) leg1Dur += 1440;

                    long waitDur = Duration.between(arr1, dep2).toMinutes();
                    if (waitDur < 0) waitDur += 1440;

                    long leg2Dur = Duration.between(dep2, arr2).toMinutes();
                    if (leg2Dur < 0) leg2Dur += 1440;

                    long totalDur = leg1Dur + waitDur + leg2Dur;

                    FareResultDto fare1 = fareService.calculateFare(route1Id, pattern1Id, fromStop.getId(), tStopId, s1Type);
                    FareResultDto fare2 = fareService.calculateFare(route2Id, pattern2Id, tStopId, toStop.getId(), s2Type);

                    BigDecimal totalFare = null;
                    String fareStatus = "UNAVAILABLE";
                    if (fare1.getAmount() != null && fare2.getAmount() != null) {
                        totalFare = fare1.getAmount().add(fare2.getAmount());
                        fareStatus = "COMPLETE";
                    } else if (fare1.getAmount() != null) {
                        totalFare = fare1.getAmount();
                        fareStatus = "PARTIAL";
                    } else if (fare2.getAmount() != null) {
                        totalFare = fare2.getAmount();
                        fareStatus = "PARTIAL";
                    }

                    JourneyLegDto l1 = new JourneyLegDto();
                    l1.setLegIndex(1);
                    l1.setRouteId(route1Id);
                    l1.setRouteNumber(r1Num);
                    l1.setRouteName(r1Name);
                    l1.setServiceType(s1Type);
                    l1.setTripId(trip1Id);
                    l1.setBoardingStop(fromStop);
                    l1.setDropOffStop(transferStop);
                    l1.setDepartureTime(dep1);
                    l1.setArrivalTime(arr1);
                    l1.setDurationMinutes(leg1Dur);
                    l1.setFare(fare1.getAmount());
                    l1.setFareType(fare1.getFareType());
                    l1.setFareStatus(fare1.getStatus());

                    JourneyLegDto l2 = new JourneyLegDto();
                    l2.setLegIndex(2);
                    l2.setRouteId(route2Id);
                    l2.setRouteNumber(r2Num);
                    l2.setRouteName(r2Name);
                    l2.setServiceType(s2Type);
                    l2.setTripId(trip2Id);
                    l2.setBoardingStop(transferStop);
                    l2.setDropOffStop(toStop);
                    l2.setDepartureTime(dep2);
                    l2.setArrivalTime(arr2);
                    l2.setDurationMinutes(leg2Dur);
                    l2.setFare(fare2.getAmount());
                    l2.setFareType(fare2.getFareType());
                    l2.setFareStatus(fare2.getStatus());

                    JourneyDto j = new JourneyDto();
                    j.setJourneyId(UUID.randomUUID().toString());
                    j.setDepartureTime(dep1);
                    j.setArrivalTime(arr2);
                    j.setDurationMinutes(totalDur);
                    j.setTransferCount(1);
                    j.setTotalFare(totalFare);
                    j.setFareStatus(fareStatus);
                    j.getLegs().add(l1);
                    j.getLegs().add(l2);

                    journeys.add(j);
                    if (journeys.size() >= 20) break;
                }
                if (journeys.size() >= 20) break;
            }
            if (journeys.size() >= 20) break;
        }

        return journeys;
    }

    private List<JourneyDto> findTwoTransferJourneys(StopDto fromStop, StopDto toStop, LocalTime reqTime) {
        // Two-transfer routing not yet implemented in pilot phase
        return List.of();
    }

    private List<JourneyDto> deduplicateJourneys(List<JourneyDto> journeys) {
        Map<String, JourneyDto> map = new LinkedHashMap<>();
        for (JourneyDto j : journeys) {
            String key = j.getTransferCount() + "-" + j.getDepartureTime() + "-" + j.getArrivalTime();
            if (!map.containsKey(key)) {
                map.put(key, j);
            }
        }
        List<JourneyDto> result = new ArrayList<>(map.values());
        if (result.size() > 10) {
            return result.subList(0, 10);
        }
        return result;
    }

    private void labelAndRankJourneys(List<JourneyDto> journeys, LocalTime reqTime) {
        if (journeys.isEmpty()) return;

        // Score journeys
        JourneyDto bestScored = null;
        double minScore = Double.MAX_VALUE;

        JourneyDto nextAvailable = null;
        long minDepWait = Long.MAX_VALUE;

        JourneyDto fastest = null;
        long minDuration = Long.MAX_VALUE;

        JourneyDto fewestTransfers = null;
        int minTransfers = Integer.MAX_VALUE;

        JourneyDto cheapest = null;
        BigDecimal minFare = null;

        for (JourneyDto j : journeys) {
            long depWait = Duration.between(reqTime, j.getDepartureTime()).toMinutes();
            if (depWait < 0) depWait += 1440;

            double fareVal = (j.getTotalFare() != null) ? j.getTotalFare().doubleValue() : 500.0;
            double score = j.getDurationMinutes() * durationWeight
                    + j.getTransferCount() * transferWeight
                    + depWait * waitingWeight
                    + fareVal * fareWeight;

            if (score < minScore) {
                minScore = score;
                bestScored = j;
            }

            if (depWait < minDepWait) {
                minDepWait = depWait;
                nextAvailable = j;
            }

            if (j.getDurationMinutes() < minDuration) {
                minDuration = j.getDurationMinutes();
                fastest = j;
            }

            if (j.getTransferCount() < minTransfers) {
                minTransfers = j.getTransferCount();
                fewestTransfers = j;
            }

            if (j.getTotalFare() != null && "COMPLETE".equals(j.getFareStatus())) {
                if (minFare == null || j.getTotalFare().compareTo(minFare) < 0) {
                    minFare = j.getTotalFare();
                    cheapest = j;
                }
            }
        }

        if (bestScored != null) bestScored.getLabels().add("RECOMMENDED");
        if (nextAvailable != null && !nextAvailable.getLabels().contains("NEXT_AVAILABLE")) {
            nextAvailable.getLabels().add("NEXT_AVAILABLE");
        }
        if (fastest != null && !fastest.getLabels().contains("FASTEST")) {
            fastest.getLabels().add("FASTEST");
        }
        if (fewestTransfers != null && !fewestTransfers.getLabels().contains("FEWEST_TRANSFERS")) {
            fewestTransfers.getLabels().add("FEWEST_TRANSFERS");
        }
        if (cheapest != null && !cheapest.getLabels().contains("CHEAPEST")) {
            cheapest.getLabels().add("CHEAPEST");
        }

        // Sort by composite score
        journeys.sort(Comparator.comparingLong(j -> {
            long depWait = Duration.between(reqTime, j.getDepartureTime()).toMinutes();
            if (depWait < 0) depWait += 1440;
            return (j.getTransferCount() * 1000) + j.getDurationMinutes() + depWait;
        }));
    }
}
