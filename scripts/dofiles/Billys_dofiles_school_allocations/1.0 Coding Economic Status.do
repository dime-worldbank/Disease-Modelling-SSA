**********************************************************************************
* Project: Zimbabwe Covid-19 Agent Based Modelling
* Module:  Preparing Individual Economic Status 
* Written by: Billy Hoo
* Last edited: 09/221/2020
**********************************************************************************

**********************************************************************************
* 1. PREPARING INDIVIDUAL DATASET 
**********************************************************************************
** Import Dataset

/*

OLD CODING
**Economic Status
gen economic_status = 0 
replace economic_status = 1 if empstatd == 330 // students
replace economic_status = 2 if empstatd == 310 // homemakers
replace economic_status = 3 if inlist(occisco,1,2,3) // office workers
replace economic_status = 4 if inlist(occisco,4,5,7,9,11,98) // service workers
replace economic_status = 5 if inlist(occisco,6) // agricultural
replace economic_status = 6 if inlist(occisco,8) // industry
replace economic_status = 7 if inlist(occisco,10) // army 
replace economic_status = 8 if disabled == 1 & empstatd == 320 // disabled and unable to work
 


------------------------------------------------------------------------------
empstatd                activity status (employment status) [detailed version]
------------------------------------------------------------------------------

                  type:  numeric (int)
                 label:  EMPSTATD

                 range:  [0,999]                      units:  1
         unique values:  8                        missing .:  0/654,688

            tabulation:  Freq.   Numeric  Label
                       184,405         0  niu (not in universe)
                       230,799       100  employed, not specified
                        28,937       200  unemployed, not specified
                        57,546       310  housework
                        16,990       320  unable to work, disabled or
                                          health reasons
                       128,628       330  in school
                         4,214       390  inactive, other reasons
                         3,169       999  unknown/missing




. tab empstatd if economic_status == 0

    activity status (employment status) |
                     [detailed version] |      Freq.     Percent        Cum.
----------------------------------------+-----------------------------------
                  niu (not in universe) |    184,405       77.57       77.57
              unemployed, not specified |     28,937       12.17       89.75
unable to work, disabled or health reas |     16,990        7.15       96.89
                inactive, other reasons |      4,214        1.77       98.67
                        unknown/missing |      3,169        1.33      100.00
----------------------------------------+-----------------------------------
                                  Total |    237,715      100.00

. tab  occisco if economic_status == 0

               occupation, isco general |      Freq.     Percent        Cum.
----------------------------------------+-----------------------------------
                  niu (not in universe) |    237,715      100.00      100.00
----------------------------------------+-----------------------------------
                                  Total |    237,715      100.00
								  
								  
----------------------------------------------------------------------------------------------------
occisco                                                                     occupation, isco general
----------------------------------------------------------------------------------------------------

                  type:  numeric (byte)
                 label:  OCCISCO

                 range:  [1,99]                       units:  1
         unique values:  13                       missing .:  0/654,688

            tabulation:  Freq.   Numeric  Label
                         2,485         1  legislators, senior officials
                                          and managers
                        12,767         2  professionals
                         5,872         3  technicians and associate
                                          professionals
                         3,906         4  clerks
                        23,851         5  service workers and shop and
                                          market sales
                       104,948         6  skilled agricultural and fishery
                                          workers
                        21,186         7  crafts and related trades
                                          workers
                         5,509         8  plant and machine operators and
                                          assemblers
                        36,262         9  elementary occupations
                         1,887        10  armed forces
                           522        11  other occupations, unspecified
                                          or n.e.c
                        11,604        98  unknown
                       423,889        99  niu (not in universe)

*/

**********************************************************************************
* 1. Student Indicators
**********************************************************************************
**Current Students Indicators
gen current_students = 0
replace current_students = 1 if zw2012a_school == 1 & zw2012a_schlevel == 0 // Primary
replace current_students = 2 if zw2012a_school == 1 & zw2012a_schlevel == 1 // Primary
replace current_students = 3 if zw2012a_school == 1 & zw2012a_schlevel == 2 // Primary


label define current_students 0"Not Current Students" 1"Current Pre-school Students" 2"Current Primary Students" 3"Current Secondary Students"
label values  current_students current_students
label variable current_students "Current Students Preschool, Primary & secondary"


