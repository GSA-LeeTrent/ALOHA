select 'Comp Time Earned' OTType, nvl(sum(lv_hrs_bal),0) OTBalance 
    from par_eds_lv lv,user_demographics ud,par_eds_basic1 peb 
    where lv.lv_ssn= peb.ssn 
      and lv.lv_agy= peb.agy 
      and lv.lv_cd = '13' 
      and lv.lv_ssn = ud.ssn 
      -- OCP 49495 - Get only Current Agency
      and peb.pay_status_cd not in ('B','D','8','9')
      -- OCP 49495 - End
      and ud.user_id = ? 
union 
select 'Credit Hours Earned' OTType, nvl(sum(lv_hrs_bal),0) OTBalance 
    from par_eds_lv lv,user_demographics ud,par_eds_basic1 peb 
    where lv.lv_ssn= peb.ssn 
      and lv.lv_agy= peb.agy 
      and lv.lv_cd = '36' 
      and lv.lv_ssn = ud.ssn
      -- OCP 49495 - Get only Current Agency
      and peb.pay_status_cd not in ('B','D','8','9')
      -- OCP 49495 - End
      and ud.user_id = ?      