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
import gov.gsa.ocfo.aloha.web.model.LRSideBySideViewItem;
import gov.gsa.ocfo.aloha.web.model.leave.LeaveViewItem;
import gov.gsa.ocfo.aloha.web.util.AlohaConstants;
import gov.gsa.ocfo.aloha.web.util.AlohaURIs;
import gov.gsa.ocfo.aloha.web.util.DateUtil;
import gov.gsa.ocfo.aloha.web.util.ErrorMessages;
import gov.gsa.ocfo.aloha.web.util.FacesContextUtil;
import gov.gsa.ocfo.aloha.web.util.NormalMessages;
import gov.gsa.ocfo.aloha.web.validator.leave.v2.LRAWSValidatorV2;
import gov.gsa.ocfo.aloha.web.validator.leave.v2.LRNonAWSValidatorV2;
import gov.gsa.ocfo.aloha.web.validator.leave.v2.LRValidatorV2;

import java.io.IOException;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
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

@ManagedBean(name=LRAmendV2MB.MANAGED_BEAN_NAME, eager=false)
@ViewScoped
public class LRAmendV2MB extends LRBaseV2MB implements Serializable {
	private static final long serialVersionUID = 6623234181499697366L;
	public static final String MANAGED_BEAN_NAME = "lrAmendV2MB";

	// ABSTRACT METHOD IN SUPERCLASS / REQUIRED TO BE IMPLEMENTED IN SUBLCASS
	protected LeaveTypeMB getLeaveTypeMB() {
		return leaveTypeMB;
	}

	// ABSTRACT METHOD IN SUPERCLASS / REQUIRED TO BE IMPLEMENTED IN SUBLCASS
	protected List<PayPeriodSchedule> getPayPeriodScheduleList() throws AlohaServerException {
		return this.getLrAmendPayPeriod().getPayPeriodScheduleList();
	}
	
	// ABSTRACT METHOD IN SUPERCLASS / REQUIRED TO BE IMPLEMENTED IN SUBLCASS
	protected DisabledVetLeaveInfo retrieveDisabledVetLeaveInfo() throws AlohaServerException {
		return this.employeeEJB.getDisabledVetLeaveInfo(this.getApprovedLeaveDetail().getEmployee().getUserId());
	}
	
	@EJB 
	private UserEJB userEJB;
	
	@EJB
	protected EmployeeEJB employeeEJB;
	
	@EJB
	private LeaveRequestEJB leaveRequestEJB;
	
	// MANAGED BEAN HELPER
	@ManagedProperty(value="#{userMB}")
	private UserMB userMB;
	public void setUserMB(UserMB userMB) {
		this.userMB = userMB;
	}
	// MANAGED BEAN HELPER
	@ManagedProperty(value="#{leaveTypeMB}")
	protected LeaveTypeMB leaveTypeMB;
	public void setLeaveTypeMB(LeaveTypeMB leaveTypeMB) {
		this.leaveTypeMB = leaveTypeMB;
	}
	// MANAGED BEAN HELPER
	@ManagedProperty(value="#{payPeriodMB}")
	protected PayPeriodMB payPeriodMB;		
	public void setPayPeriodMB(PayPeriodMB payPeriodMB) {
		this.payPeriodMB = payPeriodMB;
	}	
	// MANAGED BEAN HELPER
	@ManagedProperty(value="#{leaveStatusMB}")
	protected LeaveStatusMB leaveStatusMB;
	public void setLeaveStatusMB(LeaveStatusMB leaveStatusMB) {
		this.leaveStatusMB = leaveStatusMB;
	}	
	
