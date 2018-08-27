package gov.gsa.ocfo.aloha.web.handler;

import gov.gsa.ocfo.aloha.web.util.AlohaURIs;

import java.util.Iterator;

import javax.faces.FacesException;
import javax.faces.application.Application;
import javax.faces.application.ViewExpiredException;
import javax.faces.application.ViewHandler;
import javax.faces.component.UIViewRoot;
import javax.faces.context.ExceptionHandler;
import javax.faces.context.ExceptionHandlerWrapper;
import javax.faces.context.FacesContext;
import javax.faces.event.ExceptionQueuedEvent;
import javax.faces.event.ExceptionQueuedEventContext;
 
public class ViewExpiredExceptionHandler extends ExceptionHandlerWrapper {
 
    private ExceptionHandler wrapped;
 
    public ViewExpiredExceptionHandler(ExceptionHandler wrapped) {
        this.wrapped = wrapped;
    }
 
    @Override
    public ExceptionHandler getWrapped() {
        return this.wrapped;
    }
 
    @Override
    public void handle() throws FacesException {
      for (Iterator<ExceptionQueuedEvent> i = getUnhandledExceptionQueuedEvents().iterator(); i.hasNext();) {
            ExceptionQueuedEvent event = i.next();
            ExceptionQueuedEventContext context = (ExceptionQueuedEventContext) event.getSource();
            Throwable t = context.getException();
            if ( (t instanceof ViewExpiredException) 
            		|| (t instanceof IllegalArgumentException) ) {
                FacesContext facesContext = FacesContext.getCurrentInstance();
                try {
    				Application app = facesContext.getApplication();
    				ViewHandler viewHandler = app.getViewHandler();
    				UIViewRoot view = viewHandler.createView(facesContext, AlohaURIs.HOME);
    				facesContext.setViewRoot(view);
    				facesContext.renderResponse();
    				viewHandler.renderView(facesContext, view);
    				facesContext.responseComplete();
                } catch(Throwable thr) {
                	thr.printStackTrace();
                } finally {
                	i.remove();
                }
            }
            if (t instanceof org.icefaces.application.SessionExpiredException) {
               i.remove();
            }
        }
        // At this point, the queue will not contain any ViewExpiredEvents.
        // Therefore, let the parent handle them.
        getWrapped().handle();
    }
}