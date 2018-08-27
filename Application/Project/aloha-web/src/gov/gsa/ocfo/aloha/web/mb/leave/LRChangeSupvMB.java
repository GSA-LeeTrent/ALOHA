package gov.gsa.ocfo.aloha.web.mb.leave;

import gov.gsa.ocfo.aloha.ejb.leave.EmployeeEJB;
import gov.gsa.ocfo.aloha.ejb.leave.LeaveRequestEJB;
import gov.gsa.ocfo.aloha.exception.AlohaServerException;
import gov.gsa.ocfo.aloha.exception.AuthorizationException;
import gov.gsa.ocfo.aloha.exception.IllegalOperationException;
import gov.gsa.ocfo.aloha.model.KeyValuePair;
import gov.gsa.ocfo.aloha.model.entity.AlohaUser;
import gov.gsa.ocfo.aloha.model.entity.AuditTrail;
import gov.gsa.ocfo.aloha.model.entity.leave.LeaveDetail;
import gov.gsa.ocfo.aloha.model.entity.leave.LeaveHistory;
import gov.gsa.ocfo.aloha.model.entity.leave.LeaveStatusTransition;
import gov.gsa.ocfo.aloha.model.entity.leave.LeaveSupervisorChangeHistory;
import gov.gsa.ocfo.aloha.util.StopWatch;
import gov.gsa.ocfo.aloha.util.StringUtil;
import gov.gsa.ocfo.aloha.web.enums.LRMode;
import gov.gsa.ocfo.aloha.web.mb.UserMB;
import gov.gsa.ocfo.aloha.web.util.AlohaConstants;
import gov.gsa.ocfo.aloha.web.util.AlohaURIs;
import gov.gsa.ocfo.aloha.web.util.ErrorMessages;
import gov.gsa.ocfo.aloha.web.util.FacesContextUtil;
import gov.gsa.ocfo.aloha.web.util.NormalMessages;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;

@ManagedBean(name=LRChangeSupvMB.MANAGED_BEAN_NAME, eager=false)
@ViewScoped
public class LRChangeSupvMB implements Serializable {
	private static final long serialVersionUID = -307825242364700044L;
	public static final String MANAGED_BEAN_NAME = "lrChangeSupvMB";
	
	@EJB
	private LeaveRequestEJB leaveRequestEJB;
	
	@EJB
	private EmployeeEJB employeeEJB;
	
	@ManagedProperty(value="#{userMB}")
	private UserMB userMB;
	public void setUserMB(UserMB userMB) {
		this.userMB = userMB;
	}
	
	@ManagedProperty(value="#{leaveStatusMB}")
	private LeaveStatusMB leaveStatusMB;
	public void setLeaveStatusMB(LeaveStatusMB leaveStatusMB) {
		this.leaveStatusMB = leaveStatusMB;
	}

	// INSTANCE MEMBERS
	private LRMode lrMode;
	private String topPanelHeaderText;
	private LeaveDetail leaveDetail;
	private List<KeyValuePair> leaveBalances = new ArrayList<KeyValuePair>();	
	private String selectedSupervisor;
	private AlohaUser oldSupvervisor;
	private AlohaUser newSupervisor;
	private List<AlohaUser> dbApprovers;
	private Map<String, Object> supervisors = new TreeMap<String, Object>();
	private String changeSupervisorRemarks;
	private String confirmationMessage;

	private boolean pageOne;
	private boolean pageTwo;
	private boolean pageThree;
	
	@PreDestroy
	public void cleanup() {
		this.lrMode = null;
		this.topPanelHeaderText = null;
		this.leaveDetail = null;
		this.leaveBalances = null;
		this.selectedSupervisor = null;
		this.oldSupvervisor = null;
		this.newSupervisor = null;
		this.dbApprovers = null;
		this.supervisors = null;
		this.changeSupervisorRemarks = null;
		this.confirmationMessage = null;
		this.pageOne = false;
		this.pageTwo = false;
		this.pageThree = false;
	}
	
