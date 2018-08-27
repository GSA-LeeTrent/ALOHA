SELECT 
    DTL.HEADER_ID AS HEADER_ID,
    DTL.DETAIL_ID AS DETAIL_ID,
    HDR.OT_GROUP_ID AS OT_GROUP_ID,
    HDR.SALARY_GRADE_KEY AS SALARY_GRADE_KEY,
    TYP.TYPE_CODE AS OT_TYPE_CODE,
    TYP.TYPE_NAME AS OT_TYPE_NAME,
    TYP.FUNDING_REQUIRED AS FUNDING_REQUIRED,
    PP.START_DATE AS PP_START_DATE,
    PP.END_DATE AS PP_END_DATE,
    STS.STATUS_CODE AS STATUS_CODE,
    STS.STATUS_NAME AS STATUS_NAME,
    EMPL.FIRST_NAME AS EMPL_FIRST_NAME, 
    EMPL.MIDDLE_NAME AS EMPL_MIDDLE_NAME,
    EMPL.LAST_NAME AS EMPL_LAST_NAME,
    SUBM.FIRST_NAME AS SUBMITTER_FIRST_NAME, 
    SUBM.MIDDLE_NAME AS SUBMITTER_MIDDLE_NAME,
    SUBM.LAST_NAME AS SUBMITTER_LAST_NAME,
    SUPV.FIRST_NAME AS SUPERVISOR_FIRST_NAME, 
    SUPV.MIDDLE_NAME AS SUPERVISOR_MIDDLE_NAME,
    SUPV.LAST_NAME AS SUPERVISOR_LAST_NAME
FROM
    ALOHA.OT_HEADER HDR
    INNER JOIN ALOHA.OT_DETAIL DTL                           
        ON DTL.HEADER_ID = HDR.HEADER_ID
        AND DTL.SUBM_USER_ID = HDR.EMPL_USER_ID 
        AND DTL.DETAIL_SEQ = (SELECT MAX(INNER_DTL.DETAIL_SEQ) FROM ALOHA.OT_DETAIL INNER_DTL WHERE INNER_DTL.HEADER_ID = DTL.HEADER_ID)
    INNER JOIN ALOHA.OT_TYPE TYP                           
        ON TYP.TYPE_ID = HDR.TYPE_ID
    INNER JOIN ALOHA.OT_PAY_PERIOD PP                           
        ON PP.PAY_PERIOD_KEY = HDR.PAY_PERIOD_KEY        
    INNER JOIN ALOHA.OT_STATUS STS                              
        ON STS.STATUS_ID = DTL.STATUS_ID
    INNER JOIN USR_FEDDESK_REQUIRED.USER_DEMOGRAPHICS EMPL
        ON EMPL.USER_ID = HDR.EMPL_USER_ID
    INNER JOIN USR_FEDDESK_REQUIRED.USER_DEMOGRAPHICS SUBM
        ON SUBM.USER_ID = DTL.SUBM_USER_ID
    INNER JOIN USR_FEDDESK_REQUIRED.USER_DEMOGRAPHICS SUPV
        ON SUPV.USER_ID = DTL.SUPV_USER_ID
WHERE
   HDR.EMPL_USER_ID = ?
ORDER BY
    PP.START_DATE DESC