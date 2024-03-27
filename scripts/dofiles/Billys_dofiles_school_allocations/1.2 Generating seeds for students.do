**********************************************************************************
* Project: Zimbabwe Covid-19 Agent Based Modelling
* Module:  Creating Expanded Dataset with Random Seats/Seeds for Students
* Written by: Billy Hoo
* Last edited: 05/27/2020
**********************************************************************************


**********************************************************************************
* 1. Expanding and Allocating Seeds for Pre-schools
**********************************************************************************
clear
set seed 1010101

use "D:\work\Zimbabwe\Final Data\Data\5_percent_sample\abm_individual_new_091720_pre_stu.dta" , clear

expand 23

format PID_str %20.0g // already numerical

sort district_name serial pernum
browse district_name serial pernum

bysort district_name_shpfile serial pernum : gen cycle = _n

**Keep string digits to two spaces
tostring cycle, replace
	foreach num of numlist 1/9 {
		replace cycle = "0`num'" if cycle == "`num'"
}

**Gen HHIDs with cycles (23 times)
tostring serial, gen(serial_str)
gen serial_cycle = serial_str + cycle
*sort district_name_shpfile serial serial_cycle pernum
*browse district_name_shpfile serial serial_cycle pernum


bysort district_name : gen order_seat = _n
*bysort serial_cycle : egen order_seat_hh = min(order_seat)

sort district_name_shpfile serial serial_cycle pernum 
browse district_name_shpfile serial serial_cycle pernum 


save "D:\work\Zimbabwe\Final Data\Data\5_percent_sample\abm_individual_new_091720_pre_stu_exp.dta" , replace

**********************************************************************************
* 2. Expanding and Allocating Seeds for Primary Students
**********************************************************************************
clear
set seed 1010101

use "D:\work\Zimbabwe\Final Data\Data\5_percent_sample\abm_individual_new_091720_p_stu.dta" , clear

expand 23

format PID_str %20.0g // already numerical

sort district_name serial pernum
browse district_name serial pernum

bysort district_name_shpfile serial pernum : gen cycle = _n

tostring cycle, replace
	foreach num of numlist 1/9 {
		replace cycle = "0`num'" if cycle == "`num'"
}

**Gen HHIDs with cycles (23 times)
tostring serial, gen(serial_str)
gen serial_cycle = serial_str + cycle
*sort district_name_shpfile serial serial_cycle pernum
*browse district_name_shpfile serial serial_cycle pernum


bysort district_name : gen order_seat = _n
*bysort serial_cycle : egen order_seat_hh = min(order_seat)
sort district_name_shpfile serial serial_cycle pernum order_seat*
browse district_name_shpfile serial serial_cycle pernum order_seat*


save "D:\work\Zimbabwe\Final Data\Data\5_percent_sample\abm_individual_new_091720_p_stu_exp.dta" , replace

**********************************************************************************
* 3. Expanding and Allocating Seeds for Upper Secondary Students
**********************************************************************************
clear
set seed 1010101

use "D:\work\Zimbabwe\Final Data\Data\5_percent_sample\abm_individual_new_091720_upper_s_stu.dta"  , clear

expand 23

format PID_str %20.0g // already numerical

sort district_name serial pernum
browse district_name serial pernum

bysort district_name_shpfile serial pernum : gen cycle = _n

tostring cycle, replace
	foreach num of numlist 1/9 {
		replace cycle = "0`num'" if cycle == "`num'"
}

**Gen HHIDs with cycles (23 times)
tostring serial, gen(serial_str)
gen serial_cycle = serial_str + cycle
*sort district_name_shpfile serial serial_cycle pernum
*browse district_name_shpfile serial serial_cycle pernum


bysort district_name : gen order_seat = _n
*bysort serial_cycle : egen order_seat_hh = min(order_seat)

sort district_name_shpfile serial serial_cycle pernum 
browse district_name_shpfile serial serial_cycle pernum 


save "D:\work\Zimbabwe\Final Data\Data\5_percent_sample\abm_individual_new_091720_upper_s_stu_exp.dta" , replace

**********************************************************************************
* 4. Expanding and Allocating Seeds for Lower Secondary Students
**********************************************************************************
clear
set seed 1010101

use "D:\work\Zimbabwe\Final Data\Data\5_percent_sample\abm_individual_new_091720_lower_s_stu.dta"  , clear

expand 23

format PID_str %20.0g // already numerical

sort district_name serial pernum
browse district_name serial pernum

bysort district_name_shpfile serial pernum : gen cycle = _n

tostring cycle, replace
	foreach num of numlist 1/9 {
		replace cycle = "0`num'" if cycle == "`num'"
}

**Gen HHIDs with cycles (23 times)
tostring serial, gen(serial_str)
gen serial_cycle = serial_str + cycle
*sort district_name_shpfile serial serial_cycle pernum
*browse district_name_shpfile serial serial_cycle pernum


bysort district_name : gen order_seat = _n
*bysort serial_cycle : egen order_seat_hh = min(order_seat)

sort district_name_shpfile serial serial_cycle pernum 
browse district_name_shpfile serial serial_cycle pernum 


