update aloha.etams_ad_xref 
set 
login_name = UPPER(?),
user_id = ?,
timestamp = current_timestamp
where
login_name = UPPER(?)
