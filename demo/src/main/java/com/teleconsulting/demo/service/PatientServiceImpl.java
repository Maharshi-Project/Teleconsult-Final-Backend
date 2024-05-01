package com.teleconsulting.demo.service;

import com.teleconsulting.demo.dto.Pdetails;
import com.teleconsulting.demo.model.AuthenticationResponse;
import com.teleconsulting.demo.model.Patient;
import com.teleconsulting.demo.model.Role;
import com.teleconsulting.demo.repository.PatientRepository;
import org.springframework.security.core.parameters.P;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.teleconsulting.demo.controller.CallHistoryController.decrypt;

@Service
public class PatientServiceImpl implements PatientService{
    private  final PatientRepository patientRepository;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    public PatientServiceImpl(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    @Override
    public Patient savePatient(Patient patient) {
        patientRepository.save(patient);
        return patient;
    }


    @Override
    public Patient createPatient(Patient patient) {
        return patientRepository.save(patient);
    }

    @Override
    public Patient getPatientByPhoneNumber(String phoneNumber) {
        Patient patient = patientRepository.findByPhoneNumber(phoneNumber);
        if(patient.isDeleteFlag()) {
            return null;
        }
        return patient;
    }

    @Override
    public Patient findById(Long id) {
        Optional<Patient> patient = patientRepository.findById(id);
        if(patient.isEmpty()) {
            return null;
        }else if(patient.get().isDeleteFlag()) {
            return null;
        }
        return patient.get();
    }

    @Override
    public Patient updatePatient(Long patientId, Pdetails pdetails) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new RuntimeException("Patient not found with id: " + patientId));
        if(patient.isDeleteFlag()) {
            return null;
        }
        patient.setName(pdetails.getName());
        patient.setGender(pdetails.getGender());
        return patientRepository.save(patient);
}

    @Override
    public AuthenticationResponse saveNewPatient(Patient patient){
        Patient patient1 = patientRepository.findByEmail(patient.getEmail()).orElse(null);
        if(patient1 == null) {
            Patient patient2 = new Patient();
            patient2.setPassword(passwordEncoder.encode(patient.getPassword()));
            patient2.setEmail(patient.getEmail());
            patient2.setName(patient.getName());
            patient2.setGender(patient.getGender());
            patient2.setPhoneNumber(patient.getPhoneNumber());
            patient2.setRole(Role.valueOf(Role.USER.toString()));
            patient2.setDeleteFlag(false);
            patientRepository.save(patient2);
            return new AuthenticationResponse(null, "User Registration was Successful!!");
        }
        else
            return new AuthenticationResponse(null, "Patient Email ID already exist");
    }

    @Override
    public Optional<Patient> getUserByEmail(String email) {
        Optional<Patient> patient = patientRepository.findByEmail(email);
        if(patient.isEmpty()){
            return null;
        }else if(patient.get().isDeleteFlag()) {
            return null;
        }
        return patient;
    }

    @Override
    public List<Patient> findAllcallbackPatients() {
        List<Patient> patientList = patientRepository.findByCallbackstatus(true);
        List<Patient> finalList = new ArrayList<>();
        for(Patient patient : patientList) {
            if(!patient.isDeleteFlag()) {
                finalList.add(patient);
            }
        }
        return finalList;
    }

    @Override
    public List<Patient> getALLPatient() {
        List<Patient> patientList = patientRepository.findAllPatient();
        List<Patient> finalList = new ArrayList<>();
        for(Patient patient : patientList) {
            if(!patient.isDeleteFlag()) {
                finalList.add(patient);
            }
        }
        return finalList;
    }

    @Override
    public void deletePatient(Long id) {
        Patient patient = patientRepository.findById(id).orElse(null);
        if(patient != null) {
            patient.setDeleteFlag(true);
            patientRepository.save(patient);
        }
    }

    @Override
    public List<Patient> findAll() {
        List<Patient> patientList = patientRepository.findAll();
        List<Patient> finalList = new ArrayList<>();
        for(Patient patient : patientList) {
            String temp = patient.getPhoneNumber();
            try{
                patient.setPhoneNumber(decrypt(temp));
                finalList.add(patient);
            }catch(Exception e)
            {
                System.out.println("\nException in PatientServiceImple findById\n"+e);
            }
        }
        return finalList;
    }

    @Override
    public Patient findByEmail(String email) {
        Patient patient = patientRepository.findByEmail(email).orElse(null);
        if(patient != null)
        {
            if(patient.isDeleteFlag()) {
                return null;
            }
            String temp = patient.getPhoneNumber();
            try{
                patient.setPhoneNumber(decrypt(temp));
            }catch(Exception e)
            {
                System.out.println("\nException in PatientServiceImple findById\n"+e);
            }
        }
        return patient;
    }
    @Override
    public Long countPatients() {
        return patientRepository.count();
    }
}
