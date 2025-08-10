package com.unithon.tadadak.auth.filter;

import com.unithon.tadadak.auth.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                   HttpServletResponse response, 
                                   FilterChain filterChain) throws ServletException, IOException {

        final String authorizationHeader = request.getHeader("Authorization");
        final String token = jwtUtil.extractTokenFromHeader(authorizationHeader);

        // 토큰이 없으면 다음 필터로 진행
        if (token == null) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // 토큰 유효성 검증
            if (jwtUtil.validateToken(token)) {
                String username = jwtUtil.getUsernameFromToken(token);
                Long userId = jwtUtil.getUserIdFromToken(token);

                // UserDetails 생성 (간단한 구현)
                UserDetails userDetails = User.builder()
                        .username(username)
                        .password("") // 패스워드는 필요없음 (토큰으로 이미 인증됨)
                        .authorities(new ArrayList<>()) // 권한은 나중에 확장 가능
                        .build();

                // 인증 객체 생성
                UsernamePasswordAuthenticationToken authentication = 
                    new UsernamePasswordAuthenticationToken(
                        userDetails, 
                        null, 
                        userDetails.getAuthorities());

                // 추가 정보 설정 (userId를 나중에 사용할 수 있도록)
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                
                // SecurityContext에 인증 정보 저장
                SecurityContextHolder.getContext().setAuthentication(authentication);
                
                // request에 userId 저장 (컨트롤러에서 사용할 수 있도록)
                request.setAttribute("userId", userId);
                request.setAttribute("username", username);

                log.debug("JWT 인증 성공: username={}, userId={}", username, userId);
            }
        } catch (Exception e) {
            log.error("JWT 인증 처리 중 오류 발생", e);
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        
        // 인증이 필요없는 경로들
        return path.startsWith("/api/auth/login") ||
               path.startsWith("/api/users") ||  // 회원가입 (UserController)
               path.startsWith("/swagger-ui") ||
               path.startsWith("/v3/api-docs") ||
               path.startsWith("/api/users/check-username"); // 닉네임 중복확인
    }
} 