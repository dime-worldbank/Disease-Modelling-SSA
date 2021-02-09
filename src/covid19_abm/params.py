from datetime import datetime, timedelta
import numpy as np
import pandas as pd
import os
import pickle

from covid19_abm.dir_manager import get_data_dir


         
class ParamsConfig:

    ################## EPIDEMIOLOGICAL PARAMS ########################
    
    per_capita_contact_rates_home  = 4.12 
    
    ages = list(range(100))

    # Make sure to adjust the day hours such that a step matches the hours.
    step_timedelta = timedelta(hours=4)
    start_datetime = datetime(2020, 4, 20)

    # Start and end of weekday
    WEEKDAY_START_DAY_HOUR = 8
    WEEKDAY_END_DAY_HOUR = 16

    # Start and end of weekend
    OTHER_DAY_START_DAY_HOUR = 8
    OTHER_DAY_END_DAY_HOUR = 16

    INTERACTION_SIZE_MAP = {}  # age stratified expected number of interactions per time step
    INTERACTION_SIZE_MAX = 10  # https://journals.plos.org/plosone/article?id=10.1371/journal.pone.0170459
    INTERACTION_SIZE_MEAN = 10  # https://journals.plos.org/plosone/article?id=10.1371/journal.pone.0170459

    R0 = 3.0

    # Defines the mean period of being contagious for both symptomatic and asymptomatic
    ASYMPTOMATIC_CONTAGIOUS_PERIOD_MEAN = 6.5  # 5 std
    ASYMPTOMATIC_CONTAGIOUS_PERIOD_SHAPE = 8.45
    ASYMPTOMATIC_CONTAGIOUS_PERIOD_SCALE = 0.7692307692307693

    SYMPTOMATIC_CONTAGIOUS_PERIOD_MEAN = 7  # 5 std
    SYMPTOMATIC_CONTAGIOUS_PERIOD_SHAPE = 9.8
    SYMPTOMATIC_CONTAGIOUS_PERIOD_SCALE = 0.7142857142857143

    # Parameter corresponding to the time of exposure to the onset of being contagious for asymptomatic individuals
    ASYMPTOMATIC_TO_CONTAGIOUS_PERIOD_MEAN = 4.6  # Imperial College 16-03-2020 paper
    ASYMPTOMATIC_TO_CONTAGIOUS_PERIOD = timedelta(days=ASYMPTOMATIC_TO_CONTAGIOUS_PERIOD_MEAN)

    # Average recovery period for mild cases
    SYMPTOM_TO_RECOVERY_PERIOD = timedelta(days=SYMPTOMATIC_CONTAGIOUS_PERIOD_MEAN)

    # Average recovery period for asymptomatic cases
    ASYMPTOMATIC_TO_RECOVERY_PERIOD = timedelta(days=ASYMPTOMATIC_CONTAGIOUS_PERIOD_MEAN)

    # Period from onset of symptom to start of hospitalization
    # https://www.imperial.ac.uk/media/imperial-college/medicine/sph/ide/gida-fellowships/Imperial-College-COVID19-Global-Impact-26-03-2020v2.pdf
    SYMPTOM_TO_HOSPITALIZATION_PERIOD = timedelta(days=5)

    # Period of hospitalization
    # https://www.imperial.ac.uk/media/imperial-college/medicine/sph/ide/gida-fellowships/Imperial-College-COVID19-Global-Impact-26-03-2020v2.pdf
    HOSPITALIZATION_PERIOD = timedelta(days=8)

    # Additional period for a patient needing critical care to stay at the hospital
    # https://www.imperial.ac.uk/media/imperial-college/medicine/sph/ide/gida-fellowships/Imperial-College-COVID19-Global-Impact-26-03-2020v2.pdf
    CRITICAL_PERIOD = timedelta(days=8)  # 8 additional days on top of `HOSPITALIZATION_PERIOD`

    # Period from onset of symptoms to death. This is based on the assumption by Ferguson (03-26-2020) paper.
    # https://www.imperial.ac.uk/media/imperial-college/medicine/sph/ide/gida-fellowships/Imperial-College-COVID19-Global-Impact-26-03-2020v2.pdf
    SYMPTOMATIC_TO_DEATH_PERIOD = timedelta(days=21)

    SYMPTOMATIC_RATE = 0.6  # rate of people that have the virus and will manifest symptoms

    # https://mrc-ide.github.io/global-lmic-reports/parameters.html
    CRITICAL_FATALITY_RATE = 0.5

    # https://www.imperial.ac.uk/media/imperial-college/medicine/sph/ide/gida-fellowships/Imperial-College-COVID19-Global-Impact-26-03-2020v2.pdf
    ### Parameters derived from mean and std for gamma
    # INCUBATION_PERIOD_MEAN = 4.58
    # INCUBATION_PERIOD_STD = 3.24
    INCUBATION_PERIOD_SHAPE = 6.474197530864197
    INCUBATION_PERIOD_SCALE = 0.7074235807860262

    # Probability that a person with mild symptom will still go out of the household.
    MILD_SYMPTOM_MOVEMENT_PROBABILITY = 1.0

    # Map of district to hospital ids
    DISTRICT_HOSPITALS = {}
    
