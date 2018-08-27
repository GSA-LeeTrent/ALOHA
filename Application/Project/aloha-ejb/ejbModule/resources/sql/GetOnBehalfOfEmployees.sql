select user_id, last_name, first_name, middle_name, email_address from user_demographics where user_id in(
select up.user_id 
from etams_user_profile up, team_info t, etams_permissions_matrix pm
where up.member_of_team = t.team_id
and t.team_id = pm.group_id
and pm.group_type = 1
and pm.authority_type in (0,1,2)
and pm.user_id = ? 
union
select up.user_id 
from etams_user_profile up, team_info t, etams_permissions_matrix pm
where up.member_of_team = t.team_id
and t.area_id = pm.group_id
and pm.group_type = 2
and pm.authority_type in (0,1,2)
and pm.user_id = ?
union
select up.user_id 
from etams_user_profile up, team_info t, area_info a, etams_permissions_matrix pm
where up.member_of_team = t.team_id
and t.area_id = a.area_id
and a.facility_id = pm.group_id
and pm.group_type = 3
and pm.authority_type in (0,1,2)
and pm.user_id = ? 
)
order by last_name, first_name