	@PostConstruct
	public void init() {
		try {
			// GET REQUEST PARAMETER
			String paramVal = FacesContextUtil.getHttpServletRequest().getParameter(AlohaConstants.LR_MODE);
			// IF THE REQUEST PARAMETER IS EMPTY, WE CAN'T CONTINUE
			if (StringUtil.isNullOrEmpty(paramVal)) {
				System.out.println("HTTP Request Parameter \"" + AlohaConstants.LR_MODE + "\" is NULL in call to " 
						+ this.getClass().getName() + ".init() method. Cannot continue.");
				throw new IllegalStateException();
			}
			
			// GET LEAVE REQUEST MODE
			this.lrMode = LRMode.fromString(paramVal);
			// IF LEAVE REQUEST MODE CANNOT BE FOUND, WE CAN'T CONTINUE
			if (this.lrMode == null ) {
				System.out.println("LRMode NOT FOUND for \"" + paramVal + "\" in call to " 
						+ this.getClass().getName() + ".init() method. Cannot continue.");
				throw new IllegalStateException();
			}
			
			// GET REQUEST PARAMETER
			String leaveDetailId = FacesContextUtil.getHttpServletRequest().getParameter(AlohaConstants.LEAVE_DETAIL_ID);		
			// IF THE REQUEST PARAMETER IS EMPTY, WE CAN'T CONTINUE
			if ( StringUtil.isNullOrEmpty(leaveDetailId)) {
				System.out.println("HTTP Parameter \"leaveDetailId\" is NULL in call to " 
						+ this.getClass().getName() + ".init() method. Cannot continue.");
				throw new IllegalStateException();
			}

			// RETRIEVE LEAVE DETAIL OBJECT
			this.leaveDetail = this.leaveRequestEJB.getLeaveDetail(Long.parseLong(leaveDetailId));		
			// IF THE LEAVE DETAIL OBJECT IS NULL, WE CAN'T CONTINUE
			if ( this.leaveDetail == null) {
				System.out.println("Retrieval of LeaveDetail object, using HTTP Parameter named \"leaveDetailId\", having a value of " 
									+ leaveDetailId + ", is NULL in call to " + this.getClass().getName() + ".init() method. Cannot continue.");
				throw new IllegalStateException();				
			}

			this.checkAccessRights();
			this.checkIfLegalOperation();
			this.oldSupvervisor = this.leaveDetail.getApprover();
			this.initPossibleSupervisors(this.leaveDetail.getEmployee().getUserId());
			this.leaveBalances = this.employeeEJB.getLeaveBalances(this.leaveDetail.getLeaveHeader().getEmployee().getUserId());
			Object[] params = { this.leaveDetail.getLeaveRequestId() };
			this.topPanelHeaderText = NormalMessages.getInstance().getMessage(NormalMessages.LR_NUMBER, params);
			
			this.pageOne = true;
			this.pageTwo = false;
			this.pageThree = false;
		} catch (NumberFormatException nfe) {
			try {
				nfe.printStackTrace();
				FacesContext.getCurrentInstance().getExternalContext().redirect(AlohaURIs.SERVER_ERROR);
			} catch (IOException ignore) {}
		} catch (AuthorizationException ae) {
			try {
				FacesContext.getCurrentInstance().getExternalContext().redirect(AlohaURIs.UNAUTHORIZED);
			} catch (IOException ignore) {}
		} catch (IllegalOperationException ise) {
			try {
				FacesContext.getCurrentInstance().getExternalContext().redirect(AlohaURIs.ILLEGAL_OPERATION);
			} catch (IOException ignore) {}
		} catch (IllegalStateException ise) {
			try {
				FacesContext.getCurrentInstance().getExternalContext().redirect(AlohaURIs.ILLEGAL_OPERATION);
			} catch (IOException ignore) {}
		}  
		catch (Throwable t) {
			t.printStackTrace();
			try {
				t.printStackTrace();
				FacesContext.getCurrentInstance().getExternalContext().redirect(AlohaURIs.SERVER_ERROR);
			} catch (IOException ignore) {}
		}
	}
	private void checkAccessRights() throws AuthorizationException {
		// CHECK ROLE
		if 	( ( !this.userMB.isSubmitOwn() ) 
				&& ( !this.userMB.isOnBehalfOf() ) 
			) {
			Object[] params = { this.userMB.getFullName(), Long.valueOf(this.leaveDetail.getLeaveHeaderId()) };
			String errMsg = ErrorMessages.getInstance().getMessage(ErrorMessages.LR_CHANGE_SUPV_UNAUTHORIZED, params);
			FacesContextUtil.getHttpSession().setAttribute(AlohaConstants.UNAUTHORIZED_MESSAGE, errMsg);
			throw new AuthorizationException(errMsg);
		}
	}

