package gov.gsa.ocfo.aloha.model.entity.overtime;

import gov.gsa.ocfo.aloha.model.entity.AlohaUser;
import gov.gsa.ocfo.aloha.model.overtime.group.OTGroupRow;
import gov.gsa.ocfo.aloha.model.overtime.group.OTRequestTypeGroup;
import gov.gsa.ocfo.aloha.model.overtime.group.OTSalaryGradeGroup;
import gov.gsa.ocfo.aloha.util.ot.OTGroupStatusUtil;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.Version;

@Entity
@Table(name="OT_GROUP", schema="ALOHA")
@NamedQueries({
	@NamedQuery(name="retrieveOTGroupForSubmitter",
			query="SELECT row FROM OTGroup row " +
					"WHERE row.submitter.userId = :otGroupSubmitterUserId " +
					"AND row.payPeriod.key = :otPayPeriodKey " + 
					"AND row.status.id = :otGroupStatusId"
			),
	@NamedQuery(name="retrieveOTGroupsForSubmitterOrReceiver",
		query="SELECT row FROM OTGroup row " +
				"WHERE row.submitter.userId = :otGroupSubmitterUserId " +
				"OR row.receiver.userId = :otGroupReceiverUserId " + 
				"ORDER BY row.payPeriod.key DESC"
			),
	@NamedQuery(name="retrieveAllOTGroupsForSubmitter",
		query="SELECT row FROM OTGroup row " +
				"WHERE row.submitter.userId = :otGroupSubmitterUserId " +
				"ORDER BY row.payPeriod.key DESC"
			),
	@NamedQuery(name="retrieveAllOTGroupsForReceiver",
		query="SELECT row FROM OTGroup row " +
				"WHERE row.receiver.userId = :otGroupReceiverUserId " +
				"ORDER BY row.payPeriod.key DESC"
			),		
	@NamedQuery(name="retrieveOTGroupsForManager",
		query="SELECT grp FROM OTGroup grp " + 
				"LEFT OUTER JOIN grp.receiver recv " +
				"WHERE grp.submitter.userId = :otMgrUserId " +
				"OR recv.userId = :otMgrUserId"
			)		
})

public class OTGroup implements OTGroupRow, Serializable {
	private static final long serialVersionUID = -6616131553664343785L;
	
