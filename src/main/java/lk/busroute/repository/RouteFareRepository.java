package lk.busroute.repository;

import lk.busroute.domain.RouteFare;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RouteFareRepository extends JpaRepository<RouteFare, Long> {

    @Query("""
        SELECT rf FROM RouteFare rf
        WHERE rf.fromStop.id = :fromStopId
          AND rf.toStop.id = :toStopId
          AND (:serviceType IS NULL OR rf.serviceType = :serviceType)
          AND rf.fareType = 'EXACT_POINT_TO_POINT'
        ORDER BY rf.id DESC
    """)
    List<RouteFare> findExactFares(
        @Param("fromStopId") Long fromStopId,
        @Param("toStopId") Long toStopId,
        @Param("serviceType") String serviceType
    );

    @Query("""
        SELECT rf FROM RouteFare rf
        WHERE rf.fromStop.id = :fromStopId
          AND rf.toStop.id = :toStopId
          AND (:serviceType IS NULL OR rf.serviceType = :serviceType)
          AND rf.fareType = 'FULL_ENDPOINT_ONLY'
        ORDER BY rf.id DESC
    """)
    List<RouteFare> findEndpointFares(
        @Param("fromStopId") Long fromStopId,
        @Param("toStopId") Long toStopId,
        @Param("serviceType") String serviceType
    );

    @Query("""
        SELECT rf FROM RouteFare rf
        WHERE rf.routePattern.id = :patternId
        ORDER BY rf.id DESC
    """)
    List<RouteFare> findByRoutePatternId(@Param("patternId") Long patternId);

    @Query("""
        SELECT rf FROM RouteFare rf
        WHERE rf.route.id = :routeId
        ORDER BY rf.id DESC
    """)
    List<RouteFare> findByRouteId(@Param("routeId") Long routeId);
}
