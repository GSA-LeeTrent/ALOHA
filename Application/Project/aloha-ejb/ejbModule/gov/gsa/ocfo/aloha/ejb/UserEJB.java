package gov.gsa.ocfo.aloha.ejb;

import java.sql.SQLException;
import java.util.List;

import gov.gsa.ocfo.aloha.exception.AlohaServerException;
import gov.gsa.ocfo.aloha.exception.AuthorizationException;
import gov.gsa.ocfo.aloha.exception.ValidationException;
import gov.gsa.ocfo.aloha.model.entity.AlohaUser;
import gov.gsa.ocfo.aloha.model.entity.AlohaUserPref;
import gov.gsa.ocfo.aloha.model.entity.EtamsAdXref;
import gov.gsa.ocfo.aloha.model.entity.EtamsUser;
import gov.gsa.ocfo.aloha.model.entity.audit.EventType;

import javax.ejb.Local;

@Local
public interface UserEJB {
	public AlohaUser authorize(String loginName) throws AuthorizationException, ValidationException, AlohaServerException;
	public AlohaUserPref getUserPref(Long userId) throws AlohaServerException;
	public EtamsUser getEtamsUser(Long userId) throws AlohaServerException;
	
	public AlohaUser getUser(Long userid) throws SQLException;
	public List<EtamsAdXref> getEtamsAdXrefs(String loginName) throws AlohaServerException;
	public EtamsAdXref getEtamsAdXref(String loginName) throws AlohaServerException;
	public List<AlohaUser> getEtamsUserByName(String userName) throws AlohaServerException;
	public AlohaUser findAlohaUser(String loginName) throws AlohaServerException;
	public EtamsAdXref saveEtamsAdXref(String originalLoginName, EtamsAdXref etamsAdXref, String emailAddress) throws AlohaServerException, IllegalArgumentException;
	public Integer deleteEtamsAdXref(String loginName) throws AlohaServerException;
	public void writeToEventLog (AlohaUser alohaUser, EventType.EventTypeValue eventTypeValue) throws AlohaServerException;
	

	//////////////////////////////////////////////////////////////////////////////////////////////////////
	// OCP 82245 
	// (Add Event Logging for Change Default Approver and Change Primary Timekeepr in User Preferences)
	// Lee Trent
	//////////////////////////////////////////////////////////////////////////////////////////////////////
	public AlohaUserPref saveUserPref(AlohaUser changedBy, AlohaUserPref aup) throws AlohaServerException;	

}
