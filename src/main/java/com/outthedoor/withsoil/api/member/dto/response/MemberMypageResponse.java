package com.outthedoor.withsoil.api.member.dto.response;

import com.outthedoor.withsoil.api.member.entity.Member;

public record MemberMypageResponse(
        Long id,
        String email,
        String name,
        MemberLocationResponse location
) {
    public static MemberMypageResponse from(Member member) {
        return new MemberMypageResponse(
                member.getId(),
                member.getEmail(),
                member.getName(),
                MemberLocationResponse.from(member.getLocation())
        );
    }
}
