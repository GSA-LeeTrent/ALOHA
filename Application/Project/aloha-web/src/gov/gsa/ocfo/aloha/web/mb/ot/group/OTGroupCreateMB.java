package gov.gsa.ocfo.aloha.web.mb.ot.group;

import gov.gsa.ocfo.aloha.ejb.overtime.GroupOvertimeEJB;
import gov.gsa.ocfo.aloha.exception.AlohaServerException;
import gov.gsa.ocfo.aloha.model.entity.AlohaUser;
import gov.gsa.ocfo.aloha.model.entity.overtime.OTGroup;
import gov.gsa.ocfo.aloha.model.entity.overtime.OTGroupHistory;
import gov.gsa.ocfo.aloha.model.entity.overtime.OTGroupStatus;
import gov.gsa.ocfo.aloha.model.entity.overtime.OTGroupStatusTrans;
import gov.gsa.ocfo.aloha.model.entity.overtime.OTPayPeriod;
import gov.gsa.ocfo.aloha.util.StopWatch;
import gov.gsa.ocfo.aloha.web.mb.overtime.OTUtilMB;

import java.io.Serializable;

import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;

@ManagedBean(name=OTGroupCreateMB.MANAGED_BEAN_NAME, eager=false)
@SessionScoped
public class OTGroupCreateMB implements Serializable{

	private static final long serialVersionUID = -6940256221074375733L;
	public static final String MANAGED_BEAN_NAME = "otGroupCreateMB";

	@EJB
	private GroupOvertimeEJB groupOvertimeEJB;
	
	// JSF: MANAGED BEAN INJECTION
	@ManagedProperty(value="#{otUtilMB}")
	protected OTUtilMB otUtilMB;
	public void setOtUtilMB(OTUtilMB otUtilMB) {
		this.otUtilMB = otUtilMB;
	}
	
	public OTGroup createAndPersistOTGroup(OTPayPeriod otPayPeriod, AlohaUser otGroupSubmitter, AlohaUser createdBy) throws AlohaServerException {
		OTGroup otGroup = this.createOTGroup(otPayPeriod, otGroupSubmitter, createdBy);
		this.persistOTGroup(otGroup);
		return otGroup;
	}
	public OTGroup createOTGroup(OTPayPeriod otPayPeriod, AlohaUser otGroupSubmitter, AlohaUser createdBy) {

		// OT_GROUP
		OTGroup otGroup = new OTGroup();
		otGroup.setPayPeriod(otPayPeriod);
		otGroup.setStatus(this.otUtilMB.getOTGroupStatus(OTGroupStatus.CodeValues.PENDING));
		otGroup.setSubmitter(otGroupSubmitter);
		otGroup.getCreatedBy().setUserCreated(createdBy.getUserId());
		
		
		// OT_GROUP_HISTORY
		OTGroupHistory otGroupHist = new OTGroupHistory();
		otGroupHist.setGroup(otGroup);
		otGroupHist.setStatusTransition(this.otUtilMB.getOTGroupStatusTrans(OTGroupStatusTrans.ActionCodeValues.NONE_TO_PENDING));
		otGroupHist.setHistorySequence(otGroup.getHistoricalEntries().size() + 1);
		otGroupHist.getCreatedBy().setUserCreated(createdBy.getUserId());
		
		otGroup.addGroupHistory(otGroupHist);
		return otGroup;
	}	
	
	public void persistOTGroup(OTGroup otGroup) throws AlohaServerException {
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		this.groupOvertimeEJB.saveOTGroup(otGroup);
		stopWatch.stop();
		System.out.println("ELAPSED TIME (Create/Save Overtime Group Request): " + stopWatch.getElapsedTime() + " ms");
		stopWatch = null;
	}
}