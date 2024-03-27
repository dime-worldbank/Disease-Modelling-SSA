**********************************************************************************
* Project: Zimbabwe Covid-19 Agent Based Modelling
* Module:  Assigning Schools to Students/Teachers
* Written by: Billy Hoo
* Last edited: 05/27/2020
**********************************************************************************


**********************************************************************************
* 1. Primary
**********************************************************************************
use  "D:\work\Zimbabwe\Final Data\Data\5_percent_sample\abm_individual_new_091720_p_teach_exp.dta", clear
gen r_order_seed_p_t= order_seat

**(First round)
merge m:1 district_name_shpfile r_order_seed_p_t  using  "D:\work\Zimbabwe\Final Data\Data\School Information_092020_primary_for_teacher_exp.dta", gen(_merge)
drop if _merge == 2

preserve
keep if _merge ==3 
gen matched = "1st Round"
drop _merge*
save "D:\work\Zimbabwe\Final Data\Data\5_percent_sample\abm_individual_new_091720__p_teach_merged_1.dta" , replace
restore

**Re-match unmatched observations (2nd round)
keep if _merge ==1

drop Schoolnumber-_merge


sort district_name_shpfile serial serial_cycle pernum
browse district_name_shpfile serial serial_cycle pernum

by district_name : gen order_seat2 = _n
*bysort serial_cycle : egen order_seat_hh2 = min(order_seat2)
replace r_order_seed_p_t = order_seat2


merge m:1 district_name_shpfile r_order_seed_p_t  using  "D:\work\Zimbabwe\Final Data\Data\School Information_092020_primary_for_teacher_exp.dta", gen(_merge2)
drop if _merge2 == 2

preserve
keep if _merge2 ==3 
gen matched = "2nd Round"
drop _merge*
save "D:\work\Zimbabwe\Final Data\Data\5_percent_sample\abm_individual_new_091720__p_teach_merged_2.dta" , replace
restore

**Re-match unmatched observations (3rd round)
keep if _merge2 ==1

drop Schoolnumber-_merge


sort district_name_shpfile serial serial_cycle pernum
browse district_name_shpfile serial serial_cycle pernum

by district_name : gen order_seat3 = _n
*bysort serial_cycle : egen order_seat_hh2 = min(order_seat2)
replace r_order_seed_p_t = order_seat3


merge m:1 district_name_shpfile r_order_seed_p_t  using   "D:\work\Zimbabwe\Final Data\Data\School Information_092020_primary_for_teacher_exp.dta", gen(_merge3)

drop if _merge3 == 2 

preserve
keep if _merge3 ==3 
gen matched = "3rd Round"
drop _merge*
save "D:\work\Zimbabwe\Final Data\Data\5_percent_sample\abm_individual_new_091720__p_teach_merged_3.dta" , replace
restore


**Append them back into the dataset
use "D:\work\Zimbabwe\Final Data\Data\5_percent_sample\abm_individual_new_091720__p_teach_merged_1.dta",clear
append using "D:\work\Zimbabwe\Final Data\Data\5_percent_sample\abm_individual_new_091720__p_teach_merged_2.dta"
append using "D:\work\Zimbabwe\Final Data\Data\5_percent_sample\abm_individual_new_091720__p_teach_merged_3.dta" 

save "D:\work\Zimbabwe\Final Data\Data\5_percent_sample\abm_individual_new_091720__p_t_complete.dta" , replace


**********************************************************************************
* 1. Secondary
**********************************************************************************
use  "D:\work\Zimbabwe\Final Data\Data\5_percent_sample\abm_individual_new_091720_s_teach_exp.dta", clear
gen r_order_seed_s_t= order_seat

**(First round)
merge m:1 district_name_shpfile r_order_seed_s_t  using  "D:\work\Zimbabwe\Final Data\Data\School Information_092020_secondary_for_teacher_exp.dta", gen(_merge)
drop if _merge == 2

preserve
keep if _merge ==3 
gen matched = "1st Round"
drop _merge*
save "D:\work\Zimbabwe\Final Data\Data\5_percent_sample\abm_individual_new_091720__s_teach_merged_1.dta" , replace
restore

**Re-match unmatched observations (2nd round)
keep if _merge ==1

drop Schoolnumber-_merge

sort district_name_shpfile serial serial_cycle pernum
browse district_name_shpfile serial serial_cycle pernum

by district_name : gen order_seat2 = _n
*bysort serial_cycle : egen order_seat_hh2 = min(order_seat2)
replace r_order_seed_s_t = order_seat2


merge m:1 district_name_shpfile r_order_seed_s_t  using  "D:\work\Zimbabwe\Final Data\Data\School Information_092020_secondary_for_teacher_exp.dta", gen(_merge2)
drop if _merge2 == 2

preserve
keep if _merge2 ==3 
gen matched = "2nd Round"
drop _merge*
save "D:\work\Zimbabwe\Final Data\Data\5_percent_sample\abm_individual_new_091720__s_teach_merged_2.dta" , replace
restore

**Append them back into the dataset
use "D:\work\Zimbabwe\Final Data\Data\5_percent_sample\abm_individual_new_091720__s_teach_merged_1.dta",clear
append using "D:\work\Zimbabwe\Final Data\Data\5_percent_sample\abm_individual_new_091720__s_teach_merged_2.dta"

save "D:\work\Zimbabwe\Final Data\Data\5_percent_sample\abm_individual_new_091720__s_t_complete.dta" , replace


