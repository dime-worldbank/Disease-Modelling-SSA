import os

import numpy as np
import random
import pandas as pd
import sys
from datetime import datetime

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

        def model(self, population_1, population_2, distance):
            model_to_return = \
                (self.kappa * (population_1 ** self.alpha) * (population_2 ** self.beta)) / (distance ** self.gamma)

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
    for run in past_runs_directory:
        if run.startswith('sim_ann'):
            past_runs.append(run)
    if len(past_runs) > 0:
        past_runs.sort()
        last_run = pd.read_csv(output_dir + past_runs[-1])
        last_run = last_run.loc[last_run['Rsqr'] == last_run['Rsqr'].max()]
        best = [float(last_run.alpha.mean()), float(last_run.beta.mean()), float(last_run.gamma.mean()),
                float(last_run.kappa.mean())]
    else:
        best = [random.random(), random.random(), random.random(), random.random()]

    distance_data = pd.read_csv(datadir + "port_distances_num_id.csv")
    distance_data = distance_data.drop('Unnamed: 0', axis=1)
    population_data = pd.read_csv(datadir + "population_counts.csv")
    population_data = population_data.drop('Unnamed: 0', axis=1)
    gold_standard = pd.read_csv(datadir + 'Portugal_formatted_OD_matrix.csv')
    gold_standard = gold_standard.drop('Unnamed: 0', axis=1)
    iterations = 200
    generations = []
    initial_temperature = 200
    # generate an initial parameter set
    stepper = np.linspace(0, 0.5, 500)
    params = ['alpha', 'beta', 'gamma', 'kappa']
    results_df = pd.DataFrame(columns=['iteration_number', 'Rsqr', 'alpha', 'beta', 'gamma', 'kappa', 'parameter_changing'])
    generations.append(GravityModel(best[0], best[1], best[2], best[3]))
    df_idx = 0
    if len(past_runs) > 0:
        results_df.loc[df_idx] = [df_idx, float(last_run.Rsqr.mean()), float(last_run.alpha.mean()),
                                  float(last_run.beta.mean()), float(last_run.gamma.mean()),
                                  float(last_run.kappa.mean()), str(list(last_run.parameter_changing)[-1])]
        df_idx += 1

    for iter in range(iterations):
        val = random.choice(['alpha', 'beta'])
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

        best_SSres = []
        best_SStot = []
        alt_SSres = []
        alt_SStot = []
        for origin in distance_data.index:
            for destination in distance_data.columns:
                if origin == int(destination):
                    pass
                else:
                    origin_population = float(population_data.loc[population_data['ID'] == origin, 'count'])
                    destination_population = float(
                        population_data.loc[population_data['ID'] == int(destination), 'count']
                    )

                    distance = float(distance_data.loc[origin, destination])

                    best_model_prediction = generations[- 1].model(origin_population, destination_population,
                                                                   distance)
                    alternative_model_prediction = alternative_model.model(origin_population,
                                                                           destination_population,
                                                                           distance)
                    mobile_data = gold_standard.loc[origin, destination]

                    best_SSres.append((mobile_data - best_model_prediction) ** 2)
                    best_SStot.append((gold_standard.loc[origin, destination] -
                                       gold_standard.loc[origin].mean().mean()) ** 2)
                    alt_SSres.append((mobile_data - alternative_model_prediction) ** 2)
                    alt_SStot.append((gold_standard.loc[origin, destination] -
                                      gold_standard.loc[origin].mean().mean()) ** 2)
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
        print(df_idx)
        df_idx += 1
    best_found_this_time = results_df.loc[results_df['Rsqr'] == results_df['Rsqr'].max()]
    results_df.to_csv(output_dir + 'sim_ann_gravity_parameter_fitting_at_' + str(started_at) + '_.csv')