	public interface QueryNames{
		public static final String RETRIEVE_OT_GROUP_FOR_SUBMITTER = "retrieveOTGroupForSubmitter";
		public static final String RETRIEVE_OT_GROUPS_FOR_SUBMITTER_OR_RECEIVER = "retrieveOTGroupsForSubmitterOrReceiver";
		public static final String RETRIEVE_ALL_OT_GROUP_FOR_SUBMITTER = "retrieveAllOTGroupsForSubmitter";
		public static final String RETRIEVE_ALL_OT_GROUP_FOR_RECEIVER = "retrieveAllOTGroupsForReceiver";
		public static final String RETRIEVE_OT_GROUPS_FOR_MGR = "retrieveOTGroupsForManager";
	}
	public interface QueryParamNames {
		public static final String OT_GROUP_SUBMITTER_USER_ID = "otGroupSubmitterUserId";
		public static final String OT_GROUP_RECEIVER_USER_ID = "otGroupReceiverUserId";
		public static final String OT_PAY_PERIOD_KEY = "otPayPeriodKey";
		//public static final String OT_GROUP_STATUS_CODE = "otGroupStatusCode";
		public static final String OT_GROUP_STATUS_ID = "otGroupStatusId";
		public static final String OT_MGR_USER_ID = "otMgrUserId";
		
	}	
	@Id
	@SequenceGenerator(name="OT_GROUP_ID_GENERATOR", sequenceName="ALOHA.SEQ_OT_GROUP", allocationSize=1)
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="OT_GROUP_ID_GENERATOR")
	@Column(name="OT_GROUP_ID", unique=true)
	private long id;
	
    @ManyToOne
	@JoinColumn(name="PAY_PERIOD_KEY",  referencedColumnName="PAY_PERIOD_KEY", nullable=false)
	private OTPayPeriod payPeriod;
    
    @ManyToOne
	@JoinColumn(name="STATUS_ID",  referencedColumnName="STATUS_ID", nullable=false)
	private OTGroupStatus status;

    @ManyToOne
	@JoinColumn(name="SUBM_USER_ID",  referencedColumnName="USER_ID", nullable=false)	
	private AlohaUser submitter;
    
    @ManyToOne
	@JoinColumn(name="RECV_USER_ID",  referencedColumnName="USER_ID", nullable=false)	
	private AlohaUser receiver;

	@ManyToOne
	@JoinColumn(name="PARENT_GROUP_ID",  referencedColumnName="OT_GROUP_ID", nullable=true)	
	private OTGroup parent;

    @Version
	@Column(name="OPT_LOCK_NBR")
	private long version;

	@Embedded
	private OTCreatedBy createdBy = new OTCreatedBy();
	
	@Embedded
	private OTUpdatedBy updatedBy = new OTUpdatedBy();

	@OneToMany(mappedBy="group", fetch=FetchType.LAZY, cascade=CascadeType.ALL)
	@OrderBy("id")
	private List<OTGroupHistory> historicalEntries  = new ArrayList<OTGroupHistory>();
    
	@OneToMany(mappedBy="group", fetch=FetchType.EAGER, cascade=CascadeType.ALL)
	@OrderBy("id")
	private List<OTGroupSubmitterRemark> submitterRemarks = new ArrayList<OTGroupSubmitterRemark>();
	
	@OneToMany(mappedBy="group", fetch=FetchType.EAGER, cascade=CascadeType.ALL)
	@OrderBy("id")
	private List<OTGroupReceiverRemark> receiverRemarks = new ArrayList<OTGroupReceiverRemark>();

	@OneToMany(mappedBy="parent", fetch=FetchType.EAGER, cascade=CascadeType.ALL)
	@OrderBy("id")
	private List<OTGroup> children = new ArrayList<OTGroup>();
	
	@OneToMany(mappedBy="group", fetch=FetchType.EAGER, cascade=CascadeType.ALL)
	@OrderBy("id")
	private List<OTHeader> employees = new ArrayList<OTHeader>();
	
	@Transient
	private List<OTHeader> allEmployees = null;
	
	@Transient
	private List<OTGroup> allChildGroups = null;
	
	@Transient
	private List<AlohaUser> allAuthorizedIndividuals = null;
	
	@Transient
	private List<OTSalaryGradeGroup> salaryGradeGroupList = null;

	@Transient
	private List<OTRequestTypeGroup> requestTypeGroupList = null;
	
	@Transient
	private OTGroup topLevelGroup = null;
	
	@Transient
	private boolean cancellable;
	
	@Transient
	private boolean finalizable;
	
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public OTPayPeriod getPayPeriod() {
		return payPeriod;
	}

	public void setPayPeriod(OTPayPeriod payPeriod) {
		this.payPeriod = payPeriod;
	}

	public OTGroupStatus getStatus() {
		return status;
	}

	public void setStatus(OTGroupStatus status) {
		this.status = status;
	}

	public AlohaUser getSubmitter() {
		return submitter;
	}

	public void setSubmitter(AlohaUser submitter) {
		this.submitter = submitter;
	}

	public AlohaUser getReceiver() {
		return receiver;
	}

	public OTGroup getParent() {
		return parent;
	}

	public void setParent(OTGroup parent) {
		this.parent = parent;
	}

	public List<OTGroup> getChildren() {
		return children;
	}

	public void setChildren(List<OTGroup> children) {
		this.children = children;
	}

	public long getVersion() {
		return version;
	}

	public void setVersion(long version) {
		this.version = version;
	}

	public List<OTGroupHistory> getHistoricalEntries() {
		return historicalEntries;
	}

	public void setHistoricalEntries(List<OTGroupHistory> historicalEntries) {
		this.historicalEntries = historicalEntries;
	}

	public List<OTGroupSubmitterRemark> getSubmitterRemarks() {
		return submitterRemarks;
	}

	public void setSubmitterRemarks(List<OTGroupSubmitterRemark> submitterRemarks) {
		this.submitterRemarks = submitterRemarks;
	}

	public List<OTGroupReceiverRemark> getReceiverRemarks() {
		return receiverRemarks;
	}

	public void setReceiverRemarks(List<OTGroupReceiverRemark> receiverRemarks) {
		this.receiverRemarks = receiverRemarks;
	}
	
	public OTCreatedBy getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(OTCreatedBy createdBy) {
		this.createdBy = createdBy;
	}

	public OTUpdatedBy getUpdatedBy() {
		return updatedBy;
	}

	public void setUpdatedBy(OTUpdatedBy updatedBy) {
		this.updatedBy = updatedBy;
	}

	public void setReceiver(AlohaUser receiver) {
		this.receiver = receiver;
	}

	public boolean addGroupHistory(OTGroupHistory historicalEntry) {
		return this.historicalEntries.add(historicalEntry);
	}	
	
	public boolean addSubmitterRemark(OTGroupSubmitterRemark submitterRemark) {
		return this.submitterRemarks.add(submitterRemark);
	}	
	
	public boolean addReceiverRemark(OTGroupReceiverRemark receivererRemark) {
		return this.receiverRemarks.add(receivererRemark);
	}	
	public boolean addChildGroup(OTGroup childGroup) {
		return this.children.add(childGroup);
	}
	public String getStatusCode() {
		return this.status.getCode();
	}
	public boolean isSubmitable() {
		return ( OTGroupStatusUtil.isSubmitable(this.getStatusCode()));
	}
