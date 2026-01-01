package com.hospital.common.domain;

import java.time.LocalDateTime;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Data;

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Data
public abstract class BaseEntity {

  private static final int LENGTH_100 = 100;

  @CreatedDate
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @LastModifiedDate
  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @CreatedBy
  @Column(name = "created_by", length = LENGTH_100)
  private String createdBy;

  @LastModifiedBy
  @Column(name = "updated_by", length = LENGTH_100)
  private String updatedBy;

  // Explicit getters to avoid relying on Lombok annotation processing in some IDEs
  public LocalDateTime getCreatedAt() { return this.createdAt; }
  public LocalDateTime getUpdatedAt() { return this.updatedAt; }
  public String getCreatedBy() { return this.createdBy; }
  public String getUpdatedBy() { return this.updatedBy; }
}
