
## Respiratory illness ZWE
library(sf)
library(mgcv)
library(raster)
library(lubridate)
library(sf)

setwd("~/Documents/Work/Lnl/GitRepos/fn-covid19-prev-pred")
source('R/prevalence_predictor_mgcv_st_grid.R')

# Testing prevalence_predictor_mgcv_st_grid
resp_ill <- read.csv("~/Documents/Work/Lnl/covid_modeling_ZWE/data/dhs_cluster_illness_rate_gps_041620.csv")

# Organize data
df_trim <- data.frame(lng = resp_ill$LONGNUM,
                                 lat = resp_ill$LATNUM,
                                 n_positive = resp_ill$count_any, 
                                 n_trials = resp_ill$total_children_asked)

#boundary <- raster::getData("GADM", country = "ZWE", level = 0)
#boundary <- st_as_sf(boundary)
#boundary <- st_write(boundary, "data/ZWE_boundary.geojson")

# Prepare covariates
pred_raster_hw <- raster("~/Documents/Work/Lnl/covid_modeling_ZWE/outputs/pred_raster_hw.tif")
pred_raster_w <- raster("~/Documents/Work/Lnl/covid_modeling_ZWE/outputs/pred_raster_w.tif")
poverty_sf <- st_read('/Users/sturrockh/Dropbox/Data to be shared/poverty data/poverty_adminzones/zwe_adm3_2002_v2_utm.shp')
poverty_df <- read.csv("/Users/sturrockh/Dropbox/Data to be shared/poverty data/poverty_adminzones/master_092518_simplified.csv")
poverty_df <- poverty_df[,c("wardpcode02_v2", "Foodpovertyprevalence")]
poverty_sf <- merge(poverty_sf, poverty_df, by.x = "wardpcode0",
                    by.y = "wardpcode02_v2")
poverty_sf <- st_transform(poverty_sf, crs(pred_raster_w))
poverty_sp <- as(poverty_sf, "Spatial")
poverty_raster <- rasterize(poverty_sp, pred_raster_hw, field = "Foodpovertyprevalence")
names(poverty_raster) <- "poverty"
pop_dens <- raster('~/Documents/Work/Lnl/covid_modeling_ZWE/data/pop_dens_1km.tif')
#pop_dens <- aggregate(pop_dens, 30, sum, na.rm=T)
pop_dens <- resample(pop_dens, pred_raster_w)
pop_dens[is.na(pop_dens[])] <- 0
names(pop_dens) <- 'pop_dens'
covs <- stack(pred_raster_hw, pred_raster_w, pop_dens, poverty_raster)

# Test
system.time(res <- prevalence_predictor_mgcv_st_grid(df_trim,
                                                     boundary = '~/Documents/Work/Lnl/covid_modeling_ZWE/data/ZWE_boundary.geojson',
                                                     covariates = covs,
                                                     ncol_pred_grid = 500))
plot(res)

# Generate district level pop-weighted means
zwe_adm2 <- readRDS('/Users/sturrockh/Documents/Work/Lnl/covid_modeling_ZWE/gadm36_ZWE_2_sp.rds')
zwe_adm2 <- st_as_sf(zwe_adm2)
res_resamp <- resample(res, pop_dens)
#writeRaster(res_resamp, "~/Documents/Work/Lnl/covid_modeling_ZWE/outputs/resp_illness_prev.tif")

# Create pop-weighted district means
zwe_adm2$total_pop <- extract(pop_dens, zwe_adm2, sum, na.rm=T)
weighted_sum_res <- extract(res_resamp*pop_dens, zwe_adm2, sum, na.rm=T)
zwe_adm2$mean_resp_risk_pop_weighted <- weighted_sum_res / zwe_adm2$total_pop
write.csv(st_drop_geometry(zwe_adm2),
          "~/Documents/Work/Lnl/covid_modeling_ZWE/outputs/district_resp_risk.csv", row.names = F)

# Create ward level
poverty_sf$total_pop <- extract(pop_dens, poverty_sf, sum, na.rm=T)
weighted_sum_res_ward <- extract(res_resamp*pop_dens, poverty_sf, sum, na.rm=T)
poverty_sf$mean_resp_risk_pop_weighted <- weighted_sum_res_ward / poverty_sf$total_pop
write.csv(st_drop_geometry(poverty_sf[,c('wardpcode0',
                                    'mean_resp_risk_pop_weighted')]),
          "~/Documents/Work/Lnl/covid_modeling_ZWE/outputs/ward_resp_risk.csv", row.names = F)
