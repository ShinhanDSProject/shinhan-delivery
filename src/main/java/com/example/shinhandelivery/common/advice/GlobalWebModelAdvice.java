package com.example.shinhandelivery.common.advice;

import com.example.shinhandelivery.common.security.CustomUserDetails;
import com.example.shinhandelivery.common.security.WebSecurityUtils;
import com.example.shinhandelivery.member.service.MemberService;
import com.example.shinhandelivery.payment.entity.PointWallet;
import com.example.shinhandelivery.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * 모든 SSR Web Controller(@Controller) 요청 시 로그인한 사용자의 공통 모델(회원 프로필, 포인트 지갑)을 전역으로 자동 바인딩하여 컨트롤러 내
 * 보일러플레이트 중복 코드를 제거하는 Global ControllerAdvice입니다.
 */
@ControllerAdvice(annotations = Controller.class)
@RequiredArgsConstructor
public class GlobalWebModelAdvice {

  private final ObjectProvider<MemberService> memberServiceProvider;
  private final ObjectProvider<PaymentService> paymentServiceProvider;

  @ModelAttribute
  public void populateGlobalWebModel(Model model) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication != null
        && authentication.getPrincipal() instanceof CustomUserDetails userDetails) {
      WebSecurityUtils.ifAuthenticated(
          userDetails,
          userId -> {
            MemberService memberService = memberServiceProvider.getIfAvailable();
            if (memberService != null) {
              WebSecurityUtils.safeAddAttribute(
                  model, "member", () -> memberService.getMyProfile(userId));
            }

            PaymentService paymentService = paymentServiceProvider.getIfAvailable();
            if (paymentService != null) {
              WebSecurityUtils.safeAddAttribute(
                  model,
                  "wallet",
                  () -> {
                    PointWallet wallet = paymentService.getByMemberId(userId);
                    return wallet != null ? wallet : PointWallet.createEmpty(userId);
                  });
            }
          });
    }
  }
}
