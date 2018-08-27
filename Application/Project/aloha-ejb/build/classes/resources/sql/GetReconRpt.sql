SELECT  eeId, last_name, first_name, middle_name,  
YYYY, PP, leaveDt, leaveType, FATCode,
sum(ETAMS_LeaveHrs) leaveHrsEtams, sum(ALOHA_LeaveHrs) leaveHrsAloha, listagg(nvl2(LRHID, LRHID || '-', LRHID)  || LRDSeq, ',') within group (order by lrhid, LRDSeq) lrheaderid
FROM
(   
     select ud.user_id eeid, (dt.calendar_date - 14 + ead.dayx) LeaveDt, 
           ead.work_code LeaveType, 
           SUM(decode(substr(work_start, 4, 1), 'H', to_number(substr(work_start, 1, 3))/10, to_number(to_date(work_stop, 'hh24mi') - to_date(work_start, 'hh24mi'))*24)) ETAMS_LeaveHrs, to_number(null) ALOHA_LeaveHrs,
           dt.YEARX YYYY, dt.PAY_PERIOD PP,  
           first_name, last_name, middle_name, to_number(null) LRHID, to_number(null) LRDID, to_number(null) LRDSeq,
           ALT.FACILITY_CODE || '/' || ALT.AREA_CODE || '/' || ALT.TEAM_CODE FATCode
    from  usr_feddesk_required.epd_audit ea
    inner join (select max(ea2.process_date) maxeaprocessdt, EA2.PAY_PERIOD, ea2.yearx, ea2.ssn from usr_feddesk_required.epd_audit ea2 
            group by ea2.ssn, ea2.yearx, ea2.pay_period)eamax on EA.PROCESS_DATE = eamax.maxeaprocessdt
            and eamax.pay_period = ea.pay_period and eamax.yearx = ea.yearx and eamax.ssn = ea.ssn,
    usr_feddesk_required.epd_audit_dtl ead, 
            usr_feddesk_required.etams_user_profile eup inner join usr_feddesk_required.user_demographics ud on eup.user_id = UD.USER_ID
            inner join USR_FEDDESK_REQUIRED.ALL_TEAMS alt on ALT.TEAM_ID = EUP.MEMBER_OF_TEAM,
            (select to_number(substr(ly.tbl_pp_ly_pp_no_yr, 1, 2)) pay_period, to_number(substr(ly.tbl_pp_ly_pp_no_yr, 3, 6)) yearx, cy.calendar_date, LY.TBL_PP_BEG_DATE 
            from USR_FEDDESK_REQUIRED.par_pay_periods_mv ly,  
            USR_FEDDESK_REQUIRED.date_table cy 
            where 
            CY.CALENDAR_DATE between LY.TBL_PP_BEG_DATE and LY.TBL_PP_END_DATE
            and LY.TBL_PP_BEG_DATE  between to_date(:x_from_pp_dt, 'yyyymmdd') and to_date(:x_to_pp_dt, 'yyyymmdd')
            and LY.TBL_PP_END_DATE between to_date(:x_from_pp_dt, 'yyyymmdd') and to_date(:x_to_pp_dt, 'yyyymmdd')
            and dayx = 14) dt             
    where 
      ea.ssn = ud.ssn 
      and ea.agency_code = 'GS' 
     and ea.seq_no = ead.seq_no
     and ead.work_code in (:x_pri_leave_type , :x_sec_leave_type  )
     -- 2012-10-10 JJM 48969 Ensure ETAMS Results are only Returned for LeaveTypes that are effective
     --        Note this will only work for primary leave codes without a secondary leave code
    and ead.work_code in
     (
           select primary_code cons_work_cd from aloha.lr_type where
start_date  <= (dt.calendar_date - 14 + ead.dayx) and end_date >= (dt.calendar_date - 14 + ead.dayx)
           union
           select secondary_code cons_work_cd from aloha.lr_type where
start_date  <= (dt.calendar_date - 14 + ead.dayx) and end_date >= (dt.calendar_date - 14 + ead.dayx)
      )
     --
     and ea.pp_start_date = dt.TBL_PP_BEG_DATE
     and eup.member_of_team <> 999
            and eup.cost_center_id <> 999
            and eup.PAY_STAT IN ('1','2','4','6','A')
            and eup.delete_code = 'E' 
            and eup.GSA_PAYROLL = 'Y'
            and ud.user_id in (:x_emp_id )
     group by ud.user_id, dt.calendar_date,ead.dayx, 
           ead.work_code , 
           dt.YEARX , dt.PAY_PERIOD ,  
           first_name, last_name, middle_name, 
           ALT.FACILITY_CODE || '/' || ALT.AREA_CODE || '/' || ALT.TEAM_CODE 
UNION 
    select LRH.EMPLOYEE_USER_ID eeid, LRI.LEAVE_DATE leavedt, LRT.PRIMARY_CODE leavetype,  
    to_number(null) ETAMS_LeaveHrs, SUM(LRI.LEAVE_HOURS) ALOHA_LeaveHrs, 
    dt.yearx YYYY, dt.pay_period PP, 
    first_name, last_name, middle_name, lrh.LR_HEADER_ID LRHID, LRD.LR_DETAIL_ID LRDID, LRD.LR_DETAIL_SEQUENCE LRDSeq,
    ALT.FACILITY_CODE || '/' || ALT.AREA_CODE || '/' || ALT.TEAM_CODE FATCode
    from ALOHA.LR_HEADER lrh 
        inner join usr_feddesk_required.etams_user_profile eup on lrh.employee_user_id = eup.user_id 
        inner join usr_feddesk_required.user_demographics ud on eup.user_id = UD.USER_ID
        inner join USR_FEDDESK_REQUIRED.ALL_TEAMS alt on ALT.TEAM_ID = EUP.MEMBER_OF_TEAM
        inner join ALOHA.LR_DETAIL lrd on LRD.LR_HEADER_ID = LRH.LR_HEADER_ID
        inner join ALOHA.LR_ITEM lri on LRI.LR_DETAIL_ID = lrd.LR_DETAIL_ID
        inner join ALOHA.LR_TYPE lrt on LRT.LR_TYPE_ID = LRI.LR_TYPE_ID
        inner join
          (select to_number(substr(ly.tbl_pp_ly_pp_no_yr, 1, 2)) pay_period, to_number(substr(ly.tbl_pp_ly_pp_no_yr, 3, 6)) yearx, cy.calendar_date 
            from USR_FEDDESK_REQUIRED.par_pay_periods_mv ly,  
            USR_FEDDESK_REQUIRED.date_table cy 
            where 
            CY.CALENDAR_DATE between LY.TBL_PP_BEG_DATE and LY.TBL_PP_END_DATE
            and LY.TBL_PP_BEG_DATE  between to_date(:x_from_pp_dt, 'yyyymmdd') and to_date(:x_to_pp_dt, 'yyyymmdd')
            and LY.TBL_PP_END_DATE between to_date(:x_from_pp_dt, 'yyyymmdd') and to_date(:x_to_pp_dt, 'yyyymmdd')) dt
        on dt.calendar_date = LRI.LEAVE_DATE     
    where 
    LRD.LR_STATUS_ID = 5 and
    LRT.PRIMARY_CODE in (:x_pri_leave_type )
    and LRH.EMPLOYEE_USER_ID IN ( :x_emp_id )  
            and eup.member_of_team <> 999
            and eup.cost_center_id <> 999
            and eup.PAY_STAT IN ('1','2','4','6','A')
            and eup.delete_code = 'E' 
            and eup.GSA_PAYROLL = 'Y'
    group by LRH.EMPLOYEE_USER_ID, LRI.LEAVE_DATE, LRT.PRIMARY_CODE, dt.yearx, dt.pay_period, 
    first_name, last_name, middle_name, lrh.LR_HEADER_ID, LRD.LR_DETAIL_ID, LRD.LR_DETAIL_SEQUENCE,
    ALT.FACILITY_CODE || '/' || ALT.AREA_CODE || '/' || ALT.TEAM_CODE 
union 
    select LRH.EMPLOYEE_USER_ID eeid, LRI.LEAVE_DATE leavedt, LRT.SECONDARY_CODE leaveType,
    to_number(null) ETAMS_LeaveHrs, SUM(LRI.LEAVE_HOURS) ALOHA_LeaveHrs, 
    dt.yearx YYYY, dt.pay_period PP, 
    first_name, last_name, middle_name, lrh.LR_HEADER_ID LRHID, LRD.LR_DETAIL_ID LRDID, LRD.LR_DETAIL_SEQUENCE LRDSeq,
    ALT.FACILITY_CODE || '/' || ALT.AREA_CODE || '/' || ALT.TEAM_CODE FATCode
    from ALOHA.LR_HEADER lrh 
        inner join usr_feddesk_required.etams_user_profile eup on lrh.employee_user_id = eup.user_id
        inner join USR_FEDDESK_REQUIRED.ALL_TEAMS alt on ALT.TEAM_ID = EUP.MEMBER_OF_TEAM
        inner join usr_feddesk_required.user_demographics ud on eup.user_id = UD.USER_ID
        inner join ALOHA.LR_DETAIL lrd on LRD.LR_HEADER_ID = LRH.LR_HEADER_ID
        inner join ALOHA.LR_ITEM lri on LRI.LR_DETAIL_ID = lrd.LR_DETAIL_ID
        inner join ALOHA.LR_TYPE lrt on LRT.LR_TYPE_ID = LRI.LR_TYPE_ID
        inner join
          (select to_number(substr(ly.tbl_pp_ly_pp_no_yr, 1, 2)) pay_period, to_number(substr(ly.tbl_pp_ly_pp_no_yr, 3, 6)) yearx, cy.calendar_date 
            from USR_FEDDESK_REQUIRED.par_pay_periods_mv ly,  
            USR_FEDDESK_REQUIRED.date_table cy 
            where 
            CY.CALENDAR_DATE between LY.TBL_PP_BEG_DATE and LY.TBL_PP_END_DATE
            and LY.TBL_PP_BEG_DATE  between to_date(:x_from_pp_dt, 'yyyymmdd') and to_date(:x_to_pp_dt, 'yyyymmdd')
            and LY.TBL_PP_END_DATE between to_date(:x_from_pp_dt, 'yyyymmdd') and to_date(:x_to_pp_dt, 'yyyymmdd')) dt
        on dt.calendar_date = LRI.LEAVE_DATE     
    where 
     LRD.LR_STATUS_ID = 5 
     and
     LRT.secondary_code in (:x_sec_leave_type )
    and LRH.EMPLOYEE_USER_ID IN ( :x_emp_id )  
            and eup.member_of_team <> 999
            and eup.cost_center_id <> 999
            and eup.PAY_STAT IN ('1','2','4','6','A')
            and eup.delete_code = 'E' 
            and eup.GSA_PAYROLL = 'Y'  
    group by LRH.EMPLOYEE_USER_ID, LRI.LEAVE_DATE, LRT.SECONDARY_CODE, dt.yearx, dt.pay_period, 
    first_name, last_name, middle_name, lrh.LR_HEADER_ID, LRD.LR_DETAIL_ID, LRD.LR_DETAIL_SEQUENCE,
    ALT.FACILITY_CODE || '/' || ALT.AREA_CODE || '/' || ALT.TEAM_CODE 
)
GROUP BY eeid, first_name, last_name, middle_name, YYYY, PP, leavedt, leavetype, FATCode
ORDER BY last_name, first_name, leavedt, leavetype
