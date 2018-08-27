package gov.gsa.ocfo.aloha.model.entity.leave.recon;

import gov.gsa.ocfo.aloha.exception.LeaveReconException;
import gov.gsa.ocfo.aloha.model.PayPeriod;
import gov.gsa.ocfo.aloha.model.entity.AlohaUser;
import gov.gsa.ocfo.aloha.model.entity.leave.LeaveDetail;
import gov.gsa.ocfo.aloha.model.entity.leave.LeaveItem;
import gov.gsa.ocfo.aloha.util.StringUtil;

import java.io.Serializable;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

@Entity
@Table(name="LR_RECON_WIZARD", schema="ALOHA")
@NamedQueries({
	@NamedQuery(name="retrieveByEmployeeUserId",
	        	query="SELECT row FROM LeaveReconWizard row WHERE row.employeeUserId = :employeeUserId and row.fixPending = 0")  	
})
public class LeaveReconWizard implements Serializable {

	private static final long serialVersionUID = 8511273581917416126L;
	
	public interface QueryNames{
		public static final String RETRIEVE_BY_EMPLOYEE_USER_ID = "retrieveByEmployeeUserId";
	}
	public interface QueryParamNames {
		public static final String RETRIEVE_BY_EMPLOYEE_USER_ID = "employeeUserId";
	}
	
	@Id
	@Column(name="WIZARD_ID", unique=true)
	private String wizardId;
	
	@OneToMany(mappedBy="leaveReconWizard", fetch=FetchType.EAGER, cascade=CascadeType.ALL)
	@OrderBy("leaveDate")
	private List<LeaveReconWizardItem> leaveReconWizardItems = new ArrayList<LeaveReconWizardItem>();	
	
	@OneToMany(mappedBy="leaveReconWizard", fetch=FetchType.EAGER, cascade=CascadeType.ALL)
	@OrderBy("primaryKey")
	private List<LeaveReconWizardXref> leaveReconWizardXrefItems = new ArrayList<LeaveReconWizardXref>();	
	
	@OneToMany(mappedBy="leaveReconWizard", fetch=FetchType.EAGER, cascade=CascadeType.ALL)
	@OrderBy("primaryKey")
	private List<LeaveReconWizardAloha> leaveReconWizardAlohaItems = new ArrayList<LeaveReconWizardAloha>();	
	
	@OneToMany(mappedBy="leaveReconWizard", fetch=FetchType.EAGER, cascade=CascadeType.ALL)
	@OrderBy("leaveDate")
	private List<LeaveReconWizardPending> leaveReconWizardPendingItems = new ArrayList<LeaveReconWizardPending>();	

	@OneToMany(mappedBy="leaveReconWizard", fetch=FetchType.EAGER, cascade=CascadeType.ALL)
	@OrderBy("wizardItemId")
	private List<EtamsAmendmentItem> etamsAmendmentItems = new ArrayList<EtamsAmendmentItem>();	

	@Column(name="EMPL_USER_ID")
	private int employeeUserId;
	
	@Column(name="LV_YEAR")
	private int leaveYear;
	
	@Column(name="PP_NO")
	private int payPeriodNumber;
	
	@Temporal(TemporalType.DATE)
	@Column(name="PP_START_DATE")
	private Date payPeriodStartDate;
	
	@Temporal(TemporalType.DATE)
	@Column(name="PP_END_DATE")
	private Date payPeriodEndDate;
	
	@Column(name="EMPL_LAST_NAME")
	private String employeeLastName;
	
	@Column(name="EMPL_FIRST_NAME")
	private String employeeFirstName;
	
	@Column(name="EMPL_MIDDLE_NAME")
	private String employeeMiddleName;
	
	@Column(name="FIX_PENDING", nullable=false)
	private int fixPending;
	
    @ManyToOne
	@JoinColumn(name="SUPERVISOR_USER_ID",  referencedColumnName="USER_ID", nullable=true)	
	private AlohaUser supervisor; 
	
	@Transient
	private boolean reconDeferred = false;
	
	@Transient
	private String payPeriodDateRange;
	
