package gov.gsa.ocfo.aloha.web.util;

import gov.gsa.ocfo.aloha.model.entity.AlohaUser;
import gov.gsa.ocfo.aloha.model.entity.overtime.OTDetail;
import gov.gsa.ocfo.aloha.model.entity.overtime.OTGroup;
import gov.gsa.ocfo.aloha.model.entity.overtime.OTHeader;

import java.util.List;

public class OTGroupStatusChangeHelper {
	// DETERMINE WHICH INDIVIDUAL OT REQUESTS IN GROUP LIST ARE MODIFABLE
	public static void determineModifiablityOfAllIndividualRequestsInGroupList(AlohaUser manager, List<OTGroup> otGroupList) {
		for ( OTGroup otGroup : otGroupList) {
			for (OTHeader otHeader : otGroup.getAllEmployees()) {
				otHeader.setModifiableBySupervisor(determineModifiablityOfSingleIndividualRequest(manager, otHeader));
			}
		}
	}

	// DETERMINE WHICH INDIVIDUAL OT REQUESTS IN GROUP LIST ARE CANCELLABLE
	public static void determineCancelablityOfAllIndividualRequestsInGroupList(AlohaUser manager, List<OTGroup> otGroupList) {
		for ( OTGroup otGroup : otGroupList) {
			for (OTHeader otHeader : otGroup.getAllEmployees()) {
				otHeader.setCancellableBySupervisor(determineCancelablityOfSingleIndividualRequest(manager, otHeader));
			}
		}
	}	

	// DETERMINE WHICH GROUP REQUESTS IN LIST ARE CANCELLABLE
	public static void determineCancellablityOfAllGroupsInList(AlohaUser manager, List<OTGroup> otGroupList) {
		for ( OTGroup otGroup : otGroupList) {
			otGroup.setCancellable(determineCancelablityOfSingleGroupRequest(manager, otGroup));
		}
	}	
	
	// DETERMINE WHICH GROUP OT REQUESTS ARE FINALIZABLE
	public static void determineFinalizablityOfAllGroupsInList(AlohaUser manager, List<OTGroup> otGroupList) {
		for ( OTGroup otGroup : otGroupList) {
			otGroup.setFinalizable(determineFinalizablityOfSingleGroupRequest(manager, otGroup));
		}
	}

