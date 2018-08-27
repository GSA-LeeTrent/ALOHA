package gov.gsa.ocfo.aloha.model;

import java.io.Serializable;
import java.util.Set;

public class FacilityCoordinator implements Serializable {
	private static final long serialVersionUID = 3300678306975767045L;
	private long facilityCoordinatorUserId;
	private Set<Facility> facilities;
	
	public FacilityCoordinator(long facCoordUserId, Set<Facility> setOfFacilities) {
		this.facilityCoordinatorUserId = facCoordUserId;
		this.facilities = setOfFacilities;
	}
	
	public long getFacilityCoordinatorUserId() {
		return facilityCoordinatorUserId;
	}

	public Set<Facility> getFacilities() {
		return facilities;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime
				* result
				+ (int) (facilityCoordinatorUserId ^ (facilityCoordinatorUserId >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FacilityCoordinator other = (FacilityCoordinator) obj;
		if (facilityCoordinatorUserId != other.facilityCoordinatorUserId)
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("FacilityCoordinator [facilityCoordinatorUserId=");
		builder.append(facilityCoordinatorUserId);
		builder.append("]");
		return builder.toString();
	}
}