	private void checkIfLegalOperation() throws IllegalOperationException {
		if 	( ( !this.leaveDetail.isSubmitted() )
				&& ( !this.leaveDetail.isPendingAmendment() )
				&& ( !this.leaveDetail.isPendingWithdrawal() )
			) {
			Object[] params = { Long.valueOf(this.leaveDetail.getLeaveHeaderId()), this.leaveDetail.getLeaveStatus().getLabel() };
			String errMsg = ErrorMessages.getInstance().getMessage(ErrorMessages.LR_CHANGE_SUPV_INVALID_STATUS, params);
			FacesContextUtil.getHttpSession().setAttribute(AlohaConstants.ILLEGAL_OPERATION_MSG, errMsg);
			throw new IllegalOperationException(errMsg);			
		}
		
	}
	
	private void checkIfInLegalState() throws IllegalStateException {
		IllegalStateException illegalStateException = null;
		if ( this.leaveDetail == null) {
			illegalStateException = new IllegalStateException("LeaveDetail in " + this.getClass().getName() + ".checkIfInLegalState() is NULL. Cannot continue.");
			illegalStateException.printStackTrace();
			throw illegalStateException;
		}
		if ( this.newSupervisor == null) {
			illegalStateException = new IllegalStateException("New supervisor is NULL in " + this.getClass().getName() + ".checkIfInLegalState() for Leave Request # " 
																+ this.leaveDetail.getLeaveHeaderId() + " Cannot continue.");
			illegalStateException.printStackTrace();
			throw illegalStateException;
		}
		
	}

	private void initPossibleSupervisors(long employeeUserId) throws AlohaServerException {
			//this.selectedSupervisor= String.valueOf(employeeUserId);
		this.selectedSupervisor = null;
		this.dbApprovers = this.employeeEJB.getLeaveApprovers(employeeUserId);
			for (AlohaUser approver : dbApprovers) {
				this.supervisors.put(approver.getLabel(), approver.getValue());
			}
	}	

	private AlohaUser findApprover(long userId) {
		for ( AlohaUser approver: this.dbApprovers) {
			if ( approver.getUserId() == userId) {
				return approver;
			}
		}
		return null;
	}	
	public String getMode() {
		return this.lrMode.getText();
	}
	public String getTopPanelHeaderText() {
		return topPanelHeaderText;
	}
	public LeaveDetail getLeaveDetail() {
		return leaveDetail;
	}
	public List<KeyValuePair> getLeaveBalances() {
		return leaveBalances;
	}
	public String getSelectedSupervisor() {
		return selectedSupervisor;
	}
	public void setSelectedSupervisor(String selectedSupervisor) {
		this.selectedSupervisor = selectedSupervisor;
	}
	public Map<String, Object> getSupervisors() {
		return supervisors;
	}
	public String getChangeSupervisorRemarks() {
		return changeSupervisorRemarks;
	}
	public void setChangeSupervisorRemarks(String changeSupervisorRemarks) {
		this.changeSupervisorRemarks = changeSupervisorRemarks;
	}
	
	public AlohaUser getNewSupervisor() {
		return newSupervisor;
	}

	public String getConfirmationMessage() {
		return confirmationMessage;
	}

	public boolean isPageOne() {
		return pageOne;
	}
	public boolean isPageTwo() {
		return pageTwo;
	}
	public boolean isPageThree() {
		return pageThree;
	}	
	
