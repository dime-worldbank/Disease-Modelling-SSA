**********************************************************************************
* Project: Zimbabwe Covid-19 Agent Based Modelling
* Module:  Appending Final Datasets
* Written by: Billy Hoo
* Last edited: 09/20/2020
**********************************************************************************



**********************************************************************************
* 1. Append Teachers & Students Dataset
**********************************************************************************

use "D:\work\Zimbabwe\Final Data\Data\5_percent_sample\abm_individual_new_091720__pre_merged_complete.dta" , clear
append using "D:\work\Zimbabwe\Final Data\Data\5_percent_sample\abm_individual_new_091720__primary_merged_complete.dta"
append using "D:\work\Zimbabwe\Final Data\Data\5_percent_sample\abm_individual_new_091720__secondary_lower_merged_complete.dta"
append using "D:\work\Zimbabwe\Final Data\Data\5_percent_sample\abm_individual_new_091720__secondary_upper_merged_complete.dta" 
append using "D:\work\Zimbabwe\Final Data\Data\5_percent_sample\abm_individual_new_091720__p_t_complete.dta" 
append using "D:\work\Zimbabwe\Final Data\Data\5_percent_sample\abm_individual_new_091720__s_t_complete.dta" 

drop  Name-UpperSecondary order_seat r_order_seed_pre r_lower r_order_seed_upper r_upper order_seat3 r_order_seed_p_t latitude longitude Type MaleTeachers FemaleTeachers TotalTeachers r_p_t r_order_seed_s_t r_s_t

gen t_count = 1 if inlist(teachers,232,233,234,235)
bysort Schoolnumber: egen Teacher_count = sum(t_count)

gen s_count = 1 if inlist(current_students,1,2,3)
bysort Schoolnumber: egen Student_count = sum(s_count)

gen pre_count = 1 if inlist(current_students,1)

gen prim_count = 1 if inlist(current_students,2)

gen second_count = 1 if inlist(current_students,3)

	tostring pernum, gen(pernum_str)
	foreach num of numlist 1/9 {
	replace pernum_str = "0`num'" if pernum_str == "`num'"
}
	gen serial_cycle_pernum = serial_cycle + pernum_str
	
**Schools that were still not assigned a teacher , randomly selected teachers who were already assigned within the district
replace Schoolnumber = 1960 if serial_cycle_pernum == "176210001501"
replace Schoolnumber = 1960 if serial_cycle_pernum == "175750001301"
replace Schoolnumber = 1961 if serial_cycle_pernum == "182310001601"
replace Schoolnumber = 3226 if serial_cycle_pernum == "291540001101"
replace Schoolnumber = 3269 if serial_cycle_pernum == "266520001701"
replace Schoolnumber = 4537 if serial_cycle_pernum == "372900000605"
replace Schoolnumber = 4537 if serial_cycle_pernum == "368480001301"
replace Schoolnumber = 6618 if serial_cycle_pernum == "441680000702"
replace Schoolnumber = 6618 if serial_cycle_pernum == "444130001702"
replace Schoolnumber = 7194 if serial_cycle_pernum == "534280000707"
replace Schoolnumber = 12986 if serial_cycle_pernum == "858900001302"
replace Schoolnumber = 12986 if serial_cycle_pernum == "859630000402"
replace Schoolnumber = 13019 if serial_cycle_pernum == "858900001401"
replace Schoolnumber = 13019 if serial_cycle_pernum == "803570001801"
replace Schoolnumber = 14597 if serial_cycle_pernum == "919960002101"
replace Schoolnumber = 16963 if serial_cycle_pernum == "1078400000402"
replace Schoolnumber = 16963 if serial_cycle_pernum == "1120790001402"
replace Schoolnumber = 16963 if serial_cycle_pernum == "1121360002301"
replace Schoolnumber = 22009 if serial_cycle_pernum == "279070000301"

		
**
set seed 9999
gen primary_random = floor((100-1+1)*runiform() + 1) if inlist(teachers,233)

gen teacher_grade = 110 if inrange(primary_random,1,15) & inlist(teachers,233)
replace teacher_grade = 210 if inrange(primary_random,16,28) & inlist(teachers,233)
replace teacher_grade = 220 if inrange(primary_random,29,41) & inlist(teachers,233)
replace teacher_grade = 230 if inrange(primary_random,42,54) & inlist(teachers,233)
replace teacher_grade = 240 if inrange(primary_random,55,67) & inlist(teachers,233)
replace teacher_grade = 250 if inrange(primary_random,68,78) & inlist(teachers,233)
replace teacher_grade = 260 if inrange(primary_random,79,90) & inlist(teachers,233)
replace teacher_grade = 270 if inrange(primary_random,91,100) & inlist(teachers,233)