	// SUBMIT OWN OR ON BEHALF OF
	private LRMode lrMode;
	public String getMode() {
		return this.lrMode.getText();
	}
	// NAVIGATION INSTANCE MEMBER
	private boolean pageOne;
	public boolean isPageOne() {
		return pageOne;
	}	
	// NAVIGATION INSTANCE MEMBER
	private boolean pageTwo;
	public boolean isPageTwo() {
		return pageTwo;
	}	
	// NAVIGATION INSTANCE MEMBER
	private boolean pageThree;
	public boolean isPageThree() {
		return pageThree;
	}		
	// INSTANCE MEMBER
	private String headerTextForLeaveItemsPanel;
	public String getHeaderTextForLeaveItemsPanel() {
		if ( this.headerTextForLeaveItemsPanel == null) {
			if ( this.getApprovedLeaveDetail() != null ) {
				Object[] params = { this.getApprovedLeaveDetail().getLeaveRequestId() };
				this.headerTextForLeaveItemsPanel = NormalMessages.getInstance().getMessage(NormalMessages.LR_AMEND_SECTION_TITLE, params);
			}
		}
		return this.headerTextForLeaveItemsPanel;
	}
	// INSTANCE MEMBER
	private LeaveDetail approvedLeaveDetail;
	public LeaveDetail getApprovedLeaveDetail() {
		return approvedLeaveDetail;
	}
	// INSTANCE MEMBER
	private LeaveDetail pendAmendLeaveDetail;
	public LeaveDetail getPendAmendLeaveDetail() {
		return pendAmendLeaveDetail;
	}
	// INSTANCE MEMBER
	private String amendmentRemarks;
	public String getAmendmentRemarks() {
		return amendmentRemarks;
	}
	public void setAmendmentRemarks(String amendmentRemarks) {
		this.amendmentRemarks = amendmentRemarks;
	}
	// INSTANCE MEMBER
	private List<KeyValuePair> leaveBalances = new ArrayList<KeyValuePair>();
	public List<KeyValuePair> getLeaveBalances() {
		return this.leaveBalances;
	}
	// INSTANCE MEMBER (Helper Inner Class)
	private LRAmendAuthorizer lrAmendAuthorizer;
	private LRAmendAuthorizer getLrAmendAuthorizer() {
		if ( this.lrAmendAuthorizer == null) {
			this.lrAmendAuthorizer = new LRAmendAuthorizer();
		}
		return this.lrAmendAuthorizer;
	}
	// INSTANCE MEMBER (Helper Inner Class)
	private LRAmendInitializer lrAmendInitializer;
	private LRAmendInitializer getLrAmendInitializer() {
		if (this.lrAmendInitializer == null ){
			this.lrAmendInitializer = new LRAmendInitializer();
		}
		return this.lrAmendInitializer;
	}
	// INSTANCE MEMBER (Helper Inner Class)
	private LRAmendSupervisor lrAmendSupervisor;
	private LRAmendSupervisor getLrAmendSupervisor() {
		if ( this.lrAmendSupervisor == null) {
			this.lrAmendSupervisor = new LRAmendSupervisor();
		}
		return this.lrAmendSupervisor;
	}
	// INSTANCE MEMBER (Helper Inner Class)
	private LRAmendItems lrItems; 
	protected LRAmendItems getLrItems() {
		
		if  ( this.lrItems == null) {
			this.lrItems = new LRAmendItems();
		}
		return this.lrItems;
	}
	// INSTANCE MEMBER (Helper Inner Class)
	private LRAmendPayPeriod lrAmendPayPeriod; 
	private LRAmendPayPeriod getLrAmendPayPeriod() {
		
		if ( this.lrAmendPayPeriod == null) {
			this.lrAmendPayPeriod = new LRAmendPayPeriod();
		}
		return this.lrAmendPayPeriod;
	}
	// INSTANCE MEMBER (Helper Inner Class)
	private LRPendAmendDetailCreator lrPendAmendDetailCreator;
	private LRPendAmendDetailCreator getLrPendAmendDetailCreator() {
		if ( this.lrPendAmendDetailCreator == null ) {
			this.lrPendAmendDetailCreator = new LRPendAmendDetailCreator();
		}
		return this.lrPendAmendDetailCreator;
	}
	// INSTANCE MEMBER (Helper Inner Class)
	private LRAWSValidator lrAWSValidator;
	public LRAWSValidator getLrAWSValidator() {
		if ( this.lrAWSValidator == null ) {
			this.lrAWSValidator = new LRAWSValidator();
		}
		return this.lrAWSValidator;
	}
	@PreDestroy
	public void cleanup() {

		if (this.lrAmendAuthorizer != null) {
			this.lrAmendAuthorizer.cleanup();
			this.lrAmendAuthorizer = null;
		}
		if ( this.lrAmendInitializer != null) {
			this.lrAmendInitializer.cleanup();
			this.lrAmendInitializer = null;
		}
		if ( this.lrAmendSupervisor != null) {
			this.lrAmendSupervisor.cleanup();
			this.lrAmendSupervisor = null;
		}
		if ( this.lrItems != null) {
			this.lrItems.cleanup();
			this.lrItems = null;
		}	
		if ( this.lrAmendPayPeriod != null) {
			this.lrAmendPayPeriod.cleanup();
			this.lrAmendPayPeriod = null;
		}	
		if ( this.lrPendAmendDetailCreator != null) {
			this.lrPendAmendDetailCreator.cleanup();
			this.lrPendAmendDetailCreator = null;
		}		
		this.lrMode = null;
		this.pageOne = false;
		this.pageTwo = false;
		this.pageThree = false;
		
		this.headerTextForLeaveItemsPanel = null;
		this.approvedLeaveDetail = null;
		this.pendAmendLeaveDetail = null;
	}	
	
	@PostConstruct
	public void init() {
		try {
			super.init();
			
			// GET REQUEST PARAMETER #1
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
			
			// GET REQUEST PARAMETER #2
			String leaveDetailId = FacesContextUtil.getHttpServletRequest().getParameter(AlohaConstants.LEAVE_DETAIL_ID);		
			// IF THE REQUEST PARAMETER IS EMPTY, WE CAN'T CONTINUE
			if ( StringUtil.isNullOrEmpty(leaveDetailId)) {
				System.out.println("HTTP Parameter \"leaveDetailId\" is NULL in call to " 
						+ this.getClass().getName() + ".init() method. Cannot continue.");
				throw new IllegalStateException();
			}
			
			// RETRIEVE APPROVED LEAVE DETAIL OBJECT
			this.approvedLeaveDetail = this.leaveRequestEJB.getLeaveDetail(Long.parseLong(leaveDetailId));		
			// IF THE LEAVE DETAIL OBJECT IS NULL, WE CAN'T CONTINUE
			if ( this.approvedLeaveDetail == null) {
				StringBuilder sb = new StringBuilder();
				sb.append("Retrieval of LeaveDetail object, using HTTP Parameter named \"leaveDetailId\", having a value of " );
				sb.append(leaveDetailId + ", is NULL in call to " + this.getClass().getName() + ".init() method. Cannot continue.");
				if ( this.userMB != null && this.userMB.getUser() != null) {
					sb.append("\nUser Login: " + this.userMB.getUser().getLoginName());
					sb.append("\nLeave Detail ID: " + leaveDetailId);
				}
				System.out.println("********************ERROR********************");
				System.out.println(sb.toString());
				System.out.println("*********************************************");
				throw new IllegalStateException();				
			}		
			
			// MAKE SURE THAT RETRIEVED LEAVE DETAIL IS IN FACT "APPROVED"
			if ( this.approvedLeaveDetail.isApproved() == false) {
				StringBuilder sb = new StringBuilder();
				sb.append("Leave Request identified by leaveDetailID '" +  leaveDetailId + "' is NOT in an APPROVED status. ");
				sb.append("Actual Leave Status for Leave Request identified by leaveDetailID '" +  leaveDetailId + "' is '");  
				sb.append(this.approvedLeaveDetail.getLeaveStatus().getLabel() + "'");
				System.out.println("********************ERROR********************");
				System.out.println(sb.toString());
				System.out.println("*********************************************");				
				throw new IllegalStateException();			
			}
			
			this.getLrAmendAuthorizer().checkAccessRights();
			this.getLrAmendInitializer().execute();
			this.leaveBalances = this.getLrAmendInitializer().retrieveLeaveBalances();
			
			this.pageOne = true;
			this.pageTwo = false;
			this.pageThree = false;
			
		} catch (AuthorizationException ae) {
			try {
				FacesContext.getCurrentInstance().getExternalContext().redirect(AlohaURIs.UNAUTHORIZED);
			} catch (IOException ignore) {}
		} catch (IllegalStateException ise) {
			try {
				FacesContext.getCurrentInstance().getExternalContext().redirect(AlohaURIs.ILLEGAL_OPERATION);
			} catch (IOException ignore) {}
		} catch (AlohaServerException ase) {
			try {
				ase.printStackTrace();
				FacesContext.getCurrentInstance().getExternalContext().redirect(AlohaURIs.SERVER_ERROR);
			} catch (IOException ignore) {}
		} catch (Throwable t) {
			try {
				t.printStackTrace();
				FacesContext.getCurrentInstance().getExternalContext().redirect(AlohaURIs.SERVER_ERROR);
			} catch (IOException ignore) {}
		}
	}
	
