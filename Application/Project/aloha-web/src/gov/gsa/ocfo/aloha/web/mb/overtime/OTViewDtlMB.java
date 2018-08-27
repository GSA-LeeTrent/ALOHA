package gov.gsa.ocfo.aloha.web.mb.overtime;

import gov.gsa.ocfo.aloha.ejb.overtime.OvertimeEJB;
import gov.gsa.ocfo.aloha.exception.AlohaServerException;
import gov.gsa.ocfo.aloha.model.entity.overtime.OTDetailRemark;
import gov.gsa.ocfo.aloha.model.entity.overtime.OTDetail;
import gov.gsa.ocfo.aloha.model.entity.overtime.OTHeader;
import gov.gsa.ocfo.aloha.model.entity.overtime.OTItem;
import gov.gsa.ocfo.aloha.web.util.AlohaURIs;
import gov.gsa.ocfo.aloha.web.util.StringUtil;

import java.io.Serializable;
import java.util.List;

import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;

//VEC 2013-09-24 Displays Overtime Details.
//Can be called from anywhere
//Used in reportOT.xhtml
//usage: <ui:include src="otViewById.xhtml"></ui:include>
//input: request parameter strotdid
//
@ManagedBean
public class OTViewDtlMB implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5037429095984517684L;
	
	private long otdId;
	private String strotdId;

	private OTDetail otd;
	
	@EJB private OvertimeEJB otEJB;
	
	private String otStatus;
	private String otSubmitter;
	private String otApprover;
	private String otSubmitterComments;
	private String otSupervisorComments;
	private List<OTItem> otiList;
	
	
	public String getOvertimeRecord() throws Exception {
		try {
			//UICommand uic = (UICommand)UIComponent.getCurrentComponent(FacesContext.getCurrentInstance());
			//lrdId = (Long) uic.getValue();
			
			//prefer ot_detail_id or alternatively ot_header_id and ot_detail_seq
			strotdId = "";
			if (FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("strotdid") != null )
				strotdId = 
                FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("strotdid");
			
			if (strotdId.isEmpty()) {
				String strOTHId = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("strothid");
				String strOTDSeq = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("strotdseq");
				if ( StringUtil.isNullOrEmpty(strOTHId) || StringUtil.isNullOrEmpty(strOTDSeq) ) {
					//abandon
				} else {
					OTHeader oh = otEJB.findById(Long.parseLong(strOTHId));
					List<OTDetail> otlist = oh.getDetails();
					for (OTDetail otd : otlist) {
						if (otd.getSequence() == Integer.parseInt(strOTDSeq) ) {
							strotdId = Long.toString(otd.getId());
							break;
						}
					}
				}
			}
			if (!strotdId.isEmpty()) {
				setOtd(otEJB.retrieveDetailByID(Long.parseLong(strotdId)));
				otStatus = otd.getStatus().getCode();
				otSubmitter = otd.getSubmitter().getFullName();
				otApprover = otd.getSupervisor().getFullName();
				otSubmitterComments = "";
				for (OTDetailRemark otsc : otd.getSubmitterRemarks()) {
					if (otsc.getText() != null) {
						otSubmitterComments = otSubmitterComments + 
//						DateUtil.formatDate(otsc.getCommentDate()) + ": " + 
								otsc.getText() + "\n";		
					}
				}
				otSupervisorComments = "";
				for (OTDetailRemark otac : otd.getSupervisorRemarks()) {
					if (otac.getText() != null)  {
						otSupervisorComments = otSupervisorComments + 
//						DateUtil.formatDate(otac.getCommentDate()) + ": " + 
								otac.getText() + "\n";
					}
				}
				otiList = otd.getItems();
				//System.out.println("LRI: " + lriList.get(0).getLeaveDate());
				return null;
			}

		} catch (AlohaServerException e) {
			e.printStackTrace();
			FacesContext.getCurrentInstance().getExternalContext().redirect(AlohaURIs.SERVER_ERROR);
		}
		return null;
	}


	public void setOtrStatus(String otStatus) {
		this.otStatus = otStatus;
	}



	public String getOtStatus() {
		return otStatus;
	}



	public void setOtSubmitter(String otSubmitter) {
		this.otSubmitter = otSubmitter;
	}



	public String getOtSubmitter() {
		return otSubmitter;
	}



	public void setOtApprover(String otApprover) {
		this.otApprover = otApprover;
	}



	public String getOtApprover() {
		return otApprover;
	}



	public void setOtSubmitterComments(String otSubmitterComments) {
		this.otSubmitterComments = otSubmitterComments;
	}



	public String getOtSubmitterComments() {
		return otSubmitterComments;
	}



	public void setOtSupervisorComments(String otSupervisorComments) {
		this.otSupervisorComments = otSupervisorComments;
	}



	public String getOtSupervisorComments() {
		return otSupervisorComments;
	}



	public void setOtiList(List<OTItem> otiList) {
		this.otiList = otiList;
	}



	public List<OTItem> getOtiList() {
		return otiList;
	}



	public long getOtdId() {
		return otdId;
	}



	public void setOtdId(long otdId) {
		this.otdId = otdId;
	}



	public void setStrotdId(String strotdId) {
		this.strotdId = strotdId;
	}



	public String getStrotdId() {
		return strotdId;
	}

    public void setOtd(OTDetail otd) {
		this.otd = otd;
	}

	public OTDetail getOtd() {
		return otd;
	}
	
}

