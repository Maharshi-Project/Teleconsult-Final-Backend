package com.teleconsulting.demo.service;


import com.teleconsulting.demo.model.CallHistory;
import com.teleconsulting.demo.model.Patient;
import com.teleconsulting.demo.repository.PatientRepository;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class EmailService {
    @Autowired
    private JavaMailSender mailSender;
    private final PatientRepository patientRepository;

    public EmailService(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    public void sendAppointmentNotification(CallHistory appointment) throws MessagingException {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("azizrocky1951@gmail.com");
        message.setTo(appointment.getDoctor().getEmail());
        message.setSubject("Appointment Notification");
        message.setText("Dear " + appointment.getDoctor().getName() + ",\n\n"
                + "This is a reminder of your appointment scheduled for today.\n\n"
                + "Appointment details:\n"
                + "Date: " + appointment.getCallDate() + "\n"
                + "Time: " + appointment.getCallTime() + "\n"
                + "Thank you.");

        mailSender.send(message);
    }

    public void sendLoginOtp(String email, String otp) throws MessagingException {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("vishwavinnu1@gmail.com");
        message.setTo(email);
        Optional<Patient> patient = patientRepository.findByEmail("email");
        if(patient.isPresent())
        {
            message.setSubject("OTP Verification");
            message.setText("Dear " + patient.get().getEmail() + ",\n\n"
                    + "This is an OTP Verification Email. Do not share OTP with anyone!\n\n"
                    + "Appointment details: "
                    + otp
                    + "Thank you.");

            mailSender.send(message);
            ResponseEntity.ok("Email Sent Successfully!");
        }
        else
        {
            ResponseEntity.internalServerError();
        }
    }
}
