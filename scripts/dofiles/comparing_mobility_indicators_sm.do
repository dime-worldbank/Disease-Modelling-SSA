set scheme plottig


 cd "C:\Users\wb504522\OneDrive - WBG\COVID 19\COVID 19 Results\proof-of-concept\"

 global input panel_indicators\clean
 global intermediate databricks-results\zw\intermediate_data
 global output outputs\figures
 
 global input_dash files_for_dashboard\files_clean\

 **************************************************
 **************************************************
 ************INDICATOR 9***************************
 ***********ADMIN LEVEL 2**************************
 **************************************************

 import delimited "$input\i9_2.csv", clear 

gen date2=date(date,"YMD")
format %td date2
drop date day
drop if count==.
bys date2: egen test2=mean( mean_duration )
twoway (line test2 date2)
***Because of monthly calculations, duration for second half of month is off and shouldn't be used from March onward

replace mean_duration=. if (day(date2)>22 & month(date2)>2) | (day(date2)<3 & month(date2)==2)
replace stdev_duration=. if (day(date2)>22 & month(date2)>2) | (day(date2)<3 & month(date2)==2)
drop test2
bys date2: egen test2=mean( mean_duration )
twoway (line test2 date2)

bys  date2 home_region: egen tot=sum(count)

gen dow=dow(date) 
gen pct_reg=count/tot*100

**focus on Lilongwe
*keep if home_region==2 | home_region==10
gen period="pre" if date<=mdy(3,15,2020) & date!=mdy(2,12,2020) & date!=mdy(2,12,2020)
replace period="apr" if month(date)==4
replace period="jun" if month(date)==6
drop if period==""
bys region home_region period: gen test=_N
sum test
tab test period, column
***for some region pairs looks like they only have a few observations, should include 0s when calculating the average

gen weekday=inlist(dow,1,2,3,4,5)
keep region home_region count pct_reg period weekday mean stdev

bys weekday region home_region period: egen avg_dur_i9=mean(mean)
bys weekday region home_region period: egen avg_sddur_i9=mean(stdev)
bys weekday region home_region period: egen tot_count_i9=sum(count)
bys weekday region home_region period: egen tot_pct_i9=sum(pct_reg)

bys region home_region period weekday: gen test=_N
tab test period if weekday==0
tab test period if weekday==1
gen days=8 if (period=="apr" | period=="jun") & weekday==0
replace days=14 if period=="pre" & weekday==0
replace days=29 if period=="pre" & weekday==1
replace days=22 if (period=="apr" | period=="jun") & weekday==1
gen avg_count_i9=tot_count_i9/days
gen avg_pct_i9=tot_pct_i9/days

duplicates drop weekday region period home_region, force
keep region home_region period weekday avg*

gen imputed_dur_i9=(avg_dur)==. 
**fill in duration for the cases where it's missing using the weekend or weekday obs that is not missing
sort region home_region period avg_dur_i9
by region home_region period: carryforward avg_dur, replace
by region home_region period: carryforward avg_sd, replace
***for those where both are missing in the period, use the value from the other period
gsort region home_region weekday -avg_dur_i9
by region home_region weekday: carryforward avg_dur, replace
by region home_region weekday: carryforward avg_sd, replace

***fill in anything remaining with whatever data is available
sort region home_region avg_dur_i9
by region home_region: carryforward avg_dur, replace
by region home_region: carryforward avg_sd, replace

***for the remaining ones apply the avg*
bys weekday period: egen testdur=mean(avg_dur)
bys weekday period: egen testsd=mean(avg_sd)

replace avg_dur=testdur if avg_dur==.
replace avg_sd=testsd if avg_sd==.
drop test*

save "$intermediate\ind9_move", replace

**************************************************
 **************************************************
 ************INDICATOR 3***************************
 ***********ADMIN LEVEL 2**************************
 **************************************************
***GET DAILY POP VALUES TO USE AS DENOMINATOR
 clear
 
import delimited "$input\i3_2.csv", clear
gen date2=date(date,"YMD")
drop day date
format %td date2
rename count day_pop
rename region region_lag
save "$intermediate\ind3_pop", replace

**************************************************
 **************************************************
 ************INDICATOR 10***************************
 ***********ADMIN2 LEVEL**************************
 ************************************************** 

