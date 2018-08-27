package gov.gsa.ocfo.aloha.model.entity.overtime;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue(value="B")
public class OTDetailSubmitterRemark extends OTDetailRemark {
	private static final long serialVersionUID = -6432366368593907321L;
}
