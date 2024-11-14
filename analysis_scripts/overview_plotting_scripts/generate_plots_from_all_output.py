import os
import numpy as np
import pandas as pd
from matplotlib import pyplot as plt
import re
import sys

# define your file paths, if running from IDE specify file paths in 'if' part, otherwise specify in terminal
try:
    averaged_out_folder_path = sys.argv[1]
    output_save_path = sys.argv[2]
except IndexError:
    averaged_out_folder_path = "/Users/robbiework/Desktop/test_model_output/averaged_outputs/"
    output_save_path = "/Users/robbiework/Desktop/test_model_plot_output/"


def main():
    # Check that the user has specified an appropriate file path for this
    assert averaged_out_folder_path[-1] == '/', 'Please add / to the end of your file path'
    assert averaged_out_folder_path.split('/')[-2] == 'averaged_outputs', \
        "This script only generates plots from averaged model runs, please use " \
        "'create_averaged_out_csvs_form_output.py first."
    # Create output from the cases per district csv files
    csv_files_contain = 'Cases_Per_Admin'
    cumulative_file_names = []
    new_deaths_file_names = []
    total_asympt_file_names = []
    total_mild_file_names = []
    total_severe_file_names = []
    total_critical_file_names = []
    total_recovered_file_names = []
    total_cases_file_names = []
    scenarios = []
    # store the file names sensibly
    for file in os.listdir(averaged_out_folder_path):
        if csv_files_contain in file:
            if 'cumulative' in file:
                cumulative_file_names.append(file)
            elif 'new_deaths' in file:
                new_deaths_file_names.append(file)
            elif 'asympt' in file:
                total_asympt_file_names.append(file)
            elif 'mild' in file:
                total_mild_file_names.append(file)
            elif 'severe' in file:
                total_severe_file_names.append(file)
            elif 'critical' in file:
                total_critical_file_names.append(file)
            elif 'recovered' in file:
                total_recovered_file_names.append(file)
            elif 'total_cases' in file:
                total_cases_file_names.append(file)
            scenarios.append(file.split('_')[0])

    # unique scenarios and how many there are, also create the output save name
    scenarios = np.unique(scenarios)
    scenario_output_str = ""
    for scenario in scenarios:
        scenario_output_str += '_' + scenario
        print("Generating plots for " + scenario + "...\n")
    number_of_scenarios = len(scenarios)
    if number_of_scenarios > 6:
        print("Generating plots for numerous scenarios, graphs may not be clear")
    # Create a plot that looks at how many cases by each classification there are in each scenario
    scenario_bin = dict()
    fig1 = plt.figure(figsize=(6, 6))
    ax1 = plt.subplot(1, 1, 1)
    for idx, scenario in enumerate(scenarios):
        asympt_mild_severe_critical_dead_recovered_bin = [0, 0, 0, 0, 0, 0]
        for file in total_asympt_file_names:
            if (scenario in file) & ('mean' in file):
                data = pd.read_csv(averaged_out_folder_path + file, index_col='day')
                asympt_mild_severe_critical_dead_recovered_bin[0] = data.sum(axis=1).sum()
        for file in total_mild_file_names:
            if (scenario in file) & ('mean' in file):
                data = pd.read_csv(averaged_out_folder_path + file, index_col='day')
                asympt_mild_severe_critical_dead_recovered_bin[1] = data.sum(axis=1).sum()
        for file in total_severe_file_names:
            if (scenario in file) & ('mean' in file):
                data = pd.read_csv(averaged_out_folder_path + file, index_col='day')
                asympt_mild_severe_critical_dead_recovered_bin[2] = data.sum(axis=1).sum()
        for file in total_critical_file_names:
            if (scenario in file) & ('mean' in file):
                data = pd.read_csv(averaged_out_folder_path + file, index_col='day')
                asympt_mild_severe_critical_dead_recovered_bin[3] = data.sum(axis=1).sum()
        for file in new_deaths_file_names:
            if (scenario in file) & ('mean' in file):
                data = pd.read_csv(averaged_out_folder_path + file, index_col='day')
                asympt_mild_severe_critical_dead_recovered_bin[4] = data.sum(axis=1).sum()
        for file in total_recovered_file_names:
            if (scenario in file) & ('mean' in file):
                data = pd.read_csv(averaged_out_folder_path + file, index_col='day')
                asympt_mild_severe_critical_dead_recovered_bin[5] = data.sum(axis=1).sum()
        scenario_bin[scenario] = asympt_mild_severe_critical_dead_recovered_bin

    colours = ['lightsteelblue', 'lightsalmon', 'lemonchiffon', 'steelblue', 'k', 'g']
    labels = ['asympt.', 'mild', 'severe', 'critical', 'dead', 'recovered']
    xticks = []
    xtick_labels = []
    # Create a stacked bar chart of the number of cases by type (see label) in each scenario
    for idx, scenario in enumerate(scenario_bin.keys()):
        for idx1, val in enumerate(scenario_bin[scenario]):
            if idx1 > 0:
                ax1.bar([idx], [val], bottom=np.cumsum(scenario_bin[scenario])[idx1 - 1],
                        label=labels[idx1] if idx == 0 else "",
                        color=colours[idx1])
            else:
                ax1.bar([idx], [val], label=labels[idx1] if idx == 0 else "", color=colours[idx1])
        xticks.append(idx)
        xtick_labels.append(scenario)
    ax1.set_yscale('log')
    ax1.set_xticks(xticks, xtick_labels, fontweight='bold')
    ax1.set_ylabel('Total number, log scale', fontweight='bold')
    ax1.legend()
    ax1.set_title("Differences in Covid-19 cases by type of infection", fontweight='bold')
    plt.savefig(output_save_path + scenario_output_str[1:] + "_Breakdown_Of_Covid_Cases_By_Type.png")
    plt.close(fig1)

    # Create a plot showing the total number of cases in each scenario
    fig1 = plt.figure(figsize=(8, 8))
    ax1 = plt.subplot(1, 1, 1)
    total_cases_mean_bin = dict()

    for scenario in scenarios:
        for file in total_cases_file_names:
            if (scenario in file) & ('mean' in file):
                total_cases_mean_bin[scenario] = list(pd.read_csv(averaged_out_folder_path + file, index_col='day').sum(axis=1))

    for key in total_cases_mean_bin.keys():
        ax1.plot(np.arange(len(total_cases_mean_bin[key])), total_cases_mean_bin[key], label=key)

    ax1.set_title("Total cases", fontweight='bold')
    ax1.legend()
    ax1.set_ylabel('Number of cases', fontweight='bold')
    ax1.set_xlabel('Time', fontweight='bold')
    plt.savefig(output_save_path + scenario_output_str[1:] + "_Bell_Curve.png")
    plt.close(fig1)
    # Create a plot showing the total number of cases as a bar
    fig1 = plt.figure(figsize=(8, 8))
    ax1 = plt.subplot(1, 1, 1)

    for idx, key in enumerate(total_cases_mean_bin.keys()):
        ax1.bar(idx, sum(total_cases_mean_bin[key]))

    ax1.set_title("Total cases", fontweight='bold')
    ax1.set_ylabel('Number of cases', fontweight='bold')
    ax1.set_xticks(np.arange(len(total_cases_mean_bin.keys())), list(total_cases_mean_bin.keys()))
    plt.savefig(output_save_path + scenario_output_str[1:] + "_total_cases_bar.png")
    plt.close(fig1)
    has_cases_dict = dict()
    number_of_zones = dict()
    for scenario in scenarios:
        for file in cumulative_file_names:
            if (scenario in file) & ('mean' in file) & ('cases' in file):
                raw_data = pd.read_csv(averaged_out_folder_path + file, index_col='day')
                masked_data = raw_data > 0
                has_cases_dict[scenario] = list(masked_data.sum(axis=1))
                number_of_zones[scenario] = len(raw_data.columns)

    # Create a plot showing the number of admin zones with at least one case
    fig1 = plt.figure(figsize=(8, 8))
    ax1 = plt.subplot(1, 1, 1)

    for key in has_cases_dict.keys():
        ax1.plot(np.arange(len(has_cases_dict[key])),
                 np.multiply(np.divide(has_cases_dict[key], number_of_zones[key]), 100), label=key)

    ax1.set_title("Percent of admin zones that zones that have cases",
                  fontweight='bold')
    ax1.legend()
    ax1.set_ylabel('Percent', fontweight='bold')
    ax1.set_xlabel('Time', fontweight='bold')
    plt.savefig(output_save_path + scenario_output_str[1:] + "_Admin_Zones_With_Cases.png")
    plt.close(fig1)

    # create a plot that shows the percent of cases that are fatal in each scenario
    fig1 = plt.figure(figsize=(8, 8))
    ax1 = plt.subplot(1, 1, 1)
    percent_fatal_bin = dict()

    for scenario in scenario_bin.keys():
        percent_fatal_bin[scenario] = (scenario_bin[scenario][4] / sum(total_cases_mean_bin[scenario])) * 100

    for idx, val in enumerate(percent_fatal_bin.keys()):
        ax1.bar([idx], [percent_fatal_bin[val]], color=colours[idx])

    ax1.set_ylabel('Percent', fontweight='bold')
    ax1.set_xticks(xticks, xtick_labels, fontweight='bold')
    ax1.set_title("Percent of Covid-19 cases that are fatal in each scenario", fontweight='bold')
    plt.savefig(output_save_path + scenario_output_str[1:] + "_Percent_Of_Cases_That_Are_Fatal.png")
    plt.close(fig1)

    # create folders to store the model output data and plots
    csv_files_contain = 'Economic_Status'
    file_names = []
    scenarios = []
    for file in os.listdir(averaged_out_folder_path):
        if csv_files_contain in file:
            file_names.append(file)
            scenarios.append(file.split('_')[0])
    scenarios = np.unique(scenarios)
    scenario_output_str = ""
    for scenario in scenarios:
        scenario_output_str += '_' + scenario
    number_of_scenarios = len(scenarios)

    # get the number of people in each occupation to inform the order in which we plot
    occupation_order_to_plot = []
    cols_to_drop = []

    for file in file_names:
        if 'number_in_occ_mean' in file:
            data = pd.read_csv(averaged_out_folder_path + file, index_col='day')
            # I've set this up to use all the occupations that appear in each model run, some columns need to be dropped
            # based on which occupations feature in the census file used. Do so here...
            while data.iloc[0][data.iloc[0].idxmin()] < 1:
                cols_to_drop.append(data.iloc[0].idxmin())
                data = data.drop(data.iloc[0].idxmin(), axis=1)
            # Now get the order of occupations based on size
            while len(data.columns) > 0:
                occupation_order_to_plot.append(data.iloc[0].idxmax())
                data = data.drop(data.iloc[0].idxmax(), axis=1)
            break
    scenario_bin = dict()

    for scenario in scenarios:
        cases_per_occ_counts = []
        for file in file_names:
            if (scenario in file) & ('number_with_covid_mean' in file):
                data = pd.read_csv(averaged_out_folder_path + file, index_col='day')
                data = data.drop(cols_to_drop, axis=1)
                for col in occupation_order_to_plot:
                    cases_per_occ_counts.append(data[col].sum())
        scenario_bin[scenario] = cases_per_occ_counts

    # create a plot that shows the number of cases in each occupation
    colours = ['r', 'b', 'g', 'y', 'c', 'k', 'coral', 'saddlebrown', 'pink', 'cornflowerblue', 'olive',
               'lightsteelblue', 'gold', 'crimson', 'teal']
    fig1 = plt.figure(figsize=(8, 8))
    ax1 = plt.subplot(1, 1, 1)
    for xval, scenario in enumerate(scenarios):
        for idx_occ, occ in enumerate(occupation_order_to_plot):
            if xval == 0:
                if idx_occ > 0:
                    ax1.bar(xval, scenario_bin[scenario][idx_occ], bottom=scenario_bin[scenario][idx_occ - 1],
                            label=occ, color=colours[idx_occ])
                else:
                    ax1.bar(xval, scenario_bin[scenario][idx_occ], label=occ, color=colours[idx_occ])
            else:
                if idx_occ > 0:
                    ax1.bar(xval, scenario_bin[scenario][idx_occ], bottom=scenario_bin[scenario][idx_occ - 1],
                            label="", color=colours[idx_occ])
                else:
                    ax1.bar(xval, scenario_bin[scenario][idx_occ], label="", color=colours[idx_occ])

    ax1.legend()
    ax1.set_xticks(np.arange(len(scenarios)), scenarios)
    ax1.set_ylabel('Number of cases')
    ax1.set_title("Number of cases in each occupation for each scenario")
    plt.savefig(output_save_path + scenario_output_str[1:] + "_cases_by_occ_stack.png")
    plt.close(fig1)
    fig1 = plt.figure(figsize=(8, 8))
    ax1 = plt.subplot(1, 1, 1)
    max_bar_width = 0.95
    for xval, scenario in enumerate(scenarios):
        ax1.bar(np.arange(len(occupation_order_to_plot)) + xval * max_bar_width / len(scenarios),
                scenario_bin[scenario], color=colours[xval],
                label=scenario, width=max_bar_width / len(scenarios))

    ax1.legend()
    ax1.set_xticks(np.arange(len(occupation_order_to_plot)),
                   occupation_order_to_plot, rotation=90)
    ax1.set_ylabel('Number of cases')
    ax1.set_title("Number of cases in each occupation for each scenario")
    plt.savefig(output_save_path + scenario_output_str[1:] + "_cases_by_occ_long_bar.png")
    plt.close(fig1)


    csv_files_contain = 'Age_Gender_Demographics_Covid_cases'
    file_names = []
    scenarios = []
    for file in os.listdir(averaged_out_folder_path):
        if csv_files_contain in file:
            file_names.append(file)
            scenarios.append(file.split('_')[0])
    scenarios = np.unique(scenarios)
    scenario_output_str = ""
    for scenario in scenarios:
        scenario_output_str += '_' + scenario
    number_of_scenarios = len(scenarios)
    fig1 = plt.figure(figsize=(12, 6))
    axs = [plt.subplot(1, 2, 1), plt.subplot(1, 2, 2)]
    axs[0].set_title('Males', weight='bold')
    axs[0].set_ylabel('Cases', weight='bold')
    axs[1].set_title('Females', weight='bold')
    axs[1].set_ylabel('Cases', weight='bold')

    fig1.suptitle("Number of cases in each scenario, by sex and age group", weight='bold')

    offset = np.linspace(0, 1, num=number_of_scenarios, endpoint=False)
    total_cases = dict()
    total_cases_std = dict()
    for idx, scenario in enumerate(scenarios):
        for idx2, sex in enumerate(['_m_', '_f_']):
            for file in file_names:
                if (scenario in file) & ('mean' in file) & (sex in file):
                    data = pd.read_csv(averaged_out_folder_path + file, index_col='day')
                    data = data.loc[:, ~data.columns.str.contains('Unnamed')]
                    std_file_name = file.split('mean')[0] + 'std' + file.split('mean')[1]
                    std_data = pd.read_csv(averaged_out_folder_path + std_file_name, index_col='day')
                    axs[idx2].bar(np.arange(len(data.columns)) + offset[idx], data.sum(),
                                  label=scenario, width=(offset[-1] - offset[-2]),
                                  yerr=np.multiply(1.96, std_data.sum()))
                    total_cases[scenario] = data.sum().sum()
                    total_cases_std[scenario] = std_data.sum().sum()


    axs[0].legend()
    axs[1].legend()

    axs[0].set_xticks(np.arange(len(data.columns)) + offset[1] / 2, data.columns, weight='bold', rotation=90)
    axs[1].set_xticks(np.arange(len(data.columns)) + offset[1] / 2, data.columns, weight='bold', rotation=90)

    plt.savefig(output_save_path + scenario_output_str[1:] + "_Covid_Age_Sex_Demographics_Cases.png")
    plt.close(fig1)
    csv_files_contain = 'Age_Gender_Demographics_Covid_deaths'
    file_names = []
    for file in os.listdir(averaged_out_folder_path):
        if csv_files_contain in file:
            file_names.append(file)
    fig1 = plt.figure(figsize=(12, 6))
    axs = [plt.subplot(1, 2, 1), plt.subplot(1, 2, 2)]
    axs[0].set_title('Males', weight='bold')
    axs[0].set_ylabel('Deaths', weight='bold')
    axs[1].set_title('Females', weight='bold')
    axs[1].set_ylabel('Deaths', weight='bold')

    fig1.suptitle("Number of deaths in each scenario, by sex and age group", weight='bold')

    offset = np.linspace(0, 1, num=number_of_scenarios, endpoint=False)
    total_deaths = dict()
    total_deaths_std = dict()
    for idx, scenario in enumerate(scenarios):
        for idx2, sex in enumerate(['_m_', '_f_']):
            for file in file_names:
                if (scenario in file) & ('mean' in file) & (sex in file):
                    data = pd.read_csv(averaged_out_folder_path + file, index_col='day')
                    data = data.loc[:, ~data.columns.str.contains('Unnamed')]
                    std_file_name = file.split('mean')[0] + 'std' + file.split('mean')[1]
                    std_data = pd.read_csv(averaged_out_folder_path + std_file_name, index_col='day')
                    axs[idx2].bar(np.arange(len(data.columns)) + offset[idx], data.sum(),
                                  label=scenario, width=(offset[-1] - offset[-2]),
                                  yerr=np.multiply(1.96, std_data.sum()))
                    total_deaths[scenario] = data.sum().sum()
                    total_deaths_std[scenario] = std_data.sum().sum()

    axs[0].legend()
    axs[1].legend()

    axs[0].set_xticks(np.arange(len(data.columns)) + offset[1] / 2, data.columns, weight='bold', rotation=90)
    axs[1].set_xticks(np.arange(len(data.columns)) + offset[1] / 2, data.columns, weight='bold', rotation=90)

    plt.savefig(output_save_path + scenario_output_str[1:] + "_Covid_Age_Sex_Demographics_Deaths.png")
    plt.close(fig1)

    fig1 = plt.figure(figsize=(6, 6), constrained_layout=True)
    ax = plt.subplot(1, 1, 1)
    ax.bar(np.arange(len(total_cases.keys())), total_cases.values(),
                  yerr=np.multiply(1.96, list(total_cases_std.values())))
    ax.set_xticks(np.arange(len(total_cases.keys())), total_cases.keys(), weight='bold')
    ax.set_ylabel("Cases", weight='bold')
    ax.set_title("Total COVID-19 cases in each scenario", weight='bold')
    plt.savefig(output_save_path + scenario_output_str[1:] + "_Total_Covid_Cases.png")
    plt.close(fig1)
    fig1 = plt.figure(figsize=(6, 6), constrained_layout=True)
    ax = plt.subplot(1, 1, 1)
    ax.bar(np.arange(len(total_deaths.keys())), total_deaths.values(),
                  yerr=np.multiply(1.96, list(total_deaths_std.values())))
    ax.set_xticks(np.arange(len(total_deaths.keys())), total_deaths.keys(), weight='bold')
    ax.set_ylabel("Deaths", weight='bold')
    ax.set_title("Total COVID-19 Deaths in each scenario", weight='bold')
    plt.savefig(output_save_path + scenario_output_str[1:] + "_Total_Covid_Deaths.png")
    plt.close(fig1)

    csv_files_contain = "Overall_Demographics"
    file_names = []
    for file in os.listdir(averaged_out_folder_path):
        if csv_files_contain in file:
            file_names.append(file)
    fig1 = plt.figure(figsize=(12, 6))
    axs = [plt.subplot(1, 2, 1), plt.subplot(1, 2, 2)]
    axs[0].set_title('Males', weight='bold')
    axs[0].set_ylabel('Number', weight='bold')
    axs[1].set_title('Females', weight='bold')
    axs[1].set_ylabel('Number', weight='bold')
    offset = np.linspace(0, 1, num=number_of_scenarios, endpoint=False)

    fig1.suptitle("Breakdown of population demographics at the start of the simulation", weight='bold')
    for idx, scenario in enumerate(scenarios):
        for idx2, sex in enumerate(['m', 'f']):
            for file in file_names:
                if (scenario in file) & ('mean' in file):
                    data = pd.read_csv(averaged_out_folder_path + file, index_col='day')
                    data = data.loc[:, ~data.columns.str.contains('Unnamed')]
                    data = data.loc[data['sex'] == sex]
                    if max(data.loc[0].shape) == min(data.loc[0].shape):
                        axs[idx2].bar(np.arange(len(data.columns[1:])) + offset[idx], data.loc[0].values[1:],
                                      label=scenario, width=(offset[-1] - offset[-2]))
                    else:
                        axs[idx2].bar(np.arange(len(data.columns[1:])) + offset[idx], data.loc[0].sum(),
                                      label=scenario, width=(offset[-1] - offset[-2]))

    axs[0].legend()
    axs[1].legend()

    axs[0].set_xticks(np.arange(len(data.columns[1:])) + offset[1] / 2, data.columns[1:], weight='bold', rotation=90)
    axs[1].set_xticks(np.arange(len(data.columns[1:])) + offset[1] / 2, data.columns[1:], weight='bold', rotation=90)

    plt.savefig(output_save_path + scenario_output_str[1:] + "_Simulation_Start_Demographics.png")
    plt.close(fig1)

    fig1 = plt.figure(figsize=(12, 6))
    axs = [plt.subplot(1, 2, 1), plt.subplot(1, 2, 2)]
    axs[0].set_title('Males', weight='bold')
    axs[0].set_ylabel('Number', weight='bold')
    axs[1].set_title('Females', weight='bold')
    axs[1].set_ylabel('Number', weight='bold')
    offset = np.linspace(0, 1, num=number_of_scenarios, endpoint=False)

    fig1.suptitle("Breakdown of population demographics at the end of the simulation", weight='bold')
    admin_zone_demographics_dict = dict()
    for idx, scenario in enumerate(scenarios):
        for idx2, sex in enumerate(['m', 'f']):
            for file in file_names:
                if (scenario in file) & ('mean' in file):
                    data = pd.read_csv(averaged_out_folder_path + file, index_col='day')
                    data = data.loc[:, ~data.columns.str.contains('Unnamed')]
                    data = data.loc[data['sex'] == sex]
                    if max(data.loc[0].shape) == min(data.loc[0].shape):
                        axs[idx2].bar(np.arange(len(data.columns[1:])) + offset[idx],
                                      data.loc[max(data.index)].values[1:],
                                      label=scenario, width=(offset[-1] - offset[-2]))
                    else:
                        axs[idx2].bar(np.arange(len(data.columns[1:])) + offset[idx], data.loc[max(data.index)].sum(),
                                      label=scenario, width=(offset[-1] - offset[-2]))

    axs[0].legend()
    axs[1].legend()

    axs[0].set_xticks(np.arange(len(data.columns[1:])) + offset[1] / 2, data.columns[1:], weight='bold', rotation=90)
    axs[1].set_xticks(np.arange(len(data.columns[1:])) + offset[1] / 2, data.columns[1:], weight='bold', rotation=90)

    plt.savefig(output_save_path + scenario_output_str[1:] + "_Simulation_End_Demographics.png")
    plt.close(fig1)


if __name__ == "__main__":
    main()
