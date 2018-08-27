package gov.gsa.ocfo.aloha.ejb.impl;

import gov.gsa.ocfo.aloha.ejb.EventLoggerEJB;
import gov.gsa.ocfo.aloha.ejb.UserEJB;
import gov.gsa.ocfo.aloha.ejb.feddesk.DateTableEJB;
import gov.gsa.ocfo.aloha.exception.AlohaServerException;
import gov.gsa.ocfo.aloha.exception.AuthorizationException;
import gov.gsa.ocfo.aloha.exception.ValidationException;
import gov.gsa.ocfo.aloha.interceptor.validation.AuthorizeValidationInterceptor;
import gov.gsa.ocfo.aloha.model.entity.AlohaUser;
import gov.gsa.ocfo.aloha.model.entity.AlohaUserPref;
import gov.gsa.ocfo.aloha.model.entity.EtamsAdXref;
import gov.gsa.ocfo.aloha.model.entity.EtamsUser;
import gov.gsa.ocfo.aloha.model.entity.audit.EventLog;
import gov.gsa.ocfo.aloha.model.entity.audit.EventType;
import gov.gsa.ocfo.aloha.model.entity.audit.EventType.EventTypeValue;
import gov.gsa.ocfo.aloha.util.SqlUtil;
import gov.gsa.ocfo.aloha.util.StringUtil;
import gov.gsa.ocfo.aloha.util.UserPrefUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.interceptor.Interceptors;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.OptimisticLockException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.sql.DataSource;

@Stateless
public class UserEJBImpl implements UserEJB {

	@EJB
	DateTableEJB dateTableEJB;
	
	@EJB
	EventLoggerEJB eventLoggerEJB;
	
	private static final String ONE = "1";
	@PersistenceContext (unitName="aloha-pu")
	private EntityManager entityMgr;
	
	@Resource(name = "aloha-ds")
	private DataSource dataSource;

	@Interceptors({AuthorizeValidationInterceptor.class})
	public AlohaUser authorize(String loginName) throws AuthorizationException, ValidationException, AlohaServerException {
		AlohaUser user = null;
		try {
			user = this.getUser(loginName);
			if ( user == null) {
				throw new AuthorizationException("\"" + loginName + "\""  + " is unauthorized to use ALOHA. Please contact support.");
			}
			this.setRoles(user);
			
			/////////////////////////////////////
			// TEMPORARY
			/////////////////////////////////////
			user.setLrVarianceAcknowledged(true);
			//this.setLRVarianceFlag(user);
			////////////////////////////////////
			
			////////////////////////////////////
			// SET LEAVE RECON WIZARD FLAG
			////////////////////////////////////
			user.setShowLeaveReconWizard(false);
			//this.setLeaveReconWizardFlag(user);
			///////////////////////////////////
			
			this.writeToEventLog(user, EventType.EventTypeValue.USER_LOGIN);
		} catch (AuthorizationException ae) {
			throw ae;
		} catch (SQLException se) {
			se.printStackTrace();
			throw new AlohaServerException(se);
		} catch (Exception e) {
			e.printStackTrace();
			throw new AlohaServerException(e);
		}	
		return user;
	}	
	
