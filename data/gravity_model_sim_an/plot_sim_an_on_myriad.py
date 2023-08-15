import pandas as pd
from matplotlib import pyplot as plt
import numpy as np
import os
import sys
folder = os.listdir("/home/rmjlra2/Scratch/sim_an/")

simm_ann_csvs = []
for file in folder:
    if file.startswith('sim_ann_parameter'):
        simm_ann_csvs.append(file)

simm_ann_csvs.sort(key=os.path.getmtime)
results_df = pd.DataFrame()
global_best = [- sys.float_info.max]
for file in simm_ann_csvs:
    data = pd.read_csv("/home/rmjlra2/Scratch/sim_an/"
                       + file)
    current_max = []
    for i in data.Rsqr.values:
        if i > global_best[-1]:
            global_best.append(i)
            current_max.append(i)
        else:
            current_max.append(global_best[- 1])

    data['current_best'] = current_max
    results_df = results_df.append(data)

results_df = results_df.drop(['Unnamed: 0'], axis=1)
results_df['temperature'] = np.divide(100, np.add(1, list(results_df.iteration_number)))
results_df['model_number'] = np.arange(len(results_df))
results_df['colors'] = [[(256 - 150 + i) / 256, 0, 0] for i in list(results_df.temperature)]
results_df.loc[results_df['Rsqr'] == results_df['Rsqr'].max()]
fig = plt.figure(figsize=(12, 6))

(ax1, ax2) = fig.subplots(1, 2)

ax1.plot(np.arange(len(results_df)), results_df.Rsqr, color='r', alpha=0.4)
ax1.plot(np.arange(len(results_df)), results_df.current_best, color='r')
ax1.set_ylabel(u"R\u00b2", weight='bold')
ax1.set_xlabel("Model candidates", weight='bold')
ax1.set_ylim([-1, 1])
legend_entries = []
improved_on_last = [True]
for idx, val in enumerate(results_df.current_best.values):
    if idx == 0:
        pass
    else:
        if val > results_df.current_best.values[idx - 1]:
            improved_on_last.append(True)
        else:
            improved_on_last.append(False)

results_df['improved_on_last'] = improved_on_last

for row in results_df.iterrows():
    if row[1][-1]:
        if row[1][6] == 'alpha' and 'alpha' not in legend_entries:
            ax2.scatter(row[1][9], row[1][2], color='g', marker='.', label=u"\u03b1")
            legend_entries.append('alpha')
        elif row[1][6] == 'alpha':
            ax2.scatter(row[1][9], row[1][2], color='g', marker='.', label="")

        if row[1][6] == 'beta' and 'beta' not in legend_entries:
            ax2.scatter(row[1][9], row[1][3], color='b', marker='.', label=u"\u03b2")
            legend_entries.append('beta')
        elif row[1][6] == 'beta':
            ax2.scatter(row[1][9], row[1][3], color='b', marker='.')

        if row[1][6] == 'gamma' and 'gamma' not in legend_entries:
            ax2.scatter(row[1][9], row[1][4], color='y', marker='.', label=u"\u03B3")
            legend_entries.append('gamma')
        elif row[1][6] == 'gamma':
            ax2.scatter(row[1][9], row[1][4], color='y', marker='.')

        if row[1][6] == 'kappa' and 'kappa' not in legend_entries:
            ax2.scatter(row[1][9], row[1][5], color='r', marker='.', label=u"\u03BA")
            legend_entries.append('kappa')

        elif row[1][6] == 'kappa':
            ax2.scatter(row[1][9], row[1][5], color='r', marker='.')
    else:
        if row[1][6] == 'alpha' and 'alpha' not in legend_entries:
            ax2.scatter(row[1][9], row[1][2], color='g', marker='.', label="", alpha=0.05)
            legend_entries.append('alpha')
        elif row[1][6] == 'alpha':
            ax2.scatter(row[1][9], row[1][2], color='g', marker='.', label="", alpha=0.05)

        if row[1][6] == 'beta' and 'beta' not in legend_entries:
            ax2.scatter(row[1][9], row[1][3], color='b', marker='.', label="", alpha=0.05)
            legend_entries.append('beta')
        elif row[1][6] == 'beta':
            ax2.scatter(row[1][9], row[1][3], color='b', marker='.', alpha=0.05)

        if row[1][6] == 'gamma' and 'gamma' not in legend_entries:
            ax2.scatter(row[1][9], row[1][4], color='y', marker='.', label="", alpha=0.05)
            legend_entries.append('gamma')
        elif row[1][6] == 'gamma':
            ax2.scatter(row[1][9], row[1][4], color='y', marker='.', alpha=0.05)

        if row[1][6] == 'kappa' and 'kappa' not in legend_entries:
            ax2.scatter(row[1][9], row[1][5], color='r', marker='.', label="", alpha=0.05)
            legend_entries.append('kappa')

        elif row[1][6] == 'kappa':
            ax2.scatter(row[1][9], row[1][5], color='r', marker='.', alpha=0.05)

ax2.legend()

ax2.set_xlabel("Model candidates", weight='bold')
ax2.set_ylabel("Parameter value", weight='bold')
fig.suptitle("Simulated annealing parameter fitting of the gravity model", weight='bold')
plt.savefig("/home/rmjlra2/Scratch/sim_an/gravity_model_parameter_fitting.png")

print("params\n")
print("alpha: " + str(list(results_df.loc[results_df['Rsqr'] == results_df['Rsqr'].max()].alpha)[-1]) + "\n")
print("beta: " + str(list(results_df.loc[results_df['Rsqr'] == results_df['Rsqr'].max()].beta)[-1]) + "\n")
print("gamma: " + str(list(results_df.loc[results_df['Rsqr'] == results_df['Rsqr'].max()].gamma)[-1]) + "\n")
print("kappa: " + str(list(results_df.loc[results_df['Rsqr'] == results_df['Rsqr'].max()].kappa)[-1]) + "\n")
print("rsqr: " + str(results_df['Rsqr'].max()))
