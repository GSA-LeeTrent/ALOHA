SELECT  eeId, 
        last_name, 
        first_name, 
        middle_name,  
        YYYY, 
        PP, 
        plan_grade,
        typex||' - '||ot1.type_name type,
        listagg(otDt, ', ') within group (order by OtDt) otDate,
        sum(ETAMS_otHrs) otHrsEtams, 
        sum(ALOHA_otHrs) otHrsAloha, FATCode,
        listagg(NVL(app_lname,' '),',')within group (order by app_lname) app_lname,
        listagg(NVL(app_fname,' '),',')within group (order by app_fname) app_fname,
        listagg(NVL(app_mname,' '),',')within group (order by app_mname) app_mname
FROM
(select ud.user_id eeid, 
        TO_CHAR((dt.calendar_date - 14 + ead.dayx),'MM/DD/YYYY')  otDt, 
        decode(substr(work_start, 4, 1), 
        'H', 
        to_number(substr(work_start, 1, 3))/10, 
        SUM(to_number(to_date(work_stop, 'hh24mi') - to_date(work_start, 'hh24mi'))*24)) ETAMS_otHrs, 
        to_number(null) ALOHA_otHrs,
        eb.pay_plan||grade plan_grade,
        work_code typex,
        dt.yearx YYYY, 
        dt.pay_period PP,  
        first_name, 
        last_name, 
        middle_name, 
        to_number(null) OHID,
        ALT.FACILITY_CODE || '/' || ALT.AREA_CODE || '/' || ALT.TEAM_CODE FATCode,
        null odid,
        null app_lname,
        null app_fname,
        null app_mname
   from usr_feddesk_required.epd_audit ea, 
        usr_feddesk_required.epd_audit_dtl ead, 
        usr_feddesk_required.etams_user_profile eup inner join usr_feddesk_required.user_demographics ud on eup.user_id = UD.USER_ID
        inner join USR_FEDDESK_REQUIRED.ALL_TEAMS alt on ALT.TEAM_ID = EUP.MEMBER_OF_TEAM,
        usr_feddesk_required.par_eds_basic1 eb,
        (select calendar_date, yearx, pay_period 
           from usr_feddesk_required.date_table 
          where calendar_date >=
                   (select calendar_date 
                      from date_table 
                     where yearx = :x_from_year  
                       and pay_period = :x_from_pp and dayx = 1)
            and calendar_date <=
                   (select calendar_date 
                      from date_table 
                     where yearx = :x_to_year  
                       and pay_period = :x_to_pp 
                       and dayx = 14)
            and dayx = 14) dt  
   where ea.ssn = ud.ssn 
     and ud.ssn = eb.ssn
     and ea.seq_no = ead.seq_no
     and ea.yearx = dt.yearx
     and EA.PAY_PERIOD = DT.PAY_PERIOD
     and eup.member_of_team <> 999
     and eup.cost_center_id <> 999
     and eup.PAY_STAT IN ('1','2','4','6','A')
     and eup.delete_code = 'E' 
     and eup.GSA_PAYROLL = 'Y'
     and ud.user_id in (:x_emp_id )
     and work_code IN (:x_overtime_type) 
     and ea.process_date = (select MAX(ea2.process_date) from usr_feddesk_required.epd_audit ea2
                                                              where ea2.ssn = ea.ssn
                                                                and ea2.yearx = ea.yearx 
                                                                and ea2.pay_period = EA.PAY_PERIOD)
  group by ud.user_id, 
        TO_CHAR((dt.calendar_date - 14 + ead.dayx),'MM/DD/YYYY'), 
        work_code ,
        dt.yearx , 
        dt.pay_period ,  
        first_name, 
        last_name, 
        middle_name, work_start, eb.pay_plan||grade, 
        ALT.FACILITY_CODE || '/' || ALT.AREA_CODE || '/' || ALT.TEAM_CODE
UNION 
    select OH.EMPL_USER_ID eeid, 
           null otdt, 
           to_number(null) ETAMS_otHrs, 
           SUM(OI.EST_HOURS) ALOHA_otHrs, 
           pay_plan||grade plan_grade,
           type_code type,
           dt.yearx YYYY, 
           dt.pay_period PP, 
           ud.first_name, ud.last_name, ud.middle_name, od.HEADER_ID OHID,
           ALT.FACILITY_CODE || '/' || ALT.AREA_CODE || '/' || ALT.TEAM_CODE FATCode,
           oi.detail_id odid,
           app.last_name app_lname,
           app.first_name app_fname,
           app.middle_name app_mname
      from ALOHA.OT_HEADER oh 
           inner join usr_feddesk_required.etams_user_profile eup on oh.empl_user_id = eup.user_id 
           inner join USR_FEDDESK_REQUIRED.ALL_TEAMS alt on ALT.TEAM_ID = EUP.MEMBER_OF_TEAM
           inner join usr_feddesk_required.user_demographics ud on eup.user_id = UD.USER_ID
           inner join ALOHA.OT_DETAIL od on OD.HEADER_ID = OH.HEADER_ID
           inner join ALOHA.OT_ITEM oi on OI.DETAIL_ID = od.DETAIL_ID
           inner join ALOHA.OT_PAY_PERIOD op on op.pay_period_key = oh.pay_period_key
           INNER JOIN usr_feddesk_required.par_eds_basic1 eb ON eb.ssn = ud.ssn
           inner join aloha.ot_type ot on oh.type_id = ot.type_id 
           inner join (select calendar_date, yearx, pay_period 
                         from usr_feddesk_required.date_table 
                        where calendar_date >=
                               (select calendar_date 
                                  from date_table 
                                 where yearx = :x_from_year  
                                   and pay_period = :x_from_pp and dayx = 1)
                          and calendar_date <=
                               (select calendar_date 
                                  from date_table 
                                 where yearx = :x_to_year  
                                    and pay_period = :x_to_pp 
                                    and dayx = 14)) dt
                  on dt.calendar_date = Op.END_DATE 
           INNER JOIN
              (SELECT first_name, last_name, middle_name, detail_id 
                 FROM aloha.ot_detail,
                      usr_feddesk_required.etams_user_profile eup,
                      usr_feddesk_required.user_demographics ud
                WHERE supv_user_id = eup.user_id
                  AND eup.user_id = ud.user_id)  app
               ON app.detail_id = od.detail_id
    where OD.STATUS_ID = 4 
      and OH.EMPL_USER_ID IN ( :x_emp_id )
      and ot.type_code IN (:x_overtime_type)  
      and eup.member_of_team <> 999
      and eup.cost_center_id <> 999
      and eup.PAY_STAT IN ('1','2','4','6','A')
      and eup.delete_code = 'E' 
      and eup.GSA_PAYROLL = 'Y'
      and od.date_last_updated = (select MAX(date_last_updated) from aloha.ot_detail od2
                                   where od2.header_id = od.header_id)
    group by OH.EMPL_USER_ID, 
           pay_plan||grade,
           type_code,
           dt.yearx, 
           dt.pay_period, 
           ud.first_name, ud.last_name, ud.middle_name, od.header_ID,
           ALT.FACILITY_CODE || '/' || ALT.AREA_CODE || '/' || ALT.TEAM_CODE,
           oi.detail_id, app.last_name,
           app.first_name, app.middle_name), aloha.ot_type ot1
WHERE typex IN (:x_overtime_type) 
AND plan_grade IN (:x_plan_grade)
AND type_code IN typex
GROUP BY eeid, first_name, last_name, middle_name, YYYY, PP,  plan_grade, typex||' - '||ot1.type_name, 
         ot1.type_name, FATCode
ORDER BY last_name, first_name