**Current Students Grade Indicators
gen current_students_grade = 0
replace current_students_grade = 110 if zw2012a_schgrade == 1 & zw2012a_schlevel == 0 // ECD
replace current_students_grade = 120 if zw2012a_schgrade == 2 & zw2012a_schlevel == 0 // ECD
replace current_students_grade = 130 if zw2012a_schgrade == 3 & zw2012a_schlevel == 0 // ECD
replace current_students_grade = 210 if zw2012a_schgrade == 1 & zw2012a_schlevel == 1 // Primary
replace current_students_grade = 220 if zw2012a_schgrade == 2 & zw2012a_schlevel == 1 // Primary
replace current_students_grade = 230 if zw2012a_schgrade == 3 & zw2012a_schlevel == 1 // Primary
replace current_students_grade = 240 if zw2012a_schgrade == 4 & zw2012a_schlevel == 1 // Primary
replace current_students_grade = 250 if zw2012a_schgrade == 5 & zw2012a_schlevel == 1 // Primary
replace current_students_grade = 260 if zw2012a_schgrade == 6 & zw2012a_schlevel == 1 // Primary
replace current_students_grade = 270 if zw2012a_schgrade == 7 & zw2012a_schlevel == 1 // Primary
replace current_students_grade = 310 if zw2012a_schgrade == 1 & zw2012a_schlevel == 2 // Secondary
replace current_students_grade = 320 if zw2012a_schgrade == 2 & zw2012a_schlevel == 2 // Secondary
replace current_students_grade = 330 if zw2012a_schgrade == 3 & zw2012a_schlevel == 2 // Secondary
replace current_students_grade = 340 if zw2012a_schgrade == 4 & zw2012a_schlevel == 2 // Secondary
replace current_students_grade = 350 if zw2012a_schgrade == 5 & zw2012a_schlevel == 2 // Secondary
replace current_students_grade = 360 if zw2012a_schgrade == 6 & zw2012a_schlevel == 2 // Secondary


label define current_students_grade 110"ECDA", modify
label define current_students_grade 120"ECD2", modify
label define current_students_grade 130"ECDB", modify
label define current_students_grade 210"GRADE 1", modify
label define current_students_grade 220"GRADE 2", modify
label define current_students_grade 230"GRADE 3", modify
label define current_students_grade 240"GRADE 4", modify
label define current_students_grade 250"GRADE 5", modify
label define current_students_grade 260"GRADE 6", modify
label define current_students_grade 270"GRADE 7", modify
label define current_students_grade 310"FORM 1", modify
label define current_students_grade 320"FORM 2", modify
label define current_students_grade 330"FORM 3", modify
label define current_students_grade 340"FORM 4", modify
label define current_students_grade 350"Lower 6", modify
label define current_students_grade 360"Upper 6", modify


label values  current_students_grade current_students_grade
label variable current_students_grade "Current Student's Grade Level"


**Generate Grade Stricter Version , Restricting to current students 
gen current_students_grade_st = current_students_grade if inlist(current_students,1,2,3)
replace current_students_grade_st = 0 if current_students_grade_st == . 

label values  current_students_grade_st current_students_grade
label variable current_students_grade_st "Current Student's Grade Level (STRICT) "

**Economic Status
gen economic_status = 0 
replace economic_status = 2 if empstatd == 310 // homemakers
replace economic_status = 3 if inlist(occisco,1,2,3) // office workers
replace economic_status = 4 if inlist(occisco,4,5,7,9,11,98) // service workers
replace economic_status = 5 if inlist(occisco,6) // agricultural
replace economic_status = 6 if inlist(occisco,8) // industry
replace economic_status = 7 if inlist(occisco,10) // army 
replace economic_status = 8 if disabled == 1 & empstatd == 320 // disabled and unable to work
replace economic_status = 1 if inlist(current_students,1,2,3) // students
 

label define economic_status 0"Not working, inactive, not in universe" ///
1"Current Students" 2"Homemakers/Housework" 3"Office workers" 4"Service Workers" 5"Agriculture Workers" ///
6"Indusrtry Workers" 7"In the army" 8"Disabled and not working"

label values economic_status economic_status
label variable economic_status "Economic Status"

**Teachers
gen teachers = 0
replace teachers = isco88a if inlist(isco88a,231,232,233,234,235)
label values teachers ISCO88A
tab teachers

gen random_teacher = floor((3-1+1)*runiform() + 1) if teachers == 231

