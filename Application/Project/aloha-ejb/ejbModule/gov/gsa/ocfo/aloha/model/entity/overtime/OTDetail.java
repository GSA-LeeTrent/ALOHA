package gov.gsa.ocfo.aloha.model.entity.overtime;

import gov.gsa.ocfo.aloha.model.entity.AlohaUser;
import gov.gsa.ocfo.aloha.util.ot.OTIndivStatusUtil;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
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

@Entity
@Table(name="OT_DETAIL", schema="ALOHA")
@NamedQueries({
	@NamedQuery(name="retrieveOTDetailForEmployee",
				query="SELECT row FROM OTDetail row " +
						"WHERE row.header.employee.userId = :employeeUserId " +
						"ORDER BY row.header.payPeriod.key DESC, row.sequence DESC"),
	@NamedQuery(name="retrieveOTDetailForSubmitter",
				query="SELECT row FROM OTDetail row " +
						"WHERE row.submitter.userId = :submitterUserId " +		
						"AND row.header.employee.userId <> :submitterUserId " +
						"ORDER BY row.header.payPeriod.key DESC, row.sequence DESC"),						
	@NamedQuery(name="retrieveOTDetailForSupervisor",
			query="SELECT row FROM OTDetail row " +
					"WHERE row.supervisor.userId = :supervisorUserId " +
					"AND row.status.code = :otStatusCode " +
					"ORDER BY row.header.payPeriod.key DESC, row.sequence DESC"),
	@NamedQuery(name="retrieveAllOTDetailForSupervisor",
			query="SELECT row FROM OTDetail row " +
					"WHERE row.supervisor.userId = :supervisorUserId " +
					"ORDER BY row.header.type.code, row.status.code, row.header.id DESC, row.sequence DESC")
})

public class OTDetail implements Serializable {
	private static final long serialVersionUID = -1912883991940913947L;

	public interface QueryNames{
		public static final String RETRIEVE_FOR_EMPLOYEE = "retrieveOTDetailForEmployee";
		public static final String RETRIEVE_FOR_SUBMITTER = "retrieveOTDetailForSubmitter";
		public static final String RETRIEVE_FOR_SUPERVISOR = "retrieveOTDetailForSupervisor";
		public static final String RETRIEVE_ALL_FOR_SUPERVISOR = "retrieveAllOTDetailForSupervisor";
	}
	public interface QueryParamNames {
		public static final String EMPLOYEE_USER_ID = "employeeUserId";	
		public static final String SUBMITTER_USER_ID = "submitterUserId";
		public static final String SUPERVISOR_USER_ID = "supervisorUserId";		
		public static final String OT_STATUS_CODE = "otStatusCode";
	}	
	
