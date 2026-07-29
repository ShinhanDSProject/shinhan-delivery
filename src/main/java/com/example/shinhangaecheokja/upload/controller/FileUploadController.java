package com.example.shinhangaecheokja.upload.controller;

import com.example.shinhangaecheokja.upload.dto.response.ImageUploadResponse;
import com.example.shinhangaecheokja.upload.service.FileUploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/** 이미지 업로드 API를 제공하는 컨트롤러. */
@RestController
@RequestMapping("/api/v1/uploads")
@RequiredArgsConstructor
public class FileUploadController {

  private final FileUploadService fileUploadService;

  /** 이미지 파일을 업로드하고 접근 URL을 반환한다. */
  @PostMapping("/image")
  public ResponseEntity<ImageUploadResponse> uploadImage(@RequestParam("file") MultipartFile file) {
    return ResponseEntity.status(HttpStatus.CREATED).body(fileUploadService.upload(file));
  }
}