	@Transient
	private String employeeRemarks;
	
	@Transient
	private boolean disabledVetCertified = false;
	
	public String getWizardId() {
		return wizardId;
	}

	public void setWizardId(String wizardId) {
		this.wizardId = wizardId;
	}

	public int getEmployeeUserId() {
		return employeeUserId;
	}

	public void setEmployeeUserId(int employeeUserId) {
		this.employeeUserId = employeeUserId;
	}

	public int getLeaveYear() {
		return leaveYear;
	}

	public void setLeaveYear(int leaveYear) {
		this.leaveYear = leaveYear;
	}

	public int getPayPeriodNumber() {
		return payPeriodNumber;
	}

	public void setPayPeriodNumber(int payPeriodNumber) {
		this.payPeriodNumber = payPeriodNumber;
	}
	
	public String getPayPeriodDateRange() {
		if ( StringUtil.isNullOrEmpty(this.payPeriodDateRange) ) {
			SimpleDateFormat sdf = new SimpleDateFormat(PayPeriod.LABEL_FORMAT);
			StringBuilder sb = new StringBuilder();
			sb.append(sdf.format(this.payPeriodStartDate));
			sb.append(" - ");
			sb.append(sdf.format(this.payPeriodEndDate));
			this.payPeriodDateRange = sb.toString();
		}
		return this.payPeriodDateRange;
	}
	public Date getPayPeriodStartDate() {
		return payPeriodStartDate;
	}

	public void setPayPeriodStartDate(Date payPeriodStartDate) {
		this.payPeriodStartDate = payPeriodStartDate;
	}

	public Date getPayPeriodEndDate() {
		return payPeriodEndDate;
	}

	public void setPayPeriodEndDate(Date payPeriodEndDate) {
		this.payPeriodEndDate = payPeriodEndDate;
	}

	public String getEmployeeFullName() {
		return StringUtil.buildFullName(this.employeeFirstName, this.employeeMiddleName, this.employeeLastName);
	}
	
	public String getEmployeeLastName() {
		return employeeLastName;
	}

	public void setEmployeeLastName(String employeeLastName) {
		this.employeeLastName = employeeLastName;
	}

	public String getEmployeeFirstName() {
		return employeeFirstName;
	}

	public void setEmployeeFirstName(String employeeFirstName) {
		this.employeeFirstName = employeeFirstName;
	}

	public String getEmployeeMiddleName() {
		return employeeMiddleName;
	}

	public void setEmployeeMiddleName(String employeeMiddleName) {
		this.employeeMiddleName = employeeMiddleName;
	}
	
	public int getFixPending() {
		return fixPending;
	}

	public void setFixPending(int fixPending) {
		this.fixPending = fixPending;
	}

	public AlohaUser getSupervisor() {
		return supervisor;
	}

	public void setSupervisor(AlohaUser supervisor) {
		this.supervisor = supervisor;
	}

	public List<LeaveReconWizardItem> getLeaveReconWizardItems() {
		return leaveReconWizardItems;
	}

	public void setLeaveReconWizardItems(
			List<LeaveReconWizardItem> leaveReconWizardItems) {
		this.leaveReconWizardItems = leaveReconWizardItems;
	}

	public List<LeaveReconWizardXref> getLeaveReconWizardXrefItems() {
		return leaveReconWizardXrefItems;
	}

	public void setLeaveReconWizardXrefItems(
			List<LeaveReconWizardXref> leaveReconWizardXrefItems) {
		this.leaveReconWizardXrefItems = leaveReconWizardXrefItems;
	}

	public List<LeaveReconWizardAloha> getLeaveReconWizardAlohaItems() {
		return leaveReconWizardAlohaItems;
	}

	public void setLeaveReconWizardAlohaItems(
			List<LeaveReconWizardAloha> leaveReconWizardAlohaItems) {
		this.leaveReconWizardAlohaItems = leaveReconWizardAlohaItems;
	}
	
	public List<LeaveReconWizardPending> getLeaveReconWizardPendingItems() {
		return leaveReconWizardPendingItems;
	}

