package gov.gsa.ocfo.aloha.web.mb.ot;

import gov.gsa.ocfo.aloha.ejb.UserEJB;
import gov.gsa.ocfo.aloha.ejb.leave.EmployeeEJB;
import gov.gsa.ocfo.aloha.ejb.overtime.OvertimeEJB;
import gov.gsa.ocfo.aloha.exception.AlohaServerException;
import gov.gsa.ocfo.aloha.exception.AuthorizationException;
import gov.gsa.ocfo.aloha.model.KeyValuePair;
import gov.gsa.ocfo.aloha.model.entity.AlohaUser;
import gov.gsa.ocfo.aloha.model.entity.AlohaUserPref;
import gov.gsa.ocfo.aloha.model.entity.overtime.OTDetail;
import gov.gsa.ocfo.aloha.model.entity.overtime.OTDetailHistory;
import gov.gsa.ocfo.aloha.model.entity.overtime.OTDetailSubmitterRemark;
import gov.gsa.ocfo.aloha.model.entity.overtime.OTDetailSupervisorRemark;
import gov.gsa.ocfo.aloha.model.entity.overtime.OTHeader;
import gov.gsa.ocfo.aloha.model.entity.overtime.OTIndivStatus;
import gov.gsa.ocfo.aloha.model.entity.overtime.OTIndivStatusTrans;
import gov.gsa.ocfo.aloha.model.entity.overtime.OTItem;
import gov.gsa.ocfo.aloha.model.entity.overtime.OTPayPeriod;
import gov.gsa.ocfo.aloha.model.entity.overtime.OTType;
import gov.gsa.ocfo.aloha.util.StopWatch;
import gov.gsa.ocfo.aloha.web.mb.ConstantsMB;
import gov.gsa.ocfo.aloha.web.mb.UserMB;
import gov.gsa.ocfo.aloha.web.mb.overtime.OTUtilMB;
import gov.gsa.ocfo.aloha.web.model.OTTask;
import gov.gsa.ocfo.aloha.web.model.overtime.OTStatusChangeOutcome;
import gov.gsa.ocfo.aloha.web.security.NavigationOutcomes;
import gov.gsa.ocfo.aloha.web.util.AlohaConstants;
import gov.gsa.ocfo.aloha.web.util.ErrorMessages;
import gov.gsa.ocfo.aloha.web.util.FacesContextUtil;
import gov.gsa.ocfo.aloha.web.util.StringUtil;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedProperty;
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;

public abstract class OTCreateAbstractMB implements Serializable {
	private static final long serialVersionUID = 2440615206015302317L;

	protected abstract void checkAccessRights() throws AuthorizationException;
	protected abstract AlohaUser getEmployee();
	protected abstract String buildConfirmationMessage(OTHeader otHeader);
	protected abstract String getSuccessPage();
	protected abstract String getValidationFailurePage();	
	protected abstract void initOTBalances() throws AlohaServerException;
	protected abstract String getStatusChangeOutcomeKey();
		
	@EJB
	protected OvertimeEJB overtimeEJB;
	
	@EJB
	protected EmployeeEJB employeeEJB;
	
	@EJB 
	private UserEJB userEJB;
	
	// JSF: MANAGED BEAN INJECTION
	@ManagedProperty(value="#{userMB}")
	protected UserMB userMB;		
	public void setUserMB(UserMB userMB) {
		this.userMB = userMB;
	}

	// JSF: MANAGED BEAN INJECTION
	@ManagedProperty(value="#{otTypesMB}")
	protected OTTypesMB otTypesMB;	
	public void setOtTypesMB(OTTypesMB otTypesMB) {
		this.otTypesMB = otTypesMB;
	}

	// JSF: MANAGED BEAN INJECTION
	@ManagedProperty(value="#{constantsMB}")
	protected ConstantsMB constantsMB;	
	public void setConstantsMB(ConstantsMB constantsMB) {
		this.constantsMB = constantsMB;
	}

	// JSF: MANAGED BEAN INJECTION
	@ManagedProperty(value="#{otUtilMB}")
	protected OTUtilMB otUtilMB;	
	public void setOtUtilMB(OTUtilMB otUtilMB) {
		this.otUtilMB = otUtilMB;
	}
	
	// PAY PERIOD
	private String selectedPayPeriod;

	// OT TYPE (Overtime, Comp Time Earned, Credit Hours Earned
	private String selectedType;
	
