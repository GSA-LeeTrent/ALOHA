package gov.gsa.ocfo.aloha.web.util;

import gov.gsa.ocfo.aloha.web.security.AlohaPages;

import java.io.IOException;

import javax.faces.FacesException;
import javax.faces.application.Application;
import javax.faces.application.ConfigurableNavigationHandler;
import javax.faces.application.ViewHandler;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class FacesContextUtil {
	public static HttpServletRequest getHttpServletRequest() {
		return (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
	}
	public static HttpServletResponse getHttpServletResponse() {
		return (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext().getResponse();
	}
	public static HttpSession getHttpSession() {
		return (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
	}
	public static ConfigurableNavigationHandler getConfigurableNavigationHandler() {
		return (ConfigurableNavigationHandler)FacesContext.getCurrentInstance().getApplication().getNavigationHandler();
	}
	public static String getRequestParamaterValue(String key) {
		return FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get(key);
	}
	public static Cookie[] getCookies() {
		 return getHttpServletRequest().getCookies();
	}
	public static void callHome() {
		try {
			FacesContext facesContext = FacesContext.getCurrentInstance();
			if (facesContext.getViewRoot() != null) {
				String viewId = facesContext.getViewRoot().getViewId();				
				if ( (!viewId.contains(AlohaURIs.PUBLIC_FOLDER)) 
						&& (!viewId.contains(AlohaURIs.HOME)) ) {
					callHome(facesContext);
				}				
			} else {
				callHome(facesContext);			
			}
		} catch(Throwable t) {
			 throw new FacesException(t.getMessage(), t);
		}
	}
	private static void callHome(FacesContext facesContext) throws IOException {
		Application app = facesContext.getApplication();
		ViewHandler viewHandler = app.getViewHandler();
		UIViewRoot view = viewHandler.createView(facesContext, AlohaURIs.HOME);
		facesContext.setViewRoot(view);
		facesContext.renderResponse();
		viewHandler.renderView(facesContext, view);
		facesContext.responseComplete();
	}
	public static void redirectToLoginPagge() throws FacesException, IOException {
		FacesContext facesContext = FacesContext.getCurrentInstance();
		Application app = facesContext.getApplication();
		ViewHandler viewHandler = app.getViewHandler();
		UIViewRoot view = viewHandler.createView(facesContext, AlohaPages.LOGIN);
		facesContext.setViewRoot(view);
		facesContext.renderResponse();
		viewHandler.renderView(facesContext, view);
		facesContext.responseComplete();
	}
}