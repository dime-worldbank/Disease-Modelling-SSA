/*Purpose: Examine the spatially disaggregated model outputs vis-a-vis the mobility patterns seen across Zimbabwe from the original OD matrix.

We want to see to what extent the infection and deaths that the model predicted according to a beta of (.3?) reflected where in the country saw the highest share of inbound and outbound movement

Note: it is i5 data that is used in the model
*/

cd "/Users/sophieayling/Documents/GitHub/Disease-Modelling-SSA/data"

tempfile temp1


import excel using "preprocessed/mobility/New Files/Most mobile districts i5.xlsx", firstrow clear


import delimited using "raw/shapefiles/new_districts/ZWE_adm2.csv",clear

rename id_2 dist_id
rename name_2 dist_name
duplicates drop dist_id, force

save `temp1', replace

import delimited using "preprocessed/mobility/New Files/daily_region_transition_probability-new-district-pre-lockdown_i5.csv", clear

split home_region, parse("_")
destring home_region2, replace

rename home_region2 dist_id

merge m:1 dist_id using `temp1'

order dist_id dist_name

 *collapse to get summary datasets 
 
 collapse (mean) d_1     d_2     d_3     d_4     d_5     d_6     d_7     d_8     d_9     d_10    d_11    d_12  d_13    d_14    d_15    d_16    d_17    d_18    d_19    d_20    d_21    d_22    d_23    d_24    d_25    d_26    d_27    d_28    d_29    d_30    d_31    d_32    d_33    d_34    d_35    d_36    d_37    d_38    d_39  d_40    d_41    d_42    d_43    d_44    d_45    d_46    d_47    d_48    d_49    d_50    d_51    d_52    d_53 d_54    d_55    d_56    d_57    d_58    d_59    d_60, by(dist_id dist_name home_region)

 * generate a variable that is the proportion of travel from each district to another district (i.e. not home)
 foreach x of numlist 1/60 {
         gen n_d`x' = 100-d_`x' if dist_id ==`x'
  }

  local d_ids d_1     d_2     d_3     d_4     d_5     d_6     d_7     d_8     d_9     d_10    d_11    d_12  d_13    d_14    d_15    d_16    d_17    d_18    d_19    d_20    d_21    d_22    d_23    d_24    d_25    d_26    d_27    d_28    d_29    d_30    d_31    d_32    d_33    d_34    d_35    d_36    d_37    d_38    d_39  d_40    d_41    d_42    d_43    d_44    d_45    d_46    d_47    d_48    d_49    d_50    d_51    d_52    d_53 d_54    d_55    d_56    d_57    d_58    d_59    d_60, by(dist_id dist_name home_region)

   drop `d_ids'

   
   egen perc_pop_left_dist = rowtotal(n_d1 - n_d60)


   replace perc_pop_left_dist = perc_pop_left_dist/100
   
    drop n_d*
	rename dist_id district_id
	sort district_id



 
