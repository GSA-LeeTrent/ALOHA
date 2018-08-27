package gov.gsa.ocfo.aloha.web.mb.leave;

import gov.gsa.ocfo.aloha.ejb.UserEJB;
import gov.gsa.ocfo.aloha.exception.AlohaServerException;
import gov.gsa.ocfo.aloha.exception.AuthorizationException;
import gov.gsa.ocfo.aloha.model.KeyValuePair;
import gov.gsa.ocfo.aloha.model.PayPeriod;
import gov.gsa.ocfo.aloha.model.entity.AlohaUser;
import gov.gsa.ocfo.aloha.model.entity.AlohaUserPref;
import gov.gsa.ocfo.aloha.model.entity.leave.LeaveDetail;
import gov.gsa.ocfo.aloha.model.entity.leave.LeaveHeader;
import gov.gsa.ocfo.aloha.model.entity.leave.LeaveStatus;
import gov.gsa.ocfo.aloha.model.entity.leave.LeaveStatusTransition;
import gov.gsa.ocfo.aloha.util.StopWatch;
import gov.gsa.ocfo.aloha.util.StringUtil;
import gov.gsa.ocfo.aloha.web.enums.LRMode;
import gov.gsa.ocfo.aloha.web.mb.UserMB;
import gov.gsa.ocfo.aloha.web.security.NavigationOutcomes;
import gov.gsa.ocfo.aloha.web.util.AlohaConstants;
import gov.gsa.ocfo.aloha.web.util.ErrorMessages;
import gov.gsa.ocfo.aloha.web.util.FacesContextUtil;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;

@ManagedBean(name="lrCreateMB")
@SessionScoped
public class LRCreateMB extends LRAbstractMB {
	private static final long serialVersionUID = -2676889556401567493L;

	@ManagedProperty(value="#{userMB}")
	private UserMB userMB;		
	
	private LeaveHeader leaveRequest;
	
	// PAY PERIOD
	private String selectedPayPeriod;
	private String selectedPayPeriodHidden;
	private String selectedPayPeriodDateRange;
	
	// APPROVER
	private String selectedApprover;
	private List<AlohaUser> dbApprovers = new ArrayList<AlohaUser>();
	private Map<String, Object> approvers = new TreeMap<String, Object>();
	//SAK 20120117 Set default approver if available
	@EJB private UserEJB userEJB;
	private AlohaUserPref userPref;
	
	
	// EMPLOYEE
	private String selectedEmployee;
	private List<AlohaUser> dbEmployees = new ArrayList<AlohaUser>();
	private Map<String, Object> employees = new TreeMap<String, Object>();
	private String selectedEmployeeHidden;

	// LEAVE BALANCES
	private List<KeyValuePair> leaveBalances = new ArrayList<KeyValuePair>();
	
	private boolean renderLeaveRequestForm;
	//SAK added to disable PP dd and btn
	private boolean ppDisabled = false;
	
	// METHOD REQUIRED BY ABSTRACT SUPERCLASS
	public AlohaUser getEmployee() {
		if ( this.isSubmitOwn()) {
			return this.userMB.getUser();
		} else if (!StringUtil.isNullOrEmpty(this.selectedEmployee)){
			return this.findEmployee(Long.valueOf(this.selectedEmployee));	
		} else {
			return null;
		}
	}
	// METHOD REQUIRED BY ABSTRACT SUPERCLASS
	public AlohaUser getSubmitter() {
		return this.userMB.getUser();
	}
	// METHOD REQUIRED BY ABSTRACT SUPERCLASS
	protected AlohaUser getApprover() {
		return (this.findApprover(Long.valueOf(this.selectedApprover)));
	}
	
