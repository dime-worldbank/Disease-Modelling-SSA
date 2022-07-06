*Purpose: take a closer look at mobility data and the extent to which the mobility patterns are correlated with cases 

cap cd "/Users/sophieayling/Documents/GitHub/Disease-Modelling-SSA/data/preprocessed/mobility/New Files/"

import delimited "daily_region_transition_probability-new-district-pre-lockdown_i5.csv", encoding(Big5)clear 

egen x = rowmean(d_1 - d_60)

bys home_region: egen av_move_by_dist=mean(x)

collapse (first) av_move_by_dist, by(home_region)
split home_region, p("_")
rename home_region2 dist_no
destring dist_no, replace
drop home_region1

sort av_move_by_dist

graph bar av_move_by_dist, over(home_region, sort(av_move_by_dist)  lab(angle(45) labsize(vsmall))) nofill title("order of average mobility by district (i5)") 
graph export "../../../output/plots/order_of_mobility_i5_dist.png", replace

scatter av_move_by_dist dist_no, mlabel(dist_no) msize(.2cm) jitter(#) mlabposition(6)
graph export "../../../output/plots/order_of_mobility_i5_scatter.png", replace

*combine with national case data


