#' Function to predict prevalence excedance probabilities at spatial locations. Currently only support binomial data.
#' @name prevalence_predictor_mgcv_st_grid
#' @param point_data Required. An data.frame of points containing at least `n_trials`, `n_positive`, 
#' `lng` and `lat` fields
#' @param time_field Optional name of column referring to time. Time should be coded as an integer not date.
#' @param covariates Optional URL to rasterLayer or rasterStack of covariates to include. Is resampled to grid as 
#' defined by `ncol_pred_grid`
#' @param observed_covariates Optional string of fieldnames of covariates associated with each observation to include 
#' in the model. 
#' @param fix_prediction_covariates Required if `observed_covariates` is defined. The covariate value(s) to use for prediction.
#' Should be a named list. For example if `observed_covariates` is `c('temperature', 'elevation')` this should be 
#' `list(temperature = 20, elevation = 500)` 
#' @param boundary Optional URL to GeoJSON defining the boundary of the prediction area. If not provided, 
#' assumes slightly buffered convex-hull around `points`.
#' @param ncol_pred_grid The number of columns of the prediction grid. Number of rows is proportional to 
#' this and relative to the bounding box of `point_data`
#' @param m check out m in ?gam. Defulats to 3. 
#' @param debug Optional boolean defining whether to return diagnostics. Defaults to FALSE.
#' @import geojsonio httr sf sp mgcv 
#' @export
library(mgcv)
library(sf)
library(sp)
library(raster)

get_prediction_grid <- dget('R/get_prediction_grid_df.R')
gam_posterior_metrics <- dget('R/gam_posterior_metrics.R')

