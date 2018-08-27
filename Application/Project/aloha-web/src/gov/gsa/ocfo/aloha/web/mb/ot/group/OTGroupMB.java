package gov.gsa.ocfo.aloha.web.mb.ot.group;

import gov.gsa.ocfo.aloha.ejb.overtime.GroupOvertimeEJB;
import gov.gsa.ocfo.aloha.ejb.overtime.OvertimeEJB;
import gov.gsa.ocfo.aloha.exception.AlohaServerException;
import gov.gsa.ocfo.aloha.model.entity.AlohaUser;
import gov.gsa.ocfo.aloha.model.entity.overtime.OTDetail;
import gov.gsa.ocfo.aloha.model.entity.overtime.OTGroup;
import gov.gsa.ocfo.aloha.model.entity.overtime.OTGroupHistory;
import gov.gsa.ocfo.aloha.model.entity.overtime.OTGroupStatus;
import gov.gsa.ocfo.aloha.model.entity.overtime.OTGroupStatusTrans;
import gov.gsa.ocfo.aloha.model.entity.overtime.OTHeader;
import gov.gsa.ocfo.aloha.model.entity.overtime.OTPayPeriod;
import gov.gsa.ocfo.aloha.web.mb.UserMB;
import gov.gsa.ocfo.aloha.web.mb.overtime.OTUtilMB;

import java.io.Serializable;

import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;

@ManagedBean(name=OTGroupMB.MANAGED_BEAN_NAME, eager=false)
@SessionScoped
public class OTGroupMB implements Serializable {
	private static final long serialVersionUID = -2800517097890669465L;

	public static final String MANAGED_BEAN_NAME = "otGroupMB";
	
	@EJB
	private GroupOvertimeEJB groupOvertimeEJB;
	
	@EJB
	private OvertimeEJB overtimeEJB;
	
	// JSF: MANAGED BEAN INJECTION
	@ManagedProperty(value="#{otUtilMB}")
	protected OTUtilMB otUtilMB;
	public void setOtUtilMB(OTUtilMB otUtilMB) {
		this.otUtilMB = otUtilMB;
	}
	// JSF: MANAGED BEAN INJECTION
	@ManagedProperty(value="#{userMB}")
	protected UserMB userMB;
	public void setUserMB(UserMB userMB) {
		this.userMB = userMB;
	}
	
	public OTGroup createOTGroupFromOTDetail(OTDetail otDetail, AlohaUser createdBy) {
		OTGroup otGroup = new OTGroup();
		
		otGroup.setPayPeriod(otDetail.getPayPeriod());
		otGroup.setStatus(this.otUtilMB.getOTGroupStatus(OTGroupStatus.CodeValues.PENDING));
		
		return otGroup;
	}
	public OTGroup createOTGroup(OTPayPeriod otPayPeriod, AlohaUser otGroupSubmitter, AlohaUser createdBy) {
		// OT_GROUP
		OTGroup otGroup = new OTGroup();
		otGroup.setPayPeriod(otPayPeriod);
		otGroup.setStatus(this.otUtilMB.getOTGroupStatus(OTGroupStatus.CodeValues.PENDING));
		otGroup.setSubmitter(otGroupSubmitter);
		otGroup.getCreatedBy().setUserCreated(createdBy.getUserId());
		
		
		// OT_GROUP_HISTORY
		OTGroupHistory otGroupHist = new OTGroupHistory();
		otGroupHist.setGroup(otGroup);
		otGroupHist.setStatusTransition(this.otUtilMB.getOTGroupStatusTrans(OTGroupStatusTrans.ActionCodeValues.NONE_TO_PENDING));
		otGroupHist.setHistorySequence(otGroup.getHistoricalEntries().size() + 1);
		otGroupHist.getCreatedBy().setUserCreated(createdBy.getUserId());
		
		otGroup.addGroupHistory(otGroupHist);
		return otGroup;
	}	
	
	public boolean otFundingRequiredProcessingIsRequired(OTDetail otDetail) {
		return	( (otDetail.isFundingRequired()) 
					&& (otDetail.isReceived()) 
					&& (otDetail.getGroup() == null)
				);
	}
	
	public void doFundingRequiredOTProcessing(OTDetail otDetail) throws AlohaServerException {
		if	( (otDetail.isFundingRequired()) 
				&& (otDetail.isReceived()) 
				&& (otDetail.getGroup() == null) ) {
			
			OTGroup otGroup = this.groupOvertimeEJB.retrieveOTGroupForSubmitter(otDetail.getSupervisor(), 
				otDetail.getPayPeriod(), this.otUtilMB.getOTGroupStatus(OTGroupStatus.CodeValues.PENDING));
			
			if (otGroup == null) {
				otGroup = this.createOTGroup(otDetail.getPayPeriod(), otDetail.getSupervisor(), this.userMB.getUser());
				this.groupOvertimeEJB.saveOTGroup(otGroup);
			}
			OTHeader otHeader = otDetail.getHeader();
			otHeader.setGroup(otGroup);
			this.overtimeEJB.updateOTHeader(otHeader);
		}				
	}
}