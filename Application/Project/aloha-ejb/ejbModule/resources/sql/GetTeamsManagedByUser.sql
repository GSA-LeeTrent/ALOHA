select fi.facility_id, fi.facility_code, fi.facility_description, fi.facility_code || ' - ' || fi.facility_Description facility_text, 
ai.area_id, ai.area_code, ai.area_description, ai.area_code || ' - ' || ai.area_Description area_text, 
ti.team_id, TI.TEAM_CODE, TI.TEAM_DESCRIPTION, ti.team_code || ' - ' || ti.team_Description team_text
from team_info ti inner join etams_permissions_matrix pm on TI.TEAM_ID = pm.group_id and pm.group_Type = 1
inner join area_info ai on AI.AREA_ID = TI.AREA_ID
inner join facility_info fi on FI.FACILITY_ID = AI.FACILITY_ID
where pm.authority_type in (0,1,2)
and pm.user_id = :x_etams_userid 
union
select fi.facility_id, fi.facility_code, fi.facility_description, fi.facility_code || ' - ' || fi.facility_Description facility_text,
ai.area_id, ai.area_code, ai.area_description, ai.area_code || ' - ' || ai.area_Description area_text,
ti.team_id, TI.TEAM_CODE, TI.TEAM_DESCRIPTION, ti.team_code || ' - ' || ti.team_Description team_text 
from team_info ti
inner join area_info ai on AI.AREA_ID = TI.AREA_ID
inner join facility_info fi on FI.FACILITY_ID = AI.FACILITY_ID
inner join etams_permissions_matrix pm on aI.area_ID = pm.group_id and pm.group_Type = 2
where pm.authority_type in (0,1,2)
and pm.user_id = :x_etams_userid    
union     
select fi.facility_id, fi.facility_code, fi.facility_description, fi.facility_code || ' - ' || fi.facility_Description facility_text,
ai.area_id, ai.area_code, ai.area_description, ai.area_code || ' - ' || ai.area_Description area_text,
ti.team_id, TI.TEAM_CODE, TI.TEAM_DESCRIPTION, ti.team_code || ' - ' || ti.team_Description team_text 
from team_info ti
inner join area_info ai on AI.AREA_ID = TI.AREA_ID
inner join facility_info fi on FI.FACILITY_ID = AI.FACILITY_ID
inner join etams_permissions_matrix pm on fI.facility_ID = pm.group_id and pm.group_Type = 3
where pm.authority_type in (0,1,2)
and pm.user_id = :x_etams_userid  
order by facility_Text, area_text, team_text
