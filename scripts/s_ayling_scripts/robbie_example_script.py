import pandas as pd

five_perc_sample = pd.read_csv("/Users/robbiework/eclipse-workspace/Disease-Modelling-SSA/data/verification/"
                               "census_5perc_multiStatMultiDist.csv")

sample_size = ['10', '15', '20', '25', '30', '35', '40', '45', '50']
for size in sample_size:
    five_perc_sample = five_perc_sample.append(five_perc_sample)
    five_perc_sample['person_id'] = range(0, len(five_perc_sample))
    if 'Unnamed: 0' in five_perc_sample.columns:
        five_perc_sample = five_perc_sample.drop('Unnamed: 0', axis=1)
    file_save = "census_" + size + "perc_multiStatMultiDist.csv"
    five_perc_sample.to_csv("/Users/robbiework/eclipse-workspace/Disease-Modelling-SSA/data/verification/" + file_save)