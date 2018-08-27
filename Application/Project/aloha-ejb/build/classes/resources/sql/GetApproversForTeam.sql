select ud.user_id, ud.first_name, ud.middle_name, ud.last_name, ud.email_address 
from user_demographics ud 
inner join etams_permissions_matrix epm
  on UD.USER_ID = epm.user_id 
where EPM.GROUP_ID = ?
and epm.group_type = 1
and epm.authority_type in (1,2)
and epm.signature_authorization =1
-- OCP 49423 - Exclude SuperUsers
and a.etams_super = 0
-- OCP 49423 - End