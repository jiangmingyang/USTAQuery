package com.usta.query.repository;

import com.usta.query.entity.ScrapeJob;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScrapeJobRepository extends JpaRepository<ScrapeJob, Long> {
}
