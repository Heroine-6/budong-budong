package com.example.budongbudong.common.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class CustomSliceResponse <T>{
    private List<T> content;
    private int size;
    private int number;
    private boolean hasNext;

    public static <T> CustomSliceResponse<T> from(
            List<T> content,
            int size,
            int number,
            boolean hasNext
    ) {
        return new CustomSliceResponse<>(content, size, number, hasNext);
    }
}
