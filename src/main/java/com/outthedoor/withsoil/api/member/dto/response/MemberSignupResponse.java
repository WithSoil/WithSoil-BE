package com.outthedoor.withsoil.api.member.dto.response;

import com.outthedoor.withsoil.api.member.entity.Member;

public record MemberSignupResponse(
        Long id,
        String email,
        String name,
        MemberLocationResponse location
) {
    public static MemberSignupResponse from(Member member) {
        return new MemberSignupResponse(
                member.getId(),
                member.getEmail(),
                member.getName(),
                MemberLocationResponse.from(member.getLocation())
        );
    }
}
