select 
peb.pay_plan || peb.occup_cd || peb.grade 
as salary_grade_key
from 
usr_feddesk_required.user_demographics ud,
usr_feddesk_required.par_eds_basic1 peb
where 
ud.user_id = ?
and 
peb.ssn = ud.ssn
and
peb.pay_status_cd in ('1','2','4','6','A')