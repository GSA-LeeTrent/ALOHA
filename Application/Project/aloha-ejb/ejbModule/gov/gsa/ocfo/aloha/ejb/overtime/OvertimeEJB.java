package gov.gsa.ocfo.aloha.ejb.overtime; 

import gov.gsa.ocfo.aloha.exception.AlohaServerException;
import gov.gsa.ocfo.aloha.model.KeyValuePair;
import gov.gsa.ocfo.aloha.model.entity.overtime.OTActorType;
import gov.gsa.ocfo.aloha.model.entity.overtime.OTDetail;
import gov.gsa.ocfo.aloha.model.entity.overtime.OTEntityType;
import gov.gsa.ocfo.aloha.model.entity.overtime.OTHeader;
import gov.gsa.ocfo.aloha.model.entity.overtime.OTIndivStatus;
import gov.gsa.ocfo.aloha.model.entity.overtime.OTIndivStatusTrans;
import gov.gsa.ocfo.aloha.model.entity.overtime.OTPayPeriod;
import gov.gsa.ocfo.aloha.model.entity.overtime.OTSalaryGrade;
import gov.gsa.ocfo.aloha.model.entity.overtime.OTType;

import java.util.List;

import javax.ejb.Local;

@Local
public interface OvertimeEJB {
	public void saveOvertimeRequest(OTHeader otHeader) throws AlohaServerException;
	public OTHeader updateOTHeader(OTHeader otHeader) throws AlohaServerException;
	public OTDetail updateOTDetail(OTDetail otDetail) throws AlohaServerException;
	public OTDetail retrieveDetailByID(long otDetailId) throws AlohaServerException;
	public List<OTDetail> retrieveEmployeeList(long employeeUserId) throws AlohaServerException;	
	public List<OTDetail> retrieveSubmitterList(long submitterUserId) throws AlohaServerException;
	public List<OTDetail> retrieveSupervisorList(long supervisorUserId, String otStatusCode) throws AlohaServerException;
	public List<OTDetail> retrieveSupervisorList(long supervisorUserId) throws AlohaServerException;
	public List<OTSalaryGrade> retrieveAllOTSalaryGrade() throws AlohaServerException;
	public List<OTPayPeriod> retrieveAllOTPayPeriod() throws AlohaServerException;	
	public List<OTType> retrieveAllOTType() throws AlohaServerException;
	public List<OTType> retrieveAllOTTypeEmp() throws AlohaServerException;
	public List<OTType> retrieveAllOTTypeOBO() throws AlohaServerException;
	public List<OTEntityType> retrieveAllOTEntityType() throws AlohaServerException;
	public List<OTActorType> retrieveAllOTActorType() throws AlohaServerException;
	public List<OTIndivStatus> retrieveAllOTIndivStatus() throws AlohaServerException;
	public List<OTIndivStatusTrans> retrieveAllOTIndivStatusTrans() throws AlohaServerException;
	public String retrieveSalaryGradeKey(long userId) throws AlohaServerException;
	public List<KeyValuePair> retrieveOTBalances(long employeeId) throws AlohaServerException;
	public OTHeader findById(long id) throws AlohaServerException;
}
