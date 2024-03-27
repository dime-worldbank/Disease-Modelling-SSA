*Purpose: get occupational breakdown by district


cap cd "/Users/sophieayling/Library/CloudStorage/OneDrive-UniversityCollegeLondon/GitHub/Disease-Modelling-SSA/data/raw/"

tempfile temp1


use "census/100_perc/abm_individual_new_092320_final_merged_complete_FINAL.dta", clear

tab new_district_id economic_status, row nofreq

* percentage in each occupational category by district for export
table new_district_id, stat(fvpercent economic_status)  nformat(%5.2f)
collect export "../analysis/census/occ_by_dist_perc.xls", replace


* number in each occupational category by district for export
table new_district_id (economic_status) 
collect export "../analysis/census/occ_by_dist_n.xls", replace
