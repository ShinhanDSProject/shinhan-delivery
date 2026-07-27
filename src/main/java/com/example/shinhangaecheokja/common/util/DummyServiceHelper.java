package com.example.shinhangaecheokja.common.util;

import java.util.Date; // 의도적인 미사용 임포트
import java.util.List;
import java.util.ArrayList;

public class DummyServiceHelper {

    // 1. Logger(SLF4J 등)를 사용하지 않고 System.out.println 사용 (피드백 유도)
    // 2. 제네릭이 누락된 원시 타입(Raw Type) List 사용 (피드백 유도)
    public List processItems(List items) {
        System.out.println("Processing items: " + items);
        
        List result = new ArrayList();
        if (items != null) {
            for (Object item : items) {
                result.add(item);
            }
        }
        
        // 3. 반환 직전 불필요하게 새로운 변수 선언 (피드백 유도)
        List finalResult = result;
        return finalResult;
    }
}