import delimited "$input\i10_2.csv", clear 
gen date2=date(date,"YMD")
format %td date2
drop date day
***check issues with duration variable
bys date2: egen test2=mean( avg_duration_destination)
twoway (line test2 date2)
***Because of monthly calculations, duration for second half of month is off and shouldn't be used from March onward

replace avg_duration_destination=. if (day(date2)>14 & month(date2)==3) | (day(date2)<5 & month(date2)==2) | (day(date2)>21 & month(date2)>3)
replace stddev_duration_destination=. if (day(date2)>14 & month(date2)==3) | (day(date2)<5 & month(date2)==2) | (day(date2)>21 & month(date2)>3)
replace avg_duration_origin=. if (day(date2)>15 & month(date2)>2) | (day(date2)<5 & month(date2)==2)
replace stddev_duration_origin=. if (day(date2)>15 & month(date2)>2) | (day(date2)<5 & month(date2)==2)
replace avg_duration_destination=. if avg_duration_destination<0

drop test2
bys date2: egen test2=mean( avg_duration_destination )
twoway (line test2 date2)

bys date2: egen test=sum(count_origin)
twoway (line test date2)

keep region region_lag count_origin date2 avg_duration_destination stddev_duration_destination avg* std*
drop if count_origin<15
drop if count_origin==.
merge m:1 region_lag date2 using "$intermediate\ind3_pop"
keep if _merge==3

gen period="pre" if date<mdy(3,15,2020) & date!=mdy(2,12,2020) & date!=mdy(1,12,2020)
replace period="apr" if month(date)==4
replace period="jun" if month(date)==6
drop if period==""
gen dow=dow(date) 
gen weekday=inlist(dow,1,2,3,4,5)
drop _merge date2 dow

gen prop=count_origin/day_pop*100

bys weekday region region_lag period: egen avg_count_i10=mean(count)
bys weekday region region_lag period: egen avg_pct_i10=mean(prop)
bys weekday region region_lag period: egen avg_pop=mean(day)
bys weekday region region_lag period: egen avg_dur_destination_i10=mean(avg_duration_destination)
bys weekday region region_lag period: egen avg_sddur_destination_i10=mean(stddev_duration_destination)
bys weekday region region_lag period: egen avg_dur_origin_i10=mean(avg_duration_origin)
bys weekday region region_lag period: egen avg_sddur_origin_i10=mean(stddev_duration_origin)
drop avg_duration* std*

duplicates drop weekday region period region_lag, force
keep region region_lag period weekday avg*
sum avg_pct
*keep if region_lag==2 | region_lag==10 

rename region_lag home_region

save "$intermediate\ind10_move", replace


**************************************************
 **************************************************
 ************INDICATOR 5***************************
 ***********ADMIN2 LEVEL**************************
 ************************************************** 

import delimited "$input\i5_2.csv", clear 
gen date2=date(date,"YMD")
format %td date2
drop date con
rename region_to region
rename region_from region_lag

merge m:1 region_lag date2 using "$intermediate\ind3_pop"
keep if _merge==3

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

save "$intermediate\ind5_move", replace

********************************************************************COMBINE DATASETS

use "$intermediate\ind9_move",clear

merge 1:1 region home_region period weekday using "$intermediate\ind5_move"

drop _merge

merge 1:1 region home_region period weekday using "$intermediate\ind10_move"

drop _merge

save "$intermediate\combined_ind_move", replace

pwcorr avg_pct_i9 avg_pct_i10
***corr .93

pwcorr avg_pct_i9 avg_pcttot_i5
***97%
pwcorr avg_pct_i9 avg_pctod_i5
***98%
pwcorr avg_pct_i9 avg_pctsub_i5
***96%

pwcorr avg_pct_i10 avg_pcttot_i5

twoway scatter avg_count_i9 avg_count_i10 if region!=home_region

twoway scatter avg_count_i9 avg_counttot_i5 if region!=home_region

********************************************************************Clean up COMBINEd DATASET

***create complete matrix