	// EVENT
	public void onReviewChanges() {
		try {
			this.pageOne = false;
			this.pageTwo = true;
			this.pageThree = false;
			this.newSupervisor = this.findApprover(Long.valueOf(this.selectedSupervisor));
			this.validate();
		} catch(ValidatorException ve) {
			this.pageOne = true;
			this.pageTwo = false;
			this.pageThree = false;
		}
	}	
	// EVENT
	public void onReviseChanges() {
		this.pageOne = true;
		this.pageTwo = false;
		this.pageThree = false;
	}	
	// EVENT
	public void onConfirmChanges() {
		try {
			this.checkIfInLegalState();
			this.checkAccessRights();
			this.checkIfLegalOperation();
		
			// AUDIT TRAIL
			AuditTrail auditTrail = new AuditTrail();
			auditTrail.setUserLastUpdated(this.userMB.getLoginName());
			auditTrail.setDateLastUpdated(new Date());
			this.leaveDetail.setAuditTrail(auditTrail);
			
			// CHANGE SUPERVISOR IN LEAVE DETAIL
			this.leaveDetail.setApprover(this.newSupervisor);
			
			// CHANGE SUPVERVISOR HISTORY
			LeaveSupervisorChangeHistory supervisorChangeHistory = new LeaveSupervisorChangeHistory();
			supervisorChangeHistory.setPreviousSupervisorUserId(this.oldSupvervisor.getUserId());
			supervisorChangeHistory.setNewSupervisorUserId(this.newSupervisor.getUserId());
			supervisorChangeHistory.setUserCreatedId(this.userMB.getUserId());
			supervisorChangeHistory.setDateCreated(new Date());
			
			// LEAVE HISTORY
			LeaveHistory history = new LeaveHistory();
			history.setAuditTrail(auditTrail);
			history.setLeaveDetail(this.leaveDetail);
			history.setLeaveStatusTransition(this.determineLeaveStatusTransition());
			history.setActionDatetime(new Date());
			history.setActor(this.userMB.getUser());
			//history.setSupervisorChangeHistory(supervisorChangeHistory);
			
			if ( !StringUtil.isNullOrEmpty(this.changeSupervisorRemarks) ) {
				history.setRemarks(this.changeSupervisorRemarks);
			}
			
			this.leaveDetail.addLeaveHistory(history);

			StopWatch stopWatch = new StopWatch();
			stopWatch.start();
			this.leaveDetail = this.leaveRequestEJB.updateLeaveDetail(this.leaveDetail, supervisorChangeHistory, this.oldSupvervisor);
			stopWatch.stop();
			System.out.println("ELAPSED TIME (Change Supervisor on Leave Request): " + stopWatch.getElapsedTime() + " ms");			
			stopWatch = null;
			
			this.createConfirmationMessage();
			
			// PAGE NAVIGATION
			this.pageOne = false;
			this.pageTwo = false;
			this.pageThree = true;
		}  catch (IllegalStateException ise) {
			try {
				FacesContext.getCurrentInstance().getExternalContext().redirect(AlohaURIs.SERVER_ERROR);
			} catch (IOException ignore) {}
		} catch (AuthorizationException ae) {
			try {
				FacesContext.getCurrentInstance().getExternalContext().redirect(AlohaURIs.UNAUTHORIZED);
			} catch (IOException ignore) {}
		} catch (IllegalOperationException ise) {
			try {
				FacesContext.getCurrentInstance().getExternalContext().redirect(AlohaURIs.ILLEGAL_OPERATION);
			} catch (IOException ignore) {}
		} catch (AlohaServerException ase) {
			if ( ase.getExceptionType() == AlohaServerException.ExceptionType.OPTIMISTIC_LOCK) {
				Object[] errorParams = {Long.valueOf(this.leaveDetail.getLeaveHeaderId()).toString()};
				String errMsgText = ErrorMessages.getInstance().getMessage(ErrorMessages.LR_CHANGE_SUPV_OPT_LOCK_EXCEPTION, errorParams);
				FacesContextUtil.getHttpSession().setAttribute("userErrorMessage", errMsgText);
				try {
					FacesContext.getCurrentInstance().getExternalContext().redirect(AlohaURIs.USER_ERROR);
				} catch (IOException ignore) {}			
			} else {
				try {
					FacesContext.getCurrentInstance().getExternalContext().redirect(AlohaURIs.SERVER_ERROR);
				} catch (IOException ignore) {}
			}	
		} catch (Throwable t) {
			t.printStackTrace();
			try {
				FacesContext.getCurrentInstance().getExternalContext().redirect(AlohaURIs.SERVER_ERROR);
			} catch (IOException ignore) {}
		}		
	}	
	
