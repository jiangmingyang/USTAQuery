package com.usta.query.repository;

import com.usta.query.entity.Registration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RegistrationRepository extends JpaRepository<Registration, Long> {

    @Query(value = "SELECT r FROM Registration r JOIN FETCH r.tournament JOIN FETCH r.player1 LEFT JOIN FETCH r.player2 " +
           "WHERE r.player1.uaid = :uaid OR r.player2.uaid = :uaid",
           countQuery = "SELECT COUNT(r) FROM Registration r " +
           "WHERE r.player1.uaid = :uaid OR r.player2.uaid = :uaid")
    Page<Registration> findByPlayerUaid(@Param("uaid") String uaid, Pageable pageable);

    @Query(value = "SELECT r FROM Registration r JOIN FETCH r.tournament JOIN FETCH r.player1 LEFT JOIN FETCH r.player2 " +
           "WHERE (r.player1.uaid = :uaid OR r.player2.uaid = :uaid) AND r.status = :status",
           countQuery = "SELECT COUNT(r) FROM Registration r " +
           "WHERE (r.player1.uaid = :uaid OR r.player2.uaid = :uaid) AND r.status = :status")
    Page<Registration> findByPlayerUaidAndStatus(@Param("uaid") String uaid, @Param("status") String status, Pageable pageable);
}
