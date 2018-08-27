package gov.gsa.ocfo.aloha.web.mb.leave;

import gov.gsa.ocfo.aloha.exception.AlohaServerException;
import gov.gsa.ocfo.aloha.exception.AuthorizationException;
import gov.gsa.ocfo.aloha.model.entity.AlohaUser;
import gov.gsa.ocfo.aloha.model.entity.AuditTrail;
import gov.gsa.ocfo.aloha.model.entity.leave.LeaveDetail;
import gov.gsa.ocfo.aloha.model.entity.leave.LeaveHistory;
import gov.gsa.ocfo.aloha.model.entity.leave.LeaveStatus;
import gov.gsa.ocfo.aloha.model.entity.leave.LeaveStatusTransition;
import gov.gsa.ocfo.aloha.util.StopWatch;
import gov.gsa.ocfo.aloha.util.StringUtil;
import gov.gsa.ocfo.aloha.web.mb.UserMB;
import gov.gsa.ocfo.aloha.web.security.NavigationOutcomes;
import gov.gsa.ocfo.aloha.web.util.AlohaConstants;
import gov.gsa.ocfo.aloha.web.util.ErrorMessages;
import gov.gsa.ocfo.aloha.web.util.FacesContextUtil;

import java.util.Date;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;

@ManagedBean(name="lrWithdrawMB")
@SessionScoped
public class LRWithdrawMB extends LRAbstractMB {
	private static final long serialVersionUID = 7026161041549864746L;

	@ManagedProperty(value="#{userMB}")
	protected UserMB userMB;	
	
	private LeaveDetail leaveDetail;
	private String withdrawalRemarks;
	
	// METHOD REQUIRED BY ABSTRACT SUPERCLASS	
	protected AlohaUser getEmployee() {
		return (this.leaveDetail.getEmployee());
	}
	// METHOD REQUIRED BY ABSTRACT SUPERCLASS
	protected AlohaUser getSubmitter() {
		return (this.leaveDetail.getSubmitter());
	}
	// METHOD REQUIRED ABSTRACT SUPERCLASS
	protected AlohaUser getApprover() {
		return (this.leaveDetail.getApprover());
	}	
	@PostConstruct
	public String initWithdrawal() {
		try {
			super.init();
			this.initState();
			this.checkAccessRights();
			return NavigationOutcomes.LR_WITHDRAW;
		} catch (AuthorizationException ae) {
			return NavigationOutcomes.UNAUTHORIZED;		
		} catch (NumberFormatException nfe) {
			return NavigationOutcomes.SERVER_ERROR;
		} catch (AlohaServerException e) {
			return NavigationOutcomes.SERVER_ERROR;		
		}	
	}	
	private void initState() throws NumberFormatException, AlohaServerException{
		String leaveDetailId = FacesContextUtil.getHttpServletRequest().getParameter("leaveDetailId");
		this.leaveDetail = this.leaveRequestEJB.getLeaveDetail(Long.valueOf(leaveDetailId));
		this.initLeaveBalances();
		this.withdrawalRemarks = null;
	}

	private void clearState() {
		this.lrMode = null;
		this.leaveDetail = null;
		this.leaveBalances = null;
		this.withdrawalRemarks = null;
	}
	
	private void checkAccessRights() throws AuthorizationException {
		if ( (!this.isSubmittedByEmployee()) && (!this.isSubmittedByOnBehalfOf()) ) {
			Object[] params = { this.userMB.getFullName(), Long.valueOf(this.leaveDetail.getLeaveHeaderId()) };
			String errMsg = ErrorMessages.getInstance().getMessage(ErrorMessages.LR_WITHDRAW_UNAUTHORIZED, params);
			FacesContextUtil.getHttpSession().setAttribute("unauthMsg", errMsg);
			throw new AuthorizationException(errMsg);
		}
		if ( (this.isSubmittedByEmployee()) && (this.userMB.getUserId() != this.leaveDetail.getSubmitter().getUserId()) ) {
			Object[] params = { this.userMB.getFullName(), Long.valueOf(this.leaveDetail.getLeaveHeaderId()) };
			String errMsg = ErrorMessages.getInstance().getMessage(ErrorMessages.LR_WITHDRAW_UNAUTHORIZED, params);
			FacesContextUtil.getHttpSession().setAttribute("unauthMsg", errMsg);
			throw new AuthorizationException(errMsg);
		}
		if ( (this.isSubmittedByOnBehalfOf()) && (this.userMB.getUserId() != this.leaveDetail.getSubmitter().getUserId()) ) {
			Object[] params = { this.userMB.getFullName(), Long.valueOf(this.leaveDetail.getLeaveHeaderId()) };
			String errMsg = ErrorMessages.getInstance().getMessage(ErrorMessages.LR_WITHDRAW_UNAUTHORIZED, params);
			FacesContextUtil.getHttpSession().setAttribute("unauthMsg", errMsg);
			throw new AuthorizationException(errMsg);
		}

	}	
	private boolean isSubmittedByEmployee() {
		return	( (this.userMB.isSubmitOwn()) 
					&& (this.userMB.getUserId() == this.leaveDetail.getEmployee().getUserId())
					&& (this.userMB.getUserId() == this.leaveDetail.getSubmitter().getUserId())
				);
	}
	private boolean isSubmittedByOnBehalfOf() {
		return	( (this.userMB.isOnBehalfOf()) 
					&& (this.userMB.getUserId() != this.leaveDetail.getEmployee().getUserId())
					&& (this.userMB.getUserId() == this.leaveDetail.getSubmitter().getUserId())
				);
	}	
	public void setUserMB(UserMB userMB) {
		this.userMB = userMB;
	}	

