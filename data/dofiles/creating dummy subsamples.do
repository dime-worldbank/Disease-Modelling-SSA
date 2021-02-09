************ create initial dummy datasets for Aivin's abm
clear all

cd "/Users/sophieayling/Documents/GitHub/Disease-Modelling-SSA"
tempfile temp1 


************ add back in missing variables from other census version -- province = geo1_zw2012

** save old dataset key vars for merge as tempfile 
use "data/raw/census/versions/ABM_Simulated_Pop_WardDistributed_UpdatedMay30_school_complete_060520.dta", clear

keep district_id geo1_zw2012 geo2_zw2012
sort district_id 
duplicates drop
isid district_id



save `temp1', replace

use "data/raw/census/census_sample_1500.dta", clear
sort district_id
merge m:1 district_id using `temp1' 

drop district_id 
rename new_district_id district_id

** save new version of 1500 data set

drop _merge
save "data/raw/census/census_sample_1507.dta", replace

** this file has been MANUALLY renamed to _1500. It is the version created 5 feb 2021 16:03



************ create more characteristics for dummy dataset for PhD work



use "other_practice/census_dummy_0.001_pct.dta", clear

gen infection_status="S"

*randomly assign 10 agents to be infected
set seed 111220
gen rand = runiform()
sort rand
generate d_infected = _n <=10
sort pid new_district_id

*clean up age var 
label drop age
replace age=50 if age==999
tab age


save "other_practice/census_dummy_edited.dta", replace
export delimited using "other_practice/census_dummy_edited.csv", replace

*keep the subset of vars age, sex, econ status, pid, d_infected 
keep age sex economic_status pid d_infected
order pid sex age d_infected economic_status

save "other_practice/census_dummy_subset.dta", replace
export delimited using "other_practice/census_dummy_subset.csv", replace


/* Some old code


use "covid19-agent-based-model/data/raw/census/abm_individual_new_092220_final_merged_complete_FINAL_orig.dta"


* extracting my ~100 sample from census 
sample 0.001, by(new_district_id)

* extracting my ~1000 sample from census, making sure there are roughly similar number of obs per district

sample 0.01, by(new_district_id). // 1,507 obs

save census_sample_1,500.dta, replace
*/
