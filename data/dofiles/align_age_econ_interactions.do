**Zimbabwe Covid 
**Purpose: Align social mixing patterns by age and occupation using Manicaland paper
**by Sophie Ayling
**Version : 13th Jan 2021
 
set more off 
cap cd "/Users/sophieayling/Documents/GitHub/Disease-Modelling-SSA/data/"

**100% sample
*use "raw/census/abm_individual_new_092220_final_merged_complete_FINAL_orig.dta", clear 

***5% sample 
**check if the proportions in the 5% sample are the same when carrying out this process
use "raw/census/5_perc_sample/census_sample_5perc_092320", clear



*create the age range categories same as in Manicaland age based contacts 
gen age_c=.
replace age_c=5 if age<=5
replace age_c=10 if age>5 & age<=10
replace age_c=15 if age>10 & age<=15
replace age_c=20 if age>15 & age<=20
replace age_c=25 if age>20 & age<=25
replace age_c=30 if age>25 & age<=30
replace age_c=35 if age>30 & age<=35
replace age_c=40 if age>35 & age<=40
replace age_c=45 if age>40 & age<=45
replace age_c=50 if age>45 & age<=50
replace age_c=55 if age>50 & age<=55
replace age_c=60 if age>55 & age<=60
replace age_c=65 if age>60 & age<=65
replace age_c=70 if age>65 & age<=70
replace age_c=80 if age>70 & age<=100

tab age_c, m

*check distribution of occupation categories WITHIN each age group
tab economic_status,nol

foreach x in 5 10 15 20 25 30 35 40 45 50 55 60 65 70 80 {
	tab economic_status if age_c==`x'
}

*check distribution of ages WITHIN each economic_status group

forvalues i = 0/8 {
	tab age_c if economic_status==`i'
}

*create teachers subgroup within econ status and add to tabulation 

replace economic_status = 9 if teachers!=0

*assign each individual a number of interactions based on their econ status from OUR matrix

*super helpful command that adds the numbers to the value labels
label dir
numlabel `r(names)', add

tab economic_status

*check the average age for each economic status 
replace age=. if age==999
forvalues i =0/9 {
	tabstat age if economic_status==`i', stats(median mean range)
} 

*interaction numbers we decided on were the 'Sophie's edits' column of 'age-econ matrix.xlsx', comparsion sheet

gen interactions_all=0
replace interactions_all=6 if economic_status==0  // inactive, niu 
//median age 3 would be 7.5 interactions
replace interactions_all=12 if economic_status==1 // current students 
// median age 11 would be 12.45 interactions
replace interactions_all=9 if economic_status==2 // homemakers
// median age 29 would be 9.7 interactions
replace interactions_all=11 if economic_status==3 // office workers 
// median age of 35 would be 11.8 interactions
replace interactions_all=14 if economic_status==4 // service workers 
// median age of 31 would be 11.8 interactions
replace interactions_all=9 if economic_status==5 //ag workers 
// median age of 35 11.8 interactions
replace interactions_all=12 if economic_status==6  // industry workers 
// median age of 35, 11.8 interactions
replace interactions_all=14 if economic_status==7 // in the army 
// median age of 28
replace interactions_all=6 if economic_status==8 //disabled or not working
// median age of 73 would be 11.8
replace interactions_all=30 if economic_status==9 // teachers 
// median age of 39 12.5 interactions 

foreach x in 5 10 15 20 25 30 35 40 45 50 55 60 65 70 80 {
	sum interactions if age_c==`x'
}

** now generate no. work/school interactions based on hh size

*average household size 
replace persons=. if persons==99

*for all data
* sum persons  if hhtype !=11 & hhtype !=99 // av households size 5.13 - in 100% sample
sum persons if zw2012a_hhtype ==1 // hh size is still av 5.13 so we are good for 5% sample 

* no. interactions for work= total-(6 (-5.12-1, rounded, excluding self)) 
** updated using melegaro tables for school and work 
gen interactions_work=0
replace interactions_work=1 if economic_status==0  // inactive, niu (closer to zero but must assume one)
replace interactions_work=6 if economic_status==1 // current students 
replace interactions_work=3 if economic_status==2 // homemakers
replace interactions_work=5 if economic_status==3 // office workers 
replace interactions_work=10 if economic_status==4 // service workers (5 for that age range, but assumed double)
replace interactions_work=5 if economic_status==5 //ag workers 
replace interactions_work=5 if economic_status==6  // industry workers 
replace interactions_work=10 if economic_status==7 // in the army (assumed double)
replace interactions_work=1 if economic_status==8 //disabled or not working (closer to zero but must assume one)
replace interactions_work=12 if economic_status==9 // teachers (assumed double that of students)


*now check the number of interactions by age 
foreach x in 5 10 15 20 25 30 35 40 45 50 55 60 65 70 80 {
	sum interactions_work if age_c==`x'
}


*for each econ status 
/*
*for 100% sample
forvalues i = 0/9 {
	sum persons if economic_status==`i' & hhtype !=99
} 
*/
*for 5% sample 
forvalues i = 0/9 {
	sum persons if economic_status==`i' & zw2012a_hhtype ==1
} 

*see if household size varies by age group (it shouldn't as that would be strange)
foreach x in 5 10 15 20 25 30 35 40 45 50 55 60 65 70 80 {
	sum persons if age_c==`x'
} 
//it's fine

*get the median - though using the median age aligned nicely with manicaland, it didn't display what we think to be likely variance by econ group  
/*
*100% sample
forvalues i = 0/9 {
	tabstat persons if economic_status==`i' & hhtype !=99, stats(median)
} 
*/

forvalues i = 0/9 {
	tabstat persons if economic_status==`i' & zw2012a_hhtype ==1, stats(median)
}


