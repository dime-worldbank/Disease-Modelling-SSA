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

def processModelOutput(filename):
    r_data = pd.read_csv(filename, sep='\t')
    r_data['districtId'] = [int(str(x).split('_')[1]) for x in r_data["myId"]]
    return {'results': r_data}

def processInfections(filename):
    r_data = pd.read_csv(filename, sep='\t')
    return {'results': r_data}
    

def findROFInstance():
    # total number of individuals infected by the introduction of an infectious person
    return beta * n

def WorldBankMASONmodel(myOutputPrefix, seed, paramfile, beta, numDays=90):
    
    dataDir = "/home/wb488473/Disease-Modelling-SSA/data/verification/"
    #"/Users/swise/workspace/worldbank/Disease-Modelling-SSA/data/verification/"
    exportFilename = myOutputPrefix + "_agg_" + str(seed) + "_" + str(beta) + ".txt"
    exportInfectionsFilename = myOutputPrefix + "_Infections_" + str(seed) + "_" + str(beta) + ".txt"
    
    subprocess.call(['java', '-Xms20G', '-jar', '../java/WorldBankCovid19/libs/WB_covid19ABM.jar', str(numDays), 
                     dataDir, str(beta), str(seed), exportFilename, paramfile, exportInfectionsFilename])
    
    # Sophie upped the -Xms to 20G from 6GB
    #return processModelOutput(exportFilename)
    return processInfections(exportInfectionsFilename)

def processModelOutput(filename):
    r_data = pd.read_csv(filename, sep='\t')
    r_data['districtId'] = [int(str(x).split('_')[1]) for x in r_data["myId"]]
    return {'results': r_data}

def processInfections(filename):
    r_data = pd.read_csv(filename, sep='\t')
    return {'results': r_data}
    

def findROFInstance():
    # total number of individuals infected by the introduction of an infectious person
    return beta * n

def WorldBankMASONmodel(myOutputPrefix, seed, paramfile, beta, numDays=90):
    
    dataDir = "/home/wb488473/Disease-Modelling-SSA/data/verification/"
    #"/Users/swise/workspace/worldbank/Disease-Modelling-SSA/data/verification/"
    exportFilename = myOutputPrefix + "_agg_" + str(seed) + "_" + str(beta) + ".txt"
    exportInfectionsFilename = myOutputPrefix + "_Infections_" + str(seed) + "_" + str(beta) + ".txt"
    
    subprocess.call(['java', '-Xms20G', '-jar', '../java/WorldBankCovid19/libs/WB_covid19ABM.jar', str(numDays), 
                     dataDir, str(beta), str(seed), exportFilename, paramfile, exportInfectionsFilename])
    
    # Sophie upped the -Xms to 20G from 6GB
    #return processModelOutput(exportFilename)
    return processInfections(exportInfectionsFilename)