	@Id
	@SequenceGenerator(name="OT_DETAIL_ID_GENERATOR", sequenceName="ALOHA.SEQ_OT_DETAIL", allocationSize=1)
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="OT_DETAIL_ID_GENERATOR")
	@Column(name="DETAIL_ID", unique=true)
	private long id;

    @ManyToOne
	@JoinColumn(name="HEADER_ID", referencedColumnName="HEADER_ID", nullable=false)
	private OTHeader header;
    
    @ManyToOne
	@JoinColumn(name="STATUS_ID",  referencedColumnName="STATUS_ID", nullable=false)
	private OTIndivStatus status;
        
    @ManyToOne
	@JoinColumn(name="SUBM_USER_ID",  referencedColumnName="USER_ID", nullable=false)	
	private AlohaUser submitter;
    
    @ManyToOne
	@JoinColumn(name="SUPV_USER_ID",  referencedColumnName="USER_ID", nullable=false)	
	private AlohaUser supervisor;
    
    @Column(name="DETAIL_SEQ", nullable=false, precision=5) 
    private int sequence;
    
	@Version
	@Column(name="OPT_LOCK_NBR")
	private long version;

	@Column(name="USER_CREATED", nullable=true)	// needs to be nullable to accomodate rows that
												// were inserted prior to the addition of this column 
	private Long userCreated;

	@Column(name="USER_LAST_UPDATED", nullable=true)	// needs to be nullable to accomodate rows that
														// were inserted prior to the addition of this column
	private Long userLastUpdated;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="DATE_CREATED", nullable=true)		// needs to be nullable to accomodate rows that
													// were inserted prior to the addition of this column
	private Date dateCreated;
	
	@Transient
	private List<AlohaUser> authorizedIndividuals = null;
	
	
	/*
	 * DATE_LAST_UPDATED IS NOT NULLABLE AND USED SYSDATE BY DEFAULT
	 */

	@OneToMany(mappedBy="detail", fetch=FetchType.EAGER, cascade=CascadeType.ALL)
	@OrderBy("id")
	private List<OTItem> items = new ArrayList<OTItem>();

	@OneToMany(mappedBy="detail", fetch=FetchType.EAGER, cascade=CascadeType.ALL)
	@OrderBy("id")
	private List<OTDetailSubmitterRemark> submitterRemarks = new ArrayList<OTDetailSubmitterRemark>();
	
	@OneToMany(mappedBy="detail", fetch=FetchType.EAGER, cascade=CascadeType.ALL)
	@OrderBy("id")
	private List<OTDetailSupervisorRemark> supervisorRemarks = new ArrayList<OTDetailSupervisorRemark>();	
	
	@OneToMany(mappedBy="detail", fetch=FetchType.LAZY, cascade=CascadeType.ALL)
	@OrderBy("id")
	private List<OTDetailHistory> historicalEntries  = new ArrayList<OTDetailHistory>();
	
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public OTHeader getHeader() {
		return header;
	}

	public void setHeader(OTHeader header) {
		this.header = header;
	}
	
	public OTIndivStatus getStatus() {
		return status;
	}

	public void setStatus(OTIndivStatus status) {
		this.status = status;
	}

	public AlohaUser getSubmitter() {
		return submitter;
	}

	public void setSubmitter(AlohaUser submitter) {
		this.submitter = submitter;
	}

	public AlohaUser getSupervisor() {
		return supervisor;
	}

	public void setSupervisor(AlohaUser supervisor) {
		this.supervisor = supervisor;
	}

	public int getSequence() {
		return sequence;
	}

	public void setSequence(int sequence) {
		this.sequence = sequence;
	}

	public OTGroup getGroup() {
		
		if ( this.header != null) {
			return this.header.getGroup();
		} else {
			return null;	
		}
		
	}
	public long getVersion() {
		return version;
	}

	public void setVersion(long version) {
		this.version = version;
	}

	public Long getUserCreated() {
		return userCreated;
	}

	public void setUserCreated(Long userCreated) {
		this.userCreated = userCreated;
	}

	public Long getUserLastUpdated() {
		return userLastUpdated;
	}

	public void setUserLastUpdated(Long userLastUpdated) {
		this.userLastUpdated = userLastUpdated;
	}

	public Date getDateCreated() {
		return dateCreated;
	}

	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}

	public List<OTItem> getItems() {
		return items;
	}

	public void setItems(List<OTItem> items) {
		this.items = items;
	}

	public boolean addItem(OTItem item) {
		return this.items.add(item);
	}	

	public void setSubmitterRemarks(List<OTDetailSubmitterRemark> submitterRemarks) {
		this.submitterRemarks = submitterRemarks;
	}
	
	public boolean addSubmitterRemark(OTDetailSubmitterRemark submitterRemark) {
		return this.submitterRemarks.add(submitterRemark);
	}	
	public List<OTDetailSubmitterRemark> getSubmitterRemarksForThisDetailOnly() {
			return submitterRemarks;			
	}
	public List<OTDetailSupervisorRemark> getSupervisorRemarksForThisDetailOnly() {
			return this.supervisorRemarks;			
	}	

	public List<OTDetailSubmitterRemark> getSubmitterRemarks() {
		if (this.header != null) {
			return this.header.getAllSubmitterRemarks();
		} else {
			return submitterRemarks;			
		}		
	}
	
	public List<OTDetailSupervisorRemark> getSupervisorRemarks() {
		if (this.header != null) {
			return this.header.getAllSupervisorRemarks();
		} else {
			return this.supervisorRemarks;			
		}
	}

	public void setSupervisorRemarks(
			List<OTDetailSupervisorRemark> supervisorRemarks) {
		this.supervisorRemarks = supervisorRemarks;
	}

	public boolean addSupervisorRemark(OTDetailSupervisorRemark supervisorRemark) {
		return this.supervisorRemarks.add(supervisorRemark);
	}	
	
	public List<OTDetailHistory> getHistoricalEntries() {
		return historicalEntries;
	}

	public void setHistoricalEntries(List<OTDetailHistory> historicalEntries) {
		this.historicalEntries = historicalEntries;
	}
	public boolean addDetailHistory(OTDetailHistory historicalEntry) {
		return this.historicalEntries.add(historicalEntry);
	}		
	
	public BigDecimal getEstimatedTotalHours() {
		BigDecimal total = BigDecimal.ZERO;
		for ( OTItem item: this.items) {
			total = total.add(item.getEstimatedHours());
		}
		total.setScale(1, RoundingMode.HALF_UP);
		return total;
	}
	
	public OTPayPeriod getPayPeriod() {
		return this.header.getPayPeriod();
	}
	
	public AlohaUser getEmployee() {
		return this.header.getEmployee();
	}
	public OTType getType() {
		if (this.header != null) {
			return this.header.getType();			
		} else {
			return null;			
		}
	}
	public boolean isSubmitted() {
		return ( (this.status != null) 
					&& (this.status.getCode().equals(OTIndivStatus.CodeValues.SUBMITTED))
				);
	}
	public boolean isResubmitted() {
		return ( (this.status != null) 
					&& (this.status.getCode().equals(OTIndivStatus.CodeValues.RESUBMITTED))
				);
	}	
	public boolean isReceived() {
		return ( (this.status != null) 
					&& (this.status.getCode().equals(OTIndivStatus.CodeValues.RECEIVED))
				);
	}
	public boolean isReceivedWithMods() {
		return ( (this.status != null) 
					&& (this.status.getCode().equals(OTIndivStatus.CodeValues.RECEIVED_WITH_MODS))
				);
	}	
	public boolean isReceivedWithModifications() {
		return ( this.isReceivedWithMods() );
	}
	
	public boolean isModified() {
		return ( (this.status != null) 
				&& (this.status.getCode().equals(OTIndivStatus.CodeValues.MODIFIED))
			);
	}
	
	public boolean isApproved() {
		return ( (this.status != null) 
					&& (this.status.getCode().equals(OTIndivStatus.CodeValues.APPROVED))
				);
	}
	public boolean isDenied() {
		return ( (this.status != null) 
					&& (this.status.getCode().equals(OTIndivStatus.CodeValues.DENIED))
				);
	}
	public boolean isCancelled() {
		return ( (this.status != null) 
					&& (this.status.getCode().equals(OTIndivStatus.CodeValues.CANCELLED))
				);
	}

	public boolean isCancellableBySubmitOwn() {
		String statusCode = this.status.getCode().trim();
		return ( ( this.isSubmittedByEmployee() )
					&&	( OTIndivStatusUtil.isCancellableBySubmitOwn(statusCode))	
				);
	}	
	public boolean isSubmittedByEmployee() {
		return ( this.submitter != null 
					&& this.getEmployee() != null 
					&& this.submitter.equals(this.getEmployee()) );
	}

	public boolean isCancellableByOnBehalfOf()  {
		String statusCode = this.status.getCode().trim();
		return ( OTIndivStatusUtil.isCancellableByOnBehalfOf(statusCode));
	}

	public boolean isReviewableBySupervisor() {
		String statusCode = this.status.getCode().trim();
		return ( ( this.isInGroup() == false)
					&& (OTIndivStatusUtil.isReviewableBySupervisor(statusCode) ) );
	}
	public boolean isApprovableBySupervisor() {
		String statusCode = this.status.getCode().trim();
		return ( ( this.isInGroup() == false)
				&& (OTIndivStatusUtil.isApprovableBySupervisor(statusCode) ) );
		
	}		
	public boolean isReceivableBySupervisor() {
		String statusCode = this.status.getCode().trim();
		return ( OTIndivStatusUtil.isReceivableBySupervisor(statusCode));
	}
	public boolean isDeniableBySupervisor() {
		String statusCode = this.status.getCode().trim();
		return ( OTIndivStatusUtil.isDeniableBySupervisor(statusCode));		
	}	
