**********************************************************************************
* Project: Zimbabwe Covid-19 Agent Based Modelling
* Module:  Creating School Population Datasets by Grade Levels
* Written by: Billy Hoo
* Last edited: 09/21/2020
**********************************************************************************

**********************************************************************************
* 1. PREPARING SCHOOL POP DATASET BY GRADE LEVELS
**********************************************************************************


** Import Pre-school Dataset (6,634)
clear
import excel "D:\work\Zimbabwe\School Wards\School_Population.xlsx", sheet("School Population Pre-school") firstrow clear
browse
drop if Total == . // excel file accidentally expanded to blank rows
save "D:\work\Zimbabwe\Final Data\Data\School Information_092020_pre.dta",replace


** Import Primary Dataset (6,126 schools)
clear
import excel "D:\work\Zimbabwe\School Wards\School_Population.xlsx", sheet("School Population Primary") firstrow clear
browse
drop if Total ==.
drop if Total ==0
save "D:\work\Zimbabwe\Final Data\Data\School Information_092020_primary.dta",replace


** Import Secondary Dataset (2,953) (To be divided to lower and upper secondary schools)
clear
import excel "D:\work\Zimbabwe\School Wards\School_Population.xlsx", sheet("School Population Students Sec") firstrow clear
browse
drop if Total ==.
drop if Total ==0
save "D:\work\Zimbabwe\Final Data\Data\School Information_092020_secondary.dta",replace

**Lower Secondary Schools Only (1,871)
clear
use "D:\work\Zimbabwe\Final Data\Data\School Information_092020_secondary.dta",clear
keep if UpperSecondary == 0
drop UpperSecondary
save "D:\work\Zimbabwe\Final Data\Data\School Information_092020_lowersecondary.dta",replace

**Upper Secondary Schools Only (1,082)
clear
use "D:\work\Zimbabwe\Final Data\Data\School Information_092020_secondary.dta",clear
keep if UpperSecondary != 0
drop Total_Secondary 
drop LowerSecondary
save "D:\work\Zimbabwe\Final Data\Data\School Information_092020_uppersecondary.dta",replace

** Import All School data with Boarding School Information with Hotseating Info included 
clear
import excel "D:\work\Zimbabwe\School Wards\School_Population_new_all.xlsx", sheet("All") firstrow clear


preserve
collapse (sum) TotalTeachers_Sc_data TotalStudents_Sc_data Total_Pre Total_Primary Total_Secondary , by(district_name)
export excel using "D:\work\Zimbabwe\Final Data\School Information\schooldata_district_population_092220.xls", firstrow(variables) replace
restore

save "D:\work\Zimbabwe\Final Data\Data\School Information_092020_all.dta",replace


**********************************************************************************
* 1. PREPARING SCHOOL TEACHER DATASET BY GRADE LEVELS
**********************************************************************************


** Import All Primary School Dataset (6671)
clear
import excel "D:\work\Zimbabwe\School Wards\School_Population.xlsx", sheet("School Population Prim Teachers") firstrow clear
browse
drop if Total == .
drop if Schoolnumber == 12010
drop if Schoolnumber == 12011
drop if Schoolnumber == 12013
drop if Schoolnumber == 12017
drop if Schoolnumber == 13025
drop if Schoolnumber == 1267
drop if Schoolnumber == 16005
drop if Schoolnumber == 1007
drop if Schoolnumber == 12430
drop if Schoolnumber == 8017
drop if Schoolnumber == 10008
drop if Schoolnumber == 2014


save "D:\work\Zimbabwe\Final Data\Data\School Information_092020_primary_for_teacher.dta",replace



** Import All Secondary School Dataset (2953)
clear
import excel "D:\work\Zimbabwe\School Wards\School_Population.xlsx", sheet("School Population Second Teach") firstrow clear
browse
drop if Schoolnumber == 1419

drop if Total == .
save "D:\work\Zimbabwe\Final Data\Data\School Information_092020_secondary_for_teacher.dta",replace


stop
/*Codes below no longer used
**********************************************************************************
* 1.Setting Schoool "SEATS"
**********************************************************************************

**Ordered
sort Schoolnumber
bysort Ed_dist Schoolnumber: gen order = _n


**********************************************************************************
* 1.Setting District "SEATS"
**********************************************************************************

**Ordered
sort Ed_dist SchoolLevel Schoolnumber

bysort Ed_dist SchoolLevel: gen order_district = _n



**Random
set seed 12345
by Ed_dist SchoolLevel: generate double r = runiform()
sort Ed_dist SchoolLevel r

by Ed_dist SchoolLevel : gen r_order_district = _n

stop

sum Total order order_district r_order_district if Ed_dist == "BeitBridge"
sum Total order order_district r_order_district if Ed_dist == "BeitBridge" & SchoolLevel == "Primary"
sum Total order order_district r_order_district if Ed_dist == "BeitBridge" & SchoolLevel == "Secondary"


save "D:\work\Zimbabwe\Final Data\Data\School Information Expanded_0920200.dta",replace

**********************************************************************************
* 1.Setting District "SEATS"
**********************************************************************************
preserve

drop if SchoolLevel == "Secondary"


save "D:\work\Zimbabwe\Final Data\Data\School Information Expanded_091820_primary.dta",replace


restore
**********************************************************************************
* 1.Setting District "SEATS"
**********************************************************************************

drop if SchoolLevel == "Primary"


save "D:\work\Zimbabwe\Final Data\Data\School Information Expanded_091820_secondary.dta",replace
*/
