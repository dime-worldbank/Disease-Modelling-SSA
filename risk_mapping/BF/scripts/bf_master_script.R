
#####################################################
### MASTER SCRIPT - RISK FACTORS MAPPING DRC ########
#####################################################


# PROJECT:            Covid-19 Epidemiological Modeling Burkina Faso 
# PURPOSE:            Master script: packages, directory and calls to the construct and analysis scripts
# WRITTEN BY:         Marine




###########################################
################ 1. PACKAGES ##############
###########################################


if(!require("pacman")) install.packages("pacman")

pacman::p_load(tidyverse, foreign, readstata13, 
               kableExtra, scales, janitor, 
               ggthemes, DescTools, sf, sp,
               raster, sf, tmap,  classInt,
               geosphere, rgeos, maptools,
               stargazer, broom,  ggpubr, 
               leaflet, leaflet.extras, 
               pastecs, psych, RColorBrewer, 
               viridis, spatialEco, gstat, 
               htmlwidgets)




###########################################
############### 2. DIRECTORY ##############
###########################################


directory <- dirname(rstudioapi::getActiveDocumentContext()$path)
# The code line above determines the directory of this R script
directory
# check if the directory is correct

# or manually define directory
directory <- "/Users/marinedefranciosi/Documents/GitHub/Disease-Modelling-SSA/risk_mapping/BF"




###########################################
################ 3. SOURCE ################
###########################################


# function to source .Rmd file (instead of R script) as the analysis file is a rmarkdown file
source_rmd <- function(file, skip_plots = TRUE) {
  temp = tempfile(fileext=".R")
  knitr::purl(file, output=temp)
  
  if(skip_plots) {
    old_dev = getOption('device')
    options(device = function(...) {
      .Call("R_GD_nullDevice", PACKAGE = "grDevices")
    })
  }
  source(temp)
  if(skip_plots) {
    options(device = old_dev)
  }
}


# select code files to run
construct <- 1 
analysis <- 0 
# The analysis file will take a long time to run, I would suggest running code chunks one by one



if(construct == 1) {
  source(paste0(directory, "/scripts/bf_construct.R"), local = TRUE, echo = TRUE)
}


if(analysis = 1){
  source_rmd(paste0(directory, "/scripts/bf_risk_mapping.Rmd"), skip_plots = FALSE)
}

