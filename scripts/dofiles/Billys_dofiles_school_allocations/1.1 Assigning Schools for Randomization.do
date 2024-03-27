* *********************************************************************************
* Project: Zimbabwe Covid-19 Agent Based Modelling
* Module:  Expanding IPUMS Data
* Written by: Billy Hoo
* Last edited: 05/27/2020
**********************************************************************************

**********************************************************************************
* 1. Expanding Dataset
**********************************************************************************
clear
use "D:\work\Zimbabwe\Economic Status for Agent Interaction Matric\abm_individual_041420.dta" 

gen district_id = geo2_zw2012

merge m:1 district_id using "D:\work\Zimbabwe\District Level Schools\district_schools_new_final.dta", gen(d)
drop d

gen total= 1
browse Province Province_str geolev1
bysort Province : egen ProvincePop2019 = sum(total)
replace ProvincePop2019 = ProvincePop2019*22
label variable ProvincePop2019 "Estimated 2019 Province Level Population"

order ProvincePop2019, after(ProvincePop2012)

bysort new_district_id : egen NewDistPop2019 = sum(total)
replace NewDistPop2019 = NewDistPop2019*22
label variable NewDistPop2019 "Estimated 2019 District Level Population"

order NewDistPop2019, after(NewDistPop2012)

label variable new_district_id "New District IDs"
label variable new_dist_pop_share "New District Share of Province Population"
label variable new_dist_school_rounded "Number of Schools in New Districts (Rounded)"
label variable district_id "District IDs (2012 IPUMS)"
label variable ProvincePop2012 "Province Population 2012"
label variable NewDistPop2012 "New District Level Population 2012"
order Province Province_str ProvincePop2012 ProvincePop2019, after(geolev1)
order district_id new_district_id NewDistPop2012 NewDistPop2019 new_dist_pop_share, after (geolev2)
order Province_Primary_schools Province_Secondary_Schools,before (Province_Total_schools)
drop total


**********************************************************************************
* 1. Assigning Unique School ID to Teachers
**********************************************************************************

/*

            tabulation:  Freq.   Numeric  Label
                        64,878         0  niu (not in universe)
                        75,921        10  none
                        10,429        21  preschool, grade 1
                         4,771        22  preschool, grade 2
                        12,398        23  preschool, grade 3
                         1,732        29  preschool, grade unknown
                        23,902        31  primary, grade 1
                        25,081        32  primary, grade 2
                        27,182        33  primary, grade 3
                        25,987        34  primary, grade 4
                        28,806        35  primary, grade 5
                        30,004        36  primary, grade 6
                        72,466        37  primary, grade 7
                           598        38  primary, grade unknown
                        21,768        41  lower secondary, grade 1
                        39,902        42  lower secondary, grade 2
                        29,048        43  lower secondary, grade 3
                       119,121        44  lower secondary, grade 4
                         2,645        45  upper secondary, grade 5
                        10,079        46  upper secondary, grade 6
                           678        49  secondary, grade unknown
                         1,030        51  tertiary, certificate or diploma
                                          after primary
                        15,917        52  tertiary, certificate or diploma
                                          after secondary
                         7,966        53  tertiary, undergraduate or
                                          graduate studies
                           317        59  tertiary, unknown level
                         2,062        98  unknown
						 
						 
						 
--------------------------------------------------------------------------------------------------------------------------------------------------------------
teachers                                                                                                                                           (unlabeled)
--------------------------------------------------------------------------------------------------------------------------------------------------------------

                  type:  numeric (float)
                 label:  ISCO88A, but 1 nonmissing value is not labeled

                 range:  [0,235]                      units:  1
         unique values:  6                        missing .:  0/654,688

            tabulation:  Freq.   Numeric  Label
                       648,461         0  
                           374       231  college, university and higher
                                          education teaching professionals
                         1,852       232  secondary education teaching
                                          professionals
                         3,871       233  primary and pre-primary
                                          education teaching professionals
                            58       234  special education teaching
                                          professionals
                            72       235  other teaching professionals


*/




