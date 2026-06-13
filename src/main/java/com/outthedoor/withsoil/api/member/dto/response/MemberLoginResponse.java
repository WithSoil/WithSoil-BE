package com.outthedoor.withsoil.api.member.dto.response;

public record MemberLoginResponse(
        String accessToken
) {
    public static MemberLoginResponse of(String accessToken) {
        return new MemberLoginResponse(accessToken);
    }
}