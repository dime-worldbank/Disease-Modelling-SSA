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


import seaborn as sns
sns.set(style="darkgrid")

# run the simulation with the given param file

dataDir = "../data/verification/output/"
exportDir = dataDir + "sweep/"

def WorldBankMASONmodel(scenario, seed, paramFile, beta):
    
    numDays = 30 * 6
    exportFilename = exportDir + scenario + "_" + str(seed) + "_" + str(beta) + ".txt"
    #print("ooooh calling it")
    subprocess.call(['java', '-Xms10G', '-jar', '../java/WorldBankCovid19/libs/WB_covid19ABM.jar', str(numDays), dataDir, str(beta), str(seed), exportFilename, paramFile])
    print("written to..." + exportFilename)
    

paramsPerScenario = [ "../data/verification/params_20perc_perfectMixing.txt"]    
prefixes = []

for paramsFile in paramsPerScenario:
    myScenario = paramsFile.split(".")[-2].split("/")[-1]
    prefixes.append(myScenario)
    for i in range(5,10):
        WorldBankMASONmodel(myScenario, i, paramsFile, .1)
