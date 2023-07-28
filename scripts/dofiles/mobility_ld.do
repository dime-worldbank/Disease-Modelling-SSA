
cd "/Users/sophieayling/Documents/GitHub/Disease-Modelling-SSA/data"

tempfile temp1 temp2


import delimited using "analysis/mobility/ld_percent_leave_district_each_day.csv", clear


*reshape long

reshape long d_, i(weekday) j(district_id)

rename d_ perc_leave_home_dist

by district_id, sort: egen ld_weekly_perc_leave_dist = mean(perc_leave_home_dist)

collapse (first) ld_weekly_perc_leave_dist, by (district_id)
sort district_id
save `temp1', replace

*add in the names 

import delimited using "raw/shapefiles/new_districts/ZWE_adm2.csv",clear
rename id_2 district_id
sort district_id 

merge 1:1 district_id using `temp1'
drop _merge
rename name_2 dist_name
gen x = ld_weekly_perc_leave_dist/100
drop ld_weekly_perc_leave_dist
rename x ld_weekly_perc_leave_dist
export delimited "analysis/mobility/ld_perc_leave_weekly.csv",  replace

save `temp2', replace


*combine here with the nld file 

import delimited using "analysis/mobility/perc_leave_weekly.csv", clear

sort district_id

merge 1:1 district_id using `temp2'

drop _merge

gen diff_pre_post = ld_weekly_perc_leave_dist - weekly_perc_leave_dist

export delimited "analysis/mobility/pre_post_ld_perc_leave_weekly.csv", replace
e
*combine with the case output file 

import excel using "map_input/cum_cases_num_25p_multidist.xlsx", firstrow clear

sort district_id
merge 1:1 district_id using `temp2'
drop _merge

rename G cum_cases_25p_day30
keep district_id cum_cases_25p_day30 ld_weekly_perc_leave_dist

export delimited using "map_input/cases_plus_ld_mobility.csv", replace