//	public boolean isCancelable() {
//		return ( OTGroupStatusUtil.isCancelable(this.getStatusCode()));
//	}		
	public boolean isReceivable() {
		return ( OTGroupStatusUtil.isReceivable(this.getStatusCode()));
	}	
	public boolean isModifiable() {
		return ( OTGroupStatusUtil.isModifiable(this.getStatusCode()));
	}
	public  boolean isReviewable() {
		return ( OTGroupStatusUtil.isReviewable(this.getStatusCode()));
	}
//	public  boolean isFinalizable() {
//		return ( OTGroupStatusUtil.isFinalizable(this.getStatusCode()));
//	}
	public boolean isSubmitted() {
		return OTGroupStatus.CodeValues.SUBMITTED.equals(this.status.getCode());
	}
	public boolean isPending() {
		return (OTGroupStatus.CodeValues.PENDING.equals(this.status.getCode()));
	}	
	public boolean isReceived() {
		return (OTGroupStatus.CodeValues.RECEIVED.equals(this.status.getCode()));
	}
	public boolean isFinalized() {
		return (OTGroupStatus.CodeValues.FINAL.equals(this.status.getCode()));
	}	
	public boolean isCancelled() {
		return (OTGroupStatus.CodeValues.CANCELLED.equals(this.status.getCode()));
	}	
	public List<OTHeader> getEmployees() {
		return employees;
	}
	
	public void setEmployees(List<OTHeader> employees) {
		this.employees = employees;
	}

	// METHOD REQUIRED BY OTGroupRow INTERFACE
	public String getPayPeriodDateRange() {
		if ( this.payPeriod != null) {
			return this.payPeriod.getPayPeriodDateRange();
		} else {
			return null;
		}
	}
	// METHOD REQUIRED BY OTGroupRow INTERFACE
	public String getSubmitterName() {
		if ( this.submitter != null) {
			return this.submitter.getFullName();
		} else {
			return null;
		}
	}
	// METHOD REQUIRED BY OTGroupRow INTERFACE
	public String getReceiverName() {
		if ( this.receiver != null) {
			return this.receiver.getFullName();
		} else {
			return null;
		}
	}
	// METHOD REQUIRED BY OTGroupRow INTERFACE
	public long getGroupId() {
		return this.id;
	}

	// METHOD REQUIRED BY OTGroupRow INTERFACE
	public String getStatusName() {
		if  (this.status != null) {
			return this.status.getName();
		} else {
			return null;
		}
	}
	// METHOD REQUIRED BY OTGroupRow INTERFACE
	public Long getReceiverUserId() {
		if ( this.receiver != null) {
			return this.receiver.getUserId();
		} else {
			return null;
		}
	}
	// METHOD REQUIRED BY OTGroupRow INTERFACE
	public long getSubmitterUserId() {
		if ( this.submitter != null) {
			return this.submitter.getUserId();
		} else {
			return 0L;
		}
	}
	// METHOD REQUIRED BY OTGroupRow INTERFACE
	public List<OTHeader> getAllEmployees() {		
		if ( this.allEmployees == null) {
			// THE FOLLOWING IS BEING DONE TO "FLATTEN" THE GROUP HIERARCHY:
			this.allEmployees = new ArrayList<OTHeader>();

			// FIRST PLACE EMPLOYEES DIRECTLY BELONGING TO "THIS" GROUP IN THE "ALL EMPLOYEE" LIST
			this.allEmployees.addAll(this.employees);
			// THEN PLACE ALL THE EMPLOYEES BELONGING TO THEIR RESPECTIVE CHILD GROUP IN "ALL EMPLOYEE: LIST
			this.buildAllEmployeeList(this);
		}
		return this.allEmployees;
	}
	// HELPER METHOD
	private void buildAllEmployeeList(OTGroup otGroup) {
		for ( OTGroup childGroup : otGroup.getChildren() ) {
			this.allEmployees.addAll(childGroup.getEmployees());
			this.buildAllEmployeeList(childGroup);
		}
	}

	public List<OTHeader> getApprovableRequests() {
		List<OTHeader> approvableRequests = new ArrayList<OTHeader>();
		List<OTHeader> allRequests = this.getAllEmployees();
		
		for ( OTHeader otHeader : allRequests) {
			OTDetail otDetail = otHeader.getLatestDetail();
			if 	( (otDetail != null)
					&& (otDetail.isReceived() || otDetail.isModified())
			) {
				approvableRequests.add(otHeader);
			}
		}
		return approvableRequests;
	}

	public List<OTSalaryGradeGroup> getSalaryGradeGroupList() {
		if ( this.salaryGradeGroupList == null) {
			this.salaryGradeGroupList = new ArrayList<OTSalaryGradeGroup>(this.createSalaryGradeGroupMap().values());
		}
		return this.salaryGradeGroupList;
	}
	
	private Map<String, OTSalaryGradeGroup> createSalaryGradeGroupMap() {
		Map<String, OTSalaryGradeGroup> salaryGradeMap = new TreeMap<String, OTSalaryGradeGroup>();
		
		for (OTHeader otHeader : this.getAllEmployees()) {
			OTDetail otDetail = otHeader.getLatestDetail();
			
			if ( (otDetail.isDenied() == false) 
					&& (otDetail.isCancelled() == false) ) {

				String salaryGradeKey = otHeader.getSalaryGrade().getKey();
				OTSalaryGradeGroup salaryGradeGroup = salaryGradeMap.get(salaryGradeKey);
				if ( salaryGradeGroup == null ) {
					salaryGradeGroup = new OTSalaryGradeGroup(salaryGradeKey);
					salaryGradeMap.put(salaryGradeKey, salaryGradeGroup);
				}
				salaryGradeGroup.incrementRequestCount();
				salaryGradeGroup.addToEstNbrOfHours(otHeader.getEstNbrOfHours());
			}
		}
		return salaryGradeMap;
	}
	
	public List<OTRequestTypeGroup> getRequestTypeGroupList() {
		if ( this.requestTypeGroupList == null) {
			this.requestTypeGroupList = this.createRequestTypeGroupList();
		}
		return this.requestTypeGroupList;
	}	
	
	private List<OTRequestTypeGroup> createRequestTypeGroupList() {
		List< OTRequestTypeGroup> requestTypeGroupList = new ArrayList<OTRequestTypeGroup>();
		

		//OTRequestTypeGroup receivedGroup 	= new OTRequestTypeGroup(OTIndivStatus.NameValues.RECEIVED, this.getReceivedRequestCount(), this.getReceivedNbrOfHours());
		//OTRequestTypeGroup modifedGroup 	= new OTRequestTypeGroup(OTIndivStatus.NameValues.MODIFIED, this.getModifiedRequestCount(), this.getModifiedNbrOfHours());
		//OTRequestTypeGroup receivedAndModifiedGroup = new OTRequestTypeGroup(OTIndivStatus.NameValues.RECEIVED, this.getReceivedRequestCount(), this.getReceivedAndModifedNbrOfHours());

		OTRequestTypeGroup approvedGroup	= new OTRequestTypeGroup(OTIndivStatus.NameValues.APPROVED, this.getApprovedRequestCount(), this.getApprovedNbrOfHours());
		OTRequestTypeGroup receivedGroup 	= new OTRequestTypeGroup(OTIndivStatus.NameValues.RECEIVED, this.getReceivedRequestCount(), this.getReceivedNbrOfHours());
		OTRequestTypeGroup modifedGroup 	= new OTRequestTypeGroup(OTIndivStatus.NameValues.MODIFIED, this.getModifiedRequestCount(), this.getModifiedNbrOfHours());
		OTRequestTypeGroup deniedGroup 		= new OTRequestTypeGroup(OTIndivStatus.NameValues.DENIED, this.getDeniedRequestCount(), this.getDeniedNbrOfHours());
		OTRequestTypeGroup cancelledGroup 	= new OTRequestTypeGroup(OTIndivStatus.NameValues.CANCELLED, this.getCancelledRequestCount(), this.getCancelledNbrOfHours());
		OTRequestTypeGroup allGroups  		= new OTRequestTypeGroup("Total", this.getTotalRequestCount(), this.getTotalNbrOfHours());
		
		requestTypeGroupList.add(approvedGroup);
		requestTypeGroupList.add(receivedGroup);
		requestTypeGroupList.add(modifedGroup);
		requestTypeGroupList.add(deniedGroup);
		requestTypeGroupList.add(cancelledGroup);
		requestTypeGroupList.add(allGroups);
		
		return requestTypeGroupList;
	}	
	
