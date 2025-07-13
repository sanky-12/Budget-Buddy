package com.budgetbuddy.expense_service.filter;

import com.budgetbuddy.expense_service.security.JwtUtil;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        final String authHeader = request.getHeader("Authorization");
        System.out.println("Auth Header: " + authHeader);



        String jwt = null;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            jwt = authHeader.substring(7);
            System.out.println("JWT: " + jwt);
            System.out.println("Is Token Valid: " + jwtUtil.isTokenValid(jwt));
            try {
                if (jwtUtil.isTokenValid(jwt)) {
                    String userId = jwtUtil.extractUserId(jwt);
                    //String email = jwtUtil.extractUserEmail(jwt); // assuming you’ve added this helper

                    // Create a UsernamePasswordAuthenticationToken
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(userId, null, new ArrayList<>());

                    // Set the authentication into SecurityContext
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                } else {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    return;
                }
            } catch (Exception e) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
        }

        // Continue filter chain (allow if no token — assuming public routes allowed)
        chain.doFilter(request, response);
    }
}
