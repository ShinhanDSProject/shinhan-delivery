package com.example.shinhandelivery.common.resolver;

import com.example.shinhandelivery.common.annotation.CurrentUserId;
import com.example.shinhandelivery.common.security.CustomUserDetails;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * Controller 메서드의 @CurrentUserId 어노테이션 파라미터에 현재 로그인한 사용자 ID를 자동으로 바인딩해주는
 * HandlerMethodArgumentResolver입니다.
 */
@Component
public class CurrentUserIdArgumentResolver implements HandlerMethodArgumentResolver {

  @Override
  public boolean supportsParameter(MethodParameter parameter) {
    return parameter.hasParameterAnnotation(CurrentUserId.class)
        && Long.class.isAssignableFrom(parameter.getParameterType());
  }

  @Override
  public Object resolveArgument(
      MethodParameter parameter,
      ModelAndViewContainer mavContainer,
      NativeWebRequest webRequest,
      WebDataBinderFactory binderFactory) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication != null
        && authentication.getPrincipal() instanceof CustomUserDetails userDetails) {
      return userDetails.getId();
    }
    return null;
  }
}
