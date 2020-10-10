package propets.accounting.service.security.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

@Service
@Order(30)
public class AccountValidationFilter implements Filter {

	@Override
	public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)
			throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) resp;
		String path = request.getServletPath();
		if (checkPathAndMethod(path, request.getMethod())) {
			try {
				String login = request.getAttribute("login").toString();
				String loginFromPath = path.split("/")[4];
				if (!login.equals(loginFromPath)) {
					System.out.println("nonAuthor");
					response.sendError(403);
					return;
				}
			} catch (NullPointerException e) {
				e.printStackTrace();
				response.sendError(401);
				return;
			}
		}
		
		chain.doFilter(request, response);
	}

	private boolean checkPathAndMethod(String path, String method) {
		boolean res = path.matches("/account/en/v1/[\\w.%+-]+@[\\w.-]+\\.[a-zA-Z]{2,6}+/?")
				&& ("PUT".equalsIgnoreCase(method) || "DELETE".equalsIgnoreCase(method));
		res = res || (path.matches("/account/en/v1/[\\w.%+-]+@[\\w.-]+\\.[a-zA-Z]{2,6}+/activity/\\w+/?")
				&& ("PUT".equalsIgnoreCase(method) || "DELETE".equalsIgnoreCase(method)));
		res = res || (path.matches("/account/en/v1/[\\w.%+-]+@[\\w.-]+\\.[a-zA-Z]{2,6}+/favorite/\\w+/?")
				&& ("PUT".equalsIgnoreCase(method) || "DELETE".equalsIgnoreCase(method)));
		System.out.println("AccountValidationFilter=" + res);
		return res;
	}
}