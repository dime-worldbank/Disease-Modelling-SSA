import geopandas as gpd
import sys
import pandas as pd
import rasterio as rio
from rasterio.mask import mask
import numpy as np

if __name__ == "__main__":
    output_dir = sys.argv[1]  # "/home/rmjlra2/Model/Disease-Modelling-SSA/data/gravity_model_sim_an/"
    datadir = sys.argv[2]
    if output_dir[-1] != '/':
        print("Error: that ain't a directory, chief. Try again.")
        sys.exit()
    # get district shape file
    ward_shape = gpd.read_file(datadir + "zwe_admbnda_adm3_zimstat_ocha_20180911.shp")
    raster_2 = rio.open(datadir + "ZimRasterEPSG32736.tif")

    raster_2.transform
    names_of_ward = pd.read_csv(datadir + "zwe_adminboundaries_tabulardata.csv")
    ward_shape['ADM3_PCODE'] = names_of_ward['ADM3_PCODE']
    ward_shape['ADM2_EN'] = names_of_ward['ADM2_EN']

    centroids = ward_shape.centroid
    ward_shape['centroids'] = centroids

    distances_between_points = []
    for origin_point in centroids:
        origin_dest_dist = []
        for destination_point in centroids:
            origin_dest_dist.append(origin_point.distance(destination_point))
        distances_between_points.append(origin_dest_dist)

    ward_shape['distances_between_centroids'] = distances_between_points
    # Create the circles
    circles = []
    for ward in ward_shape.iterrows():
        radius = []
        for distance in ward[1][-1]:
            radius.append(ward[1][-2].buffer(distance))
        circles.append(gpd.GeoSeries(radius))

    ward_shape['circles'] = circles

    for row in ward_shape.iterrows():
        prior_df = pd.read_csv(datadir + 'rad_ward_output.csv')
        prior_df = prior_df.loc[:, ~prior_df.columns.str.contains('^Unnamed')]
        for idx, circle in enumerate(row[1][-1]):
            # Remove origin shape from circle
            # Store origin name
            origin = row[1].ADM3_PCODE
            # store destination name
            destination = ward_shape.iloc[idx].ADM3_PCODE
            if origin == destination:
                pass
            else:
                if len(prior_df.loc[(prior_df['origin'] == origin) & (prior_df['destination'] == destination)]) == 0:
                    row_to_add = [origin, destination]
                    # remove the origin from the shape
                    no_orig = gpd.GeoSeries(circle.difference(row[1].geometry))
                    # Store the origin shape
                    origin_shape = row[1].geometry
                    # Remove the destination from the shape
                    no_orig_no_dest = gpd.GeoSeries(no_orig.difference(ward_shape.iloc[idx].geometry))
                    # store the destination shape
                    destination_shape = ward_shape.iloc[idx].geometry
                    # Store the destination shape
                    sij_raster, out_transform = mask(raster_2, no_orig_no_dest, invert=False)
                    sij_raster[sij_raster < 0] = 0
                    origin_raster, t = mask(raster_2, gpd.GeoSeries(origin_shape), invert=False)
                    origin_raster[origin_raster < 0] = 0
                    dest_raster, t = mask(raster_2, gpd.GeoSeries(destination_shape), invert=False)
                    dest_raster[dest_raster < 0] = 0
                    numerator = np.sum(origin_raster, dtype='float') * np.sum(dest_raster, dtype='float')
                    denomenator = (
                            np.sum(origin_raster, dtype='float') ** 2 +
                            np.sum(origin_raster, dtype='float') * np.sum(dest_raster, dtype='float') +
                            2 * np.sum(origin_raster, dtype='float') * np.sum(sij_raster, dtype='float') +
                            np.sum(sij_raster, dtype='float') * np.sum(dest_raster, dtype='float') +
                            np.sum(sij_raster, dtype='float') ** 2
                    )
                    row_to_add.append(numerator / denomenator)
                    prior_df.loc[len(prior_df)] = row_to_add
        prior_df.to_csv(datadir + "rad_ward_output.csv")
        prior_df.to_csv(output_dir + "rad_ward_output.csv")
        prior_df.to_csv(output_dir + origin + "_rad_ward_output.csv")

