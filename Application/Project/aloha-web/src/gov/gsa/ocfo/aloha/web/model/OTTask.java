package gov.gsa.ocfo.aloha.web.model;

import java.io.Serializable;
import java.math.BigDecimal;

public class OTTask implements Serializable {
	private static final long serialVersionUID = 7813166564572603181L;
	private static final String ID_PREFIX = "otTask_";
	private static final String DESC_ID_PREFIX = "otTaskDesc_";
	private static final String EST_HRS_ID_PREFIX = "otEstHours_";
	private static final String DESC_ERROR_ID_PREFIX = "otTaskDescError_";
	private static final String EST_HRS_ERROR_ID_PREFIX = "otEstHoursError_";
	

	private int index;
	private String id;
	private String desc;
	private BigDecimal estHours;
	
	private String descId;
	private String estHoursId;
	
	private boolean renderable;
	private boolean descError;
	private boolean estHoursError;

	private String descErrorId;
	private String estHoursErrorId;

	private int errorRowNbr;
	
	public OTTask(int index, boolean render) {
		this.index = index;
		this.renderable = render;
		this.id = ID_PREFIX + this.index;		
		this.descId = DESC_ID_PREFIX + this.index;
		this.estHoursId = EST_HRS_ID_PREFIX + this.index;
		this.descErrorId = DESC_ERROR_ID_PREFIX + this.index;
		this.estHoursErrorId = EST_HRS_ERROR_ID_PREFIX + this.index;

		this.descError = false;
		this.estHoursError = false;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public BigDecimal getEstHours() {
		return estHours;
	}

	public void setEstHours(BigDecimal estHours) {
		this.estHours = estHours;
	}

	public boolean isRenderable() {
		return renderable;
	}

	public boolean isError() {
		return ((this.descError == true) || (this.estHoursError == true));
	}

	public boolean isFirstRow() {
		return (this.index == 1);
	}
	
	public void setRenderable(boolean renderable) {
		this.renderable = renderable;
	}

	public String getDescId() {
		//return DESC_ID_PREFIX + this.index;
		return descId;
	}

	public String getEstHoursId() {
		return estHoursId;
		//return EST_HRS_ID_PREFIX + this.index;
	}

	public void setDescId(String descId) {
		this.descId = descId;
	}

	public void setEstHoursId(String estHoursId) {
		this.estHoursId = estHoursId;
	}
	
	public String getDescErrorId() {
		return descErrorId;
	}

	public String getEstHoursErrorId() {
		return estHoursErrorId;
	}

	public int getErrorRowNbr() {
		return errorRowNbr;
	}

	public void setErrorRowNbr(int errorRowNbr) {
		this.errorRowNbr = errorRowNbr;
	}

	public boolean isDescError() {
		return descError;
	}

	public void setDescError(boolean descError) {
		this.descError = descError;
	}

	public boolean isEstHoursError() {
		return estHoursError;
	}

	public void setEstHoursError(boolean estHoursError) {
		this.estHoursError = estHoursError;
	}
}
