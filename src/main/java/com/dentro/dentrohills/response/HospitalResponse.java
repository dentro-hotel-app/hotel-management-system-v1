package com.dentro.dentrohills.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor  // generates the 3-arg constructor
public class HospitalResponse {
    private Long id;
    private String name;
    private String photoBase64; // Base64


}
