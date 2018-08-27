package gov.gsa.ocfo.aloha.model.entity.leave;

import gov.gsa.ocfo.aloha.model.PayPeriod;
import gov.gsa.ocfo.aloha.model.ScheduleItem;
import gov.gsa.ocfo.aloha.model.entity.AlohaUser;
import gov.gsa.ocfo.aloha.model.entity.AuditTrail;
import gov.gsa.ocfo.aloha.util.StringUtil;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.persistence.Version;


/**
 * The persistent class for the LR_DETAIL database table.
 * 
 */
@Entity
@Table(name="LR_DETAIL", schema="ALOHA")

@NamedQueries({
	@NamedQuery(name="findLeaveDetailByApprover",
			query="SELECT row FROM LeaveDetail row " +
					"WHERE row.approver.userId = :approverUserId " +
					"AND row.leaveStatus.code <> :leaveStatusCode " +
					"ORDER BY row.leaveHeader.id DESC, row.sequence DESC"),
	@NamedQuery(name="findLeaveDetailByApproverAndStatusCode",
			query="SELECT row FROM LeaveDetail row WHERE row.approver.userId = :approverUserId AND row.leaveStatus.code = :leaveStatusCode ORDER BY row.leaveHeader.id DESC"),
	@NamedQuery(name="findLeaveDetailBySubmitter",
			query="SELECT row FROM LeaveDetail row WHERE row.submitter.userId = :submitterUserId ORDER BY row.leaveHeader.id DESC, row.sequence DESC"),
	@NamedQuery(name="findLeaveDetailForSubmitOwn",
				query="SELECT row FROM LeaveDetail row " +
						"WHERE row.leaveHeader.employee.userId = :submitOwnUserId " +				
//						"WHERE row.submitter.userId = :submitOwnUserId " +
//						"AND row.leaveHeader.employee.userId = :submitOwnUserId " +
						"AND row.leaveStatus.code <> :leaveStatusCode " +
						"ORDER BY row.leaveHeader.id DESC, row.sequence DESC"),
	@NamedQuery(name="findLeaveDetailForOnBehalfOf",
				query="SELECT row FROM LeaveDetail row " +
						"WHERE row.submitter.userId = :onBehalfOfUserId " +
						"AND row.leaveHeader.employee.userId <> :onBehalfOfUserId " +
						"AND row.leaveStatus.code <> :leaveStatusCode " +
						"ORDER BY row.leaveHeader.id DESC, row.sequence DESC")
})
public class LeaveDetail implements Serializable {

	/********************************************************************************************
	 IMPORTANT 
	---------------------------------------------------------------------------------------------
	Starting with the 0.7.5 release, ALOHA will NO LONGER write to the following tables:
	1) LR_APPROVER_COMMENT
	2) LR_SUBMITTER_COMMENT

	Instead, all remarks will be inserted into the LR_HISTORY table, using the REMARKS column.

	ALOHA will continue to read from these tables for backwards compatability puposes. To display
	all previous remarks, ALOHA will read from the following tables:
	1) LR_APPROVER_COMMENT
	2) LR_SUBMITTER_COMMENT
	3) LR_HISTORY

	To see how this is being done, look at the "getAllRemarks()" method in 
	gov.gsa.ocfo.aloha.model.entity.leaveLeaveHeader.

	-- Lee TRent (09/23/2013)

	********************************************************************************************/	
	
	private static final long serialVersionUID = -1219393815434155822L;

