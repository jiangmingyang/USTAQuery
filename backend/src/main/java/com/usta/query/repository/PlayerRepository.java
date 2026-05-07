package com.usta.query.repository;

import com.usta.query.entity.Player;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface PlayerRepository extends JpaRepository<Player, Long> {

    Optional<Player> findByUaid(String uaid);

    @Query("SELECT p FROM Player p WHERE LOWER(p.firstName) LIKE LOWER(CONCAT('%', :q, '%')) " +
           "OR LOWER(p.lastName) LIKE LOWER(CONCAT('%', :q, '%')) " +
           "OR p.uaid LIKE CONCAT('%', :q, '%')")
    Page<Player> search(@Param("q") String query, Pageable pageable);
}
