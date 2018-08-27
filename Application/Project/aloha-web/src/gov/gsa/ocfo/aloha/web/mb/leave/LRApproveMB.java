package gov.gsa.ocfo.aloha.web.mb.leave;

import gov.gsa.ocfo.aloha.ejb.leave.EmployeeEJB;
import gov.gsa.ocfo.aloha.ejb.leave.LeaveRequestEJB;
import gov.gsa.ocfo.aloha.exception.AlohaServerException;
import gov.gsa.ocfo.aloha.exception.AuthorizationException;
import gov.gsa.ocfo.aloha.model.KeyValuePair;
import gov.gsa.ocfo.aloha.model.entity.AlohaUser;
import gov.gsa.ocfo.aloha.model.entity.AuditTrail;
import gov.gsa.ocfo.aloha.model.entity.leave.LeaveDetail;
import gov.gsa.ocfo.aloha.model.entity.leave.LeaveHeader;
import gov.gsa.ocfo.aloha.model.entity.leave.LeaveHistory;
import gov.gsa.ocfo.aloha.model.entity.leave.LeaveItem;
import gov.gsa.ocfo.aloha.model.entity.leave.LeaveStatus;
import gov.gsa.ocfo.aloha.model.entity.leave.LeaveStatusTransition;
import gov.gsa.ocfo.aloha.util.StopWatch;
import gov.gsa.ocfo.aloha.util.StringUtil;
import gov.gsa.ocfo.aloha.web.mb.PayPeriodMB;
import gov.gsa.ocfo.aloha.web.mb.UserMB;
import gov.gsa.ocfo.aloha.web.model.LRSideBySideItem;
import gov.gsa.ocfo.aloha.web.model.leave.LRSupvReviewOutcome;
import gov.gsa.ocfo.aloha.web.security.NavigationOutcomes;
import gov.gsa.ocfo.aloha.web.util.DateUtil;
import gov.gsa.ocfo.aloha.web.util.ErrorMessages;
import gov.gsa.ocfo.aloha.web.util.FacesContextUtil;
import gov.gsa.ocfo.aloha.web.util.NormalMessages;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;

@ManagedBean(name="lrApproveMB")
@SessionScoped
public class LRApproveMB implements Serializable {

	private static final long serialVersionUID = -7017975010045446693L;
	
	@EJB
	private LeaveRequestEJB leaveRequestEJB;
	
	@EJB
	private EmployeeEJB employeeEJB;
	
	@ManagedProperty(value="#{userMB}")
	private UserMB userMB;

	@ManagedProperty(value="#{leaveStatusMB}")
	private LeaveStatusMB leaveStatusMB;

	@ManagedProperty(value="#{payPeriodMB}")
	protected PayPeriodMB payPeriodMB;
	
	private LeaveDetail submittedLeaveDetail;
	private LeaveDetail approvedLeaveDetail;
	private String selectedPayPeriod;
	private String approverRemarks;
	private List<KeyValuePair> leaveBalances = new ArrayList<KeyValuePair>();
	private LRSupvReviewOutcome supervisorReviewOutcome;
	
	private boolean validationErrors;
	
	private List<LRSideBySideItem> sideBySideItems = new ArrayList<LRSideBySideItem>();
	
	private void clearState() {
		this.submittedLeaveDetail = null;
		this.approvedLeaveDetail = null;
		this.selectedPayPeriod = null;
		this.approverRemarks = null;
		this.leaveBalances.clear();
		this.supervisorReviewOutcome = null;
		this.validationErrors = false;
	}

