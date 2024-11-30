package com.wings.tender.util;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.SecretKey;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

//Jwt Util for 0.11
@Component
public class JwtUtil {

	private String secretKey = "myJwtSecretKeymyJwtSecretKeymyJwtSecretKeymyJwtSecretKey";

	private long validity = 60 * 60 * 1000;

	public String genJwtToken(String username) {
		Map<String, Object> claims = new HashMap<>();
		return Jwts.builder().setClaims(claims).setSubject(username).setIssuedAt(new Date(System.currentTimeMillis()))
				.setExpiration(new Date(System.currentTimeMillis() + validity))
				.signWith(getKey(), SignatureAlgorithm.HS256).compact();
	}

	private SecretKey getKey() {
		// TODO Auto-generated method stub
		byte[] key = Decoders.BASE64.decode(secretKey);
		return Keys.hmacShaKeyFor(key);
	}

	public String getUsernameFromToken(String token) {
		final Claims claims = Jwts.parserBuilder().setSigningKey(getKey()).build().parseClaimsJws(token).getBody();
		return claims.getSubject();
	}

	public boolean validateToken(String token, UserDetails userDetails) {
		String username = getUsernameFromToken(token);
		Claims claims = Jwts.parserBuilder().setSigningKey(getKey()).build().parseClaimsJws(token).getBody();
		boolean isTokenExpired = claims.getExpiration().before(new Date());

		return (userDetails.getUsername().equals(username) && !isTokenExpired);
	}

//	public String getUsernameFromToken(String token) {
//
//		final Claims claim = Jwts.parser().verifyWith(getKey()).build().parseSignedClaims(token).getPayload();
//		return claim.getSubject();
//	}
//
//	// validate JWT token
//	public boolean validateToken(String token, UserDetails userDetails) {
//		String username = getUsernameFromToken(token);
//		Claims claim = Jwts.parser().verifyWith(getKey()).build().parseSignedClaims(token).getPayload();
//		boolean isTokenExpired = claim.getExpiration().before(new Date());
//		return (userDetails.getUsername().equals(username) && !isTokenExpired);
//
//	}

}
