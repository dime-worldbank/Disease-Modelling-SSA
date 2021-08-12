
cd "C:\Users\wb488473\WBG\Data analysis to inform Global Water Operations - WB Group - Documents\Zim COVID - via Teams\08_Data\files for Sarah\outputs_gitignored\"

tempfile temp1

*use district level dataset
**use "processed/model_outputs_dist_level.dta", replace
use "processed/model_outputs_dist_level_p016_120_18062021", replace
destring dist_no, replace


*drop after day 80
**drop if day >80

*add in district names
sort dist_no
save `temp1', replace

import delimited "processed/district_names.csv", encoding(ISO-8859-9) clear
sort dist_no
 
merge 1:m dist_no using `temp1'
drop _merge

*sort ready for next stage
sort dist_no day
*******************************CUMULATIVE DATA**************************



*create cumulative totals for case numbers & deaths (this needs correcting)
bysort district (day): gen cumul_cases=sum(new_sympt)
scatter cumul_cases day, title("District predictions: Cumulative symptomatic")
graph export "plots/district/m_cumul_cases_dist_p016_120_18062021.png", replace

*don't need to make cumul_deaths as we have died_count
line died_count day, title("National predictions: Cumulative deaths")
graph export "plots/district/m_cumul_deaths_dist_p016_120_18062021.png", replace

* create cumulative for hosp and crit
bysort dist_no (day): gen cumul_hosp=sum(new_hosp)
line cumul_hosp day, title("District predictions: Cumulative Hospitalizations")
graph export "plots/district/m_cumul_hosp_dist_p016_120_18062021.png", replace

scatter cumul_hosp day, title("District predictions: Cumulative Hospitalizations") msize(small)
graph export "plots/district/m_cumul_hosp_dist_scatter_p016_120_18062021.png", replace


bysort district (day): gen cumul_crit=sum(new_crit)
line cumul_crit day, title("District predictions: Cumulative Critical Cases")
graph export "plots/district/m_cumul_crit_dist_p016_120_18062021.png", replace


*******************************COMPARE DISTRICTS COLLAPSE **************

*preserve 
*when is the first occasion in each district where the value of cumul_cases is at least 1 (https://www.stata.com/support/faqs/data-management/first-and-last-occurrences/) 

by district(day), sort: gen byte first = sum(inrange(cumul_cases, 1,.)) == 1  & sum(inrange(cumul_cases[_n - 1],1,.)) == 0  
 
 *create a variable to indicate which day that was 
 
gen day_first_case = day if first ==1 
e
*collapse at district level to keep the total number of cases, hospitalizations, critical and deaths by district, and the day of the first case by district 
collapse (max) cumul_cases cumul_hosp cumul_crit died_count day_first_case, by(dist_no district district_name_shpfile)

save "processed/model_outputs_collapsed_dist_p016_120_18062021.dta", replace
export excel "processed/model_outputs_collapsed_dist_p016_120_18062021.xlsx", first(var) replace


*make graphs of which district came first
*and the number of cases in each district on that day
scatter day_first_case day, mlabel(district_name_shpfile) msize(.5cm) 

keep if day_first_case !=.
graph bar cumul_cases, over(district_name_shpfile, sort(day_first_case)) nofill title("no. cases on first day of cases in that district")
graph export "plots/district/cases_districts_at_start.png", replace


graph bar day_first_case, over(district_name_shpfile, sort(day_first_case)) nofill title("order of cases appearing by district")
graph export "plots/district/first_days_districts.png", replace


twoway scatter cumul_cases day_first_case, mlabel(district_name_shpfile) xtitle("day of first case in district") ytitle("cumulative cases in district on that day") title("First cases in districts")

graph export "plots/district/first_day_cases_district.png", replace

restore





*******************************CASE DATA*******************************


preserve
*just cases by district by days
keep day dist_no district new_sympt
xtset dist_no day
set scheme cleanplots, perm

*to be improved graphed cases by district overlaid
xtline new_sympt, overlay title("Daily cases by district") scheme(cleanplots)  legend(size(tiny))

graph export "plots/district/m_new_sympt_district_day_overlay_p016_120_18062021.png", replace

*can include above but for some reason colours are not there
*refer here for more tips https://medium.com/the-stata-guide/covid-19-visualizations-with-stata-part-2-customizing-color-schemes-206af77d00ce


*graphs for each district individually as a panel 
twoway (line new_sympt day), by(district) 
graph export "plots/district/m_new_sympt_district_day_p016_120_18062021.png", replace

*reshape the data to play with in excel, r or python for better visuals
rename new_sympt ns_
reshape wide ns_, i(district dist_no) j(day)
sort dist_no
export excel "processed/model_outputs_daily_cases_dist_reshape_p016_120_18062021.xlsx", first(var) replace



restore

*******************************HOSP & CRIT DATA************************

*to be improved graphed cases by district overlaid
sort dist_no day
xtset dist_no day
xtline new_hosp, overlay title("Daily Hospitalizations by district") scheme(cleanplots)  legend(size(tiny))

xtset dist_no day
xtline new_crit, overlay title("Daily Critical by district") scheme(cleanplots)  legend(size(tiny))

*******************************DEATH DATA*******************************

preserve
*just deaths by district by days
keep day dist_no district new_deaths died_count


sort dist_no day
xtset dist_no day
set scheme s1color


*to be improved graphed cases by district overlaid
xtline new_deaths, overlay title("Daily deaths by district") scheme(cleanplots)  legend(size(tiny))

*graphs for each district individually as a panel 
twoway (line new_deaths day), by(district) 
graph export "plots/district/m_new_deaths_district_day_p016_120_18062021.png", replace

*graph for cumulative deaths for each district overlaid
xtline new_deaths, overlay title("Daily deaths by district") scheme(cleanplots)  legend(size(tiny))
graph export "plots/district/m_cumul_deaths_district_day_p016_120_18062021.png", replace


*reshape the data 
rename died died_
reshape wide died_, i(district dist_no) j(day)
sort dist_no
export excel "processed/model_outputs_daily_deaths_dist_reshape_p016_120_18062021.xlsx", first(var)replace

restore



