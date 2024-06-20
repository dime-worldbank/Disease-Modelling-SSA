import os
import numpy as np
import pandas as pd
import pandas.errors
import re
import sys
from matplotlib import pyplot as plt


try:
    folder_path = sys.argv[1]
    output_save_path = sys.argv[2]
except IndexError:
    folder_path = "/Users/robbiework/PycharmProjects/spacialEpidemiologyAnalysis/bubble_analysis/outputs/" \
                  "averaged_outputs/"
    output_save_path = "/Users/robbiework/PycharmProjects/spacialEpidemiologyAnalysis/bubble_analysis/outputs/" \
                       "plots/"


def main():
    # create folders to store the model output data and plots

    csv_files_contain = 'Age_Gender_Demographics_Covid_cases'
    file_names = []
    scenarios = []
    for file in os.listdir(folder_path):
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
    for idx, scenario in enumerate(scenarios):
        for idx2, sex in enumerate(['_m_', '_f_']):
            for file in file_names:
                if (scenario in file) & ('mean' in file) & (sex in file):
                    data = pd.read_csv(folder_path + file, index_col='day')
                    data = data.loc[:, ~data.columns.str.contains('Unnamed')]
                    axs[idx2].bar(np.arange(len(data.columns)) + offset[idx], data.sum(),
                                  label=scenario, width=(offset[-1] - offset[-2]))
                    total_cases[scenario] = data.sum().sum()

    axs[0].legend()
    axs[1].legend()

    axs[0].set_xticks(np.arange(len(data.columns)) + offset[1] / 2, data.columns, weight='bold', rotation=90)
    axs[1].set_xticks(np.arange(len(data.columns)) + offset[1] / 2, data.columns, weight='bold', rotation=90)

    plt.savefig(output_save_path + scenario_output_str[1:] + "Covid_Age_Sex_Demographics_Cases.png")
    plt.close(fig1)
    csv_files_contain = 'Age_Gender_Demographics_Covid_deaths'
    file_names = []
    for file in os.listdir(folder_path):
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
    for idx, scenario in enumerate(scenarios):
        for idx2, sex in enumerate(['_m_', '_f_']):
            for file in file_names:
                if (scenario in file) & ('mean' in file) & (sex in file):
                    data = pd.read_csv(folder_path + file, index_col='day')
                    data = data.loc[:, ~data.columns.str.contains('Unnamed')]
                    axs[idx2].bar(np.arange(len(data.columns)) + offset[idx], data.sum(),
                                  label=scenario, width=(offset[-1] - offset[-2]))
                    total_deaths[scenario] = data.sum().sum()

    axs[0].legend()
    axs[1].legend()

    axs[0].set_xticks(np.arange(len(data.columns)) + offset[1] / 2, data.columns, weight='bold', rotation=90)
    axs[1].set_xticks(np.arange(len(data.columns)) + offset[1] / 2, data.columns, weight='bold', rotation=90)

    plt.savefig(output_save_path + scenario_output_str[1:] + "Covid_Age_Sex_Demographics_Deaths.png")
    plt.close(fig1)

    fig1 = plt.figure(figsize=(6, 6), constrained_layout=True)
    ax = plt.subplot(1, 1, 1)
    ax.bar(np.arange(len(total_cases.keys())), total_cases.values())
    ax.set_xticks(np.arange(len(total_cases.keys())), total_cases.keys(), weight='bold')
    ax.set_ylabel("Cases", weight='bold')
    ax.set_title("Total COVID-19 cases in each scenario", weight='bold')
    plt.savefig(output_save_path + scenario_output_str[1:] + "Total_Covid_Cases.png")
    plt.close(fig1)
    fig1 = plt.figure(figsize=(6, 6), constrained_layout=True)
    ax = plt.subplot(1, 1, 1)
    ax.bar(np.arange(len(total_deaths.keys())), total_deaths.values())
    ax.set_xticks(np.arange(len(total_deaths.keys())), total_deaths.keys(), weight='bold')
    ax.set_ylabel("Deaths", weight='bold')
    ax.set_title("Total COVID-19 Deaths in each scenario", weight='bold')
    plt.savefig(output_save_path + scenario_output_str[1:] + "Total_Covid_Deaths.png")
    plt.close(fig1)


if __name__ == "__main__":
    main()
