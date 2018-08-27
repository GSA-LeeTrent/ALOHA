package gov.gsa.ocfo.aloha.web.mb.leave;

import gov.gsa.ocfo.aloha.exception.AlohaServerException;
import gov.gsa.ocfo.aloha.exception.AuthorizationException;
import gov.gsa.ocfo.aloha.model.KeyValuePair;
import gov.gsa.ocfo.aloha.model.PayPeriod;
import gov.gsa.ocfo.aloha.model.ScheduleItem;
import gov.gsa.ocfo.aloha.model.entity.AlohaUser;
import gov.gsa.ocfo.aloha.model.entity.leave.LeaveDetail;
import gov.gsa.ocfo.aloha.model.entity.leave.LeaveHeader;
import gov.gsa.ocfo.aloha.model.entity.leave.LeaveItem;
import gov.gsa.ocfo.aloha.model.entity.leave.LeaveStatus;
import gov.gsa.ocfo.aloha.model.entity.leave.LeaveStatusTransition;
import gov.gsa.ocfo.aloha.util.StopWatch;
import gov.gsa.ocfo.aloha.web.mb.UserMB;
import gov.gsa.ocfo.aloha.web.model.LRSideBySideItem;
import gov.gsa.ocfo.aloha.web.security.NavigationOutcomes;
import gov.gsa.ocfo.aloha.web.util.AlohaConstants;
import gov.gsa.ocfo.aloha.web.util.DateUtil;
import gov.gsa.ocfo.aloha.web.util.ErrorMessages;
import gov.gsa.ocfo.aloha.web.util.FacesContextUtil;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;

@ManagedBean(name="lrAmendMB")
@SessionScoped
public class LRAmendMB extends LRAbstractMB {
	
	private static final long serialVersionUID = -8716566124943189185L;

	@ManagedProperty(value="#{userMB}")
	protected UserMB userMB;		

	private LeaveDetail approvedLeaveDetail;
	private LeaveDetail amendedLeaveDetail;
	
	protected String selectedApprover;
	protected List<AlohaUser> dbApprovers = new ArrayList<AlohaUser>();
	protected Map<String, Object> approvers = new TreeMap<String, Object>();

	protected List<KeyValuePair> leaveBalances = new ArrayList<KeyValuePair>();

	private List<LRSideBySideItem> sideBySideItems = new ArrayList<LRSideBySideItem>();