	public void setLeaveReconWizardPendingItems(
			List<LeaveReconWizardPending> leaveReconWizardPendingItems) {
		this.leaveReconWizardPendingItems = leaveReconWizardPendingItems;
	}
	

	public List<EtamsAmendmentItem> getEtamsAmendmentItems() {
		return etamsAmendmentItems;
	}

	public void setEtamsAmendmentItems(List<EtamsAmendmentItem> etamsAmendmentItems) {
		this.etamsAmendmentItems = etamsAmendmentItems;
	}

	public boolean isReconDeferred() {
		return reconDeferred;
	}

	public void setReconDeferred(boolean reconDeferred) {
		this.reconDeferred = reconDeferred;
	}

	public String getEmployeeRemarks() {
		return employeeRemarks;
	}

	public void setEmployeeRemarks(String employeeRemarks) {
		this.employeeRemarks = employeeRemarks;
	}

	public boolean xrefItemsAreNullOrEmpty() {
		return (this.leaveReconWizardXrefItems == null || this.leaveReconWizardXrefItems.isEmpty());
	}
	
	public boolean wizardItemsAreNullOrEmpty() {
		return (this.leaveReconWizardItems == null || this.leaveReconWizardItems.isEmpty());
	}

	
	public boolean xrefItemsArePopulated() {
		return (this.leaveReconWizardXrefItems != null && this.leaveReconWizardXrefItems.isEmpty() == false);
	}
	
	public boolean alohaItemsAreNullOrEmpty() {
		return (this.leaveReconWizardAlohaItems == null || this.leaveReconWizardAlohaItems.isEmpty());
	}
	
	public boolean alohaItemsArePopulated() {
		return (this.leaveReconWizardAlohaItems != null &&  this.leaveReconWizardAlohaItems.isEmpty() == false);
	}
	
	public boolean isBrandNewLeaveRequestRequired() {
		return ( this.allCorrectLeaveHoursAreZero() == false && this.incorrectAlohaLeaveHoursDetected() && this.xrefItemsAreNullOrEmpty() && this.alohaItemsAreNullOrEmpty());
	}
	
	public boolean isAlohaAmendmentRequired() {
		boolean xrefItemsCountIsOne = (this.xrefItemsArePopulated() &&  this.leaveReconWizardXrefItems.size() == 1);
		return ( this.allCorrectLeaveHoursAreZero() == false && this.incorrectAlohaLeaveHoursDetected() && xrefItemsCountIsOne && this.alohaItemsArePopulated());
	}
	
	public boolean isAlohaWithdrawCreateRequired() {
		boolean xrefItemsCountIsGreaterThanOne = (this.xrefItemsArePopulated() &&  this.leaveReconWizardXrefItems.size() > 1);
		return ( this.allCorrectLeaveHoursAreZero() == false && this.alohaItemsArePopulated() && this.incorrectAlohaLeaveHoursDetected() && xrefItemsCountIsGreaterThanOne);
	}
	
	public boolean isAlohaWithrawOnlyRequired() {
		return ( this.allCorrectLeaveHoursAreZero() == true && this.xrefItemsArePopulated() == true );
	}
	
	public boolean allCorrectLeaveHoursArePopulated() {
		for ( LeaveReconWizardItem item: this.leaveReconWizardItems ) {
			if ( item.getCorrectLeaveHours() == null ) {
				return false;
			}
		}
		return true;
	}
	
	public boolean incorrectAlohaLeaveHoursDetected() {
		int incorrectCount = 0;
		for ( LeaveReconWizardItem item: this.leaveReconWizardItems ) {
			if ( item.getCorrectLeaveHours().equals(item.getAlohaLeaveHours()) == false ) {
				incorrectCount++;
			}
		}
		return (incorrectCount > 0);
	}	
	
	public boolean incorrectEtamsLeaveHoursDetected() {
		int incorrectCount = 0;
		for ( LeaveReconWizardItem item: this.leaveReconWizardItems ) {
			if ( item.getCorrectLeaveHours().equals(item.getEtamsLeaveHours()) == false ) {
				incorrectCount++;
			}
		}
		return (incorrectCount > 0);
	}	

