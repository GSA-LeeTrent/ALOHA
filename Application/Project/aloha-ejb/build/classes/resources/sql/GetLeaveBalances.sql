select 'Annual Leave' LeaveType, lv_hrs_bal LeaveBalance
    from par_eds_lv lv,user_demographics ud,par_eds_basic1 peb
    where lv.lv_ssn= peb.ssn
      and lv.lv_agy= peb.agy
      and lv.lv_cd = '40'
      and lv.lv_ssn = ud.ssn
      -- OCP 49495 - Get only Current Agency
      and peb.pay_status_cd not in ('B','D','8','9')
      -- OCP 49495 - End
      and ud.user_id = ?  
union    
select 'Sick Leave' LeaveType, lv_hrs_bal LeaveBalance
    from par_eds_lv lv,user_demographics ud,par_eds_basic1 peb
    where lv.lv_ssn= peb.ssn
      and lv.lv_agy= peb.agy
      and lv.lv_cd = '50'
      and lv.lv_ssn = ud.ssn
      -- OCP 49495 - Get only Current Agency
      and peb.pay_status_cd not in ('B','D','8','9')
      -- OCP 49495 - End
      and ud.user_id = ?
union 
select 'Comp Time' LeaveType, lv_hrs_bal LeaveBalance
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
select 'Credit Hours' LeaveType, lv_hrs_bal LeaveBalance
    from par_eds_lv lv,user_demographics ud,par_eds_basic1 peb
    where lv.lv_ssn= peb.ssn
      and lv.lv_agy= peb.agy
      and lv.lv_cd = '36'
      and lv.lv_ssn = ud.ssn
      -- OCP 49495 - Get only Current Agency
      and peb.pay_status_cd not in ('B','D','8','9')
      -- OCP 49495 - End
      and ud.user_id = ?
union    
select 'Use Or Lose' LeaveType, lv_use_lose_hrs LeaveBalance
    from par_eds_lv lv,user_demographics ud,par_eds_basic1 peb
    where lv.lv_ssn= peb.ssn
      and lv.lv_agy= peb.agy
      and lv.lv_cd = '40'
      and lv.lv_ssn = ud.ssn
      -- OCP 49495 - Get only Current Agency
      and peb.pay_status_cd not in ('B','D','8','9')
      -- OCP 49495 - End
      and ud.user_id = ?
union    
select 'Home Leave' LeaveType, home_lv_days_bal LeaveBalance
    from par_eds_home_lv hlv,user_demographics ud,par_eds_basic1 peb
    where hlv.home_lv_ssn= peb.ssn
      and hlv.home_lv_agy= peb.agy
      and hlv.home_lv_ssn = ud.ssn
      -- OCP 49495 - Get only Current Agency
      and peb.pay_status_cd not in ('B','D','8','9')
      -- OCP 49495 - End
      and ud.user_id = ?
union
select 'Mil Res Tech' LeaveType, mil_lv_hrs_bal LeaveBalance   
    from par_eds_mil_lv mlv,user_demographics ud,par_eds_basic1 peb
    where mlv.mil_lv_ssn= peb.ssn
      and mlv.mil_lv_agy= peb.agy
      and mlv.mil_lv_cd = '49'
      and mlv.mil_lv_ssn = ud.ssn
      -- OCP 49495 - Get only Current Agency
      and peb.pay_status_cd not in ('B','D','8','9')
      -- OCP 49495 - End
      and ud.user_id = ?
union
select 'Reg Mil Lv' LeaveType, mil_lv_hrs_bal LeaveBalance   
    from par_eds_mil_lv mlv,user_demographics ud,par_eds_basic1 peb
    where mlv.mil_lv_ssn= peb.ssn
      and mlv.mil_lv_agy= peb.agy
      and mlv.mil_lv_cd = '51'
      and mlv.mil_lv_ssn = ud.ssn
      -- OCP 49495 - Get only Current Agency
      and peb.pay_status_cd not in ('B','D','8','9')
      -- OCP 49495 - End
      and ud.user_id = ?
union
select 'LE Mil Lv' LeaveType, mil_lv_hrs_bal LeaveBalance   
    from par_eds_mil_lv mlv,user_demographics ud,par_eds_basic1 peb
    where mlv.mil_lv_ssn= peb.ssn
      and mlv.mil_lv_agy= peb.agy
      and mlv.mil_lv_cd = '52'
      and mlv.mil_lv_ssn = ud.ssn
      -- OCP 49495 - Get only Current Agency
      and peb.pay_status_cd not in ('B','D','8','9')
      -- OCP 49495 - End
      and ud.user_id = ?
