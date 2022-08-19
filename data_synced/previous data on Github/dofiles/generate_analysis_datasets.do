

cd "C:\Users\wb488473\WBG\Data analysis to inform Global Water Operations - WB Group - Documents\Zim COVID - via Teams\08_Data\files for Sarah\outputs_gitignored"

*use "stata_raw_45days.dta", clear
*import delimited "results_-1709889735.txt", encoding(ISO-8859-2) clear
*import delimited "results_-1103319989.txt", encoding(ISO-8859-2) clear
*import delimited "results_-1103319989.txt", encoding(ISO-8859-2) clear
*import delimited "results_-1017601462_491days.txt", encoding(ISO-8859-2) clear
*import delimited "beta_testing/results_-524127817.txt", encoding(ISO-8859-2) clear
*import delimited "beta_testing/results_-484773976.txt", encoding(ISO-8859-2) clear
import delimited "beta_testing/results_0.016_1_120_18062021.txt", encoding(ISO-8859-2) clear


/*
rename myid time
rename metric_died_count district
rename metric_new_hospitalized died_count
rename metric_new_critical  new_hosp
rename metric_new_cases_asympt new_critical 
rename metric_new_cases_sympt new_asympt
rename metric_new_deaths new_sympt
rename v8 new_deaths
*/
rename myid district
rename metric_died_count died_count
rename metric_new_hospitalized new_hosp
rename metric_new_critical new_crit
rename metric_new_cases_asympt new_asympt
rename metric_new_cases_sympt new_sympt
rename metric_new_deaths new_deaths
drop v9

tab new_sympt if district=="d_2" 

*drop d of district id 
split district, parse (d_) generate (dist_no)
drop dist_no1
sort dist_no2
rename dist_no2 dist_no
sort dist_no

*first remove duplicate time steps
by district time, sort: gen time_cleaned = _n
keep if time_cleaned==1
drop time_cleaned 

*generate day variable from timesteps (4 x 6 hour blocks) (this has changed in the raw data, no longer needed in versions past 1st June)
/*
gen day=time/24
replace day=floor(day)
order time day
sort dist_no day time
*/

*in more recent datasets, time= day
gen day = time 

*Disaggregated dataset: export disaggregated dataset for checkbacks

bys day district: egen newsypmt_district_day=sum(new_sympt)
bys day district: egen newasymp_district_day=sum(new_asympt)
bys day district: egen deaths_district_day = sum(new_deaths)

save "processed/model_outputs_disaggregated_p016_120_18062021.dta", replace

export excel "processed/model_outputs_disaggregated_p016_120_18062021.xlsx", first(var)replace

*District level analysis: create collapsed dataset: all data to day by district
preserve
collapse (sum) new_asympt new_sympt new_hosp new_crit new_deaths died_count (first)dist_no, by(day district)

bys day: egen newsympt_nat_day=sum(new_sympt)
bys day: egen newasympt_nat_day=sum(new_asympt)
bys day: egen deaths_nat_day = sum(new_deaths)

save "processed/model_outputs_dist_level_p016_120_18062021.dta", replace
export excel "processed/model_outputs_dist_level_p016_120_18062021.xlsx", first(varl) replace 
restore

*National level analysis: create collapsed dataset: all data by day nationally
collapse (sum) new_asympt new_sympt new_hosp new_crit new_deaths died_count, by(day)
save "processed/model_outputs_nat_level_p016_120_18062021.dta", replace
export excel "processed/model_outputs_nat_level_p016_120_18062021.xlsx", first(varl) replace 






