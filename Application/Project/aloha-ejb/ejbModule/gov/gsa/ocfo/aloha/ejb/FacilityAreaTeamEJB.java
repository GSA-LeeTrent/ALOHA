package gov.gsa.ocfo.aloha.ejb;

import gov.gsa.ocfo.aloha.exception.AlohaServerException;
import gov.gsa.ocfo.aloha.model.FacilityAreaTeam;

import java.util.List;

import javax.ejb.Local;

@Local
public interface FacilityAreaTeamEJB  {
	public List<FacilityAreaTeam> getTeamsManagedByUser(long employeeId) throws AlohaServerException;

}
