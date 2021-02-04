#!/usr/bin/env python
# -*- coding: utf-8 -*-

import pandas as pd
import numpy as np
from datetime import datetime, timedelta
import glob
import pylab as plt
import json
import matplotlib as mpl
import os
import pickle


import seaborn as sns
sns.set(style="darkgrid")

import geopandas as gpd


# UTILITIES

# prepare log data for use in analysis
def load_model_logs_df(log_name):
    with open(log_name) as fl:
        content = fl.read()

    data = []
    prev_case = 0
    prev_hosp = 0
    prev_critical = 0

    for days, l in enumerate(content.strip().split('\n')):
        l = l.split(' $$ ')[-1]
        l = json.loads(l)

        total_infected = l['infected_count']
        total_hosp = l['hospitalized_count']
        total_critical = l['critical_count']

        current_case = total_infected - prev_case
        prev_case = total_infected

        current_hosp = total_hosp - prev_hosp
        prev_hosp = total_hosp

        current_critical = total_critical - prev_critical
        prev_critical = total_infected

        l['new_cases'] = current_case
        l['new_hospitalized'] = current_hosp
        l['new_critical'] = current_critical

        l['version'] = log_name
        l['days'] = days
        data.append(l)

    return pd.DataFrame(data)


# read in multiple scenarios and frame them
def process_experiments(R0, scenario_files, cutoff_date=datetime(2020, 12, 31), write_out=False):

	# read in data for each scenario
	for scenario in scenario_files:
	    scenario_code = '_'.join(scenario.split('.')[0].split('_')[2:])
	    scenario_code = scenario_files.get(scenario)
	    scenario_df = pd.DataFrame()
	    
	    myfilenames = glob.glob((f'../../data/logs/model_log_file_{scenario}*_R{R0}_*.log'))
	    
	    for fname in myfilenames:
	        dump_file = fname.replace('model_log_', 'model_dump_').replace('.log', '.pickle')
	        print(dump_file)
	        if not os.path.isfile(fname):#dump_file):
	            continue

	        d = load_model_logs_df(fname)
	        d['scenario'] = scenario_code
	        if scenario_df.empty:
	            scenario_df = d
	        else:
	            scenario_df = pd.concat([scenario_df, d])

	    if scenario_df.empty:
	        print(scenario)
	        continue
	    
	    #scenario_df.head() 
	    
	# work with date data
	scenario_df['date'] = pd.to_datetime(scenario_df['date'])
	max_date = scenario_df['date'].max()
	min_date = scenario_df['date'].min()

	full_dates = scenario_df[scenario_df['version'] == scenario_df[scenario_df['date'] == max_date].iloc[0].version]
	idx = full_dates['date']

	df = pd.DataFrame()

	for v, gdf in scenario_df.groupby('version'):
	    gdf = gdf.set_index('date')

	    gdf = gdf.reindex(idx)
	    gdf = gdf.fillna(method='ffill')
	    gdf = gdf.reset_index()

	    if df.empty:
	        df = gdf
	    else:
	        df = pd.concat([df, gdf])

	df['date'] = pd.to_datetime(df['date'])
	
	df_dec= df#.loc[df['date'] < cutoff_date]


	# possibly export data to file?
	if(write_out):
		df_dec.to_csv(f'../../data/plots/R{R0}_countrywide.csv')
	
	# return data frame
	return df_dec

# plot some variable of the dataframe relative to the date column
def plot_by_date(df, output_file, title, my_key):

	fig = plt.figure(figsize=(10,6), dpi=600)
	ax = fig.add_subplot()

	new_key = my_key.replace('_', ' ')
	df[new_key] = scale * df[my_key]

	sns.lineplot(x="date", y=new_key, hue='scenario',
	             data=df, ax=ax)

	ax.set_title(title, fontsize=20)
	ax.set_ylabel(new_key)
	ax.legend(fontsize=10, title_fontsize=30, loc='upper left')
	ax.yaxis.set_major_formatter(mpl.ticker.StrMethodFormatter('{x:,.0f}'))

	ax.patch.set_alpha(0)
	# ax.axes.get_xaxis().set_visible(False)
	# ax.axes.get_yaxis().set_visible(False)
	plt.xticks(rotation=45)
	plt.tight_layout()
	fig.savefig(output_file)


