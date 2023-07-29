pop_1 <- data.frame(smoker = sample(0:1, 100, prob = c(0.2, 0.8), replace=T),
                    under_50 = sample(0:1, 100, prob = c(0.3, 0.7), replace=T))
pop_2 <- data.frame(smoker = sample(0:1, 100, prob = c(0.8, 0.2), replace=T),
                    under_50 = sample(0:1, 100, prob = c(0.5, 0.5), replace=T))

pop_1$log_risk <- pop_1$smoker*2.5 + pop_1$under_50*-2
pop_2$log_risk <- pop_2$smoker*2.5 + pop_2$under_50*-2
pop_1$risk <- exp(pop_1$log_risk) / (1+exp(pop_1$log_risk))
pop_2$risk <- exp(pop_2$log_risk) / (1+exp(pop_2$log_risk))
mean(pop_1$risk )
mean(pop_2$risk )

# Now calc based on aggregate figures
pop_1_agg_log_risk <- mean(pop_1$smoker)*2.5 + mean(pop_1$under_50)*-2
exp(pop_1_agg_log_risk) / (1+exp(pop_1_agg_log_risk))
pop_2_agg_log_risk <- mean(pop_2$smoker)*2.5 + mean(pop_2$under_50)*-2
exp(pop_2_agg_log_risk) / (1+exp(pop_2_agg_log_risk))