gen secondary_random = floor((100-1+1)*runiform() + 1) if inlist(teachers,232,234,235)

replace teacher_grade = 310 if inrange(secondary_random,1,22) & inlist(teachers,232,234,235)
replace teacher_grade = 320 if inrange(secondary_random,23,45) & inlist(teachers,232,234,235)
replace teacher_grade = 330 if inrange(secondary_random,46,68) & inlist(teachers,232,234,235)
replace teacher_grade = 340 if inrange(secondary_random,69,91) & inlist(teachers,232,234,235)
replace teacher_grade = 350 if inrange(secondary_random,92,96) & inlist(teachers,232,234,235)
replace teacher_grade = 360 if inrange(secondary_random,97,100) & inlist(teachers,232,234,235)


/*
preserve
collapse (sum) s_count t_count, by(Schoolnumber)
export excel using "D:\work\Zimbabwe\Final Data\School Information\simulated_school_population_nosiblings_092220.xls", firstrow(variables) replace
restore

preserve
collapse (sum) s_count t_count pre_count prim_count second_count, by(district_name)
export excel using "D:\work\Zimbabwe\Final Data\School Information\simulated_district_population_nosiblings_092220.xls", firstrow(variables) replace
restore
*/
drop  t_count Teacher_count s_count Student_count serial_cycle_pernum


save "D:\work\Zimbabwe\Final Data\Data\5_percent_sample\abm_individual_new_091720__all_merged_complete.dta" , replace


**********************************************************************************
* 2. Reappend to original and expanded dataset
**********************************************************************************

**Original Dataset 
use "D:\work\Zimbabwe\Final Data\Data\5_percent_sample\abm_individual_new_withgeo_091720.dta", clear

drop if inlist(teachers,232,233,234,235)
drop if inlist(current_students,1,2)
drop if inlist(current_students,3) & inlist(current_students_grade_st,350,360) 
drop if inlist(current_students,3) & inlist(current_students_grade_st,310,320,330,340)

**Trim variables
keep serial	pernum PID_str	persons	strata serial geolev1 geolev2 geo1_zw geo1_zw2012 geo2_zw geo2_zw2012 dhs_ipumsi_zw ///														
gq	zw2012a_dwtype	age	sex	urban	economic_status	new_district_id	district_id	famunit	famsize	nfams race citizen	empstatd empstat zw2012a_hhtype	hhtype	zw2012a_edlevel	current_students current_students_grade_st teacher ///
marst disabled	disblnd	disdeaf	dismntl ///														
district_name_shpfile  district_id new_district_id ///
mining_workers manufacturing_workers mining_district

label variable mining_workers "Indicator for mining workers"
label variable mining_districts "Indicator for mining districts"
label variable manufacturing_workers "Indicator for manufacturing workers"
label variable PID_str "Unexpanded Unique IDs"
label variable teacher "Indicator for teachers"
label variable district_id "District IDs 2012, 88 districts"
label variable new_district_id "New District IDs 2015, 60 districts"

**For Michael to use
save "D:\work\Zimbabwe\Final Data\Data\5_percent_sample\abm_individual_new_091720_non_teachers_students.dta" , replace

**Expanding for randomization
expand 23

format PID_str %20.0g // already numerical

sort district_name serial pernum

bysort district_name_shpfile serial pernum : gen cycle = _n

tostring cycle, replace
	foreach num of numlist 1/9 {
		replace cycle = "0`num'" if cycle == "`num'"
}

**Gen HHIDs with cycles
tostring serial, gen(serial_str)
gen serial_cycle = serial_str + cycle
sort district_name_shpfile serial serial_cycle pernum

append using "D:\work\Zimbabwe\Final Data\Data\5_percent_sample\abm_individual_new_091720__all_merged_complete.dta" 

sort district_name_shpfile serial serial_cycle pernum

save "D:\work\Zimbabwe\Final Data\Data\5_percent_sample\abm_individual_new_091720_final_merged_complete.dta" , replace

**********************************************************************************
* 3. Reappend to original and expanded dataset (part two)
**********************************************************************************

use "D:\work\Zimbabwe\Final Data\Data\5_percent_sample\abm_individual_new_091720_final_merged_complete.dta" , clear

drop pernum_str

	**UNIQUE Individual IDs
		tostring pernum, gen(pernum_str)
		foreach num of numlist 1/9 {
		replace pernum_str = "0`num'" if pernum_str == "`num'"
}
	gen serial_cycle_pernum = serial_cycle + pernum_str // indicator for individual IDs
	
	**Label Remaining Variables
	label variable cycle "Number of Duplicates for PID"
	label variable serial_cycle "Number of Duplicates for HH"
	label variable serial_cycle_pernum "Unique Identifier for each Individual"

