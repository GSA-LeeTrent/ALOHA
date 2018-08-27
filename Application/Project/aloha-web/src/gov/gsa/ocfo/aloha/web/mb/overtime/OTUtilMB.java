package gov.gsa.ocfo.aloha.web.mb.overtime;

import gov.gsa.ocfo.aloha.ejb.overtime.GroupOvertimeEJB;
import gov.gsa.ocfo.aloha.ejb.overtime.OvertimeEJB;
import gov.gsa.ocfo.aloha.exception.AlohaServerException;
import gov.gsa.ocfo.aloha.model.entity.overtime.OTActorType;
import gov.gsa.ocfo.aloha.model.entity.overtime.OTEntityType;
import gov.gsa.ocfo.aloha.model.entity.overtime.OTGroupStatus;
import gov.gsa.ocfo.aloha.model.entity.overtime.OTGroupStatusTrans;
import gov.gsa.ocfo.aloha.model.entity.overtime.OTIndivStatus;
import gov.gsa.ocfo.aloha.model.entity.overtime.OTIndivStatusTrans;
import gov.gsa.ocfo.aloha.model.entity.overtime.OTPayPeriod;
import gov.gsa.ocfo.aloha.model.entity.overtime.OTSalaryGrade;
import gov.gsa.ocfo.aloha.model.entity.overtime.OTType;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.model.SelectItem;

@ManagedBean(name="otUtilMB", eager=false)
@SessionScoped
public class OTUtilMB implements Serializable {
	private static final long serialVersionUID = -7998363001469380608L;

	@EJB
	private OvertimeEJB overtimeEJB;
	
	@EJB
	private GroupOvertimeEJB groupOvertimeEJB;
	
	private Map<String, OTSalaryGrade> otSalaryGradeMap = new HashMap<String, OTSalaryGrade>();	
	private Map<Long, OTPayPeriod> otPayPeriodMap = new HashMap<Long, OTPayPeriod>();	
	private Map<String, OTType> otTypeMap = new HashMap<String, OTType>();	
	private Map<String, OTEntityType> otEntityTypeMap = new HashMap<String, OTEntityType>();
	private Map<String, OTActorType> otActorTypeMap = new HashMap<String, OTActorType>();
	private Map<String, OTIndivStatus> otIndivStatusMap = new HashMap<String, OTIndivStatus>();
	private Map<String, OTIndivStatusTrans> otIndivStatusTransMap = new HashMap<String, OTIndivStatusTrans>();
	private Map<String, OTGroupStatus> otGroupStatusMap = new HashMap<String, OTGroupStatus>();
	private Map<String, OTGroupStatusTrans> otGroupStatusTransMap = new HashMap<String, OTGroupStatusTrans>();

	private SelectItem[] otTypes = new SelectItem[0];
	private SelectItem[] otIndivStatuses = new SelectItem[0];
	private SelectItem[] otGroupStatuses = new SelectItem[0];
	private SelectItem[] otSalaryGrades = new SelectItem[0];
	
