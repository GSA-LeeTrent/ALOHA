package gov.gsa.ocfo.aloha.web.mb.ot.list.mgr;

import gov.gsa.ocfo.aloha.web.model.overtime.OTStatusChangeOutcome;
import gov.gsa.ocfo.aloha.web.util.AlohaConstants;
import gov.gsa.ocfo.aloha.web.util.AlohaURIs;
import gov.gsa.ocfo.aloha.web.util.FacesContextUtil;

import java.io.IOException;
import java.io.Serializable;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

@ManagedBean(name=OTMgrListBannerMB.MANAGED_BEAN_NAME)
@ViewScoped
public class OTMgrListBannerMB implements Serializable {
	private static final long serialVersionUID = 1906527836985005636L;
	public static final String MANAGED_BEAN_NAME = "otMgrListBannerMB";
	
	// CONFIRMATION MESSAGE
	private String confirmMsg;
	
	@PostConstruct
	public void init() {
		try {
			this.inspectStatusChangeOutcome();
		} catch( Throwable t) {
			t.printStackTrace();
			try {
				FacesContext.getCurrentInstance().getExternalContext().redirect(AlohaURIs.SERVER_ERROR);
			} catch (IOException ignore) {};
		}
	}
	private void inspectStatusChangeOutcome() {
		if ( (FacesContextUtil.getHttpSession() != null)
				&& (FacesContextUtil.getHttpSession().getAttribute(AlohaConstants.OT_STATUS_CHANGE_OUTCOME_SUPV) != null)
				&& (FacesContextUtil.getHttpSession().getAttribute(AlohaConstants.OT_STATUS_CHANGE_OUTCOME_SUPV) instanceof OTStatusChangeOutcome)
			) {
			OTStatusChangeOutcome outcome = (OTStatusChangeOutcome)FacesContextUtil.getHttpSession().getAttribute(AlohaConstants.OT_STATUS_CHANGE_OUTCOME_SUPV);
			this.confirmMsg = outcome.getConfirmMsg();
			FacesContextUtil.getHttpSession().setAttribute(AlohaConstants.OT_STATUS_CHANGE_OUTCOME_SUPV, null);	
		}
	}
	public String getConfirmMsg() {
		return confirmMsg;
	}
}