################## MOBILITY PARAMS ########################

    DISTRICT_MOVEMENT_ALLOWED_AGE = 18

    def __init__(
        self, district='old', data_sample_size=5, R0=None,
        normal_interaction_matrix_file='../../configs/interaction_matrix_nld.txt', # updated by Sophie
        lockdown_interaction_matrix_file='../../configs/interaction_matrix_ld.txt',  # updated by Sophie
        stay_duration_file='../../preprocessed/mobility/New Files/weekday_mobility_duration_count_df-new-district i5.pickle', # i5 to start
        transition_probability_file='../../preprocessed/mobility/New Files/daily_region_transition_probability-new-district-post-lockdown_i5.csv', # i5 to start
        intra_district_decreased_mobility_rates_file='../../preprocessed/mobility/intra_district_decreased_mobility_rates.csv',
        timestep=None,
    ):
        if R0 is not None:
            self.R0 = R0

        if timestep is not None:
            self.step_timedelta = timestep


        # read in from files


        ################## MORE EPIDEMIOLOGICAL PARAMS (move up once pushed) ########################

        os.chdir("/Users/sophieayling/Documents/GitHub/Disease-Modelling-SSA/src/covid19_abm")
        #os.chdir("/Users/swise/workspace/worldbank/Disease-Modelling-SSA/src/covid19_abm")
        #cwd = os.getcwd()
        #print(cwd)

        # Functions for reading in params as text files 

        self.susceptibility_by_age = pd.Series(self.readInParams("../../configs/susceptibility_by_age.txt"))

        self.hospitalization_rates_by_age = pd.Series(self.readInParams("../../configs/hospitalization_rates_by_age.txt"))

        self.critical_rates_by_age = pd.Series(self.readInParams("../../configs/critical_rates_by_age.txt"))

        self.hospitalized_death_rates_by_age = pd.Series(self.readInParams("../../configs/hospitalized_death_rates_by_age.txt"))
            
        self.per_capita_contact_rates_wk = pd.Series(self.readInParams("../../configs/per_capita_contact_rates_work.txt"))

        self.ECONOMIC_STATUS_WEEKDAY_MOVEMENT_PROBABILITY = self.readInParams2("../../configs/ECONOMIC_STATUS_WEEKDAY_MOVEMENT_PROBABILITY.txt")

        self.ECONOMIC_STATUS_OTHER_DAY_MOVEMENT_PROBABILITY = self.readInParams2("../../configs/ECONOMIC_STATUS_OTHER_DAY_MOVEMENT_PROBABILITY.txt")

        # STOP READ IN


        self.data_sample_size = data_sample_size
        self.district_type = district  # 'new' or 'old'
        self.SCENARIO = 'UNMITIGATED'

        # Note that in the https://mrc-ide.github.io/global-lmic-reports/parameters.html, they combined mild symptomatic and asymptomatic.
        # We need to perform this correction since we explicitly model asymptomatic separately from mild symptomatic cases.
        self.hospitalization_rates_by_age = self.hospitalization_rates_by_age / self.SYMPTOMATIC_RATE
        self.MEAN_HOSPITALIZATION_RATE = self.hospitalization_rates_by_age.mean()

        self.AGE_HOSPITALIZATION_PROBABILITY = {age: self.hospitalization_rates_by_age.iloc[(self.hospitalization_rates_by_age.index >= age).argmax()] for age in self.ages}
        self.AGE_CRITICAL_CARE_PROBABILITY = {age: self.critical_rates_by_age.iloc[(self.critical_rates_by_age.index >= age).argmax()] for age in self.ages}
        self.AGE_HOSPITALIZATION_FATALITY_PROBABILITY = {age: self.hospitalized_death_rates_by_age.iloc[(self.hospitalized_death_rates_by_age.index >= age).argmax()] for age in self.ages}

        self.AGE_SUSCEPTIBILITY_PROBABILITY = {age: self.susceptibility_by_age.iloc[(self.susceptibility_by_age.index >= age).argmax()] for age in self.ages}

        # Derived value of infection rate per time step for symptomatic and asymptomatic during the contagious period
        # Formula from: https://journals.plos.org/plosone/article?id=10.1371/journal.pone.0208775 (Equation 1)
        self.ASYMPTOMATIC_INFECTION_RATE = self.R0 / ((timedelta(days=self.ASYMPTOMATIC_CONTAGIOUS_PERIOD_MEAN) / self.step_timedelta) * self.INTERACTION_SIZE_MEAN)
        self.SYMPTOMATIC_INFECTION_RATE = self.R0 / ((timedelta(days=self.SYMPTOMATIC_CONTAGIOUS_PERIOD_MEAN) / self.step_timedelta) * self.INTERACTION_SIZE_MEAN)

        # Computing beta with contact matrix https://github.com/mrc-ide/squire/blob/master/R/beta.R
        # https://cran.r-project.org/web/packages/socialmixr/vignettes/introduction.html
        self.AGE_ASYMPTOMATIC_INFECTION_RATE = pd.Series({
            age: self.R0 / ((timedelta(days=self.ASYMPTOMATIC_CONTAGIOUS_PERIOD_MEAN) / self.step_timedelta) * self.per_capita_contact_rates_wk.iloc[(self.per_capita_contact_rates_wk.index >= age).argmax()]) for age in self.ages})
        self.AGE_ASYMPTOMATIC_INFECTION_RATE_VALUES = self.AGE_ASYMPTOMATIC_INFECTION_RATE[sorted(self.ages)].values

        self.AGE_SYMPTOMATIC_INFECTION_RATE = pd.Series({
            age: self.R0 / ((timedelta(days=self.SYMPTOMATIC_CONTAGIOUS_PERIOD_MEAN) / self.step_timedelta) * self.per_capita_contact_rates_wk.iloc[(self.per_capita_contact_rates_wk.index >= age).argmax()]) for age in self.ages})
        self.AGE_SYMPTOMATIC_INFECTION_RATE_VALUES = self.AGE_SYMPTOMATIC_INFECTION_RATE[sorted(self.ages)].values

        self.DISTRICT_MOVING_ECONOMIC_STATUS = set([i for i, j in self.ECONOMIC_STATUS_WEEKDAY_MOVEMENT_PROBABILITY.items() if j > 0])
        self.DISTRICT_MOVING_ECONOMIC_STATUS.remove('In School')
        self.DISTRICT_MOVING_ECONOMIC_STATUS.remove('Teachers')

        self.DAILY_DISTRICT_TRANSITION_PROBABILITY = pd.read_csv(transition_probability_file, index_col=[0, 1])
        self.DAILY_DISTRICT_TRANSITION_PROBABILITY = self.DAILY_DISTRICT_TRANSITION_PROBABILITY.loc[sorted(self.DAILY_DISTRICT_TRANSITION_PROBABILITY.index)]

        self.DISTRICT_IDS = sorted(self.DAILY_DISTRICT_TRANSITION_PROBABILITY.columns)
        self.DISTRICT_ID_TO_NAME = dict(enumerate(self.DISTRICT_IDS))
        self.DISTRICT_NAME_TO_ID = {j: i for i, j in self.DISTRICT_ID_TO_NAME.items()}

        self.DAILY_DISTRICT_TRANSITION_PROBABILITY = self.DAILY_DISTRICT_TRANSITION_PROBABILITY[self.DISTRICT_IDS]

        for wid_i in self.DISTRICT_IDS:
            self.DISTRICT_HOSPITALS[wid_i] = [f'c_{wid_i}_{i}' for i in list(np.random.randint(0, 1000, size=10))]

        # Values in hours
        self.DISTRICT_WEEKDAY_OD_STAY_COUNT_MATRIX = pd.read_csv(stay_duration_file) #

        myregions = sorted(self.DISTRICT_WEEKDAY_OD_STAY_COUNT_MATRIX['home_region'].unique())
        full_idx = [(dow, 'd_' + str(src), 'd_' + str(dst)) for dow in range(7) for src in myregions for dst in myregions]

        self.DISTRICT_WEEKDAY_OD_STAY_COUNT_MATRIX["idx"] = [(x, 'd_' + str(y), 'd_' + str(z)) for (x, y, z) in zip(self.DISTRICT_WEEKDAY_OD_STAY_COUNT_MATRIX["weekday"], self.DISTRICT_WEEKDAY_OD_STAY_COUNT_MATRIX["home_region"], self.DISTRICT_WEEKDAY_OD_STAY_COUNT_MATRIX["region"])]
        self.DISTRICT_WEEKDAY_OD_STAY_COUNT_MATRIX = self.DISTRICT_WEEKDAY_OD_STAY_COUNT_MATRIX.set_index("idx")
        self.DISTRICT_WEEKDAY_OD_STAY_COUNT_MATRIX = self.DISTRICT_WEEKDAY_OD_STAY_COUNT_MATRIX.reindex(full_idx)

        self.DISTRICT_WEEKDAY_OD_STAY_COUNT_MATRIX['avg_duration'] = self.DISTRICT_WEEKDAY_OD_STAY_COUNT_MATRIX['avg_duration'].fillna(24)
        self.DISTRICT_WEEKDAY_OD_STAY_COUNT_MATRIX['stddev_duration'] = self.DISTRICT_WEEKDAY_OD_STAY_COUNT_MATRIX['stddev_duration'].fillna(self.DISTRICT_WEEKDAY_OD_STAY_COUNT_MATRIX['stddev_duration'].mean())
        self.DISTRICT_WEEKDAY_OD_STAY_COUNT_MATRIX[['avg_duration', 'stddev_duration']] = self.DISTRICT_WEEKDAY_OD_STAY_COUNT_MATRIX[['avg_duration', 'stddev_duration']] + 0.001

        if self.district_type == 'old':
            self.set_old_district_seed(seed_infected=3)
        elif self.district_type == 'new':
            self.set_new_district_seed(seed_infected=3)

        self.normal_interaction_matrix_file = normal_interaction_matrix_file
        self.lockdown_interaction_matrix_file = lockdown_interaction_matrix_file
        self.set_interaction_parameters(self.normal_interaction_matrix_file)

        self.blocked = False
        self.lockdown = False

        self.intra_district_decreased_mobility_rates_file = intra_district_decreased_mobility_rates_file


    def readInParams(self, filename):
        rawfile = pd.read_csv(filename, skipinitialspace=True)
        rawfile.dropna(axis=1, inplace=True)
        rawDict = { k: v for (k,v) in zip(rawfile["age"], rawfile["rate"])} 
        maxVal = float(rawDict.pop("older"))
        myDict = {int(k): float(v) for (k, v) in rawDict.items()}
        myDict[np.inf] = maxVal
        return myDict

    # define another function that will read in the other format of text files and turn them to objects
    def readInParams2(self, filename):
        rawfile = pd.read_csv(filename, skipinitialspace=True)
        rawfile.dropna(axis=1, inplace=True)
        myDict = { k:float(v) for (k,v) in zip(rawfile["economic_status"], rawfile["movement_probability"])} 
        return myDict


    def set_interaction_parameters(self, interaction_matrix_file):
        # This matrix defines the probability of an interaction between two economic status.
        # This will be multiplied by the population density per district to quantify the mixing intensity per district.
        # ECONOMIC_STATUS_INTERACTION_MATRIX = {'employed': {'unemployed'}}
        
        self.ECONOMIC_STATUS_INTERACTION_MATRIX = pd.read_csv("../../configs/interaction_matrix_nld.txt", index_col=0)
        self.ECONOMIC_STATUS_INTERACTION_SIZE_MAP = pd.read_csv("../../configs/no_interactions_wk_econ.txt", index_col=0)

        # self.DISTRICT_POP_DENSITY = pd.read_csv(os.path.join(data_dir, 'district_pop_dens_friction.csv'))

        self.ECONOMIC_STATUS_INTERACTION_MATRIX_CUMSUM = self.ECONOMIC_STATUS_INTERACTION_MATRIX.cumsum(axis=1)
        self.ECON_STAT_ID_TO_NAME = dict(enumerate(self.ECONOMIC_STATUS_INTERACTION_MATRIX_CUMSUM.columns))
        self.ECON_STAT_NAME_TO_ID = {j: i for i, j in self.ECON_STAT_ID_TO_NAME.items()}
        self.ECONOMIC_STATUS_INTERACTION_MATRIX_CUMSUM_VALUES = self.ECONOMIC_STATUS_INTERACTION_MATRIX_CUMSUM.values
        self.ECONOMIC_STATUS_INTERACTION_MATRIX_VALUES = self.ECONOMIC_STATUS_INTERACTION_MATRIX[
            self.ECONOMIC_STATUS_INTERACTION_MATRIX_CUMSUM.columns].values

    def set_intra_district_decreased_mobility_rates(self, intra_district_decreased_mobility_rates_file):

        print(intra_district_decreased_mobility_rates_file)
        self.LOCKDOWN_DECREASED_MOBILITY_RATE = pd.read_csv(
            get_data_dir('preprocessed', 'mobility', intra_district_decreased_mobility_rates_file),
            index_col=0
        )['pctdif_distance']

        self.LOCKDOWN_DECREASED_MOBILITY_RATE = {
            self.DISTRICT_NAME_TO_ID[i]: j for i, j in self.LOCKDOWN_DECREASED_MOBILITY_RATE.items()
        }

    def set_lockdown_parameters(self, lockdown_mode='lockdown_empirical'):
        self.set_intra_district_decreased_mobility_rates(
            self.intra_district_decreased_mobility_rates_file)

        if lockdown_mode == 'lockdown_empirical':
            # 33% decrease in inter-district mobility (empirical)
            self.LOCKDOWN_ALLOWED_PROBABILITY = {w: 0.67 for w in self.DISTRICT_ID_TO_NAME}
            # self.LOCKDOWN_DECREASED_MOBILITY_RATE = {w: 0.59 for w in self.DISTRICT_ID_TO_NAME}
            # SA: this is currently not being used. Aivin says we could here replace the input parameter with a file with the reduced mobility levels. Am concerned that we have w everywhere here, should be d? This should then be updated --> scenario_models, THEN to base_model for scenario specific changes  
        elif lockdown_mode == 'lockdown_assumed':
            self.LOCKDOWN_ALLOWED_PROBABILITY = {w: 0.05 for w in self.DISTRICT_ID_TO_NAME}
            # 41% decrease in short-range mobility
            # self.LOCKDOWN_DECREASED_MOBILITY_RATE = {w: 0.59 for w in self.DISTRICT_ID_TO_NAME}
        elif lockdown_mode == 'lockdown_eased':
            self.LOCKDOWN_ALLOWED_PROBABILITY = {w: 0.86 for w in self.DISTRICT_ID_TO_NAME}
            # self.LOCKDOWN_DECREASED_MOBILITY_RATE = {w: 0.838 for w in self.DISTRICT_ID_TO_NAME}
        else:
            raise ValueError(f'lockdown_mode `{lockdown_mode}` not valid!')

        self.set_interaction_parameters(self.lockdown_interaction_matrix_file)

        self.lockdown = True

    def set_blocked_parameters(self, block_mode='block_empirical'):
        if block_mode == 'block_empirical':
            self.BLOCK_ALLOWED_PROBABILITY = 0.67
        elif block_mode == 'block_assumed':
            self.BLOCK_ALLOWED_PROBABILITY = 0.05
        elif block_mode == 'block_eased':
            self.BLOCK_ALLOWED_PROBABILITY = 0.86
        else:
            raise ValueError(f'block_mode `{block_mode}` not valid!')

        self.blocked = True

    def set_old_district_seed(self, seed_infected):
        # NOTE: This should be updated when the admin level is changed.
        # model.params.DISTRICT_NAME_TO_ID['d_21'] -> 18 Bulawayo
        # model.params.DISTRICT_NAME_TO_ID['d_921'] -> 85 Harare
        # model.params.DISTRICT_NAME_TO_ID['d_302'] -> 21 Goromonzi
        self.SEED_INFECT_DISTRICT_IDS = np.array([18, 85, 21])
        self.SEED_INFECT_AGE_MIN = 20
        self.SEED_INFECT_NUM = seed_infected  # 3 -> 66 for a 5% sample
        self.SIMULATION_START_DATE = datetime(2020, 6, 28, 8)

    def set_new_district_seed(self, seed_infected):
        # NOTE: This should be updated when the admin level is changed.
        # model.params.DISTRICT_NAME_TO_ID['d_1'] -> 0 Bulawayo
        # model.params.DISTRICT_NAME_TO_ID['d_2'] -> 11 Harare
        # model.params.DISTRICT_NAME_TO_ID['d_18'] -> 9 Goromonzi

        with open(get_data_dir('preprocessed', 'line_list', 'd_line_list.pickle'), 'rb') as fl:
            infected_count = pickle.load(fl)
            self.DISTRICT_ID_INFECTED_COUNT = {self.DISTRICT_NAME_TO_ID.get(i): j for i, j in infected_count.items()}
            self.DISTRICT_ID_INFECTED_PROB = pd.Series(self.DISTRICT_ID_INFECTED_COUNT)
            self.DISTRICT_ID_INFECTED_PROB = (self.DISTRICT_ID_INFECTED_PROB / self.DISTRICT_ID_INFECTED_PROB.sum()).to_dict()

        self.SEED_INFECT_DISTRICT_IDS =  np.array([i for i in self.DISTRICT_ID_INFECTED_COUNT])
        self.SEED_INFECT_AGE_MIN = 20
        self.SEED_INFECT_AGE_MAX = 60
        self.SEED_INFECT_NUM = seed_infected  # 3 -> 66 for a 5% sample
        self.SIMULATION_START_DATE = datetime(2020, 9, 1, 8)

        self.data_file_name = get_data_dir('preprocessed', 'census', f'zimbabwe_expanded_census_consolidated_{self.data_sample_size}pct.pickle')

    def get_effective_R0(self, hw):
        self.HW_MIN_TO_MEAN_INCREASE = 0.05
        self.HW_MIN_TO_MEAN_DELTA = self.MEAN_HW_RISK - self.MIN_HW_RISK

        return 1 + (self.HW_MIN_TO_MEAN_INCREASE * (hw - self.MEAN_HW_RISK) / self.HW_MIN_TO_MEAN_DELTA)
    
    

        ################## SCENARIO FUNCTIONS SET UP ########################

    def scenario_test_interaction_matrix_sensitivity(self):
        self.SCENARIO = 'INTERACTION_MATRIX_SENSITIVITY'
        self.set_interaction_parameters(
            get_data_dir('raw', 'age-econ_matrix', 'sensitivity_interaction_matrix.xlsx'))

    def scenario_handwashing_risk(self):
        self.SCENARIO = 'HANDWASHING_RISK'
        self.risk_data = pd.read_csv(
            get_data_dir('preprocessed', 'risk', 'hw_and_severe_disease_risk.csv'))
        self.DISTRICT_HW_RISK = self.risk_data[['ID_2', 'mean_hw_risk_pop_weighted']].copy()
        self.MEAN_HW_RISK = self.DISTRICT_HW_RISK['mean_hw_risk_pop_weighted'].mean()
        self.MIN_HW_RISK = self.DISTRICT_HW_RISK['mean_hw_risk_pop_weighted'].min()

        self.DISTRICT_HW_RISK['ID_2'] = self.DISTRICT_HW_RISK['ID_2'].map(lambda x: f'd_{x}')
        self.DISTRICT_HW_RISK['effective_hw_risk'] = self.DISTRICT_HW_RISK['mean_hw_risk_pop_weighted'].map(self.get_effective_R0)
        self.DISTRICT_HW_RISK = self.DISTRICT_HW_RISK.set_index('ID_2').to_dict()['effective_hw_risk']

        self.DISTRICT_SEVERE_DISEASE_RISK = self.risk_data[['ID_2', 'severe_covid_risk']].copy()
        self.DISTRICT_SEVERE_DISEASE_RISK['severe_covid_risk'] = (
            self.DISTRICT_SEVERE_DISEASE_RISK['severe_covid_risk'] / self.MEAN_HOSPITALIZATION_RATE
        )
        self.DISTRICT_SEVERE_DISEASE_RISK = self.DISTRICT_SEVERE_DISEASE_RISK.set_index('ID_2').to_dict()['severe_covid_risk']

    def scenario_improved_handwashing_risk_1(self):
        self.SCENARIO = 'HANDWASHING_RISK_1'
        self.risk_data = pd.read_csv(
            get_data_dir('preprocessed', 'risk', 'hw_and_severe_disease_risk.csv'))
        self.DISTRICT_HW_RISK = self.risk_data[['ID_2', 'mean_hw_risk_pop_weighted']].copy()
        self.MEAN_HW_RISK = self.DISTRICT_HW_RISK['mean_hw_risk_pop_weighted'].mean()
        self.MIN_HW_RISK = self.DISTRICT_HW_RISK['mean_hw_risk_pop_weighted'].min()

        self.DISTRICT_HW_RISK['ID_2'] = self.DISTRICT_HW_RISK['ID_2'].map(lambda x: f'd_{x}')
        self.DISTRICT_HW_RISK['mean_hw_risk_pop_weighted'] = self.DISTRICT_HW_RISK['mean_hw_risk_pop_weighted'].min()
        self.DISTRICT_HW_RISK['effective_hw_risk'] = self.DISTRICT_HW_RISK['mean_hw_risk_pop_weighted'].map(self.get_effective_R0)
        self.DISTRICT_HW_RISK = self.DISTRICT_HW_RISK.set_index('ID_2').to_dict()['effective_hw_risk']

        self.DISTRICT_SEVERE_DISEASE_RISK = self.risk_data[['ID_2', 'severe_covid_risk_improved_1']].copy()
        self.DISTRICT_SEVERE_DISEASE_RISK['severe_covid_risk_improved_1'] = (
            self.DISTRICT_SEVERE_DISEASE_RISK['severe_covid_risk_improved_1'] / self.MEAN_HOSPITALIZATION_RATE
        )
        self.DISTRICT_SEVERE_DISEASE_RISK = self.DISTRICT_SEVERE_DISEASE_RISK.set_index('ID_2').to_dict()['severe_covid_risk_improved_1']

    def scenario_improved_handwashing_risk_2(self):
        self.SCENARIO = 'HANDWASHING_RISK_2'
        self.risk_data = pd.read_csv(
            get_data_dir('preprocessed', 'risk', 'hw_and_severe_disease_risk.csv'))
        self.DISTRICT_HW_RISK = self.risk_data[['ID_2', 'mean_hw_risk_pop_weighted']].copy()
        self.MEAN_HW_RISK = self.DISTRICT_HW_RISK['mean_hw_risk_pop_weighted'].mean()
        self.MIN_HW_RISK = self.DISTRICT_HW_RISK['mean_hw_risk_pop_weighted'].min()

        self.DISTRICT_HW_RISK['ID_2'] = self.DISTRICT_HW_RISK['ID_2'].map(lambda x: f'd_{x}')
        self.DISTRICT_HW_RISK['mean_hw_risk_pop_weighted'] = 0
        self.DISTRICT_HW_RISK['effective_hw_risk'] = self.DISTRICT_HW_RISK['mean_hw_risk_pop_weighted'].map(self.get_effective_R0)
        self.DISTRICT_HW_RISK = self.DISTRICT_HW_RISK.set_index('ID_2').to_dict()['effective_hw_risk']

        self.DISTRICT_SEVERE_DISEASE_RISK = self.risk_data[['ID_2', 'severe_covid_risk_improved_2']].copy()
        self.DISTRICT_SEVERE_DISEASE_RISK['severe_covid_risk_improved_2'] = (
            self.DISTRICT_SEVERE_DISEASE_RISK['severe_covid_risk_improved_2'] / self.MEAN_HOSPITALIZATION_RATE
        )
        self.DISTRICT_SEVERE_DISEASE_RISK = self.DISTRICT_SEVERE_DISEASE_RISK.set_index('ID_2').to_dict()['severe_covid_risk_improved_2']

    def scenario_block_greatest_inbound_movement(self):
        # Districts with the greatest number of inbound movements
        self.SCENARIO = 'BLOCK_GREATEST_INBOUND'
        self.BLOCK_DISTRICTS = ['d_901', 'd_921', 'd_922', 'd_302', 'd_406']
        self.BLOCK_DISTRICTS_IDS = list(map(self.DISTRICT_NAME_TO_ID.get, self.BLOCK_DISTRICTS))
        self.set_blocked_parameters()

    def scenario_block_greatest_outbound_movement(self):
        # Districts with the greatest number of outbound movements
        self.SCENARIO = 'BLOCK_GREATEST_OUTBOUND'
        self.BLOCK_DISTRICTS = ['d_901', 'd_922', 'd_304', 'd_21', 'd_302']
        self.BLOCK_DISTRICTS_IDS = list(map(self.DISTRICT_NAME_TO_ID.get, self.BLOCK_DISTRICTS))
        self.set_blocked_parameters()

    def scenario_block_greatest_movement(self):
        # Districts with the greatest number of outbound movements
        self.SCENARIO = 'BLOCK_GREATEST'
        self.BLOCK_DISTRICTS = ['d_901', 'd_921', 'd_922', 'd_302', 'd_406', 'd_304', 'd_21',]
        self.BLOCK_DISTRICTS_IDS = list(map(self.DISTRICT_NAME_TO_ID.get, self.BLOCK_DISTRICTS))
        self.set_blocked_parameters()

    def scenario_block_new_district_greatest_movement(self):
        # Districts with the greatest number of outbound movements
        self.SCENARIO = 'BLOCK_GREATEST_NEW_DIST'
        self.BLOCK_DISTRICTS = ['d_2', 'd_31', 'd_18', 'd_1', 'd_36', 'd_7', 'd_26', 'd_23', 'd_28', 'd_56']
        self.BLOCK_DISTRICTS_IDS = list(map(self.DISTRICT_NAME_TO_ID.get, self.BLOCK_DISTRICTS))
        self.set_blocked_parameters()

    def scenario_lockdown_greatest_inbound_movement(self):
        # Districts with the greatest number of inbound movements
        self.SCENARIO = 'LOCKDOWN_GREATEST_INBOUND'
        self.LOCKDOWN_DISTRICTS = ['d_901', 'd_921', 'd_922', 'd_302', 'd_406']
        self.LOCKDOWN_DISTRICTS_IDS = list(map(self.DISTRICT_NAME_TO_ID.get, self.LOCKDOWN_DISTRICTS))
        self.set_lockdown_parameters()

    def scenario_lockdown_greatest_outbound_movement(self):
        # Districts with the greatest number of outbound movements
        self.SCENARIO = 'LOCKDOWN_GREATEST_OUTBOUND'
        self.LOCKDOWN_DISTRICTS = ['d_901', 'd_922', 'd_304', 'd_21', 'd_302']
        self.LOCKDOWN_DISTRICTS_IDS = list(map(self.DISTRICT_NAME_TO_ID.get, self.LOCKDOWN_DISTRICTS))
        self.set_lockdown_parameters()

    def scenario_lockdown_greatest_movement(self):
        # Districts with the greatest number of outbound movements
        self.SCENARIO = 'LOCKDOWN_GREATEST'
        self.LOCKDOWN_DISTRICTS = ['d_901', 'd_921', 'd_922', 'd_302', 'd_406', 'd_304', 'd_21',]
        self.LOCKDOWN_DISTRICTS_IDS = list(map(self.DISTRICT_NAME_TO_ID.get, self.LOCKDOWN_DISTRICTS))
        self.set_lockdown_parameters()

    def scenario_lockdown_new_district_greatest_movement(self):
        # Districts with the greatest number of outbound movements
        self.SCENARIO = 'LOCKDOWN_GREATEST_NEW_DIST'
        self.LOCKDOWN_DISTRICTS = ['d_2', 'd_31', 'd_18', 'd_1', 'd_36', 'd_7', 'd_26', 'd_23', 'd_28', 'd_56']
        self.LOCKDOWN_DISTRICTS_IDS = list(map(self.DISTRICT_NAME_TO_ID.get, self.LOCKDOWN_DISTRICTS))
        self.set_lockdown_parameters()

    def scenario_block_greatest_reach_movement(self):
        # Districts that have movement to the greatest number of other districts (reach)
        self.SCENARIO = 'BLOCK_GREATEST_REACH'

    def scenario_isolate_symptomatic_population(self):
        # Isolating the symptomatic population
        self.SCENARIO = 'ISOLATE_SYMPTOMATIC'
        self.MILD_SYMPTOM_MOVEMENT_PROBABILITY = 0.05

    def scenario_isolate_symptomatic_population_50pct(self):
        # Isolating the symptomatic population
        self.SCENARIO = 'ISOLATE_SYMPTOMATIC_50pct'
        self.MILD_SYMPTOM_MOVEMENT_PROBABILITY = 0.5

    def scenario_isolate_vulnerable_groups(self):
        # Isolating vulnerable age groups
        self.SCENARIO = 'ISOLATE_VULNERABLE'
        self.VULNERABLE_AGE = 60
        self.VULNERABLE_LOCATION = -1 * (max(self.DISTRICT_ID_TO_NAME) + 1)

    def scenario_isolate_vulnerable_groups_in_house(self):
        # Isolating vulnerable age groups
        self.SCENARIO = 'ISOLATE_VULNERABLE_HOUSE'
        self.VULNERABLE_AGE = 60

    def scenario_mask_hygiene(self):
        # Isolating vulnerable age groups
        self.SCENARIO = 'MASK_HYGIENE'
        self.EFFECTIVE_TRANSMISSION = 0.85

    def scenario_continued_all_lockdown(self):
        self.SCENARIO = 'CONTINUED_ALL_LOCKDOWN'
        self.LOCKDOWN_DISTRICTS = list(self.DISTRICT_NAME_TO_ID.keys())
        self.LOCKDOWN_DISTRICTS_IDS = list(map(self.DISTRICT_NAME_TO_ID.get, self.LOCKDOWN_DISTRICTS))
        self.set_lockdown_parameters()

    def scenario_continued_all_lockdown_open_mining(self):
        self.scenario_continued_all_lockdown()
        self.SCENARIO = 'CONTINUED_ALL_LOCKDOWN_MINING'

    def scenario_continued_all_lockdown_open_manufacturing(self):
        self.scenario_continued_all_lockdown()
        self.SCENARIO = 'CONTINUED_ALL_LOCKDOWN_MANUFACTURING'

    def scenario_continued_all_lockdown_open_schools(self):
        self.scenario_continued_all_lockdown()
        self.SCENARIO = 'CONTINUED_ALL_LOCKDOWN_SCHOOLS'

    def scenario_continued_all_lockdown_open_schools_seed_kids(self):
        self.scenario_continued_all_lockdown()
        self.SCENARIO = 'CONTINUED_ALL_LOCKDOWN_SCHOOLS_SEED_KIDS'
        self.SEED_INFECT_AGE_MIN = 8
        self.SEED_INFECT_AGE_MAX = 18

    def scenario_continued_all_lockdown_open_manufacturing_schools(self):
        self.scenario_continued_all_lockdown()
        self.SCENARIO = 'CONTINUED_ALL_LOCKDOWN_MANUFACTURING_SCHOOLS'

    def scenario_eased_all_lockdown(self):
        self.SCENARIO = 'EASED_ALL_LOCKDOWN'
        self.LOCKDOWN_DISTRICTS = list(self.DISTRICT_NAME_TO_ID.keys())
        self.LOCKDOWN_DISTRICTS_IDS = list(map(self.DISTRICT_NAME_TO_ID.get, self.LOCKDOWN_DISTRICTS))
        self.set_lockdown_parameters(lockdown_mode='lockdown_eased')

    def scenario_eased_all_lockdown_open_schools(self):
        self.scenario_eased_all_lockdown()
        self.SCENARIO = 'EASED_ALL_LOCKDOWN_SCHOOLS'

    def scenario_phase1_government_open_schools(self):
        self.scenario_eased_all_lockdown()
        self.SCENARIO = "PHASE1_GOVERNMENT_OPEN_SCHOOLS"

    def scenario_dynamic_phase1_government_open_schools(self):
        self.scenario_eased_all_lockdown()
        self.SCENARIO = "DYNAMIC_PHASE1_GOVERNMENT_OPEN_SCHOOLS"

    def scenario_accelerated_government_open_schools(self):
        self.scenario_eased_all_lockdown()
        self.SCENARIO = "ACCELERATED_GOVERNMENT_OPEN_SCHOOLS"

    def get_gamma_shape_scale(self, mean, std):
        shape = mean ** 2 / std
        scale = std / mean

        return shape, scale

    def get_district_movement_stay_period(self, weekday, src, dst):
        try:
            mean = self.DISTRICT_WEEKDAY_OD_STAY_COUNT_MATRIX.at[(weekday, src, dst), 'avg_duration']
            std = self.DISTRICT_WEEKDAY_OD_STAY_COUNT_MATRIX.at[(weekday, src, dst), 'stddev_duration']

            shape, scale = self.get_gamma_shape_scale(mean, std)

            period = timedelta(hours=np.random.gamma(shape, scale))
        except KeyError:
            period = timedelta(hours=24)

        return period

    def get_district_movement_stay_parameters(self, weekday, src, dst):
        shape = 24
        scale = 0.01

        try:
            mean = self.DISTRICT_WEEKDAY_OD_STAY_COUNT_MATRIX.at[(weekday, src, dst), 'avg_duration']
            std = self.DISTRICT_WEEKDAY_OD_STAY_COUNT_MATRIX.at[(weekday, src, dst), 'stddev_duration']

            shape, scale = self.get_gamma_shape_scale(mean, std)
        except KeyError:
            pass

        return (shape, scale)


def log_to_file(fname, message, as_log=True, verbose=True, delim='$$'):

    if as_log:
        log_message = f'{datetime.now()} {delim} {message}'
    else:
        log_message = message

    with open(fname, 'a+') as fl:
        fl.write(log_message + '\n')

    if verbose:
        print(message)
