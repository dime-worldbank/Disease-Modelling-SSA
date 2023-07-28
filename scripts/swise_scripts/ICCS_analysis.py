import pandas as pd
import numpy as np
from matplotlib import pyplot as plt
import random
import geopandas
import os
import re
from PIL import Image, ImageDraw

# Compare the number of cases predicted by version 1 of the model with different population sizes
# 5 %
onedrive_file_path = "/Users/swise/OneDrive - University College London/data/"

graphsavepath = "/Users/swise/Downloads/ICCS_plots/"
onedrive_save = "/Users/swise/Downloads/onedrive_dump"

zimFile = "/Users/swise/workspace/worldbank/Disease-Modelling-SSA/data/raw/shapefiles/new_districts/ZWE_adm2.shp"


def average_output_from_runs(filepath, sample_size):
    storage_df = pd.DataFrame()
    for file in os.listdir(filepath):
        data = pd.read_csv(filepath + file, delimiter='\t')
        data = data.drop('Unnamed: 10', axis=1)
        storage_df = storage_df.append(data)

    # Scale the outputs
    storage_df *= 100 / sample_size
    storage_df['new_cases'] = storage_df['metric_new_cases_asympt'] + storage_df['metric_new_cases_sympt']
    storage_df_mean = storage_df.groupby('time').mean()
    storage_df_std = storage_df.groupby('time').std()
    return (storage_df_mean, storage_df_std)


def average_output_from_runs_v3(filepath, sample_size):
    storage_df = pd.DataFrame()
    for file in os.listdir(filepath):
        data = pd.read_csv(filepath + file, delimiter='\t')
        data = data.drop('Unnamed: 10', axis=1)
        data = data.groupby('time').sum()
        storage_df = storage_df.append(data)
    storage_df *= 100 / sample_size
    storage_df['new_cases'] = storage_df['metric_new_cases_asympt'] + storage_df['metric_new_cases_sympt']
    storage_df_mean = storage_df.groupby('time').mean()
    storage_df_std = storage_df.groupby('time').std()
    return (storage_df_mean, storage_df_std)


def sample_output_from_runs(filepath, sample_size, number_of_samples):
    storage_df = pd.DataFrame()
    for file in random.sample(os.listdir(filepath), number_of_samples):
        data = pd.read_csv(filepath + file, delimiter='\t')
        data = data.drop('Unnamed: 10', axis=1)
        storage_df = storage_df.append(data)

    storage_df['new_cases'] = storage_df['metric_new_cases_asympt'] + storage_df['metric_new_cases_sympt']
    mean_df = storage_df.groupby('time').mean()
    std_df = storage_df.groupby('time').std()
    # Scale the outputs
    mean_df *= 100 / sample_size
    std_df *= 100 / sample_size
    return mean_df, std_df


def iccs_graphs(remote_folder_path, savepath, zimfilepath):
    # get file paths to version 1 runs
    v1_5_output_file_path = remote_folder_path + \
                            "output/ICCS/extended_submission/ver_1 (singleDist)/5_perc/beta_0.3/output/"

    v1_5_outputs, v1_5_std = average_output_from_runs(v1_5_output_file_path, 5)
    v1_5_upper = v1_5_outputs.new_cases.values + (1.96 / np.sqrt(10)) * v1_5_std.new_cases.values
    v1_5_lower = v1_5_outputs.new_cases.values - (1.96 / np.sqrt(10)) * v1_5_std.new_cases.values
    v1_5_upper_deaths = v1_5_outputs.metric_new_deaths.values + \
                        (1.96 / np.sqrt(10)) * v1_5_std.metric_new_deaths.values
    v1_5_lower_deaths = v1_5_outputs.metric_new_deaths.values - \
                        (1.96 / np.sqrt(10)) * v1_5_std.metric_new_deaths.values
    # 10%
    v1_10_output_file_path = remote_folder_path + \
                             "output/ICCS/extended_submission/ver_1 (singleDist)/10_perc/beta_0.3/output/"
