## Calculate proportion of symptomatics that require hospitalization
## across ZWE
# zwe_adm2 <- readRDS('/Users/sturrockh/Documents/Work/Lnl/covid_modeling_ZWE/gadm36_ZWE_2_sp.rds')
# zwe_adm2 <- st_as_sf(zwe_adm2)
# pop_dens_raw <- raster('~/Documents/Work/Lnl/covid_modeling_ZWE/data/pop_dens_1km.tif')

load("~/Documents/Work/Lnl/covid_modeling_ZWE/outputs/zwe_disease_severity.RData")
age_breakdow_dist <- read.csv("~/Documents/Work/Lnl/covid_modeling_ZWE/abm_district_age_052920.csv")
age_breakdow_dist <- merge(age_breakdow_dist, 
                           zwe_adm2[,c("NAME_2", "total_pop")],
                           by = "NAME_2")
num_per_age_cat <- apply(age_breakdow_dist, 1, 
                         function(x){as.numeric(x[10:24]) * as.numeric(x[26])})
age_breakdow_national <- apply(num_per_age_cat, 1, sum) / sum(num_per_age_cat)
age_risk <- read.csv("~/Documents/Work/Lnl/covid_modeling_ZWE/abm_hosp_risk_age.csv")
age_risk$Proportion.of.Symptomatics.Hospitalised <- age_risk$Proportion.of.Infections.Hospitalised*1.6666

# Calc weighted average
sum(age_risk$Proportion.of.Symptomatics.Hospitalised * age_breakdow_national)