	@PostConstruct
	public String initApproval() {
		try {
			String leaveDetailId = FacesContextUtil.getHttpServletRequest().getParameter("leaveDetailId");
			if (StringUtil.isNullOrEmpty(leaveDetailId)) {
				FacesContextUtil.callHome();
				return null;
			} 
			this.doSecurityCheck();
			this.clearState();
			this.submittedLeaveDetail = this.leaveRequestEJB.getLeaveDetail(Long.parseLong(leaveDetailId));

			if ( this.submittedLeaveDetail == null ) {
				
				System.out.println("********************ERROR********************");
				System.out.println("Submitted Leave Detail not found for Leave Detail ID " + leaveDetailId  + " in LRApproveMB.initApproval(). Cannot continue.");
				System.out.println("User Login: " + this.userMB.getUser().getLoginName());
				System.out.println("Leave Detail ID: " + leaveDetailId);
				System.out.println("*********************************************");				
				return NavigationOutcomes.SERVER_ERROR;
			}
			
			this.supervisorReviewOutcome = new LRSupvReviewOutcome(this.submittedLeaveDetail);
			
			if	( (this.submittedLeaveDetail.isPendingAmendment())	) {
				this.approvedLeaveDetail = this.submittedLeaveDetail.getLeaveHeader().getLastestApprovedDetail();
				
				// ADDED NULL CHECKS - LTT (2012.05.15)
				if ( this.approvedLeaveDetail == null ) {
					System.out.println(" ");
					System.out.println("****************************ERROR***************************************");					
					System.out.println("Approved Leave Detail not found for Leave Request ID " + this.submittedLeaveDetail.getLeaveHeader().getId() 
							+ " in LRApproveMB.initApproval(). Cannot continue with Pending Amendment Approval Process.");
					System.out.println("User Login: " + this.userMB.getUser().getLoginName());
					System.out.println("Leave Request ID: " + this.submittedLeaveDetail.getLeaveHeader().getId());
					System.out.println("Submitted Leave Detail ID : " + this.submittedLeaveDetail.getId());

					if (this.submittedLeaveDetail != null) {
						LeaveHeader leaveHeader = this.submittedLeaveDetail.getLeaveHeader();
						if (leaveHeader != null) {
							System.out.println(" ");
							System.out.println("ADDITIONAL DEBUG INFORMATION:");
							System.out.println(" ");
							System.out.println("Number of Leave Details in Leave Header: " + leaveHeader.getLeaveDetails().size());
							System.out.println(" ");
							System.out.println("Leave Details for Leave Header ID " + leaveHeader.getId() + ":");
							for ( LeaveDetail leaveDetail : leaveHeader.getLeaveDetails()) {
								System.out.println("Leave Detail ID / Status: " + leaveDetail.getId() + " / " + leaveDetail.getLeaveStatus().getCode());
							}
							System.out.println(" ");
						}
					}
					System.out.println("************************************************************************");
					System.out.println(" ");
					
					return NavigationOutcomes.SERVER_ERROR;
				}
				this.initSideBySideItems();
			}
			this.doInitStateCheck();
			this.initLeaveBalances();

			if	( (this.submittedLeaveDetail.isPendingAmendment())	) {
				return NavigationOutcomes.LR_APPROVE_AMEND;
			} else {
				return NavigationOutcomes.LR_APPROVE;				
			}
		} catch (NumberFormatException nfe) {
			nfe.printStackTrace();
			return NavigationOutcomes.SERVER_ERROR;
		} catch (AuthorizationException ae) {
			return NavigationOutcomes.UNAUTHORIZED;
		} catch (AlohaServerException ase) {
			return NavigationOutcomes.SERVER_ERROR;
		} catch (IllegalStateException ase) {
			return NavigationOutcomes.SERVER_ERROR;
		} catch (NullPointerException npe) {
			npe.printStackTrace();
			System.out.println("userMB: " + this.userMB);
			if ( this.userMB != null) {
				AlohaUser alohaUser = this.userMB.getUser();
				System.out.println("userMB.getUser(): " + alohaUser);	
				if ( alohaUser != null) {
					System.out.println("alohaUser: " + alohaUser);
				}
			} 
			return null;
		}
	}
	