union
select 'DC Natl Guard Mil Lv' LeaveType, mil_lv_hrs_bal LeaveBalance   
    from par_eds_mil_lv mlv,user_demographics ud,par_eds_basic1 peb
    where mlv.mil_lv_ssn= peb.ssn
      and mlv.mil_lv_agy= peb.agy
      and mlv.mil_lv_cd = '53'
      and mlv.mil_lv_ssn = ud.ssn
      -- OCP 49495 - Get only Current Agency
      and peb.pay_status_cd not in ('B','D','8','9')
      -- OCP 49495 - End
      and ud.user_id = ?
union
select 'Restored Leave1' LeaveType, rest_lv_bal LeaveBalance
    from par_eds_rest_lv rlv,user_demographics ud,par_eds_basic1 peb
    where rlv.rest_lv_ssn= peb.ssn
      and rlv.rest_lv_agy= peb.agy
      and rlv.rest_lv_cd = '44'
      and rlv.rest_lv_ssn = ud.ssn
      -- OCP 49495 - Get only Current Agency
      and peb.pay_status_cd not in ('B','D','8','9')
      -- OCP 49495 - End
      and ud.user_id = ?
union
select 'Restored Leave2' LeaveType, rest_lv_bal LeaveBalance
    from par_eds_rest_lv rlv,user_demographics ud,par_eds_basic1 peb
    where rlv.rest_lv_ssn= peb.ssn
      and rlv.rest_lv_agy= peb.agy
      and rlv.rest_lv_cd = '45'
      and rlv.rest_lv_ssn = ud.ssn
      -- OCP 49495 - Get only Current Agency
      and peb.pay_status_cd not in ('B','D','8','9')
      -- OCP 49495 - End
      and ud.user_id = ?
union
select 'Restored Leave3' LeaveType, rest_lv_bal LeaveBalance
    from par_eds_rest_lv rlv,user_demographics ud,par_eds_basic1 peb
    where rlv.rest_lv_ssn= peb.ssn
      and rlv.rest_lv_agy= peb.agy
      and rlv.rest_lv_cd = '89'
      and rlv.rest_lv_ssn = ud.ssn
      -- OCP 49495 - Get only Current Agency
      and peb.pay_status_cd not in ('B','D','8','9')
      -- OCP 49495 - End
      and ud.user_id = ?
union
select 'Shared Leave' LeaveType, shd_lv_bal LeaveBalance
    from par_eds_shd_lv_recip slv,user_demographics ud,par_eds_basic1 peb
    where slv.shd_lv_ssn= peb.ssn
      and slv.shd_lv_agy= peb.agy
      and slv.shd_lv_cd = '85'
      and slv.shd_lv_term_date is not null
      and slv.shd_lv_hr_term_date is not null
      and slv.shd_lv_ssn = ud.ssn
      -- OCP 49495 - Get only Current Agency
      and peb.pay_status_cd not in ('B','D','8','9')
      -- OCP 49495 - End
      and ud.user_id = ?
union    
select 'Religious Comp' LeaveType, lv_hrs_bal LeaveBalance
    from par_eds_lv lv,user_demographics ud,par_eds_basic1 peb
    where lv.lv_ssn= peb.ssn
      and lv.lv_agy= peb.agy
      and lv.lv_cd = '46'
      and lv.lv_ssn = ud.ssn
      -- OCP 49495 - Get only Current Agency
      and peb.pay_status_cd not in ('B','D','8','9')
      -- OCP 49495 - End
      and ud.user_id = ?
union       
select 'Award Leave' LeaveType, lv_hrs_bal LeaveBalance
    from par_eds_lv lv,user_demographics ud,par_eds_basic1 peb
    where lv.lv_ssn= peb.ssn
      and lv.lv_agy= peb.agy
      and lv.lv_cd = '54'
      and lv.lv_ssn = ud.ssn
      -- OCP 49495 - Get only Current Agency
      and peb.pay_status_cd not in ('B','D','8','9')
      -- OCP 49495 - End
      and ud.user_id = ?
union         
select 'Disabled Veteran Leave' LeaveType, lv_hrs_bal LeaveBalance
    from par_eds_lv lv,user_demographics ud,par_eds_basic1 peb
    where lv.lv_ssn= peb.ssn
      and lv.lv_agy= peb.agy
      and lv.lv_cd = '69'
      and lv.lv_ssn = ud.ssn
      -- OCP 49495 - Get only Current Agency
      and peb.pay_status_cd not in ('B','D','8','9')
      -- OCP 49495 - End
      and ud.user_id = ?     