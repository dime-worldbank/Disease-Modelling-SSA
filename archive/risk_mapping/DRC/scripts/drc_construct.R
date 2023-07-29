

#####################################################
### INDEX CONSTRUCTION - RISK FACTORS MAPPING DRC ###
#####################################################


# PROJECT:            Covid-19 Epidemiological Modeling DRC 
# PURPOSE:            Construct indices for 5 risk factors and aggregate at the DHS cluster level
# USES:               DHS datasets: CDHR61FL.DTA & CDAR61FL.DTA 
# PRODUCES:           drc_cluster.rds 
# WRITTEN BY:         Marine




      ###########################################
      ##########SET-UP: READING DATA ############
      ###########################################

setwd("~/GitHub/Disease-Modelling-SSA/risk_mapping/DRC/")

# reading Household Member Recode data from the DHS (Demographic and Health Survey)
dhs_drc <- readstata13::read.dta13(paste0(directory, "/input_data/dhs_data/CDHR61FL.DTA"), convert.factors = TRUE, generate.factors = TRUE)

# checking dimensions of the datasets
dim(dhs_drc)
# 18171 observations and 4721 variables 

sum(!duplicated(dhs_drc$hhid)) == dim(dhs_drc)[1]
# hhid uniquely identifies observations



        ######################################
        ### RISK FACTOR 1:HAND-WASHING INDEX 1
        ######################################


# water in dwelling
dhs_drc$water_dwelling <- NA
dhs_drc$water_dwelling[!is.na(dhs_drc$hv235) & !is.na(dhs_drc$hv230a)] <- 0
dhs_drc$water_dwelling[dhs_drc$hv235 == "1. in own dwelling" | dhs_drc$hv235 == "2. in own yard/plot"] <- 1

# observed hand-washing station
dhs_drc$handwash_station <- NA
dhs_drc$handwash_station[!is.na(dhs_drc$hv235) & !is.na(dhs_drc$hv230a)] <- 0
dhs_drc$handwash_station[dhs_drc$hv230a == "1. observed"] <- 1

# water at hand-washing station 
dhs_drc$handwash_water <- 0
dhs_drc$handwash_water[dhs_drc$water_avail_hw == "1. water is available"] <- 1

# soap at handwashing place
dhs_drc$handwash_soap <- 0
dhs_drc$handwash_soap[dhs_drc$soap == "1. yes"] <- 1


# HW risk as defined in paper:*

# 0 =  observed hand-washing station + water in hand-washing station + soap 
# 0.5 = observed hand-washing station + water hand-washing + no soap
# 1 =  observed hand-washing station + no water + no soap 

dhs_drc$hw_risk <- NA
dhs_drc$hw_risk[dhs_drc$handwash_station == 1 & dhs_drc$handwash_water == 1 & dhs_drc$handwash_soap == 1] <- 0
dhs_drc$hw_risk[dhs_drc$handwash_station == 1 & dhs_drc$handwash_water == 1 & dhs_drc$handwash_soap == 0] <- 0.5
dhs_drc$hw_risk[dhs_drc$handwash_station == 1 & dhs_drc$handwash_water == 0 & dhs_drc$handwash_soap == 0] <- 1

sum(is.na(dhs_drc$hw_risk))
# a lot of values are missing

# distribution of index values
table(dhs_drc$hw_risk)

# Sophie's index defined by do-file: 
table(dhs_drc$hw_risk2)
sum(is.na(dhs_drc$hw_risk2))

# normalize risk index between 0 and 1
dhs_drc$hw_risk2 <- (dhs_drc$hw_risk2 - min(dhs_drc$hw_risk2, na.rm = TRUE))/(max(dhs_drc$hw_risk2, na.rm = TRUE)-min(dhs_drc$hw_risk2, na.rm = TRUE))
table(dhs_drc$hw_risk2)


        ##############################
        ### RISK FACTOR 2: ANEMIA ####
        ##############################

