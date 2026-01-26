package com.example.budongbudong.domain.property.event;

/**
 * 매물 변경 이벤트의 의미를 명확하게 표현하기 위한
 */
public enum PropertyEventType {
    CREATED("매물 등록 이벤트"),
    UPDATED("매물 수정 이벤트"),
    DELETED("매물 삭제 이벤트");
    
    private String message;
    PropertyEventType(String message) {
        this.message = message;
    }
}
