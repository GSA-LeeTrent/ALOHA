package gov.gsa.ocfo.aloha.web.mb.leave;

import gov.gsa.ocfo.aloha.ejb.leave.EmployeeEJB;
import gov.gsa.ocfo.aloha.ejb.leave.LeaveRequestEJB;
import gov.gsa.ocfo.aloha.exception.AlohaServerException;
import gov.gsa.ocfo.aloha.exception.AlohaValidationException;
import gov.gsa.ocfo.aloha.model.KeyValuePair;
import gov.gsa.ocfo.aloha.model.PayPeriod;
import gov.gsa.ocfo.aloha.model.ScheduleItem;
import gov.gsa.ocfo.aloha.model.entity.AlohaUser;
import gov.gsa.ocfo.aloha.model.entity.AuditTrail;
import gov.gsa.ocfo.aloha.model.entity.leave.LeaveDetail;
import gov.gsa.ocfo.aloha.model.entity.leave.LeaveHeader;
import gov.gsa.ocfo.aloha.model.entity.leave.LeaveHistory;
import gov.gsa.ocfo.aloha.model.entity.leave.LeaveItem;
import gov.gsa.ocfo.aloha.model.entity.leave.LeaveStatus;
import gov.gsa.ocfo.aloha.model.entity.leave.LeaveStatusTransition;
import gov.gsa.ocfo.aloha.util.StringUtil;
import gov.gsa.ocfo.aloha.web.enums.LRMode;
import gov.gsa.ocfo.aloha.web.mb.PayPeriodMB;
import gov.gsa.ocfo.aloha.web.util.AlohaConstants;
import gov.gsa.ocfo.aloha.web.util.DateUtil;
import gov.gsa.ocfo.aloha.web.util.ErrorMessages;
import gov.gsa.ocfo.aloha.web.util.FacesContextUtil;
import gov.gsa.ocfo.aloha.web.validator.leave.LRAWSValidator;
import gov.gsa.ocfo.aloha.web.validator.leave.LRNonAWSValidator;
import gov.gsa.ocfo.aloha.web.validator.leave.LRValidator;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedProperty;
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;

public abstract class LRAbstractMB implements Serializable {
	private static final long serialVersionUID = -8310586868898880449L;

	protected abstract AlohaUser getEmployee();
	protected abstract AlohaUser getSubmitter();
	protected abstract AlohaUser getApprover();
	
	@EJB
	protected EmployeeEJB employeeEJB;

	@EJB
	protected LeaveRequestEJB leaveRequestEJB;

	@ManagedProperty(value="#{payPeriodMB}")
	protected PayPeriodMB payPeriodMB;		

	@ManagedProperty(value="#{leaveStatusMB}")
	protected LeaveStatusMB leaveStatusMB;

	@ManagedProperty(value="#{leaveTypeMB}")
	protected LeaveTypeMB leaveTypeMB;
	
	protected LRMode lrMode;
	protected BigDecimal totalLeaveHours;
	protected List<ScheduleItem> employeeSchedule;
	protected String submitterRemarks;
	protected List<KeyValuePair> leaveBalances = new ArrayList<KeyValuePair>();
	
	protected void init() {
		this.lrMode = LRMode.fromString(FacesContextUtil.getHttpServletRequest().getParameter(AlohaConstants.LR_MODE));
		if ( this.lrMode == null) {
			FacesContextUtil.callHome();
		}
	}
	
