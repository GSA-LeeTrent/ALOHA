package gov.gsa.ocfo.aloha.web.util;

public interface AlohaConstants {
	public static final String LR_MODE = "LR_MODE";
	public static final String OT_MODE = "OT_MODE";
	public static final String LEAVE_DETAIL_ID = "leaveDetailId";
	public static final String UNAUTHORIZED_MESSAGE = "unauthMsg";
	public static final String ILLEGAL_OPERATION_MSG = "illegalOperationMsg";
	public static final String STATUS_PENDING = "Pending";
	public static final String ABRV_TO_BE_DETERMINED = "TBD";
	public static final String NEWLY_CREATED_LEAVE_REQUEST = "newlyCreatedLeaveRequest";
	public static final String NEWLY_CANCELLED_LEAVE_DETAIL = "newlyCancelledLeaveDetail";
	public static final String NEWLY_WITHDRAWN_LEAVE_DETAIL = "newlyWithdrawnLeaveDetail";
	public static final String NEWLY_AMENDED_LEAVE_DETAIL = "newlyAmendedLeaveDetail";	
	public static final String NEWLY_REVIEWED_LEAVE_REQUEST = "newlyReviewedLeaveRequest";
	
	public static final String LR_MODE_ON_LAST_CREATE = "lrModeOnLastCreate";
	public static final String LR_MODE_ON_LAST_AMEND = "lrModeOnLastAmend";
	public static final String LR_MODE_ON_LAST_CANCEL = "lrModeOnLastCancel";	
	public static final String LR_MODE_ON_LAST_WITHDRAW = "lrModeOnLastWithdraw";	
	public static final String LR_MODE_ON_LAST_REVIEW = "lrModeOnLastReview";
	
	
	public static final String OT_DETAIL_ID = "otDetailId";
	public static final String OT_GROUP_ID = "otGroupId";
	
	//public static final String OT_MSG_SUPV_CONFIRM = "otMsgSupvConfirm";
	
	//public static final String OT_SUPV_REVIEW_OUTCOME = "otSupvReviewOutcome";
	public static final String OT_STATUS_CHANGE_OUTCOME_SO = "otStatusChangeOutcomeSO";
	public static final String OT_STATUS_CHANGE_OUTCOME_OBO = "otStatusChangeOutcomeOBO";
	public static final String OT_STATUS_CHANGE_OUTCOME_SUPV = "otStatusChangeOutcomeSupv";

	
	/*
	public static final String OT_MSG_CONFIRM_RECEIVE = "otMsgConfirmReceive";
	public static final String OT_MSG_CONFIRM_DENY = "otMsgConfirmDeny";
	public static final String OT_MSG_CONFIRM_APPROVE = "otMsgConfirmApprove";	
	public static final String OT_MSG_CONFIRM_RETURN = "otMsgConfirmReturn";	
	*/
	
	public static final String OT_MSG_CONFIRM_CREATE_OBO = "otMsgConfirmCreateOBO";
	public static final String OT_MSG_CONFIRM_CREATE_SO = "otMsgConfirmCreateSO";	
	public static final String OT_MSG_CONFIRM_CREATE  = "otMsgConfirmCreate";
	
	public static final int TASK_LIST_UPPER_BOUND = 25;
	
	public static final String lastAlohaHomeTabVisited = "lastAlohaHomeTabVisited";
	
	public static final String OT_POLICY_ACKNOWLEDGED = "otPolicyAcknowledged";
	
	public static final String LR_VARIANCE_ACKNOWLEDGED = "lrVarianceAcknowledged";
}
