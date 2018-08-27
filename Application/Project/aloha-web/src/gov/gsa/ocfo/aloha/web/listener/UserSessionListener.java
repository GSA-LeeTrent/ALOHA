package gov.gsa.ocfo.aloha.web.listener;

import gov.gsa.ocfo.aloha.ejb.UserEJB;
import gov.gsa.ocfo.aloha.exception.AlohaServerException;
import gov.gsa.ocfo.aloha.model.entity.AlohaUser;
import gov.gsa.ocfo.aloha.model.entity.audit.EventType;
import gov.gsa.ocfo.aloha.web.util.AlohaConstants;
import gov.gsa.ocfo.aloha.web.util.CookieUtil;


import javax.ejb.EJB;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

public class UserSessionListener implements HttpSessionListener {
	@EJB
	private UserEJB userEJB;
	
	@Override
	public void sessionCreated(HttpSessionEvent sessionEvent) {
		//do nothing
	}

	@Override
	public void sessionDestroyed(HttpSessionEvent sessionEvent) {
		//System.out.println("UserSessionListener.sessionDestroyed() : BEGIN");
		
		CookieUtil.expireCookie(AlohaConstants.lastAlohaHomeTabVisited);
		HttpSession httpSession = sessionEvent.getSession();
		//sak 20120108 this session key is set to null in UserMB logout
		if (httpSession.getAttribute(AlohaUser.SESSION_KEY) == null) {
			//do nothing
		} else {
			try {
				//if a timeout occurs, AlohaUser session variable will contain a value
				AlohaUser user = (AlohaUser) httpSession.getAttribute(AlohaUser.SESSION_KEY);
				//System.out.println("alohaUser: " + user);
				this.userEJB.writeToEventLog(user, EventType.EventTypeValue.SESSION_TIMEOUT);
			} catch (AlohaServerException e) {
				e.printStackTrace();
			}			
		}
		//System.out.println("UserSessionListener.sessionDestroyed() : END");
	}

}