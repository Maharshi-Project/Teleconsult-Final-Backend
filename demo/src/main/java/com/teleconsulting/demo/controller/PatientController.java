package com.teleconsulting.demo.controller;

import com.teleconsulting.demo.dto.Ddetails;
import com.teleconsulting.demo.dto.DoctorRating;
import com.teleconsulting.demo.exception.UserNotFoundException;
import com.teleconsulting.demo.model.CallHistory;
import com.teleconsulting.demo.model.Doctor;
import com.teleconsulting.demo.model.Patient;
import com.teleconsulting.demo.repository.PatientRepository;
import com.teleconsulting.demo.service.CallHistoryService;
import com.teleconsulting.demo.service.DoctorService;
import com.teleconsulting.demo.service.PatientService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.AbstractMap;
import java.util.List;
import java.util.Optional;

import static com.teleconsulting.demo.controller.CallHistoryController.decrypt;
import static com.teleconsulting.demo.controller.CallHistoryController.encrypt;

@RestController
@CrossOrigin("http://localhost:3000")
@RequestMapping("/patient")
public class PatientController {
    private final PatientService patientService;
    private final PatientRepository patientRepository;
    private final CallHistoryService callHistoryService;
    private final DoctorService doctorService;


    public PatientController(PatientService patientService, PatientRepository patientRepository, CallHistoryService callHistoryService, DoctorService doctorService) {
        this.patientService = patientService;
        this.patientRepository = patientRepository;
        this.callHistoryService = callHistoryService;
        this.doctorService = doctorService;
    }
//    @PostMapping("/add") // Patient
//    public String add(@RequestBody Patient patient)
//    {
//        patientService.savePatient(patient);
//        return "New Patient Added";
//    }

    @GetMapping("/patient/{id}") // Get Patient details by its ID
    Patient getUserById(@PathVariable Long id) {
        return patientRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
    }
    @PutMapping("/patient/{id}") //
    Patient updatePatient(@RequestBody Patient newPatient, @PathVariable Long id) {
        return patientRepository.findById(id)
                .map(Patient -> {
                    Patient.setGender(newPatient.getGender());
                    Patient.setName(newPatient.getName());
                    Patient.setPhoneNumber(newPatient.getPhoneNumber());
                    return patientRepository.save(Patient);
                }).orElseThrow(() -> new UserNotFoundException(id));
    }

    @PostMapping
    public ResponseEntity<Patient> createPatient(@RequestBody Patient patient) {
        Patient createdPatient = patientService.createPatient(patient);
        return new ResponseEntity<>(createdPatient, HttpStatus.CREATED);
    }
    @GetMapping(params = "phoneNumber") // Get patient details from phone number
    public ResponseEntity<Patient> getPatientByPhoneNumber(@RequestParam String phoneNumber) {
        Patient patient = patientService.getPatientByPhoneNumber(phoneNumber);
        return ResponseEntity.ok(patient);
    }
    @GetMapping("/id") // Get ID from phone number
    public ResponseEntity<Patient> getPatientById(@RequestParam String phoneNumber) {
        Patient patient = patientService.getPatientByPhoneNumber(phoneNumber);
        return ResponseEntity.ok(patient);
    }

    @GetMapping("/patient-details/{email}")
    public ResponseEntity<?> getUserDetailsByEmail(@PathVariable String email) {
        System.out.println("\n Inside getUserFrom Email \n" + email);
        Optional<Patient> userDetails = patientService.getUserByEmail(email);
        if (userDetails.isPresent()) {
            return ResponseEntity.ok(userDetails.get());
        } else {
            return ResponseEntity.notFound().build(); // User not found
        }
    }

    @GetMapping("/getsnrdoctors")
    List<Ddetails> getSnrDoctors() {
        return doctorService.getSnrDoctors();
    }

    @GetMapping("/time-slots")
    public ResponseEntity<List<String>> getTimeSlotsForDoctorAndDate(@RequestParam("doctorId") Long doctorId, @RequestParam("date") String date){
        System.out.println("/n Inside time-slots /n");
        return callHistoryService.getDoctorTimeSlots(doctorId, date);
    }

