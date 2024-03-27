import geopandas as gpd
import pandas as pd
import sys
import os

if __name__ == "__main__":
    # get folder containing all the files
    output_dir = sys.argv[1]
    datadir = sys.argv[2]
    container_dir = sys.argv[3]

    # "/Users/robbiework/PycharmProjects/spacialEpidemiologyAnalysis/movementModels/betweenWards/ward_radiation_model/"
    files = os.listdir(container_dir)
    split_file_names = []
    for file in files:
        if (file.startswith("ZW")) and (file[-1] == 'p'):
            if file.split('_')[0] not in split_file_names:
                split_file_names.append(file.split('_')[0])

    # load in ward shape file
    ward_shape = gpd.read_file(datadir + "zwe_admbnda_adm3_zimstat_ocha_20180911.shp")
    names_of_ward = pd.read_csv(datadir + "zwe_adminboundaries_tabulardata.csv")
    ward_shape['ADM3_PCODE'] = names_of_ward['ADM3_PCODE']
    file_name_ext = '_ward_radiation_model_orig_dest_radius.shp'
    for ward_name in split_file_names:
        origins = []
        destinations = []
        geometries = []
        shape_file = gpd.read_file(container_dir + ward_name + file_name_ext)
        origin_shape = ward_shape.loc[ward_shape['ADM3_PCODE'] == ward_name, 'geometry']
        for destination in shape_file.destinatio:
            dest_shape = ward_shape.loc[ward_shape['ADM3_PCODE'] == destination, 'geometry']
            full_circle = shape_file.loc[shape_file['destinatio'] == destination].geometry
            no_orig = gpd.GeoSeries(full_circle.difference(origin_shape, align=False))
            # Remove the destination from the shape
            no_dest = gpd.GeoSeries(no_orig.difference(dest_shape, align=False))
            origins.append(ward_name)
            destinations.append(destination)
            geometries.append(no_dest.values[0])
        geometries = gpd.GeoSeries(geometries)
        geo_dict = {'origin': origins, 'destination': destinations, 'geometry': geometries}
        cut_out_df = gpd.GeoDataFrame(geo_dict, geometry='geometry')
        cut_out_df.to_file(output_dir + ward_name + "_ward_radiation_model_cut_out_orig_dest_radius.shp")
