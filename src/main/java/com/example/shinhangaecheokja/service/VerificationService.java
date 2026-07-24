package com.example.shinhangaecheokja.service;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class VerificationService {

    // (휴대폰 번호 -> 인증번호) 저장
    private final Map<String, String> verificationCodes = new ConcurrentHashMap<>();
    // (휴대폰 번호 -> 인증 완료 여부) 저장
    private final Map<String, Boolean> verifiedPhones = new ConcurrentHashMap<>();

    public String sendVerificationCode(String phoneNumber) {
        String cleanPhone = phoneNumber.replaceAll("-", "").trim();
        Random random = new Random();
        String code = String.format("%06d", random.nextInt(1000000));
        
        verificationCodes.put(cleanPhone, code);
        verifiedPhones.put(cleanPhone, false);

        // 테스트/개발 환경용 로그 (실제 서비스 환경에서는 SMS 발송 API 호출)
        System.out.println("[SMS 발송] " + cleanPhone + " 번호로 인증번호 [" + code + "]가 발송되었습니다.");
        
        return code;
    }

    public boolean verifyCode(String phoneNumber, String code) {
        String cleanPhone = phoneNumber.replaceAll("-", "").trim();
        String storedCode = verificationCodes.get(cleanPhone);

        if (storedCode != null && storedCode.equals(code.trim())) {
            verifiedPhones.put(cleanPhone, true);
            verificationCodes.remove(cleanPhone);
            return true;
        }
        return false;
    }

    public boolean isPhoneVerified(String phoneNumber) {
        String cleanPhone = phoneNumber.replaceAll("-", "").trim();
        return verifiedPhones.getOrDefault(cleanPhone, false);
    }
}