	private AlohaUser getUser(String loginName) throws SQLException, Exception {
		AlohaUser user = null;

		Connection conn = null;
		PreparedStatement ps = null;

		try {
			conn = dataSource.getConnection();
			ps = conn.prepareStatement(SqlUtil.GET_USER_SQL);
			ps.setString(1, loginName.toUpperCase());
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				user = new AlohaUser();
				user.setLoginName(rs.getString("login_name"));
				user.setUserId(rs.getLong("user_id"));
				user.setFirstName(rs.getString("first_name"));
				user.setMiddleName(rs.getString("middle_name"));
				user.setLastName(rs.getString("last_name"));
				if ( (!StringUtil.isNullOrEmpty(user.getFirstName())) && (!StringUtil.isNullOrEmpty(user.getLastName()))) {
					user.setFullName(user.getFirstName() + " " + (user.getMiddleName()== null ? "" : user.getMiddleName().substring(0,1).toUpperCase())+ 
							" " + user.getLastName());
				}
				user.setEmailAddress(rs.getString("email_address"));

				if (rs.getLong("alohaAdminUserId") > 0)  {
					user.setAlohaAdmin(true);
				} else {
					user.setAlohaAdmin(false);
				}

				String awsInd = rs.getString("aws_ind");
				if ( (awsInd != null) && (awsInd.equals("Y")) ) {
					user.setAws(true);
				} else {
					user.setAws(false);
				}
			}
		} catch (SQLException sqle) {
			throw sqle;
		} catch (Exception e) {
			throw e;
		} finally {
			SqlUtil.closePreparedStatement(ps);
			SqlUtil.closeConnection(conn);
			ps = null;
			conn = null;
		}
		return user;
	}	
	
	public AlohaUser getUser(Long userid) throws SQLException {
		AlohaUser user = null;

		Connection conn = null;
		PreparedStatement ps = null;

		try {
			conn = dataSource.getConnection();
			ps = conn.prepareStatement(SqlUtil.GET_USER_BY_ID_SQL);
			ps.setLong(1, userid);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				user = new AlohaUser();
				user.setLoginName(rs.getString("login_name"));
				user.setUserId(rs.getLong("user_id"));
				user.setFirstName(rs.getString("first_name"));
				user.setMiddleName(rs.getString("middle_name"));
				user.setLastName(rs.getString("last_name"));
				if ( (!StringUtil.isNullOrEmpty(user.getFirstName())) && (!StringUtil.isNullOrEmpty(user.getLastName()))) {
					user.setFullName(user.getFirstName() + " " + (user.getMiddleName()== null ? "" : user.getMiddleName().substring(0,1).toUpperCase())+ 
							" " + user.getLastName());
				}
				user.setEmailAddress(rs.getString("email_address"));
				//sak adxref
				if (rs.getLong("alohaAdminUserId") > 0)  {
					user.setAlohaAdmin(true);
				} else {
					user.setAlohaAdmin(false);
				}

				String awsInd = rs.getString("aws_ind");
				if ( (awsInd != null) && (awsInd.equals("Y")) ) {
					user.setAws(true);
				} else {
					user.setAws(false);
				}
			}
		} catch (SQLException sqle) {
			throw sqle;
		} finally {
			SqlUtil.closePreparedStatement(ps);
			SqlUtil.closeConnection(conn);
			ps = null;
			conn = null;
		}
		return user;
	}	
	
	public EtamsUser getEtamsUser(Long userId) throws AlohaServerException{
		try {
			Query query = this.entityMgr.createNamedQuery(EtamsUser.QueryNames.GET_ETAMS_USER_BY_USERID);
			query.setParameter(EtamsUser.QueryParamNames.GET_ETAMS_USER_BY_USERID, userId);
			return (EtamsUser)query.getSingleResult();
		} catch (NoResultException nre) {
			return null;
		} catch (Exception e) {
			e.printStackTrace();
			throw new AlohaServerException("Exception encountered in " + this.getClass().getName() 
										+ ".getEtamsUser(String loginName): " + e.getMessage());
		}		
	}
	
	private void setRoles(AlohaUser user) throws SQLException, Exception {
		Connection conn = null;
		PreparedStatement ps = null;

		try {
			conn = dataSource.getConnection();
			
			ps = conn.prepareStatement(SqlUtil.GET_ROLES_SQL);
			ps.setLong(1, user.getUserId());
			ps.setLong(2, user.getUserId());
			ps.setLong(3, user.getUserId());
			ps.setLong(4, user.getUserId());
			
			ResultSet rs = ps.executeQuery();

			while (rs.next()) {
				String userRole = rs.getString("UserRole");
				String hasRole  = rs.getString("HasRole");	
				
				//System.out.println(userRole + " / " + hasRole);
				
				if ( hasRole.equals(ONE)) {
					if ( userRole.equals(AlohaUser.SUBMIT_OWN_ROLE_NAME) ) {
						user.setSubmitOwn(true);
					} else if ( userRole.equals(AlohaUser.APPROVER_ROLE_NAME) ) {
						user.setApprover(true);
					} else if ( userRole.equals(AlohaUser.ON_BEHALF_OF_ROLE_NAME) ) {
						user.setOnBehalfOf(true);
					} else if ( userRole.equals(AlohaUser.FACILITY_COORDINATOR_ROLE_NAME) ) {
						user.setFacilityCoordinator(true);
					}  
				}
			}
		} catch (SQLException sqle) {
			throw sqle;
		} catch (Exception e) {
			throw e;
		} finally {
			SqlUtil.closePreparedStatement(ps);
			SqlUtil.closeConnection(conn);
			ps = null;
			conn = null;
		}
	}
	
	/****************************************************************************************************
	private void setLRVarianceFlag(AlohaUser user) throws SQLException, AlohaServerException, Exception {
		Connection conn = null;
		PreparedStatement ps = null;

		try {
				DateTable dateTableForToday = this.dateTableEJB.retrieveDateTableForToday();
				int year = dateTableForToday.getYear();
				int payPeriod = dateTableForToday.getPayPeriod();
				
				// WE NEED TO FIND THE LATEST CLOSED PAY PERIOD
				// IF PAY PERIOD IN DATE TABLE FOR TODAY IS 1
				// WE NEED TO DECREMENT THE YEAR AND FIND THE 
				// MAX PAY PERIOD IN THE DECEREMENTED YEAR (could be 26 or 27)
				if ( payPeriod == 1 ) {
					year = (year - 1);
					payPeriod = this.dateTableEJB.retrieveMaxPayPeriodInDateTableForYear(year);
				} else {
					payPeriod = (payPeriod - 1);
				}
				
			conn = dataSource.getConnection();
			
			ps = conn.prepareStatement(SqlUtil.LR_VARIANCE_COUNT_SQL);
			
			ps.setInt(1, year); // CALANDER YEAR
			ps.setInt(2, payPeriod); // PAY PERIOD
			ps.setLong(3, user.getUserId()); // APPROVER_USER_ID
			ps.setLong(4, user.getUserId()); // SUBMITTER_USER_ID
			ps.setLong(5, user.getUserId()); // CERTIFIER_USER_ID
			ps.setLong(6, user.getUserId()); // TIMEKEEPER_USER_ID
			
			ResultSet rs = ps.executeQuery();

			if (rs.next()) {
				int lrVarianceCount = rs.getInt("LR_VARIANCE_COUNT");
				//System.out.println("lrVarianceCount: " + lrVarianceCount);
				
				if ( lrVarianceCount > 0) {
	
					// WE NEED THE DATE RANGE FOR THE OFFENDING PAY PERIOD
					DateTable startPP 	= this.dateTableEJB.retrieveDateTableForYearPayPeriodDay(year, payPeriod, 1);
					DateTable endPP 	= this.dateTableEJB.retrieveDateTableForYearPayPeriodDay(year, payPeriod, 14);
					
					// POPULATE LRVariancePayPeriod WITH YEAR, PAY PERIOD, START DATE, END DATE
					LRVariancePayPeriod lrVariancePayPeriod = new LRVariancePayPeriod(year, payPeriod, startPP.getCalendarDate(), endPP.getCalendarDate());
					
					// SET ALOHA USER PROPERLY - IT WILL BE USED DOWNSTREAM 
					// AND NEEDS THE LR VARIANCE PAY PERIOD INFO
					user.setLrVarianceAcknowledged(false);
					user.setLrVariancePayPeriod(lrVariancePayPeriod);
				} else {
					
					// SETTING THIS FLAG TO TRUE MEANS THAT 
					// NOTHING HAS TO BE DONE DOWNSTREAM
					user.setLrVarianceAcknowledged(true);
				}
			}
			
		} catch (SQLException sqle) {
			throw sqle;
		} catch (AlohaServerException ale) {
			throw ale;
		} catch (Exception e) {
			throw e;
		} finally {
			SqlUtil.closePreparedStatement(ps);
			SqlUtil.closeConnection(conn);
			ps = null;
			conn = null;
		}
	}	
	****************************************************************************************************/

	private void setLeaveReconWizardFlag(AlohaUser user) throws SQLException, AlohaServerException, Exception {
		Connection conn = null;
		PreparedStatement ps = null;

		try {
			conn = dataSource.getConnection();
			ps = conn.prepareStatement(SqlUtil.LEAVE_RECON_WIZARD_COUNT_SQL);
			ps.setLong(1, user.getUserId()); // EMPLOYEE_USER_ID

			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				int leaveReconWizardCount = rs.getInt("LEAVE_RECON_WIZARD_COUNT");
				System.out.println("leaveReconWizardCount: " + leaveReconWizardCount);
				if ( leaveReconWizardCount > 0) {
					user.setShowLeaveReconWizard(true);
				} else {
					user.setShowLeaveReconWizard(false);
				}
			}
		} catch (SQLException sqle) {
			throw sqle;
		} catch (Exception e) {
			throw e;
		} finally {
			SqlUtil.closePreparedStatement(ps);
			SqlUtil.closeConnection(conn);
			ps = null;
			conn = null;
		}
	}
	public AlohaUserPref getUserPref(Long userId) throws AlohaServerException{
		try {
			Query query = this.entityMgr.createNamedQuery(AlohaUserPref.QueryNames.FIND_BY_USERID);
			query.setParameter(AlohaUserPref.QueryParamNames.FIND_BY_USERID, userId);
			return (AlohaUserPref)query.getSingleResult();
		} catch (NoResultException nre) {
			return null;
		} catch (Exception e) {
			e.printStackTrace();
			throw new AlohaServerException("Exception encountered in " + this.getClass().getName() 
										+ ".getUserPref(String loginName): " + e.getMessage());
		}		
	}

