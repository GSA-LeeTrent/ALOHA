package gov.gsa.ocfo.aloha.model.entity.leave;

import gov.gsa.ocfo.aloha.comparator.LeaveDetailComparator;
import gov.gsa.ocfo.aloha.comparator.LeaveHistoryComparator;
import gov.gsa.ocfo.aloha.model.entity.AlohaUser;
import gov.gsa.ocfo.aloha.model.entity.AuditTrail;
import gov.gsa.ocfo.aloha.model.leave.LeaveRemark;
import gov.gsa.ocfo.aloha.model.leave.LeaveRemarkComparator;
import gov.gsa.ocfo.aloha.util.StringUtil;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
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
import javax.persistence.Version;


/**
 * The persistent class for the LR_HEADER database table.
 * 
 */
@Entity
@Table(name="LR_HEADER", schema="ALOHA")
@NamedQueries({
	@NamedQuery(name="findById",
	        	query="SELECT row FROM LeaveHeader row WHERE row.id = :lrHeaderId")  	
})
public class LeaveHeader implements Serializable {
	private static final long serialVersionUID = -4762985625568444003L;

	public interface QueryNames{
		public static final String FIND_BY_ID = "findById";
	}
	public interface QueryParamNames {
		public static final String FIND_BY_ID = "lrHeaderId";	
	}

