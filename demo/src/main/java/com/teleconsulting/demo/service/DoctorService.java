package com.teleconsulting.demo.service;
import com.teleconsulting.demo.dto.Ddetails;
import com.teleconsulting.demo.dto.DoctorRating;
import com.teleconsulting.demo.dto.RegDoc;
import com.teleconsulting.demo.exception.UserNotFoundException;
import com.teleconsulting.demo.model.AuthenticationResponse;
import com.teleconsulting.demo.model.Doctor;
import com.teleconsulting.demo.model.Patient;

import java.util.List;
import java.util.Optional;

public interface DoctorService{
    Doctor saveDoctor(Doctor doctor);
    AuthenticationResponse saveNewDoctor(RegDoc doctor);
    List<Doctor> getAllDoctors();
    Doctor findByPhoneNumber(String phoneNumber);
    Doctor findById(Long id);
    Doctor updateDoctorIncomingCall(String doctorPhoneNumber, String patientPhoneNumber);
    Doctor updateDoctor(Long id,Doctor doctor);
    void deleteDoctorById(Long id) throws UserNotFoundException;
    List<Doctor> getDoctorsBySupervisorId(Long supervisorId);
    Optional<Doctor> getUserByEmail(String token);
    List<Doctor> findAllAvailableDoctors();
    List<Ddetails> getSnrDoctors();
    void updateRating(Long id, int rating);
    List<DoctorRating> getAllRatings();
    List<Doctor> getOnlineDoctorsforPat();
    List<Doctor> getAllSrDoctors();
    List<Doctor> getAllDoctorsExceptPassword();
    Long countDoctors();
    List<Doctor> getDoctorsUnderSeniorDoctor(Long supervisorId);
    Doctor updateDoctors(Long id, Doctor updatedDoctor);
    void updateDoctorSdid(Long doctorId, Long newSdid);
    Doctor findByEmail(String email);
    Doctor getDoctorNameAndPhoneNumber(Long doctorId);
}
