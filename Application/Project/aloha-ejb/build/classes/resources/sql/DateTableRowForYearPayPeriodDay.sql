SELECT 
    YEARX, 
    PAY_PERIOD, 
    DAYX, 
    CALENDAR_DATE 
FROM 
    USR_FEDDESK_REQUIRED.DATE_TABLE 
WHERE
    YEARX = ?
AND
    PAY_PERIOD = ?
AND
    DAYX = ?