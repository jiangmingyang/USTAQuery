package com.usta.query.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "scrape_jobs")
public class ScrapeJob {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "job_type", nullable = false, length = 50)
    private String jobType;

    @Column(nullable = false, length = 20)
    private String status = "PENDING";

    @Column(name = "target_url", length = 1000)
    private String targetUrl;

    @Column(columnDefinition = "JSON")
    private String parameters;

    @Column(name = "records_processed", nullable = false)
    private int recordsProcessed;

    @Column(name = "records_created", nullable = false)
    private int recordsCreated;

    @Column(name = "records_updated", nullable = false)
    private int recordsUpdated;

    @Column(name = "records_failed", nullable = false)
    private int recordsFailed;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getJobType() { return jobType; }
    public void setJobType(String jobType) { this.jobType = jobType; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getTargetUrl() { return targetUrl; }
    public void setTargetUrl(String targetUrl) { this.targetUrl = targetUrl; }

    public String getParameters() { return parameters; }
    public void setParameters(String parameters) { this.parameters = parameters; }

    public int getRecordsProcessed() { return recordsProcessed; }
    public void setRecordsProcessed(int recordsProcessed) { this.recordsProcessed = recordsProcessed; }

    public int getRecordsCreated() { return recordsCreated; }
    public void setRecordsCreated(int recordsCreated) { this.recordsCreated = recordsCreated; }

    public int getRecordsUpdated() { return recordsUpdated; }
    public void setRecordsUpdated(int recordsUpdated) { this.recordsUpdated = recordsUpdated; }

    public int getRecordsFailed() { return recordsFailed; }
    public void setRecordsFailed(int recordsFailed) { this.recordsFailed = recordsFailed; }

    public LocalDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
