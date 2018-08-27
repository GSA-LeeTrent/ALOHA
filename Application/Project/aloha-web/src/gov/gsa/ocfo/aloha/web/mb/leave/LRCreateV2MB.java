package gov.gsa.ocfo.aloha.web.mb.leave;

import gov.gsa.ocfo.aloha.ejb.UserEJB;
import gov.gsa.ocfo.aloha.ejb.leave.EmployeeEJB;
import gov.gsa.ocfo.aloha.ejb.leave.LeaveRequestEJB;
import gov.gsa.ocfo.aloha.exception.AlohaServerException;
import gov.gsa.ocfo.aloha.exception.AlohaValidationException;
import gov.gsa.ocfo.aloha.exception.AuthorizationException;
import gov.gsa.ocfo.aloha.model.KeyValuePair;
import gov.gsa.ocfo.aloha.model.PayPeriod;
import gov.gsa.ocfo.aloha.model.PayPeriodSchedule;
import gov.gsa.ocfo.aloha.model.entity.AlohaUser;
import gov.gsa.ocfo.aloha.model.entity.AlohaUserPref;
import gov.gsa.ocfo.aloha.model.entity.AuditTrail;
import gov.gsa.ocfo.aloha.model.entity.leave.LeaveDetail;
import gov.gsa.ocfo.aloha.model.entity.leave.LeaveHeader;
import gov.gsa.ocfo.aloha.model.entity.leave.LeaveHistory;
import gov.gsa.ocfo.aloha.model.entity.leave.LeaveItem;
import gov.gsa.ocfo.aloha.model.entity.leave.LeaveStatus;
import gov.gsa.ocfo.aloha.model.entity.leave.LeaveStatusTransition;
import gov.gsa.ocfo.aloha.model.leave.DisabledVetLeaveInfo;
import gov.gsa.ocfo.aloha.util.StringUtil;
import gov.gsa.ocfo.aloha.web.enums.LRMode;
import gov.gsa.ocfo.aloha.web.mb.PayPeriodMB;
import gov.gsa.ocfo.aloha.web.mb.UserMB;
import gov.gsa.ocfo.aloha.web.model.leave.LeaveViewItem;
import gov.gsa.ocfo.aloha.web.util.AlohaConstants;
import gov.gsa.ocfo.aloha.web.util.AlohaURIs;
import gov.gsa.ocfo.aloha.web.util.DateUtil;
import gov.gsa.ocfo.aloha.web.util.ErrorMessages;
import gov.gsa.ocfo.aloha.web.util.FacesContextUtil;
import gov.gsa.ocfo.aloha.web.validator.leave.v2.LRAWSValidatorV2;
import gov.gsa.ocfo.aloha.web.validator.leave.v2.LRNonAWSValidatorV2;
import gov.gsa.ocfo.aloha.web.validator.leave.v2.LRValidatorV2;

import java.io.IOException;
import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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
import javax.faces.model.SelectItem;
import javax.faces.validator.ValidatorException;

@ManagedBean(name=LRCreateV2MB.MANAGED_BEAN_NAME, eager=false)
@ViewScoped
public class LRCreateV2MB extends LRBaseV2MB implements Serializable{
	private static final long serialVersionUID = 4362697303453926006L;
	public static final String MANAGED_BEAN_NAME = "lrCreateV2MB";
	
	// ABSTRACT METHOD IN SUPERCLASS / REQUIRED TO BE IMPLEMENTED IN SUBLCASS
	protected LeaveTypeMB getLeaveTypeMB() {
		return leaveTypeMB;
	}
	// ABSTRACT METHOD IN SUPERCLASS / REQUIRED TO BE IMPLEMENTED IN SUBLCASS
	protected List<PayPeriodSchedule> getPayPeriodScheduleList() throws AlohaServerException {
		return this.getLrPayPeriod().getPayPeriodScheduleList();
	}
	// ABSTRACT METHOD IN SUPERCLASS / REQUIRED TO BE IMPLEMENTED IN SUBLCASS
	protected DisabledVetLeaveInfo retrieveDisabledVetLeaveInfo() throws AlohaServerException {
		return this.employeeEJB.getDisabledVetLeaveInfo(this.getLrEmployee().getEmployee().getUserId());
	}	
	
	@EJB 
	private UserEJB userEJB;

	@EJB
	private EmployeeEJB employeeEJB;

	@EJB
	private LeaveRequestEJB leaveRequestEJB;
	
	@ManagedProperty(value="#{userMB}")
	private UserMB userMB;
	public void setUserMB(UserMB userMB) {
		this.userMB = userMB;
	}
	
