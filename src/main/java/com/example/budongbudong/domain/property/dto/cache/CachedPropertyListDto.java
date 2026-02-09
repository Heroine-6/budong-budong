package com.example.budongbudong.domain.property.dto.cache;

import com.example.budongbudong.common.response.CustomSliceResponse;
import com.example.budongbudong.domain.property.dto.response.ReadAllPropertyResponse;
import lombok.*;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CachedPropertyListDto {
    private List<CachedPropertyDto> content;
    private boolean hasNext;

    public static CachedPropertyListDto from(Slice<ReadAllPropertyResponse> slice) {
        return new CachedPropertyListDto(
                slice.getContent().stream()
                        .map(CachedPropertyDto::from)
                        .toList(),
                slice.hasNext()
        );
    }

    public CustomSliceResponse<ReadAllPropertyResponse> toResponse(Pageable pageable) {
        return CustomSliceResponse.from(
                content.stream()
                        .map(CachedPropertyDto::toResponse)
                        .toList(),
                pageable.getPageSize(),
                pageable.getPageNumber(),
                hasNext
        );
    }
}
