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

keep pernum age sex serial new_district_id economic_status school_goers manufacturing_workers cycle serial_cycle serial_cycle_pernum


rename serial household_id
rename serial_cycle household_id_cycle
rename serial_cycle_pernum person_id_cycle
rename pernum person_id 
rename new_district_id district_id

// keep person_id	age sex	household_id district_id economic_status school_goers manufacturing_workers --  economic activity location id 

*** CHECKS BEFORE

tabout age using "data/raw/census/checks/age_100p.xls", c(col freq) replace
tabout sex using "data/raw/census/checks/sex_100p.xls", c(col freq) replace
tabout school_goers using "data/raw/census/checks/school_goers_100p.xls", c(col freq)  replace
tabout economic_status using "data/raw/census/checks/econ_status_100p.xls", c(col freq) replace
tabout manufacturing_workers using "data/raw/census/checks/manu_workers_100p.xls", c(col freq) replace
tabout district_id using "data/raw/census/checks/district_id_100p.xls", c(col freq)  replace


** to keep the 75 percent sample 
destring cycle, replace
tab cycle
keep if inrange(cycle, 1,18 )  // through 18

save "data/raw/census/75_perc_sample/abm_individual_new_092320_75_perc_080222.dta", replace

*** CHECKS AFTER

tabout age using "data/raw/census/checks/age_75p.xls", c(col freq) replace
tabout sex using "data/raw/census/checks/sex_75p.xls", c(col freq) replace
tabout school_goers using "data/raw/census/checks/school_goers_75p.xls", c(col freq)  replace
tabout economic_status using "data/raw/census/checks/econ_status_75p.xls", c(col freq) replace
tabout manufacturing_workers using "data/raw/census/checks/manu_workers_75p.xls", c(col freq) replace
tabout district_id using "data/raw/census/checks/district_id_75p.xls", c(col freq)  replace



** to keep the 50 percent sample 
destring cycle, replace
tab cycle
keep if inrange(cycle, 1,12 )  // through 12

save "data/raw/census/50_perc_sample/abm_individual_new_092320_50_perc_080222.dta", replace

*** CHECKS AFTER

tabout age using "data/raw/census/checks/age_50p.xls", c(col freq) replace
tabout sex using "data/raw/census/checks/sex_50p.xls", c(col freq) replace
tabout school_goers using "data/raw/census/checks/school_goers_50p.xls", c(col freq)  replace
tabout economic_status using "data/raw/census/checks/econ_status_50p.xls", c(col freq) replace
tabout manufacturing_workers using "data/raw/census/checks/manu_workers_50p.xls", c(col freq) replace
tabout district_id using "data/raw/census/checks/district_id_50p.xls", c(col freq)  replace


***to keep the 25 percent sample 

keep if inrange(cycle, 1,6)  // through 6
tab cycle
save "data/raw/census/25_perc_sample/abm_individual_new_092320_25_perc_080222.dta", replace

*** CHECKS AFTER
tabout age using "data/raw/census/checks/age_25p.xls", c(col freq) replace
tabout sex using "data/raw/census/checks/sex_25p.xls", c(col freq)  replace
tabout school_goers using "data/raw/census/checks/school_goers_25p.xls", c(col freq) replace
tabout economic_status using "data/raw/census/checks/econ_status_25p.xls", c(col freq) replace
tabout manufacturing_workers using "data/raw/census/checks/manu_workers_25p.xls", c(col freq) replace
tabout district_id using "data/raw/census/checks/district_id_25p.xls", c(col freq) replace



** to keep the original IPUMS 5 percent

keep if cycle == 1
tab cycle
save "data/raw/census/5_perc_sample/abm_individual_new_092320_5_perc_080222.dta", replace

*** CHECKS AFTER
tabout age using "data/raw/census/checks/age_5p.xls", c(col freq) replace
tabout sex using "data/raw/census/checks/sex_5p.xls", c(col freq)  replace
tabout school_goers using "data/raw/census/checks/school_goers_5p.xls", c(col freq) replace
tabout economic_status using "data/raw/census/checks/econ_status_5p.xls", c(col freq) replace
tabout manufacturing_workers using "data/raw/census/checks/manu_workers_5p.xls", c(col freq) replace
tabout district_id using "data/raw/census/checks/district_id_5p.xls", c(col freq) replace


