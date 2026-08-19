package lk.busroute.repository;

import lk.busroute.domain.RouteStop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RouteStopRepository extends JpaRepository<RouteStop, Long> {

    @Query("""
        SELECT rs FROM RouteStop rs
        WHERE rs.routePattern.id = :routePatternId
        ORDER BY rs.stopSequence ASC
    """)
    List<RouteStop> findByRoutePatternIdOrderByStopSequenceAsc(@Param("routePatternId") Long routePatternId);

    @Query("""
        SELECT rs FROM RouteStop rs
        JOIN rs.routePattern rp
        JOIN rp.route r
        WHERE rs.stop.id = :stopId
          AND rp.active = true
          AND r.active = true
    """)
    List<RouteStop> findByStopId(@Param("stopId") Long stopId);
}
