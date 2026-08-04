package com.example.shinhandelivery.upload.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.shinhandelivery.upload.dto.response.ImageUploadResponse;
import com.example.shinhandelivery.upload.exception.FileTooLargeException;
import com.example.shinhandelivery.upload.exception.InvalidFileTypeException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

/** 실제 디스크(JUnit {@code @TempDir}로 격리)에 파일을 쓰는 저장 로직까지 검증하는 테스트. */
class FileUploadServiceTest {

  @TempDir private Path tempDir;

  private FileUploadService fileUploadService;

  @BeforeEach
  void setUp() {
    fileUploadService = new FileUploadService(tempDir.toString(), "/uploads");
  }

  @Test
  @DisplayName("정상 jpg 파일을 업로드하면 실제로 저장되고 URL을 반환한다")
  void uploadSuccess() throws Exception {
    MockMultipartFile file =
        new MockMultipartFile("file", "photo.jpg", "image/jpeg", "dummy-image-bytes".getBytes());

    ImageUploadResponse response = fileUploadService.upload(file);

    assertThat(response.imageUrl()).startsWith("/uploads/").endsWith(".jpg");
    String filename = response.imageUrl().substring("/uploads/".length());
    assertThat(Files.exists(tempDir.resolve(filename))).isTrue();
  }

  @Test
  @DisplayName("허용되지 않는 확장자면 InvalidFileTypeException을 던진다")
  void uploadInvalidExtensionShouldThrowException() {
    MockMultipartFile file =
        new MockMultipartFile("file", "malware.exe", "application/octet-stream", "x".getBytes());

    assertThatThrownBy(() -> fileUploadService.upload(file))
        .isInstanceOf(InvalidFileTypeException.class);
  }

  @Test
  @DisplayName("파일 크기가 5MB를 초과하면 FileTooLargeException을 던진다")
  void uploadFileTooLargeShouldThrowException() {
    byte[] bigContent = new byte[6 * 1024 * 1024];
    MockMultipartFile file = new MockMultipartFile("file", "big.png", "image/png", bigContent);

    assertThatThrownBy(() -> fileUploadService.upload(file))
        .isInstanceOf(FileTooLargeException.class);
  }

  @Test
  @DisplayName("빈 파일이면 InvalidFileTypeException을 던진다")
  void uploadEmptyFileShouldThrowException() {
    MockMultipartFile file = new MockMultipartFile("file", "empty.jpg", "image/jpeg", new byte[0]);

    assertThatThrownBy(() -> fileUploadService.upload(file))
        .isInstanceOf(InvalidFileTypeException.class);
  }
}
