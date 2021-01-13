cd "/Users/sophieayling/Documents/GitHub/"
/*


use "covid19-agent-based-model/data/raw/census/abm_individual_new_092220_final_merged_complete_FINAL_orig.dta"


* extracting my ~100 sample from census 
sample 0.001, by 

* extracting my ~1000 sample from census, making sure there are roughly similar number of obs per district

sample 0.01, by(new_district_id). // 1,507 obs

save census_sample_1,500.dta, replace
*/
************ create more characteristics for dummy dataset



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
