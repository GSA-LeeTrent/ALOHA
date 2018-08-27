select ead.login_name, ead.user_id,
ud.last_name, ud.first_name, UD.MIDDLE_NAME, UD.EMAIL_ADDRESS
from aloha.etams_ad_xref ead
inner join USR_FEDDESK_REQUIRED.USER_DEMOGRAPHICS ud on ud.user_id = ead.user_id
order by login_name;