	private void initSideBySideItems() {
		this.sideBySideItems.clear();

		GregorianCalendar ppStartCal = new GregorianCalendar();
		ppStartCal.setTime(this.approvedLeaveDetail.getPayPeriodStartDate());
		
		for ( int ii = 0; ii < 14; ii++ ) {
			LRSideBySideItem sideBySideItem = this.createSideBySideItem(ppStartCal.getTime());
			if ( sideBySideItem.isPopulated()) {
				this.sideBySideItems.add(sideBySideItem);					
			}
			ppStartCal.add(Calendar.DAY_OF_MONTH, 1);
		}
	}	
	
	private LRSideBySideItem createSideBySideItem(Date date) {
		
		LRSideBySideItem sideBySideItem = new LRSideBySideItem();
		sideBySideItem.setDate(date);
		String sideBySideDateString = DateUtil.formatDate(sideBySideItem.getDate(), DateUtil.DateFormats.YYYYMMDD);

		// LOAD APPROVED LEAVE ITEMS
		for ( LeaveItem approvedLeaveItem : this.approvedLeaveDetail.getLeaveItems()) {
			// GET DATE IN THE RIGHT FORMAT FOR COMPARISON PURPOSES (STRIP-OFF ANY TIME VALUE)
			String approvedLeaveDateString = DateUtil.formatDate(approvedLeaveItem.getLeaveDate(), DateUtil.DateFormats.YYYYMMDD);
			if (approvedLeaveDateString.equals(sideBySideDateString) ) {	
				sideBySideItem.addApprovedLeaveItem(approvedLeaveItem);
			}
		}
		
		// LOAD PEND AMEND LEAVE ITEMS
		for ( LeaveItem pendAmendLeaveItem : this.submittedLeaveDetail.getLeaveItems()) {
			// GET DATE IN THE RIGHT FORMAT FOR COMPARISON PURPOSES (STRIP-OFF ANY TIME VALUE)
			String pendAmendLeaveDateString = DateUtil.formatDate(pendAmendLeaveItem.getLeaveDate(), DateUtil.DateFormats.YYYYMMDD);
			if (pendAmendLeaveDateString.equals(sideBySideDateString) ) {	
				sideBySideItem.addPendAmendLeaveItem(pendAmendLeaveItem);
			}
		}		
		return sideBySideItem;
	}
	
	private void initLeaveBalances() throws AlohaServerException {
		this.leaveBalances = this.employeeEJB.getLeaveBalances(this.submittedLeaveDetail.getLeaveHeader().getEmployee().getUserId());
	}

