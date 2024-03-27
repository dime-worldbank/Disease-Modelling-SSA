import geopandas as gpd
import pandas as pd
import sys

if __name__ == "__main__":
    output_dir = sys.argv[1]  # "/home/rmjlra2/Model/Disease-Modelling-SSA/data/gravity_model_sim_an/"
    datadir = sys.argv[2]
    # get district shape file
    ward_shape = gpd.read_file(datadir + "zwe_admbnda_adm3_zimstat_ocha_20180911.shp")
    names_of_ward = pd.read_csv(datadir + "zwe_adminboundaries_tabulardata.csv")
    ward_shape['ADM3_PCODE'] = names_of_ward['ADM3_PCODE']
    
    centroids = ward_shape.centroid
    ward_shape['centroids'] = centroids
    
    for orig in ward_shape.ADM3_PCODE.values:
        destinations = []
        distances = []
        circles = []
        for dest in ward_shape.ADM3_PCODE.values:
            if orig == dest:
                pass
            else:
                destinations.append(dest)
                origin_point = ward_shape.loc[ward_shape['ADM3_PCODE'] == orig, 'centroids'].values[0]
                destination_point = ward_shape.loc[ward_shape['ADM3_PCODE'] == dest, 'centroids'].values[0]
                circles.append(
                    ward_shape.loc[
                        ward_shape['ADM3_PCODE'] == orig, 'centroids'
                    ].values[0].buffer(origin_point.distance(destination_point))
                )
                distances.append(origin_point.distance(destination_point))
        circles = gpd.GeoSeries(circles)
        geo_dict = {'destinations': destinations, 'geometry': circles}
        rad_ward_circles = gpd.GeoDataFrame(geo_dict)
        rad_ward_circles.to_file(output_dir + orig + "_ward_radiation_model_orig_dest_radius.shp")


