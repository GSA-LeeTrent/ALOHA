package gov.gsa.ocfo.aloha.web.mb.ot.group;

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
import gov.gsa.ocfo.aloha.model.overtime.group.OTRequestTypeGroup;
import gov.gsa.ocfo.aloha.model.overtime.group.OTSalaryGradeGroup;
import gov.gsa.ocfo.aloha.web.security.NavigationOutcomes;
import gov.gsa.ocfo.aloha.web.util.AlohaConstants;
import gov.gsa.ocfo.aloha.web.util.ErrorMessages;
import gov.gsa.ocfo.aloha.web.util.FacesContextUtil;
import gov.gsa.ocfo.aloha.web.util.NormalMessages;
import gov.gsa.ocfo.aloha.web.util.OTGroupStatusTransUtil;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.annotation.PostConstruct;
import javax.faces.bean.CustomScoped;
import javax.faces.bean.ManagedBean;

@ManagedBean(name=OTGroupFinalizeMB.MANAGED_BEAN_NAME, eager=false)
@CustomScoped(value = "#{window}")
public class OTGroupFinalizeMB extends OTGroupStatusChangeAbstractMB {
	
	// CLASS MEMBERS
	private static final long serialVersionUID = -7214409260536544575L;
	public static final String MANAGED_BEAN_NAME = "otGroupFinalizeMB";
	public static final String GROUP_SESSION_KEY = MANAGED_BEAN_NAME + "_" + serialVersionUID + "_OTGroup"; 

	//INSTANCE MEMBERS
	private String groupFinalizerRemarks;
	private boolean reviewChanges;
	private OTIndivStatus approvedIndivStatus;
	private OTIndivStatus deniedIndivStatus;
	private OTGroupStatus finalGroupStatus;
	private List<OTSalaryGradeGroup> salaryGradeGroupList = null;
	private List<OTRequestTypeGroup> requestTypeGroupList = null;
	
	@PostConstruct
	public void init() {
		super.init();
		this.reviewChanges = false;
		this.salaryGradeGroupList = this.getOtGroup().getSalaryGradeGroupList();
		this.requestTypeGroupList = this.getOtGroup().getRequestTypeGroupList();
	}
	
	// METHOD REQUIRED BY ABSTRACT SUPERCLASS
	protected void checkAccessRights() throws AuthorizationException {
		// CHECK ROLE
		if ( !this.userMB.isApprover() ) {
			Object[] params = { this.userMB.getFullName() };
			String errMsg = ErrorMessages.getInstance().getMessage(ErrorMessages.OT_GROUP_FINALIZE_UNAUTHORIZED, params);
			FacesContextUtil.getHttpSession().setAttribute(AlohaConstants.UNAUTHORIZED_MESSAGE, errMsg);
			throw new AuthorizationException(errMsg);
		}
		// CHECK SUPERVISOR
		if 	( (this.getOtGroup().getSubmitter().getUserId() != this.userMB.getUser().getUserId())
				&& (this.getOtGroup().getReceiver().getUserId() != this.userMB.getUser().getUserId())
			) {
			Object[] params = { this.userMB.getFullName(), this.getOtGroup().getId() };
			String errMsg = ErrorMessages.getInstance().getMessage(ErrorMessages.OT_GROUP_FINALIZE_UNAUTHORIZED_SPECIFIC, params);
			FacesContextUtil.getHttpSession().setAttribute(AlohaConstants.UNAUTHORIZED_MESSAGE, errMsg);
			throw new AuthorizationException(errMsg);
		}
		
	}
	// METHOD REQUIRED BY ABSTRACT SUPERCLASS
	protected void checkIfLegalOperation() throws IllegalOperationException {
		if 	( ! this.getOtGroup().isFinalizable() ) {
			Object[] params = { this.getOtGroup().getId(), this.getOtGroup().getStatus().getName() };
			String errMsg = ErrorMessages.getInstance().getMessage(ErrorMessages.OT_GROUP_FINALIZE_INVALID_STATUS, params);
			FacesContextUtil.getHttpSession().setAttribute(AlohaConstants.ILLEGAL_OPERATION_MSG, errMsg);
			throw new IllegalOperationException(errMsg);
		}	
	}
	// METHOD REQUIRED BY ABSTRACT SUPERCLASS
	protected String getNewStatusCode() {
		return OTGroupStatus.CodeValues.FINAL;
	}
	// METHOD REQUIRED BY ABSTRACT SUPERCLASS
	protected AlohaUser getSubmitter() {
		return this.getOtGroup().getSubmitter();
	}
	// METHOD REQUIRED BY ABSTRACT SUPERCLASS
	protected AlohaUser getReceiver() {
		return this.userMB.getUser();
	}
	// METHOD REQUIRED BY ABSTRACT SUPERCLASS
	protected String getSubmitterRemarks() {
		return null;
	}
	// METHOD REQUIRED BY ABSTRACT SUPERCLASS
	protected String getReceiverRemarks() {
		return this.groupFinalizerRemarks;
	}
	// METHOD REQUIRED BY ABSTRACT SUPERCLASS
	protected String getStatusChangeOutcomeKey() {
		return AlohaConstants.OT_STATUS_CHANGE_OUTCOME_SUPV;
	}
	// METHOD REQUIRED BY ABSTRACT SUPERCLASS
	protected String createConfirmationMessage() {
		Object[] params = {Long.valueOf(this.getOtGroup().getId()), this.getOtGroup().getPayPeriod().getShortLabel()};
		return (NormalMessages.getInstance().getMessage(NormalMessages.OT_GROUP_MSG_STATUS_CHANGE_FINALIZED, params));
	}	
	// METHOD REQUIRED BY ABSTRACT SUPERCLASS
	protected String getSuccessPage() {
		return NavigationOutcomes.OT_MGR_LIST;
	}	
	// METHOD REQUIRED BY ABSTRACT SUPERCLASS
	protected void processEmployees() {		
		List<OTHeader> allEmployees = this.getOtGroup().getAllEmployees();
		for ( OTHeader employee : allEmployees ) {
			this.processSingleEmployee(employee);
		}
	}		
	// METHOD REQUIRED BY ABSTRACT SUPERCLASS
	protected void processChildGroups() {
		for (OTGroup childGroup : this.getOtGroup().getAllChildGroups()) {
			this.processSingleChildGroup(childGroup);
		}
	} 	
	// METHOD REQUIRED BY ABSTRACT SUPERCLASS
	protected String getGroupSessionKey() {
		return GROUP_SESSION_KEY;
	}
	