	public boolean allCorrectLeaveHoursAreZero() {
		for ( LeaveReconWizardItem item: this.leaveReconWizardItems ) {
			if ( BigDecimal.ZERO.equals(item.getCorrectLeaveHours() ) == false ) {
				return false;
			}
		}
		return true;
	}
	
	public boolean isEtamsAmendmentRequired() {
		int incorrectCount = 0;
		for ( LeaveReconWizardItem item: this.leaveReconWizardItems ) {
			if ( item.getCorrectLeaveHours().equals(item.getEtamsLeaveHours()) == false ) {
				incorrectCount++;
			}
		}
		return (incorrectCount > 0);
	}
	
//	public Map<String, LeaveItem> buildLeaveItemMap(LeaveDetail leaveDetail) throws LeaveReconException {
//		
//		//System.out.println("BEGIN: LeaveReconWizard.buildLeaveItemMap(LeaveDetail)");
//		
//		if ( this.alohaItemsAreNullOrEmpty() ) {
//			StringBuilder errMsg = new StringBuilder();
//			errMsg.append("List<LeaveReconWizardAlohaItem> IS NULL OR EMPTY.");	
//			errMsg.append(" Cannot create ALOHA Leave Item Map for: Employee ['");
//			errMsg.append(this.getEmployeeUserId());
//			errMsg.append("'], Leave Year ['");
//			errMsg.append(this.getLeaveYear());
//			errMsg.append("'], Pay Period ['");
//			errMsg.append(this.getPayPeriodNumber());
//			errMsg.append("']");
//			throw new LeaveReconException(errMsg.toString());
//		}
//		
//		if ( this.wizardItemsAreNullOrEmpty() ) {
//			StringBuilder errMsg = new StringBuilder();
//			errMsg.append("List<LeaveReconWizardItem> IS NULL OR EMPTY.");	
//			errMsg.append(" Cannot create ALOHA Leave Item Map for: Employee ['");
//			errMsg.append(this.getEmployeeUserId());
//			errMsg.append("'], Leave Year ['");
//			errMsg.append(this.getLeaveYear());
//			errMsg.append("'], Pay Period ['");
//		
//			errMsg.append(this.getPayPeriodNumber());
//			errMsg.append("']");
//			throw new LeaveReconException(errMsg.toString());
//		}		
//		
//		//System.out.println("Wizard Items Count: " + this.getLeaveReconWizardItems().size());
//		//System.out.println("Aloha Items Count: " + this.getLeaveReconWizardAlohaItems().size());
//		
//		Map<String, LeaveItem> leaveItemMap = new HashMap<String, LeaveItem>();
//
//		//System.out.println("leaveItemMap.size(): " + leaveItemMap.size());
//
//		// INITIALIZE LEAVE ITEM MAP WITH PREVIOUSLY APPROVED LEAVE ITEMS
//		// FOR THIS EMPLOYEE AND PAY PERIOD
//		for ( LeaveReconWizardAloha alohaItem : this.leaveReconWizardAlohaItems) {
//			
//			// MAKE SURE LEAVE HOURS ARE GREATER THAN ZERO
//			if ( alohaItem.getLeaveHours().compareTo(BigDecimal.ZERO) == 1 ) {	
//				
//				LeaveItem leaveItem = new LeaveItem();
//				
//				leaveItem.setLeaveDetail(leaveDetail);
//				leaveItem.setAuditTrail(leaveDetail.getAuditTrail());
//				leaveItem.setLeaveDate(alohaItem.getLeaveDate());
//				leaveItem.setLeaveType(alohaItem.getLeaveType());
//				leaveItem.setLeaveHours(alohaItem.getLeaveHours());
//				leaveItem.setStartTime(null); // WIZARD IS NOT HANDLING START TIME
//				
//				leaveItemMap.put(alohaItem.getLeaveItemMapKey(), leaveItem);				
//			}
//		}
//
//		//System.out.println("leaveItemMap.size()" + leaveItemMap.size());
//		
//		// NOW OVERLAY WHAT'S ALREADY IN THIS MAP WITH LEAVE ITEMS THAT
//		// WERE CORRECTED BY THE WIZARD
//		for ( LeaveReconWizardItem wizardItem : this.leaveReconWizardItems) {
//			
//			// ONLY OVERLAY THOSE LEAVE ITEMS THAT NEED TO BE CORRECTED
//			if ( ( wizardItem.getCorrectLeaveHours().compareTo(BigDecimal.ZERO) == 1  ) 
//					&& (wizardItem.getCorrectLeaveHours().equals(wizardItem.getAlohaLeaveHours()) == false)) {
//				
//				LeaveItem leaveItem = new LeaveItem();
//				
//				leaveItem.setLeaveDetail(leaveDetail);
//				leaveItem.setAuditTrail(leaveDetail.getAuditTrail());
//				leaveItem.setLeaveDate(wizardItem.getLeaveDate());
//				leaveItem.setLeaveType(wizardItem.getLeaveTypeObj());
//				leaveItem.setLeaveHours(wizardItem.getCorrectLeaveHours());
//				leaveItem.setStartTime(null); // WIZARD IS NOT HANDLING START TIME
//				
//				leaveItemMap.put(wizardItem.getLeaveItemMapKey(), leaveItem);				
//			}
//		}		
//		
//		//System.out.println("leaveItemMap.size(): " + leaveItemMap.size());
//		//System.out.println("END: LeaveReconWizard.buildLeaveItemMap(LeaveDetail)");
//		return leaveItemMap;
//	}
	
