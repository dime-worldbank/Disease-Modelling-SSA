# get_prediction_grid_df

function(points_to_chull_from, 
         covariates = NULL, 
         boundary = NULL,
         fix_prediction_covariates = NULL, 
         ncol=200){

  if(is.null(boundary)){
  
      # Convert to sf object
      points_to_chull_from_sf <- st_as_sf(points_to_chull_from,
                                          coords = c("lng", "lat"))
    
      # Decide on buffer. Arbitrarily 5% of widest point
      chull <- st_convex_hull(st_union(points_to_chull_from_sf))
      x_y_size <- c(diff(st_bbox(chull)[c(3,1)]),
                    diff(st_bbox(chull)[c(4,2)]))
      buff_dist <- max(sqrt(x_y_size^2)) * 0.05
      
      # Crop according to convex hull of points
      conv_hull_poly <- st_buffer(chull,
                                dist=buff_dist)
    
  }else{
    conv_hull_poly <- st_read(boundary, quiet = TRUE)
    x_y_size <- c(diff(st_bbox(conv_hull_poly)[c(3,1)]),
                  diff(st_bbox(conv_hull_poly)[c(4,2)]))
  }
  
  # Generate raster
  pred_raster <- raster(extent(as(conv_hull_poly, "Spatial")), 
                        ncol = ncol, nrow = ncol * (x_y_size[2] / x_y_size[1]))
  
  # Generate stack of covariates
  lng_raster <- lat_raster <- pred_raster
  lng_raster[] <- coordinates(pred_raster)[,1]
  lat_raster[] <- coordinates(pred_raster)[,2]
  
  if(is.null(covariates)){
    pred_stack <- stack(lng_raster, lat_raster)
  }else{
    #covs_to_resample <- covariates[[which(!(names(covariates) %in% names(fix_prediction_covariates)))]]
    covariates_resamp <- resample(covariates, pred_raster)
    pred_stack <- stack(lng_raster, lat_raster, covariates_resamp)
  }
  
  # Add any fixed covariates
  if(!is.null(fix_prediction_covariates)){
    for(j in 1:length(fix_prediction_covariates)){
      cov_raster <- pred_raster
      cov_raster[] <- fix_prediction_covariates[[j]]
      names(cov_raster) <- names(fix_prediction_covariates[j])
      pred_stack <- stack(pred_stack, cov_raster)
    }
  }
  
  names(pred_stack)[1:2] <- c('lng', 'lat')
  
  pred_stack <- raster::mask(pred_stack, as(conv_hull_poly, "Spatial"))
  
  # ID which points we are making predictions at
  # pred_pixels_idx <- which(!is.na(prev_raster[]))
  # 
  # interp_grid <- data.frame(lng = coordinates(prev_raster)[pred_pixels_idx,1], 
  #                           lat = coordinates(prev_raster)[pred_pixels_idx,2])

  return(pred_stack)
}