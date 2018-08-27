package gov.gsa.ocfo.aloha.ejb.impl;

import gov.gsa.ocfo.aloha.ejb.PayPeriodEJB;
import gov.gsa.ocfo.aloha.exception.AlohaServerException;
import gov.gsa.ocfo.aloha.model.PayPeriod;
import gov.gsa.ocfo.aloha.util.SqlUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.sql.DataSource;

@Stateless
public class PayPeriodEJBImpl implements PayPeriodEJB {
	@Resource(name = "aloha-ds")
	private DataSource dataSource;

	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public List<Integer> getPayPeriodYears() throws AlohaServerException {
		List<Integer> list = new ArrayList<Integer>();
		
		Connection conn = null;
		PreparedStatement ps = null;
		
		try {
			conn = dataSource.getConnection();
			ps = conn.prepareStatement(SqlUtil.GET_PAY_PERIODS_YEARS_SQL);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				list.add(Integer.valueOf(rs.getInt("pay_period_year")));
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
		return list;
	}
	
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public Map<Integer, List<PayPeriod>> getAllPayPeriods() throws AlohaServerException {
		Map<Integer, List<PayPeriod>> allPayPeriods = new TreeMap<Integer, List<PayPeriod>>();
		
		Connection conn = null;
		PreparedStatement ps = null;

		try {
			conn = dataSource.getConnection();
			ps = conn.prepareStatement(SqlUtil.GET_ALL_PAY_PERIODS_SQL);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				PayPeriod payPeriod = new PayPeriod();
				payPeriod.setYear(rs.getInt("year"));
				payPeriod.setPayPeriod(rs.getInt("pay_period"));
				payPeriod.setFromDate(rs.getDate("from_date"));
				payPeriod.setToDate(rs.getDate("to_date"));		
				
				// setValue:
				SimpleDateFormat valueDateFormat = new SimpleDateFormat(PayPeriod.VALUE_FORMAT);
				GregorianCalendar ppStartDate = new GregorianCalendar();
				ppStartDate.setTime(payPeriod.getFromDate());
				payPeriod.setValue(valueDateFormat.format(ppStartDate.getTime()));
				
				// setLabel:
				SimpleDateFormat labelDateFormat = new SimpleDateFormat(PayPeriod.LABEL_FORMAT);
				StringBuilder sb = new StringBuilder();
				sb.append("[");
				if ( payPeriod.getPayPeriod() < 10 ) {sb.append("0");}
				sb.append(payPeriod.getPayPeriod());
				sb.append("]: ");
				sb.append(labelDateFormat.format(payPeriod.getFromDate()));
				sb.append(" - ");
				sb.append(labelDateFormat.format(payPeriod.getToDate()));
				payPeriod.setLabel(sb.toString());
				
				List<PayPeriod> yearList = allPayPeriods.get(payPeriod.getYear());
				if ( yearList == null) {
					yearList = new ArrayList<PayPeriod>();
					allPayPeriods.put(payPeriod.getYear(), yearList);
				}
				yearList.add(payPeriod);
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
		return allPayPeriods;
	}	
	
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public Map<Integer, List<PayPeriod>> getLimitedPayPeriods() throws AlohaServerException {
		Map<Integer, List<PayPeriod>> allPayPeriods = new TreeMap<Integer, List<PayPeriod>>();
		
		Connection conn = null;
		PreparedStatement ps = null;

		try {
			conn = dataSource.getConnection();
			ps = conn.prepareStatement(SqlUtil.GET_LIMITED_PAY_PERIODS_SQL);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				PayPeriod payPeriod = new PayPeriod();
				payPeriod.setYear(rs.getInt("year"));
				payPeriod.setPayPeriod(rs.getInt("pay_period"));
				payPeriod.setFromDate(rs.getDate("from_date"));
				payPeriod.setToDate(rs.getDate("to_date"));		
				
				// setValue:
				SimpleDateFormat valueDateFormat = new SimpleDateFormat(PayPeriod.VALUE_FORMAT);
				GregorianCalendar ppStartDate = new GregorianCalendar();
				ppStartDate.setTime(payPeriod.getFromDate());
				payPeriod.setValue(valueDateFormat.format(ppStartDate.getTime()));
				
				// setLabel:
				SimpleDateFormat labelDateFormat = new SimpleDateFormat(PayPeriod.LABEL_FORMAT);
				StringBuilder sb = new StringBuilder();
				sb.append("[");
				if ( payPeriod.getPayPeriod() < 10 ) {sb.append("0");}
				sb.append(payPeriod.getPayPeriod());
				sb.append("]: ");
				sb.append(labelDateFormat.format(payPeriod.getFromDate()));
				sb.append(" - ");
				sb.append(labelDateFormat.format(payPeriod.getToDate()));
				payPeriod.setLabel(sb.toString());
				
				List<PayPeriod> yearList = allPayPeriods.get(payPeriod.getYear());
				if ( yearList == null) {
					yearList = new ArrayList<PayPeriod>();
					allPayPeriods.put(payPeriod.getYear(), yearList);
				}
				yearList.add(payPeriod);
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
		return allPayPeriods;
	}	
	
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public List<PayPeriod> getPayPeriodsForYear(int year) throws AlohaServerException {
		List<PayPeriod> payPeriodsForYear = new ArrayList<PayPeriod>();

		Connection conn = null;
		PreparedStatement ps = null;
		
		try {
			conn = dataSource.getConnection();
			ps = conn.prepareStatement(SqlUtil.GET_PAY_PERIODS_FOR_YEAR_SQL);
			ps.setInt(1, year);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				PayPeriod payPeriod = new PayPeriod();
				payPeriod.setPayPeriod(rs.getInt("pay_period"));
				payPeriod.setFromDate(rs.getDate("from_date"));
				payPeriod.setToDate(rs.getDate("to_date"));				
				
				payPeriodsForYear.add(payPeriod);
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
		return payPeriodsForYear;
	}	
}