set seed 10000
**Primary Schools to Primary Schools Teachers (Assuming Primary Schools, Special Needs and other teachers as primary school teachers)
**Numbers of schools scaled to 5%
gen school_id_teachers =.
replace school_id_teachers = floor((15)*runiform() + 1) if new_district_id == 1 & inlist(teachers,233,234,235) 
replace school_id_teachers = floor((16)*runiform() + 1) if new_district_id == 2 & inlist(teachers,233,234,235) 
replace school_id_teachers = floor((8)*runiform() + 1) if new_district_id == 3 & inlist(teachers,233,234,235) 
replace school_id_teachers = floor((4)*runiform() + 1) if new_district_id == 4 & inlist(teachers,233,234,235) 
replace school_id_teachers = floor((10)*runiform() + 1) if new_district_id == 5 & inlist(teachers,233,234,235) 
replace school_id_teachers = floor((9)*runiform() + 1) if new_district_id == 6 & inlist(teachers,233,234,235) 
replace school_id_teachers = floor((14)*runiform() + 1) if new_district_id == 7 & inlist(teachers,233,234,235) 
replace school_id_teachers = floor((5)*runiform() + 1) if new_district_id == 8 & inlist(teachers,233,234,235) 
replace school_id_teachers = floor((4)*runiform() + 1) if new_district_id == 9 & inlist(teachers,233,234,235) 
replace school_id_teachers = floor((4)*runiform() + 1) if new_district_id == 10 & inlist(teachers,233,234,235) 
replace school_id_teachers = floor((3)*runiform() + 1) if new_district_id == 11 & inlist(teachers,233,234,235) 
replace school_id_teachers = floor((5)*runiform() + 1) if new_district_id == 12 & inlist(teachers,233,234,235) 
replace school_id_teachers = floor((6)*runiform() + 1) if new_district_id == 13 & inlist(teachers,233,234,235) 
replace school_id_teachers = floor((5)*runiform() + 1) if new_district_id == 14 & inlist(teachers,233,234,235) 
replace school_id_teachers = floor((2)*runiform() + 1) if new_district_id == 15 & inlist(teachers,233,234,235) 
replace school_id_teachers = floor((3)*runiform() + 1) if new_district_id == 16 & inlist(teachers,233,234,235) 
replace school_id_teachers = floor((3)*runiform() + 1) if new_district_id == 17 & inlist(teachers,233,234,235) 
replace school_id_teachers = floor((8)*runiform() + 1) if new_district_id == 18 & inlist(teachers,233,234,235) 
replace school_id_teachers = floor((5)*runiform() + 1) if new_district_id == 19 & inlist(teachers,233,234,235) 
replace school_id_teachers = floor((4)*runiform() + 1) if new_district_id == 20 & inlist(teachers,233,234,235) 
replace school_id_teachers = floor((6)*runiform() + 1) if new_district_id == 21 & inlist(teachers,233,234,235) 
replace school_id_teachers = floor((4)*runiform() + 1) if new_district_id == 22 & inlist(teachers,233,234,235) 
replace school_id_teachers = floor((3)*runiform() + 1) if new_district_id == 23 & inlist(teachers,233,234,235) 
replace school_id_teachers = floor((3)*runiform() + 1) if new_district_id == 24 & inlist(teachers,233,234,235) 
replace school_id_teachers = floor((2)*runiform() + 1) if new_district_id == 25 & inlist(teachers,233,234,235) 
replace school_id_teachers = floor((7)*runiform() + 1) if new_district_id == 26 & inlist(teachers,233,234,235) 
replace school_id_teachers = floor((9)*runiform() + 1) if new_district_id == 27 & inlist(teachers,233,234,235) 
replace school_id_teachers = floor((8)*runiform() + 1) if new_district_id == 28 & inlist(teachers,233,234,235) 
replace school_id_teachers = floor((2)*runiform() + 1) if new_district_id == 29 & inlist(teachers,233,234,235) 
replace school_id_teachers = floor((6)*runiform() + 1) if new_district_id == 30 & inlist(teachers,233,234,235) 
replace school_id_teachers = floor((7)*runiform() + 1) if new_district_id == 31 & inlist(teachers,233,234,235) 
replace school_id_teachers = floor((5)*runiform() + 1) if new_district_id == 32 & inlist(teachers,233,234,235) 
replace school_id_teachers = floor((9)*runiform() + 1) if new_district_id == 33 & inlist(teachers,233,234,235) 
replace school_id_teachers = floor((5)*runiform() + 1) if new_district_id == 34 & inlist(teachers,233,234,235) 
replace school_id_teachers = floor((6)*runiform() + 1) if new_district_id == 35 & inlist(teachers,233,234,235) 
replace school_id_teachers = floor((9)*runiform() + 1) if new_district_id == 36 & inlist(teachers,233,234,235) 
replace school_id_teachers = floor((5)*runiform() + 1) if new_district_id == 37 & inlist(teachers,233,234,235) 
replace school_id_teachers = floor((5)*runiform() + 1) if new_district_id == 38 & inlist(teachers,233,234,235) 
replace school_id_teachers = floor((6)*runiform() + 1) if new_district_id == 39 & inlist(teachers,233,234,235) 
replace school_id_teachers = floor((3)*runiform() + 1) if new_district_id == 40 & inlist(teachers,233,234,235) 
replace school_id_teachers = floor((6)*runiform() + 1) if new_district_id == 41 & inlist(teachers,233,234,235) 
replace school_id_teachers = floor((4)*runiform() + 1) if new_district_id == 42 & inlist(teachers,233,234,235) 
replace school_id_teachers = floor((4)*runiform() + 1) if new_district_id == 43 & inlist(teachers,233,234,235) 
replace school_id_teachers = floor((5)*runiform() + 1) if new_district_id == 44 & inlist(teachers,233,234,235) 
replace school_id_teachers = floor((4)*runiform() + 1) if new_district_id == 45 & inlist(teachers,233,234,235) 
replace school_id_teachers = floor((5)*runiform() + 1) if new_district_id == 46 & inlist(teachers,233,234,235) 
replace school_id_teachers = floor((4)*runiform() + 1) if new_district_id == 47 & inlist(teachers,233,234,235) 
replace school_id_teachers = floor((5)*runiform() + 1) if new_district_id == 48 & inlist(teachers,233,234,235) 
replace school_id_teachers = floor((4)*runiform() + 1) if new_district_id == 49 & inlist(teachers,233,234,235) 
replace school_id_teachers = floor((3)*runiform() + 1) if new_district_id == 50 & inlist(teachers,233,234,235) 
replace school_id_teachers = floor((4)*runiform() + 1) if new_district_id == 51 & inlist(teachers,233,234,235) 
replace school_id_teachers = floor((2)*runiform() + 1) if new_district_id == 52 & inlist(teachers,233,234,235) 
replace school_id_teachers = floor((2)*runiform() + 1) if new_district_id == 53 & inlist(teachers,233,234,235) 
replace school_id_teachers = floor((6)*runiform() + 1) if new_district_id == 54 & inlist(teachers,233,234,235) 
replace school_id_teachers = floor((9)*runiform() + 1) if new_district_id == 55 & inlist(teachers,233,234,235) 
replace school_id_teachers = floor((7)*runiform() + 1) if new_district_id == 56 & inlist(teachers,233,234,235) 
replace school_id_teachers = floor((8)*runiform() + 1) if new_district_id == 57 & inlist(teachers,233,234,235) 
replace school_id_teachers = floor((5)*runiform() + 1) if new_district_id == 58 & inlist(teachers,233,234,235) 
replace school_id_teachers = floor((3)*runiform() + 1) if new_district_id == 59 & inlist(teachers,233,234,235) 
replace school_id_teachers = floor((3)*runiform() + 1) if new_district_id == 60 & inlist(teachers,233,234,235) 