	public void populatePendingItems() throws LeaveReconException {
		
		//System.out.println("BEGIN: LeaveReconWizard.populatePendingItems()");
		
		if ( this.wizardItemsAreNullOrEmpty() ) {
			StringBuilder errMsg = new StringBuilder();
			errMsg.append("List<LeaveReconWizardItem> IS NULL OR EMPTY.");	
			errMsg.append(" Cannot Pending 'Fix' items for: Employee ['");
			errMsg.append(this.getEmployeeUserId());
			errMsg.append("'], Leave Year ['");
			errMsg.append(this.getLeaveYear());
			errMsg.append("'], Pay Period ['");
		
			errMsg.append(this.getPayPeriodNumber());
			errMsg.append("']");
			throw new LeaveReconException(errMsg.toString());
		}	

		for ( LeaveReconWizardItem wizardItem : this.leaveReconWizardItems) {

			//////////////////////////////////////////////////////////////////			
			// PLACE THE WIZARD ITEMS IN THE PENDING TABLE WHERE AN AMENDMENT
			// IS REQUIRED ON EITHER THE ALOHA SIDE OR THE ALOHA SIDE
			//////////////////////////////////////////////////////////////////
			if ( wizardItem.getCorrectLeaveHours().equals(wizardItem.getAlohaLeaveHours()) == false 
					|| wizardItem.getCorrectLeaveHours().equals(wizardItem.getEtamsLeaveHours()) == false ) {

				LeaveReconWizardPending pendingItem = new LeaveReconWizardPending();
				
				pendingItem.setWizardItemId(wizardItem.getWizardItemId());
				pendingItem.setLeaveReconWizard(this);
				pendingItem.setEmployeeUserId(wizardItem.getEmployeeUserId());
				pendingItem.setLeaveYear(wizardItem.getLeaveYear());
				pendingItem.setPayPeriodNumber(wizardItem.getPayPeriodNumber());
				pendingItem.setLeaveDate(wizardItem.getLeaveDate());
				pendingItem.setLeaveTypeObj(wizardItem.getLeaveTypeObj());
				pendingItem.setLeaveTypeCode(wizardItem.getLeaveTypeCode());
				pendingItem.setCorrectLeaveHours(wizardItem.getCorrectLeaveHours());
				pendingItem.setAlohaLeaveHours(wizardItem.getAlohaLeaveHours());
				pendingItem.setEtamsLeaveHours(wizardItem.getEtamsLeaveHours());
				
				this.addLeaveReconWizardPendingItem(pendingItem);
			}
		}		
		
		//System.out.println("END: LeaveReconWizard.populatePendingItems()");
	}
	
	
	private boolean addLeaveReconWizardPendingItem(LeaveReconWizardPending pendingItem) {
		
		if ( this.leaveReconWizardPendingItems == null) {
			this.leaveReconWizardPendingItems = new ArrayList<LeaveReconWizardPending>();
		}
		
		return this.leaveReconWizardPendingItems.add(pendingItem);
	}

