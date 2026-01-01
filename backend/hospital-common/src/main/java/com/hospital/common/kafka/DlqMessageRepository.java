package com.hospital.common.kafka;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DlqMessageRepository extends JpaRepository<DlqMessage, Long> {
    // Additional query methods can be added as needed
}
