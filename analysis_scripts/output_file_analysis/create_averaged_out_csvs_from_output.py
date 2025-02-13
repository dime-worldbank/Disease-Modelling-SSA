import os
import numpy as np
import pandas as pd
import pandas.errors
import re
import sys

# define your file paths, if running from IDE specify file paths in 'except' part folder_path/outout_save_path variable,
# otherwise specify in terminal
try:
    folder_path = sys.argv[1]
    output_save_path = sys.argv[2]
except IndexError:
    folder_path = "/Users/robbiework/Desktop/test_logging/"
    output_save_path = "/Users/robbiework/Desktop/test_model_output/"


def main():
    # Quick check on the input and output folder variable names, this makes sure that the rest of the operations done on
    # these folders will work as intended
    assert folder_path[-1] == '/', "Please add a '/' to the end of your input folder path"
    assert output_save_path[-1] == '/', "Please add a '/' to the end of your output save path"

    # create folders to store the model output data and plots
    if not os.path.exists(output_save_path + 'averaged_outputs'):
        os.makedirs(output_save_path + 'averaged_outputs')

    # get the names of the scenarios
    scenarios = []
    output_filenames = []
    for file in os.listdir(folder_path):
        if ~file.startswith('.') & file.endswith('.txt'):
            scenarios.append(file.split('_')[1])
            if (re.split(r'\d+', file)[1][1:-4] != ''):
                output_filenames.append(re.split(r'\d+', file)[1][1:-4])
    # Only need the unique names of the scenario and output filenames for averaging and storing the results
    scenarios = np.unique(scenarios)
    output_filenames = np.unique(output_filenames)
    # iterate over the scenarios and outputs to create dictionaries for each scenario with corresponding averaged output
    averaged_output = dict()
    for scenario in scenarios:
        for output_filename in output_filenames:
            # loop over all the output files
            output_df = pd.DataFrame()
            # Filter through the output files and pull out only those that are of this scenario and of this particular
            # output file type
            relevant_files = [f for f in os.listdir(folder_path) if re.search(scenario, f)]
            relevant_files = [f for f in relevant_files if re.search(output_filename, f)]
            # Iterate over the relevant file names
            for file in relevant_files:
                # Try to avoid issues that may have happened in the running of the model, e.g. parse errors and
                # name errors
                try:
                    # Load in the data
                    data = pd.read_csv(folder_path + file, delimiter='\t')
                    # Drop any 'unnamed' columns
                    data = data.loc[:, ~data.columns.str.contains('Unnamed')]
                    # If this is the first time loading in the dataframe, we need to copy the column format of the
                    # loaded in data in our output dataframe
                    if len(output_df.columns) == 0:
                        output_df = pd.DataFrame(columns=data.columns)
                    # Concat any existing dataframes with the one being loaded in for this line
                    output_df = pd.concat([output_df, data])
                except (pandas.errors.ParserError, NameError) as e:
                    print("Error in reading in scenario " + scenario + "file " + file)
                    pass

            # Process them into a format we can use for easily creating plots by averaging out certain output files.
            # Some output files will have certain breakdowns of the population that they focus on, e.g. sex, location,
            # age etc... As we are doing a general purpose averaging of the model output rather than a bespoke one, we
            # choose options we think are best for this purpose.
            if 'Admin_Zone_level_Demographics' in output_filename:
                output_df_mean = output_df.groupby(['day', 'admin_zone', 'sex']).mean().reset_index()
                output_df_std = output_df.groupby(['day', 'admin_zone', 'sex']).std().reset_index()
                averaged_output[scenario + "_" + output_filename + "_mean"] = output_df_mean
                averaged_output[scenario + "_" + output_filename + "_std"] = output_df_std
            elif 'Age_Gender_Demographics_Covid' in output_filename:
                output_df_mean = output_df.groupby(['day', 'metric', 'sex']).mean()
                output_df_std = output_df.groupby(['day', 'metric', 'sex']).std()
                for metric in output_df.metric.unique():
                    for sex in output_df.sex.unique():
                        averaged_output[scenario + '_' + output_filename + "_" + metric + '_' + sex + "_mean"] = \
                            output_df_mean.xs((metric, sex), level=('metric', 'sex'))
                        averaged_output[scenario + '_' + output_filename + "_" + metric + '_' + sex + "_std"] = \
                            output_df_std.xs((metric, sex), level=('metric', 'sex'))
            elif 'Overall_Demographics' in output_filename:
                output_df_mean = output_df.groupby(['day', 'sex']).mean().reset_index()
                output_df_std = output_df.groupby(['day', 'sex']).mean().reset_index()
                averaged_output[scenario + "_" + output_filename + "_mean"] = output_df_mean
                averaged_output[scenario + "_" + output_filename + "_std"] = output_df_std
            elif ('Cases_Per_District' in output_filename) or ('Cases_Per_Admin_Zone' in output_filename):
                output_df_mean = output_df.groupby(['day', 'metric']).mean()
                output_df_std = output_df.groupby(['day', 'metric']).std()
                for metric in output_df.metric.unique():
                    averaged_output[scenario + '_' + output_filename + "_" + metric + "_mean"] = \
                        output_df_mean.xs(metric, level='metric')
                    averaged_output[scenario + '_' + output_filename + "_" + metric + "_std"] = \
                        output_df_std.xs(metric, level='metric')
            elif ('District_Level_Population_Size' in output_filename) or \
                    ('Admin_Zone_Level_Population_Size' in output_filename):
                output_df_mean = output_df.groupby('day').mean()
                output_df_std = output_df.groupby('day').std()
                averaged_output[scenario + '_' + output_filename + "_mean"] = output_df_mean
                averaged_output[scenario + '_' + output_filename + "_std"] = output_df_std
            elif 'Economic_Status_Covid' in output_filename:
                output_df_mean = output_df.groupby(['day', 'metric']).mean()
                output_df_std = output_df.groupby(['day', 'metric']).std()
                for metric in output_df.metric.unique():
                    averaged_output[scenario + '_' + output_filename + "_" + metric + "_mean"] = \
                        output_df_mean.xs(metric, level='metric')
                    averaged_output[scenario + '_' + output_filename + "_" + metric + "_std"] = \
                        output_df_std.xs(metric, level='metric')
            elif 'Incidence_Of_Covid' in output_filename:
                output_df_mean = output_df.groupby(['day', 'sex']).mean()
                output_df_std = output_df.groupby(['day', 'sex']).std()
                for sex in output_df.sex.unique():
                    averaged_output[scenario + '_' + output_filename + "_" + sex + "_mean"] = \
                        output_df_mean.xs(sex, level='sex')
                    averaged_output[scenario + '_' + output_filename + "_" + sex + "_std"] = \
                        output_df_std.xs(sex, level='sex')
            elif 'Incidence_Of_Covid_Death' in output_filename:
                output_df_mean = output_df.groupby(['day', 'sex']).mean()
                output_df_std = output_df.groupby(['day', 'sex']).std()
                for sex in output_df.sex.unique():
                    averaged_output[scenario + '_' + output_filename + "_" + sex + "_mean"] = \
                        output_df_mean.xs(sex, level='sex')
                    averaged_output[scenario + '_' + output_filename + "_" + sex + "_std"] = \
                        output_df_std.xs(sex, level='sex')
            elif 'Incidence_Of_Other_Death' in output_filename:
                output_df_mean = output_df.groupby(['day', 'sex']).mean()
                output_df_std = output_df.groupby(['day', 'sex']).std()
                for sex in output_df.sex.unique():
                    averaged_output[scenario + '_' + output_filename + "_" + sex + "_mean"] = \
                        output_df_mean.xs(sex, level='sex')
                    averaged_output[scenario + '_' + output_filename + "_" + sex + "_std"] = \
                        output_df_std.xs(sex, level='sex')
            elif ('Percent_In_District_With_Covid' in output_filename) or \
                    ('Percent_In_Admin_Zone_With_Covid' in output_filename):
                output_df_mean = output_df.groupby('day').mean()
                output_df_std = output_df.groupby('day').std()
                averaged_output[scenario + '_' + output_filename + "_std"] = output_df_mean
                averaged_output[scenario + '_' + output_filename + "_std"] = output_df_std
            elif 'Percent_Covid_Cases_Fatal_In_Admin_Zone' in output_filename:
                output_df_mean = output_df.groupby('day').mean()
                output_df_std = output_df.groupby('day').std()
                averaged_output[scenario + '_' + output_filename + "_std"] = output_df_mean
                averaged_output[scenario + '_' + output_filename + "_std"] = output_df_std
            elif 'Percent_In_Admin_Zone_Died_From_Covid' in output_filename:
                output_df_mean = output_df.groupby('day').mean()
                output_df_std = output_df.groupby('day').std()
                averaged_output[scenario + '_' + output_filename + "_std"] = output_df_mean
                averaged_output[scenario + '_' + output_filename + "_std"] = output_df_std
            # Finally at the end of all the averaging. We have a quick check and a message to the user, explaining that
            # certain output files did not get aggregated.
            else:
                print(output_filename + " did not get aggregated")
    # Now save all the averaged output files in the 'output_save_path' location
    for key in averaged_output.keys():
        averaged_output[key].to_csv(output_save_path + 'averaged_outputs/' + key + ".csv")


if __name__ == "__main__":
    main()
