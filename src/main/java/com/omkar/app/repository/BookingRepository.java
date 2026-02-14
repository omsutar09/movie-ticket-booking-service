package com.omkar.app.repository;

import com.omkar.app.entity.Booking;
import com.omkar.app.entity.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByStatusAndHoldExpiresAtBefore(BookingStatus status, LocalDateTime holdExpiresAt);
}
