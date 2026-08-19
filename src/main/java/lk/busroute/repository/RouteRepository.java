package lk.busroute.repository;

import lk.busroute.domain.Route;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RouteRepository extends JpaRepository<Route, Long> {

    @Query("""
        SELECT DISTINCT r FROM Route r
        JOIN RoutePattern rp ON rp.route.id = r.id
        WHERE r.active = true
          AND rp.active = true
          AND (:routeNumber IS NULL OR LOWER(r.routeNumber) = LOWER(:routeNumber))
          AND (:serviceType IS NULL OR LOWER(r.serviceType) = LOWER(:serviceType))
        ORDER BY r.routeNumber ASC
    """)
    List<Route> findRoutableRoutes(
        @Param("routeNumber") String routeNumber,
        @Param("serviceType") String serviceType
    );

    @Query("""
        SELECT r FROM Route r
        WHERE r.id = :id
          AND r.active = true
    """)
    Optional<Route> findRoutableById(@Param("id") Long id);
}
