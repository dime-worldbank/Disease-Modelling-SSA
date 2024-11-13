import os
import sys
# define your file paths, if running from IDE specify file paths in 'if' part, otherwise specify in terminal
try:
    folder_path = sys.argv[1]
    parameter_files_to_check = sys.argv[2]
except IndexError:
    folder_path = "/Users/robbiework/Library/CloudStorage/OneDrive-UniversityCollegeLondon/data/mobility_paper_runs/"
    parameter_files_to_check = ['params_wardRun.txt', 'params_districtRun.txt']

necessary_components_for_model_run = [
    'dataDir',
    'population_filename',
    'admin_zone_transition_LOCKDOWN_filename',
    'admin_zone_transition_PRELOCKDOWN_filename',
    'economic_status_weekday_movement_prob_filename',
    'economic_status_otherday_movement_prob_filename',
    'line_list_filename',
    'infection_transition_params_filename'
]


def main():
    components_for_model_run_found = []
    has_parameter_file_present = []
    # open up the file parameter text file
    for parameter_file in parameter_files_to_check:
        print("Checking data in " + parameter_file + " is in the data directory")
        with open(folder_path + parameter_file, 'r') as file:
            for line in file:  # iterate over file lines using an iterator
                if ':' in line:
                    component = line.split(':')[0]
                    components_for_model_run_found.append(component)
                    value = line.split(':')[1].strip(' ')
                    value = value.strip('\n')
                    if component == 'dataDir':
                        if (len(value) > 0) & (value[-1] == '/'):
                            has_parameter_file_present.append(True)
                    else:
                        if '.' in value:
                            components_for_model_run_found.append(value in os.listdir(folder_path))
                            assert value in os.listdir(folder_path), value + " is not present in the data directory"

        assert set(necessary_components_for_model_run).intersection(set(components_for_model_run_found)) == \
               set(necessary_components_for_model_run), parameter_file + " does not include every thing needed for a " \
                                                                         "model run"



if __name__ == "__main__":
    main()
