package gov.gsa.ocfo.aloha.model.entity.overtime;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue(value="B")
public class OTGroupSubmitterRemark extends OTGroupRemark {
	private static final long serialVersionUID = 4318385061159248504L;


}
