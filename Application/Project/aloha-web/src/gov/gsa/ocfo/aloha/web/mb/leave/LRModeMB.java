package gov.gsa.ocfo.aloha.web.mb.leave;

import gov.gsa.ocfo.aloha.web.enums.LRMode;
import gov.gsa.ocfo.aloha.web.util.AlohaConstants;

import java.io.Serializable;

import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;

@ManagedBean(name="lrModeMB", eager=true)
@ApplicationScoped
public class LRModeMB implements Serializable {
	private static final long serialVersionUID = 8068813107208909782L;

	public String getSubmitOwnMode() {
		return LRMode.SUBMIT_OWN.getText();
	}
	public String getOnBehalfOfMode() {
		return LRMode.ON_BEHALF_OF.getText();
	}
	public String getApproverMode() {
		return LRMode.APPROVER.getText();
	}
	public String getModeKey() {
		return AlohaConstants.LR_MODE;
	}
}