	protected void initLeaveBalances() throws AlohaServerException {
		if ( this.getEmployee() != null) {
			this.leaveBalances = this.employeeEJB.getLeaveBalances(this.getEmployee().getUserId());			
		}
	}	
	protected void doValidation(PayPeriod payPeriod) throws ValidatorException, AlohaServerException {
		this.doInternalValidation();
		this.doExternalValidation(payPeriod, null);
	}
	protected void doValidation(PayPeriod payPeriod, Long leaveDetailIdToExclude) throws ValidatorException, AlohaServerException {
		this.doInternalValidation();
		this.doExternalValidation(payPeriod, leaveDetailIdToExclude);
	}

	
	protected void doInternalValidation() throws ValidatorException {
		int errorCount = 0;
		
		// APPROVER
		if ( this.getApprover() == null) {
			String errMsgText = ErrorMessages.getInstance().getMessage(ErrorMessages.APPROVER_REQUIRED);
			FacesMessage facesMsg = new FacesMessage(errMsgText, errMsgText);
			facesMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
			FacesContext.getCurrentInstance().addMessage(null, facesMsg);
			errorCount++;
		}			
		
		// LEAVE HOURS MUST BE GREATER THAN ZERO
		this.totalLeaveHours = BigDecimal.ZERO;

		for ( ScheduleItem scheduleItem: this.employeeSchedule) {
			if ( scheduleItem.getNumberOfLeaveHours() != null) {
				this.totalLeaveHours = this.totalLeaveHours.add(scheduleItem.getNumberOfLeaveHours());				
			}
		}
		
		this.totalLeaveHours.setScale(1, RoundingMode.HALF_DOWN);
		
		if ( this.totalLeaveHours.compareTo(LeaveItem.MIN_LEAVE_HOURS) == -1 ) {
			String errMsgText = ErrorMessages.getInstance().getMessage(ErrorMessages.ZERO_TOTAL_LEAVE_HOURS);
			FacesMessage facesMsg = new FacesMessage(errMsgText, errMsgText);
			facesMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
			FacesContext.getCurrentInstance().addMessage(null, facesMsg);
			errorCount++;
		}

		// VALIDATE LEAVE HOURS, LEAVE TYPE AND START TIME 
		for ( ScheduleItem scheduleItem: this.employeeSchedule) {
			boolean leaveHoursSelected	= ( (scheduleItem.getNumberOfLeaveHours() != null) 
												&& (scheduleItem.getNumberOfLeaveHours().compareTo(LeaveItem.MIN_LEAVE_HOURS) >= 0) );			
			boolean leaveTypeSelected = (!scheduleItem.getSelectedLeaveTypeCode().equals(ScheduleItem.UNSELECTED_LEAVE_TYPE_CODE));
			boolean startTimeSelected = (!scheduleItem.getStartTime().equals(ScheduleItem.UNSELECTED_START_TIME));
			
			//boolean startHourSelected  = (!scheduleItem.getStartHour().equals(ScheduleItem.UNSELECTED_START_HOUR));
			//boolean startMinuteSelected  = (!scheduleItem.getStartMinute().equals(ScheduleItem.UNSELECTED_START_MINUTE));
			//boolean meridiemSelected = (!scheduleItem.getSelectedMeridiem().equals(ScheduleItem.UNSELECTED_MERIDIEM));
			//boolean startTimeSelected = (startHourSelected || startMinuteSelected || meridiemSelected);
						
			if (leaveHoursSelected || leaveTypeSelected || startTimeSelected) {
				
				// IF LEAVE HOURS ARE GREATER THAN ZERO OR START TIME HAS BEEN POPULATED, THEN A CORRESPONDING LEAVE TYPE MUST BE SELECTED
				if ( (leaveHoursSelected || startTimeSelected) && (!leaveTypeSelected) ) {  
					Object[] params = {DateUtil.formatDate(scheduleItem.getCalendarDate())};
					String errMsgText = ErrorMessages.getInstance().getMessage(ErrorMessages.LEAVE_TYPE_FOR_ROW_REQUIRED, params);
					FacesMessage facesMsg = new FacesMessage(errMsgText, errMsgText);
					facesMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
					FacesContext.getCurrentInstance().addMessage(null, facesMsg);
					errorCount++;
				}

				/***********************************************************************************************************
					WE'VE OPENED UP THE BASE SCHEDULE SO NOW WE'RE ALLOWING A USER TO ENTER LEAVE HOURS ON A DAY THAT HE/SHE
					IS NOT SCHEDULE TO WORK (ie: Weekends and Holidays)
				if  ( (leaveHoursSelected) 
						&& (scheduleItem.getNumberOfLeaveHours().floatValue() > scheduleItem.getHoursScheduled()) ) {  
					Object[] params = {DateUtil.formatDate(scheduleItem.getCalendarDate())};
					String errMsgText = ErrorMessages.getInstance().getMessage(ErrorMessages.LR_TOO_MANY_HOURS, params);
					FacesMessage facesMsg = new FacesMessage(errMsgText, errMsgText);
					facesMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
					FacesContext.getCurrentInstance().addMessage(null, facesMsg);
					errorCount++;
				}
				***********************************************************************************************************/	
				
				// IF LEAVE TYPE HAS BEEN SELECTED OR START TIME HAS BEEN POPULATED, THEN CORRESPONDING LEAVE HOURS MUST BE GREATER THAN ZERO
				if (  (leaveTypeSelected  || startTimeSelected) && (!leaveHoursSelected) ) { 
					Object[] params = {DateUtil.formatDate(scheduleItem.getCalendarDate())};
					String errMsgText = ErrorMessages.getInstance().getMessage(ErrorMessages.LEAVE_HOURS_FOR_ROW_REQUIRED, params);
					FacesMessage facesMsg = new FacesMessage(errMsgText, errMsgText);
					facesMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
					FacesContext.getCurrentInstance().addMessage(null, facesMsg);
					errorCount++;
				}
		
				/*
				// START TIME IS OPTIONAL BUT MAKE SURE THAT IT'S POPULATED CORRECTLY (START MINUTE)
				if ( ( startHourSelected || meridiemSelected )	&& ( !startMinuteSelected ) ) {
					Object[] params = {DateUtil.formatDate(scheduleItem.getCalendarDate())};
					String errMsgText = ErrorMessages.getInstance().getMessage(ErrorMessages.START_MINUTE_REQUIRED, params);
					FacesMessage facesMsg = new FacesMessage(errMsgText, errMsgText);
					facesMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
					FacesContext.getCurrentInstance().addMessage(null, facesMsg);
					errorCount++;
				}
				*/
				/*
				// START TIME IS OPTIONAL BUT MAKE SURE THAT IT'S POPULATED CORRECTLY (START HOUR)
				if ( ( startMinuteSelected || meridiemSelected ) && ( !startHourSelected) ) {
					Object[] params = {DateUtil.formatDate(scheduleItem.getCalendarDate())};
					String errMsgText = ErrorMessages.getInstance().getMessage(ErrorMessages.START_HOUR_REQUIRED, params);
					FacesMessage facesMsg = new FacesMessage(errMsgText, errMsgText);
					facesMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
					FacesContext.getCurrentInstance().addMessage(null, facesMsg);
					errorCount++;
				}
				*/
				
				/*
				// START TIME IS OPTIONAL BUT MAKE SURE THAT IT'S POPULATED CORRECTLY (AM/PM)
				if ( ( startHourSelected || startMinuteSelected ) && ( !meridiemSelected) ) {
					Object[] params = {DateUtil.formatDate(scheduleItem.getCalendarDate())};
					String errMsgText = ErrorMessages.getInstance().getMessage(ErrorMessages.START_MERIDIEM_REQUIRED, params);
					FacesMessage facesMsg = new FacesMessage(errMsgText, errMsgText);
					facesMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
					FacesContext.getCurrentInstance().addMessage(null, facesMsg);
					errorCount++;
				}
				*/
			}
		}

		if ( errorCount > 0) {
			throw new ValidatorException(new FacesMessage());
		}
	}		
	protected void doExternalValidation(PayPeriod payPeriod, Long leaveDetailIdToExlude) throws AlohaServerException, ValidatorException {
		//List<String> leaveStatusCodes = Arrays.asList(LeaveStatus.CodeValues.APPROVED, LeaveStatus.CodeValues.SUBMITTED);
		
		List<LeaveItem> priorLeaveItemsForPP = null;
		if ( leaveDetailIdToExlude == null) {
			priorLeaveItemsForPP = this.leaveRequestEJB.getPriorLeaveItemsForCreate(this.getEmployee().getUserId(), payPeriod.getFromDate());	
		} else {
			priorLeaveItemsForPP = this.leaveRequestEJB.getPriorLeaveItemsForAmend
									(this.getEmployee().getUserId(), payPeriod.getFromDate(), leaveDetailIdToExlude.longValue());			
		}
		try {
			LRValidator lrVal = null;
			///////////////////////////////////////////////////////////////////////////
			//////////////////////// IMPORTANT /////////////////////////////////////// 
			// The AWS property on AlohaUser is transient and is therefore unreliable.
			// As such, retrieve AWS Indicator directly from database
			///////////////////////////////////////////////////////////////////////////
			boolean employeeIsOnAws = this.employeeEJB.getAwsFlagForEmployee(this.getEmployee().getUserId());
			if (employeeIsOnAws) {
				lrVal = new LRAWSValidator(payPeriod, priorLeaveItemsForPP);
			} else {
				lrVal = new LRNonAWSValidator(payPeriod, priorLeaveItemsForPP);
			}
			lrVal.validateCurrentRequest(this.employeeSchedule);
		} catch (AlohaValidationException ave) {
			List<String> errorMessages = ave.getErrorMessages();
			for ( String errMsg : errorMessages) {
				FacesMessage facesMsg = new FacesMessage(errMsg, errMsg);
				facesMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
				FacesContext.getCurrentInstance().addMessage(null, facesMsg);
			}
			throw new ValidatorException(new FacesMessage());
		}
	}
	
