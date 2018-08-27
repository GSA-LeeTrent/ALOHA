package gov.gsa.ocfo.aloha.ejb.overtime;

import gov.gsa.ocfo.aloha.exception.AlohaServerException;
import gov.gsa.ocfo.aloha.model.entity.AlohaUser;
import gov.gsa.ocfo.aloha.model.entity.overtime.OTGroup;
import gov.gsa.ocfo.aloha.model.entity.overtime.OTGroupStatus;
import gov.gsa.ocfo.aloha.model.entity.overtime.OTGroupStatusTrans;
import gov.gsa.ocfo.aloha.model.entity.overtime.OTPayPeriod;

import java.util.List;

import javax.ejb.Local;

@Local
public interface GroupOvertimeEJB {
	public void saveOTGroup(OTGroup otGroup) throws AlohaServerException;
	public OTGroup updateOTGroup(OTGroup otGroup) throws AlohaServerException;
	public OTGroup retrieveByGroupId(long otGroupId) throws AlohaServerException;
	public List<OTGroupStatus> retrieveAllOTGroupStatus() throws AlohaServerException;
	public List<OTGroupStatusTrans> retrieveAllOTGroupStatusTrans() throws AlohaServerException;
	
	public OTGroup retrieveOTGroupForSubmitter(AlohaUser otGroupSubmitter, 
			OTPayPeriod otPayPeriod, OTGroupStatus otGroupStatus) throws AlohaServerException;
	
	public List<OTGroup> retrieveOTGroupsForSubmitterOrReceiver(long mgrUserId) throws AlohaServerException;
	
	public List<OTGroup> retrieveAllOTGroupsForSubmitter(long submitterUserId) throws AlohaServerException;
	public List<OTGroup> retrieveAllOTGroupsForReceiver(long receiverUserId) throws AlohaServerException;
	
	public List<OTGroup> retrieveOTGroupsForManager(long otMgrUserId) throws AlohaServerException;
	
}
