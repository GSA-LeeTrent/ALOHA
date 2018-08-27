package gov.gsa.ocfo.aloha.web.security;

import gov.gsa.ocfo.aloha.model.entity.AlohaUser;
import gov.gsa.ocfo.aloha.web.mb.UserMB;
import gov.gsa.ocfo.aloha.web.util.AlohaConstants;
import gov.gsa.ocfo.aloha.web.util.AlohaURIs;
import gov.gsa.ocfo.aloha.web.util.FacesContextUtil;
import gov.gsa.ocfo.aloha.web.util.StringUtil;

import javax.el.ELContext;
import javax.faces.FacesException;
import javax.faces.application.Application;
import javax.faces.application.ViewHandler;
import javax.faces.component.UIViewRoot;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;
import javax.servlet.http.HttpSession;

public class SecurityListener implements PhaseListener {
	private static final long serialVersionUID = -3930985672044549469L;
	private static final String FILE_EXTENSION = ".xhtml";
	private static final int MIN_FILE_NAME_LEN = 5;

	@Override
	public void beforePhase(PhaseEvent event) {

		try {
			FacesContext facesContext = event.getFacesContext();
			ExternalContext externalContext = facesContext.getExternalContext();
			String requestPathInfo = externalContext.getRequestPathInfo();

			// PREVENT USER FROM HACKING INTO A FOLDER AND MAKE USER THAT THE REQUEST IS FOR A ".xhtml" FILE
			if ( ! this.isValidRequest(requestPathInfo)) {
				Application app = facesContext.getApplication();
				ViewHandler viewHandler = app.getViewHandler();
				UIViewRoot view = viewHandler.createView(facesContext, AlohaPages.FORBIDDEN);
				facesContext.setViewRoot(view);
				facesContext.renderResponse();
				viewHandler.renderView(facesContext, view);
				facesContext.responseComplete();
			}

			// IF A SECURE RESOURCE HAS BEEN REQUESTED, DO THE FOLLOWING CHECKS:
			if ( this.isSecuredResource(requestPathInfo)) {
				// IF USER HAS SUCCESSFULLY LOGGED IN, AlohaUser OBJECT WILL BE POPULATED
				AlohaUser alohaUser = this.findUser(externalContext);
				if ( alohaUser == null ) {
					// USER HAS NOT YET LOGGED SO TAKE THEM TO THE LOGIN PAGE
					Application app = facesContext.getApplication();
					ViewHandler viewHandler = app.getViewHandler();
					UIViewRoot view = viewHandler.createView(facesContext, AlohaPages.LOGIN);
					facesContext.setViewRoot(view);
					facesContext.renderResponse();
					viewHandler.renderView(facesContext, view);
					facesContext.responseComplete();
				} else { // USER HAS SUCCESSFULLY LOGGED IN

					// CHECK TO SEE IF HE/SHE HAS ALREADY ACKNOWLEDGED OVERTIME POLICY
					if (!alohaUser.isOtPolicyAcknowledged()) { // USER HASN'T AS YET ACKNOWLEDGED OVERTIME POLICY

						// WE NEED TO RETRIEVE THE USER MB TO SET SOME PROPERTIES
						ELContext elContext = FacesContext.getCurrentInstance().getELContext();
						UserMB userMB = (UserMB) FacesContext.getCurrentInstance().getApplication()
						    .getELResolver().getValue(elContext, null, UserMB.BEAN_NAME);

						// HIDE LINKS IN BOTH THE HEADER AND FOOTER SECTIONS OF EACH PAGE
						userMB.setShowHeaderAndFooterLinks(false);

						// USER MAY BE COMING FROM THE OT ACKNOWLEDGE PAGE, SO CHECK THE REQUEST PARAMETER
						String param = FacesContextUtil.getHttpServletRequest().getParameter(AlohaConstants.OT_POLICY_ACKNOWLEDGED);
						if ( ! StringUtil.isNullOrEmpty(param)) {

							// SET OT POLICY ACKNOWLEDGED FLAG EQUAL TO THE PASSED-IN HTTP PARAMETER
							userMB.setOtPolicyAcknowledged(Boolean.parseBoolean(param));
						}
						// NOW CHECK AGAIN 
						if (!alohaUser.isOtPolicyAcknowledged()) { 
							Application app = facesContext.getApplication(); 
							ViewHandler viewHandler = app.getViewHandler(); 
							UIViewRoot view = viewHandler.createView(facesContext, AlohaPages.OT_ACKNOWLEDGE); 
							facesContext.setViewRoot(view); 
						} 						
					}
				}
			}
		} catch(Throwable t) {
			 throw new FacesException(t.getMessage(), t);
		}
	}

	private boolean isSecuredResource(String requestPathInfo) {
		return ( (requestPathInfo != null) && (requestPathInfo.contains(AlohaURIs.SECURE_FOLDER)) );
	}

	private AlohaUser findUser(ExternalContext externalContext) {
		AlohaUser user = null;
		if ( (externalContext != null)
				&& (externalContext.getSession(false) != null)
				&& (externalContext.getSession(false) instanceof HttpSession )
				&& ( ((HttpSession)externalContext.getSession(false)).getAttribute(AlohaUser.SESSION_KEY) != null )
				&& ( ((HttpSession)externalContext.getSession(false)).getAttribute(AlohaUser.SESSION_KEY) instanceof AlohaUser)
			) {
			user = (AlohaUser) FacesContextUtil.getHttpSession().getAttribute(AlohaUser.SESSION_KEY);
		}
		return user;
	}

	private boolean isValidRequest(String path) {
		if ( (!StringUtil.isNullOrEmpty(path))
				&& (path.trim().length() >= MIN_FILE_NAME_LEN)
				&& (path.endsWith(FILE_EXTENSION)) ) {

			return true;
		}
		return false;
	}

	@Override
	public void afterPhase(PhaseEvent event) {
	}

	@Override
	public PhaseId getPhaseId() {
		 return PhaseId.RESTORE_VIEW;
	}
}