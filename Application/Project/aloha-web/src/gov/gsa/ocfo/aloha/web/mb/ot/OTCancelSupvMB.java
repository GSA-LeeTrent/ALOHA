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

@ManagedBean(name="otCancelSupvMB")
@ViewScoped
public class OTCancelSupvMB extends OTCancelAbstractMB {
	private static final long serialVersionUID = 8999577181692238183L;
	
	@PostConstruct
	public void init() {
		super.init();
	}
	// METHOD REQUIRED BY ABSTRACT SUPERCLASS
	protected void checkAccessRights() throws AuthorizationException {
		// CHECK ROLE
		if ( ! this.userMB.isApprover() ) {
			Object[] params = { this.userMB.getFullName() };
			String errMsg = ErrorMessages.getInstance().getMessage(ErrorMessages.OT_CANCEL_UNAUTHORIZED_SUPV, params);
			FacesContextUtil.getHttpSession().setAttribute(AlohaConstants.UNAUTHORIZED_MESSAGE, errMsg);
			throw new AuthorizationException(errMsg);
		}
		
//		// CHECK SUPERVISOR
//		if ( ! this.otDetail.isSupervisorUserId(this.userMB.getUserId()) ) {
//			Object[] params = { this.userMB.getFullName(), this.otDetail.getRequestId() };
//			String errMsg = ErrorMessages.getInstance().getMessage(ErrorMessages.OT_CANCEL_UNAUTHORIZED_SUPV_SPECIFIC, params);
//			FacesContextUtil.getHttpSession().setAttribute(AlohaConstants.UNAUTHORIZED_MESSAGE, errMsg);
//			throw new AuthorizationException(errMsg);
//		}
		if ( ! this.otDetail.isAuthorized(this.userMB.getUser()) ) {
			Object[] params = { this.userMB.getFullName(), this.otDetail.getRequestId() };
			String errMsg = ErrorMessages.getInstance().getMessage(ErrorMessages.OT_CANCEL_UNAUTHORIZED_SUPV_SPECIFIC, params);
			FacesContextUtil.getHttpSession().setAttribute(AlohaConstants.UNAUTHORIZED_MESSAGE, errMsg);
			throw new AuthorizationException(errMsg);
		}		
		
		
	}			
	// METHOD REQUIRED BY ABSTRACT SUPERCLASS
	protected void checkIfLegalOperation() throws IllegalOperationException {
		if 	(  ! this.otDetail.isCancellableBySupervisor() ) {
			Object[] params = { this.otDetail.getRequestId(), this.otDetail.getStatus().getName() };
			String errMsg = ErrorMessages.getInstance().getMessage(ErrorMessages.OT_CANCEL_INVALID_STATUS_SUPV, params);
			FacesContextUtil.getHttpSession().setAttribute(AlohaConstants.ILLEGAL_OPERATION_MSG, errMsg);
			throw new IllegalOperationException(errMsg);
		}				
	}
	// METHOD REQUIRED BY ABSTRACT SUPERCLASS
	protected String getSupervisorRemarks() {
		return this.getCancellationRemarks();
	}		
	// METHOD REQUIRED BY ABSTRACT SUPERCLASS
	protected String getSubmitterRemarks() {
		return null;
	}
	// METHOD REQUIRED BY ABSTRACT SUPERCLASS
	protected String getSuccessPage()	{
		return NavigationOutcomes.OT_MGR_LIST;
	}
	// METHOD REQUIRED BY ABSTRACT SUPERCLASS
	protected String getStatusChangeOutcomeKey() {
		return AlohaConstants.OT_STATUS_CHANGE_OUTCOME_SUPV;
	}
}