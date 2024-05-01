package com.teleconsulting.demo.service;

import com.teleconsulting.demo.model.Doctor;
import com.teleconsulting.demo.model.Patient;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

@Service
public class CallHandlingService {

    // Queue to store available doctors
    private final DoctorService doctorService;
    private final PatientService patientService;
    public static Queue<Doctor> availableDoctorsQueue = new LinkedList<>();
    public static Queue<Doctor> callbackDoctorQueue = new LinkedList<>();
    public static Queue<Patient> waitingPatientsQueue = new LinkedList<>();
    public static Queue<Patient> callbackPatientQueue = new LinkedList<>();
    public CallHandlingService(DoctorService doctorService, PatientService patientService) {
        this.doctorService = doctorService;
        this.patientService = patientService;
    }
    // Method to add a doctor to the available doctors queue
    public void addAvailableDoctor(Doctor doctor) {
        availableDoctorsQueue.add(doctor);
    }
    public void addcallbackDoctor(Doctor doctor) { callbackDoctorQueue.add(doctor);}
    public void addAllAvailableDoctors(List<Doctor> doctors) {
        availableDoctorsQueue.addAll(doctors);
    }
    public void addAllcallbackPatients(List<Patient>patients) {callbackPatientQueue.addAll(patients);}
    // Method to remove and return the first available doctor from the queue
    public Doctor getAvailableDoctor() {
        return availableDoctorsQueue.poll();
    }
    public Doctor getcallbackDoctor() {
        return callbackDoctorQueue.poll();
    }
    // Method to add a patient to the waiting patients queue
    public void addWaitingPatient(Patient patient) {
        waitingPatientsQueue.add(patient);
    }
    public void addcallbackPatient(Patient patient) {
        callbackPatientQueue.add(patient);
    }
    // Method to remove and return the first waiting patient from the queue
    public Patient getWaitingPatient() {
        return waitingPatientsQueue.poll();
    }
    public Patient getcallbackPatient() {
        return callbackPatientQueue.poll();
    }
    @PostConstruct
    public void initializeAvailableDoctorsQueue() {
        List<Doctor> availableDoctors = doctorService.findAllAvailableDoctors();
        System.out.println("Available Doctors:");
        for (Doctor doctor : availableDoctors) {
            System.out.println(doctor);
        }
        addAllAvailableDoctors(availableDoctors);
    }
    @PostConstruct
    public void initializecallbackPatientsQueue() {
        List<Patient> cbpatients = patientService.findAllcallbackPatients();
        System.out.println("CallBack Patients");
        for (Patient patient : cbpatients) {
            System.out.println(patient);
        }
        addAllcallbackPatients(cbpatients);
    }
}

