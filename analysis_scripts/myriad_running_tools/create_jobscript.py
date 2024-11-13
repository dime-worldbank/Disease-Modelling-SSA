import numpy as np
# Specify what you want from the runs
# How many runs per scenario
number_of_runs_per_scenario = 3
# Which parameter files do you want to use in the simulation?
# (NOTE THAT WHATEVER YOU NAME THE PARAMETER FILE NAME AFTER THE FIRST '_' WILL BE USED TO NAME THE OUTPUT FILENAME
params_file_names = ['params_exampleFilename1.txt', 'params_exampleFilename2.txt']


# What value of the infection transmission probability (beta) do you want to use in the simulations
beta = 0.16
# How many days do you want to run this for
days = 100
# Where is the bubble_runs folder on your myriad? (USE PWD TO FIND THIS PATH ON MYRIAD RELATIVE TO WHERE YOUR DATAFOLDER IS)
data_dir = f"/home/myExampleUserName/Model/Disease-Modelling-SSA/data/myExampleFolder/"
# Where do you want this to be saved? This is specific to your wished
savepath = "/home/Scratch/myExampleSaveFolder/"

# Do not change job_n txt or runs
job_n = 1
run_n = 1
txt = ""
runs = np.arange(1, number_of_runs_per_scenario + 1)

for r in runs:
    for params in params_file_names:
        # create the name of the output file
        output_filename = "output_" + params.split('_')[1]
        output_filename = output_filename.split('.')[0]
        output_filename = output_filename + "_" + str(run_n)
        txt += f"{job_n}\t{days}\t" + data_dir + \
               f"\t{beta}\t{r}\t" + output_filename + "\t" + \
               data_dir + params + "\n"
        job_n += 1
    run_n += 1

with open(savepath, 'w') as f:
    f.write(txt)