**Primary Schools to Primary Schools Students (Assuming Primary Schools, Special Needs and other teachers as primary school teachers)
**Numbers of schools scaled to 5%
gen school_id_student =.
replace school_id_student = floor((15)*runiform() + 1) if new_district_id == 1 & inrange(educzw,10,38) & economic_status == 1
replace school_id_student = floor((16)*runiform() + 1) if new_district_id == 2 & inrange(educzw,10,38) & economic_status == 1
replace school_id_student = floor((8)*runiform() + 1) if new_district_id == 3 & inrange(educzw,10,38) & economic_status == 1  
replace school_id_student = floor((4)*runiform() + 1) if new_district_id == 4 & inrange(educzw,10,38) & economic_status == 1 
replace school_id_student = floor((10)*runiform() + 1) if new_district_id == 5 & inrange(educzw,10,38) & economic_status == 1 
replace school_id_student = floor((9)*runiform() + 1) if new_district_id == 6 & inrange(educzw,10,38) & economic_status == 1 
replace school_id_student = floor((14)*runiform() + 1) if new_district_id == 7 & inrange(educzw,10,38) & economic_status == 1 
replace school_id_student = floor((5)*runiform() + 1) if new_district_id == 8 & inrange(educzw,10,38) & economic_status == 1 
replace school_id_student = floor((4)*runiform() + 1) if new_district_id == 9 & inrange(educzw,10,38) & economic_status == 1 
replace school_id_student = floor((4)*runiform() + 1) if new_district_id == 10 & inrange(educzw,10,38) & economic_status == 1 
replace school_id_student = floor((3)*runiform() + 1) if new_district_id == 11 & inrange(educzw,10,38) & economic_status == 1 
replace school_id_student = floor((5)*runiform() + 1) if new_district_id == 12 & inrange(educzw,10,38) & economic_status == 1 
replace school_id_student = floor((6)*runiform() + 1) if new_district_id == 13 & inrange(educzw,10,38) & economic_status == 1 
replace school_id_student = floor((5)*runiform() + 1) if new_district_id == 14 & inrange(educzw,10,38) & economic_status == 1 
replace school_id_student = floor((2)*runiform() + 1) if new_district_id == 15 & inrange(educzw,10,38) & economic_status == 1 
replace school_id_student = floor((3)*runiform() + 1) if new_district_id == 16 & inrange(educzw,10,38) & economic_status == 1 
replace school_id_student = floor((3)*runiform() + 1) if new_district_id == 17 & inrange(educzw,10,38) & economic_status == 1 
replace school_id_student = floor((8)*runiform() + 1) if new_district_id == 18 & inrange(educzw,10,38) & economic_status == 1 
replace school_id_student = floor((5)*runiform() + 1) if new_district_id == 19 & inrange(educzw,10,38) & economic_status == 1 
replace school_id_student = floor((4)*runiform() + 1) if new_district_id == 20 & inrange(educzw,10,38) & economic_status == 1 
replace school_id_student = floor((6)*runiform() + 1) if new_district_id == 21 & inrange(educzw,10,38) & economic_status == 1 
replace school_id_student = floor((4)*runiform() + 1) if new_district_id == 22 & inrange(educzw,10,38) & economic_status == 1 
replace school_id_student = floor((3)*runiform() + 1) if new_district_id == 23 & inrange(educzw,10,38) & economic_status == 1 
replace school_id_student = floor((3)*runiform() + 1) if new_district_id == 24 & inrange(educzw,10,38) & economic_status == 1 
replace school_id_student = floor((2)*runiform() + 1) if new_district_id == 25 & inrange(educzw,10,38) & economic_status == 1 
replace school_id_student = floor((7)*runiform() + 1) if new_district_id == 26 & inrange(educzw,10,38) & economic_status == 1 
replace school_id_student = floor((9)*runiform() + 1) if new_district_id == 27 & inrange(educzw,10,38) & economic_status == 1 
replace school_id_student = floor((8)*runiform() + 1) if new_district_id == 28 & inrange(educzw,10,38) & economic_status == 1 
replace school_id_student = floor((2)*runiform() + 1) if new_district_id == 29 & inrange(educzw,10,38) & economic_status == 1 
replace school_id_student = floor((6)*runiform() + 1) if new_district_id == 30 & inrange(educzw,10,38) & economic_status == 1 
replace school_id_student = floor((7)*runiform() + 1) if new_district_id == 31 & inrange(educzw,10,38) & economic_status == 1 
replace school_id_student = floor((5)*runiform() + 1) if new_district_id == 32 & inrange(educzw,10,38) & economic_status == 1 
replace school_id_student = floor((9)*runiform() + 1) if new_district_id == 33 & inrange(educzw,10,38) & economic_status == 1 
replace school_id_student = floor((5)*runiform() + 1) if new_district_id == 34 & inrange(educzw,10,38) & economic_status == 1 
replace school_id_student = floor((6)*runiform() + 1) if new_district_id == 35 & inrange(educzw,10,38) & economic_status == 1 
replace school_id_student = floor((9)*runiform() + 1) if new_district_id == 36 & inrange(educzw,10,38) & economic_status == 1 
replace school_id_student = floor((5)*runiform() + 1) if new_district_id == 37 & inrange(educzw,10,38) & economic_status == 1 
replace school_id_student = floor((5)*runiform() + 1) if new_district_id == 38 & inrange(educzw,10,38) & economic_status == 1 
replace school_id_student = floor((6)*runiform() + 1) if new_district_id == 39 & inrange(educzw,10,38) & economic_status == 1 
replace school_id_student = floor((3)*runiform() + 1) if new_district_id == 40 & inrange(educzw,10,38) & economic_status == 1 
replace school_id_student = floor((6)*runiform() + 1) if new_district_id == 41 & inrange(educzw,10,38) & economic_status == 1 
replace school_id_student = floor((4)*runiform() + 1) if new_district_id == 42 & inrange(educzw,10,38) & economic_status == 1 
replace school_id_student = floor((4)*runiform() + 1) if new_district_id == 43 & inrange(educzw,10,38) & economic_status == 1 
replace school_id_student = floor((5)*runiform() + 1) if new_district_id == 44 & inrange(educzw,10,38) & economic_status == 1 
replace school_id_student = floor((4)*runiform() + 1) if new_district_id == 45 & inrange(educzw,10,38) & economic_status == 1 
replace school_id_student = floor((5)*runiform() + 1) if new_district_id == 46 & inrange(educzw,10,38) & economic_status == 1 
replace school_id_student = floor((4)*runiform() + 1) if new_district_id == 47 & inrange(educzw,10,38) & economic_status == 1 
replace school_id_student = floor((5)*runiform() + 1) if new_district_id == 48 & inrange(educzw,10,38) & economic_status == 1 
replace school_id_student = floor((4)*runiform() + 1) if new_district_id == 49 & inrange(educzw,10,38) & economic_status == 1 
replace school_id_student = floor((3)*runiform() + 1) if new_district_id == 50 & inrange(educzw,10,38) & economic_status == 1 
replace school_id_student = floor((4)*runiform() + 1) if new_district_id == 51 & inrange(educzw,10,38) & economic_status == 1 
replace school_id_student = floor((2)*runiform() + 1) if new_district_id == 52 & inrange(educzw,10,38) & economic_status == 1 
replace school_id_student = floor((2)*runiform() + 1) if new_district_id == 53 & inrange(educzw,10,38) & economic_status == 1 
replace school_id_student = floor((6)*runiform() + 1) if new_district_id == 54 & inrange(educzw,10,38) & economic_status == 1 
replace school_id_student = floor((9)*runiform() + 1) if new_district_id == 55 & inrange(educzw,10,38) & economic_status == 1 
replace school_id_student = floor((7)*runiform() + 1) if new_district_id == 56 & inrange(educzw,10,38) & economic_status == 1 
replace school_id_student = floor((8)*runiform() + 1) if new_district_id == 57 & inrange(educzw,10,38) & economic_status == 1 
replace school_id_student = floor((5)*runiform() + 1) if new_district_id == 58 & inrange(educzw,10,38) & economic_status == 1 
replace school_id_student = floor((3)*runiform() + 1) if new_district_id == 59 & inrange(educzw,10,38) & economic_status == 1 
replace school_id_student = floor((3)*runiform() + 1) if new_district_id == 60 & inrange(educzw,10,38) & economic_status == 1 