	@PostConstruct
	public void refresh() {
		this.initAllOTSalaryGrade();
		this.initAllOTPayPeriod();
		this.initAllOTType();
		this.initAllOTEntityType();
		this.initAllOTActorType();
		this.initAllOTIndivStatus();
		this.initAllOTGroupStatus();
		this.initAllOTIndivStatusTrans();
		this.initAllOTGroupStatusTrans();
	}
	private void initAllOTSalaryGrade() {
		try {
			List<OTSalaryGrade> list = this.overtimeEJB.retrieveAllOTSalaryGrade();
			synchronized(this.otSalaryGradeMap) {
				this.otSalaryGradeMap.clear();
				for ( OTSalaryGrade model: list) {
					this.otSalaryGradeMap.put(model.getKey(), model);
				}
			}
			// BUILD DROP-DOWN
			synchronized(this.otSalaryGrades) {
				this.otSalaryGrades = null;
				this.otSalaryGrades = new SelectItem[list.size()];
				for (int ii = 0; ii < list.size(); ii++) {
					OTSalaryGrade model = list.get(ii);
					otSalaryGrades[ii] = new SelectItem(String.valueOf(model.getValue()), model.getLabel());
				}
			}
		} catch (AlohaServerException ase) {
			ase.printStackTrace();
			synchronized(this.otSalaryGradeMap) {
				this.otSalaryGradeMap.clear();
			}
			synchronized(this.otSalaryGrades) {
				this.otSalaryGrades = new SelectItem[0];
			}
		}
	}	
	private void initAllOTPayPeriod() {
		try {
			List<OTPayPeriod> list = this.overtimeEJB.retrieveAllOTPayPeriod();
			synchronized(this.otPayPeriodMap) {
				this.otPayPeriodMap.clear();
				for ( OTPayPeriod model: list) {
					this.otPayPeriodMap.put(Long.valueOf(model.getKey()), model);
				}
			}
		} catch (AlohaServerException ase) {
			ase.printStackTrace();
			synchronized(this.otPayPeriodMap) {
				this.otPayPeriodMap.clear();
			}
		}
	}	
	private void initAllOTType() {
		try {
			List<OTType> list = this.overtimeEJB.retrieveAllOTType();
			synchronized(this.otTypeMap) {
				this.otTypeMap.clear();
				for (OTType model: list) {
					this.otTypeMap.put(model.getCode(), model);
				}
			}
			// BUILD DROP-DOWN
			synchronized(this.otTypes) {
				this.otTypes = null;
				this.otTypes = new SelectItem[list.size()];
				for (int ii = 0; ii < list.size(); ii++) {
					OTType model = list.get(ii);
					otTypes[ii] = new SelectItem(String.valueOf(model.getCode()), model.getLabel());
				}			
			}
		} catch(AlohaServerException ase) {
			ase.printStackTrace();
			synchronized(this.otTypeMap) {
				this.otTypeMap.clear();
			}
			synchronized(this.otTypes) {
				this.otTypes = new SelectItem[0];
			}
		}
	}		
	private void initAllOTEntityType() {
		try {
			List<OTEntityType> list = this.overtimeEJB.retrieveAllOTEntityType();
			synchronized(this.otEntityTypeMap) {
				this.otEntityTypeMap.clear();	
				for ( OTEntityType model: list) {
					this.otEntityTypeMap.put(model.getCode(), model);
				}
			}
		} catch (AlohaServerException ase) {
			ase.printStackTrace();
			synchronized(this.otEntityTypeMap) {
				this.otEntityTypeMap.clear();	
			}
		}
	}	
	private void initAllOTActorType()  {
		try {
			List<OTActorType> list = this.overtimeEJB.retrieveAllOTActorType();
			synchronized(this.otActorTypeMap) {
				this.otActorTypeMap.clear();
				for ( OTActorType model: list) {
					this.otActorTypeMap.put(model.getCode(), model);
				}
			}
		} catch (AlohaServerException ase) {
			ase.printStackTrace();
			synchronized(this.otActorTypeMap) {
				this.otActorTypeMap.clear();
			}
		}
	}		
	private void initAllOTIndivStatus() {
		try {
			List<OTIndivStatus> list = this.overtimeEJB.retrieveAllOTIndivStatus();
			synchronized(this.otIndivStatusMap) {
				this.otIndivStatusMap.clear();
				for ( OTIndivStatus model: list) {
					this.otIndivStatusMap.put(model.getCode(), model);
				}
			}
			// BUILD DROP-DOWN
			synchronized(this.otIndivStatuses) {
				this.otIndivStatuses = null;
				this.otIndivStatuses = new SelectItem[list.size()];
				for (int ii = 0; ii < list.size(); ii++) {
					OTIndivStatus model = list.get(ii);
					this.otIndivStatuses[ii] = new SelectItem(String.valueOf(model.getId()), model.getName());
				}
			}	
		} catch (AlohaServerException ase) {
			ase.printStackTrace();
			synchronized(this.otIndivStatusMap) {
				this.otIndivStatusMap.clear();
			}
			synchronized(this.otIndivStatuses) {
				this.otIndivStatuses = new SelectItem[0];
			}	
		}
	}
	private void initAllOTGroupStatus() {
		try {
			List<OTGroupStatus> list = this.groupOvertimeEJB.retrieveAllOTGroupStatus();
			synchronized(this.otGroupStatusMap) {
				this.otGroupStatusMap.clear();
				for ( OTGroupStatus model: list) {
					this.otGroupStatusMap.put(model.getCode(), model);
				}
			}
			// BUILD DROP-DOWN
			synchronized(this.otGroupStatuses) {
				this.otGroupStatuses = null;
				this.otGroupStatuses = new SelectItem[list.size()];
				for (int ii = 0; ii < list.size(); ii++) {
					OTGroupStatus model = list.get(ii);
					this.otGroupStatuses[ii] = new SelectItem(String.valueOf(model.getId()), model.getName());
				}
			}	
		} catch (AlohaServerException ase) {
			ase.printStackTrace();
			synchronized(this.otGroupStatusMap) {
				this.otGroupStatusMap.clear();
			}
			synchronized(this.otGroupStatuses) {
				this.otGroupStatuses = new SelectItem[0];
			}	
		}
	}	
	private void initAllOTIndivStatusTrans() {
		try {
			List<OTIndivStatusTrans> list = this.overtimeEJB.retrieveAllOTIndivStatusTrans();
			synchronized(this.otIndivStatusTransMap) {
				this.otIndivStatusTransMap.clear();
				for ( OTIndivStatusTrans model: list) {
					this.otIndivStatusTransMap.put(model.getActionCode(), model);
				}
			}
		} catch (AlohaServerException ase) {
			ase.printStackTrace();
			synchronized(this.otIndivStatusTransMap) {
				this.otIndivStatusTransMap.clear();
			}
		}
	}	
	private void initAllOTGroupStatusTrans() {
		try {
			List<OTGroupStatusTrans> list = this.groupOvertimeEJB.retrieveAllOTGroupStatusTrans();
			synchronized(this.otGroupStatusTransMap) {
				this.otGroupStatusTransMap.clear();
				for ( OTGroupStatusTrans model: list) {
					this.otGroupStatusTransMap.put(model.getActionCode(), model);
				}
			}
		} catch (AlohaServerException ase) {
			ase.printStackTrace();
			synchronized(this.otGroupStatusTransMap) {
				this.otGroupStatusTransMap.clear();
			}
		}
	}	
	public OTSalaryGrade getOTSalaryGrade(String key) {
		if ( this.otSalaryGradeMap.isEmpty() ) {
			this.initAllOTSalaryGrade();
		}
		return this.otSalaryGradeMap.get(key);
	}			
	public OTPayPeriod getOTPayPeriod(String key) {
		if ( this.otPayPeriodMap.isEmpty() ) {
			this.initAllOTPayPeriod();
		}
		return this.otPayPeriodMap.get(Long.valueOf(key));
	}				
	
