package gov.gsa.ocfo.aloha.web.mb.ot;

import gov.gsa.ocfo.aloha.ejb.overtime.OvertimeEJB;
import gov.gsa.ocfo.aloha.exception.AlohaServerException;
import gov.gsa.ocfo.aloha.exception.AuthorizationException;
import gov.gsa.ocfo.aloha.model.KeyValuePair;
import gov.gsa.ocfo.aloha.model.entity.overtime.OTDetail;
import gov.gsa.ocfo.aloha.model.entity.overtime.OTHeader;
import gov.gsa.ocfo.aloha.util.StopWatch;
import gov.gsa.ocfo.aloha.util.StringUtil;
import gov.gsa.ocfo.aloha.web.mb.UserMB;
import gov.gsa.ocfo.aloha.web.util.AlohaConstants;
import gov.gsa.ocfo.aloha.web.util.AlohaURIs;
import gov.gsa.ocfo.aloha.web.util.FacesContextUtil;
import gov.gsa.ocfo.aloha.web.util.OTGroupStatusChangeHelper;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.ejb.EJB;
import javax.faces.bean.ManagedProperty;
import javax.faces.context.FacesContext;

public abstract class OTViewAbstractMB implements Serializable {
	private static final long serialVersionUID = -8503091602711668193L;

	// ABSTRACT METHOD TO BE IMPLEMENTED BY SUB-CLASSES
	protected abstract void checkAccessRights() throws AuthorizationException;
	
	@EJB
	protected OvertimeEJB overtimeEJB;	

	// JSF INJECTED MANAGED BEAN
	@ManagedProperty(value="#{userMB}")
	protected UserMB userMB;	
	public void setUserMB(UserMB userMB) {
		this.userMB = userMB;
	}	
	
	// INSTANCE MEMBER
	protected OTDetail otDetail;
	// INSTANCE MEMBER
	protected List<KeyValuePair> otBalances = new ArrayList<KeyValuePair>();

	public void init() {
		try {
			StopWatch stopWatch = new StopWatch();
			stopWatch.start();			
			
			String paramId = FacesContextUtil.getHttpServletRequest().getParameter(AlohaConstants.OT_DETAIL_ID);
			if ( ! StringUtil.isNullOrEmpty(paramId)) {
				this.otDetail = this.overtimeEJB.retrieveDetailByID(Long.parseLong(paramId));
				
				OTHeader otHeader = this.otDetail.getHeader();
				
				boolean modifiableBySupervisor = OTGroupStatusChangeHelper.determineModifiablityOfSingleIndividualRequest(this.userMB.getUser(), otHeader);
				boolean cancellableBySupervisor = OTGroupStatusChangeHelper.determineCancelablityOfSingleIndividualRequest(this.userMB.getUser(), otHeader);				
				
				otHeader.setModifiableBySupervisor(modifiableBySupervisor);
				otHeader.setCancellableBySupervisor(cancellableBySupervisor);
				
				this.checkAccessRights();
				this.otBalances = this.overtimeEJB.retrieveOTBalances(this.otDetail.getEmployee().getUserId());
				
				stopWatch.stop();
				System.out.println("ELAPSED TIME (View Overtime Request): " + stopWatch.getElapsedTime() + " ms");
				stopWatch = null;				
			}
		} catch (NumberFormatException nfe) {
			try {
				FacesContext.getCurrentInstance().getExternalContext().redirect(AlohaURIs.SERVER_ERROR);
			} catch (IOException ignore) {};
		}catch (AuthorizationException ae) {
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