//	private List<OTRequestTypeGroup> createApprovedAndDeniedGroupList() {
//		List< OTRequestTypeGroup> requestTypeGroupList = new ArrayList<OTRequestTypeGroup>();
//		
//
//		//OTRequestTypeGroup receivedGroup 	= new OTRequestTypeGroup(OTIndivStatus.NameValues.RECEIVED, this.getReceivedRequestCount(), this.getReceivedNbrOfHours());
//		//OTRequestTypeGroup modifedGroup 	= new OTRequestTypeGroup(OTIndivStatus.NameValues.MODIFIED, this.getModifiedRequestCount(), this.getModifiedNbrOfHours());
//		//OTRequestTypeGroup receivedAndModifiedGroup = new OTRequestTypeGroup(OTIndivStatus.NameValues.RECEIVED, this.getReceivedRequestCount(), this.getReceivedAndModifedNbrOfHours());
//
//		OTRequestTypeGroup approvedGroup	= new OTRequestTypeGroup(OTIndivStatus.NameValues.APPROVED, this.getApprovedRequestCount(), this.getApprovedNbrOfHours());
//		OTRequestTypeGroup receivedGroup 	= new OTRequestTypeGroup(OTIndivStatus.NameValues.RECEIVED, this.getReceivedRequestCount(), this.getReceivedNbrOfHours());
//		OTRequestTypeGroup modifedGroup 	= new OTRequestTypeGroup(OTIndivStatus.NameValues.MODIFIED, this.getModifiedRequestCount(), this.getModifiedNbrOfHours());
//		OTRequestTypeGroup deniedGroup 		= new OTRequestTypeGroup(OTIndivStatus.NameValues.DENIED, this.getDeniedRequestCount(), this.getDeniedNbrOfHours());
//		OTRequestTypeGroup cancelledGroup 	= new OTRequestTypeGroup(OTIndivStatus.NameValues.CANCELLED, this.getCancelledRequestCount(), this.getCancelledNbrOfHours());
//		OTRequestTypeGroup allGroups  		= new OTRequestTypeGroup("Total", this.getTotalRequestCount(), this.getTotalNbrOfHours());
//		
//		requestTypeGroupList.add(approvedGroup);
//		requestTypeGroupList.add(receivedGroup);
//		requestTypeGroupList.add(modifedGroup);
//		requestTypeGroupList.add(deniedGroup);
//		requestTypeGroupList.add(cancelledGroup);
//		requestTypeGroupList.add(allGroups);
//		
//		return requestTypeGroupList;
//	}		
	
	
	
	// THE FOLLOWING IS BEING DONE TO "FLATTEN" THE GROUP HIERARCHY:
	public List<OTGroup> getAllChildGroups() {		
		if ( this.allChildGroups == null) {
			this.allChildGroups = new ArrayList<OTGroup>();
			this.buildAllChildGroupsList(this);
		}
		return this.allChildGroups;
	}	

	// HELPER METHOD
	private void buildAllChildGroupsList(OTGroup otGroup) {
		for ( OTGroup childGroup : otGroup.getChildren() ) {
			this.allChildGroups.add(childGroup);
			this.buildAllChildGroupsList(childGroup);
		}
	}	
	// METHOD REQUIRED BY OTGroupRow INTERFACE
	public int getEmployeeCount() {
		int employeeCount = 0;
		for ( OTHeader otHeader : this.getAllEmployees()) {
			OTDetail otDetail = otHeader.getLatestDetail();
			if 	( (otDetail != null)
					&& (!otDetail.isDenied()) 
					&& (!otDetail.isCancelled())
				) {
				employeeCount++;
			}
		}
		return employeeCount;
	}
	public int getReceivedRequestCount() {
		int receivedCount = 0;
		for ( OTHeader otHeader : this.getAllEmployees()) {
			OTDetail otDetail = otHeader.getLatestDetail();
			if 	( (otDetail != null)
					&& (otDetail.isReceived())
				) {
				receivedCount++;
			}
		}
		return receivedCount;
	}	
	public int getModifiedRequestCount() {
		int modifiedCount = 0;
		for ( OTHeader otHeader : this.getAllEmployees()) {
			OTDetail otDetail = otHeader.getLatestDetail();
			if 	( (otDetail != null)
					&& (otDetail.isModified())
				) {
				modifiedCount++;
			}
		}
		return modifiedCount;
	}	
	
	public int getApprovedRequestCount() {
		int approvedCount = 0;
		for ( OTHeader otHeader : this.getAllEmployees()) {
			OTDetail otDetail = otHeader.getLatestDetail();
			if 	( (otDetail != null)
					&& (otDetail.isApproved())
				) {
				approvedCount++;
			}
		}
		return approvedCount;
	}	
	public int getDeniedRequestCount() {
		int deniedCount = 0;
		for ( OTHeader otHeader : this.getAllEmployees()) {
			OTDetail otDetail = otHeader.getLatestDetail();
			if 	( (otDetail != null)
					&& (otDetail.isDenied())
				) {
				deniedCount++;
			}
		}
		return deniedCount;
	}		
	public int getCancelledRequestCount() {
		int cancelledCount = 0;
		for ( OTHeader otHeader : this.getAllEmployees()) {
			OTDetail otDetail = otHeader.getLatestDetail();
			if 	( (otDetail != null)
					&& (otDetail.isCancelled())
				) {
				cancelledCount++;
			}
		}
		return cancelledCount;
	}			
	public int getTotalRequestCountExcludeCancelled() {
		int totalCount = 0;
		for ( OTHeader otHeader : this.getAllEmployees()) {
			OTDetail otDetail = otHeader.getLatestDetail();
			if 	( (otDetail != null)
					&& (!otDetail.isCancelled())
				) {
				totalCount++;
			}
		}
		return totalCount;
	}	
	public int getTotalRequestCount() {
		return ( this.getAllEmployees().size());
	}		
	
	
	// METHOD REQUIRED BY OTGroupRow INTERFACE
	public BigDecimal getEstNbrOfHrs() {
		BigDecimal estNbrOfHours = BigDecimal.ZERO;
		for ( OTHeader otHeader : this.getAllEmployees()) {
			OTDetail otDetail = otHeader.getLatestDetail();
			if 	( (otDetail != null)
					&& (!otDetail.isDenied()) 
					&& (!otDetail.isCancelled())
				) {
				estNbrOfHours = estNbrOfHours.add(otHeader.getEstNbrOfHours());
			}
		}				
		return estNbrOfHours;
	}	
	public BigDecimal getReceivedNbrOfHours() {
		BigDecimal receivedNbrOfHours = BigDecimal.ZERO;
		for ( OTHeader otHeader : this.getAllEmployees()) {
			OTDetail otDetail = otHeader.getLatestDetail();
			if 	( (otDetail != null)
					&& (otDetail.isReceived())
				) {
				receivedNbrOfHours = receivedNbrOfHours.add(otHeader.getEstNbrOfHours());
			}
		}				
		return receivedNbrOfHours;
	}	
	
	public BigDecimal getModifiedNbrOfHours() {
		BigDecimal modifiedNbrOfHours = BigDecimal.ZERO;
		for ( OTHeader otHeader : this.getAllEmployees()) {
			OTDetail otDetail = otHeader.getLatestDetail();
			if 	( (otDetail != null)
					&& (otDetail.isModified())
				) {
				modifiedNbrOfHours = modifiedNbrOfHours.add(otHeader.getEstNbrOfHours());
			}
		}				
		return modifiedNbrOfHours;
	}
	public BigDecimal getReceivedAndModifedNbrOfHours() {
		BigDecimal receivedNbrOfHours = BigDecimal.ZERO;
		for ( OTHeader otHeader : this.getAllEmployees()) {
			OTDetail otDetail = otHeader.getLatestDetail();
			if 	( (otDetail != null)
					&& (otDetail.isReceived() || otDetail.isModified())
				) {
				receivedNbrOfHours = receivedNbrOfHours.add(otHeader.getEstNbrOfHours());
			}
		}				
		return receivedNbrOfHours;
	}	

	
	
	
	
	public BigDecimal getApprovedNbrOfHours() {
		BigDecimal approvedNbrOfHours = BigDecimal.ZERO;
		for ( OTHeader otHeader : this.getAllEmployees()) {
			OTDetail otDetail = otHeader.getLatestDetail();
			if 	( (otDetail != null)
					&& (otDetail.isApproved())
				) {
				approvedNbrOfHours = approvedNbrOfHours.add(otHeader.getEstNbrOfHours());
			}
		}				
		return approvedNbrOfHours;
	}	
	public BigDecimal getDeniedNbrOfHours() {
		BigDecimal deniedNbrOfHours = BigDecimal.ZERO;
		for ( OTHeader otHeader : this.getAllEmployees()) {
			OTDetail otDetail = otHeader.getLatestDetail();
			if 	( (otDetail != null)
					&& (otDetail.isDenied())
				) {
				deniedNbrOfHours = deniedNbrOfHours.add(otHeader.getEstNbrOfHours());
			}
		}				
		return deniedNbrOfHours;
	}	
	public BigDecimal getCancelledNbrOfHours() {
		BigDecimal cancelledNbrOfHours = BigDecimal.ZERO;
		for ( OTHeader otHeader : this.getAllEmployees()) {
			OTDetail otDetail = otHeader.getLatestDetail();
			if 	( (otDetail != null)
					&& (otDetail.isCancelled())
				) {
				cancelledNbrOfHours = cancelledNbrOfHours.add(otHeader.getEstNbrOfHours());
			}
		}				
		return cancelledNbrOfHours;
	}	
