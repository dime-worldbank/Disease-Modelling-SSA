**********************************************************************************
* Project: Zimbabwe Covid-19 Agent Based Modelling
* Module:  Creating School Seats
* Written by: Billy Hoo
* Last edited: 05/27/2020
**********************************************************************************


**********************************************************************************
* 1. Expanding and Allocating Seeds for Pre-schools
**********************************************************************************
clear
set seed 1010101

use "D:\work\Zimbabwe\Final Data\Data\School Information_092020_pre.dta",clear 

expand = Total

sort district_name_shpfile Schoolnumber

bysort district_name_shpfile : gen double r_pre = runiform()

sort district r_pre

by district_name_shpfile : gen r_order_seed_pre = _n
by district_name_shpfile : gen total_district = _N

browse district_name Schoolnumber Name r_order_seed_pre


sum total_district r_order_seed_pre if district_name == "Bikita"
sum total_district r_order_seed_pre if district_name == "Bindura"
sum total_district r_order_seed_pre if district_name == "Lupane"
sum total_district r_order_seed_pre if district_name == "Kadoma"


save "D:\work\Zimbabwe\Final Data\Data\School Information_092020_pre_expanded.dta",replace

**********************************************************************************
* 2. Expanding and Allocating Seeds for Primary Schools
**********************************************************************************
clear
set seed 1010101

use "D:\work\Zimbabwe\Final Data\Data\School Information_092020_primary.dta",clear 


expand = Total

sort district_name_shpfile Schoolnumber

bysort district_name_shpfile : gen double r_prima = runiform()

sort district r_prima

by district_name_shpfile : gen r_order_seed_prima = _n
by district_name_shpfile : gen total_district = _N

browse district_name Schoolnumber Name r_order_seed_prima total_district


sum total_district r_order_seed_prima if district_name == "Bikita"
sum total_district r_order_seed_prima if district_name == "Bindura"
sum total_district r_order_seed_prima if district_name == "Lupane"
sum total_district r_order_seed_prima if district_name == "Kadoma"




save "D:\work\Zimbabwe\Final Data\Data\School Information_092020_primary_expanded.dta",replace


**********************************************************************************
* 2. Expanding and Allocating Seeds for All Lower Secondary Schools
**********************************************************************************
clear
set seed 1010101

use "D:\work\Zimbabwe\Final Data\Data\School Information_092020_secondary.dta",clear

expand = Total_Secondary

sort district_name_shpfile Schoolnumber

bysort district_name_shpfile : gen double r_lower = runiform()

sort district r_lower

by district_name_shpfile : gen r_order_seed_lower = _n
by district_name_shpfile : gen total_district = _N

browse district_name Schoolnumber Name r_order_seed_lower total_district


sum total_district r_order_seed_lower if district_name == "Bikita"
sum total_district r_order_seed_lower if district_name == "Bindura"
sum total_district r_order_seed_lower if district_name == "Lupane"
sum total_district r_order_seed_lower if district_name == "Kadoma"


save "D:\work\Zimbabwe\Final Data\Data\School Information_092020_secondary_expanded.dta",replace


**********************************************************************************
* 1. PREPARING SCHOOL POP DATASET BY GRADE LEVELS
**********************************************************************************
clear
set seed 1010101

use "D:\work\Zimbabwe\Final Data\Data\School Information_092020_uppersecondary.dta",clear

expand = UpperSecondary

sort district_name_shpfile Schoolnumber

bysort district_name_shpfile : gen double r_upper = runiform()

sort district r_upper

by district_name_shpfile : gen r_order_seed_upper = _n
by district_name_shpfile : gen total_district = _N

browse district_name Schoolnumber Name r_order_seed_upper total_district


sum total_district r_order_seed_upper if district_name == "Bikita"
sum total_district r_order_seed_upper if district_name == "Bindura"
sum total_district r_order_seed_upper if district_name == "Lupane"
sum total_district r_order_seed_upper if district_name == "Kadoma"

save "D:\work\Zimbabwe\Final Data\Data\School Information_092020_upper_secondary_expanded.dta",replace


**********************************************************************************
* 1. PREPARING SCHOOL Teacher POP DATASET BY GRADE LEVELS
**********************************************************************************
clear
set seed 1010101

use "D:\work\Zimbabwe\Final Data\Data\School Information_092020_primary_for_teacher.dta",clear

expand = TotalTeachers

sort district_name_shpfile Schoolnumber

bysort district_name_shpfile : gen double r_p_t = runiform()

sort district r_p_t

by district_name_shpfile : gen r_order_seed_p_t = _n
by district_name_shpfile : gen total_district = _N

browse district_name Schoolnumber Name r_order_seed_p_t total_district


sum total_district r_order_seed_p_t if district_name == "Bikita"
sum total_district r_order_seed_p_t if district_name == "Bindura"
sum total_district r_order_seed_p_t if district_name == "Lupane"
sum total_district r_order_seed_p_t if district_name == "Kadoma"

save "D:\work\Zimbabwe\Final Data\Data\School Information_092020_primary_for_teacher_exp.dta",replace

**********************************************************************************
* 1. PREPARING SCHOOL Teacher POP DATASET BY GRADE LEVELS
**********************************************************************************
clear
*set seed 1010129
set seed 1010101
*set seed 1010129
*set seed 1010129

use "D:\work\Zimbabwe\Final Data\Data\School Information_092020_secondary_for_teacher.dta",clear

expand = TotalTeachers

sort district_name_shpfile Schoolnumber

bysort district_name_shpfile : gen double r_s_t = runiform()

sort district r_s_t

by district_name_shpfile : gen r_order_seed_s_t = _n
by district_name_shpfile : gen total_district = _N

browse district_name Schoolnumber Name r_order_seed_s_t total_district


sum total_district r_order_seed_s_t if district_name == "Bikita"
sum total_district r_order_seed_s_t if district_name == "Bindura"
sum total_district r_order_seed_s_t if district_name == "Lupane"
sum total_district r_order_seed_s_t if district_name == "Kadoma"

save "D:\work\Zimbabwe\Final Data\Data\School Information_092020_secondary_for_teacher_exp.dta",replace
