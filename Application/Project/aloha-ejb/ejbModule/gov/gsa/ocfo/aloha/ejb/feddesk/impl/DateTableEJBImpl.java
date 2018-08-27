package gov.gsa.ocfo.aloha.ejb.feddesk.impl;

import gov.gsa.ocfo.aloha.ejb.feddesk.DateTableEJB;
import gov.gsa.ocfo.aloha.exception.AlohaServerException;
import gov.gsa.ocfo.aloha.model.feddesk.DateTable;
import gov.gsa.ocfo.aloha.util.SqlUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.sql.DataSource;

@Stateless
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class DateTableEJBImpl implements DateTableEJB {
	
	@Resource(name = "aloha-ds")
	private DataSource dataSource;

	@Override
	public DateTable retrieveDateTableForToday() throws AlohaServerException {

		DateTable dateTable = null;
		Connection conn = null;
		PreparedStatement ps = null;
		
		try {
			
			conn = dataSource.getConnection();
			ps = conn.prepareStatement(SqlUtil.DATE_TABLE_ROW_FOR_TODAY_SQL);
			
			ResultSet rs = ps.executeQuery();
			dateTable = this.mapFromResultSet(rs);
			
			return dateTable;
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

	@Override
	public DateTable retrieveDateTableForYearPayPeriodDay(int year,
			int payPeriod, int day) throws AlohaServerException {

		DateTable dateTable = null;
		Connection conn = null;
		PreparedStatement ps = null;
		
		try {
			
			conn = dataSource.getConnection();
			
			ps = conn.prepareStatement(SqlUtil.DATE_TABLE_ROW_FOR_YEAR_PAY_PERIOD_DAY_SQl);
			ps.setInt(1, year);
			ps.setInt(2, payPeriod);
			ps.setInt(3, day);
			
			ResultSet rs = ps.executeQuery();
			dateTable = this.mapFromResultSet(rs);
			
			return dateTable;
			
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

	@Override
	public Integer retrieveMaxPayPeriodInDateTableForYear(int year) throws AlohaServerException {
	
	Integer maxPayPeriodForYear = null;
	
	Connection conn = null;
	PreparedStatement ps = null;
	
	try {
		
		conn = dataSource.getConnection();
		
		ps = conn.prepareStatement(SqlUtil.MAX_PAY_PERIOD_IN_DATE_TABLE_FOR_YEAR_SQL);
		ps.setInt(1, year);

		
		ResultSet rs = ps.executeQuery();
		
		if ( rs != null && rs.next() ) {
			maxPayPeriodForYear = rs.getInt("MAX_PAY_PERIOD");
		}	
		
		return maxPayPeriodForYear;
		
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
	
	private DateTable mapFromResultSet(ResultSet rs) throws SQLException {
		
		DateTable dateTable = null;
		
		if ( rs != null && rs.next() ) {
			
			int year 			= rs.getInt("YEARX");
			int payPeriod		= rs.getInt("PAY_PERIOD");
			int day 			= rs.getInt("DAYX");
			Date calendarDate 	= rs.getDate("CALENDAR_DATE");
			
			dateTable = new DateTable(year, payPeriod, day, calendarDate);
		}
		
		return dateTable;
	}

	@Override
	public DateTable retrieveDateTableForDate(Date anyDate) throws AlohaServerException {

		DateTable dateTable = null;
		Connection conn = null;
		PreparedStatement ps = null;
		
		try {
			
			conn = dataSource.getConnection();
			ps = conn.prepareStatement(SqlUtil.DATE_TABLE_ROW_FOR_DATE_SQL);
			ps.setDate(1, new java.sql.Date(anyDate.getTime()));
			ResultSet rs = ps.executeQuery();
			dateTable = this.mapFromResultSet(rs);
			
			return dateTable;
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

}