	public String onApprove() {
		try {
			this.doSecurityCheck();		
			this.doStateCheck();
			this.getSupervisorReviewOutcome().setApproved(true);
			this.validationErrors = false;
			return NavigationOutcomes.LR_APPROVE_PENDING;
		} catch (AuthorizationException ae) {
			return NavigationOutcomes.UNAUTHORIZED;
		}  catch (IllegalStateException ise) {
			return NavigationOutcomes.USER_ERROR;
		} catch (Exception e) {
			e.printStackTrace();
			return NavigationOutcomes.SERVER_ERROR;
		}	
	}
	public String onDeny() {
		try {
			this.doSecurityCheck();		
			this.doStateCheck();
			this.doDenialValidation();
			this.getSupervisorReviewOutcome().setApproved(false);
			return NavigationOutcomes.LR_APPROVE_PENDING;
		} catch (AuthorizationException ae) {
			return NavigationOutcomes.UNAUTHORIZED;
		}  catch (IllegalStateException ise) {
			return NavigationOutcomes.USER_ERROR;
		} catch (ValidatorException ve) {
			if	( (this.submittedLeaveDetail.isPendingAmendment())	) {
				return NavigationOutcomes.LR_APPROVE_AMEND;
			} else {
				return NavigationOutcomes.LR_APPROVE;				
			}					
		} catch (Exception e) {
			e.printStackTrace();
			return NavigationOutcomes.SERVER_ERROR;
		}	
	}
	private void doDenialValidation() throws ValidatorException {
		if ( StringUtil.isNullOrEmpty(this.approverRemarks)) {
			this.validationErrors = true;
			String errMsgText = ErrorMessages.getInstance().getMessage(ErrorMessages.LR_DENIAL_NO_REMARKS);
			FacesMessage facesMsg = new FacesMessage(errMsgText, errMsgText);
			facesMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
			FacesContext.getCurrentInstance().addMessage(null, facesMsg);
			throw new ValidatorException(facesMsg);
		}
	}
	public String onConfirm() {
		try{
			//System.out.println("LRApproveMB.onConfirm() : BEGIN");
			
			this.doSecurityCheck();		
			this.doStateCheck();
			
			StopWatch stopWatch = new StopWatch();
			if ( this.isApproved() ) {
				if ( this.isPendingAmendment()) {
					stopWatch.start();
					this.doLeaveAmendmentApproval();
					stopWatch.stop();
					System.out.println("ELAPSED TIME (Approve Amended Leave Request): " + stopWatch.getElapsedTime() + " ms");
				} else {
					stopWatch.start();
					this.doLeaveApproval();
					stopWatch.stop();
					System.out.println("ELAPSED TIME (Approve Leave Request): " + stopWatch.getElapsedTime() + " ms");					
				}
			} else {
				stopWatch.start();
				this.doLeaveDenial();
				stopWatch.stop();
				System.out.println("ELAPSED TIME (Deny Leave Request): " + stopWatch.getElapsedTime() + " ms");	
			}
			stopWatch = null;
			
			this.validationErrors = false;
			this.getSupervisorReviewOutcome().setApproverRemarks(this.approverRemarks);
			
			//HttpSession httpSession = FacesContextUtil.getHttpSession();
			//System.out.println("httpSession:" + httpSession);
			FacesContextUtil.getHttpSession().setAttribute("lrSupvReviewOutcome", this.getSupervisorReviewOutcome());
			this.clearState();
			
			//System.out.println("LRApproveMB.onConfirm() : END");
			return NavigationOutcomes.LR_APPROVE_CONFIRM;
		} catch (AuthorizationException ae) {
			return NavigationOutcomes.UNAUTHORIZED;
		}  catch (IllegalStateException ise) {
			return NavigationOutcomes.USER_ERROR;
		}  catch (AlohaServerException ase) {
			if ( ase.getExceptionType() == AlohaServerException.ExceptionType.OPTIMISTIC_LOCK) {
				Object[] errorParams = {Long.valueOf(this.submittedLeaveDetail.getLeaveHeaderId()).toString()};
				String errMsgText = ErrorMessages.getInstance().getMessage(ErrorMessages.LR_APPROVE_OPT_LOCK_EXCEPTION, errorParams);
				FacesContextUtil.getHttpSession().setAttribute("userErrorMessage", errMsgText);
				return NavigationOutcomes.USER_ERROR;				
			} else {
				return NavigationOutcomes.SERVER_ERROR;	
			}	
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("userMB: " + this.userMB);
			if ( this.userMB != null) {
				AlohaUser alohaUser = this.userMB.getUser();
				System.out.println("userMB.getUser(): " + alohaUser);	
				if ( alohaUser != null) {
					System.out.println("alohaUser: " + alohaUser);
				}
			} 
			return NavigationOutcomes.SERVER_ERROR;
		}			
	}
	private void doLeaveAmendmentApproval() throws AlohaServerException {
		// AUDIT TRAIL
		AuditTrail auditTrail = new AuditTrail();
		auditTrail.setUserLastUpdated(this.userMB.getLoginName());
		auditTrail.setDateLastUpdated(new Date());

		this.submittedLeaveDetail.setAuditTrail(auditTrail);
		this.approvedLeaveDetail.setAuditTrail(auditTrail);

		// LEAVE STATUS
		this.submittedLeaveDetail.setLeaveStatus(this.leaveStatusMB.getLeaveStatus(LeaveStatus.CodeValues.APPROVED));
		this.approvedLeaveDetail.setLeaveStatus(this.leaveStatusMB.getLeaveStatus(LeaveStatus.CodeValues.AMENDED));
		
	   /********************************************************************************************
	  	************************************IMPORTANT***********************************************
	  	********************************************************************************************
		Starting with the 0.7.5 release, ALOHA will NO LONGER write to the following tables:
		1) LR_APPROVER_COMMENT
		2) LR_SUBMITTER_COMMENT
		
		Instead, all remarks will be inserted into the LR_HISTORY table, using the REMARKS column.
		
		ALOHA will continue to read from these tables for backwards compatibility purposes. To display
		all previous remarks, ALOHA will read from the following tables:
		1) LR_APPROVER_COMMENT
		2) LR_SUBMITTER_COMMENT
		3) LR_HISTORY
		
		To see how this is being done in the Java ALOHA application, look at the "getAllRemarks()"  
		method in gov.gsa.ocfo.aloha.model.entity.leave.LeaveHeader.
		
		-- Lee Trent (09/24/2013)
	    ********************************************************************************************/		
		/********************************************************************************************
		// APPROVER COMMENTS (on latest leave detail to which the approval will be applied)
		if ( !StringUtil.isNullOrEmpty(this.approverRemarks) ) {
			
			LeaveApproverComment laComment = new LeaveApproverComment();
			laComment.setLeaveDetail(submittedLeaveDetail);
			laComment.setComment(this.approverRemarks);
			laComment.setSequence(this.submittedLeaveDetail.getApproverComments().size() + 1 );
			laComment.setAuditTrail(auditTrail);
			this.submittedLeaveDetail.addApproverComment(laComment);
		}		
		********************************************************************************************/	
		
		// LEAVE HISTORY - PEND_AMEND_TO_APPROVED (NEW LEAVE REQUEST)
		LeaveHistory pendAmendToApprovedHistory = new LeaveHistory();
		pendAmendToApprovedHistory.setAuditTrail(auditTrail);
		pendAmendToApprovedHistory.setLeaveDetail(this.submittedLeaveDetail);
		pendAmendToApprovedHistory.setLeaveStatusTransition
			(this.leaveStatusMB.getLeaveStatusTransition(LeaveStatusTransition.ActionCodeValues.PEND_AMEND_TO_APPROVED));
		pendAmendToApprovedHistory.setActionDatetime(new Date());
		pendAmendToApprovedHistory.setActor(this.userMB.getUser());
		if ( !StringUtil.isNullOrEmpty(this.approverRemarks) ) {
			pendAmendToApprovedHistory.setRemarks(this.approverRemarks);
		}
		this.submittedLeaveDetail.addLeaveHistory(pendAmendToApprovedHistory);
			
		// LEAVE HISTORY - PEND_ANEND_TO_AMENDED (OLD LEAVE REQUEST)
		LeaveHistory pendAmendToAmendedHistory = new LeaveHistory();
		pendAmendToAmendedHistory.setAuditTrail(auditTrail);
		pendAmendToAmendedHistory.setLeaveDetail(this.approvedLeaveDetail);
		pendAmendToAmendedHistory.setLeaveStatusTransition
			(this.leaveStatusMB.getLeaveStatusTransition(LeaveStatusTransition.ActionCodeValues.PEND_ANEND_TO_AMENDED));
		pendAmendToAmendedHistory.setActionDatetime(new Date());
		pendAmendToAmendedHistory.setActor(this.userMB.getUser());
		this.approvedLeaveDetail.addLeaveHistory(pendAmendToAmendedHistory);
		
		this.leaveRequestEJB.updateLeaveHeader(this.submittedLeaveDetail.getLeaveHeader());
	}
	