	public String onWithdrawRequest() {
		return NavigationOutcomes.LR_WITHDRAW_PENDING;
	}
	public String onChange() {
		try {
			this.checkAccessRights();
			return NavigationOutcomes.LR_WITHDRAW;
		} catch (AuthorizationException ae) {
			return NavigationOutcomes.UNAUTHORIZED;
		} catch (IllegalStateException ise) {
			return NavigationOutcomes.USER_ERROR;
		} catch (Exception e) {
			e.printStackTrace();
			return NavigationOutcomes.SERVER_ERROR;
		}		
	}		
	public String onConfirm() {
		try{
			this.checkAccessRights();
			this.doLeaveWithdrawal();
			FacesContextUtil.getHttpSession().setAttribute(AlohaConstants.NEWLY_WITHDRAWN_LEAVE_DETAIL, this.leaveDetail);
			FacesContextUtil.getHttpSession().setAttribute(AlohaConstants.LR_MODE_ON_LAST_WITHDRAW, this.getMode());
			this.clearState();
			return NavigationOutcomes.LR_WITHDRAW_CONFIRM;
		} catch (AuthorizationException ae) {
			return NavigationOutcomes.UNAUTHORIZED;
		}  catch (IllegalStateException ise) {
			return NavigationOutcomes.USER_ERROR;
		}  catch (AlohaServerException ase) {
			if ( ase.getExceptionType() == AlohaServerException.ExceptionType.OPTIMISTIC_LOCK) {
				Object[] errorParams = {Long.valueOf(this.leaveDetail.getLeaveHeaderId()).toString()};
				String errMsgText = ErrorMessages.getInstance().getMessage(ErrorMessages.LR_CANCEL_OPT_LOCK_EXCEPTION, errorParams);
				FacesContextUtil.getHttpSession().setAttribute("userErrorMessage", errMsgText);
				return NavigationOutcomes.USER_ERROR;				
			} else {
				return NavigationOutcomes.SERVER_ERROR;	
			}	
		} catch (Exception e) {
			e.printStackTrace();
			return NavigationOutcomes.SERVER_ERROR;
		}					
		
		

	}
	private void doLeaveWithdrawal() throws AlohaServerException {

		LeaveStatusTransition leaveStatusTransition = this.leaveStatusMB.getLeaveStatusTransition
														(LeaveStatusTransition.ActionCodeValues.APPROVED_TO_PEND_WITHDRAW);
		LeaveStatus leaveStatus = this.leaveStatusMB.getLeaveStatus(LeaveStatus.CodeValues.PEND_WITHDRAW);
		
		// AUDIT TRAIL
		AuditTrail auditTrail = new AuditTrail();
		auditTrail.setUserLastUpdated(this.userMB.getLoginName());
		auditTrail.setDateLastUpdated(new Date());

		this.leaveDetail.setAuditTrail(auditTrail);

		// LEAVE STATUS
		this.leaveDetail.setLeaveStatus(leaveStatus);
		
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
		
		-- Lee Trent (09/23/2013)
	    ********************************************************************************************/		
		
		/*******************************************************************************
		// WITHDRAWAL REMARKS
		if ( !StringUtil.isNullOrEmpty(this.withdrawalRemarks) ) {
			
			LeaveSubmitterComment lsComment = new LeaveSubmitterComment();
			lsComment.setLeaveDetail(leaveDetail);
			lsComment.setComment(this.withdrawalRemarks);
			lsComment.setSequence(this.leaveDetail.getSubmitterComments().size() + 1 );
			lsComment.setAuditTrail(auditTrail);
			this.leaveDetail.addSubmitterComment(lsComment);
		}
		********************************************************************************/
		
		// LEAVE HISTORY (INCLUDES REMARKS)
		LeaveHistory history = new LeaveHistory();
		history.setAuditTrail(auditTrail);
		history.setLeaveDetail(this.leaveDetail);
		history.setLeaveStatusTransition(leaveStatusTransition);
		history.setActionDatetime(new Date());
		history.setActor(this.userMB.getUser());
		if ( !StringUtil.isNullOrEmpty(this.withdrawalRemarks) ) {
			history.setRemarks(this.withdrawalRemarks);
		}		
		this.leaveDetail.addLeaveHistory(history);
		
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		this.leaveRequestEJB.updateLeaveDetail(this.leaveDetail);
		stopWatch.stop();
		System.out.println("ELAPSED TIME (Withdraw Leave Request): " + stopWatch.getElapsedTime() + " ms");
		stopWatch = null;		
	}	
	
	public LeaveDetail getLeaveDetail() {
		if ( this.leaveDetail == null) {
			FacesContextUtil.callHome();
		}		
		return leaveDetail;
	}
	public String getWithdrawalRemarks() {
		return withdrawalRemarks;
	}
	public void setWithdrawalRemarks(String withdrawalRemarks) {
		this.withdrawalRemarks = withdrawalRemarks;
	}
}