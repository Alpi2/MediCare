package com.hospital.repo;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.hospital.audit.Patient;
import jakarta.persistence.QueryHint;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {
  @Query(value = "SELECT * FROM patients WHERE mrn = :mrn", nativeQuery = true)
  @QueryHints({@QueryHint(name = "org.hibernate.cacheable", value = "true")})
  Optional<Patient> findByMrn(@Param("mrn") String mrn);
}
