SELECT FacilityCd, FacilityDesc, AreaCd, AreaDesc, TeamCd, TeamDesc, eeid, last_name, first_name, middle_name,  YYYY, PP, leavedt, leavetype, leavehrs, 
    count(ETAMS_Count) ETAMS, count(ALOHA_Count) ALOHA 
FROM
(    select 1 ETAMS_Count, to_number(null) ALOHA_Count, team.etams_uid eeid, (dt.calendar_date - 14 + ead.dayx) LeaveDt, 
           ead.work_code || ' - ' || ead.work_code_desc LeaveType, 
           decode(substr(work_start, 4, 1), 'H', to_number(substr(work_start, 1, 3))/10, to_number(to_date(work_stop, 'hh24mi') - to_date(work_start, 'hh24mi'))*24) LeaveHrs,
           dt.yearx YYYY, dt.pay_period PP, FacilityCd, FacilityDesc, AreaCd, AreaDesc, TeamCd, TeamDesc, first_name, last_name, middle_name      
    from  usr_feddesk_required.epd_audit ea, usr_feddesk_required.epd_audit_dtl ead, 
          (select calendar_date, yearx, pay_period 
            from usr_feddesk_required.date_table 
            where calendar_date >=
            (select calendar_date from date_table where yearx = :x_from_year and pay_period = :x_from_pp and dayx = 1)
            and calendar_date <=
            (select calendar_date from date_table where yearx = :x_to_year and pay_period = :x_to_pp and dayx = 14)
            and dayx = 14) dt,
            (select eup.user_id etams_uid, ssn etams_ssn, first_name, last_name, middle_name, 
            ti.team_description TeamDesc, ai.area_description AreaDesc, fi.facility_description FacilityDesc,
            ti.team_code TeamCd, ai.area_code AreaCd, fi.facility_code FacilityCd
            from usr_feddesk_required.etams_user_profile eup inner join usr_feddesk_required.user_demographics ud on eup.user_id = UD.USER_ID
            inner join usr_feddesk_required.team_info ti on ti.team_id = eup.member_of_team
            inner join usr_feddesk_required.area_info ai on ai.area_id = ti.area_id
            inner join usr_feddesk_required.facility_info fi on fi.facility_id = ai.facility_id
            where EUP.MEMBER_OF_TEAM = :x_teamid) team
    where ea.ssn = team.etams_ssn 
     and ea.seq_no = ead.seq_no
     and ead.work_code in (select primary_code from ALOHA.LR_TYPE)
     and ea.yearx = dt.yearx
     and EA.PAY_PERIOD = DT.PAY_PERIOD
UNION ALL
    select to_number(null) ETAMS_Count, 1 ALOHA_Count, LRH.EMPLOYEE_USER_ID eeid, LRI.LEAVE_DATE leavedt, LRT.PRIMARY_CODE || ' - ' || LRT.PRIMARY_CODE_DESC leavetype, LRI.LEAVE_HOURS leavehrs,
    dt.yearx, dt.pay_period, FacilityCd, FacilityDesc, AreaCd, AreaDesc, TeamCd, TeamDesc, first_name, last_name, middle_name 
    from ALOHA.LR_HEADER lrh inner join  
        (select eup.user_id etams_uid, ssn etams_ssn, first_name, last_name, middle_name, 
            ti.team_description TeamDesc, ai.area_description AreaDesc, fi.facility_description FacilityDesc,
            ti.team_code TeamCd, ai.area_code AreaCd, fi.facility_code FacilityCd
            from usr_feddesk_required.etams_user_profile eup inner join usr_feddesk_required.user_demographics ud on eup.user_id = UD.USER_ID
            inner join usr_feddesk_required.team_info ti on ti.team_id = eup.member_of_team
            inner join usr_feddesk_required.area_info ai on ai.area_id = ti.area_id
            inner join usr_feddesk_required.facility_info fi on fi.facility_id = ai.facility_id
        where EUP.MEMBER_OF_TEAM = :x_teamid) team
        on LRH.EMPLOYEE_USER_ID = team.etams_uid 
        inner join ALOHA.LR_DETAIL lrd
        on LRD.LR_HEADER_ID = LRH.ID
        inner join ALOHA.LR_ITEM lri
        on LRI.LR_DETAIL_ID = lrd.id
        inner join ALOHA.LR_TYPE lrt on LRT.ID = LRI.LR_TYPE_ID
        inner join
          (select calendar_date, yearx, pay_period 
			from usr_feddesk_required.date_table 
			where calendar_date >=
			(select calendar_date from date_table where yearx = :x_from_year and pay_period = :x_from_pp and dayx = 1)
			and calendar_date <=
			(select calendar_date from date_table where yearx = :x_to_year and pay_period = :x_to_pp and dayx = 14)) dt
        on dt.calendar_date = LRI.LEAVE_DATE     
    where LRD.LR_STATUS_ID = 5
)
GROUP BY eeid, first_name, last_name, middle_name, FacilityCd, AreaCd, TeamCd, FacilityDesc, AreaDesc, TeamDesc, YYYY, PP, leavedt, leavetype, leavehrs
HAVING COUNT(ALOHA_COUNT) <> COUNT(ETAMS_COUNT)
ORDER BY last_name, first_name, leavedt, leavetype, leavehrs