	public interface QueryNames{
		public static final String FIND_BY_APPROVER = "findLeaveDetailByApprover";
		public static final String FIND_BY_APPROVER_AND_STATUS_CODE = "findLeaveDetailByApproverAndStatusCode";
		public static final String FIND_BY_SUBMITTER = "findLeaveDetailBySubmitter";		
		public static final String FIND_FOR_SUBMIT_OWN = "findLeaveDetailForSubmitOwn";
		public static final String FIND_FOR_ON_BEHALF_OF = "findLeaveDetailForOnBehalfOf";		
	}
	public interface QueryParamNames {
		public static final String APPROVER_USER_ID = "approverUserId";	
		public static final String SUBMITTER_USER_ID = "submitterUserId";
		public static final String SUBMIT_OWN_USER_ID = "submitOwnUserId";		
		public static final String ON_BEHALF_OF_USER_ID = "onBehalfOfUserId";		
		public static final String LEAVE_STATUS_CODE = "leaveStatusCode";
	}
	
	@Id
	@SequenceGenerator(name="LR_DETAIL_ID_GENERATOR", sequenceName="ALOHA.SEQ_LR_DETAIL", allocationSize=1)
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="LR_DETAIL_ID_GENERATOR")
	@Column(name="LR_DETAIL_ID", unique=true)
	private long id;	

	//bi-directional many-to-one association to LeaveHeader
    @ManyToOne
	@JoinColumn(name="LR_HEADER_ID", referencedColumnName="LR_HEADER_ID", nullable=false)
	private LeaveHeader leaveHeader;

    @ManyToOne
	@JoinColumn(name="LR_STATUS_ID",  referencedColumnName="LR_STATUS_ID", nullable=false)
	private LeaveStatus leaveStatus;
	
    @ManyToOne
	@JoinColumn(name="SUBMITTER_USER_ID",  referencedColumnName="USER_ID", nullable=false)	
	private AlohaUser submitter;
	
    @ManyToOne
	@JoinColumn(name="APPROVER_USER_ID",  referencedColumnName="USER_ID", nullable=true)	
	private AlohaUser approver;   
    
    @Column(name="LR_DETAIL_SEQUENCE", nullable=false, precision=5) 
    private int sequence;

	@OneToMany(mappedBy="leaveDetail", fetch=FetchType.LAZY, cascade=CascadeType.ALL)
	@OrderBy("leaveDate")
	private List<LeaveItem> leaveItems = new ArrayList<LeaveItem>();

	@OneToMany(mappedBy="leaveDetail", fetch=FetchType.LAZY, cascade=CascadeType.ALL)
	@OrderBy("id")
	private List<LeaveHistory> leaveHistories  = new ArrayList<LeaveHistory>();

	/***************************************************************************************
	    *** IMPORTANT: submitterComments is "READ-ONLY" as of the 0.7.5 release ***
	 ***************************************************************************************/
	@OneToMany(mappedBy="leaveDetail", fetch=FetchType.LAZY, cascade=CascadeType.ALL)
	private List<LeaveSubmitterComment> submitterComments = new ArrayList<LeaveSubmitterComment>();

	/***************************************************************************************
	    *** IMPORTANT: approverComments is "READ-ONLY" as of the 0.7.5 release ***
	 ***************************************************************************************/
	@OneToMany(mappedBy="leaveDetail", fetch=FetchType.LAZY, cascade=CascadeType.ALL)
	private List<LeaveApproverComment> approverComments = new ArrayList<LeaveApproverComment>();
	
	@Temporal(TemporalType.DATE)
	@Column(name="PP_START_DATE", nullable=false)
	private Date payPeriodStartDate;
	
	@Temporal(TemporalType.DATE)
	@Column(name="PP_END_DATE", nullable=false)
	private Date payPeriodEndDate;

	@Transient
	private String payPeriodDateRange;
	
	@Embedded
	private AuditTrail auditTrail;
	
	@Version
	@Column(name="OPT_LOCK_NBR")
	private long version;
	
	@Column(name="DISABLED_VET_CERT", nullable=false)
	private int disabledVetCert = 0;
	
	public LeaveDetail() {
    }

	public long getId() {
		return this.id;
	}

	public void setId(long id) {
		this.id = id;
	}
	
	public AlohaUser getSubmitter() {
		return submitter;
	}

	public void setSubmitter(AlohaUser submitter) {
		this.submitter = submitter;
	}

	public AlohaUser getApprover() {
		return approver;
	}

	public void setApprover(AlohaUser approver) {
		this.approver = approver;
	}

	public LeaveHeader getLeaveHeader() {
		return this.leaveHeader;
	}

	public void setLeaveHeader(LeaveHeader leaveHeader) {
		this.leaveHeader = leaveHeader;
	}
	
	public LeaveStatus getLeaveStatus() {
		return this.leaveStatus;
	}

	public void setLeaveStatus(LeaveStatus leaveStatus) {
		this.leaveStatus = leaveStatus;
	}
	
	public List<LeaveItem> getLeaveItems() {
		return this.leaveItems;
	}

	public void setLeaveItems(List<LeaveItem> leaveItems) {
		this.leaveItems = leaveItems;
	}
	
	public List<LeaveHistory> getLeaveHistories() {
		return this.leaveHistories;
	}

	public void setLeaveHistories(List<LeaveHistory> leaveHistories) {
		this.leaveHistories = leaveHistories;
	}

	public boolean addLeaveItem(LeaveItem leaveItem) {
		return this.leaveItems.add(leaveItem);
	}
	public boolean addLeaveHistory(LeaveHistory leaveHistory) {
		return this.leaveHistories.add(leaveHistory);
	}
	public boolean addSubmitterComment(LeaveSubmitterComment lsComment) {
		return this.submitterComments.add(lsComment);
	}

	public boolean addApproverComment(LeaveApproverComment laComment) {
		return this.approverComments.add(laComment);
	}
	
	/***************************************************************************************
	    *** IMPORTANT: submitterComments is "READ-ONLY" as of the 0.7.5 release ***
	 ***************************************************************************************/
	public List<LeaveSubmitterComment> getSubmitterComments() {
		return submitterComments;
	}

	/***************************************************************************************
	    *** IMPORTANT: submitterComments is "READ-ONLY" as of the 0.7.5 release ***
	 ***************************************************************************************/
	public void setSubmitterComments(List<LeaveSubmitterComment> submitterComments) {
		this.submitterComments = submitterComments;
	}

	/***************************************************************************************
	    *** IMPORTANT: approverComments is "READ-ONLY" as of the 0.7.5 release ***
	 ***************************************************************************************/
	public List<LeaveApproverComment> getApproverComments() {
		return approverComments;
	}

	/***************************************************************************************
	    *** IMPORTANT: approverComments is "READ-ONLY" as of the 0.7.5 release ***
	 ***************************************************************************************/
	public void setApproverComments(List<LeaveApproverComment> approverComments) {
		this.approverComments = approverComments;
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
	public int getSequence() {
		return sequence;
	}

	public void setSequence(int sequence) {
		this.sequence = sequence;
	}

	public AuditTrail getAuditTrail() {
		return auditTrail;
	}

	public void setAuditTrail(AuditTrail auditTrail) {
		this.auditTrail = auditTrail;
	}

	public BigDecimal getTotalLeaveHours() {
		BigDecimal  total = ScheduleItem.ZERO_LEAVE_HOURS;
		for ( LeaveItem item: this.leaveItems) {
			total = total.add(item.getLeaveHours());
		}
		total.setScale(1, RoundingMode.HALF_UP);
		return total;
	}	
	
	public String getEmployeeName() {
		return this.leaveHeader.getEmployee().getFullName();
	}
	public String getSubmitterName() {
		return this.submitter.getFullName();
	}
	public AlohaUser getEmployee() {
		if ( this.leaveHeader != null ) {
			return this.leaveHeader.getEmployee();
		}
		return null;
	}
	public Date getDateSubmitted() {
		return (this.leaveHeader.getAuditTrail().getDateLastUpdated());
	}
	public Date getDateLastModified() {
		return (this.auditTrail.getDateLastUpdated());
	}
	public long getLeaveHeaderId() {
		return this.leaveHeader.getId();
	}
	public String getLeaveHeaderIdAsString() {
		return Long.toString(this.leaveHeader.getId());
	}
	
	public boolean isSubmitted() {
		return (this.leaveStatus.getCode().trim().equals(LeaveStatus.CodeValues.SUBMITTED));
	}
	public boolean isCancelled() {
		return (this.leaveStatus.getCode().trim().equals(LeaveStatus.CodeValues.CANCELLED));
	}
	public boolean isApproved() {
		return (this.leaveStatus.getCode().trim().equals(LeaveStatus.CodeValues.APPROVED));
	}
	public boolean isDenied() {
		return (this.leaveStatus.getCode().trim().equals(LeaveStatus.CodeValues.DENIED));
	}
	public boolean isPendingAmendment() {
		return (LeaveStatus.CodeValues.PEND_AMEND.equals(this.leaveStatus.getCode().trim()));
	}
	public boolean isPendingWithdrawal() {
		return (LeaveStatus.CodeValues.PEND_WITHDRAW.equals(this.leaveStatus.getCode().trim()));
	}
	public boolean isAmended() {
		return (LeaveStatus.CodeValues.AMENDED.equals(this.leaveStatus.getCode().trim()));
	}
	public boolean isWithdrawn() {
		return (LeaveStatus.CodeValues.WITHDRAWN.equals(this.leaveStatus.getCode().trim()));
	}
	public boolean isActionable() {
		return	( (this.isApprovable()) || (this.isAmendable())	);
	}
	public long getVersion() {
		return version;
	}
	public void setVersion(long version) {
		this.version = version;
	}
	
	public boolean isEmployee(long id) {
		return (this.getEmployee().getUserId() == id);
	}
	
	public boolean isApprover(long id) {
		return (this.approver.getUserId() == id);
	}

	public boolean isSubmitter(long id) {
		return (this.submitter.getUserId() == id);
	}

	public boolean isHasMultipleSubmitterComments() {
		return this.submitterComments.size() > 1;
	}
	public boolean isHasMultipleApproverComments() {
		return this.approverComments.size() > 1;
	}	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (id ^ (id >>> 32));
		result = prime * result
				+ ((leaveHeader == null) ? 0 : leaveHeader.hashCode());
		result = prime
				* result
				+ ((payPeriodEndDate == null) ? 0 : payPeriodEndDate.hashCode());
		result = prime
				* result
				+ ((payPeriodStartDate == null) ? 0 : payPeriodStartDate
						.hashCode());
		result = prime * result + sequence;
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
		LeaveDetail other = (LeaveDetail) obj;
		if (id != other.id)
			return false;
		if (leaveHeader == null) {
			if (other.leaveHeader != null)
				return false;
		} else if (!leaveHeader.equals(other.leaveHeader))
			return false;
		if (payPeriodEndDate == null) {
			if (other.payPeriodEndDate != null)
				return false;
		} else if (!payPeriodEndDate.equals(other.payPeriodEndDate))
			return false;
		if (payPeriodStartDate == null) {
			if (other.payPeriodStartDate != null)
				return false;
		} else if (!payPeriodStartDate.equals(other.payPeriodStartDate))
			return false;
		if (sequence != other.sequence)
			return false;
		return true;
	}

	// CANCELLABLE
	public boolean isCancelable() {
		return this.leaveHeader.isCancelable();
	}
	public boolean isCancelableByThisUser(AlohaUser user) {
		return this.leaveHeader.isCancelableByThisUser(user);
	}	
	
	// AMENDABLE
	public boolean isAmendable() {
		return this.leaveHeader.isAmendable();
	}
	public boolean isAmendableByThisUser(AlohaUser user) {
		return this.leaveHeader.isAmendableByThisUser(user);
	}	
	
	// WITHDRAWABLE
	public boolean isWithdrawable() {
		return this.leaveHeader.isWithdrawable();
	}
	public boolean isWithdrawableByThisUser(AlohaUser user) {
		return this.leaveHeader.isWithdrawableByThisUser(user);
	}
	
	// APPROVEABLE
	public boolean isApprovable() {
		return this.leaveHeader.isApprovable();
	}
	public boolean isApprovableByThisUser(AlohaUser user) {
		return this.leaveHeader.isApprovableByThisUser(user);
	}
	
	// CHANGE OF SUPERVISOR ALLOWED
	public boolean isChangeOfSupervisorAllowed() {
		return ( this.leaveHeader.isChangeOfSupervisorAllowed() );
	}
	public boolean isChangeOfSupervisorAllowedByThisUser(AlohaUser user) {
		return ( this.leaveHeader.isChangeOfSupervisorAllowedByThisUser(user) );
	}
	
	public String getLeaveRequestId() {
		return ( (this.leaveHeader == null) ? String.valueOf(0) : this.leaveHeader.getRequestId() );
	}
	public LeaveHistory getLatestHistoricalEntry() {
		if ( !this.leaveHistories.isEmpty()) {
			return ( this.leaveHistories.get(this.leaveHistories.size() -1));
		}
		return null;
	}		
	public String toEventLog() {
		StringBuilder sb = new StringBuilder();
		LeaveHistory history = this.getLatestHistoricalEntry();
		if ( (this.leaveHeader != null) && (history != null) && (history.getLeaveStatusTransition() != null)) {
			sb.append( history.getLeaveStatusTransition().getActionLabel());			
			sb.append(" [");
			sb.append("requestId=");
			sb.append(this.getLeaveRequestId());
			sb.append(";statusCodeChange=");
			sb.append(history.getLeaveStatusTransition().getActionCode());
			sb.append(";peformedBy=");
			sb.append(this.getLatestHistoricalEntry().getActor().getFullName());
			sb.append(";employee=");
			sb.append(this.getEmployee().getFullName());
			sb.append(";submitter=");
			sb.append(this.submitter.getFullName());
			sb.append(";supervisor=");
			sb.append(this.approver.getFullName());
			sb.append(";payPeriod=");
			sb.append(this.getPayPeriodDateRange());
			sb.append("]");
		}
		return sb.toString();
	}	
	
	public List<LeaveSubmitterComment> getSubmitterCommentsForThisDetailOnly() {
		return submitterComments;			
	}	
	
	public List<LeaveApproverComment> getApproverCommentsForThisDetailOnly() {
		return approverComments;			
	}	
	
	public List<LeaveSubmitterComment> getSubmitterRemarks() {
		if (this.leaveHeader != null) {
			return this.leaveHeader.getAllSubmitterRemarks();
		} else {
			return submitterComments;			
		}		
	}	
	public List<LeaveApproverComment> getSupervisorRemarks() {
		if (this.leaveHeader != null) {
			return this.leaveHeader.getAllSupervisorRemarks();
		} else {
			return approverComments;			
		}		
	}

	public int getDisabledVetCert() {
		return disabledVetCert;
	}

	public void setDisabledVetCert(int disabledVetCert) {
		this.disabledVetCert = disabledVetCert;
	}

	public boolean isDisabledVeteranCertified() {
		return this.getDisabledVetCert() == 1;
	}
	
	public void setDisabledVeteranCertified(boolean val) {
		if ( val == true ) {
			this.setDisabledVetCert(1);
		} else {
			this.setDisabledVetCert(0);
		}
	}
	
	public boolean isHasDisabledVetLeaveItem() { 
		if ( this.leaveItems != null ) {
			for ( LeaveItem li : this.leaveItems ) {
				if ( li.isDisabledVetLeaveItem() ) {
					return true;
				}
			}			
		}
		return false;
	}
}