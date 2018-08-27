package gov.gsa.ocfo.aloha.web.mb;

import gov.gsa.ocfo.aloha.ejb.FacilityCoordinatorEJB;
import gov.gsa.ocfo.aloha.exception.AlohaNotFoundException;
import gov.gsa.ocfo.aloha.exception.AlohaServerException;
import gov.gsa.ocfo.aloha.model.Facility;
import gov.gsa.ocfo.aloha.model.FacilityCoordinator;
import gov.gsa.ocfo.aloha.web.util.AlohaURIs;
import gov.gsa.ocfo.aloha.web.util.ErrorMessages;
import gov.gsa.ocfo.aloha.web.util.FacesContextUtil;

import java.io.IOException;
import java.io.Serializable;
import java.util.Iterator;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;

@ManagedBean(name="facilityCoordinatorMB", eager=false)
@SessionScoped
public class FacilityCoordinatorMB implements Serializable {

	private static final long serialVersionUID = 1998860441251445073L;

	public static final String MANAGED_BEAN_NAME = "facilityCoordinatorMB";

	@EJB
	private FacilityCoordinatorEJB facilityCoordinatorEJB;

	@ManagedProperty(value="#{userMB}")
	private UserMB userMB;
	public void setUserMB(UserMB userMB) {
		this.userMB = userMB;
	}

	private SelectItem[] facilities = new SelectItem[0];
	public SelectItem[] getFacilities() {
		return facilities;
	}
	
	private FacilityCoordinator facilityCoordinator;

	@PostConstruct
	public void init() {
		if ( this.userMB != null & this.userMB.getUser() != null) {
			try {
				
				this.facilityCoordinator = this.facilityCoordinatorEJB.retrieveById(this.userMB.getUser().getUserId());
				this.buildSelectItems();
			} catch (AlohaNotFoundException e) {
				try {
					Object[] params = { this.userMB.getFullName() };
					String errMsg = ErrorMessages.getInstance().getMessage(ErrorMessages.LR_REPORT_VARIANCE_UNAUTHORIZED, params);
					
					//System.out.println(this.userMB.getFullName());
					//System.out.println(errMsg);
					FacesContextUtil.getHttpSession().setAttribute("unauthMsg", errMsg);
					FacesContext.getCurrentInstance().getExternalContext().redirect(AlohaURIs.UNAUTHORIZED);
				} catch (IOException ignore) {}

			} catch (AlohaServerException ase) {
				try {
					ase.printStackTrace();
					FacesContext.getCurrentInstance().getExternalContext().redirect(AlohaURIs.SERVER_ERROR);
				} catch (IOException ignore) {}
			}
		}
	}
		
	private void buildSelectItems() {

		synchronized(this.facilities) {
			
			this.facilities = null;
			this.facilities = new SelectItem[this.facilityCoordinator.getFacilities().size()];
			
			Iterator<Facility>facilityIterator = this.facilityCoordinator.getFacilities().iterator();
			int index = 0;
			
			while ( facilityIterator.hasNext() ) {
				Facility facility = facilityIterator.next();
				this.facilities[index++] = new SelectItem(String.valueOf(facility.getId()), facility.getLabel());
			}
		}
	}
}