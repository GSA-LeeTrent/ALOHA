select eax.login_name, eax.user_id, ud.first_name, ud.middle_name, ud.last_name, ud.email_address, 
eup.member_of_team, eup.aws_ind, au.al_admin_user_id alohaAdminUserId  
from usr_feddesk_required.user_demographics ud, aloha.etams_ad_xref eax, usr_feddesk_required.etams_user_profile eup 
left outer join aloha.al_admin_user au on au.user_id = eup.user_id
where ud.user_id = eax.user_id    
and ud.user_id = eup.user_id
and ud.user_id = ?