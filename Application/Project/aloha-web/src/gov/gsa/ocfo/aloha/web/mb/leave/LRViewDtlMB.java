package gov.gsa.ocfo.aloha.web.mb.leave;

import gov.gsa.ocfo.aloha.ejb.leave.LeaveRequestEJB;
import gov.gsa.ocfo.aloha.exception.AlohaServerException;
import gov.gsa.ocfo.aloha.model.entity.leave.LeaveDetail;
import gov.gsa.ocfo.aloha.model.entity.leave.LeaveHeader;
import gov.gsa.ocfo.aloha.model.entity.leave.LeaveItem;
import gov.gsa.ocfo.aloha.model.leave.LeaveRemark;
import gov.gsa.ocfo.aloha.web.util.AlohaURIs;
import gov.gsa.ocfo.aloha.web.util.StringUtil;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.List;

import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;

//SAK 2012-08-06 Displays Leave Details.
//Can be called from anywhere
//Used in reportLR.xhtml
//usage: <ui:include src="lrViewById.xhtml"></ui:include>
//input: request parameter strlrdid
//
@ManagedBean
public class LRViewDtlMB implements Serializable {

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
	
	-- Lee Trent (09/24/2013)
    ********************************************************************************************/			
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -5037429095984517684L;
	private static final SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
	
	private long lrdId;
	private String strlrdId;

	private LeaveDetail lrd;
	
	@EJB private LeaveRequestEJB lrEJB;
	
	private String lrStatus;
	private String lrSubmitter;
	private String lrApprover;
	
	//private String lrSubmitterComments;
	//private String lrApproverComments;
	
	private String lrAllComments;
	
	private List<LeaveItem> lriList;
	
	
	public String getLeaveRecord() throws Exception {
		try {
			//UICommand uic = (UICommand)UIComponent.getCurrentComponent(FacesContext.getCurrentInstance());
			//lrdId = (Long) uic.getValue();
			
			//prefer lr_detail_id or alternatively lr_header_id and lr_detail_seq
			strlrdId = "";
			if (FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("strlrdid") != null )
				strlrdId = 
                FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("strlrdid");
			if (strlrdId.isEmpty()) {
				String strLRHId = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("strlrhid");
				String strLRDSeq = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("strlrdseq");
				if (strLRHId.isEmpty() || strLRDSeq.isEmpty()) {
					//abandon
				} else {
					LeaveHeader lh = lrEJB.findById(Long.parseLong(strLRHId));
					List<LeaveDetail> ldlist = lh.getLeaveDetails();
					for (LeaveDetail ld : ldlist) {
						if (ld.getSequence() == Integer.parseInt(strLRDSeq) ) {
							strlrdId = Long.toString(ld.getId());
							break;
						}
					}
				}
			}
			if (!strlrdId.isEmpty()) {
				setLrd(lrEJB.getLeaveDetail(Long.parseLong(strlrdId)));
				lrStatus = lrd.getLeaveStatus().getCode();
				lrSubmitter = lrd.getSubmitterName();
				lrApprover = lrd.getApprover().getFullName();

				/**********************************************************************************
				lrSubmitterComments = "";
				for (LeaveSubmitterComment lsc : lrd.getSubmitterComments()) {
					if (lsc.getComment() != null) {
						lrSubmitterComments = lrSubmitterComments + 
						DateUtil.formatDate(lsc.getCommentDate()) + ": " + lsc.getComment() + "\n";		
					}
				}
				lrApproverComments = "";
				for (LeaveApproverComment lac : lrd.getApproverComments()) {
					if (lac.getComment() != null)  {
						lrApproverComments = lrApproverComments + 
						DateUtil.formatDate(lac.getCommentDate()) + ": " + lac.getComment() + "\n";
					}
				}
				***********************************************************************************/
				
				this.lrAllComments = "";
				StringBuilder sb = new StringBuilder();
				for ( LeaveRemark leaveRemark : lrd.getLeaveHeader().getAllRemarks()) {
					if ( ! StringUtil.isNullOrEmpty(leaveRemark.getRemark()) )  {
						sb.append(leaveRemark.getActor().getLastName());	
						sb.append(" (");	
						sb.append(dateFormat.format(leaveRemark.getRemarkDate()));
						sb.append("): ");	
						sb.append(leaveRemark.getRemark());
						sb.append("\n");
					}					
				}
				this.lrAllComments = sb.toString();

				lriList = lrd.getLeaveItems();
				//System.out.println("LRI: " + lriList.get(0).getLeaveDate());
				return null;
			}

		} catch (AlohaServerException e) {
			e.printStackTrace();
			FacesContext.getCurrentInstance().getExternalContext().redirect(AlohaURIs.SERVER_ERROR);
		}
		return null;
	}



	public void setLrd(LeaveDetail lrd) {
		this.lrd = lrd;
	}

	public LeaveDetail getLrd() {
		return lrd;
	}



	public void setLrStatus(String lrStatus) {
		this.lrStatus = lrStatus;
	}



	public String getLrStatus() {
		return lrStatus;
	}



	public void setLrSubmitter(String lrSubmitter) {
		this.lrSubmitter = lrSubmitter;
	}



	public String getLrSubmitter() {
		return lrSubmitter;
	}



	public void setLrApprover(String lrApprover) {
		this.lrApprover = lrApprover;
	}



	public String getLrApprover() {
		return lrApprover;
	}



	/*********************************************************************
	public void setLrSubmitterComments(String lrSubmitterComments) {
		this.lrSubmitterComments = lrSubmitterComments;
	}



	public String getLrSubmitterComments() {
		return lrSubmitterComments;
	}



	public void setLrApproverComments(String lrApproverComments) {
		this.lrApproverComments = lrApproverComments;
	}



	public String getLrApproverComments() {
		return lrApproverComments;
	}
	*********************************************************************/


	public void setLriList(List<LeaveItem> lriList) {
		this.lriList = lriList;
	}



	public List<LeaveItem> getLriList() {
		return lriList;
	}



	public long getLrdId() {
		return lrdId;
	}



	public void setLrdId(long lrdId) {
		this.lrdId = lrdId;
	}



	public void setStrlrdId(String strlrdId) {
		this.strlrdId = strlrdId;
	}



	public String getStrlrdId() {
		return strlrdId;
	}



	public String getLrAllComments() {
		return lrAllComments;
	}
}