	// APPROVER
	private String selectedApprover;
	private List<AlohaUser> dbApprovers = new ArrayList<AlohaUser>();
	private Map<String, Object> approvers = new TreeMap<String, Object>();	
	
	
	protected String submitterRemarks;
	protected String supervisorRemarks;
	protected List<OTTask> taskList = new ArrayList<OTTask>();

	// TOTALS OT HOURS
	protected BigDecimal totalOvertimeHours;	
		
	// OT BALANCES
	protected List<KeyValuePair> otBalances = new ArrayList<KeyValuePair>();
	
	protected void init() throws AuthorizationException {
		this.checkAccessRights();
		this.clearState();
		this.initTaskList();
	}
	protected void clearState() {
		this.selectedType = null;
		this.selectedPayPeriod = null;
		this.selectedApprover = null;
		this.submitterRemarks = null;
		this.supervisorRemarks = null;
		this.totalOvertimeHours = BigDecimal.ZERO;
		this.taskList.clear();
		this.otBalances.clear();
		this.approvers.clear();

	}	
	
	private void initTaskList() {
		// The 1st task on the page is visible. 
		this.taskList.add(new OTTask(1, true));
		
		//The remaining tasks are hidden until needed by user.
		for ( int ii = 1; ii <= AlohaConstants.TASK_LIST_UPPER_BOUND; ii++) {
			this.taskList.add(new OTTask((ii+1), false));
		}
	}	
	protected void initApprovers() throws AlohaServerException {
		if ( this.getEmployee() != null) {
			
			// POPULATE DROPDOWN
			this.dbApprovers = this.employeeEJB.getLeaveApprovers(this.getEmployee().getUserId());
			for (AlohaUser approver : this.dbApprovers) {
				this.approvers.put(approver.getLabel(), approver.getValue());
			}
			
			// SELECTED APPROVER
			AlohaUserPref userPrefs = this.userEJB.getUserPref(this.getEmployee().getUserId());
			if ( (userPrefs != null) && (userPrefs.getDefaultApproverUserId() != null) ) {
				this.selectedApprover = String.valueOf(userPrefs.getDefaultApproverUserId());
			}
		}
	}	
	// HELPER METHOD
	private OTStatusChangeOutcome buildStatusChangeOutcome(OTHeader otHeader) {
		return new OTStatusChangeOutcome(OTIndivStatus.CodeValues.SUBMITTED, this.buildConfirmationMessage(otHeader));
	}
	
	public String onSubmit() {
		try {
			this.doValidation();

			StopWatch stopWatch = new StopWatch();
			stopWatch.start();
			OTHeader otHeader = this.createOvertimeRequest(OTIndivStatus.CodeValues.SUBMITTED, OTIndivStatusTrans.ActionCodeValues.NONE_TO_SUBMITTED);
			stopWatch.stop();
			System.out.println("ELAPSED TIME (Create Overtime Request): " + stopWatch.getElapsedTime() + " ms");
			stopWatch = null;			
			
			this.overtimeEJB.saveOvertimeRequest(otHeader);
			FacesContextUtil.getHttpSession().setAttribute(this.getStatusChangeOutcomeKey(), this.buildStatusChangeOutcome(otHeader));
			return this.getSuccessPage();
		} catch (ValidatorException ve) {
			return this.getValidationFailurePage();
		} catch (AlohaServerException ase) {
			return NavigationOutcomes.SERVER_ERROR;
		}
	}	
	protected OTHeader createOvertimeRequest(String otStatus, String otStatusTransition) throws AlohaServerException {
		OTHeader otHeader = new OTHeader();
		
		// OT TYPE (Overtime, Comp Time, Credit Hours)
		//2012-10-12 JJM 48969: Replaced by OTTypesMB
		otHeader.setType(this.otTypesMB.getOTType(this.selectedType));

		// PAY PERIOD
		otHeader.setPayPeriod(this.otUtilMB.getOTPayPeriod(this.selectedPayPeriod));
		
		// EMPLOYEE
		otHeader.setEmployee(this.getEmployee());
	
		// SALARY GRADE 
		if ( StringUtil.isNullOrEmpty(otHeader.getEmployee().getSalaryGradeKey()) ) {
			String salaryGradeKey = this.overtimeEJB.retrieveSalaryGradeKey(otHeader.getEmployee().getUserId());
			otHeader.setSalaryGrade(this.otUtilMB.getOTSalaryGrade(salaryGradeKey));
		} else {
			otHeader.setSalaryGrade(this.otUtilMB.getOTSalaryGrade(otHeader.getEmployee().getSalaryGradeKey()));
		}
		
		// DETAIL
		OTDetail otDetail = this.createOvertimeDetail(otStatus, otStatusTransition);
		otHeader.addDetail(otDetail);
		otDetail.setHeader(otHeader);
		otDetail.setSequence(otHeader.getDetails().size());
				
		return otHeader;
	}
	
