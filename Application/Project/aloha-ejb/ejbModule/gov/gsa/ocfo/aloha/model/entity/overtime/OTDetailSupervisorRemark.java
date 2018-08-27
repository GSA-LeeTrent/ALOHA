package gov.gsa.ocfo.aloha.model.entity.overtime;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue(value="S")
public class OTDetailSupervisorRemark extends OTDetailRemark {
	private static final long serialVersionUID = 2491164195176069804L;

}
