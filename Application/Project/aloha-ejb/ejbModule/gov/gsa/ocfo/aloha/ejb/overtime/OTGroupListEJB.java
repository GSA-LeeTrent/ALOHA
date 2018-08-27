package gov.gsa.ocfo.aloha.ejb.overtime;

import gov.gsa.ocfo.aloha.exception.AlohaServerException;
import gov.gsa.ocfo.aloha.model.overtime.OTListRow;

import java.util.List;

import javax.ejb.Local;

@Local
public interface OTGroupListEJB {
	public List<OTListRow> retrieveOTGroupListForSubmitter(long submitterUserId) throws AlohaServerException;
}
