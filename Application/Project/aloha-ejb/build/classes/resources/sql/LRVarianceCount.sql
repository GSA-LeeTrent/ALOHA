SELECT 
    COUNT(*) 
AS 
    LR_VARIANCE_COUNT
FROM
    ALOHA.LR_VARIANCES
WHERE 
(
	LV_YEAR = ? 
AND 
	PP_NO = ?
)
AND 
(
	APPROVER_USER_ID = ? 
OR 
	SUBMITTER_USER_ID = ?
OR 
	CERTIFIER_USER_ID = ? 
OR
    TIMEKEEPER_USER_ID = ?
)