package gov.gsa.ocfo.aloha.web.mb.ot;

import gov.gsa.ocfo.aloha.ejb.overtime.OvertimeEJB;
import gov.gsa.ocfo.aloha.exception.AlohaServerException;
import gov.gsa.ocfo.aloha.exception.AuthorizationException;
import gov.gsa.ocfo.aloha.model.entity.overtime.OTDetail;
import gov.gsa.ocfo.aloha.model.entity.overtime.OTIndivStatus;
import gov.gsa.ocfo.aloha.web.mb.UserMB;
import gov.gsa.ocfo.aloha.web.model.overtime.OTStatusChangeOutcome;
import gov.gsa.ocfo.aloha.web.util.AlohaConstants;
import gov.gsa.ocfo.aloha.web.util.AlohaURIs;
import gov.gsa.ocfo.aloha.web.util.ErrorMessages;
import gov.gsa.ocfo.aloha.web.util.FacesContextUtil;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

@ManagedBean(name="otListSupvMB")
@ViewScoped
public class OTListSupvMB implements Serializable {
	private static final long serialVersionUID = 2484471479902354123L;

	@EJB
	protected OvertimeEJB overtimeEJB;	

	// JSF Managed Bean
	@ManagedProperty(value="#{userMB}")
	protected UserMB userMB;
	public void setUserMB(UserMB userMB) {
		this.userMB = userMB;
	}
	
	// LISTS
	private List<OTDetail> receivedList = new ArrayList<OTDetail>();
	private List<OTDetail> submittedList = new ArrayList<OTDetail>();
	private List<OTDetail> deniedList = new ArrayList<OTDetail>();
	private List<OTDetail> approvedList = new ArrayList<OTDetail>();	
	private List<OTDetail> cancelledList = new ArrayList<OTDetail>();
	
	// BOOLEAN CHECKBOXES
	private boolean showSubmittedList;
	private boolean showReceivedList;
	private boolean showDeniedList;
	private boolean showApprovedList;
	private boolean showCancelledList;	
		
	// CONFIRMATION MESSAGE
	private String confirmMsg;

	private final String displayNone = "display:none;";
	private final String displayBlock = "display:block;";
	
	@PostConstruct
	public void init() {
		try {
			this.checkAccessRights();
			this.initLists();
			this.initCheckboxes();
			this.inspectStatusChangeOutcome();
		} catch (AuthorizationException ae) {
			try {
				FacesContext.getCurrentInstance().getExternalContext().redirect(AlohaURIs.UNAUTHORIZED);
			} catch (IOException ignore) {};
		} catch (AlohaServerException ase) {
			try {
				FacesContext.getCurrentInstance().getExternalContext().redirect(AlohaURIs.UNAUTHORIZED);
			} catch (IOException ignore) {};
		} catch( Throwable t) {
			t.printStackTrace();
			try {
				FacesContext.getCurrentInstance().getExternalContext().redirect(AlohaURIs.SERVER_ERROR);
			} catch (IOException ignore) {};
		}
	}
	private void checkAccessRights() throws AuthorizationException {
		if ( ! this.userMB.getUser().isApprover()) {
			String errMsg = ErrorMessages.getInstance().getMessage(ErrorMessages.OT_VIEW_UNAUTHORIZED);
			FacesContextUtil.getHttpSession().setAttribute("unauthMsg", errMsg);
			throw new AuthorizationException(errMsg);
		}
	}
	private void initLists() throws AlohaServerException {
		this.receivedList = overtimeEJB.retrieveSupervisorList(this.userMB.getUser().getUserId(), OTIndivStatus.CodeValues.RECEIVED);
		this.submittedList = overtimeEJB.retrieveSupervisorList(this.userMB.getUser().getUserId(), OTIndivStatus.CodeValues.SUBMITTED);
		this.deniedList = overtimeEJB.retrieveSupervisorList(this.userMB.getUser().getUserId(), OTIndivStatus.CodeValues.DENIED);
		this.approvedList = overtimeEJB.retrieveSupervisorList(this.userMB.getUser().getUserId(), OTIndivStatus.CodeValues.APPROVED);	
		this.cancelledList = overtimeEJB.retrieveSupervisorList(this.userMB.getUser().getUserId(), OTIndivStatus.CodeValues.CANCELLED);
	}
	private void initCheckboxes() {
		this.showSubmittedList = (this.submittedList.size() > 0);
		this.showReceivedList = false;
		this.showApprovedList = false;
		this.showDeniedList = false;
		this.showCancelledList = false;
	}
	private void inspectStatusChangeOutcome() {
		if ( (FacesContextUtil.getHttpSession() != null)
				&& (FacesContextUtil.getHttpSession().getAttribute(AlohaConstants.OT_STATUS_CHANGE_OUTCOME_SUPV) != null)
				&& (FacesContextUtil.getHttpSession().getAttribute(AlohaConstants.OT_STATUS_CHANGE_OUTCOME_SUPV) instanceof OTStatusChangeOutcome)
			) {
			this.showSubmittedList = false;
			OTStatusChangeOutcome outcome = (OTStatusChangeOutcome)FacesContextUtil.getHttpSession().getAttribute(AlohaConstants.OT_STATUS_CHANGE_OUTCOME_SUPV);
			this.confirmMsg = outcome.getConfirmMsg();
			if ( outcome.getStatusCode().equals(OTIndivStatus.CodeValues.RECEIVED)) {
				this.showReceivedList = true;
			} else if ( outcome.getStatusCode().equals(OTIndivStatus.CodeValues.APPROVED)) {
				this.showApprovedList = true;
			} else if ( outcome.getStatusCode().equals(OTIndivStatus.CodeValues.DENIED)) {
				this.showDeniedList = true;
			} else if ( outcome.getStatusCode().equals(OTIndivStatus.CodeValues.CANCELLED)) {
				this.showCancelledList = true;
			}		
			FacesContextUtil.getHttpSession().setAttribute(AlohaConstants.OT_STATUS_CHANGE_OUTCOME_SUPV, null);	
		}
	}

