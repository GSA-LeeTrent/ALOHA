--2012-01-23: Changed to use LR HDR Id instead of LR DTL ID; Added LRHID to select list
--2012-06-12: Added back FAT
--2012-08-09: Added LRD Seq Number
--2012-08-24: Change to use LY instead of CY, added start time
SELECT  eeId, last_name, first_name, middle_name,  
YYYY, PP, leaveDt, leaveType, secleavetype, leaveStatus, sum(ALOHA_LeaveHrs) leaveHrsAloha, LRDID, LRHID, FATCode, LRDSeq, leaveStartTime
FROM
( 
    select LRH.LR_HEADER_ID LRHID, LRD.LR_DETAIL_ID LRDID, LRD.LR_DETAIL_SEQUENCE LRDSeq, LRH.EMPLOYEE_USER_ID eeid, LRI.LEAVE_DATE leavedt, LRT.PRIMARY_CODE || ' - ' || LRT.PRIMARY_CODE_DESC leavetype, 
    LRT.SECONDARY_CODE || ' - ' || LRT.SECONDARY_CODE_DESC secleavetype, 
    to_number(null) ETAMS_LeaveHrs, LRI.LEAVE_HOURS ALOHA_LeaveHrs, LRI.START_TIME leaveStartTime,
    dt.yearx YYYY, dt.pay_period PP, 
    first_name, last_name, middle_name, LRS.label leaveStatus,
    ALT.FACILITY_CODE || '/' || ALT.AREA_CODE || '/' || ALT.TEAM_CODE FATCode
    from ALOHA.LR_HEADER lrh 
        inner join usr_feddesk_required.etams_user_profile eup on lrh.employee_user_id = eup.user_id 
        inner join usr_feddesk_required.user_demographics ud on eup.user_id = UD.USER_ID
        inner join ALOHA.LR_DETAIL lrd on LRD.LR_HEADER_ID = LRH.LR_HEADER_ID
        inner join ALOHA.LR_ITEM lri on LRI.LR_DETAIL_ID = lrd.LR_DETAIL_ID
        inner join ALOHA.LR_TYPE lrt on LRT.LR_TYPE_ID = LRI.LR_TYPE_ID
        inner join ALOHA.LR_STATUS lrs on LRD.LR_STATUS_ID = LRS.LR_STATUS_ID
        inner join USR_FEDDESK_REQUIRED.ALL_TEAMS alt on ALT.TEAM_ID = EUP.MEMBER_OF_TEAM
        inner join
          (select substr(ly.tbl_pp_ly_pp_no_yr, 1, 2) pay_period, substr(ly.tbl_pp_ly_pp_no_yr, 3, 6) yearx, cy.calendar_date 
            from USR_FEDDESK_REQUIRED.par_pay_periods_mv ly,  
            USR_FEDDESK_REQUIRED.date_table cy 
            where 
            CY.CALENDAR_DATE between LY.TBL_PP_BEG_DATE and LY.TBL_PP_END_DATE
            and LY.TBL_PP_BEG_DATE  between to_date(:x_from_pp_dt, 'yyyymmdd') and to_date(:x_to_pp_dt, 'yyyymmdd')
            and LY.TBL_PP_END_DATE between to_date(:x_from_pp_dt, 'yyyymmdd') and to_date(:x_to_pp_dt, 'yyyymmdd')) dt
        on dt.calendar_date = LRI.LEAVE_DATE     
    where LRD.LR_STATUS_ID in (:x_leave_status ) 
    and LRT.PRIMARY_CODE in (:x_primary_leave_code )
    and LRT.SECONDARY_CODE is null  
     and LRH.EMPLOYEE_USER_ID IN ( :x_emp_id )  
            and eup.member_of_team <> 999
            and eup.cost_center_id <> 999
            and eup.PAY_STAT IN ('1','2','4','6','A')
            and eup.delete_code = 'E' 
            and eup.GSA_PAYROLL = 'Y'
union 
    select LRH.LR_HEADER_ID LRHID, LRD.LR_DETAIL_ID LRDID, LRD.LR_DETAIL_SEQUENCE LRDSeq, LRH.EMPLOYEE_USER_ID eeid, LRI.LEAVE_DATE leavedt, LRT.PRIMARY_CODE || ' - ' || LRT.PRIMARY_CODE_DESC leavetype, 
    LRT.SECONDARY_CODE || ' - ' || LRT.SECONDARY_CODE_DESC secleavetype, 
    to_number(null) ETAMS_LeaveHrs, LRI.LEAVE_HOURS ALOHA_LeaveHrs, LRI.START_TIME leaveStartTime, 
    dt.yearx YYYY, dt.pay_period PP, 
    first_name, last_name, middle_name, LRS.label leaveStatus,
    ALT.FACILITY_CODE || '/' || ALT.AREA_CODE || '/' || ALT.TEAM_CODE FATCode
    from ALOHA.LR_HEADER lrh 
        inner join usr_feddesk_required.etams_user_profile eup on lrh.employee_user_id = eup.user_id 
        inner join usr_feddesk_required.user_demographics ud on eup.user_id = UD.USER_ID
        inner join ALOHA.LR_DETAIL lrd on LRD.LR_HEADER_ID = LRH.LR_HEADER_ID
        inner join ALOHA.LR_ITEM lri on LRI.LR_DETAIL_ID = lrd.LR_DETAIL_ID
        inner join ALOHA.LR_TYPE lrt on LRT.LR_TYPE_ID = LRI.LR_TYPE_ID
        inner join ALOHA.LR_STATUS lrs on LRD.LR_STATUS_ID = LRS.LR_STATUS_ID
        inner join USR_FEDDESK_REQUIRED.ALL_TEAMS alt on ALT.TEAM_ID = EUP.MEMBER_OF_TEAM
        inner join
          (select substr(ly.tbl_pp_ly_pp_no_yr, 1, 2) pay_period, substr(ly.tbl_pp_ly_pp_no_yr, 3, 6) yearx, cy.calendar_date 
            from USR_FEDDESK_REQUIRED.par_pay_periods_mv ly,  
            USR_FEDDESK_REQUIRED.date_table cy 
            where 
            CY.CALENDAR_DATE between LY.TBL_PP_BEG_DATE and LY.TBL_PP_END_DATE
            and LY.TBL_PP_BEG_DATE  between to_date(:x_from_pp_dt, 'yyyymmdd') and to_date(:x_to_pp_dt, 'yyyymmdd')
            and LY.TBL_PP_END_DATE between to_date(:x_from_pp_dt, 'yyyymmdd') and to_date(:x_to_pp_dt, 'yyyymmdd')) dt
        on dt.calendar_date = LRI.LEAVE_DATE     
    where LRD.LR_STATUS_ID in (:x_leave_status ) 
    and LRT.SECONDARY_CODE in (:x_secondary_leave_code )
     and LRH.EMPLOYEE_USER_ID IN ( :x_emp_id )  
            and eup.member_of_team <> 999
            and eup.cost_center_id <> 999
            and eup.PAY_STAT IN ('1','2','4','6','A')
            and eup.delete_code = 'E' 
            and eup.GSA_PAYROLL = 'Y'            
)
GROUP BY eeid, first_name, last_name, middle_name, YYYY, PP, leavedt, leavetype, secleavetype, leaveStatus, leaveStartTime, LRDID, LRHID, FATCode, LRDSeq
ORDER BY last_name, first_name, LRHID, LRDID, LRDSeq, leavedt, leavetype