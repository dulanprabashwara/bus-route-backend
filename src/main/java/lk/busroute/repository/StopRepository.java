package lk.busroute.repository;

import lk.busroute.domain.Stop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StopRepository extends JpaRepository<Stop, Long> {

    @Query("""
        SELECT s FROM Stop s
        WHERE s.active = true
          AND (s.nameEn IS NULL OR s.nameEn NOT LIKE '%Corrupted%')
          AND s.rawCorruptedName IS NULL
          AND (
              LOWER(s.normalizedName) LIKE LOWER(CONCAT('%', :query, '%'))
              OR LOWER(s.nameEn) LIKE LOWER(CONCAT('%', :query, '%'))
              OR s.nameSi LIKE CONCAT('%', :query, '%')
              OR s.nameTa LIKE CONCAT('%', :query, '%')
          )
        ORDER BY
          CASE WHEN LOWER(s.nameEn) LIKE LOWER(CONCAT(:query, '%')) THEN 0 ELSE 1 END,
          s.nameEn ASC
    """)
    List<Stop> searchStops(@Param("query") String query);

    @Query("""
        SELECT s FROM Stop s
        WHERE s.id = :id
          AND s.active = true
          AND (s.nameEn IS NULL OR s.nameEn NOT LIKE '%Corrupted%')
          AND s.rawCorruptedName IS NULL
    """)
    Optional<Stop> findRoutableById(@Param("id") Long id);
}