clear
set ob 60
gen home_region=_n
expand 60
bys home_region: gen region=_n
tab home_region
tab region
expand 3
sort home_region region
by home_region region: gen period="pre" if _n==1
by home_region region: replace period="apr" if _n==2
by home_region region: replace period="jun" if _n==3
expand 2
sort home_region region period
by home_region region period: gen weekday=0 if _n==1
by home_region region period: replace weekday=1 if _n==2

save "$intermediate\complete_reg_matrix", replace

use "$intermediate\combined_ind_move",clear

drop avg_pop avg_pct_i10 avg_count_i10 avg_pctsub_i5 avg_pctod_i5 avg_countsub_i5 avg_countod_i5 avg_count_i9 avg_counttot_i5

sort home_region period weekday tot
by home_region period weekday: carryforward tot, replace

replace avg_pcttot_i5=100-tot if home_region==region
sum avg_pcttot_i5 if home_region==region
pwcorr avg_pct_i9 avg_pcttot_i5
pwcorr avg_pct_i9 avg_pcttot_i5 if home_region==region
*twoway scatter avg_pct_i9 avg_pcttot_i5 if home_region==region
drop tot

merge 1:1 home_region region period weekday using "$intermediate\complete_reg_matrix"

local var avg_pct_i9 avg_dur_i9 avg_sddur_i9 avg_pcttot_i5 

foreach x in `var'{
replace `x'=0 if `x'==.
}

gen imputed_dur_i10=(avg_dur_destination_i10==. & avg_pcttot!=0)

**fill in duration for the cases where it's missing using the weekend or weekday obs that is not missing
sort region home_region period avg_dur_destination
by region home_region period: carryforward avg_dur_destination if imputed_dur_i10==1, replace
by region home_region period: carryforward avg_sddur_destination if imputed_dur_i10==1, replace
by region home_region period: carryforward avg_dur_origin if imputed_dur_i10==1, replace
by region home_region period: carryforward avg_sddur_origin if imputed_dur_i10==1, replace
***for those where both are missing in the period, use the value from the other period
gsort region home_region weekday -avg_dur_destination
by region home_region weekday: carryforward avg_dur_destination if imputed_dur_i10==1, replace
by region home_region weekday: carryforward avg_sddur_destination if imputed_dur_i10==1, replace
by region home_region weekday: carryforward avg_dur_origin if imputed_dur_i10==1, replace
by region home_region weekday: carryforward avg_sddur_origin if imputed_dur_i10==1, replace

***fill in anything remaining with whatever data is available
sort region home_region avg_dur_destination
by region home_region: carryforward avg_dur_destination if imputed_dur_i10==1, replace
by region home_region : carryforward avg_sddur_destination if imputed_dur_i10==1, replace
by region home_region : carryforward avg_dur_origin if imputed_dur_i10==1, replace
by region home_region : carryforward avg_sddur_origin if imputed_dur_i10==1, replace

***for the remaining ones apply the avg*
bys weekday period: egen testdur=mean(avg_dur_destination)
bys weekday period: egen testsd=mean(avg_sddur_destination)
bys weekday period: egen testduror=mean(avg_dur_origin)
bys weekday period: egen testsdor=mean(avg_sddur_origin)

replace avg_dur_destination=testdur if avg_dur_destination==. & imputed_dur_i10==1
replace avg_sddur_destination=testsd if avg_sddur_destination==. & imputed_dur_i10==1
replace avg_dur_origin=testduror if avg_dur_origin==. & imputed_dur_i10==1
replace avg_sddur_origin=testsdor if avg_sddur_origin==. & imputed_dur_i10==1

drop test*

local var avg_dur_destination_i10 avg_sddur_destination_i10 avg_dur_origin_i10 avg_sddur_origin_i10

foreach x in `var'{
replace `x'=0 if avg_pcttot==0
}

drop _merge
sum *
replace imputed_dur_i9=0 if imputed_dur_i9==.

