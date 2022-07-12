cap cd "C:\Users\WB504522\WBG\Sophie Charlotte Emi Ayling - DIME - ABM\scaling paper"
cap cd "/Users/sophieayling/Documents/GitHub/Disease-Modelling-SSA/data/output/ICCS/all"


** Purpose: recreate Sveta's District level maps (i.e. v3) of the day by day progression in whether there are cases or not in the 5 percent vs. the 25 percent sample 

set scheme s1color
graph set window fontface "Times New Roman"


**v3

forvalues x=1/10{
import delimited "myriad_test_agg_5p_v3_0.3_`x'.txt", encoding(ISO-8859-2) clear

gen s_=`x'

*denote that these are 5 percents 
rename metric* metric*_5

save s_v3_5_`x', replace

import delimited "myriad_test_agg_25p_v3_0.3_`x'.txt", encoding(ISO-8859-2) clear

gen s_=`x'

*denote that these are 25 percents 
rename metric* metric*_25

save s_v3_25_`x', replace

}

use s_v3_5_1, clear
forvalues x=2/10{
append using s_v3_5_`x'
}

tab s_
// each sample has 5,400 values which is 60 districts * 90 days 
drop v11

*for each district I want to have the median value for each day 
collapse (median) metric* , by(myid time)

save combined_samplev3_5, replace

use s_v3_25_1, clear
forvalues x=2/10{
append using s_v3_25_`x'
}

tab s_
// looking good now same as line 40
drop v11

collapse (median) metric* , by(time myid)


merge 1:1 time myid using combined_samplev3_5

sort myid time

*scale the cases so they are on the same comparable scale 
replace metric_new_cases_sympt_25=metric_new_cases_sympt_25*4
replace metric_new_cases_sympt_5=metric_new_cases_sympt_5*20

*generate cumulative case count by day for each of the 60 districts 
by myid(time): gen cum_cases_sympt25=sum(metric_new_cases_sympt_25)
by myid(time): gen cum_cases_sympt5=sum(metric_new_cases_sympt_5)

*generate a dummy for whether there has been a case in each district for each day 
gen cum_cases_5_d = 0 
replace cum_cases_5_d = 1 if cum_cases_sympt5 >0 
replace cum_cases_5_d = . if cum_cases_sympt5 ==. 

tab cum_cases_5_d,m

gen cum_cases_25_d = 0 
replace cum_cases_25_d = 1 if cum_cases_sympt25 >0 
replace cum_cases_25_d = . if cum_cases_sympt25 ==. 

tab cum_cases_25_d,m

*generate a scaled cumulative deaths 
replace metric_died_count_5 = metric_died_count_5*20
replace metric_died_count_25= metric_died_count_25*4

* create a total deaths per district at end of simulation dataset and export 
preserve 
collapse (max) metric_died_count*, by (myid)
split myid, parse ("_")
rename myid2 district_id 
destring district_id, replace
drop myid myid1
gen ratio_dead=metric_died_count_5/metric_died_count_25
order district_id
sort district_id
// generate the ratios 
export excel using "../../../map_input/cum_died_25_5_comparison.xlsx", first(varl) replace

restore


* drop down to the essential variables for plotting 

keep myid time cum_*
split myid, parse ("_")
rename myid2 district_id 
destring district_id, replace
drop myid myid1
sort district_id 

* segment and reshape dataset with days along the top (wide) , districts long

rename time day
// first variable of interest for 5% - dummy :
preserve

keep day district_id cum_cases_5_d
reshape wide cum_cases_5_d, i(district_id) j(day)
keep district_id cum_cases_5_d1 cum_cases_5_d5 cum_cases_5_d10 cum_cases_5_d10 cum_cases_5_d15 cum_cases_5_d20 cum_cases_5_d25 cum_cases_5_d30
export excel using "../../../map_input/cum_cases_dummy_5p_multidist_pd10.xlsx", first(varl) replace

restore

// first variable of interest for 25% - dummy :

preserve

keep day district_id cum_cases_25_d
reshape wide cum_cases_25_d, i(district_id) j(day)
keep district_id cum_cases_25_d1 cum_cases_25_d5 cum_cases_25_d10 cum_cases_25_d15 cum_cases_25_d20 cum_cases_25_d25 cum_cases_25_d30
export excel using "../../../map_input/cum_cases_dummy_25p_multidist_pd10.xlsx", first(varl) replace

restore

// second variable of interest for 5% - cum cases num :
preserve

keep day district_id cum_cases_sympt5
reshape wide cum_cases_sympt5, i(district_id) j(day)
keep district_id cum_cases_sympt51 cum_cases_sympt55 cum_cases_sympt510 cum_cases_sympt515 cum_cases_sympt520 cum_cases_sympt525 cum_cases_sympt530
export excel using "../../../map_input/cum_cases_num_5p_multidist_pd10.xlsx", first(varl) replace

restore

// first variable of interest for 25% - cum cases num:

preserve

keep day district_id cum_cases_sympt25
reshape wide cum_cases_sympt25, i(district_id) j(day)
keep district_id cum_cases_sympt251 cum_cases_sympt255 cum_cases_sympt250 cum_cases_sympt2515 cum_cases_sympt2520 cum_cases_sympt2525 cum_cases_sympt2530
export excel using "../../../map_input/cum_cases_num_25p_multidist_pd10.xlsx", first(varl) replace

restore
// do this for the rest of them (the keep only few days for the maps) 

e
