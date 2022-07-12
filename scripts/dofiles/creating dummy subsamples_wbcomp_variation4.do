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
3. With real age, household, multi district, single econ status (variation 3)
4. With real age, household, multi district, multi econ statuses (variation 4)

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

keep age sex serial new_district_id economic_status school_goers manufacturing_workers cycle serial_cycle serial_cycle_pernum

rename serial household_id
rename serial_cycle_pernum person_id
rename new_district_id district_id


// keep person_id	age sex	household_id district_id economic_status school_goers manufacturing_workers --  economic activity location id 

*******************************create variation 4**********************


/* characteristics:
-  multi district
-  original econ status
-  real ages
-  real hh sizes
*/

*** to keep 100 percent sample but with only key vars 

save "data/raw/census/100_perc_sample/abm_092320_100_perc_080222_ver4.dta", replace


** to keep the 75 percent sample 
destring cycle, replace
tab cycle
keep if inrange(cycle, 1,18 )  // through 18

save "data/raw/census/75_perc_sample/abm_092320_75_perc_080222_ver4.dta", replace

** to keep the 50 percent sample 
destring cycle, replace
tab cycle
keep if inrange(cycle, 1,12 )  // through 12

save "data/raw/census/50_perc_sample/abm_092320_50_perc_080222_ver4.dta", replace

***to keep the 25 percent sample 

keep if inrange(cycle, 1,6)  // through 6
tab cycle
save "data/raw/census/25_perc_sample/abm_092320_25_perc_080222_ver4.dta", replace

** to keep the original IPUMS 5 percent

keep if cycle == 1
tab cycle
save "data/raw/census/5_perc_sample/abm_092320_5_perc_080222_ver4.dta", replace




