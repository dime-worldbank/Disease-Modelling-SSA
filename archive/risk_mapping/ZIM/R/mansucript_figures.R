library(ggplot2)
library(sf)
library(leaflet)
library(viridis)
library(gridExtra)
library(dplyr)

## Maps of handwashing risk
load("~/Documents/Work/Lnl/covid_modeling_ZWE/data/risk_layers_data.RData")
st_crs(risk_data_sf) <- st_crs(zwe_adm2)
hw_clusters <- ggplot() + geom_sf(data = zwe_adm2,
                                  col = "gray50",
                                  lwd = 0.2) + 
  
                          geom_sf(data = risk_data_sf,
                                  col = "gray80",
                                  cex = 3,
                                  pch = 21,
                                  lwd = 0.2,
                                  aes(fill = hw_risk_index)) 

hw_clusters <- hw_clusters + scale_fill_viridis_c(option = "inferno")

# plot with same palette
range_hw <- range(risk_data_sf$hw_risk_index)
rescale_hw <- function(x, min, max){
  (x-min)/(max-min)
}

# # Example Data
# x = sample(-100:100, 50)
# 
# #Normalized Data
# normalized = (x-min(x))/(max(x)-min(x))

hw_districts <- ggplot() + geom_sf(data = zwe_adm2,
                                   col = "white",
                                   lwd = 0.2,
                                   aes(fill = mean_hw_risk_pop_weighted))
hw_districts <- hw_districts + scale_fill_viridis_c(option = "inferno",
                                                    begin = rescale_hw(0.249, range_hw[1], range_hw[2]),
                                                    end = rescale_hw(0.69, range_hw[1], range_hw[2]))
  
grid.arrange(hw_clusters,
             hw_districts,
             nrow=2)

## Maps of respiratory illness
load("~/Documents/Work/Lnl/covid_modeling_ZWE/data/resp_illness_layers_data.RData")
df_trim_sf <- st_as_sf(df_trim, coords = c('lng', 'lat'))
df_trim_sf$prevalence_respiratory_illness <- df_trim_sf$n_positive / df_trim_sf$n_trials
quick_map(df_trim_sf, 'prevalence_respiratory_illness')

col_pal <- colorNumeric(wesanderson::wes_palette("Zissou1", 64, type='continuous')[1:64],
                        c(0, 0.61))

resp_data_to_plot <- data.frame(GID_2 = rep(zwe_adm2$GID_2, 3),
                           resp_risk = c(zwe_adm2$mean_resp_risk_pop_weighted,
                                    zwe_adm2$mean_resp_risk_improved_1_pop_weighted,
                                    zwe_adm2$mean_resp_risk_improved_3_pop_weighted),
                           scenario = sort(rep(c("A - Current", "B - Improved scenario 1", "C - Improved scenario 2"),
                                               nrow(zwe_adm2))))

resp_zwe_adm2_plot <- left_join(zwe_adm2, resp_data_to_plot)

p0_resp <- ggplot() + geom_sf(data = resp_zwe_adm2_plot,
                         col = "white",
                         lwd = 0.2,
                         aes(fill = resp_risk))

p1 <- p0_resp + scale_fill_viridis_c(option = "cividis")

p1 + facet_wrap(~scenario, ncol = 3) +
  theme(legend.position = "bottom",
        strip.background = element_blank()) +
  labs(fill = "Risk of respiratory illness",
       title = "")  

## Severe risk maps for ZWE
risk_severe <- read.csv("~/Documents/Work/Lnl/covid_modeling_ZWE/outputs/severe_disease_risk_district_v2.csv")
zwe_adm2 <- readRDS('/Users/sturrockh/Documents/Work/Lnl/covid_modeling_ZWE/gadm36_ZWE_2_sp.rds')
zwe_adm2 <- st_as_sf(zwe_adm2)
zwe_adm2 <- merge(zwe_adm2, risk_severe, by="GID_2")

# Plot
data_to_plot <- data.frame(GID_2 = rep(zwe_adm2$GID_2, 3),
                             risk = c(zwe_adm2$severe_covid_risk_with_age,
                                    zwe_adm2$severe_covid_risk_improved_1,
                                    zwe_adm2$severe_covid_risk_improved_3),
                           scenario = sort(rep(c("A - Current", "B - Improved scenario 1", "C - Improved scenario 2"),
                                                 nrow(zwe_adm2))))

zwe_adm2_plot <- left_join(zwe_adm2, data_to_plot)

p0 <- ggplot() + geom_sf(data = zwe_adm2_plot,
                         col = "white",
                         lwd = 0.2,
                         aes(fill = risk))

p1 <- p0 + scale_fill_viridis_c(option = "cividis")

p1 + facet_wrap(~scenario, ncol = 3) +
  theme(legend.position = "bottom",
        strip.text = element_text(size=15),
        legend.key.width = unit(0.6, 'in'),
        strip.background = element_blank()) +
  labs(fill = "Risk of severe COVID-19",
       title = "")  

