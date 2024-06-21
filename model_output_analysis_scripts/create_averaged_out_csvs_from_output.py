import os
import numpy as np
import pandas as pd
import pandas.errors
import re
import sys

# define your file paths, if running from IDE specify file paths in 'if' part, otherwise specify in terminal
try:
    folder_path = sys.argv[1]
    output_save_path = sys.argv[2]
except IndexError:
    folder_path = "/Users/robbiework/Desktop/perfectBubbleArmy/"
    output_save_path = "/Users/robbiework/PycharmProjects/spacialEpidemiologyAnalysis/bubble_analysis/outputs/"


def main():
    # create folders to store the model output data and plots
    if not os.path.exists(output_save_path + 'averaged_outputs'):
        os.makedirs(output_save_path + 'averaged_outputs')

    # get the names of the scenarios
    scenarios = []
    output_filenames = []
    for file in os.listdir(folder_path):
        scenarios.append(file.split('_')[1])
        if (re.split(r'\d+', file)[1][1:-4] != '') & file.endswith('.txt'):
            output_filenames.append(re.split(r'\d+', file)[1][1:-4])

    scenarios = np.unique(scenarios)
    output_filenames = np.unique(output_filenames)
    # iterate over the scenarios and outputs to create dictionaries for each scenario with corresponding averaged output
    averaged_output = dict()
    for scenario in scenarios:
        for output_filename in output_filenames:
            # loop over all the output files
            output_df = pd.DataFrame()
            for file in os.listdir(folder_path):
                if (scenario in file) and (output_filename in file):
                    try:
                        data = pd.read_csv(folder_path + file, delimiter='\t')
                    except pandas.errors.ParserError:
                        pass
                    data = data.loc[:, ~data.columns.str.contains('Unnamed')]
                    if len(output_df.columns) == 0:
                        output_df = pd.DataFrame(columns=data.columns)
                    output_df = pd.concat([output_df, data])

            # process them into a format we can use for easily creating plots
            if output_filename == 'Admin_Zone_Demographics':
                output_df_mean = output_df.groupby(['day', 'district', 'sex']).mean().reset_index()
                output_df_std = output_df.groupby(['day', 'district', 'sex']).std().reset_index()
                averaged_output[scenario + "_" + output_filename + "_mean"] = output_df_mean
                averaged_output[scenario + "_" + output_filename + "_std"] = output_df_std
            elif output_filename == 'Age_Gender_Demographics_Covid':
                output_df_mean = output_df.groupby(['day', 'metric', 'sex']).mean()
                output_df_std = output_df.groupby(['day', 'metric', 'sex']).std()
                for metric in output_df.metric.unique():
                    for sex in output_df.sex.unique():
                        averaged_output[scenario + '_' + output_filename + "_" + metric + '_' + sex + "_mean"] = \
                            output_df_mean.xs((metric, sex), level=('metric', 'sex'))
                        averaged_output[scenario + '_' + output_filename + "_" + metric + '_' + sex + "_std"] = \
                            output_df_std.xs((metric, sex), level=('metric', 'sex'))
            elif output_filename == 'Cases_Per_District':
                output_df_mean = output_df.groupby(['day', 'metric']).mean()
                output_df_std = output_df.groupby(['day', 'metric']).std()
                for metric in output_df.metric.unique():
                    averaged_output[scenario + '_' + output_filename + "_" + metric + "_mean"] = \
                        output_df_mean.xs(metric, level='metric')
                    averaged_output[scenario + '_' + output_filename + "_" + metric + "_std"] = \
                        output_df_std.xs(metric, level='metric')
            elif output_filename == 'District_Level_Population_Size':
                output_df_mean = output_df.groupby('day').mean()
                output_df_std = output_df.groupby('day').std()
                averaged_output[scenario + '_' + output_filename + "_mean"] = output_df_mean
                averaged_output[scenario + '_' + output_filename + "_std"] = output_df_std
            elif output_filename == 'Economic_Status_Covid':
                output_df_mean = output_df.groupby(['day', 'metric']).mean()
                output_df_std = output_df.groupby(['day', 'metric']).std()
                for metric in output_df.metric.unique():
                    averaged_output[scenario + '_' + output_filename + "_" + metric + "_mean"] = \
                        output_df_mean.xs(metric, level='metric')
                    averaged_output[scenario + '_' + output_filename + "_" + metric + "_std"] = \
                        output_df_std.xs(metric, level='metric')
            elif output_filename == 'Incidence_Of_Covid':
                output_df_mean = output_df.groupby(['day', 'sex']).mean()
                output_df_std = output_df.groupby(['day', 'sex']).std()
                for sex in output_df.sex.unique():
                    averaged_output[scenario + '_' + output_filename + "_" + sex + "_mean"] = \
                        output_df_mean.xs(sex, level='sex')
                    averaged_output[scenario + '_' + output_filename + "_" + sex + "_std"] = \
                        output_df_std.xs(sex, level='sex')
            elif output_filename == 'Incidence_Of_Covid_Death':
                output_df_mean = output_df.groupby(['day', 'sex']).mean()
                output_df_std = output_df.groupby(['day', 'sex']).std()
                for sex in output_df.sex.unique():
                    averaged_output[scenario + '_' + output_filename + "_" + sex + "_mean"] = \
                        output_df_mean.xs(sex, level='sex')
                    averaged_output[scenario + '_' + output_filename + "_" + sex + "_std"] = \
                        output_df_std.xs(sex, level='sex')
            elif output_filename == 'Incidence_Of_Other_Death':
                output_df_mean = output_df.groupby(['day', 'sex']).mean()
                output_df_std = output_df.groupby(['day', 'sex']).std()
                for sex in output_df.sex.unique():
                    averaged_output[scenario + '_' + output_filename + "_" + sex + "_mean"] = \
                        output_df_mean.xs(sex, level='sex')
                    averaged_output[scenario + '_' + output_filename + "_" + sex + "_std"] = \
                        output_df_std.xs(sex, level='sex')
            elif output_filename == 'Percent_In_District_With_Covid':
                output_df_mean = output_df.groupby('day').mean()
                output_df_std = output_df.groupby('day').std()
                averaged_output[scenario + '_' + output_filename + "_std"] = output_df_mean
                averaged_output[scenario + '_' + output_filename + "_std"] = output_df_std
            else:
                print(output_filename + "did not get aggregated")

    for key in averaged_output.keys():
        averaged_output[key].to_csv(output_save_path + 'averaged_outputs/' + key + ".csv")


if __name__ == "__main__":
    main()
