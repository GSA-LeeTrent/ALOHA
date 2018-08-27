SELECT oh.empl_user_id eeid,
       ud.last_name emp_lname, 
       ud.first_name emp_fname,
       ud.middle_name emp_mname,
       pay_plan||grade plan_grade,
       dt.yearx yyyy,
       dt.pay_period pp,
       TO_CHAR(op.end_date,'MM/DD/YYYY') otdt,
       stat.status_name otstatus,
       ot.type_code||' - '||ot.type_name type,
       task_desc,
       SUM(oi.est_hours) aloha_othrs,
       od.header_id ohid,
       ALT.FACILITY_CODE || '/' || ALT.AREA_CODE || '/' || ALT.TEAM_CODE FATCode,
       app.last_name app_lname,
       app.first_name app_fname,
       app.middle_name app_mname,
       od.detail_id odid
  FROM aloha.ot_header oh 
           INNER JOIN usr_feddesk_required.etams_user_profile eup ON oh.empl_user_id = eup.user_id 
           inner join USR_FEDDESK_REQUIRED.ALL_TEAMS alt on ALT.TEAM_ID = EUP.MEMBER_OF_TEAM
           INNER JOIN usr_feddesk_required.user_demographics ud ON eup.user_id = ud.user_id
           INNER JOIN aloha.ot_detail od ON od.header_id = oh.header_id
           INNER JOIN aloha.ot_item oi ON oi.detail_id = od.detail_id
           INNER JOIN aloha.ot_status os ON od.status_id = os.status_id
           INNER JOIN aloha.ot_pay_period op ON op.pay_period_key = oh.pay_period_key
           INNER JOIN usr_feddesk_required.par_eds_basic1 eb ON eb.ssn = ud.ssn
           INNER JOIN ALOHA.ot_type ot ON oh.type_id = ot.type_id
           INNER JOIN
                (SELECT calendar_date, yearx, pay_period 
                   FROM usr_feddesk_required.date_table 
                  WHERE calendar_date >=
                         (SELECT calendar_date 
                            FROM date_table 
                           WHERE yearx = :x_from_year 
                             AND pay_period = :x_from_pp 
                             AND dayx = 1)
                    AND calendar_date <=
                          (SELECT calendar_date 
                             FROM date_table 
                            WHERE yearx = :x_to_year 
                              AND pay_period = :x_to_pp 
                              AND dayx = 14)) dt
                ON dt.calendar_date = op.end_date  
           INNER JOIN
              (SELECT UNIQUE first_name, last_name, middle_name, header_id 
                 FROM aloha.ot_detail,
                      usr_feddesk_required.etams_user_profile eup,
                      usr_feddesk_required.user_demographics ud
                WHERE supv_user_id = eup.user_id
                  AND eup.user_id = ud.user_id)  app
               ON app.header_id = oh.header_id
           INNER JOIN 
             (SELECT status_name, header_id
                FROM aloha.ot_detail otd, aloha.ot_status ots
               WHERE otd.status_id = ots.status_id
                 AND detail_seq = (SELECT max(detail_seq)
                                     FROM aloha.ot_detail
                                    WHERE header_id = otd.header_id)) stat  
              ON stat.header_id = od.header_id
 WHERE od.status_id IN (:x_ot_status ) 
   AND oh.empl_user_id IN ( :x_emp_id ) 
   AND pay_plan||grade IN ( :x_plan_grade)
   AND ot.type_code IN (:x_overtime_type)
   AND eup.member_of_team <> 999
   AND eup.cost_center_id <> 999
   AND eup.pay_stat IN ('1','2','4','6','A')
   AND eup.delete_code = 'E' 
   AND eup.gsa_payroll = 'Y'
   AND od.detail_id = (SELECT MAX(detail_id) 
                         FROM aloha.ot_detail od2
                        WHERE od2.header_id IN (select oh2.header_id from aloha.ot_header oh2
                                                 where oh2.header_id = OH.HEADER_ID))
GROUP BY oh.empl_user_id, ud.first_name, ud.last_name, 
         ud.middle_name, pay_plan||grade, dt.yearx, 
         dt.pay_period, op.end_date, stat.status_name, 
         task_desc, od.header_id, ot.type_name, ot.type_code,
         op.start_date, ALT.FACILITY_CODE || '/' || ALT.AREA_CODE || '/' || ALT.TEAM_CODE,
         app.last_name, app.first_name, app.middle_name, od.detail_id
ORDER BY ud.last_name, ud.first_name, otdt, ohid