    @PostMapping("/book-apt")
    public ResponseEntity<String> bookApt(@RequestBody CallHistory callHistory) {
        Long id = callHistory.getId();
        LocalDate date = callHistory.getCallDate();
        LocalTime time = callHistory.getCallTime();
        AbstractMap.SimpleEntry<Long, AbstractMap.SimpleEntry<LocalDate, LocalTime>> key = new AbstractMap.SimpleEntry<>(id, new AbstractMap.SimpleEntry<>(date, time));

        // Check if the key exists in the global map
        boolean isKeyPresent = GlobalMap.globalMap.containsKey(key);

        if (isKeyPresent) {
            return ResponseEntity.ok("Slot Already Booked!");
        } else {
            try {
                // Add the key to the global map
                GlobalMap.globalMap.put(key, true);

                // Other logic for booking the appointment...
                callHistoryService.saveCallHistory(callHistory);
                return ResponseEntity.ok("Appointment booked successfully");
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error booking Appointment");
            }
        }
    }

    @GetMapping("/up-apt")
    public List<Object> getUpApt(@RequestParam("patientId") Long patientId){
        System.out.println("\n Inside up-apt \n");
        return callHistoryService.getUpAptPat(patientId);
    }

    @GetMapping("/past-apt")
    public List<Object> getPastApt(@RequestParam("patientId") Long patientId){
        System.out.println("\n Inside past-apt \n");
        return callHistoryService.getPastAptPat(patientId);
    }
    //    @PostMapping("/rateDoc")
//    public void updateDocRating(@RequestBody DoctorRatings doctorRating) {
//        System.out.println("\nInside Patient Controller calling update Rating\n");
//        doctorService.updateRating(doctorRating.getId(),doctorRating.getRating());
//}
    //murli
    @GetMapping("/all")
    public ResponseEntity<List<Patient>> getAllPatients() {
        List<Patient> patients = patientRepository.findAll();
        for (Patient patient : patients) {
            patient.setPassword("none"); // Exclude password field
        }
        System.out.println("patuentapi");
        return ResponseEntity.ok(patients);
    }
    // murli
    @GetMapping("/count")
    public ResponseEntity<Long> countPatients() {
        Long count = patientService.countPatients();
        return ResponseEntity.ok(count);
    }
    @PostMapping("/rateDoc")
    public void updateDocRating(@RequestBody DoctorRating doctorRating) {
        System.out.println("\nInside Patient Controller calling update Rating\n");
        doctorService.updateRating(doctorRating.getId(),doctorRating.getTotalRating());
    }
    @GetMapping("/getAllRating")
    public ResponseEntity<List<DoctorRating>> getAllDoctorRating() {
        System.out.println("\nInside Patient Controller calling get All rating\n");
        List<DoctorRating> ratings = doctorService.getAllRatings();
        return ResponseEntity.ok(ratings);
    }

    @GetMapping("/online-doc")
    public List<Doctor> getOnlineDoctors(){
        return doctorService.getOnlineDoctorsforPat();
    }

    @GetMapping("/today-apt")
    public List<Object> getTodayApt(@RequestParam("patientId") Long patientId){
        System.out.println("\n Inside Today-apt \n");
        return callHistoryService.getTodayAptPat(patientId);
    }

    @PutMapping("/put-doc")
    public void updDocApt(@RequestParam("doctorId") Long doctorId, @RequestParam("phoneNumber") String pemail) throws Exception {
        Doctor doctor = doctorService.findById(doctorId);
        Patient patient = patientService.findByEmail(pemail);
        String phoneNumber = decrypt(patient.getPhoneNumber());
        System.out.println("\n\n"+phoneNumber+"\n\n");
        if(doctor!=null )
        {
            doctor.setIncomingCall(encrypt(phoneNumber));
            doctor.setAvailability(false);
            doctorService.saveDoctor(doctor);
            System.out.println("Updated");
        }
        else {
            System.out.println("doctor not found");
        }
    }

}