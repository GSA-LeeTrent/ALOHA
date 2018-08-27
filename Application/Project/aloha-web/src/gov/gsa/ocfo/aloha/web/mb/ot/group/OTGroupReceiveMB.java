package gov.gsa.ocfo.aloha.web.mb.ot.group;

import gov.gsa.ocfo.aloha.exception.AlohaServerException;
import gov.gsa.ocfo.aloha.exception.AuthorizationException;
import gov.gsa.ocfo.aloha.exception.IllegalOperationException;
import gov.gsa.ocfo.aloha.model.entity.AlohaUser;
import gov.gsa.ocfo.aloha.model.entity.overtime.OTGroup;
import gov.gsa.ocfo.aloha.model.entity.overtime.OTGroupStatus;
import gov.gsa.ocfo.aloha.util.StopWatch;
import gov.gsa.ocfo.aloha.web.model.overtime.OTStatusChangeOutcome;
import gov.gsa.ocfo.aloha.web.security.NavigationOutcomes;
import gov.gsa.ocfo.aloha.web.util.AlohaConstants;
import gov.gsa.ocfo.aloha.web.util.ErrorMessages;
import gov.gsa.ocfo.aloha.web.util.FacesContextUtil;
import gov.gsa.ocfo.aloha.web.util.NormalMessages;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;

@ManagedBean(name=OTGroupReceiveMB.MANAGED_BEAN_NAME, eager=false)
@ViewScoped
public class OTGroupReceiveMB extends OTGroupStatusChangeAbstractMB {
	
	// CLASS MEMBERS
	private static final long serialVersionUID = -4366734721744927697L;
	public static final String MANAGED_BEAN_NAME = "otGroupReceiveMB";
	public static final String GROUP_SESSION_KEY = MANAGED_BEAN_NAME + "_" + serialVersionUID + "_OTGroup"; 
	
	// JSF: MANAGED BEAN INJECTION
	@ManagedProperty(value="#{otGroupCreateMB}")
	private OTGroupCreateMB otGroupCreateMB;
	public void setOtGroupCreateMB(OTGroupCreateMB otGroupCreateMB) {
		this.otGroupCreateMB = otGroupCreateMB;
	}
	//INSTANCE MEMBERS
	private String groupReceiverRemarks;
	
	@PostConstruct
	public void init() {
		super.init();
	}

	// METHOD REQUIRED BY ABSTRACT SUPERCLASS
	protected void checkAccessRights() throws AuthorizationException {
		// CHECK ROLE
		if ( !this.userMB.isApprover() ) {
			Object[] params = { this.userMB.getFullName() };
			String errMsg = ErrorMessages.getInstance().getMessage(ErrorMessages.OT_GROUP_RECEIVE_UNAUTHORIZED, params);
			FacesContextUtil.getHttpSession().setAttribute(AlohaConstants.UNAUTHORIZED_MESSAGE, errMsg);
			throw new AuthorizationException(errMsg);
		}
		// CHECK SUPERVISOR
		if ( this.getOtGroup().getReceiver().getUserId() != this.userMB.getUser().getUserId()) {
			Object[] params = { this.userMB.getFullName(), this.getOtGroup().getId() };
			String errMsg = ErrorMessages.getInstance().getMessage(ErrorMessages.OT_GROUP_RECEIVE_UNAUTHORIZED_SPECIFIC, params);
			FacesContextUtil.getHttpSession().setAttribute(AlohaConstants.UNAUTHORIZED_MESSAGE, errMsg);
			throw new AuthorizationException(errMsg);
		}
	}
	// METHOD REQUIRED BY ABSTRACT SUPERCLASS	
	protected void checkIfLegalOperation() throws IllegalOperationException {
		if 	( ! this.getOtGroup().isReceivable() ) {
			Object[] params = { this.getOtGroup().getId(), this.getOtGroup().getStatus().getName() };
			String errMsg = ErrorMessages.getInstance().getMessage(ErrorMessages.OT_GROUP_RECEIVE_INVALID_STATUS, params);
			FacesContextUtil.getHttpSession().setAttribute(AlohaConstants.ILLEGAL_OPERATION_MSG, errMsg);
			throw new IllegalOperationException(errMsg);
		}	
	}
	// METHOD REQUIRED BY ABSTRACT SUPERCLASS
	protected String getNewStatusCode() {
		return OTGroupStatus.CodeValues.RECEIVED;
	}
	// METHOD REQUIRED BY ABSTRACT SUPERCLASS
	protected AlohaUser getSubmitter() {
		return this.getOtGroup().getSubmitter();
	}
	// METHOD REQUIRED BY ABSTRACT SUPERCLASS
	protected AlohaUser getReceiver() {
		return this.getOtGroup().getReceiver();
	}
	// METHOD REQUIRED BY ABSTRACT SUPERCLASS
	protected String getSubmitterRemarks() {
		return null;
	}
	// METHOD REQUIRED BY ABSTRACT SUPERCLASS
	protected String getReceiverRemarks() {
		return this.groupReceiverRemarks;
	}
	
