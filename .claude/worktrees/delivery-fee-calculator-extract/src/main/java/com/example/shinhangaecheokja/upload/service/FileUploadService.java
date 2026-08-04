package com.example.shinhandelivery.upload.service;

import com.example.shinhandelivery.upload.dto.response.ImageUploadResponse;
import com.example.shinhandelivery.upload.exception.FileTooLargeException;
import com.example.shinhandelivery.upload.exception.InvalidFileTypeException;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/** 이미지 파일을 검증 후 로컬 디스크에 저장하고 접근 URL을 반환하는 서비스. DB에는 아무것도 저장하지 않는다. */
@Service
public class FileUploadService {

  private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp");
  private static final long MAX_FILE_SIZE_BYTES = 5L * 1024 * 1024;

  private final Path uploadDir;
  private final String baseUrl;

  public FileUploadService(
      @Value("${app.upload.dir}") String uploadDir,
      @Value("${app.upload.base-url}") String baseUrl) {
    this.uploadDir = Path.of(uploadDir);
    this.baseUrl = baseUrl;
  }

  /** 빈 파일 → 확장자 화이트리스트 → 크기(5MB) 순으로 검증한 뒤, UUID 파일명으로 저장하고 접근 URL을 반환한다. */
  public ImageUploadResponse upload(MultipartFile file) {
    String extension = extractExtension(file);
    if (!ALLOWED_EXTENSIONS.contains(extension)) {
      throw new InvalidFileTypeException(extension);
    }
    if (file.getSize() > MAX_FILE_SIZE_BYTES) {
      throw new FileTooLargeException(file.getSize(), MAX_FILE_SIZE_BYTES);
    }

    String filename = UUID.randomUUID() + "." + extension;
    try {
      Files.createDirectories(uploadDir);
      Files.copy(file.getInputStream(), uploadDir.resolve(filename));
    } catch (IOException e) {
      throw new UncheckedIOException("이미지 저장에 실패했습니다.", e);
    }

    String normalizedBaseUrl =
        baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    return new ImageUploadResponse(normalizedBaseUrl + "/" + filename);
  }

  private String extractExtension(MultipartFile file) {
    String originalFilename = file.getOriginalFilename();
    if (file.isEmpty() || originalFilename == null || !originalFilename.contains(".")) {
      throw new InvalidFileTypeException("(빈 파일 또는 확장자 없음)");
    }
    return originalFilename.substring(originalFilename.lastIndexOf('.') + 1).toLowerCase();
  }
}
