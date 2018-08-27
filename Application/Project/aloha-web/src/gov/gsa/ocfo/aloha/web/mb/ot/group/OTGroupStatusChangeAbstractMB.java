package gov.gsa.ocfo.aloha.web.mb.ot.group;

import gov.gsa.ocfo.aloha.exception.AlohaServerException;
import gov.gsa.ocfo.aloha.model.entity.AlohaUser;
import gov.gsa.ocfo.aloha.model.entity.overtime.OTGroup;
import gov.gsa.ocfo.aloha.model.entity.overtime.OTGroupHistory;
import gov.gsa.ocfo.aloha.model.entity.overtime.OTGroupReceiverRemark;
import gov.gsa.ocfo.aloha.model.entity.overtime.OTGroupStatusTrans;
import gov.gsa.ocfo.aloha.model.entity.overtime.OTGroupSubmitterRemark;
import gov.gsa.ocfo.aloha.util.StopWatch;
import gov.gsa.ocfo.aloha.util.StringUtil;
import gov.gsa.ocfo.aloha.web.mb.overtime.OTUtilMB;
import gov.gsa.ocfo.aloha.web.model.overtime.OTStatusChangeOutcome;
import gov.gsa.ocfo.aloha.web.security.NavigationOutcomes;
import gov.gsa.ocfo.aloha.web.util.FacesContextUtil;
import gov.gsa.ocfo.aloha.web.util.OTGroupStatusTransUtil;

import java.io.Serializable;

import javax.faces.bean.ManagedProperty;

public abstract class OTGroupStatusChangeAbstractMB extends OTGroupAbstractMB implements Serializable {
	private static final long serialVersionUID = -4169381171606791727L;

	// ABSTRACT METHODS REQUIRED TO BY IMPLEMENTED BY SUBCLASSES
	protected abstract String getNewStatusCode();
	protected abstract AlohaUser getSubmitter();
	protected abstract AlohaUser getReceiver();
	protected abstract String getSubmitterRemarks();
	protected abstract String getReceiverRemarks();
	protected abstract void processEmployees();
	protected abstract void processChildGroups();
	protected abstract String createConfirmationMessage();
	protected abstract String getSuccessPage();
	protected abstract String getStatusChangeOutcomeKey();
	
	// JSF: MANAGED BEAN INJECTION
	@ManagedProperty(value="#{otUtilMB}")
	protected OTUtilMB otUtilMB;
	public void setOtUtilMB(OTUtilMB otUtilMB) {
		this.otUtilMB = otUtilMB;
	}
	
	// INIT
	protected void init() {
		super.init();
	}
	
