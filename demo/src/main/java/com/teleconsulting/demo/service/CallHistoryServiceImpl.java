package com.teleconsulting.demo.service;

import com.teleconsulting.demo.model.CallHistory;
import com.teleconsulting.demo.model.Doctor;
import com.teleconsulting.demo.model.Patient;
import com.teleconsulting.demo.repository.CallHistoryRepository;
import com.teleconsulting.demo.repository.DoctorRepository;
import com.teleconsulting.demo.repository.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CallHistoryServiceImpl implements CallHistoryService{
    @Autowired
    private CallHistoryRepository callHistoryRepository;
    @Autowired
    private DoctorService doctorService;
    @Autowired
    private DoctorRepository doctorRepository;
    @Autowired
    private PatientRepository patientRepository;

    @Override
    public List<CallHistory> getCallHistoryForDoctorsWithSdid(Long sdid) {
        Optional<Doctor> doctor = doctorRepository.findById(sdid);
        if(doctor.isEmpty()) {
            return null;
        } else if(doctor.get().isDeleteFlag()) {
            return null;
        }
        List<Long> doctorIds = doctorService.getDoctorsBySupervisorId(sdid).stream()
                .map(Doctor::getId)
                .collect(Collectors.toList());
        return callHistoryRepository.findByDoctorIdIn(doctorIds);
    }


    @Override
    public List<CallHistory> getCallHistoryForDoctor(Long doctorId) {
        Optional<Doctor> doctor = doctorRepository.findById(doctorId);
        if(doctor.isPresent()) {
            return callHistoryRepository.findByDoctorId(doctorId);
        }
        else {
            return null;
        }
    }
    @Override
    public CallHistory saveCallHistory(CallHistory callHistory) {
        return callHistoryRepository.save(callHistory);
    }

    @Override
    public List<CallHistory> getCallHistoryForToday() {
        return callHistoryRepository.findByCallDate(LocalDate.now());
    }

    @Override
    public List<CallHistory> getCallHistoryForTodayWithinTimeRange(LocalTime startTime, LocalTime endTime) {
        LocalDate today = LocalDate.now();
        return callHistoryRepository.findByCallDateAndCallTimeBetween(today, startTime, endTime);
    }
    @Override
    public List<CallHistory> getCallHistoryForDoctorToday(Long doctorId, LocalDate date) {
        Optional<Doctor> doctor = doctorRepository.findById(doctorId);
        if(doctor.isPresent()) {
            return callHistoryRepository.findByDoctorIdAndCallDate(doctorId, date);
        }
        else {
            return null;
        }
    }

    @Override
    public List<CallHistory> getCallHistoryForDoctorTodayWithinTimeRange(Long doctorId,LocalDate date, LocalTime startTime, LocalTime endTime) {
        LocalDate today = date;
        Optional<Doctor> doctor = doctorRepository.findById(doctorId);
        if(doctor.isPresent()) {
            return callHistoryRepository.findByDoctorIdAndCallDateAndCallTimeBetween(doctorId, today, startTime,endTime);
        }
        else {
            return null;
        }
}
    @Override
    public void updatePrescription(Long id, String prescription) {
        Optional<CallHistory> optionalCallHistory = callHistoryRepository.findById(id);
        if (optionalCallHistory.isPresent()) {
            CallHistory callHistory = optionalCallHistory.get();
            callHistory.setPrescription(prescription);
            callHistoryRepository.save(callHistory);
        } else {
            throw new IllegalArgumentException("Call history entry with ID " + id + " not found");
        }
}

    @Override
    public void updateendtime(Long id, LocalTime endtime) {
        Optional<CallHistory> optionalCallHistory = callHistoryRepository.findById(id);
        if(optionalCallHistory.isPresent()) {
            CallHistory callHistory = optionalCallHistory.get();
            callHistory.setEndTime(endtime);
            callHistoryRepository.save(callHistory);
        }
        else {
            throw new IllegalArgumentException("Call history entry with ID " + id + " not found");

        }
    }

    @Override
    public Long getPatientIdFromCallHistory(Long id) {
        Optional<CallHistory> callHistoryOptional = callHistoryRepository.findById(id);
        if (callHistoryOptional.isPresent()) {
            CallHistory callHistory = callHistoryOptional.get();
            Patient patient = callHistory.getPatient();
            if (patient != null && !patient.isDeleteFlag()) {
                return patient.getId();
            } else {
                throw new IllegalStateException("Patient ID not found for call history entry with ID " + id);
            }
        } else {
            throw new IllegalArgumentException("Call history entry with ID " + id + " not found");
        }}

    @Override
    public List<CallHistory> getAllCallHistoryForDoctor(Long doctorId) {
        Optional<Doctor> doctor = doctorRepository.findById(doctorId);
        if(doctor.isEmpty()) {
            return null;
        } else if(doctor.get().isDeleteFlag()) {
            return null;
        }
        return callHistoryRepository.findByDoctorId(doctorId);
    }
    @Override
    public ResponseEntity<List<String>> getDoctorTimeSlots(Long doctorId, String date) {
        Optional<Doctor> doctor = doctorRepository.findById(doctorId);
        if(doctor.isEmpty()) {
            return ResponseEntity.notFound().build();
        } else if(doctor.get().isDeleteFlag()) {
            return ResponseEntity.notFound().build();
        }
        LocalDate callDate = LocalDate.parse(date);
        List<CallHistory> callHistoryList = callHistoryRepository.findByDoctorIdAndCallDate(doctorId, callDate);

        List<String> timeSlots = callHistoryList.stream()
                .map(CallHistory::getCallTime) // Map each CallHistory record to its call time
                .map(callTime -> callTime.toString()) // Convert each LocalTime to string
                .collect(Collectors.toList());

        return ResponseEntity.ok(timeSlots);
    }

    @Override
    public List<Object> getUpAptPat(Long patientId) {
        Optional<Patient> patient = patientRepository.findById(patientId);
        if (patient.isEmpty()) {
            return null;
        } else if(patient.get().isDeleteFlag()) {
            return null;
        }
        LocalDate today = LocalDate.now();
        List<CallHistory> callHistories = callHistoryRepository.findByPatientIdAndGreaterThanEqualAndEndTimeIsNull(patientId, today);
        System.out.println("\n\n\n Before Map \n\n\n");
        List<Object> result = callHistories.stream()
                .map(callHistory -> {
                    if (callHistory.getDoctor() != null) {
                        Map<String, Object> entry = new HashMap<>();
                        entry.put("doctorId", callHistory.getDoctor().getId());
                        entry.put("doctorName", callHistory.getDoctor().getName());
                        entry.put("doctorGender", callHistory.getDoctor().getGender());
//                        try {
                        String temp = callHistory.getDoctor().getPhoneNumber();
                        entry.put("doctorPhoneNumber", (temp));
//                        } cat/ch (Exception e) {
//                            entry.put("doctorPhoneNumber", "Error: Unable to decrypt phone number");
//                        }
                        entry.put("doctorEmail", callHistory.getDoctor().getEmail());
                        entry.put("callDate", callHistory.getCallDate());
                        entry.put("callTime", callHistory.getCallTime());
                        entry.put("prescription", callHistory.getPrescription());
                        entry.put("endTime", callHistory.getEndTime());
                        entry.put("reason",callHistory.getReason());
                        entry.put("doctorRating",callHistory.getDoctor().getTotalRating());
                        entry.put("callRating", callHistory.getCallRating());
                        return entry;
                    } else {
                        // Handle case where doctor is null
                        return null;
                    }
                })
                .filter(Objects::nonNull) // Filter out null entries
                .collect(Collectors.toList());
        System.out.println("\n\n\n After Map \n\n\n");
        return result;
    }

    @Override
    public List<Object> getPastAptPat(Long patientId) {
        Optional<Patient> patient = patientRepository.findById(patientId);
        if (patient.isEmpty()) {
            return null;
        } else if(patient.get().isDeleteFlag()) {
            return null;
        }
        List<CallHistory> callHistories = callHistoryRepository.findByPatientIdAndEndTimeIsNotNull(patientId);

        List<Object> result = callHistories.stream()
                .map(callHistory -> {
                    Map<String, Object> entry = new HashMap<>();
                    entry.put("doctorId", callHistory.getDoctor().getId());
                    entry.put("doctorName", callHistory.getDoctor().getName());
                    entry.put("doctorGender", callHistory.getDoctor().getGender());
//                    try {
                    entry.put("doctorPhoneNumber", (callHistory.getDoctor().getPhoneNumber()));
//                    } catch (Exception e) {
//                        entry.put("doctorPhoneNumber", "Error: Unable to decrypt phone number");
//                    }
                    entry.put("doctorEmail", callHistory.getDoctor().getEmail());
                    entry.put("callDate", callHistory.getCallDate());
                    entry.put("callTime", callHistory.getCallTime());
                    entry.put("prescription", callHistory.getPrescription());
                    entry.put("endTime", callHistory.getEndTime());
                    entry.put("reason",callHistory.getReason());
                    entry.put("doctorRating",callHistory.getDoctor().getTotalRating());
                    entry.put("callRating", callHistory.getCallRating());
                    return entry;
                })
                .collect(Collectors.toList());

        return result;
    }

    @Override
    public List<Object> getTodayAptPat(Long patientId) {
        Optional<Patient> patient = patientRepository.findById(patientId);
        if (patient.isEmpty()) {
            return null;
        } else if(patient.get().isDeleteFlag()) {
            return null;
        }

        LocalDate today = LocalDate.now();

        List<CallHistory>callHistories = callHistoryRepository.findByPatientIdAndCallDateAndEndTimeIsNull(patientId, today);
        List<Object> result = callHistories.stream()
                .map(callHistory -> {
                    Map<String, Object> entry = new HashMap<>();
                    entry.put("doctorId", callHistory.getDoctor().getId());
                    entry.put("doctorName", callHistory.getDoctor().getName());
                    entry.put("doctorGender", callHistory.getDoctor().getGender());
                    try {
                        entry.put("doctorPhoneNumber", (callHistory.getDoctor().getPhoneNumber()));
                    } catch (Exception e) {
                        entry.put("doctorPhoneNumber", "Error: Unable to decrypt phone number");
                    }
                    entry.put("doctorEmail", callHistory.getDoctor().getEmail());
                    entry.put("callDate", callHistory.getCallDate());
                    entry.put("callTime", callHistory.getCallTime());
                    entry.put("prescription", callHistory.getPrescription());
                    entry.put("endTime", callHistory.getEndTime());
                    entry.put("reason",callHistory.getReason());
                    entry.put("pEmail", callHistory.getPatient().getEmail());
                    return entry;
                })
                .collect(Collectors.toList());

        return result;
    }
    @Override
    public List<CallHistory> getAllCallHistory() {
        System.out.println("\nInside Call History Service Impl\n");
        return callHistoryRepository.findAll();
    }
    @Override
    public List<CallHistory> getCallHistoryByPatientId(Long patientId) {
        Optional<Patient> patient = patientRepository.findById(patientId);
        if (patient.isEmpty()) {
            return null;
        } else if(patient.get().isDeleteFlag()) {
            return null;
        }
        return callHistoryRepository.findByPatientId(patientId);
    }
    @Override
    public void updateAppointmentDetails(Long id, LocalDate callDate, LocalTime callTime, LocalTime endTime) {
        Optional<CallHistory> optionalCallHistory = callHistoryRepository.findById(id);
        if (optionalCallHistory.isPresent()) {
            CallHistory callHistory = optionalCallHistory.get();
            callHistory.setCallDate(callDate);
            callHistory.setCallTime(callTime);
            callHistory.setEndTime(endTime);
            // Update any other fields if needed
            callHistoryRepository.save(callHistory);
        } else {
            throw new IllegalArgumentException("Call history entry with ID " + id + " not found");
        }
    }
    @Override
    public List<CallHistory> getTodayAppointmentsForDoctor(Long doctorId) {
        Optional<Doctor> doctor = doctorRepository.findById(doctorId);
        if (doctor.isEmpty()) {
            return null;
        } else if(doctor.get().isDeleteFlag()) {
            return null;
        }
        return callHistoryRepository.findByDoctorIdAndCallDate(doctorId, LocalDate.now());
    }

    @Override
    public Long countAppointmentsByDoctorId(Long doctorId) {
        Optional<Doctor> doctor = doctorRepository.findById(doctorId);
        if (doctor.isEmpty()) {
            return null;
        } else if(doctor.get().isDeleteFlag()) {
            return null;
        }
        return callHistoryRepository.countByDoctorId(doctorId);
    }
    @Override
    public Long countPatientsByDoctorId(Long doctorId) {
        Optional<Doctor> doctor = doctorRepository.findById(doctorId);
        if (doctor.isEmpty()) {
            return null;
        } else if(doctor.get().isDeleteFlag()) {
            return null;
        }
        return callHistoryRepository.countDistinctByDoctorId(doctorId);
    }

}
