import matplotlib.pyplot as plt 
import numpy as np
import pandas as pd

filepath="/Users/sophieayling/Library/CloudStorage/OneDrive-UniversityCollegeLondon/GitHub/Disease-Modelling-SSA/data/output/ICCS/extended_submission/ver_1 (singleDist)/5_perc/beta_0.3/output/output_5p_v1_0.3_1.txt"
# Create dummy data
x = np.linspace(0, np.pi * 3, 100)
y = np.sin(x)
y2 = np.cos(x)

# OR import REAL data :)
data = pd.read_csv(filepath, delimiter='\t')

x= np.arange(0,len(data)) # refers to the number of obs in the dataset i.e. here it is days
y= data['metric_new_cases_asympt'].values

# Plot with the basic line function
plt.subplot(2,1,1) # for a plot with 2 rows 1 column, in which we are now going to write the code for the first 
plt.plot(x, y, color='r', alpha=0.3)
# Create graph labels
plt.xlabel('days')
plt.ylabel('num of asympt cases(x)')
plt.title('Asympt cases in timeframe')
# Set the max and minimum values shown on the x and y axis
#plt.ylim([- 1.5, 1.5])
#plt.xlim([0, 10])

#subplot two
plt.subplot(2,1,2) # for a plot with 2 rows 1 column, in which we are now going to write the code for the first 
plt.plot(x, y2, color='r', alpha=0.3)
# Create graph labels
plt.xlabel('x')
plt.ylabel('cos(x)')
plt.title('A riveting cos wave')
# Set the max and minimum values shown on the x and y axis
plt.ylim([- 1.5, 1.5])
plt.xlim([0, 10])
# save the figure
plt.subplots_adjust(hspace=1.2)
plt.show()
