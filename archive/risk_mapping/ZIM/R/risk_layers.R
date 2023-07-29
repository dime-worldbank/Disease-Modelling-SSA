library(leaflet)
library(sf)
library(raster)
library(mgcv)
library(disarmr)
setwd("~/Documents/Work/Lnl/covid_modeling_ZWE/")
risk_data <- read.csv("dhs_cluster_water_covid_risk_gps.csv")
risk_data_sf <- st_as_sf(risk_data, coords = c('LONGNUM', 'LATNUM'))
pop <- raster('/Users/sturrockh/Documents/Work/GIS/WorldPop/global/ppp_2020_1km_Aggregated.tif')
wards <- st_read('/Users/sturrockh/Dropbox/Data to be shared/admin zone boundaries/Zimbabwe admin zones/Zimbabwe_admin_zone.shp')
wards <- st_simplify(wards, dTolerance = 100)
wards <- st_transform(wards, crs(pop))
pop <- crop(pop, wards)
pop <- mask(pop, wards)

# Import covariates
NTL <- raster("covariate_rasters/zwe_viirs_100m_2015.tif")
dist_to_road <- raster("covariate_rasters/zwe_osm_dst_road_100m_2016.tif")
NTL <- resample(NTL, pop)
dist_to_road <- resample(dist_to_road, pop)

# Map
quick_map(risk_data_sf, 'hw_risk_index')
quick_map(risk_data_sf, 'w_risk_index')

# Extract covars
risk_data_sf$NTL <- extract(NTL, risk_data_sf)
risk_data_sf$dist_to_road <- extract(dist_to_road, risk_data_sf)
risk_data_df <- as.data.frame(risk_data_sf)
risk_data_df <- cbind(risk_data_df, st_coordinates(risk_data_sf))

# Standardize weights
risk_data_df$weights <- risk_data_df$total_hhs / mean(risk_data_df$total_hhs)

# Fit beta model
gam_mod_hw <- gam(hw_risk_index ~ s(NTL) + s(dist_to_road) + s(X, Y, k=300),
               data = risk_data_df, family = "betar", select = T, weights = weights)
gam.check(gam_mod_hw)
gam_mod_w <- gam(w_risk_index ~ s(NTL, k=50) + s(dist_to_road) + s(X, Y, k=300),
               data = risk_data_df, family = "betar", select = T, weights = total_hhs)
# gam_mod_w_2 <- gam(risk_data_sf$w_risk_index ~ te(NTL, dist_to_road, k=15) + #s(dist_to_road) + 
#                      s(X, Y, k=100),
#                  data = risk_data_df, family = "betar")
gam.check(gam_mod_w)
#gam.check(gam_mod_w_2)

# Generate predictor raster stack
X_raster <- Y_raster <- pop
X_raster[] <- coordinates(pop)[,1]
Y_raster[] <- coordinates(pop)[,2]
pred_stack <- stack(X_raster,
                    Y_raster,
                    NTL,
                    dist_to_road)
names(pred_stack) <- c('X', 'Y', 'NTL', 'dist_to_road')

pred_raster_hw <- predict(pred_stack, gam_mod_hw, type="response")
pred_raster_w <- predict(pred_stack, gam_mod_w, type="response")
plot(pred_raster_hw)
plot(pred_raster_w)

# Create pop-weighted ward means
wards$total_pop <- extract(pop, wards, sum, na.rm=T)
weighted_sum_hw <- extract(pred_raster_hw*pop, wards, sum, na.rm=T)
weighted_sum_w <- extract(pred_raster_w*pop, wards, sum, na.rm=T)
wards$mean_hw_risk_pop_weighted <- weighted_sum_hw / wards$total_pop 
wards$mean_w_risk_pop_weighted <- weighted_sum_w / wards$total_pop 

# Plot ward level
quick_map(wards, 'mean_hw_risk_pop_weighted')
quick_map(wards, 'mean_w_risk_pop_weighted')