//	@Override
//	public AlohaUserPref saveUserPref(AlohaUserPref aup) throws AlohaServerException {
//		try {
//			AlohaUserPref mergedAUP = new AlohaUserPref();
//			if (aup.getId() > 0) { 
//				//return merged entity as it contains the new VERSION value
//				mergedAUP = this.entityMgr.merge(aup);
//			} else {
//				//persist updates the object with VERSION value (=1)
//				this.entityMgr.persist(aup);
//				mergedAUP = aup;
//			}			
//			this.entityMgr.flush();
//			return mergedAUP;
//		} catch (OptimisticLockException e) {
//			e.printStackTrace();
//			throw new AlohaServerException(e, AlohaServerException.ExceptionType.OPTIMISTIC_LOCK);
//		} catch (Exception e) {
//			e.printStackTrace();
//			throw new AlohaServerException("Exception encountered in " + this.getClass().getName() 
//										+ ".saveUserPref(AlohaUserPref aup): " + e.getMessage());
//		}
//	}
	
	@Override
	public AlohaUserPref saveUserPref(AlohaUser changedBy, AlohaUserPref aup) throws AlohaServerException {
		try {
			
			AlohaUserPref mergedAUP = aup;
			
			if (aup.getId() > 0) { 
				
				AlohaUserPref oldUserPref = this.retrieveUserPreflByID(aup.getId());
				
				boolean defaultApproverHasChanged   = UserPrefUtil.defaultApproverHasChanged(oldUserPref, aup);
				boolean defaultTimekeeperHasChanged = UserPrefUtil.defaultTimekeeperHasChanged(oldUserPref, aup);
				
				if ( defaultApproverHasChanged || defaultTimekeeperHasChanged ) {
					
//					System.out.println("---------------------------------------------------------------------------------------------------");	
//					System.out.println("UPDAING AL_USER_PREF table");
//					System.out.println("---------------------------------------------------------------------------------------------------");						
//					System.out.println("oldUserPref: " + oldUserPref.toString());
//					System.out.println("---------------------------------------------------------------------------------------------------");	
//					System.out.println("newUserPref: " + aup.toString());	
//					System.out.println("---------------------------------------------------------------------------------------------------");
					
					///////////////////////////////////////////////////////////////////////////////////////////////////////////
					// WRITE TO EVENT LOG
					// Write to the EVENT LOG first. Otherwise, the "old" UserPref will be updated
					// with the new values if we write to the event log after the "merge" (table row update).
					// REASON: We are inside the "Persistence Context". It's a 'JPA' thing.
					///////////////////////////////////////////////////////////////////////////////////////////////////////////
					if ( defaultApproverHasChanged ) {
						this.writeToEventLog(changedBy, oldUserPref, aup, EventTypeValue.USER_PREF_CHANGED_DEFAULT_APPROVER);
					}
					
					if ( defaultTimekeeperHasChanged ) {
						this.writeToEventLog(changedBy, oldUserPref, aup, EventTypeValue.USER_PREF_CHANGED_PRIMARY_TIMEKEEPER);
					}	
					
					///////////////////////////////////////////////////////////////////////////////////////////////////////////
					// UPDATE TABLE ROW
					// (return merged entity as it contains the new VERSION value)
					//////////////////////////////////////////////////////////////////////////////////////////////////////////
					mergedAUP = this.entityMgr.merge(aup);	
					//////////////////////////////////////////////////////////////////////////////////////////////////////////				
				}
			} else {
				
//				System.out.println("---------------------------------------------------------------------------------------------------");	
//				System.out.println("INSERTING INTO AL_USER_PREF table");
//				System.out.println("---------------------------------------------------------------------------------------------------");	
//				System.out.println("newUserPref: " + aup.toString());	
//				System.out.println("---------------------------------------------------------------------------------------------------");				
				
				///////////////////////////////////////////////////////////////////////////////////////////////////////////
				// INSERT NEW TABLE ROW
				// (persist updates the object with VERSION value (=1))
				///////////////////////////////////////////////////////////////////////////////////////////////////////////
				this.entityMgr.persist(aup);
				mergedAUP = aup;
				///////////////////////////////////////////////////////////////////////////////////////////////////////////				
				
				///////////////////////////////////////////////////////////////////////////////////////////////////////////
				// WRITE TO EVENT LOG
				///////////////////////////////////////////////////////////////////////////////////////////////////////////
				this.writeToEventLog(changedBy, null, aup, EventTypeValue.USER_PREF_CREATED_NEW_RECORD);	
				///////////////////////////////////////////////////////////////////////////////////////////////////////////				
			}	
			
			this.entityMgr.flush();
			return mergedAUP;
			
		} catch (OptimisticLockException e) {
			e.printStackTrace();
			throw new AlohaServerException(e, AlohaServerException.ExceptionType.OPTIMISTIC_LOCK);
		} catch (Exception e) {
			e.printStackTrace();
			throw new AlohaServerException("Exception encountered in " + this.getClass().getName() 
										+ ".saveUserPref(AlohaUserPref aup): " + e.getMessage());
		}
	}	

	@SuppressWarnings("unchecked")
	@Override
	public List<EtamsAdXref> getEtamsAdXrefs(String loginName)
			throws AlohaServerException {
		try {
			Query query = this.entityMgr.createNamedQuery(EtamsAdXref.QueryNames.FIND_BY_LOGINNAME);
			query.setParameter(EtamsAdXref.QueryParamNames.FIND_BY_LOGINNAME, loginName);
			query.setMaxResults(50);
			//sak 20120118 without the hint below, EM will not refresh as values
			//were updated externally to EM using jdbc
			//query.setHint(QueryHints.REFRESH, HintValues.TRUE);
			query.setHint("eclipselink.refresh", "True");
			//query.setHint(QueryHints.REFRESH_CASCADE, CascadePolicy.CascadeAllParts);
			query.setHint("eclipselink.refresh.cascade", "CascadeAllParts");
			return query.getResultList();

		} catch (NoResultException nre) {
			return null;			
		} catch (Exception e) {
			e.printStackTrace();
			throw new AlohaServerException("Exception encountered in " + this.getClass().getName() 
										+ ".getEtamsAdXrefs(String loginName): " + e.getMessage());
		}
	}

	@Override
	public List<AlohaUser> getEtamsUserByName(String userName) throws AlohaServerException {
		List<AlohaUser> users = new ArrayList<AlohaUser>();

		Connection conn = null;
		PreparedStatement psUserDemographics = null;

		try {
			conn = dataSource.getConnection();
			psUserDemographics = conn.prepareStatement(SqlUtil.GET_ETAMS_USER_BY_NAME_SQL);
			psUserDemographics.setString(1, userName.toUpperCase() + "%");

			ResultSet rs = psUserDemographics.executeQuery();
			AlohaUser au = null;
			while (rs.next()) {
				au = new AlohaUser();
				au.setEmailAddress(rs.getString("email_address"));
				au.setFirstName(rs.getString("first_name"));
				au.setLastName(rs.getString("last_name"));
				au.setMiddleName(rs.getString("middle_name"));
				au.setUserId(rs.getLong("user_id"));
				users.add(au);
			}
		} catch (NoResultException nre) {
			return null;			
		} catch (Exception e) {
			e.printStackTrace();
			throw new AlohaServerException("Exception encountered in " + this.getClass().getName() 
										+ ".saveUserPref(AlohaUserPref aup): " + e.getMessage());
		} finally {
			SqlUtil.closePreparedStatement(psUserDemographics);
			SqlUtil.closeConnection(conn);
			psUserDemographics = null;
			conn = null;
			
		}
		return users;
	}


	@Override
	public EtamsAdXref getEtamsAdXref(String loginName)
			throws AlohaServerException {
		try {
			return this.entityMgr.find(gov.gsa.ocfo.aloha.model.entity.EtamsAdXref.class, loginName);
		} catch (NoResultException nre) {
			return null;			
		} catch (Exception e) {
			e.printStackTrace();
			throw new AlohaServerException("Exception encountered in " + this.getClass().getName() 
										+ ".getEtamsAdXref(String loginName): " + e.getMessage());
		}
	}


	@Override
	public AlohaUser findAlohaUser(String loginName) throws AlohaServerException {
		try {
			return this.getUser(loginName);
		} catch (SQLException se) {
			se.printStackTrace();
			throw new AlohaServerException("SQLException encountered in UserEJB.authorize(String loginName): " + se.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			throw new AlohaServerException("Exception encountered in UserEJB.authorize(String loginName): " + e.getMessage());
		}	
	}


	//original login name must contain the original Login Name of the EtamsAdXref to be updated (user could have changed the login name)
	//assume update if originalLoginName is supplied; insert, if originalLoginName is NOT provided
	//inEad contains the updated or new values
	@Override
	public EtamsAdXref saveEtamsAdXref(String originalLoginName, EtamsAdXref inEad, String emailAddress)
			throws AlohaServerException, IllegalArgumentException {
/*		save adxref
update?
  loginname updated?
    y: does loginname exist
      y: cannot update - loginname PK constraint violated
      n: userid updated?
         y: does userid exist?
            y: cannot update - userid unique constraint violated
            n: ok to update
         n: ok to update
    n: userid updated?
         y: does userid exist
            y: cannot update
            n: ok to update
         n: nothing to update
add?
  does loginname exist?
    y: cannot update
    n: userid exist?
       y: cannot update
       n: ok to add
		      */
	
		try { 
			if (inEad.getLoginName().isEmpty() || inEad.getUserId() <= 0) {
				throw new IllegalArgumentException("Mandatory fields missing", new Throwable("AlohaBadData"));
			}
			this.updateUserDemographicsEmail(inEad.getUserId(), emailAddress);
			if (originalLoginName == null || originalLoginName.isEmpty()) {
				//insert
				//em.find - unable to force refresh, switched to query
				Query qry = this.entityMgr.createNamedQuery(EtamsAdXref.QueryNames.FIND_BY_LOGINNAME);
				qry.setParameter(EtamsAdXref.QueryParamNames.FIND_BY_LOGINNAME, inEad.getLoginName().toUpperCase());
				//sak 20120118 without the hint below, EM will not refresh as values
				//were updated externally to EM using jdbc
				//qry.setHint(QueryHints.REFRESH, HintValues.TRUE);
				qry.setHint("eclipselink.refresh", "True");
				//qry.setHint(QueryHints.REFRESH_CASCADE, CascadePolicy.CascadeAllParts);
				qry.setHint("eclipselink.refresh.cascade", "CascadeAllParts");
				//EtamsAdXref newEad = null;
				try {
					qry.getSingleResult();
					//login name exists - cannot add - PK violation
					throw new IllegalArgumentException("Login Name already exists - cannot add", new Throwable("AlohaBadData"));
				} catch (NoResultException nre) {
					//login name does not exist - loginname ok - now check userid
					Query query = this.entityMgr.createNamedQuery(EtamsAdXref.QueryNames.GET_BY_USERID);
					query.setParameter(EtamsAdXref.QueryParamNames.GET_BY_USERID, inEad.getUserId());
					try {
						EtamsAdXref ead2 = (EtamsAdXref) query.getSingleResult();	
						//userid exists -- cannot add -- unique constraint violation
						throw new IllegalArgumentException("Userid in use for login name: " + ead2.getLoginName(), new Throwable("AlohaBadData") );
					} catch (NoResultException nre2) {
						//userid not in use...ok to add
						return insertEtamsAdXref(inEad);
					}					
				}
			} else {
				//update
				EtamsAdXref oldEad = this.entityMgr.find(inEad.getClass(), originalLoginName);
				if (originalLoginName.toUpperCase().equals(inEad.getLoginName().toUpperCase())) {
					//login name not changed
					if (oldEad.getUserId() == inEad.getUserId()) {
						// userid not changed so no data changed - nothing to update
						return inEad;
					} else {
						//userid changed - check if new userid exists
						Query query = this.entityMgr.createNamedQuery(EtamsAdXref.QueryNames.GET_BY_USERID);
						query.setParameter(EtamsAdXref.QueryParamNames.GET_BY_USERID, inEad.getUserId());
						try {
							EtamsAdXref ead2 = (EtamsAdXref) query.getSingleResult();	
							//userid exists -- cannot update -- unique constraint violation
							throw new IllegalArgumentException("Userid in use for login name: " + ead2.getLoginName(), new Throwable("AlohaBadData") );
						} catch (NoResultException nre) {
							//userid not in use...ok to update
							return updateEtamsAdXref(originalLoginName, inEad);
						}
					}
				} else {
					//login name changed
					EtamsAdXref newEad = this.entityMgr.find(inEad.getClass(), inEad.getLoginName());
					if (newEad == null) {
						//login name does not exist - loginname ok - now check userid
						if (oldEad.getUserId() == inEad.getUserId()) {
							// userid not changed ok to update
							return updateEtamsAdXref(originalLoginName, inEad);
						} else {
							//userid changed - check if new userid exists
							Query query = this.entityMgr.createNamedQuery(EtamsAdXref.QueryNames.GET_BY_USERID);
							query.setParameter(EtamsAdXref.QueryParamNames.GET_BY_USERID, inEad.getUserId());
							EtamsAdXref ead2 = (EtamsAdXref) query.getSingleResult();
							if (ead2 != null) {
								//userid exists -- cannot update -- unique constraint violation
								throw new IllegalArgumentException("Userid in use for login name: " + ead2.getLoginName(), new Throwable("AlohaBadData") );
							} else {
								//userid not in use...ok to update
								return updateEtamsAdXref(originalLoginName, inEad);
							}						
						}
					} else {
						//login name exists - cannot update - PK violation
						throw new IllegalArgumentException("Login Name already exists - cannot update", new Throwable("AlohaBadData"));
					}
				}
			}
		} catch (IllegalArgumentException iae) {
			throw iae;
		} catch (Exception e) {
			e.printStackTrace();
			throw new AlohaServerException("Exception encountered in " + this.getClass().getName() 
											+ ".saveEtamsAdXref(EtamsAdXref inEad): " + e.getMessage());				
		}
	}
	
	private EtamsAdXref updateEtamsAdXref(String originalLoginName, EtamsAdXref inEad) throws AlohaServerException {
		
		Connection conn = null;
		PreparedStatement psAdXref = null;

		try {
			
			conn = dataSource.getConnection();
			psAdXref = conn.prepareStatement(SqlUtil.UPDATE_ETAMS_AD_XREF_SQL);
			// bind variables
			psAdXref.setString(1, inEad.getLoginName().toUpperCase());
			psAdXref.setLong(2, inEad.getUserId());
			psAdXref.setString(3, originalLoginName.toUpperCase());
			int rowsUpdated = psAdXref.executeUpdate();
			
			if ( rowsUpdated > 0) {	
				//conn.commit();
				return this.entityMgr.find(inEad.getClass(), inEad.getLoginName().toUpperCase());
			}	
		}
		catch (Exception se) {
			throw new AlohaServerException("Exception encountered in " + this.getClass().getName() 
					+ ".updateEtamsAdXref(String originalLoginName, EtamsAdXref inEad): " + se.getMessage());
		}
		finally {
			if (psAdXref != null)
				try {
					psAdXref.close();
				} catch (SQLException e) {
					throw new AlohaServerException("Exception encountered in " + this.getClass().getName() 
							+ ".updateEtamsAdXref(String originalLoginName, EtamsAdXref inEad): " + e.getMessage());
				}
			if(conn !=null) {	
				try {
					conn.close();
				} catch (SQLException e) {
					throw new AlohaServerException("Exception encountered in " + this.getClass().getName() 
							+ ".updateEtamsAdXref(String originalLoginName, EtamsAdXref inEad): " + e.getMessage());
				}
		        conn = null;
			}
		}
		return null;	
	}

	private EtamsAdXref insertEtamsAdXref(EtamsAdXref inEad) throws AlohaServerException {
		Connection conn = null;
		PreparedStatement psAdXref = null;

		try {
			conn = dataSource.getConnection();
			psAdXref = conn.prepareStatement(SqlUtil.INSERT_ETAMS_AD_XREF_SQL);
			// bind variables
			psAdXref.setString(1, inEad.getLoginName().toUpperCase());
			psAdXref.setLong(2, inEad.getUserId());
			int rowsUpdated = psAdXref.executeUpdate();
			
			if ( rowsUpdated > 0) {	
				//conn.commit();
				return (this.entityMgr.find(inEad.getClass(), inEad.getLoginName().toUpperCase()));
			}
		}
		catch (Exception se) {
			throw new AlohaServerException("Exception encountered in " + this.getClass().getName() 
					+ ".insertEtamsAdXref(EtamsAdXref inEad): " + se.getMessage());
		}
		finally {
			if (psAdXref != null)
				try {
					psAdXref.close();
				} catch (SQLException e) {
					throw new AlohaServerException("Exception encountered in " + this.getClass().getName() 
							+ ".insertEtamsAdXref(EtamsAdXref inEad): " + e.getMessage());
				}
			if(conn !=null) {	
				try {
					conn.close();
				} catch (SQLException e) {
					throw new AlohaServerException("Exception encountered in " + this.getClass().getName() 
							+ ".insertEtamsAdXref(EtamsAdXref inEad): " + e.getMessage());
				}
		        conn = null;
			}
		}
		return null;	
	}
	
	private Integer updateUserDemographicsEmail(long userId, String emailAddress) throws AlohaServerException {
		
		Connection conn = null;
		PreparedStatement psUserDemographics = null;
		try {
			conn = dataSource.getConnection();
			//check if user email address has changed
			psUserDemographics = conn.prepareStatement(SqlUtil.GET_EMAIL_ADDR_BY_UID_SQL);
			psUserDemographics.setLong(1, userId);
			ResultSet rs = psUserDemographics.executeQuery();
			if (rs.next()) {
				if (rs.getString("email_address") != null) {
					if (rs.getString("email_address").equals(emailAddress)) return 0;
				}
			}
			psUserDemographics = conn.prepareStatement(SqlUtil.UPDATE_USER_DEMOGRAPHICS_SQL);
			psUserDemographics.setString(1, emailAddress);
			psUserDemographics.setLong(2, userId);
			
			int rowsUpdated = psUserDemographics.executeUpdate();
			
			if ( rowsUpdated > 0) {	
				//conn.commit();
			}	
			return rowsUpdated;
		}
		catch (Exception e) {
			throw new AlohaServerException("Exception encountered in " + this.getClass().getName() 
					+ ".updateUserDemographicsEmail(long userId, String emailAddress): " + e.getMessage());
		}
		finally {
			if (psUserDemographics != null)
				try {
					psUserDemographics.close();
				} catch (SQLException e1) {
					throw new AlohaServerException("Exception encountered in " + this.getClass().getName() 
							+ ".updateUserDemographicsEmail(long userId, String emailAddress): " + e1.getMessage());
				}
			if(conn !=null) {	
				try {
					conn.close();
				} catch (SQLException e) {
					throw new AlohaServerException("Exception encountered in " + this.getClass().getName() 
							+ ".updateUserDemographicsEmail(long userId, String emailAddress): " + e.getMessage());
				}
		        conn = null;
			}
		}	
	}


	@Override
	public Integer deleteEtamsAdXref(String loginName)
			throws AlohaServerException {
		Connection conn = null;
		PreparedStatement psAdXref = null;

		try {
			
			conn = dataSource.getConnection();
			psAdXref = conn.prepareStatement(SqlUtil.DELETE_ETAMS_AD_XREF_SQL);
			// bind variables
			psAdXref.setString(1, loginName.toUpperCase());
			int rowsUpdated = psAdXref.executeUpdate();
			
			if ( rowsUpdated > 0) {	
				//conn.commit();
				return 1;
			}	
		}
		catch (Exception se) {
			throw new AlohaServerException("Exception encountered in " + this.getClass().getName() 
					+ ".deleteEtamsAdXref(String loginName): " + se.getMessage());
		}
		finally {
			if (psAdXref != null)
				try {
					psAdXref.close();
				} catch (SQLException e) {
					throw new AlohaServerException("Exception encountered in " + this.getClass().getName() 
							+ ".deleteEtamsAdXref(String loginName): " + e.getMessage());
				}
			if(conn !=null) {	
				try {
					conn.close();
				} catch (SQLException e) {
					throw new AlohaServerException("Exception encountered in " + this.getClass().getName() 
							+ ".deleteEtamsAdXref(String loginName): " + e.getMessage());
				}
		        conn = null;
			}
		}
		return 0;	
	}
	
	public void writeToEventLog (AlohaUser alohaUser, EventType.EventTypeValue eventTypeValue) throws AlohaServerException {
		EventLog eventLog = new EventLog();
			
		EventType eventType = this.eventLoggerEJB.getEventType(eventTypeValue.toString()); 
		eventLog.setEventType(eventType);
		eventLog.setUserCreated(alohaUser.getLoginName());	
		String oldValue = alohaUser.toString();
		if ( oldValue.length() > 4000) {
			oldValue = oldValue.substring(0, 4000);	
		}
		eventLog.setOldValue(oldValue);
			
		this.eventLoggerEJB.logEvent(eventLog);
	}	
	public void writeToEventLog (AlohaUser changedBy, AlohaUserPref oldUserPref, AlohaUserPref newUserPref, EventType.EventTypeValue eventTypeValue) throws AlohaServerException {
		EventLog eventLog = new EventLog();
		
		// Set correct Event Type
		EventType eventType = this.eventLoggerEJB.getEventType(eventTypeValue.toString()); 
		System.out.println("---------------------------------------------------------------------------------------------------");
		System.out.println(eventType);
		System.out.println("---------------------------------------------------------------------------------------------------");
		eventLog.setEventType(eventType);
		
		// Set User who made this change
		eventLog.setUserCreated(changedBy.getLoginName());	
		
		//////////////////////////////////////////////////
		// OLD VALUES
		//------------------------------------------------
		// Set the previous values for comparison purposes
		// (truncate if necessary)		
		//------------------------------------------------
		if (oldUserPref != null) {
			String oldValue = oldUserPref.toString();
			if ( oldValue.length() > 4000) {
				oldValue = oldValue.substring(0, 4000);	
			}
			eventLog.setOldValue(oldValue);			
		}
		//////////////////////////////////////////////////		
		
		//////////////////////////////////////////////////
		// NEW VALUES
		//------------------------------------------------
		// Set the new values for comparison purposes
		// (truncate if necessary)		
		//------------------------------------------------
		String newValue = newUserPref.toString();
		if ( newValue.length() > 4000) {
			newValue = newValue.substring(0, 4000);	
		}
		eventLog.setNewValue(newValue);		
		//////////////////////////////////////////////////
			
		// Now we're ready to write to Event Log
		this.eventLoggerEJB.logEvent(eventLog);
	}	
	
	public AlohaUserPref retrieveUserPreflByID(long userPrefID) throws AlohaServerException {
		try {
			return this.entityMgr.find(AlohaUserPref.class, userPrefID);
		} catch (IllegalStateException ise) {
			ise.printStackTrace();
			throw new AlohaServerException(ise);
		} catch(IllegalArgumentException iae) {
			iae.printStackTrace();
			throw new AlohaServerException(iae);
		} catch (Exception e) {
			e.printStackTrace();
			throw new AlohaServerException(e);
		}		
	}		
}