# An observation represents one household in the DHS dataset
# However, some variables are specific to individuals within the household
# For instance for anemia, variables ha57_01 to ha57_18 give information on anemia levels for women (for the 1st to the 18th female member of the household)
# hb57_01 to hb57_12 give information on anemia levels for the 1st to the 12th male member of the household
# Anemia levels are coded as: 1 = severe, 2 = moderate, 3 = mild, 4 = not anemic
# Variables ha57_01 to ha57_18 & hb57_01 to hb57_08 take values between 1 and 4 
# Variables severe_anemia_men_hb57_1 or moderate_anemia_men_hb57_1 are dummy variables indicating whether the household member in question is severely or moderately anemic

# We need to calculate the proportion of severely anemic and moderately anemic out of all households members for which we have valid (non-missing) anemia values 
# Few households, if any, will have 8 male and 8 female members. 
#There will therefore be missing values for ha57_18 or hb57_12, they should not be taken into account when calculating proportions
# We therefore count the number of household members who are severely or moderately anemic and the overall number of household members with non-missing anemia values



      ### Men ###

# counting number of non-missing observations per household 
dhs_drc$total_valid_anemia_men <- apply(dhs_drc[, c("hb57_1", "hb57_2", "hb57_3", "hb57_4",
                                                    "hb57_5", "hb57_6", "hb57_7", "hb57_8")], 1, function(x) sum(!is.na(x)))

# counting number of severely anemic men per household:
dhs_drc$total_severe_anemia_men <- rowSums(dhs_drc[, c("severe_anemia_men_hb57_1", "severe_anemia_men_hb57_2",
                                                     "severe_anemia_men_hb57_3", "severe_anemia_men_hb57_4",
                                                     "severe_anemia_men_hb57_5", "severe_anemia_men_hb57_6", 
                                                     "severe_anemia_men_hb57_7", "severe_anemia_men_hb57_8")], na.rm = TRUE)


# counting number of moderately anemic men per household:
dhs_drc$total_moderate_anemia_men <- rowSums(dhs_drc[, c("moderate_anemia_men_hb57_1", "moderate_anemia_men_hb57_2",
                                                       "moderate_anemia_men_hb57_3", "moderate_anemia_men_hb57_4",
                                                       "moderate_anemia_men_hb57_5", "moderate_anemia_men_hb57_6",
                                                       "moderate_anemia_men_hb57_7", "moderate_anemia_men_hb57_8")], na.rm = TRUE)


# number of severely or moderately anemic men per household
dhs_drc$total_anemia_men <- dhs_drc$total_severe_anemia_men + dhs_drc$total_moderate_anemia_men



        ### Women ###

# counting number of non-missing observations per household:
dhs_drc$total_valid_anemia_women <- apply(dhs_drc[, c("ha57_1", "ha57_2", "ha57_3", "ha57_4",
                                                      "ha57_5", "ha57_6", "ha57_7", "ha57_8")], 1, function(x) sum(!is.na(x)))

# counting number of severely anemic women per household:
dhs_drc$total_severe_anemia_women <- rowSums(dhs_drc[, c("severe_anemia_women_ha57_1", "severe_anemia_women_ha57_2",
                                                       "severe_anemia_women_ha57_3", "severe_anemia_women_ha57_4",
                                                       "severe_anemia_women_ha57_5", "severe_anemia_women_ha57_6",
                                                       "severe_anemia_women_ha57_7", "severe_anemia_women_ha57_8")], na.rm = TRUE)

# counting number of severely anemic women per household:
dhs_drc$total_moderate_anemia_women <- rowSums(dhs_drc[, c("moderate_anemia_women_ha57_1", "moderate_anemia_women_ha57_2",
                                                         "moderate_anemia_women_ha57_3", "moderate_anemia_women_ha57_4",
                                                         "moderate_anemia_women_ha57_5", "moderate_anemia_women_ha57_6",
                                                         "moderate_anemia_women_ha57_7", "moderate_anemia_women_ha57_8")], na.rm = TRUE)


# number of severely or moderately anemic men per household
dhs_drc$total_anemia_women <- dhs_drc$total_severe_anemia_women + dhs_drc$total_moderate_anemia_women


          ### All: Women and Men ###

# counting number of non-missing observations per household (men + women)
dhs_drc$total_valid_anemia <- dhs_drc$total_valid_anemia_men + dhs_drc$total_valid_anemia_women

