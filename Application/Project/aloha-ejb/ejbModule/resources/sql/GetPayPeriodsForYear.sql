select substr(ly.tbl_pp_ly_pp_no_yr, 3, 6) year, substr(ly.tbl_pp_ly_pp_no_yr, 1, 2) pay_period, 
LY.TBL_PP_BEG_DATE from_date, LY.TBL_PP_END_DATE to_date 
from USR_FEDDESK_REQUIRED.par_pay_periods_mv ly
where tbl_pp_pay_freq = 'BW'
order by year desc, pay_period