	@ManagedProperty(value="#{payPeriodMB}")
	protected PayPeriodMB payPeriodMB;		
	public void setPayPeriodMB(PayPeriodMB payPeriodMB) {
		this.payPeriodMB = payPeriodMB;
	}
	
	@ManagedProperty(value="#{leaveTypeMB}")
	protected LeaveTypeMB leaveTypeMB;
	public void setLeaveTypeMB(LeaveTypeMB leaveTypeMB) {
		this.leaveTypeMB = leaveTypeMB;
	}

	@ManagedProperty(value="#{leaveStatusMB}")
	protected LeaveStatusMB leaveStatusMB;
	public void setLeaveStatusMB(LeaveStatusMB leaveStatusMB) {
		this.leaveStatusMB = leaveStatusMB;
	}

	@ManagedProperty(value="#{leaveViewItemMB}")
	protected LeaveViewItemMB leaveViewItemMB;
	public void setLeaveViewItemMB(LeaveViewItemMB leaveViewItemMB) {
		this.leaveViewItemMB = leaveViewItemMB;
	}
	
	// INSTANCE MEMBERS
	private LREmployee lrEmployee; // helper inner class
	private LRPayPeriod lrPayPeriod; // helper inner class
	private LRAWSValidator lrAWSValidator; // helper inner class
	protected LRItems lrItems; // helper inner class
	protected LRItems getLrItems() {
		
		if  ( this.lrItems == null) {
			this.lrItems = new LRItems();
		}
		return this.lrItems;
	}
	
	private String submitterCreationRemarks;
	private LeaveHeader leaveRequest;
	
	// NAVIGATION
	private LRMode lrMode;
	private boolean pageOne;
	private boolean pageTwo;
	private boolean pageThree;
	private boolean pageFour;
	
	private String confirmationMessage;
		
	@PreDestroy
	public void cleanup() {
		super.cleanup();
		
		if ( this.lrEmployee != null) {
			this.lrEmployee.cleanup();
			this.lrEmployee = null;
		}
		
		if ( this.lrPayPeriod != null) {
			this.lrPayPeriod.cleanup();
			this.lrPayPeriod = null;
		}
		
		if ( this.lrItems != null) {
			this.lrItems.cleanup();
			this.lrItems = null;
		}

		this.lrMode = null;
		this.pageOne = false;
		this.pageTwo = false;
		this.pageThree = false;
		this.pageFour = false;
		
		this.submitterCreationRemarks = null;
		this.confirmationMessage = null;
		this.leaveRequest = null;
	}
	
	@PostConstruct
	public void init() {
		try {
			super.init();
			
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
			
			this.checkAccessRights();
			
			this.pageOne = true;
			this.pageTwo = false;
			this.pageThree = false;
			this.pageFour = false;
			
		} catch (AuthorizationException ae) {
			try {
				FacesContext.getCurrentInstance().getExternalContext().redirect(AlohaURIs.UNAUTHORIZED);
			} catch (IOException ignore) {}
		} catch (IllegalStateException ise) {
			try {
				FacesContext.getCurrentInstance().getExternalContext().redirect(AlohaURIs.ILLEGAL_OPERATION);
			} catch (IOException ignore) {}
		} catch (Throwable t) {
			try {
				t.printStackTrace();
				FacesContext.getCurrentInstance().getExternalContext().redirect(AlohaURIs.SERVER_ERROR);
			} catch (IOException ignore) {}
		}
	}
	
	private void checkAccessRights() throws AuthorizationException {
		if ( (!userMB.isSubmitOwn()) && (!userMB.isOnBehalfOf()) ) {
			Object[] params = { userMB.getFullName() };
			String errMsg = ErrorMessages.getInstance().getMessage(ErrorMessages.LR_CREATE_UNAUTHORIZED, params);
			FacesContextUtil.getHttpSession().setAttribute(AlohaConstants.UNAUTHORIZED_MESSAGE, errMsg);
			throw new AuthorizationException(errMsg);
		}
	}
	
