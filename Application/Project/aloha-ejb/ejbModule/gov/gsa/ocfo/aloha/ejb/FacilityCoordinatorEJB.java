package gov.gsa.ocfo.aloha.ejb;

import gov.gsa.ocfo.aloha.exception.AlohaNotFoundException;
import gov.gsa.ocfo.aloha.exception.AlohaServerException;
import gov.gsa.ocfo.aloha.model.FacilityCoordinator;

import javax.ejb.Local;

@Local
public interface FacilityCoordinatorEJB {
	public FacilityCoordinator retrieveById(long userId)
			throws AlohaNotFoundException, AlohaServerException;
}
