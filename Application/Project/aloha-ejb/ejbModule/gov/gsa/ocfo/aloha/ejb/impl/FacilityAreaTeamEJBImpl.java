package gov.gsa.ocfo.aloha.ejb.impl;

import gov.gsa.ocfo.aloha.ejb.FacilityAreaTeamEJB;
import gov.gsa.ocfo.aloha.exception.AlohaServerException;
import gov.gsa.ocfo.aloha.model.FacilityAreaTeam;
import gov.gsa.ocfo.aloha.util.SqlUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.sql.DataSource;

@Stateless
public class FacilityAreaTeamEJBImpl implements FacilityAreaTeamEJB {
	@Resource(name = "aloha-ds")
	private DataSource dataSource;
	//sak
	
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public List<FacilityAreaTeam> getTeamsManagedByUser(long employeeId) throws AlohaServerException {
		List<FacilityAreaTeam> teamsManagedByUser = new ArrayList<FacilityAreaTeam>();
		
		Connection conn = null;
		PreparedStatement ps = null;
		
		try {
			conn = dataSource.getConnection();
			String sqlStmt = SqlUtil.GET_TEAMS_MANAGED_BY_USER_SQL;
			//sqlStmt = sqlStmt.replaceAll(":x_etams_userid", Long.toString(employeeId));
			
			ps = conn.prepareStatement(sqlStmt.replaceAll(":x_etams_userid", Long.toString(employeeId)));
			
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				FacilityAreaTeam facilityAreaTeam = new FacilityAreaTeam();
				facilityAreaTeam.setAreaId(rs.getLong("area_id"));
				facilityAreaTeam.setAreaCd(rs.getString("area_code"));
				facilityAreaTeam.setAreaDesc(rs.getString("area_description"));
				facilityAreaTeam.setAreaText(rs.getString("area_text"));
				facilityAreaTeam.setFacilityId(rs.getLong("facility_id"));
				facilityAreaTeam.setFacilityCd(rs.getString("facility_code"));
				facilityAreaTeam.setFacilityDesc(rs.getString("facility_description"));
				facilityAreaTeam.setFacilityText(rs.getString("facility_text"));
				facilityAreaTeam.setTeamId(rs.getLong("team_id"));
				facilityAreaTeam.setTeamCd(rs.getString("team_code"));
				facilityAreaTeam.setTeamDesc(rs.getString("team_description"));
				facilityAreaTeam.setTeamText(rs.getString("team_text"));
				
				teamsManagedByUser.add(facilityAreaTeam);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			throw new AlohaServerException(e);
		} catch (Exception e) {
			e.printStackTrace();
			throw new AlohaServerException(e);
		} finally {
			SqlUtil.closePreparedStatement(ps);
			SqlUtil.closeConnection(conn);
			ps = null;
			conn = null;
		}			
		return teamsManagedByUser;
	}
}