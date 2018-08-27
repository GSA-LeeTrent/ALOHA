select 'SubmitOwn' UserRole, case when count(*) > 0 then 1 else 0 end HasRole
  from ETAMS_USER_PROFILE eup, user_demographics ud  
 where member_of_team <> 999 
   and cost_center_id <> 999 
   and PAY_STAT IN ('1','2','4','6','A') 
   and delete_code = 'E'  
   and GSA_PAYROLL = 'Y' 
   and eup.user_id = ?
   and eup.user_id = ud.user_id
union 
 select 'OnBehalfOf' UserRole, case when count(*) > 0 then 1 else 0 end HasRole 
   from etams_permissions_matrix epm, user_demographics ud 
  where authority_type in (0,1,2) 
    and epm.user_id = ?
    and epm.user_id = ud.user_id 
union 
 select 'Approver' UserRole, case when count(*) > 0 then 1 else 0 end HasRole 
   from etams_permissions_matrix epm, user_demographics ud 
  where signature_authorization = 1
    and epm.user_id = ? 
    and epm.user_id = ud.user_id
union  
SELECT 'FacilityCoordinator' UserRole, case when count(*) > 0 then 1 else 0 end HasRole  
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