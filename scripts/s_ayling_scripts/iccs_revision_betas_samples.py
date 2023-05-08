

import numpy as np
runs = np.arange(1, 11)
txt = ""
job_n = 1
beta = 0.003
b_filename = "b003"
days = 30 
data_dir = f"/home/ucfu056/Disease-Modelling-SSA/data/verification/ICCS_long/ver3/"
scale = 50

for r in runs:
    txt += f"{job_n}\t{days}\t" + data_dir + f"\t{beta}\t{r}\toutput_{scale}p_v3_{b_filename}_{r}.txt\t" + \
           f"/home/ucfu056/Disease-Modelling-SSA/data/verification/ICCS_long/ver3/" \
           f"params_defaultMultiDist_noWknds_{scale}_perc.txt\t" + f"inf_output_{scale}p_v3_{b_filename}_{r}.txt\n"
    job_n += 1
with open(f"/Users/sophieayling/Library/CloudStorage/OneDrive-UniversityCollegeLondon/GitHub/Disease-Modelling-SSA/data/verification/ICCS_long/ver3/jobscript_{scale}p_v3_{b_filename}_rev.txt", 'w') as f:
    f.write(txt)