package com.dentro.dentrohills.repository;

import com.dentro.dentrohills.model.Hospital;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
public interface HospitalRepository extends JpaRepository<Hospital, Long> {
}