*recode teachers 231 = 232 if random_teacher == 3
*recode teachers 231 = 233 if inlist(random_teacher,1,2)

*drop random_teacher

**********************************************************************************
* 2. Manufacturing and Mining Workers
**********************************************************************************
**Manufacturing Workers
gen manufacturing_workers = 1 if inrange(isco88a,721,744)
replace manufacturing_workers = 1 if inlist(isco88a,828,829,833,932)

**Mining Workers
gen mining_workers = 1 if inlist(isco88a,711,811,931)

**From Website List + 3% Cutoff; Districts with Urban and Rural Designation are both included 
gen mining_districts = 1 if inlist(geo2_zw2012,7005,4003,6005,2007,9021,4007,6004,7006,2004,3005)
replace mining_districts = 1 if inlist(geo2_zw2012,1005,9022,6021,7004,4021,9001,1021,6001)



**********************************************************************************
* 3. Generate Unique IDS
**********************************************************************************

gen serial_copy = serial 
gen pernum_copy = pernum 
tostring serial_copy,gen(serial_str) 
tostring pernum_copy,gen(pernum_str)

	foreach num of numlist 1/9 {
		replace pernum_str = "0`num'" if pernum == 	`num'
}
gen PID_str = serial_str + pernum_str

destring PID, replace
stop
**********************************************************************************
* 4. Preparing Individual Level Datasets
**********************************************************************************

**Produce 5% Intermediate Data
keep geolev1 geolev2 geo1_zw geo1_zw2012 geo2_zw geo2_zw2012 dhs_ipumsi_zw serial persons nfams famunit famsize hhwt subsamp strata urban gq pernum age age2 sex marst citizen race ///
edattain edattaind yrschool empstat empstatd educzw economic_status teachers labforce occ occis isco88a disabled disblnd disdeaf dismntl perwt ///
zw2012a_dwnum zw2012a_pernum zw2012a_areatype zw2012a_hhtype zw2012a_agric zw2012a_ownershp zw2012a_dwtype zw2012a_schever zw2012a_edlevel ///
zw2012a_edattain zw2012a_school zw2012a_schyr zw2012a_schlevel zw2012a_schgrade zw2012a_activity zw2012a_occ zw2012a_field ///
district_id-district_name_shpfile current_students current_students_grade_st PID serial_copy pernum_copy hhtype ///
mining_districts mining_workers manufacturing_workers 

save "D:\work\Zimbabwe\Final Data\Data\5_percent_sample\abm_individual_new_withgeo_091720.dta" , replace


**Produce 5% Intermediate Individual Level data Data
keep serial persons nfams famunit famsize hhwt subsamp strata urban gq pernum age age2 sex marst citizen race ///
edattain edattaind yrschool empstat empstatd educzw economic_status teachers labforce occ occis isco88a disabled disblnd disdeaf dismntl perwt ///
zw2012a_dwnum zw2012a_pernum zw2012a_areatype zw2012a_hhtype zw2012a_agric zw2012a_ownershp zw2012a_dwtype zw2012a_schever zw2012a_edlevel ///
zw2012a_edattain zw2012a_school zw2012a_schyr zw2012a_schlevel zw2012a_schgrade zw2012a_activity zw2012a_occ zw2012a_field /// 
new_district_id district_id district_name_shpfile current_students current_students_grade_st PID serial_copy pernum_copy hhtype ///
mining_districts mining_workers manufacturing_workers 

save "D:\work\Zimbabwe\Final Data\Data\5_percent_sample\abm_individual_new_091720.dta" , replace

**Produce 5% Intermediate Individual Level data FOR SIMULATION
**Simulate at the school level population 
**Calibrate population distribution
stop
**********************************************************************************
* 5. Preparing Simulation Datasets (students)
**********************************************************************************

**Pre-School Students 

use "D:\work\Zimbabwe\Final Data\Data\5_percent_sample\abm_individual_new_091720.dta" , replace

keep if inlist(current_students,1) // 22,330 students 187 grades unknown 

keep serial pernum PID persons strata /// input variables
PID gq zw2012a_dwtype age sex urban economic_status new_district_id district_id famunit famsize nfams race citizen  empstatd empstat zw2012a_hhtype hhtype zw2012a_edlevel current_students current_students_grade_st  /// variables to remain in expansion 
marst disabled disblnd disdeaf dismntl /// variables that can be used to calibrate
district_name_shpfile

save "D:\work\Zimbabwe\Final Data\Data\5_percent_sample\abm_individual_new_091720_pre_stu.dta" , replace


**Primary School Students 

use "D:\work\Zimbabwe\Final Data\Data\5_percent_sample\abm_individual_new_091720.dta" , replace

keep if inlist(current_students,2)  // 127,248 students 130 grades unknown

keep serial pernum PID persons strata /// input variables
PID gq zw2012a_dwtype age sex urban economic_status new_district_id district_id famunit famsize nfams race citizen  empstatd empstat zw2012a_hhtype hhtype zw2012a_edlevel current_students current_students_grade_st  /// variables to remain in expansion 
marst disabled disblnd disdeaf dismntl /// variables that can be used to calibrate
district_name_shpfile

save "D:\work\Zimbabwe\Final Data\Data\5_percent_sample\abm_individual_new_091720_p_stu.dta" , replace

**Upper Secondary School Students

use "D:\work\Zimbabwe\Final Data\Data\5_percent_sample\abm_individual_new_091720.dta" , replace

keep if inlist(current_students,3) & inlist(current_students_grade_st,350,360) // 4,488

keep serial pernum PID persons strata /// input variables
PID gq zw2012a_dwtype age sex urban economic_status new_district_id district_id famunit famsize nfams race citizen  empstatd empstat zw2012a_hhtype hhtype zw2012a_edlevel current_students current_students_grade_st /// variables to remain in expansion 
marst disabled disblnd disdeaf dismntl /// variables that can be used to calibrate
district_name_shpfile

save "D:\work\Zimbabwe\Final Data\Data\5_percent_sample\abm_individual_new_091720_upper_s_stu.dta" , replace


**Lower Secondary School Students 

use "D:\work\Zimbabwe\Final Data\Data\5_percent_sample\abm_individual_new_091720.dta" , replace

keep if inlist(current_students,3) & inlist(current_students_grade_st,310,320,330,340) // 46,395 students

keep serial pernum PID persons strata /// input variables
PID gq zw2012a_dwtype age sex urban economic_status new_district_id district_id famunit famsize nfams race citizen  empstatd empstat zw2012a_hhtype hhtype zw2012a_edlevel current_students current_students_grade_st  /// variables to remain in expansion 
marst disabled disblnd disdeaf dismntl /// variables that can be used to calibrate
district_name_shpfile

save "D:\work\Zimbabwe\Final Data\Data\5_percent_sample\abm_individual_new_091720_lower_s_stu.dta" , replace

**********************************************************************************
* 5. Preparing Simulation Datasets (Teachers)
**********************************************************************************
**Pre-schoool & Primary School Teachers

use "D:\work\Zimbabwe\Final Data\Data\5_percent_sample\abm_individual_new_091720.dta" , replace

keep if inlist(teachers, 233) // 3,871 observations

keep serial pernum PID persons strata  /// input variables
PID gq zw2012a_dwtype age sex urban economic_status new_district_id district_id famunit famsize nfams race citizen empstatd empstat zw2012a_hhtype hhtype teachers /// variables to remain in expansion 
marst disabled disblnd disdeaf dismntl zw2012a_edlevel /// variables that can be used to calibrate
district_name_shpfile


save "D:\work\Zimbabwe\Final Data\Data\5_percent_sample\abm_individual_new_091720_p_teach.dta" , replace

**Secondary  School Teachers
use "D:\work\Zimbabwe\Final Data\Data\5_percent_sample\abm_individual_new_091720.dta" , replace

keep if inlist(teachers, 232,234,235) //  1,852+58+72 = 1982 observations

keep serial pernum PID persons strata /// input variables
PID gq zw2012a_dwtype age sex urban economic_status new_district_id district_id famunit famsize nfams race citizen empstatd empstat zw2012a_hhtype hhtype teachers /// variables to remain in expansion 
marst disabled disblnd disdeaf dismntl zw2012a_edlevel /// variables that can be used to calibrate
district_name_shpfile


save "D:\work\Zimbabwe\Final Data\Data\5_percent_sample\abm_individual_new_091720_s_teach.dta" , replace

**export excel using "D:\work\Zimbabwe\Economic Status for Agent Interaction Matric\abm_individual_041420.xlsx", firstrow(variables) replace

