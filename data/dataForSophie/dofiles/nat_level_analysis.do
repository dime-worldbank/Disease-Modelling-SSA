cd "C:\Users\wb488473\WBG\Data analysis to inform Global Water Operations - WB Group - Documents\Zim COVID - via Teams\08_Data\files for Sarah\outputs_gitignored\"

*use the national level dataset 
**use "processed/model_outputs_nat_level.dta", replace
**use "processed/model_outputs_nat_level_180days.dta", replace
use "processed/model_outputs_nat_level_p016_120_18062021.dta", replace

*drop after day 80
*drop if day >80

*generate and graph daily case numbers
line new_sympt day, title("National predictions: New symptomatic cases by day")
graph export "plots/national/m_new_sympt_day_nat_p016_120_18062021.png", replace


*generate and graph daily deaths numbers
line new_deaths day, title("National predictions: New deaths by day")
graph export "plots/national/m_new_deaths_day_nat_p016_120_18062021.png", replace

*generate and graph hospitalizations and critical cases
line new_hosp day, title("National predictions: New hospitalization by day")
graph export "plots/national/m_new_hosp_day_nat_p016_120_18062021.png", replace

line new_crit day, title("National predictions: New critical by day")
graph export "plots/national/m_new_crit_day_nat_p016_120_18062021.png", replace

*create cumulative totals for case numbers & deaths
gen country="zim"
bysort country (day): gen cumul_cases=sum(new_sympt)

line cumul_cases day, title("National predictions: Cumulative symptomatic")
graph export "plots/national/m_cumul_cases_nat_p016_120_18062021.png", replace

bysort country (day): gen cumul_deaths=sum(new_deaths)
line cumul_deaths day, title("National predictions: Cumulative deaths")
graph export "plots/national/m_cumul_deaths_nat_p016_120_18062021.png", replace


*create cumulative totals for hospitalization & critical
bysort country (day): gen cumul_hosp=sum(new_hosp)

line cumul_hosp day, title("National predictions: Cumulative hospitalization")
graph export "plots/national/m_cumul_hosp_nat_p016_120_18062021.png", replace

bysort country (day): gen cumul_crit=sum(new_crit)
line cumul_crit day, title("National predictions: Cumulative critical")
graph export "plots/national/m_cumul_crit_nat_p016_120_18062021.png", replace

*if the variable is correctly labeled then this should be equivalent to died_count
line died_count day, title("National predictions: Cumulative deaths")
graph export "plots/national/m_died_count_nat_p016_120_18062021.png", replace
//this shows it is fine so we only need to keep died count

/*
*lagged cumulative case numbers
by day: gen lag=cumul_cases[_n-1]
gen new=lag==0 & cumul_cases!=0
tab day if new==1