//	public BigDecimal getTotalNbrOfHours() {
//		BigDecimal estNbrOfHours = BigDecimal.ZERO;
//		for ( OTHeader otHeader : this.getAllEmployees()) {
//			OTDetail otDetail = otHeader.getLatestDetail();
//			if 	( (otDetail != null)
//					&& (!otDetail.isCancelled())
//				) {
//				estNbrOfHours = estNbrOfHours.add(otHeader.getEstNbrOfHours());
//			}
//		}				
//		return estNbrOfHours;
//	}
	public BigDecimal getTotalNbrOfHours() {
		BigDecimal totalEstNbrOfHours = BigDecimal.ZERO;
		for ( OTHeader otHeader : this.getAllEmployees()) {
			totalEstNbrOfHours = totalEstNbrOfHours.add(otHeader.getEstNbrOfHours());
		}
		return totalEstNbrOfHours;
	}	

	
	public List<AlohaUser> getAllAuthorizedIndividuals() {
		if ( this.allAuthorizedIndividuals == null ) {
			this.allAuthorizedIndividuals = new ArrayList<AlohaUser>();
			this.buildAllAuthorizedIndividualsList(this);
		}
		return this.allAuthorizedIndividuals;
	}
	// HELPER METHOD
	private void buildAllAuthorizedIndividualsList(OTGroup otGroup) {
		if ( otGroup != null ) {
			if ( otGroup.getSubmitter() != null ) {
				this.allAuthorizedIndividuals.add(otGroup.getSubmitter());
			}
			if ( otGroup.getReceiver() != null ) {
				this.allAuthorizedIndividuals.add(otGroup.getReceiver());
			}
			this.buildAllAuthorizedIndividualsList(otGroup.getParent());
		}		
	}
	public OTGroup getTopLevelParent() {
		if ( this.topLevelGroup == null ) {
			this.assignTopLevelParent(this);
		}
		return this.topLevelGroup;
		
	}
	private void assignTopLevelParent(OTGroup otGroup) {
		if ( otGroup.getParent() == null) {
			this.topLevelGroup = otGroup;
		} else {
			this.assignTopLevelParent(otGroup.getParent());
		}
	}

	public boolean hasParent() {
		return (this.parent != null);
	}

	public boolean isCancellable() {
		return cancellable;
	}
	public void setCancellable(boolean cancellable) {
		this.cancellable = cancellable;
	}
	
	public boolean isFinalizable() {
		return finalizable;
	}

	public void setFinalizable(boolean finalizable) {
		this.finalizable = finalizable;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (id ^ (id >>> 32));
		result = prime * result
				+ ((payPeriod == null) ? 0 : payPeriod.hashCode());
		result = prime * result
				+ ((receiver == null) ? 0 : receiver.hashCode());
		result = prime * result + ((status == null) ? 0 : status.hashCode());
		result = prime * result
				+ ((submitter == null) ? 0 : submitter.hashCode());
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
		OTGroup other = (OTGroup) obj;
		if (id != other.id)
			return false;
		if (payPeriod == null) {
			if (other.payPeriod != null)
				return false;
		} else if (!payPeriod.equals(other.payPeriod))
			return false;
		if (receiver == null) {
			if (other.receiver != null)
				return false;
		} else if (!receiver.equals(other.receiver))
			return false;
		if (status == null) {
			if (other.status != null)
				return false;
		} else if (!status.equals(other.status))
			return false;
		if (submitter == null) {
			if (other.submitter != null)
				return false;
		} else if (!submitter.equals(other.submitter))
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("OTGroup [id=");
		builder.append(id);
		builder.append(", payPeriod=");
		builder.append(payPeriod);
		builder.append(", status=");
		builder.append(status);
		builder.append(", submitter=");
		builder.append(submitter);
		builder.append(", receiver=");
		builder.append(receiver);
		builder.append("]");
		return builder.toString();
	}
}