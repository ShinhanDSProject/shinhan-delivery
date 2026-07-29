package com.example.shinhangaecheokja.upload.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.shinhangaecheokja.upload.dto.response.ImageUploadResponse;
import com.example.shinhangaecheokja.upload.exception.FileTooLargeException;
import com.example.shinhangaecheokja.upload.exception.InvalidFileTypeException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
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
  void 정상_jpg_파일을_업로드하면_실제로_저장되고_URL을_반환한다() throws Exception {
    MockMultipartFile file =
        new MockMultipartFile("file", "photo.jpg", "image/jpeg", "dummy-image-bytes".getBytes());

    ImageUploadResponse response = fileUploadService.upload(file);

    assertThat(response.imageUrl()).startsWith("/uploads/").endsWith(".jpg");
    String filename = response.imageUrl().substring("/uploads/".length());
    assertThat(Files.exists(tempDir.resolve(filename))).isTrue();
  }

  @Test
  void 허용되지_않는_확장자면_InvalidFileTypeException을_던진다() {
    MockMultipartFile file =
        new MockMultipartFile("file", "malware.exe", "application/octet-stream", "x".getBytes());

    assertThatThrownBy(() -> fileUploadService.upload(file))
        .isInstanceOf(InvalidFileTypeException.class);
  }

  @Test
  void 파일_크기가_5MB를_초과하면_FileTooLargeException을_던진다() {
    byte[] bigContent = new byte[6 * 1024 * 1024];
    MockMultipartFile file = new MockMultipartFile("file", "big.png", "image/png", bigContent);

    assertThatThrownBy(() -> fileUploadService.upload(file))
        .isInstanceOf(FileTooLargeException.class);
  }

  @Test
  void 빈_파일이면_InvalidFileTypeException을_던진다() {
    MockMultipartFile file = new MockMultipartFile("file", "empty.jpg", "image/jpeg", new byte[0]);

    assertThatThrownBy(() -> fileUploadService.upload(file))
        .isInstanceOf(InvalidFileTypeException.class);
  }
}
