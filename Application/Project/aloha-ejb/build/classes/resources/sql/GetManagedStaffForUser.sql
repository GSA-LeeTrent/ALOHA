select ud.user_id, ud.last_name, ud.first_name, ud.middle_name, ud.email_address, eup.member_of_team, eup.aws_ind, eax.login_name
from user_demographics ud inner join etams_user_profile eup on eup.user_id = ud.user_id
inner join aloha.etams_ad_xref eax on eax.user_id = ud.user_id
where ud.user_id in(
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