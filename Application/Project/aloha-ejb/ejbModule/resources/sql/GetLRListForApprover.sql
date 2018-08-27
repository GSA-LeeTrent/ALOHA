SELECT 
DTL.LR_HEADER_ID AS REQUEST_ID, 
DTL.LR_DETAIL_ID AS DETAIL_ID,
DTL.SUBMITTER_USER_ID,
DTL.APPROVER_USER_ID,
DTL.DATE_LAST_UPDATED AS DTL_LAST_UPDATED,
DTL.PP_START_DATE,
DTL.PP_END_DATE,
HDR.EMPLOYEE_USER_ID,
HDR.DATE_LAST_UPDATED AS HDR_DATE_SUBMITTED,
STS.CODE AS STATUS_CODE,
STS.LABEL AS STATUS_LABEL,
UD1.FIRST_NAME AS EMPLOYEE_FIRST_NAME, 
UD1.LAST_NAME AS EMPLOYEE_LAST_NAME,
UD2.FIRST_NAME AS SUBMITTER_FIRST_NAME, 
UD2.LAST_NAME AS SUBMITTER_LAST_NAME
FROM 
ALOHA.LR_DETAIL DTL, 
ALOHA.LR_HEADER HDR, 
USR_FEDDESK_REQUIRED.USER_DEMOGRAPHICS UD1,
USR_FEDDESK_REQUIRED.USER_DEMOGRAPHICS UD2,
ALOHA.LR_STATUS STS
WHERE 
DTL.APPROVER_USER_ID = ?
AND
UD2.USER_ID = DTL.SUBMITTER_USER_ID
AND
HDR.LR_HEADER_ID = DTL.LR_HEADER_ID
AND 
UD1.USER_ID = HDR.EMPLOYEE_USER_ID
AND
STS.LR_STATUS_ID = DTL.LR_STATUS_ID
ORDER BY
HDR.DATE_LAST_UPDATED DESC,
DTL.LR_DETAIL_SEQUENCE DESC