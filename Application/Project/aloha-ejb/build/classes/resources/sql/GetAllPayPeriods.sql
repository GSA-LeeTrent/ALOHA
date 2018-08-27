SELECT 
    SUBSTR(LY.TBL_PP_LY_PP_NO_YR, 3, 6) YEAR, 
    SUBSTR(LY.TBL_PP_LY_PP_NO_YR, 1, 2) PAY_PERIOD, 
    LY.TBL_PP_BEG_DATE FROM_DATE, 
    LY.TBL_PP_END_DATE TO_DATE 
FROM 
    USR_FEDDESK_REQUIRED.PAR_PAY_PERIODS_MV LY
ORDER BY 
    YEAR DESC, 
    PAY_PERIOD