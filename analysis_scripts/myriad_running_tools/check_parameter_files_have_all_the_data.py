import os
import sys
# define your file paths, if running from IDE specify file paths in 'except' part, otherwise specify in terminal
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
    # Create a quick check on the input format of the variables
    assert folder_path[-1] == '/', "Please add '/' to the end of your folder path"
    assert isinstance(parameter_files_to_check, list), "Please format the parameter files you want to check as a list"
    # Create storage to store what has been found in the parameter file, firstly a list of the file names listed in
    # the parameter file
    components_for_model_run_found = []
    # Create a list of
    has_data_dir_present = False
    # open up the file parameter text file
    for parameter_file in parameter_files_to_check:
        print("Checking data in " + parameter_file + " is in the data directory")
        with open(folder_path + parameter_file, 'r') as file:
            # Read over each line in the file
            for line in file:  # iterate over file lines using an iterator
                # Filter those lines that relate to parameter files by checking for ':'
                if ':' in line:
                    # Split the string into the 'component' part
                    component = line.split(':')[0]
                    components_for_model_run_found.append(component)
                    # Get the filename part of this line, stripping away 'spaces' and 'enters'
                    value = line.split(':')[1].strip(' ')
                    value = value.strip('\n')
                    # If this line in the file is just the dataDir, check that it's formatted correctly
                    if component == 'dataDir':
                        if (len(value) > 0) & (value[-1] == '/'):
                            has_data_dir_present = True
                    # If it's not the dataDir, we first need to filter out parameters found that read in a file and
                    # ignore those that don't by checking for '.' in the value variable
                    else:
                        if '.' in value:
                            assert value in os.listdir(folder_path), value + " is not present in the data directory"
        # Check that every file name is present, if not print out the name of the missing model components
        assert set(necessary_components_for_model_run).intersection(set(components_for_model_run_found)) == \
               set(necessary_components_for_model_run), \
            parameter_file + " does not include every thing needed for a model run, need the following: " + \
            ', '.join(map(str, set(necessary_components_for_model_run).difference(components_for_model_run_found)))
        # Check that a data directory has been listed
        assert has_data_dir_present, "You need to include the name of the folder where all the files needed for a" \
                                     " model run are included in the format dataDir: yourFolderPath/"


if __name__ == "__main__":
    main()