	private void doLeaveApproval() throws AlohaServerException {
		LeaveStatus leaveStatus = null;
		LeaveStatusTransition leaveStatusTransition = null;

		if ( this.submittedLeaveDetail.isSubmitted() ) {
			leaveStatus = this.leaveStatusMB.getLeaveStatus(LeaveStatus.CodeValues.APPROVED);
			leaveStatusTransition = this.leaveStatusMB.getLeaveStatusTransition(LeaveStatusTransition.ActionCodeValues.SUBMIT_TO_APPROVED);
		} else if ( this.submittedLeaveDetail.isPendingWithdrawal()) {
			leaveStatus = this.leaveStatusMB.getLeaveStatus(LeaveStatus.CodeValues.WITHDRAWN);
			leaveStatusTransition = this.leaveStatusMB.getLeaveStatusTransition(LeaveStatusTransition.ActionCodeValues.PEND_WITHDRAW_TO_WITHDRAWN);
		}	

		this.doUpdate(leaveStatus, leaveStatusTransition);
	}
	
	private void doLeaveDenial() throws AlohaServerException {
		LeaveStatus leaveStatus = null;
		LeaveStatusTransition leaveStatusTransition = null;

		if ( this.submittedLeaveDetail.isSubmitted() ) {
			leaveStatus = this.leaveStatusMB.getLeaveStatus(LeaveStatus.CodeValues.DENIED);
			leaveStatusTransition = this.leaveStatusMB.getLeaveStatusTransition(LeaveStatusTransition.ActionCodeValues.SUBMIT_TO_DENIED);
		} else if ( this.submittedLeaveDetail.isPendingAmendment()) {
			leaveStatus = this.leaveStatusMB.getLeaveStatus(LeaveStatus.CodeValues.DENIED);
			leaveStatusTransition = this.leaveStatusMB.getLeaveStatusTransition(LeaveStatusTransition.ActionCodeValues.PEND_AMEND_TO_DENIED);
		} else if ( this.submittedLeaveDetail.isPendingWithdrawal()) {
			// THIS IS NOT A MISTAKE - Lee (9/14/11)
			leaveStatus = this.leaveStatusMB.getLeaveStatus(LeaveStatus.CodeValues.APPROVED);
			// THIS IS NOT A MISTAKE - Lee (9/14/11)
			leaveStatusTransition = this.leaveStatusMB.getLeaveStatusTransition(LeaveStatusTransition.ActionCodeValues.PEND_WITHDRAW_TO_APPROVED);
		}	
		
		this.doUpdate(leaveStatus, leaveStatusTransition);
	}
	