### SCW EDITS TO HERE
    v1_10_outputs = average_output_from_runs(v1_10_output_file_path, 10)
    v1_10_std = std_output_from_runs(v1_10_output_file_path, 10)
    v1_10_upper = v1_10_outputs.new_cases.values + (1.96 / np.sqrt(10)) * v1_10_std.new_cases.values
    v1_10_lower = v1_10_outputs.new_cases.values - (1.96 / np.sqrt(10)) * v1_10_std.new_cases.values

    v1_10_upper_deaths = v1_10_outputs.metric_new_deaths.values + \
                         (1.96 / np.sqrt(10)) * v1_10_std.metric_new_deaths.values
    v1_10_lower_deaths = v1_10_outputs.metric_new_deaths.values - \
                         (1.96 / np.sqrt(10)) * v1_10_std.metric_new_deaths.values
    # 25%
    v1_25_output_file_path = remote_folder_path + \
                             "output/ICCS/extended_submission/ver_1 (singleDist)/25_perc/beta_0.3/output/"
    v1_25_outputs = average_output_from_runs(v1_25_output_file_path, 25)
    v1_25_std = std_output_from_runs(v1_25_output_file_path, 25)
    v1_25_upper = v1_25_outputs.new_cases.values + (1.96 / np.sqrt(10)) * v1_25_std.new_cases.values
    v1_25_lower = v1_25_outputs.new_cases.values - (1.96 / np.sqrt(10)) * v1_25_std.new_cases.values
    v1_25_upper_deaths = v1_25_outputs.metric_new_deaths.values + \
                         (1.96 / np.sqrt(10)) * v1_25_std.metric_new_deaths.values
    v1_25_lower_deaths = v1_25_outputs.metric_new_deaths.values - \
                         (1.96 / np.sqrt(10)) * v1_25_std.metric_new_deaths.values
    # 50%
    v1_50_output_file_path = remote_folder_path + \
                             "output/ICCS/extended_submission/ver_1 (singleDist)/50_perc/beta_0.3/output/"

    v1_50_outputs = average_output_from_runs(v1_50_output_file_path, 50)
    v1_50_std = std_output_from_runs(v1_50_output_file_path, 50)
    v1_50_upper = v1_50_outputs.new_cases.values + (1.96 / np.sqrt(10)) * v1_50_std.new_cases.values
    v1_50_lower = v1_50_outputs.new_cases.values - (1.96 / np.sqrt(10)) * v1_50_std.new_cases.values
    v1_50_upper_deaths = v1_50_outputs.metric_new_deaths.values + \
                         (1.96 / np.sqrt(10)) * v1_50_std.metric_new_deaths.values
    v1_50_lower_deaths = v1_50_outputs.metric_new_deaths.values - \
                         (1.96 / np.sqrt(10)) * v1_50_std.metric_new_deaths.values
    # get file paths to version 3 runs
    # 5%
    v3_5_output_file_path = remote_folder_path + \
                            "output/ICCS/extended_submission/ver_3 (multiDist)/5_perc/beta_0.3/output/"

    v3_5_outputs = average_output_from_runs_v3(v3_5_output_file_path, 5)
    v3_5_std = std_output_from_runs_v3(v3_5_output_file_path, 5)
    v3_5_upper = v3_5_outputs.new_cases.values + (1.96 / np.sqrt(10)) * v3_5_std.new_cases.values
    v3_5_lower = v3_5_outputs.new_cases.values - (1.96 / np.sqrt(10)) * v3_5_std.new_cases.values
    v3_5_upper_deaths = v3_5_outputs.metric_new_deaths.values + \
                         (1.96 / np.sqrt(10)) * v3_5_std.metric_new_deaths.values
    v3_5_lower_deaths = v3_5_outputs.metric_new_deaths.values - \
                         (1.96 / np.sqrt(10)) * v3_5_std.metric_new_deaths.values
    # 10%
    v3_10_output_file_path = remote_folder_path + \
                            "output/ICCS/extended_submission/ver_3 (multiDist)/10_perc/beta_0.3/output/"

    v3_10_outputs = average_output_from_runs_v3(v3_10_output_file_path, 10)
    v3_10_std = std_output_from_runs_v3(v3_10_output_file_path, 10)
    v3_10_upper = v3_10_outputs.new_cases.values + (1.96 / np.sqrt(10)) * v3_10_std.new_cases.values
    v3_10_lower = v3_10_outputs.new_cases.values - (1.96 / np.sqrt(10)) * v3_10_std.new_cases.values
    v3_10_upper_deaths = v3_10_outputs.metric_new_deaths.values + \
                         (1.96 / np.sqrt(10)) * v3_10_std.metric_new_deaths.values
    v3_10_lower_deaths = v3_10_outputs.metric_new_deaths.values - \
                         (1.96 / np.sqrt(10)) * v3_10_std.metric_new_deaths.values
    # 25%
    v3_25_output_file_path = remote_folder_path + \
                             "output/ICCS/extended_submission/ver_3 (multiDist)/25_perc/beta_0.3/output/"

    v3_25_outputs = average_output_from_runs_v3(v3_25_output_file_path, 25)
    v3_25_std = std_output_from_runs_v3(v3_25_output_file_path, 25)
    v3_25_upper = v3_25_outputs.new_cases.values + (1.96 / np.sqrt(10)) * v3_25_std.new_cases.values
    v3_25_lower = v3_25_outputs.new_cases.values - (1.96 / np.sqrt(10)) * v3_25_std.new_cases.values
    v3_25_upper_deaths = v3_25_outputs.metric_new_deaths.values + \
                         (1.96 / np.sqrt(10)) * v3_25_std.metric_new_deaths.values
    v3_25_lower_deaths = v3_25_outputs.metric_new_deaths.values - \
                         (1.96 / np.sqrt(10)) * v3_25_std.metric_new_deaths.values
    # 50%
    v3_50_output_file_path = remote_folder_path + \
                             "output/ICCS/extended_submission/ver_3 (multiDist)/50_perc/beta_0.3/output/"

    v3_50_outputs = average_output_from_runs_v3(v3_50_output_file_path, 50)
    v3_50_std = std_output_from_runs_v3(v3_50_output_file_path, 50)
    v3_50_upper = v3_50_outputs.new_cases.values + (1.96 / np.sqrt(10)) * v3_50_std.new_cases.values
    v3_50_lower = v3_50_outputs.new_cases.values - (1.96 / np.sqrt(10)) * v3_50_std.new_cases.values
    v3_50_upper_deaths = v3_50_outputs.metric_new_deaths.values + \
                         (1.96 / np.sqrt(10)) * v3_50_std.metric_new_deaths.values
    v3_50_lower_deaths = v3_50_outputs.metric_new_deaths.values - \
                         (1.96 / np.sqrt(10)) * v3_50_std.metric_new_deaths.values
    fig = plt.figure(figsize=(20, 20), constrained_layout=True)
    (subfig1, subfig2, subfig3, subfig4) = fig.subfigures(1, 4)
    (ax11, ax21, ax31, ax41) = subfig1.subplots(4, 1)
    ax11.plot(v1_5_outputs.index, v1_5_outputs.new_cases, color='lightsteelblue')
    ax11.fill_between(v1_5_outputs.index, v1_5_lower, v1_5_upper, color='lightsteelblue', alpha=0.5)
    ax11.set_ylabel("Non-spatial, N. cases", weight='bold', fontsize=15)
    ax21.plot(v3_5_outputs.index, v3_5_outputs.new_cases, color='lightsteelblue')
    ax21.fill_between(v3_5_outputs.index, v3_5_lower, v3_5_upper, color='lightsteelblue', alpha=0.5)
    ax21.set_ylabel("Spatial, N. cases", weight='bold', fontsize=15)
    ax31.plot(v1_5_outputs.index, v1_5_outputs.metric_new_deaths, color='lightsalmon')
    ax31.fill_between(v1_5_outputs.index, v1_5_lower_deaths, v1_5_upper_deaths, color='lightsalmon', alpha=0.5)
    ax31.set_ylabel("Non-spatial, N. deaths", weight='bold', fontsize=15)
    ax41.plot(v3_5_outputs.index, v3_5_outputs.metric_new_deaths, color='lightsalmon')
    ax41.fill_between(v3_5_outputs.index, v3_5_lower_deaths, v3_5_upper_deaths, color='lightsalmon', alpha=0.5)
    ax41.set_ylabel("Spatial, N. deaths", weight='bold', fontsize=15)
    subfig1.suptitle('5%', weight='bold', fontsize=25)
    (ax12, ax22, ax32, ax42) = subfig2.subplots(4, 1)
    ax12.plot(v1_10_outputs.index, v1_10_outputs.new_cases, color='lightsteelblue')
    ax12.fill_between(v1_10_outputs.index, v1_10_lower, v1_10_upper, color='lightsteelblue', alpha=0.5)
    ax22.plot(v3_10_outputs.index, v3_10_outputs.new_cases, color='lightsteelblue')
    ax22.fill_between(v3_10_outputs.index, v3_10_lower, v3_10_upper, color='lightsteelblue', alpha=0.5)
    ax32.plot(v1_10_outputs.index, v1_10_outputs.metric_new_deaths, color='lightsalmon')
    ax32.fill_between(v1_10_outputs.index, v1_10_lower_deaths, v1_10_upper_deaths, color='lightsalmon', alpha=0.5)
    ax42.plot(v3_10_outputs.index, v3_10_outputs.metric_new_deaths, color='lightsalmon')
    ax42.fill_between(v3_10_outputs.index, v3_10_lower_deaths, v3_10_upper_deaths, color='lightsalmon', alpha=0.5)
    subfig2.suptitle('10%', weight='bold', fontsize=25)
    (ax13, ax23, ax33, ax43) = subfig3.subplots(4, 1)
    ax13.plot(v1_25_outputs.index, v1_25_outputs.new_cases, color='lightsteelblue')
    ax13.fill_between(v1_25_outputs.index, v1_25_lower, v1_25_upper, color='lightsteelblue', alpha=0.5)
    ax23.plot(v3_25_outputs.index, v3_25_outputs.new_cases, color='lightsteelblue')
    ax23.fill_between(v3_25_outputs.index, v3_25_lower, v3_25_upper, color='lightsteelblue', alpha=0.5)
    ax33.plot(v1_25_outputs.index, v1_25_outputs.metric_new_deaths, color='lightsalmon')
    ax33.fill_between(v1_25_outputs.index, v1_25_lower_deaths, v1_25_upper_deaths, color='lightsalmon', alpha=0.5)
    ax43.plot(v3_25_outputs.index, v3_25_outputs.metric_new_deaths, color='lightsalmon')
    ax43.fill_between(v3_25_outputs.index, v3_25_lower_deaths, v3_25_upper_deaths, color='lightsalmon', alpha=0.5)
    subfig3.suptitle('25%', weight='bold', fontsize=25)
    (ax14, ax24, ax34, ax44) = subfig4.subplots(4, 1)
    ax14.plot(v1_50_outputs.index, v1_50_outputs.new_cases, color='lightsteelblue')
    ax14.fill_between(v1_50_outputs.index, v1_50_lower, v1_50_upper, color='lightsteelblue', alpha=0.5)
    ax24.plot(v3_50_outputs.index, v3_50_outputs.new_cases, color='lightsteelblue')
    ax24.fill_between(v3_50_outputs.index, v3_50_lower, v3_50_upper, color='lightsteelblue', alpha=0.5)
    ax34.plot(v1_50_outputs.index, v1_50_outputs.metric_new_deaths, color='lightsalmon')
    ax34.fill_between(v1_50_outputs.index, v1_50_lower_deaths, v1_50_upper_deaths, color='lightsalmon', alpha=0.5)
    ax44.plot(v3_50_outputs.index, v3_50_outputs.metric_new_deaths, color='lightsalmon')
    ax44.fill_between(v3_50_outputs.index, v3_50_lower_deaths, v3_50_upper_deaths, color='lightsalmon', alpha=0.5)
    subfig4.suptitle('50%', weight='bold', fontsize=25)
    fig.suptitle('Variation shown in the model runs for cases and deaths\nfor different sample sizes\n', fontsize=30)
    fig.supxlabel('Time', fontsize=40)
    # plt.subplots_adjust(left=0.2)
    plt.savefig(savepath + "cases_deaths_and_confidence_intervals.png", dpi=400, bbox_inches='tight')
    plt.close(fig)
    plt.clf()

    fig = plt.figure(figsize=(12, 18))
    plt.subplot(4, 2, 1)
    plt.plot(v1_5_outputs.index, v1_5_outputs.new_cases, color='r', label='5%', alpha=0.5)
    plt.plot(v1_10_outputs.index, v1_10_outputs.new_cases, color='b', label='10%', alpha=0.5)
    plt.plot(v1_25_outputs.index, v1_25_outputs.new_cases, color='c', label='25%', alpha=0.5)
    plt.plot(v1_50_outputs.index, v1_50_outputs.new_cases, color='y', label='50%', alpha=0.5)
    plt.xlabel('time')
    plt.ylabel('New cases')
    plt.title('Daily New Cases, with \nDemographics (Version 1)')
    plt.legend()
    plt.subplot(4, 2, 2)
    plt.plot(v3_5_outputs.index, v3_5_outputs.new_cases, color='r', label='5%', alpha=0.5)
    plt.plot(v3_10_outputs.index, v3_10_outputs.new_cases, color='b', label='10%', alpha=0.5)
    plt.plot(v3_25_outputs.index, v3_25_outputs.new_cases, color='c', label='25%', alpha=0.5)
    plt.plot(v3_50_outputs.index, v3_50_outputs.new_cases, color='y', label='50%', alpha=0.5)
    plt.xlabel('time')
    plt.ylabel('New cases')
    plt.title('Daily New Cases, with Demographics and \nSpatial Mobility (Version 3)')
    plt.legend()
    plt.subplot(4, 2, 3)
    plt.plot(v1_5_outputs.index[:30], v1_5_outputs.new_cases[:30], color='r', label='5%', alpha=0.5)
    plt.plot(v1_10_outputs.index[:30], v1_10_outputs.new_cases[:30], color='b', label='10%', alpha=0.5)
    plt.plot(v1_25_outputs.index[:30], v1_25_outputs.new_cases[:30], color='c', label='25%', alpha=0.5)
    plt.plot(v1_50_outputs.index[:30], v1_50_outputs.new_cases[:30], color='y', label='50%', alpha=0.5)
    plt.xlabel('time')
    plt.ylabel('New cases')
    plt.title('Daily New Cases, with \nDemographics (Version 1)')
    plt.legend()
    plt.subplot(4, 2, 4)
    plt.plot(v3_5_outputs.index[:30], v3_5_outputs.new_cases[:30], color='r', label='5%', alpha=0.5)
    plt.plot(v3_10_outputs.index[:30], v3_10_outputs.new_cases[:30], color='b', label='10%', alpha=0.5)
    plt.plot(v3_25_outputs.index[:30], v3_25_outputs.new_cases[:30], color='c', label='25%', alpha=0.5)
    plt.plot(v3_50_outputs.index[:30], v3_50_outputs.new_cases[:30], color='y', label='50%', alpha=0.5)
    plt.xlabel('time')
    plt.ylabel('New cases')
    plt.title('Daily New Cases, with Demographics and \nSpatial Mobility (Version 3)')
    plt.legend()
    plt.subplot(4, 2, 5)
    v1_5_outputs['cumulative_cases'] = np.cumsum(v1_5_outputs.new_cases)
    v1_10_outputs['cumulative_cases'] = np.cumsum(v1_10_outputs.new_cases)
    v1_25_outputs['cumulative_cases'] = np.cumsum(v1_25_outputs.new_cases)
    v1_50_outputs['cumulative_cases'] = np.cumsum(v1_50_outputs.new_cases)
    ratio_v1_5_10 = list(np.divide(v1_5_outputs.cumulative_cases, v1_10_outputs.cumulative_cases))
    ratio_v1_5_25 = list(np.divide(v1_5_outputs.cumulative_cases, v1_25_outputs.cumulative_cases))
    ratio_v1_5_50 = list(np.divide(v1_5_outputs.cumulative_cases, v1_50_outputs.cumulative_cases))
    # ratio_v1_5_10 = list(np.divide(v1_10_outputs.cumulative_cases, v1_5_outputs.cumulative_cases))
    # ratio_v1_5_25 = list(np.divide(v1_25_outputs.cumulative_cases, v1_5_outputs.cumulative_cases))
    # ratio_v1_5_50 = list(np.divide(v1_50_outputs.cumulative_cases, v1_5_outputs.cumulative_cases))
    plt.plot(np.arange(len(ratio_v1_5_10)), ratio_v1_5_10, label='5%:10%', color='r')
    plt.plot(np.arange(len(ratio_v1_5_25)), ratio_v1_5_25, label='5%:25%', color='b')
    plt.plot(np.arange(len(ratio_v1_5_50)), ratio_v1_5_50, label='5%:50%', color='c')
    plt.xlabel('time')
    plt.ylabel('Ratio')
    plt.title('Ratio of Cumulative cases between 5% samples and \n10%, 25% and 50% Samples,\n with Demographics '
              '(Version 1)')
    plt.legend()
    # plt.subplots_adjust(top=0.8)
    plt.subplot(4, 2, 6)
    v3_5_outputs['cumulative_cases'] = np.cumsum(v3_5_outputs.new_cases)
    v3_10_outputs['cumulative_cases'] = np.cumsum(v3_10_outputs.new_cases)
    v3_25_outputs['cumulative_cases'] = np.cumsum(v3_25_outputs.new_cases)
    v3_50_outputs['cumulative_cases'] = np.cumsum(v3_50_outputs.new_cases)
    ratio_v3_5_10 = list(np.divide(v3_5_outputs.cumulative_cases, v3_10_outputs.cumulative_cases))
    ratio_v3_5_25 = list(np.divide(v3_5_outputs.cumulative_cases, v3_25_outputs.cumulative_cases))
    ratio_v3_5_50 = list(np.divide(v3_5_outputs.cumulative_cases, v3_50_outputs.cumulative_cases))
    # ratio_v3_5_10 = list(np.divide(v3_10_outputs.cumulative_cases, v3_5_outputs.cumulative_cases))
    # ratio_v3_5_25 = list(np.divide(v3_25_outputs.cumulative_cases, v3_5_outputs.cumulative_cases))
    # ratio_v3_5_50 = list(np.divide(v3_50_outputs.cumulative_cases, v3_5_outputs.cumulative_cases))
    plt.plot(np.arange(len(ratio_v3_5_10)), ratio_v3_5_10, label='5%:10%', color='r')
    plt.plot(np.arange(len(ratio_v3_5_25)), ratio_v3_5_25, label='5%:25%', color='b')
    plt.plot(np.arange(len(ratio_v3_5_50)), ratio_v3_5_50, label='5%:50%', color='c')
    plt.xlabel('time')
    plt.ylabel('Ratio')
    plt.title('Ratio of Cumulative cases between 5% samples and \n10%, 25% and 50% Samples,\n with Demographics and'
              'spatial Mobility (Version 3)')
    plt.legend()
    # plt.subplots_adjust(top=0.8)
    plt.subplot(4, 2, 7)
    plt.plot(v1_5_outputs.index, v1_5_outputs.metric_new_deaths, color='r', label='5%')
    plt.plot(v1_10_outputs.index, v1_10_outputs.metric_new_deaths, color='b', label='10%')
    plt.plot(v1_25_outputs.index, v1_25_outputs.metric_new_deaths, color='c', label='25%')
    plt.plot(v1_50_outputs.index, v1_50_outputs.metric_new_deaths, color='y', label='50%')
    plt.xlabel('time')
    plt.ylabel('New deaths')
    plt.title('Daily New Deaths, with \nDemographics (Version 1)')
    plt.legend()
    plt.subplot(4, 2, 8)
    plt.plot(v3_5_outputs.index, v3_5_outputs.metric_new_deaths, color='r', label='5%')
    plt.plot(v3_10_outputs.index, v3_10_outputs.metric_new_deaths, color='b', label='10%')
    plt.plot(v3_25_outputs.index, v3_25_outputs.metric_new_deaths, color='c', label='25%')
    plt.plot(v3_50_outputs.index, v3_50_outputs.metric_new_deaths, color='y', label='50%')
    plt.xlabel('time')
    plt.ylabel('New deaths')
    plt.title('Daily New Deaths, with Demographics and \nSpatial Mobility (Version 3)')
    plt.legend()
    plt.subplots_adjust(hspace=0.4, wspace=0.25)
    plt.savefig(savepath + "figure_1.png", dpi=400, bbox_inches='tight', pad_inches=0.5)
    plt.clf()
    plt.close(fig)
    v1_5_mean, v1_5_std = sample_output_from_runs(v1_5_output_file_path, 5, 10)
    v1_10_mean, v1_10_std = sample_output_from_runs(v1_10_output_file_path, 10, 10)
    v1_25_mean, v1_25_std = sample_output_from_runs(v1_25_output_file_path, 25, 10)
    v1_50_mean, v1_50_std = sample_output_from_runs(v1_50_output_file_path, 50, 10)
    v3_5_mean, v3_5_std = sample_output_from_runs(v3_5_output_file_path, 5, 10)
    v3_10_mean, v3_10_std = sample_output_from_runs(v3_10_output_file_path, 10, 10)
    v3_25_mean, v3_25_std = sample_output_from_runs(v3_25_output_file_path, 25, 10)
    v3_50_mean, v3_50_std = sample_output_from_runs(v3_50_output_file_path, 50, 10)

    plt.plot(v1_5_outputs.index, v1_5_outputs.new_cases, color='r', label='5%', alpha=0.5)
    plt.plot(v1_10_outputs.index, v1_10_outputs.new_cases, color='b', label='10%', alpha=0.5)
    plt.plot(v1_25_outputs.index, v1_25_outputs.new_cases, color='c', label='25%', alpha=0.5)
    plt.plot(v1_50_outputs.index, v1_50_outputs.new_cases, color='y', label='50%', alpha=0.5)
    plt.xlabel('time')
    plt.ylabel('New cases')
    plt.title('Daily New Cases, with \nDemographics (Version 1)')
    plt.legend()
    plt.savefig(savepath + "v1_cases.png", dpi=400)
    plt.clf()
    plt.plot(v1_5_outputs.index, v1_5_outputs.new_cases, color='r', label='5%', alpha=0.5)
    plt.plot(v1_10_outputs.index, v1_10_outputs.new_cases, color='b', label='10%', alpha=0.5)
    plt.plot(v1_25_outputs.index, v1_25_outputs.new_cases, color='c', label='25%', alpha=0.5)
    plt.plot(v1_50_outputs.index, v1_50_outputs.new_cases, color='y', label='50%', alpha=0.5)
    plt.xlabel('time')
    plt.ylabel('New cases')
    plt.title('Daily New Cases, with \nDemographics (Version 1)')
    plt.yscale('log')
    plt.legend()
    plt.savefig(savepath + "v1_cases_log.png", dpi=400)
    plt.clf()
    plt.plot(v3_5_outputs.index, v3_5_outputs.new_cases, color='r', label='5%', alpha=0.5)
    plt.plot(v3_10_outputs.index, v3_10_outputs.new_cases, color='b', label='10%', alpha=0.5)
    plt.plot(v3_25_outputs.index, v3_25_outputs.new_cases, color='c', label='25%', alpha=0.5)
    plt.plot(v3_50_outputs.index, v3_50_outputs.new_cases, color='y', label='50%', alpha=0.5)
    plt.xlabel('time')
    plt.ylabel('New cases')
    plt.title('Daily New Cases, with Demographics and \nSpatial Mobility (Version 3)')
    plt.legend()
    plt.savefig(savepath + "v3_cases.png", dpi=400)
    plt.clf()
    plt.plot(v3_5_outputs.index, v3_5_outputs.new_cases, color='r', label='5%', alpha=0.5)
    plt.plot(v3_10_outputs.index, v3_10_outputs.new_cases, color='b', label='10%', alpha=0.5)
    plt.plot(v3_25_outputs.index, v3_25_outputs.new_cases, color='c', label='25%', alpha=0.5)
    plt.plot(v3_50_outputs.index, v3_50_outputs.new_cases, color='y', label='50%', alpha=0.5)
    plt.xlabel('time')
    plt.ylabel('New cases')
    plt.title('Daily New Cases, with Demographics and \nSpatial Mobility (Version 3)')
    plt.legend()
    plt.yscale('log')
    plt.savefig(savepath + "v3_cases_log.png", dpi=400)
    plt.clf()
    plt.plot(v1_5_outputs.index[:30], v1_5_outputs.new_cases[:30], color='r', label='5%', alpha=0.5)
    plt.plot(v1_10_outputs.index[:30], v1_10_outputs.new_cases[:30], color='b', label='10%', alpha=0.5)
    plt.plot(v1_25_outputs.index[:30], v1_25_outputs.new_cases[:30], color='c', label='25%', alpha=0.5)
    plt.plot(v1_50_outputs.index[:30], v1_50_outputs.new_cases[:30], color='y', label='50%', alpha=0.5)
    plt.xlabel('time')
    plt.ylabel('New cases')
    plt.title('Daily New Cases, with \nDemographics (Version 1)')
    plt.legend()
    plt.savefig(savepath + "v1_cases_first_30.png", dpi=400)
    plt.clf()
    plt.plot(v3_5_outputs.index[:30], v3_5_outputs.new_cases[:30], color='r', label='5%', alpha=0.5)
    plt.plot(v3_10_outputs.index[:30], v3_10_outputs.new_cases[:30], color='b', label='10%', alpha=0.5)
    plt.plot(v3_25_outputs.index[:30], v3_25_outputs.new_cases[:30], color='c', label='25%', alpha=0.5)
    plt.plot(v3_50_outputs.index[:30], v3_50_outputs.new_cases[:30], color='y', label='50%', alpha=0.5)
    plt.xlabel('time')
    plt.ylabel('New cases')
    plt.title('Daily New Cases, with Demographics and \nSpatial Mobility (Version 3)')
    plt.legend()
    plt.savefig(savepath + "v3_cases_first_30.png", dpi=400)
    plt.clf()
    v1_5_outputs['cumulative_cases'] = np.cumsum(v1_5_outputs.new_cases)
    v1_10_outputs['cumulative_cases'] = np.cumsum(v1_10_outputs.new_cases)
    v1_25_outputs['cumulative_cases'] = np.cumsum(v1_25_outputs.new_cases)
    v1_50_outputs['cumulative_cases'] = np.cumsum(v1_50_outputs.new_cases)
    ratio_v1_5_10 = list(np.divide(v1_5_outputs.cumulative_cases, v1_10_outputs.cumulative_cases))
    ratio_v1_5_25 = list(np.divide(v1_5_outputs.cumulative_cases, v1_25_outputs.cumulative_cases))
    ratio_v1_5_50 = list(np.divide(v1_5_outputs.cumulative_cases, v1_50_outputs.cumulative_cases))
    plt.plot(np.arange(len(ratio_v1_5_10)), ratio_v1_5_10, label='5%:10%', color='r')
    plt.plot(np.arange(len(ratio_v1_5_25)), ratio_v1_5_25, label='5%:25%', color='b')
    plt.plot(np.arange(len(ratio_v1_5_50)), ratio_v1_5_50, label='5%:50%', color='c')
    plt.xlabel('time')
    plt.ylabel('Ratio')
    plt.title('Ratio of Cumulative cases between 5% samples and \n10%, 25% and 50% Samples,\n with Demographics '
              '(Version 1)')
    plt.legend()
    plt.savefig(savepath + "v1_cumulative_cases_ratio.png", dpi=400)
    plt.clf()
    v3_5_outputs['cumulative_cases'] = np.cumsum(v3_5_outputs.new_cases)
    v3_10_outputs['cumulative_cases'] = np.cumsum(v3_10_outputs.new_cases)
    v3_25_outputs['cumulative_cases'] = np.cumsum(v3_25_outputs.new_cases)
    v3_50_outputs['cumulative_cases'] = np.cumsum(v3_50_outputs.new_cases)
    ratio_v3_5_10 = list(np.divide(v3_5_outputs.cumulative_cases, v3_10_outputs.cumulative_cases))
    ratio_v3_5_25 = list(np.divide(v3_5_outputs.cumulative_cases, v3_25_outputs.cumulative_cases))
    ratio_v3_5_50 = list(np.divide(v3_5_outputs.cumulative_cases, v3_50_outputs.cumulative_cases))
    plt.plot(np.arange(len(ratio_v3_5_10)), ratio_v3_5_10, label='5%:10%', color='r')
    plt.plot(np.arange(len(ratio_v3_5_25)), ratio_v3_5_25, label='5%:25%', color='b')
    plt.plot(np.arange(len(ratio_v3_5_50)), ratio_v3_5_50, label='5%:50%', color='c')
    plt.xlabel('time')
    plt.ylabel('Ratio')
    plt.title('Ratio of Cumulative cases between 5% samples and \n10%, 25% and 50% Samples,\n with Demographics and'
              'spatial Mobility (Version 3)')
    plt.legend()
    plt.savefig(savepath + "v3_cumulative_cases_ratio.png", dpi=400)
    plt.clf()
    plt.plot(v1_5_outputs.index, v1_5_outputs.metric_new_deaths, color='r', label='5%')
    plt.plot(v1_10_outputs.index, v1_10_outputs.metric_new_deaths, color='b', label='10%')
    plt.plot(v1_25_outputs.index, v1_25_outputs.metric_new_deaths, color='c', label='25%')
    plt.plot(v1_50_outputs.index, v1_50_outputs.metric_new_deaths, color='y', label='50%')
    plt.xlabel('time')
    plt.ylabel('New deaths')
    plt.title('Daily New Deaths, with \nDemographics (Version 1)')
    plt.legend()
    plt.savefig(savepath + "v1_deaths.png", dpi=400)
    plt.clf()
    plt.plot(v1_5_outputs.index, v1_5_outputs.metric_new_deaths, color='r', label='5%')
    plt.plot(v1_10_outputs.index, v1_10_outputs.metric_new_deaths, color='b', label='10%')
    plt.plot(v1_25_outputs.index, v1_25_outputs.metric_new_deaths, color='c', label='25%')
    plt.plot(v1_50_outputs.index, v1_50_outputs.metric_new_deaths, color='y', label='50%')
    plt.xlabel('time')
    plt.ylabel('New deaths')
    plt.title('Daily New Deaths, with \nDemographics (Version 1)')
    plt.yscale('log')
    plt.legend()
    plt.savefig(savepath + "v1_deaths_log.png", dpi=400)
    plt.clf()
    plt.plot(v3_5_outputs.index, v3_5_outputs.metric_new_deaths, color='r', label='5%')
    plt.plot(v3_10_outputs.index, v3_10_outputs.metric_new_deaths, color='b', label='10%')
    plt.plot(v3_25_outputs.index, v3_25_outputs.metric_new_deaths, color='c', label='25%')
    plt.plot(v3_50_outputs.index, v3_50_outputs.metric_new_deaths, color='y', label='50%')
    plt.xlabel('time')
    plt.ylabel('New deaths')
    plt.title('Daily New Deaths, with Demographics and \nSpatial Mobility (Version 3)')
    plt.legend()
    plt.savefig(savepath + "v3_deaths.png", dpi=400)
    plt.clf()
    plt.plot(v3_5_outputs.index, v3_5_outputs.metric_new_deaths, color='r', label='5%')
    plt.plot(v3_10_outputs.index, v3_10_outputs.metric_new_deaths, color='b', label='10%')
    plt.plot(v3_25_outputs.index, v3_25_outputs.metric_new_deaths, color='c', label='25%')
    plt.plot(v3_50_outputs.index, v3_50_outputs.metric_new_deaths, color='y', label='50%')
    plt.xlabel('time')
    plt.ylabel('New deaths')
    plt.title('Daily New Deaths, with Demographics and \nSpatial Mobility (Version 3)')
    plt.legend()
    plt.yscale('log')
    plt.savefig(savepath + "v3_deaths_log.png", dpi=400)
    plt.clf()
    # Create a plot of all the runs at different time intervals
    fig = plt.figure(figsize=(12, 18))
    # 5% and 50%
    plt.subplot(4, 2, 1)
    for file in os.listdir(v3_5_output_file_path):
        data = pd.read_csv(v3_5_output_file_path + file, delimiter='\t')
        data = data.drop('Unnamed: 10', axis=1)
        data = data.groupby('time').sum()
        data *= 100 / 5
        data['new_cases'] = data['metric_new_cases_asympt'] + data['metric_new_cases_sympt']
        plt.plot(data.index[:10], data['new_cases'][:10], color='b', alpha=0.3)
    for file in os.listdir(v1_5_output_file_path):
        data = pd.read_csv(v1_5_output_file_path + file, delimiter='\t')
        data = data.drop('Unnamed: 10', axis=1)
        data = data.groupby('time').sum()
        data *= 100 / 5
        data['new_cases'] = data['metric_new_cases_asympt'] + data['metric_new_cases_sympt']
        plt.plot(data.index[:10], data['new_cases'][:10], color='r', alpha=0.3)
    plt.xlabel('Time')
    plt.ylabel('New cases')
    plt.title('First 10 days')
    plt.subplot(4, 2, 2)
    for file in os.listdir(v3_5_output_file_path):
        data = pd.read_csv(v3_5_output_file_path + file, delimiter='\t')
        data = data.drop('Unnamed: 10', axis=1)
        data = data.groupby('time').sum()
        data *= 100 / 5
        data['new_cases'] = data['metric_new_cases_asympt'] + data['metric_new_cases_sympt']
        plt.plot(data.index[:30], data['new_cases'][:30], color='b', alpha=0.3)
    for file in os.listdir(v1_5_output_file_path):
        data = pd.read_csv(v1_5_output_file_path + file, delimiter='\t')
        data = data.drop('Unnamed: 10', axis=1)
        data = data.groupby('time').sum()
        data *= 100 / 5
        data['new_cases'] = data['metric_new_cases_asympt'] + data['metric_new_cases_sympt']
        plt.plot(data.index[:30], data['new_cases'][:30], color='r', alpha=0.3)
    plt.xlabel('Time')
    plt.ylabel('New cases')
    plt.title('First 30 days')
    plt.subplot(4, 2, 3)
    for file in os.listdir(v3_5_output_file_path):
        data = pd.read_csv(v3_5_output_file_path + file, delimiter='\t')
        data = data.drop('Unnamed: 10', axis=1)
        data = data.groupby('time').sum()
        data *= 100 / 5
        data['new_cases'] = data['metric_new_cases_asympt'] + data['metric_new_cases_sympt']
        plt.plot(data.index[:60], data['new_cases'][:60], color='b', alpha=0.3)
    for file in os.listdir(v1_5_output_file_path):
        data = pd.read_csv(v1_5_output_file_path + file, delimiter='\t')
        data = data.drop('Unnamed: 10', axis=1)
        data = data.groupby('time').sum()
        data *= 100 / 5
        data['new_cases'] = data['metric_new_cases_asympt'] + data['metric_new_cases_sympt']
        plt.plot(data.index[:60], data['new_cases'][:60], color='r', alpha=0.3)
    plt.xlabel('Time')
    plt.ylabel('New cases')
    plt.title('First 60 days')
    plt.subplot(4, 2, 4)
    for file in os.listdir(v3_5_output_file_path):
        data = pd.read_csv(v3_5_output_file_path + file, delimiter='\t')
        data = data.drop('Unnamed: 10', axis=1)
        data = data.groupby('time').sum()
        data *= 100 / 50
        data['new_cases'] = data['metric_new_cases_asympt'] + data['metric_new_cases_sympt']
        plt.plot(data.index, data['new_cases'], color='b', alpha=0.3)
    for file in os.listdir(v1_5_output_file_path):
        data = pd.read_csv(v1_5_output_file_path + file, delimiter='\t')
        data = data.drop('Unnamed: 10', axis=1)
        data = data.groupby('time').sum()
        data *= 100 / 50
        data['new_cases'] = data['metric_new_cases_asympt'] + data['metric_new_cases_sympt']
        plt.plot(data.index, data['new_cases'], color='r', alpha=0.3)
    plt.xlabel('Time')
    plt.ylabel('New cases')
    plt.title('First 100 days')
    plt.subplot(4, 2, 5)
    for file in os.listdir(v3_50_output_file_path):
        data = pd.read_csv(v3_50_output_file_path + file, delimiter='\t')
        data = data.drop('Unnamed: 10', axis=1)
        data = data.groupby('time').sum()
        data *= 100 / 50
        data['new_cases'] = data['metric_new_cases_asympt'] + data['metric_new_cases_sympt']
        plt.plot(data.index[:10], data['new_cases'][:10], color='b', alpha=0.3)
    for file in os.listdir(v1_50_output_file_path):
        data = pd.read_csv(v1_50_output_file_path + file, delimiter='\t')
        data = data.drop('Unnamed: 10', axis=1)
        data = data.groupby('time').sum()
        data *= 100 / 50
        data['new_cases'] = data['metric_new_cases_asympt'] + data['metric_new_cases_sympt']
        plt.plot(data.index[:10], data['new_cases'][:10], color='r', alpha=0.3)
    plt.xlabel('Time')
    plt.ylabel('New cases')
    plt.title('First 10 days')
    plt.subplot(4, 2, 6)
    for file in os.listdir(v3_50_output_file_path):
        data = pd.read_csv(v3_50_output_file_path + file, delimiter='\t')
        data = data.drop('Unnamed: 10', axis=1)
        data = data.groupby('time').sum()
        data *= 100 / 50
        data['new_cases'] = data['metric_new_cases_asympt'] + data['metric_new_cases_sympt']
        plt.plot(data.index[:30], data['new_cases'][:30], color='b', alpha=0.3)
    for file in os.listdir(v1_50_output_file_path):
        data = pd.read_csv(v1_50_output_file_path + file, delimiter='\t')
        data = data.drop('Unnamed: 10', axis=1)
        data = data.groupby('time').sum()
        data *= 100 / 50
        data['new_cases'] = data['metric_new_cases_asympt'] + data['metric_new_cases_sympt']
        plt.plot(data.index[:30], data['new_cases'][:30], color='r', alpha=0.3)
    plt.xlabel('Time')
    plt.ylabel('New cases')
    plt.title('First 30 days')
    plt.subplot(4, 2, 7)
    for file in os.listdir(v3_50_output_file_path):
        data = pd.read_csv(v3_50_output_file_path + file, delimiter='\t')
        data = data.drop('Unnamed: 10', axis=1)
        data = data.groupby('time').sum()
        data *= 100 / 50
        data['new_cases'] = data['metric_new_cases_asympt'] + data['metric_new_cases_sympt']
        plt.plot(data.index[:60], data['new_cases'][:60], color='b', alpha=0.3)
    for file in os.listdir(v1_50_output_file_path):
        data = pd.read_csv(v1_50_output_file_path + file, delimiter='\t')
        data = data.drop('Unnamed: 10', axis=1)
        data = data.groupby('time').sum()
        data *= 100 / 50
        data['new_cases'] = data['metric_new_cases_asympt'] + data['metric_new_cases_sympt']
        plt.plot(data.index[:60], data['new_cases'][:60], color='r', alpha=0.3)
    plt.xlabel('Time')
    plt.ylabel('New cases')
    plt.title('First 60 days')
    plt.subplot(4, 2, 8)
    for file in os.listdir(v3_50_output_file_path):
        data = pd.read_csv(v3_50_output_file_path + file, delimiter='\t')
        data = data.drop('Unnamed: 10', axis=1)
        data = data.groupby('time').sum()
        data *= 100 / 50
        data['new_cases'] = data['metric_new_cases_asympt'] + data['metric_new_cases_sympt']
        plt.plot(data.index, data['new_cases'], color='b', label='Spatial', alpha=0.3)
    for file in os.listdir(v1_50_output_file_path):
        data = pd.read_csv(v1_50_output_file_path + file, delimiter='\t')
        data = data.drop('Unnamed: 10', axis=1)
        data = data.groupby('time').sum()
        data *= 100 / 50
        data['new_cases'] = data['metric_new_cases_asympt'] + data['metric_new_cases_sympt']
        plt.plot(data.index, data['new_cases'], color='r', label='Non-spatial', alpha=0.3)
    plt.xlabel('Time')
    plt.ylabel('New cases')
    plt.title('First 100 days')
    plt.subplots_adjust(hspace=0.4, wspace=0.25)
    handles, labels = plt.gca().get_legend_handles_labels()
    by_label = dict(zip(labels, handles))
    fig.legend(by_label.values(), by_label.keys(), bbox_to_anchor=(1, 0.9))
    plt.savefig(savepath + "figure_2_5_50.png", dpi=400, bbox_inches='tight', pad_inches=0.5)
    plt.clf()
    plt.close(fig)
    # Create a plot of all the runs at different time intervals
    fig = plt.figure(figsize=(12, 18))
    # 5% and 25%
    plt.subplot(4, 2, 1)
    for file in os.listdir(v3_5_output_file_path):
        data = pd.read_csv(v3_5_output_file_path + file, delimiter='\t')
        data = data.drop('Unnamed: 10', axis=1)
        data = data.groupby('time').sum()
        data *= 100 / 5
        data['new_cases'] = data['metric_new_cases_asympt'] + data['metric_new_cases_sympt']
        plt.plot(data.index[:10], data['new_cases'][:10], color='b', alpha=0.3)
    for file in os.listdir(v1_5_output_file_path):
        data = pd.read_csv(v1_5_output_file_path + file, delimiter='\t')
        data = data.drop('Unnamed: 10', axis=1)
        data = data.groupby('time').sum()
        data *= 100 / 5
        data['new_cases'] = data['metric_new_cases_asympt'] + data['metric_new_cases_sympt']
        plt.plot(data.index[:10], data['new_cases'][:10], color='r', alpha=0.3)
    plt.xlabel('Time')
    plt.ylabel('New cases')
    plt.title('First 10 days')
    plt.subplot(4, 2, 2)
    for file in os.listdir(v3_5_output_file_path):
        data = pd.read_csv(v3_5_output_file_path + file, delimiter='\t')
        data = data.drop('Unnamed: 10', axis=1)
        data = data.groupby('time').sum()
        data *= 100 / 5
        data['new_cases'] = data['metric_new_cases_asympt'] + data['metric_new_cases_sympt']
        plt.plot(data.index[:30], data['new_cases'][:30], color='b', alpha=0.3)
    for file in os.listdir(v1_5_output_file_path):
        data = pd.read_csv(v1_5_output_file_path + file, delimiter='\t')
        data = data.drop('Unnamed: 10', axis=1)
        data = data.groupby('time').sum()
        data *= 100 / 5
        data['new_cases'] = data['metric_new_cases_asympt'] + data['metric_new_cases_sympt']
        plt.plot(data.index[:30], data['new_cases'][:30], color='r', alpha=0.3)
    plt.xlabel('Time')
    plt.ylabel('New cases')
    plt.title('First 30 days')
    plt.subplot(4, 2, 3)
    for file in os.listdir(v3_5_output_file_path):
        data = pd.read_csv(v3_5_output_file_path + file, delimiter='\t')
        data = data.drop('Unnamed: 10', axis=1)
        data = data.groupby('time').sum()
        data *= 100 / 5
        data['new_cases'] = data['metric_new_cases_asympt'] + data['metric_new_cases_sympt']
        plt.plot(data.index[:60], data['new_cases'][:60], color='b', alpha=0.3)
    for file in os.listdir(v1_5_output_file_path):
        data = pd.read_csv(v1_5_output_file_path + file, delimiter='\t')
        data = data.drop('Unnamed: 10', axis=1)
        data = data.groupby('time').sum()
        data *= 100 / 5
        data['new_cases'] = data['metric_new_cases_asympt'] + data['metric_new_cases_sympt']
        plt.plot(data.index[:60], data['new_cases'][:60], color='r', alpha=0.3)
    plt.xlabel('Time')
    plt.ylabel('New cases')
    plt.title('First 60 days')
    plt.subplot(4, 2, 4)
    for file in os.listdir(v3_5_output_file_path):
        data = pd.read_csv(v3_5_output_file_path + file, delimiter='\t')
        data = data.drop('Unnamed: 10', axis=1)
        data = data.groupby('time').sum()
        data *= 100 / 50
        data['new_cases'] = data['metric_new_cases_asympt'] + data['metric_new_cases_sympt']
        plt.plot(data.index, data['new_cases'], color='b', alpha=0.3)
    for file in os.listdir(v1_5_output_file_path):
        data = pd.read_csv(v1_5_output_file_path + file, delimiter='\t')
        data = data.drop('Unnamed: 10', axis=1)
        data = data.groupby('time').sum()
        data *= 100 / 50
        data['new_cases'] = data['metric_new_cases_asympt'] + data['metric_new_cases_sympt']
        plt.plot(data.index, data['new_cases'], color='r', alpha=0.3)
    plt.xlabel('Time')
    plt.ylabel('New cases')
    plt.title('First 100 days')
    plt.subplot(4, 2, 5)
    for file in os.listdir(v3_25_output_file_path):
        data = pd.read_csv(v3_25_output_file_path + file, delimiter='\t')
        data = data.drop('Unnamed: 10', axis=1)
        data = data.groupby('time').sum()
        data *= 100 / 25
        data['new_cases'] = data['metric_new_cases_asympt'] + data['metric_new_cases_sympt']
        plt.plot(data.index[:10], data['new_cases'][:10], color='b', alpha=0.3)
    for file in os.listdir(v1_25_output_file_path):
        data = pd.read_csv(v1_25_output_file_path + file, delimiter='\t')
        data = data.drop('Unnamed: 10', axis=1)
        data = data.groupby('time').sum()
        data *= 100 / 25
        data['new_cases'] = data['metric_new_cases_asympt'] + data['metric_new_cases_sympt']
        plt.plot(data.index[:10], data['new_cases'][:10], color='r', alpha=0.3)
    plt.xlabel('Time')
    plt.ylabel('New cases')
    plt.title('First 10 days')
    plt.subplot(4, 2, 6)
    for file in os.listdir(v3_25_output_file_path):
        data = pd.read_csv(v3_25_output_file_path + file, delimiter='\t')
        data = data.drop('Unnamed: 10', axis=1)
        data = data.groupby('time').sum()
        data *= 100 / 25
        data['new_cases'] = data['metric_new_cases_asympt'] + data['metric_new_cases_sympt']
        plt.plot(data.index[:30], data['new_cases'][:30], color='b', alpha=0.3)
    for file in os.listdir(v1_25_output_file_path):
        data = pd.read_csv(v1_25_output_file_path + file, delimiter='\t')
        data = data.drop('Unnamed: 10', axis=1)
        data = data.groupby('time').sum()
        data *= 100 / 25
        data['new_cases'] = data['metric_new_cases_asympt'] + data['metric_new_cases_sympt']
        plt.plot(data.index[:30], data['new_cases'][:30], color='r', alpha=0.3)
    plt.xlabel('Time')
    plt.ylabel('New cases')
    plt.title('First 30 days')
    plt.subplot(4, 2, 7)
    for file in os.listdir(v3_25_output_file_path):
        data = pd.read_csv(v3_25_output_file_path + file, delimiter='\t')
        data = data.drop('Unnamed: 10', axis=1)
        data = data.groupby('time').sum()
        data *= 100 / 25
        data['new_cases'] = data['metric_new_cases_asympt'] + data['metric_new_cases_sympt']
        plt.plot(data.index[:60], data['new_cases'][:60], color='b', alpha=0.3)
    for file in os.listdir(v1_25_output_file_path):
        data = pd.read_csv(v1_25_output_file_path + file, delimiter='\t')
        data = data.drop('Unnamed: 10', axis=1)
        data = data.groupby('time').sum()
        data *= 100 / 25
        data['new_cases'] = data['metric_new_cases_asympt'] + data['metric_new_cases_sympt']
        plt.plot(data.index[:60], data['new_cases'][:60], color='r', alpha=0.3)
    plt.xlabel('Time')
    plt.ylabel('New cases')
    plt.title('First 60 days')
    plt.subplot(4, 2, 8)
    for file in os.listdir(v3_25_output_file_path):
        data = pd.read_csv(v3_25_output_file_path + file, delimiter='\t')
        data = data.drop('Unnamed: 10', axis=1)
        data = data.groupby('time').sum()
        data *= 100 / 25
        data['new_cases'] = data['metric_new_cases_asympt'] + data['metric_new_cases_sympt']
        plt.plot(data.index, data['new_cases'], color='b', label='Spatial', alpha=0.3)
    for file in os.listdir(v1_25_output_file_path):
        data = pd.read_csv(v1_25_output_file_path + file, delimiter='\t')
        data = data.drop('Unnamed: 10', axis=1)
        data = data.groupby('time').sum()
        data *= 100 / 25
        data['new_cases'] = data['metric_new_cases_asympt'] + data['metric_new_cases_sympt']
        plt.plot(data.index, data['new_cases'], color='r', label='Non-spatial', alpha=0.3)
    plt.xlabel('Time')
    plt.ylabel('New cases')
    plt.title('First 100 days')
    plt.subplots_adjust(hspace=0.4, wspace=0.25)
    handles, labels = plt.gca().get_legend_handles_labels()
    by_label = dict(zip(labels, handles))
    fig.legend(by_label.values(), by_label.keys(), bbox_to_anchor=(1, 0.9))
    plt.savefig(savepath + "figure_2_5_25.png", dpi=400, bbox_inches='tight', pad_inches=0.5)
    plt.clf()
    plt.close(fig)
    # Create a plot of all the runs at different time intervals
    fig = plt.figure(figsize=(12, 18))
    # 5% and 10%
    plt.subplot(4, 2, 1)
    for file in os.listdir(v3_5_output_file_path):
        data = pd.read_csv(v3_5_output_file_path + file, delimiter='\t')
        data = data.drop('Unnamed: 10', axis=1)
        data = data.groupby('time').sum()
        data *= 100 / 5
        data['new_cases'] = data['metric_new_cases_asympt'] + data['metric_new_cases_sympt']
        plt.plot(data.index[:10], data['new_cases'][:10], color='b', alpha=0.3)
    for file in os.listdir(v1_5_output_file_path):
        data = pd.read_csv(v1_5_output_file_path + file, delimiter='\t')
        data = data.drop('Unnamed: 10', axis=1)
        data = data.groupby('time').sum()
        data *= 100 / 5
        data['new_cases'] = data['metric_new_cases_asympt'] + data['metric_new_cases_sympt']
        plt.plot(data.index[:10], data['new_cases'][:10], color='r', alpha=0.3)
    plt.xlabel('Time')
    plt.ylabel('New cases')
    plt.title('First 10 days')
    plt.subplot(4, 2, 2)
    for file in os.listdir(v3_5_output_file_path):
        data = pd.read_csv(v3_5_output_file_path + file, delimiter='\t')
        data = data.drop('Unnamed: 10', axis=1)
        data = data.groupby('time').sum()
        data *= 100 / 5
        data['new_cases'] = data['metric_new_cases_asympt'] + data['metric_new_cases_sympt']
        plt.plot(data.index[:30], data['new_cases'][:30], color='b', alpha=0.3)
    for file in os.listdir(v1_5_output_file_path):
        data = pd.read_csv(v1_5_output_file_path + file, delimiter='\t')
        data = data.drop('Unnamed: 10', axis=1)
        data = data.groupby('time').sum()
        data *= 100 / 5
        data['new_cases'] = data['metric_new_cases_asympt'] + data['metric_new_cases_sympt']
        plt.plot(data.index[:30], data['new_cases'][:30], color='r', alpha=0.3)
    plt.xlabel('Time')
    plt.ylabel('New cases')
    plt.title('First 30 days')
    plt.subplot(4, 2, 3)
    for file in os.listdir(v3_5_output_file_path):
        data = pd.read_csv(v3_5_output_file_path + file, delimiter='\t')
        data = data.drop('Unnamed: 10', axis=1)
        data = data.groupby('time').sum()
        data *= 100 / 5
        data['new_cases'] = data['metric_new_cases_asympt'] + data['metric_new_cases_sympt']
        plt.plot(data.index[:60], data['new_cases'][:60], color='b', alpha=0.3)
    for file in os.listdir(v1_5_output_file_path):
        data = pd.read_csv(v1_5_output_file_path + file, delimiter='\t')
        data = data.drop('Unnamed: 10', axis=1)
        data = data.groupby('time').sum()
        data *= 100 / 5
        data['new_cases'] = data['metric_new_cases_asympt'] + data['metric_new_cases_sympt']
        plt.plot(data.index[:60], data['new_cases'][:60], color='r', alpha=0.3)
    plt.xlabel('Time')
    plt.ylabel('New cases')
    plt.title('First 60 days')
    plt.subplot(4, 2, 4)
    for file in os.listdir(v3_5_output_file_path):
        data = pd.read_csv(v3_5_output_file_path + file, delimiter='\t')
        data = data.drop('Unnamed: 10', axis=1)
        data = data.groupby('time').sum()
        data *= 100 / 50
        data['new_cases'] = data['metric_new_cases_asympt'] + data['metric_new_cases_sympt']
        plt.plot(data.index, data['new_cases'], color='b', alpha=0.3)
    for file in os.listdir(v1_5_output_file_path):
        data = pd.read_csv(v1_5_output_file_path + file, delimiter='\t')
        data = data.drop('Unnamed: 10', axis=1)
        data = data.groupby('time').sum()
        data *= 100 / 50
        data['new_cases'] = data['metric_new_cases_asympt'] + data['metric_new_cases_sympt']
        plt.plot(data.index, data['new_cases'], color='r', alpha=0.3)
    plt.xlabel('Time')
    plt.ylabel('New cases')
    plt.title('First 100 days')
    plt.subplot(4, 2, 5)
    for file in os.listdir(v3_10_output_file_path):
        data = pd.read_csv(v3_10_output_file_path + file, delimiter='\t')
        data = data.drop('Unnamed: 10', axis=1)
        data = data.groupby('time').sum()
        data *= 100 / 10
        data['new_cases'] = data['metric_new_cases_asympt'] + data['metric_new_cases_sympt']
        plt.plot(data.index[:10], data['new_cases'][:10], color='b', alpha=0.3)
    for file in os.listdir(v1_10_output_file_path):
        data = pd.read_csv(v1_10_output_file_path + file, delimiter='\t')
        data = data.drop('Unnamed: 10', axis=1)
        data = data.groupby('time').sum()
        data *= 100 / 10
        data['new_cases'] = data['metric_new_cases_asympt'] + data['metric_new_cases_sympt']
        plt.plot(data.index[:10], data['new_cases'][:10], color='r', alpha=0.3)
    plt.xlabel('Time')
    plt.ylabel('New cases')
    plt.title('First 10 days')
    plt.subplot(4, 2, 6)
    for file in os.listdir(v3_10_output_file_path):
        data = pd.read_csv(v3_10_output_file_path + file, delimiter='\t')
        data = data.drop('Unnamed: 10', axis=1)
        data = data.groupby('time').sum()
        data *= 100 / 10
        data['new_cases'] = data['metric_new_cases_asympt'] + data['metric_new_cases_sympt']
        plt.plot(data.index[:30], data['new_cases'][:30], color='b', alpha=0.3)
    for file in os.listdir(v1_10_output_file_path):
        data = pd.read_csv(v1_10_output_file_path + file, delimiter='\t')
        data = data.drop('Unnamed: 10', axis=1)
        data = data.groupby('time').sum()
        data *= 100 / 10
        data['new_cases'] = data['metric_new_cases_asympt'] + data['metric_new_cases_sympt']
        plt.plot(data.index[:30], data['new_cases'][:30], color='r', alpha=0.3)
    plt.xlabel('Time')
    plt.ylabel('New cases')
    plt.title('First 30 days')
    plt.subplot(4, 2, 7)
    for file in os.listdir(v3_10_output_file_path):
        data = pd.read_csv(v3_10_output_file_path + file, delimiter='\t')
        data = data.drop('Unnamed: 10', axis=1)
        data = data.groupby('time').sum()
        data *= 100 / 10
        data['new_cases'] = data['metric_new_cases_asympt'] + data['metric_new_cases_sympt']
        plt.plot(data.index[:60], data['new_cases'][:60], color='b', alpha=0.3)
    for file in os.listdir(v1_10_output_file_path):
        data = pd.read_csv(v1_10_output_file_path + file, delimiter='\t')
        data = data.drop('Unnamed: 10', axis=1)
        data = data.groupby('time').sum()
        data *= 100 / 10
        data['new_cases'] = data['metric_new_cases_asympt'] + data['metric_new_cases_sympt']
        plt.plot(data.index[:60], data['new_cases'][:60], color='r', alpha=0.3)
    plt.xlabel('Time')
    plt.ylabel('New cases')
    plt.title('First 60 days')
    plt.subplot(4, 2, 8)
    for file in os.listdir(v3_10_output_file_path):
        data = pd.read_csv(v3_10_output_file_path + file, delimiter='\t')
        data = data.drop('Unnamed: 10', axis=1)
        data = data.groupby('time').sum()
        data *= 100 / 10
        data['new_cases'] = data['metric_new_cases_asympt'] + data['metric_new_cases_sympt']
        plt.plot(data.index, data['new_cases'], color='b', label='Spatial', alpha=0.3)
    for file in os.listdir(v1_10_output_file_path):
        data = pd.read_csv(v1_10_output_file_path + file, delimiter='\t')
        data = data.drop('Unnamed: 10', axis=1)
        data = data.groupby('time').sum()
        data *= 100 / 10
        data['new_cases'] = data['metric_new_cases_asympt'] + data['metric_new_cases_sympt']
        plt.plot(data.index, data['new_cases'], color='r', label='Non-spatial', alpha=0.3)
    plt.xlabel('Time')
    plt.ylabel('New cases')
    plt.title('First 100 days')
    plt.subplots_adjust(hspace=0.4, wspace=0.25)
    handles, labels = plt.gca().get_legend_handles_labels()
    by_label = dict(zip(labels, handles))
    fig.legend(by_label.values(), by_label.keys(), bbox_to_anchor=(1, 0.9))
    plt.savefig(savepath + "figure_2_5_10.png", dpi=400, bbox_inches='tight', pad_inches=0.5)
    plt.clf()
    plt.close(fig)
    # Plot maps
    zimbabwe = geopandas.read_file(zimfilepath)
    spatialDf = zimbabwe.copy()
    v3_5_map_outputs = pd.DataFrame()
    for file in os.listdir(v3_5_output_file_path):
        data = pd.read_csv(v3_5_output_file_path + file, delimiter='\t')
        data = data.drop('Unnamed: 10', axis=1)
        v3_5_map_outputs = v3_5_map_outputs.append(data)
    v3_5_first_30 = v3_5_map_outputs.loc[v3_5_map_outputs['time'] < 30]
    v3_5_first_30['new_cases'] = v3_5_first_30['metric_new_cases_asympt'] + v3_5_first_30['metric_new_cases_sympt']
    v3_5_first_30['new_cases'] *= 100 / 5
    v3_5_first_30 = v3_5_first_30.groupby('myId').median()
    v3_5_first_30['district_number'] = [int(idx[2:]) for idx in v3_5_first_30.index]
    v3_5_first_30 = v3_5_first_30.sort_values('district_number')
    spatialDf['v3_5_perc_median_1_30'] = v3_5_first_30['new_cases'].values
    # 30-60
    v3_5_30_60 = v3_5_map_outputs.loc[(v3_5_map_outputs['time'] < 60) & (v3_5_map_outputs['time'] >= 30)]
    v3_5_30_60['new_cases'] = v3_5_30_60['metric_new_cases_asympt'] + v3_5_30_60['metric_new_cases_sympt']
    v3_5_30_60['new_cases'] *= 100 / 5
    v3_5_30_60 = v3_5_30_60.groupby('myId').median()
    v3_5_30_60['district_number'] = [int(idx[2:]) for idx in v3_5_30_60.index]
    v3_5_30_60 = v3_5_30_60.sort_values('district_number')
    spatialDf['v3_5_perc_median_31_60'] = v3_5_30_60['new_cases'].values
    # 61+
    v3_5_61_plus = v3_5_map_outputs.loc[v3_5_map_outputs['time'] >= 60]
    v3_5_61_plus['new_cases'] = v3_5_61_plus['metric_new_cases_asympt'] + v3_5_61_plus['metric_new_cases_sympt']
    v3_5_61_plus['new_cases'] *= 100 / 5
    v3_5_61_plus = v3_5_61_plus.groupby('myId').median()
    v3_5_61_plus['district_number'] = [int(idx[2:]) for idx in v3_5_61_plus.index]
    v3_5_61_plus = v3_5_61_plus.sort_values('district_number')
    spatialDf['v3_5_perc_median_61_plus'] = v3_5_61_plus['new_cases'].values
    # Values for 10 %
    v3_10_map_outputs = pd.DataFrame()
    for file in os.listdir(v3_10_output_file_path):
        data = pd.read_csv(v3_10_output_file_path + file, delimiter='\t')
        data = data.drop('Unnamed: 10', axis=1)
        v3_10_map_outputs = v3_10_map_outputs.append(data)
    v3_10_first_30 = v3_10_map_outputs.loc[v3_10_map_outputs['time'] < 30]
    v3_10_first_30['new_cases'] = v3_10_first_30['metric_new_cases_asympt'] + v3_10_first_30['metric_new_cases_sympt']
    v3_10_first_30['new_cases'] *= 100 / 10
    v3_10_first_30 = v3_10_first_30.groupby('myId').median()
    v3_10_first_30['district_number'] = [int(idx[2:]) for idx in v3_10_first_30.index]
    v3_10_first_30 = v3_10_first_30.sort_values('district_number')
    spatialDf['v3_10_perc_median_1_30'] = v3_10_first_30['new_cases'].values
    # 30-60
    v3_10_30_60 = v3_10_map_outputs.loc[(v3_10_map_outputs['time'] < 60) & (v3_10_map_outputs['time'] >= 30)]
    v3_10_30_60['new_cases'] = v3_10_30_60['metric_new_cases_asympt'] + v3_10_30_60['metric_new_cases_sympt']
    v3_10_30_60['new_cases'] *= 100 / 10
    v3_10_30_60 = v3_10_30_60.groupby('myId').median()
    v3_10_30_60['district_number'] = [int(idx[2:]) for idx in v3_10_30_60.index]
    v3_10_30_60 = v3_10_30_60.sort_values('district_number')
    spatialDf['v3_10_perc_median_31_60'] = v3_10_30_60['new_cases'].values
    # 61+
    v3_10_61_plus = v3_10_map_outputs.loc[v3_10_map_outputs['time'] >= 60]
    v3_10_61_plus['new_cases'] = v3_10_61_plus['metric_new_cases_asympt'] + v3_10_61_plus['metric_new_cases_sympt']
    v3_10_61_plus['new_cases'] *= 100 / 10
    v3_10_61_plus = v3_10_61_plus.groupby('myId').median()
    v3_10_61_plus['district_number'] = [int(idx[2:]) for idx in v3_10_61_plus.index]
    v3_10_61_plus = v3_10_61_plus.sort_values('district_number')
    spatialDf['v3_10_perc_median_61_plus'] = v3_10_61_plus['new_cases'].values
    # Values for 25 %
    v3_25_map_outputs = pd.DataFrame()
    for file in os.listdir(v3_25_output_file_path):
        data = pd.read_csv(v3_25_output_file_path + file, delimiter='\t')
        data = data.drop('Unnamed: 10', axis=1)
        v3_25_map_outputs = v3_25_map_outputs.append(data)
    v3_25_first_30 = v3_25_map_outputs.loc[v3_25_map_outputs['time'] < 30]
    v3_25_first_30['new_cases'] = v3_25_first_30['metric_new_cases_asympt'] + v3_25_first_30['metric_new_cases_sympt']
    v3_25_first_30['new_cases'] *= 100 / 25
    v3_25_first_30 = v3_25_first_30.groupby('myId').median()
    v3_25_first_30['district_number'] = [int(idx[2:]) for idx in v3_25_first_30.index]
    v3_25_first_30 = v3_25_first_30.sort_values('district_number')
    spatialDf['v3_25_perc_median_1_30'] = v3_25_first_30['new_cases'].values
    # 30-60
    v3_25_30_60 = v3_25_map_outputs.loc[(v3_25_map_outputs['time'] < 60) & (v3_25_map_outputs['time'] >= 30)]
    v3_25_30_60['new_cases'] = v3_25_30_60['metric_new_cases_asympt'] + v3_25_30_60['metric_new_cases_sympt']
    v3_25_30_60['new_cases'] *= 100 / 25
    v3_25_30_60 = v3_25_30_60.groupby('myId').median()
    v3_25_30_60['district_number'] = [int(idx[2:]) for idx in v3_25_30_60.index]
    v3_25_30_60 = v3_25_30_60.sort_values('district_number')
    spatialDf['v3_25_perc_median_31_60'] = v3_25_30_60['new_cases'].values
    # 61+
    v3_25_61_plus = v3_25_map_outputs.loc[v3_25_map_outputs['time'] >= 60]
    v3_25_61_plus['new_cases'] = v3_25_61_plus['metric_new_cases_asympt'] + v3_25_61_plus['metric_new_cases_sympt']
    v3_25_61_plus['new_cases'] *= 100 / 25
    v3_25_61_plus = v3_25_61_plus.groupby('myId').median()
    v3_25_61_plus['district_number'] = [int(idx[2:]) for idx in v3_25_61_plus.index]
    v3_25_61_plus = v3_25_61_plus.sort_values('district_number')
    spatialDf['v3_25_perc_median_61_plus'] = v3_25_61_plus['new_cases'].values
    # Values for 50 %
    v3_50_map_outputs = pd.DataFrame()
    for file in os.listdir(v3_50_output_file_path):
        data = pd.read_csv(v3_50_output_file_path + file, delimiter='\t')
        data = data.drop('Unnamed: 10', axis=1)
        v3_50_map_outputs = v3_50_map_outputs.append(data)
    v3_50_first_30 = v3_50_map_outputs.loc[v3_50_map_outputs['time'] < 30]
    v3_50_first_30['new_cases'] = v3_50_first_30['metric_new_cases_asympt'] + v3_50_first_30['metric_new_cases_sympt']
    v3_50_first_30['new_cases'] *= 100 / 50
    v3_50_first_30 = v3_50_first_30.groupby('myId').median()
    v3_50_first_30['district_number'] = [int(idx[2:]) for idx in v3_50_first_30.index]
    v3_50_first_30 = v3_50_first_30.sort_values('district_number')
    spatialDf['v3_50_perc_median_1_30'] = v3_50_first_30['new_cases'].values
    # 30-60
    v3_50_30_60 = v3_50_map_outputs.loc[(v3_50_map_outputs['time'] < 60) & (v3_50_map_outputs['time'] >= 30)]
    v3_50_30_60['new_cases'] = v3_50_30_60['metric_new_cases_asympt'] + v3_50_30_60['metric_new_cases_sympt']
    v3_50_30_60['new_cases'] *= 100 / 50
    v3_50_30_60 = v3_50_30_60.groupby('myId').median()
    v3_50_30_60['district_number'] = [int(idx[2:]) for idx in v3_50_30_60.index]
    v3_50_30_60 = v3_50_30_60.sort_values('district_number')
    spatialDf['v3_50_perc_median_31_60'] = v3_50_30_60['new_cases'].values
    # 61+
    v3_50_61_plus = v3_50_map_outputs.loc[v3_50_map_outputs['time'] >= 60]
    v3_50_61_plus['new_cases'] = v3_50_61_plus['metric_new_cases_asympt'] + v3_50_61_plus['metric_new_cases_sympt']
    v3_50_61_plus['new_cases'] *= 100 / 50
    v3_50_61_plus = v3_50_61_plus.groupby('myId').median()
    v3_50_61_plus['district_number'] = [int(idx[2:]) for idx in v3_50_61_plus.index]
    v3_50_61_plus = v3_50_61_plus.sort_values('district_number')
    spatialDf['v3_50_perc_median_61_plus'] = v3_50_61_plus['new_cases'].values
    max_first_30 = max([max(spatialDf.v3_5_perc_median_1_30), max(spatialDf.v3_10_perc_median_1_30),
                        max(spatialDf.v3_25_perc_median_1_30), max(spatialDf.v3_50_perc_median_1_30)])
    max_31_60 = max([max(spatialDf.v3_5_perc_median_31_60), max(spatialDf.v3_10_perc_median_31_60),
                     max(spatialDf.v3_25_perc_median_31_60), max(spatialDf.v3_50_perc_median_31_60)])
    max_61_plus = max([max(spatialDf.v3_5_perc_median_61_plus), max(spatialDf.v3_10_perc_median_61_plus),
                       max(spatialDf.v3_25_perc_median_61_plus), max(spatialDf.v3_50_perc_median_61_plus)])
    fig = plt.figure(figsize=(20, 16))
    ax1 = plt.subplot(3, 4, 1)
    spatialDf.plot(ax=ax1, column='v3_5_perc_median_1_30', cmap='Oranges', edgecolor='k', legend=False,
                   vmax=max_first_30)
    ax1.axis('off')

    plt.title("Median cases day 1-30, 5%")
    ax1 = plt.subplot(3, 4, 2)
    spatialDf.plot(ax=ax1, column='v3_10_perc_median_1_30', cmap='Oranges', edgecolor='k', legend=True,
                   vmax=max_first_30)
    ax1.axis('off')

    plt.title("Median cases day 1-30, 10%")
    ax1 = plt.subplot(3, 4, 3)
    spatialDf.plot(ax=ax1, column='v3_25_perc_median_1_30', cmap='Oranges', edgecolor='k', legend=False,
                   vmax=max_first_30)
    ax1.axis('off')

    plt.title("Median cases day 1-30, 25%")
    ax1 = plt.subplot(3, 4, 4)
    spatialDf.plot(ax=ax1, column='v3_50_perc_median_1_30', cmap='Oranges', edgecolor='k', legend=False,
                   vmax=max_first_30)
    ax1.axis('off')

    plt.title("Median cases day 1-30, 50%")
    ax1 = plt.subplot(3, 4, 5)
    spatialDf.plot(ax=ax1, column='v3_5_perc_median_31_60', cmap='Purples', edgecolor='k', legend=False,
                   vmax=max_31_60)
    ax1.axis('off')

    plt.title("Median cases day 31-60, 5%")
    ax1 = plt.subplot(3, 4, 6)
    spatialDf.plot(ax=ax1, column='v3_10_perc_median_31_60', cmap='Purples', edgecolor='k', legend=True,
                   vmax=max_31_60)
    ax1.axis('off')

    plt.title("Median cases day 31-60, 10%")
    ax1 = plt.subplot(3, 4, 7)
    spatialDf.plot(ax=ax1, column='v3_25_perc_median_31_60', cmap='Purples', edgecolor='k', legend=False,
                   vmax=max_31_60)
    ax1.axis('off')

    plt.title("Median cases day 31-60, 25%")
    ax1 = plt.subplot(3, 4, 8)
    spatialDf.plot(ax=ax1, column='v3_50_perc_median_31_60', cmap='Purples', edgecolor='k', legend=False,
                   vmax=max_31_60)
    ax1.axis('off')

    plt.title("Median cases day 31-60, 50%")
    ax1 = plt.subplot(3, 4, 9)
    spatialDf.plot(ax=ax1, column='v3_5_perc_median_61_plus', cmap='Greens', edgecolor='k', legend=False,
                   vmax=max_61_plus)
    ax1.axis('off')

    plt.title("Median cases day 61-100, 5%")
    ax1 = plt.subplot(3, 4, 10)
    spatialDf.plot(ax=ax1, column='v3_10_perc_median_61_plus', cmap='Greens', edgecolor='k', legend=True,
                   vmax=max_61_plus)
    ax1.axis('off')

    plt.title("Median cases day 61-100, 10%")
    ax1 = plt.subplot(3, 4, 11)
    spatialDf.plot(ax=ax1, column='v3_25_perc_median_61_plus', cmap='Greens', edgecolor='k',
                   vmax=max_61_plus, legend=False)
    ax1.axis('off')

    plt.title("Median cases day 61-100, 25%")
    ax1 = plt.subplot(3, 4, 12)
    ax1.axis('off')
    spatialDf.plot(ax=ax1, column='v3_50_perc_median_61_plus', cmap='Greens', edgecolor='k', legend=False,
                   vmax=max_61_plus)
    plt.title("Median cases day 61-100, 50%")

    # plt.subplots_adjust(hspace=0.1)
    plt.savefig(savepath + "median_cases_per_sample_size.png", dpi=400, bbox_inches='tight')
    plt.clf()
    plt.close(fig)
    v3_5_outputs = pd.DataFrame()
    for file in os.listdir(v3_5_output_file_path):
        data = pd.read_csv(v3_5_output_file_path + file, delimiter='\t')
        data = data.drop('Unnamed: 10', axis=1)
        data['cumulative_cases'] = [0] * len(data)
        data['new_cases'] = data['metric_new_cases_asympt'] + data['metric_new_cases_sympt']
        data['has_cases'] = [0] * len(data)
        for district in data.myId.unique():
            cases_in_district = data.loc[data['myId'] == district]
            data.loc[cases_in_district.index, 'cumulative_cases'] = np.cumsum(cases_in_district['new_cases'].values)
            data.loc[cases_in_district.index, 'has_cases'] = \
                [1 if val > 0 else 0 for val in np.cumsum(cases_in_district['new_cases'].values)]
        v3_5_outputs = v3_5_outputs.append(data)

    n_infected_districts = v3_5_outputs.groupby('time').sum()
    v3_5_n_infected_dist = list(np.divide(n_infected_districts['has_cases'].values, 10))
    v3_10_outputs = pd.DataFrame()
    for file in os.listdir(v3_10_output_file_path):
        data = pd.read_csv(v3_10_output_file_path + file, delimiter='\t')
        data = data.drop('Unnamed: 10', axis=1)
        data['cumulative_cases'] = [0] * len(data)
        data['new_cases'] = data['metric_new_cases_asympt'] + data['metric_new_cases_sympt']
        data['has_cases'] = [0] * len(data)
        for district in data.myId.unique():
            cases_in_district = data.loc[data['myId'] == district]
            data.loc[cases_in_district.index, 'cumulative_cases'] = np.cumsum(cases_in_district['new_cases'].values)
            data.loc[cases_in_district.index, 'has_cases'] = \
                [1 if val > 0 else 0 for val in np.cumsum(cases_in_district['new_cases'].values)]
        v3_10_outputs = v3_10_outputs.append(data)

    n_infected_districts = v3_10_outputs.groupby('time').sum()
    v3_10_n_infected_dist = list(np.divide(n_infected_districts['has_cases'].values, 10))
    v3_25_outputs = pd.DataFrame()
    for file in os.listdir(v3_25_output_file_path):
        data = pd.read_csv(v3_25_output_file_path + file, delimiter='\t')
        data = data.drop('Unnamed: 10', axis=1)
        data['cumulative_cases'] = [0] * len(data)
        data['new_cases'] = data['metric_new_cases_asympt'] + data['metric_new_cases_sympt']
        data['has_cases'] = [0] * len(data)
        for district in data.myId.unique():
            cases_in_district = data.loc[data['myId'] == district]
            data.loc[cases_in_district.index, 'cumulative_cases'] = np.cumsum(cases_in_district['new_cases'].values)
            data.loc[cases_in_district.index, 'has_cases'] = \
                [1 if val > 0 else 0 for val in np.cumsum(cases_in_district['new_cases'].values)]
        v3_25_outputs = v3_25_outputs.append(data)

    n_infected_districts = v3_25_outputs.groupby('time').sum()
    v3_25_n_infected_dist = list(np.divide(n_infected_districts['has_cases'].values, 10))
    v3_50_outputs = pd.DataFrame()
    for file in os.listdir(v3_50_output_file_path):
        data = pd.read_csv(v3_50_output_file_path + file, delimiter='\t')
        data = data.drop('Unnamed: 10', axis=1)
        data['cumulative_cases'] = [0] * len(data)
        data['new_cases'] = data['metric_new_cases_asympt'] + data['metric_new_cases_sympt']
        data['has_cases'] = [0] * len(data)
        for district in data.myId.unique():
            cases_in_district = data.loc[data['myId'] == district]
            data.loc[cases_in_district.index, 'cumulative_cases'] = np.cumsum(cases_in_district['new_cases'].values)
            data.loc[cases_in_district.index, 'has_cases'] = \
                [1 if val > 0 else 0 for val in np.cumsum(cases_in_district['new_cases'].values)]
        v3_50_outputs = v3_50_outputs.append(data)

    n_infected_districts = v3_50_outputs.groupby('time').sum()
    v3_50_n_infected_dist = list(np.divide(n_infected_districts['has_cases'].values, 10))
    spatialDf['ratio_med_cases_5_50_mid_sim'] = np.divide(spatialDf['v3_50_perc_median_31_60'],
                                                          spatialDf['v3_5_perc_median_31_60'])
    fig = plt.figure(figsize=(6, 10))
    ax1 = plt.subplot(2, 1, 1)
    spatialDf.plot(ax=ax1, column='ratio_med_cases_5_50_mid_sim', cmap='RdYlBu', edgecolor='k', legend=True,
                   vmax=spatialDf['ratio_med_cases_5_50_mid_sim'].max())
    ax1.axis('off')

    plt.title("Difference in 50% and 5% median daily cases\n in the second month of simulation time")
    # plt.plot(v1_5_outputs.index, v1_5_outputs.metric_new_deaths, color='r', label='5%')
    # plt.plot(v1_10_outputs.index, v1_10_outputs.metric_new_deaths, color='b', label='10%')
    # plt.plot(v1_25_outputs.index, v1_25_outputs.metric_new_deaths, color='c', label='25%')
    # plt.plot(v1_50_outputs.index, v1_50_outputs.metric_new_deaths, color='y', label='50%')
    plt.subplot(2, 1, 2)
    plt.plot(np.arange(len(v3_5_n_infected_dist)), v3_5_n_infected_dist, color='r', label='5%')
    plt.plot(np.arange(len(v3_10_n_infected_dist)), v3_10_n_infected_dist, color='b', label='10%')
    plt.plot(np.arange(len(v3_25_n_infected_dist)), v3_25_n_infected_dist, color='c', label='25%')
    plt.plot(np.arange(len(v3_50_n_infected_dist)), v3_50_n_infected_dist, color='y', label='50%')
    plt.legend()
    plt.xlabel('Time')
    plt.ylabel('Number of districts')
    plt.title('Number of districts with at least on cumulative case')
    plt.savefig(savepath + "figure_4")
    plt.clf()
    plt.bar(np.arange(len(v3_5_n_infected_dist)), v3_5_n_infected_dist, color='r', label='5%', width=0.25)
    plt.bar(np.arange(len(v3_10_n_infected_dist)) + 0.25, v3_10_n_infected_dist, color='b', label='10%', width=0.25)
    plt.bar(np.arange(len(v3_25_n_infected_dist)) + 0.5, v3_25_n_infected_dist, color='c', label='25%', width=0.25)
    plt.bar(np.arange(len(v3_50_n_infected_dist)) + 0.75, v3_50_n_infected_dist, color='y', label='50%', width=0.25)
    plt.legend()
    plt.xlabel('Time')
    plt.ylabel('Number of districts')
    plt.title('Number of districts with at least on cumulative case')
    plt.savefig(savepath + "figure_4b_bar")
    plt.clf()
    #
    # v3_5_first_5 = v3_5_map_outputs.loc[v3_5_map_outputs['time'] < 5]
    # v3_5_first_5['new_cases'] = v3_5_first_5['metric_new_cases_asympt'] + v3_5_first_5['metric_new_cases_sympt']
    # v3_5_first_5 = v3_5_first_5.groupby('myId').sum()
    # v3_5_first_5['had_cases'] = [1 if val > 0 else 0 for val in v3_5_first_5['new_cases'].values]
    # v3_5_5_10 = v3_5_map_outputs.loc[(v3_5_map_outputs['time'] >= 5) & (v3_5_map_outputs['time'] < 10)]
    # v3_5_5_10['new_cases'] = v3_5_5_10['metric_new_cases_asympt'] + v3_5_5_10['metric_new_cases_sympt']
    # v3_5_5_10 = v3_5_5_10.groupby('myId').sum()
    # v3_5_5_10['has_cases'] = [1 if val > 0 else 0 for val in v3_5_5_10['new_cases'].values]
    # v3_5_5_10['has_cases']
    #
    # v3_5_first_5['new_cases'] = v3_5_first_5['metric_new_cases_asympt'] + v3_5_first_5['metric_new_cases_sympt']
    # v3_5_first_5 = v3_5_first_5.groupby('myId').sum()
    # v3_5_first_5['has_cases'] = [1 if val > 0 else 0 for val in v3_5_first_5['new_cases'].values]
    # v3_5_first_5['new_cases'] *= 100 / 5
    # v3_5_first_30 = v3_5_first_30.groupby('myId').median()
    # v3_5_first_30['district_number'] = [int(idx[2:]) for idx in v3_5_first_30.index]
    # v3_5_first_30 = v3_5_first_30.sort_values('district_number')
    # spatialDf['v3_5_perc_median_1_30'] = v3_5_first_30['new_cases'].values

    # plot the spread of which district has cases
    v3_5_outputs['district_number'] = [int(dist[2:]) for dist in v3_5_outputs.myId.values]
    v3_5_cases_at_5 = v3_5_outputs.loc[v3_5_outputs['time'] == 5]
    v3_5_cases_at_5 = v3_5_cases_at_5.groupby('myId').mean()
    v3_5_cases_at_5 = v3_5_cases_at_5.sort_values('district_number')
    spatialDf['v3_5_has_cases_at_5'] = np.round(v3_5_cases_at_5['has_cases'].values)
    v3_5_cases_at_10 = v3_5_outputs.loc[v3_5_outputs['time'] == 10]
    v3_5_cases_at_10 = v3_5_cases_at_10.groupby('myId').mean()
    v3_5_cases_at_10 = v3_5_cases_at_10.sort_values('district_number')
    spatialDf['v3_5_has_cases_at_10'] = np.round(v3_5_cases_at_10['has_cases'].values)
    v3_5_cases_at_15 = v3_5_outputs.loc[v3_5_outputs['time'] == 15]
    v3_5_cases_at_15 = v3_5_cases_at_15.groupby('myId').mean()
    v3_5_cases_at_15 = v3_5_cases_at_15.sort_values('district_number')
    spatialDf['v3_5_has_cases_at_15'] = np.round(v3_5_cases_at_15['has_cases'].values)
    v3_5_cases_at_20 = v3_5_outputs.loc[v3_5_outputs['time'] == 20]
    v3_5_cases_at_20 = v3_5_cases_at_20.groupby('myId').mean()
    v3_5_cases_at_20 = v3_5_cases_at_20.sort_values('district_number')
    spatialDf['v3_5_has_cases_at_20'] = np.round(v3_5_cases_at_20['has_cases'].values)
    v3_10_outputs['district_number'] = [int(dist[2:]) for dist in v3_10_outputs.myId.values]
    v3_10_cases_at_5 = v3_10_outputs.loc[v3_10_outputs['time'] == 5]
    v3_10_cases_at_5 = v3_10_cases_at_5.groupby('myId').mean()
    v3_10_cases_at_5 = v3_10_cases_at_5.sort_values('district_number')
    spatialDf['v3_10_has_cases_at_5'] = np.round(v3_10_cases_at_5['has_cases'].values)
    v3_10_cases_at_10 = v3_10_outputs.loc[v3_10_outputs['time'] == 10]
    v3_10_cases_at_10 = v3_10_cases_at_10.groupby('myId').mean()
    v3_10_cases_at_10 = v3_10_cases_at_10.sort_values('district_number')
    spatialDf['v3_10_has_cases_at_10'] = np.round(v3_10_cases_at_10['has_cases'].values)
    v3_10_cases_at_15 = v3_10_outputs.loc[v3_10_outputs['time'] == 15]
    v3_10_cases_at_15 = v3_10_cases_at_15.groupby('myId').mean()
    v3_10_cases_at_15 = v3_10_cases_at_15.sort_values('district_number')
    spatialDf['v3_10_has_cases_at_15'] = np.round(v3_10_cases_at_15['has_cases'].values)
    v3_10_cases_at_20 = v3_10_outputs.loc[v3_10_outputs['time'] == 20]
    v3_10_cases_at_20 = v3_10_cases_at_20.groupby('myId').mean()
    v3_10_cases_at_20 = v3_10_cases_at_20.sort_values('district_number')
    spatialDf['v3_10_has_cases_at_20'] = np.round(v3_10_cases_at_20['has_cases'].values)
    v3_25_outputs['district_number'] = [int(dist[2:]) for dist in v3_25_outputs.myId.values]
    v3_25_cases_at_5 = v3_25_outputs.loc[v3_25_outputs['time'] == 5]
    v3_25_cases_at_5 = v3_25_cases_at_5.groupby('myId').mean()
    v3_25_cases_at_5 = v3_25_cases_at_5.sort_values('district_number')
    spatialDf['v3_25_has_cases_at_5'] = np.round(v3_25_cases_at_5['has_cases'].values)
    v3_25_cases_at_10 = v3_25_outputs.loc[v3_25_outputs['time'] == 10]
    v3_25_cases_at_10 = v3_25_cases_at_10.groupby('myId').mean()
    v3_25_cases_at_10 = v3_25_cases_at_10.sort_values('district_number')
    spatialDf['v3_25_has_cases_at_10'] = np.round(v3_25_cases_at_10['has_cases'].values)
    v3_25_cases_at_15 = v3_25_outputs.loc[v3_25_outputs['time'] == 15]
    v3_25_cases_at_15 = v3_25_cases_at_15.groupby('myId').mean()
    v3_25_cases_at_15 = v3_25_cases_at_15.sort_values('district_number')
    spatialDf['v3_25_has_cases_at_15'] = np.round(v3_25_cases_at_15['has_cases'].values)
    v3_25cases_at_20 = v3_25_outputs.loc[v3_25_outputs['time'] == 20]
    v3_25cases_at_20 = v3_25cases_at_20.groupby('myId').mean()
    v3_25cases_at_20 = v3_25cases_at_20.sort_values('district_number')
    spatialDf['v3_25_has_cases_at_20'] = np.round(v3_25cases_at_20['has_cases'].values)
    v3_50_outputs['district_number'] = [int(dist[2:]) for dist in v3_50_outputs.myId.values]
    v3_50_cases_at_5 = v3_50_outputs.loc[v3_50_outputs['time'] == 5]
    v3_50_cases_at_5 = v3_50_cases_at_5.groupby('myId').mean()
    v3_50_cases_at_5 = v3_50_cases_at_5.sort_values('district_number')
    spatialDf['v3_50_has_cases_at_5'] = np.round(v3_50_cases_at_5['has_cases'].values)
    v3_50_cases_at_10 = v3_50_outputs.loc[v3_50_outputs['time'] == 10]
    v3_50_cases_at_10 = v3_50_cases_at_10.groupby('myId').mean()
    v3_50_cases_at_10 = v3_50_cases_at_10.sort_values('district_number')
    spatialDf['v3_50_has_cases_at_10'] = np.round(v3_50_cases_at_10['has_cases'].values)
    v3_50_cases_at_15 = v3_50_outputs.loc[v3_50_outputs['time'] == 15]
    v3_50_cases_at_15 = v3_50_cases_at_15.groupby('myId').mean()
    v3_50_cases_at_15 = v3_50_cases_at_15.sort_values('district_number')
    spatialDf['v3_50_has_cases_at_15'] = np.round(v3_50_cases_at_15['has_cases'].values)
    v3_50cases_at_20 = v3_50_outputs.loc[v3_50_outputs['time'] == 20]
    v3_50cases_at_20 = v3_50cases_at_20.groupby('myId').mean()
    v3_50cases_at_20 = v3_50cases_at_20.sort_values('district_number')
    spatialDf['v3_50_has_cases_at_20'] = np.round(v3_50cases_at_20['has_cases'].values)

    fig = plt.figure(figsize=(20, 20), constrained_layout=True)
    (subfig1, subfig2, subfig3, subfig4) = fig.subfigures(1, 4)
    (ax11, ax21, ax31, ax41) = subfig1.subplots(4, 1)
    spatialDf.plot(ax=ax11, column='v3_5_has_cases_at_5', cmap='Greens', edgecolor='k', legend=False,
                   vmax=1)
    for key, spine in ax11.spines.items():
        spine.set_visible(False)
    ax11.set_xticks([], [])
    ax11.set_yticks([], [])
    ax11.set_ylabel("Days 1-5", weight='bold', fontsize=25)
    spatialDf.plot(ax=ax21, column='v3_5_has_cases_at_10', cmap='Greens', edgecolor='k', legend=False,
                   vmax=1)
    for key, spine in ax21.spines.items():
        spine.set_visible(False)
    ax21.set_xticks([], [])
    ax21.set_yticks([], [])
    ax21.set_ylabel("Days 1-10", weight='bold', fontsize=25)
    spatialDf.plot(ax=ax31, column='v3_5_has_cases_at_15', cmap='Greens', edgecolor='k', legend=False,
                   vmax=1)
    for key, spine in ax31.spines.items():
        spine.set_visible(False)
    ax31.set_xticks([], [])
    ax31.set_yticks([], [])
    ax31.set_ylabel("Days 1-15", weight='bold', fontsize=25)
    spatialDf.plot(ax=ax41, column='v3_5_has_cases_at_20', cmap='Greens', edgecolor='k', legend=False,
                   vmax=1)
    for key, spine in ax41.spines.items():
        spine.set_visible(False)
    ax41.set_xticks([], [])
    ax41.set_yticks([], [])
    ax41.set_ylabel("Days 1-20", weight='bold', fontsize=25)
    subfig1.suptitle('5%', weight='bold', fontsize=25)
    (ax12, ax22, ax32, ax42) = subfig2.subplots(4, 1)
    spatialDf.plot(ax=ax12, column='v3_10_has_cases_at_5', cmap='Greens', edgecolor='k', legend=False,
                   vmax=1)
    ax12.axis('off')
    spatialDf.plot(ax=ax22, column='v3_10_has_cases_at_10', cmap='Greens', edgecolor='k', legend=False,
                   vmax=1)
    ax22.axis('off')
    spatialDf.plot(ax=ax32, column='v3_10_has_cases_at_15', cmap='Greens', edgecolor='k', legend=False,
                   vmax=1)
    ax32.axis('off')
    spatialDf.plot(ax=ax42, column='v3_5_has_cases_at_20', cmap='Greens', edgecolor='k', legend=False,
                   vmax=1)
    ax42.axis('off')
    subfig2.suptitle('10%', weight='bold', fontsize=25)
    (ax13, ax23, ax33, ax43) = subfig3.subplots(4, 1)
    spatialDf.plot(ax=ax13, column='v3_25_has_cases_at_5', cmap='Greens', edgecolor='k', legend=False,
                   vmax=1)
    ax13.axis('off')
    spatialDf.plot(ax=ax23, column='v3_25_has_cases_at_10', cmap='Greens', edgecolor='k', legend=False,
                   vmax=1)
    ax23.axis('off')
    spatialDf.plot(ax=ax33, column='v3_25_has_cases_at_15', cmap='Greens', edgecolor='k', legend=False,
                   vmax=1)
    ax33.axis('off')
    spatialDf.plot(ax=ax43, column='v3_25_has_cases_at_20', cmap='Greens', edgecolor='k', legend=False,
                   vmax=1)
    ax43.axis('off')
    subfig3.suptitle('25%', weight='bold', fontsize=25)
    (ax14, ax24, ax34, ax44) = subfig4.subplots(4, 1)
    spatialDf.plot(ax=ax14, column='v3_50_has_cases_at_5', cmap='Greens', edgecolor='k', legend=False,
                   vmax=1)
    ax14.axis('off')
    spatialDf.plot(ax=ax24, column='v3_50_has_cases_at_10', cmap='Greens', edgecolor='k', legend=False,
                   vmax=1)
    ax24.axis('off')
    spatialDf.plot(ax=ax34, column='v3_50_has_cases_at_15', cmap='Greens', edgecolor='k', legend=False,
                   vmax=1)
    ax34.axis('off')
    spatialDf.plot(ax=ax44, column='v3_50_has_cases_at_20', cmap='Greens', edgecolor='k', legend=False,
                   vmax=1)
    ax44.axis('off')
    subfig4.suptitle('50%', weight='bold', fontsize=25)
    fig.suptitle('Districts with at least one case\n', fontsize=40)
    plt.savefig(savepath + "figure_5_take_2.png", dpi=400, bbox_inches='tight')
    plt.close(fig)
    plt.clf()

    ax1 = plt.subplot(4, 4, 1)
    spatialDf.plot(ax=ax1, column='v3_5_has_cases_at_5', cmap='Greens', edgecolor='k', legend=False,
                   vmax=1)
    ax1.axis('off')
    plt.text(x=0.1, y=0.5, s='5%', fontweight='bold')
    plt.title("Districts with at least one case, days 1-5")

    ax1 = plt.subplot(4, 4, 1)
    spatialDf.plot(ax=ax1, column='v3_5_has_cases_at_5', cmap='Greens', edgecolor='k', legend=False,
                   vmax=1)
    ax1.axis('off')
    plt.text(x=0.1, y=0.5, s='5%', fontweight='bold')
    plt.title("Districts with at least one case, days 1-5")
    ax1 = plt.subplot(4, 4, 2)
    spatialDf.plot(ax=ax1, column='v3_10_has_cases_at_5', cmap='Greens', edgecolor='k', legend=False,
                   vmax=1)
    ax1.axis('off')

    plt.title("10%\n\nDistricts with at least one case, days 1-5")
    ax1 = plt.subplot(4, 4, 3)
    spatialDf.plot(ax=ax1, column='v3_25_has_cases_at_5', cmap='Greens', edgecolor='k', legend=False,
                   vmax=1)
    ax1.axis('off')

    plt.title("25%\n\nDistricts with at least one case, days 1-5")
    ax1 = plt.subplot(4, 4, 4)
    spatialDf.plot(ax=ax1, column='v3_50_has_cases_at_5', cmap='Greens', edgecolor='k', legend=False,
                   vmax=1)
    ax1.axis('off')

    plt.title("50%\n\nDistricts with at least one case, days 1-5")
    ax1 = plt.subplot(4, 4, 5)
    spatialDf.plot(ax=ax1, column='v3_5_has_cases_at_10', cmap='Greens', edgecolor='k', legend=False,
                   vmax=1)
    ax1.axis('off')

    plt.title("Districts with at least one case, days 1-10")
    ax1 = plt.subplot(4, 4, 6)
    spatialDf.plot(ax=ax1, column='v3_10_has_cases_at_10', cmap='Greens', edgecolor='k', legend=False,
                   vmax=1)
    ax1.axis('off')

    plt.title("Districts with at least one case, days 1-10")
    ax1 = plt.subplot(4, 4, 7)
    spatialDf.plot(ax=ax1, column='v3_25_has_cases_at_10', cmap='Greens', edgecolor='k', legend=False,
                   vmax=1)
    ax1.axis('off')

    plt.title("Districts with at least one case, days 1-10")
    ax1 = plt.subplot(4, 4, 8)
    spatialDf.plot(ax=ax1, column='v3_50_has_cases_at_10', cmap='Greens', edgecolor='k', legend=False,
                   vmax=1)
    ax1.axis('off')

    plt.title("Districts with at least one case, days 1-10")
    ax1 = plt.subplot(4, 4, 9)
    spatialDf.plot(ax=ax1, column='v3_5_has_cases_at_15', cmap='Greens', edgecolor='k', legend=False,
                   vmax=1)
    ax1.axis('off')

    plt.title("Districts with at least one case, days 1-15")
    ax1 = plt.subplot(4, 4, 10)
    spatialDf.plot(ax=ax1, column='v3_10_has_cases_at_15', cmap='Greens', edgecolor='k', legend=False,
                   vmax=1)
    ax1.axis('off')

    plt.title("Districts with at least one case, days 1-15")
    ax1 = plt.subplot(4, 4, 11)
    spatialDf.plot(ax=ax1, column='v3_25_has_cases_at_15', cmap='Greens', edgecolor='k', legend=False,
                   vmax=1)
    ax1.axis('off')

    plt.title("Districts with at least one case, days 1-15")
    ax1 = plt.subplot(4, 4, 12)
    spatialDf.plot(ax=ax1, column='v3_50_has_cases_at_15', cmap='Greens', edgecolor='k', legend=False,
                   vmax=1)
    ax1.axis('off')

    plt.title("Districts with at least one case, days 1-15")

    ax1 = plt.subplot(4, 4, 13)
    spatialDf.plot(ax=ax1, column='v3_5_has_cases_at_20', cmap='Greens', edgecolor='k', legend=False,
                   vmax=1)
    ax1.axis('off')

    plt.title("Districts with at least one case, days 1-20")
    ax1 = plt.subplot(4, 4, 14)
    spatialDf.plot(ax=ax1, column='v3_10_has_cases_at_20', cmap='Greens', edgecolor='k', legend=False,
                   vmax=1)
    ax1.axis('off')

    plt.title("Districts with at least one case, days 1-20")
    ax1 = plt.subplot(4, 4, 15)
    spatialDf.plot(ax=ax1, column='v3_25_has_cases_at_20', cmap='Greens', edgecolor='k', legend=False,
                   vmax=1)
    ax1.axis('off')

    plt.title("Districts with at least one case, days 1-20")
    ax1 = plt.subplot(4, 4, 16)
    spatialDf.plot(ax=ax1, column='v3_50_has_cases_at_20', cmap='Greens', edgecolor='k', legend=False,
                   vmax=1)
    ax1.axis('off')

    plt.title("Districts with at least one case, days 1-20")
    plt.savefig(savepath + "figure_5.png", dpi=400, bbox_inches='tight')
    plt.clf()
    plt.close(fig)
    print("")