	protected LeaveHeader createLeaveRequest(String leaveStatus, String leaveStatusTransition) throws AlohaServerException {
		// AUDIT TRAIL
		AuditTrail auditTrail = new AuditTrail();
		auditTrail.setUserLastUpdated(this.getSubmitter().getLoginName());
		auditTrail.setDateLastUpdated(new Date());
		
		// HEADER
		LeaveHeader header = new LeaveHeader();
		header.setAuditTrail(auditTrail);
		header.setEmployee(this.getEmployee());
		
		// DETAIL
		LeaveDetail detail = this.createLeaveDetail(leaveStatus, leaveStatusTransition);
		header.addLeaveDetail(detail);
		detail.setLeaveHeader(header);
		detail.setSequence(header.getLeaveDetails().size());
		
		return header;
	}
	
	protected LeaveDetail createLeaveDetail(String leaveStatus, String leaveStatusTransition) throws AlohaServerException {
		// AUDIT TRAIL
		AuditTrail auditTrail = new AuditTrail();
		auditTrail.setUserLastUpdated(this.getSubmitter().getLoginName());
		auditTrail.setDateLastUpdated(new Date());
		
		// DETAIL
		LeaveDetail detail = new LeaveDetail();
		detail.setAuditTrail(auditTrail);
		detail.setSubmitter(this.getSubmitter());
		detail.setApprover(this.getApprover());
		detail.setPayPeriodStartDate(this.employeeSchedule.get(0).getCalendarDate());
		detail.setPayPeriodEndDate(this.employeeSchedule.get(employeeSchedule.size() - 1).getCalendarDate());
		
		LeaveStatus status = this.leaveStatusMB.getLeaveStatus(leaveStatus);
		detail.setLeaveStatus(status);
				
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
		
		/**************************************************************************************************************
		// SUBMITTER COMMENT
		if ( !StringUtil.isNullOrEmpty(this.submitterRemarks) ) {
			int sequence = detail.getSubmitterComments().size() + 1;
			LeaveSubmitterComment lsComment = new LeaveSubmitterComment(this.submitterRemarks.trim(), sequence, detail);
			lsComment.setAuditTrail(auditTrail);
			detail.addSubmitterComment(lsComment);
		}
		**************************************************************************************************************/
		
		// HISTORY
		LeaveStatusTransition transition = this.leaveStatusMB.getLeaveStatusTransition(leaveStatusTransition);
		LeaveHistory history = new LeaveHistory();
		history.setAuditTrail(auditTrail);
		history.setLeaveDetail(detail);
		history.setLeaveStatusTransition(transition);
		history.setActionDatetime(new Date());
		history.setActor(this.getSubmitter());
		if ( !StringUtil.isNullOrEmpty(this.submitterRemarks) ) {
			history.setRemarks(this.submitterRemarks);
		}
		
		detail.addLeaveHistory(history);
		
		// ITEMS
		for ( ScheduleItem scheduleItem: this.employeeSchedule) {
			if ( (scheduleItem.getNumberOfLeaveHours() != null) 
					&& (scheduleItem.getNumberOfLeaveHours().compareTo(LeaveItem.MIN_LEAVE_HOURS) >= 0)) {
				LeaveItem item = new LeaveItem();
				item.setAuditTrail(auditTrail);
				item.getAuditTrail().setUserLastUpdated(this.getSubmitter().getLoginName());
				item.setLeaveDetail(detail);
				
				// LEAVE TYPE CODE
				String selectedLeaveTypeCode = scheduleItem.getSelectedLeaveTypeCode();
				if ( selectedLeaveTypeCode.length() == 2) {
					item.setLeaveType(this.leaveTypeMB.getLeaveType(selectedLeaveTypeCode));
				} else if (selectedLeaveTypeCode.length() == 4) {
					item.setLeaveType(this.leaveTypeMB.getLeaveType(selectedLeaveTypeCode.substring(0,2), selectedLeaveTypeCode.substring(2)));
				}
				
				// LEAVE DATE
				item.setLeaveDate(scheduleItem.getCalendarDate());

				// LEAVE HOURS
				item.setLeaveHours(scheduleItem.getNumberOfLeaveHours());

				// START TIME
				if ( (scheduleItem.getStartTime() != null)
						&& (!scheduleItem.getStartTime().equals(ScheduleItem.UNSELECTED_START_TIME)) ) {
					
					// Initialize start time GregorianCalendar with leave date and clear out the time portion of calendar
					Date startTime = scheduleItem.getCalendarDate();
					GregorianCalendar startCal = new GregorianCalendar();
					startCal.setTime(startTime);
					startCal.set(Calendar.HOUR_OF_DAY, 0);
					startCal.set(Calendar.MINUTE, 0);					
					startCal.set(Calendar.SECOND, 0);
					startCal.set(Calendar.MILLISECOND, 0);
					
					// SET HOUR
					String strHour = scheduleItem.getStartTime().substring(0, 2);
					Integer intHour = Integer.valueOf(strHour);
					startCal.set(Calendar.HOUR_OF_DAY, intHour.intValue());
					
					// SET MINUTE
					String strMinute = scheduleItem.getStartTime().substring(2, 4);
					Integer intMinute = Integer.valueOf(strMinute);
					startCal.set(Calendar.MINUTE, intMinute.intValue());

					item.setStartTime(startCal.getTime());
				}
				detail.addLeaveItem(item);
			}
		}
		return detail;
	}	
	public void setPayPeriodMB(PayPeriodMB payPeriodMB) {
		this.payPeriodMB = payPeriodMB;
	}
	public void setLeaveStatusMB(LeaveStatusMB leaveStatusMB) {
		this.leaveStatusMB = leaveStatusMB;
	}
	public void setLeaveTypeMB(LeaveTypeMB leaveTypeMB) {
		this.leaveTypeMB = leaveTypeMB;
	}
	
