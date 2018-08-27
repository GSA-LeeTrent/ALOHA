package gov.gsa.ocfo.aloha.web.mb.ot.list.mgr;

import java.io.Serializable;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.event.AbortProcessingException;

import com.icesoft.faces.component.paneltabset.TabChangeEvent;
import com.icesoft.faces.component.paneltabset.TabChangeListener;

@ManagedBean(name=OTMgrListTabSetMB.MANAGED_BEAN_NAME)
@SessionScoped
public class OTMgrListTabSetMB implements TabChangeListener, Serializable {
	private static final long serialVersionUID = 4191762449620014133L;
	public static final String MANAGED_BEAN_NAME = "otMgrListTabSetMB";

	private int selectedIndex = 0;
	
    public void processTabChange(TabChangeEvent tabChangeEvent)
			throws AbortProcessingException {		
	}
	
    public int getSelectedIndex() {
		return selectedIndex;
	}
	public void setSelectedIndex(int selectedIndex) {
		this.selectedIndex = selectedIndex;
	}
	
}
