package com.outthedoor.withsoil.api.member.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Getter
@Table(
        name = "member",
        indexes = {
                @Index(name = "idx_member_email", columnList = "email")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;                        // 회원 ID (PK)

    @Column(nullable = false, unique = true, length = 100)
    private String email;                   // 이메일

    @Column(nullable = false, length = 255)
    private String password;                // 비밀번호

    @Column(nullable = false, length = 20)
    private String name;                    // 본명

    @Embedded
    private MemberLocation location;        // 사용자 위치 정보

    @Column(nullable = false)
    private boolean isDeleted;              // 삭제 여부

    private LocalDateTime deletedAt;        // 삭제 날짜

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;                      // 권한

    // 멤버 생성 정적 팩토리 메서드 -> 회원가입 시 사용
    public static Member createMember(String email, String encodedPassword,
                                      String name, MemberLocation location) {
        Member member = new Member();
        member.email = email;
        member.password = encodedPassword;
        member.name = name;
        member.location = location;
        member.isDeleted = false;
        member.role = Role.ROLE_USER;
        return member;
    }
}
