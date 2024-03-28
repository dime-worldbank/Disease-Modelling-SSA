import pandas as pd
import numpy as np
import geopandas as gpd
# get the original matrix for mobility
od_data = pd.read_csv('/Users/robbiework/PycharmProjects/spacialEpidemiologyAnalysis/movementModels/betweenDistricts/'
                      'dailyDistrictTransitionProb_preLockdown_multiDist.csv')
# get the names of the districts
zimbabwe_data = gpd.read_file("/Users/robbiework/PycharmProjects/spacialEpidemiologyAnalysis/data/new_districts/"
                              "ZWE_adm2.shp")


def convert_cumulative_od_matrix_to_probabilities(data_path, save_path, average_week):
    """
    A function to convert the cumulative probability OD matrix to the probability of each someone in a given
    district visiting a given district
    :param data_path: Where you have the OD matrix saved
    :param save_path: Where you want to save the new matrix
    :param average_week: Boolean, if you want to keep the probabilities for each day or just average the week out
    :return:
    """
    data = pd.read_csv(data_path)
    if 'Unnamed: 0' in data.columns:
        data = data.drop('Unnamed: 0', axis=1)
    for idx in data.index:
        cumulative_values = list(data.iloc[idx][2:])
        displaced_cumulative_values = np.insert(np.delete(cumulative_values, -1), 0, 0)
        probabilities = cumulative_values - displaced_cumulative_values
        probabilities = probabilities / 100
        data.iloc[idx][2:] = probabilities
        for idx_2, val in enumerate(data.columns[2:]):
            data.loc[idx, val] = probabilities[idx_2]
    if average_week:
        data = data.groupby('home_region').mean()
        data = data.drop('weekday', axis=1)
    data.to_csv(save_path)



convert_cumulative_od_matrix_to_probabilities('/Users/robbiework/PycharmProjects/spacialEpidemiologyAnalysis/movementModels/betweenDistricts/daily_region_transition_probability-new-district-pre-lockdown_i5.csv',
                                              '/Users/robbiework/PycharmProjects/spacialEpidemiologyAnalysis/movementModels/betweenDistricts/'
                                              'formatted_pre_lockdown_movement_not_averaged.csv',
                                              False)
convert_cumulative_od_matrix_to_probabilities('/Users/robbiework/PycharmProjects/spacialEpidemiologyAnalysis/movementModels/betweenDistricts/daily_region_transition_probability-new-district-pre-lockdown_i5.csv',
                                              '/Users/robbiework/PycharmProjects/spacialEpidemiologyAnalysis/movementModels/betweenDistricts/'
                                              'formatted_pre_lockdown_movement_averaged.csv',
                                              True)
# match the name of the districts to the d_ format
zimbabwe_data['district_id'] = ['d_' + str(i) for i in zimbabwe_data.ID_2.values]
# rename the columns
od_data = od_data.rename(columns=dict(zip(od_data.columns,
                                          ['weekday', 'home_region', 'Bulawayo', 'Harare', 'Buhera',
                                           'Chimanimani', 'Chipinge', 'Makoni', 'Mutare', 'Mutasa', 'Nyanga', 'Bindura',
                                           'Centenary', 'Guruve', 'Mazowe', 'Mount Darwin', 'Rushinga', 'Shamva',
                                           'Chikomba', 'Goromonzi', 'Marondera', 'Mudzi', 'Murehwa', 'Mutoko', 'Seke',
                                           'UMP', 'Wedza', 'Chegutu', 'Hurungwe', 'Kadoma', 'Kariba', 'Makonde',
                                           'Zvimba', 'Bikita', 'Chiredzi', 'Chivi', 'Gutu', 'Masvingo', 'Mwenezi',
                                           'Zaka', 'Binga', 'Bubi', 'Hwange', 'Lupane', 'Nkayi', 'Tsholotsho', 'Umguza',
                                           'Beitbridge', 'Bulilima (North)', 'Gwanda', 'Insiza', 'Mangwe (South)',
                                           'Matobo', 'Umzingwane', 'Chirumhanzu', 'Gokwe North', 'Gokwe South', 'Gweru',
                                           'Kwekwe', 'Mberengwa', 'Shurugwi', 'Zvishavane'])))
# Rename the districts in the rows
row_map_dict = dict(zip(list(zimbabwe_data['district_id']), list(zimbabwe_data['NAME_2'])))
od_data['home_region'] = od_data['home_region'].map(row_map_dict)
# Turn each column to a percentage
for idx in od_data.index:
    cumulative_values = list(od_data.iloc[idx][2:])
    displaced_cumulative_values = np.insert(np.delete(cumulative_values, -1), 0, 0)
    probabilities = cumulative_values - displaced_cumulative_values
    probabilities = probabilities / 100
    print(sum(probabilities))
    od_data.iloc[idx][2:] = probabilities
    for idx_2, val in enumerate(od_data.columns[2:]):
        od_data.loc[idx, val] = probabilities[idx_2]
od_data.to_csv('/Users/robbiework/PycharmProjects/spacialEpidemiologyAnalysis/movementModels/betweenDistricts/'
               'formatted_od_matrix.csv')