iccs_graphs(onedrive_file_path, graphsavepath, zimFile)


def create_gif_of_cases_with_confidence_interval_v1(filepath, max_n_runs, samplesize, savepath, gifsavepath,
                                                    gif_file_name):

    for i in range(2, max_n_runs + 1):
        mean, std = sample_output_from_runs(filepath, samplesize, i)
        upper = mean.new_cases.values + (1.96 / np.sqrt(i)) * std.new_cases.values
        lower = mean.new_cases.values - (1.96 / np.sqrt(i)) * std.new_cases.values

        plt.plot(mean.index, mean.new_cases, color='r', label='Mean cases')
        plt.fill_between(mean.index, lower, upper, color='r', alpha=0.5, label='95% C.I.')
        plt.xlabel('Time')
        plt.ylim([0, 650000])
        plt.ylabel('Number of cases')
        plt.title(f"Mean number of cases and 95% C.I., sampling {i} runs")
        plt.savefig(gifsavepath + f"/cases_{i}_samples.png", bbox_inches='tight')
        plt.clf()
    graph_path = gifsavepath
    files = os.listdir(graph_path)
    files.sort(key=lambda f: int(re.sub('\D', '', f)))
    images = []

    for file in files:
        im = Image.open(graph_path + file)
        draw = ImageDraw.Draw(im)
        images.append(im)

    images[0].save(savepath + gif_file_name,
                   save_all=True, append_images=images[1:], optimize=False, duration=100, loop=0)