prevalence_predictor_mgcv_st_grid <- function(point_data, 
                                      time_field=NULL,
                                      prediction_periods='last',
                                      covariates = NULL,
                                      observed_covariates = NULL,
                                      fix_prediction_covariates = NULL,
                                      boundary = NULL,
                                      seed = 1981,
                                      ncol_pred_grid = 300,
                                      m=3,
                                      debug = FALSE) {
    

      set.seed(seed)

      # Choose k
      mod_data <- as.data.frame(point_data)
      mod_data$n_neg <- mod_data$n_trials - mod_data$n_positive
      train_data <- mod_data[!is.na(mod_data$n_trials),]
      
      # Deal with covariates if present
      # Define prediction grid
      if(!is.null(covariates)){
        covariates <- stack(covariates)
      }

      raster_covariates_to_fix <- fix_prediction_covariates[names(fix_prediction_covariates) %in% names(covariates)]
      obs_covariates_to_fix <- fix_prediction_covariates[!(names(fix_prediction_covariates)  %in% names(covariates))]
      if(length(obs_covariates_to_fix)==0){
        obs_covariates_to_fix <- NULL
      }
      pred_stack <- get_prediction_grid(point_data, 
                                        covariates,
                                        boundary,
                                        obs_covariates_to_fix,
                                        ncol=ncol_pred_grid)

      if(!is.null(covariates)){
        covariates_resamp <- pred_stack[[names(covariates)]]
        covar_extract <- as.data.frame(extract(covariates_resamp, train_data[,c('lng', 'lat')]))
        names(covar_extract) <- names(covariates_resamp)
        train_data <- cbind(train_data, covar_extract)
      }

      k <- floor(nrow(train_data)*0.8)
      if(k > 500){
        k <- 500
      }

      if(is.null(time_field)){
        
        if(!is.null(covariates) | !is.null(observed_covariates)){

          model_covariates <- c(names(covariates), observed_covariates)
          form <- as.formula(paste("cbind(n_positive, n_neg) ~
                               s(lng, lat, bs='gp', 
                                  k=k, m=m) +",
                                   paste('s(', model_covariates, ')',collapse = "+")))

          # Determine number of bins for discretization of covariates
          #discrete_bins <- c(c(50,10,10,10,10))
          gam_mod <- gam(form,
                         data = train_data,
                         #method="fREML",
                         #nthreads = 8,
                         select = T,
                         #discrete = 500,
                         family="binomial")

        }else{

          gam_mod <- gam(cbind(n_positive, n_neg) ~
                           s(lng, lat, bs="gp", k=k, m=m),
                         data = train_data,
                         #method="fREML",
                         nthreads = 8,
                         discrete = TRUE,
                         family="binomial")
        }

          }else{

            train_data$t <- as.numeric(as.character(unlist(train_data[[time_field]])))

            time_knots <- length(unique(train_data$t))
              if(time_knots > 6){
                time_knots <- 6
            }
            space_knots <- floor(nrow(train_data) / time_knots)
            if(space_knots > 250){
              space_knots <- 250
            }
      
            if(!is.null(covariates) | !is.null(observed_covariates)){
              
              model_covariates <- c(names(covariates), observed_covariates)
              form <- as.formula(paste("cbind(n_positive, n_neg) ~
                               te(lng, lat, t, bs=c('ds','cr'), d=c(2,1), 
                                  k=c(space_knots, time_knots), m=m) +",
                                       paste('s(', model_covariates, ')',collapse = "+")))
            }else{
              form <- as.formula(paste("cbind(n_positive, n_neg) ~
                               te(lng, lat, t, bs=c('ds','cr'), d=c(2,1), 
                                  k=c(space_knots, time_knots), m=m)"))
            }

            gam_mod <- bam(form,
                           data = train_data,
                           discrete = TRUE,
                           nthreads = 8,
                           select = TRUE,
                           #method="REML",
                           family="binomial")

      }

    ## HERE CAN SET THE TIME FOR PREDICTIONS IF MULTI-TEMPORAL
    if(!is.null(raster_covariates_to_fix)){
      for(cov in 1:length(raster_covariates_to_fix)){
        pred_stack[[names(raster_covariates_to_fix)[cov]]][] <- raster_covariates_to_fix[[cov]]
      }
    }  
    
    if(!is.null(time_field)){
      if(prediction_periods == 'last'){
        timeslice_raster <- pred_stack[[1]]
        timeslice_raster[] <- max(train_data$t)
        names(timeslice_raster) <- 't'
        pred_stack <- stack(pred_stack, timeslice_raster)
        pred_prev_raster <- predict(pred_stack, gam_mod, type = "response")
      }
      
      if(prediction_periods == 'each'){
        pred_prev_raster <- stack()
        timeslice_raster <- pred_stack[[1]]
        
        for(timeslice in min(train_data$t):max(train_data$t)){
          timeslice_raster[] <- timeslice
          names(timeslice_raster) <- 't'
          pred_stack_time <- stack(pred_stack, timeslice_raster)
          pred_prev_raster <- stack(pred_prev_raster,
                                    predict(pred_stack_time, gam_mod, type = "response"))
          
        }
      }
    }else{
      pred_prev_raster <- predict(pred_stack, gam_mod, type = "response")
    }


    # Get posterior metrics
    # posterior_metrics <- gam_posterior_metrics(gam_mod,
    #                                            pred_data$interp_grid,
    #                                            500,
    #                                            exceedance_threshold)



    
    # If batch_size is specitfied, then perform adaptive sampling
    # if(!is.null(batch_size)){
    #   
    #   if(uncertainty_fieldname == 'exceedance_probability'){
    #     uncertainty_fieldname = 'exceedance_uncertainty'
    #   }
    #   
    #   ## COULD KEEP THIS AS AN OPTION IF VERY LARGE NUMBER OF POINTS
    #   new_batch_idx <- choose_batch_simple(point_data = posterior_metrics,
    #                       batch_size = batch_size,
    #                       uncertainty_fieldname = uncertainty_fieldname,
    #                       candidate = rep(TRUE, nrow(posterior_metrics)))
    #   
    #   adaptive_sampled_coords <- pred_data$interp_grid[new_batch_idx, ]
    #   
    #   return(list(pred_prev_raster = pred_prev_raster,
    #               adaptive_sampled_coords = adaptive_sampled_coords))
    # }

    # Set CRS
    crs(pred_prev_raster)="+proj=longlat +datum=WGS84"
    
    # If debug is T then return diagnostics
    if(debug){
      complete <- train_data[complete.cases(train_data),]
      diagnostics <- list(obs_fitted = data.frame(fitted = predict(gam_mod, complete, type = "response"),
                                                  obs = complete$n_positive / 
                                                    (complete$n_positive + complete$n_neg)),
                          k.check = mgcv:::k.check(gam_mod),
                          model = gam_mod,
                          AIC = AIC(gam_mod))
      return(list(pred_prev_raster = pred_prev_raster,
                  diagnostics = diagnostics))
    }
    return(pred_prev_raster)
}