keep serial	pernum cycle PID_str serial_cycle serial_cycle_pernum persons strata ///														
gq	zw2012a_dwtype	age	sex	urban	economic_status	new_district_id	district_id	famunit	famsize	nfams race citizen	empstatd empstat zw2012a_hhtype	hhtype	zw2012a_edlevel	current_students current_students_grade_st teacher* ///
marst disabled	disblnd	disdeaf	dismntl ///														
district_name_shpfile  district_id new_district_id ///
mining_workers manufacturing_workers mining_district Schoolnumber

	**Cleaning Dataset

		*Current Students Indicators
		replace current_students = 0 if current_students ==. // teachers who are not students
		
		*Current Students Grades Indicators
		replace current_students_grade = 0 if current_students_grade ==. // unknown grades
		replace current_students_grade_st = 99 if inlist(current_students,1,2,3) & current_students_grade == 0 // unknown grades 

		label define current_students_grade 99"Unknown and Not Assigned to Schools" , modify

		replace current_students = 0 if current_students_grade_st == 99 
		*Current Teachers
		replace teachers = 0 if teachers == . // non-teachers 

		*School Goers
		gen school_goers = 1 if inlist(teachers,232,233,234,235) // teachers
		replace school_goers = 1 if inrange(current_students_grade_st,110,360) // school_goers
		
		label variable school_goers "Indicator for School Goers"
		
		*Economic Status
		replace economic_status = 0 if current_students_grade == 99 // assigned to unknown economic status

		*School Details 
		replace Schoolnumber =. if current_students_grade == 99 // unknown grades will be unassigned for simplicity 		

		**Check all important variables
		unique PID 
		unique district_name 
		unique Schoolnumber 
		unique Schoolnumber if inlist(teachers,232,233,234,235) 
		unique Schoolnumber if inlist(current_students,1,2,3)
		unique Schoolnumber if inlist(teachers,232,234,235) 
		unique Schoolnumber if inlist(teachers,233) 
		unique Schoolnumber if inlist(current_students,3) 
		unique Schoolnumber if inlist(current_students,1,2) 

		foreach var of varlist mining_workers manufacturing_workers mining_districts {
		replace `var' = 0 if `var' == .
		}

save "D:\work\Zimbabwe\Final Data\Data\5_percent_sample\abm_individual_new_091720_final_merged_complete_int.dta" , replace


**********************************************************************************
* 4. District level averages for mixing 
**********************************************************************************
use "D:\work\Zimbabwe\Final Data\Data\5_percent_sample\abm_individual_new_091720.dta" , clear // had to use 5% data

*Current Students Indicators // Clean the student indicators as shown above to be consistent
		replace current_students = 0 if current_students ==. // teachers who are not students
		
		*Current Students Grades Indicators
		replace current_students_grade = 0 if current_students_grade ==. // unknown grades
		replace current_students_grade_st = 99 if inlist(current_students,1,2,3) & current_students_grade == 0 // unknown grades 

		label define current_students_grade 99"Unknown and Not Assigned to Schools" , modify

		replace current_students = 0 if current_students_grade_st == 99 
		*Current Teachers
		replace teachers = 0 if teachers == . // non-teachers 

		*School Goers
		gen school_goers = 1 if inlist(teachers,232,233,234,235) // teachers
		replace school_goers = 1 if inrange(current_students_grade_st,110,360) // school_goers
		
		label variable school_goers "Indicator for School Goers"
		
		*Economic Status
		replace economic_status = 0 if current_students_grade == 99 // assigned to unknown economic status

**Economic Status By Gender
gen economic_status_m = economic_status if sex == 1
gen economic_status_f = economic_status if sex == 2

label values economic_status_m  economic_status
label values economic_status_f economic_status 

tab sex, gen(sex_)
tab economic_status, gen(econ_stat_)
tab economic_status_m, gen(m_econ_stat_)
tab economic_status_f, gen(f_econ_stat_)
tab urban, gen(urban_)
tab disabled, gen(dis_)

foreach var of varlist sex_* econ_stat_* urban_* dis_* {
gen mean_`var' = `var'
}

gen total = 1 

**Collapse data at district level for distribution // In the end, only the economic status variables were used 
collapse  (mean) mean*  , by(new_district_id)

save "D:\work\Zimbabwe\Final Data\Data\5_percent_sample\abm_individual_new_091720_district_averages.dta" , replace


**********************************************************************************
* 4.1 Original District & Province variables
**********************************************************************************

use "D:\work\Zimbabwe\Final Data\Data\5_percent_sample\abm_individual_new_withgeo_091720.dta" , clear


keep serial geolev1 geolev2 geo1_zw geo1_zw2012 geo2_zw geo2_zw2012 dhs_ipumsi_zw

