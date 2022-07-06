cd "C:\Users\WB504522\WBG\Sophie Charlotte Emi Ayling - DIME - ABM\scaling paper"


set scheme s1color
graph set window fontface "Times New Roman"

**v1


forvalues x=1/10{
import delimited "myriad_test_agg_5p_v1_0.3_`x'.txt", encoding(ISO-8859-2) clear

gen sample=`x'

save samplev1_5_`x', replace

import delimited "myriad_test_agg_25p_v1_0.3_`x'.txt", encoding(ISO-8859-2) clear

gen sample=`x'

save samplev1_25_`x', replace

}

use samplev1_5_1, clear
forvalues x=2/10{
append using samplev1_5_`x'
}

tab sample
drop v1

collapse (median) metric* , by(time myid)

rename metric* metric*_5

save combined_samplev1_5, replace

use samplev1_25_1, clear
forvalues x=2/10{
append using samplev1_25_`x'
}

tab sample
drop v1

collapse (median) metric* , by(time myid)

rename metric* metric*_25

merge 1:1 time myid using combined_samplev1_5

sort myid time

replace metric_new_deaths_25=metric_new_deaths_25*4
replace metric_new_deaths_5=metric_new_deaths_5*20
twoway (line metric_new_deaths_5 time ) (line metric_new_deaths_25 time, lpattern(-)) , legend(label(1 "5% sample") label(2 "25% sample")) xtitle(Days) ytitle(New Deaths) title("Daily New Deaths, with Demographics (Version 1)", size(medium))
graph export new_deaths_v1.png, replace

replace metric_new_cases_sympt_25=metric_new_cases_sympt_25*4
replace metric_new_cases_sympt_5=metric_new_cases_sympt_5*20
twoway (line metric_new_cases_sympt_5 time ) (line metric_new_cases_sympt_25 time, lpattern(-)) , legend(label(1 "5% sample") label(2 "25% sample")) xtitle(Days) ytitle(New Cases) title("Daily New Cases, with Demographics (Version 1)", size(medium))
graph export new_cases_v1.png, replace

twoway (line metric_new_cases_sympt_5 time ) (line metric_new_cases_sympt_25 time, lpattern(-)) if time<30, legend(label(1 "5% sample") label(2 "25% sample")) xtitle(Days) ytitle(New Cases) title("Daily New Cases, with Demographics  (Version 1)", size(medium))
graph export new_cases_day30_v1.png, replace



gen ratio=metric_new_cases_sympt_5/metric_new_cases_sympt_25

twoway (line ratio time)

replace metric_died_count_5=metric_died_count_5*20
replace metric_died_count_25=metric_died_count_25*4
gen ratio_dead=metric_died_count_5/metric_died_count_25

twoway (line ratio_dead time) if metric_died_count_5>0

sort time
gen tot_sympt5=sum(metric_new_cases_sympt_5)
gen tot_sympt25=sum(metric_new_cases_sympt_25)

gen ratio_sympt=tot_sympt5/tot_sympt25

twoway (line ratio_sympt time) , ylabel(.6(.2) 1.2)  yline(1, lpattern(.))  xtitle(Days) ytitle("Ratio") title("Ratio of Cumulative Cases in 5% Sample to 25% Sample," "with Demographics  (Version 1)", size(medium))
graph export ratio_cum_cases_v1.png, replace


replace metric_new_cases_asympt_25=metric_new_cases_asympt_25*4
replace metric_new_cases_asympt_5=metric_new_cases_asympt_5*20




**v3

forvalues x=1/10{
import delimited "myriad_test_agg_5p_v3_0.3_`x'.txt", encoding(ISO-8859-2) clear

gen sample=`x'

save samplev3_5_`x', replace

import delimited "myriad_test_agg_25p_v3_0.3_`x'.txt", encoding(ISO-8859-2) clear

gen sample=`x'

save samplev3_25_`x', replace

}

use samplev3_5_1, clear
forvalues x=2/10{
append using samplev3_5_`x'
}

tab sample
drop v1

collapse (median) metric* , by(time myid)

rename metric* metric*_5

save combined_samplev3_5, replace

use samplev3_25_1, clear
forvalues x=2/10{
append using samplev3_25_`x'
}

tab sample
drop v1

collapse (median) metric* , by(time myid)

rename metric* metric*_25

merge 1:1 time myid using combined_samplev3_5

sort myid time


bys time: egen totdead25=sum(metric_new_deaths_25)
bys time: egen totdead5=sum(metric_new_deaths_5)

replace metric_new_cases_sympt_25=metric_new_cases_sympt_25*4
replace metric_new_cases_sympt_5=metric_new_cases_sympt_5*20

