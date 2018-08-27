package gov.gsa.ocfo.aloha.util.ot;

import gov.gsa.ocfo.aloha.model.entity.overtime.OTGroupStatus;

public class OTGroupStatusUtil {

	public static boolean isCancelable(String statusCode) {
		return	( OTGroupStatus.CodeValues.PENDING.equals(statusCode)
				|| OTGroupStatus.CodeValues.SUBMITTED.equals(statusCode)
				|| OTGroupStatus.CodeValues.FINAL.equals(statusCode)
			);
	}	
	public static boolean isSubmitable(String statusCode) {
		return ( OTGroupStatus.CodeValues.PENDING.equals(statusCode));
	}
	public static boolean isReviewable(String statusCode) {
		return ( OTGroupStatus.CodeValues.SUBMITTED.equals(statusCode));
	}
	public static boolean isApprovable(String statusCode) {
		return 	( (OTGroupStatus.CodeValues.SUBMITTED.equals(statusCode))
					|| (OTGroupStatus.CodeValues.RECEIVED.equals(statusCode))
				);
	}			
	public static boolean isReceivable(String statusCode) {
		return ( OTGroupStatus.CodeValues.SUBMITTED.equals(statusCode));
	}	
	public static boolean isModifiable(String statusCode) {
		return	( OTGroupStatus.CodeValues.PENDING.equals(statusCode)
					|| OTGroupStatus.CodeValues.RECEIVED.equals(statusCode)
				);
	}
	public static boolean isFinalizable(String statusCode) {
		return	( OTGroupStatus.CodeValues.RECEIVED.equals(statusCode)
					|| OTGroupStatus.CodeValues.SUBMITTED.equals(statusCode)
					|| OTGroupStatus.CodeValues.PENDING.equals(statusCode)
				);
	}	
}