	/*********************************************
	 * SETTERS
	 *********************************************/
	public void setShowSubmittedList(boolean showSubmittedList) {
		this.showSubmittedList = showSubmittedList;
	}
	public void setShowReceivedList(boolean showReceivedList) {
		this.showReceivedList = showReceivedList;
	}
	public void setShowDeniedList(boolean showDeniedList) {
		this.showDeniedList = showDeniedList;
	}
	public void setShowApprovedList(boolean showApprovedList) {
		this.showApprovedList = showApprovedList;
	}
	public void setShowCancelledList(boolean showCancelledList) {
		this.showCancelledList = showCancelledList;
	}
	/*********************************************
	 * GETTERS
	 *********************************************/	
	public String getConfirmMsg() {
		return confirmMsg;
	}		

	// CHECKBOXES	
	public boolean isShowSubmittedList() {
		return showSubmittedList;
	}
	public boolean isShowReceivedList() {
		return showReceivedList;
	}
	public boolean isShowDeniedList() {
		return showDeniedList;
	}
	public boolean isShowApprovedList() {
		return showApprovedList;
	}
	public boolean isShowCancelledList() {
		return showCancelledList;
	}
	
	// LISTS
	public List<OTDetail> getSubmittedList() {
		return submittedList;
	}
	public List<OTDetail> getReceivedList() {
		return receivedList;
	}
	public List<OTDetail> getDeniedList() {
		return deniedList;
	}
	public List<OTDetail> getApprovedList() {
		return approvedList;
	}
	public List<OTDetail> getCancelledList() {
		return cancelledList;
	}	
	
	// COUNTS
	public int getSubmittedListCount() {
		return this.submittedList.size();
	}
	public int getReceivedListCount() {
		return this.receivedList.size();
	}
	public int getApprovedListCount() {
		return this.approvedList.size();
	}
	public int getDeniedListCount() {
		return this.deniedList.size();
	}
	public int getCancelledListCount() {
		return this.cancelledList.size();
	}

	// DISPLAY
	public String getSubmittedListDisplay() {
		return ( (this.showSubmittedList) ? this.displayBlock : this.displayNone );	
	}
	public String getReceivedListDisplay() {
		return ( (this.showReceivedList) ? this.displayBlock : this.displayNone );
	}
	public String getApprovedListDisplay() {
		return ( (this.showApprovedList) ? this.displayBlock : this.displayNone );
	}
	public String getDeniedListDisplay() {
		return ( (this.showDeniedList) ? this.displayBlock : this.displayNone );
	}
	public String getCancelledListDisplay() {
		return ( (this.showCancelledList) ? this.displayBlock : this.displayNone );
	}	
}