	// HELPER METHOD
	private void processSingleChildGroup(OTGroup childGroup) {
		// PRIOR CHILD GROUP STATUS
		OTGroupStatus priorChildGroupStatus = childGroup.getStatus();
		
		// NEW CHILD GROUP STATUS
		childGroup.setStatus(this.finalGroupStatus);
		
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
	public String onGroupFinalize() {
		this.finalGroupStatus = this.otUtilMB.getOTGroupStatus(OTGroupStatus.CodeValues.FINAL);
		return this.onStatusChange();
	}	
	// EVENT
	public void onGoBackAndMakeChanges() {
		this.reviewChanges = false;
	}
	// EVENT
	public void onReviewChanges() {
		this.reviewChanges = true;
		this.approvedIndivStatus = this.otUtilMB.getOTIndivStatus(OTIndivStatus.CodeValues.APPROVED);
		this.deniedIndivStatus = this.otUtilMB.getOTIndivStatus(OTIndivStatus.CodeValues.DENIED);
		
		List<OTHeader> approvableRequestList = this.getOtGroup().getApprovableRequests();
		for ( OTHeader otHeader : approvableRequestList ) {
			if ( otHeader.isApprovalComanded()) {
				otHeader.setApprovalCommandStatusChangeOutcome(this.approvedIndivStatus.getCode());
				
			} else {
				otHeader.setApprovalCommandStatusChangeOutcome(this.deniedIndivStatus.getCode());
			}
		}
		this.salaryGradeGroupList = new ArrayList<OTSalaryGradeGroup>(this.createSalaryGradeGroupMapForNewlyApprovedRequests().values());
		this.requestTypeGroupList = this.createRequestTypeGroupListForNewlyApprovedAndDeniedRequests();
	}
	
	private Map<String, OTSalaryGradeGroup> createSalaryGradeGroupMapForNewlyApprovedRequests() {
		Map<String, OTSalaryGradeGroup> salaryGradeMap = new TreeMap<String, OTSalaryGradeGroup>();
		
		for (OTHeader otHeader : this.getOtGroup().getApprovableRequests() ) {
			if ( otHeader.isApprovalComanded() ) {
				String salaryGradeKey = otHeader.getSalaryGrade().getKey();
				OTSalaryGradeGroup salaryGradeGroup = salaryGradeMap.get(salaryGradeKey);
				if ( salaryGradeGroup == null ) {
					salaryGradeGroup = new OTSalaryGradeGroup(salaryGradeKey);
					salaryGradeMap.put(salaryGradeKey, salaryGradeGroup);
				}
				salaryGradeGroup.incrementRequestCount();
				salaryGradeGroup.addToEstNbrOfHours(otHeader.getEstNbrOfHours());
			}
		}
		return salaryGradeMap;
	}	
	private List<OTRequestTypeGroup> createRequestTypeGroupListForNewlyApprovedAndDeniedRequests() {
		List< OTRequestTypeGroup> requestTypeGroupList = new ArrayList<OTRequestTypeGroup>();

		OTRequestTypeGroup approvedGroup	= new OTRequestTypeGroup(OTIndivStatus.NameValues.APPROVED, this.getApprovedRequestCount(), this.getApprovedNbrOfHours());
		OTRequestTypeGroup deniedGroup 		= new OTRequestTypeGroup(OTIndivStatus.NameValues.DENIED, this.getDeniedRequestCount(), this.getDeniedNbrOfHours());
		
		int totalCount = approvedGroup.getCount() + deniedGroup.getCount();
		BigDecimal totalHours = approvedGroup.getNbrOfHours().add(deniedGroup.getNbrOfHours());
		OTRequestTypeGroup allGroups  		= new OTRequestTypeGroup("Total", totalCount, totalHours);
		
		requestTypeGroupList.add(approvedGroup);
		requestTypeGroupList.add(deniedGroup);
		requestTypeGroupList.add(allGroups);
		
		return requestTypeGroupList;
	}		
	
	private int getApprovedRequestCount() {
		int approvedCount = 0;
		for (OTHeader otHeader : this.getOtGroup().getApprovableRequests() ) {
			if ( otHeader.isApprovalComanded() == true ) {
				approvedCount++;
			}
		}
		return approvedCount;
	}

	private int getDeniedRequestCount() {
		int deniedCount = 0;
		for (OTHeader otHeader : this.getOtGroup().getApprovableRequests() ) {
			if ( otHeader.isApprovalComanded() == false) {
				deniedCount++;
			}
		}
		return deniedCount;
	}
	public BigDecimal getApprovedNbrOfHours() {
		BigDecimal approvedNbrOfHours = BigDecimal.ZERO;
		for (OTHeader otHeader : this.getOtGroup().getApprovableRequests() ) {
			if ( otHeader.isApprovalComanded() == true ) {
				approvedNbrOfHours = approvedNbrOfHours.add(otHeader.getEstNbrOfHours());
			}
		}				
		return approvedNbrOfHours;
	}
	public BigDecimal getDeniedNbrOfHours() {
		BigDecimal deniedNbrOfHours = BigDecimal.ZERO;
		for (OTHeader otHeader : this.getOtGroup().getApprovableRequests() ) {
			if ( otHeader.isApprovalComanded() == false ) {
				deniedNbrOfHours = deniedNbrOfHours.add(otHeader.getEstNbrOfHours());
			}
		}				
		return deniedNbrOfHours;
	}	
	
	
	// HELPER METHOD
	private void processSingleEmployee(OTHeader employee) {
		// GET LATEST EMPLOYEE DETAIL
		OTDetail latestEmployeeDetail = employee.getLatestDetail();
		
		if ( ! latestEmployeeDetail.isCancelled()) {
			// PRIOR EMPLOYEE STATUS
			OTIndivStatus priorEmployeeStatus = latestEmployeeDetail.getStatus();

			// NEW EMPLOYEE STATUS
			if ( employee.isApprovalComanded()) {
				latestEmployeeDetail.setStatus(this.approvedIndivStatus);
			} else {
				latestEmployeeDetail.setStatus(this.deniedIndivStatus);
			}
			
			// EMPLOYEE STATUS TRANSITION (PRIOR TO NEW)
			String employeeStatusTransCode = this.otUtilMB.determineOTIndivStatusTransCode(priorEmployeeStatus.getCode(), latestEmployeeDetail.getStatus().getCode());
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
	
	// GETTTER
	public boolean isReviewChanges() {
		return reviewChanges;
	}
	// GETTTER
	public String getGroupFinalizerRemarks() {
		return groupFinalizerRemarks;
	}
	public List<OTSalaryGradeGroup> getSalaryGradeGroupList() {
		return salaryGradeGroupList;
	}
	public List<OTRequestTypeGroup> getRequestTypeGroupList() {
		return requestTypeGroupList;
	}
	// SETTER
	public void setGroupFinalizerRemarks(String groupFinalizerRemarks) {
		this.groupFinalizerRemarks = groupFinalizerRemarks;
	}
}