	private LeaveStatusTransition determineLeaveStatusTransition() throws IllegalOperationException, AlohaServerException  {
		if 	( this.leaveDetail.isSubmitted() ) {
			return (this.leaveStatusMB.getLeaveStatusTransition
						(LeaveStatusTransition.ActionCodeValues.SUBMIT_TO_SUBMIT));
		} else if (this.leaveDetail.isPendingAmendment()) {
			return (this.leaveStatusMB.getLeaveStatusTransition
					(LeaveStatusTransition.ActionCodeValues.PEND_AMENED_TO_PEND_AMEND));
		} else if (this.leaveDetail.isPendingWithdrawal()) {
			return (this.leaveStatusMB.getLeaveStatusTransition
					(LeaveStatusTransition.ActionCodeValues.PEND_WITHDRAW_TO_PEND_WITHDRAW));
		} else {
			Object[] params = { Long.valueOf(this.leaveDetail.getLeaveHeaderId()), this.leaveDetail.getLeaveStatus().getLabel() };
			String errMsg = ErrorMessages.getInstance().getMessage(ErrorMessages.LR_CHANGE_SUPV_INVALID_STATUS, params);
			FacesContextUtil.getHttpSession().setAttribute(AlohaConstants.ILLEGAL_OPERATION_MSG, errMsg);
			throw new IllegalOperationException(errMsg);	
		}
	}
	
	protected void createConfirmationMessage() {
		Object[] params = {this.leaveDetail.getLeaveRequestId(), this.oldSupvervisor.getFullName(), this.newSupervisor.getFullName()};
		this.confirmationMessage = NormalMessages.getInstance().getMessage(NormalMessages.LR_CHANGE_SUPV_CONFIRM_MSG, params);
	}	
	
	private void validate() throws ValidatorException {
		// APPROVER
		if ( this.newSupervisor == null) {
			String errMsgText = ErrorMessages.getInstance().getMessage(ErrorMessages.LR_CHANGE_SUPV_NEW_SUPV_NOT_SELECTED);
			FacesMessage facesMsg = new FacesMessage(errMsgText, errMsgText);
			facesMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
			FacesContext.getCurrentInstance().addMessage(null, facesMsg);
			throw new ValidatorException(new FacesMessage());
		}
		if ( this.newSupervisor.equals(this.oldSupvervisor)) {
			Object[] params = { this.newSupervisor.getFullName(), this.oldSupvervisor.getFullName() };
			String errMsgText = ErrorMessages.getInstance().getMessage(ErrorMessages.LR_CHANGE_SUPV_NO_CHANGES_MADE, params);
			FacesMessage facesMsg = new FacesMessage(errMsgText, errMsgText);
			facesMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
			FacesContext.getCurrentInstance().addMessage(null, facesMsg);
			throw new ValidatorException(new FacesMessage());
		}		
	}
	public boolean isInSubmitOwnMode() {
		return ( (this.lrMode != null) && (this.lrMode.equals(LRMode.SUBMIT_OWN)) && (this.userMB.isSubmitOwn()) );
	}
	public boolean isInOnBehalfOfMode() {
		return ( (this.lrMode != null) && (this.lrMode.equals(LRMode.ON_BEHALF_OF)) && (this.userMB.isOnBehalfOf()) );
	}
}