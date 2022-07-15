infix using "C:\COUNTRIES\ZW70\ZWFW71FL.dct"

label variable fw101    "Fieldworker code"
label variable fw000    "Country code and phase"
label variable fw102    "Fieldworker region of residence"
label variable fw103    "Fieldworker type of place of residence"
label variable fw104    "Fieldworker age"
label variable fw105    "Fieldworker sex"
label variable fw106    "Fieldworker marital status"
label variable fw107    "Fieldworker number of living children"
label variable fw108    "Fieldworker ever had a child who died"
label variable fw109    "Highest level of school attended by fieldworker"
label variable fw110    "Highest grade/form/year at that level completed by fieldworker"
label variable fw111    "Fieldworker religion"
label variable fw112    "NA - Fieldworker ethnicity"
label variable fw113a   "Language A"
label variable fw113b   "Language B"
label variable fw113c   "Language C"
label variable fw113d   "Language D"
label variable fw113e   "Language E"
label variable fw113f   "Language F"
label variable fw113g   "Language G"
label variable fw113h   "Language H"
label variable fw113i   "Language I"
label variable fw113j   "Language J"
label variable fw113k   "Language K"
label variable fw113l   "Language L"
label variable fw113m   "Language M"
label variable fw113x   "Other language"
label variable fw113y   "No other language"
label variable fw114    "Fieldworker's mother tongue/native language"
label variable fw115    "Has fieldworker ever worked on a DHS"
label variable fw116    "Has fieldworker ever worked on any other survey"
label variable fw117    "Was fieldworker working for agency at the time employed for DHS"
label variable fw118    "Is fieldworker a permanent or temporary employee of agency"

#delimit ;
label define FW102   
     1 "Bulawayo"
     2 "Manicaland"
     3 "Mashonaland central"
     4 "Mashonaland east"
     5 "Mashonaland west"
     6 "Matabeleland north"
     7 "Matabeleland south"
     8 "Midlands"
     9 "Masvingo"
    10 "Harare"
;
label define FW103   
     1 "Urban"
     2 "Rural"
;
label define FW104   
    99 "Missing"
;
label define FW105   
     1 "Male"
     2 "Female"
;
label define FW106   
     1 "Currently married"
     2 "Living with a man/woman"
     3 "Widowed"
     4 "Divorced"
     5 "Separated"
     6 "Never married or lived with a man/woman"
;
label define FW107   
    99 "Missing"
;
label define FW108   
     1 "Yes"
     2 "No"
;
label define FW109   
     1 "Primary"
     2 "Secondary"
     3 "Higher"
;
label define FW110   
     0 "Less than one year at level"
;
label define FW111   
     1 "Traditional"
     2 "Roman catholic"
     3 "Protestant"
     4 "Pentecostal"
     5 "Apostolic sect"
     6 "Other christian"
     7 "Muslim"
    95 "No religion"
    96 "Other"
;
label define FW114   
     1 "Shona"
     2 "Ndebele"
     3 "English"
     4 "Nambia"
     5 "Venda"
     6 "Tonga"
    96 "Other"
;
label define FW115   
     1 "Yes"
     2 "No"
;
label define FW116   
     1 "Yes"
     2 "No"
;
label define FW117   
     1 "Yes, ZIMSTAT"
     3 "No"
;
label define FW118   
     1 "Permanent"
     2 "Temporary"
;

#delimit cr
label values fw102    FW102   
label values fw103    FW103   
label values fw105    FW105   
label values fw106    FW106   
label values fw108    FW108   
label values fw109    FW109   
label values fw110    FW110   
label values fw111    FW111   
label values fw114    FW114   
label values fw115    FW115   
label values fw116    FW116   
label values fw117    FW117   
label values fw118    FW118   
