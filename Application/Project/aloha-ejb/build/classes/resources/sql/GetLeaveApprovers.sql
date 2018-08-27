select a.user_id, a.first_name, a.last_name, a.email_address, a.middle_name 
from user_Demographics a, etams_user_profile b, etams_permissions_matrix c, team_info d 
where b.user_id = ?
and b.member_of_team = d.team_id 
and c.group_id = D.TEAM_ID and c.group_type = 1 
and c.user_id = a.user_id and c.authority_type in (1,2) 
and c.signature_authorization = 1 
-- OCP 49423 - Exclude SuperUsers
and a.etams_super = 0
-- OCP 49423 - End
union 
select a.user_id, a.first_name, a.last_name, a.email_address, a.middle_name 
from user_Demographics a, etams_user_profile b, etams_permissions_matrix c, team_info d, area_info e 
where b.user_id = ?
and b.member_of_team = d.team_id 
and d.area_id = e.area_id 
and c.group_id = e.area_id 
and c.group_type = 2 
and c.user_id = a.user_id 
and c.authority_type in (1,2) 
and c.signature_authorization = 1
-- OCP 49423 - Exclude SuperUsers
and a.etams_super = 0
-- OCP 49423 - End
union
select a.user_id, a.first_name, a.last_name, a.email_address, a.middle_name 
from user_Demographics a, etams_user_profile b, etams_permissions_matrix c, team_info d, area_info e, facility_info f 
where b.user_id = ?
and b.member_of_team = d.team_id 
and d.area_id = e.area_id 
and e.facility_id = f.facility_id 
and c.group_id = f.facility_id 
and c.group_type = 3 
and c.user_id = a.user_id 
and c.authority_type in (1,2) 
and c.signature_authorization = 1
-- OCP 49423 - Exclude SuperUsers
and a.etams_super = 0
-- OCP 49423 - End