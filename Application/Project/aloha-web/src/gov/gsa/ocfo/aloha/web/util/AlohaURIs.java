package gov.gsa.ocfo.aloha.web.util;

public interface AlohaURIs {
	public final String UNAUTHORIZED = "/aloha/faces/pages/errors/unauthorized.xhtml";
	public final String SERVER_ERROR = "/aloha/faces/pages/errors/serverError.xhtml";
	public final String ILLEGAL_OPERATION = "/aloha/faces/pages/errors/illegalOperation.xhtml";
	public final String USER_ERROR = "/aloha/faces/pages/errors/userError.xhtml";
	
	public final String SECURE_FOLDER = "/secure/";
	public final String PUBLIC_FOLDER = "/public/";
	
	public final String HOME = "/pages/secure/home.xhtml";
	public final String LOGIN = "/aloha/faces/pages/public/login.xhtml";
	
	public final String LANDING_PAGE_PRD = "http://aloha.gsa.gov";
	public final String LANDING_PAGE_TST = "http://test-apps.ocfo.gsa.gov/alohaweb";
	public final String LANDING_PAGE_DEV = "http://dev-apps.ocfo.gsa.gov/alohaweb";
	
	public final String SSO_LOGIN_PRD = "https://apps.ocfo.gsa.gov/aloha";
	public final String SSO_LOGIN_TST = "https://test-apps.ocfo.gsa.gov/aloha";
	public final String SSO_LOGIN_DEV = "https://dev-apps.ocfo.gsa.gov/aloha";
}