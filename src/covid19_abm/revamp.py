import base_model
import params
import scenario_models
import dir_manager
import pickle
import sys
import os


from datetime import datetime, timedelta
timestep=timedelta(hours=4)

#### Set directory
#os.chdir("/Users/swise/workspace/worldbank/Disease-Modelling-SSA/src/covid19_abm")
os.chdir("/Users/sophieayling/Documents/GitHub/Disease-Modelling-SSA/src/covid19_abm")
cwd = os.getcwd()

stay_duration_file = dir_manager.get_data_dir('preprocessed', 'mobility', 'weekday_mobility_duration_count_df-new-district i5.csv')#weekday_mobility_duration_count_df-new-district.pickle')
transition_probability_file = dir_manager.get_data_dir('preprocessed', 'mobility', 'daily_region_transition_probability-new-district-pre-lockdown_i5.csv')#daily_region_transition_probability-new-district-pre-lockdown.csv')
reduced_transition_probability_file = dir_manager.get_data_dir('preprocessed', 'mobility', 'daily_region_transition_probability-new-district-post-lockdown_i5.csv')

# note from Sophie that I'm not sure how any of these are working because they are not in the data folder? Maybe get_data_dir is working its magic? I didn't get any error so perhaps


sample_size = 10
R0 = 1.3

# start to run into difficulties with the below however (Sophie)

params = params.ParamsConfig(
    district='new', data_sample_size=sample_size, R0=R0,
    normal_interaction_matrix_file=('../../configs/interaction_matrix_nld.txt') # sophie replaced with text files
    lockdown_interaction_matrix_file=('../../configs/interaction_matrix_ld.txt') # sophie replaced with text files
    #normal_interaction_matrix_file=('../../data/raw/interaction_matrix_update 130121.xlsx'), # sophie commented out
    #lockdown_interaction_matrix_file=('../../Disease-Modelling-SSA/data/raw/interaction_matrix_update 130121.xlsx'), # sophie commented out

#    normal_interaction_matrix_file=('/Users/swise/workspace/worldbank/Disease-Modelling-SSA/data/raw/final_close_interaction_matrix.xlsx'),
#    lockdown_interaction_matrix_file=('/Users/swise/workspace/worldbank/Disease-Modelling-SSA/data/raw/final_close_interaction_matrix.xlsx'),
    stay_duration_file=dir_manager.get_data_dir('preprocessed', 'mobility', stay_duration_file),
    transition_probability_file=dir_manager.get_data_dir('preprocessed', 'mobility', transition_probability_file),
 #   intra_district_decreased_mobility_rates_file=dir_manager.get_data_dir('preprocessed', 'mobility', transition_probability_file),
    timestep=timestep)

params.set_new_district_seed(seed_infected=2)

#model = scenario_models.Phase1GovernmentOpenSchoolsScenario(params)
model = scenario_models.DynamicPhase1GovernmentOpenSchoolsScenario(params)

params.data_file_name = '../../data/preprocessed/census/zimbabwe_expanded_census_consolidated_100pct.pickle'
model.load_agents(params.data_file_name, size=None, infect_num=params.SEED_INFECT_NUM)
end_date = datetime(2021, 6, 1)

# just to make sure!
for i in range(100):
	model.step()

print(model.scheduler.real_time)

model_dump_file = dir_manager.get_data_dir('logs', f'model_dump_file_blah.pickle')
with open(model_dump_file, 'wb') as fl:
	pickle.dump(model, fl)
