package gov.gsa.ocfo.aloha.ejb.overtime.impl;

import gov.gsa.ocfo.aloha.ejb.overtime.OTIndivListEJB;
import gov.gsa.ocfo.aloha.exception.AlohaServerException;
import gov.gsa.ocfo.aloha.model.overtime.OTIndivRow;
import gov.gsa.ocfo.aloha.model.overtime.OTListRow;
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
public class OTIndivListEJBImpl implements OTIndivListEJB{
	@Resource(name = "aloha-ds")
	private DataSource dataSource;
	
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public List<OTListRow> retrieveOTIndivListForSupervisor(long supvUserId) throws AlohaServerException {
		return this.retrieveOTIndivList(SqlUtil.GET_OT_INDIV_LIST_FOR_SUPERVISOR_SQL, supvUserId);
	}
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public List<OTListRow> retrieveOTIndivListForSubmitOwn(long soUserId) throws AlohaServerException {
		return this.retrieveOTIndivList(SqlUtil.GET_OT_INDIV_LIST_FOR_SUBMIT_OWN_SQL, soUserId);
	}	
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public List<OTListRow> retrieveOTIndivListForOnBehalfOf(long oboUserId) throws AlohaServerException {
		return this.retrieveOTIndivList(SqlUtil.GET_OT_INDIV_LIST_FOR_ON_BEHALF_OF_SQL, oboUserId);
	}	
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	private List<OTListRow> retrieveOTIndivList(String sqlStmt, long id) throws AlohaServerException {
		Connection conn = null;
		PreparedStatement ps = null;
		try {
			conn = dataSource.getConnection();
			ps = conn.prepareStatement(sqlStmt);
			ps.setLong(1, id);
			return this.buildOTInividualList(ps.executeQuery());
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
	}		
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	private List<OTListRow> buildOTInividualList(ResultSet rs) throws SQLException {
		List<OTListRow> otIndivRowList = new ArrayList<OTListRow>();
		while (rs.next()) {
			OTIndivRow otIndivRow = new OTIndivRow	(	rs.getLong("HEADER_ID"), 
														rs.getLong("DETAIL_ID"),
														rs.getString("SALARY_GRADE_KEY"),													
														rs.getString("OT_TYPE_CODE"), 
														rs.getString("OT_TYPE_NAME"), 
														rs.getString("FUNDING_REQUIRED"),
														rs.getDate("PP_START_DATE"),
														rs.getDate("PP_END_DATE"),
														rs.getString("STATUS_CODE"), 
														rs.getString("STATUS_NAME"),
														rs.getString("EMPL_FIRST_NAME"),
														rs.getString("EMPL_MIDDLE_NAME"),
														rs.getString("EMPL_LAST_NAME"),
														rs.getString("SUBMITTER_FIRST_NAME"),
														rs.getString("SUBMITTER_MIDDLE_NAME"),
														rs.getString("SUBMITTER_LAST_NAME"), 
														rs.getString("SUPERVISOR_FIRST_NAME"),
														rs.getString("SUPERVISOR_MIDDLE_NAME"),
														rs.getString("SUPERVISOR_LAST_NAME"),
														null
													);
			otIndivRowList.add(otIndivRow);
		} 
		return otIndivRowList;
	}	
}