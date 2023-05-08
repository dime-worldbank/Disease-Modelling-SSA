import numpy as np
betas = np.linspace(0.005, 1, 20)
runs = np.arange(1, 5)
txt = ""
job_n = 1
days = 100
data_dir = f"/home/ucfu056/Disease-Modelling-SSA/data/verification/model_calibration/"
for beta in betas:
    for r in runs:
        txt += f"{job_n}\t{days}\t" + data_dir + \
               f"\t{beta}\t{r}\toutput_beta_5_perc_{beta}_{r}.txt\t" \
               f"/home/ucfu056/Disease-Modelling-SSA/data/verification/model_calibration/" \
               f"params_defaultMultiDist_noWknds_5_perc.txt\t" \
               f"inf_output_beta_5_perc_{beta}_{r}.txt\n"
        job_n += 1
with open(f"/Users/sophieayling/Library/CloudStorage/OneDrive-UniversityCollegeLondon/GitHub/Disease-Modelling-SSA/data/verification/model_calibration/betaRunScript_sa.txt", 'w') as f:
    f.write(txt)