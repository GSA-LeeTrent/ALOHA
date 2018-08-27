package gov.gsa.ocfo.aloha.web.util;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

public class CookieUtil {

	public static Cookie findCookie(String cookieName) {
		FacesContext facesContext = FacesContext.getCurrentInstance();
		if ( facesContext != null ) {
			ExternalContext externalContext = facesContext.getExternalContext();
			if ( externalContext != null ) {
				HttpServletRequest httpRequest = (HttpServletRequest)externalContext.getRequest();
				if ( httpRequest != null ) {
					Cookie[] cookies = FacesContextUtil.getCookies();
					if ( (cookies != null) && (cookies.length > 0) ) {
						for (Cookie cookie : cookies) {
							if ( cookie.getName().equals(cookieName)) {
								return cookie;
							}
						}	
					}
				}
			}
		}
		return null;
	}
	public static void expireCookie(String cookieName) {
		Cookie cookie = findCookie(cookieName);
		if ( cookie != null) {
			cookie.setMaxAge(0);
		}
	}
}