	protected OTDetail createOvertimeDetail(String otStatus, String otStatusTransition) throws AlohaServerException {
		OTDetail otDetail = new OTDetail();
		otDetail.setStatus(this.otUtilMB.getOTIndivStatus(otStatus));
		otDetail.setSubmitter(this.userMB.getUser());
		otDetail.setSupervisor(this.getApprover());
		
		OTIndivStatusTrans otStatusTrans = this.otUtilMB.getOTIndivStatusTrans(otStatusTransition);
		
		// SUBMITTER REMARK
		if ( !StringUtil.isNullOrEmpty(this.submitterRemarks) ) {
			OTDetailSubmitterRemark otSubmitterRemark = new OTDetailSubmitterRemark();
			otSubmitterRemark.setDetail(otDetail);
			otSubmitterRemark.setStatusTransition(otStatusTrans);
			otSubmitterRemark.setSequence(otDetail.getSubmitterRemarks().size() + 1);
			otSubmitterRemark.setText(this.submitterRemarks);
			otDetail.addSubmitterRemark(otSubmitterRemark);
		}
		
		// SUPERVISOR REMARK
		if ( !StringUtil.isNullOrEmpty(this.supervisorRemarks) ) {
			OTDetailSupervisorRemark otSupervisorRemark = new OTDetailSupervisorRemark();
			otSupervisorRemark.setDetail(otDetail);
			otSupervisorRemark.setStatusTransition(otStatusTrans);
			otSupervisorRemark.setSequence(otDetail.getSupervisorRemarks().size() + 1);
			otSupervisorRemark.setText(this.supervisorRemarks);
			otDetail.addSupervisorRemark(otSupervisorRemark);
		}
		
		// HISTORY
		//OTIndivStatusTrans otStatusTrans = this.otUtilMB.getOTIndivStatusTrans(otStatusTransition);
		OTDetailHistory otHistory = new OTDetailHistory();
		otHistory.setDetail(otDetail);
		otHistory.setStatusTransition(this.otUtilMB.getOTIndivStatusTrans(otStatusTransition));
		otHistory.setActor(this.userMB.getUser());
		otHistory.setActionDatetime(new Date());
		otDetail.addDetailHistory(otHistory);
		
		for (OTTask otTask: this.taskList ) {
			if ( (otTask.getEstHours() != null) && (!StringUtil.isNullOrEmpty(otTask.getDesc())) ) {
				OTItem otItem = new OTItem();
				otItem.setDetail(otDetail);
				otItem.setTaskDescription(otTask.getDesc().trim());
				otItem.setEstimatedHours(otTask.getEstHours());
				otDetail.addItem(otItem);				
			}
		}
		return otDetail;
	}