def create_gif_of_deaths_with_confidence_interval_v1(filepath, max_n_runs, samplesize, savepath, gifsavepath,
                                                     gif_file_name):

    for i in range(2, max_n_runs + 1):
        mean, std = sample_output_from_runs(filepath, samplesize, i)
        upper = mean.metric_new_deaths.values + (1.96 / np.sqrt(i)) * std.metric_new_deaths.values
        lower = mean.metric_new_deaths.values - (1.96 / np.sqrt(i)) * std.metric_new_deaths.values

        plt.plot(mean.index, mean.metric_new_deaths, color='r', label='Mean cases')
        plt.fill_between(mean.index, lower, upper, color='r', alpha=0.5, label='95% C.I.')
        plt.xlabel('Time')
        plt.ylim([0, 2000])
        plt.ylabel('Number of deaths')
        plt.title(f"Mean number of deaths and 95% C.I., sampling {i} runs")
        plt.savefig(gifsavepath + f"/deaths_{i}_samples.png", bbox_inches='tight')
        plt.clf()
    graph_path = gifsavepath
    files = os.listdir(graph_path)
    files.sort(key=lambda f: int(re.sub('\D', '', f)))
    images = []

    for file in files:
        im = Image.open(graph_path + file)
        draw = ImageDraw.Draw(im)
        images.append(im)

    images[0].save(savepath + gif_file_name,
                   save_all=True, append_images=images[1:], optimize=False, duration=100, loop=0)


