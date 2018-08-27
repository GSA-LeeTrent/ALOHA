package gov.gsa.ocfo.aloha.web.mb.ot.group;

import gov.gsa.ocfo.aloha.ejb.overtime.OvertimeEJB;
import gov.gsa.ocfo.aloha.exception.AuthorizationException;
import gov.gsa.ocfo.aloha.exception.IllegalOperationException;
import gov.gsa.ocfo.aloha.model.entity.AlohaUser;
import gov.gsa.ocfo.aloha.model.entity.overtime.OTDetail;
import gov.gsa.ocfo.aloha.model.entity.overtime.OTDetailHistory;
import gov.gsa.ocfo.aloha.model.entity.overtime.OTGroup;
import gov.gsa.ocfo.aloha.model.entity.overtime.OTGroupHistory;
import gov.gsa.ocfo.aloha.model.entity.overtime.OTGroupStatus;
import gov.gsa.ocfo.aloha.model.entity.overtime.OTGroupStatusTrans;
import gov.gsa.ocfo.aloha.model.entity.overtime.OTHeader;
import gov.gsa.ocfo.aloha.model.entity.overtime.OTIndivStatus;
import gov.gsa.ocfo.aloha.model.entity.overtime.OTIndivStatusTrans;
import gov.gsa.ocfo.aloha.web.security.NavigationOutcomes;
import gov.gsa.ocfo.aloha.web.util.AlohaConstants;
import gov.gsa.ocfo.aloha.web.util.ErrorMessages;
import gov.gsa.ocfo.aloha.web.util.FacesContextUtil;
import gov.gsa.ocfo.aloha.web.util.NormalMessages;
import gov.gsa.ocfo.aloha.web.util.OTGroupStatusTransUtil;

import java.util.Date;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

@ManagedBean(name=OTGroupCancelMB.MANAGED_BEAN_NAME, eager=false)
@ViewScoped
public class OTGroupCancelMB extends OTGroupStatusChangeAbstractMB {
	
	// CLASS MEMBERS
	private static final long serialVersionUID = 4456942152905578333L;
	public static final String MANAGED_BEAN_NAME = "otGroupCancelMB";
	public static final String GROUP_SESSION_KEY = MANAGED_BEAN_NAME + "_" + serialVersionUID + "_OTGroup"; 
	
	
	// EJB INJECTION
	@EJB
	protected OvertimeEJB overtimeEJB;
	
	//INSTANCE MEMBERS
	private String groupCancellationRemarks;
	private OTGroupStatus cancelledGroupStatus;
	private OTIndivStatus cancelledIndivStatus;
	
	@PostConstruct
	public void init() {
		super.init();
	}

