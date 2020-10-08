package propets.accounting.service.security;

import static propets.accounting.configuration.Constants.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import propets.accounting.dto.exception.TokenExpiredException;

@Service
public class TokenServiceImpl implements TokenService {
	
	@Value("${secret.value}")
	private String secret;
	
	@Autowired
	private SecretKey secretKey;
	
	@Override
	public String createToken(String login, String password) {
		return Jwts.builder()
				.claim("login", login)
				.claim("password", password)
				.claim("timestamp", Instant.now().plus(TOKEN_PERIOD_DAYS, ChronoUnit.DAYS).toEpochMilli())
				.signWith(SignatureAlgorithm.HS256, secretKey)
				.compact();
	}

	@Override
	public String tokenValidation(String token) {
		Jws<Claims> jws = Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token);
		Claims claims = jws.getBody();
		Instant time = Instant.ofEpochMilli(Long.parseLong(claims.get("timestamp").toString()));
		if (time.isBefore(Instant.now())) {
			throw new TokenExpiredException();
		}
		claims.put("timestamp", Instant.now().plus(TOKEN_PERIOD_DAYS, ChronoUnit.DAYS).toEpochMilli());
		token = Jwts.builder().setClaims(claims).compact();
		return token;
	}
	
	@Bean
	public SecretKey secretKey() {
		return new SecretKeySpec(Base64.getUrlEncoder().encode(secret.getBytes()), "AES");
	}

}
