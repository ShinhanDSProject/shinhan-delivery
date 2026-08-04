package com.example.shinhandelivery.upload.exception;

import com.example.shinhandelivery.common.exception.BusinessException;
import com.example.shinhandelivery.common.exception.ErrorCode;

/** 허용된 최대 크기를 초과하는 파일을 업로드하려 할 때 던진다. */
public class FileTooLargeException extends BusinessException {

  public FileTooLargeException(long fileSizeBytes, long maxSizeBytes) {
    super(
        ErrorCode.FILE_TOO_LARGE,
        "파일 크기가 허용 범위를 초과했습니다: " + fileSizeBytes + " bytes (최대 " + maxSizeBytes + " bytes)");
  }
}
