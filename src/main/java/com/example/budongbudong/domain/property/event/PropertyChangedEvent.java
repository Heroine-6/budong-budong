package com.example.budongbudong.domain.property.event;

public record PropertyChangedEvent(Long propertyId, PropertyEventType type) {
}
