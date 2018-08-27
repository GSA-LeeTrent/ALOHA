package gov.gsa.ocfo.aloha.ejb.impl;

import gov.gsa.ocfo.aloha.ejb.FacilityCoordinatorEJB;
import gov.gsa.ocfo.aloha.exception.AlohaNotFoundException;
import gov.gsa.ocfo.aloha.exception.AlohaServerException;
import gov.gsa.ocfo.aloha.model.Facility;
import gov.gsa.ocfo.aloha.model.FacilityCoordinator;
import gov.gsa.ocfo.aloha.util.SqlUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.sql.DataSource;

@Stateless
public class FacilityCoordinatorEJBImpl implements FacilityCoordinatorEJB {
	
	@Resource(name = "aloha-ds")
	private DataSource dataSource;

	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public FacilityCoordinator retrieveById(long userId)
			throws AlohaNotFoundException, AlohaServerException {

		FacilityCoordinator facilityCoordinator = null;
		Set<Facility> facilities = new HashSet<Facility>();

		Connection conn = null;
		PreparedStatement ps = null;

		try {
			conn = dataSource.getConnection();
			ps = conn.prepareStatement(SqlUtil.FIND_FACILITIES_FOR_FACILITY_COORDINATOR_SQL);
			ps.setLong(1, userId);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {

				long facilityId = rs.getLong("facility_id");
				Facility facility = new Facility(Long.valueOf(facilityId).toString(),
						rs.getString("Facility_code"),
						rs.getString("facility_description"));
				facilities.add(facility);
			}
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new AlohaServerException(sqle);
		} catch (Exception e) {
			e.printStackTrace();
			throw new AlohaServerException(e);
		} finally {
			SqlUtil.closePreparedStatement(ps);
			SqlUtil.closeConnection(conn);
			ps = null;
			conn = null;
		}

		if (facilities.isEmpty()) {
			throw new AlohaNotFoundException(
					"Facility Coordinator not found for the following USER ID: "
							+ userId);
		} else {
			facilityCoordinator = new FacilityCoordinator(userId, facilities);
		}

		return facilityCoordinator;
	}
}
