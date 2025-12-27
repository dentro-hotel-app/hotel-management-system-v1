package com.dentro.dentrohills.controller;

import com.dentro.dentrohills.model.Hospital;
import com.dentro.dentrohills.repository.HospitalRepository;
import com.dentro.dentrohills.response.HospitalResponse;
import com.dentro.dentrohills.service.HospitalService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/hospitals")
@RequiredArgsConstructor
public class HospitalController {
    private final HospitalService hospitalService;
    private final HospitalRepository hospitalRepository; // inject repository for direct testing

    @GetMapping("/home")
    public List<HospitalResponse> getHospitalsForHomepage() {
        return hospitalService.getHospitalsForHome();
    }

    // TEMP endpoint to debug 500 errors
    @GetMapping("/all") // full list, for testing
    public List<Hospital> getAllHospitals() {
        List<Hospital> hospitals = hospitalRepository.findAll();
        for (Hospital h : hospitals) {
            if (h.getPhoto() == null) {
                System.out.println("Hospital " + h.getName() + " has no photo");
            }
        }
        return hospitals;
    }

    @PostMapping("/{hospitalId}/upload-photo")
    public ResponseEntity<String> uploadHospitalPhoto(
            @PathVariable Long hospitalId,
            @RequestParam("file") MultipartFile file
    ) throws IOException {

        hospitalService.uploadHospitalPhoto(hospitalId, file);
        return ResponseEntity.ok("Photo uploaded successfully");
    }
}