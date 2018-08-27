package gov.gsa.ocfo.aloha.web.mb.ot;

import gov.gsa.ocfo.aloha.exception.AuthorizationException;
import gov.gsa.ocfo.aloha.exception.IllegalOperationException;
import gov.gsa.ocfo.aloha.web.security.NavigationOutcomes;
import gov.gsa.ocfo.aloha.web.util.AlohaConstants;
import gov.gsa.ocfo.aloha.web.util.ErrorMessages;
import gov.gsa.ocfo.aloha.web.util.FacesContextUtil;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

@ManagedBean(name="otCancelSOMB")
@ViewScoped
public class OTCancelSOMB extends OTCancelAbstractMB {
	private static final long serialVersionUID = -7542810982056374019L;

	@PostConstruct
	public void init() {
		super.init();
	}

	// METHOD REQUIRED BY ABSTRACT SUPERCLASS
	protected void checkAccessRights() throws AuthorizationException {
		// CHECK ROLE
		if ( ! this.userMB.isSubmitOwn() ) {
			Object[] params = { this.userMB.getFullName() };
			String errMsg = ErrorMessages.getInstance().getMessage(ErrorMessages.OT_CANCEL_UNAUTHORIZED_SO, params);
			FacesContextUtil.getHttpSession().setAttribute(AlohaConstants.UNAUTHORIZED_MESSAGE, errMsg);
			throw new AuthorizationException(errMsg);
		}
		// CHECK SUBMITTER
		if ( ! this.otDetail.isEmployeeUserId(this.userMB.getUserId()) ) {
			Object[] params = { this.userMB.getFullName(), this.otDetail.getRequestId() };
			String errMsg = ErrorMessages.getInstance().getMessage(ErrorMessages.OT_CANCEL_UNAUTHORIZED_SO_SPECIFIC, params);
			FacesContextUtil.getHttpSession().setAttribute(AlohaConstants.UNAUTHORIZED_MESSAGE, errMsg);
			throw new AuthorizationException(errMsg);
		}
	}
	// METHOD REQUIRED BY ABSTRACT SUPERCLASS
	protected void checkIfLegalOperation() throws IllegalOperationException {
		if 	(  ! this.otDetail.isCancellableBySubmitOwn() ) {
			Object[] params = { this.otDetail.getRequestId(), this.otDetail.getStatus().getName() };
			String errMsg = ErrorMessages.getInstance().getMessage(ErrorMessages.OT_CANCEL_INVALID_STATUS_SO, params);
			FacesContextUtil.getHttpSession().setAttribute(AlohaConstants.ILLEGAL_OPERATION_MSG, errMsg);
			throw new IllegalStateException(errMsg);
		}
	}
	// METHOD REQUIRED BY ABSTRACT SUPERCLASS
	protected String getSupervisorRemarks() {
		return null;		
	}		
	// METHOD REQUIRED BY ABSTRACT SUPERCLASS
	protected String getSubmitterRemarks() {
		return this.getCancellationRemarks();
	}
	
	// METHOD REQUIRED BY ABSTRACT SUPERCLASS
	protected String getSuccessPage()	{
		return NavigationOutcomes.OT_LIST_SO;
	}	
	// METHOD REQUIRED BY ABSTRACT SUPERCLASS
	protected String getStatusChangeOutcomeKey() {
		return AlohaConstants.OT_STATUS_CHANGE_OUTCOME_SO;
	}	
}