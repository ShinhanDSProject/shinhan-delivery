package com.example.shinhandelivery.common.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Controller 메서드 파라미터에서 현재 인증된 사용자의 ID(memberId: Long)를 자동으로 안전하게 직접 주입받는 어노테이션입니다. 비인증 상태이거나
 * SecurityContext에 인증 정보가 없을 경우 null을 반환합니다.
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CurrentUserId {}
