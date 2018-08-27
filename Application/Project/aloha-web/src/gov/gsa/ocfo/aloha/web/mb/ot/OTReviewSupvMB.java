package gov.gsa.ocfo.aloha.web.mb.ot;

import gov.gsa.ocfo.aloha.exception.AuthorizationException;
import gov.gsa.ocfo.aloha.exception.IllegalOperationException;
import gov.gsa.ocfo.aloha.model.entity.overtime.OTIndivStatus;
import gov.gsa.ocfo.aloha.web.security.NavigationOutcomes;
import gov.gsa.ocfo.aloha.web.util.AlohaConstants;
import gov.gsa.ocfo.aloha.web.util.ErrorMessages;
import gov.gsa.ocfo.aloha.web.util.FacesContextUtil;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

@ManagedBean(name="otReviewSupvMB")
@ViewScoped
public class OTReviewSupvMB extends OTStatusChangeAbstractMB {
	private static final long serialVersionUID = 1460791458362902191L;

	// INSTANCE MEMEBER
	private String newStatusCode;
	// INSTANCE MEMEBER
	private String supervisorRemarks;
	
	@PostConstruct
	public void init() {
		super.init();
	}
	// METHOD REQUIRED BY ABSTRACT SUPERCLASS
	protected void checkAccessRights() throws AuthorizationException {
		// CHECK ROLE
		if ( ! this.userMB.getUser().isApprover()) {
			String errMsg = ErrorMessages.getInstance().getMessage(ErrorMessages.OT_SUPERVISOR_UNAUTHORIZED);
			FacesContextUtil.getHttpSession().setAttribute("unauthMsg", errMsg);
			throw new AuthorizationException(errMsg);
		}		
		// CHECK SUPERVISOR
		if ( ! this.otDetail.getSupervisor().equals(this.userMB.getUser()) ) {
				Object[] params = { this.otDetail.getEmployee().getFirstName() };
				String errMsg = ErrorMessages.getInstance().getMessage(ErrorMessages.OT_SUPERVISOR_UNAUTHORIZED_DETAIL, params);
				FacesContextUtil.getHttpSession().setAttribute("unauthMsg", errMsg);
				throw new AuthorizationException(errMsg);
		}
	}
	// METHOD REQUIRED BY ABSTRACT SUPERCLASS
	protected void checkIfLegalOperation() throws IllegalOperationException {
		if 	(  ( !this.otDetail.isReviewableBySupervisor() ) 
					&& (!this.otDetail.isApprovableBySupervisor()) ) {
			Object[] params = { this.otDetail.getRequestId(), this.otDetail.getStatus().getName() };
			String errMsg = ErrorMessages.getInstance().getMessage(ErrorMessages.OT_REVIEW_INVALID_STATUS_SUPV, params);
			FacesContextUtil.getHttpSession().setAttribute(AlohaConstants.ILLEGAL_OPERATION_MSG, errMsg);
			throw new IllegalOperationException(errMsg);
		}		
	}
	// METHOD REQUIRED BY ABSTRACT SUPERCLASS
	protected String getNewStatusCode() {
		return this.newStatusCode;
	}
	// METHOD REQUIRED BY ABSTRACT SUPERCLASS
	protected String getSubmitterRemarks() {
		return null;
	}
	// METHOD REQUIRED BY ABSTRACT SUPERCLASS
	protected String getSuccessPage() {
		//return NavigationOutcomes.OT_LIST_SUPV;
		return NavigationOutcomes.OT_MGR_LIST;
		
	}
	// METHOD REQUIRED BY ABSTRACT SUPERCLASS
	protected String getStatusChangeOutcomeKey() {
		return AlohaConstants.OT_STATUS_CHANGE_OUTCOME_SUPV;
	}	
	// EVENT
	public String onReceive() {
		this.newStatusCode = OTIndivStatus.CodeValues.RECEIVED;
		return super.onStatusChange();
	}		
	// EVENT
	public String onApprove() {
		this.newStatusCode = OTIndivStatus.CodeValues.APPROVED;
		return super.onStatusChange();	
	}		
	// EVENT
	public String onDeny() {
		this.newStatusCode = OTIndivStatus.CodeValues.DENIED;
		return super.onStatusChange();	
	}
	// GETTER
	public String getSupervisorRemarks() {
		return supervisorRemarks;
	}
	// SETTER
	public void setSupervisorRemarks(String supervisorRemarks) {
		this.supervisorRemarks = supervisorRemarks;
	}	
}