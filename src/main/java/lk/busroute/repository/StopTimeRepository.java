package lk.busroute.repository;

import lk.busroute.domain.StopTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.sql.Time;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface StopTimeRepository extends JpaRepository<StopTime, Long> {

    @Query(value = """
        SELECT st.* FROM v_routable_stop_times st
        JOIN v_routable_trips t ON t.id = st.trip_id
        JOIN v_routable_patterns rp ON rp.id = t.route_pattern_id
        JOIN routes r ON r.id = rp.route_id
        WHERE st.stop_id = :stopId
          AND st.departure_time >= :time
        ORDER BY st.departure_time ASC
        LIMIT :limit
    """, nativeQuery = true)
    List<StopTime> findUpcomingDeparturesFromTime(
        @Param("stopId") Long stopId,
        @Param("time") Time time,
        @Param("limit") int limit
    );

    @Query(value = """
        SELECT st.* FROM v_routable_stop_times st
        JOIN v_routable_trips t ON t.id = st.trip_id
        JOIN v_routable_patterns rp ON rp.id = t.route_pattern_id
        JOIN routes r ON r.id = rp.route_id
        WHERE st.stop_id = :stopId
        ORDER BY st.departure_time ASC
        LIMIT :limit
    """, nativeQuery = true)
    List<StopTime> findEarlyMorningDepartures(
        @Param("stopId") Long stopId,
        @Param("limit") int limit
    );

    @Query(value = """
        SELECT st1.trip_id, st1.stop_sequence as from_seq, st2.stop_sequence as to_seq,
               st1.departure_time, st2.arrival_time, rp.id as pattern_id, r.id as route_id,
               r.route_number, r.name as route_name, t.service_type
        FROM v_routable_stop_times st1
        JOIN v_routable_stop_times st2 ON st2.trip_id = st1.trip_id AND st2.stop_sequence > st1.stop_sequence
        JOIN v_routable_trips t ON t.id = st1.trip_id
        JOIN v_routable_patterns rp ON rp.id = t.route_pattern_id
        JOIN routes r ON r.id = rp.route_id
        WHERE st1.stop_id = :fromStopId
          AND st2.stop_id = :toStopId
          AND st1.departure_time >= :time
        ORDER BY st1.departure_time ASC
        LIMIT 50
    """, nativeQuery = true)
    List<Object[]> findDirectTripLegs(
        @Param("fromStopId") Long fromStopId,
        @Param("toStopId") Long toStopId,
        @Param("time") Time time
    );

    @Query(value = """
        SELECT st1.trip_id, st1.stop_sequence as from_seq, st2.stop_sequence as to_seq,
               st1.departure_time, st2.arrival_time, rp.id as pattern_id, r.id as route_id,
               r.route_number, r.name as route_name, t.service_type
        FROM v_routable_stop_times st1
        JOIN v_routable_stop_times st2 ON st2.trip_id = st1.trip_id AND st2.stop_sequence > st1.stop_sequence
        JOIN v_routable_trips t ON t.id = st1.trip_id
        JOIN v_routable_patterns rp ON rp.id = t.route_pattern_id
        JOIN routes r ON r.id = rp.route_id
        WHERE st1.stop_id = :fromStopId
          AND st2.stop_id = :toStopId
        ORDER BY st1.departure_time ASC
        LIMIT 50
    """, nativeQuery = true)
    List<Object[]> findDirectTripLegsNextDay(
        @Param("fromStopId") Long fromStopId,
        @Param("toStopId") Long toStopId
    );
}
