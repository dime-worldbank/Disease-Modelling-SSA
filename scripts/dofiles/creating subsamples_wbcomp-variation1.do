
/* ******************************************

Program name: ABM scale up
Author: SA
Date last modified: 
Project:  ABM
Purpose:   scale back up from original 5%

****************************************** */

***0. SET STATA 
clear matrix
clear
set more off

************ recreate the subsamples by taking the original IPUMS 5% and then scale back UP to 20% and 50% for the paper. Creating different versions:

/*

1. No age, all households of 6 (synthetic) (variation -1)
2. With real age and households (variation 1)
3. With real age, household, multi district (variation 3)

Multiply each by 4  to create 20% 
(consider 50% later)

*/


** Keep only the core variables from IPUMS That is: 	person_id	age	sex	household_id	district_id	economic_status	economic_activity_location_id	school_goers	manufacturing_workers

************ recreate the dummy subsamples and the same 5% and 20% and 50% and 75% and 100%
clear all

cap cd "/Users/sophieayling/Documents/GitHub/Disease-Modelling-SSA"
cap cd "\Users\wb488473\OneDrive - WBG\Documents\GitHub\Disease-Modelling-SSA"
tempfile temp1 temp2


** take the 100 perc dataset with missing vars to sample on

use "data/raw/census/100_perc_sample/abm_individual_new_092320_final_merged_complete_FINAL.dta", clear

* it still needs some of the work done down below, but first cut it to the sample size i want - using new method from Billy using cycle 


// ages and hh size stay the same - just rename the household id cycle as this is the identifier 



keep pernum age sex serial new_district_id economic_status school_goers manufacturing_workers cycle  serial_cycle_pernum


rename serial household_id
rename serial_cycle_pernum person_id
rename new_district_id district_id

// keep person_id	age sex	household_id district_id economic_status school_goers manufacturing_workers --  economic activity location id 

**create variation 1 on 100 perc*

*replace district_id with a single identifier 
replace district_id = 1


*make everyone have the same econ status
decode economic_status, gen(ec_st)
tab ec_st, nol
replace ec_st = "Default"
drop economic_status pernum
rename ec_st economic_status

order person_id household_id district_id

save "data/raw/census/100_perc_sample/abm_092320_100_perc_080222_ver1.dta", replace



*********************************************** CREATE 5%
destring cycle, replace
keep if cycle == 1
tab cycle

sort person_id household_id

order person_id	age sex	household_id district_id economic_status school_goers manufacturing_workers 

*check household size 
bysort household_id: gen tot= _N 
sum tot // mean number of hhs = 6 
drop tot 
*check no. hh members
codebook person_id   // 654,688
codebook household_id  // 160,728



save "data/raw/census/5_perc_sample/abm_092320_5_perc_080222_ver1.dta", replace

save `temp1', replace


*********************************************** CREATE 25%

expand 5

bys person_id: replace cycle=_n
tab cycle
sort cycle household_id person_id

*replacing all person ids and hh ids to correspond to 25%

drop person_id
gen person_id=_n
sort household_id
tostring(household_id), replace
gen num="num"
egen new_id=concat(household_id  num cycle)
drop household_id num 
rename new_id household_id

*check household size 
bysort household_id: gen tot= _N 
sum tot // mean number of hhs = 6 
drop tot 
*check no. hh members
codebook person_id   // 3.2 million
codebook household_id // 803,640 hhs
save "data/raw/census/25_perc_sample/abm_092320_25_perc_080222_ver1.dta", replace


*********************************************** CREATE 50 from 5%

use "data/raw/census/5_perc_sample/abm_092320_5_perc_080222_ver1.dta", clear

expand 10

bys person_id: replace cycle=_n
tab cycle
sort cycle household_id person_id

drop person_id
gen person_id=_n
sort household_id
tostring(household_id), replace
gen num="num"
egen new_id=concat(household_id  num cycle)
drop household_id num 
rename new_id household_id


*check household size 
bysort household_id: gen tot= _N 
sum tot // mean number of hhs = 6 
drop tot 
*check no. hh members
codebook person_id   // 6.5 million
codebook household_id  // 1.6 million

save "data/raw/census/50_perc_sample/abm_092320_50_perc_080222_ver1.dta", replace

*********************************************** CREATE 75 from 5%


use "data/raw/census/5_perc_sample/abm_092320_5_perc_080222_ver1.dta", clear

expand 15

bys person_id: replace cycle=_n
tab cycle
sort cycle household_id person_id

drop person_id
gen person_id=_n
sort household_id
tostring(household_id), replace
gen num="num"
egen new_id=concat(household_id  num cycle)
drop household_id num
rename new_id household_id


*check household size 
bysort household_id: gen tot= _N 
sum tot // mean number of hhs = 6 
drop tot 
*check no. hh members
codebook person_id   // 9.8 million
codebook household_id // 2.4 million


save "data/raw/census/75_perc_sample/abm_092320_75_perc_080222_ver1.dta", replace