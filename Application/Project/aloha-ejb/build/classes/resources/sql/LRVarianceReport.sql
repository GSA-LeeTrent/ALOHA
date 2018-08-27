SELECT 
    facility_id,
    lv_date,
    user_id,
    last_name,
    first_name,
    middle_name,
    fat_code,
    org_loc,
    lv_year,
    pp_no,
    lr_header_id,
    lv_type,
    lv_hrs_aloha,
    lv_hrs_etams
FROM
    aloha.lr_recon
WHERE
    facility_id in (:x_facility_id_list)
AND
    lv_date BETWEEN TO_DATE(:x_from_date, 'yyyymmdd') 
    		AND TO_DATE(:x_to_date, 'yyyymmdd')