**Secondary Schools to Secondary Schools Teachers (Assuming Secondary Schools, Tertiary Schools as Secondary school)
**Numbers of schools scaled to 5%
**Secondary School Teachers
replace school_id_teachers = floor((4)*runiform() + 1) if new_district_id == 1 & inlist(teachers,231,232) 
replace school_id_teachers = floor((13)*runiform() + 1) if new_district_id == 2 & inlist(teachers,231,232) 
replace school_id_teachers = floor((2)*runiform() + 1) if new_district_id == 3 & inlist(teachers,231,232) 
replace school_id_teachers = floor((2)*runiform() + 1) if new_district_id == 4 & inlist(teachers,231,232) 
replace school_id_teachers = floor((4)*runiform() + 1) if new_district_id == 5 & inlist(teachers,231,232) 
replace school_id_teachers = floor((4)*runiform() + 1) if new_district_id == 6 & inlist(teachers,231,232) 
replace school_id_teachers = floor((6)*runiform() + 1) if new_district_id == 7 & inlist(teachers,231,232) 
replace school_id_teachers = floor((2)*runiform() + 1) if new_district_id == 8 & inlist(teachers,231,232) 
replace school_id_teachers = floor((2)*runiform() + 1) if new_district_id == 9 & inlist(teachers,231,232) 
replace school_id_teachers = floor((2)*runiform() + 1) if new_district_id == 10 & inlist(teachers,231,232) 
replace school_id_teachers = floor((1)*runiform() + 1) if new_district_id == 11 & inlist(teachers,231,232) 
replace school_id_teachers = floor((2)*runiform() + 1) if new_district_id == 12 & inlist(teachers,231,232) 
replace school_id_teachers = floor((3)*runiform() + 1) if new_district_id == 13 & inlist(teachers,231,232) 
replace school_id_teachers = floor((2)*runiform() + 1) if new_district_id == 14 & inlist(teachers,231,232) 
replace school_id_teachers = floor((1)*runiform() + 1) if new_district_id == 15 & inlist(teachers,231,232) 
replace school_id_teachers = floor((1)*runiform() + 1) if new_district_id == 16 & inlist(teachers,231,232) 
replace school_id_teachers = floor((2)*runiform() + 1) if new_district_id == 17 & inlist(teachers,231,232) 
replace school_id_teachers = floor((4)*runiform() + 1) if new_district_id == 18 & inlist(teachers,231,232) 
replace school_id_teachers = floor((3)*runiform() + 1) if new_district_id == 19 & inlist(teachers,231,232) 
replace school_id_teachers = floor((2)*runiform() + 1) if new_district_id == 20 & inlist(teachers,231,232) 
replace school_id_teachers = floor((3)*runiform() + 1) if new_district_id == 21 & inlist(teachers,231,232) 
replace school_id_teachers = floor((2)*runiform() + 1) if new_district_id == 22 & inlist(teachers,231,232) 
replace school_id_teachers = floor((1)*runiform() + 1) if new_district_id == 23 & inlist(teachers,231,232) 
replace school_id_teachers = floor((2)*runiform() + 1) if new_district_id == 24 & inlist(teachers,231,232) 
replace school_id_teachers = floor((1)*runiform() + 1) if new_district_id == 25 & inlist(teachers,231,232) 
replace school_id_teachers = floor((4)*runiform() + 1) if new_district_id == 26 & inlist(teachers,231,232) 
replace school_id_teachers = floor((5)*runiform() + 1) if new_district_id == 27 & inlist(teachers,231,232) 
replace school_id_teachers = floor((4)*runiform() + 1) if new_district_id == 28 & inlist(teachers,231,232) 
replace school_id_teachers = floor((1)*runiform() + 1) if new_district_id == 29 & inlist(teachers,231,232) 
replace school_id_teachers = floor((3)*runiform() + 1) if new_district_id == 30 & inlist(teachers,231,232) 
replace school_id_teachers = floor((3)*runiform() + 1) if new_district_id == 31 & inlist(teachers,231,232) 
replace school_id_teachers = floor((2)*runiform() + 1) if new_district_id == 32 & inlist(teachers,231,232) 
replace school_id_teachers = floor((4)*runiform() + 1) if new_district_id == 33 & inlist(teachers,231,232) 
replace school_id_teachers = floor((2)*runiform() + 1) if new_district_id == 34 & inlist(teachers,231,232) 
replace school_id_teachers = floor((2)*runiform() + 1) if new_district_id == 35 & inlist(teachers,231,232) 
replace school_id_teachers = floor((4)*runiform() + 1) if new_district_id == 36 & inlist(teachers,231,232) 
replace school_id_teachers = floor((2)*runiform() + 1) if new_district_id == 37 & inlist(teachers,231,232) 
replace school_id_teachers = floor((2)*runiform() + 1) if new_district_id == 38 & inlist(teachers,231,232) 
replace school_id_teachers = floor((2)*runiform() + 1) if new_district_id == 39 & inlist(teachers,231,232) 
replace school_id_teachers = floor((1)*runiform() + 1) if new_district_id == 40 & inlist(teachers,231,232) 
replace school_id_teachers = floor((2)*runiform() + 1) if new_district_id == 41 & inlist(teachers,231,232) 
replace school_id_teachers = floor((1)*runiform() + 1) if new_district_id == 42 & inlist(teachers,231,232) 
replace school_id_teachers = floor((1)*runiform() + 1) if new_district_id == 43 & inlist(teachers,231,232) 
replace school_id_teachers = floor((2)*runiform() + 1) if new_district_id == 44 & inlist(teachers,231,232) 
replace school_id_teachers = floor((1)*runiform() + 1) if new_district_id == 45 & inlist(teachers,231,232) 
replace school_id_teachers = floor((2)*runiform() + 1) if new_district_id == 46 & inlist(teachers,231,232) 
replace school_id_teachers = floor((1)*runiform() + 1) if new_district_id == 47 & inlist(teachers,231,232) 
replace school_id_teachers = floor((2)*runiform() + 1) if new_district_id == 48 & inlist(teachers,231,232) 
replace school_id_teachers = floor((1)*runiform() + 1) if new_district_id == 49 & inlist(teachers,231,232) 
replace school_id_teachers = floor((1)*runiform() + 1) if new_district_id == 50 & inlist(teachers,231,232) 
replace school_id_teachers = floor((1)*runiform() + 1) if new_district_id == 51 & inlist(teachers,231,232) 
replace school_id_teachers = floor((1)*runiform() + 1) if new_district_id == 52 & inlist(teachers,231,232) 
replace school_id_teachers = floor((1)*runiform() + 1) if new_district_id == 53 & inlist(teachers,231,232) 
replace school_id_teachers = floor((3)*runiform() + 1) if new_district_id == 54 & inlist(teachers,231,232) 
replace school_id_teachers = floor((4)*runiform() + 1) if new_district_id == 55 & inlist(teachers,231,232) 
replace school_id_teachers = floor((3)*runiform() + 1) if new_district_id == 56 & inlist(teachers,231,232) 
replace school_id_teachers = floor((4)*runiform() + 1) if new_district_id == 57 & inlist(teachers,231,232) 
replace school_id_teachers = floor((2)*runiform() + 1) if new_district_id == 58 & inlist(teachers,231,232) 
replace school_id_teachers = floor((1)*runiform() + 1) if new_district_id == 59 & inlist(teachers,231,232) 
replace school_id_teachers = floor((1)*runiform() + 1) if new_district_id == 60 & inlist(teachers,231,232) 
 
