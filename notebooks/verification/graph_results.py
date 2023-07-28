import pandas as pd
import numpy as np
from datetime import datetime, timedelta
import matplotlib as mpl
import matplotlib.pyplot as plt
import os
import pylab as plt
from os.path import isfile, join
from glob import glob
import subprocess
import re

dataDir = "../data/verification/output/"
exportDir = dataDir + "sweep/"
path= "/home/wb488473/Disease-Modelling-SSA/data/verification/output/sweep/"

import seaborn as sns
sns.set(style="darkgrid")



def plotMetric(files, prefix, suffix, metrics = [], symbol = '-', alpha = 1, weighting = 1):
    myAvgLine = []
    index = 0
    template = "^" + prefix + ".*" + suffix + "$"

    for f in files:

        if(re.search(template, f) != None): # f.startswith(prefix) and 
            r_data = pd.read_csv(path + f, sep='\t')

            totcases = []
            for metric in metrics:
                if len(totcases) == 0:
                    totcases = r_data[metric]
                else:        
                    totcases = totcases + r_data[metric]

            if len(myAvgLine) == 0:
                myAvgLine = totcases
            else:
                myAvgLine += totcases
            
            # plot it
            plt.plot(r_data["time"][1:], weighting * totcases[1:], symbol, alpha=alpha)
            index += 1
    return {"myLine": weighting * myAvgLine/index, "myTime": r_data["time"]}


files = os.listdir(exportDir)

print(files)



prefix_5p_30b = "params_5perc_bubbles_30"
prefix_5p_60b = "params_5perc_bubbles_60"
prefix_5p_perf = "params_5perc_perfectMixing"

prefix_20p_30b = "params_20perc_bubbles_30"
prefix_20p_60b = "params_20perc_bubbles_60"
prefix_20p_perf = "params_20perc_perfectMixing"

#
# compare different bubble sizes
#

myMetrics = ["metric_new_cases_sympt", "metric_new_cases_asympt"]
# 5% version
line1 = plotMetric(files, prefix_5p_30b, "0.1.txt", myMetrics, 'r-', 0.15)
line2 = plotMetric(files, prefix_5p_60b, "0.1.txt", myMetrics, 'b-', 0.15)
line3 = plotMetric(files, prefix_5p_perf, "0.1.txt", myMetrics, 'g-', 0.15)

plt.plot(line1["myTime"][1:], line1['myLine'][1:], 'r', label="30 People")
plt.plot(line2["myTime"][1:], line2['myLine'][1:], 'b', label="60 People")
plt.plot(line3["myTime"][1:], line3['myLine'][1:], 'g', label="Perfect Mixing")

plt.title("5% Population with Beta 0.1")
plt.legend()

plt.savefig(path + "5perc_comparison.png", dpi=300)

# 20% version
line1 = plotMetric(files, prefix_20p_30b, "0.1.txt", myMetrics, 'r-', 0.15)
line2 = plotMetric(files, prefix_20p_60b, "0.1.txt", myMetrics, 'b-', 0.15)
line3 = plotMetric(files, prefix_20p_perf, "0.1.txt", myMetrics, 'g-', 0.15)

plt.plot(line1["myTime"][1:], line1['myLine'][1:], 'r', label="30 People")
plt.plot(line2["myTime"][1:], line2['myLine'][1:], 'b', label="60 People")
plt.plot(line3["myTime"][1:], line3['myLine'][1:], 'g', label="Perfect Mixing")

plt.title("20% Population with Beta 0.1")
plt.legend()

plt.savefig(path + "20perc_comparison.png", dpi=300)