	public void populateEtamsAmendmentItems() throws LeaveReconException {
		
		//System.out.println("BEGIN: LeaveReconWizard.populateEtamsAmendmentItems()");
		
		if ( this.wizardItemsAreNullOrEmpty() ) {
			StringBuilder errMsg = new StringBuilder();
			errMsg.append("List<LeaveReconWizardItem> IS NULL OR EMPTY.");	
			errMsg.append(" Cannot add/update ETAMS Amendment Items for: Employee ['");
			errMsg.append(this.getEmployeeUserId());
			errMsg.append("'], Leave Year ['");
			errMsg.append(this.getLeaveYear());
			errMsg.append("'], Pay Period ['");
			errMsg.append(this.getPayPeriodNumber());
			errMsg.append("']");
			throw new LeaveReconException(errMsg.toString());
		}	

		////////////////////////////////////////////////////////////////////////
		// PROCESS ETAMS ITEMS TO BE AMENDED AND PLACED IN THE PENDING TABLE
		////////////////////////////////////////////////////////////////////////
		for ( LeaveReconWizardItem wizardItem : this.leaveReconWizardItems) {

			///////////////////////////////////////////////////////////////////////////////////////
			// WE ONLY WANT TO CREATE OR UPDATE AN ETAMS AMENDMENT ITEMS IF THERE IS A DISCREPANCY
			///////////////////////////////////////////////////////////////////////////////////////
			if ( wizardItem.getCorrectLeaveHours().equals(wizardItem.getEtamsLeaveHours()) == false ) {

				////////////////////////////////////////////////////////////////////////////////////			
				// EtamsAmendmentItem MAY ALREADY EXIST. IF IT DOES, UPDATE IT WITH THE CORRECT
				// LEAVE HOURS. IF IT DOESN'T EXIST, CREATE IT AND ADD IT TO THE ETAMS AMDNEMDNET
				// ITEM COLLECTION. IT WILL BE WRITTEN TO THE DATABASE (EVENTUALLY)
				////////////////////////////////////////////////////////////////////////////////////
				EtamsAmendmentItem etamsAmendmentItem = this.findEtamsAmendmentItem(wizardItem.getWizardItemId());
				if ( etamsAmendmentItem != null) {
					etamsAmendmentItem.setCorrectLeaveHours(wizardItem.getCorrectLeaveHours());
					etamsAmendmentItem.setStatus(EtamsAmendmentItem.StatusValues.UNPROCESSED);
				} else {
					etamsAmendmentItem = this.createEtamsAmendmentItem(wizardItem);
					this.addEtamsAmendmentItem(etamsAmendmentItem);
				}
			}
		}		
		
		//System.out.println("Wizard Items Count: " + this.getLeaveReconWizardItems().size());
		//System.out.println("ETAMS Items Count: " + this.etamsAmendmentItems.size());

		//System.out.println("END: LeaveReconWizard.populateEtamsAmendmentItems()");
	}

	private boolean addEtamsAmendmentItem(EtamsAmendmentItem etamsAmendmentItem) {
		
		if ( this.etamsAmendmentItems == null) {
			this.etamsAmendmentItems = new ArrayList<EtamsAmendmentItem>();
		}
		
		return this.etamsAmendmentItems.add(etamsAmendmentItem);
	}
	
	private EtamsAmendmentItem findEtamsAmendmentItem(String wizardItemId) {
		
		for ( EtamsAmendmentItem etamsItem : this.etamsAmendmentItems) {
			
			if ( etamsItem.getWizardItemId().equals(wizardItemId)) {
				return etamsItem;
			}
		}
		
		return null;
	}
	
