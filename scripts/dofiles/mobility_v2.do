
cd "/Users/sophieayling/Documents/GitHub/Disease-Modelling-SSA/data"

tempfile temp1


import delimited using "analysis/mobility/percent_leave_district_each_day.csv", clear


*reshape long

reshape long d_, i(weekday) j(district_id)

rename d_ perc_leave_home_dist

by district_id, sort: egen weekly_perc_leave_dist = mean(perc_leave_home_dist)

collapse (first) weekly_perc_leave_dist, by (district_id)
sort district_id
save `temp1', replace

*add in the names 

import delimited using "raw/shapefiles/new_districts/ZWE_adm2.csv",clear
rename id_2 district_id
sort district_id 

merge 1:1 district_id using `temp1'
drop _merge
rename name_2 dist_name
gen x = weekly_perc_leave_dist/100
drop weekly_perc_leave_dist
rename x weekly_perc_leave_dist
export delimited "analysis/mobility/perc_leave_weekly.csv",  replace

save `temp2', replace

*combine with the case output file 

import excel using "map_input/cum_cases_num_25p_multidist.xlsx", firstrow clear
