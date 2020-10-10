package propets.accounting.service.security;

import static propets.accounting.configuration.Constants.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.stream.Collectors;

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
import propets.accounting.dto.TokenDto;
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
		String roles = account.getRoles().stream()
				.collect(Collectors.joining(":"));
		return Jwts.builder()
				.claim("login", account.getEmail())
				.claim("name", account.getName())
				.claim("avatar", account.getAvatar())
				.claim("phone", account.getPhone())
				.claim("roles", roles)
				.claim("flBlocked", account.isFlBlocked())
				.claim("timeUnblock", account.getTimeUnblock())
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
		//TODO reinitialize all fields
		claims.put("timestamp", Instant.now().plus(TOKEN_PERIOD_DAYS, ChronoUnit.DAYS).toEpochMilli());
		token = Jwts.builder().setClaims(claims).compact();
		return token;
	}
	
	@Override
	public TokenDto getTokenInfo(String token) {
		Jws<Claims> jws = Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token);
		Claims claims = jws.getBody();
		return TokenDto.builder()
				.email(claims.get("login", String.class))
				.name(claims.get("name", String.class))
				.avatar(claims.get("avatar", String.class))
				.phone(claims.get("phone", String.class))
				.roles(claims.get("roles", String.class))
				.flBlocked(claims.get("flBlocked", Boolean.class))
				.timeUnblock(claims.get("timeUnblock", Long.class))
				.build();
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
