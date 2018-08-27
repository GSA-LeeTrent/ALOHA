package gov.gsa.ocfo.aloha.web.util;

import gov.gsa.ocfo.aloha.model.entity.AlohaUser;
import gov.gsa.ocfo.aloha.model.entity.overtime.OTDetail;
import gov.gsa.ocfo.aloha.model.entity.overtime.OTGroup;
import gov.gsa.ocfo.aloha.model.entity.overtime.OTHeader;

import java.util.List;

public class OTGroupStatusChangeUtil {
	
	// DETERMINE WHICH INDIVIDUAL OT REQUESTS IN GROUP LIST ARE MODIFABLE
	public static void determineModifiablityOfAllIndividualRequestsInGroup(AlohaUser manager, List<OTGroup> otGroupList) {
		for ( OTGroup otGroup : otGroupList) {
			for (OTHeader otHeader : otGroup.getAllEmployees()) {
				otHeader.setModifiableBySupervisor(determineModifiablityOfSingleIndividualRequest(manager, otHeader));
			}
		}
	}
	// DETERMINE WHICH INDIVIDUAL OT REQUESTS IN GROUP LIST ARE CANCELLABLE
	public static void determineCancelablityOfAllIndividualRequestsInGroup(AlohaUser manager, List<OTGroup> otGroupList) {
		for ( OTGroup otGroup : otGroupList) {
			for (OTHeader otHeader : otGroup.getAllEmployees()) {
				otHeader.setCancellableBySupervisor(determineCancelablityOfSingleIndividualRequest(manager, otHeader));
			}
		}
	}	
	// DETERMINE IF THIS INDIVIDUAL OT REQUEST IS MODIFIABLE
	private static boolean determineModifiablityOfSingleIndividualRequest(AlohaUser manager, OTHeader otHeader) {
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
		return false;
	}	

	// DETERMINE IF THIS INDIVIDUAL OT REQUEST IS CANCELLABLE
	private static boolean determineCancelablityOfSingleIndividualRequest(AlohaUser manager, OTHeader otHeader) {
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
		return false;
	}		
}
