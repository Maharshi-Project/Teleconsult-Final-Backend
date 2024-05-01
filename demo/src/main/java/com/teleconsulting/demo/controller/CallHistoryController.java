package com.teleconsulting.demo.controller;

import com.teleconsulting.demo.dto.DateTime;
import com.teleconsulting.demo.dto.Pdetails;
import com.teleconsulting.demo.model.CallHistory;
import com.teleconsulting.demo.model.Doctor;
import com.teleconsulting.demo.model.Patient;
import com.teleconsulting.demo.repository.CallHistoryRepository;
import com.teleconsulting.demo.repository.DoctorRepository;
import com.teleconsulting.demo.repository.PatientRepository;
import com.teleconsulting.demo.service.*;
import jakarta.mail.MessagingException;
import org.aspectj.weaver.ast.Call;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.print.Doc;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.teleconsulting.demo.service.CallHandlingService.*;

@RestController
@RequestMapping("/callhistory")
@EnableScheduling
public class CallHistoryController {
    private final CallHistoryService callHistoryService;

    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final CallHistoryRepository callHistoryRepository;
    private final CallHandlingService callHandlingService;

    private final DoctorService doctorService;
    private final PatientService patientService;
    private final EmailService emailService;

    private static final String key = "AP6bYQSb8OBtd6k9Xp80koDXwOwzo03V";
    public static String encrypt(String plaintext) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(), "AES");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] encryptedBytes = cipher.doFinal(plaintext.getBytes());
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    public static String decrypt(String ciphertext) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(), "AES");
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(ciphertext));
        return new String(decryptedBytes);
    }


    public CallHistoryController(CallHistoryService callHistoryService, DoctorRepository doctorRepository, PatientRepository patientRepository, CallHistoryRepository callHistoryRepository, CallHandlingService callHandlingService, DoctorService doctorService, PatientService patientService, EmailService emailService) {
        this.callHistoryService = callHistoryService;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
        this.callHistoryRepository = callHistoryRepository;
        this.callHandlingService = callHandlingService;
        this.doctorService = doctorService;
        this.patientService = patientService;
        this.emailService = emailService;
    }

    @PostMapping("/add") // Doctor
    public Long add(@RequestBody CallHistory callHistory) {
        CallHistory savedCallHistory = callHistoryService.saveCallHistory(callHistory);
        return savedCallHistory.getId();
    }
    @GetMapping("/today") // TRASH
    public ResponseEntity<List<CallHistory>> getCallHistoryForToday() {
        List<CallHistory> callHistoryList = callHistoryService.getCallHistoryForToday();
        return new ResponseEntity<>(callHistoryList, HttpStatus.OK);
    }
    @GetMapping("/today/search") // TRASH
    public ResponseEntity<List<CallHistory>> searchCallHistory(
            @RequestParam("startTime") String startTime,
            @RequestParam("endTime") String endTime) {
        // Get today's date
        LocalTime start = LocalTime.parse(startTime);
        LocalTime end = LocalTime.parse(endTime);
        List<CallHistory> callHistoryList = callHistoryService.getCallHistoryForTodayWithinTimeRange(start, end);
        return ResponseEntity.ok(callHistoryList);
    }
    @GetMapping("/getappointment/{id}") // Doctor Past Appointment ( Call history )
    public ResponseEntity<List<CallHistory>> getCallHistoryForDoctor(@PathVariable("id") Long doctorId) {
        List<CallHistory> callHistoryList = callHistoryService.getCallHistoryForDoctor(doctorId);
        return ResponseEntity.ok(callHistoryList);
    }
    @GetMapping("/seniordoctors/{sdid}") // Past appointment of Sr Doc and Doc under Sr Doc
    public ResponseEntity<List<CallHistory>> getCallHistoryForDoctorsWithSdid(@PathVariable("sdid") Long sdid) {
            List<CallHistory> callHistoryList = callHistoryService.getCallHistoryForDoctorsWithSdid(sdid);
            return ResponseEntity.ok(callHistoryList);
    }
    @GetMapping("/{id}/patientId") // Get Appointment for given patient
    public ResponseEntity<Long> getPatientIdFromCallHistory(@PathVariable Long id) {
        Long patientId = callHistoryService.getPatientIdFromCallHistory(id);
        return new ResponseEntity<>(patientId, HttpStatus.OK);
    }
    @PutMapping("/{cid}/updateendtime/{endtime}") // Update End time when call ends
    public ResponseEntity<?> updateendtime(@PathVariable Long cid, @PathVariable LocalTime endtime) {
        try {
            callHistoryService.updateendtime(cid, endtime);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error updating prescription");
        }
    }
    @PutMapping("/{cid}/update-prescription/{prescription}") // Update Prescription when call end
    public ResponseEntity<?> updatePrescription(@PathVariable Long cid, @PathVariable String prescription) {
        try {
            callHistoryService.updatePrescription(cid, prescription);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error updating prescription");
        }
    }
    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<List<CallHistory>> getCallHistoryForDoctor(
            @PathVariable Long doctorId,
            @RequestParam(required = false) LocalDate date,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime) {
        List<CallHistory> callHistoryList;
        if (date != null) {
            // If date is provided, fetch call history for that specific date
            if (startTime != null && endTime != null) {
                // If startTime and endTime are also provided, filter call history within the specified time range
                LocalTime start = LocalTime.parse(startTime);
                LocalTime end = LocalTime.parse(endTime);
                callHistoryList = callHistoryService.getCallHistoryForDoctorTodayWithinTimeRange(doctorId, date, start, end);
            } else {
                // If startTime and endTime are not provided, fetch all call history for the doctor for that specific date
                callHistoryList = callHistoryService.getCallHistoryForDoctorToday(doctorId, date);
            }
        } else {
            // If date is not provided, fetch call history for today
            if (startTime != null && endTime != null) {
                // If startTime and endTime are provided, filter call history within the specified time range for today
                LocalTime start = LocalTime.parse(startTime);
                LocalTime end = LocalTime.parse(endTime);
                callHistoryList = callHistoryService.getCallHistoryForDoctorTodayWithinTimeRange(doctorId, LocalDate.now(), start, end);
            } else {
                // If startTime and endTime are not provided, fetch all call history for the doctor for today
                callHistoryList = callHistoryService.getCallHistoryForDoctorToday(doctorId, LocalDate.now());
            }
        }
        List<CallHistory> filteredCallHistoryList = callHistoryList.stream()
                .filter(call -> call.getEndTime() == null)
                .collect(Collectors.toList());
        return new ResponseEntity<>(filteredCallHistoryList, HttpStatus.OK);
    }



    @GetMapping("/doctor/{doctorId}/all")
    public ResponseEntity<List<CallHistory>> getAllCallHistoryForDoctor(
            @PathVariable Long doctorId, @RequestParam(required = false) Integer month, @RequestParam(required = false) Integer day, @RequestParam(required = false) Integer year) {

        List<CallHistory> callHistoryList = callHistoryService.getAllCallHistoryForDoctor(doctorId);
        LocalDate today = LocalDate.now();
        callHistoryList.removeIf(call -> call.getCallDate().isAfter(today));
        List<CallHistory> filteredList = new ArrayList<>(callHistoryList);

        // Filter call history list to include records before toda
        int currentYear = today.getYear();
        callHistoryList.removeIf(call -> call.getCallDate().getYear() != currentYear);
//        int currentmonth = today.getMonthValue();
//        callHistoryList.removeIf(call -> call.getCallDate().getMonthValue() != currentmonth);

        // Filter call history list based on the provided month, day, and year parameters

        if (year != null && year >= 2020 && year <= 2050) {
            filteredList.removeIf(call -> call.getCallDate().getYear() != year);
            if (month != null && month >= 1 && month <= 12) {
                filteredList.removeIf(call -> call.getCallDate().getMonthValue() != month);
                if (day != null && day >= 1 && day <= 31) {
                    filteredList.removeIf(call -> call.getCallDate().getDayOfMonth() != day);
                }
            }
        }

        // Return filtered call history list
        return new ResponseEntity<>(filteredList, HttpStatus.OK);

    }

    @PostMapping("/schedule")
    public ResponseEntity<String> scheduleCall(@RequestBody CallHistory callHistory) {
        try {
            callHistoryService.saveCallHistory(callHistory);
            return ResponseEntity.ok("Call scheduled successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error scheduling call");
        }
    }

    @GetMapping("/doctor/un/{doctorId}")
    public ResponseEntity<List<CallHistory>> getunfilteredCallHistoryForDoctor(
            @PathVariable Long doctorId,
            @RequestParam(required = false) LocalDate date,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime) {
        List<CallHistory> callHistoryList;
        if (date != null) {
            // If date is provided, fetch call history for that specific date
            if (startTime != null && endTime != null) {
                // If startTime and endTime are also provided, filter call history within the specified time range
                LocalTime start = LocalTime.parse(startTime);
                LocalTime end = LocalTime.parse(endTime);
                callHistoryList = callHistoryService.getCallHistoryForDoctorTodayWithinTimeRange(doctorId, date, start, end);
            } else {
                // If startTime and endTime are not provided, fetch all call history for the doctor for that specific date
                callHistoryList = callHistoryService.getCallHistoryForDoctorToday(doctorId, date);
            }
        } else {
            // If date is not provided, fetch call history for today
            if (startTime != null && endTime != null) {
                // If startTime and endTime are provided, filter call history within the specified time range for today
                LocalTime start = LocalTime.parse(startTime);
                LocalTime end = LocalTime.parse(endTime);
                callHistoryList = callHistoryService.getCallHistoryForDoctorTodayWithinTimeRange(doctorId, LocalDate.now(), start, end);
            } else {
                // If startTime and endTime are not provided, fetch all call history for the doctor for today
                callHistoryList = callHistoryService.getCallHistoryForDoctorToday(doctorId, LocalDate.now());
            }
        }
        List<CallHistory> unfilteredCallHistoryList = callHistoryList.stream()
                .filter(call -> call.getEndTime() != null)
                .collect(Collectors.toList());

        return new ResponseEntity<>(unfilteredCallHistoryList, HttpStatus.OK);
    }

    @GetMapping("/fetchcalls")
    public List<?> getCallHistoryByDoctorAndPatient(
            @RequestParam Long did,
            @RequestParam Long pid) {
        Doctor doctor = doctorRepository.findById(did).orElse(null);
        Patient patient = patientRepository.findById(pid).orElse(null);
        if (doctor != null && patient != null) {
            List<?> callHistoryList = callHistoryRepository.findByDoctorAndPatient(doctor, patient);

            // Filter the list based on endTime != null
            List<?> filteredList = callHistoryList.stream()
                    .filter(callHistory -> ((CallHistory) callHistory).getEndTime() != null)
                    .collect(Collectors.toList());

            return filteredList;
        } else {
            // Handle case when doctor or patient not found
            return null;
        }
    }

    @PostMapping("/join-room") // Update incoming call
    public ResponseEntity<?> joinRoom(@RequestBody RoomJoinRequest request) {
        String helpline = request.getDoctorPhoneNumber();
        System.out.print(helpline);
        if (helpline != null) {
            // Check if there are available doctors in the queue
            if (!availableDoctorsQueue.isEmpty()) {
                // Dequeue the first available doctor
                Doctor availableDoctor = availableDoctorsQueue.poll();
                System.out.println("Inside callhistory joinroom");

                // Update the incoming call for the dequeued doctor
                availableDoctor.setIncomingCall(request.getPatientPhoneNumber());
                availableDoctor.setAvailability(false);
                doctorService.saveDoctor(availableDoctor);
                Patient patient = patientRepository.findByPhoneNumber(request.getPatientPhoneNumber());
                patient.setAssigneddoctor(availableDoctor.getId());
                patientService.savePatient(patient);

                // Return the doctor to the available doctors queue if needed
                // (for scenarios where the doctor was not immediately available
                String roomnumber = helpline+ Long.toString(availableDoctor.getId());
                System.out.print(roomnumber);
                return ResponseEntity.ok(roomnumber);
            } else {
                // No available doctors, patient will be added to the waiting queue
                Patient patient = patientRepository.findByPhoneNumber(request.getPatientPhoneNumber());
                String sorry = "0";
                if (patient != null) {
                    callHandlingService.addWaitingPatient(patient);
                    patient.setWstatus(true);
                    patientService.savePatient(patient);
                    return ResponseEntity.ok(sorry);
                } else {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Patient not found");
                }

            }
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Doctor not found");
        }
    }

    @PutMapping("/{patientId}/rjctcall")
    public ResponseEntity<?> patientrjctCall(@PathVariable("patientId") Long patientId) {
        Patient patient = patientService.findById(patientId);
        if (patient != null) {
            // Set incoming call to null
            patient.setAssigneddoctor(null);
            patient.setPincomingcall(null);
            patient.setAcptstatus(0L);
            patientService.savePatient(patient);

            // Add the doctor back to the available doctors queue
//            availableDoctorsQueue.add(doctor);

            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Patient not found");
        }
    }
    @PutMapping("/{doctorId}/reject-call")
    public ResponseEntity<?> rejectCall(@PathVariable("doctorId") Long doctorId) {
        Doctor doctor = doctorService.findById(doctorId);
        if (doctor != null) {
            // Set incoming call to null
            doctor.setIncomingCall(null);
            doctorService.saveDoctor(doctor);

            // Add the doctor back to the available doctors queue
//            availableDoctorsQueue.add(doctor);

            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Doctor not found");
        }
    }

    @Scheduled(fixedDelay = 1000)
    public ResponseEntity<?> checkforpatient(){

            while(!availableDoctorsQueue.isEmpty() && !waitingPatientsQueue.isEmpty())
            {
                Doctor availabledoctor = availableDoctorsQueue.poll();
                Patient avalablepatient = waitingPatientsQueue.poll();
                availabledoctor.setIncomingCall(avalablepatient.getPhoneNumber());
                availabledoctor.setAvailability(false);
                availabledoctor.setCallbackavailabilty(false);
                doctorService.saveDoctor(availabledoctor);
                avalablepatient.setWstatus(false);
                avalablepatient.setCallbackstatus(false);
                avalablepatient.setAssigneddoctor(availabledoctor.getId());
                patientService.savePatient(avalablepatient);
            }
            System.out.println("Checked for the present details");
            return ResponseEntity.ok("checked for the status");

    }

    @GetMapping("/checkassigneddoctor")
    public ResponseEntity<?> getAssignedDoctorForPatient(@RequestParam("patientphonenumber") String phonenumber){
        Patient patient = patientRepository.findByPhoneNumber(phonenumber);
        if(patient != null) {
            Long assignedDoctorId = patient.getAssigneddoctor();
            System.out.println(assignedDoctorId);
            return ResponseEntity.ok().body(assignedDoctorId);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Patient not found");
        }
    }

    @GetMapping("/waiting")
    public ResponseEntity<List<Patient>> getPatientsWithWStatusAndAssignedDoctor(@RequestParam("doctorId") String doctorId) {
        Long longValue = Long.parseLong(doctorId);
        List<Patient> patientsWithWStatusAndAssignedDoctor = patientRepository.findBywstatusAndAssigneddoctor(false, longValue);
        return new ResponseEntity<>(patientsWithWStatusAndAssignedDoctor, HttpStatus.OK);
    }


    @GetMapping("/avblstatus/{doctorId}")
    public ResponseEntity<?> getAvailability(@PathVariable("doctorId") Long doctorId) {
        Doctor doctor = doctorService.findById(doctorId);
        if (doctor != null) {
            boolean availability = doctor.isAvailability();
            return ResponseEntity.ok().body(availability);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Doctor not found");
        }
    }

    @GetMapping("/cbavblstatus/{doctorId}")
    public ResponseEntity<?> getcallback(@PathVariable("doctorId") Long doctorId) {
        Doctor doctor = doctorService.findById(doctorId);
        if (doctor != null) {
            boolean callback = doctor.isCallbackavailabilty();
            return ResponseEntity.ok().body(callback);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Doctor not found");
        }
    }


    @PutMapping("/availabilitystatus/{doctorId}")
    public ResponseEntity<?> changeavailability(@PathVariable("doctorId") Long doctorId, @RequestParam("avbl") String avbl)
    {
        Doctor doctor = doctorService.findById(doctorId);
        if(doctor != null)
        {
            if ("false".equals(avbl)) {
                doctor.setAvailability(false);
                doctorService.saveDoctor(doctor);
                Doctor doctorToRemove = null;
                for (Doctor d : availableDoctorsQueue) {
                    if (d.getId().equals(doctorId)) {
                        doctorToRemove = d;
                        break; // Exit loop once the doctor is found
                    }
                }

// If the doctor is found, remove it from the queue
                if (doctorToRemove != null) {
                    availableDoctorsQueue.remove(doctorToRemove);
                    System.out.println("Doctor removed from the queue:");
                    System.out.println(doctorToRemove.getId() + ": " + doctorToRemove.getName() + " - Availability: " + doctorToRemove.isAvailability());
                } else {
                    System.out.println("Doctor with ID " + doctorId + " not found in the queue.");
                }
            } else if ("true".equals(avbl)) {
                doctor.setAvailability(true);
                doctorService.saveDoctor(doctor);
                availableDoctorsQueue.add(doctor);
                System.out.println("Available Doctors Queue:");
                for (Doctor d : availableDoctorsQueue) {
                    System.out.println(d.getId() + ": " + d.getName() + " - Availability: " + d.isAvailability());
                }
            }


            System.out.println("Updated");

            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Doctor not found");
        }
    }

    @PutMapping("/callbackavailabilitystatus/{doctorId}")
    public ResponseEntity<?> changecallbackavailability(@PathVariable("doctorId") Long doctorId, @RequestParam("cavbl") String cavbl)
    {
        Doctor doctor = doctorService.findById(doctorId);
        if(doctor != null)
        {
            if ("false".equals(cavbl)) {
                doctor.setCallbackavailabilty(false);
                doctorService.saveDoctor(doctor);
                Doctor doctorToRemove = null;
                for (Doctor d : callbackDoctorQueue) {
                    if (d.getId().equals(doctorId)) {
                        doctorToRemove = d;
                        break; // Exit loop once the doctor is found
                    }
                }

// If the doctor is found, remove it from the queue
                if (doctorToRemove != null) {
                    callbackDoctorQueue.remove(doctorToRemove);
                    System.out.println("Doctor removed from the queue:");
                    System.out.println(doctorToRemove.getId() + ": " + doctorToRemove.getName() + " - CallbackAvailability: " + doctorToRemove.isCallbackavailabilty());
                } else {
                    System.out.println("Doctor with ID " + doctorId + " not found in the queue.");
                }
            } else if ("true".equals(cavbl)) {
                doctor.setCallbackavailabilty(true);
                doctorService.saveDoctor(doctor);
                callbackDoctorQueue.add(doctor);
                System.out.println("Available Doctors Queue:");
                for (Doctor d : callbackDoctorQueue) {
                    System.out.println(d.getId() + ": " + d.getName() + " - CallbackAvailability: " + d.isCallbackavailabilty());
                }
            }


            System.out.println("Updated");

            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Doctor not found");
        }
    }

    @GetMapping("/waitingqueue/removepatient")
    public ResponseEntity<?> changeWaitingQueue(@RequestParam("ptphonenumber") String phonenumber)
    {
        Patient patient = patientRepository.findByPhoneNumber(phonenumber);
        if(patient!=null)
        {
            Patient patienttoremove = null;
            for(Patient p: waitingPatientsQueue) {
                if (p.getId().equals(patient.getId())) {
                    patienttoremove = p;
                    break; // Exit loop once the doctor is found
                }
            }
            if (patienttoremove != null) {
                waitingPatientsQueue.remove(patienttoremove);
                System.out.println("Patient removed from the queue:");
                System.out.println(patienttoremove.getId() + ": " + patienttoremove.getName());
            } else {
                System.out.println("Patient with ID " + patient.getId() + " not found in the queue.");
            }
            for (Patient p : waitingPatientsQueue) {
                System.out.println(p.getId() + ": " + p.getName() + " - Waiting Status: " + p.isWstatus());
            }
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Patient not found");
        }
    }
    @PutMapping("/{patientId}") // Doctor can access
    public ResponseEntity<Patient> updatePatient(@PathVariable Long patientId, @RequestBody Pdetails pdetails) {
        Patient patient = patientService.updatePatient(patientId, pdetails );
        return ResponseEntity.ok(patient);
    }

    @Scheduled(cron = "0 35 15 * * ?", zone = "Asia/Kolkata")
    public ResponseEntity<String> sendEmailNotificationsForToday() {
        try {
            List<CallHistory> appointmentsToday = callHistoryService.getCallHistoryForToday();
            System.out.print(appointmentsToday);
            for (CallHistory appointment : appointmentsToday) {
                emailService.sendAppointmentNotification(appointment);
            }
            return ResponseEntity.ok("Email notifications sent successfully.");
        } catch (MessagingException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error sending email notifications.");
        }
    }
    @PostMapping("/addpt")
    public ResponseEntity<Patient> createPatient(@RequestBody Patient patient) {
        Patient createdPatient = patientService.createPatient(patient);
        return new ResponseEntity<>(createdPatient, HttpStatus.CREATED);
    }

    @PutMapping("/set-pincoming-call")
    public ResponseEntity<String> setPatientIncomingCall(@RequestBody SetIncomingCallRequest request) {
        try {
            // Here you would set the pincomingcall of the patient with the specified ID
            // to the desired value (e.g., "18002347" + doctorId).
            // This is just a placeholder implementation.
            Long patientId = request.getPatientId();
            Long doctorId = request.getDoctorId();

            String pincomingCall = "18002347" + String.valueOf(doctorId);

            Patient patient = patientService.findById(patientId);
            patient.setPincomingcall(pincomingCall);
            patientService.savePatient(patient);


            return ResponseEntity.ok("pincomingcall set successfully for patient ID: " + patientId);
        } catch (Exception e) {
            // Return an error response if something goes wrong.
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to set pincomingcall: " + e.getMessage());
        }
    }

    @Scheduled(fixedDelay = 1000)
    public ResponseEntity<?> checkforcallbackpatient(){

        while(!callbackDoctorQueue.isEmpty() && !callbackPatientQueue.isEmpty())
        {
            Doctor availabledoctor = callbackDoctorQueue.poll();
            Patient avalablepatient = callbackPatientQueue.poll();
            String pincomingCall = "18002347" + String.valueOf(availabledoctor.getId());
            availabledoctor.setCallbackavailabilty(false);
            availabledoctor.setAvailability(false);
            doctorService.saveDoctor(availabledoctor);
            avalablepatient.setWstatus(false);
            avalablepatient.setCallbackstatus(false);
            avalablepatient.setAssigneddoctor(availabledoctor.getId());
            patientService.savePatient(avalablepatient);
        }
        System.out.println("Checked for the callback details");
        return ResponseEntity.ok("checked for the status");

    }

    @PutMapping("/callback")
    public void changecallback(@RequestParam("ptphonenumber") String phonenumber,@RequestParam("needcallback") String needcallback)
    {
        Patient patient = patientRepository.findByPhoneNumber(phonenumber);
        System.out.println("Entered the changing callbackstatus function");
        if(patient!=null)
        {
            if("false".equals(needcallback))
            {
                patient.setCallbackstatus(false);
                patientService.savePatient(patient);
            } else if ("true".equals(needcallback)) {
                patient.setCallbackstatus(true);
                callbackPatientQueue.add(patient);
                patientService.savePatient(patient);
            }
        }
    }

    @GetMapping("/ptcallback")
    public ResponseEntity<?> getcallback(@RequestParam("ptphonenumber") String phonenumber)
    {
        Patient patient = patientRepository.findByPhoneNumber(phonenumber);
        System.out.println("Entered the changing callbackstatus function");
        if (patient != null) {
            boolean callbackStatus = patient.isCallbackstatus(); // Retrieve the callback status
            return ResponseEntity.ok(callbackStatus); // Return the callback status
        } else {
            return ResponseEntity.notFound().build(); // Return 404 if patient not found
        }
    }

    @GetMapping("/getbooked/{doctorId}")
    public ResponseEntity<List<CallHistory>> getBookedcallsForDoctor(
            @PathVariable Long doctorId) {
        List<CallHistory> callHistoryList;
        callHistoryList = callHistoryService.getCallHistoryForDoctorToday(doctorId, LocalDate.now());
        List<CallHistory> filteredCallHistoryList = callHistoryList.stream()
                .filter(call -> call.getEndTime() == null)
                .collect(Collectors.toList());
        return new ResponseEntity<>(filteredCallHistoryList, HttpStatus.OK);
    }

    @PutMapping("/insertback")
    public void insertbackpatient(@RequestParam("patientId") Long patientId)
    {
        Patient patient = patientService.findById(patientId);
        if(patient!=null)
        {
            patient.setAssigneddoctor(null);
            patient.setCallbackstatus(true);
            patientService.savePatient(patient);
            callbackPatientQueue.add(patient);
        }
        else {
            System.out.println("Patient not found");
        }
    }

    @GetMapping("/ptacptstatus")
    public ResponseEntity<?> getacptststusofpatient(@RequestParam("patientId") Long patientId)
    {
        Patient patient = patientService.findById(patientId);
        if(patient!=null)
        {
            return ResponseEntity.ok(patient.getAcptstatus());
        }
        else {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/putacptstatus")
    public void updateacptstatus(@RequestParam("ptnphonenumber") String ptnphonenumber)
    {

        Patient patient = patientRepository.findByPhoneNumber(ptnphonenumber);
        if(patient!=null)
        {
            patient.setAcptstatus(1L);
            patientService.savePatient(patient);
        }
        else {
            System.out.println("Patient not found");
        }
    }

    @PutMapping("/putrjctstatus")
    public void decreaseacptstatus(@RequestParam("pntphonenumber") String pntphonenumber)
    {

        Patient patient = patientRepository.findByPhoneNumber(pntphonenumber);
        if(patient!=null)
        {
            patient.setAcptstatus(-1L);
            patientService.savePatient(patient);
        }
        else {
            System.out.println("Patient not found");
        }
    }

    @PutMapping("/scheduleconsent")
    public void updatescheduleconsent(@RequestParam("callId") Long callId)
    {
        Optional<CallHistory> optionalCallHistory = callHistoryRepository.findById(callId);

        if (optionalCallHistory.isPresent()) {
            CallHistory callHistory = optionalCallHistory.get();
            callHistory.setScheduleconsent(true);
            callHistoryRepository.save(callHistory); // Save the updated call history
        } else {
            // Handle the case where the call history with the given ID is not found
            // For example, you can log an error or throw an exception
            System.err.println("Call history with ID " + callId + " not found.");
        }
    }

    @PutMapping("/changerecordconsent")
    public void updaterecordconsent(@RequestParam("cid") Long cid)
    {
        Optional<CallHistory> optionalCallHistory1 = callHistoryRepository.findById(cid);
        if(optionalCallHistory1.isPresent()) {
            CallHistory callHistory = optionalCallHistory1.get();
            callHistory.setRecordingconsent(true);
            callHistoryRepository.save(callHistory);
        }
        else {
            System.err.println("Call history with ID " + cid + " not found.");
        }
    }

    @GetMapping("/getscheduleconsent")
    public ResponseEntity<?> getScheduleconsent(@RequestParam("callId") Long callId)
    {
        Optional<CallHistory> optionalCallHistory = callHistoryRepository.findById(callId);
        if (optionalCallHistory.isPresent()) {
            CallHistory callHistory = optionalCallHistory.get();
            boolean scheduleConsent = callHistory.isScheduleconsent(); // Assuming 'scheduleconsent' is a boolean field
            return ResponseEntity.ok(scheduleConsent); // Return the scheduleconsent value
        } else {
            // Return 404 Not Found if the call history with the given ID is not found
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/getrecordingconsent")
    public ResponseEntity<?> getRecordingconsent(@RequestParam("callId") Long callId)
    {
        Optional<CallHistory> optionalCallHistory = callHistoryRepository.findById(callId);
        if (optionalCallHistory.isPresent()) {
            CallHistory callHistory = optionalCallHistory.get();
            boolean scheduleConsent = callHistory.isRecordingconsent(); // Assuming 'scheduleconsent' is a boolean field
            return ResponseEntity.ok(scheduleConsent); // Return the scheduleconsent value
        } else {
            // Return 404 Not Found if the call history with the given ID is not found
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/getendcallstatus")
    public ResponseEntity<?> getEndcallstatus(@RequestParam("callid") Long callid)
    {
        Optional<CallHistory> optionalCallHistory = callHistoryRepository.findById(callid);
        if(optionalCallHistory.isPresent()) {
            CallHistory callHistory = optionalCallHistory.get();
            return  ResponseEntity.ok(callHistory.getEndTime());
        }
        else {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/put-doc/{doctorId}")
    public void updDocApt(@PathVariable("doctorId") Long doctorId, @RequestParam("pemail") String pemail) {
        System.out.println("CaLLLLLLLLLLLLLLLLLLLLLLl");
        Doctor doctor = doctorService.findById(doctorId);
        Patient patient = patientService.findByEmail(pemail);
        String phonenumber= patient.getPhoneNumber();
        System.out.println("\n\n"+phonenumber+"\n\n");
        if(doctor!=null )
        {
            try {
                doctor.setIncomingCall((phonenumber));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            doctor.setAvailability(false);
            doctorService.saveDoctor(doctor);
            System.out.println("Updated");
        }
        else {
            System.out.println("doctor not found");
        }
    }

    @GetMapping("/all")
    public List<CallHistory> getAllCallHistory() {
        System.out.println("\nInside Call History All : GET\n");
        return callHistoryService.getAllCallHistory();
    }
    //murli
    @GetMapping("/count")
    public ResponseEntity<Long> getTotalAppointmentsCount() {
        List<CallHistory> allCallHistory = callHistoryService.getAllCallHistory();
        long totalCount = allCallHistory.size();
        return ResponseEntity.ok(totalCount);
    }
    //murli
    @GetMapping("/{patientId}")
    public ResponseEntity<List<CallHistory>> getCallHistoryByPatientId(@PathVariable Long patientId) {
        List<CallHistory> callHistoryList = callHistoryService.getCallHistoryByPatientId(patientId);
        return ResponseEntity.ok(callHistoryList);
    }
    //murli
    @GetMapping("/doctor/{doctorId}/callhistory")
    public ResponseEntity<List<CallHistory>> getCallHistoryForDoctors(
            @PathVariable Long doctorId) {
        List<CallHistory> callHistoryList = callHistoryService.getCallHistoryForDoctor(doctorId);
        return ResponseEntity.ok(callHistoryList);
    }
    //murli
    @PostMapping("/doctor/add")
    public ResponseEntity<Long> addAppointment(@RequestBody CallHistory callHistory) {
        try {
            CallHistory savedCallHistory = callHistoryService.saveCallHistory(callHistory);
            return ResponseEntity.ok(savedCallHistory.getId());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(-1L); // Return -1 if failed
        }
    }
    //murli
    @PutMapping("/{id}/update") // Update appointment details
    public ResponseEntity<?> updateAppointmentDetails(
            @PathVariable Long id,
            @RequestBody DateTime dateTime) {
        try {
            callHistoryService.updateAppointmentDetails(id, dateTime.getCallDate(), dateTime.getCallTime(), dateTime.getEndTime());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error updating appointment details");
        }
    }
    //murli
    @GetMapping("/doctor/{doctorId}/today")
    public ResponseEntity<List<CallHistory>> getTodayAppointmentsForDoctor(
            @PathVariable Long doctorId) {
        List<CallHistory> callHistoryList = callHistoryService.getTodayAppointmentsForDoctor(doctorId);
        return ResponseEntity.ok(callHistoryList);
    }
    //MURLI
// Count appointments for a specific doctor
    @GetMapping("/doctor/{doctorId}/appointments/count")
    public ResponseEntity<Long> countAppointmentsByDoctorId(@PathVariable Long doctorId) {
        Long count = callHistoryService.countAppointmentsByDoctorId(doctorId);
        return ResponseEntity.ok(count);
    }
    //murli
    @GetMapping("/doctor/{doctorId}/patient/count")
    public ResponseEntity<Long> countPatientsByDoctorId(@PathVariable Long doctorId) {
        Long count = callHistoryService.countPatientsByDoctorId(doctorId);
        return ResponseEntity.ok(count);
    }



}
