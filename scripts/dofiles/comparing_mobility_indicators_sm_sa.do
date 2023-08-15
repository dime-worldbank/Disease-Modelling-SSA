
*** Sophie to adapt the following file to run on her computer, also so that we can reaggregate the mobility data in different ways with different end date cut offs that reflect the lockdown changes more accurately

*Specifically...
/*
Phase 1 - Level 4 - no interdistrict movement - 30March2020 - 17May2020
Phase 2 - Level 2 - interdistrict travel resumed - 17May2020 - 22July2020
Phase 3 - Level 4 - no interdistrict movement (1st wave) - 22July - 30Sept2020

// only use i5 indicator so remove i9, and reaggregate in that way.
*/
cap cd "C:\Users\wb504522\OneDrive - WBG\COVID 19\COVID 19 Results\proof-of-concept\"
cap cd "/Users/sophieayling/Library/CloudStorage/OneDrive-UniversityCollegeLondon/GitHub/Disease-Modelling-SSA/data/preprocessed/mobility/New Files/"

 global input panel_indicators\clean
 global intermediate databricks-results\zw\intermediate_data
 global output outputs\figures
 
 global input_dash files_for_dashboard\files_clean\
 
**************************************************
 **************************************************
 ************INDICATOR 3***************************
 ***********ADMIN LEVEL 2**************************
 **************************************************
***GET DAILY POP VALUES TO USE AS DENOMINATOR
 clear
 
import delimited "input/i3_2.csv", clear
gen date2=date(date,"MDY")
drop day date
format %td date2
rename count day_pop
rename region region_lag
save "intermediate/ind3_pop", replace


**************************************************
 **************************************************
 ************INDICATOR 5***************************
 ***********ADMIN2 LEVEL**************************
 ************************************************** 

import delimited "input/i5_2.csv", clear 
gen date2=date(date,"MDY")
format %td date2
drop date con
rename region_to region
rename region_from region_lag

merge m:1 region_lag date2 using "intermediate/ind3_pop"
keep if _merge==3
// full merge

gen period="pre" if date<=mdy(3,15,2020) & date!=mdy(2,12,2020) & date!=mdy(2,1,2020)
replace period="apr" if month(date)==4
replace period="jun" if month(date)==6
drop if period==""
gen dow=dow(date) 
gen weekday=inlist(dow,1,2,3,4,5)
drop _merge date2 dow

gen prop1=total_count/day_pop*100
gen prop2=od_count/day_pop*100
gen prop3=subscriber_count/day_pop*100

bys weekday region region_lag period: egen tot_counttot_i5=sum(total_count)
bys weekday region region_lag period: egen tot_countod_i5=sum(od_count)
bys weekday region region_lag period: egen tot_countsub_i5=sum(subscriber_count)
bys weekday region region_lag period: egen tot_pcttot_i5=sum(prop1)
bys weekday region region_lag period: egen tot_pctod_i5=sum(prop2)
bys weekday region region_lag period: egen tot_pctsub_i5=sum(prop3)

gen days=8 if (period=="apr" | period=="jun") & weekday==0
replace days=13 if period=="pre" & weekday==0
replace days=29 if period=="pre" & weekday==1
replace days=22 if (period=="apr" | period=="jun") & weekday==1

gen avg_counttot_i5=tot_counttot_i5/days
gen avg_countod_i5=tot_countod_i5/days
gen avg_countsub_i5=tot_countsub_i5/days
gen avg_pcttot_i5=tot_pcttot_i5/days
gen avg_pctod_i5=tot_pctod_i5/days
gen avg_pctsub_i5=tot_pctsub_i5/days

duplicates drop weekday region period region_lag, force
keep region region_lag period weekday avg*
sum avg_pcttot avg_pctod avg_pctsub
*keep if region_lag==2 | region_lag==10 

rename region_lag home_region
bys home_region weekday period: egen tot=sum(avg_pcttot)
sum tot, detail

save "intermediate/ind5_move", replace

********************************************************************************************************************************
**PRODUCE FILES FOR BASE MODEL

******************************************************************Indicator 5

import delimited "input/i5_2.csv", clear 
gen date2=date(date,"MDY")
format %td date2
drop date con
rename region_to region
rename region_from region_lag
fillin region region_lag date2
replace total_count=0 if total_count==.
merge m:1 region_lag date2 using "intermediate/ind3_pop"
keep if _merge==3

gen period="pre" if date<=mdy(3,15,2020) & date!=mdy(2,12,2020) & date!=mdy(2,1,2020)
replace period="apr" if month(date)==4
replace period="jun" if month(date)==6
drop if period==""
gen dow=dow(date) 

drop _merge date2 

gen prop1=total_count/day_pop*100

bys dow region region_lag period: egen avg_pcttot_i5=mean(prop1)

duplicates drop dow region period region_lag, force
keep region region_lag period dow avg*

rename region_lag home_region


bys home_region dow period: egen tot=sum(avg_pcttot)
sum tot, detail
replace avg=100-tot if home_region==region

keep region home_region period dow avg*
tostring(home_region), replace
rename dow weekday
replace home_region="d_" + home_region
sort home_region period weekday region
by home_region period weekday: gen d_=sum(avg)
replace d_=round(d_, 0.0001)
drop avg

preserve
keep if period=="pre"
drop period
reshape wide d_, i(home_region weekday) j(region)
export delimited "output/daily_region_transition_probability-new-district-pre-lockdown_i5.csv", replace
restore

preserve
keep if period=="apr"
drop period
reshape wide d_, i(home_region weekday) j(region)
export delimited "output/daily_region_transition_probability-new-district-post-lockdown_i5.csv", replace
restore

*****************************************************************Most mobile districts

**Indicator 5

import delimited "input/i5_2.csv", clear 
gen date2=date(date,"MDY")
format %td date2
drop date con
rename region_to region
rename region_from home_region
keep if date2<mdy(3,15,2020)
fillin region home_region date2
replace total_count=0 if total_count==.
drop if region==home_region
keep home_region date region total
bys date2 home_region: egen tot=sum(total)
bys date2 region: egen tot_enter=sum(total)

preserve
duplicates drop date2 home_region, force
bys home_region: egen avg_exit=mean(tot) 
duplicates drop home_region, force
keep home_region avg
gsort -avg
gen order=_n-1
drop avg
tempfile test
save `test', replace
restore

duplicates drop date2 region, force
bys region: egen avg_enter=mean(tot_enter) 
duplicates drop region, force
keep region avg
gsort -avg
gen order=_n-1
drop avg
merge 1:1 order using `test'
tostring(home_region), replace
replace home_region="d_" + home_region
tostring(region), replace
replace region="d_" + region
drop _merge
rename region inbound_district
rename home_region outbound_district

export excel using "output/Most mobile districts i5.xlsx", firstrow(variables) replace
