package com.teleconsulting.demo.repository;

import com.teleconsulting.demo.model.Doctor;
import com.teleconsulting.demo.model.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface PatientRepository extends JpaRepository<Patient, Long> {
    Patient findByPhoneNumber(String phoneNumber);
    Optional<Patient> findById(Long id);
    Optional<Patient> findByEmail(String email);
    List<Patient> findBywstatus(boolean wstatus);
    List<Patient> findBywstatusAndPincomingcallIsNull(boolean wstatus);
    List<Patient> findByCallbackstatus(boolean callbackstatus);
    List<Patient> findBywstatusAndAssigneddoctor(boolean wstatus, Long assigneddoctor);
    @Query("SELECT p FROM Patient p WHERE p.deleteFlag = FALSE")
    List<Patient> findAllPatient();
}
