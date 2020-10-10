package propets.accounting.service.security;

import propets.accounting.dto.TokenDto;
import propets.accounting.model.Account;

public interface TokenService {
	
	String createToken(Account account);
	
	String tokenValidation(String token);

	TokenDto getTokenInfo(String token);

	String getLogin(String token);
	
}
