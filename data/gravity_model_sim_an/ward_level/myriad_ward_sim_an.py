import pandas as pd
from datetime import datetime
import os
import random
import numpy as np
import sys

if __name__ == "__main__":

    output_dir = sys.argv[1]  # "/home/rmjlra2/Model/Disease-Modelling-SSA/data/gravity_model_sim_an/"
    datadir = sys.argv[2]
    if output_dir[-1] != '/':
        print("Error: that ain't a directory, chief. Try again.")
        sys.exit()

    class GravityModel:

        def __init__(self, alpha, beta, gamma, kappa):
            self.alpha = alpha
            self.beta = beta
            self.gamma = gamma
            self.kappa = kappa
            self.SSE = 0

        def model(self, population_1, population_2, dist):
            model_to_return = \
                (self.kappa * (population_1 ** self.alpha) * (population_2 ** self.beta)) / (dist ** self.gamma)

            return model_to_return

        def parameters(self):
            return self.alpha, self.beta, self.gamma, self.kappa

        def set_see(self, resulting_SSE):
            self.SSE = resulting_SSE

        def report_see(self):
            return self.SSE


    started_at = datetime.now().strftime("%d-%m-%Y-%H-%M-%S")
    past_runs_directory = os.listdir(output_dir)
    leniency_param_larger_than_one = 1.1
    past_runs = []

    # add all previously found runs into the mix
    for run in past_runs_directory:
        if run.startswith('sim_ann'):
            past_runs.append(run)

    # read in background data
    ward_pop_data = pd.read_csv(datadir + "ward_pops.csv")
    ward_pop_data = ward_pop_data.loc[:, ~ward_pop_data.columns.str.contains('^Unnamed')]
    # load in and format the data
    gold_standard = pd.read_csv(datadir + "ward_OD_to_calibrate_to_w_assumption.csv")
    gold_standard = gold_standard.loc[:, ~gold_standard.columns.str.contains('^Unnamed')]
    distance_data = pd.read_csv(datadir + "zim_ward_distances.csv")
    distance_data = distance_data.loc[:, ~distance_data.columns.str.contains('^Unnamed')]
    distance_data = distance_data.set_index('origin')
    # create lists of the wards and their populations
    origins = list(ward_pop_data.ward)
    destinations = list(ward_pop_data.ward)
    populations = list(ward_pop_data.population)
    # Check that all wards feature in all files
    cdr_wards = list(gold_standard.home_region.unique())
    pop_data_wards = list(np.unique(origins))
    distance_data_wards = list(distance_data.index)
    # set up the study
    iterations = 500
    generations = []
    initial_temperature = 200

    # generate an initial parameter set
    stepper = np.linspace(0, 0.5, 500)
    params = ['alpha', 'beta', 'gamma', 'kappa']
    results_df = pd.DataFrame(columns=['iteration_number', 'Rsqr', 'alpha', 'beta', 'gamma', 'kappa',
                                       'parameter_changing'])

    # if some already exist, identify the best param combo
    df_idx = 0
    if len(past_runs) > 0:
        past_runs.sort()
        last_run = pd.read_csv(output_dir + past_runs[-1])
        last_run = last_run.loc[last_run['Rsqr'] == last_run['Rsqr'].max()]
        best = [float(last_run.alpha.mean()), float(last_run.beta.mean()), float(last_run.gamma.mean()),
                float(last_run.kappa.mean())]
        results_df.loc[df_idx] = [df_idx, float(last_run.Rsqr.mean()), float(last_run.alpha.mean()),
                                  float(last_run.beta.mean()), float(last_run.gamma.mean()),
                                  float(last_run.kappa.mean()), str(list(last_run.parameter_changing)[-1])]
        df_idx += 1
        # otherwise, we're just starting - pick something at random
    else:
        best = [random.random() * 0.1, random.random() * 0.1, random.random() * 0.1, random.random()]

    generations.append(GravityModel(best[0], best[1], best[2], best[3]))

    # parameter fitting!
    for iter in range(iterations):
        val = random.choice(['alpha', 'beta', 'gamma', 'kappa'])
        current_temperature = initial_temperature / (iter + 1)
        if val == 'alpha':
            alternative_model = GravityModel(best[0] + random.choice([1, -1]) * random.choice(stepper),
                                             best[1],
                                             best[2],
                                             best[3])
        elif val == 'beta':
            alternative_model = GravityModel(best[0],
                                             best[1] + random.choice([1, -1]) * random.choice(stepper),
                                             best[2],
                                             best[3])
        elif val == 'gamma':
            alternative_model = GravityModel(best[0],
                                             best[1],
                                             best[2] + random.choice([1, -1]) * random.choice(stepper),
                                             best[3])
        else:
            alternative_model = GravityModel(best[0],
                                             best[1],
                                             best[2],
                                             best[3] + random.choice([1, -1]) * random.choice(stepper))

        # set up holders for residuals
        best_SSres = []
        best_SStot = []
        alt_SSres = []
        alt_SStot = []

        # calculate the residuals
        for or_idx, or_ward in enumerate(origins):
            for de_idx, de_ward in enumerate(destinations):
                if or_ward == de_ward:
                    pass
                else:
                    origin_population = float(ward_pop_data.loc[ward_pop_data['ward'] == or_ward, 'population'])
                    destination_population = float(ward_pop_data.loc[ward_pop_data['ward'] == de_ward, 'population'])
                    distance = float(distance_data.loc[or_ward, de_ward])


                    best_model_prediction = generations[- 1].model(origin_population, destination_population,
                                                                   distance)
                    alternative_model_prediction = alternative_model.model(origin_population,
                                                                           destination_population,
                                                                           distance)
                    mobile_data = gold_standard.loc[gold_standard['home_region'] == or_ward, de_ward].mean()
                    best_SSres.append((mobile_data - best_model_prediction) ** 2)
                    best_SStot.append(
                        (gold_standard.loc[gold_standard['home_region'] == or_ward, de_ward].mean() -
                         gold_standard.loc[gold_standard['home_region'] == or_ward,
                                           gold_standard.columns[3:]].mean().mean()) ** 2)
                    alt_SSres.append((mobile_data - alternative_model_prediction) ** 2)
                    alt_SStot.append(
                        (gold_standard.loc[gold_standard['home_region'] == or_ward, de_ward].mean() -
                         gold_standard.loc[gold_standard['home_region'] == or_ward,
                                           gold_standard.columns[3:]].mean().mean()) ** 2)
    #
        # choose next generation's parents
        best_rsq = 1 - sum(best_SSres) / sum(best_SStot)
        alt_rsq = 1 - sum(alt_SSres) / sum(alt_SStot)
        diff = alt_rsq - best_rsq
        next_gen_rsq = best_rsq
        next_gen_alpha = generations[- 1].alpha
        next_gen_beta = generations[- 1].beta
        next_gen_gamma = generations[- 1].gamma
        next_gen_kappa = generations[- 1].kappa
        criterion = np.exp(- diff / current_temperature)
        eligible_for_change = (criterion < leniency_param_larger_than_one)
        will_change = random.choice([True, False])
        swap_for_slightly_worse = (eligible_for_change and will_change)
        has_improved = (diff > 0)

        # create next generation
        if has_improved or swap_for_slightly_worse:
            next_gen_alpha = alternative_model.alpha
            next_gen_beta = alternative_model.beta
            next_gen_gamma = alternative_model.gamma
            next_gen_kappa = alternative_model.kappa
            next_gen_rsq = alt_rsq
            print("criterion:" + str(np.round(criterion, 5)) + "\n")
            print("old:" + str(best_rsq) + "\n")
            print("new:" + str(alt_rsq) + "\n")
            print("iter:" + str(iter) + "\n")
            generations.append(alternative_model)
        results_df.loc[df_idx] = [df_idx, next_gen_rsq, next_gen_alpha, next_gen_beta, next_gen_gamma,
                                  next_gen_kappa, val]
        df_idx += 1
    best_found_this_time = results_df.loc[results_df['Rsqr'] == results_df['Rsqr'].max()]

    # write out results to file
    results_df.to_csv(output_dir + 'sim_ann_parameter_fitting_at_' + str(started_at) + '_.csv')