save "D:\work\Zimbabwe\Final Data\Data\5_percent_sample\abm_geo_info_hh_091720.dta",replace 



stop
**********************************************************************************
* 5. Final MERGES
**********************************************************************************

use "D:\work\Zimbabwe\Final Data\Data\5_percent_sample\abm_individual_new_091720_final_merged_complete_int.dta" , clear

**Merging in District level averages
merge m:1 new_district_id using "D:\work\Zimbabwe\Final Data\Data\5_percent_sample\abm_individual_new_091720_district_averages.dta" , gen(_merge)
drop _merge

drop mean_sex_1 mean_sex_2
drop mean_urban_1 mean_urban_2
drop mean_dis_*

**Merge School Boarding and Hotseating Indicators
merge m:1 Schoolnumber using "D:\work\Zimbabwe\Final Data\Data\School Information_092020_all.dta", gen(_merge)
drop if _merge == 2
drop _merge 
drop Number 

**Random Assignment for Hotseating Session
set seed 1001010
gen hotseating_session = floor((2-1+1)*runiform() + 1) if Hotseating == 1

label define hotseating_session 1"Morning" 2"Afternoon", replace
label values hotseating_session hotseating_session

**Phases
gen phase = 1 if inlist(current_students_grade,270,340,360)
replace phase = 1 if inlist(teacher_grade,270,340,360)

replace phase = 2 if inlist(current_students_grade,260,330,350)
replace phase = 2 if inlist(teacher_grade,260,330,350)

replace phase = 3 if inlist(current_students_grade,230,240,250,310,320)
replace phase = 3 if inlist(teacher_grade,230,240,250,310,320)

replace phase = 4 if inlist(current_students_grade,210,220)
replace phase = 4 if inlist(teacher_grade,210,220)

replace phase = 5 if inlist(current_students_grade,110,120,130)
replace phase = 5 if inlist(teacher_grade,110)


**Final cleaning 
replace school_goers = 0 if school_goers ==.
replace school_goers = 0 if current_students_grade == 99
replace current_students_grade = 0 if current_students_grade == 99
replace phase = 0 if phase ==. 

replace teachers = 0 if teachers == .

replace Hotseating = 0 if hotseating == . & Schoolnumber !=.
replace hotseating_session = 0 if hotseating == . & Schoolnumber !=.

drop TotalTeachers_Sc_data-Total_Secondary


generate boardingschool = 1 if Boarding_school =="Day and Boarding"
replace boardingschool = 2 if Boarding_school =="Boarding School"
replace boardingschool = 0 if boardingschool ==. & Schoolnumber !=.

drop Boarding_school

gen school_goers_detailed = 0
replace school_goers_detailed = 1 if inlist(teachers,232,233,234,235)
replace school_goers_detailed = 2 if inlist(current_students,1,2,3)
label define school_goers 1"School Goers", modify
label values school_goers school_goers
label define school_goers_detailed 1"Teachers" 2"Students" , replace
label values school_goers_detailed school_goers_detailed

label variable school_goers_detailed "School Goers with Teacher and Students Dummies "

label define boardingschool 1"Day and Boarding" 2"Boarding only" 0"Not Boarding School"
label values boardingschool boardingschool

label define hotseating_session 1"Morning" 2"Evening",modify
label values hotseating_session hotseating_session

label variable phase "Opening Phases"
label variable boardingschool "Boarding School Indicators"
label variable teacher_grade "Teacher Teaching Grade Assignment"

label define teacher_grade 110"ECD Teachers", modify
label define teacher_grade 210"GRADE 1 Teachers", modify
label define teacher_grade 220"GRADE 2 Teachers", modify
label define teacher_grade 230"GRADE 3 Teachers", modify
label define teacher_grade 240"GRADE 4 Teachers", modify
label define teacher_grade 250"GRADE 5 Teachers", modify
label define teacher_grade 260"GRADE 6 Teachers", modify
label define teacher_grade 270"GRADE 7 Teachers", modify
label define teacher_grade 310"FORM 1 Teachers", modify
label define teacher_grade 320"FORM 2 Teachers", modify
label define teacher_grade 330"FORM 3 Teachers", modify
label define teacher_grade 340"FORM 4 Teachers", modify
label define teacher_grade 350"Lower 6 Teachers", modify
label define teacher_grade 360"Upper 6 Teachers", modify


label values  teacher_grade teacher_grade
label variable teacher_grade "Current Student's Grade Level" 

label variable hotseating_session "Hot Seating Session Indicators"

**DONE 
save "D:\work\Zimbabwe\Final Data\Data\5_percent_sample\abm_individual_new_092320_final_merged_complete_FINAL.dta" , replace