label variable region "Destination"
label variable home_region "Origin"
label variable period "Observation Period"
label variable weekday "Dummy for Weekday Avg"
label variable avg_dur_i9 "Average Duration in Destination i9, Seconds"
label variable avg_sddur_i9 "Average Standard Deviation in Duration in Destination i9, Seconds"
label variable avg_pct_i9 "Average Percent of Origin Pop Spending Majority Time in Destination"
label variable imputed_dur_i9 "Filled in missing duration"
label variable avg_pcttot_i5 "Average Percent of Daily Origin People in Destination"
label variable avg_dur_destination_i10 "Average Duration in Destination i10, Seconds"
label variable avg_sddur_destination_i10 "Average Standard Deviation in Duration in Destination i10, Seconds"
label variable avg_dur_origin_i10 "Average Duration in Origin i10, Seconds"
label variable avg_sddur_origin_i10 "Average Standard Deviation in Duration in Origin i10, Seconds"
label variable imputed_dur_i10 "Filled in Missing Duration"

export delimited "$intermediate\combined_ind_move.csv", replace

************************************************************************************************************************************************************************************************
**PRODUCE FILES FOR BASE MODEL

 **************************************************
 **************************************************
 ************INDICATOR 9***************************
 ***********ADMIN LEVEL 2**************************
 **************************************************

 ***indicator 9
 import delimited "$input\i9_2.csv", clear 

gen date2=date(date,"YMD")
format %td date2
drop date day
drop if count==.

fillin region home_region date2

gen dow=dow(date) 
replace count=0 if count==.

gen period="pre" if date<=mdy(3,15,2020) & date!=mdy(2,12,2020) & date!=mdy(2,12,2020)
replace period="apr" if month(date)==4
replace period="jun" if month(date)==6
drop if period==""

keep region home_region count period dow 

bys dow region home_region period: egen avg_count_i9=mean(count)

duplicates drop dow region period home_region, force
bys  dow period home_region: egen tot=sum(avg_count_i9)
gen avg_pct_i9=avg_count_i9/tot*100

keep region home_region period dow avg_pct_i9
rename dow weekday
sort home_region period weekday region
by home_region period weekday: gen d_=sum(avg)

tostring(home_region), replace
replace home_region="d_" + home_region
replace d_=round(d_, 0.0001)
drop avg

preserve
keep if period=="pre"
drop period
reshape wide d_, i(home_region weekday) j(region)
export delimited "C:\Users\WB504522\WBG\Yi Rong Hoo - Zimbabwe COVID-19\08_Data\files for Sarah\git hub copy sa 19th Dec 2020\data - SHAREPOINT\preprocessed\mobility\New Files\daily_region_transition_probability-new-district-pre-lockdown_i9.csv", replace
restore

preserve
keep if period=="apr"
drop period
reshape wide d_, i(home_region weekday) j(region)
export delimited "C:\Users\WB504522\WBG\Yi Rong Hoo - Zimbabwe COVID-19\08_Data\files for Sarah\git hub copy sa 19th Dec 2020\data - SHAREPOINT\preprocessed\mobility\New Files\daily_region_transition_probability-new-district-post-lockdown_i9.csv", replace
restore

******************************************************************Indicator 5

import delimited "$input\i5_2.csv", clear 
gen date2=date(date,"YMD")
format %td date2
drop date con
rename region_to region
rename region_from region_lag
fillin region region_lag date2
replace total_count=0 if total_count==.
merge m:1 region_lag date2 using "$intermediate\ind3_pop"
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
export delimited "C:\Users\WB504522\WBG\Yi Rong Hoo - Zimbabwe COVID-19\08_Data\files for Sarah\git hub copy sa 19th Dec 2020\data - SHAREPOINT\preprocessed\mobility\New Files\daily_region_transition_probability-new-district-pre-lockdown_i5.csv", replace
restore

preserve
keep if period=="apr"
drop period
reshape wide d_, i(home_region weekday) j(region)
export delimited "C:\Users\WB504522\WBG\Yi Rong Hoo - Zimbabwe COVID-19\08_Data\files for Sarah\git hub copy sa 19th Dec 2020\data - SHAREPOINT\preprocessed\mobility\New Files\daily_region_transition_probability-new-district-post-lockdown_i5.csv", replace
restore

*****************************************************************Most mobile districts

 ***indicator 9
 import delimited "$input\i9_2.csv", clear 

gen date2=date(date,"YMD")
format %td date2
drop date day
drop if count==.
keep if date2<mdy(3,15,2020)
fillin region home_region date2
replace count=0 if count==.
drop if region==home_region
bys date2 home_region: egen tot=sum(count)
bys date2 region: egen tot_enter=sum(count)

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