	protected void processStatusChange() {
		this.getOtGroup().setStatus(this.otUtilMB.getOTGroupStatus(this.getNewStatusCode()));
		this.getOtGroup().setSubmitter(this.getSubmitter());
		this.getOtGroup().setReceiver(this.getReceiver());
	
		//System.out.println("Old Status Code: " + this.statusCurrentlyInForce.getCode());
		//System.out.println("New Status Code: " + this.getNewStatusCode());
		
		String statusTransCode = OTGroupStatusTransUtil.determineOTGroupStatusTransCode(this.statusCurrentlyInForce.getCode(), this.getNewStatusCode());
			
		//System.out.println("statusTransCode: " + statusTransCode);
		
		OTGroupStatusTrans statusTrans = this.otUtilMB.getOTGroupStatusTrans(statusTransCode);
		
		// Even though OTSubmitterRemark and OTReceiverRemark are different classes, they both share the same table.
		// There is a unique constraint on the OT_GROUP_REMARK table (OT_GROUP_REMARK_UQ1).
		// Here's the definition:
		// CREATE UNIQUE INDEX OT_GROUP_REMARK_UQ1 ON OT_GROUP_REMARK
		// (OT_GROUP_ID, REMARK_SEQ)
		// As such, we have to take the following measures to assure that this unique constraint is not violated. 
		int remarkSequence = this.getOtGroup().getSubmitterRemarks().size() + this.getOtGroup().getReceiverRemarks().size();
		
		// SUBMITTER REMARK
		if ( !StringUtil.isNullOrEmpty(this.getSubmitterRemarks()) ) {
			OTGroupSubmitterRemark submitterRemark = new OTGroupSubmitterRemark();
			this.getOtGroup().addSubmitterRemark(submitterRemark);
			submitterRemark.setGroup(this.getOtGroup());
			submitterRemark.setStatusTransition(statusTrans);
			//submitterRemark.setRemarkSequence(this.otGroup.getSubmitterRemarks().size());
			submitterRemark.setRemarkSequence( (remarkSequence + 1) );
			submitterRemark.setText(this.getSubmitterRemarks());
			submitterRemark.getCreatedBy().setUserCreated(this.userMB.getUserId());
			
		}
		
		// Calculate the remark sequence again in case there was a submitter remark:
		remarkSequence = this.getOtGroup().getSubmitterRemarks().size() + this.getOtGroup().getReceiverRemarks().size();
		
		// RECEIVER REMARK
		if ( !StringUtil.isNullOrEmpty(this.getReceiverRemarks()) ) {
			OTGroupReceiverRemark receiverRemark = new OTGroupReceiverRemark();
			this.getOtGroup().addReceiverRemark(receiverRemark);
			receiverRemark.setGroup(this.getOtGroup());
			receiverRemark.setStatusTransition(statusTrans);
			//receiverRemark.setRemarkSequence(this.otGroup.getReceiverRemarks().size());
			receiverRemark.setRemarkSequence( (remarkSequence + 1) );
			receiverRemark.setText(this.getReceiverRemarks());
			receiverRemark.getCreatedBy().setUserCreated(this.userMB.getUserId());
		}			
		
		// GROUP_HISTORY
		OTGroupHistory groupHist = new OTGroupHistory();
		groupHist.setGroup(this.getOtGroup());
		groupHist.setStatusTransition(statusTrans);
		groupHist.setHistorySequence(this.getOtGroup().getHistoricalEntries().size() + 1);
		groupHist.getCreatedBy().setUserCreated(this.userMB.getUserId());
		this.getOtGroup().addGroupHistory(groupHist);

		this.processChildGroups();
		this.processEmployees();		
	}
	protected void persistStatusChange() throws AlohaServerException {
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		OTGroup updatedOTGroup = this.groupOvertimeEJB.updateOTGroup(this.getOtGroup());
		stopWatch.stop();
		System.out.println("ELAPSED TIME (Overtime Group Status Change): " + stopWatch.getElapsedTime() + " ms");
		stopWatch = null;
		this.setOtGroup(updatedOTGroup);
	}
	protected void processAndPersistStatusChange() throws AlohaServerException {
		this.processStatusChange();
		this.persistStatusChange();
	}
	protected String onStatusChange() {
		try {
			this.processStatusChange();
			this.persistStatusChange();
			FacesContextUtil.getHttpSession().setAttribute(this.getStatusChangeOutcomeKey(), this.buildStatusChangeOutcome());
			return this.getSuccessPage();			
		} catch (AlohaServerException ase) {
			return  NavigationOutcomes.SERVER_ERROR;
		} catch (Exception e) {
			e.printStackTrace();
			return  NavigationOutcomes.SERVER_ERROR;
		} finally {
			FacesContextUtil.getHttpSession().removeAttribute(this.getGroupSessionKey());
		}
	}
	// HELPER METHOD
	private OTStatusChangeOutcome buildStatusChangeOutcome() {
		return new OTStatusChangeOutcome(this.getNewStatusCode(),
				this.createConfirmationMessage());
	}
	
/*	
	protected String onStatusChange() {
		try {
			this.otGroup.setStatus(this.otUtilMB.getOTGroupStatus(this.getNewStatusCode()));
			this.otGroup.setSubmitter(this.getSubmitter());
			this.otGroup.setReceiver(this.getReceiver());
		
			String statusTransCode = OTGroupStatusTransUtil.determineOTGroupStatusTransCode(this.statusCurrentlyInForce.getCode(), this.getNewStatusCode());
			OTGroupStatusTrans statusTrans = this.otUtilMB.getOTGroupStatusTrans(statusTransCode);
			
			// SUBMITTER REMARK
			if ( !StringUtil.isNullOrEmpty(this.getSubmitterRemarks()) ) {
				OTGroupSubmitterRemark submitterRemark = new OTGroupSubmitterRemark();
				submitterRemark.setGroup(this.otGroup);
				submitterRemark.setStatusTransition(statusTrans);
				submitterRemark.setRemarkSequence(this.otGroup.getSubmitterRemarks().size() + 1);
				submitterRemark.setText(this.getSubmitterRemarks());
				submitterRemark.getCreatedBy().setUserCreated(this.userMB.getUserId());
				this.otGroup.addSubmitterRemark(submitterRemark);
			}
			
			// RECEIVER REMARK
			if ( !StringUtil.isNullOrEmpty(this.getReceiverRemarks()) ) {
				OTGroupReceiverRemark receiverRemark = new OTGroupReceiverRemark();
				receiverRemark.setGroup(this.otGroup);
				receiverRemark.setStatusTransition(statusTrans);
				receiverRemark.setRemarkSequence(this.otGroup.getReceiverRemarks().size() + 1);
				receiverRemark.setText(this.getReceiverRemarks());
				receiverRemark.getCreatedBy().setUserCreated(this.userMB.getUserId());
				this.otGroup.addReceiverRemark(receiverRemark);
			}			
			
			// GROUP_HISTORY
			OTGroupHistory groupHist = new OTGroupHistory();
			groupHist.setGroup(this.otGroup);
			groupHist.setStatusTransition(statusTrans);
			groupHist.setHistorySequence(this.otGroup.getHistoricalEntries().size() + 1);
			groupHist.getCreatedBy().setUserCreated(this.userMB.getUserId());
			this.otGroup.addGroupHistory(groupHist);

			this.processChildGroups();
			this.processEmployees();
			
			// SAVE TO DATABASE
			this.groupOvertimeEJB.updateOTGroup(otGroup);
			
			FacesContextUtil.getHttpSession().setAttribute(this.getStatusChangeOutcomeKey(), this.buildStatusChangeOutcome());
			return this.getSuccessPage();			
		} catch (AlohaServerException ase) {
			return  NavigationOutcomes.SERVER_ERROR;
		} catch (Exception e) {
			e.printStackTrace();
			return  NavigationOutcomes.SERVER_ERROR;
		} 
		
	}
*/	
}