package com.outthedoor.withsoil.api.member.dto.response;

import com.outthedoor.withsoil.api.member.entity.MemberLocation;

import java.math.BigDecimal;

public record MemberLocationResponse(
        String sido,
        String sigungu,
        String eupMyeonDong,
        String ri,
        BigDecimal latitude,
        BigDecimal longitude
) {
    public static MemberLocationResponse from(MemberLocation location) {
        return new MemberLocationResponse(
                location.getSido(),
                location.getSigungu(),
                location.getEupMyeonDong(),
                location.getRi(),
                location.getLatitude(),
                location.getLongitude()
        );
    }
}