	// METHOD REQUIRED BY ABSTRACT SUPERCLASS
	protected String createConfirmationMessage() {
		return null; // NO-OP (HANDLED BY buildConfirmationMessage() INSISE onGroupReceive() METHOD
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
		//System.out.println(this.getClass().getName() + ".processEmployees(): NO-OP");
	}
	// METHOD REQUIRED BY ABSTRACT SUPERCLASS
	protected void processChildGroups() {
		// NO-OP
		//System.out.println(this.getClass().getName() + ".processChildGroups(): NO-OP");
	}
	// METHOD REQUIRED BY ABSTRACT SUPERCLASS
	protected String getGroupSessionKey() {
		return GROUP_SESSION_KEY;
	}		
	// EVENT
	public String onGroupReceive() {
		try {
			
			// HANDLE STATUS CHANGE (to 'RECEIVED'), HISTORY AND RECEIVER REMARKS FOR THIS GROUP
			this.processAndPersistStatusChange();
			
			// NOW CONSOLIDATE THIS GROUP INTO PARENT (RECEIVING) GROUP:

			// CHECK TO SEE IF THE RECEIVING GROUP ALREADY EXISTS
			///////////////////////////////////////////////////////////////////////////////////////////////////////////
			// IMPORTANT:		
			// WE'RE MAKING A CALL TO 'groupOvertimeEJB.retrieveOTGroupForSubmitter()'. HOWEVER, WE NEED TO PASS IN  
			// 'otGroup.getReceiver()' BECAUSE THE RECEIVER OF 'THIS' GROUP WILL BE THE SUBMITTER OF THE GROUP THAT 'THIS'
			// GROUP IS CONSOLIDATED INTO.
			///////////////////////////////////////////////////////////////////////////////////////////////////////////
			OTGroup receivingGroup = this.groupOvertimeEJB.retrieveOTGroupForSubmitter(this.getOtGroup().getReceiver(), 
					this.getOtGroup().getPayPeriod(), this.otUtilMB.getOTGroupStatus(OTGroupStatus.CodeValues.PENDING));
						
			// IF RECEIVING GROUP DOES NOT EXIST, CREATE IT
			if (receivingGroup == null) {
				receivingGroup = this.otGroupCreateMB.createAndPersistOTGroup(this.getOtGroup().getPayPeriod(), this.userMB.getUser(), this.userMB.getUser());
			}
			
			// NOW WIRE UP THIS OT_GROUP WITH THE RECEIVING OT_GROUP AND SAVE IT TO THE DATABASE
			receivingGroup.addChildGroup(this.getOtGroup());
			this.getOtGroup().setParent(receivingGroup);
			
			StopWatch stopWatch = new StopWatch();
			stopWatch.start();
			this.groupOvertimeEJB.updateOTGroup(receivingGroup);
			stopWatch.stop();
			System.out.println("ELAPSED TIME (Receive Overtime Group Request): " + stopWatch.getElapsedTime() + " ms");
			stopWatch = null;		

			// BUILD CONFIRMATION MESSAGE
			String confirmMsg = this.buildConfirmationMessage(receivingGroup, this.getOtGroup());			
			FacesContextUtil.getHttpSession().setAttribute(this.getStatusChangeOutcomeKey(), new OTStatusChangeOutcome(this.getNewStatusCode(), confirmMsg));
			
			// NAVIGATE TO SUCCESS PAGE
			return this.getSuccessPage();
		} catch (AlohaServerException e) {
			return NavigationOutcomes.SERVER_ERROR;
		} catch (Exception e) {
			e.printStackTrace();
			return NavigationOutcomes.SERVER_ERROR;
		} finally {
			FacesContextUtil.getHttpSession().removeAttribute(this.getGroupSessionKey());
		}
	}	

	// HELPER METHOD
	private String buildConfirmationMessage(OTGroup receivingGroup, OTGroup consolidatedGroup) {		
		Object[] params = {Long.valueOf(consolidatedGroup.getId()), 
				consolidatedGroup.getPayPeriod().getShortLabel(), this.getNewStatusCode(), Long.valueOf(receivingGroup.getId())};
		return (NormalMessages.getInstance().getMessage(NormalMessages.MSG_OT_GROUP_RECEIVED_AND_CONSOLIDATED, params));
	}	
	// GETTER
	public String getGroupReceiverRemarks() {
		return groupReceiverRemarks;
	}
	//SETTER
	public void setGroupReceiverRemarks(String groupReceiverRemarks) {
		this.groupReceiverRemarks = groupReceiverRemarks;
	}	
}