if __name__ == '__main__':

	# SETUP

	scale = 1

	scenario_files = {
	  #'AcceleratedGovernmentOpenSchoolsScenario': 'Accelerated School Opening - Govt Plan',
	#   'ContinuedLockdownScenario' : 'Continued Lockdown scenario',
	 # 'EasedLockdownScenario': 'Eased Lockdown Scenario',
	  'DynamicPhase1GovernmentOpenSchoolsScenario': 'Dynamic Phase1 Government Open Schools Scenario',
	  'Phase1GovernmentOpenSchoolsScenario': 'Phase1 Government Open Schools Scenario',
	#   'UnmitigatedScenario':'Unmitigated Scenario'
	}

	# PROCESS DATA

	df_14 = process_experiments(1.4, scenario_files)
	df_12 = process_experiments(1.2, scenario_files)
	df_10 = process_experiments(1.0, scenario_files)

	plot_by_date(df_14, '../../data/plots/cumulative_deaths14', 'Cumulative Deaths', 'died_count')
	plot_by_date(df_14, '../../data/plots/daily_new_cases14', 'Daily New Cases', 'new_cases')

	plot_by_date(df_12, '../../data/plots/cumulative_deaths12', 'Cumulative Deaths', 'died_count')
	plot_by_date(df_12, '../../data/plots/daily_new_cases12', 'Daily New Cases', 'new_cases')

	plot_by_date(df_10, '../../data/plots/cumulative_deaths10', 'Cumulative Deaths', 'died_count')
	plot_by_date(df_10, '../../data/plots/daily_new_cases10', 'Daily New Cases', 'new_cases')

	#
	# deal with various R0s
	#

	#Adding R1 column
	df_14["R1"] = 'R1.4'
	df_14_Phase1 = df_14.loc[df_14['scenario'] == 'Phase1 Government Open Schools Scenario']

	#Adding R1 column
	df_12["R1"] = 'R1.2'
	df_12_Phase1 = df_12.loc[df_12['scenario'] == 'Phase1 Government Open Schools Scenario']

	#Adding R1 column
	df_10["R1"] = 'R1.0'
	df_10_Phase1 = df_10.loc[df_10['scenario'] == 'Phase1 Government Open Schools Scenario']

	df_agg = df_14_Phase1.append(df_12_Phase1, ignore_index = True)
	df_agg = df_agg.append(df_10_Phase1, ignore_index = True)


	df_agg.to_csv(f'../../data/plots/RALL_countrywide.csv')


	# PLOTTING

	fig = plt.figure(figsize=(10, 6), dpi=600)
	ax = fig.add_subplot()

	sns.lineplot(x="date", y="died_count", hue="R1", data=df_agg, ax=ax)


	ax.set_title('Cumulative deaths \n Phase1 Government Open Schools Scenario', fontsize=20)
	ax.set_ylabel('Number of deaths')
	ax.legend(fontsize=10, title_fontsize=30, loc='upper left')
	ax.yaxis.set_major_formatter(mpl.ticker.StrMethodFormatter('{x:,.0f}'))

	xticks = ['2020-09-01', '2020-09-15', '2020-10-01', '2020-10-15',
	         '2020-11-01', '2020-11-15', '2020-12-01', '2020-12-15', '2021-01-01']
	ax.xaxis.set_major_locator(mpl.ticker.FixedLocator(xticks))
	ax.xaxis.set_ticks(xticks)


	ax.patch.set_alpha(0)

	plt.xticks(rotation=45)
	plt.tight_layout()
	fig.savefig('../../data/plots/cumulative_deaths')