	// METHOD REQUIRED BY ABSTRACT SUPERCLASS	
	protected AlohaUser getEmployee() {
		if (this.approvedLeaveDetail != null) {
			return this.approvedLeaveDetail.getEmployee();
		} 
		return null;
	}
	// METHOD REQUIRED BY ABSTRACT SUPERCLASS
	protected AlohaUser getSubmitter() {
		return this.userMB.getUser();
	}
	// METHOD REQUIRED ABSTRACT SUPERCLASS
	protected AlohaUser getApprover() {
		return (this.findApprover(Long.valueOf(this.selectedApprover)));
	}
	@PostConstruct
	public String initAmendment() {
		try {
			super.init();
			this.initState();
			this.checkAccessRights();
			return NavigationOutcomes.LR_AMEND;
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
		this.approvedLeaveDetail = this.leaveRequestEJB.getLeaveDetail(Long.valueOf(leaveDetailId));
		if ( this.approvedLeaveDetail == null ) {
			StringBuilder sb = new StringBuilder("Approved Leave Detail not found for Leave Detail ID " + leaveDetailId  + " in LRAmendMB.initAmendment(). Cannot continue.");
			sb.append("\nUser Login: " + this.userMB.getUser().getLoginName());
			sb.append("\nLeave Detail ID: " + leaveDetailId);
			System.out.println("********************ERROR********************");
			System.out.println(sb.toString());
			System.out.println("*********************************************");			
			throw new AlohaServerException(sb.toString());
		}
		this.initLeaveTypes();
		this.initApprovers();
		this.initEmployeeSchedule();
		this.totalLeaveHours = this.approvedLeaveDetail.getTotalLeaveHours();
		this.initLeaveBalances();
		this.submitterRemarks = null;
	}
	private void initLeaveTypes() {
		if ( this.approvedLeaveDetail != null ) {
			Date startDate = this.approvedLeaveDetail.getPayPeriodStartDate();
			if  (this.isSubmittedByOnBehalfOf()) {
				//2012-10-10 JJM 48969: get obo leave types
				this.leaveTypeMB.setLeaveTypes(this.leaveTypeMB.getLeaveTypesEffOBO(startDate));
			}
			else {
				//2012-10-10 JJM 48969: get emp leave types
				this.leaveTypeMB.setLeaveTypes(this.leaveTypeMB.getLeaveTypesEff(startDate));
			}			
		}
	}
	
	private void clearState() {
		this.lrMode = null;
		this.approvedLeaveDetail = null;
		this.amendedLeaveDetail = null;
		this.selectedApprover = null;
		this.dbApprovers.clear();
		this.approvers.clear();
		this.employeeSchedule.clear();
		this.totalLeaveHours = BigDecimal.ZERO;
		this.leaveBalances.clear();
		this.submitterRemarks = null;
	}
	protected AlohaUser findApprover(long userId) {
		for ( AlohaUser approver: this.dbApprovers) {
			if ( approver.getUserId() == userId) {
				return approver;
			}
		}
		return null;
	}
	private void checkAccessRights() throws AuthorizationException {
		if ( (!this.isSubmittedByEmployee()) && (!this.isSubmittedByOnBehalfOf()) ) {
			Object[] params = { this.userMB.getFullName(), Long.valueOf(this.approvedLeaveDetail.getLeaveHeaderId()) };
			String errMsg = ErrorMessages.getInstance().getMessage(ErrorMessages.LR_AMEND_UNAUTHORIZED, params);
			FacesContextUtil.getHttpSession().setAttribute("unauthMsg", errMsg);
			throw new AuthorizationException(errMsg);
		}
		if ( (this.isSubmittedByEmployee()) && (this.userMB.getUserId() != this.approvedLeaveDetail.getSubmitter().getUserId()) ) {
			Object[] params = { this.userMB.getFullName(), Long.valueOf(this.approvedLeaveDetail.getLeaveHeaderId()) };
			String errMsg = ErrorMessages.getInstance().getMessage(ErrorMessages.LR_AMEND_UNAUTHORIZED, params);
			FacesContextUtil.getHttpSession().setAttribute("unauthMsg", errMsg);
			throw new AuthorizationException(errMsg);
		}
		if ( (this.isSubmittedByOnBehalfOf()) && (this.userMB.getUserId() != this.approvedLeaveDetail.getSubmitter().getUserId()) ) {
			Object[] params = { this.userMB.getFullName(), Long.valueOf(this.approvedLeaveDetail.getLeaveHeaderId()) };
			String errMsg = ErrorMessages.getInstance().getMessage(ErrorMessages.LR_AMEND_UNAUTHORIZED, params);
			FacesContextUtil.getHttpSession().setAttribute("unauthMsg", errMsg);
			throw new AuthorizationException(errMsg);
		}
	}	
	private boolean isSubmittedByEmployee() {
		return	( (this.userMB.isSubmitOwn()) 
					&& (this.userMB.getUserId() == this.approvedLeaveDetail.getEmployee().getUserId())
					&& (this.userMB.getUserId() == this.approvedLeaveDetail.getSubmitter().getUserId())
				);
	}
	private boolean isSubmittedByOnBehalfOf() {
		return	( (this.userMB.isOnBehalfOf()) 
					&& (this.userMB.getUserId() != this.approvedLeaveDetail.getEmployee().getUserId())
					&& (this.userMB.getUserId() == this.approvedLeaveDetail.getSubmitter().getUserId())
				);
	}
	private void initApprovers() throws AlohaServerException {
		if ( this.getEmployee() != null) {
			this.selectedApprover = String.valueOf(this.approvedLeaveDetail.getApprover().getUserId());
			this.dbApprovers = this.employeeEJB.getLeaveApprovers(this.getEmployee().getUserId());
			for (AlohaUser approver : dbApprovers) {
				approvers.put(approver.getLabel(), approver.getValue());
			}
		}
	}	
	public void initEmployeeSchedule() throws AlohaServerException {
		String formattedPayPeriodStartDate = DateUtil.formatDate(this.approvedLeaveDetail.getPayPeriodStartDate(), PayPeriod.VALUE_FORMAT);
		this.employeeSchedule = this.employeeEJB.getPayPeriodSchedule(this.getEmployee().getUserId(), formattedPayPeriodStartDate);
		
		for ( ScheduleItem scheduleItem : this.employeeSchedule ) {		
			for ( LeaveItem leaveItem : this.approvedLeaveDetail.getLeaveItems()) {
				if ( !leaveItem.isCaptured()) {
					
					// THIS IS DONE BECAUSE LEAVE DATE CAN HAVE A TIME VALUE APPENDED TO IT 
					// WHEREAS SCHEDULE DATE WILL NOT HAVE A TIME VALUE APPENDED TO IT.
					// WE NEED TO COMPARE THE 'yyyyMMdd' VAUES ONLY.
					String yyyyMMddLeaveDate = DateUtil.formatDate(leaveItem.getLeaveDate(), DateUtil.DateFormats.YYYYMMDD);
					String yyyyMMddScheduleDate = DateUtil.formatDate(scheduleItem.getCalendarDate(), DateUtil.DateFormats.YYYYMMDD);
					
					if ( yyyyMMddLeaveDate.equals(yyyyMMddScheduleDate) ) {						
						// LEAVE yyyyMMddLeaveDate CODE
						scheduleItem.setSelectedLeaveTypeCode(leaveItem.getLeaveType().getValue());

						// LEAVE HOURS
						scheduleItem.setNumberOfLeaveHours(leaveItem.getLeaveHours());
						
						// FULL DAY OFF
						if ( leaveItem.getLeaveHours().compareTo(new BigDecimal(scheduleItem.getHoursScheduled())) >= 0) {						
							
							scheduleItem.setFullDayOff(Boolean.TRUE);
						} else {
							scheduleItem.setFullDayOff(Boolean.FALSE);
						}
						
						// START TIME
						if ( leaveItem.getStartTime() != null ) {
							GregorianCalendar cal = new GregorianCalendar();
							cal.setTime(leaveItem.getStartTime());
						
							int intHour = cal.get(Calendar.HOUR_OF_DAY);
							String strHour = (intHour < 10) ? "0" + String.valueOf(intHour) : String.valueOf(intHour);
							
							int intMinute = cal.get(Calendar.MINUTE);
							String strMinute = (intMinute < 10) ? "0" + String.valueOf(intMinute) : String.valueOf(intMinute);
							
							scheduleItem.setStartTime(strHour + strMinute);
							
						} else {
							scheduleItem.setStartTime(ScheduleItem.UNSELECTED_START_TIME);
						}
						leaveItem.setCaptured(true);
						break;
					}
				}
			}		
		}	
	}

	public String onAmend() throws ValidatorException {
		try{
			this.checkAccessRights();
			
			String dayKey = DateUtil.formatDate(this.approvedLeaveDetail.getPayPeriodStartDate(), DateUtil.DateFormats.YYYYMMDD);
			this.doValidation(this.payPeriodMB.getPayPeriodForStartDate(dayKey), Long.valueOf(this.approvedLeaveDetail.getId()));

			/***************************************************************************************
			 * CREATE THE AMENDED LEAVE DETAIL FOR THE PENDING AMENDMENT PAGE (lrAmendPending.xhtml)
			 * BUT DON'T ADD IT TO THE LEAVE HEADER UNTIL AFTER USE HAS CONFIRMED HIS/HER INTENT
			 * TO AMEND THIS LEAVE REQUEST
			 ***************************************************************************************/
			LeaveHeader leaveHeader = this.approvedLeaveDetail.getLeaveHeader();
			this.amendedLeaveDetail = this.createLeaveDetail(LeaveStatus.CodeValues.PEND_AMEND, LeaveStatusTransition.ActionCodeValues.APPROVED_TO_PEND_AMEND);
			this.doValidationForDifferences();
			
			// IT'S OK TO SET THIS OBJECT REFERENCE - JUST DON'T ADD THIS LEAVE DETAIL TO THE LEAVE HEADER YET
			this.amendedLeaveDetail.setLeaveHeader(leaveHeader); 
			/***************************************************************************************************/
			this.populateSideBySideItems();
			return NavigationOutcomes.LR_AMEND_PENDING;
		} catch (AuthorizationException ae) {
			return NavigationOutcomes.UNAUTHORIZED;
		}  catch (IllegalStateException ise) {
			return NavigationOutcomes.USER_ERROR;
		} catch (ValidatorException ve) {
			return NavigationOutcomes.LR_AMEND;
		} catch (AlohaServerException ase) {
			return NavigationOutcomes.SERVER_ERROR;
		} catch (Exception e) {
			e.printStackTrace();
			return NavigationOutcomes.SERVER_ERROR;
		}		
	}
	
	private void populateSideBySideItems() {
		this.sideBySideItems.clear();
		
		for (ScheduleItem baseScheduleItem : this.employeeSchedule) {
			if ( ! baseScheduleItem.isDuplicate()) {
				LRSideBySideItem sideBySideItem = new LRSideBySideItem();

				// SET BASE SCHEDULE DATE
				sideBySideItem.setDate(baseScheduleItem.getCalendarDate());

				// GET DATE IN THE RIGHT FORMAT FOR COMPARISON PURPOSES (STRIP-OFF ANY TIME VALUE)
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
				for ( LeaveItem pendAmendLeaveItem : this.amendedLeaveDetail.getLeaveItems()) {
					// GET DATE IN THE RIGHT FORMAT FOR COMPARISON PURPOSES (STRIP-OFF ANY TIME VALUE)
					String pendAmendLeaveDateString = DateUtil.formatDate(pendAmendLeaveItem.getLeaveDate(), DateUtil.DateFormats.YYYYMMDD);
					if (pendAmendLeaveDateString.equals(sideBySideDateString) ) {	
						sideBySideItem.addPendAmendLeaveItem(pendAmendLeaveItem);
					}
				}
				if ( sideBySideItem.isPopulated()) {
					this.sideBySideItems.add(sideBySideItem);					
				}
			}
		}
	}
	
	private void doValidationForDifferences() throws ValidatorException {
		if ( this.leaveItemsAreTheSame() ) {
			String errMsgText = ErrorMessages.getInstance().getMessage(ErrorMessages.LR_AMEND_NO_CHANGE);
			FacesMessage facesMsg = new FacesMessage(errMsgText, errMsgText);
			facesMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
			FacesContext.getCurrentInstance().addMessage(null, facesMsg);
			throw new ValidatorException(facesMsg);			
		}
	}
	private boolean leaveItemsAreTheSame() {
		List<LeaveItem> approvedLeaveItems = this.approvedLeaveDetail.getLeaveItems();
		List<LeaveItem> amendedLeaveItems = this.amendedLeaveDetail.getLeaveItems();

		if ( amendedLeaveItems.size() != approvedLeaveItems.size()) {
			return false;
		}

		int size = amendedLeaveItems.size();
		for ( int index = 0; index < size; index++) {
			
			/*
			System.out.println("\n-------------------------------------------------------------------------");
			System.out.println(amendedLeaveItems.get(index).getLeaveDate() + " / " + approvedLeaveItems.get(index).getLeaveDate());
			System.out.println(amendedLeaveItems.get(index).getLeaveType().getValue() + " / " + approvedLeaveItems.get(index).getLeaveType().getValue());
			System.out.println(amendedLeaveItems.get(index).getLeaveHours() + " / " + approvedLeaveItems.get(index).getLeaveHours());		
			System.out.println(amendedLeaveItems.get(index).getStartTime() + " / " + approvedLeaveItems.get(index).getStartTime());
			System.out.println("-------------------------------------------------------------------------");
			*/
			
			if ( amendedLeaveItems.get(index).getLeaveDate().compareTo(approvedLeaveItems.get(index).getLeaveDate()) != 0 ) {
				return false;
			}
			if ( ! amendedLeaveItems.get(index).getLeaveType().equals(approvedLeaveItems.get(index).getLeaveType())) {
				return false;
			}
			if ( amendedLeaveItems.get(index).getLeaveHours().compareTo(approvedLeaveItems.get(index).getLeaveHours()) != 0) {
				return false;
			}
			if ( amendedLeaveItems.get(index).getStartTime() != null && approvedLeaveItems.get(index).getStartTime() == null ) {
				return false;
			}
			if ( amendedLeaveItems.get(index).getStartTime() == null && approvedLeaveItems.get(index).getStartTime() != null ) {
				return false;
			}
			if ( amendedLeaveItems.get(index).getStartTime() != null && approvedLeaveItems.get(index).getStartTime() != null) {
				if ( amendedLeaveItems.get(index).getStartTime().compareTo(approvedLeaveItems.get(index).getStartTime()) != 0  ) {
					return false;
				}
			}
		}		
		return true;
	}
	public String onCancel() {
		this.clearState();
		return NavigationOutcomes.HOME_PAGE;
	}	
	public String onChange() {
		try {
			this.checkAccessRights();
			this.amendedLeaveDetail = null;
			this.refreshEmployeeSchedule();
			return NavigationOutcomes.LR_AMEND;
		} catch (AuthorizationException ae) {
			return NavigationOutcomes.UNAUTHORIZED;
		} catch (IllegalStateException ise) {
			return NavigationOutcomes.USER_ERROR;
		} catch (Exception e) {
			e.printStackTrace();
			return NavigationOutcomes.SERVER_ERROR;
		}		
	}		
	private void checkIfInLegalState() throws IllegalStateException {
		IllegalStateException illegalStateException = null;

		if ( this.userMB == null || this.userMB.getUser() == null ) {
			illegalStateException = new IllegalStateException("AlohaUser in " + this.getClass().getName() + ".checkIfInLegalState() is NULL. Cannot continue.");
			illegalStateException.printStackTrace();
			throw illegalStateException;
		}
		if ( this.approvedLeaveDetail == null) {
			String errMsg = "Approved LeaveDetail in " + this.getClass().getName() + ".checkIfInLegalState() is NULL. Cannot continue. AlohaUser is: " + this.userMB.getUser();
			illegalStateException = new IllegalStateException(errMsg);
			illegalStateException.printStackTrace();
			throw illegalStateException;
		}
		if ( this.amendedLeaveDetail == null) {
			String errMsg = "Amended LeaveDetail in " + this.getClass().getName() + ".checkIfInLegalState() is NULL. Cannot continue. AlohaUser is: " + this.userMB.getUser();
			illegalStateException = new IllegalStateException(errMsg);
			illegalStateException.printStackTrace();
			throw illegalStateException;
		}
	}	
	public String onConfirm() {
		try{
			this.checkIfInLegalState();
			this.checkAccessRights();
			
			/*********************************************************************************
			 * ONLY ADD THE NEWLY CREATED AMENDED LEAVE DETAIL TO THE LEAVE HEADER 
			 * AFTER USER HAS CONFIRMED HIS/HER INTENT
			**********************************************************************************/
			LeaveHeader leaveHeader = this.approvedLeaveDetail.getLeaveHeader();
			leaveHeader.addLeaveDetail(this.amendedLeaveDetail);
			this.amendedLeaveDetail.setSequence(leaveHeader.getLeaveDetails().size());
			/*********************************************************************************
			 * ONLY WRITE TO THE DATABASE WITH THIS LEAVE REQUEST AMENDMENT 
			 * AFTER USER HAS CONFIRMED HIS/HER INTENT. 
			 *********************************************************************************/
			StopWatch stopWatch = new StopWatch();
			stopWatch.start();
			this.leaveRequestEJB.updateLeaveHeader(this.approvedLeaveDetail.getLeaveHeader());
			stopWatch.stop();
			System.out.println("ELAPSED TIME (Amend Leave Request): " + stopWatch.getElapsedTime() + " ms");
			stopWatch = null;
			/*********************************************************************************/
			
			FacesContextUtil.getHttpSession().setAttribute(AlohaConstants.NEWLY_AMENDED_LEAVE_DETAIL, this.amendedLeaveDetail);
			FacesContextUtil.getHttpSession().setAttribute(AlohaConstants.LR_MODE_ON_LAST_AMEND, this.getMode());
			this.clearState();
			return NavigationOutcomes.LR_AMEND_CONFIRM;
		} catch (AuthorizationException ae) {
			return NavigationOutcomes.UNAUTHORIZED;
		}  catch (IllegalStateException ise) {
			return NavigationOutcomes.USER_ERROR;
		} catch (AlohaServerException ase) {
			if ( ase.getExceptionType() == AlohaServerException.ExceptionType.OPTIMISTIC_LOCK) {
				Object[] errorParams = {Long.valueOf(this.approvedLeaveDetail.getLeaveHeaderId()).toString()};
				String errMsgText = ErrorMessages.getInstance().getMessage(ErrorMessages.LR_APPROVE_OPT_LOCK_EXCEPTION, errorParams);
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
	public String onHome() {
		this.clearState();
		return NavigationOutcomes.HOME_PAGE;
	}	
	
	public LeaveDetail getApprovedLeaveDetail() {
		return approvedLeaveDetail;
	}

	public LeaveDetail getAmendedLeaveDetail() {
		if ( this.amendedLeaveDetail == null) {
			FacesContextUtil.callHome();
		}
		return this.amendedLeaveDetail;
	}
	public void setUserMB(UserMB userMB) {
		this.userMB = userMB;
	}
	public String getSelectedApprover() {
		return selectedApprover;
	}
	public void setSelectedApprover(String selectedApprover) {
		this.selectedApprover = selectedApprover;
	}
	public Map<String, Object> getApprovers() {
		return approvers;
	}
	public String getEmployeeName() {
		if ( this.getEmployee() != null) {
			return this.getEmployee().getFullName();
		}
		return null;
	}
	public List<LRSideBySideItem> getSideBySideItems() {
		return sideBySideItems;
	}
}