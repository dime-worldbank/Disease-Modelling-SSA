
setwd("~/Documents/Work/Lnl/covid_modeling_ZWE/")
zwe_adm2_old <- st_read('geo2_zw2012/geo2_zw2012.shp')
svet_dist <- st_read("~/Dropbox/Data to be shared/admin zone boundaries/districts/ZWE_adm2.shp")

zwe_adm2_old_sp <- as(zwe_adm2_old, "Spatial")
svet_dist_sp <- as(svet_dist, "Spatial")

plot(zwe_adm2_old_sp)
lines(svet_dist_sp, col="red")

zwe_adm2_old_centroids <- st_centroid(zwe_adm2_old)
contains <- st_within(zwe_adm2_old_centroids, svet_dist)

zwe_adm2_old_sp$NEW_DIST_ID_2 <- svet_dist$ID_2[unlist(contains)]
write.csv(zwe_adm2_old_sp@data, "district_relation.csv")

library(leaflet)
leaflet() %>% addTiles() %>% addPolygons(data = zwe_adm2_old, group = "old") %>%
  addPolygons(data = zwe_adm2_old_sp[zwe_adm2_old_sp$NEW_DIST_ID_2==41,], group = "old_grouped", col="red") %>% 
  addPolygons(data = svet_dist, group = "new", col="green") %>% 
  addLayersControl(overlayGroups = c("old","old_grouped", "new"))

svet_dist[1,]