**Secondary Schools to Secondary Schools Students (Assuming Secondary Schools, Tertiary Schools as Secondary schools)
**Numbers of schools scaled to 5%
**Secondary School Teachers
replace school_id_student = floor((5)*runiform() + 1) if new_district_id == 1 & inrange(educzw,41,59) & economic_status == 1
replace school_id_student = floor((14)*runiform() + 1) if new_district_id == 2 & inrange(educzw,41,59) & economic_status == 1
replace school_id_student = floor((3)*runiform() + 1) if new_district_id == 3 & inrange(educzw,41,59) & economic_status == 1
replace school_id_student = floor((2)*runiform() + 1) if new_district_id == 4 & inrange(educzw,41,59) & economic_status == 1
replace school_id_student = floor((4)*runiform() + 1) if new_district_id == 5 & inrange(educzw,41,59) & economic_status == 1
replace school_id_student = floor((4)*runiform() + 1) if new_district_id == 6 & inrange(educzw,41,59) & economic_status == 1
replace school_id_student = floor((6)*runiform() + 1) if new_district_id == 7 & inrange(educzw,41,59) & economic_status == 1
replace school_id_student = floor((2)*runiform() + 1) if new_district_id == 8 & inrange(educzw,41,59) & economic_status == 1
replace school_id_student = floor((2)*runiform() + 1) if new_district_id == 9 & inrange(educzw,41,59) & economic_status == 1
replace school_id_student = floor((2)*runiform() + 1) if new_district_id == 10 & inrange(educzw,41,59) & economic_status == 1
replace school_id_student = floor((1)*runiform() + 1) if new_district_id == 11 & inrange(educzw,41,59) & economic_status == 1
replace school_id_student = floor((2)*runiform() + 1) if new_district_id == 12 & inrange(educzw,41,59) & economic_status == 1
replace school_id_student = floor((3)*runiform() + 1) if new_district_id == 13 & inrange(educzw,41,59) & economic_status == 1
replace school_id_student = floor((2)*runiform() + 1) if new_district_id == 14 & inrange(educzw,41,59) & economic_status == 1
replace school_id_student = floor((1)*runiform() + 1) if new_district_id == 15 & inrange(educzw,41,59) & economic_status == 1
replace school_id_student = floor((1)*runiform() + 1) if new_district_id == 16 & inrange(educzw,41,59) & economic_status == 1
replace school_id_student = floor((2)*runiform() + 1) if new_district_id == 17 & inrange(educzw,41,59) & economic_status == 1
replace school_id_student = floor((4)*runiform() + 1) if new_district_id == 18 & inrange(educzw,41,59) & economic_status == 1
replace school_id_student = floor((3)*runiform() + 1) if new_district_id == 19 & inrange(educzw,41,59) & economic_status == 1
replace school_id_student = floor((2)*runiform() + 1) if new_district_id == 20 & inrange(educzw,41,59) & economic_status == 1
replace school_id_student = floor((3)*runiform() + 1) if new_district_id == 21 & inrange(educzw,41,59) & economic_status == 1
replace school_id_student = floor((2)*runiform() + 1) if new_district_id == 22 & inrange(educzw,41,59) & economic_status == 1
replace school_id_student = floor((1)*runiform() + 1) if new_district_id == 23 & inrange(educzw,41,59) & economic_status == 1
replace school_id_student = floor((2)*runiform() + 1) if new_district_id == 24 & inrange(educzw,41,59) & economic_status == 1
replace school_id_student = floor((1)*runiform() + 1) if new_district_id == 25 & inrange(educzw,41,59) & economic_status == 1
replace school_id_student = floor((4)*runiform() + 1) if new_district_id == 26 & inrange(educzw,41,59) & economic_status == 1
replace school_id_student = floor((5)*runiform() + 1) if new_district_id == 27 & inrange(educzw,41,59) & economic_status == 1
replace school_id_student = floor((4)*runiform() + 1) if new_district_id == 28 & inrange(educzw,41,59) & economic_status == 1
replace school_id_student = floor((1)*runiform() + 1) if new_district_id == 29 & inrange(educzw,41,59) & economic_status == 1
replace school_id_student = floor((3)*runiform() + 1) if new_district_id == 30 & inrange(educzw,41,59) & economic_status == 1
replace school_id_student = floor((3)*runiform() + 1) if new_district_id == 31 & inrange(educzw,41,59) & economic_status == 1
replace school_id_student = floor((2)*runiform() + 1) if new_district_id == 32 & inrange(educzw,41,59) & economic_status == 1
replace school_id_student = floor((4)*runiform() + 1) if new_district_id == 33 & inrange(educzw,41,59) & economic_status == 1
replace school_id_student = floor((2)*runiform() + 1) if new_district_id == 34 & inrange(educzw,41,59) & economic_status == 1
replace school_id_student = floor((2)*runiform() + 1) if new_district_id == 35 & inrange(educzw,41,59) & economic_status == 1
replace school_id_student = floor((4)*runiform() + 1) if new_district_id == 36 & inrange(educzw,41,59) & economic_status == 1
replace school_id_student = floor((2)*runiform() + 1) if new_district_id == 37 & inrange(educzw,41,59) & economic_status == 1
replace school_id_student = floor((2)*runiform() + 1) if new_district_id == 38 & inrange(educzw,41,59) & economic_status == 1
replace school_id_student = floor((2)*runiform() + 1) if new_district_id == 39 & inrange(educzw,41,59) & economic_status == 1
replace school_id_student = floor((1)*runiform() + 1) if new_district_id == 40 & inrange(educzw,41,59) & economic_status == 1
replace school_id_student = floor((2)*runiform() + 1) if new_district_id == 41 & inrange(educzw,41,59) & economic_status == 1
replace school_id_student = floor((1)*runiform() + 1) if new_district_id == 42 & inrange(educzw,41,59) & economic_status == 1
replace school_id_student = floor((1)*runiform() + 1) if new_district_id == 43 & inrange(educzw,41,59) & economic_status == 1
replace school_id_student = floor((2)*runiform() + 1) if new_district_id == 44 & inrange(educzw,41,59) & economic_status == 1
replace school_id_student = floor((1)*runiform() + 1) if new_district_id == 45 & inrange(educzw,41,59) & economic_status == 1
replace school_id_student = floor((2)*runiform() + 1) if new_district_id == 46 & inrange(educzw,41,59) & economic_status == 1
replace school_id_student = floor((1)*runiform() + 1) if new_district_id == 47 & inrange(educzw,41,59) & economic_status == 1
replace school_id_student = floor((2)*runiform() + 1) if new_district_id == 48 & inrange(educzw,41,59) & economic_status == 1
replace school_id_student = floor((1)*runiform() + 1) if new_district_id == 49 & inrange(educzw,41,59) & economic_status == 1
replace school_id_student = floor((1)*runiform() + 1) if new_district_id == 50 & inrange(educzw,41,59) & economic_status == 1
replace school_id_student = floor((1)*runiform() + 1) if new_district_id == 51 & inrange(educzw,41,59) & economic_status == 1
replace school_id_student = floor((1)*runiform() + 1) if new_district_id == 52 & inrange(educzw,41,59) & economic_status == 1
replace school_id_student = floor((1)*runiform() + 1) if new_district_id == 53 & inrange(educzw,41,59) & economic_status == 1
replace school_id_student = floor((3)*runiform() + 1) if new_district_id == 54 & inrange(educzw,41,59) & economic_status == 1
replace school_id_student = floor((4)*runiform() + 1) if new_district_id == 55 & inrange(educzw,41,59) & economic_status == 1
replace school_id_student = floor((3)*runiform() + 1) if new_district_id == 56 & inrange(educzw,41,59) & economic_status == 1
replace school_id_student = floor((4)*runiform() + 1) if new_district_id == 57 & inrange(educzw,41,59) & economic_status == 1
replace school_id_student = floor((2)*runiform() + 1) if new_district_id == 58 & inrange(educzw,41,59) & economic_status == 1
replace school_id_student = floor((1)*runiform() + 1) if new_district_id == 59 & inrange(educzw,41,59) & economic_status == 1
replace school_id_student = floor((1)*runiform() + 1) if new_district_id == 60 & inrange(educzw,41,59) & economic_status == 1