	public BigDecimal getTotalLeaveHours() {
		return totalLeaveHours;
	}
	public void setTotalLeaveHours(BigDecimal totalLeaveHours) {
		this.totalLeaveHours = totalLeaveHours;
	}
	public List<ScheduleItem> getEmployeeSchedule() {
		return employeeSchedule;
	}
	public String getSubmitterRemarks() {
		return submitterRemarks;
	}
	public void setSubmitterRemarks(String submitterRemarks) {
		this.submitterRemarks = submitterRemarks;
	}
	public List<KeyValuePair> getLeaveBalances() {
		return leaveBalances;
	}
	public int getLeaveBalanceCount() {
		return (this.leaveBalances == null) ? (0) :  (this.leaveBalances.size()) ;
	}	
	public String getMode() {
		return this.lrMode.getText();
	}
	protected void refreshEmployeeSchedule() {
		for ( ScheduleItem si : this.employeeSchedule) {
			si.setDuplicatePopulated(this.isScheduleItemDuplicatePopulated(si.getCalendarDate()));
		}
		//this.debugEmployeeSchedule();
	}
	/*
	private void debugEmployeeSchedule() {
		for (ScheduleItem si : this.employeeSchedule ) {
			System.out.println(si.getCalendarDate() + " | " + si.getHoursScheduled() + " | duplicatePopulated: " + si.isDuplicatePopulated() +  " | duplicate: " + si.isDuplicate() + " | populated: " + si.isPopulated() );
		}
	}
	*/
	private boolean isScheduleItemDuplicatePopulated(Date scheduleDate) {
		for ( ScheduleItem si : this.employeeSchedule) {
			if 	( (si.isDuplicate()) 
					&& (si.isPopulated())
					&& (si.getCalendarDate().compareTo(scheduleDate) == 0)
				) {
				return true;
			}
		}
		return false;
	}
	
	public int getWorkDayCount() {
		return (this.employeeSchedule.size());
	}
	public void setWorkDayCount(int workDayCount) {
		// do nothing (keeping JSF happy)
	}
	public boolean isInSubmitOwnMode() {
		return ( (this.lrMode != null) && (this.lrMode.equals(LRMode.SUBMIT_OWN)) );
	}
	public boolean isInOnBehalfOfMode() {
		return ( (this.lrMode != null) && (this.lrMode.equals(LRMode.ON_BEHALF_OF)) );
	}	
}