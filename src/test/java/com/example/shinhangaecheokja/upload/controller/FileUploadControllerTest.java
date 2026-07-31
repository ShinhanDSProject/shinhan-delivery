package com.example.shinhangaecheokja.upload.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.shinhangaecheokja.upload.dto.response.ImageUploadResponse;
import com.example.shinhangaecheokja.upload.exception.FileTooLargeException;
import com.example.shinhangaecheokja.upload.exception.InvalidFileTypeException;
import com.example.shinhangaecheokja.upload.service.FileUploadService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(FileUploadController.class)
class FileUploadControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private FileUploadService fileUploadService;

  @Test
  @DisplayName("이미지 업로드에 성공하면 201과 imageUrl을 반환한다")
  void uploadImageSuccess() throws Exception {
    MockMultipartFile file =
        new MockMultipartFile("file", "photo.jpg", "image/jpeg", "bytes".getBytes());
    when(fileUploadService.upload(any())).thenReturn(new ImageUploadResponse("/uploads/abc.jpg"));

    mockMvc
        .perform(multipart("/api/v1/uploads/image").file(file))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.imageUrl").value("/uploads/abc.jpg"));
  }

  @Test
  @DisplayName("허용되지 않는 확장자면 400을 반환한다")
  void uploadImageInvalidExtensionShouldReturn400() throws Exception {
    MockMultipartFile file =
        new MockMultipartFile("file", "malware.exe", "application/octet-stream", "x".getBytes());
    when(fileUploadService.upload(any())).thenThrow(new InvalidFileTypeException("exe"));

    mockMvc
        .perform(multipart("/api/v1/uploads/image").file(file))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("파일이 너무 크면 400을 반환한다")
  void uploadImageFileTooLargeShouldReturn400() throws Exception {
    MockMultipartFile file =
        new MockMultipartFile("file", "big.png", "image/png", "bytes".getBytes());
    when(fileUploadService.upload(any()))
        .thenThrow(new FileTooLargeException(6_000_000L, 5_000_000L));

    mockMvc
        .perform(multipart("/api/v1/uploads/image").file(file))
        .andExpect(status().isBadRequest());
  }
}
