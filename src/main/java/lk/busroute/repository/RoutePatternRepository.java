package lk.busroute.repository;

import lk.busroute.domain.RoutePattern;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoutePatternRepository extends JpaRepository<RoutePattern, Long> {

    @Query("""
        SELECT rp FROM RoutePattern rp
        WHERE rp.route.id = :routeId
          AND rp.active = true
          AND rp.route.active = true
    """)
    List<RoutePattern> findActiveByRouteId(@Param("routeId") Long routeId);
}
