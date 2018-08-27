package gov.gsa.ocfo.aloha.web.mb.ot.group;

import gov.gsa.ocfo.aloha.ejb.UserEJB;
import gov.gsa.ocfo.aloha.ejb.leave.EmployeeEJB;
import gov.gsa.ocfo.aloha.exception.AlohaServerException;
import gov.gsa.ocfo.aloha.exception.AuthorizationException;
import gov.gsa.ocfo.aloha.exception.IllegalOperationException;
import gov.gsa.ocfo.aloha.model.entity.AlohaUser;
import gov.gsa.ocfo.aloha.model.entity.AlohaUserPref;
import gov.gsa.ocfo.aloha.model.entity.overtime.OTGroupStatus;
import gov.gsa.ocfo.aloha.util.StringUtil;
import gov.gsa.ocfo.aloha.web.security.NavigationOutcomes;
import gov.gsa.ocfo.aloha.web.util.AlohaConstants;
import gov.gsa.ocfo.aloha.web.util.AlohaURIs;
import gov.gsa.ocfo.aloha.web.util.ErrorMessages;
import gov.gsa.ocfo.aloha.web.util.FacesContextUtil;
import gov.gsa.ocfo.aloha.web.util.NormalMessages;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;

@ManagedBean(name=OTGroupSubmitMB.MANAGED_BEAN_NAME, eager=false)
@ViewScoped
public class OTGroupSubmitMB extends OTGroupStatusChangeAbstractMB {
	
	// CLASS MEMBERS
	private static final long serialVersionUID = -4366734721744927697L;
	public static final String MANAGED_BEAN_NAME = "otGroupSubmitMB";
	public static final String GROUP_SESSION_KEY = MANAGED_BEAN_NAME + "_" + serialVersionUID + "_OTGroup"; 
	
	// EJB INJECTION
	@EJB 
	protected UserEJB userEJB;	
	
	@EJB
	protected EmployeeEJB employeeEJB;
	
	
	//INSTANCE MEMBERS
	private String selectedReceiver;
	private List<AlohaUser> dbReceivers = new ArrayList<AlohaUser>();
	private Map<String, Object> receivers = new TreeMap<String, Object>();	
	private String groupSubmitterRemarks;
	
