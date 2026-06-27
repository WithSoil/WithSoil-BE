package com.outthedoor.withsoil.api.member.dto.response;

import com.fasterxml.jackson.annotation.JsonRawValue;
import com.outthedoor.withsoil.api.member.entity.Member;

public record MemberMypageResponse(
        Long id,
        String email,
        String name,
        MemberLocationResponse location,
        String recommendations
) {
    @JsonRawValue
    public String recommendations() {
        return recommendations;
    }

    public static MemberMypageResponse from(Member member, String recommendationJson) {
        return new MemberMypageResponse(
                member.getId(),
                member.getEmail(),
                member.getName(),
                MemberLocationResponse.from(member.getLocation()),
                recommendationJson
        );
    }
}
