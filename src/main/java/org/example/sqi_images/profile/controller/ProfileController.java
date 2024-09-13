package org.example.sqi_images.profile.controller;

import lombok.RequiredArgsConstructor;
import org.example.sqi_images.common.authentication.annotation.Authenticated;
import org.example.sqi_images.employee.domain.Employee;
import org.example.sqi_images.profile.dto.request.CreateProfileDto;
import org.example.sqi_images.profile.dto.response.ImageDataResponse;
import org.example.sqi_images.profile.dto.response.ProfileDetailResponse;
import org.example.sqi_images.profile.dto.response.ProfileResponseList;
import org.example.sqi_images.profile.service.ProfileService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/profiles")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @PostMapping(value = "", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> createProfile(@Authenticated Employee employee,
                                                @RequestPart(value = "data") CreateProfileDto createProfileDto,
                                                @RequestPart(value = "image") MultipartFile file) throws IOException {
        profileService.createProfile(employee, createProfileDto, file);
        return ResponseEntity.status(HttpStatus.CREATED).body("프로필 생성 완료");
    }

    @GetMapping
    public ResponseEntity<ProfileResponseList> getAllProfiles() {
        ProfileResponseList profiles = profileService.getAllProfiles();
        return ResponseEntity.ok(profiles);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProfileDetailResponse> getProfileDetail(@PathVariable Long id) {
        ProfileDetailResponse profileDetail = profileService.getProfileDetail(id);
        return ResponseEntity.ok(profileDetail);
    }

    @GetMapping("/{profileId}/image")
    public ResponseEntity<byte[]> getProfileImage(@PathVariable Long profileId) {
        ImageDataResponse imageDataResponse = profileService.getProfileImage(profileId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(imageDataResponse.contentType()));

        return ResponseEntity.ok()
                .headers(headers)
                .body(imageDataResponse.imageData());
    }
}
