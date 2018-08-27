package gov.gsa.ocfo.aloha.web.mb.ot;

import gov.gsa.ocfo.aloha.ejb.overtime.GroupOvertimeEJB;
import gov.gsa.ocfo.aloha.ejb.overtime.OvertimeEJB;
import gov.gsa.ocfo.aloha.exception.AlohaServerException;
import gov.gsa.ocfo.aloha.exception.AuthorizationException;
import gov.gsa.ocfo.aloha.exception.IllegalOperationException;
import gov.gsa.ocfo.aloha.model.KeyValuePair;
import gov.gsa.ocfo.aloha.model.entity.overtime.OTDetail;
import gov.gsa.ocfo.aloha.model.entity.overtime.OTDetailHistory;
import gov.gsa.ocfo.aloha.model.entity.overtime.OTDetailSubmitterRemark;
import gov.gsa.ocfo.aloha.model.entity.overtime.OTDetailSupervisorRemark;
import gov.gsa.ocfo.aloha.model.entity.overtime.OTHeader;
import gov.gsa.ocfo.aloha.model.entity.overtime.OTIndivStatusTrans;
import gov.gsa.ocfo.aloha.util.StopWatch;
import gov.gsa.ocfo.aloha.util.StringUtil;
import gov.gsa.ocfo.aloha.web.mb.UserMB;
import gov.gsa.ocfo.aloha.web.mb.ot.group.OTGroupMB;
import gov.gsa.ocfo.aloha.web.mb.overtime.OTUtilMB;
import gov.gsa.ocfo.aloha.web.model.overtime.OTStatusChangeOutcome;
import gov.gsa.ocfo.aloha.web.security.NavigationOutcomes;
import gov.gsa.ocfo.aloha.web.util.AlohaConstants;
import gov.gsa.ocfo.aloha.web.util.AlohaURIs;
import gov.gsa.ocfo.aloha.web.util.FacesContextUtil;
import gov.gsa.ocfo.aloha.web.util.NormalMessages;
import gov.gsa.ocfo.aloha.web.util.OTGroupStatusChangeHelper;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.ejb.EJB;
import javax.faces.bean.ManagedProperty;
import javax.faces.context.FacesContext;

public abstract class OTStatusChangeAbstractMB implements Serializable {
	private static final long serialVersionUID = -8144727299182312654L;

	protected abstract void checkAccessRights() throws AuthorizationException;
	protected abstract void checkIfLegalOperation() throws IllegalOperationException;
	protected abstract String getNewStatusCode();
	protected abstract String getSubmitterRemarks();
	protected abstract String getSupervisorRemarks();
	protected abstract String getSuccessPage();
	protected abstract String getStatusChangeOutcomeKey();

	@EJB
	protected OvertimeEJB overtimeEJB;
	
	@EJB
	protected GroupOvertimeEJB groupOvertimeEJB;
	
	// JSF: MANAGED BEAN INJECTION
	@ManagedProperty(value="#{userMB}")
	protected UserMB userMB;		
	public void setUserMB(UserMB userMB) {
		this.userMB = userMB;
	}
	// JSF: MANAGED BEAN INJECTION
	@ManagedProperty(value="#{otUtilMB}")
	protected OTUtilMB otUtilMB;
	public void setOtUtilMB(OTUtilMB otUtilMB) {
		this.otUtilMB = otUtilMB;
	}
	// JSF: MANAGED BEAN INJECTION
	@ManagedProperty(value="#{otGroupMB}")
	protected OTGroupMB otGroupMB;
	public void setOtGroupMB(OTGroupMB otGroupMB) {
		this.otGroupMB = otGroupMB;
	}	

	// INSTANCE MEMBERS
	protected OTDetail otDetail;
	private List<KeyValuePair> otBalances = new ArrayList<KeyValuePair>();

