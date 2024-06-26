package com.teleconsulting.demo.service;

import com.teleconsulting.demo.dto.Ddetails;
import com.teleconsulting.demo.dto.DoctorRating;
import com.teleconsulting.demo.dto.RegDoc;
import com.teleconsulting.demo.exception.UserNotFoundException;
import com.teleconsulting.demo.dto.AuthenticationResponse;
import com.teleconsulting.demo.model.Doctor;
import com.teleconsulting.demo.model.Role;
import com.teleconsulting.demo.repository.DoctorRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class DoctorServiceImpl implements DoctorService{

    private final DoctorRepository doctorRepository;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public DoctorServiceImpl(DoctorRepository doctorRepository) {
        this.doctorRepository = doctorRepository;
    }

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

    @Override
    public List<Doctor> getDoctorsBySupervisorId(Long supervisorId) {
        Optional<Doctor> doctor = doctorRepository.findById(supervisorId);
        if(doctor.isEmpty()) {
            return null;
        } else if(doctor.get().isDeleteFlag()) {
            return null;
        }
        return doctorRepository.findBySupervisorDoctorId(supervisorId);
    }

    @Override
    public Optional<Doctor> getUserByEmail(String email) {
        Optional<Doctor> doctor = doctorRepository.findByEmail(email);
        if(doctor.isEmpty()) {
            return Optional.empty();
        } else if(doctor.get().isDeleteFlag()) {
            return Optional.empty();
        }
        return doctorRepository.findByEmail(email);
    }

    @Override
    public List<Doctor> findAllAvailableDoctors() {
        List<Doctor> doctorList = doctorRepository.findByAvailability(true);
        List<Doctor> finalList = new ArrayList<>();
        for(Doctor doctor : doctorList) {
            if(!doctor.isDeleteFlag()) {
                finalList.add(doctor);
            }
        }
        return finalList;
    }



    @Override
    public Doctor saveDoctor(Doctor doctor) {
        return doctorRepository.save(doctor);
    }
    @Override
    public AuthenticationResponse saveNewDoctor(RegDoc regDoc) {
        Doctor doctor2 = doctorRepository.findByEmail(regDoc.getEmail()).orElse(null);
        if(doctor2 == null)
        {
            Doctor doctor1 = new Doctor();
            doctor1.setName(regDoc.getName());
            doctor1.setEmail(regDoc.getEmail());
            doctor1.setGender(regDoc.getGender());
            doctor1.setPassword(passwordEncoder.encode("password"));
            try{
                doctor1.setPhoneNumber(encrypt(regDoc.getPhoneNumber()));
            }catch(Exception e)
            {
                System.out.println(e.getMessage());
            }
            if(regDoc.getSupervisorDoctor() == null)
            {
                System.out.println("Hello Sr Doc \n");
                doctor1.setRole(Role.valueOf(Role.SRDOC.toString()));
                System.out.println("\nRole is "+Role.valueOf(Role.SRDOC.toString()));
            }
            else
            {
                doctor1.setRole(Role.valueOf(Role.DOCTOR.toString()));
            }
            if (regDoc.getSupervisorDoctor() != null) {
                Doctor supervisorDoctor = doctorRepository.findById(regDoc.getSupervisorDoctor()).orElse(null);
                doctor1.setSupervisorDoctor(supervisorDoctor);
            }
            else
            {
                System.out.println("Super is set to null\n");
                doctor1.setSupervisorDoctor(null);
            }
            doctor1.setIncomingCall(null);
            doctor1.setDeleteFlag(false);
            doctor1.setAvailability(false);
            doctor1.setValidated(false);
            doctorRepository.save(doctor1);
            return new AuthenticationResponse(null, "Doctor Registration was Successful");
        }
        else
            return new AuthenticationResponse(null, "Email ID already exist!!");
    }

    @Override
    public List<Doctor> getAllDoctors() {
        List<Doctor> doctorList = doctorRepository.findAll();
        List<Doctor> finalList = new ArrayList<>();
        for(Doctor doctor : doctorList) {
            if(!doctor.isDeleteFlag()) {
                finalList.add(doctor);
            }
        }
        return finalList;
    }
    @Override
    public Doctor findByPhoneNumber(String phoneNumber) {
        Doctor doctor = doctorRepository.findByPhoneNumber(phoneNumber);
        if(doctor.isDeleteFlag()) {
            return  null;
        }
        else
            return doctor;
    }

    @Override
    public Doctor findById(Long id) {
        Optional<Doctor> doctor = doctorRepository.findById(id);
        if(doctor.isEmpty()) {
            return null;
        } else if(doctor.get().isDeleteFlag()) {
            return null;
        }
        return doctor.get();
    }

    @Override
    public Doctor updateDoctorIncomingCall(String doctorPhoneNumber, String patientPhoneNumber) {
        Doctor doctor = doctorRepository.findByPhoneNumber(doctorPhoneNumber);
        if(doctor.isDeleteFlag()) {
            return null;
        }
        doctor.setIncomingCall(patientPhoneNumber);
        return doctorRepository.save(doctor);
    }

    @Override
    public Doctor updateDoctor(Long id, Doctor updatedDoctor) {
        Doctor existingDoctor = doctorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Doctor not found with id: " + id));
        if(existingDoctor.isDeleteFlag()) {
            return null;
        }
        System.out.println("\nUpdated DOc"+updatedDoctor+"\n");
        existingDoctor.setName(updatedDoctor.getName());
        existingDoctor.setGender(updatedDoctor.getGender());
        existingDoctor.setPhoneNumber(updatedDoctor.getPhoneNumber());
        existingDoctor.setEmail(updatedDoctor.getEmail());
        existingDoctor.setPassword(passwordEncoder.encode(updatedDoctor.getPassword()));
        existingDoctor.setRole(Role.valueOf(Role.DOCTOR.toString()));
        existingDoctor.setSupervisorDoctor(updatedDoctor.getSupervisorDoctor());
        doctorRepository.save(existingDoctor);
        // Save the updated doctor entity
        return existingDoctor;
    }

    @Override
    public void deleteDoctorById(Long id) {
        if(!doctorRepository.existsById(id)){
            throw new UserNotFoundException(id);
        }
        doctorRepository.deleteById(id);
    }

    @Override
    public List<Ddetails> getSnrDoctors() {
        List<Doctor> doctors = doctorRepository.findBySupervisorDoctorIsNull();
        return doctors.stream()
                .map(doctor -> {
                    Ddetails doctorDetails = new Ddetails();
                    doctorDetails.setId(doctor.getId());
                    doctorDetails.setName(doctor.getName());
                    doctorDetails.setGender(doctor.getGender());
                    doctorDetails.setPhoneNumber(doctor.getPhoneNumber());
                    doctorDetails.setEmail(doctor.getEmail());
                    doctorDetails.setAppointmentCount(doctor.getAppointmentCount());
                    doctorDetails.setTotalRating(doctor.getTotalRating());
                    return doctorDetails;
                })
                .collect(Collectors.toList());
    }
    @Override
    public void updateRating(Long id, float rating) {
        Optional<Doctor> doctor = doctorRepository.findById(id);
        if(doctor.isEmpty()) {
            return;
        } else if(doctor.get().isDeleteFlag()) {
            return;
        }
        float tempRating = doctor.get().getTotalRating();
        tempRating += rating;
        int tempCount = doctor.get().getAppointmentCount();
        tempCount++;
        doctor.get().setTotalRating(tempRating);
        doctor.get().setAppointmentCount(tempCount);
        doctorRepository.save(doctor.get());
    }

    @Override
    public List<DoctorRating> getAllRatings() {
        List<Object[]> ratingObjects = doctorRepository.getRatings();
        List<DoctorRating> doctorRatings = new ArrayList<>();
        for (Object[] ratingObject : ratingObjects) {
            Long id = (Long) ratingObject[0];
            String name = (String) ratingObject[1];
            float rating = (float) ratingObject[2];
            int count = (int) ratingObject[3];
            doctorRatings.add(new DoctorRating(id, name,rating, count));
        }
        return doctorRatings;
    }

    @Override
    public List<Doctor> getOnlineDoctorsforPat() {
        List<Doctor> doctors = doctorRepository.findByAvailability(true);
        List<Doctor> finalList = new ArrayList<>();
        for(Doctor doctor : doctors) {
            if(!doctor.isDeleteFlag()) {
                finalList.add(doctor);
            }
        }
        return finalList;
    }

    @Override
    public List<Doctor> getAllSrDoctors() {
        List<Doctor> doctors = doctorRepository.findAllSrDocs();
        List<Doctor> finalList = new ArrayList<>();
        for(Doctor doctor : doctors){
            if(!doctor.isDeleteFlag()) {
                String temp = doctor.getPhoneNumber();
                try{
                    doctor.setPhoneNumber(decrypt(temp));
                }catch(Exception e)
                {
                    System.out.println("\nException in PatientServiceImple findById\n"+e);
                }
                finalList.add(doctor);
            }
        }
        return finalList;
    }

    @Override
    public List<Doctor> getAllDoctorsExceptPassword() {
        List<Doctor> doctors = doctorRepository.findAll();
        List<Doctor> finalList = new ArrayList<>();
        for (Doctor doctor : doctors) {
            if(!doctor.isDeleteFlag()) {
                doctor.setPassword("none");
                finalList.add(doctor);
            }
        }
        return finalList;
    }

    @Override
    public Long countDoctors() {
        List<Doctor> doctorList = doctorRepository.findAllDeletedDocs();
        int delCount = doctorList.size();
        return doctorRepository.count()-delCount;
    }

    @Override
    public List<Doctor> getDoctorsUnderSeniorDoctor(Long supervisorId) {
        Optional<Doctor> doctor1 = doctorRepository.findById(supervisorId);
        if(doctor1.isEmpty()) {
            return null;
        } else if(doctor1.get().isDeleteFlag()) {
            return null;
        }
        System.out.println("\nInside getDoctorBySup DoctorServiceImpl\n");
        List<Doctor> doctors = doctorRepository.findBySupervisorDoctorId(supervisorId);
        for(Doctor doctor : doctors){
            if(!doctor.isDeleteFlag()) {
                String temp = doctor.getPhoneNumber();
                try{
                    doctor.setPhoneNumber(decrypt(temp));
                }catch(Exception e)
                {
                    System.out.println("\nException in DocServiceImple\n"+e);
                }
            }
        }
        return doctors;
    }

    @Override
    public Doctor updateDoctors(Long id, RegDoc updatedDoctor) {
        Doctor existingDoctor = doctorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Doctor not found with id: " + id));
        if(existingDoctor.isDeleteFlag()) {
            return null;
        }
        System.out.println("\nUpdateDoctors RegDoc\n");
        existingDoctor.setName(updatedDoctor.getName());
        existingDoctor.setGender(updatedDoctor.getGender());
        try{
            existingDoctor.setPhoneNumber(encrypt(updatedDoctor.getPhoneNumber()));
        } catch (Exception e){
            System.out.println("\nError: "+e.getMessage());
        }
        existingDoctor.setEmail(updatedDoctor.getEmail());
        return doctorRepository.save(existingDoctor);
    }

    @Override
    public void updateDoctorSdid(Long doctorId, Long newSdid) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new UserNotFoundException( doctorId));
        Doctor supervisorDoctor = doctorRepository.findById(newSdid)
                .orElseThrow(() -> new UserNotFoundException( newSdid));
        if(doctor.isDeleteFlag() || supervisorDoctor.isDeleteFlag()) {
            return;
        }
        doctor.setSupervisorDoctor(supervisorDoctor);
        doctorRepository.save(doctor);
    }

    @Override
    public Doctor findByEmail(String email) {
        Optional<Doctor> doctor = doctorRepository.findByEmail(email);
        if(doctor.isEmpty()) {
            return null;
        } else if(doctor.get().isDeleteFlag()) {
            return null;
        }
        return doctor.get();
    }

    @Override
    public Doctor getDoctorNameAndPhoneNumber(Long doctorId) {
        Optional<Doctor> doctor1 = doctorRepository.findById(doctorId);
        if(doctor1.isEmpty()) {
            return null;
        } else if(doctor1.get().isDeleteFlag()) {
            return null;
        }
        return doctorRepository.findById(doctorId).map(doctor -> {
            Doctor doctorDetails = new Doctor();
            doctorDetails.setName(doctor.getName());
            doctorDetails.setPhoneNumber(doctor.getPhoneNumber());
            return doctorDetails;
        }).orElse(null);
    }

    @Override
    public List<Long> findAllSrDocExcept(Long doctorId) {
        List<Long> finalIds = new ArrayList<>();
        List<Doctor> doctorList = doctorRepository.findAllSrDocs();
        for (Doctor doctor : doctorList) {
            if (!doctor.getId().equals(doctorId)) {
                finalIds.add(doctor.getId());
            }
        }
        return finalIds;
    }
}