# number of severely or moderately anemic men AND women per household
dhs_drc$total_anemia <- dhs_drc$total_anemia_men + dhs_drc$total_anemia_women


        ##############################
        #### RISK FACTOR 3: OBESITY ##
        ##############################

# obesity and overweight data are based on the BMI Index
# These data are only available for women in the DHS dataset
# BMI values within the 25-30 range indicate that the individual is overweight 
# BMI values above 30 indicate obesity 

# The variables ha40_1 to ha40_8 include the bmi values 
# The variables ow_ha40_1 to ow_ha40_8 are dummies for whether the individual is overweight 
# The variables obese_ha40_1 to obese_ha40_8 are dummies for whether the individual is obese

# number of valid (non_missing) observations for BMI data
cols_bmi <- c("ha40_1", "ha40_2", "ha40_3", "ha40_4", "ha40_5", "ha40_6",
              "ha40_7", "ha40_8")
dhs_drc$total_valid_bmi <- apply(dhs_drc[, cols_bmi], 1, function(x) sum(!is.na(x)))


# counting the number of overweight individuals 
cols_ow <- c("ow_ha40_1", "ow_ha40_2", "ow_ha40_3", "ow_ha40_4", "ow_ha40_5", "ow_ha40_6",
             "ow_ha40_7", "ow_ha40_8")
dhs_drc$ow_total <- rowSums(dhs_drc[, cols_ow], 
                           na.rm = TRUE)

# counting the number of obese individuals
cols_obese <- c("obese_ha40_1", "obese_ha40_2", "obese_ha40_3", "obese_ha40_4", "obese_ha40_5", "obese_ha40_6",
                "obese_ha40_7", "obese_ha40_8")
dhs_drc$obese_total <- rowSums(dhs_drc[, cols_obese], 
                              na.rm = TRUE)


        ###############################
        #### RISK FACTOR 4: SMOKING ###
        ###############################


# counting the number of valid (non-missing observations) for smoking

    ## men ## 
cols_smoke_men <- c("hb35_1", "hb35_2", "hb35_3", "hb35_4", "hb35_5", "hb35_6", "hb35_7", "hb35_8")
dhs_drc$total_valid_smoke_men <- apply(dhs_drc[, cols_smoke_men], 1, function(x) sum(!is.na(x)))

    ## women ##
cols_smoke_women <- c("ha35_1", "ha35_2", "ha35_3", "ha35_4", "ha35_5", "ha35_6", "ha35_7", "ha35_8")
dhs_drc$total_valid_smoke_women <- apply(dhs_drc[, cols_smoke_women], 1, function(x) sum(!is.na(x)))

dhs_drc$total_valid_smoke_women <- apply(dhs_drc[, cols_smoke_women], 1, function(x) sum(!is.na(x)))

    ## women and men ## 
dhs_drc$total_valid_smoke <- dhs_drc$total_valid_smoke_men + dhs_drc$total_valid_smoke_women


# variables s_hb35_1 to s_hb35_8 are dummies for whether the household member smokes

# counting the number of men within the household who smoke 
dhs_drc$total_smoking_men <- rowSums(dhs_drc[, c("s_hb35_1", "s_hb35_2", "s_hb35_3", "s_hb35_4",
                                             "s_hb35_5", "s_hb35_6", "s_hb35_7", "s_hb35_8")], na.rm = TRUE)

# counting the number of women within the household who smoke 
dhs_drc$total_smoking_women <- rowSums(dhs_drc[, c("s_ha35_1", "s_ha35_2", "s_ha35_3", "s_ha35_4",
                                               "s_ha35_5", "s_ha35_6", "s_ha35_7", "s_ha35_8")], na.rm = TRUE)
# adding numbers men and women 
dhs_drc$total_smoking <- dhs_drc$total_smoking_men + dhs_drc$total_smoking_women



      #################################
      #### AGGREGATING BY CLUSTER: ####
      #################################

# by cluster: 

