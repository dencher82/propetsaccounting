package propets.accounting.service.security;

public interface TokenService {
	
	String createToken(String login, String password);
	
	String tokenValidation(String token);
	
}
