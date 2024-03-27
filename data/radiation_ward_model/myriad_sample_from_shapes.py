import geopandas as gpd
import sys
import pandas as pd
import rasterio as rio
from rasterio.mask import mask
import numpy as np
import os

if __name__ == "__main__":
    data_dir = sys.argv[1]
    shape_file_dir = sys.argv[2]
    output_dir = sys.argv[3]
    end_num_str = sys.argv[4]
    # load in the raster
    pop_raster = rio.open(data_dir + "ZimRasterEPSG32736.tif")

    pop_raster.transform
    # load in the shape file
    ward_shape = gpd.read_file(data_dir + "zwe_admbnda_adm3_zimstat_ocha_20180911.shp")

    names_of_ward = pd.read_csv(data_dir + "zwe_adminboundaries_tabulardata.csv")
    ward_shape['ADM3_PCODE'] = names_of_ward['ADM3_PCODE']
    ward_shape['ADM2_EN'] = names_of_ward['ADM2_EN']
    # load in ward population numbers
    ward_population = pd.read_csv(data_dir + "ward_pops.csv")
    # iterate over the cut out files
    # get the shp files only
    shape_file_names = []
    for file in os.listdir(shape_file_dir):
        if ".shp" in file:
            shape_file_names.append(file)
    # Drop files that we already have the output csv for
    current_outputs = []
    for file in os.listdir(output_dir):
        if file.startswith("ZW"):
            if file.split('.')[0][-1] == end_num_str:
                current_outputs.append(file.split('.')[0])

    for file in shape_file_names:
        if file.split('_')[0] in current_outputs:
            shape_file_names.remove(file)

    for file in shape_file_names:
        shp_file = gpd.read_file(shape_file_dir + file)
        orig = file.split('_')[0]
        dest_names = []
        dest_probs = []
        for row in shp_file.iterrows():
            destination = ward_shape['ADM3_PCODE'] == row[1].destinatio
            sij_shp = gpd.GeoSeries(row[1].geometry)
            sij_raster, out_transform = mask(pop_raster, sij_shp, invert=False)
            sij_raster[sij_raster < 0] = 0
            sij_pop = np.sum(sij_raster, dtype='float')
            origin_pop = ward_population.loc[ward_population['ward'] == orig, 'population']
            dest_pop = ward_population.loc[ward_population['ward'] == destination, 'population']                          
            numerator = origin_pop * dest_pop
            denomenator = (
                    origin_pop ** 2 + origin_pop * dest_pop + 2 * origin_pop * sij_pop +
                    sij_pop * dest_pop + sij_pop ** 2
            )
            dest_names.append(row[1].destinatio)
            dest_probs.append(numerator / denomenator)
        out_dict = {'destination': dest_names, 'prob': dest_probs}
        df = pd.DataFrame(out_dict)
        df.to_csv(output_dir + file.split('_')[0] + '.csv')
