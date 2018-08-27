package gov.gsa.ocfo.aloha.web.mb.ot;

import gov.gsa.ocfo.aloha.exception.AuthorizationException;
import gov.gsa.ocfo.aloha.web.util.ErrorMessages;
import gov.gsa.ocfo.aloha.web.util.FacesContextUtil;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

@ManagedBean(name="otViewSOMB")
@ViewScoped
public class OTViewSOMB extends OTViewAbstractMB {
	private static final long serialVersionUID = 7806762750750000363L;

	@PostConstruct
	public void init() {
		super.init();
	}
	
	// METHOD REQUIRED BY ABSTRACT SUPERCLASS
	protected void checkAccessRights() throws AuthorizationException {
		if ( this.otDetail.getEmployee().getUserId() != this.userMB.getUserId() ) {
			Object[] params = { userMB.getFullName(), Long.valueOf(this.otDetail.getRequestId()) };
			String errMsg = ErrorMessages.getInstance().getMessage(ErrorMessages.OT_VIEW_UNAUTHORIZED, params);
			FacesContextUtil.getHttpSession().setAttribute("unauthMsg", errMsg);
			throw new AuthorizationException(errMsg);
		}
	}
}