	// INIT METHOD
	protected void init() {
		try {
			String paramId = FacesContextUtil.getHttpServletRequest().getParameter(AlohaConstants.OT_DETAIL_ID);

			//----------------------------------------------------------------------------------------------------------
			// IMPORTANT:
			//----------------------------------------------------------------------------------------------------------
			// With View Scoped Managed Beans, the @PostConstruct method is called even when a user tries to get off the page.
			// For example, this method will get called even when the user clicks the "Quit" button 
			// or tries to link off the page by clicking links such as "Home" and "Sitemap".
			// As such, we check for the HTTP Request Parameter before continuing.
			if ( !StringUtil.isNullOrEmpty(paramId)) {
				this.otDetail = this.overtimeEJB.retrieveDetailByID(Long.parseLong(paramId));
				
				OTHeader otHeader = this.otDetail.getHeader();
				
				boolean modifiableBySupervisor = OTGroupStatusChangeHelper.determineModifiablityOfSingleIndividualRequest(this.userMB.getUser(), otHeader);
				boolean cancellableBySupervisor = OTGroupStatusChangeHelper.determineCancelablityOfSingleIndividualRequest(this.userMB.getUser(), otHeader);				
				
				otHeader.setModifiableBySupervisor(modifiableBySupervisor);
				otHeader.setCancellableBySupervisor(cancellableBySupervisor);				
				
				this.checkAccessRights();
				this.checkIfLegalOperation();
				this.otBalances = this.overtimeEJB.retrieveOTBalances(this.otDetail.getEmployee().getUserId());	
			} else {
				FacesContextUtil.callHome();
			}
		} catch (NumberFormatException nfe) {
			try {
				nfe.printStackTrace();
				FacesContext.getCurrentInstance().getExternalContext().redirect(AlohaURIs.SERVER_ERROR);
			} catch (IOException ignore) {};
		}  catch (AlohaServerException ase) {
			try {
				FacesContext.getCurrentInstance().getExternalContext().redirect(AlohaURIs.SERVER_ERROR);
			} catch (IOException ignore) {};
		} catch (AuthorizationException ae) {
			try {
				FacesContext.getCurrentInstance().getExternalContext().redirect(AlohaURIs.UNAUTHORIZED);
			} catch (IOException ignore) {};
		} catch (IllegalOperationException ise) {
			try {
				FacesContext.getCurrentInstance().getExternalContext().redirect(AlohaURIs.ILLEGAL_OPERATION);
			} catch (IOException ignore) {};
		} catch( Throwable t) {
			t.printStackTrace();
			try {
				t.printStackTrace();
				FacesContext.getCurrentInstance().getExternalContext().redirect(AlohaURIs.SERVER_ERROR);
			} catch (IOException ignore) {};
		}
	}
	
