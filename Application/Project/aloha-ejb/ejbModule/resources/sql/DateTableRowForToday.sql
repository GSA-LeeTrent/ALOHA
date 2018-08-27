SELECT 
    YEARX, 
    PAY_PERIOD, 
    DAYX, 
    CALENDAR_DATE 
FROM 
    USR_FEDDESK_REQUIRED.DATE_TABLE 
WHERE 
    TO_CHAR(CALENDAR_DATE, 'MM/DD/YYYY') = TO_CHAR(SYSDATE, 'MM/DD/YYYY')