def quick_n_runs_analysis(desktoppath, savepath):
    v1_5_output_file_path = desktoppath + \
                            "five/output/"
    gif_path = savepath + f"gifs/v1_5_effect_of_number_of_runs/"

    # create_gif_of_cases_with_confidence_interval_v1(v1_5_output_file_path, 30, 5, savepath, gif_path,
    #                                                 "effect_of_number_of_runs_v1_5.gif")
    gif_path = savepath + f"gifs/v1_5_effect_of_number_of_runs_deaths/"

    # create_gif_of_deaths_with_confidence_interval_v1(v1_5_output_file_path, 30, 5, savepath, gif_path,
    #                                                  "effect_of_number_of_runs_v1_5_deaths.gif")
    # # 10%
    v1_10_output_file_path = desktoppath + "ten/output/"
    gif_path = savepath + f"gifs/v1_10_effect_of_number_of_runs/"

    # create_gif_of_cases_with_confidence_interval_v1(v1_10_output_file_path, 30, 10, savepath, gif_path,
    #                                                 "effect_of_number_of_runs_v1_10.gif")
    gif_path = savepath + f"gifs/v1_10_effect_of_number_of_runs_deaths/"

    # create_gif_of_deaths_with_confidence_interval_v1(v1_10_output_file_path, 30, 10, savepath, gif_path,
    #                                                  "effect_of_number_of_runs_v1_10_deaths.gif")
    # 25%
    v1_25_output_file_path = desktoppath + "twentyfive/output/"
    gif_path = savepath + f"gifs/v1_25_effect_of_number_of_runs/"

    # create_gif_of_cases_with_confidence_interval_v1(v1_25_output_file_path, 30, 25, savepath, gif_path,
    #                                                 "effect_of_number_of_runs_v1_25.gif")
    gif_path = savepath + f"gifs/v1_25_effect_of_number_of_runs_deaths/"

    # create_gif_of_deaths_with_confidence_interval_v1(v1_25_output_file_path, 30, 25, savepath, gif_path,
    #                                                  "effect_of_number_of_runs_v1_25_deaths.gif")
    # 50%
    v1_50_output_file_path = desktoppath + "fifty/output/"

    gif_path = savepath + f"gifs/v1_50_effect_of_number_of_runs/"

    # create_gif_of_cases_with_confidence_interval_v1(v1_50_output_file_path, 30, 50, savepath, gif_path,
    #                                                 "effect_of_number_of_runs_v1_50.gif")
    gif_path = savepath + f"gifs/v1_50_effect_of_number_of_runs_deaths/"

    # create_gif_of_deaths_with_confidence_interval_v1(v1_50_output_file_path, 30, 50, savepath, gif_path,
    #                                                  "effect_of_number_of_runs_v1_50_deaths.gif")

    for i in range(1, 30 + 1):
        v1_10_mean, v1_10_std = sample_output_from_runs(v1_10_output_file_path, 10, 10)
        v1_5_mean, v1_5_std = sample_output_from_runs(v1_5_output_file_path, 5, 10)
        v1_10_mean_cumulative = np.cumsum(v1_10_mean.new_cases.values)
        v1_5_mean_cumulative = np.cumsum(v1_5_mean.new_cases.values)

        cases_ratio = np.divide(v1_10_mean_cumulative, v1_5_mean_cumulative)
        plt.plot(v1_5_mean.index, cases_ratio, color='lightsteelblue')
        plt.axhline(1, color='lightsalmon')
        plt.xlabel('time')
        plt.ylabel('Ratio')
        plt.title(f"The ratio of cases between the 10% and 5% census runs\naverage cases from 10 runs, sample {i}")
        plt.ylim([0.5, 1.5])
        plt.xlim([0, 100])
        plt.savefig(savepath + f"gifs/v1_cases_ratio_stochasticity_5_10/sample_{i}_runs", bbox_inches='tight')
        plt.clf()
    #
    graph_path = savepath + f"gifs/v1_cases_ratio_stochasticity_5_10/"
    files = os.listdir(graph_path)
    files.sort(key=lambda f: int(re.sub('\D', '', f)))
    images = []

    for file in files:
        im = Image.open(graph_path + file)
        draw = ImageDraw.Draw(im)
        images.append(im)

    images[0].save(savepath + "ratio_and_runs_5_10.gif", save_all=True, append_images=images[1:], optimize=False,
                   duration=100, loop=0)
    for i in range(1, 30 + 1):
        v1_25_mean, v1_25_std = sample_output_from_runs(v1_25_output_file_path, 25, 10)
        v1_5_mean, v1_5_std = sample_output_from_runs(v1_5_output_file_path, 5, 10)
        v1_25_mean_cumulative = np.cumsum(v1_25_mean.new_cases.values)
        v1_5_mean_cumulative = np.cumsum(v1_5_mean.new_cases.values)

        cases_ratio = np.divide(v1_25_mean_cumulative, v1_5_mean_cumulative)
        plt.plot(v1_5_mean.index, cases_ratio, color='lightsteelblue')
        plt.axhline(1, color='lightsalmon')
        plt.xlabel('time')
        plt.ylabel('Ratio')
        plt.title(f"The ratio of cases between the 25% and 5% census runs\naverage cases from 10 runs, sample {i}")
        plt.ylim([0.5, 1.5])
        plt.xlim([0, 100])
        plt.savefig(savepath + f"gifs/v1_cases_ratio_stochasticity_5_25/sample_{i}_runs", bbox_inches='tight')
        plt.clf()

    graph_path = savepath + f"gifs/v1_cases_ratio_stochasticity_5_25/"
    files = os.listdir(graph_path)
    files.sort(key=lambda f: int(re.sub('\D', '', f)))
    images = []

    for file in files:
        im = Image.open(graph_path + file)
        draw = ImageDraw.Draw(im)
        images.append(im)

    images[0].save(savepath + "ratio_and_runs_5_25.gif", save_all=True, append_images=images[1:], optimize=False,
                   duration=100, loop=0)
    for i in range(1, 30 + 1):
        v1_50_mean, v1_50_std = sample_output_from_runs(v1_50_output_file_path, 50, 10)
        v1_5_mean, v1_5_std = sample_output_from_runs(v1_5_output_file_path, 5, 10)
        v1_50_mean_cumulative = np.cumsum(v1_50_mean.new_cases.values)
        v1_5_mean_cumulative = np.cumsum(v1_5_mean.new_cases.values)
        cases_ratio = np.divide(v1_50_mean_cumulative, v1_5_mean_cumulative)
        plt.plot(v1_5_mean.index, cases_ratio, color='lightsteelblue')
        plt.axhline(1, color='lightsalmon')
        plt.xlabel('time')
        plt.ylabel('Ratio')
        plt.title(f"The ratio of cases between the 50% and 5% census runs\naverage cases from 10 runs, sample {i}")
        plt.ylim([0, 2])
        plt.xlim([0, 100])
        plt.savefig(savepath + f"gifs/v1_cases_ratio_stochasticity_5_50/sample_{i}_runs", bbox_inches='tight')
        plt.clf()

    graph_path = savepath + f"gifs/v1_cases_ratio_stochasticity_5_50/"
    files = os.listdir(graph_path)
    files.sort(key=lambda f: int(re.sub('\D', '', f)))
    images = []

    for file in files:
        im = Image.open(graph_path + file)
        draw = ImageDraw.Draw(im)
        images.append(im)

    images[0].save(savepath + "ratio_and_runs_5_50.gif", save_all=True, append_images=images[1:], optimize=False,
                   duration=100, loop=0)

    # for i in range(1, 30 + 1):
    #     v1_5_files = os.listdir(v1_5_output_file_path)
    #     v1_5_files.sort(key=lambda f: int(re.sub('\D', '', f)))
    #     v1_5_df = pd.DataFrame()
    #     for file in v1_5_files:
    #         data = pd.read_csv(v1_5_output_file_path + file, delimiter='\t')
    #         data = data.drop('Unnamed: 10', axis=1)
    #         storage_df = storage_df.append(data)
    #
    #     storage_df = storage_df.groupby('time').mean()
    #     # Scale the outputs
    #     storage_df *= 100 / sample_size
    #     storage_df['new_cases'] = storage_df['metric_new_cases_asympt'] + storage_df['metric_new_cases_sympt']
    #     v1_50_mean, v1_50_std = average_output_from_runs(v1_50_output_file_path, 50)
    #     v1_5_mean, v1_5_std = average_output_from_runs(v1_5_output_file_path, 5)
    #     cases_ratio = np.divide(v1_5_mean.new_cases.values, v1_50_mean.new_cases.values)
    #     cases_ratio = [i if i is not np.nan else 1 for i in cases_ratio]
    #     plt.plot(v1_5_mean.index, cases_ratio, color='lightsteelblue')
    #     plt.axhline(1, color='lightsalmon')
    #     plt.xlabel('time')
    #     plt.ylabel('Ratio')
    #     plt.title(f"The ratio of cases between the 50% and 5% census runs\naverage cases from 10 runs, sample {i}")
    #     plt.ylim([0, 2])
    #     plt.xlim([0, 100])
    #     plt.savefig(savepath + f"gifs/v1_cases_ratio_stochasticity_5_50/sample_{i}_runs", bbox_inches='tight')
    #     plt.clf()
    #
    # graph_path = savepath + f"gifs/v1_cases_ratio_stochasticity_5_50/"
    # files = os.listdir(graph_path)
    # files.sort(key=lambda f: int(re.sub('\D', '', f)))
    # images = []
    #
    # for file in files:
    #     im = Image.open(graph_path + file)
    #     draw = ImageDraw.Draw(im)
    #     images.append(im)
    #
    # images[0].save(savepath + "ratio_and_runs_5_50.gif", save_all=True, append_images=images[1:], optimize=False,
    #                duration=100, loop=0)



