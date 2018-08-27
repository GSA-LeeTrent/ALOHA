package gov.gsa.ocfo.aloha.ejb.overtime;

import gov.gsa.ocfo.aloha.exception.AlohaServerException;
import gov.gsa.ocfo.aloha.model.overtime.OTListRow;

import java.util.List;

import javax.ejb.Local;

@Local
public interface OTIndivListEJB {
	public List<OTListRow> retrieveOTIndivListForSupervisor(long supvUserId) throws AlohaServerException;
	public List<OTListRow> retrieveOTIndivListForSubmitOwn(long soUserId) throws AlohaServerException;
	public List<OTListRow> retrieveOTIndivListForOnBehalfOf(long oboUserId) throws AlohaServerException;
	
}