	// EVENT
	protected String onStatusChange() {
		try {
			OTIndivStatusTrans otStatusTrans = this.otUtilMB.getOTIndivStatusTrans(this.determineOTIndivStatusTransitionCode());
			
			// STATUS
			this.otDetail.setStatus(this.otUtilMB.getOTIndivStatus(this.getNewStatusCode()));
			
			// HISTORY
			OTDetailHistory otHistory = new OTDetailHistory();
			otHistory.setDetail(this.otDetail);
			otHistory.setStatusTransition(otStatusTrans);
			otHistory.setActor(this.userMB.getUser());
			
			otHistory.setActionDatetime(new Date());
			this.otDetail.addDetailHistory(otHistory);

			// SUBMITTER REMARK
			if ( !StringUtil.isNullOrEmpty(this.getSubmitterRemarks()) ) {
				OTDetailSubmitterRemark otSubmitterRemark = new OTDetailSubmitterRemark();
				otSubmitterRemark.setDetail(this.otDetail);
				otSubmitterRemark.setStatusTransition(otStatusTrans);
				otSubmitterRemark.setSequence(this.otDetail.getSubmitterRemarks().size() + 1);
				otSubmitterRemark.setText(this.getSubmitterRemarks());
				otDetail.addSubmitterRemark(otSubmitterRemark);
			}

			// SUPERVISOR REMARK
			if ( !StringUtil.isNullOrEmpty(this.getSupervisorRemarks()) ) {
				OTDetailSupervisorRemark otSupervisorRemark = new OTDetailSupervisorRemark();
				otSupervisorRemark.setDetail(this.otDetail);
				otSupervisorRemark.setStatusTransition(otStatusTrans);
				otSupervisorRemark.setSequence(this.otDetail.getSupervisorRemarks().size() + 1);
				otSupervisorRemark.setText(this.getSupervisorRemarks());
				this.otDetail.addSupervisorRemark(otSupervisorRemark);
			}		
			
			if ( this.otGroupMB.otFundingRequiredProcessingIsRequired(this.otDetail)) {
				this.otGroupMB.doFundingRequiredOTProcessing(otDetail);
				FacesContextUtil.getHttpSession().setAttribute(this.getStatusChangeOutcomeKey(), this.buildStatusChangeOutcome(this.otDetail.getGroup().getId()));
			} else {
				
				StopWatch stopWatch = new StopWatch();
				stopWatch.start();
				this.overtimeEJB.updateOTDetail(this.otDetail);
				stopWatch.stop();
				System.out.println("ELAPSED TIME (Overtime Request Status Change): " + stopWatch.getElapsedTime() + " ms");
				stopWatch = null;
				
				FacesContextUtil.getHttpSession().setAttribute(this.getStatusChangeOutcomeKey(), this.buildStatusChangeOutcome());
			}
			return this.getSuccessPage();			
		} catch (AlohaServerException ase) {
			return NavigationOutcomes.SERVER_ERROR;
		} catch (Exception e) {
			e.printStackTrace();
			return NavigationOutcomes.SERVER_ERROR;
		}	
	}
	
	// HELPER METHOD
	private String determineOTIndivStatusTransitionCode() {
		return (this.otUtilMB.determineOTIndivStatusTransCode(this.otDetail.getStatus().getCode(), this.getNewStatusCode()));
	}	
	// HELPER METHOD
	private OTStatusChangeOutcome buildStatusChangeOutcome() {
		return new OTStatusChangeOutcome(this.getNewStatusCode(), this.buildConfirmationMessage());
	}
	// HELPER METHOD
	private OTStatusChangeOutcome buildStatusChangeOutcome(long groupId) {
		return new OTStatusChangeOutcome(this.getNewStatusCode(), this.buildConfirmationMessage(groupId));
	}	
	// HELPER METHOD
	private String buildConfirmationMessage() {
		String employeeName = null;
		if ( this.otDetail.getEmployee().getFullName().endsWith("s")) {
			employeeName = this.otDetail.getEmployee().getFullName() + "'"; 
		} else {
			employeeName = this.otDetail.getEmployee().getFullName() + "'s";
		}
		Object[] params = {this.otDetail.getType().getName(),
				employeeName,
				this.otDetail.getPayPeriod().getShortLabel(), 
				this.otDetail.getStatus().getName()};
		return (NormalMessages.getInstance().getMessage(NormalMessages.OT_MSG_STATUS_CHANGE, params));
	}
	// HELPER METHOD
	private String buildConfirmationMessage(long groupId) {
		String employeeName = null;
		if ( this.otDetail.getEmployee().getFullName().endsWith("s")) {
			employeeName = this.otDetail.getEmployee().getFullName() + "'"; 
		} else {
			employeeName = this.otDetail.getEmployee().getFullName() + "'s";
		}
		Object[] params = {this.otDetail.getType().getName(),
				employeeName,
				this.otDetail.getPayPeriod().getShortLabel(), 
				this.otDetail.getStatus().getName(),
				groupId};
		return (NormalMessages.getInstance().getMessage(NormalMessages.OT_MSG_STATUS_CHANGE_CONSOLIDATED, params));
	}	
	/*****************************************
	 * GETTERS
	 *****************************************/
	public OTDetail getOtDetail() {
		return otDetail;
	}
	public List<KeyValuePair> getOtBalances() {
		return otBalances;
	}
}