	private void doUpdate(LeaveStatus leaveStatus, LeaveStatusTransition leaveStatusTransition) throws AlohaServerException {
		// AUDIT TRAIL
		AuditTrail auditTrail = new AuditTrail();
		auditTrail.setUserLastUpdated(this.userMB.getLoginName());
		auditTrail.setDateLastUpdated(new Date());

		this.submittedLeaveDetail.setAuditTrail(auditTrail);

		// LEAVE STATUS
		this.submittedLeaveDetail.setLeaveStatus(leaveStatus);
				
	   /********************************************************************************************
	  	************************************IMPORTANT***********************************************
	  	********************************************************************************************
		Starting with the 0.7.5 release, ALOHA will NO LONGER write to the following tables:
		1) LR_APPROVER_COMMENT
		2) LR_SUBMITTER_COMMENT
		
		Instead, all remarks will be inserted into the LR_HISTORY table, using the REMARKS column.
		
		ALOHA will continue to read from these tables for backwards compatibility purposes. To display
		all previous remarks, ALOHA will read from the following tables:
		1) LR_APPROVER_COMMENT
		2) LR_SUBMITTER_COMMENT
		3) LR_HISTORY
		
		To see how this is being done in the Java ALOHA application, look at the "getAllRemarks()"  
		method in gov.gsa.ocfo.aloha.model.entity.leave.LeaveHeader.
		
		-- Lee Trent (09/24/2013)
	    ********************************************************************************************/		
		/********************************************************************************************
		// APPROVER COMMENTS
		if ( !StringUtil.isNullOrEmpty(this.approverRemarks) ) {
			
			LeaveApproverComment laComment = new LeaveApproverComment();
			laComment.setLeaveDetail(submittedLeaveDetail);
			laComment.setComment(this.approverRemarks);
			laComment.setSequence(this.submittedLeaveDetail.getApproverComments().size() + 1 );
			laComment.setAuditTrail(auditTrail);
			this.submittedLeaveDetail.addApproverComment(laComment);
		}		
		********************************************************************************************/		
		
		// LEAVE HISTORY
		LeaveHistory history = new LeaveHistory();
		history.setAuditTrail(auditTrail);
		history.setLeaveDetail(this.submittedLeaveDetail);
		history.setLeaveStatusTransition(leaveStatusTransition);
		history.setActionDatetime(new Date());
		history.setActor(this.userMB.getUser());
		if ( !StringUtil.isNullOrEmpty(this.approverRemarks) ) {
			history.setRemarks(this.approverRemarks);
		}
		
		this.submittedLeaveDetail.addLeaveHistory(history);
			
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();		
		this.submittedLeaveDetail = this.leaveRequestEJB.updateLeaveDetail(this.submittedLeaveDetail);
		stopWatch.stop();
		System.out.println("ELAPSED TIME (Create Leave Request): " + stopWatch.getElapsedTime() + " ms");		
	}
	