	// DETERMINE IF GROUP OT REQUEST IS CANCELABLE
	public static boolean determineCancelablityOfSingleGroupRequest(AlohaUser manager, OTGroup otGroup) {
		if ( otGroup.isFinalized() ) {
			return true;
		}
		if ( otGroup.isCancelled() ) {
			return false;
		}
		if ( otGroup.isSubmitted()) {
			if ( otGroup.getSubmitter().getUserId() == manager.getUserId() ) {
				return true;
			}
		}
		if ( otGroup.isPending()) {
			if ( otGroup.getSubmitter().getUserId() == manager.getUserId() ) {
				return true;
			}
		}
		if ( otGroup.isReceived() ) {			
			OTGroup topLevelGroup = otGroup.getTopLevelParent();

			if ( topLevelGroup.isPending()) {
				if ( topLevelGroup.getSubmitter().getUserId() == manager.getUserId() ) {
					return true;
				}
			}
			if ( topLevelGroup.isSubmitted() ) {
				if ( topLevelGroup.getSubmitter().getUserId() == manager.getUserId() ) {
					return true;
				}
			}		
			if ( topLevelGroup.isReceived() ) {
				if ( topLevelGroup.getReceiver().getUserId() == manager.getUserId() ) {
					return true;
				}
			}						
		}
		return false;
	}	
	// DETERMINE IF A SINGLE GROUP IS FINALIZABLE
	public static boolean determineFinalizablityOfSingleGroupRequest(AlohaUser manager, OTGroup otGroup) {
		if ( otGroup.isFinalized() ) {
			return false;
		}
		if ( otGroup.isCancelled() ) {
			return false;
		}
		if ( otGroup.isSubmitted()) {
			if ( otGroup.getReceiver().getUserId() == manager.getUserId() ) {
				return true;
			}
		}
		if ( otGroup.isPending()) {
			if ( otGroup.getSubmitter().getUserId() == manager.getUserId() ) {
				return true;
			}
		}
		if ( otGroup.isReceived() ) {			
			OTGroup topLevelGroup = otGroup.getTopLevelParent();

			if ( topLevelGroup.isPending()) {
				if ( topLevelGroup.getSubmitter().getUserId() == manager.getUserId() ) {
					return true;
				}
			}
			if ( topLevelGroup.isSubmitted() ) {
				if ( topLevelGroup.getSubmitter().getUserId() == manager.getUserId() ) {
					return true;
				}
			}		
			if ( topLevelGroup.isReceived() ) {
				if ( topLevelGroup.getReceiver().getUserId() == manager.getUserId() ) {
					return true;
				}
			}						
		}
		return false;
	}		
	// DETERMINE IF THIS INDIVIDUAL OT REQUEST IS MODIFIABLE
	public static boolean determineModifiablityOfSingleIndividualRequest(AlohaUser manager, OTHeader otHeader) {
		OTDetail latestDetail = otHeader.getLatestDetail();
		if ( latestDetail == null ) {
			return false;
		}
		if ( latestDetail.isApproved()) {
			return false;
		}
		if ( latestDetail.isDenied()) {
			return false;
		}
		if ( latestDetail.isCancelled()) {
			return false;
		}		
		if ( !latestDetail.isInGroup()) {
			if (latestDetail.isModified()) {
				return true;
			}
		}
		if ( latestDetail.isInGroup() ) {
			if	( latestDetail.isReceived() || latestDetail.isModified() ) {
				OTGroup topLevelGroup = otHeader.getGroup().getTopLevelParent();

				if ( topLevelGroup.isPending()) {
					if ( topLevelGroup.getSubmitter().getUserId() == manager.getUserId() ) {
						return true;
					}
				}
				if ( topLevelGroup.isSubmitted() ) {
					if ( topLevelGroup.getSubmitter().getUserId() == manager.getUserId() ) {
						return true;
					}
				}		
				if ( topLevelGroup.isReceived() ) {
					if ( topLevelGroup.getReceiver().getUserId() == manager.getUserId() ) {
						return true;
					}
				}				
			}
		}
		return false;
	}		
	// DETERMINE IF THIS INDIVIDUAL OT REQUEST IS CANCELLABLE
	public static boolean determineCancelablityOfSingleIndividualRequest(AlohaUser manager, OTHeader otHeader) {
		OTDetail latestDetail = otHeader.getLatestDetail();
		if ( latestDetail == null ) {
			return false;
		}
		if ( latestDetail.isApproved()) {
			return true;
		}
		if ( latestDetail.isDenied()) {
			return false;
		}
		if ( latestDetail.isCancelled()) {
			return false;
		}		
		if ( !latestDetail.isInGroup()) {
			if (latestDetail.isModified()) {
				return true;
			}
		}
		if ( latestDetail.isInGroup() ) {
			if	( latestDetail.isReceived()  || latestDetail.isModified() ) {
				OTGroup topLevelGroup = otHeader.getGroup().getTopLevelParent();

				if ( topLevelGroup.isPending()) {
					if ( topLevelGroup.getSubmitter().getUserId() == manager.getUserId() ) {
						return true;
					}
				}
				if ( topLevelGroup.isSubmitted() ) {
					if ( topLevelGroup.getSubmitter().getUserId() == manager.getUserId() ) {
						return true;
					}
				}		
				if ( topLevelGroup.isReceived() ) {
					if ( topLevelGroup.getReceiver().getUserId() == manager.getUserId() ) {
						return true;
					}
				}				
			}			
		}
		return false;
	}	
}