bys time: egen totcases_sympt25=sum(metric_new_cases_sympt_25)
bys time: egen totcases_sympt5=sum(metric_new_cases_sympt_5)

sort myid time
by myid: gen cum5=sum(metric_new_cases_sympt_5)
by myid: gen cum25=sum(metric_new_cases_sympt_25)
by myid: gen tot_asympt5=sum(metric_new_cases_asympt_5)
by myid: gen tot_asympt25=sum(metric_new_cases_asympt_25)

gen dummy5=cum5>0
gen dummy25=cum25>0
bys time: egen tot_dist5=sum(dummy5)
bys time: egen tot_dist25=sum(dummy25)

gen dummyall5=cum5>0 | tot_asympt5>0
gen dummyall25=cum25>0 | tot_asympt25>0

bys time: egen tot_distall5=sum(dummyall5)
bys time: egen tot_distall25=sum(dummyall25)

graph bar (sum) dummyall5 (sum) dummyall25 if time<40, over(time, label(labsize(vsmall))) bar(2, fcolor(gray) lcolor(navy)) bar(1, fcolor(dknavy) lcolor(dknavy)) ytitle(Number of Districts) legend(order(1 "5 percent" 2 "25 percent"))
graph export first.png, replace



duplicates drop time, force
replace totdead25=totdead25*4
replace totdead5=totdead5*20
twoway (line totdead5 time) (line totdead25 time, lpattern(-)) , legend(label(1 "5% sample") label(2 "25% sample")) xtitle(Days) ytitle(New Deaths) title("Daily New Deaths, with Demographics and Spatial Mobility (Version 3)", size(medium))
graph export new_deaths_v3.png, replace
twoway (line totcases_sympt5 time ) (line totcases_sympt25 time, lpattern(-)) , legend(label(1 "5% sample") label(2 "25% sample")) xtitle(Days) ytitle(New Cases) title("Daily New Cases, with Demographics and Spatial Mobility (Version 3)", size(medium))
graph export new_cases_v3.png, replace

twoway (line totcases_sympt5 time ) (line totcases_sympt25 time, lpattern(-)) if time<30, legend(label(1 "5% sample") label(2 "25% sample")) xtitle(Days) ytitle(New Cases) title("Daily New Cases, with Demographics and Spatial Mobility (Version 3)", size(medium))
graph export new_cases_day30_v3.png, replace

gen ratio=totcases_sympt5/totcases_sympt25
twoway (line ratio time)

sort time
gen tot_sympt5=sum(totcases_sympt5)
gen tot_sympt25=sum(totcases_sympt25)




twoway (line tot_sympt5 time) (line tot_sympt25 time) if time<40

gen ratio_sympt=tot_sympt5/tot_sympt25

br time tot_sympt5 tot_sympt25 ratio_sympt totcases_sympt5 totcases_sympt25

twoway (line ratio_sympt time) , yline(1, lwidth(thin) lpattern(.))  xtitle(Days) ytitle("Ratio") title("Ratio of Cumulative Cases in 5% Sample to 25% Sample," "with Demographics and Spatial Mobility (Version 3)", size(medium))
graph export ratio_cum_cases_v3.png, replace


restore

preserve
replace metric_new_deaths_25=metric_new_deaths_25*4
replace metric_new_deaths_5=metric_new_deaths_5*20
twoway (line metric_new_deaths_5 time if myid=="d_2") (line metric_new_deaths_25 time if myid=="d_2") 
restore

preserve
replace metric_new_deaths_25=metric_new_deaths_25*4
replace metric_new_deaths_5=metric_new_deaths_5*20
twoway (line metric_new_deaths_5 time if myid=="d_29") (line metric_new_deaths_25 time if myid=="d_29") 
restore

preserve
replace metric_new_deaths_25=metric_new_deaths_25*4
replace metric_new_deaths_5=metric_new_deaths_5*20
twoway (line metric_new_deaths_5 time ) (line metric_new_deaths_25 time ) , by(myid, rescale)
restore


*******m1

forvalues x=1/10{
import delimited "myriad_test_agg_5p_m1_0.3_`x'.txt", encoding(ISO-8859-2) clear

gen sample=`x'

save samplem1_5_`x', replace

import delimited "myriad_test_agg_25p_m1_0.3_`x'.txt", encoding(ISO-8859-2) clear

gen sample=`x'

save samplem1_25_`x', replace

}

use samplem1_5_1, clear
forvalues x=2/10{
append using samplem1_5_`x'
}

tab sample
drop v1

collapse (median) metric* , by(time myid)

rename metric* metric*_5

save combined_samplem1_5, replace

use samplem1_25_1, clear
forvalues x=2/10{
append using samplem1_25_`x'
}

tab sample
drop v1

collapse (median) metric* , by(time myid)

