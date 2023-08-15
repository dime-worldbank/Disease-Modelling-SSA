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
    class UniversalOpportunityModel:



        def __init__(self, alpha, beta):
            self.alpha = alpha
            self.beta = beta
            self.SSE = 0

        def model(self, mi, mj, Sij):
            model_to_return = \
                ((mi + Sij * self.alpha) * mj) / ((mi + Sij *(self.alpha + self.beta)) * (mi + Sij * (
                        self.alpha + self.beta) + mj))

            return model_to_return

        def parameters(self):
            return self.alpha, self.beta

        def set_see(self, resulting_SSE):
            self.SSE = resulting_SSE

        def report_see(self):
            return self.SSE

    population_shps = pd.read_csv(datadir + "population_cut_out.csv")

    started_at = datetime.now().strftime("%d-%m-%Y_%H:%M:%S")
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
        best = [float(last_run.alpha.mean()), float(last_run.beta.mean())]
    else:
        best = [0, 1]

    gold_standard = pd.read_csv(datadir + 'formatted_od_matrix.csv')
    iterations = 500
    generations = []
    initial_temperature = 200
    # generate an initial parameter set
    stepper = np.linspace(0, 0.5, 500)
    params = ['alpha', 'beta']
    results_df = pd.DataFrame(columns=['iteration_number', 'Rsqr', 'alpha', 'beta', 'parameter_changing'])
    generations.append(UniversalOpportunityModel(best[0], best[1]))
    df_idx = 0
    if len(past_runs) > 0:
        results_df.loc[df_idx] = [df_idx, float(last_run.Rsqr.mean()), float(last_run.alpha.mean()),
                                  float(last_run.beta.mean()), str(list(last_run.parameter_changing)[-1])]
        df_idx += 1

    for iter in range(iterations):
        val = random.choice(['alpha', 'beta'])
        current_temperature = initial_temperature / (iter + 1)
        if val == 'alpha':
            alternative_model = UniversalOpportunityModel(best[0] + random.choice([1, -1]) * random.choice(stepper),
                                                          best[1])
        elif val == 'beta':
            alternative_model = UniversalOpportunityModel(best[0],
                                                          best[1] + random.choice([1, -1]) * random.choice(stepper))

        best_SSres = []
        best_SStot = []
        alt_SSres = []
        alt_SStot = []
        for row in population_shps.iterrows():
            origin = row[1][1]
            destination = row[1][2]

            if origin == destination:
                pass
            else:

                origin_population = row[1][3]
                destination_population = row[1][4]
                sij = row[1][5]
                best_model_prediction = generations[- 1].model(origin_population, destination_population,
                                                               sij)
                alternative_model_prediction = alternative_model.model(origin_population,
                                                                       destination_population,
                                                                       sij)
                mobile_data = gold_standard.loc[gold_standard['home_region'] == origin, destination].mean()
                best_SSres.append((mobile_data - best_model_prediction) ** 2)
                best_SStot.append((gold_standard.loc[gold_standard['home_region'] == origin, destination].mean() -
                                   gold_standard.loc[gold_standard['home_region'] == origin,
                                                     gold_standard.columns[3:]].mean().mean()) ** 2)
                alt_SSres.append((mobile_data - alternative_model_prediction) ** 2)
                alt_SStot.append(
                    (gold_standard.loc[gold_standard['home_region'] == origin, destination].mean() -
                     gold_standard.loc[gold_standard['home_region'] == origin,
                                       gold_standard.columns[3:]].mean().mean()) ** 2)
        best_rsq = 1 - sum(best_SSres) / sum(best_SStot)
        alt_rsq = 1 - sum(alt_SSres) / sum(alt_SStot)
        diff = alt_rsq - best_rsq
        next_gen_rsq = best_rsq
        next_gen_alpha = generations[- 1].alpha
        next_gen_beta = generations[- 1].beta
        criterion = np.exp(- diff / current_temperature)
        eligible_for_change = (criterion < leniency_param_larger_than_one)
        will_change = random.choice([True, False])
        swap_for_slightly_worse = (eligible_for_change and will_change)
        has_improved = (diff > 0)
        #
        if has_improved or swap_for_slightly_worse:
            next_gen_alpha = alternative_model.alpha
            next_gen_beta = alternative_model.beta
            next_gen_rsq = alt_rsq
            print("criterion:" + str(np.round(criterion, 5)) + "\n")
            print("old:" + str(best_rsq) + "\n")
            print("new:" + str(alt_rsq) + "\n")
            print("iter:" + str(iter) + "\n")
            generations.append(alternative_model)
        results_df.loc[df_idx] = [df_idx, next_gen_rsq, next_gen_alpha, next_gen_beta, val]
        df_idx += 1
    best_found_this_time = results_df.loc[results_df['Rsqr'] == results_df['Rsqr'].max()]
    results_df.to_csv(output_dir + 'sim_ann_univ_op_parameter_fitting_at_' + str(started_at) + '_.csv')