	public String getMode() {
		return this.lrMode.getText();
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
	public boolean isPageFour() {
		return pageFour;
	}
	public boolean isInSubmitOwnMode() {
		return ( (this.lrMode != null) && (this.lrMode.equals(LRMode.SUBMIT_OWN)) && (this.userMB.isSubmitOwn()) );
	}
	public boolean isInOnBehalfOfMode() {
		return ( (this.lrMode != null) && (this.lrMode.equals(LRMode.ON_BEHALF_OF)) && (this.userMB.isOnBehalfOf()) );
	}
	
	public AlohaUser getSubmitter() {
		return this.userMB.getUser();
	}
	
	public String getSubmitterCreationRemarks() {
		return submitterCreationRemarks;
	}

	public void setSubmitterCreationRemarks(String submitterCreationRemarks) {
		this.submitterCreationRemarks = submitterCreationRemarks;
	}

	public void onReviewChanges() {
		
		try {
			this.performValidation();
			
			// If validation passes, take user to page 3
			this.pageOne = false;
			this.pageTwo = false;
			this.pageThree = true;
			this.pageFour = false;
		} catch(ValidatorException ve) {
			// Take user back to page 2 to fix validation errors
			this.pageOne = false;
			this.pageTwo = true; 
			this.pageThree = false;
			this.pageFour = false;
		} catch (AlohaServerException ase) {
			try {
				if ( this.userMB != null && this.userMB.getUser() != null) {
					System.out.println(this.userMB.getUser());
				}				
				ase.printStackTrace();
				FacesContext.getCurrentInstance().getExternalContext().redirect(AlohaURIs.SERVER_ERROR);
			} catch (IOException ignore) {}
		} catch (ParseException pe) {
			try {
				pe.printStackTrace();
				FacesContext.getCurrentInstance().getExternalContext().redirect(AlohaURIs.SERVER_ERROR);
			} catch (IOException ignore) {}
		}
	}
	private void performValidation() throws ValidatorException, AlohaServerException, ParseException {
		getLrItems().performValidation();
		this.getLRAWSValidator().performValidation();
	}	
	public void onMakeRevisions() {
		super.onMakeRevisions();
		this.pageOne = false;
		this.pageTwo = true;
		this.pageThree = false;
		this.pageFour = false;
	}
	public void onConfirm() {
		try {
			
			//System.out.println("BEGIN: onConfirm()");
			
			////////////////////////////////////////////////////////////
			// Check to see if disabled veteran leave has been selected,
			// and if so, make sure that the certification check box
			// was checked
			////////////////////////////////////////////////////////////			
			this.validateDisabledVetCertification();
			////////////////////////////////////////////////////////////
			
			this.leaveRequest = this.createLeaveRequest();
			this.leaveRequestEJB.saveLeaveRequest(this.leaveRequest);
			//System.out.println("this.leaveRequest.getId(): " + this.leaveRequest.getId());
			
			this.pageOne = false;
			this.pageTwo = false;
			this.pageThree = false;
			this.pageFour = true;

			//System.out.println("END: onConfirm()");
		
		} catch (ValidatorException valEx) {
			
			//System.out.println("BEGIN: ValidatorException");
			
			this.pageOne = false;
			this.pageTwo = false;
			this.pageThree = true;
			this.pageFour = false;			

			//System.out.println("END: ValidatorException");
			
		} catch (AlohaServerException e) {
			this.logException(e);
			try {
				FacesContext.getCurrentInstance().getExternalContext().redirect(AlohaURIs.SERVER_ERROR);
			} catch (IOException ignore) {}
		}
	}		
	public LeaveHeader getLeaveRequest() {
		return leaveRequest;
	}
	private LeaveHeader createLeaveRequest() throws AlohaServerException {
		// AUDIT TRAIL
		AuditTrail auditTrail = new AuditTrail();
		auditTrail.setUserLastUpdated(this.getSubmitter().getLoginName());
		auditTrail.setDateLastUpdated(new Date());
		
		// HEADER
		LeaveHeader leaveHeader = new LeaveHeader();
		leaveHeader.setAuditTrail(auditTrail);
		leaveHeader.setEmployee(this.getEmployee());
		
		// DETAIL
		LeaveDetail leaveDetail = this.createLeaveDetailUsingAuditTrail(auditTrail);
		leaveHeader.addLeaveDetail(leaveDetail);
		leaveDetail.setLeaveHeader(leaveHeader);
		leaveDetail.setSequence(leaveHeader.getLeaveDetails().size());
		
		return leaveHeader;
	}	
	private LeaveDetail createLeaveDetailUsingAuditTrail(AuditTrail auditTrail) throws AlohaServerException {
		
		// DETAIL
		LeaveDetail leaveDetail = new LeaveDetail();
		leaveDetail.setAuditTrail(auditTrail);
		leaveDetail.setSubmitter(this.getSubmitter());
		leaveDetail.setApprover(this.getSupervisor());
		leaveDetail.setPayPeriodStartDate(this.getLrPayPeriod().getSelectedPayPeriod().getFromDate());
		leaveDetail.setPayPeriodEndDate(this.getLrPayPeriod().getSelectedPayPeriod().getToDate());
		
		LeaveStatus leaveStatus = this.leaveStatusMB.getLeaveStatus(LeaveStatus.CodeValues.SUBMITTED);
		leaveDetail.setLeaveStatus(leaveStatus);
				
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
		
		// HISTORY
		LeaveStatusTransition leaveStatusTransition
			= this.leaveStatusMB.getLeaveStatusTransition(LeaveStatusTransition.ActionCodeValues.NONE_TO_SUBMIT);
		LeaveHistory leaveHistory = new LeaveHistory();
		leaveHistory.setAuditTrail(auditTrail);
		leaveHistory.setLeaveDetail(leaveDetail);
		leaveHistory.setLeaveStatusTransition(leaveStatusTransition);
		leaveHistory.setActionDatetime(new Date());
		leaveHistory.setActor(this.getSubmitter());
		if ( !StringUtil.isNullOrEmpty(this.getSubmitterCreationRemarks()) ) {
			leaveHistory.setRemarks(this.getSubmitterCreationRemarks());
		}
		
		leaveDetail.addLeaveHistory(leaveHistory);
		leaveDetail.setLeaveItems(this.createLeaveItemsForLeaveDetailUsingAuditTrail(leaveDetail, auditTrail));
		
		if ( leaveDetail.isHasDisabledVetLeaveItem() ) {
			
			if ( super.disabledVetCertified == true ) {
				leaveDetail.setDisabledVeteranCertified(true);
			} else {
				throw new AlohaServerException("Disabled veteran leave type selected but not certified.");
			}
		}
		
		return leaveDetail;
	}
	
	private List<LeaveItem>createLeaveItemsForLeaveDetailUsingAuditTrail(LeaveDetail leaveDetail, AuditTrail auditTrail) {
		List<LeaveItem> leaveItemList = new ArrayList<LeaveItem>();
		//this.getLrItems().getCollectedLeaveItems();
		
		for ( LeaveViewItem leaveViewItem : this.getLrItems().getCollectedLeaveItems()) {
			LeaveItem leaveItem = new LeaveItem();
			leaveItem.setLeaveDetail(leaveDetail);
			leaveItem.setAuditTrail(auditTrail);
			leaveItem.getAuditTrail().setUserLastUpdated(this.getSubmitter().getLoginName());
			
			leaveItem.setLeaveDate(leaveViewItem.getLeaveDate());
			leaveItem.setLeaveType(leaveViewItem.getLeaveType());
			leaveItem.setLeaveHours(leaveViewItem.getLeaveNumberOfHours());
			leaveItem.setStartTime(leaveViewItem.getLeaveStartTime());
			leaveItemList.add(leaveItem);
		}
		return leaveItemList;
	}
	
	public void onSelectedPayPeriod() {
		Date ppStartDate = getLrPayPeriod().getSelectedPayPeriod().getFromDate();
		leaveTypeMB.setLeaveTypes(leaveTypeMB.getLeaveTypesEff(ppStartDate));
		this.pageOne = false;
		this.pageTwo = true;
		this.pageThree = false;
		this.pageFour = false;
	}
	
	public void onSelectedEmployeeAndPayPeriod() {
		Date ppStartDate = getLrPayPeriod().getSelectedPayPeriod().getFromDate();
		leaveTypeMB.setLeaveTypes(leaveTypeMB.getLeaveTypesEffOBO(ppStartDate));
		this.pageOne = false;
		this.pageTwo = true;
		this.pageThree = false;
		this.pageFour = false;	
	}
	
	private void logException(Exception exc) {
		if ( exc != null && userMB != null) {
			System.out.println("AlohaUser: " + userMB.getFullName() + " / " + userMB.getFullName() 
					+ " / " + userMB.getEmailAddress());
			exc.printStackTrace();
		}
	}

	public String getConfirmationMessage() {
		return confirmationMessage;
	}
	
	private LREmployee getLrEmployee() {
		if (this.lrEmployee == null) {
			if ( this.isInSubmitOwnMode()) {
				this.lrEmployee = new LREmployeeImplForSubmitOwn();
			} else if ( this.isInOnBehalfOfMode()) {
				this.lrEmployee = new LREmployeeImplForOnBehalfOf();
			} else {
				this.lrEmployee = null;
			}
		}
		return this.lrEmployee;
	}
	
	private LRPayPeriod getLrPayPeriod() {
		
		if  ( this.lrPayPeriod == null) {
			this.lrPayPeriod = new LRPayPeriod();
		}
		return this.lrPayPeriod;
	}
	

	private LRAWSValidator getLRAWSValidator() {
		if ( this.lrAWSValidator== null) {
			this.lrAWSValidator = new LRAWSValidator();
		}
		return this.lrAWSValidator;
	}
	
	public void addLeaveViewItems() {
		try 
		{
			super.addLeaveViewItems();
			this.pageOne = false;
			this.pageTwo = true;
			this.pageThree = false;
			this.pageFour = false;
		} 
		catch(ValidatorException ignore) {
			// Take user back to page one to fix validation errors
			this.pageOne = false;
			this.pageTwo = true;
			this.pageThree = false;
			this.pageFour = false;
		} catch (Exception exc) {
			exc.printStackTrace();
			try {
				
				FacesContext.getCurrentInstance().getExternalContext().redirect(AlohaURIs.SERVER_ERROR);
			} catch (IOException ignore) {}
		}
	}
	public void editLeaveViewItem() {
		try 
		{
			super.editLeaveViewItem();
		} 
		catch(ValidatorException ignore) {
			// Take user back to page one to fix validation errors
			this.pageOne = false;
			this.pageTwo = true;
			this.pageThree = false;
			this.pageFour = false;
		} catch (Exception exc) {
			if ( this.userMB != null && this.userMB.getUser() != null) {
				System.out.println(this.userMB.getUser());
			}
			exc.printStackTrace();
			try {
				FacesContext.getCurrentInstance().getExternalContext().redirect(AlohaURIs.SERVER_ERROR);
			} catch (IOException ignore) {}
		}
	}	
	public void cancelEditLeaveViewItem() {
		super.cancelEditLeaveViewItem();
		this.pageOne = false;
		this.pageTwo = true;
		this.pageThree = false;		
		this.pageFour = false;
	}
	public void prepareLeaveViewItemForEdit() {
		super.prepareLeaveViewItemForEdit();
		this.pageOne = false;
		this.pageTwo = true;
		this.pageThree = false;
		this.pageFour = false;
	}
	public void removeNewLeaveItem() {
		super.removeNewLeaveItem();
		this.pageOne = false;
		this.pageTwo = true;
		this.pageThree = false;
		this.pageFour = false;
	}	

	public SelectItem[] getSelectItemsForDaysInSelectedPayPeriod() throws AlohaServerException {
		return this.getLrPayPeriod().getSelectItemsForDaysInSelectedPayPeriod();
	}
	public String getSelectedEmployeeUserIdAsString() {
		return this.getLrEmployee().getSelectedEmployeeUserIdAsString();
	}
	public void setSelectedEmployeeUserIdAsString(String selectedEmployeeUserIdAsString) {
		this.getLrEmployee().setSelectedEmployeeUserIdAsString(selectedEmployeeUserIdAsString);
	}
	public String getSelectedSupervisorUserIdAsString() {
		return this.getLrEmployee().getSelectedSupervisorUserIdAsString();
	}
	public void setSelectedSupervisorUserIdAsString(String selectedEmployeeUserIdAsString) {
		this.getLrEmployee().setSelectedSupervisorUserIdAsString(selectedEmployeeUserIdAsString);
	}
	public boolean isEmployeeSelected() {
		//return this.getLrEmployee().isEmployeeSelected();
		
		if ( this.getLrEmployee() != null ) {
			return this.getLrEmployee().isEmployeeSelected();
		}
		return false;
	}
	public AlohaUser getEmployee() {
		return this.getLrEmployee().getEmployee();
	}
	public Map<String, Object> getEmployees() {
		return this.getLrEmployee().getEmployees();
	}
	public AlohaUser getSupervisor() {
		return this.getLrEmployee().getSupervisor();
	}
	public Map<String, Object> getSupervisors() {
		return this.getLrEmployee().getSupervisors();
	}
	public List<KeyValuePair> getLeaveBalances() {
		return this.getLrEmployee().getLeaveBalances();
	}
	
	public String getSelectedPayPeriodValue() {
		return this.getLrPayPeriod().getSelectedPayPeriodValue();
	}
	public void setSelectedPayPeriodValue(String selectedPayPeriodValue) {
		this.getLrPayPeriod().setSelectedPayPeriodValue(selectedPayPeriodValue);
	}
	public String getSelectedPayPeriodLabel() {
		return this.getLrPayPeriod().getSelectedPayPeriodLabel();
	}
	public String getSelectedPayPeriodDateRange() {
		return this.getLrPayPeriod().getSelectedPayPeriodDateRange();
	}

	/****************************************************************************************************
	 *
	 * class LRPayPeriod
	 * @author LeeTTrent
	 *
	 ****************************************************************************************************/
	private class LRPayPeriod {
		private PayPeriod selectedPayPeriod;
		private SelectItem[] selectItemsForDaysInSelectedPayPeriod;
		private String selectedPayPeriodDateRange;
		private List<PayPeriodSchedule> payPeriodScheduleList;
		
		private void cleanup() {
			this.selectedPayPeriod = null;
			this.selectItemsForDaysInSelectedPayPeriod = null;
			this.selectedPayPeriodDateRange = null;
		}
		
		private PayPeriod getSelectedPayPeriod() {
			if ( this.selectedPayPeriod == null) {
				this.selectedPayPeriod = payPeriodMB.findCurrentPayPeriod();
			}
			return selectedPayPeriod;
		}
		
		private String getSelectedPayPeriodLabel() {
			return this.getSelectedPayPeriod().getLabel();
		}
		
		private String getSelectedPayPeriodValue() {
			return this.getSelectedPayPeriod().getValue();
		}
		
		private void setSelectedPayPeriodValue(String selectedPayPeriodValue) {
			try {
				this.selectedPayPeriod = payPeriodMB.getPayPeriodForStartDate(selectedPayPeriodValue);
			} catch (ParseException e) {
				logException(e);
			}
		}
		private String getSelectedPayPeriodDateRange() {
			if ( this.selectedPayPeriodDateRange == null) {
				SimpleDateFormat sdf = new SimpleDateFormat(PayPeriod.LABEL_FORMAT);
				StringBuilder sb = new StringBuilder();
				sb.append(sdf.format(this.getSelectedPayPeriod().getFromDate()));
				sb.append(" - ");
				sb.append(sdf.format(this.getSelectedPayPeriod().getToDate()));
				this.selectedPayPeriodDateRange = sb.toString();
			}
			return this.selectedPayPeriodDateRange;
		}

		private SelectItem[] getSelectItemsForDaysInSelectedPayPeriod() throws AlohaServerException {
			if ( this.selectItemsForDaysInSelectedPayPeriod == null) {
				if ( this.getSelectedPayPeriod() != null && getLrEmployee() != null) {
					this.selectItemsForDaysInSelectedPayPeriod = new SelectItem[14];
					List<PayPeriodSchedule> payPeriodScheduleList = this.getPayPeriodScheduleList();
					if (payPeriodScheduleList != null) {
						int rowIndex = 0;
						for ( PayPeriodSchedule ppSchedule: payPeriodScheduleList) {
							//System.out.println(ppSchedule);
							String dateValue = DateUtil.formatDate(ppSchedule.getCalendarDate(), DateUtil.DateFormats.YYYYMMDD);
							StringBuffer sb = new StringBuffer(DateUtil.formatDate(ppSchedule.getCalendarDate()));									
							sb.append(" - ");
							if ( ppSchedule.isHoliday()) {
								sb.append(ppSchedule.getHolidayDescription());
							} else {
								sb.append(ppSchedule.getDayOfWeek());
							}
							String dateLabel = sb.toString();
							this.selectItemsForDaysInSelectedPayPeriod[rowIndex++] = new SelectItem(dateValue, dateLabel);
						}
					}
				}
			}
			return selectItemsForDaysInSelectedPayPeriod;
		}		
		
		public List<PayPeriodSchedule> getPayPeriodScheduleList() throws AlohaServerException {
			if ( this.payPeriodScheduleList == null) {
				this.payPeriodScheduleList = employeeEJB.retrievePayPeriodScheduleForEmployeeAndPayPeriodStartDate
						(getLrEmployee().getEmployee().getUserId(), this.getSelectedPayPeriodValue());
			}
			return this.payPeriodScheduleList;
		}
	}
	
	/****************************************************************************************************
	 *
	 * interface LREmployee
	 * @author LeeTTrent
	 *
	  ****************************************************************************************************/
	private interface LREmployee {
		public void cleanup();
		public boolean isEmployeeSelected();
		public List<KeyValuePair> getLeaveBalances();
		public String getSelectedEmployeeUserIdAsString();
		public void setSelectedEmployeeUserIdAsString(String selectedEmployeeUserIdAsString);
		public String getSelectedSupervisorUserIdAsString();
		public void setSelectedSupervisorUserIdAsString(String selectedEmployeeUserIdAsString);
		public AlohaUser getEmployee();
		public Map<String, Object> getEmployees();
		public AlohaUser getSupervisor();
		public Map<String, Object> getSupervisors();
	}
	
	/****************************************************************************************************
	 *
	 * abstract implementation of LREmployee (includes 2 concrete method implementations)
	 * @author LeeTTrent
	 *
	 ****************************************************************************************************/
	private abstract class LREmployeeImpl implements LREmployee {
		
		// INSTANCE MEMBERS
		protected List<KeyValuePair> leaveBalances;
		protected AlohaUser employee;
		private String selectedSupervisorUserIdAsString;
		protected AlohaUser supervisor;
		
		// ABSTRACT METHODS (required to be implemented by subclasses)
		public abstract AlohaUser getEmployee();
		public abstract String getSelectedEmployeeUserIdAsString();
		public abstract void setSelectedEmployeeUserIdAsString(String selectedEmployeeUserIdAsString);
		public abstract Map<String, Object> getEmployees();

		public  void cleanup() {
			this.leaveBalances = null;
			this.employee = null;
			this.selectedSupervisorUserIdAsString = null;
			this.supervisor = null;
			
		}
		
		public boolean isEmployeeSelected() {
			return (this.getEmployee() != null);
		}

		public List<KeyValuePair> getLeaveBalances()  {
			if ( this.leaveBalances == null) {
				if ( this.getEmployee() != null) {
					try {
						leaveBalances = employeeEJB.getLeaveBalances(this.getEmployee().getUserId());
					} catch (AlohaServerException ase) {
						logException(ase);
					}			
				}				
			}
			return leaveBalances;
		}	
		
		public Map<String, Object> getSupervisors() {
			Map<String, Object> htmlSupervisors = null;

			if ( this.getEmployee() != null) {
				List<AlohaUser> dbSupervisors = this.getDbSupervisors();
				if ( dbSupervisors != null) {
					htmlSupervisors = new TreeMap<String, Object>();
					for (AlohaUser dbSupervisor : dbSupervisors) {
						htmlSupervisors.put(dbSupervisor.getLabel(), dbSupervisor.getValue());
					}
				}
			}
			return htmlSupervisors;
		}
		
		private List<AlohaUser> getDbSupervisors() {
			List<AlohaUser> dbSupervisors = null;
			if ( this.getEmployee() != null) {
				try {
					dbSupervisors = employeeEJB.getLeaveApprovers(this.getEmployee().getUserId());
				} catch (AlohaServerException ase) {
					logException(ase);
				}
			}
			return dbSupervisors;
		}
		private AlohaUser findSupervisor(long userId) {
			for ( AlohaUser dbSupervisor: this.getDbSupervisors()) {
				if ( dbSupervisor.getUserId() == userId) {
					return dbSupervisor;
				}
			}
			return null;
		}
		public AlohaUser getSupervisor() {
			if ( this.supervisor == null ) {
				if (!StringUtil.isNullOrEmpty(this.selectedSupervisorUserIdAsString)) {
					try {
						this.supervisor =  this.findSupervisor(Long.valueOf(this.selectedSupervisorUserIdAsString));
					} catch (NumberFormatException nfe) {
						logException(nfe);
					} catch (Exception e) {
						logException(e);
					}
				}				
			}
			return this.supervisor;
		}
		public String getSelectedSupervisorUserIdAsString() {
			if ( StringUtil.isNullOrEmpty(this.selectedSupervisorUserIdAsString)) {
				try {
					AlohaUserPref userPreferences = userEJB.getUserPref(this.getEmployee().getUserId());
					if ( userPreferences != null 
							&& userPreferences.getDefaultApproverUserId() != null ) {
						this.selectedSupervisorUserIdAsString = userPreferences.getDefaultApproverUserId().toString();
					}
				} catch (AlohaServerException e) {
					logException(e);
				}
			}
			return this.selectedSupervisorUserIdAsString;
		}
		public void setSelectedSupervisorUserIdAsString(
				String selectedSupervisorUserIdAsString) {
			this.selectedSupervisorUserIdAsString = selectedSupervisorUserIdAsString;
		}
	}
	/****************************************************************************************************
	 *
	 * Concrete implementation of LREmployee for SubmitOwn use case (extends LREmployeeImpl 
	 * @author LeeTTrent
	 *
	 ****************************************************************************************************/
	private class LREmployeeImplForSubmitOwn extends LREmployeeImpl  {
		@Override
		public void cleanup() {
			super.cleanup();
		}

		@Override
		public AlohaUser getEmployee() {
			if ( this.employee == null) {
				if (userMB != null) {
					this.employee = userMB.getUser();
				}
			}
			return this.employee;
		}
		
		@Override
		public String getSelectedEmployeeUserIdAsString() {
			throw new IllegalStateException("Method 'getSelectedEmployeeUserIdAsString()' not supported in " + this.getClass().getName());
		}

		@Override
		public void setSelectedEmployeeUserIdAsString(String selectedEmployeeUserIdAsString) {
			throw new IllegalStateException("Method 'setSelectedEmployeeUserIdAsString()' not supported in " + this.getClass().getName());
		}

		@Override
		public Map<String, Object> getEmployees() {
			throw new IllegalStateException("Method 'getEmployees()' not supported in " + this.getClass().getName());
		}
		
	}
	/****************************************************************************************************
	 *
	 * Concrete implementation of LREmployee for OnBehalfOf use case (extends LREmployeeImpl 
	 * @author LeeTTrent
	 *
	 ****************************************************************************************************/

	private class LREmployeeImplForOnBehalfOf extends LREmployeeImpl {
		private String selectedEmployeeUserIdAsString;

		@Override
		public void cleanup() {
			super.cleanup();
			this.selectedEmployeeUserIdAsString = null;
		}

		@Override
		public String getSelectedEmployeeUserIdAsString() {
			return selectedEmployeeUserIdAsString;
		}

		@Override
		public void setSelectedEmployeeUserIdAsString(
				String selectedEmployeeUserIdAsString) {
			this.selectedEmployeeUserIdAsString = selectedEmployeeUserIdAsString;
			this.employee = null;
			this.setSelectedSupervisorUserIdAsString(null);
			this.supervisor = null;
			this.leaveBalances = null;
		}

		@Override
		public AlohaUser getEmployee() {
			if ( this.employee == null ) {
				if (!StringUtil.isNullOrEmpty(this.selectedEmployeeUserIdAsString)) {
					try {
						this.employee =  this.findEmployee(Long.valueOf(this.selectedEmployeeUserIdAsString));
					} catch (NumberFormatException nfe) {
						logException(nfe);
					} catch (Exception e) {
						logException(e);
					}
				}				
			}
			return this.employee;
		}
		
		@Override
		public Map<String, Object> getEmployees() {
			Map<String, Object> employees  = new TreeMap<String, Object>();
			for ( AlohaUser employee : this.getOnBehalfOfEmployees()) {
				employees.put(employee.getLabel(), employee.getValue());
			}	
			return employees;
		}
		
		private AlohaUser findEmployee(long userId) {
			for ( AlohaUser employee: this.getOnBehalfOfEmployees()) {
				if ( employee.getUserId() == userId) {
					return employee;
				}
			}
			return null;
		}
		private List<AlohaUser>getOnBehalfOfEmployees() {
			List<AlohaUser> onBehalfOfEmployees = null;
				try {
					onBehalfOfEmployees = employeeEJB.getOnBehalfOfEmployees(userMB.getUserId());
				} catch (AlohaServerException e) {
					if ( userMB != null) {
						//System.out.println("AlohaUser: " + userMB.getFullName() + " / " + userMB.getFullName() + " / " + userMB.getEmailAddress());
					}
					e.printStackTrace();
				}			
			return onBehalfOfEmployees;
		}
	}
	

	
	
	/****************************************************************************************************
	 *
	 * class LRAWSValidator
	 * @author LeeTTrent
	 *
	 ****************************************************************************************************/	
	private class LRAWSValidator {
		private void performValidation() throws AlohaServerException, ValidatorException  {
			
			PayPeriod selectedPayPeriod = lrPayPeriod.getSelectedPayPeriod();
			
			List<LeaveItem> priorLeaveItemsForPP = leaveRequestEJB.getPriorLeaveItemsForCreate
					(lrEmployee.getEmployee().getUserId(), selectedPayPeriod.getFromDate());
			
			//System.out.println("priorLeaveItemsForPP.size(): " + priorLeaveItemsForPP.size());

			for (LeaveItem leaveItem : priorLeaveItemsForPP) {
				System.out.println(leaveItem.getLeaveDetail().getLeaveStatus().getCode() );
			}
			
			try {
				LRValidatorV2 lrValV2 = null;
				///////////////////////////////////////////////////////////////////////////
				//////////////////////// IMPORTANT /////////////////////////////////////// 
				// The AWS property on AlohaUser is transient and is therefore unreliable.
				// As such, retrieve AWS Indicator directly from database
				///////////////////////////////////////////////////////////////////////////
				boolean employeeIsOnAws = employeeEJB.getAwsFlagForEmployee(lrEmployee.getEmployee().getUserId());
				//System.out.println("employeeIsOnAws: " + employeeIsOnAws);
				
				if (employeeIsOnAws) {
					lrValV2 = new LRAWSValidatorV2(selectedPayPeriod, priorLeaveItemsForPP);
				} else {
					lrValV2 = new LRNonAWSValidatorV2(selectedPayPeriod, priorLeaveItemsForPP);
				}
				lrValV2.validateCurrentRequest(lrItems.getCollectedLeaveItems());
			} catch (AlohaValidationException ave) {
				List<String> errorMessages = ave.getErrorMessages();
				for ( String errMsg : errorMessages) {
					System.out.println(errMsg);
					FacesMessage facesMsg = new FacesMessage(errMsg, errMsg);
					facesMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
					FacesContext.getCurrentInstance().addMessage(null, facesMsg);
				}
				throw new ValidatorException(new FacesMessage());
			}
		}		
	}
}