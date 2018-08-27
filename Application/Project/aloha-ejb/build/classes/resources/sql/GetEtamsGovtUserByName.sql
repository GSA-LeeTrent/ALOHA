select ud.user_id, UD.EMAIL_ADDRESS, upper(ud.last_name || ' ' || ud.first_name) username, ud.last_name, ud.middle_name
from user_demographics ud
inner join etams_user_profile eup on ud.user_id = EUP.USER_ID
where member_of_team <> 999
  and cost_center_id <> 999
  and PAY_STAT IN ('1','2','4','6','A')
  and delete_code = 'E' 
  and GSA_PAYROLL = 'Y'
  and upper(last_name || ' ' || first_name) like ? || '%'
  and rownum < 26
order by username
