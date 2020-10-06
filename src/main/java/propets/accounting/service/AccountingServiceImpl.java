package propets.accounting.service;

import java.util.Set;

import org.apache.commons.validator.routines.EmailValidator;
import org.mindrot.jbcrypt.BCrypt;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import propets.accounting.dao.AccountingRepository;
import propets.accounting.dto.AccountCreateDto;
import propets.accounting.dto.AccountDto;
import propets.accounting.dto.AccountUpdateDto;
import propets.accounting.dto.exception.AccountExistsException;
import propets.accounting.dto.exception.AccountNotFoundException;
import propets.accounting.dto.exception.BadRequestException;
import propets.accounting.model.Account;
import propets.accounting.service.security.AccountingSecurity;

@Service
public class AccountingServiceImpl implements AccountingService {

	@Autowired
	ModelMapper mapper;

	@Autowired
	AccountingRepository repository;

	@Autowired
	AccountingSecurity securityService;

	@Value("${default.avatar}")
	private String defaultAvatar;

	@Value("${default.role}")
	private String defaultRole;

	@Override
	public AccountDto registerUser(AccountCreateDto accountCreateDto) {
		if (repository.existsById(accountCreateDto.getEmail())) {
			throw new AccountExistsException(accountCreateDto.getEmail());
		}
		passwordCheck(accountCreateDto.getPassword());
		loginCheck(accountCreateDto.getEmail());
		String hashPassword = BCrypt.hashpw(accountCreateDto.getPassword(), BCrypt.gensalt());
		Account account = new Account(accountCreateDto.getEmail(), accountCreateDto.getName());
		account.setPassword(hashPassword);
		account.setAvatar(defaultAvatar);
		account.addRole(defaultRole);
		repository.save(account);
		return mapper.map(account, AccountDto.class);
	}

	private void loginCheck(String email) {
		EmailValidator validator = EmailValidator.getInstance();
		if (!validator.isValid(email)) {
			throw new BadRequestException();
		}
	}

	private void passwordCheck(String password) {
		if (password.length() < 7) {
			throw new BadRequestException();
		} else {
			boolean upper = false;
			boolean lower = false;
			boolean number = false;
			for (char c : password.toCharArray()) {
				if (Character.isUpperCase(c)) {
					upper = true;
				} else if (Character.isLowerCase(c)) {
					lower = true;
				} else if (Character.isDigit(c)) {
					number = true;
				}
			}
			if (!upper || !lower || !number) {
				throw new BadRequestException();
			}
		}
	}

	@Override
	public AccountDto loginUser(String login) {
		Account account = repository.findById(login).orElseThrow(() -> new AccountNotFoundException(login));
		return mapper.map(account, AccountDto.class);
	}

	@Override
	public AccountDto getUser(String login) {
		Account account = repository.findById(login).orElseThrow(() -> new AccountNotFoundException(login));
		return mapper.map(account, AccountDto.class);
	}

	@Override
	public AccountDto updateUser(String login, AccountUpdateDto accountUpdateDto) {
		Account account = repository.findById(login).orElseThrow(() -> new AccountNotFoundException(login));
		if (accountUpdateDto.getAvatar() != null && !accountUpdateDto.getAvatar().isEmpty()) {
			account.setAvatar(accountUpdateDto.getAvatar());
		}
		if (accountUpdateDto.getName() != null && !accountUpdateDto.getName().isEmpty()) {
			account.setName(accountUpdateDto.getName());
		}
		if (accountUpdateDto.getPhone() != null && !accountUpdateDto.getPhone().isEmpty()) {
			account.setPhone(accountUpdateDto.getPhone());
		}
		repository.save(account);
		return mapper.map(account, AccountDto.class);
	}

	@Override
	public AccountDto removeUser(String login) {
		Account account = repository.findById(login).orElseThrow(() -> new AccountNotFoundException(login));
		repository.deleteById(login);
		return mapper.map(account, AccountDto.class);
	}

	@Override
	public Set<String> addRole(String login, String role) {
		Account account = repository.findById(login).orElseThrow(() -> new AccountNotFoundException(login));
		account.addRole(role);
		repository.save(account);
		return account.getRoles();
	}

	@Override
	public Set<String> deleteRole(String login, String role) {
		Account account = repository.findById(login).orElseThrow(() -> new AccountNotFoundException(login));
		account.removeRole(role);
		repository.save(account);
		return account.getRoles();
	}

	@Override
	public boolean blockUser(String login, String blockStatus) {
		Account account = repository.findById(login).orElseThrow(() -> new AccountNotFoundException(login));
		if ("true".equals(blockStatus)) {
			account.setFlBlocked(true);
			repository.save(account);
			return true;
		}
		if ("false".equals(blockStatus)) {
			account.setFlBlocked(false);
			repository.save(account);
			return true;
		}
		return false;
	}

	@Override
	public void addFavorite(String login, String postId) {
		Account account = repository.findById(login).orElseThrow(() -> new AccountNotFoundException(login));
		account.addFavorite(postId);
		repository.save(account);
	}

	@Override
	public void addActivity(String login, String postId) {
		Account account = repository.findById(login).orElseThrow(() -> new AccountNotFoundException(login));
		account.addActivity(postId);
		repository.save(account);
	}

	@Override
	public void removeFavorite(String login, String postId) {
		Account account = repository.findById(login).orElseThrow(() -> new AccountNotFoundException(login));
		account.removeFavorite(postId);
		repository.save(account);
	}

	@Override
	public void removeActivity(String login, String postId) {
		Account account = repository.findById(login).orElseThrow(() -> new AccountNotFoundException(login));
		account.removeActivity(postId);
		repository.save(account);
	}

	@Override
	public Set<String> getUserDate(String login, boolean dataType) {
		Account account = repository.findById(login).orElseThrow(() -> new AccountNotFoundException(login));
		if (dataType) {
			return account.getActivities();
		} else {
			return account.getFavorites();
		}
	}

	@Override
	public void tokenValidation() {
		// TODO Auto-generated method stub

	}

}