	@PostConstruct
	public String initCreate() {
		try {
			super.init();
			this.initState();
			this.checkAccessRights();
			return NavigationOutcomes.LR_CREATE;
		} catch (AuthorizationException ae) {
			return NavigationOutcomes.UNAUTHORIZED;
		} catch (NumberFormatException nfe) {
			return NavigationOutcomes.SERVER_ERROR;
		} catch (AlohaServerException e) {
			return NavigationOutcomes.SERVER_ERROR;		
		}
	}	
	protected void initState() throws AlohaServerException, AuthorizationException {
		this.leaveRequest = null;
		this.renderLeaveRequestForm = false;
		
		// PAY PERIOD
		PayPeriod currentPP = this.payPeriodMB.findCurrentPayPeriod();
		if ( currentPP != null) {
			this.selectedPayPeriod = currentPP.getValue();
			this.selectedPayPeriodHidden = selectedPayPeriod;
		} else {
			this.selectedPayPeriod = null;			
			this.selectedPayPeriodHidden = null;
		}		
		
		// APPROVER
		this.dbApprovers.clear();
		this.approvers.clear();
		this.selectedApprover = null;

		// EMPLOYEES
		this.dbEmployees.clear();
		this.employees.clear();
		this.selectedEmployee = null;

		// LEAVE BALANCES
		this.leaveBalances.clear();
		
		// EMPLOYEEE SCHEDULE 
		this.employeeSchedule = null;
		this.totalLeaveHours = BigDecimal.ZERO;
		
		// REMARKS
		this.submitterRemarks = null;
		
		this.ppDisabled = false;
		
		if ( this.isSubmitOwn()) {
			this.initStateForSubmitOwn();
		} else if ( this.isOnBehalfOf()) {
			this.initStateForOnBehalfOf();
		} else {
			Object[] params = { this.userMB.getFullName() };
			String errMsg = ErrorMessages.getInstance().getMessage(ErrorMessages.LR_CREATE_UNAUTHORIZED, params);
			FacesContextUtil.getHttpSession().setAttribute(AlohaConstants.UNAUTHORIZED_MESSAGE, errMsg);
			throw new AuthorizationException(errMsg);
		}

	}
	private void clearState() {
		this.lrMode = null;
		this.leaveRequest = null;
		this.renderLeaveRequestForm = false;
		
		// PAY PERIOD
		this.selectedPayPeriod = null;
		this.selectedPayPeriodHidden = null;
		
		// APPROVERS
		this.selectedApprover = null;
		
		if ( this.dbApprovers != null) {
			this.dbApprovers.clear();			
		}
		if ( this.approvers != null ) {
			this.approvers.clear();			
		}
		
		// EMPLOYEES
		this.selectedEmployee = null;
		
		if ( this.dbEmployees != null) {
			this.dbEmployees.clear();			
		}
		if ( this.employees != null) {
			this.employees.clear();			
		}
		if ( this.employeeSchedule != null) {
			this.employeeSchedule.clear();			
		}

		this.submitterRemarks = null;

		if ( this.leaveBalances != null) {
			this.leaveBalances.clear();			
		}

		this.totalLeaveHours = BigDecimal.ZERO;
	}
	
	private void initStateForSubmitOwn() throws AlohaServerException {
		this.initApprovers();
		this.initLeaveBalances();
	}

	private void initStateForOnBehalfOf() throws AlohaServerException {
		this.initEmployees();
	}
	private void initEmployees() throws AlohaServerException {
		this.dbEmployees = this.employeeEJB.getOnBehalfOfEmployees(this.userMB.getUserId());
		for ( AlohaUser employee : this.dbEmployees) {
			this.employees.put(employee.getLabel(), employee.getValue());
		}
	}
	private void initApprovers() throws AlohaServerException {
		if ( this.getEmployee() != null) {
			this.dbApprovers = this.employeeEJB.getLeaveApprovers(this.getEmployee().getUserId());
			for (AlohaUser approver : this.dbApprovers) {
				this.approvers.put(approver.getLabel(), approver.getValue());
			}
		}
	}		
	