	protected void doValidation() throws ValidatorException, AlohaServerException {
		int errorCount = 0;

		// EMPLOYEE
		AlohaUser employee = this.getEmployee();
		if ( employee == null) {
			String errMsgText = ErrorMessages.getInstance().getMessage(ErrorMessages.EMPLOYEE_REQUIRED);
			FacesMessage facesMsg = new FacesMessage(errMsgText, errMsgText);
			facesMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
			FacesContext.getCurrentInstance().addMessage(null, facesMsg);
			errorCount++;
		}
		
		// EMPLOYEE'S SALARY GRADE 
		if ( StringUtil.isNullOrEmpty(employee.getSalaryGradeKey())) {
			String salaryGradeKey = this.overtimeEJB.retrieveSalaryGradeKey(employee.getUserId());
			if ( StringUtil.isNullOrEmpty(salaryGradeKey)) {
				Object[] params = { this.getEmployee().getFullName() };
				String errMsgText = ErrorMessages.getInstance().getMessage(ErrorMessages.OT_SALARY_GRADE_MISSING, params);
				FacesMessage facesMsg = new FacesMessage(errMsgText, errMsgText);
				facesMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
				FacesContext.getCurrentInstance().addMessage(null, facesMsg);
				errorCount++;
			} else {
				employee.setSalaryGradeKey(salaryGradeKey);
			}
		}

		// APPROVER
		if ( !this.isApproverSelected()) {
			String errMsgText = ErrorMessages.getInstance().getMessage(ErrorMessages.APPROVER_REQUIRED);
			FacesMessage facesMsg = new FacesMessage(errMsgText, errMsgText);
			facesMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
			FacesContext.getCurrentInstance().addMessage(null, facesMsg);
			errorCount++;
		}			
		// PAY PERIOD
		if (  !this.isPayPeriodSelected()) {
			String errMsgText = ErrorMessages.getInstance().getMessage(ErrorMessages.PAY_PERIOD_REQUIRED);
			FacesMessage facesMsg = new FacesMessage(errMsgText, errMsgText);
			facesMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
			FacesContext.getCurrentInstance().addMessage(null, facesMsg);
			errorCount++;
		}			
		// OT TYPE (Overtime, Comp Time Earned, Credit Hours Earned
		if (  !this.isTypeSelected()) {
			String errMsgText = ErrorMessages.getInstance().getMessage(ErrorMessages.OT_TYPE_REQUIRED);
			FacesMessage facesMsg = new FacesMessage(errMsgText, errMsgText);
			facesMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
			FacesContext.getCurrentInstance().addMessage(null, facesMsg);
			errorCount++;
		}			

		// OVERTIME HOURS MUST BE GREATER THAN ZERO
		this.totalOvertimeHours = BigDecimal.ZERO;
		for ( OTTask otTask: this.taskList) {
			if ( otTask.getEstHours() != null) {
				this.totalOvertimeHours = this.totalOvertimeHours.add(otTask.getEstHours());				
			}
		}
		this.totalOvertimeHours.setScale(1, RoundingMode.HALF_DOWN);
		
		if ( this.totalOvertimeHours.compareTo(OTItem.MIN_OT_HOURS) == -1 ) {
			String errMsgText = ErrorMessages.getInstance().getMessage(ErrorMessages.OT_ZERO_TOTAL_EST_HOURS);
			FacesMessage facesMsg = new FacesMessage(errMsgText, errMsgText);
			facesMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
			FacesContext.getCurrentInstance().addMessage(null, facesMsg);
			errorCount++;
		}
		// MAKE SURE THAT CORRESPONDING TASK DESCRIPTION AND ESTIMATED HOURS FOR A GIVEN ROW ARE BOTH POPULATED.
		// IN OTHER WORDS, IF ONE IS POPULATED, THE OTHER ONE MUST ALSO BE POPULATED.
		int taskErrorCount = 0;
		for ( OTTask otTask: this.taskList) {
			// INITIALIZE FLAGS
			otTask.setRenderable(false);
			otTask.setDescError(false);
			otTask.setEstHoursError(false);
			
			// MANAGE PRESENTATION
			if	( ( !StringUtil.isNullOrEmpty(otTask.getDesc()) ) 
					|| ( otTask.getEstHours() != null )
				) {
				otTask.setRenderable(true);
			}

			// CHECK FOR EMPTY ESTIMATED HOURS FOR A GIVEN ROW
			if	( ( !StringUtil.isNullOrEmpty(otTask.getDesc()) ) 
					&& ( otTask.getEstHours() == null )
				) {
				taskErrorCount++;
				Object[] params = { Integer.valueOf(taskErrorCount) };
				String errMsgText = ErrorMessages.getInstance().getMessage(ErrorMessages.OT_MISSING_EST_HOURS_ROW, params);
				FacesMessage facesMsg = new FacesMessage(errMsgText, errMsgText);
				facesMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
				FacesContext.getCurrentInstance().addMessage(null, facesMsg);
				otTask.setEstHoursError(true);
				otTask.setErrorRowNbr(taskErrorCount);
				errorCount++;		
			}
			// CHECK FOR A NEGATIVE NUMBER FOR ESTIMATED HOURS
			if	( (otTask.getEstHours() != null)
					&& ( OTItem.MIN_OT_HOURS.compareTo(otTask.getEstHours()) > 0) ) {
				taskErrorCount++;				
				Object[] params = { Integer.valueOf(taskErrorCount)};
				String errMsgText = ErrorMessages.getInstance().getMessage(ErrorMessages.OT_MINIMUM_TIME_FOR_TASK, params);
				FacesMessage facesMsg = new FacesMessage(errMsgText, errMsgText);
				facesMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
				FacesContext.getCurrentInstance().addMessage(null, facesMsg);
				otTask.setEstHoursError(true);
				otTask.setErrorRowNbr(taskErrorCount);
				errorCount++;		
			}			

			// CHECK FOR EMPTY TASK DESCRIPTION FOR A GIVEN ROW
			if	( ( StringUtil.isNullOrEmpty(otTask.getDesc()) ) 
					&& ( otTask.getEstHours() != null )
				) {
				taskErrorCount++;				
				Object[] params = { Integer.valueOf(taskErrorCount)};
				String errMsgText = ErrorMessages.getInstance().getMessage(ErrorMessages.OT_MISSING_TASK_DESC_ROW, params);
				FacesMessage facesMsg = new FacesMessage(errMsgText, errMsgText);
				facesMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
				FacesContext.getCurrentInstance().addMessage(null, facesMsg);
				otTask.setDescError(true);
				otTask.setErrorRowNbr(taskErrorCount);
				errorCount++;		
			}
		}
			
		if ( taskErrorCount == 0) {
			this.taskList.get(0).setRenderable(true);
		}
		
		if ( errorCount > 0) {
			throw new ValidatorException(new FacesMessage());
		}
	}		
	public boolean isPayPeriodSelected() {
		try {
			return (Integer.parseInt(this.selectedPayPeriod) > 0);	
		} catch (NumberFormatException ignore) {
			return false;
		}
	}	
	private boolean isTypeSelected() {
		return ( !this.selectedType.equals(OTType.CodeValues.UNSELECTED) );
	}	
	private boolean isApproverSelected() {
		try {
			return (Integer.parseInt(this.selectedApprover) > 0);	
		} catch (NumberFormatException ignore) {
			return false;
		}
	}
	
