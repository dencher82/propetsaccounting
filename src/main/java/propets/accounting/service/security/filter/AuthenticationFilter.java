package propets.accounting.service.security.filter;

import java.io.IOException;
import java.security.Principal;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import propets.accounting.dto.exception.AccountNotFoundException;
import propets.accounting.dto.exception.UnauthorizedException;
import propets.accounting.service.security.AccountingSecurity;

@Service
public class AuthenticationFilter implements Filter{
	
	@Autowired
	AccountingSecurity securityService;
	
	@Override
	public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)
			throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) resp;
		String path = request.getServletPath();
		String token = request.getHeader("Authorization");
		if ("/account/en/v1/login".equals(path)) {
			try {
				String login = securityService.authentication(token);
				request = new WrapperRequest(request, login);
			} catch (AccountNotFoundException e) {
				response.sendError(404, "Account not found");
				return;
			} catch (UnauthorizedException e) {
				response.sendError(401);
				return;
			} catch (Exception e) {
				response.sendError(400);
				return;
			}
		}
		
		chain.doFilter(request, response);
	}
	
	private class WrapperRequest extends HttpServletRequestWrapper {
		String user;
		
		public WrapperRequest(HttpServletRequest request, String user) {
			super(request);
			this.user = user;
		}
		
		@Override
		public Principal getUserPrincipal() {
			return new Principal() {
				
				@Override
				public String getName() {
					return user;
				}
			};
		}
	}

}