	private void checkAccessRights() throws AuthorizationException {
		if ( (!this.isSubmitOwn()) && (!this.isOnBehalfOf()) ) {
			Object[] params = { this.userMB.getFullName() };
			String errMsg = ErrorMessages.getInstance().getMessage(ErrorMessages.LR_CREATE_UNAUTHORIZED, params);
			FacesContextUtil.getHttpSession().setAttribute(AlohaConstants.UNAUTHORIZED_MESSAGE, errMsg);
			throw new AuthorizationException(errMsg);
		}
	}
	public boolean isSubmitOwn() {
		if ( this.lrMode == null) {
			FacesContextUtil.callHome();
		}
		return ( (this.lrMode != null) && (this.lrMode.equals(LRMode.SUBMIT_OWN))  && (this.userMB.isSubmitOwn()) );
	}
	public boolean isOnBehalfOf() {
		if ( this.lrMode == null) {
			FacesContextUtil.callHome();
		}
		return ( (this.lrMode != null) && (this.lrMode.equals(LRMode.ON_BEHALF_OF)) && (this.userMB.isOnBehalfOf()) );
	}
	private AlohaUser findEmployee(long userId) {
		for ( AlohaUser employee: this.dbEmployees) {
			if ( employee.getUserId() == userId) {
				return employee;
			}
		}
		return null;
	}
	private AlohaUser findApprover(long userId) {
		for ( AlohaUser approver: this.dbApprovers) {
			if ( approver.getUserId() == userId) {
				return approver;
			}
		}
		return null;
	}
	public void setUserMB(UserMB userMB) {
		this.userMB = userMB;
	}
	public String getSelectedPayPeriod() {
		return selectedPayPeriod;
	}
	public String getSelectedPayPeriodDateRange() {
		return selectedPayPeriodDateRange;
	}
	public void setSelectedPayPeriod(String selectedPayPeriod) {
		this.selectedPayPeriod = selectedPayPeriod;
	}
	public String getSelectedPayPeriodHidden() {
		return selectedPayPeriodHidden;
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
	public Map<String, Object> getEmployees() {
		return employees;
	}
	public String getSelectedEmployee() {
		return selectedEmployee;
	}
	public void setSelectedEmployee(String selectedEmployee) {
		this.selectedEmployee = selectedEmployee;
	}
	public String getSelectedEmployeeHidden() {
		return selectedEmployeeHidden;
	}
	public boolean isPayPeriodSelected() {
		try {
			return (Integer.parseInt(this.selectedPayPeriod) > 0);	
		} catch (NumberFormatException ignore) {
			return false;
		}
	}	
	public boolean isEmployeeSelected() {
		return (this.getEmployee() != null);
	}	
	
	public String onSelectedEmployeeAndPayPeriod() {
		try{
			if ( (this.isPayPeriodSelected()) && (this.isEmployeeSelected()) ) {
				this.renderLeaveRequestForm = true;
				this.checkAccessRights();
				this.employeeSchedule = this.employeeEJB.getPayPeriodSchedule(Long.valueOf(this.selectedEmployee), this.selectedPayPeriod);
				this.selectedPayPeriodHidden = this.selectedPayPeriod;
				//2012-10-02 JJM 48969: Get Leave Types based on pp start 
				SimpleDateFormat formatEffDate = new SimpleDateFormat("yyyyMMdd");
				Date startDate = formatEffDate.parse(selectedPayPeriodHidden);
				this.leaveTypeMB.setLeaveTypes(this.leaveTypeMB.getLeaveTypesEffOBO(startDate));
				this.assignPayPeriodDateRange();
				this.initApprovers();
				this.initLeaveBalances();
				this.ppDisabled = true;
				//SAK Default Approver
				this.getUserPref();
				return NavigationOutcomes.LR_CREATE;
			} else {
				if ( !this.isPayPeriodSelected()) {
					String errMsgText = ErrorMessages.getInstance().getMessage(ErrorMessages.PAY_PERIOD_REQUIRED);
					FacesMessage facesMsg = new FacesMessage(errMsgText, errMsgText);
					facesMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
					FacesContext.getCurrentInstance().addMessage(null, facesMsg);
				}
				if ( !this.isEmployeeSelected()) {
					String errMsgText = ErrorMessages.getInstance().getMessage(ErrorMessages.EMPLOYEE_REQUIRED);
					FacesMessage facesMsg = new FacesMessage(errMsgText, errMsgText);
					facesMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
					FacesContext.getCurrentInstance().addMessage(null, facesMsg);
				}
				return null;
			}
		} catch (AuthorizationException ae) {
			return NavigationOutcomes.UNAUTHORIZED;
		} catch (Exception e) {
			e.printStackTrace();
			return NavigationOutcomes.SERVER_ERROR;
		}
	}	
	public String onSelectedPayPeriod() {
		try{
			if ( this.isPayPeriodSelected()) {
				this.renderLeaveRequestForm = true;
				this.checkAccessRights();
				this.employeeSchedule = this.employeeEJB.getPayPeriodSchedule(this.getEmployee().getUserId(), this.selectedPayPeriod);
				//this.debug(this.employeeSchedule);
				this.selectedPayPeriodHidden = this.selectedPayPeriod;
				//2012-10-02 JJM 48969: Get Leave Types based on pp start 
				SimpleDateFormat formatEffDate = new SimpleDateFormat("yyyyMMdd");
				Date startDate = formatEffDate.parse(selectedPayPeriodHidden);
				this.leaveTypeMB.setLeaveTypes(this.leaveTypeMB.getLeaveTypesEff(startDate));
				this.assignPayPeriodDateRange();
				this.ppDisabled = true;
				//SAK Default Approver
				this.getUserPref();
				return NavigationOutcomes.LR_CREATE;
			} else {
					String errMsgText = ErrorMessages.getInstance().getMessage(ErrorMessages.PAY_PERIOD_REQUIRED);
					FacesMessage facesMsg = new FacesMessage(errMsgText, errMsgText);
					facesMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
					FacesContext.getCurrentInstance().addMessage(null, facesMsg);
					return null;
				}			
		} catch (AuthorizationException ae) {
			return NavigationOutcomes.UNAUTHORIZED;
		} catch (AlohaServerException se) {
			return NavigationOutcomes.SERVER_ERROR;
		} catch (Exception e) {
			e.printStackTrace();
			return NavigationOutcomes.SERVER_ERROR;
		}
	}	
	private void assignPayPeriodDateRange() throws ParseException {
		PayPeriod selectedPayPeriodObj = this.payPeriodMB.getPayPeriodForStartDate(this.selectedPayPeriod);
		SimpleDateFormat sdf = new SimpleDateFormat(PayPeriod.LABEL_FORMAT);
		StringBuilder sb = new StringBuilder();
		sb.append(sdf.format(selectedPayPeriodObj.getFromDate()));
		sb.append(" - ");
		sb.append(sdf.format(selectedPayPeriodObj.getToDate()));
		this.selectedPayPeriodDateRange = sb.toString();
	}
	public String onSubmit() throws ValidatorException{
		try{
			this.checkAccessRights();
			this.refreshEmployeeSchedule();
			this.doValidation(this.payPeriodMB.getPayPeriodForStartDate(this.selectedPayPeriod));
			this.leaveRequest = this.createLeaveRequest(LeaveStatus.CodeValues.SUBMITTED, LeaveStatusTransition.ActionCodeValues.NONE_TO_SUBMIT);
			return NavigationOutcomes.LR_CREATE_PENDING;
		} catch (AuthorizationException ae) {
			return NavigationOutcomes.UNAUTHORIZED;
		}  catch (IllegalStateException ise) {
			return NavigationOutcomes.USER_ERROR;
		} catch (ValidatorException ve) {
			return NavigationOutcomes.LR_CREATE;
		} catch (AlohaServerException ase) {
			return NavigationOutcomes.SERVER_ERROR;
		} catch (Exception e) {
			e.printStackTrace();
			return NavigationOutcomes.SERVER_ERROR;
		}		
	}
	public String onCancel() {
		this.clearState();
		return NavigationOutcomes.HOME_PAGE;
	}
	public String onChange() {
		try{
			this.leaveRequest = null;
			this.checkAccessRights();
			this.refreshEmployeeSchedule();
			return NavigationOutcomes.LR_CREATE;
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
		if ( this.leaveRequest == null) {
			String errMsg = "LeaveHeader in " + this.getClass().getName() + ".checkIfInLegalState() is NULL. Cannot continue. AlohaUser is: " + this.userMB.getUser();
			illegalStateException = new IllegalStateException(errMsg);
			illegalStateException.printStackTrace();
			throw illegalStateException;
		}
	}
	public String onConfirm() {
		try{
			this.checkIfInLegalState();
			this.checkAccessRights();
			
			StopWatch stopWatch = new StopWatch();
			stopWatch.start();
			this.leaveRequestEJB.saveLeaveRequest(this.leaveRequest);
			stopWatch.stop();
			System.out.println("ELAPSED TIME (Create Leave Request): " + stopWatch.getElapsedTime() + " ms");
			
			FacesContextUtil.getHttpSession().setAttribute(AlohaConstants.NEWLY_CREATED_LEAVE_REQUEST, this.leaveRequest.getLeaveDetails().get(0));
			FacesContextUtil.getHttpSession().setAttribute(AlohaConstants.LR_MODE_ON_LAST_CREATE, this.getMode());
			this.clearState();
			return NavigationOutcomes.LR_CREATE_CONFIRM;
		} catch (IllegalStateException ise) {
			ise.printStackTrace();
			return NavigationOutcomes.SERVER_ERROR;	
		} catch (AuthorizationException ae) {
			return NavigationOutcomes.UNAUTHORIZED;
		} catch (AlohaServerException ase) {
			if ( ase.getExceptionType() == AlohaServerException.ExceptionType.OPTIMISTIC_LOCK) {
				Object[] errorParams = {Long.valueOf(this.leaveRequest.getId()).toString()};
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
	public LeaveHeader getLeaveRequest() {
		return this.leaveRequest;
	}
	public LeaveDetail getLeaveDetail() {
		if 	( (this.leaveRequest != null)
				&& (!this.leaveRequest.getLeaveDetails().isEmpty()) 
			) {
			return this.leaveRequest.getLeaveDetails().get(0);			
		} else {
			return null;
		}
	}
	public String getToBeDetermined() {
		return AlohaConstants.ABRV_TO_BE_DETERMINED;
	}
	public String getPendingStatus() {
		return AlohaConstants.STATUS_PENDING;
	}
	public boolean isRenderLeaveRequestForm() {
		return renderLeaveRequestForm;
	}
	public boolean isPpDisabled() {
		return ppDisabled;
	}
	public void setPpDisabled(boolean ppDisabled) {
		this.ppDisabled = ppDisabled;
	}
	public void setUserPref(AlohaUserPref userPref) {
		this.userPref = userPref;
	}
	public AlohaUserPref getUserPref() throws AlohaServerException {
		if (this.isSubmitOwn()) {
			userPref = this.userEJB.getUserPref(userMB.getUserId());
			if (userPref == null) {
				userPref = new AlohaUserPref();
				userPref.setUserId(userMB.getUserId());
			} else {
				if (userPref.getDefaultApproverUserId() != null) {
					this.selectedApprover = userPref.getDefaultApproverUserId().toString();	
				}
			}
		} else if (this.isOnBehalfOf()) {
			userPref = this.userEJB.getUserPref(Long.parseLong(this.selectedEmployee));
			if (userPref == null) {
				userPref = new AlohaUserPref();
				userPref.setUserId(Long.parseLong(this.selectedEmployee));
			} else {
				if (userPref.getDefaultApproverUserId() != null) {
					this.selectedApprover = userPref.getDefaultApproverUserId().toString();	
				}
			}			
		}
	
		return userPref;
	}
}