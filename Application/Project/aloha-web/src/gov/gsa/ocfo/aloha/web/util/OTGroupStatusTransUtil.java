package gov.gsa.ocfo.aloha.web.util;

import gov.gsa.ocfo.aloha.model.entity.overtime.OTGroupStatus;
import gov.gsa.ocfo.aloha.model.entity.overtime.OTGroupStatusTrans;

public class OTGroupStatusTransUtil {
	
	public static String determineOTGroupStatusTransCode(String oldStatusCode, String newStatusCode) {
		if ( oldStatusCode.equals(OTGroupStatus.CodeValues.NONE)) {
			if ( newStatusCode.equals(OTGroupStatus.CodeValues.PENDING)) {
				return OTGroupStatusTrans.ActionCodeValues.NONE_TO_PENDING;
			} 
		} else if ( oldStatusCode.equals(OTGroupStatus.CodeValues.PENDING)) {
			if ( newStatusCode.equals(OTGroupStatus.CodeValues.SUBMITTED)) {
				return OTGroupStatusTrans.ActionCodeValues.PENDING_TO_SUBMITTED;
			} else if (newStatusCode.equals(OTGroupStatus.CodeValues.CANCELLED)) {
				return OTGroupStatusTrans.ActionCodeValues.PENDING_TO_CANCELLED;
			} else if (newStatusCode.equals(OTGroupStatus.CodeValues.FINAL)) {
				return OTGroupStatusTrans.ActionCodeValues.PENDING_TO_FINAL;
			}
		} else if ( oldStatusCode.equals(OTGroupStatus.CodeValues.RECEIVED)) {
			if ( newStatusCode.equals(OTGroupStatus.CodeValues.CANCELLED)) {
				return OTGroupStatusTrans.ActionCodeValues.RECEIVED_TO_CANCELLED;
			}  else if ( newStatusCode.equals(OTGroupStatus.CodeValues.FINAL)) {
				return OTGroupStatusTrans.ActionCodeValues.RECEIVED_TO_FINAL;
			}
		} else if ( oldStatusCode.equals(OTGroupStatus.CodeValues.SUBMITTED)) {
			if ( newStatusCode.equals(OTGroupStatus.CodeValues.RECEIVED)) {
				return OTGroupStatusTrans.ActionCodeValues.SUBMITTED_TO_RECEIVED;
			} else if ( newStatusCode.equals(OTGroupStatus.CodeValues.CANCELLED)) {
				return OTGroupStatusTrans.ActionCodeValues.SUBMITTED_TO_CANCELLED;
			} else if ( newStatusCode.equals(OTGroupStatus.CodeValues.FINAL)) {
				return OTGroupStatusTrans.ActionCodeValues.SUBMITTED_TO_FINAL;
			}
		} else if ( oldStatusCode.equals(OTGroupStatus.CodeValues.FINAL)) {
			if ( newStatusCode.equals(OTGroupStatus.CodeValues.CANCELLED)) {
				return OTGroupStatusTrans.ActionCodeValues.FINAL_TO_CANCELLED;
			}			
		}
		return null;
	}
}