	private void logException(Exception exc) {
		if ( exc != null && userMB != null) {
			System.out.println("AlohaUser: " + userMB.getFullName() + " / " + userMB.getFullName() 
					+ " / " + userMB.getEmailAddress());
			exc.printStackTrace();
		}
	}
	public void onMakeRevisions() {
		super.onMakeRevisions();
		this.pageOne = true;
		this.pageTwo = false;
		this.pageThree = false;
	}
	public void onConfirm() {
		try {
			
			////////////////////////////////////////////////////////////
			// Check to see if disabled veteran leave has been selected,
			// and if so, make sure that the certification check box
			// was checked
			////////////////////////////////////////////////////////////			
			this.validateDisabledVetCertification();
			////////////////////////////////////////////////////////////
			
			this.pendAmendLeaveDetail = this.getLrPendAmendDetailCreator().createPendAmendLeaveDetail();
			LeaveHeader leaveHeader = this.approvedLeaveDetail.getLeaveHeader();
			leaveHeader.addLeaveDetail(this.pendAmendLeaveDetail);
			this.pendAmendLeaveDetail.setLeaveHeader(leaveHeader);
			this.pendAmendLeaveDetail.setSequence(leaveHeader.getLeaveDetails().size());
			this.leaveRequestEJB.updateLeaveHeader(leaveHeader);
			
			this.pageOne = false;
			this.pageTwo = false;
			this.pageThree = true;		
		}  
		catch (ValidatorException valEx) {
			this.pageOne   = false;
			this.pageTwo   = true;
			this.pageThree = false;		
		} 
		catch (AlohaServerException e) {
			this.logException(e);
			try {
				FacesContext.getCurrentInstance().getExternalContext().redirect(AlohaURIs.SERVER_ERROR);
			} catch (IOException ignore) {}
		}		
	}
	// CALL TO LRAmendPayPeriod method
	public SelectItem[] getSelectItemsForDaysInSelectedPayPeriod() {
		return this.getLrAmendPayPeriod().getSelectItemsForDaysInSelectedPayPeriod();
	}
	// CALL TO LRAmendPayPeriod method
	public String getSelectedPayPeriodValue() {
		return this.getLrAmendPayPeriod().getSelectedPayPeriodValue();
	}
	// CALL TO LRAmendPayPeriod method
	public void setSelectedPayPeriodValue(String selectedPayPeriodValue) {
		this.getLrAmendPayPeriod().setSelectedPayPeriodValue(selectedPayPeriodValue);
	}
	// CALL TO LRAmendPayPeriod method
	public String getSelectedPayPeriodLabel() {
		return this.getLrAmendPayPeriod().getSelectedPayPeriodLabel();
	}
	// CALL TO LRAmendPayPeriod method
	public String getSelectedPayPeriodDateRange() {
		return this.getLrAmendPayPeriod().getSelectedPayPeriodDateRange();
	}	
	// CALL TO LRAmendSupervisor method
	public Map<String, Object> getSupervisors() {
		return this.getLrAmendSupervisor().getSupervisors();
	}
	// CALL TO LRAmendSupervisor method
	public String getSelectedSupervisorUserIdAsString() {
		return this.getLrAmendSupervisor().getSelectedSupervisorUserIdAsString();
	}
	// CALL TO LRAmendSupervisor method
	public void setSelectedSupervisorUserIdAsString(String selectedEmployeeUserIdAsString) {
		this.getLrAmendSupervisor().setSelectedSupervisorUserIdAsString(selectedEmployeeUserIdAsString);
	}	
	// CALL TO LRItems method
	public LeaveViewItem getSelectedLeaveItem() {
		return this.getLrItems().getSelectedLeaveItem();
	}
	// CALL TO LRItems method
	public List<LeaveViewItem> getAmendableLeaveItems() {
		return this.getLrItems().getAmendableLeaveViewItems();
	}
	public void addLeaveViewItems() {
		try 
		{
			super.addLeaveViewItems();
			this.pageOne = true;
			this.pageTwo = false;
			this.pageThree = false;
		} 
		catch(ValidatorException ignore) {
			// Take user back to page one to fix validation errors
			this.pageOne = true;
			this.pageTwo = false;
			this.pageThree = false;
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
			this.pageOne = true;
			this.pageTwo = false;
			this.pageThree = false;
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
		this.pageOne = true;
		this.pageTwo = false;
		this.pageThree = false;
	}
	public void prepareLeaveViewItemForEdit() {
		super.prepareLeaveViewItemForEdit();
		this.pageOne = true;
		this.pageTwo = false;
		this.pageThree = false;
	}
	public void removeNewLeaveItem() {
		super.removeNewLeaveItem();
		this.pageOne = true;
		this.pageTwo = false;
		this.pageThree = false;
	}	
	
	// CALL TO LRItems method
	public List<LRSideBySideViewItem> getSideBySideLeaveItems() {
		return this.getLrItems().getSideBySideLeaveViewItems();
	}
	
	public boolean isInSubmitOwnMode() {
		return ( (this.lrMode != null) && (this.lrMode.equals(LRMode.SUBMIT_OWN)) && (this.userMB.isSubmitOwn()) );
	}
	public boolean isInOnBehalfOfMode() {
		return ( (this.lrMode != null) && (this.lrMode.equals(LRMode.ON_BEHALF_OF)) && (this.userMB.isOnBehalfOf()) );
	}
	
	public void onReviewChanges() {
		
		try {
			this.performValidation();
			
			// If validation passes, take user to page 3
			this.pageOne = false;
			this.pageTwo = true;
			this.pageThree = false;
		} catch(ValidatorException ve) {
			// Take user back to page 2 to fix validation errors
			this.pageOne = true;
			this.pageTwo = false; 
			this.pageThree = false;
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
		this.getLrItems().performValidation();
		this.getLrAWSValidator().performValidation();
	}	
	
	public BigDecimal getApprovedTotalLeaveHours() {
		return (this.getLrItems().sumApprovedLeaveHours());
	}

	public BigDecimal getPendAmendTotalLeaveHours() {
		return (this.getLrItems().sumPendAmendLeaveHours());
	}

	/****************************************************************************************************
	 *
	 * class LRAmendAuthorizer
	 * @author LeeTTrent
	 *
	 ****************************************************************************************************/		
	public class LRAmendAuthorizer {

		private void cleanup() {
			// no-op (no instance members)
		}
		private void checkAccessRights() throws AuthorizationException {
			if ( (!this.isSubmittedByEmployee()) && (!this.isSubmittedByOnBehalfOf()) ) {
				Object[] params = { userMB.getFullName(), Long.valueOf(approvedLeaveDetail.getLeaveHeaderId()) };
				String errMsg = ErrorMessages.getInstance().getMessage(ErrorMessages.LR_AMEND_UNAUTHORIZED, params);
				FacesContextUtil.getHttpSession().setAttribute("unauthMsg", errMsg);
				throw new AuthorizationException(errMsg);
			}
			if ( (this.isSubmittedByEmployee()) && (userMB.getUserId() != approvedLeaveDetail.getSubmitter().getUserId()) ) {
				Object[] params = { userMB.getFullName(), Long.valueOf(approvedLeaveDetail.getLeaveHeaderId()) };
				String errMsg = ErrorMessages.getInstance().getMessage(ErrorMessages.LR_AMEND_UNAUTHORIZED, params);
				FacesContextUtil.getHttpSession().setAttribute("unauthMsg", errMsg);
				throw new AuthorizationException(errMsg);
			}
			if ( (this.isSubmittedByOnBehalfOf()) && (userMB.getUserId() != approvedLeaveDetail.getSubmitter().getUserId()) ) {
				Object[] params = { userMB.getFullName(), Long.valueOf(approvedLeaveDetail.getLeaveHeaderId()) };
				String errMsg = ErrorMessages.getInstance().getMessage(ErrorMessages.LR_AMEND_UNAUTHORIZED, params);
				FacesContextUtil.getHttpSession().setAttribute("unauthMsg", errMsg);
				throw new AuthorizationException(errMsg);
			}
		}			
		private boolean isSubmittedByEmployee() {
			return	( (userMB.isSubmitOwn()) 
						&& (userMB.getUserId() == approvedLeaveDetail.getEmployee().getUserId())
						&& (userMB.getUserId() == approvedLeaveDetail.getSubmitter().getUserId())
					);
		}
		private boolean isSubmittedByOnBehalfOf() {
			return	( (userMB.isOnBehalfOf()) 
						&& (userMB.getUserId() != approvedLeaveDetail.getEmployee().getUserId())
						&& (userMB.getUserId() == approvedLeaveDetail.getSubmitter().getUserId())
					);
		}
	}

	/****************************************************************************************************
	 *
	 * class LRAmendInitializer
	 * @author LeeTTrent
	 *
	 ****************************************************************************************************/		
	private class LRAmendInitializer {
		
		private void cleanup() {
			// no-op (no instance members)
		}
		private void execute() {
			this.initLeaveTypes();
		}
		private void initLeaveTypes() {
			if ( approvedLeaveDetail != null ) {
				Date startDate = approvedLeaveDetail.getPayPeriodStartDate();
				if  ( getLrAmendAuthorizer().isSubmittedByOnBehalfOf()) {
					//2012-10-10 JJM 48969: get obo leave types
					leaveTypeMB.setLeaveTypes(leaveTypeMB.getLeaveTypesEffOBO(startDate));
				}
				else {
					//2012-10-10 JJM 48969: get emp leave types
					leaveTypeMB.setLeaveTypes(leaveTypeMB.getLeaveTypesEff(startDate));
				}			
			}
		}
		private List<KeyValuePair> retrieveLeaveBalances() throws AlohaServerException {
			return employeeEJB.getLeaveBalances(approvedLeaveDetail.getEmployee().getUserId());
		}
	}
	
	/****************************************************************************************************
	 *
	 * class LRAmendSupervisor
	 * @author LeeTTrent
	 *
	 ****************************************************************************************************/
	private class LRAmendSupervisor {
		// INSTANCE MEMEBER
		private String selectedSupervisorUserIdAsString;
		
		// INSTANCE MEMEBER
		protected AlohaUser supervisor;
		
		private void cleanup() {
			this.selectedSupervisorUserIdAsString = null;
		}
		
		public Map<String, Object> getSupervisors() {
			Map<String, Object> htmlSupervisors = null;
			List<AlohaUser> dbSupervisors = this.getDbSupervisors();
			if ( dbSupervisors != null) {
				htmlSupervisors = new TreeMap<String, Object>();
				for (AlohaUser dbSupervisor : dbSupervisors) {
					htmlSupervisors.put(dbSupervisor.getLabel(), dbSupervisor.getValue());
				}
			}
			return htmlSupervisors;
		}		
		private List<AlohaUser> getDbSupervisors() {
			List<AlohaUser> dbSupervisors = null;
			try {
				dbSupervisors = employeeEJB.getLeaveApprovers(approvedLeaveDetail.getEmployee().getUserId());
			} catch (AlohaServerException ase) {
				logException(ase);
			}
			return dbSupervisors;
		}
		
		private String getSelectedSupervisorUserIdAsString() {
			if ( StringUtil.isNullOrEmpty(this.selectedSupervisorUserIdAsString)) {
				try {
					AlohaUserPref userPreferences = userEJB.getUserPref(approvedLeaveDetail.getEmployee().getUserId());
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
		private void setSelectedSupervisorUserIdAsString(
				String selectedSupervisorUserIdAsString) {
			this.selectedSupervisorUserIdAsString = selectedSupervisorUserIdAsString;
		}		
		private AlohaUser getSupervisor() {
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
		private AlohaUser findSupervisor(long userId) {
			for ( AlohaUser dbSupervisor: this.getDbSupervisors()) {
				if ( dbSupervisor.getUserId() == userId) {
					return dbSupervisor;
				}
			}
			return null;
		}
	}
	
	/****************************************************************************************************
	 *
	 * class LRItemsMgr
	 * @author LeeTTrent
	 *
	 ****************************************************************************************************/
	private class LRAmendItems extends LRItems {
		private List<LeaveViewItem> approvedLeaveViewItems;
		
		private LRAmendItems() {
			this.collectedLeaveItems = this.createLeaveViewItemListUsingApprovedLeaveModelItems();
			this.approvedLeaveViewItems = this.createLeaveViewItemListUsingApprovedLeaveModelItems();
		}

	    public BigDecimal sumPendAmendLeaveHours() {
	    	return this.sumLeaveHours(this.getAmendableLeaveViewItems());
	    }
	    public BigDecimal sumApprovedLeaveHours() {
	    	return this.sumLeaveHours(this.getApprovedLeaveViewItems());
	    }
		protected void cleanup() {
			super.cleanup();
			this.approvedLeaveViewItems = null;
		}

		private List<LeaveViewItem> getAmendableLeaveViewItems() {
			return this.collectedLeaveItems;
		}

		private List<LeaveViewItem> getApprovedLeaveViewItems() {
			return this.approvedLeaveViewItems;
		}		

		private boolean leaveItemsAreTheSame() {

			// CAN'T BE THE SAME IF THE SIZES ARE DIFFERENT
			if ( this.getAmendableLeaveViewItems().size() != this.approvedLeaveViewItems.size()) {
				return false;
			}

			int size = this.getAmendableLeaveViewItems().size();
			for ( int index = 0; index < size; index++) {
				
				/*
				System.out.println("\n-------------------------------------------------------------------------");
				System.out.println(this.getAmendableLeaveViewItems().get(index).getLeaveDate() + " / " + this.approvedLeaveViewItems.get(index).getLeaveDate());
				System.out.println(this.getAmendableLeaveViewItems().get(index).getLeaveType().getValue() + " / " + this.approvedLeaveViewItems.get(index).getLeaveType().getValue());
				System.out.println(this.getAmendableLeaveViewItems().get(index).getLeaveNumberOfHours() + " / " + this.approvedLeaveViewItems.get(index).getLeaveNumberOfHours());		
				System.out.println(this.getAmendableLeaveViewItems().get(index).getLeaveStartTime() + " / " + this.approvedLeaveViewItems.get(index).getLeaveStartTime());
				System.out.println("-------------------------------------------------------------------------");
				*/
				if ( this.getAmendableLeaveViewItems().get(index).getLeaveDate().compareTo(this.approvedLeaveViewItems.get(index).getLeaveDate()) != 0 ) {
					return false;
				}
				if ( ! this.getAmendableLeaveViewItems().get(index).getLeaveType().equals(this.approvedLeaveViewItems.get(index).getLeaveType())) {
					return false;
				}
				if ( this.getAmendableLeaveViewItems().get(index).getLeaveNumberOfHours().compareTo(this.approvedLeaveViewItems.get(index).getLeaveNumberOfHours()) != 0) {
					return false;
				}
				if ( this.getAmendableLeaveViewItems().get(index).getLeaveStartTime() != null && this.approvedLeaveViewItems.get(index).getLeaveStartTime() == null ) {
					return false;
				}
				if ( this.getAmendableLeaveViewItems().get(index).getLeaveStartTime() == null && this.approvedLeaveViewItems.get(index).getLeaveStartTime() != null ) {
					return false;
				}
				if ( this.getAmendableLeaveViewItems().get(index).getLeaveStartTime() != null && this.approvedLeaveViewItems.get(index).getLeaveStartTime() != null) {
					if ( this.getAmendableLeaveViewItems().get(index).getLeaveStartTime().compareTo(this.approvedLeaveViewItems.get(index).getLeaveStartTime()) != 0  ) {
						return false;
					}
				}
			}		
			return true;
		}		
		
		
		private List<LeaveViewItem> createLeaveViewItemListUsingApprovedLeaveModelItems() {

			List<LeaveViewItem> leaveItemViewList = new ArrayList<LeaveViewItem>();
			for (LeaveItem leaveModelItem : approvedLeaveDetail.getLeaveItems() ) {
				
				// INSTANTIATE NEW LEAVE VIEW ITEM
				LeaveViewItem leaveViewItem = new LeaveViewItem();
				
				// LEAVE VIEW ITEM ID
				leaveViewItem.setLeaveViewItemId(leaveModelItem.getId());
				
				// LEAVE ITEM DATE VALUE
				leaveViewItem.setLeaveDate(new Date(leaveModelItem.getLeaveDate().getTime()));
								
				// LEAVE ITEM TYPE VALUE
				leaveViewItem.setLeaveType(leaveModelItem.getLeaveType());
				
				// LEAVE NUMBER OF HOURS VALUE
				leaveViewItem.setLeaveNumberOfHours(leaveModelItem.getLeaveHours().setScale(1, RoundingMode.HALF_DOWN));
				
				// LEAVE NUMBER OF HOURS AS STRING
				leaveViewItem.setLeaveNumberOfHoursAsString(new String(leaveViewItem.getLeaveNumberOfHours().toString()));
				
				// LEAVE START TIME
				if ( leaveModelItem.getStartTime() != null ) {
					
					// LEAVE START TIME VALUE
					leaveViewItem.setLeaveStartTime(new Date(leaveModelItem.getStartTime().getTime()));	
				} else {
					// LEAVE START TIME VALUE
					leaveViewItem.setLeaveStartTime(null);						
				}	
				
				// ADD NEWLY CREATED LEAVE VIEW ITEM TO THE COLLECTED LEAVE ITEMS LIST
				leaveItemViewList.add(leaveViewItem);
			}
			
			return (LeaveViewItem.consolidateAndSort(leaveItemViewList) );
		}
		
	    protected void performValidation() throws ValidatorException, AlohaServerException, ParseException 
	    {
	    	super.performValidation();
	    	this.performValidationForDifferences();
	    	this.performDisabledVetValidation();
	    }
	    
		private void performValidationForDifferences() throws ValidatorException {
			if ( this.leaveItemsAreTheSame() ) {
				String errMsgText = ErrorMessages.getInstance().getMessage(ErrorMessages.LR_AMEND_NO_CHANGE);
				FacesMessage facesMsg = new FacesMessage(errMsgText, errMsgText);
				facesMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
				FacesContext.getCurrentInstance().addMessage(null, facesMsg);
				throw new ValidatorException(facesMsg);			
			}
		}	    
	    
	    public List<LRSideBySideViewItem> getSideBySideLeaveViewItems() {

	    	Map<String, LRSideBySideViewItem> sideBySideMap = new HashMap<String, LRSideBySideViewItem>();
			
			// PUT ALL THE APPROVED LEAVE VIEW ITEMS IN THE MAP FIRST
			for ( LeaveViewItem approvedLeaveViewItem : this.getApprovedLeaveViewItems() ) {
				
				LRSideBySideViewItem sideBySideItem = new LRSideBySideViewItem(approvedLeaveViewItem.getLeaveDate());
				sideBySideItem.setApprovedLeaveViewItem(approvedLeaveViewItem);
				sideBySideMap.put(sideBySideItem.getMapKey(), sideBySideItem);
				
				//System.out.println("Approved SideBySide Map Key: " + sideBySideItem.getMapKey());
				
			}
			
			// FOR PEND AMEND LEAVE VIEW ITEMS, THE CORRECT SIDE-BE-SIDE OBJECT MAY ALERADY 
			// BE IN THE MAP SO TRY TO FIND IT FIRST. IF IT ISN'T, INSERT ('PUT') A NEW
			// SIDE-BY-SIDE OBJECT IN THE MAP
			for ( LeaveViewItem pendAmendLeaveViewItem : this.getAmendableLeaveViewItems() ) {
				
				LRSideBySideViewItem sideBySideItem = sideBySideMap.get(pendAmendLeaveViewItem.getMapKey());
				
				if (sideBySideItem == null) {
					sideBySideItem = new LRSideBySideViewItem(pendAmendLeaveViewItem.getLeaveDate());
					sideBySideItem.setPendAmendLeaveViewItem(pendAmendLeaveViewItem);
					sideBySideMap.put(sideBySideItem.getMapKey(), sideBySideItem);		
					//System.out.println("PendAmend SideBySide Map Key: " + sideBySideItem.getMapKey());
				} else {
					sideBySideItem.setPendAmendLeaveViewItem(pendAmendLeaveViewItem);
				}
				
			}
			
			Collection<LRSideBySideViewItem> sideBySideCollection = sideBySideMap.values();
			List<LRSideBySideViewItem> sideBySideList = new ArrayList<LRSideBySideViewItem>(sideBySideCollection);
			
			Collections.sort(sideBySideList, new Comparator<LRSideBySideViewItem>() {
				@Override
				public int compare(LRSideBySideViewItem sideBySideOne, LRSideBySideViewItem sideBySideTwo) {
					String mapKeyOne = sideBySideOne.getMapKey();
					String mapKeyTwo = sideBySideTwo.getMapKey();
					return (mapKeyOne.compareTo(mapKeyTwo));
				}	
			});				
			
			return sideBySideList;
		}		
	    
	    
		private void performDisabledVetValidation() throws ValidatorException, AlohaServerException {
			
			//////////////////////////////////////////////////////////////////////////////
			// IF user has selected the disabled veteran leave type, we need to validate:
			//////////////////////////////////////////////////////////////////////////////			
			if ( this.isHasDisabledVetLeaveViewItem() ) {
				
				DisabledVetLeaveInfo vetInfo = retrieveDisabledVetLeaveInfo();	
				
				/////////////////////////////////////////////////////////////////////////////////////////////////
				// If there isn't a row in the PAR_EDS_LV table for this employee that contains the 
				// Disabled Veteran Leave type code ('69'), then employee is not eligible for Disabled Veteran 
				// Leave so we need to throw a validation error
				/////////////////////////////////////////////////////////////////////////////////////////////////		
				if ( vetInfo == null ) {
					
					String errMsgText = ErrorMessages.getInstance().getMessage(ErrorMessages.LR_DISABLED_VET_RECORD_NOT_FOUND);
					FacesMessage facesMsg = new FacesMessage(errMsgText, errMsgText);
					facesMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
					FacesContext.getCurrentInstance().addMessage(null, facesMsg);
					throw new ValidatorException(new FacesMessage());
				}
				
				//////////////////////////////////////////////////////////////////////////////////////
				// If the last day of the selected pay period is later than the date held 
				// in the par_eds_lv.lv_used_beg_date column for this employee, then employee
				// is not eligible for Disabled Veteran Leave so we need to throw a validation error
				/////////////////////////////////////////////////////////////////////////////////////						
				
//				List<PayPeriodSchedule> ppSchedule = getPayPeriodScheduleList();
//				PayPeriodSchedule lastDayInPP = ppSchedule.get( ( ppSchedule.size() ) - 1 );
//				
//				if ( lastDayInPP.getCalendarDate().compareTo(vetInfo.getExpirationDate()) > 0 ) {
//					
//					//////////////////////////////////////////////////////////////////////////////////////////////////////////
//					// Build error message, passing in the expiration date contained in the par_eds_lv.lv_used_beg_date column
//					//////////////////////////////////////////////////////////////////////////////////////////////////////////					
//					Object[] params = { DateUtil.formatDate(vetInfo.getExpirationDate()) };
//					String errMsgText = ErrorMessages.getInstance().getMessage(ErrorMessages.LR_DISABLED_VET_RECORD_RECORD_EXPIRED, params);
//					
//					FacesMessage facesMsg = new FacesMessage(errMsgText, errMsgText);
//					facesMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
//					FacesContext.getCurrentInstance().addMessage(null, facesMsg);
//					throw new ValidatorException(new FacesMessage());					
//				}
				
				////////////////////////////////////////////////////////////////////////
				// Check to make sure that Disabled Veteran Leave Record hasn't expired
				////////////////////////////////////////////////////////////////////////
				for ( LeaveViewItem lvi: this.getAmendableLeaveViewItems() ) {
					
					if ( lvi.isDisabledVetLeaveViewItem() ) {

						if ( lvi.getLeaveDate().compareTo(vetInfo.getExpirationDate()) > 0 ) {
							
							Object[] params = { DateUtil.formatDate(lvi.getLeaveDate()), DateUtil.formatDate(vetInfo.getExpirationDate()) };
							String errMsgText = ErrorMessages.getInstance().getMessage(ErrorMessages.LR_DISABLED_VET_RECORD_RECORD_EXPIRED, params);
							
							FacesMessage facesMsg = new FacesMessage(errMsgText, errMsgText);
							facesMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
							FacesContext.getCurrentInstance().addMessage(null, facesMsg);
							throw new ValidatorException(new FacesMessage());							
						}						
					}
				}				
				
				////////////////////////////////////////////////////////////////////////////////////////////////////				
				// If sum of all the Disabled Veteran Leave time requested for this pay period exceeds the
				// Disabled Veteran Leave balance held in the par_eds_lv_lv_hrs_bal column for this employee,
				// then we need to throw a validation error.
				////////////////////////////////////////////////////////////////////////////////////////////////////				
			
				if ( this.sumDiabledVetLeaveHours().compareTo( vetInfo.getLeaveBalance() ) > 0 ) {
					
					Object[] params = { vetInfo.getLeaveBalance() };
					String errMsgText = ErrorMessages.getInstance().getMessage(ErrorMessages.LR_DISABLED_VET_RECORD_INSUFFICIENT_LEAVE_BALANCES, params);
					FacesMessage facesMsg = new FacesMessage(errMsgText, errMsgText);
					facesMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
					FacesContext.getCurrentInstance().addMessage(null, facesMsg);
					throw new ValidatorException(new FacesMessage());						
				}
			}
		}	    
		
		public boolean isHasDisabledVetLeaveViewItem() { 
			
			if ( this.getAmendableLeaveViewItems() != null ) {
				
				for ( LeaveViewItem lvi : this.getAmendableLeaveViewItems() ) {
					if ( lvi.isDisabledVetLeaveViewItem() ) {
						return true;
					}
				}			
			}
			
			return false;
		}		
	    
	    public BigDecimal sumDiabledVetLeaveHours() {
	    	return this.sumDiabledVetLeaveHours(this.getAmendableLeaveViewItems());
	    }
		
	    private BigDecimal sumDiabledVetLeaveHours(List<LeaveViewItem> lvItemList) {
	    	
	    	BigDecimal sum = BigDecimal.ZERO;
	    	
	    	for (LeaveViewItem lvItem : lvItemList) {
	    		
	    		if ( lvItem.isDisabledVetLeaveViewItem() ) {
	    			sum = sum.add(lvItem.getLeaveNumberOfHours());
	    		}
	    	}
	    	return sum;
	    }
	}
	/****************************************************************************************************
	 *
	 * class LRAmendPayPeriod
	 * @author LeeTTrent
	 *
	 ****************************************************************************************************/
	private class LRAmendPayPeriod {
		private PayPeriod selectedPayPeriod;
		private SelectItem[] selectItemsForDaysInSelectedPayPeriod;
		private String selectedPayPeriodDateRange;
		private List<PayPeriodSchedule> payPeriodScheduleList;
		
		private void cleanup() {
			this.selectedPayPeriod = null;
			this.selectItemsForDaysInSelectedPayPeriod = null;
			this.selectedPayPeriodDateRange = null;
			this.payPeriodScheduleList = null;
		}
		
		private PayPeriod getSelectedPayPeriod() {
			if ( this.selectedPayPeriod == null) {
				this.selectedPayPeriod = payPeriodMB.getPayPeriodForStartDate(approvedLeaveDetail.getPayPeriodStartDate());
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

		private SelectItem[] getSelectItemsForDaysInSelectedPayPeriod() {
			if ( this.selectItemsForDaysInSelectedPayPeriod == null) {
				this.selectItemsForDaysInSelectedPayPeriod = new SelectItem[14];
				try {
					List<PayPeriodSchedule> payPeriodScheduleList 
						= employeeEJB.retrievePayPeriodScheduleForEmployeeAndPayPeriodStartDate
							(approvedLeaveDetail.getEmployee().getUserId(), this.getSelectedPayPeriodValue());
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
				} catch (AlohaServerException e) {
					logException(e);
				}
			}
			return selectItemsForDaysInSelectedPayPeriod;
		}
		public List<PayPeriodSchedule> getPayPeriodScheduleList() throws AlohaServerException {
			if ( this.payPeriodScheduleList == null) {
				this.payPeriodScheduleList = employeeEJB.retrievePayPeriodScheduleForEmployeeAndPayPeriodStartDate
						(getApprovedLeaveDetail().getEmployee().getUserId(), this.getSelectedPayPeriodValue());
			}
			return this.payPeriodScheduleList;
		}
	}	
	
	/****************************************************************************************************
	 *
	 * class LRDetailCreator
	 * @author LeeTTrent
	 *
	 ****************************************************************************************************/
	private class LRPendAmendDetailCreator {	
		private void cleanup() {
			// no-op
		}
		private LeaveDetail createPendAmendLeaveDetail() throws AlohaServerException {
			// AUDIT TRAIL
			AuditTrail auditTrail = new AuditTrail();
			auditTrail.setUserLastUpdated(userMB.getUser().getLoginName());
			auditTrail.setDateLastUpdated(new Date());
			
			// LEAVE DETAIL
			LeaveDetail leaveDetail = new LeaveDetail();
			leaveDetail.setAuditTrail(auditTrail);
			leaveDetail.setSubmitter(userMB.getUser());
			leaveDetail.setApprover(getLrAmendSupervisor().getSupervisor());
			leaveDetail.setPayPeriodStartDate(getApprovedLeaveDetail().getPayPeriodStartDate());
			leaveDetail.setPayPeriodEndDate(getApprovedLeaveDetail().getPayPeriodEndDate());
			
			// LEAVE STATUS
			LeaveStatus leaveStatus = leaveStatusMB.getLeaveStatus(LeaveStatus.CodeValues.PEND_AMEND);
			leaveDetail.setLeaveStatus(leaveStatus);
			
			// LEAVE HISTORY (Includes Amendment Remarks)
			LeaveStatusTransition leaveStatusTransition
				= leaveStatusMB.getLeaveStatusTransition(LeaveStatusTransition.ActionCodeValues.APPROVED_TO_PEND_AMEND);
			LeaveHistory leaveHistory = new LeaveHistory();
			leaveHistory.setAuditTrail(auditTrail);
			leaveHistory.setLeaveDetail(leaveDetail);
			leaveHistory.setLeaveStatusTransition(leaveStatusTransition);
			leaveHistory.setActionDatetime(new Date());
			leaveHistory.setActor(leaveDetail.getSubmitter());
			if ( !StringUtil.isNullOrEmpty(getAmendmentRemarks()) ) {
				leaveHistory.setRemarks(getAmendmentRemarks());
			}
			leaveDetail.addLeaveHistory(leaveHistory);			
			
			// LEAVE ITEMS
			List<LeaveItem> leaveItemList = new ArrayList<LeaveItem>();
			
			for ( LeaveViewItem leaveViewItem : getLrItems().getAmendableLeaveViewItems()) {
				LeaveItem leaveItem = new LeaveItem();
				leaveItem.setLeaveDetail(leaveDetail);
				leaveItem.setAuditTrail(auditTrail);
				leaveItem.getAuditTrail().setUserLastUpdated(leaveDetail.getSubmitter().getLoginName());
				
				leaveItem.setLeaveDate(leaveViewItem.getLeaveDate());
				leaveItem.setLeaveType(leaveViewItem.getLeaveType());
				leaveItem.setLeaveHours(leaveViewItem.getLeaveNumberOfHours());
				leaveItem.setStartTime(leaveViewItem.getLeaveStartTime());
				leaveItemList.add(leaveItem);
			}		
			leaveDetail.setLeaveItems(leaveItemList);
			
//			if ( leaveDetail.isHasDisabledVetLeaveItem() ) {
//				
//				if ( disabledVetCertified == true ) {
//					leaveDetail.setDisabledVeteranCertified(true);
//				} else {
//					throw new AlohaServerException("Disabled veteran leave type selected but not certified.");
//				}
//			}			
			
//			System.out.println("------------------------------------------------------------------------------------");
//			System.out.println("TESTING PEND_AMEND / DISABLED LEAVE TYPE CODE");
//			System.out.println("leaveDetail.getLeaveItems().size(): " + leaveDetail.getLeaveItems().size());
//			System.out.println("leaveDetail.isHasDisabledVetLeaveItem(): " + leaveDetail.isHasDisabledVetLeaveItem());
//			System.out.println("------------------------------------------------------------------------------------");
			
			if ( leaveDetail.isHasDisabledVetLeaveItem() ) {
				leaveDetail.setDisabledVeteranCertified(true);
			}	
			
			return leaveDetail;
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
			
			PayPeriod selectedPayPeriod = getLrAmendPayPeriod().getSelectedPayPeriod();
						
			List<LeaveItem> priorLeaveItemsForPP 
				= leaveRequestEJB.getPriorLeaveItemsForAmend
					(approvedLeaveDetail.getEmployee().getUserId(), selectedPayPeriod.getFromDate(), approvedLeaveDetail.getId());
			
			try {
				LRValidatorV2 lrValV2 = null;
				///////////////////////////////////////////////////////////////////////////
				//////////////////////// IMPORTANT /////////////////////////////////////// 
				// The AWS property on AlohaUser is transient and is therefore unreliable.
				// As such, retrieve AWS Indicator directly from database
				///////////////////////////////////////////////////////////////////////////
				boolean employeeIsOnAws = employeeEJB.getAwsFlagForEmployee(approvedLeaveDetail.getEmployee().getUserId());
				//System.out.println("employeeIsOnAws: " + employeeIsOnAws);
				
				if (employeeIsOnAws) {
					lrValV2 = new LRAWSValidatorV2(selectedPayPeriod, priorLeaveItemsForPP);
				} else {
					lrValV2 = new LRNonAWSValidatorV2(selectedPayPeriod, priorLeaveItemsForPP);
				}
				lrValV2.validateCurrentRequest(getLrItems().getAmendableLeaveViewItems());
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