# quick_n_runs_analysis("/Users/robbiework/Desktop/", graphsavepath)


def compare_betas(remote_folder_path, savepath, zimfilepath):

    v3_50_03_output_file_path = remote_folder_path + \
                                "output/ICCS/extended_submission/ver_3 (multiDist)/50_perc/beta_0.3/output/"
    v3_50_003_output_file_path = remote_folder_path + \
                                 "output/ICCS/extended_submission/ver_3 (multiDist)/50_perc/beta_0.03/output/"
    v3_50_0003_output_file_path = remote_folder_path + \
                                  "output/ICCS/extended_submission/ver_3 (multiDist)/50_perc/beta_0.003/output/"
    create_compare_betas_plots(v3_50_03_output_file_path, v3_50_003_output_file_path, v3_50_0003_output_file_path,
                               savepath, "50", zimfilepath=zimfilepath)
    v3_25_03_output_file_path = remote_folder_path + \
                                "output/ICCS/extended_submission/ver_3 (multiDist)/25_perc/beta_0.3/output/"
    v3_25_003_output_file_path = remote_folder_path + \
                                 "output/ICCS/extended_submission/ver_3 (multiDist)/25_perc/beta_0.03/output/"
    v3_25_0003_output_file_path = remote_folder_path + \
                                  "output/ICCS/extended_submission/ver_3 (multiDist)/25_perc/beta_0.003/output/"
    create_compare_betas_plots(v3_25_03_output_file_path, v3_25_003_output_file_path, v3_25_0003_output_file_path,
                               savepath, "25", zimfilepath=zimfilepath)
    v3_10_03_output_file_path = remote_folder_path + \
                                "output/ICCS/extended_submission/ver_3 (multiDist)/10_perc/beta_0.3/output/"
    v3_10_003_output_file_path = remote_folder_path + \
                                 "output/ICCS/extended_submission/ver_3 (multiDist)/10_perc/beta_0.03/output/"
    v3_10_0003_output_file_path = remote_folder_path + \
                                  "output/ICCS/extended_submission/ver_3 (multiDist)/10_perc/beta_0.003/output/"
    create_compare_betas_plots(v3_10_03_output_file_path, v3_10_003_output_file_path, v3_10_0003_output_file_path,
                               savepath, "10", zimfilepath=zimfilepath)
    v3_5_03_output_file_path = remote_folder_path + \
                                "output/ICCS/extended_submission/ver_3 (multiDist)/5_perc/beta_0.3/output/"
    v3_5_003_output_file_path = remote_folder_path + \
                                 "output/ICCS/extended_submission/ver_3 (multiDist)/5_perc/beta_0.03/output/"
    v3_5_0003_output_file_path = remote_folder_path + \
                                  "output/ICCS/extended_submission/ver_3 (multiDist)/5_perc/beta_0.003/output/"
    create_compare_betas_plots(v3_5_03_output_file_path, v3_5_003_output_file_path, v3_5_0003_output_file_path,
                               savepath, "5", zimfilepath=zimfilepath)
    print("")