	@Id
	@SequenceGenerator(name="LR_HEADER_ID_GENERATOR", sequenceName="ALOHA.SEQ_LR_HEADER", allocationSize=1)
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="LR_HEADER_ID_GENERATOR")
	@Column(name="LR_HEADER_ID", unique=true)
	private long id;	

    @ManyToOne
	@JoinColumn(name="EMPLOYEE_USER_ID",  referencedColumnName="USER_ID", nullable=false)	
	private AlohaUser employee;
	
	//bi-directional many-to-one association to LeaveDetail
	@OneToMany(mappedBy="leaveHeader", fetch=FetchType.EAGER, cascade=CascadeType.ALL)
	@OrderBy("id")
	private List<LeaveDetail> leaveDetails = new ArrayList<LeaveDetail>();

	@Embedded
	private AuditTrail auditTrail;
	
	@Version
	@Column(name="OPT_LOCK_NBR")
	private long version;
	
    public LeaveHeader() {
    }

	public long getId() {
		return this.id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public List<LeaveDetail> getLeaveDetails() {
		return this.leaveDetails;
	}

	public void setLeaveDetails(List<LeaveDetail> leaveDetails) {
		this.leaveDetails = leaveDetails;
	}
	public boolean addLeaveDetail(LeaveDetail leaveDetail) {
		return this.leaveDetails.add(leaveDetail);
	}

	public boolean removeLeaveDetail(LeaveDetail leaveDetail) {
		return this.leaveDetails.remove(leaveDetail);
	}

	public AuditTrail getAuditTrail() {
		return auditTrail;
	}

	public void setAuditTrail(AuditTrail auditTrail) {
		this.auditTrail = auditTrail;
	}

	public AlohaUser getEmployee() {
		return employee;
	}

	public void setEmployee(AlohaUser employee) {
		this.employee = employee;
	}
	public LeaveDetail getLatestDetail() {
		return ( (this.leaveDetails.size() > 0 ) ? this.leaveDetails.get(this.leaveDetails.size() - 1): null);
	}
	public long getVersion() {
		return version;
	}
	public void setVersion(long version) {
		this.version = version;
	}
	public LeaveDetail getLastestApprovedDetail() {
		if ( !this.leaveDetails.isEmpty() ) {
			for ( int ii = this.leaveDetails.size() -1; ii >= 0; ii--) {
				LeaveDetail leaveDetail = this.leaveDetails.get(ii);
				if (LeaveStatus.CodeValues.APPROVED.equals(leaveDetail.getLeaveStatus().getCode())) {
					return leaveDetail;
				}
			}
		}
		return null;
	}
	
//	public List<LeaveHistory> getLeaveHistory() {
//		List<LeaveHistory> historyList = new ArrayList<LeaveHistory>();
//		for ( LeaveDetail detail : this.leaveDetails) {
//			for ( LeaveHistory history: detail.getLeaveHistories()) {
//				if ( ! LeaveStatusTransition.ActionCodeValues.PEND_ANEND_TO_AMENDED.equals(history.getLeaveStatusTransition().getActionCode().trim()) ) {
//					historyList.add(history);
//				}
//			}
//		}
//		return historyList;
//	}

//	public List<LeaveHistory> getLeaveHistory() {
//		List<LeaveHistory> historyList = new ArrayList<LeaveHistory>();
//
//		Collections.sort(this.leaveDetails, new LeaveDetailComparator());
//		this.debug();
//		for ( LeaveDetail detail : this.leaveDetails) {
//			Collections.sort(detail.getLeaveHistories(), new LeaveHistoryComparator());
//			this.debugLeaveHistory();
//			for ( LeaveHistory history: detail.getLeaveHistories()) {
//				if ( ! LeaveStatusTransition.ActionCodeValues.PEND_ANEND_TO_AMENDED.equals(history.getLeaveStatusTransition().getActionCode().trim()) ) {
//					historyList.add(history);
//				}
//			}
//		}
//		return historyList;
//	}
//	private void debugLeaveHistory() {
//		for ( int ii = this.leaveDetails.size() -1; ii >= 0; ii--) {
//			System.out.println(this.leaveDetails.get(ii).getLeaveHeaderId() + " / " + this.leaveDetails.get(ii).getId());
//			for ( LeaveHistory history: this.leaveDetails.get(ii).getLeaveHistories()) {
//				System.out.println(history.getLeaveDetail().getId() + " / " + history.getId() 
//						+ " / " + history.getLeaveStatusTransition().getActionLabel());
//			}
//		}
//	}

	public List<LeaveHistory> getLeaveHistory() {
		List<LeaveHistory> historyList = new ArrayList<LeaveHistory>();
		for ( LeaveDetail detail : this.leaveDetails) {
			for ( LeaveHistory history: detail.getLeaveHistories()) {
				if ( ! LeaveStatusTransition.ActionCodeValues.PEND_ANEND_TO_AMENDED.equals(history.getLeaveStatusTransition().getActionCode().trim()) ) {
					historyList.add(history);
				}
			}
		}
		Collections.sort(historyList, new LeaveHistoryComparator());
		return historyList;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (id ^ (id >>> 32));
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
		LeaveHeader other = (LeaveHeader) obj;
		if (id != other.id)
			return false;
		return true;
	}

	/***********************************************************************
	 'VIEWABLE' INCLUDES LEAVE REQUESTS THAT ARE IN A FINAL STATE
	 SUCH AS 'CANCELED' AND 'WITHDRAWN'. FOR THESE LEAVE REQUESTS THE 
	 'AVAILABLE ACTION' IS TO VIEW IT ('VIEWABLE'). 
	 'DENIED' CAN BE A FINAL STATE BUT ONLY WHEN THE 'DENY ACTION'
	 DOES NOT APPLY TO A PENDING AMENDMENT.
	 **********************************************************************/
	public LeaveDetail getViewableLeaveDetail() {
		if ( (this.leaveDetails == null) || (this.leaveDetails.isEmpty()) ) {
			return null;
		}
		
		//this.debug();
		Collections.sort(this.leaveDetails, new LeaveDetailComparator());
		//this.debug();
		// IF THE LAST ACTION TAKEN ON A LEAVE REQUEST WAS TO DENY A PENDING AMENDMENT
		// OR PENDING WITHDRAWAL, THEN THE DENIED LEAVE REQUEST IS NOT IN A FINAL STATE. 
		// IN CASES SUCH AS THESE, WE HAVE TO GO BACK AND FIND THE LEAVE REQUEST THAT 
		// PRECEDED THE DENIED LEAVE REQUEST AND RETURN THIS RECORD AS AN 'VIEWABLE/ACTIONABLE' ITEM.
		if	( (this.leaveDetails.size() > 1)
				&& (this.leaveDetails.get(this.leaveDetails.size() - 1).getLeaveStatus().getCode().equals(LeaveStatus.CodeValues.DENIED)) 
		) {
				return this.leaveDetails.get(this.leaveDetails.size() - 2); // RETURN THE LEAVE REQUEST RECORD PRIOR TO THE 'DENIED' ONE
		} else {
			return (this.leaveDetails.get(this.leaveDetails.size() - 1)); // OTHERWISE, THE LATEST DETAIL RECORD IS 'ACTIONABLE/VIEWABLE'			
		}
	}
	/*
	private void debug() {
		System.out.println("-----------------------------------------------------------");
		System.out.println("Leave Detail IDs for Leave Request #" + this.id);
		System.out.println("-----------------------------------------------------------");
		for ( LeaveDetail ld : this.leaveDetails) {
			System.out.println(ld.getId() + " / " + ld.getLeaveStatus().getCode());
		}
		System.out.println("-----------------------------------------------------------");
	}
	*/
	
	// CANCELLABLE0
	public boolean isCancelable() {
		return	( this.getViewableLeaveDetail().isSubmitted() );
	}
	public boolean isCancelableByThisUser(AlohaUser user) {
		LeaveDetail  viewable = this.getViewableLeaveDetail();
		return	( viewable.isSubmitted() 
					&& viewable.getSubmitter().equals(user));
	}
	
	// AMENDABLE
	public boolean isAmendable() {
		return	( this.getViewableLeaveDetail().isApproved() );
	}
	public boolean isAmendableByThisUser(AlohaUser user) {
		LeaveDetail  viewable = this.getViewableLeaveDetail();
		return	( viewable.isApproved() 
					&& viewable.getSubmitter().equals(user));		
	}
	
	// WITHDRAWABLE
	public boolean isWithdrawable() {
		return	( this.getViewableLeaveDetail().isApproved() );
	}	
	public boolean isWithdrawableByThisUser(AlohaUser user) {
		LeaveDetail  viewable = this.getViewableLeaveDetail();
		return	( viewable.isApproved() 
					&& viewable.getSubmitter().equals(user));
	}		
	
	// APPROVABLE
	public boolean isApprovable() {
		LeaveDetail  viewable = this.getViewableLeaveDetail();
		return	( (viewable.isSubmitted())
					|| (viewable.isPendingAmendment())
					|| (viewable.isPendingWithdrawal())
			);		
	}
	public boolean isApprovableByThisUser(AlohaUser user) {
		LeaveDetail  viewable = this.getViewableLeaveDetail();
		return	( this.isApprovable()
					&& viewable.getApprover().equals(user)
				);		
	}	
	
	// CHANGE OF SUPERVISOR
	public boolean isChangeOfSupervisorAllowed() {
		LeaveDetail  viewable = this.getViewableLeaveDetail();
		return ( (viewable.isSubmitted()) 
					|| (viewable.isPendingAmendment())
					|| (viewable.isPendingWithdrawal())
				);
	}	
	public boolean isChangeOfSupervisorAllowedByThisUser(AlohaUser user) {
		LeaveDetail  viewable = this.getViewableLeaveDetail();
		return ( this.isChangeOfSupervisorAllowed() 
					&& ( viewable.getSubmitter().equals(user) 
							|| viewable.getEmployee().equals(user) )	
				);
	}	
	
	public LeaveDetail getNextToLastDetail() {
		return ( (this.leaveDetails.size() > 1 ) ? this.leaveDetails.get(this.leaveDetails.size() - 2): null);
	}	
	public String getRequestId() {
		return String.valueOf(this.id);
	}
	public List<LeaveSubmitterComment> getAllSubmitterRemarks() {
		List<LeaveSubmitterComment> submRemarksList = new ArrayList<LeaveSubmitterComment>();
		for ( LeaveDetail lrDetail : this.leaveDetails) {
			for ( LeaveSubmitterComment submRemark : lrDetail.getSubmitterCommentsForThisDetailOnly()) {
				submRemarksList.add(submRemark);
			}
		}
		return submRemarksList;
	}
	
	public List<LeaveApproverComment> getAllSupervisorRemarks() {
		List<LeaveApproverComment> submRemarksList = new ArrayList<LeaveApproverComment>();
		for ( LeaveDetail lrDetail : this.leaveDetails) {
			for ( LeaveApproverComment supvRemark : lrDetail.getApproverCommentsForThisDetailOnly()) {
				submRemarksList.add(supvRemark);
			}
		}
		return submRemarksList;
	}	
	public List<LeaveRemark> getAllRemarks() {
		List<LeaveRemark> allRemarksList = new ArrayList<LeaveRemark>();
		
		// EXTRACT REMARKS FROM LR_HISTORY
		for ( LeaveDetail detail : this.leaveDetails) {
			for ( LeaveHistory history: detail.getLeaveHistories()) {
				if ( ! StringUtil.isNullOrEmpty(history.getRemarks()) ) {
					Date remarkDate = history.getAuditTrail().getDateLastUpdated();
					AlohaUser actor = history.getActor();
					String eventDesc = history.getLeaveStatusTransition().getActionDescription();
					String remark = history.getRemarks();
					allRemarksList.add(new LeaveRemark(remarkDate, actor, eventDesc, remark));
				}
			}
		}
		
		// EXTRACT REMARKS FROM LR_SUBMITTER_COMMENT
		for ( LeaveDetail detail : this.leaveDetails) {
			AlohaUser actor = detail.getSubmitter();
			for ( LeaveSubmitterComment submitterRemark : detail.getSubmitterCommentsForThisDetailOnly()) {
				if ( ! StringUtil.isNullOrEmpty(submitterRemark.getComment()) ) {
					Date remarkDate = submitterRemark.getAuditTrail().getDateLastUpdated();
					String remark = submitterRemark.getComment();
					allRemarksList.add(new LeaveRemark(remarkDate, actor, remark));
				}
			}
		}
		
		// EXTRACT REMARKS FROM LR_APPROVER_COMMENT
		for ( LeaveDetail detail : this.leaveDetails) {
			AlohaUser actor = detail.getApprover();
			for ( LeaveApproverComment approverRemark : detail.getApproverCommentsForThisDetailOnly()) {
				if ( ! StringUtil.isNullOrEmpty(approverRemark.getComment()) ) {
					Date remarkDate = approverRemark.getAuditTrail().getDateLastUpdated();
					String remark = approverRemark.getComment();
					allRemarksList.add(new LeaveRemark(remarkDate, actor, remark));
				}
			}
		}		
		
		Collections.sort(allRemarksList, new LeaveRemarkComparator());
		return allRemarksList;
	}
	
	public String getPayPeriodDateRange() {
		return ( this.getLatestDetail() == null ) ? null : (this.getLatestDetail().getPayPeriodDateRange());
	}
	public String getLeaveHeaderIdAsString() {
		return Long.toString(this.getId());
	}	
	public LeaveStatus getLeaveStatus() {
		return this.getViewableLeaveDetail().getLeaveStatus();
	}	
}