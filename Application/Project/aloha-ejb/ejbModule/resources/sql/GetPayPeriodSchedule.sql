--sak 20120823 - no changes required for CY to LY sswitch as 
--               sql does not retrieve PP or YR  
select dt.calendar_date calendar_date, UPPER(to_char(dt.calendar_date,'day')) day_of_week,  
       DECODE(dt.holiday_ind, '1', 0, DECODE(ebs.work_start,'9000', '0',SUBSTR(ebs.work_start,1,3) / 10)) hours_scheduled, holiday_ind, holiday_desc
  from user_demographics ud, emp_base_schedule ebs, date_table dt
  where ud.user_id = ?
    and ud.user_id = ebs.user_id
    and dt.dayx = ebs.dayx
    and pay_period = (select pay_period
                        from date_table
                       where calendar_date = to_date(?, 'yyyymmdd'))
    and yearx = (select yearx from date_table where calendar_date = to_date(?,'YYYYMMDD'))
    and ebs.work_code is null
  order by dt.dayx