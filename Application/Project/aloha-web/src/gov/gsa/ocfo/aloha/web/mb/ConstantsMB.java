package gov.gsa.ocfo.aloha.web.mb;

import gov.gsa.ocfo.aloha.web.util.AlohaConstants;
import gov.gsa.ocfo.aloha.web.util.AlohaURIs;

import java.io.IOException;
import java.net.InetAddress;

import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;

@ManagedBean(name="constantsMB", eager=true)
@ApplicationScoped
public class ConstantsMB {

	private final String tabMyRequests = "tabMyRequests";
	private final String tabMyTeam = "tabMyTeam";
	private final String tabReports = "tabReports";	
	private final String tabManagement = "tabManagement";	
	private final String tabAdmin = "tabAdmin";
	private final String lastAlohaHomeTabVisited = "lastAlohaHomeTabVisited";
	private final String prd = "prd";
	private final String tst = "tst";
	private final String dev = "dev";
	public enum envType {dev,tst,prd};
	private envType env;
	private Boolean boolFalse = Boolean.FALSE;
	private Boolean boolTrue  = Boolean.TRUE;
	
	//private final String displayNone = "display:none;";
	//private final String displayBlock = "display:block;";
	
	private final String displayNone = "position:absolute;left:-999em;";
	private final String displayBlock = "left:auto;";
	
	private final String cssClassForSelectedHomeTab = "selected";
	private final String cssClassForNotSelectedHomeTab = "notSelected";
	
	private final String initOtSoCreate = "initOtSoCreate";
	private final String initOtOboCreate = "initOtOboCreate";
	
	public void ssoRedirect() {
		try {
			String ssoLogin;
			if (this.getEnv().equals(ConstantsMB.envType.dev)) {
				ssoLogin = AlohaURIs.SSO_LOGIN_DEV;
			} else if (this.getEnv().equals(ConstantsMB.envType.tst)) {
				ssoLogin = AlohaURIs.SSO_LOGIN_TST;
			} else {
				ssoLogin = AlohaURIs.SSO_LOGIN_PRD;
			}
			FacesContext.getCurrentInstance().getExternalContext().redirect(ssoLogin);
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public String getTabMyRequests() {
		return tabMyRequests;
	}
	public String getTabMyTeam() {
		return tabMyTeam;
	}
	public String getTabReports() {
		return tabReports;
	}
	public String getTabManagement() {
		return tabManagement;
	}
	public String getTabAdmin() {
		return tabAdmin;
	}
	public String getLastAlohaHomeTabVisited() {
		return lastAlohaHomeTabVisited;
	}
	public String getDisplayNone() {
		return displayNone;
	}
	public String getDisplayBlock() {
		return displayBlock;
	}
	public String getCssClassForSelectedHomeTab() {
		return cssClassForSelectedHomeTab;
	}
	public String getCssClassForNotSelectedHomeTab() {
		return cssClassForNotSelectedHomeTab;
	}
	public String getInitOtSoCreate() {
		return initOtSoCreate;
	}
	public String getInitOtOboCreate() {
		return initOtOboCreate;
	}
	public envType getEnv() {
		String hostName;
		env = envType.prd;
		try {
			HttpServletRequest req = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
			String baseURL = req.getRequestURL().toString().replace(req.getRequestURI(), "");
			//String baseURL = req.getRequestURI().toString();
			hostName = InetAddress.getLocalHost().getHostName().toLowerCase();
			//System.out.println("baseURL = " + baseURL + "\nHostName : "  + hostName);
			if (baseURL.contains("localhost") || baseURL.contains(envType.dev.toString()) || hostName.contains(envType.dev.toString())) {
				env = envType.dev;
			} else if (baseURL.contains(envType.tst.toString())|| hostName.contains(envType.tst.toString())) {
				env = envType.tst;
			} else if (baseURL.equals(envType.prd.toString())|| hostName.contains(envType.prd.toString())) {
				env = envType.prd;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return env;
	}
	public String getPrd() {
		return prd;
	}
	public String getTst() {
		return tst;
	}
	public String getDev() {
		return dev;
	}

	public Boolean getBoolFalse() {
		return boolFalse;
	}

	public Boolean getBoolTrue() {
		return boolTrue;
	}
	public String getOtPolicyAcknowledgedParamName() {
		return AlohaConstants.OT_POLICY_ACKNOWLEDGED;
	}
	public String getLrVarianceAcknowledgedParamName() {
		return AlohaConstants.LR_VARIANCE_ACKNOWLEDGED;
	}	
}
