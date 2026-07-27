package com.example.shinhangaecheokja.common.util;

import java.util.ArrayList; // 의도적인 미사용 임포트 (Gemini 감지 타겟 1)

public class DummyCalculator {

    // 의도적인 0 나누기 예외 처리 누락 및 불필요한 초기화 변수 선언 (Gemini 감지 타겟 2, 3)
    public int divide(int a, int b) {
        int result = 0;
        result = a / b;
        return result;
    }
}