//	public boolean isModifiableBySupervisor() {
//		String statusCode = this.status.getCode().trim();
//		return ( OTIndivStatusUtil.isModifiableBySupervisor(statusCode));		
//	}	
//	public boolean isModifiableBySupervisor() {
//		return 	( ( this.isInHeader() )
//					&& ( this.header.isModifiableBySupervisor() )
//				);		
//	}	
	public boolean isModifiableBySupervisor() {
		if ( this.isInGroup() ) {
			return ( this.header.isModifiableBySupervisor() );
		} else {
			String statusCode = this.status.getCode().trim();
			return ( OTIndivStatusUtil.isModifiableBySupervisor(statusCode) );	
		}
	}	
//	public boolean isCancellableBySupervisor() {
//		String statusCode = this.status.getCode().trim();
//		return ( OTIndivStatusUtil.isCancellableBySupervisor(statusCode));
//	}	
//	public boolean isCancellableBySupervisor() {
//		return 	( ( this.isInHeader() )
//					&& ( this.header.isCancellableBySupervisor() )
//				);		
//	}		
	public boolean isCancellableBySupervisor() {
		if ( this.isInGroup() ) {
			return ( this.header.isCancellableBySupervisor() );
		} else {
			String statusCode = this.status.getCode().trim();
			return ( OTIndivStatusUtil.isCancellableBySupervisor(statusCode) );	
		}
	}		
	public boolean isModifiableBySubmitOwn() {
		String statusCode = this.status.getCode().trim();
		return ( OTIndivStatusUtil.isModifiableBySubmitOwn(statusCode));		
	}	
	public boolean isModifiableByOnBehalfOf() {
		String statusCode = this.status.getCode().trim();
		return ( OTIndivStatusUtil.isModifiableByOnBehalfOf(statusCode));		
	}		
	public boolean isSubmitterUserId(long userId) {
		return 	( (this.submitter != null) 
					&& (this.submitter.getUserId() == userId)
				);
	}
	public boolean isSupervisorUserId(long userId) {
		return 	( (this.supervisor != null) 
					&& (this.supervisor.getUserId() == userId)
				);
	}
	public boolean isEmployeeUserId(long userId) {
		return	( (this.header != null)
					&& (this.header.isEmployeeUserId(userId))
				);
	}
	public String getRequestId() {
		return ( (this.header == null) ? String.valueOf(0) : this.header.getRequestId() );
	}
	public OTDetailHistory getLatestHistoricalEntry() {
		if ( !this.historicalEntries.isEmpty()) {
			return ( this.historicalEntries.get(this.historicalEntries.size() -1));
		}
		return null;
	}
	public String toEventLog() {
		StringBuilder sb = new StringBuilder();
		OTDetailHistory history = this.getLatestHistoricalEntry();
		if ( (this.header != null) && (history != null) && (history.getStatusTransition() != null)) {
			sb.append( history.getStatusTransition().getActionLabel());			
			sb.append(" [");
			sb.append("requestId=");
			sb.append(this.getRequestId());
			sb.append(";statusCodeChange=");
			sb.append(history.getStatusTransition().getActionCode());
			sb.append(";peformedBy=");
			sb.append(this.getLatestHistoricalEntry().getActor().getFullName());
			sb.append(";employee=");
			sb.append(this.getEmployee().getFullName());
			sb.append(";submitter=");
			sb.append(this.submitter.getFullName());
			sb.append(";supervisor=");
			sb.append(this.supervisor.getFullName());
			sb.append(";payPeriod=");
			sb.append(this.getPayPeriod().getShortLabel());
			sb.append("]");
		}
		return sb.toString();
	}	
	public boolean isFundingRequired() {
		return 	( (this.header != null)
					&& (this.header.isFundingRequired())
				);
	}

	public List<AlohaUser> getAuthorizedIndividuals() {
		if ( this.authorizedIndividuals == null) {
			this.authorizedIndividuals = new ArrayList<AlohaUser>();
			authorizedIndividuals.add(this.supervisor);
			if ( this.header != null && this.header.isInGroup()) {
				List<AlohaUser> authorizedIndividualsInGroup = this.header.getAuthorizedIndividualsInGroup();
				if (authorizedIndividualsInGroup != null) {
					this.authorizedIndividuals.addAll(authorizedIndividualsInGroup);
				}
			}
		}
		return this.authorizedIndividuals;
	}
	public boolean isAuthorized(AlohaUser alohaUser) {
		for ( AlohaUser authorizedUser : this.getAuthorizedIndividuals() ) {
			if ( authorizedUser.getUserId() == alohaUser.getUserId() ) {
				return true;
			}
		}
		return false;
	}	
	public boolean isInHeader() {
		return 	( this.header != null); 
	}	
	public boolean isInGroup() {
		return 	( (this.header != null) 
					&& (this.header.isInGroup())
				);
	}
}