rename metric* metric*_25

merge 1:1 time myid using combined_samplem1_5

sort myid time

replace metric_new_deaths_25=metric_new_deaths_25*4
replace metric_new_deaths_5=metric_new_deaths_5*20
twoway (line metric_new_deaths_5 time ) (line metric_new_deaths_25 time ) 

replace metric_new_cases_sympt_25=metric_new_cases_sympt_25*4
replace metric_new_cases_sympt_5=metric_new_cases_sympt_5*20
twoway (line metric_new_cases_sympt_5 time ) (line metric_new_cases_sympt_25 time) 

gen ratio=metric_new_cases_sympt_5/metric_new_cases_sympt_25

twoway (line ratio time)

sort time
gen tot_sympt5=sum(metric_new_cases_sympt_5)
gen tot_sympt25=sum(metric_new_cases_sympt_25)

gen ratio_sympt=tot_sympt5/tot_sympt25

twoway (line metric_new_cases_sympt_5 time ) (line metric_new_cases_sympt_25 time, lpattern(-)) if time<30, legend(label(1 "5% sample") label(2 "25% sample")) xtitle(Days) ytitle(New Cases) title("Daily New Cases, with Demographics  (Version 2)", size(medium))
graph export new_cases_day30_m1.png, replace


twoway (line ratio_sympt time)  , ylabel(.6(.2) 1.2)  yline(1, lpattern(.))  xtitle(Days) ytitle("Ratio") title("Ratio of Cumulative Cases in 5% Sample to 25% Sample," "with Demographics  (Version 2)", size(medium))
graph export ratio_cum_cases_m1.png, replace



replace metric_new_cases_asympt_25=metric_new_cases_asympt_25*4
replace metric_new_cases_asympt_5=metric_new_cases_asympt_5*20





use samplev3_25_1, clear
forvalues x=2/10{
append using samplev3_25_`x'
}

tab sample
drop v1

collapse (median) metric* , by(time myid)

rename metric* metric*_25

merge 1:1 time myid using combined_samplev3_5

replace metric_new_cases_sympt_25=metric_new_cases_sympt_25*4
replace metric_new_cases_sympt_5=metric_new_cases_sympt_5*20
replace metric_new_deaths_25=metric_new_deaths_25*4
replace metric_new_deaths_5=metric_new_deaths_5*20


sort myid time
by myid: gen tot_sympt5=sum(metric_new_cases_sympt_5)
by myid: gen tot_sympt25=sum(metric_new_cases_sympt_25)


keep if time==30 | time==60 | time==89
gen ratio_deaths=metric_died_count_5/metric_died_count_25
gen ratio_sympt=tot_sympt5/tot_sympt25

keep myid metric_died_count* tot_sympt* ratio_deaths ratio_symp time

reshape wide metric_died_count* tot_sympt* ratio_deaths ratio_symp, i(myid) j(time)

gen id=substr(myid,3,2)
destring id, replace
tab id

export delimited spatial_spread.csv, replace


use samplev3_25_1, clear
forvalues x=2/10{
append using samplev3_25_`x'
}

tab sample
drop v1

collapse (median) metric* , by(time myid)

rename metric* metric*_25

merge 1:1 time myid using combined_samplev3_5

replace metric_new_cases_sympt_25=metric_new_cases_sympt_25*4
replace metric_new_cases_sympt_5=metric_new_cases_sympt_5*20
replace metric_new_deaths_25=metric_new_deaths_25*4
replace metric_new_deaths_5=metric_new_deaths_5*20


sort myid time
by myid: gen tot_sympt5=sum(metric_new_cases_sympt_5)
by myid: gen tot_sympt25=sum(metric_new_cases_sympt_25)

sort myid time
by myid: gen tot_asympt5=sum(metric_new_cases_asympt_5)
by myid: gen tot_asympt25=sum(metric_new_cases_asympt_25)

gen dummy5=tot_sympt5>0
gen dummy25=tot_sympt25>0

gen dummyall5=tot_sympt5>0 | tot_asympt5>0
gen dummyall25=tot_sympt25>0 | tot_asympt25>0

bys time: egen tot_dist5=sum(dummy5)
bys time: egen tot_dist25=sum(dummy25)

bys time: egen tot_distall5=sum(dummyall5)
bys time: egen tot_distall25=sum(dummyall25)

twoway (line tot_dist5 time) (line tot_dist25 time)  (line tot_distall5 time) (line tot_distall25 time)


keep if time==0 | time==5 | time==10 | time==15 | time==20 | time==25

keep myid dummyall5 dummyall25 time

reshape wide dum*, i(myid) j(time)

gen id=substr(myid,3,2)
destring id, replace
tab id

export delimited spatial_spread_first25.csv, replace