package com.outthedoor.withsoil.global.security.service;

import com.outthedoor.withsoil.api.member.entity.Member;
import com.outthedoor.withsoil.api.member.repository.MemberRepository;
import com.outthedoor.withsoil.global.security.entity.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final MemberRepository memberRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        Member member = memberRepository.findByEmailAndIsDeleted(email, false)
                .orElseThrow(() -> new UsernameNotFoundException("존재하지 않는 회원입니다."));

        return new CustomUserDetails(member);
    }
}
