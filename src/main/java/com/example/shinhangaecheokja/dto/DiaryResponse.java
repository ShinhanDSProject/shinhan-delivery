package com.example.shinhangaecheokja.dto;

import com.example.shinhangaecheokja.diary.entity.Diary;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DiaryResponse {

    private String id;
    private Long date;
    private String content;
    private Integer emotionId;

    public static DiaryResponse from(Diary diary) {
        return DiaryResponse.builder()
                .id(diary.getId())
                .date(diary.getDate())
                .content(diary.getContent())
                .emotionId(diary.getEmotionId())
                .build();
    } // 안녕하세요 민욱이형
    // 안녕하세요 안녕안여아녀아녀아아녕앙안녕안녕

}