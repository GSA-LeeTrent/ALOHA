package gov.gsa.ocfo.aloha.web.mb.ot.list;

import gov.gsa.ocfo.aloha.ejb.overtime.OTIndivListEJB;
import gov.gsa.ocfo.aloha.exception.AlohaServerException;
import gov.gsa.ocfo.aloha.exception.AuthorizationException;
import gov.gsa.ocfo.aloha.model.overtime.OTListRow;
import gov.gsa.ocfo.aloha.util.StopWatch;
import gov.gsa.ocfo.aloha.util.StringUtil;
import gov.gsa.ocfo.aloha.web.mb.UserMB;
import gov.gsa.ocfo.aloha.web.model.overtime.OTStatusChangeOutcome;
import gov.gsa.ocfo.aloha.web.util.AlohaURIs;
import gov.gsa.ocfo.aloha.web.util.FacesContextUtil;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

import javax.ejb.EJB;
import javax.faces.bean.ManagedProperty;
import javax.faces.context.FacesContext;

import com.icesoft.faces.facelets.component.table.autosort.AutoSortTableBean;

public abstract class OTIndivListAbstractMB implements Serializable {
	private static final long serialVersionUID = -187473600397134997L;
	private static final String ID = "id";

	// ABSTRACT METHOD TO BE IMPLEMENTED BY SUB-CLASSES
	protected abstract void checkAccessRights() throws AuthorizationException;
	
	// ABSTRACT METHOD TO BE IMPLEMENTED BY SUB-CLASSES
	protected abstract List<OTListRow> retrieveOTIndivList() throws AlohaServerException;
	
	// ABSTRACT METHOD TO BE IMPLEMENTED BY SUB-CLASSES
	protected abstract String getStatusChangeOutcomeKey();
	
	@EJB
	protected OTIndivListEJB otIndivListEJB;	

	// JSF INJECTED MANAGED BEAN
	@ManagedProperty(value="#{userMB}")
	protected UserMB userMB;	
	public void setUserMB(UserMB userMB) {
		this.userMB = userMB;
	}	
	// INSTANCE MEMBER
	 private AutoSortTableBean tableBean;
	// INSTANCE MEMBER
	protected String confirmMsg;
	
	public void init() {
		try {
			this.checkAccessRights();
			
			StopWatch stopWatch = new StopWatch();
			stopWatch.start();
			this.tableBean = new AutoSortTableBean(this.retrieveOTIndivList(), ID);
			stopWatch.stop();
			System.out.println("ELAPSED TIME (Retrieve Overtime List - My Requests & My Team): " + stopWatch.getElapsedTime() + " ms");
			stopWatch = null;			
			
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

	public AutoSortTableBean getTableBean() {
		return tableBean;
	}
}
