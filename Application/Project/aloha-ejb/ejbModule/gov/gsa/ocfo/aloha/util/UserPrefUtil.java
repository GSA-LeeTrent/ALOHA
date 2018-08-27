package gov.gsa.ocfo.aloha.util;

import gov.gsa.ocfo.aloha.model.entity.AlohaUserPref;

public class UserPrefUtil {
	
	public static boolean defaultApproverHasChanged(AlohaUserPref oldUserPref, AlohaUserPref newUserPref) {
		
		////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// CHECK FOR CHANGE IN DEFAULT APPROVER
		////////////////////////////////////////////////////////////////////////////////////////////////////////////////////		
		
		if ( newUserPref.getDefaultApproverUserId() != null 
				&& oldUserPref.getDefaultApproverUserId() != null 
				&& newUserPref.getDefaultApproverUserId().longValue() != oldUserPref.getDefaultApproverUserId().longValue() ) {
			return true;
		}
		
		if (newUserPref.getDefaultApproverUserId() != null && oldUserPref.getDefaultApproverUserId() == null ) {
			return true;
		}
		
		if (newUserPref.getDefaultApproverUserId() == null && oldUserPref.getDefaultApproverUserId() != null ) {
			return true;
		}
		
		return false;
	}	

	
	public static boolean defaultTimekeeperHasChanged(AlohaUserPref oldUserPref, AlohaUserPref newUserPref) {
		
		////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// CHECK FOR CHANGE IN PRIMARY TIMEKEEPER
		////////////////////////////////////////////////////////////////////////////////////////////////////////////////////			
		if ( newUserPref.getDefaultTimekeeperUserId() != null 
				&& oldUserPref.getDefaultTimekeeperUserId() != null 
				&& newUserPref.getDefaultTimekeeperUserId().longValue() != oldUserPref.getDefaultTimekeeperUserId().longValue() ) {
			return true;
		}
		
		if (newUserPref.getDefaultTimekeeperUserId() != null && oldUserPref.getDefaultTimekeeperUserId() == null ) {
			return true;
		}
		
		if (newUserPref.getDefaultTimekeeperUserId() == null && oldUserPref.getDefaultTimekeeperUserId() != null ) {
			return true;
		}		
		
		return false;
	}		
}
