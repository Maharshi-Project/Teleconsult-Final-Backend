package com.teleconsulting.demo.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Patient {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String gender;
    @NonNull
    private String phoneNumber;
    private String email;
    private String password;
    private boolean wstatus;
    private boolean callbackstatus;
    private Long assigneddoctor;
    private String pincomingcall;
    @Column(columnDefinition = "bigint default 0")
    private Long acptstatus;
    @Enumerated(value = EnumType.STRING)
    Role role;
    private boolean deleteFlag;
}
