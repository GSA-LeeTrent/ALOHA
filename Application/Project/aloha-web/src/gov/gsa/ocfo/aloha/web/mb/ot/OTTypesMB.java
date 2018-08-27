package gov.gsa.ocfo.aloha.web.mb.ot;

import gov.gsa.ocfo.aloha.ejb.overtime.OvertimeEJB;
import gov.gsa.ocfo.aloha.exception.AlohaServerException;
import gov.gsa.ocfo.aloha.model.entity.overtime.OTType;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.model.SelectItem;

@ManagedBean(name="otTypesMB", eager=false)
@SessionScoped
public class OTTypesMB implements Serializable {

	private static final long serialVersionUID = 6928391684730491840L;

	@EJB
	private OvertimeEJB overtimeEJB;
	
	private Map<String, OTType> otTypeMap = new HashMap<String, OTType>();	
	private SelectItem[] otTypes = new SelectItem[0];
	public SelectItem[] getOtTypes() {
		return otTypes;
	}

	public OTType getOTType(String code) {
		return this.otTypeMap.get(code);
	}			
	
	//2012-10-02 JJM 48969: Add buildOTTypesEffEmp()	
	public void buildOTTypesEmp() throws AlohaServerException {
		try {
			List<OTType> list = this.overtimeEJB.retrieveAllOTTypeEmp();
			
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

	//2012-10-02 JJM 48969: Add buildOTTypesEffOBO()
	public void buildOTTypesOBO() throws AlohaServerException {
		try {
			List<OTType> list = this.overtimeEJB.retrieveAllOTTypeOBO();
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
	
//	public SelectItem[] getOtTypesEmp() {
//	return otTypes;
//}
//
//public void setOtTypes(SelectItem[] otTypes) {
//	this.otTypes = otTypes;
//}
//
//
//public SelectItem[] getOTTypesEmp() {
//	try {
//		this.buildOTTypesEmp();
//	} catch (AlohaServerException e) {
//		e.printStackTrace();
//		try {
//			FacesContext.getCurrentInstance().getExternalContext().redirect(AlohaURIs.SERVER_ERROR);				
//		} catch( IOException ioe) {
//			ioe.printStackTrace();
//		}
//	}
//	return this.otTypes;
//}

//public SelectItem[] getOTTypesOBO() {
//	try {
//		this.buildOTTypesOBO();
//	} catch (AlohaServerException e) {
//		e.printStackTrace();
//		try {
//			FacesContext.getCurrentInstance().getExternalContext().redirect(AlohaURIs.SERVER_ERROR);				
//		} catch( IOException ioe) {
//			ioe.printStackTrace();
//		}
//	}
//	return this.otTypes;
//}
}