	public String onChange() {
		try {
			this.doSecurityCheck();
			this.doStateCheck();	
			this.validationErrors = false;
		} catch (AuthorizationException e) {
			return NavigationOutcomes.UNAUTHORIZED;
		} catch (IllegalStateException ise) {
			return NavigationOutcomes.USER_ERROR;
		}		
		if	( (this.submittedLeaveDetail.isPendingAmendment())	) {
			return NavigationOutcomes.LR_APPROVE_AMEND;
		} else {
			return NavigationOutcomes.LR_APPROVE;				
		}		
	}
	public String onCancel() {
		this.clearState();	
		return NavigationOutcomes.LR_LIST;
	}
	public String onReturnToList() {
		this.clearState();
		return NavigationOutcomes.LR_LIST;
	}
	public String onHome() {
		this.clearState();
		return NavigationOutcomes.HOME_PAGE;
	}	
	private void doSecurityCheck() throws AuthorizationException {
		if ( ! this.userMB.isApprover() ) {
			Object[] params = { userMB.getFullName()  };
			String errMsg = ErrorMessages.getInstance().getMessage(ErrorMessages.APPROVER_UNAUTHORIZED, params);
			FacesContextUtil.getHttpSession().setAttribute("unauthMsg", errMsg);
			throw new AuthorizationException(errMsg);
		}
	}
	private void doInitStateCheck() {
		if ( (!this.submittedLeaveDetail.getLeaveStatus().getCode().equals(LeaveStatus.CodeValues.SUBMITTED))
				&& (!this.submittedLeaveDetail.getLeaveStatus().getCode().equals(LeaveStatus.CodeValues.PEND_AMEND))
				&& (!this.submittedLeaveDetail.getLeaveStatus().getCode().equals(LeaveStatus.CodeValues.PEND_WITHDRAW))
			) {
			Object[] errorParams = new Object[4];
			errorParams[0] = LeaveStatus.LabelValues.SUBMITTED;
			errorParams[1] = LeaveStatus.LabelValues.PEND_WITHDRAW;
			errorParams[2] = Long.valueOf(this.submittedLeaveDetail.getLeaveHeaderId()).toString();
			errorParams[3] = this.submittedLeaveDetail.getLeaveStatus().getLabel();

			String errMsgText = ErrorMessages.getInstance().getMessage(ErrorMessages.LR_APPROVE_INVALID_STATUS, errorParams);
			FacesContextUtil.getHttpSession().setAttribute("userErrorMessage", errMsgText);
			throw new IllegalStateException(errMsgText);
		}

	}
	private void doStateCheck() {
		if	( (!this.submittedLeaveDetail.getLeaveStatus().getCode().equals(LeaveStatus.CodeValues.SUBMITTED))
				&&  (!this.submittedLeaveDetail.getLeaveStatus().getCode().equals(LeaveStatus.CodeValues.PEND_AMEND))
				&&  (!this.submittedLeaveDetail.getLeaveStatus().getCode().equals(LeaveStatus.CodeValues.PEND_WITHDRAW))
			) {
			Object[] errorParams = new Object[2];
			errorParams[0] = String.valueOf(this.submittedLeaveDetail.getLeaveHeaderId());
			if ( this.submittedLeaveDetail.getLeaveStatus().getCode().equals(LeaveStatus.CodeValues.APPROVED)) {
				errorParams[1] = LeaveStatus.LabelValues.APPROVED;
			}		
			if ( this.submittedLeaveDetail.getLeaveStatus().getCode().equals(LeaveStatus.CodeValues.DENIED)) {
				errorParams[1] = LeaveStatus.LabelValues.DENIED;
			}		
			String errMsgText = ErrorMessages.getInstance().getMessage(ErrorMessages.LR_ALREADY_APPROVED_DENIED, errorParams);
			FacesContextUtil.getHttpSession().setAttribute("userErrorMessage", errMsgText);
			throw new IllegalStateException(errMsgText);
		}
	}
	