	public OTType getOTType(String code) {
		if ( this.otTypeMap.isEmpty() ) {
			this.initAllOTType();
		}
		return this.otTypeMap.get(code);
	}			
	public OTEntityType getOTEntityType(String code) {
		if ( this.otEntityTypeMap.isEmpty() ) {
			this.initAllOTEntityType();
		}
		return this.otEntityTypeMap.get(code);
	}		
	public OTActorType getOTActorType(String code) {
		if ( this.otActorTypeMap.isEmpty() ) {
			this.initAllOTActorType();
		}
		return this.otActorTypeMap.get(code);
	}		
	public OTIndivStatus getOTIndivStatus(String code) {
		if ( this.otIndivStatusMap.isEmpty() ) {
			this.initAllOTIndivStatus();
		}
		return this.otIndivStatusMap.get(code);
	}	
	public OTIndivStatusTrans getOTIndivStatusTrans(String code) {
		if ( this.otIndivStatusTransMap.isEmpty() ) {
			this.initAllOTIndivStatusTrans();
		}
		return this.otIndivStatusTransMap.get(code);
	}
	public OTGroupStatus getOTGroupStatus(String code) {
		if ( this.otGroupStatusMap.isEmpty() ) {
			this.initAllOTGroupStatus();
		}
		return this.otGroupStatusMap.get(code);
	}	
	public OTGroupStatusTrans getOTGroupStatusTrans(String code) {
		if ( this.otGroupStatusTransMap.isEmpty() ) {
			this.initAllOTGroupStatusTrans();
		}
		return this.otGroupStatusTransMap.get(code);
	}	
	public SelectItem[] getOtTypes() {
		return otTypes;
	}
	public SelectItem[] getOtSalaryGrades() {
		if ( (this.otSalaryGrades == null) || (this.otSalaryGrades.length == 0) ) {
			this.initAllOTSalaryGrade();
		}
		return otSalaryGrades;
	}
	public SelectItem[] getOtIndivStatuses() {
		return otIndivStatuses;
	}	
	public String determineOTIndivStatusTransCode(String oldStatusCode, String newStatusCode) {
		if ( oldStatusCode.equals(OTIndivStatus.CodeValues.SUBMITTED)) {
			if ( newStatusCode.equals(OTIndivStatus.CodeValues.RECEIVED)) {
				return OTIndivStatusTrans.ActionCodeValues.SUBMITTED_TO_RECEIVED;
			} else if ( newStatusCode.equals(OTIndivStatus.CodeValues.APPROVED)) {
				return OTIndivStatusTrans.ActionCodeValues.SUBMITTED_TO_APPROVED;
			} else if ( newStatusCode.equals(OTIndivStatus.CodeValues.DENIED)) {
				return OTIndivStatusTrans.ActionCodeValues.SUBMITTED_TO_DENIED;
			} else if ( newStatusCode.equals(OTIndivStatus.CodeValues.CANCELLED)) {
				return OTIndivStatusTrans.ActionCodeValues.SUBMITTED_TO_CANCELLED;
			} else if ( newStatusCode.equals(OTIndivStatus.CodeValues.RESUBMITTED)) {
				return OTIndivStatusTrans.ActionCodeValues.SUBMITTED_TO_RESUBMITTED;
			} else if ( newStatusCode.equals(OTIndivStatus.CodeValues.MODIFIED)) {
				return OTIndivStatusTrans.ActionCodeValues.SUBMITTED_TO_MODIFIED;
			} 
		} else if ( oldStatusCode.equals(OTIndivStatus.CodeValues.RECEIVED)) {
			if ( newStatusCode.equals(OTIndivStatus.CodeValues.APPROVED)) {
				return OTIndivStatusTrans.ActionCodeValues.RECEIVED_TO_APPROVED;
			} else if ( newStatusCode.equals(OTIndivStatus.CodeValues.DENIED)) {
				return OTIndivStatusTrans.ActionCodeValues.RECEIVED_TO_DENIED;
			} else if ( newStatusCode.equals(OTIndivStatus.CodeValues.CANCELLED)) {
				return OTIndivStatusTrans.ActionCodeValues.RECEIVED_TO_CANCELLED;
			} else if ( newStatusCode.equals(OTIndivStatus.CodeValues.MODIFIED)) {
				return OTIndivStatusTrans.ActionCodeValues.RECEIVED_TO_MODIFIED;
			} else if ( newStatusCode.equals(OTIndivStatus.CodeValues.RECEIVED_WITH_MODS)) {
				return OTIndivStatusTrans.ActionCodeValues.RECEIVED_TO_RECEIVED_WITH_MODS;
			}
		} else if ( oldStatusCode.equals(OTIndivStatus.CodeValues.APPROVED)) {
			if ( newStatusCode.equals(OTIndivStatus.CodeValues.CANCELLED)) {
				return OTIndivStatusTrans.ActionCodeValues.APPROVED_TO_CANCELLED;
			}	
		} else if ( oldStatusCode.equals(OTIndivStatus.CodeValues.RECEIVED_WITH_MODS)) {
			if ( newStatusCode.equals(OTIndivStatus.CodeValues.RECEIVED_WITH_MODS)) {
				return OTIndivStatusTrans.ActionCodeValues.RECEIVED_WITH_MODS_TO_RECEIVED_WITH_MODS;
			} else if ( newStatusCode.equals(OTIndivStatus.CodeValues.CANCELLED)) {
				return OTIndivStatusTrans.ActionCodeValues.RECEIVED_WITH_MODS_TO_CANCELLED;
			} else if ( newStatusCode.equals(OTIndivStatus.CodeValues.APPROVED)) {
				return OTIndivStatusTrans.ActionCodeValues.RECEIVED_WITH_MODS_TO_APPROVED;
			} else if ( newStatusCode.equals(OTIndivStatus.CodeValues.DENIED)) {
				return OTIndivStatusTrans.ActionCodeValues.RECEIVED_WITH_MODS_TO_DENIED;
			}	
		} else if ( oldStatusCode.equals(OTIndivStatus.CodeValues.RESUBMITTED)) {
			if ( newStatusCode.equals(OTIndivStatus.CodeValues.APPROVED)) {
				return OTIndivStatusTrans.ActionCodeValues.RESUBMITTED_TO_APPROVED;
			} else if ( newStatusCode.equals(OTIndivStatus.CodeValues.RECEIVED)) {
				return OTIndivStatusTrans.ActionCodeValues.RESUBMITTED_TO_RECEIVED;
			} else if ( newStatusCode.equals(OTIndivStatus.CodeValues.RESUBMITTED)) {
				return OTIndivStatusTrans.ActionCodeValues.RESUBMITTED_TO_RESUBMITTED;
			} else if ( newStatusCode.equals(OTIndivStatus.CodeValues.CANCELLED)) {
				return OTIndivStatusTrans.ActionCodeValues.RESUBMITTED_TO_CANCELLED;
			} else if ( newStatusCode.equals(OTIndivStatus.CodeValues.DENIED)) {
				return OTIndivStatusTrans.ActionCodeValues.RESUBMITTED_TO_DENIED;
			} else if ( newStatusCode.equals(OTIndivStatus.CodeValues.MODIFIED)) {
				return OTIndivStatusTrans.ActionCodeValues.RESUBMITTED_TO_MODIFIED;
			} 
		} else if ( oldStatusCode.equals(OTIndivStatus.CodeValues.MODIFIED)) {
			if ( newStatusCode.equals(OTIndivStatus.CodeValues.APPROVED)) {
				return OTIndivStatusTrans.ActionCodeValues.MODIFIED_TO_APPROVED;
			} else if ( newStatusCode.equals(OTIndivStatus.CodeValues.DENIED)) {
				return OTIndivStatusTrans.ActionCodeValues.MODIFIED_TO_DENIED;
			} else if ( newStatusCode.equals(OTIndivStatus.CodeValues.MODIFIED)) {
				return OTIndivStatusTrans.ActionCodeValues.MODIFIED_TO_MODIFIED;
			} else if ( newStatusCode.equals(OTIndivStatus.CodeValues.RECEIVED)) {
				return OTIndivStatusTrans.ActionCodeValues.MODIFIED_TO_RECEIVED;
			} else if ( newStatusCode.equals(OTIndivStatus.CodeValues.CANCELLED)) {
				return OTIndivStatusTrans.ActionCodeValues.MODIFIED_TO_CANCELLED;
			}
		}
		return null;
	}
}