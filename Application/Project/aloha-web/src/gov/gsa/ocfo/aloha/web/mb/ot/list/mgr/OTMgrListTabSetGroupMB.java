package gov.gsa.ocfo.aloha.web.mb.ot.list.mgr;

import java.io.Serializable;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.event.AbortProcessingException;

import com.icesoft.faces.component.paneltabset.TabChangeEvent;
import com.icesoft.faces.component.paneltabset.TabChangeListener;

@ManagedBean(name=OTMgrListTabSetGroupMB.MANAGED_BEAN_NAME)
@SessionScoped
public class OTMgrListTabSetGroupMB implements TabChangeListener, Serializable {
	private static final long serialVersionUID = 3353907812254207529L;

	public static final String MANAGED_BEAN_NAME = "otMgrListTabSetGroupMB";

	private int selectedIndex = 0;
	
    public void processTabChange(TabChangeEvent tabChangeEvent)
			throws AbortProcessingException {
    	//this.selectedIndex = tabChangeEvent.getNewTabIndex();
		//    	System.out.println(this.getClass().getSimpleName() + ".processTabChange(): BEGIN");
		//		System.out.println("-----------------------------------------------------------------------");
		//		System.out.println("this.selectedIndex: " + this.selectedIndex);		
		//		System.out.println("-----------------------------------------------------------------------");		
		//		System.out.println("oldTabIndex: " + tabChangeEvent.getOldTabIndex());
		//		System.out.println("newTabIndex: " + tabChangeEvent.getNewTabIndex());
		//		System.out.println("isAppropriateListener: " + tabChangeEvent.isAppropriateListener(this));
		//		System.out.println("-----------------------------------------------------------------------");		
		//		System.out.println("this.selectedIndex: " + this.selectedIndex);		
		//		System.out.println("-----------------------------------------------------------------------");		
		//		System.out.println(this.getClass().getSimpleName() + ".processTabChange(): END");			}
    }	
    public int getSelectedIndex() {
		return selectedIndex;
	}
	public void setSelectedIndex(int selectedIndex) {
		this.selectedIndex = selectedIndex;
	}
}