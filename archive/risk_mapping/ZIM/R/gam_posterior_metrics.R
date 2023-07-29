#' Function to return metrics from posterior of (binomial) GAM model
#' @param gam_mod gam model object
#' @param new_data data.frame of new data 
#' @param n_sims Number of simulations
#' @param exceedance_threshold Prevalence threshold to use for exceedance probabilities
#' @import mgcv
#' @export

# useful functions for fn-prevalence-predictor-spaMM
function(gam_mod, 
         new_data, 
         n_sims,
         exceedance_threshold){
  
  Cg <- predict(gam_mod, new_data, type = "lpmatrix")
  sims <- rmvn(n_sims, mu = coef(gam_mod), V = vcov(gam_mod, unconditional = TRUE))
  fits <- Cg %*% t(sims)
  sims <- exp(fits) / (1 + exp(fits))

  get_bci_width <- function(realization){
    quantiles <- quantile(realization, prob = c(0.025, 0.975))
    return(as.vector(diff(quantiles)))
  }

  logit_prediction <- predict(gam_mod, new_data) 
  prevalence_prediction <- exp(logit_prediction) / (1 + exp(logit_prediction))
  prevalence_bci_width <- apply(sims, 1, get_bci_width)

  # If 'excedance probability' exists, then calc additional stats
  if(!is.null(exceedance_threshold)){
    exceedance_probability <- apply(sims, 1, function(x){sum(x >= exceedance_threshold)/
                                                              n_sims})
    
    # Calc 'exceedance_uncertainty' as normalized around 0.5
    exceedance_uncertainty <- 0.5 - abs(exceedance_probability - 0.5)
    entropy <- -exceedance_probability * base::log(exceedance_probability, base=2) -
                (1-exceedance_probability) * base::log (1-exceedance_probability, base=2)
    entropy[is.na(entropy)] <- 0
    
    # return
    return(data.frame(prevalence_prediction = prevalence_prediction,
                      prevalence_bci_width = prevalence_bci_width,
                      exceedance_probability = exceedance_probability,
                      exceedance_uncertainty = exceedance_uncertainty,
                      entropy = entropy))
  }else{
    return(data.frame(prevalence_prediction = prevalence_prediction,
                      prevalence_bci_width = prevalence_bci_width))
  }
  
}
