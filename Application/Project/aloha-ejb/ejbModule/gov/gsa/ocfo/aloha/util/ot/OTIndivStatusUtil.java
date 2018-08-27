package gov.gsa.ocfo.aloha.util.ot;

import gov.gsa.ocfo.aloha.model.entity.overtime.OTIndivStatus;

public class OTIndivStatusUtil {
	public static boolean isCancellableBySubmitOwn(String statusCode) {
		return	(OTIndivStatusUtil.isCancellableByNonSupervisor(statusCode));
	}	
	
	public static boolean isCancellableByOnBehalfOf(String statusCode)  {
		return	(OTIndivStatusUtil.isCancellableByNonSupervisor(statusCode));
	}
	
	public static boolean isCancellableByNonSupervisor(String statusCode)  {
		return ( (OTIndivStatus.CodeValues.SUBMITTED.equals(statusCode))
					|| (OTIndivStatus.CodeValues.RESUBMITTED.equals(statusCode))
					|| (OTIndivStatus.CodeValues.RECEIVED.equals(statusCode))
					|| (OTIndivStatus.CodeValues.RECEIVED_WITH_MODS.equals(statusCode))
					|| (OTIndivStatus.CodeValues.MODIFIED.equals(statusCode))
					|| (OTIndivStatus.CodeValues.APPROVED.equals(statusCode))
				);
	}
	public static boolean isCancellableBySupervisor(String statusCode) {
		return	( (OTIndivStatus.CodeValues.RECEIVED.equals(statusCode))
					|| (OTIndivStatus.CodeValues.RECEIVED_WITH_MODS.equals(statusCode))
					|| (OTIndivStatus.CodeValues.MODIFIED.equals(statusCode))
					|| (OTIndivStatus.CodeValues.APPROVED.equals(statusCode))
				);
	}	
	public static boolean isReviewableBySupervisor(String statusCode) {
		return ( (OTIndivStatus.CodeValues.SUBMITTED.equals(statusCode)) 
							|| (OTIndivStatus.CodeValues.RESUBMITTED.equals(statusCode))
							|| (OTIndivStatus.CodeValues.MODIFIED.equals(statusCode))
				); 
	}
	public static boolean isDeniableBySupervisor(String statusCode) {
		return 	( (OTIndivStatus.CodeValues.SUBMITTED.equals(statusCode))
					|| (OTIndivStatus.CodeValues.RECEIVED.equals(statusCode))
					|| (OTIndivStatus.CodeValues.RESUBMITTED.equals(statusCode))
					|| (OTIndivStatus.CodeValues.RECEIVED_WITH_MODS.equals(statusCode))
					|| (OTIndivStatus.CodeValues.MODIFIED.equals(statusCode))
				);
	}	
	public static boolean isApprovableBySupervisor(String statusCode) {
		return 	( (OTIndivStatus.CodeValues.SUBMITTED.equals(statusCode))
					|| (OTIndivStatus.CodeValues.RESUBMITTED.equals(statusCode))
					|| (OTIndivStatus.CodeValues.RECEIVED.equals(statusCode))
					|| (OTIndivStatus.CodeValues.RECEIVED_WITH_MODS.equals(statusCode))
					|| (OTIndivStatus.CodeValues.MODIFIED.equals(statusCode))
				);
	}	
	public static boolean isModifiableBySupervisor(String statusCode) {
		return 	( (OTIndivStatus.CodeValues.SUBMITTED.equals(statusCode))
				|| (OTIndivStatus.CodeValues.RESUBMITTED.equals(statusCode))
				|| (OTIndivStatus.CodeValues.RECEIVED.equals(statusCode))
				|| (OTIndivStatus.CodeValues.RECEIVED_WITH_MODS.equals(statusCode))
				|| (OTIndivStatus.CodeValues.MODIFIED.equals(statusCode))
			);
	}
	public static boolean isModifiableByNonSupervisor(String statusCode) {
		return 	( (OTIndivStatus.CodeValues.SUBMITTED.equals(statusCode))
					|| (OTIndivStatus.CodeValues.RESUBMITTED.equals(statusCode))
				);
	}	
	public static boolean isModifiableBySubmitOwn(String statusCode) {
		return	(OTIndivStatusUtil.isModifiableByNonSupervisor(statusCode));
	}	
	
	public static boolean isModifiableByOnBehalfOf(String statusCode)  {
		return	(OTIndivStatusUtil.isModifiableByNonSupervisor(statusCode));
	}
		
	public static boolean isReceivableBySupervisor(String statusCode) {
		return 	( (OTIndivStatus.CodeValues.SUBMITTED.equals(statusCode))
				|| (OTIndivStatus.CodeValues.RESUBMITTED.equals(statusCode))
			);
	}	
}