tostring school_id_student, replace
tostring school_id_teachers, replace 

replace school_id_student ="" if school_id_student == "."
replace school_id_teachers ="" if school_id_teachers == "."



replace school_id_student = school_id_student +"_s" if inrange(educzw,41,59) & economic_status == 1 
replace school_id_teacher = school_id_teacher +"_s" if inlist(teachers,231,232) 

replace school_id_student = school_id_student +"_p" if inrange(educzw,10,38) & economic_status == 1 
replace school_id_teacher = school_id_teacher +"_p" if inlist(teachers,233,234,235) 


**Combine them
gen str15 school_id = school_id_student + school_id_teacher


label variable school_id "Unique School ID within NEW District (Strings)"
label variable school_id_teacher "School ID within NEW District among teachers"
label variable school_id_student " School ID within NEW District among students"



**Set New District ID Strings 
tostring new_district_id ,gen(new_district_id_st)
gen school_id_district = new_district_id_st + "_" + school_id if school_id !=""


**********************************************************************************
* 2. Mining Districts and Mining ID
**********************************************************************************
**Manufacturing Workers
gen manufacturing_workers = 1 if inrange(isco88a,721,744)
replace manufacturing_workers = 1 if inlist(isco88a,828,829,833,932)

**Mining Workers
gen mining_workers = 1 if inlist(isco88a,711,811,931)