export excel using "C:\Users\WB504522\WBG\Yi Rong Hoo - Zimbabwe COVID-19\08_Data\files for Sarah\git hub copy sa 19th Dec 2020\data\preprocessed\mobility\New Files\Most mobile districts i9.xlsx", firstrow(variables) replace

**Indicator 5

import delimited "$input\i5_2.csv", clear 
gen date2=date(date,"YMD")
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

export excel using "C:\Users\WB504522\WBG\Yi Rong Hoo - Zimbabwe COVID-19\08_Data\files for Sarah\git hub copy sa 19th Dec 2020\data\preprocessed\mobility\New Files\Most mobile districts i5.xlsx", firstrow(variables) replace

*****************************************************************Duration

import delimited "$input\i9_2.csv", clear 

gen date2=date(date,"YMD")
format %td date2
drop date day
drop if count==.
replace mean_duration=. if (day(date2)>22 & month(date2)>2) | (day(date2)<3 & month(date2)==2)
replace stdev_duration=. if (day(date2)>22 & month(date2)>2) | (day(date2)<3 & month(date2)==2)

gen dow=dow(date) 
keep region home_region dow  mean stdev

bys dow region home_region: egen avg_dur_i9=mean(mean)
bys dow region home_region: egen avg_sddur_i9=mean(stdev)

duplicates drop dow region home_region, force
keep region home_region dow avg*

**fill in duration for the cases where it's missing 
sort region home_region avg_dur_i9
by region home_region: carryforward avg_dur, replace
by region home_region: carryforward avg_sd, replace

***for the remaining ones apply the avg*
bys dow: egen testdur=mean(avg_dur)
bys dow: egen testsd=mean(avg_sd)

replace avg_dur=testdur if avg_dur==.
replace avg_sd=testsd if avg_sd==.
drop test*
rename dow weekday
fillin region home_region weekday
drop _f
replace avg_dur=avg_dur/(60*60)
replace avg_sd=avg_sd/(60*60)
sum avg*
export delimited "C:\Users\WB504522\WBG\Yi Rong Hoo - Zimbabwe COVID-19\08_Data\files for Sarah\git hub copy sa 19th Dec 2020\data\preprocessed\mobility\New Files\weekday_mobility_duration_count_df-new-district i9.csv", replace

**indicator 10

import delimited "$input\i10_2.csv", clear 
gen date2=date(date,"YMD")
format %td date2
drop date day

replace avg_duration_destination=. if (day(date2)>14 & month(date2)==3) | (day(date2)<5 & month(date2)==2) | (day(date2)>21 & month(date2)>3)
replace stddev_duration_destination=. if (day(date2)>14 & month(date2)==3) | (day(date2)<5 & month(date2)==2) | (day(date2)>21 & month(date2)>3)
replace avg_duration_destination=. if avg_duration_destination<0
drop if count_origin<15
drop if count_origin==.
keep region region_lag date2 avg_duration_destination stddev_duration_destination 

gen weekday=dow(date) 
bys weekday region region_lag : egen avg_dur_destination_i10=mean(avg_duration_destination)
bys weekday region region_lag : egen avg_sddur_destination_i10=mean(stddev_duration_destination)

drop avg_duration* std* date

duplicates drop weekday region region_lag, force
rename region_lag home_region

**fill in duration for the cases where it's missing 
sort region home_region avg_dur_
by region home_region: carryforward avg_dur, replace
by region home_region: carryforward avg_sd, replace

***for the remaining ones apply the avg*
bys weekday: egen testdur=mean(avg_dur)
bys weekday: egen testsd=mean(avg_sd)

replace avg_dur=testdur if avg_dur==.
replace avg_sd=testsd if avg_sd==.
drop test*
fillin region home_region weekday
drop _f
replace avg_dur=avg_dur/(60*60)
replace avg_sd=avg_sd/(60*60)
sum avg*

export delimited "C:\Users\WB504522\WBG\Yi Rong Hoo - Zimbabwe COVID-19\08_Data\files for Sarah\git hub copy sa 19th Dec 2020\data\preprocessed\mobility\New Files\weekday_mobility_duration_count_df-new-district i5", replace