drc_cluster <- dhs_drc %>% 
  group_by(hv001) %>% 
  summarise(hw_risk = mean(hw_risk, na.rm = TRUE), 
            hw_risk2 = mean(hw_risk2, na.rm = TRUE),
            total_valid_anemia = sum(total_valid_anemia, na.rm = TRUE),
            total_anemia = sum(total_anemia, na.rm = TRUE),
            total_anemia_women = sum(total_anemia_women, na.rm = TRUE), 
            total_valid_anemia_women = sum(total_valid_anemia_women, na.rm = TRUE), 
            total_anemia_men = sum(total_anemia_men, na.rm = TRUE), 
            total_valid_anemia_men = sum(total_valid_anemia_men, na.rm = TRUE),
            total_valid_bmi = sum(total_valid_bmi, na.rm = TRUE), 
            obese_total = sum(obese_total, na.rm = TRUE), 
            ow_total = sum(ow_total, na.rm = TRUE), 
            total_valid_smoke_men = sum(total_valid_smoke_men, na.rm = TRUE), 
            total_valid_smoke_women = sum(total_valid_smoke_women, na.rm = TRUE), 
            total_valid_smoke  = sum(total_valid_smoke, na.rm = TRUE), 
            total_smoking_men = sum(total_smoking_men, na.rm = TRUE), 
            total_smoking_women = sum(total_smoking_women, na.rm = TRUE),
            total_smoking = sum(total_smoking, na.rm = TRUE)) %>% 
  mutate(clusterid = hv001, 
         anemia_women_prop = total_anemia_women/total_valid_anemia_women,
         anemia_men_prop = total_anemia_men/total_valid_anemia_men, 
         anemia_prop = total_anemia/ total_valid_anemia,
         obese_prop = obese_total/total_valid_bmi, 
         ow_prop = ow_total/total_valid_bmi, 
         smoke_prop_women = total_smoking_women/total_valid_smoke_women, 
         smoke_prop_men = total_smoking_men/total_valid_smoke_men, 
         smoke_prop = total_smoking/total_valid_smoke, 
         total_non_smoke = total_valid_smoke - total_smoking, 
         total_non_obese = total_valid_bmi - obese_total, 
         total_non_anemia = total_valid_anemia - total_anemia)


drc_cluster2 <- dhs_drc %>% 
  filter(!is.na(dhs_drc$hw_risk)) %>% 
  group_by(hv001) %>% 
  summarise(total_valid_hw = n()) %>% 
  mutate(clusterid = hv001)

drc_cluster2 <- dhs_drc %>% 
  filter(!is.na(dhs_drc$hw_risk2)) %>% 
  group_by(hv001) %>% 
  summarise(total_valid_hw2 = n()) %>% 
  mutate(clusterid = hv001)

drc_cluster <- left_join(x = drc_cluster, y = drc_cluster2[, c("clusterid", "total_valid_hw")], by = "clusterid")
drc_cluster <- left_join(x = drc_cluster, y = drc_cluster3[, c("clusterid", "total_valid_hw2")], by = "clusterid")
drc_cluster$total_valid_hw[is.na(drc_cluster$total_valid_hw)] <- 0
drc_cluster$total_valid_hw2[is.na(drc_cluster$total_valid_hw2)] <- 0




        #################################
        ##### RISK FACTOR 5: HIV  #######
        #################################


hiv_cluster <- read.dta13(paste0(directory, "/input_data/dhs_data/CDAR61FL.DTA"))

# proportion of HIV positive individuals by cluster 

hiv_cluster$hiv03 <- as.character(hiv_cluster$hiv03)
hiv_cluster$hiv03[hiv_cluster$hiv03 == "hiv negative"] <- "0"
hiv_cluster$hiv03[hiv_cluster$hiv03 == "hiv  positive"] <- "1"

hiv_cluster$hiv03 <- as.integer(hiv_cluster$hiv03)

hiv_cluster <- hiv_cluster %>% 
  group_by(hivclust) %>% 
  summarise(hiv_positive = sum(hiv03, na.rm = TRUE),
            hiv_prop = mean(hiv03, na.rm = TRUE)) %>% 
  mutate(clusterid = hivclust, 
         hiv_prop = hiv_prop*100)

hiv_cluster <- hiv_cluster[, -1]

drc_cluster <- left_join(x = drc_cluster, y = hiv_cluster, by = "clusterid")


saveRDS(drc_cluster, paste0(directory, "/input_data/constructed_data/drc_cluster.rds"))






