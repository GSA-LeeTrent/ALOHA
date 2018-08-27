package gov.gsa.ocfo.aloha.web.mb;

import gov.gsa.ocfo.aloha.ejb.UserEJB;
import gov.gsa.ocfo.aloha.exception.AlohaServerException;
import gov.gsa.ocfo.aloha.model.entity.AlohaUser;
import gov.gsa.ocfo.aloha.model.entity.audit.EventType;
import gov.gsa.ocfo.aloha.web.util.AlohaURIs;
import gov.gsa.ocfo.aloha.web.util.FacesContextUtil;

import java.io.IOException;
import java.io.Serializable;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import weblogic.servlet.security.ServletAuthentication;

@ManagedBean(name="userMB")
@SessionScoped
public class UserMB implements Serializable {
	private static final long serialVersionUID = -7200974450683640154L;
	public static final String SESSION_KEY = "userMB";
	public static final String BEAN_NAME = "userMB";

	@EJB
	private UserEJB userEJB;
	
	private boolean showFooterLinks = true;
	private boolean showHeaderLinks = true;
	
	// JSF BEAN
	@ManagedProperty(value="#{constantsMB}")
	protected ConstantsMB constantsMB;
	public void setConstantsMB(ConstantsMB constantsMB) {
		this.constantsMB = constantsMB;
	}

	private AlohaUser user;

	@PostConstruct
	public void init() {
		try {
			if ( (FacesContext.getCurrentInstance().getExternalContext() != null)
					&& (FacesContext.getCurrentInstance().getExternalContext().getSession(false) != null)
					&& (FacesContext.getCurrentInstance().getExternalContext().getSession(false) instanceof HttpSession )
					&& ( ((HttpSession)FacesContext.getCurrentInstance().getExternalContext().getSession(false)).getAttribute(AlohaUser.SESSION_KEY) != null )
					&& ( ((HttpSession)FacesContext.getCurrentInstance().getExternalContext().getSession(false)).getAttribute(AlohaUser.SESSION_KEY) instanceof AlohaUser)
				) {
				this.user = (AlohaUser) FacesContextUtil.getHttpSession().getAttribute(AlohaUser.SESSION_KEY);
			}
		} catch (Exception e) {
			e.printStackTrace();
			try {
				FacesContext.getCurrentInstance().getExternalContext().redirect(AlohaURIs.SERVER_ERROR);
			} catch( IOException ioe) {
				ioe.printStackTrace();
			}
		}
 	}
	public AlohaUser getUser() {
		if ( this.user == null) {
			this.init();
		}
		return this.user;
	}
	public Long getUserId() {
		return this.getUser().getUserId();
	}
	public String getLoginName(){
		return this.getUser().getLoginName();
	}
	public String getFullName() {
		return this.getUser().getFullName();
	}
	public String getEmailAddress() {
		return this.getUser().getEmailAddress();
	}
	public boolean isSubmitOwn() {
		return this.getUser().isSubmitOwn();
	}
	public boolean isOnBehalfOf() {
		return this.getUser().isOnBehalfOf();
	}
	public boolean isApprover() {
		return this.getUser().isApprover();
	}
	public boolean isAlohaAdmin() {
		return this.getUser().isAlohaAdmin();
	}
	public boolean isLoggedIn() {
		return (this.getUser() != null);
	}
	public boolean isHasAmendRole() {
		return ( this.isSubmitOwn() || this.isOnBehalfOf());
	}
	public boolean isHasApproverRole() {
		return ( this.isApprover() );
	}
	public String logout() throws IOException {
		//System.out.println("UserMB.logout() : BEGIN");
		//System.out.println("AlohaUser: " + this.getUser());
		
		try {

			this.userEJB.writeToEventLog(this.getUser(), EventType.EventTypeValue.USER_LOGOUT);
		} catch (AlohaServerException e) {
			e.printStackTrace();
			FacesContext.getCurrentInstance().getExternalContext().redirect(AlohaURIs.SERVER_ERROR);
		} catch (Exception e) {
			e.printStackTrace();
			FacesContext.getCurrentInstance().getExternalContext().redirect(AlohaURIs.SERVER_ERROR);
		}
		this.user = null;
		HttpSession session = FacesContextUtil.getHttpSession();

		//sak 20120208 set session var null so SessionDestroyed can distinguish between Timeout and Logout i.e.
		//if a timeout occurs, session var will NOT be null
		// ltt 20120522 - adding null check
		if (session != null) {
			session.setAttribute(AlohaUser.SESSION_KEY, null);
			session.invalidate();
			//sak 20120816 - added per Oracle SR -- SAML logout
			ServletAuthentication.logout((HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest());
			
		}
		//HttpServletRequest req = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
		//String baseURL = req.getRequestURL().toString().replace(req.getRequestURI(), "");
		//String baseURL = req.getRequestURI().toString();
		
		String landingPage;
		if (constantsMB.getEnv().equals(ConstantsMB.envType.dev)) {
			landingPage = AlohaURIs.LANDING_PAGE_DEV;
		} else if (constantsMB.getEnv().equals(ConstantsMB.envType.tst)) {
			landingPage = AlohaURIs.LANDING_PAGE_TST;
		} else {
			landingPage = AlohaURIs.LANDING_PAGE_PRD;
		}
		
		//System.out.println("Landing Page redirect = " + landingPage);
		FacesContext.getCurrentInstance().getExternalContext().redirect(landingPage);
		//return NavigationOutcomes.LOGOUT_SUCCESS;
		//System.out.println("UserMB.logout() : END (returning null");
		return null;
	}
	
	public boolean isOtPolicyAcknowledged() {
		return	( this.isLoggedIn() 
					&& this.getUser().isOtPolicyAcknowledged()
				);	
	}	
	public void setOtPolicyAcknowledged(boolean otPolicyAcknowledged) {
		this.getUser().setOtPolicyAcknowledged(otPolicyAcknowledged);
		this.setShowHeaderAndFooterLinks(otPolicyAcknowledged);
 		try {
			this.userEJB.writeToEventLog(this.getUser(), EventType.EventTypeValue.OT_POLICY_ACKNOWLEDGED);
		} catch (AlohaServerException e) {
			e.printStackTrace();
		}
	}	
	
	public boolean isLrVarianceAcknowledged() {
		return	( this.isLoggedIn() 
					&& this.getUser().isLrVarianceAcknowledged()
				);	
	}	
	public void setLrVarianceAcknowledged(boolean lrVarianceAcknowledged) {
		this.getUser().setLrVarianceAcknowledged(lrVarianceAcknowledged);
 		try {
			this.userEJB.writeToEventLog(this.getUser(), EventType.EventTypeValue.LR_VARIANCE_ACKNOWLEDGED);
		} catch (AlohaServerException e) {
			e.printStackTrace();
		}
	}
	
	public boolean isShowLeaveReconWizard() {
		return this.getUser().isShowLeaveReconWizard();
	}
	public boolean isShowFooterLinks() {
		return showFooterLinks;
	}
	public void setShowFooterLinks(boolean showFooterLinks) {
		this.showFooterLinks = showFooterLinks;
	}
	public boolean isShowHeaderLinks() {
		return showHeaderLinks;
	}
	public void setShowHeaderLinks(boolean showHeaderLinks) {
		this.showHeaderLinks = showHeaderLinks;
	}
	public void setShowHeaderAndFooterLinks(boolean bool) {
		this.setShowHeaderLinks(bool);
		this.setShowFooterLinks(bool);
	}
}