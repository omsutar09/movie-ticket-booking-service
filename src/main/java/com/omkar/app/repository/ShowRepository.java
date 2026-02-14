package com.omkar.app.repository;

import com.omkar.app.entity.Show;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ShowRepository extends JpaRepository<Show, Long> {

    @Query("SELECT s FROM Show s WHERE s.theatre.city.name = :cityName AND s.movie.title = :movieTitle AND s.startTime BETWEEN :start AND :end")
    List<Show> findShowsByCityAndMovieAndDate(
            @Param("cityName") String cityName,
            @Param("movieTitle") String movieTitle,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);
}