	public void setLeaveStatusMB(LeaveStatusMB leaveStatusMB) {
		this.leaveStatusMB = leaveStatusMB;
	}
	
	public void setUserMB(UserMB userMB) {
		this.userMB = userMB;
	}
	public LeaveDetail getSubmittedLeaveDetail() {
		if ( this.submittedLeaveDetail == null) {
			FacesContextUtil.callHome();
		}
		return submittedLeaveDetail;
	}

	public String getApproverRemarks() {
		return approverRemarks;
	}

	public void setApproverRemarks(String approverRemarks) {
		this.approverRemarks = approverRemarks;
	}
	public List<KeyValuePair> getLeaveBalances() {
		return leaveBalances;
	}
	public int getLeaveBalanceCount() {
		return ( this.leaveBalances.size());
	}
	public boolean isApproved() {
		if ( this.supervisorReviewOutcome == null) {
			FacesContextUtil.callHome();
			return false;
		} else {
			return this.supervisorReviewOutcome.isApproved();			
		}
 	}
	public String getLeaveStatusLabel() {
		return ( (this.isApproved()) ? NormalMessages.getInstance().getMessage(NormalMessages.PENDING_LEAVE_APPROVAL) 
				: NormalMessages.getInstance().getMessage(NormalMessages.PENDING_LEAVE_DENIAL));
	}
	public LeaveDetail getApprovedLeaveDetail() {
		return approvedLeaveDetail;
	}
	public boolean isPendingAmendment() {
		return (this.submittedLeaveDetail != null ) && (this.submittedLeaveDetail.isPendingAmendment());
	}
	public boolean isPendingWithdrawal() {
		return (this.submittedLeaveDetail != null ) && (this.submittedLeaveDetail.isPendingWithdrawal());
	}
	public boolean isNew() {
		return (this.submittedLeaveDetail != null ) && (this.submittedLeaveDetail.isSubmitted());
	}
	public void setPayPeriodMB(PayPeriodMB payPeriodMB) {
		this.payPeriodMB = payPeriodMB;
	}
	public String getFormattedSelectedPayPeriod() {
		if ( this.selectedPayPeriod != null && this.selectedPayPeriod.trim().length() == 8) {
			return this.selectedPayPeriod.substring(0,4) + "-" + this.selectedPayPeriod.substring(4,6) + "-" + this.selectedPayPeriod.substring(6);
		} else {
			return this.selectedPayPeriod;
		}
	}
	public boolean isAmendmentRequest() {
		return this.getSupervisorReviewOutcome().isAmendmentRequest();
	}

	public boolean isWithdrawalRequest() {
		return this.getSupervisorReviewOutcome().isWithdrawalRequest();
	}

	public boolean isNewRequest() {
		return this.getSupervisorReviewOutcome().isNewRequest();
	}

	public boolean isValidationErrors() {
		return validationErrors;
	}

	public List<LRSideBySideItem> getSideBySideItems() {
		return sideBySideItems;
	}

	public LRSupvReviewOutcome getSupervisorReviewOutcome() {
		if ( this.supervisorReviewOutcome == null) {
			FacesContextUtil.callHome();
		}
		return supervisorReviewOutcome;
	}
}