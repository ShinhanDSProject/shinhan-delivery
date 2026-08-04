package com.example.shinhandelivery.upload.exception;

import com.example.shinhandelivery.common.exception.BusinessException;
import com.example.shinhandelivery.common.exception.ErrorCode;

/** 허용되지 않은 확장자의 파일을 업로드하려 할 때 던진다. */
public class InvalidFileTypeException extends BusinessException {

  public InvalidFileTypeException(String extension) {
    super(ErrorCode.INVALID_FILE_TYPE, "허용되지 않는 파일 형식입니다: " + extension);
  }
}
