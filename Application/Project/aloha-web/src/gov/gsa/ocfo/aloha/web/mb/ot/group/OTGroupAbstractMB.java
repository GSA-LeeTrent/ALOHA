package gov.gsa.ocfo.aloha.web.mb.ot.group;

import gov.gsa.ocfo.aloha.ejb.overtime.GroupOvertimeEJB;
import gov.gsa.ocfo.aloha.exception.AlohaServerException;
import gov.gsa.ocfo.aloha.exception.AuthorizationException;
import gov.gsa.ocfo.aloha.exception.IllegalOperationException;
import gov.gsa.ocfo.aloha.model.entity.overtime.OTGroup;
import gov.gsa.ocfo.aloha.model.entity.overtime.OTGroupStatus;
import gov.gsa.ocfo.aloha.util.StopWatch;
import gov.gsa.ocfo.aloha.util.StringUtil;
import gov.gsa.ocfo.aloha.web.mb.UserMB;
import gov.gsa.ocfo.aloha.web.util.AlohaConstants;
import gov.gsa.ocfo.aloha.web.util.AlohaURIs;
import gov.gsa.ocfo.aloha.web.util.FacesContextUtil;
import gov.gsa.ocfo.aloha.web.util.NormalMessages;
import gov.gsa.ocfo.aloha.web.util.OTGroupStatusChangeHelper;

import java.io.IOException;
import java.io.Serializable;

import javax.ejb.EJB;
import javax.faces.bean.ManagedProperty;
import javax.faces.context.FacesContext;

public abstract class OTGroupAbstractMB implements Serializable {

	private static final long serialVersionUID = 3344057986305029878L;

	// ABSTRACT METHODS REQUIRED TO BY IMPLEMENTED BY SUBCLASSES
	protected abstract void checkAccessRights() throws AuthorizationException;
	protected abstract void checkIfLegalOperation()	throws IllegalOperationException;
	protected abstract String getGroupSessionKey();
	
	@EJB
	protected GroupOvertimeEJB groupOvertimeEJB;
	
	// JSF: MANAGED BEAN INJECTION
	@ManagedProperty(value = "#{userMB}")
	protected UserMB userMB;
	public void setUserMB(UserMB userMB) {
		this.userMB = userMB;	
	}
	// INSTANCE MEMBERS
	private OTGroup otGroup;
	protected OTGroupStatus statusCurrentlyInForce;
	private String topPanelHeaderText;
	
	// INIT METHOD
	protected void init() {
		try {

			this.initGroup();
			
			Object[] params = { Long.valueOf(this.otGroup.getId()) };
			this.topPanelHeaderText = NormalMessages.getInstance().getMessage(NormalMessages.APPLICATION_SECTION_TITLE_OT_GROUP_TOP_PANEL, params);

			this.checkAccessRights();
			this.checkIfLegalOperation();
			
		} catch (NumberFormatException nfe) {
			try {
				nfe.printStackTrace();
				FacesContext.getCurrentInstance().getExternalContext().redirect(AlohaURIs.SERVER_ERROR);
			} catch (IOException ignore) {}
		} catch (AlohaServerException ase) {
			try {
				FacesContext.getCurrentInstance().getExternalContext().redirect(AlohaURIs.SERVER_ERROR);
			} catch (IOException ignore) {}
		} catch (AuthorizationException ae) {
			try {
				FacesContext.getCurrentInstance().getExternalContext().redirect(AlohaURIs.SERVER_ERROR);
			} catch (IOException ignore) {}
		} catch (IllegalOperationException ise) {
			try {
				FacesContext.getCurrentInstance().getExternalContext().redirect(AlohaURIs.ILLEGAL_OPERATION);
			} catch (IOException ignore) {}
		} catch (Throwable t) {
			t.printStackTrace();
			try {
				t.printStackTrace();
				FacesContext.getCurrentInstance().getExternalContext().redirect(AlohaURIs.SERVER_ERROR);
			} catch (IOException ignore) {}
		}
	}
	
	private void initGroup() throws AlohaServerException {
		String paramId = FacesContextUtil.getHttpServletRequest().getParameter(AlohaConstants.OT_GROUP_ID);
	
		if ( StringUtil.isNullOrEmpty(paramId)) {
			this.otGroup = (OTGroup)FacesContextUtil.getHttpSession().getAttribute(this.getGroupSessionKey());
		} else {
			StopWatch stopWatch = new StopWatch();
			stopWatch.start();
			this.otGroup = this.groupOvertimeEJB.retrieveByGroupId(Long.parseLong(paramId));
			stopWatch.stop();
			System.out.println("ELAPSED TIME (groupOvertimeEJB.retrieveByGroupId): " + stopWatch.getElapsedTime() + " ms");
			stopWatch = null;

			this.otGroup.setCancellable(OTGroupStatusChangeHelper.determineCancelablityOfSingleGroupRequest(this.userMB.getUser(), this.otGroup));	
			this.otGroup.setFinalizable(OTGroupStatusChangeHelper.determineFinalizablityOfSingleGroupRequest(this.userMB.getUser(), this.otGroup));	
			
			FacesContextUtil.getHttpSession().setAttribute(this.getGroupSessionKey(), this.otGroup);
		}
		this.statusCurrentlyInForce = this.otGroup.getStatus();		
	}
	
	/*****************************************
	 * GETTERS:
	 *****************************************/
	
	public OTGroup getOtGroup() {
		if ( this.otGroup == null) {
			try {
				this.initGroup();
			} catch (AlohaServerException e) {
				e.printStackTrace();
			}
		}
		return this.otGroup;
	}
	
	public String getTopPanelHeaderText() {
		return topPanelHeaderText;
	}
	protected void setOtGroup(OTGroup otGroup) {
		this.otGroup = otGroup;
	}
	
	
}