package gov.gsa.ocfo.aloha.web.mb.ot.group;

import gov.gsa.ocfo.aloha.exception.AuthorizationException;
import gov.gsa.ocfo.aloha.exception.IllegalOperationException;
import gov.gsa.ocfo.aloha.web.util.AlohaConstants;
import gov.gsa.ocfo.aloha.web.util.ErrorMessages;
import gov.gsa.ocfo.aloha.web.util.FacesContextUtil;

import java.io.Serializable;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

@ManagedBean(name=OTGroupViewMB.MANAGED_BEAN_NAME, eager=false)
@ViewScoped
public class OTGroupViewMB extends OTGroupAbstractMB implements Serializable {
	
	// CLASS MEMBERS
	private static final long serialVersionUID = -1622172737543595511L;
	public static final String MANAGED_BEAN_NAME = "otGroupViewMB";
	public static final String GROUP_SESSION_KEY = MANAGED_BEAN_NAME + "_" + serialVersionUID + "_OTGroup"; 

	@PostConstruct
	public void init() {
		super.init();
	}
	// METHOD REQUIRED BY ABSTRACT SUPERCLASS
	protected void checkAccessRights() throws AuthorizationException {
		// CHECK ROLE
		if ( !this.userMB.isApprover() ) {
			Object[] params = { this.userMB.getFullName() };
			String errMsg = ErrorMessages.getInstance().getMessage(ErrorMessages.OT_GROUP_VIEW_UNAUTHORIZED, params);
			FacesContextUtil.getHttpSession().setAttribute(AlohaConstants.UNAUTHORIZED_MESSAGE, errMsg);
			throw new AuthorizationException(errMsg);
		}
		
		// MUST BE THE SUBMITTER OR RECEIVER
		if ( ( this.getOtGroup().getSubmitter().getUserId() != this.userMB.getUser().getUserId() )
				&& ( this.getOtGroup().getReceiver().getUserId() != this.userMB.getUser().getUserId() )
				
			) {
			
			Object[] params = { this.userMB.getFullName(), this.getOtGroup().getId() };
			String errMsg = ErrorMessages.getInstance().getMessage(ErrorMessages.OT_GROUP_VIEW_UNAUTHORIZED_SPECIFIC, params);
			FacesContextUtil.getHttpSession().setAttribute(AlohaConstants.UNAUTHORIZED_MESSAGE, errMsg);
			throw new AuthorizationException(errMsg);
		}
	}
	// METHOD REQUIRED BY ABSTRACT SUPERCLASS	
	protected void checkIfLegalOperation() throws IllegalOperationException {
		// no op - "checkAccessRights()" is sufficient
	}
	// METHOD REQUIRED BY ABSTRACT SUPERCLASS
	protected String getGroupSessionKey() {
		return GROUP_SESSION_KEY;
	}		
}