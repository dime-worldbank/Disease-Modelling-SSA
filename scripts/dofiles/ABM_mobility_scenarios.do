/* ******************************************

Program name: MOBILITY ABM runs 
Author: SA
Date last modified: 
Project:  ABM mobility focus in scenarios
Purpose:   Make output data mappable for scenarios of outbreak starting in different districts

****************************************** */

***0. SET STATA 
clear matrix
clear
set more off

cap cd "/Users/sophieayling/Documents/GitHub/Disease-Modelling-SSA/data/"

tempfile temp1 temp2 temp3 temp4 temp5 temp6 temp7

*checks to add more stuff 

import delimited "output/dist_mobility_output/start_at_d31_output.csv", clear

*create the cumulative aggregate on cases at different cut offs as per Robbie's at day 30, 60 and 90 of the simulation



 ************************** Replicate in a loop *********************
// Most travelled 
foreach x in 2 26 23 18 31 {
	import delimited "output/dist_mobility_output/start_at_d`x'_output.csv", clear

drop v1

split myid, p("d_")
rename myid2 district_id
destring district_id, replace
sort district_id time 
order district_id time 
drop myid1 myid unnamed10
rename time day

by district_id, sort: egen tot_sympt_cases_100_days = total(metric_new_cases_sympt)

collapse (first)tot_sympt_cases_100_days (max) metric_died_count, by(district_id)

gen deaths_100_days = round(metric_died_count)
gen cases_100_days = round(tot_sympt_cases_100_days)

drop tot_sympt_cases_100_days metric_died_count

*merge in dist names 
save `temp1', replace

import delimited "map_input/shapefiles/60_districts/ZWE_adm2.csv", clear

rename id_2 district_id 
keep district_id name_2
merge 1:1 district_id using `temp1'
drop _merge 

export delimited "map_input/model_output/mobility_scenarios/most_travelled/start_at_d`x'.csv", replace 
	
}


*************Least travelled

foreach x in 51 42 41 39 29 {
	import delimited "output/dist_mobility_output/start_at_d`x'_output.csv", clear

drop v1

split myid, p("d_")
rename myid2 district_id
destring district_id, replace
sort district_id time 
order district_id time 
drop myid1 myid unnamed10
rename time day

by district_id, sort: egen tot_sympt_cases_100_days = total(metric_new_cases_sympt)

collapse (first)tot_sympt_cases_100_days (max) metric_died_count, by(district_id)

gen deaths_100_days = round(metric_died_count)
gen cases_100_days = round(tot_sympt_cases_100_days)

drop tot_sympt_cases_100_days metric_died_count

*merge in dist names 
save `temp1', replace

import delimited "map_input/shapefiles/60_districts/ZWE_adm2.csv", clear

rename id_2 district_id 
keep district_id name_2
merge 1:1 district_id using `temp1'
drop _merge 

export delimited "map_input/model_output/mobility_scenarios/least_travelled/start_at_d`x'.csv", replace 
	
}
