package gov.gsa.ocfo.aloha.model;

import java.io.Serializable;

public class FacilityAreaTeam implements Serializable {
	private static final long serialVersionUID = -6887881099324756406L;
	
	private long facilityId;
	private String facilityCd;
	private String facilityDesc;
	private String facilityText;
	private long areaId;
	private String areaCd;
	private String areaDesc;
	private String areaText;
	private long teamId;
	private String teamCd;
	private String teamDesc;
	private String teamText;
	
	public String getFacilityCd() {
		return facilityCd;
	}
	public void setFacilityCd(String facilityCd) {
		this.facilityCd = facilityCd;
	}
	public String getFacilityDesc() {
		return facilityDesc;
	}
	public void setFacilityDesc(String facilityDesc) {
		this.facilityDesc = facilityDesc;
	}
	public String getFacilityText() {
		return facilityText;
	}
	public void setFacilityText(String facilityText) {
		this.facilityText = facilityText;
	}
	public String getAreaCd() {
		return areaCd;
	}
	public void setAreaCd(String areaCd) {
		this.areaCd = areaCd;
	}
	public String getAreaDesc() {
		return areaDesc;
	}
	public void setAreaDesc(String areaDesc) {
		this.areaDesc = areaDesc;
	}
	public String getAreaText() {
		return areaText;
	}
	public void setAreaText(String areaText) {
		this.areaText = areaText;
	}
	public String getTeamCd() {
		return teamCd;
	}
	public void setTeamCd(String teamCd) {
		this.teamCd = teamCd;
	}
	public String getTeamDesc() {
		return teamDesc;
	}
	public void setTeamDesc(String teamDesc) {
		this.teamDesc = teamDesc;
	}
	public String getTeamText() {
		return teamText;
	}
	public void setTeamText(String teamText) {
		this.teamText = teamText;
	}
	public void setFacilityId(long facilityId) {
		this.facilityId = facilityId;
	}
	public long getFacilityId() {
		return facilityId;
	}
	public void setAreaId(long areaId) {
		this.areaId = areaId;
	}
	public long getAreaId() {
		return areaId;
	}
	public void setTeamId(long teamId) {
		this.teamId = teamId;
	}
	public long getTeamId() {
		return teamId;
	}
	
}
