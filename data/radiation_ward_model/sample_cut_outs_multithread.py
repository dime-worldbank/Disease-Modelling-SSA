import sys
import geopandas as gpd
import rasterio as rio
from rasterio.mask import mask
import pandas as pd
import numpy as np
import os
from threading import Thread, Lock

if __name__ == '__main__':
    # get shell script passes parameters
    data_dir = sys.argv[1]
    shape_file_dir = sys.argv[2]
    output_dir = sys.argv[3]
    end_num_str = sys.argv[4]
    # load in the raster
    pop_raster = rio.open(data_dir + "ZimRasterEPSG32736.tif")
    pop_raster.transform
    # load in ward population numbers
    ward_pops = pd.read_csv(data_dir + "ward_pops.csv")

    def create_csv_files(filenames, lock):
        global pop_raster
        global ward_pops

        for _f in filenames:
            lock.acquire()

            shp_file = gpd.read_file(shape_file_dir + _f)
            orig = _f.split('_')[0]
            dest_names = []
            dest_probs = []

            for row in shp_file.iterrows():
                sij_raster, out_transform = mask(pop_raster, gpd.GeoSeries(row[1].geometry))
                sij_raster[sij_raster < 0] = 0
                sij_pop = np.sum(sij_raster, dtype='float')
                origin_pop = float(ward_pops.loc[ward_pops['ward'] == orig, 'population'])
                dest_pop = float(ward_pops.loc[ward_pops['ward'] == row[1].destinatio, 'population'])
                numerator = origin_pop * dest_pop
                denomenator = (
                        origin_pop ** 2 +
                        origin_pop * dest_pop +
                        2 * origin_pop * sij_pop +
                        sij_pop * dest_pop +
                        sij_pop ** 2
                )

                dest_names.append(row[1].destinatio)
                dest_probs.append(numerator / denomenator)

            out_dict = {'destination': dest_names, 'prob': dest_probs}
            df = pd.DataFrame(out_dict)
            df.to_csv(output_dir + _f.split('_')[0] + '.csv')
            lock.release()

    # create a lock
    run_lock = Lock()

    # load in the shape file
    ward_shape = gpd.read_file(data_dir + "zwe_admbnda_adm3_zimstat_ocha_20180911.shp")

    names_of_ward = pd.read_csv(data_dir + "zwe_adminboundaries_tabulardata.csv")
    ward_shape['ADM3_PCODE'] = names_of_ward['ADM3_PCODE']
    ward_shape['ADM2_EN'] = names_of_ward['ADM2_EN']
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

    # We are going to request 16 cores, split the list of wards to process into 16 parts
    lists = np.array_split(shape_file_names, 16)
    # create the threads
    p1 = Thread(target=create_csv_files, args=(lists[0], run_lock))
    p2 = Thread(target=create_csv_files, args=(lists[1], run_lock))
    p3 = Thread(target=create_csv_files, args=(lists[2], run_lock))
    p4 = Thread(target=create_csv_files, args=(lists[3], run_lock))
    p5 = Thread(target=create_csv_files, args=(lists[4], run_lock))
    p6 = Thread(target=create_csv_files, args=(lists[5], run_lock))
    p7 = Thread(target=create_csv_files, args=(lists[6], run_lock))
    p8 = Thread(target=create_csv_files, args=(lists[7], run_lock))
    p9 = Thread(target=create_csv_files, args=(lists[8], run_lock))
    p10 = Thread(target=create_csv_files, args=(lists[9], run_lock))
    p11 = Thread(target=create_csv_files, args=(lists[10], run_lock))
    p12 = Thread(target=create_csv_files, args=(lists[11], run_lock))
    p13 = Thread(target=create_csv_files, args=(lists[12], run_lock))
    p14 = Thread(target=create_csv_files, args=(lists[13], run_lock))
    p15 = Thread(target=create_csv_files, args=(lists[14], run_lock))
    p16 = Thread(target=create_csv_files, args=(lists[15], run_lock))
    # run the threads
    p1.start()
    p2.start()
    p3.start()
    p4.start()
    p5.start()
    p6.start()
    p7.start()
    p8.start()
    p9.start()
    p10.start()
    p11.start()
    p12.start()
    p13.start()
    p14.start()
    p15.start()
    p16.start()