**From Website List + 3% Cutoff; Districts with Urban and Rural Designation are both included 
gen mining_districts = 1 if inlist(geo2_zw2012,7005,4003,6005,2007,9021,4007,6004,7006,2004,3005)
replace mining_districts = 1 if inlist(geo2_zw2012,1005,9022,6021,7004,4021,9001,1021,6001)
**Only for analysis to apply smartlockdown on identified mining districts

**Distribute Mininig DIstricts to all Districts within the Province.
gen mining_district_id = 0 if mining_workers ==1
replace mining_district_id = floor((1+1)*runiform() + 1) if  mining_workers == 1 & geo1_zw2012 ==1
replace mining_district_id = floor((2+1)*runiform() + 1) if  mining_workers == 1 & geo1_zw2012 ==2 
replace mining_district_id = floor((0+1)*runiform() + 1) if  mining_workers == 1 & geo1_zw2012 ==3 
replace mining_district_id = floor((2+1)*runiform() + 1) if  mining_workers == 1 & geo1_zw2012 ==4 
replace mining_district_id = floor((3+1)*runiform() + 1) if  mining_workers == 1 & geo1_zw2012 ==6 
replace mining_district_id = floor((2+1)*runiform() + 1) if  mining_workers == 1 & geo1_zw2012 ==7 
replace mining_district_id = floor((2+1)*runiform() + 1) if  mining_workers == 1 & geo1_zw2012 ==9 

