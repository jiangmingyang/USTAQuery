package com.usta.query.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "scrape_errors")
public class ScrapeError {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scrape_job_id", nullable = false)
    private ScrapeJob scrapeJob;

    @Column(length = 1000)
    private String url;

    @Column(name = "error_type", nullable = false, length = 100)
    private String errorType;

    @Column(name = "error_message", nullable = false, columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "http_status_code", columnDefinition = "SMALLINT")
    private Short httpStatusCode;

    @Column(name = "occurred_at", nullable = false)
    private LocalDateTime occurredAt;

    @PrePersist
    protected void onCreate() {
        occurredAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public ScrapeJob getScrapeJob() { return scrapeJob; }
    public void setScrapeJob(ScrapeJob scrapeJob) { this.scrapeJob = scrapeJob; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public String getErrorType() { return errorType; }
    public void setErrorType(String errorType) { this.errorType = errorType; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public Short getHttpStatusCode() { return httpStatusCode; }
    public void setHttpStatusCode(Short httpStatusCode) { this.httpStatusCode = httpStatusCode; }

    public LocalDateTime getOccurredAt() { return occurredAt; }
}
