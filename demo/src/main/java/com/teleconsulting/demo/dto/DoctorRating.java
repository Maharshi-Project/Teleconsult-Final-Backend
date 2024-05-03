package com.teleconsulting.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DoctorRating {
    Long id;
    String name;
    float totalRating;
    float appointmentCount;
}