	private AlohaUser getApprover() {
		return (this.findApprover(Long.valueOf(this.selectedApprover)));
	}	
	
	private AlohaUser findApprover(long userId) {
		for ( AlohaUser approver: this.dbApprovers) {
			if ( approver.getUserId() == userId) {
				return approver;
			}
		}
		return null;
	}	
	
	/*****************************************
	 * SETTERS
	 *****************************************/
	
	public void setSelectedPayPeriod(String selectedPayPeriod) {
		this.selectedPayPeriod = selectedPayPeriod;
	}

	public void setSelectedType(String selectedType) {
		this.selectedType = selectedType;
	}
	public void setSelectedApprover(String selectedApprover) {
		this.selectedApprover = selectedApprover;
	}

	public void setSubmitterRemarks(String submitterRemarks) {
		this.submitterRemarks = submitterRemarks;
	}
	

	public void setSupervisorRemarks(String supervisorRemarks) {
		this.supervisorRemarks = supervisorRemarks;
	}
	public void setTaskListSize(int arg) {
		// do nothing (keeping JSF happy)
	}
	/*****************************************
	 * GETTERS
	 *****************************************/

	public String getSelectedPayPeriod() {
		return selectedPayPeriod;
	}

	public String getSelectedType() {
		return selectedType;
	}
	public String getSelectedApprover() {
		return selectedApprover;
	}

	public String getSubmitterRemarks() {
		return submitterRemarks;
	}

	public String getSupervisorRemarks() {
		return supervisorRemarks;
	}
	public List<OTTask> getTaskList() {
		return taskList;
	}
	public BigDecimal getTotalOvertimeHours() {
		return totalOvertimeHours;
	}	
	public AlohaUser getSubmitter() {
		return this.userMB.getUser();
	}
	public Map<String, Object> getApprovers() {
		return approvers;
	}	
	public int getTaskListSize() {
		return this.taskList.size();
	}
	public String getUnselectedType() {
		return OTType.CodeValues.UNSELECTED;
	}
	public List<KeyValuePair> getOtBalances() {
		return otBalances;
	}
	public int getOtBalanceCount() {
		return ( this.otBalances.size());
	}	
	public boolean isEmployeeSelected() {
		return (this.getEmployee() != null);
	}
	public String getSelectedPayPeriodValue() {
		String value = null;
		if ( this.isPayPeriodSelected()) {
			OTPayPeriod ppObj = this.otUtilMB.getOTPayPeriod(this.selectedPayPeriod);
			if ( ppObj != null) {
				value = ppObj.getLabel();
			}
		}
		return value;
	}
}