def calculate_median_cases_in_time_range(filepath, timemin, timemax, scale):
    storage_df = pd.DataFrame()
    for file in os.listdir(filepath):
        data = pd.read_csv(filepath + file, delimiter='\t')
        data = data.drop('Unnamed: 10', axis=1)
        storage_df = storage_df.append(data)
    storage_df = storage_df.loc[(storage_df['time'] < timemax) & (storage_df['time'] >= timemin)]
    storage_df['new_cases'] = storage_df['metric_new_cases_sympt'] + storage_df['metric_new_cases_asympt']
    storage_df['new_cases'] *= 100 / scale
    storage_df = storage_df.groupby('myId').median()
    storage_df['district_number'] = [int(idx[2:]) for idx in storage_df.index]
    storage_df = storage_df.sort_values('district_number')
    return storage_df


def calculate_has_cases_in_space(filepath, timemax):
    storage_df = pd.DataFrame()
    for file in os.listdir(filepath):
        data = pd.read_csv(filepath + file, delimiter='\t')
        data = data.drop('Unnamed: 10', axis=1)
        data['cumulative_cases'] = [0] * len(data)
        data['new_cases'] = data['metric_new_cases_asympt'] + data['metric_new_cases_sympt']
        data['has_cases'] = [0] * len(data)
        for district in data.myId.unique():
            cases_in_district = data.loc[data['myId'] == district]
            data.loc[cases_in_district.index, 'cumulative_cases'] = np.cumsum(cases_in_district['new_cases'].values)
            data.loc[cases_in_district.index, 'has_cases'] = \
                [1 if val > 0 else 0 for val in np.cumsum(cases_in_district['new_cases'].values)]
        storage_df = storage_df.append(data)
    storage_df['district_number'] = [int(dist[2:]) for dist in storage_df.myId.values]
    storage_df = storage_df.loc[storage_df['time'] == timemax]
    storage_df = storage_df.groupby('myId').mean()
    storage_df = storage_df.sort_values('district_number')
    return storage_df


