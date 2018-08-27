package gov.gsa.ocfo.aloha.web.mb.ot;

import gov.gsa.ocfo.aloha.ejb.overtime.OvertimeEJB;
import gov.gsa.ocfo.aloha.exception.AlohaServerException;
import gov.gsa.ocfo.aloha.exception.AuthorizationException;
import gov.gsa.ocfo.aloha.model.entity.overtime.OTDetail;
import gov.gsa.ocfo.aloha.util.StringUtil;
import gov.gsa.ocfo.aloha.web.mb.UserMB;
import gov.gsa.ocfo.aloha.web.model.overtime.OTStatusChangeOutcome;
import gov.gsa.ocfo.aloha.web.util.AlohaURIs;
import gov.gsa.ocfo.aloha.web.util.FacesContextUtil;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.ejb.EJB;
import javax.faces.bean.ManagedProperty;
import javax.faces.context.FacesContext;

public abstract class OTListAbstractMB implements Serializable {
	private static final long serialVersionUID = 135714682259172740L;

	// ABSTRACT METHOD TO BE IMPLEMENTED BY SUB-CLASSES
	protected abstract void checkAccessRights() throws AuthorizationException;
	
	// ABSTRACT METHOD TO BE IMPLEMENTED BY SUB-CLASSES
	protected abstract List<OTDetail> retrieveOTDetaiList() throws AlohaServerException;
	
	// ABSTRACT METHOD TO BE IMPLEMENTED BY SUB-CLASSES
	protected abstract String getStatusChangeOutcomeKey();
	
	@EJB
	protected OvertimeEJB overtimeEJB;	

	// JSF INJECTED MANAGED BEAN
	@ManagedProperty(value="#{userMB}")
	protected UserMB userMB;	
	public void setUserMB(UserMB userMB) {
		this.userMB = userMB;
	}	
	// INSTANCE MEMBER
	protected List<OTDetail> otDetaiList = new ArrayList<OTDetail>();
	// INSTANCE MEMBER
	protected String confirmMsg;
	
	public void init() {
		try {
			this.checkAccessRights();
			this.otDetaiList = this.retrieveOTDetaiList();	
			this.initConfirmMsg();
		} catch (AuthorizationException ae) {
			try {
				FacesContext.getCurrentInstance().getExternalContext().redirect(AlohaURIs.UNAUTHORIZED);
			} catch (IOException ignore) {};
		} catch (AlohaServerException ase) {
			try {
				FacesContext.getCurrentInstance().getExternalContext().redirect(AlohaURIs.SERVER_ERROR);
			} catch (IOException ignore) {};
		} catch( Throwable t) {
			t.printStackTrace();
			try {
				FacesContext.getCurrentInstance().getExternalContext().redirect(AlohaURIs.SERVER_ERROR);
			} catch (IOException ignore) {};
		}
	}	
	
	private void initConfirmMsg() {
		String statusChangeOutcomeKey = this.getStatusChangeOutcomeKey();
		if ( (!StringUtil.isNullOrEmpty(statusChangeOutcomeKey))
				&& (FacesContextUtil.getHttpSession() != null)
				&& (FacesContextUtil.getHttpSession().getAttribute(statusChangeOutcomeKey) != null)
				&& (FacesContextUtil.getHttpSession().getAttribute(statusChangeOutcomeKey) instanceof OTStatusChangeOutcome)
			) {
			OTStatusChangeOutcome outcome = (OTStatusChangeOutcome)FacesContextUtil.getHttpSession().getAttribute(statusChangeOutcomeKey);
			this.confirmMsg = outcome.getConfirmMsg();
			FacesContextUtil.getHttpSession().setAttribute(statusChangeOutcomeKey, null);
		}
	}	

	/*********************************************
	 * GETTERS
	 *********************************************/	
	public String getConfirmMsg() {
		return confirmMsg;
	}
	public List<OTDetail> getOtDetaiList() {
		return otDetaiList;
	}	
}
