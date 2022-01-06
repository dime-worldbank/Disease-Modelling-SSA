#!/usr/bin/env python
# coding: utf-8

# In[1]:


from ema_workbench import (Model, RealParameter, ScalarOutcome, TimeSeriesOutcome, ArrayOutcome, ema_logging, Constant,
                           perform_experiments)
import subprocess
import pandas as pd
import math

import matplotlib.pyplot as plt


# In[2]:


def processModelOutput(filename):
    r_data = pd.read_csv(filename, sep='\t')
    r_data['districtId'] = [int(str(x).split('_')[1]) for x in r_data["myId"]]
    return {'results': r_data}

def WorldBankMASONmodel(beta):
    
#     seed = str(int(beta))
    seed = "01102021"
    #dataDir = "/Users/swise/workspace/worldbank/Disease-Modelling-SSA/data/"
    dataDir = "/home/wb488473/Disease-Modelling-SSA/data/"
    numDays = 30 * 6
#     beta = 0.03 #0.016
    exportFilename = "exportMe" + seed +"_"+ str(beta) + ".txt"
    subprocess.call(['java', '-Xms20G', '-jar', '../java/WorldBankCovid19/libs/WB_covid19ABM.jar', str(numDays), dataDir, str(beta), seed, exportFilename])
    # Sophie upped the -Xms to 20G from 6GB
    return processModelOutput(exportFilename)


# In[3]:


#myResult = WorldBankMASONmodel(12345) # testing


# In[4]:


from os.path import isfile, join

java_dir = "/home/wb488473/Disease-Modelling-SSA/java/WorldBankCovid19/libs/"


# In[6]:


### ema_logging.LOG_FORMAT = '[%(name)s/%(levelname)s/%(processName)s] %(message)s'
ema_logging.log_to_stderr(ema_logging.INFO)

model = Model('WorldBankMason', function=WorldBankMASONmodel)  # instantiate the model

# specify uncertainties
#model.uncertainties = [RealParameter('rawseed', 1, 10000)]
model.uncertainties = [RealParameter('beta', 0.001, 0.03)]

#model.constants = [
#    Constant('dataDir', "/Users/swise/workspace/worldbank/Disease-Modelling-SSA/data/"),
#    Constant('numDays', 7),
#    Constant('beta', 0.016)
#]

# specify outcomes

model.outcomes = [
    ArrayOutcome('results')
#    TimeSeriesOutcome('metric_died_count'),
#    TimeSeriesOutcome('metric_new_hospitalized'),
#    TimeSeriesOutcome('metric_new_critical'),
#    TimeSeriesOutcome('metric_new_cases_asympt'),
#    TimeSeriesOutcome('metric_new_cases_sympt')
]

results = perform_experiments(model, 10)


# In[ ]:


results[1]

