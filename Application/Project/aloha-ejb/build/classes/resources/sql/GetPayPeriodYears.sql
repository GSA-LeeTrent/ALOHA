select distinct substr(ly.tbl_pp_ly_pp_no_yr, 3, 6) pay_period_year
from USR_FEDDESK_REQUIRED.par_pay_periods_mv ly
order by pay_period_year desc