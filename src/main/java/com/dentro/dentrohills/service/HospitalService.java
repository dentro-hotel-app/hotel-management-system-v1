package com.dentro.dentrohills.service;

import com.dentro.dentrohills.model.Hospital;
import com.dentro.dentrohills.repository.HospitalRepository;
import com.dentro.dentrohills.response.HospitalResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HospitalService {
    private final HospitalRepository hospitalRepository;

    public List<HospitalResponse> getHospitalsForHome() {
        return hospitalRepository.findAll()
                .stream()
                .map(hospital -> new HospitalResponse(
                        hospital.getId(),
                        hospital.getName(),
                        hospital.getPhoto() != null
                                ? Base64.getEncoder().encodeToString(hospital.getPhoto())
                                : null
                ))
                .collect(Collectors.toList());
    }

    public void uploadHospitalPhoto(Long hospitalId, MultipartFile file) throws IOException {
        Hospital hospital = hospitalRepository.findById(hospitalId)
                .orElseThrow(() -> new RuntimeException("Hospital not found"));

        hospital.setPhoto(file.getBytes());
        hospitalRepository.save(hospital);
    }
}