	private EtamsAmendmentItem createEtamsAmendmentItem(LeaveReconWizardItem wizardItem) {

		EtamsAmendmentItem etamsAmendmentItem = new EtamsAmendmentItem();
		
		etamsAmendmentItem.setWizardItemId(wizardItem.getWizardItemId());
		etamsAmendmentItem.setLeaveReconWizard(this);
		etamsAmendmentItem.setEmployeeUserId(wizardItem.getEmployeeUserId());
		etamsAmendmentItem.setLeaveYear(wizardItem.getLeaveYear());
		etamsAmendmentItem.setPayPeriodNumber(wizardItem.getPayPeriodNumber());
		etamsAmendmentItem.setLeaveDate(wizardItem.getLeaveDate());
		etamsAmendmentItem.setPrimaryLeaveTypeCode(wizardItem.getLeaveTypeObj().getPrimaryCode());
		etamsAmendmentItem.setSecondaryLeaveTypeCode(wizardItem.getLeaveTypeObj().getSecondaryCode());
		etamsAmendmentItem.setStatus(EtamsAmendmentItem.StatusValues.UNPROCESSED);
		etamsAmendmentItem.setCorrectLeaveHours(wizardItem.getCorrectLeaveHours());
		
		return etamsAmendmentItem;
	}
	
