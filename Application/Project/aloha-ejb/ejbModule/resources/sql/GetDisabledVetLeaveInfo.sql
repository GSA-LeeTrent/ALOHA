  SELECT
    PEL.LV_USED_BEG_DATE,
    PEL.LV_HRS_BAL
FROM
    USR_FEDDESK_REQUIRED.USER_DEMOGRAPHICS UD
INNER JOIN
    USR_FEDDESK_REQUIRED.PAR_EDS_LV PEL
        ON PEL.LV_SSN = UD.SSN
        AND PEL.LV_CD = '69' 
WHERE
    UD.USER_ID = ?  