	// METHOD REQUIRED BY ABSTRACT SUPERCLASS
	protected void checkAccessRights() throws AuthorizationException {
		// CHECK ROLE
		if ( !this.userMB.isApprover() ) {
			Object[] params = { this.userMB.getFullName() };
			String errMsg = ErrorMessages.getInstance().getMessage(ErrorMessages.OT_GROUP_CANCEL_UNAUTHORIZED, params);
			FacesContextUtil.getHttpSession().setAttribute(AlohaConstants.UNAUTHORIZED_MESSAGE, errMsg);
			throw new AuthorizationException(errMsg);
		}

		// MUST BE THE SUBMITTER OR RECEIVER
		if ( ( this.getOtGroup().getSubmitter().getUserId() != this.userMB.getUser().getUserId() )
				&& ( this.getOtGroup().getReceiver().getUserId() != this.userMB.getUser().getUserId() )
				
			) {

			Object[] params = { this.userMB.getFullName(), this.getOtGroup().getId() };
			String errMsg = ErrorMessages.getInstance().getMessage(ErrorMessages.OT_GROUP_CANCEL_UNAUTHORIZED_SPECIFIC, params);
			FacesContextUtil.getHttpSession().setAttribute(AlohaConstants.UNAUTHORIZED_MESSAGE, errMsg);
			throw new AuthorizationException(errMsg);
		}
	}
	// METHOD REQUIRED BY ABSTRACT SUPERCLASS	
	protected void checkIfLegalOperation() throws IllegalOperationException {
		if 	( ! this.getOtGroup().isCancellable() ) {
			Object[] params = { this.getOtGroup().getId(), this.getOtGroup().getStatus().getName() };
			String errMsg = ErrorMessages.getInstance().getMessage(ErrorMessages.OT_GROUP_CANCEL_INVALID_STATUS, params);
			FacesContextUtil.getHttpSession().setAttribute(AlohaConstants.ILLEGAL_OPERATION_MSG, errMsg);
			throw new IllegalOperationException(errMsg);
		}	
	}
	// METHOD REQUIRED BY ABSTRACT SUPERCLASS
	protected String getNewStatusCode() {
		return OTGroupStatus.CodeValues.CANCELLED;
	}
	// METHOD REQUIRED BY ABSTRACT SUPERCLASS
	protected AlohaUser getSubmitter() {
		return this.userMB.getUser();
	}
	// METHOD REQUIRED BY ABSTRACT SUPERCLASS
	protected AlohaUser getReceiver() {
		return this.getOtGroup().getReceiver();
	}
	// METHOD REQUIRED BY ABSTRACT SUPERCLASS
	protected String getSubmitterRemarks() {
		return this.groupCancellationRemarks;
	}
	// METHOD REQUIRED BY ABSTRACT SUPERCLASS
	protected String getReceiverRemarks() {
		return null;
	}
	// METHOD REQUIRED BY ABSTRACT SUPERCLASS
	protected String createConfirmationMessage() {
		Object[] params = {Long.valueOf(this.getOtGroup().getId()), this.getOtGroup().getPayPeriod().getLabel()};
		return (NormalMessages.getInstance().getMessage(NormalMessages.OT_GROUP_MSG_STATUS_CHANGE_CANCELLED, params));
	}	
	// METHOD REQUIRED BY ABSTRACT SUPERCLASS
	protected String getStatusChangeOutcomeKey() {
		return AlohaConstants.OT_STATUS_CHANGE_OUTCOME_SUPV;
	}
	// METHOD REQUIRED BY ABSTRACT SUPERCLASS
	protected String getSuccessPage() {
		return NavigationOutcomes.OT_MGR_LIST;
	}	
	// METHOD REQUIRED BY ABSTRACT SUPERCLASS
	protected void processEmployees() {
		for (OTHeader employeeHeader : this.getOtGroup().getAllEmployees()) {
			this.processSingleEmployee(employeeHeader);
		}
	}	
	// METHOD REQUIRED BY ABSTRACT SUPERCLASS
	protected String getGroupSessionKey() {
		return GROUP_SESSION_KEY;
	}	
	// HELPER METHOD
	private void processSingleEmployee(OTHeader employee) {
		// GET LATEST EMPLOYEE DETAIL
		OTDetail latestEmployeeDetail = employee.getLatestDetail();
		
		if 	( ( !latestEmployeeDetail.isDenied() )
				&& ( !latestEmployeeDetail.isCancelled() )
			) {
			// PRIOR EMPLOYEE STATUS
			OTIndivStatus priorEmployeeStatus = latestEmployeeDetail.getStatus();
			
			// NEW EMPLOYEE STATUS
			latestEmployeeDetail.setStatus(this.cancelledIndivStatus);
			
			// EMPLOYEE STATUS TRANSITION (PRIOR TO NEW)
			String employeeStatusTransCode = this.otUtilMB.determineOTIndivStatusTransCode(priorEmployeeStatus.getCode(), OTGroupStatus.CodeValues.CANCELLED);
			OTIndivStatusTrans employeeStatusTrans = this.otUtilMB.getOTIndivStatusTrans(employeeStatusTransCode);
			
			// EMPLOYEE HISTORY
			OTDetailHistory employeeHistory = new OTDetailHistory();
			latestEmployeeDetail.addDetailHistory(employeeHistory);	
			employeeHistory.setDetail(latestEmployeeDetail);
			employeeHistory.setStatusTransition(employeeStatusTrans);
			employeeHistory.setActor(this.userMB.getUser());		
			employeeHistory.setActionDatetime(new Date());
		}
	}	
	// METHOD REQUIRED BY ABSTRACT SUPERCLASS
	protected void processChildGroups() {
		for (OTGroup childGroup : this.getOtGroup().getAllChildGroups()) {
			this.processSingleChildGroup(childGroup);
		}
	} 	
	// HELPER METHOD
	private void processSingleChildGroup(OTGroup childGroup) {
		// PRIOR CHILD GROUP STATUS
		OTGroupStatus priorChildGroupStatus = childGroup.getStatus();
		
		// NEW CHILD GROUP STATUS
		childGroup.setStatus(this.cancelledGroupStatus);
		
		// CHILD GROUP STATUS TRANSITION (PRIOR TO NEW)
		String statusTransCodeForChildGroup = OTGroupStatusTransUtil.determineOTGroupStatusTransCode(priorChildGroupStatus.getCode(), childGroup.getStatusCode());
		OTGroupStatusTrans statusTransForChildGroup = this.otUtilMB.getOTGroupStatusTrans(statusTransCodeForChildGroup);
		
		// GROUP_HISTORY
		OTGroupHistory childGroupHist = new OTGroupHistory();
		childGroup.addGroupHistory(childGroupHist);
		childGroupHist.setGroup(childGroup);
		childGroupHist.setStatusTransition(statusTransForChildGroup);
		childGroupHist.setHistorySequence(childGroup.getHistoricalEntries().size());
		childGroupHist.getCreatedBy().setUserCreated(this.userMB.getUserId());;
	
	}		
	// EVENT
	public String onGroupCancel() {
		this.cancelledGroupStatus = this.otUtilMB.getOTGroupStatus(OTGroupStatus.CodeValues.CANCELLED);
		this.cancelledIndivStatus = this.otUtilMB.getOTIndivStatus(OTIndivStatus.CodeValues.CANCELLED);
		return this.onStatusChange();
	}
	// GETTER
	public String getGroupCancellationRemarks() {
		return groupCancellationRemarks;
	}
	// SETTER
	public void setGroupCancellationRemarks(String groupCancellationRemarks) {
		this.groupCancellationRemarks = groupCancellationRemarks;
	}
}