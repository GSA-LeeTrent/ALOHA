package gov.gsa.ocfo.aloha.web.listener;

import javax.faces.context.FacesContext;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;
import javax.servlet.http.HttpServletResponse;

public class CacheControlPhaseListener implements PhaseListener {
	private static final long serialVersionUID = 2065927137298348250L;

	public PhaseId getPhaseId() {
        return PhaseId.RENDER_RESPONSE;
    }

    public void afterPhase(PhaseEvent event)  {
    }

    public void beforePhase(PhaseEvent event)  {
        FacesContext facesContext = event.getFacesContext();
        HttpServletResponse response = (HttpServletResponse) facesContext.getExternalContext().getResponse();
        /*
        response.addHeader("Pragma", "no-cache");
        response.addHeader("Cache-Control", "no-cache");
        response.addHeader("Cache-Control", "no-store");
        response.addHeader("Cache-Control", "must-revalidate");
        response.addHeader("Expires", "Mon, 26 Dec 2011 10:00:00 GMT");
        */
        response.setHeader("Cache-Control", "no-cache"); // Prevents HTTP 1.1 caching.     
        response.setHeader("Pragma", "no-cache"); // Prevents HTTP 1.0 caching.  
        response.setDateHeader("Expires", -1); // Prevents proxy caching. 
    }

}
