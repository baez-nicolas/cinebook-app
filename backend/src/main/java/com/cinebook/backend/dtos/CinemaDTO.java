package com.cinebook.backend.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CinemaDTO {
    private Long id;
    private String name;
    private String address;
    private String city;
    private String phone;
}