package gov.gsa.ocfo.aloha.web.mb.ot.list;

import gov.gsa.ocfo.aloha.exception.AlohaServerException;
import gov.gsa.ocfo.aloha.exception.AuthorizationException;
import gov.gsa.ocfo.aloha.model.overtime.OTListRow;
import gov.gsa.ocfo.aloha.web.util.AlohaConstants;
import gov.gsa.ocfo.aloha.web.util.ErrorMessages;
import gov.gsa.ocfo.aloha.web.util.FacesContextUtil;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

@ManagedBean(name="otIndivListSOMB")
@ViewScoped
public class OTIndivListSOMB extends OTIndivListAbstractMB {
	private static final long serialVersionUID = 344662072210463316L;

	@PostConstruct
	public void init() {
		super.init();
	}
	
	// METHOD REQUIRED BY ABSTRACT SUPERCLASS
	protected void checkAccessRights() throws AuthorizationException {
		if ( !this.userMB.isSubmitOwn() ) {
			Object[] params = { this.userMB.getFullName() };
			String errMsg = ErrorMessages.getInstance().getMessage(ErrorMessages.OT_LIST_UNAUTHORIZED_SO, params);
			FacesContextUtil.getHttpSession().setAttribute(AlohaConstants.UNAUTHORIZED_MESSAGE, errMsg);
			throw new AuthorizationException(errMsg);
		}
	}		

	// METHOD REQUIRED BY ABSTRACT SUPERCLASS
	protected List<OTListRow> retrieveOTIndivList() throws AlohaServerException {
		return this.otIndivListEJB.retrieveOTIndivListForSubmitOwn(this.userMB.getUser().getUserId());	
	}

	// METHOD REQUIRED BY ABSTRACT SUPERCLASS
	protected String getStatusChangeOutcomeKey() {
		return AlohaConstants.OT_STATUS_CHANGE_OUTCOME_SO;
	}
}