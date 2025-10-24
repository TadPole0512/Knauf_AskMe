package com.limenets.eorder.svc;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.DatatypeConverter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.limenets.common.util.Converter;
import com.limenets.common.util.HttpUtils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@Service
public class JwtSvc {
	private static final Logger logger = LoggerFactory.getLogger(JwtSvc.class);
	
	private String jwtSecretKey = "db1dptm@wl3qh$fkf5!";
	
	/**
	 * JWT 토큰 생성.
	 */
	public String makeJWT(HttpServletRequest req, Map<String, Object> infoMap) throws Exception {
		SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;
		Date expireTime = new Date();
		expireTime.setTime(expireTime.getTime() + (1000 * 60 * 60 * 24 * 7)); // 1분 * 60분 * 24시 * 7일
		//expireTime.setTime(expireTime.getTime() + (1000 * 60 * 1)); // 1분 
		byte[] apiKeySecretBytes = DatatypeConverter.parseBase64Binary(jwtSecretKey);
		Key signingKey = new SecretKeySpec(apiKeySecretBytes, signatureAlgorithm.getJcaName());
		
		Map<String, Object> headerMap = new HashMap<>();
		headerMap.put("typ","JWT");
        headerMap.put("alg","HS256");
        
        JwtBuilder builder = Jwts.builder().setHeader(headerMap)
                .setClaims(infoMap)
                .setExpiration(expireTime)
                .signWith(signatureAlgorithm, signingKey);
		
        return builder.compact();
	}

	/**
	 * JWT 파싱하기.
	 * @param jwtToken : 토큰값.
	 * @return resMap : token_check => SUCCESS, EXPIRE, FALSIFY
	 * @return resMap : token_check => SUCCESS 일때만 key=Member.MB_ID
	 */
	public Map<String, Object> parseJWT(HttpServletRequest req, String jwtToken) throws Exception {
		Map<String, Object> resMap = new HashMap<>();
		
		try {
			Claims claims = Jwts.parser().setSigningKey(DatatypeConverter.parseBase64Binary(jwtSecretKey))
	                .parseClaimsJws(jwtToken).getBody(); // 정상 수행된다면 해당 토큰은 정상토큰
			//logger.debug("JWT claims : {}", claims);
			//logger.info("expireTime :" + claims.getExpiration());
			//logger.info("name :" + claims.get("name"));
			
			resMap.put("key", Converter.toStr(claims.get("key")));
			resMap.put("token_check", "SUCCESS");
		} catch (ExpiredJwtException exception) {
	          logger.debug("토큰 만료된 토큰 : {}", jwtToken);
	          resMap.put("token_check", "EXPIRE");
		} catch (JwtException exception) {
	          logger.debug("토큰 변조된 토큰 : {}", jwtToken);
	          logger.debug("토큰 변조 URL : {}", HttpUtils.getRemoteAddr(req));
	          resMap.put("token_check", "FALSIFY");
		}
		
		return resMap;
	}
}
