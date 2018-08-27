package gov.gsa.ocfo.aloha.web.security;

import gov.gsa.ocfo.aloha.ejb.UserEJB;
import gov.gsa.ocfo.aloha.exception.AlohaServerException;
import gov.gsa.ocfo.aloha.exception.AuthorizationException;
import gov.gsa.ocfo.aloha.exception.ValidationException;
import gov.gsa.ocfo.aloha.model.entity.AlohaUser;
import gov.gsa.ocfo.aloha.util.StopWatch;
import gov.gsa.ocfo.aloha.util.StringUtil;

import java.io.IOException;
import java.util.Date;

import javax.ejb.EJB;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

public class SecurityFilter implements Filter {
	@EJB
	private UserEJB userEJB;

	public void init(FilterConfig fc) throws ServletException {}

	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		HttpServletRequest httpRequest = (HttpServletRequest)request;

		if ( httpRequest.getSession().getAttribute(AlohaUser.SESSION_KEY) == null ) {
			AlohaUser user = null;
			try {
				String userPrincipalName = httpRequest.getUserPrincipal().getName();
				if ( StringUtil.isNullOrEmpty(userPrincipalName) ) {
					String errMsg = "'httpRequest.getUserPrincipal().getName()' RETURNED NULL! CANNOT CONTINUE!";
					System.out.println(errMsg);
					throw new AlohaServerException(errMsg);
				}

				System.out.println("Attempting to authorize \"" + userPrincipalName + "\" against ALOHA tables.");
				StopWatch stopWatch = new StopWatch();
				stopWatch.start();
				user = userEJB.authorize(userPrincipalName);
				System.out.println("ELAPSED TIME (Authorize " +  userPrincipalName + "): " + stopWatch.getElapsedTime() + " ms");
				stopWatch.stop();
				stopWatch = null;

				if ( user == null) {
					String msg = "AlohaUser NOT FOUND for '" + userPrincipalName + "'. CANNOT CONTINUE!";
					System.out.println(msg);
					throw new AuthorizationException(msg);
				}

				System.out.println("\"" + userPrincipalName + "\" is authorized to use ALOHA on: ");
				System.out.println(new Date());
				
				/*************************************************************
				 * BEGIN: Write Client's IP Address and Request URI to the log
				 *************************************************************/
				StringBuilder sb = new StringBuilder();
				sb.append("\nClient IP Address for '");
				sb.append(userPrincipalName);
				sb.append("': '");
				sb.append(httpRequest.getRemoteAddr());
				sb.append("'");
				sb.append("\nrequestURL: '");
				sb.append(httpRequest.getRequestURL());
				sb.append("'");				
				sb.append("\nrequestURI: '");
				sb.append(httpRequest.getRequestURI());
				sb.append("'");
				
				System.out.println(sb.toString());
				System.out.println(new Date());
				
				/*************************************************************
				 * END: Write Client's IP Address and Request URI to the log
				 *************************************************************/				
				
				/**********************************************
				 * BEGIN: Write Client's Cookie Info to the log
				 **********************************************/
				sb = new StringBuilder();
				sb.append("Cookies for '");
				sb.append(userPrincipalName);
				sb.append("': [");

				Cookie[] cookies = httpRequest.getCookies();
				for ( Cookie cookie : cookies ) {
					sb.append("\ndomain=");
					sb.append(cookie.getDomain());
					sb.append(",path=");
					sb.append(cookie.getPath());
					sb.append(",name=");
					sb.append(cookie.getName());
					sb.append(",value=");
					sb.append(cookie.getValue());
					sb.append(",maxAge=");
					sb.append(cookie.getMaxAge());
					sb.append(",version=");
					sb.append(cookie.getVersion());
					sb.append(",secure=");
					sb.append(cookie.getSecure());
					if ( cookie.getComment() != null) {
						sb.append(",comment=");
						sb.append(cookie.getComment());
					}
				}
				sb.append("]");
				System.out.println(sb.toString());
				System.out.println(new Date());
				
				/**********************************************
				 * END: Write Client's Cookie Info to the log
				 **********************************************/

				/**********************************************
				 * BEGIN: Write Session ID Info to the log
				 **********************************************/
				sb = new StringBuilder();
				sb.append("\nSession ID Info for '");
				sb.append(userPrincipalName);
				sb.append("':\n[");
				sb.append("\nremoteUser=");
				sb.append(httpRequest.getRemoteUser());
				sb.append("\nrequestedSessionId=");
				sb.append(httpRequest.getRequestedSessionId());
				sb.append("\nisRequestedSessionIdValid=");
				sb.append(httpRequest.isRequestedSessionIdValid());
				sb.append("\nisRequestedSessionIdFromCookie=");
				sb.append(httpRequest.isRequestedSessionIdFromCookie());
				sb.append("\nisRequestedSessionIdFromURL=");
				sb.append(httpRequest.isRequestedSessionIdFromURL());
				sb.append("\n]");
				
				System.out.println(sb.toString());
				System.out.println(new Date());
				
				/**********************************************
				 * END: Write Session ID Info to the log
				 **********************************************/

				sb = null;

				//System.out.println("ELAPSED TIME (authorization): " + stopWatch.getElapsedTime() + " ms");

				httpRequest.getSession().setAttribute(AlohaUser.SESSION_KEY, user);
			} catch (AuthorizationException authException ) {
				httpRequest.getSession().invalidate();
				RequestDispatcher dispatcher = request.getRequestDispatcher("/faces/pages/public/login.xhtml?errorMsg=" + authException.getMessage());
				dispatcher.forward(request, response);
			} catch (ValidationException valException ) {
				httpRequest.getSession().invalidate();
				RequestDispatcher dispatcher = request.getRequestDispatcher("/faces/pages/public/login.xhtml?errorMsg=" + valException.getMessage());
				dispatcher.forward(request, response);
			}  catch (AlohaServerException ase ) {
				httpRequest.getSession().invalidate();
				RequestDispatcher dispatcher = request.getRequestDispatcher("/faces/pages/public/login.xhtml?errorMsg=There has been an unexpected error. Please contact support.");
				dispatcher.forward(request, response);
			} catch (Exception e) {
				e.printStackTrace();
				httpRequest.getSession().invalidate();
				RequestDispatcher dispatcher = request.getRequestDispatcher("/faces/pages/public/login.xhtml?errorMsg=There has been an unexpected error. Please contact support.");
				dispatcher.forward(request, response);
			}
		} else {

			if ( httpRequest != null 
					&& httpRequest.getUserPrincipal() != null ) {
				
				String userPrincipalName = httpRequest.getUserPrincipal().getName();
				
				/*************************************************************
				 * BEGIN: Write Client's IP Address and Request URI to the log
				 *************************************************************/
				StringBuilder sb = new StringBuilder();
				sb.append("\nClient IP Address for '");
				sb.append(userPrincipalName);
				sb.append("': '");
				sb.append(httpRequest.getRemoteAddr());
				sb.append("'");
				sb.append("\nrequestURL: '");
				sb.append(httpRequest.getRequestURL());
				sb.append("'");				
				sb.append("\nrequestURI: '");
				sb.append(httpRequest.getRequestURI());
				sb.append("'");
				
				System.out.println(sb.toString());
				System.out.println(new Date());
				
				/*************************************************************
				 * END: Write Client's IP Address and Request URI to the log
				 *************************************************************/
			}
		}
		chain.doFilter(request, response);
	}
	public void destroy() {}
}