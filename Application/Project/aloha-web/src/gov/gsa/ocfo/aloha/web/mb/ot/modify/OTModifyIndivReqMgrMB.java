package gov.gsa.ocfo.aloha.web.mb.ot.modify;

import gov.gsa.ocfo.aloha.exception.AuthorizationException;
import gov.gsa.ocfo.aloha.exception.IllegalOperationException;
import gov.gsa.ocfo.aloha.model.entity.AlohaUser;
import gov.gsa.ocfo.aloha.model.entity.overtime.OTIndivStatus;
import gov.gsa.ocfo.aloha.web.security.NavigationOutcomes;
import gov.gsa.ocfo.aloha.web.util.AlohaConstants;
import gov.gsa.ocfo.aloha.web.util.ErrorMessages;
import gov.gsa.ocfo.aloha.web.util.FacesContextUtil;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

@ManagedBean(name=OTModifyIndivReqMgrMB.MANAGED_BEAN_NAME)
@ViewScoped
public class OTModifyIndivReqMgrMB extends OTModifyIndivReqAbstractMB {
	private static final long serialVersionUID = 1460791458362902191L;
	public static final String MANAGED_BEAN_NAME = "otModifyIndivReqMgrMB";
	
	@PostConstruct
	public void init() {
		super.init();
	}
	// METHOD REQUIRED BY ABSTRACT SUPERCLASS
	protected void checkAccessRights() throws AuthorizationException {
		// CHECK ROLE
		if ( ! this.userMB.getUser().isApprover()) {
			String errMsg = ErrorMessages.getInstance().getMessage(ErrorMessages.OT_MGR_MODIFY_UNAUTHORIZED);
			FacesContextUtil.getHttpSession().setAttribute("unauthMsg", errMsg);
			throw new AuthorizationException(errMsg);
		}		
//		// CHECK SUPERVISOR
//		if ( ! this.otDetail.getSupervisor().equals(this.userMB.getUser()) ) {
//				Object[] params = { this.otDetail.getEmployee().getFullName() };
//				String errMsg = ErrorMessages.getInstance().getMessage(ErrorMessages.OT_MGR_MODIFY_UNAUTHORIZED, params);
//				FacesContextUtil.getHttpSession().setAttribute("unauthMsg", errMsg);
//				throw new AuthorizationException(errMsg);
//		}

		// CHECK SUPERVISOR
		if ( ! this.otDetail.isAuthorized(this.userMB.getUser()) ) {
				Object[] params = { this.otDetail.getEmployee().getFullName() };
				String errMsg = ErrorMessages.getInstance().getMessage(ErrorMessages.OT_MGR_MODIFY_UNAUTHORIZED, params);
				FacesContextUtil.getHttpSession().setAttribute("unauthMsg", errMsg);
				throw new AuthorizationException(errMsg);
		}		
		
		
	}
	// METHOD REQUIRED BY ABSTRACT SUPERCLASS
	protected void checkIfLegalOperation() throws IllegalOperationException {
		if 	(  ! this.otDetail.isModifiableBySupervisor() ) {
			Object[] params = { this.otDetail.getRequestId(), this.otDetail.getStatus().getName() };
			String errMsg = ErrorMessages.getInstance().getMessage(ErrorMessages.OT_MGR_MODIFY_INVALID_STATUS, params);
			FacesContextUtil.getHttpSession().setAttribute(AlohaConstants.ILLEGAL_OPERATION_MSG, errMsg);
			throw new IllegalOperationException(errMsg);
		}	
		
		/************************************************************************************************************
		NOT SURE IF THIS IS NEEDED
		if 	(  ! this.otDetail.isFundingRequired() ) {
			Object[] params = { this.otDetail.getRequestId(), this.otDetail.getType().getName() };
			String errMsg = ErrorMessages.getInstance().getMessage(ErrorMessages.OT_MGR_MODIFY_INVALID_TYPE, params);
			FacesContextUtil.getHttpSession().setAttribute(AlohaConstants.ILLEGAL_OPERATION_MSG, errMsg);
			throw new IllegalOperationException(errMsg);
		}	
		************************************************************************************************************/
	}
	// METHOD REQUIRED BY ABSTRACT SUPERCLASS
	protected AlohaUser getSubmitter() {
		return this.otDetail.getSubmitter();
		
	}
	// METHOD REQUIRED BY ABSTRACT SUPERCLASS
	protected AlohaUser getSupervisor() {
		return this.userMB.getUser();
	}
	// METHOD REQUIRED BY ABSTRACT SUPERCLASS
	protected String getNewStatusCode() {
		return OTIndivStatus.CodeValues.MODIFIED;
	}
	// METHOD REQUIRED BY ABSTRACT SUPERCLASS
	protected String getSubmitterRemarks() {
		return null;
	}
	// METHOD REQUIRED BY ABSTRACT SUPERCLASS
	public String getSupervisorRemarks() {
		return this.modificationRemarks;
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
}