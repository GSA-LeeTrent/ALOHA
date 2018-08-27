select ud.user_id, UD.EMAIL_ADDRESS, upper(ud.last_name || ' ' || ud.first_name) username, ud.last_name, ud.first_name, ud.middle_name
from user_demographics ud
where upper(last_name || ' ' || first_name) like ? 
  and rownum < 26
order by username