tab1 mining_district_id 

tostring mining_district_id ,replace
replace mining_district_id = "" if mining_district_id =="."

tostring geo1_zw2012, gen(geo1_string)

replace mining_district_id  = geo1_string +"_"+mining_district_id if mining_district_id !=""


log using "D:\work\Zimbabwe\Expanding IPUMS data\data_dictionary.smcl",replace
describe
log close

label variable school_id_district "Unique school ID with District Designation for simulation"
label variable  manufacturing_workers "Indicator for manufacturing workers"
label variable mining_workers "Indicator for mining workers"
label variable   mining_districts "Indicator for mining districts"
label variable   mining_district_id "Minining District ID allocated to each miner within the province"

save  "D:\work\Zimbabwe\Economic Status for Agent Interaction Matric\abm_individual_new_dist_042420.dta" ,replace
save  "D:\work\Zimbabwe\Economic Status for Agent Interaction Matric\abm_individual_new_dist_051320.dta" ,replace

stop
**********************************************************************************
* 2. Check ID Distributions
**********************************************************************************

foreach num of numlist 1/22 {
use  "D:\work\Zimbabwe\Economic Status for Agent Interaction Matric\abm_individual_new_dist_041420.dta"  ,clear

**Persons ID
bysort serial: gen hh_nth = _n
gen person_id = serial + hh_nth
tostring person_id, replace
replace person_id = person_id + "_0`num'"

label variable person_id "Person ID in HH + Expanded Data ID"

gen serial_expanded = serial 
tostring serial_expanded , replace
replace serial_expanded = serial_expanded + "_0`num'"

label variable serial_expanded "HH Serial Number + Expanded Data ID"


gen expanded_data = `num'

order expanded_data serial_expanded person_id ,after(serial) 

save "D:\work\Zimbabwe\Expanding IPUMS data\expanded_`num'.dta",replace
}

**Append data

use "D:\work\Zimbabwe\Expanding IPUMS data\expanded_1.dta",clear

foreach num of numlist 2/22 {

append using  "D:\work\Zimbabwe\Expanding IPUMS data\expanded_`num'.dta"

}

save "D:\work\Zimbabwe\Expanding IPUMS data\expanded_final_041420.dta",replace
