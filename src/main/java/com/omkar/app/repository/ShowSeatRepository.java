package com.omkar.app.repository;

import com.omkar.app.entity.ShowSeat;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShowSeatRepository extends JpaRepository<ShowSeat, Long> {

    List<ShowSeat> findByShowId(Long showId);

    Optional<ShowSeat> findByShowIdAndSeatNumber(Long showId, String seatNumber);

    /** Locks rows for update to prevent concurrent booking of the same seats. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM ShowSeat s WHERE s.show.id = :showId AND s.seatNumber IN :seatNumbers ORDER BY s.id")
    List<ShowSeat> findByShowIdAndSeatNumberInForUpdate(
            @Param("showId") Long showId,
            @Param("seatNumbers") List<String> seatNumbers);
}
