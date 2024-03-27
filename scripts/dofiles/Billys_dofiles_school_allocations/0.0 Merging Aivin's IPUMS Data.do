**********************************************************************************
* Project: Zimbabwe Covid-19 Agent Based Modelling
* Module:  Merging Student Information from Aivin's IPUMS into our Downloaded IPUS
* Written by: Billy Hoo
* Last edited: 09/21/2020
**********************************************************************************

**********************************************************************************
* 1. PREPARING INDIVIDUAL DATASET 
**********************************************************************************
** Import Aivin's Dataset
clear
use "D:\work\Zimbabwe\Final Data\Data\5_percent_sample\Zimbabwe_2012_aivin_vers_091720" , clear

**Export Codebook

codebookout "D:\work\Zimbabwe\Final Data\Data\5_percent_sample\aivin_vers_codebokk.xls", replace

**Keep variables
keep serial pernum perwt zw2012a_dwnum zw2012a_pernum zw2012a_areatype zw2012a_hhtype zw2012a_agric zw2012a_ownershp ///
zw2012a_dwtype zw2012a_schever zw2012a_edlevel ///
zw2012a_edattain zw2012a_school zw2012a_schyr zw2012a_schlevel zw2012a_schgrade zw2012a_activity zw2012a_occ zw2012a_field


save "D:\work\Zimbabwe\Final Data\Data\5_percent_sample\Zimbabwe_2012_aivin_vers_091720_tomatch" , replace

**********************************************************************************
* 2. PREPARING NEW DISTRICTS 
**********************************************************************************
clear
import excel "D:\work\Zimbabwe\District Level Schools\district_school_random_filled.xlsx", sheet("New District School") firstrow clear
drop I
rename IPUM2012 district_id
rename NEW_DIST_ID_2 new_district_id
save "D:\work\Zimbabwe\Final Data\Geo_Info\new_district_info_091720.dta", replace

**********************************************************************************
* 1. Import Geo Information Extracted from Shpfile
**********************************************************************************
clear
import excel "D:\work\Zimbabwe\Final Data\Data\geo_information_frm_shpfile_060120.xlsx", sheet("Sheet1") firstrow
rename ID_1 province_id_shpfile
rename NAME_1 province_name_shpfile
rename ID_2 new_district_id
rename NAME_2 district_name_shpfile
label variable province_id_shpfile "Province ID from Shpfile"
label variable province_name_shpfile "Province Name (String) from Shpfile"
label variable district_name_shpfile "District Name (String) from Shpfile"
save "D:\work\Zimbabwe\Final Data\Geo_Info\geo_information_frm_shpfile_091720.dta", replace


**********************************************************************************
* 3. Match into downloaded IPUMS dataset
**********************************************************************************
** Import Aivin's Dataset

use "D:\work\Zimbabwe\Final Data\Data\5_percent_sample\ipumsi_00001" ,clear

merge 1:1 serial pernum using "D:\work\Zimbabwe\Final Data\Data\5_percent_sample\Zimbabwe_2012_aivin_vers_091720_tomatch"


gen district_id = geo2_zw2012
merge m:1 district_id using "D:\work\Zimbabwe\Final Data\Geo_Info\new_district_info_091720.dta", gen(_merge_geo)
merge m:1 new_district_id using "D:\work\Zimbabwe\Final Data\Geo_Info\geo_information_frm_shpfile_091720.dta", gen(_merge_geo2)

drop _merge
drop _merge_geo
drop _merge_geo2
