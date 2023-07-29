Handwashing and water risk Zimbabwe
================

Map raw handwashing and water risk
    indeces

![](risk_layers_files/figure-gfm/unnamed-chunk-1-1.png)<!-- -->![](risk_layers_files/figure-gfm/unnamed-chunk-1-2.png)<!-- -->

<!-- Two predictors, mean night light intensity in 2015 and distance to road (OSM) were considered. These were resampled to the same 1km resolution. -->

<!-- ```{r, echo=FALSE} -->

<!-- par(mfrow = c(1, 3)) -->

<!-- plot(pop, main = "Population") -->

<!-- plot(NTL, main = "Night time light intensity") -->

<!-- plot(dist_to_road, main = "Distance to nearest road") -->

<!-- ``` -->

<!-- To model the 2 indeces, we fit Beta Generalized Additive Models which allow for non-linear covariate effects. Additionally, we included a bivariate smooth on latitude and longitude to account for spatial effects. -->

<!-- ```{r, echo=F} -->

<!-- # Extract covars -->

<!-- risk_data_sf$NTL <- extract(NTL, risk_data_sf) -->

<!-- risk_data_sf$dist_to_road <- extract(dist_to_road, risk_data_sf) -->

<!-- risk_data_df <- as.data.frame(risk_data_sf) -->

<!-- risk_data_df <- cbind(risk_data_df, st_coordinates(risk_data_sf)) -->

<!-- # Standardize weights -->

<!-- risk_data_df$weights <- risk_data_df$total_hhs / mean(risk_data_df$total_hhs) -->

<!-- # Fit beta model -->

<!-- gam_mod_hw <- gam(hw_risk_index ~ s(NTL) + s(dist_to_road) + s(X, Y, k=300), -->

<!--                data = risk_data_df, family = "betar", select = T, weights = weights) -->

<!-- gam_mod_w <- gam(w_risk_index ~ s(NTL, k=50) + s(dist_to_road) + s(X, Y, k=300), -->

<!--                data = risk_data_df, family = "betar", select = T, weights = weights) -->

<!-- # gam_mod_w_2 <- gam(risk_data_sf$w_risk_index ~ te(NTL, dist_to_road, k=15) + #s(dist_to_road) + -->

<!-- #                      s(X, Y, k=100), -->

<!-- #                  data = risk_data_df, family = "betar") -->

<!-- #gam.check(gam_mod_w_2) -->

<!-- ``` -->

<!-- Take a look at the covariate effects -->

<!-- ```{r, echo=FALSE} -->

<!-- draw(gam_mod_hw) -->

<!-- draw(gam_mod_w) -->

<!-- ``` -->

<!-- Take a look at the fitted (predicted) versus observed values -->

<!-- ```{r, echo=F} -->

<!-- gratia::observed_fitted_plot(gam_mod_hw, title = "Handwashing risk") -->

<!-- gratia::observed_fitted_plot(gam_mod_w, title = "Water risk") -->

<!-- ``` -->

<!-- Using the model, predict the 2 indeces at 1km resolution across the country. -->

<!-- ```{r, echo=F} -->

<!-- # Generate predictor raster stack -->

<!-- X_raster <- Y_raster <- pop -->

<!-- X_raster[] <- coordinates(pop)[,1] -->

<!-- Y_raster[] <- coordinates(pop)[,2] -->

<!-- pred_stack <- stack(X_raster, -->

<!--                     Y_raster, -->

<!--                     NTL, -->

<!--                     dist_to_road) -->

<!-- names(pred_stack) <- c('X', 'Y', 'NTL', 'dist_to_road') -->

<!-- pred_raster_hw <- predict(pred_stack, gam_mod_hw, type="response") -->

<!-- pred_raster_w <- predict(pred_stack, gam_mod_w, type="response") -->

<!-- plot(pred_raster_hw, col =  viridis(64), main = "Hand washing risk index") -->

<!-- plot(pred_raster_w, col = magma(64)[1:55], main = "Water risk index") -->

<!-- # quick_map(pred_raster_hw, col = viridis(64), raster_legend_title = "Handwashing risk index") -->

<!-- # quick_map(pred_raster_w, col = magma(64)[1:55], raster_legend_title = "Water risk index") -->

<!-- ``` -->

<!-- To obtain risk by ward, calculate the population-weighted mean. -->

<!-- ```{r, echo=F} -->

<!-- # Create pop-weighted ward means -->

<!-- wards$total_pop <- extract(pop, wards, sum, na.rm=T) -->

<!-- weighted_sum_hw <- extract(pred_raster_hw*pop, wards, sum, na.rm=T) -->

<!-- weighted_sum_w <- extract(pred_raster_w*pop, wards, sum, na.rm=T) -->

<!-- wards$mean_hw_risk_pop_weighted <- weighted_sum_hw / wards$total_pop -->

<!-- wards$mean_w_risk_pop_weighted <- weighted_sum_w / wards$total_pop -->

<!-- write.csv(st_drop_geometry(wards[,c('wardpcode0', -->

<!--                                  'mean_hw_risk_pop_weighted', -->

<!--                                  'mean_w_risk_pop_weighted')]),  -->

<!--           "ward_hw_w_risk.csv", row.names = F) -->

<!-- # Plot ward level -->

<!-- hw_pal <- colorNumeric( viridis(64), wards$mean_hw_risk_pop_weighted) -->

<!-- w_pal <- colorNumeric( magma(64)[1:55], wards$mean_w_risk_pop_weighted) -->

<!-- basemap <- leaflet() %>% addProviderTiles("CartoDB.Positron") -->

<!-- basemap %>% addPolygons(data = wards, -->

<!--                         col = "black", -->

<!--                         fillColor = hw_pal(wards$mean_hw_risk_pop_weighted), weight = 1, -->

<!--                         fillOpacity = 0.9, opacity = 0.5) %>% -->

<!--   addLegend(pal = hw_pal, values = wards$mean_hw_risk_pop_weighted, title = "Handwashing risk") -->

<!-- ``` -->

<!-- ```{r, echo=F} -->

<!-- basemap %>% addPolygons(data = wards, -->

<!--                         col = "black", -->

<!--                         fillColor = w_pal(wards$mean_w_risk_pop_weighted), weight = 1, -->

<!--                         fillOpacity = 0.9, opacity = 0.5)%>% -->

<!--   addLegend(pal = w_pal, values = wards$mean_w_risk_pop_weighted, title = "Water risk") -->

<!-- ``` -->
