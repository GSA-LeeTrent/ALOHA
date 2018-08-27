package gov.gsa.ocfo.aloha.web.util;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;

public class NormalMessages {

	public static final String PENDING_LEAVE_APPROVAL = "leaveRequest.approver.pending.leaveStatus.approved";
	public static final String PENDING_LEAVE_DENIAL = "leaveRequest.approver.pending.leaveStatus.denied";
	public static final String CONTACT_US_MSG_SENT_OK = "contactUs.confirmation";
	
	public static final String PREVIOUS_REMARKS = "application.text.previousRemarks";
	public static final String ADXREF_ADD_OK = "adXref.addOk";
	public static final String ADXREF_DELETE_OK = "adXref.deleteOk";
	public static final String ADXREF_UPDATE_OK = "adXref.updateOk";
	
	public static final String OT_CREATE_CONFIRM_MSG_SO = "ot.create.msg.confirm.submitOwn";
	public static final String OT_CREATE_CONFIRM_MSG_OBO = "ot.create.msg.confirm.onBehalfOf";
	
	public static final String OT_REVIEW_OUTCOME_MSG = "ot.review.outcome.msg";
	public static final String OT_MSG_STATUS_CHANGE = "ot.msg.statusChange";
	public static final String OT_MSG_STATUS_CHANGE_CONSOLIDATED = "ot.msg.statusChange.consolidated";	
	
	public static final String OT_GROUP_MSG_STATUS_CHANGE = "ot.group.msg.statusChange";
	public static final String APPLICATION_SECTION_TITLE_OT_GROUP_TOP_PANEL = "application.sectionTitle.ot.group.topPanel";
	
	public static final String OT_GROUP_MSG_STATUS_CHANGE_SUBMIITED = "ot.group.msg.statusChange.submitted";
	public static final String OT_GROUP_MSG_STATUS_CHANGE_CANCELLED = "ot.group.msg.statusChange.cancelled";
	public static final String OT_GROUP_MSG_STATUS_CHANGE_FINALIZED = "ot.group.msg.statusChange.finalized";
	
	public static final String MSG_OT_GROUP_RECEIVED_AND_CONSOLIDATED = "msg.ot.group.received.and.consolidated";
	
	public static final String LR_NUMBER= "lr.number";
	
	public static final String LR_CHANGE_SUPV_CONFIRM_MSG = "lr.changeSupervisor.message.confirmation";
	
	public static final String LR_AMEND_SECTION_TITLE = "leaveRequest.amend.sectionTitle";
	
	
	/* The volatile keyword ensures that multiple threads 
	 * handle the unique instance variable correctly 
	 * when it is being initialized to the Singleton instance
	 ***********************************************************/ 
	private volatile static NormalMessages uniqueInstance;
	/**********************************************************/

	private static final String BASE_NAME = "gov.gsa.ocfo.aloha.web.messages";
	
	public static NormalMessages getInstance() {
		// Check for an instance and if there isn't one, enter a synchronized block
		if ( uniqueInstance == null ) { 
			// We only synchronize the first time through
			synchronized(ErrorMessages.class) { 
				// Once in the synchronized block, check again and if the instance is still null, 
				// create an instance of this Singleton
				if ( uniqueInstance == null ) { 
					uniqueInstance = new NormalMessages();		
				}
			}
		}
		return uniqueInstance;
	}	
	private ResourceBundle bundle;
	
	private NormalMessages() {
		FacesContext context = FacesContext.getCurrentInstance();
		UIViewRoot viewRoot = context.getViewRoot();
		Locale locale = viewRoot.getLocale();
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		this.bundle = ResourceBundle.getBundle(BASE_NAME, locale, loader);
	}
	
	public String getMessage(String key) {
		return this.bundle.getString(key);
	}
	
	public String getMessage(String key, Object[] params) {
		try {
			String resource = this.bundle.getString(key);
			if ( !StringUtil.isNullOrEmpty(resource)) {
				MessageFormat mf = new MessageFormat(resource);
				return mf.format(params);
			} else {
				return null;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