save "D:\work\Zimbabwe\Final Data\Data\5_percent_sample\abm_individual_new_091720_lower_s_stu_exp.dta" , replace


**********************************************************************************
* 5. Expanding and Allocating Seeds for Primary Teachers
**********************************************************************************
clear
set seed 99999 // played around with the seed randomization to address schools unassigned to teachers

use "D:\work\Zimbabwe\Final Data\Data\5_percent_sample\abm_individual_new_091720_p_teach.dta" , clear

drop if serial == 3546000 & pernum == 4

expand 23
/*
expand =  4 if district_name =="Lupane"
expand =  4 if district_name =="Nkayi"
expand =  4 if district_name =="Bubi"
expand =  3 if district_name =="Mangwe (South)"
expand =  3 if district_name =="Mwenezi"
expand =  3 if district_name =="Rushinga"
expand =  3 if district_name =="Kariba"
expand =  3 if district_name =="Binga"
expand =  2 if district_name =="Tsholotsho"
expand =  2 if district_name =="UMP"
expand =  2 if district_name =="Insiza"
expand =  2 if district_name =="Gokwe North"
expand =  2 if district_name =="Centenary"
expand =  2 if district_name =="Umzingwane"
expand =  2 if district_name =="Wedza"
expand =  2 if district_name =="Shurugwi"
expand =  2 if district_name =="Mount Darwin"
expand =  2 if district_name =="Hurungwe"
expand =  2 if district_name =="Guruve"
expand =  2 if district_name =="Gokwe South"
expand =  2 if district_name =="Mudzi"
expand =  2 if district_name =="Mutoko"
expand =  2 if district_name =="Shamva"
expand =  2 if district_name =="Seke"
expand =  3 if district_name =="Zvimba"
expand =  2 if district_name =="Chipinge"
expand =  2 if district_name =="Mazowe"
expand =  2 if district_name =="Murehwa"
*/


format PID_str %20.0g // already numerical

sort district_name serial pernum
browse district_name serial pernum

bysort district_name_shpfile serial pernum : gen cycle = _n

tostring cycle, replace
	foreach num of numlist 1/9 {
		replace cycle = "0`num'" if cycle == "`num'"
}

**Gen HHIDs with cycles (23 times)
tostring serial, gen(serial_str)
gen serial_cycle = serial_str + cycle
*sort district_name_shpfile serial serial_cycle pernum
*browse district_name_shpfile serial serial_cycle pernum


bysort district_name : gen order_seat = _n
*bysort serial_cycle : egen order_seat_hh = min(order_seat)

sort district_name_shpfile serial serial_cycle pernum 
browse district_name_shpfile serial serial_cycle pernum 


save "D:\work\Zimbabwe\Final Data\Data\5_percent_sample\abm_individual_new_091720_p_teach_exp.dta",replace


**********************************************************************************
* 6. Expanding and Allocating Seeds for Secondary Teachers
**********************************************************************************
clear
set seed 1010101

use "D:\work\Zimbabwe\Final Data\Data\5_percent_sample\abm_individual_new_091720_s_teach.dta" , clear

drop if serial == 113393000 & pernum == 4

expand = 23 
/*
expand =  9 if district_name =="Tsholotsho"
expand =  8 if district_name =="UMP"
expand =  5 if district_name =="Lupane"
expand =  4 if district_name =="Centenary"
expand =  3 if district_name =="Binga"
expand =  3 if district_name =="Nkayi"
expand =  2 if district_name =="Rushinga"
expand =  2 if district_name =="Mangwe (South)"
expand =  2 if district_name =="Bubi"
expand =  2 if district_name =="Mwenezi"
expand =  2 if district_name =="Hurungwe"
expand =  2 if district_name =="Mount Darwin"
expand =  2 if district_name =="Guruve"
expand =  2 if district_name =="Mutoko"
expand =  2 if district_name =="Umguza"
expand =  2 if district_name =="Matobo"
expand =  2 if district_name =="Zvimba"
expand =  2 if district_name =="Mudzi"
expand =  2 if district_name =="Shamva"
expand =  2 if district_name =="Gokwe South"
expand =  3 if district_name =="Buhera"
expand =  4 if district_name =="Mazowe"
*/

format PID_str %20.0g // already numerical

sort district_name serial pernum
browse district_name serial pernum

bysort district_name_shpfile serial pernum : gen cycle = _n

tostring cycle, replace
	foreach num of numlist 1/9 {
		replace cycle = "0`num'" if cycle == "`num'"
}

**Gen HHIDs with cycles (23 times)
tostring serial, gen(serial_str)
gen serial_cycle = serial_str + cycle
*sort district_name_shpfile serial serial_cycle pernum
*browse district_name_shpfile serial serial_cycle pernum


bysort district_name : gen order_seat = _n
*bysort serial_cycle : egen order_seat_hh = min(order_seat)

sort district_name_shpfile serial serial_cycle pernum 
browse district_name_shpfile serial serial_cycle pernum 


save "D:\work\Zimbabwe\Final Data\Data\5_percent_sample\abm_individual_new_091720_s_teach_exp.dta",replace