	public Map<String, LeaveItem> buildLeaveItemMap(LeaveDetail leaveDetail) throws LeaveReconException {
		
		System.out.println("BEGIN: LeaveReconWizard.buildLeaveItemMap(LeaveDetail)");
		
		if ( this.wizardItemsAreNullOrEmpty() ) {
			StringBuilder errMsg = new StringBuilder();
			errMsg.append("List<LeaveReconWizardItem> IS NULL OR EMPTY.");	
			errMsg.append(" Cannot create ALOHA Leave Item Map for: Employee ['");
			errMsg.append(this.getEmployeeUserId());
			errMsg.append("'], Leave Year ['");
			errMsg.append(this.getLeaveYear());
			errMsg.append("'], Pay Period ['");
		
			errMsg.append(this.getPayPeriodNumber());
			errMsg.append("']");
			throw new LeaveReconException(errMsg.toString());
		}		
				
		Map<String, LeaveItem> leaveItemMap = new HashMap<String, LeaveItem>();

		//////////////////////////////////////////////////////////////////
		// INITIALIZE LEAVE ITEM MAP WITH PREVIOUSLY APPROVED LEAVE ITEMS
		// FOR THIS EMPLOYEE AND PAY PERIOD (IF ANY)
		//////////////////////////////////////////////////////////////////		
		
		System.out.println("this.getLeaveReconWizardAlohaItems().size(): " + this.getLeaveReconWizardAlohaItems().size());
		
		for ( LeaveReconWizardAloha alohaItem : this.getLeaveReconWizardAlohaItems()) {
			
			/////////////////////////////////////////////////////////
			// LEAVE HOURS IN ALOHA SHOULD BE GREATER THAN ZERO 
			// FOR PREVIOULSY APPROVED LEAVE REQUESTS BUT MAKE SURE 
			// ANYWAY
			/////////////////////////////////////////////////////////
			if ( alohaItem.getLeaveHours().compareTo(BigDecimal.ZERO) == 1 ) {	
				
				LeaveItem leaveItem = new LeaveItem();
				
				leaveItem.setLeaveDetail(leaveDetail);
				leaveItem.setAuditTrail(leaveDetail.getAuditTrail());
				leaveItem.setLeaveDate(alohaItem.getLeaveDate());
				leaveItem.setLeaveType(alohaItem.getLeaveType());
				leaveItem.setLeaveHours(alohaItem.getLeaveHours());
				leaveItem.setStartTime(null); // WIZARD IS NOT HANDLING START TIME
				
				leaveItemMap.put(alohaItem.getLeaveItemMapKey(), leaveItem);				
			}
		}

		System.out.println(" leaveItemMap.size(): " + leaveItemMap.size());
		System.out.println("this.getLeaveReconWizardItems().size(): " + this.getLeaveReconWizardItems().size());
		
		//////////////////////////////////////////////////////////////////
		// NOW OVERLAY WHAT'S ALREADY IN THIS MAP WITH LEAVE ITEMS THAT
		// WERE CORRECTED BY THE WIZARD
		//////////////////////////////////////////////////////////////////
		for ( LeaveReconWizardItem wizardItem : this.getLeaveReconWizardItems() ) {
			
			////////////////////////////////////////////////////////////
			// ONLY OVERLAY THOSE LEAVE ITEMS THAT NEED TO BE CORRECTED			
			// IT'S OKAY TO ALLOW ZERO HOURS HERE BECAUSE THE CORRECT
			// LEAVE HOURS COULD IN FACT BE ZERO. THIS COULD HAPPEN
			// WHEN EMPLOYEE SELECTS "ETAMS IS CORRECT" AND ETAMS HOURS 
			// ARE ZERO. THIS MEANS THAT EMPLOYEE DIDN'TAKE THE LEAVE 
			// RECORDED BY ALOHA
			/////////////////////////////////////////////////////////
			
			if (  wizardItem.getCorrectLeaveHours().equals(wizardItem.getAlohaLeaveHours()) == false) {
				
				LeaveItem leaveItem = new LeaveItem();
				
				leaveItem.setLeaveDetail(leaveDetail);
				leaveItem.setAuditTrail(leaveDetail.getAuditTrail());
				leaveItem.setLeaveDate(wizardItem.getLeaveDate());
				leaveItem.setLeaveType(wizardItem.getLeaveTypeObj());
				leaveItem.setLeaveHours(wizardItem.getCorrectLeaveHours());
				leaveItem.setStartTime(null); // WIZARD IS NOT HANDLING START TIME
				
				leaveItemMap.put(wizardItem.getLeaveItemMapKey(), leaveItem);				
			}
		}		
		
		System.out.println(" leaveItemMap.size(): " + leaveItemMap.size());
		
		return leaveItemMap;
	}
	
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((wizardId == null) ? 0 : wizardId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		LeaveReconWizard other = (LeaveReconWizard) obj;
		if (wizardId == null) {
			if (other.wizardId != null)
				return false;
		} else if (!wizardId.equals(other.wizardId))
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("LeaveReconWizard [wizardId=");
		builder.append(wizardId);
		builder.append(", employeeUserId=");
		builder.append(employeeUserId);
		builder.append(", leaveYear=");
		builder.append(leaveYear);
		builder.append(", payPeriodNumber=");
		builder.append(payPeriodNumber);
		builder.append(", payPeriodStartDate=");
		builder.append(payPeriodStartDate);
		builder.append(", payPeriodEndDate=");
		builder.append(payPeriodEndDate);
		builder.append(", employeeLastName=");
		builder.append(employeeLastName);
		builder.append(", employeeFirstName=");
		builder.append(employeeFirstName);
		builder.append(", employeeMiddleName=");
		builder.append(employeeMiddleName);
		builder.append(", fixPending=");
		builder.append(fixPending);
		builder.append(", reconDeferred=");
		builder.append(reconDeferred);
		builder.append(", payPeriodDateRange=");
		builder.append(payPeriodDateRange);
		builder.append(", employeeRemarks=");
		builder.append(employeeRemarks);
		builder.append("]");
		return builder.toString();
	}
	
	public boolean isHasDisabledVetLeaveViewItem() { 
		
		if ( this.leaveReconWizardItems != null ) {
			
			for ( LeaveReconWizardItem lvi : this.leaveReconWizardItems ) {
				if ( lvi.isDisabledVetLeaveViewItem() ) {
					return true;						
				}
			}			
		}

		return false;
	}

	public boolean isDisabledVetCertified() {
		return disabledVetCertified;
	}

	public void setDisabledVetCertified(boolean disabledVetCertified) {
		this.disabledVetCertified = disabledVetCertified;
	}
}