	@PostConstruct
	public void init() {
		super.init();
		try {
			this.initReceivers();
		} catch (AlohaServerException e) {
			try {
				FacesContext.getCurrentInstance().getExternalContext().redirect(AlohaURIs.SERVER_ERROR);
			} catch (IOException ignore) {}	
		}
	}
	// METHOD REQUIRED BY ABSTRACT SUPERCLASS
	protected void checkAccessRights() throws AuthorizationException {
		// CHECK ROLE
		if ( !this.userMB.isApprover() ) {
			Object[] params = { this.userMB.getFullName() };
			String errMsg = ErrorMessages.getInstance().getMessage(ErrorMessages.OT_GROUP_SUBMIT_UNAUTHORIZED, params);
			FacesContextUtil.getHttpSession().setAttribute(AlohaConstants.UNAUTHORIZED_MESSAGE, errMsg);
			throw new AuthorizationException(errMsg);
		}
		// CHECK SUPERVISOR
		if ( this.getOtGroup().getSubmitter().getUserId() != this.userMB.getUser().getUserId()) {
			Object[] params = { this.userMB.getFullName(), this.getOtGroup().getId() };
			String errMsg = ErrorMessages.getInstance().getMessage(ErrorMessages.OT_GROUP_SUBMIT_UNAUTHORIZED_SPECIFIC, params);
			FacesContextUtil.getHttpSession().setAttribute(AlohaConstants.UNAUTHORIZED_MESSAGE, errMsg);
			throw new AuthorizationException(errMsg);
		}
	}
	// METHOD REQUIRED BY ABSTRACT SUPERCLASS	
	protected void checkIfLegalOperation() throws IllegalOperationException {
		if 	( ! this.getOtGroup().isSubmitable() ) {
			Object[] params = { this.getOtGroup().getId(), this.getOtGroup().getStatus().getName() };
			String errMsg = ErrorMessages.getInstance().getMessage(ErrorMessages.OT_GROUP_SUBMIT_INVALID_STATUS, params);
			FacesContextUtil.getHttpSession().setAttribute(AlohaConstants.ILLEGAL_OPERATION_MSG, errMsg);
			throw new IllegalOperationException(errMsg);
		}	
	}
	// METHOD REQUIRED BY ABSTRACT SUPERCLASS
	protected String getNewStatusCode() {
		return OTGroupStatus.CodeValues.SUBMITTED;
	}
	// METHOD REQUIRED BY ABSTRACT SUPERCLASS
	protected AlohaUser getSubmitter() {
		return this.userMB.getUser();
	}
	// METHOD REQUIRED BY ABSTRACT SUPERCLASS
	protected AlohaUser getReceiver() {
		if (!StringUtil.isNullOrEmpty(this.selectedReceiver)){
			return this.findReceiver(Long.valueOf(this.selectedReceiver));	
		} else {
			return null;
		}
	}
	// METHOD REQUIRED BY ABSTRACT SUPERCLASS
	protected String getSubmitterRemarks() {
		return this.groupSubmitterRemarks;
	}
	// METHOD REQUIRED BY ABSTRACT SUPERCLASS
	protected String getReceiverRemarks() {
		return null;
	}
	// METHOD REQUIRED BY ABSTRACT SUPERCLASS
	protected String getSuccessPage() {
		return NavigationOutcomes.OT_MGR_LIST;
	}
	// METHOD REQUIRED BY ABSTRACT SUPERCLASS
	protected String getStatusChangeOutcomeKey() {
		return AlohaConstants.OT_STATUS_CHANGE_OUTCOME_SUPV;
	}
	// METHOD REQUIRED BY ABSTRACT SUPERCLASS
	protected void processEmployees() {
		// NO-OP
	}
	// METHOD REQUIRED BY ABSTRACT SUPERCLASS
	protected void processChildGroups() {
		// NO-OP
	}
	// METHOD REQUIRED BY ABSTRACT SUPERCLASS
	protected String createConfirmationMessage() {
		Object[] params = {Long.valueOf(this.getOtGroup().getId()), this.getOtGroup().getPayPeriod().getShortLabel(), this.getOtGroup().getReceiverName()};
		return (NormalMessages.getInstance().getMessage(NormalMessages.OT_GROUP_MSG_STATUS_CHANGE_SUBMIITED, params));
	}	
	// METHOD REQUIRED BY ABSTRACT SUPERCLASS
	protected void doValidation() throws ValidatorException {
		if (this.getReceiver() == null) {
			String errMsgText = ErrorMessages.getInstance().getMessage(ErrorMessages.OT_GROUP_SUBMIT_RECEIVER_REQUIRED);
			FacesMessage facesMsg = new FacesMessage(errMsgText, errMsgText);
			facesMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
			FacesContext.getCurrentInstance().addMessage(null, facesMsg);	
			throw new ValidatorException(facesMsg);
		}
	}
	// METHOD REQUIRED BY ABSTRACT SUPERCLASS
	protected String getGroupSessionKey() {
		return GROUP_SESSION_KEY;
	}		
	
	public String getSelectedReceiver() {
		return selectedReceiver;
	}
	public void setSelectedReceiver(String selectedReceiver) {
		this.selectedReceiver = selectedReceiver;
	}	
	public Map<String, Object> getReceivers() {
		return receivers;
	}
	private void initReceivers() throws AlohaServerException {
		// POPULATE REVEIVER DROPDOWN
		this.dbReceivers = this.employeeEJB.getLeaveApprovers(this.userMB.getUserId());
		for (AlohaUser uiReceiver : this.dbReceivers) {
			this.receivers.put(uiReceiver.getLabel(), uiReceiver.getValue());
		}
		
		// SELECTED REVEIVER
		AlohaUserPref userPrefs = this.userEJB.getUserPref(this.userMB.getUserId());
		if ( (userPrefs != null) 
				&& (userPrefs.getDefaultApproverUserId() != null) ) {
			this.selectedReceiver = String.valueOf(userPrefs.getDefaultApproverUserId());
		}
	}	
	private AlohaUser findReceiver(long userId) {
		for ( AlohaUser receiver: this.dbReceivers) {
			if ( receiver.getUserId() == userId) {
				return receiver;
			}
		}
		return null;
	}
	
	public String getGroupSubmitterRemarks() {
		return groupSubmitterRemarks;
	}
	public void setGroupSubmitterRemarks(String groupSubmitterRemarks) {
		this.groupSubmitterRemarks = groupSubmitterRemarks;
	}
	// EVENT
	public String onGroupSubmit() {
		try {
			this.doValidation();
			return this.onStatusChange();			
		} catch (ValidatorException ve) {
			return null;
		}
	}	
}
