*checks on characteristics of 5 perc sample 
use "C:\Users\wb488473\OneDrive - WBG\Documents\GitHub\Disease-Modelling-SSA\data\preprocessed\census\census_sample_5perc_040521.dta", clear

* check av age per district 
bys district_id: egen av_age_dist=mean(age)
gen over_59 = .
replace over_59=1 if age >=60

collapse (mean)age (count)over_59, by(district_id district_name_shpfile)