def create_compare_betas_plots(filepath_03, filepath_003, filepath_0003, savepath, samplesize, zimfilepath):
    zimbabwe = geopandas.read_file(zimfilepath)
    spatialDf = zimbabwe.copy()
    beta_03_outputs_0_29 = calculate_median_cases_in_time_range(filepath_03, 0, 30, 50)
    beta_03_outputs_30_59 = calculate_median_cases_in_time_range(filepath_03, 30, 60, 50)
    beta_03_outputs_60_plus = calculate_median_cases_in_time_range(filepath_03, 60, 100, 50)
    spatialDf['03_0_29'] = beta_03_outputs_0_29.new_cases.values
    spatialDf['03_30_59'] = beta_03_outputs_30_59.new_cases.values
    spatialDf['03_60_plus'] = beta_03_outputs_60_plus.new_cases.values
    beta_003_outputs_0_29 = calculate_median_cases_in_time_range(filepath_003, 0, 30, 50)
    beta_003_outputs_30_59 = calculate_median_cases_in_time_range(filepath_003, 30, 60, 50)
    beta_003_outputs_60_plus = calculate_median_cases_in_time_range(filepath_003, 60, 100, 50)
    spatialDf['003_0_29'] = beta_003_outputs_0_29.new_cases.values
    spatialDf['003_30_59'] = beta_003_outputs_30_59.new_cases.values
    spatialDf['003_60_plus'] = beta_003_outputs_60_plus.new_cases.values
    beta_0003_outputs_0_29 = calculate_median_cases_in_time_range(filepath_0003, 0, 30, 50)
    beta_0003_outputs_30_59 = calculate_median_cases_in_time_range(filepath_0003, 30, 60, 50)
    beta_0003_outputs_60_plus = calculate_median_cases_in_time_range(filepath_0003, 60, 100, 50)
    spatialDf['0003_0_29'] = beta_0003_outputs_0_29.new_cases.values
    spatialDf['0003_30_59'] = beta_0003_outputs_30_59.new_cases.values
    spatialDf['0003_60_plus'] = beta_0003_outputs_60_plus.new_cases.values
    # plot the spatial spread with smaller betas
    fig = plt.figure(figsize=(16, 16), constrained_layout=True)
    (subfig1, subfig2, subfig3) = fig.subfigures(1, 3)
    (ax11, ax21, ax31) = subfig1.subplots(3, 1)
    spatialDf.plot(ax=ax11, column='03_0_29', cmap='Blues', edgecolor='k', legend=True,
                   vmax=max([spatialDf['03_0_29'].values.max(), spatialDf['03_30_59'].values.max(),
                                 spatialDf['03_60_plus'].values.max()]))
    for key, spine in ax11.spines.items():
        spine.set_visible(False)
    ax11.set_xticks([], [])
    ax11.set_yticks([], [])
    ax11.set_ylabel("Days 1-30", weight='bold', fontsize=25)
    spatialDf.plot(ax=ax21, column='03_30_59', cmap='Blues', edgecolor='k', legend=True,
                   vmax=max([spatialDf['03_0_29'].values.max(), spatialDf['03_30_59'].values.max(),
                                 spatialDf['03_60_plus'].values.max()]))
    for key, spine in ax21.spines.items():
        spine.set_visible(False)
    ax21.set_xticks([], [])
    ax21.set_yticks([], [])
    ax21.set_ylabel("Days 31-60", weight='bold', fontsize=25)
    spatialDf.plot(ax=ax31, column='03_60_plus', cmap='Blues', edgecolor='k', legend=True,
                   vmax=max([spatialDf['03_0_29'].values.max(), spatialDf['03_30_59'].values.max(),
                                 spatialDf['03_60_plus'].values.max()]))
    for key, spine in ax31.spines.items():
        spine.set_visible(False)
    ax31.set_xticks([], [])
    ax31.set_yticks([], [])
    ax31.set_ylabel("Days 60-100", weight='bold', fontsize=25)
    subfig1.suptitle('β = 0.3', weight='bold', fontsize=25)
    (ax12, ax22, ax32) = subfig2.subplots(3, 1)
    spatialDf.plot(ax=ax12, column='003_0_29', cmap='Greens', edgecolor='k', legend=True,
                   vmax=max([spatialDf['003_0_29'].values.max(), spatialDf['003_30_59'].values.max(),
                                 spatialDf['003_60_plus'].values.max()]))
    ax12.axis('off')
    spatialDf.plot(ax=ax22, column='003_30_59', cmap='Greens', edgecolor='k', legend=True,
                   vmax=max([spatialDf['003_0_29'].values.max(), spatialDf['003_30_59'].values.max(),
                                 spatialDf['003_60_plus'].values.max()]))
    ax22.axis('off')
    spatialDf.plot(ax=ax32, column='003_60_plus', cmap='Greens', edgecolor='k', legend=True,
                   vmax=max([spatialDf['003_0_29'].values.max(), spatialDf['003_30_59'].values.max(),
                                 spatialDf['003_60_plus'].values.max()]))
    ax32.axis('off')
    subfig2.suptitle('β = 0.03', weight='bold', fontsize=25)
    (ax13, ax23, ax33) = subfig3.subplots(3, 1)
    spatialDf.plot(ax=ax13, column='0003_0_29', cmap='Reds', edgecolor='k', legend=True,
                   vmax=max([spatialDf['0003_0_29'].values.max(), spatialDf['0003_30_59'].values.max(),
                                 spatialDf['0003_60_plus'].values.max()]))
    ax13.axis('off')
    spatialDf.plot(ax=ax23, column='0003_30_59', cmap='Reds', edgecolor='k', legend=True,
                   vmax=max([spatialDf['0003_0_29'].values.max(), spatialDf['0003_30_59'].values.max(),
                                 spatialDf['0003_60_plus'].values.max()]))
    ax23.axis('off')
    spatialDf.plot(ax=ax33, column='0003_60_plus', cmap='Reds', edgecolor='k', legend=True,
                   vmax=max([spatialDf['0003_0_29'].values.max(), spatialDf['0003_30_59'].values.max(),
                                 spatialDf['0003_60_plus'].values.max()]))
    ax33.axis('off')
    subfig3.suptitle('β = 0.003', weight='bold', fontsize=25)
    plt.savefig(savepath + samplesize + "_perc_compare_median_case_numbers_betas.png")
    plt.close(fig)
    plt.clf()
    beta_03_has_cases_5 = calculate_has_cases_in_space(filepath_03, 5)
    beta_03_has_cases_10 = calculate_has_cases_in_space(filepath_03, 10)
    beta_03_has_cases_20 = calculate_has_cases_in_space(filepath_03, 20)
    spatialDf['03_has_cases_5'] = np.round(beta_03_has_cases_5.has_cases.values)
    spatialDf['03_has_cases_10'] = np.round(beta_03_has_cases_10.new_cases.values)
    spatialDf['03_has_cases_20'] = np.round(beta_03_has_cases_20.new_cases.values)
    beta_003_has_cases_5 = calculate_has_cases_in_space(filepath_003, 5)
    beta_003_has_cases_10 = calculate_has_cases_in_space(filepath_003, 10)
    beta_003_has_cases_20 = calculate_has_cases_in_space(filepath_003, 20)
    spatialDf['003_has_cases_5'] = np.round(beta_003_has_cases_5.new_cases.values)
    spatialDf['003_has_cases_10'] = np.round(beta_003_has_cases_10.new_cases.values)
    spatialDf['003_has_cases_20'] = np.round(beta_003_has_cases_20.new_cases.values)
    beta_0003_has_cases_5 = calculate_has_cases_in_space(filepath_0003, 5)
    beta_0003_has_cases_10 = calculate_has_cases_in_space(filepath_0003, 10)
    beta_0003_has_cases_20 = calculate_has_cases_in_space(filepath_0003, 20)
    spatialDf['0003_has_cases_5'] = np.round(beta_0003_has_cases_5.new_cases.values)
    spatialDf['0003_has_cases_10'] = np.round(beta_0003_has_cases_10.new_cases.values)
    spatialDf['0003_has_cases_20'] = np.round(beta_0003_has_cases_20.new_cases.values)
    # plot the spatial spread with smaller betas
    fig = plt.figure(figsize=(16, 16), constrained_layout=True)
    (subfig1, subfig2, subfig3) = fig.subfigures(1, 3)
    (ax11, ax21, ax31) = subfig1.subplots(3, 1)
    spatialDf.plot(ax=ax11, column='03_has_cases_5', cmap='Blues', edgecolor='k', legend=False,
                   vmax=1)
    for key, spine in ax11.spines.items():
        spine.set_visible(False)
    ax11.set_xticks([], [])
    ax11.set_yticks([], [])
    ax11.set_ylabel("Days 1-5", weight='bold', fontsize=25)
    spatialDf.plot(ax=ax21, column='03_has_cases_10', cmap='Blues', edgecolor='k', legend=False,
                   vmax=1)
    for key, spine in ax21.spines.items():
        spine.set_visible(False)
    ax21.set_xticks([], [])
    ax21.set_yticks([], [])
    ax21.set_ylabel("Days 1-10", weight='bold', fontsize=25)
    spatialDf.plot(ax=ax31, column='03_has_cases_20', cmap='Blues', edgecolor='k', legend=False,
                   vmax=1)
    for key, spine in ax31.spines.items():
        spine.set_visible(False)
    ax31.set_xticks([], [])
    ax31.set_yticks([], [])
    ax31.set_ylabel("Days 1-20", weight='bold', fontsize=25)
    subfig1.suptitle('β = 0.3', weight='bold', fontsize=25)
    (ax12, ax22, ax32) = subfig2.subplots(3, 1)
    spatialDf.plot(ax=ax12, column='003_has_cases_5', cmap='Greens', edgecolor='k', legend=False,
                   vmax=1)
    ax12.axis('off')
    spatialDf.plot(ax=ax22, column='003_has_cases_10', cmap='Greens', edgecolor='k', legend=False,
                   vmax=1)
    ax22.axis('off')
    spatialDf.plot(ax=ax32, column='003_has_cases_20', cmap='Greens', edgecolor='k', legend=False,
                   vmax=1)
    ax32.axis('off')
    subfig2.suptitle('β = 0.03', weight='bold', fontsize=25)
    (ax13, ax23, ax33) = subfig3.subplots(3, 1)
    spatialDf.plot(ax=ax13, column='0003_has_cases_5', cmap='Reds', edgecolor='k', legend=False,
                   vmax=1)
    ax13.axis('off')
    spatialDf.plot(ax=ax23, column='0003_has_cases_10', cmap='Reds', edgecolor='k', legend=False,
                   vmax=1)
    ax23.axis('off')
    spatialDf.plot(ax=ax33, column='0003_has_cases_20', cmap='Reds', edgecolor='k', legend=False,
                   vmax=1)
    ax33.axis('off')
    subfig3.suptitle('β = 0.003', weight='bold', fontsize=25)
    plt.savefig(savepath + samplesize + "_perc_compare_district_spread_betas.png")
    plt.close(fig)
    plt.clf()


#compare_betas(onedrive_file_path, graphsavepath, zimFile)

