package com.example.shinhandelivery.notice.exception;

import com.example.shinhandelivery.common.exception.EntityNotFoundException;
import com.example.shinhandelivery.common.exception.ErrorCode;

/** 존재하지 않는 공지사항 조회 시 발생하는 예외 클래스입니다. */
public class NoticeNotFoundException extends EntityNotFoundException {

  public NoticeNotFoundException() {
    super(ErrorCode.NOTICE_NOT_FOUND);
  }
}
