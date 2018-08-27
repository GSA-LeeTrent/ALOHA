SELECT
    DISTINCT
    fac.facility_id,
    fac.facility_code,
    fac.facility_description
FROM
    usr_feddesk_required.etams_permissions_matrix epm,
	usr_feddesk_required.facility_info fac,
    usr_feddesk_required.agency_info agy
WHERE
    epm.user_id = ?
AND
    epm.authority_type = 1
AND
    epm.group_type = 3
AND
    fac.facility_id = epm.group_id
AND
    agy.agency_id = fac.agency_id    
AND
    agy.agency_code = 'GS'
ORDER BY
    fac.facility_id