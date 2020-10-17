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
import propets.accounting.model.Account;

@Service
public class TokenServiceImpl implements TokenService {
	
	@Value("${secret.value}")
	private String secret;
	
	@Autowired
	private SecretKey secretKey;
	
	@Override
	public String createToken(Account account) {
		return Jwts.builder()
				.claim("login", account.getEmail())
				.claim("password", account.getPassword())
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
		token = Jwts.builder().setClaims(claims).signWith(SignatureAlgorithm.HS256, secretKey).compact();
		return token;
	}
	
	@Override
	public String getLogin(String token) {
		Jws<Claims> jws = Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token);
		Claims claims = jws.getBody();
		return claims.get("login", String.class);
	}
	
	@Bean
	public SecretKey secretKey() {
		return new SecretKeySpec(Base64.getUrlEncoder().encode(secret.getBytes()), "AES");
	}

}
