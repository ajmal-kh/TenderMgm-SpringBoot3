package com.wings.tender.filter;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.wings.tender.service.MyUserDetailsService;
import com.wings.tender.util.JwtUtil;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtFilter extends OncePerRequestFilter{
	
	@Autowired
	JwtUtil jwtUtil;

	@Autowired
	MyUserDetailsService myUDService;
	
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		String header = request.getHeader("Authorization");
		String token=null;
		String username=null;
		
		if(header!=null && header.startsWith("Bearer ")) {
			token = header.substring(7);
			try {
				username = jwtUtil.getUsernameFromToken(token);
			}catch(JwtException ex) {
				throw new JwtException("Invalid Token");
			}
		}
		
		if(username!=null && SecurityContextHolder.getContext().getAuthentication()==null){
			UserDetails userDetails = myUDService.loadUserByUsername(username);
			
			if(jwtUtil.validateToken(token, userDetails)) {
				UsernamePasswordAuthenticationToken uPAT = new UsernamePasswordAuthenticationToken(userDetails,null,userDetails.getAuthorities());
				uPAT.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
				
				SecurityContextHolder.getContext().setAuthentication(uPAT);
			}
		}
		filterChain.doFilter(request, response);
	}

}
