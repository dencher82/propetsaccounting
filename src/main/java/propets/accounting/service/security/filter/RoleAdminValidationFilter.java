package propets.accounting.service.security.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import propets.accounting.dao.AccountingRepository;
import propets.accounting.model.Account;

@Service
@Order(20)
public class RoleAdminValidationFilter implements Filter {
	
	@Autowired
	AccountingRepository repository;

	@Override
	public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)
			throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) resp;
		if (checkPathAndMethod(request.getServletPath(), request.getMethod())) {
			try {
				String login = request.getAttribute("login").toString();
				Account account = repository.findById(login).orElse(null);
				if (!account.getRoles().stream().anyMatch(r -> "ADMIN".equals(r))) {
					System.out.println("nonEqualAdmin");
					response.sendError(403);
					return;
				}
			} catch (NullPointerException e) {
				e.printStackTrace();
				response.sendError(401);
				return;
			}
//			try {
//				TokenDto tokenDto = tokenService.getTokenInfo(token);
//				String roles = tokenDto.getRoles();
//				if (!roles.contains("ADMIN")) {
//					response.sendError(403);
//					return;
//				}
//			} catch (NullPointerException e) {
//				e.printStackTrace();
//				response.sendError(401);
//				return;
//			}
		}
		
		chain.doFilter(request, response);
	}

	private boolean checkPathAndMethod(String path, String method) {
		boolean res = path.matches("/account/en/v1/[\\p{Graph}^/]+/role/[a-zA-Z]+/?");
		System.out.println("RoleAdminValidationFilter=" + res);
		return res;
	}

}
