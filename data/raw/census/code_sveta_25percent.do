cap cd "C:\Users\wb488473\OneDrive - WBG\Documents\GitHub\Disease-Modelling-SSA\data

use "raw/census/5_perc_sample/abm_092320_5_perc_080222_ver1.dta", clear

expand 5

bys person_id: replace cycle=_n
tab cycle
sort cycle household_id person_id

drop person_id
gen person_id=_n
sort household_id
tostring(household_id), replace
gen num="num"
egen new_id=concat(household_id  num cycle)
drop household_id num person_id_cycle
rename new_id household_id

save "C:\Users\WB504522\WBG\Sophie Charlotte Emi Ayling - census\25_perc_sample\abm_092320_25_perc_080222_ver1", replace


use "C:\Users\WB504522\WBG\Sophie Charlotte Emi Ayling - census\5_perc_sample\abm_092320_5_perc_080222_ver3.dta", clear
sort household_id_cycle
drop person_id_cycle household_id
expand 5

bys person_id: replace cycle=_n
tab cycle
sort cycle household_id person_id
drop person_id
gen person_id=_n
gen num="num"
tostring(household_id), replace
egen new_id=concat(household_id  num cycle)
drop household_id num 
rename new_id household_id

save "C:\Users\WB504522\WBG\Sophie Charlotte Emi Ayling - census\25_perc_sample\abm_092320_25_perc_080222_ver3", replace



use "C:\Users\WB504522\WBG\Sophie Charlotte Emi Ayling - census\5_perc_sample\abm_092320_5_perc_080222_var-m1.dta", clear
expand 5
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

save "C:\Users\WB504522\WBG\Sophie Charlotte Emi Ayling - census\25_perc_sample\abm_092320_25_perc_080222_var-m1", replace


**clean up the 5% samples so they all have only one var for person id and for hh id

use "C:\Users\WB504522\WBG\Sophie Charlotte Emi Ayling - census\5_perc_sample\abm_092320_5_perc_080222_ver1.dta", clear
drop person_id_cycle
save "C:\Users\WB504522\WBG\Sophie Charlotte Emi Ayling - census\5_perc_sample\abm_092320_5_perc_080222_ver1.dta", replace

use "C:\Users\WB504522\WBG\Sophie Charlotte Emi Ayling - census\5_perc_sample\abm_092320_5_perc_080222_ver3.dta"
drop person_id_cycle household_id
rename household_id_cycle household_id
save "C:\Users\WB504522\WBG\Sophie Charlotte Emi Ayling - census\5_perc_sample\abm_092320_5_perc_080222_ver3.dta", replace