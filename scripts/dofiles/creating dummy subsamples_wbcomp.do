************ recreate the dummy subsamples and the same 5% and 20% and 50% and 75% and 100%
clear all

cap cd "/Users/sophieayling/Documents/GitHub/Disease-Modelling-SSA"
cap cd "\Users\wb488473\OneDrive - WBG\Documents\GitHub\Disease-Modelling-SSA"
tempfile temp1 temp2


** take the 100 perc dataset with missing vars to sample on

use "data/raw/census/100_perc_sample/abm_individual_new_092320_final_merged_complete_FINAL.dta", clear

* it still needs some of the work done down below, but first cut it to the sample size i want

sample 75, by(new_district_id)
save "data/raw/census/75_perc_sample/75_perc_092320_missingvars.dta", replace
*/

***** use 75 percent version as created in commented out code above 
tempfile temp1
*use "data/raw/census/100_perc_sample/100_perc_092320_missingvars.dta", clear
sort district_id
rename district_id dist_id_88

save `temp1', replace

import delimited "data/raw/district_relation_plus_mapcode.csv", clear

merge 1:m dist_id_88 using `temp1'
drop _merge

*change the old to new district id
rename new_district_id district_id


** save new version of 75 perc dataset
tab economic_status, nol
gen economic_status2 = economic_status

*** gen teachers var 

replace economic_status2=9 if teachers !=0

la define economic_stat 0 "Not working, inactive, not in universe" 1 "Current Students" 2 "Homemakers/Housework" 3 "Office workers" 4 "Service workers" 5 "Agriculture workers" 6 "Industry Workers" 7 "In the army" 8 "Disabled and not working" 9 "Teachers"

label values economic_status2 economic_stat
tab economic_status2,m
drop economic_status 
rename economic_status2 economic_status

*create the school goers variable (this is just a dummy for now) 
*drop _merge

*clean age variable 
replace age = . if age==999

*temporary solve to python code issue
drop if age ==. 

*rename household id
rename serial household_id


*order 
order district_id district_name_shpfile 
save "data/raw/census/75_perc_sample/census_sample_75perc_070921.dta", replace
export delimited using "data\raw\census\75_perc_sample\census_sample_75perc_070921.csv", replace
****check variables in this 75 perc sample are all the same as the one in the 1500 below
e
*use "data/raw/census/100_perc_sample/census_sample_100perc_070921.dta", clear
tab economic_status



**************************************************************************************************

/*
***same thing with 091720 version

use "data/raw/census/5_perc_sample/abm_individual_new_091720.dta", clear
sort district_id
merge m:1 district_id using `temp1' 

drop district_id 
rename new_district_id district_id


** save new version of 5 perc dataset
tab economic_status, nol
gen economic_status2 = economic_status

*** gen teachers var 

replace economic_status2=9 if teachers !=0

la define economic_stat 0 "Not working, inactive, not in universe" 1 "Current Students" 2 "Homemakers/Housework" 3 "Office workers" 4 "Service workers" 5 "Agriculture workers" 6 "Industry Workers" 7 "In the army" 8 "Disabled and not working" 9 "Teachers"

label values economic_status2 economic_stat
tab economic_status2,m
drop economic_status 
rename economic_status2 economic_status

*create the school goers variable (this is just a dummy for now) 
gen school_goers = 0
replace school_goers=1 if economic_status==1 

gen manufacturing_workers=0
replace manufacturing_workers=1 if economic_status==6

drop _merge
save "data/raw/census/5_perc_sample/census_sample_5perc.dta", replace



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

/*
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

*/
************ For 5% sample: add back in missing variables from other census version -- province = geo1_zw2012



/* Some old code


use "covid19-agent-based-model/data/raw/census/abm_individual_new_092220_final_merged_complete_FINAL_orig.dta"


* extracting my ~100 sample from census 
sample 0.001, by(new_district_id)

* extracting my ~1000 sample from census, making sure there are roughly similar number of obs per district

sample 0.01, by(new_district_id). // 1,507 obs

save census_sample_1,500.dta, replace
*/
