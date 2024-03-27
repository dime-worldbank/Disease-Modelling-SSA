* NOTE: You need to set the Stata working directory to the path
* where the data file is located.
cd "/Users/sophieayling/Library/CloudStorage/OneDrive-UniversityCollegeLondon/GitHub/Disease-Modelling-SSA/data/raw/census/5_perc_sample/IPUMS_raw/"
set more off

clear
quietly infix                     ///
  int     country        1-3      ///
  int     year           4-7      ///
  double  sample         8-16     ///
  double  serial         17-28    ///
  int     persons        29-32    ///
  double  hhwt           33-40    ///
  byte    subsamp        41-42    ///
  double  strata         43-54    ///
  byte    urban          55-55    ///
  double  geolev2        56-64    ///
  double  popdensgeo2    65-76    ///
  double  areamollwgeo1  77-86    ///
  double  areamollwgeo2  87-96    ///
  int     geo1_zw2012    97-99    ///
  double  geo2_zw        100-108  ///
  long    geo2_zw2012    109-114  ///
  double  geo3_zw2012    115-123  ///
  byte    dhs_ipumsi_zw  124-125  ///
  byte    ownership      126-126  ///
  int     ownershipd     127-129  ///
  byte    electric       130-130  ///
  byte    watsup         131-132  ///
  byte    fuelcook       133-134  ///
  byte    toilet         135-136  ///
  byte    mortnum        137-137  ///
  byte    anymort        138-138  ///
  int     pernum         139-142  ///
  double  perwt          143-150  ///
  byte    polymal        151-151  ///
  byte    poly2nd        152-152  ///
  byte    relate         153-153  ///
  int     related        154-157  ///
  int     age            158-160  ///
  byte    age2           161-162  ///
  byte    sex            163-163  ///
  byte    race           164-165  ///
  byte    edattain       166-166  ///
  int     edattaind      167-169  ///
  byte    yrschool       170-171  ///
  byte    educzw         172-173  ///
  byte    empstat        174-174  ///
  int     empstatd       175-177  ///
  byte    labforce       178-178  ///
  byte    occisco        179-180  ///
  int     occ            181-184  ///
  int     isco88a        185-187  ///
  byte    migrate0       188-189  ///
  long    geomig1_10     190-195  ///
  long    mig1_10_zw     196-201  ///
  double  mig2_10_zw     202-210  ///
  using `"ipumsi_00001.dat"'

replace hhwt          = hhwt          / 100
replace perwt         = perwt         / 100

format sample        %9.0f
format serial        %12.0f
format hhwt          %8.2f
format strata        %12.0f
format geolev2       %9.0f
format popdensgeo2   %12.0g
format areamollwgeo1 %10.0g
format areamollwgeo2 %10.0g
format geo2_zw       %9.0f
format geo3_zw2012   %9.0f
format perwt         %8.2f
format mig2_10_zw    %9.0f

label var country       `"Country"'
label var year          `"Year"'
label var sample        `"IPUMS sample identifier"'
label var serial        `"Household serial number"'
label var persons       `"Number of person records in the household"'
label var hhwt          `"Household weight"'
label var subsamp       `"Subsample number"'
label var strata        `"Strata identifier"'
label var urban         `"Urban-rural status"'
label var geolev2       `"2nd subnational geographic level, world [consistent boundaries over time]"'
label var popdensgeo2   `"Population density of GEOLEV2 unit, in persons per square kilometer"'
label var areamollwgeo1 `"Area of GEOLEV1 unit in square kilometers"'
label var areamollwgeo2 `"Area of GEOLEV2 unit in square kilometers"'
label var geo1_zw2012   `"Zimbabwe, Province 2012 [Level 1, GIS]"'
label var geo2_zw       `"Zimbabwe, District 2012 [Level 2; consistent boundaries, GIS]"'
label var geo2_zw2012   `"Zimbabwe, District 2012 [Level 2, GIS]"'
label var geo3_zw2012   `"Zimbabwe, Ward 2012 [Level 3, GIS]"'
label var dhs_ipumsi_zw `"DHS-IPUMS-I Zimbabwe regions, 1994-2015 [consistent boundaries, GIS]"'
label var ownership     `"Ownership of dwelling [general version]"'
label var ownershipd    `"Ownership of dwelling [detailed version]"'
label var electric      `"Electricity"'
label var watsup        `"Water supply"'
label var fuelcook      `"Cooking fuel"'
label var toilet        `"Toilet"'
label var mortnum       `"Number of deaths in household last year"'
label var anymort       `"Any deaths in household last year"'
label var pernum        `"Person number"'
label var perwt         `"Person weight"'
label var polymal       `"Man with more than one wife linked"'
label var poly2nd       `"Woman is second or higher order wife"'
label var relate        `"Relationship to household head [general version]"'
label var related       `"Relationship to household head [detailed version]"'
label var age           `"Age"'
label var age2          `"Age, grouped into intervals"'
label var sex           `"Sex"'
label var race          `"Race or color"'
label var edattain      `"Educational attainment, international recode [general version]"'
label var edattaind     `"Educational attainment, international recode [detailed version]"'
label var yrschool      `"Years of schooling"'
label var educzw        `"Educational attainment, Zimbabwe"'
label var empstat       `"Activity status (employment status) [general version]"'
label var empstatd      `"Activity status (employment status) [detailed version]"'
label var labforce      `"Labor force participation"'
label var occisco       `"Occupation, ISCO general"'
label var occ           `"Occupation, unrecoded"'
label var isco88a       `"Occupation, ISCO-1988, 3-digit"'
label var migrate0      `"Migration status, 10 years"'
label var geomig1_10    `"1st subnational geographic level of residence 10 years prior to survey, world [c"'
label var mig1_10_zw    `"Province of residence 10 years ago, Zimbabwe; consistent boundaries, GIS"'
label var mig2_10_zw    `"District of residence 10 years ago, Zimbabwe; consistent boundaries, GIS"'

label define country_lbl 032 `"Argentina"'
label define country_lbl 051 `"Armenia"', add
label define country_lbl 040 `"Austria"', add
label define country_lbl 050 `"Bangladesh"', add
label define country_lbl 112 `"Belarus"', add
label define country_lbl 204 `"Benin"', add
label define country_lbl 068 `"Bolivia"', add
label define country_lbl 072 `"Botswana"', add
label define country_lbl 076 `"Brazil"', add
label define country_lbl 854 `"Burkina Faso"', add
label define country_lbl 116 `"Cambodia"', add
label define country_lbl 120 `"Cameroon"', add
label define country_lbl 124 `"Canada"', add
label define country_lbl 152 `"Chile"', add
label define country_lbl 156 `"China"', add
label define country_lbl 170 `"Colombia"', add
label define country_lbl 188 `"Costa Rica"', add
label define country_lbl 192 `"Cuba"', add
label define country_lbl 208 `"Denmark"', add
label define country_lbl 214 `"Dominican Republic"', add
label define country_lbl 218 `"Ecuador"', add
label define country_lbl 818 `"Egypt"', add
label define country_lbl 222 `"El Salvador"', add
label define country_lbl 231 `"Ethiopia"', add
label define country_lbl 242 `"Fiji"', add
label define country_lbl 246 `"Finland"', add
label define country_lbl 250 `"France"', add
label define country_lbl 276 `"Germany"', add
label define country_lbl 288 `"Ghana"', add
label define country_lbl 300 `"Greece"', add
label define country_lbl 320 `"Guatemala"', add
label define country_lbl 324 `"Guinea"', add
label define country_lbl 332 `"Haiti"', add
label define country_lbl 340 `"Honduras"', add
label define country_lbl 348 `"Hungary"', add
label define country_lbl 352 `"Iceland"', add
label define country_lbl 356 `"India"', add
label define country_lbl 360 `"Indonesia"', add
label define country_lbl 364 `"Iran"', add
label define country_lbl 368 `"Iraq"', add
label define country_lbl 372 `"Ireland"', add
label define country_lbl 376 `"Israel"', add
label define country_lbl 380 `"Italy"', add
label define country_lbl 384 `"Ivory Coast"', add
label define country_lbl 388 `"Jamaica"', add
label define country_lbl 400 `"Jordan"', add
label define country_lbl 404 `"Kenya"', add
label define country_lbl 417 `"Kyrgyz Republic"', add
label define country_lbl 418 `"Laos"', add
label define country_lbl 426 `"Lesotho"', add
label define country_lbl 430 `"Liberia"', add
label define country_lbl 454 `"Malawi"', add
label define country_lbl 458 `"Malaysia"', add
label define country_lbl 466 `"Mali"', add
label define country_lbl 480 `"Mauritius"', add
label define country_lbl 484 `"Mexico"', add
label define country_lbl 496 `"Mongolia"', add
label define country_lbl 504 `"Morocco"', add
label define country_lbl 508 `"Mozambique"', add
label define country_lbl 104 `"Myanmar"', add
label define country_lbl 524 `"Nepal"', add
label define country_lbl 528 `"Netherlands"', add
label define country_lbl 558 `"Nicaragua"', add
label define country_lbl 566 `"Nigeria"', add
label define country_lbl 578 `"Norway"', add
label define country_lbl 586 `"Pakistan"', add
label define country_lbl 275 `"Palestine"', add
label define country_lbl 591 `"Panama"', add
label define country_lbl 598 `"Papua New Guinea"', add
label define country_lbl 600 `"Paraguay"', add
label define country_lbl 604 `"Peru"', add
label define country_lbl 608 `"Philippines"', add
label define country_lbl 616 `"Poland"', add
label define country_lbl 620 `"Portugal"', add
label define country_lbl 630 `"Puerto Rico"', add
label define country_lbl 642 `"Romania"', add
label define country_lbl 643 `"Russia"', add
label define country_lbl 646 `"Rwanda"', add
label define country_lbl 662 `"Saint Lucia"', add
label define country_lbl 686 `"Senegal"', add
label define country_lbl 694 `"Sierra Leone"', add
label define country_lbl 703 `"Slovak Republic"', add
label define country_lbl 705 `"Slovenia"', add
label define country_lbl 710 `"South Africa"', add
label define country_lbl 728 `"South Sudan"', add
label define country_lbl 724 `"Spain"', add
label define country_lbl 729 `"Sudan"', add
label define country_lbl 740 `"Suriname"', add
label define country_lbl 752 `"Sweden"', add
label define country_lbl 756 `"Switzerland"', add
label define country_lbl 834 `"Tanzania"', add
label define country_lbl 764 `"Thailand"', add
label define country_lbl 768 `"Togo"', add
label define country_lbl 780 `"Trinidad and Tobago"', add
label define country_lbl 792 `"Turkey"', add
label define country_lbl 800 `"Uganda"', add
label define country_lbl 804 `"Ukraine"', add
label define country_lbl 826 `"United Kingdom"', add
label define country_lbl 840 `"United States"', add
label define country_lbl 858 `"Uruguay"', add
label define country_lbl 862 `"Venezuela"', add
label define country_lbl 704 `"Vietnam"', add
label define country_lbl 894 `"Zambia"', add
label define country_lbl 716 `"Zimbabwe"', add
label values country country_lbl

label define year_lbl 1703 `"1703"'
label define year_lbl 1729 `"1729"', add
label define year_lbl 1787 `"1787"', add
label define year_lbl 1801 `"1801"', add
label define year_lbl 1819 `"1819"', add
label define year_lbl 1845 `"1845"', add
label define year_lbl 1848 `"1848"', add
label define year_lbl 1850 `"1850"', add
label define year_lbl 1851 `"1851"', add
label define year_lbl 1852 `"1852"', add
label define year_lbl 1860 `"1860"', add
label define year_lbl 1861 `"1861"', add
label define year_lbl 1865 `"1865"', add
label define year_lbl 1868 `"1868"', add
label define year_lbl 1870 `"1870"', add
label define year_lbl 1871 `"1871"', add
label define year_lbl 1875 `"1875"', add
label define year_lbl 1880 `"1880"', add
label define year_lbl 1881 `"1881"', add
label define year_lbl 1885 `"1885"', add
label define year_lbl 1890 `"1890"', add
label define year_lbl 1891 `"1891"', add
label define year_lbl 1900 `"1900"', add
label define year_lbl 1901 `"1901"', add
label define year_lbl 1910 `"1910"', add
label define year_lbl 1911 `"1911"', add
label define year_lbl 1960 `"1960"', add
label define year_lbl 1961 `"1961"', add
label define year_lbl 1962 `"1962"', add
label define year_lbl 1963 `"1963"', add
label define year_lbl 1964 `"1964"', add
label define year_lbl 1966 `"1966"', add
label define year_lbl 1968 `"1968"', add
label define year_lbl 1969 `"1969"', add
label define year_lbl 1970 `"1970"', add
label define year_lbl 1971 `"1971"', add
label define year_lbl 1972 `"1972"', add
label define year_lbl 1973 `"1973"', add
label define year_lbl 1974 `"1974"', add
label define year_lbl 1975 `"1975"', add
label define year_lbl 1976 `"1976"', add
label define year_lbl 1977 `"1977"', add
label define year_lbl 1978 `"1978"', add
label define year_lbl 1979 `"1979"', add
label define year_lbl 1980 `"1980"', add
label define year_lbl 1981 `"1981"', add
label define year_lbl 1982 `"1982"', add
label define year_lbl 1983 `"1983"', add
label define year_lbl 1984 `"1984"', add
label define year_lbl 1985 `"1985"', add
label define year_lbl 1986 `"1986"', add
label define year_lbl 1987 `"1987"', add
label define year_lbl 1989 `"1989"', add
label define year_lbl 1990 `"1990"', add
label define year_lbl 1991 `"1991"', add
label define year_lbl 1992 `"1992"', add
label define year_lbl 1993 `"1993"', add
label define year_lbl 1994 `"1994"', add
label define year_lbl 1995 `"1995"', add
label define year_lbl 1996 `"1996"', add
label define year_lbl 1997 `"1997"', add
label define year_lbl 1998 `"1998"', add
label define year_lbl 1999 `"1999"', add
label define year_lbl 2000 `"2000"', add
label define year_lbl 2001 `"2001"', add
label define year_lbl 2002 `"2002"', add
label define year_lbl 2003 `"2003"', add
label define year_lbl 2004 `"2004"', add
label define year_lbl 2005 `"2005"', add
label define year_lbl 2006 `"2006"', add
label define year_lbl 2007 `"2007"', add
label define year_lbl 2008 `"2008"', add
label define year_lbl 2009 `"2009"', add
label define year_lbl 2010 `"2010"', add
label define year_lbl 2011 `"2011"', add
label define year_lbl 2012 `"2012"', add
label define year_lbl 2013 `"2013"', add
label define year_lbl 2014 `"2014"', add
label define year_lbl 2015 `"2015"', add
label define year_lbl 2016 `"2016"', add
label define year_lbl 2017 `"2017"', add
label define year_lbl 2018 `"2018"', add
label define year_lbl 2019 `"2019"', add
label define year_lbl 2020 `"2020"', add
label values year year_lbl

label define sample_lbl 032197001 `"Argentina 1970"'
label define sample_lbl 032198001 `"Argentina 1980"', add
label define sample_lbl 032199101 `"Argentina 1991"', add
label define sample_lbl 032200101 `"Argentina 2001"', add
label define sample_lbl 032201001 `"Argentina 2010"', add
label define sample_lbl 051200101 `"Armenia 2001"', add
label define sample_lbl 051201101 `"Armenia 2011"', add
label define sample_lbl 040197101 `"Austria 1971"', add
label define sample_lbl 040198101 `"Austria 1981"', add
label define sample_lbl 040199101 `"Austria 1991"', add
label define sample_lbl 040200101 `"Austria 2001"', add
label define sample_lbl 040201101 `"Austria 2011"', add
label define sample_lbl 050199101 `"Bangladesh 1991"', add
label define sample_lbl 050200101 `"Bangladesh 2001"', add
label define sample_lbl 050201101 `"Bangladesh 2011"', add
label define sample_lbl 112199901 `"Belarus 1999"', add
label define sample_lbl 112200901 `"Belarus 2009"', add
label define sample_lbl 204197901 `"Benin 1979"', add
label define sample_lbl 204199201 `"Benin 1992"', add
label define sample_lbl 204200201 `"Benin 2002"', add
label define sample_lbl 204201301 `"Benin 2013"', add
label define sample_lbl 068197601 `"Bolivia 1976"', add
label define sample_lbl 068199201 `"Bolivia 1992"', add
label define sample_lbl 068200101 `"Bolivia 2001"', add
label define sample_lbl 068201201 `"Bolivia 2012"', add
label define sample_lbl 072198101 `"Botswana 1981"', add
label define sample_lbl 072199101 `"Botswana 1991"', add
label define sample_lbl 072200101 `"Botswana 2001"', add
label define sample_lbl 072201101 `"Botswana 2011"', add
label define sample_lbl 076196001 `"Brazil 1960"', add
label define sample_lbl 076197001 `"Brazil 1970"', add
label define sample_lbl 076198001 `"Brazil 1980"', add
label define sample_lbl 076199101 `"Brazil 1991"', add
label define sample_lbl 076200001 `"Brazil 2000"', add
label define sample_lbl 076201001 `"Brazil 2010"', add
label define sample_lbl 854198501 `"Burkina Faso 1985"', add
label define sample_lbl 854199601 `"Burkina Faso 1996"', add
label define sample_lbl 854200601 `"Burkina Faso 2006"', add
label define sample_lbl 116199801 `"Cambodia 1998"', add
label define sample_lbl 116200401 `"Cambodia 2004"', add
label define sample_lbl 116200801 `"Cambodia 2008"', add
label define sample_lbl 116201301 `"Cambodia 2013"', add
label define sample_lbl 116201901 `"Cambodia 2019"', add
label define sample_lbl 120197601 `"Cameroon 1976"', add
label define sample_lbl 120198701 `"Cameroon 1987"', add
label define sample_lbl 120200501 `"Cameroon 2005"', add
label define sample_lbl 124185201 `"Canada 1852"', add
label define sample_lbl 124187101 `"Canada 1871"', add
label define sample_lbl 124188101 `"Canada 1881"', add
label define sample_lbl 124189101 `"Canada 1891"', add
label define sample_lbl 124190101 `"Canada 1901"', add
label define sample_lbl 124191101 `"Canada 1911"', add
label define sample_lbl 124197101 `"Canada 1971"', add
label define sample_lbl 124198101 `"Canada 1981"', add
label define sample_lbl 124199101 `"Canada 1991"', add
label define sample_lbl 124200101 `"Canada 2001"', add
label define sample_lbl 124201101 `"Canada 2011"', add
label define sample_lbl 152196001 `"Chile 1960"', add
label define sample_lbl 152197001 `"Chile 1970"', add
label define sample_lbl 152198201 `"Chile 1982"', add
label define sample_lbl 152199201 `"Chile 1992"', add
label define sample_lbl 152200201 `"Chile 2002"', add
label define sample_lbl 152201701 `"Chile 2017"', add
label define sample_lbl 156198201 `"China 1982"', add
label define sample_lbl 156199001 `"China 1990"', add
label define sample_lbl 156200001 `"China 2000"', add
label define sample_lbl 170196401 `"Colombia 1964"', add
label define sample_lbl 170197301 `"Colombia 1973"', add
label define sample_lbl 170198501 `"Colombia 1985"', add
label define sample_lbl 170199301 `"Colombia 1993"', add
label define sample_lbl 170200501 `"Colombia 2005"', add
label define sample_lbl 188196301 `"Costa Rica 1963"', add
label define sample_lbl 188197301 `"Costa Rica 1973"', add
label define sample_lbl 188198401 `"Costa Rica 1984"', add
label define sample_lbl 188200001 `"Costa Rica 2000"', add
label define sample_lbl 188201101 `"Costa Rica 2011"', add
label define sample_lbl 192200201 `"Cuba 2002"', add
label define sample_lbl 192201201 `"Cuba 2012"', add
label define sample_lbl 208178701 `"Denmark 1787"', add
label define sample_lbl 208180101 `"Denmark 1801"', add
label define sample_lbl 208184501 `"Denmark 1845"', add
label define sample_lbl 208188001 `"Denmark 1880"', add
label define sample_lbl 208188501 `"Denmark 1885"', add
label define sample_lbl 214196001 `"Dominican Republic 1960"', add
label define sample_lbl 214197001 `"Dominican Republic 1970"', add
label define sample_lbl 214198101 `"Dominican Republic 1981"', add
label define sample_lbl 214200201 `"Dominican Republic 2002"', add
label define sample_lbl 214201001 `"Dominican Republic 2010"', add
label define sample_lbl 218196201 `"Ecuador 1962"', add
label define sample_lbl 218197401 `"Ecuador 1974"', add
label define sample_lbl 218198201 `"Ecuador 1982"', add
label define sample_lbl 218199001 `"Ecuador 1990"', add
label define sample_lbl 218200101 `"Ecuador 2001"', add
label define sample_lbl 218201001 `"Ecuador 2010"', add
label define sample_lbl 818184801 `"Egypt 1848"', add
label define sample_lbl 818186801 `"Egypt 1868"', add
label define sample_lbl 818198601 `"Egypt 1986"', add
label define sample_lbl 818199601 `"Egypt 1996"', add
label define sample_lbl 818200601 `"Egypt 2006"', add
label define sample_lbl 222199201 `"El Salvador 1992"', add
label define sample_lbl 222200701 `"El Salvador 2007"', add
label define sample_lbl 231198401 `"Ethiopia 1984"', add
label define sample_lbl 231199401 `"Ethiopia 1994"', add
label define sample_lbl 231200701 `"Ethiopia 2007"', add
label define sample_lbl 242196601 `"Fiji 1966"', add
label define sample_lbl 242197601 `"Fiji 1976"', add
label define sample_lbl 242198601 `"Fiji 1986"', add
label define sample_lbl 242199601 `"Fiji 1996"', add
label define sample_lbl 242200701 `"Fiji 2007"', add
label define sample_lbl 242201401 `"Fiji 2014"', add
label define sample_lbl 246201001 `"Finland 2010"', add
label define sample_lbl 250196201 `"France 1962"', add
label define sample_lbl 250196801 `"France 1968"', add
label define sample_lbl 250197501 `"France 1975"', add
label define sample_lbl 250198201 `"France 1982"', add
label define sample_lbl 250199001 `"France 1990"', add
label define sample_lbl 250199901 `"France 1999"', add
label define sample_lbl 250200601 `"France 2006"', add
label define sample_lbl 250201101 `"France 2011"', add
label define sample_lbl 276181901 `"Germany 1819 (Mecklenburg)"', add
label define sample_lbl 276197001 `"Germany 1970 (West)"', add
label define sample_lbl 276197101 `"Germany 1971 (East)"', add
label define sample_lbl 276198101 `"Germany 1981 (East)"', add
label define sample_lbl 276198701 `"Germany 1987 (West)"', add
label define sample_lbl 288198401 `"Ghana 1984"', add
label define sample_lbl 288200001 `"Ghana 2000"', add
label define sample_lbl 288201001 `"Ghana 2010"', add
label define sample_lbl 300197101 `"Greece 1971"', add
label define sample_lbl 300198101 `"Greece 1981"', add
label define sample_lbl 300199101 `"Greece 1991"', add
label define sample_lbl 300200101 `"Greece 2001"', add
label define sample_lbl 300201101 `"Greece 2011"', add
label define sample_lbl 320196401 `"Guatemala 1964"', add
label define sample_lbl 320197301 `"Guatemala 1973"', add
label define sample_lbl 320198101 `"Guatemala 1981"', add
label define sample_lbl 320199401 `"Guatemala 1994"', add
label define sample_lbl 320200201 `"Guatemala 2002"', add
label define sample_lbl 324198301 `"Guinea 1983"', add
label define sample_lbl 324199601 `"Guinea 1996"', add
label define sample_lbl 324201401 `"Guinea 2014"', add
label define sample_lbl 332197101 `"Haiti 1971"', add
label define sample_lbl 332198201 `"Haiti 1982"', add
label define sample_lbl 332200301 `"Haiti 2003"', add
label define sample_lbl 340196101 `"Honduras 1961"', add
label define sample_lbl 340197401 `"Honduras 1974"', add
label define sample_lbl 340198801 `"Honduras 1988"', add
label define sample_lbl 340200101 `"Honduras 2001"', add
label define sample_lbl 348197001 `"Hungary 1970"', add
label define sample_lbl 348198001 `"Hungary 1980"', add
label define sample_lbl 348199001 `"Hungary 1990"', add
label define sample_lbl 348200101 `"Hungary 2001"', add
label define sample_lbl 348201101 `"Hungary 2011"', add
label define sample_lbl 352170301 `"Iceland 1703"', add
label define sample_lbl 352172901 `"Iceland 1729"', add
label define sample_lbl 352180101 `"Iceland 1801"', add
label define sample_lbl 352190101 `"Iceland 1901"', add
label define sample_lbl 352191001 `"Iceland 1910"', add
label define sample_lbl 356198341 `"India 1983"', add
label define sample_lbl 356198741 `"India 1987"', add
label define sample_lbl 356199341 `"India 1993"', add
label define sample_lbl 356199941 `"India 1999"', add
label define sample_lbl 356200441 `"India 2004"', add
label define sample_lbl 356200941 `"India 2009"', add
label define sample_lbl 360197101 `"Indonesia 1971"', add
label define sample_lbl 360197601 `"Indonesia 1976"', add
label define sample_lbl 360198001 `"Indonesia 1980"', add
label define sample_lbl 360198501 `"Indonesia 1985"', add
label define sample_lbl 360199001 `"Indonesia 1990"', add
label define sample_lbl 360199501 `"Indonesia 1995"', add
label define sample_lbl 360200001 `"Indonesia 2000"', add
label define sample_lbl 360200501 `"Indonesia 2005"', add
label define sample_lbl 360201001 `"Indonesia 2010"', add
label define sample_lbl 364200601 `"Iran 2006"', add
label define sample_lbl 364201101 `"Iran 2011"', add
label define sample_lbl 368199701 `"Iraq 1997"', add
label define sample_lbl 372190101 `"Ireland 1901"', add
label define sample_lbl 372191101 `"Ireland 1911"', add
label define sample_lbl 372197101 `"Ireland 1971"', add
label define sample_lbl 372197901 `"Ireland 1979"', add
label define sample_lbl 372198101 `"Ireland 1981"', add
label define sample_lbl 372198601 `"Ireland 1986"', add
label define sample_lbl 372199101 `"Ireland 1991"', add
label define sample_lbl 372199601 `"Ireland 1996"', add
label define sample_lbl 372200201 `"Ireland 2002"', add
label define sample_lbl 372200601 `"Ireland 2006"', add
label define sample_lbl 372201101 `"Ireland 2011"', add
label define sample_lbl 372201601 `"Ireland 2016"', add
label define sample_lbl 376197201 `"Israel 1972"', add
label define sample_lbl 376198301 `"Israel 1983"', add
label define sample_lbl 376199501 `"Israel 1995"', add
label define sample_lbl 376200801 `"Israel 2008"', add
label define sample_lbl 380200101 `"Italy 2001"', add
label define sample_lbl 380201101 `"Italy 2011"', add
label define sample_lbl 380201121 `"Italy 2011 Q1 LFS"', add
label define sample_lbl 380201221 `"Italy 2012 Q1 LFS"', add
label define sample_lbl 380201321 `"Italy 2013 Q1 LFS"', add
label define sample_lbl 380201421 `"Italy 2014 Q1 LFS"', add
label define sample_lbl 380201521 `"Italy 2015 Q1 LFS"', add
label define sample_lbl 380201621 `"Italy 2016 Q1 LFS"', add
label define sample_lbl 380201721 `"Italy 2017 Q1 LFS"', add
label define sample_lbl 380201821 `"Italy 2018 Q1 LFS"', add
label define sample_lbl 380201921 `"Italy 2019 Q1 LFS"', add
label define sample_lbl 380202021 `"Italy 2020 Q1 LFS"', add
label define sample_lbl 384198801 `"Ivory Coast 1988"', add
label define sample_lbl 384199801 `"Ivory Coast 1998"', add
label define sample_lbl 388198201 `"Jamaica 1982"', add
label define sample_lbl 388199101 `"Jamaica 1991"', add
label define sample_lbl 388200101 `"Jamaica 2001"', add
label define sample_lbl 400200401 `"Jordan 2004"', add
label define sample_lbl 404196901 `"Kenya 1969"', add
label define sample_lbl 404197901 `"Kenya 1979"', add
label define sample_lbl 404198901 `"Kenya 1989"', add
label define sample_lbl 404199901 `"Kenya 1999"', add
label define sample_lbl 404200901 `"Kenya 2009"', add
label define sample_lbl 417199901 `"Kyrgyz Republic 1999"', add
label define sample_lbl 417200901 `"Kyrgyz Republic 2009"', add
label define sample_lbl 418199501 `"Laos 1995"', add
label define sample_lbl 418200501 `"Laos 2005"', add
label define sample_lbl 418201501 `"Laos 2015"', add
label define sample_lbl 426199601 `"Lesotho 1996"', add
label define sample_lbl 426200601 `"Lesotho 2006"', add
label define sample_lbl 430197401 `"Liberia 1974"', add
label define sample_lbl 430200801 `"Liberia 2008"', add
label define sample_lbl 454198701 `"Malawi 1987"', add
label define sample_lbl 454199801 `"Malawi 1998"', add
label define sample_lbl 454200801 `"Malawi 2008"', add
label define sample_lbl 458197001 `"Malaysia 1970"', add
label define sample_lbl 458198001 `"Malaysia 1980"', add
label define sample_lbl 458199101 `"Malaysia 1991"', add
label define sample_lbl 458200001 `"Malaysia 2000"', add
label define sample_lbl 466198701 `"Mali 1987"', add
label define sample_lbl 466199801 `"Mali 1998"', add
label define sample_lbl 466200901 `"Mali 2009"', add
label define sample_lbl 480199001 `"Mauritius 1990"', add
label define sample_lbl 480200001 `"Mauritius 2000"', add
label define sample_lbl 480201101 `"Mauritius 2011"', add
label define sample_lbl 484196001 `"Mexico 1960"', add
label define sample_lbl 484197001 `"Mexico 1970"', add
label define sample_lbl 484199001 `"Mexico 1990"', add
label define sample_lbl 484199501 `"Mexico 1995"', add
label define sample_lbl 484200001 `"Mexico 2000"', add
label define sample_lbl 484200501 `"Mexico 2005"', add
label define sample_lbl 484201001 `"Mexico 2010"', add
label define sample_lbl 484201501 `"Mexico 2015"', add
label define sample_lbl 484202001 `"Mexico 2020"', add
label define sample_lbl 484200521 `"Mexico 2005 Q1 LFS"', add
label define sample_lbl 484200522 `"Mexico 2005 Q2 LFS"', add
label define sample_lbl 484200523 `"Mexico 2005 Q3 LFS"', add
label define sample_lbl 484200524 `"Mexico 2005 Q4 LFS"', add
label define sample_lbl 484200621 `"Mexico 2006 Q1 LFS"', add
label define sample_lbl 484200622 `"Mexico 2006 Q2 LFS"', add
label define sample_lbl 484200623 `"Mexico 2006 Q3 LFS"', add
label define sample_lbl 484200624 `"Mexico 2006 Q4 LFS"', add
label define sample_lbl 484200721 `"Mexico 2007 Q1 LFS"', add
label define sample_lbl 484200722 `"Mexico 2007 Q2 LFS"', add
label define sample_lbl 484200723 `"Mexico 2007 Q3 LFS"', add
label define sample_lbl 484200724 `"Mexico 2007 Q4 LFS"', add
label define sample_lbl 484200821 `"Mexico 2008 Q1 LFS"', add
label define sample_lbl 484200822 `"Mexico 2008 Q2 LFS"', add
label define sample_lbl 484200823 `"Mexico 2008 Q3 LFS"', add
label define sample_lbl 484200824 `"Mexico 2008 Q4 LFS"', add
label define sample_lbl 484200921 `"Mexico 2009 Q1 LFS"', add
label define sample_lbl 484200922 `"Mexico 2009 Q2 LFS"', add
label define sample_lbl 484200923 `"Mexico 2009 Q3 LFS"', add
label define sample_lbl 484200924 `"Mexico 2009 Q4 LFS"', add
label define sample_lbl 484201021 `"Mexico 2010 Q1 LFS"', add
label define sample_lbl 484201022 `"Mexico 2010 Q2 LFS"', add
label define sample_lbl 484201023 `"Mexico 2010 Q3 LFS"', add
label define sample_lbl 484201024 `"Mexico 2010 Q4 LFS"', add
label define sample_lbl 484201121 `"Mexico 2011 Q1 LFS"', add
label define sample_lbl 484201122 `"Mexico 2011 Q2 LFS"', add
label define sample_lbl 484201123 `"Mexico 2011 Q3 LFS"', add
label define sample_lbl 484201124 `"Mexico 2011 Q4 LFS"', add
label define sample_lbl 484201221 `"Mexico 2012 Q1 LFS"', add
label define sample_lbl 484201222 `"Mexico 2012 Q2 LFS"', add
label define sample_lbl 484201223 `"Mexico 2012 Q3 LFS"', add
label define sample_lbl 484201224 `"Mexico 2012 Q4 LFS"', add
label define sample_lbl 484201321 `"Mexico 2013 Q1 LFS"', add
label define sample_lbl 484201322 `"Mexico 2013 Q2 LFS"', add
label define sample_lbl 484201323 `"Mexico 2013 Q3 LFS"', add
label define sample_lbl 484201324 `"Mexico 2013 Q4 LFS"', add
label define sample_lbl 484201421 `"Mexico 2014 Q1 LFS"', add
label define sample_lbl 484201422 `"Mexico 2014 Q2 LFS"', add
label define sample_lbl 484201423 `"Mexico 2014 Q3 LFS"', add
label define sample_lbl 484201424 `"Mexico 2014 Q4 LFS"', add
label define sample_lbl 484201521 `"Mexico 2015 Q1 LFS"', add
label define sample_lbl 484201522 `"Mexico 2015 Q2 LFS"', add
label define sample_lbl 484201523 `"Mexico 2015 Q3 LFS"', add
label define sample_lbl 484201524 `"Mexico 2015 Q4 LFS"', add
label define sample_lbl 484201621 `"Mexico 2016 Q1 LFS"', add
label define sample_lbl 484201622 `"Mexico 2016 Q2 LFS"', add
label define sample_lbl 484201623 `"Mexico 2016 Q3 LFS"', add
label define sample_lbl 484201624 `"Mexico 2016 Q4 LFS"', add
label define sample_lbl 484201721 `"Mexico 2017 Q1 LFS"', add
label define sample_lbl 484201722 `"Mexico 2017 Q2 LFS"', add
label define sample_lbl 484201723 `"Mexico 2017 Q3 LFS"', add
label define sample_lbl 484201724 `"Mexico 2017 Q4 LFS"', add
label define sample_lbl 484201821 `"Mexico 2018 Q1 LFS"', add
label define sample_lbl 484201822 `"Mexico 2018 Q2 LFS"', add
label define sample_lbl 484201823 `"Mexico 2018 Q3 LFS"', add
label define sample_lbl 484201824 `"Mexico 2018 Q4 LFS"', add
label define sample_lbl 484201921 `"Mexico 2019 Q1 LFS"', add
label define sample_lbl 484201922 `"Mexico 2019 Q2 LFS"', add
label define sample_lbl 484201923 `"Mexico 2019 Q3 LFS"', add
label define sample_lbl 484201924 `"Mexico 2019 Q4 LFS"', add
label define sample_lbl 484202021 `"Mexico 2020 Q1 LFS"', add
label define sample_lbl 484202023 `"Mexico 2020 Q3 LFS"', add
label define sample_lbl 496198901 `"Mongolia 1989"', add
label define sample_lbl 496200001 `"Mongolia 2000"', add
label define sample_lbl 504198201 `"Morocco 1982"', add
label define sample_lbl 504199401 `"Morocco 1994"', add
label define sample_lbl 504200401 `"Morocco 2004"', add
label define sample_lbl 504201401 `"Morocco 2014"', add
label define sample_lbl 508199701 `"Mozambique 1997"', add
label define sample_lbl 508200701 `"Mozambique 2007"', add
label define sample_lbl 104201401 `"Myanmar 2014"', add
label define sample_lbl 524200101 `"Nepal 2001"', add
label define sample_lbl 524201101 `"Nepal 2011"', add
label define sample_lbl 528196001 `"Netherlands 1960"', add
label define sample_lbl 528197101 `"Netherlands 1971"', add
label define sample_lbl 528200101 `"Netherlands 2001"', add
label define sample_lbl 528201101 `"Netherlands 2011"', add
label define sample_lbl 558197101 `"Nicaragua 1971"', add
label define sample_lbl 558199501 `"Nicaragua 1995"', add
label define sample_lbl 558200501 `"Nicaragua 2005"', add
label define sample_lbl 566200621 `"Nigeria 2006"', add
label define sample_lbl 566200721 `"Nigeria 2007"', add
label define sample_lbl 566200821 `"Nigeria 2008"', add
label define sample_lbl 566200921 `"Nigeria 2009"', add
label define sample_lbl 566201021 `"Nigeria 2010"', add
label define sample_lbl 578180101 `"Norway 1801"', add
label define sample_lbl 578186501 `"Norway 1865"', add
label define sample_lbl 578187501 `"Norway 1875"', add
label define sample_lbl 578190001 `"Norway 1900"', add
label define sample_lbl 578191001 `"Norway 1910"', add
label define sample_lbl 586197301 `"Pakistan 1973"', add
label define sample_lbl 586198101 `"Pakistan 1981"', add
label define sample_lbl 586199801 `"Pakistan 1998"', add
label define sample_lbl 275199701 `"Palestine 1997"', add
label define sample_lbl 275200701 `"Palestine 2007"', add
label define sample_lbl 275201701 `"Palestine 2017"', add
label define sample_lbl 591196001 `"Panama 1960"', add
label define sample_lbl 591197001 `"Panama 1970"', add
label define sample_lbl 591198001 `"Panama 1980"', add
label define sample_lbl 591199001 `"Panama 1990"', add
label define sample_lbl 591200001 `"Panama 2000"', add
label define sample_lbl 591201001 `"Panama 2010"', add
label define sample_lbl 598198001 `"Papua New Guinea 1980"', add
label define sample_lbl 598199001 `"Papua New Guinea 1990"', add
label define sample_lbl 598200001 `"Papua New Guinea 2000"', add
label define sample_lbl 600196201 `"Paraguay 1962"', add
label define sample_lbl 600197201 `"Paraguay 1972"', add
label define sample_lbl 600198201 `"Paraguay 1982"', add
label define sample_lbl 600199201 `"Paraguay 1992"', add
label define sample_lbl 600200201 `"Paraguay 2002"', add
label define sample_lbl 604199301 `"Peru 1993"', add
label define sample_lbl 604200701 `"Peru 2007"', add
label define sample_lbl 604201701 `"Peru 2017"', add
label define sample_lbl 608199001 `"Philippines 1990"', add
label define sample_lbl 608199501 `"Philippines 1995"', add
label define sample_lbl 608200001 `"Philippines 2000"', add
label define sample_lbl 608201001 `"Philippines 2010"', add
label define sample_lbl 616197801 `"Poland 1978"', add
label define sample_lbl 616198801 `"Poland 1988"', add
label define sample_lbl 616200201 `"Poland 2002"', add
label define sample_lbl 616201101 `"Poland 2011"', add
label define sample_lbl 620198101 `"Portugal 1981"', add
label define sample_lbl 620199101 `"Portugal 1991"', add
label define sample_lbl 620200101 `"Portugal 2001"', add
label define sample_lbl 620201101 `"Portugal 2011"', add
label define sample_lbl 630197001 `"Puerto Rico 1970"', add
label define sample_lbl 630198001 `"Puerto Rico 1980"', add
label define sample_lbl 630199001 `"Puerto Rico 1990"', add
label define sample_lbl 630200001 `"Puerto Rico 2000"', add
label define sample_lbl 630200501 `"Puerto Rico 2005"', add
label define sample_lbl 630201001 `"Puerto Rico 2010"', add
label define sample_lbl 630201501 `"Puerto Rico 2015"', add
label define sample_lbl 630202001 `"Puerto Rico 2020"', add
label define sample_lbl 642197701 `"Romania 1977"', add
label define sample_lbl 642199201 `"Romania 1992"', add
label define sample_lbl 642200201 `"Romania 2002"', add
label define sample_lbl 642201101 `"Romania 2011"', add
label define sample_lbl 643200201 `"Russia 2002"', add
label define sample_lbl 643201001 `"Russia 2010"', add
label define sample_lbl 646199101 `"Rwanda 1991"', add
label define sample_lbl 646200201 `"Rwanda 2002"', add
label define sample_lbl 646201201 `"Rwanda 2012"', add
label define sample_lbl 662198001 `"Saint Lucia 1980"', add
label define sample_lbl 662199101 `"Saint Lucia 1991"', add
label define sample_lbl 686198801 `"Senegal 1988"', add
label define sample_lbl 686200201 `"Senegal 2002"', add
label define sample_lbl 686201301 `"Senegal 2013"', add
label define sample_lbl 694200401 `"Sierra Leone 2004"', add
label define sample_lbl 694201501 `"Sierra Leone 2015"', add
label define sample_lbl 703199101 `"Slovak Republic 1991"', add
label define sample_lbl 703200101 `"Slovak Republic 2001"', add
label define sample_lbl 703201101 `"Slovak Republic 2011"', add
label define sample_lbl 705200201 `"Slovenia 2002"', add
label define sample_lbl 710199601 `"South Africa 1996"', add
label define sample_lbl 710200101 `"South Africa 2001"', add
label define sample_lbl 710200701 `"South Africa 2007"', add
label define sample_lbl 710201101 `"South Africa 2011"', add
label define sample_lbl 710201601 `"South Africa 2016"', add
label define sample_lbl 728200801 `"South Sudan 2008"', add
label define sample_lbl 724198101 `"Spain 1981"', add
label define sample_lbl 724199101 `"Spain 1991"', add
label define sample_lbl 724200101 `"Spain 2001"', add
label define sample_lbl 724201101 `"Spain 2011"', add
label define sample_lbl 724200521 `"Spain 2005 Q1 LFS"', add
label define sample_lbl 724200522 `"Spain 2005 Q2 LFS"', add
label define sample_lbl 724200523 `"Spain 2005 Q3 LFS"', add
label define sample_lbl 724200524 `"Spain 2005 Q4 LFS"', add
label define sample_lbl 724200621 `"Spain 2006 Q1 LFS"', add
label define sample_lbl 724200622 `"Spain 2006 Q2 LFS"', add
label define sample_lbl 724200623 `"Spain 2006 Q3 LFS"', add
label define sample_lbl 724200624 `"Spain 2006 Q4 LFS"', add
label define sample_lbl 724200721 `"Spain 2007 Q1 LFS"', add
label define sample_lbl 724200722 `"Spain 2007 Q2 LFS"', add
label define sample_lbl 724200723 `"Spain 2007 Q3 LFS"', add
label define sample_lbl 724200724 `"Spain 2007 Q4 LFS"', add
label define sample_lbl 724200821 `"Spain 2008 Q1 LFS"', add
label define sample_lbl 724200822 `"Spain 2008 Q2 LFS"', add
label define sample_lbl 724200823 `"Spain 2008 Q3 LFS"', add
label define sample_lbl 724200824 `"Spain 2008 Q4 LFS"', add
label define sample_lbl 724200921 `"Spain 2009 Q1 LFS"', add
label define sample_lbl 724200922 `"Spain 2009 Q2 LFS"', add
label define sample_lbl 724200923 `"Spain 2009 Q3 LFS"', add
label define sample_lbl 724200924 `"Spain 2009 Q4 LFS"', add
label define sample_lbl 724201021 `"Spain 2010 Q1 LFS"', add
label define sample_lbl 724201022 `"Spain 2010 Q2 LFS"', add
label define sample_lbl 724201023 `"Spain 2010 Q3 LFS"', add
label define sample_lbl 724201024 `"Spain 2010 Q4 LFS"', add
label define sample_lbl 724201121 `"Spain 2011 Q1 LFS"', add
label define sample_lbl 724201122 `"Spain 2011 Q2 LFS"', add
label define sample_lbl 724201123 `"Spain 2011 Q3 LFS"', add
label define sample_lbl 724201124 `"Spain 2011 Q4 LFS"', add
label define sample_lbl 724201221 `"Spain 2012 Q1 LFS"', add
label define sample_lbl 724201222 `"Spain 2012 Q2 LFS"', add
label define sample_lbl 724201223 `"Spain 2012 Q3 LFS"', add
label define sample_lbl 724201224 `"Spain 2012 Q4 LFS"', add
label define sample_lbl 724201321 `"Spain 2013 Q1 LFS"', add
label define sample_lbl 724201322 `"Spain 2013 Q2 LFS"', add
label define sample_lbl 724201323 `"Spain 2013 Q3 LFS"', add
label define sample_lbl 724201324 `"Spain 2013 Q4 LFS"', add
label define sample_lbl 724201421 `"Spain 2014 Q1 LFS"', add
label define sample_lbl 724201422 `"Spain 2014 Q2 LFS"', add
label define sample_lbl 724201423 `"Spain 2014 Q3 LFS"', add
label define sample_lbl 724201424 `"Spain 2014 Q4 LFS"', add
label define sample_lbl 724201521 `"Spain 2015 Q1 LFS"', add
label define sample_lbl 724201522 `"Spain 2015 Q2 LFS"', add
label define sample_lbl 724201523 `"Spain 2015 Q3 LFS"', add
label define sample_lbl 724201524 `"Spain 2015 Q4 LFS"', add
label define sample_lbl 724201621 `"Spain 2016 Q1 LFS"', add
label define sample_lbl 724201622 `"Spain 2016 Q2 LFS"', add
label define sample_lbl 724201623 `"Spain 2016 Q3 LFS"', add
label define sample_lbl 724201624 `"Spain 2016 Q4 LFS"', add
label define sample_lbl 724201721 `"Spain 2017 Q1 LFS"', add
label define sample_lbl 724201722 `"Spain 2017 Q2 LFS"', add
label define sample_lbl 724201723 `"Spain 2017 Q3 LFS"', add
label define sample_lbl 724201724 `"Spain 2017 Q4 LFS"', add
label define sample_lbl 724201821 `"Spain 2018 Q1 LFS"', add
label define sample_lbl 724201822 `"Spain 2018 Q2 LFS"', add
label define sample_lbl 724201823 `"Spain 2018 Q3 LFS"', add
label define sample_lbl 724201824 `"Spain 2018 Q4 LFS"', add
label define sample_lbl 724201921 `"Spain 2019 Q1 LFS"', add
label define sample_lbl 724201922 `"Spain 2019 Q2 LFS"', add
label define sample_lbl 724201923 `"Spain 2019 Q3 LFS"', add
label define sample_lbl 724201924 `"Spain 2019 Q4 LFS"', add
label define sample_lbl 724202021 `"Spain 2020 Q1 LFS"', add
label define sample_lbl 724202022 `"Spain 2020 Q2 LFS"', add
label define sample_lbl 724202023 `"Spain 2020 Q3 LFS"', add
label define sample_lbl 724202024 `"Spain 2020 Q4 LFS"', add
label define sample_lbl 729200801 `"Sudan 2008"', add
label define sample_lbl 740200401 `"Suriname 2004"', add
label define sample_lbl 740201201 `"Suriname 2012"', add
label define sample_lbl 752188001 `"Sweden 1880"', add
label define sample_lbl 752189001 `"Sweden 1890"', add
label define sample_lbl 752190001 `"Sweden 1900"', add
label define sample_lbl 752191001 `"Sweden 1910"', add
label define sample_lbl 756197001 `"Switzerland 1970"', add
label define sample_lbl 756198001 `"Switzerland 1980"', add
label define sample_lbl 756199001 `"Switzerland 1990"', add
label define sample_lbl 756200001 `"Switzerland 2000"', add
label define sample_lbl 756201101 `"Switzerland 2011"', add
label define sample_lbl 834198801 `"Tanzania 1988"', add
label define sample_lbl 834200201 `"Tanzania 2002"', add
label define sample_lbl 834201201 `"Tanzania 2012"', add
label define sample_lbl 764197001 `"Thailand 1970"', add
label define sample_lbl 764198001 `"Thailand 1980"', add
label define sample_lbl 764199001 `"Thailand 1990"', add
label define sample_lbl 764200001 `"Thailand 2000"', add
label define sample_lbl 768196001 `"Togo 1960"', add
label define sample_lbl 768197001 `"Togo 1970"', add
label define sample_lbl 768201001 `"Togo 2010"', add
label define sample_lbl 780197001 `"Trinidad and Tobago 1970"', add
label define sample_lbl 780198001 `"Trinidad and Tobago 1980"', add
label define sample_lbl 780199001 `"Trinidad and Tobago 1990"', add
label define sample_lbl 780200001 `"Trinidad and Tobago 2000"', add
label define sample_lbl 780201101 `"Trinidad and Tobago 2011"', add
label define sample_lbl 792198501 `"Turkey 1985"', add
label define sample_lbl 792199001 `"Turkey 1990"', add
label define sample_lbl 792200001 `"Turkey 2000"', add
label define sample_lbl 800199101 `"Uganda 1991"', add
label define sample_lbl 800200201 `"Uganda 2002"', add
label define sample_lbl 800201401 `"Uganda 2014"', add
label define sample_lbl 804200101 `"Ukraine 2001"', add
label define sample_lbl 826185101 `"United Kingdom 1851 (England and Wales)"', add
label define sample_lbl 826185102 `"United Kingdom 1851 (Scotland)"', add
label define sample_lbl 826185103 `"United Kingdom 1851 (2% sample)"', add
label define sample_lbl 826186101 `"United Kingdom 1861 (England and Wales)"', add
label define sample_lbl 826186102 `"United Kingdom 1861 (Scotland)"', add
label define sample_lbl 826187101 `"United Kingdom 1871 (Scotland)"', add
label define sample_lbl 826188101 `"United Kingdom 1881 (England and Wales)"', add
label define sample_lbl 826188102 `"United Kingdom 1881 (Scotland)"', add
label define sample_lbl 826189101 `"United Kingdom 1891 (England and Wales)"', add
label define sample_lbl 826189102 `"United Kingdom 1891 (Scotland)"', add
label define sample_lbl 826190101 `"United Kingdom 1901 (England and Wales)"', add
label define sample_lbl 826190102 `"United Kingdom 1901 (Scotland)"', add
label define sample_lbl 826191101 `"United Kingdom 1911 (England and Wales)"', add
label define sample_lbl 826196101 `"United Kingdom 1961"', add
label define sample_lbl 826197101 `"United Kingdom 1971"', add
label define sample_lbl 826199101 `"United Kingdom 1991"', add
label define sample_lbl 826200101 `"United Kingdom 2001"', add
label define sample_lbl 840185001 `"United States 1850 (100%)"', add
label define sample_lbl 840185002 `"United States 1850 (1%)"', add
label define sample_lbl 840186001 `"United States 1860 (1%)"', add
label define sample_lbl 840187001 `"United States 1870 (1%)"', add
label define sample_lbl 840188001 `"United States 1880 (100%)"', add
label define sample_lbl 840188002 `"United States 1880 (10%)"', add
label define sample_lbl 840190001 `"United States 1900 (5%)"', add
label define sample_lbl 840191001 `"United States 1910 (1%)"', add
label define sample_lbl 840196001 `"United States 1960"', add
label define sample_lbl 840197001 `"United States 1970"', add
label define sample_lbl 840198001 `"United States 1980"', add
label define sample_lbl 840199001 `"United States 1990"', add
label define sample_lbl 840200001 `"United States 2000"', add
label define sample_lbl 840200501 `"United States 2005"', add
label define sample_lbl 840201001 `"United States 2010"', add
label define sample_lbl 840201501 `"United States 2015"', add
label define sample_lbl 840202001 `"United States 2020"', add
label define sample_lbl 858196301 `"Uruguay 1963"', add
label define sample_lbl 858196302 `"Uruguay 1963 (full count)"', add
label define sample_lbl 858197501 `"Uruguay 1975"', add
label define sample_lbl 858197502 `"Uruguay 1975 (full count)"', add
label define sample_lbl 858198501 `"Uruguay 1985"', add
label define sample_lbl 858198502 `"Uruguay 1985 (full count)"', add
label define sample_lbl 858199601 `"Uruguay 1996"', add
label define sample_lbl 858199602 `"Uruguay 1996 (full count)"', add
label define sample_lbl 858200621 `"Uruguay 2006"', add
label define sample_lbl 858201101 `"Uruguay 2011"', add
label define sample_lbl 858201102 `"Uruguay 2011 (full count)"', add
label define sample_lbl 862197101 `"Venezuela 1971"', add
label define sample_lbl 862198101 `"Venezuela 1981"', add
label define sample_lbl 862199001 `"Venezuela 1990"', add
label define sample_lbl 862200101 `"Venezuela 2001"', add
label define sample_lbl 704198901 `"Vietnam 1989"', add
label define sample_lbl 704199901 `"Vietnam 1999"', add
label define sample_lbl 704200901 `"Vietnam 2009"', add
label define sample_lbl 704201901 `"Vietnam 2019"', add
label define sample_lbl 894199001 `"Zambia 1990"', add
label define sample_lbl 894200001 `"Zambia 2000"', add
label define sample_lbl 894201001 `"Zambia 2010"', add
label define sample_lbl 716201201 `"Zimbabwe 2012"', add
label values sample sample_lbl

label define subsamp_lbl 00 `"1st 1% subsample"'
label define subsamp_lbl 01 `"2nd 1% subsample"', add
label define subsamp_lbl 02 `"3rd 1% subsample"', add
label define subsamp_lbl 03 `"4th 1% subsample"', add
label define subsamp_lbl 04 `"5th 1% subsample"', add
label define subsamp_lbl 05 `"6th 1% subsample"', add
label define subsamp_lbl 06 `"7th 1% subsample"', add
label define subsamp_lbl 07 `"8th 1% subsample"', add
label define subsamp_lbl 08 `"9th 1% subsample"', add
label define subsamp_lbl 09 `"10th 1% subsample"', add
label define subsamp_lbl 10 `"11th 1% subsample"', add
label define subsamp_lbl 11 `"12th 1% subsample"', add
label define subsamp_lbl 12 `"13th 1% subsample"', add
label define subsamp_lbl 13 `"14th 1% subsample"', add
label define subsamp_lbl 14 `"15th 1% subsample"', add
label define subsamp_lbl 15 `"16th 1% subsample"', add
label define subsamp_lbl 16 `"17th 1% subsample"', add
label define subsamp_lbl 17 `"18th 1% subsample"', add
label define subsamp_lbl 18 `"19th 1% subsample"', add
label define subsamp_lbl 19 `"20th 1% subsample"', add
label define subsamp_lbl 20 `"21st 1% subsample"', add
label define subsamp_lbl 21 `"22nd 1% subsample"', add
label define subsamp_lbl 22 `"23rd 1% subsample"', add
label define subsamp_lbl 23 `"24th 1% subsample"', add
label define subsamp_lbl 24 `"25th 1% subsample"', add
label define subsamp_lbl 25 `"26th 1% subsample"', add
label define subsamp_lbl 26 `"27th 1% subsample"', add
label define subsamp_lbl 27 `"28th 1% subsample"', add
label define subsamp_lbl 28 `"29th 1% subsample"', add
label define subsamp_lbl 29 `"30th 1% subsample"', add
label define subsamp_lbl 30 `"31st 1% subsample"', add
label define subsamp_lbl 31 `"32nd 1% subsample"', add
label define subsamp_lbl 32 `"33rd 1% subsample"', add
label define subsamp_lbl 33 `"34th 1% subsample"', add
label define subsamp_lbl 34 `"35th 1% subsample"', add
label define subsamp_lbl 35 `"36th 1% subsample"', add
label define subsamp_lbl 36 `"37th 1% subsample"', add
label define subsamp_lbl 37 `"38th 1% subsample"', add
label define subsamp_lbl 38 `"39th 1% subsample"', add
label define subsamp_lbl 39 `"40th 1% subsample"', add
label define subsamp_lbl 40 `"41st 1% subsample"', add
label define subsamp_lbl 41 `"42nd 1% subsample"', add
label define subsamp_lbl 42 `"43rd 1% subsample"', add
label define subsamp_lbl 43 `"44th 1% subsample"', add
label define subsamp_lbl 44 `"45th 1% subsample"', add
label define subsamp_lbl 45 `"46th 1% subsample"', add
label define subsamp_lbl 46 `"47th 1% subsample"', add
label define subsamp_lbl 47 `"48th 1% subsample"', add
label define subsamp_lbl 48 `"49th 1% subsample"', add
label define subsamp_lbl 49 `"50th 1% subsample"', add
label define subsamp_lbl 50 `"51st 1% subsample"', add
label define subsamp_lbl 51 `"52nd 1% subsample"', add
label define subsamp_lbl 52 `"53rd 1% subsample"', add
label define subsamp_lbl 53 `"54th 1% subsample"', add
label define subsamp_lbl 54 `"55th 1% subsample"', add
label define subsamp_lbl 55 `"56th 1% subsample"', add
label define subsamp_lbl 56 `"57th 1% subsample"', add
label define subsamp_lbl 57 `"58th 1% subsample"', add
label define subsamp_lbl 58 `"59th 1% subsample"', add
label define subsamp_lbl 59 `"60th 1% subsample"', add
label define subsamp_lbl 60 `"61st 1% subsample"', add
label define subsamp_lbl 61 `"62nd 1% subsample"', add
label define subsamp_lbl 62 `"63rd 1% subsample"', add
label define subsamp_lbl 63 `"64th 1% subsample"', add
label define subsamp_lbl 64 `"65th 1% subsample"', add
label define subsamp_lbl 65 `"66th 1% subsample"', add
label define subsamp_lbl 66 `"67th 1% subsample"', add
label define subsamp_lbl 67 `"68th 1% subsample"', add
label define subsamp_lbl 68 `"69th 1% subsample"', add
label define subsamp_lbl 69 `"70th 1% subsample"', add
label define subsamp_lbl 70 `"71st 1% subsample"', add
label define subsamp_lbl 71 `"72nd 1% subsample"', add
label define subsamp_lbl 72 `"73rd 1% subsample"', add
label define subsamp_lbl 73 `"74th 1% subsample"', add
label define subsamp_lbl 74 `"75th 1% subsample"', add
label define subsamp_lbl 75 `"76th 1% subsample"', add
label define subsamp_lbl 76 `"77th 1% subsample"', add
label define subsamp_lbl 77 `"78th 1% subsample"', add
label define subsamp_lbl 78 `"79th 1% subsample"', add
label define subsamp_lbl 79 `"80th 1% subsample"', add
label define subsamp_lbl 80 `"81st 1% subsample"', add
label define subsamp_lbl 81 `"82nd 1% subsample"', add
label define subsamp_lbl 82 `"83rd 1% subsample"', add
label define subsamp_lbl 83 `"84th 1% subsample"', add
label define subsamp_lbl 84 `"85th 1% subsample"', add
label define subsamp_lbl 85 `"86th 1% subsample"', add
label define subsamp_lbl 86 `"87th 1% subsample"', add
label define subsamp_lbl 87 `"88th 1% subsample"', add
label define subsamp_lbl 88 `"89th 1% subsample"', add
label define subsamp_lbl 89 `"90th 1% subsample"', add
label define subsamp_lbl 90 `"91st 1% subsample"', add
label define subsamp_lbl 91 `"92nd 1% subsample"', add
label define subsamp_lbl 92 `"93rd 1% subsample"', add
label define subsamp_lbl 93 `"94th 1% subsample"', add
label define subsamp_lbl 94 `"95th 1% subsample"', add
label define subsamp_lbl 95 `"96th 1% subsample"', add
label define subsamp_lbl 96 `"97th 1% subsample"', add
label define subsamp_lbl 97 `"98th 1% subsample"', add
label define subsamp_lbl 98 `"99th 1% subsample"', add
label define subsamp_lbl 99 `"100th 1% subsample"', add
label values subsamp subsamp_lbl

label define urban_lbl 1 `"Rural"'
label define urban_lbl 2 `"Urban"', add
label define urban_lbl 9 `"Unknown"', add
label values urban urban_lbl

label define geo1_zw2012_lbl 000 `"Bulawayo"'
label define geo1_zw2012_lbl 001 `"Manicaland"', add
label define geo1_zw2012_lbl 002 `"Mashonaland Central"', add
label define geo1_zw2012_lbl 003 `"Mashonaland East"', add
label define geo1_zw2012_lbl 004 `"Mashonaland West"', add
label define geo1_zw2012_lbl 005 `"Matabeleland North"', add
label define geo1_zw2012_lbl 006 `"Matabeleland South"', add
label define geo1_zw2012_lbl 007 `"Midlands"', add
label define geo1_zw2012_lbl 008 `"Masvingo"', add
label define geo1_zw2012_lbl 009 `"Harare"', add
label values geo1_zw2012 geo1_zw2012_lbl

label define geo2_zw_lbl 716000021 `"Bulawayo Urban"'
label define geo2_zw_lbl 716001001 `"Buhera"', add
label define geo2_zw_lbl 716001002 `"Chimanimani"', add
label define geo2_zw_lbl 716001003 `"Chipinge Rural"', add
label define geo2_zw_lbl 716001004 `"Makoni"', add
label define geo2_zw_lbl 716001005 `"Mutare Rural"', add
label define geo2_zw_lbl 716001006 `"Mutasa"', add
label define geo2_zw_lbl 716001007 `"Nyanga"', add
label define geo2_zw_lbl 716001021 `"Mutare Urban"', add
label define geo2_zw_lbl 716001022 `"Rusape Urban"', add
label define geo2_zw_lbl 716001023 `"Chipinge Urban"', add
label define geo2_zw_lbl 716002001 `"Bindura Rural"', add
label define geo2_zw_lbl 716002002 `"Muzarabani"', add
label define geo2_zw_lbl 716002003 `"Guruve"', add
label define geo2_zw_lbl 716002004 `"Mazowe, Mvurwi"', add
label define geo2_zw_lbl 716002005 `"Mt Darwin"', add
label define geo2_zw_lbl 716002006 `"Rushinga"', add
label define geo2_zw_lbl 716002007 `"Shamva"', add
label define geo2_zw_lbl 716002008 `"Mbire"', add
label define geo2_zw_lbl 716002021 `"Bindura Urban"', add
label define geo2_zw_lbl 716003001 `"Chikomba"', add
label define geo2_zw_lbl 716003002 `"Goromonzi"', add
label define geo2_zw_lbl 716003003 `"Hwedza"', add
label define geo2_zw_lbl 716003004 `"Marondera Rural"', add
label define geo2_zw_lbl 716003005 `"Mudzi"', add
label define geo2_zw_lbl 716003006 `"Murehwa"', add
label define geo2_zw_lbl 716003007 `"Mutoko"', add
label define geo2_zw_lbl 716003008 `"Seke"', add
label define geo2_zw_lbl 716003009 `"Uzumba Maramba Pfungwe"', add
label define geo2_zw_lbl 716003021 `"Marondera Urban"', add
label define geo2_zw_lbl 716003023 `"Ruwa"', add
label define geo2_zw_lbl 716004001 `"Chegutu"', add
label define geo2_zw_lbl 716004002 `"Hurungwe, Karoi"', add
label define geo2_zw_lbl 716004003 `"Mhondoro-Ngezi"', add
label define geo2_zw_lbl 716004004 `"Kariba Rural"', add
label define geo2_zw_lbl 716004005 `"Makonde"', add
label define geo2_zw_lbl 716004006 `"Zvimba"', add
label define geo2_zw_lbl 716004007 `"Sanyati"', add
label define geo2_zw_lbl 716004021 `"Chinhoyi"', add
label define geo2_zw_lbl 716004022 `"Kadoma"', add
label define geo2_zw_lbl 716004023 `"Chegutu Urban"', add
label define geo2_zw_lbl 716004024 `"Kariba Urban"', add
label define geo2_zw_lbl 716004025 `"Norton"', add
label define geo2_zw_lbl 716005001 `"Binga"', add
label define geo2_zw_lbl 716005002 `"Bubi"', add
label define geo2_zw_lbl 716005003 `"Hwange Rural"', add
label define geo2_zw_lbl 716005004 `"Lupane"', add
label define geo2_zw_lbl 716005005 `"Nkayi"', add
label define geo2_zw_lbl 716005006 `"Tsholotsho"', add
label define geo2_zw_lbl 716005007 `"Umguza"', add
label define geo2_zw_lbl 716005021 `"Hwange Urban"', add
label define geo2_zw_lbl 716005022 `"Victoria Falls"', add
label define geo2_zw_lbl 716006001 `"Beitbridge Rural"', add
label define geo2_zw_lbl 716006002 `"Bulilima"', add
label define geo2_zw_lbl 716006003 `"Mangwe Rural, Mangwe Urban (Plumtree)"', add
label define geo2_zw_lbl 716006004 `"Gwanda Rural"', add
label define geo2_zw_lbl 716006005 `"Insiza"', add
label define geo2_zw_lbl 716006006 `"Matobo"', add
label define geo2_zw_lbl 716006007 `"Umzingwane"', add
label define geo2_zw_lbl 716006021 `"Gwanda Urban"', add
label define geo2_zw_lbl 716006022 `"Beitbridge Urban"', add
label define geo2_zw_lbl 716007001 `"Chirumanzu"', add
label define geo2_zw_lbl 716007002 `"Gokwe North"', add
label define geo2_zw_lbl 716007003 `"Gokwe South"', add
label define geo2_zw_lbl 716007004 `"Gweru Rural"', add
label define geo2_zw_lbl 716007005 `"Kwekwe Rural"', add
label define geo2_zw_lbl 716007006 `"Mberengwa"', add
label define geo2_zw_lbl 716007007 `"Shurugwi Rural"', add
label define geo2_zw_lbl 716007008 `"Zvishavane Rural"', add
label define geo2_zw_lbl 716007021 `"Gweru Urban"', add
label define geo2_zw_lbl 716007022 `"Kwekwe Urban"', add
label define geo2_zw_lbl 716007023 `"Redcliff"', add
label define geo2_zw_lbl 716007024 `"Zvishavane Urban"', add
label define geo2_zw_lbl 716007025 `"Gokwe Centre"', add
label define geo2_zw_lbl 716007026 `"Shurugwi Urban"', add
label define geo2_zw_lbl 716008001 `"Bikita"', add
label define geo2_zw_lbl 716008002 `"Chiredzi Rural"', add
label define geo2_zw_lbl 716008003 `"Chivi"', add
label define geo2_zw_lbl 716008004 `"Gutu"', add
label define geo2_zw_lbl 716008005 `"Masvingo Rural"', add
label define geo2_zw_lbl 716008006 `"Mwenezi"', add
label define geo2_zw_lbl 716008007 `"Zaka"', add
label define geo2_zw_lbl 716008021 `"Masvingo Urban"', add
label define geo2_zw_lbl 716008022 `"Chiredzi Town"', add
label define geo2_zw_lbl 716009001 `"Harare Rural"', add
label define geo2_zw_lbl 716009021 `"Harare Urban"', add
label define geo2_zw_lbl 716009022 `"Chitungwiza"', add
label define geo2_zw_lbl 716009023 `"Epworth"', add
label values geo2_zw geo2_zw_lbl

label define geo2_zw2012_lbl 000021 `"Bulawayo Urban"'
label define geo2_zw2012_lbl 001001 `"Buhera"', add
label define geo2_zw2012_lbl 001002 `"Chimanimani"', add
label define geo2_zw2012_lbl 001003 `"Chipinge Rural"', add
label define geo2_zw2012_lbl 001004 `"Makoni"', add
label define geo2_zw2012_lbl 001005 `"Mutare Rural"', add
label define geo2_zw2012_lbl 001006 `"Mutasa"', add
label define geo2_zw2012_lbl 001007 `"Nyanga"', add
label define geo2_zw2012_lbl 001021 `"Mutare Urban"', add
label define geo2_zw2012_lbl 001022 `"Rusape Urban"', add
label define geo2_zw2012_lbl 001023 `"Chipinge Urban"', add
label define geo2_zw2012_lbl 002001 `"Bindura Rural"', add
label define geo2_zw2012_lbl 002002 `"Muzarabani"', add
label define geo2_zw2012_lbl 002003 `"Guruve"', add
label define geo2_zw2012_lbl 002004 `"Mazowe, Mvurwi"', add
label define geo2_zw2012_lbl 002005 `"Mt Darwin"', add
label define geo2_zw2012_lbl 002006 `"Rushinga"', add
label define geo2_zw2012_lbl 002007 `"Shamva"', add
label define geo2_zw2012_lbl 002008 `"Mbire"', add
label define geo2_zw2012_lbl 002021 `"Bindura Urban"', add
label define geo2_zw2012_lbl 003001 `"Chikomba"', add
label define geo2_zw2012_lbl 003002 `"Goromonzi"', add
label define geo2_zw2012_lbl 003003 `"Hwedza"', add
label define geo2_zw2012_lbl 003004 `"Marondera Rural"', add
label define geo2_zw2012_lbl 003005 `"Mudzi"', add
label define geo2_zw2012_lbl 003006 `"Murehwa"', add
label define geo2_zw2012_lbl 003007 `"Mutoko"', add
label define geo2_zw2012_lbl 003008 `"Seke"', add
label define geo2_zw2012_lbl 003009 `"Uzumba Maramba Pfungwe"', add
label define geo2_zw2012_lbl 003021 `"Marondera Urban"', add
label define geo2_zw2012_lbl 003023 `"Ruwa"', add
label define geo2_zw2012_lbl 004001 `"Chegutu"', add
label define geo2_zw2012_lbl 004002 `"Hurungwe, Karoi"', add
label define geo2_zw2012_lbl 004003 `"Mhondoro-Ngezi"', add
label define geo2_zw2012_lbl 004004 `"Kariba Rural"', add
label define geo2_zw2012_lbl 004005 `"Makonde"', add
label define geo2_zw2012_lbl 004006 `"Zvimba"', add
label define geo2_zw2012_lbl 004007 `"Sanyati"', add
label define geo2_zw2012_lbl 004021 `"Chinhoyi"', add
label define geo2_zw2012_lbl 004022 `"Kadoma"', add
label define geo2_zw2012_lbl 004023 `"Chegutu Urban"', add
label define geo2_zw2012_lbl 004024 `"Kariba Urban"', add
label define geo2_zw2012_lbl 004025 `"Norton"', add
label define geo2_zw2012_lbl 005001 `"Binga"', add
label define geo2_zw2012_lbl 005002 `"Bubi"', add
label define geo2_zw2012_lbl 005003 `"Hwange Rural"', add
label define geo2_zw2012_lbl 005004 `"Lupane"', add
label define geo2_zw2012_lbl 005005 `"Nkayi"', add
label define geo2_zw2012_lbl 005006 `"Tsholotsho"', add
label define geo2_zw2012_lbl 005007 `"Umguza"', add
label define geo2_zw2012_lbl 005021 `"Hwange Urban"', add
label define geo2_zw2012_lbl 005022 `"Victoria Falls"', add
label define geo2_zw2012_lbl 006001 `"Beitbridge Rural"', add
label define geo2_zw2012_lbl 006002 `"Bulilima"', add
label define geo2_zw2012_lbl 006003 `"Mangwe Rural, Mangwe Urban (Plumtree)"', add
label define geo2_zw2012_lbl 006004 `"Gwanda Rural"', add
label define geo2_zw2012_lbl 006005 `"Insiza"', add
label define geo2_zw2012_lbl 006006 `"Matobo"', add
label define geo2_zw2012_lbl 006007 `"Umzingwane"', add
label define geo2_zw2012_lbl 006021 `"Gwanda Urban"', add
label define geo2_zw2012_lbl 006022 `"Beitbridge Urban"', add
label define geo2_zw2012_lbl 007001 `"Chirumanzu"', add
label define geo2_zw2012_lbl 007002 `"Gokwe North"', add
label define geo2_zw2012_lbl 007003 `"Gokwe South"', add
label define geo2_zw2012_lbl 007004 `"Gweru Rural"', add
label define geo2_zw2012_lbl 007005 `"Kwekwe Rural"', add
label define geo2_zw2012_lbl 007006 `"Mberengwa"', add
label define geo2_zw2012_lbl 007007 `"Shurugwi Rural"', add
label define geo2_zw2012_lbl 007008 `"Zvishavane Rural"', add
label define geo2_zw2012_lbl 007021 `"Gweru Urban"', add
label define geo2_zw2012_lbl 007022 `"Kwekwe Urban"', add
label define geo2_zw2012_lbl 007023 `"Redcliff"', add
label define geo2_zw2012_lbl 007024 `"Zvishavane Urban"', add
label define geo2_zw2012_lbl 007025 `"Gokwe Centre"', add
label define geo2_zw2012_lbl 007026 `"Shurugwi Urban"', add
label define geo2_zw2012_lbl 008001 `"Bikita"', add
label define geo2_zw2012_lbl 008002 `"Chiredzi Rural"', add
label define geo2_zw2012_lbl 008003 `"Chivi"', add
label define geo2_zw2012_lbl 008004 `"Gutu"', add
label define geo2_zw2012_lbl 008005 `"Masvingo Rural"', add
label define geo2_zw2012_lbl 008006 `"Mwenezi"', add
label define geo2_zw2012_lbl 008007 `"Zaka"', add
label define geo2_zw2012_lbl 008021 `"Masvingo Urban"', add
label define geo2_zw2012_lbl 008022 `"Chiredzi Town"', add
label define geo2_zw2012_lbl 009001 `"Harare Rural"', add
label define geo2_zw2012_lbl 009021 `"Harare Urban"', add
label define geo2_zw2012_lbl 009022 `"Chitungwiza"', add
label define geo2_zw2012_lbl 009023 `"Epworth"', add
label values geo2_zw2012 geo2_zw2012_lbl

label define geo3_zw2012_lbl 000021001 `"Bulawayo Urban Wards 01, 07, 20"'
label define geo3_zw2012_lbl 000021002 `"Bulawayo Urban Wards 10, 11"', add
label define geo3_zw2012_lbl 000021003 `"Bulawayo Urban Wards 17, 27"', add
label define geo3_zw2012_lbl 000021004 `"Bulawayo Urban Wards 21, 22"', add
label define geo3_zw2012_lbl 000021005 `"Bulawayo Urban Ward 12, 13"', add
label define geo3_zw2012_lbl 000021006 `"Bulawayo Urban Ward 28"', add
label define geo3_zw2012_lbl 000021007 `"Bulawayo Urban Wards 23, 24"', add
label define geo3_zw2012_lbl 000021008 `"Bulawayo Urban Wards 18, 29"', add
label define geo3_zw2012_lbl 000021009 `"Bulawayo Urban Wards 05, 06"', add
label define geo3_zw2012_lbl 000021010 `"Bulawayo Urban Ward 03"', add
label define geo3_zw2012_lbl 000021011 `"Bulawayo Urban Wards 15, 16"', add
label define geo3_zw2012_lbl 000021012 `"Bulawayo Urban Ward 09"', add
label define geo3_zw2012_lbl 000021013 `"Bulawayo Urban Ward 02"', add
label define geo3_zw2012_lbl 000021014 `"Bulawayo Urban Ward 04"', add
label define geo3_zw2012_lbl 000021015 `"Bulawayo Urban Ward 25"', add
label define geo3_zw2012_lbl 000021016 `"Bulawayo Urban Ward 08"', add
label define geo3_zw2012_lbl 000021017 `"Bulawayo Urban Ward 26"', add
label define geo3_zw2012_lbl 000021018 `"Bulawayo Urban Ward 19"', add
label define geo3_zw2012_lbl 000021019 `"Bulawayo Urban Ward 14"', add
label define geo3_zw2012_lbl 001001001 `"Buhera Wards 01, 02, 03, 04, 05, 08, 31"', add
label define geo3_zw2012_lbl 001001002 `"Buhera Wards 22, 23, 32"', add
label define geo3_zw2012_lbl 001001003 `"Buhera Wards 11, 12, 18, 20"', add
label define geo3_zw2012_lbl 001001004 `"Buhera Wards 06, 07, 14"', add
label define geo3_zw2012_lbl 001001005 `"Buhera Wards 09, 10, 13, 15"', add
label define geo3_zw2012_lbl 001001006 `"Buhera Wards 24, 26, 27"', add
label define geo3_zw2012_lbl 001001007 `"Buhera Wards 29, 30, 33"', add
label define geo3_zw2012_lbl 001001008 `"Buhera Wards 16, 17, 19, 21"', add
label define geo3_zw2012_lbl 001001009 `"Buhera Wards 25, 28"', add
label define geo3_zw2012_lbl 001002001 `"Chimanimani Wards 06, 09, 13, 15, 16, 17, 18, 19, 20"', add
label define geo3_zw2012_lbl 001002002 `"Chimanimani Wards 02, 03, 04, 05, 08"', add
label define geo3_zw2012_lbl 001002003 `"Chimanimani Wards 01, 07, 10, 11, 12, 14"', add
label define geo3_zw2012_lbl 001002004 `"Chimanimani Wards 21, 22, 23"', add
label define geo3_zw2012_lbl 001003001 `"Chipinge Rural Wards 22, 23, 25, 28"', add
label define geo3_zw2012_lbl 001003002 `"Chipinge Rural Wards 04, 05, 06, 10, 11, 12, 13"', add
label define geo3_zw2012_lbl 001003003 `"Chipinge Rural Wards 24, 26"', add
label define geo3_zw2012_lbl 001003004 `"Chipinge Rural Wards 14, 15, 18, 19"', add
label define geo3_zw2012_lbl 001003005 `"Chipinge Rural Wards 16, 20"', add
label define geo3_zw2012_lbl 001003006 `"Chipinge Rural Wards 07, 08, 09"', add
label define geo3_zw2012_lbl 001003007 `"Chipinge Rural Wards 01, 02, 03"', add
label define geo3_zw2012_lbl 001003008 `"Chipinge Rural Wards 27, 29, 30"', add
label define geo3_zw2012_lbl 001003009 `"Chipinge Rural Wards 17, 21"', add
label define geo3_zw2012_lbl 001004001 `"Makoni Wards 09, 10, 11, 17, 20, 21, 33"', add
label define geo3_zw2012_lbl 001004002 `"Makoni Wards 12, 15, 16, 26, 27, 29, 30"', add
label define geo3_zw2012_lbl 001004003 `"Makoni Wards 14, 24, 25, 28, 31, 39"', add
label define geo3_zw2012_lbl 001004004 `"Makoni Wards 05, 07, 08"', add
label define geo3_zw2012_lbl 001004005 `"Makoni Wards 01, 02, 03, 04, 35, 36"', add
label define geo3_zw2012_lbl 001004006 `"Makoni Wards 06, 13, 32, 34, 37, 38"', add
label define geo3_zw2012_lbl 001004007 `"Makoni Wards 18, 19, 22, 23"', add
label define geo3_zw2012_lbl 001005001 `"Mutare Rural Wards 04, 05, 10, 12, 13, 14, 17, 18, 20, 21, 35, 36"', add
label define geo3_zw2012_lbl 001005002 `"Mutare Rural Wards 24, 28, 29, 30"', add
label define geo3_zw2012_lbl 001005003 `"Mutare Rural Wards 02, 08, 11, 34"', add
label define geo3_zw2012_lbl 001005004 `"Mutare Rural Wards 06, 15, 32"', add
label define geo3_zw2012_lbl 001005005 `"Mutare Rural Wards 19, 23, 25, 26, 27"', add
label define geo3_zw2012_lbl 001005006 `"Mutare Rural Wards 01, 03, 31"', add
label define geo3_zw2012_lbl 001005007 `"Mutare Rural Wards 09, 16"', add
label define geo3_zw2012_lbl 001005008 `"Mutare Rural Wards 07, 22, 33"', add
label define geo3_zw2012_lbl 001006001 `"Mutasa Wards 01, 02, 03, 04, 05, 28, 29, 30"', add
label define geo3_zw2012_lbl 001006002 `"Mutasa Wards 17, 18, 20, 21, 24, 26, 27"', add
label define geo3_zw2012_lbl 001006003 `"Mutasa Wards 12, 13, 14, 15, 16, 22, 23, 25"', add
label define geo3_zw2012_lbl 001006004 `"Mutasa Wards 06, 07, 11, 31"', add
label define geo3_zw2012_lbl 001006005 `"Mutasa Wards 08, 09, 10, 19"', add
label define geo3_zw2012_lbl 001007001 `"Nyanga Wards 07, 08, 16, 20, 21, 23, 24, 25, 26, 27, 28"', add
label define geo3_zw2012_lbl 001007002 `"Nyanga Wards 17, 18, 19, 22, 29, 30, 31"', add
label define geo3_zw2012_lbl 001007003 `"Nyanga Wards 03, 05, 10, 11, 12"', add
label define geo3_zw2012_lbl 001007004 `"Nyanga Wards 01, 02, 04, 06"', add
label define geo3_zw2012_lbl 001007005 `"Nyanga Wards 09, 13, 14, 15"', add
label define geo3_zw2012_lbl 001021001 `"Mutare Urban Wards 03, 05, 17"', add
label define geo3_zw2012_lbl 001021002 `"Mutare Urban Wards 14, 16"', add
label define geo3_zw2012_lbl 001021003 `"Mutare Urban Wards 06, 08, 09, 18"', add
label define geo3_zw2012_lbl 001021004 `"Mutare Urban Wards 10, 11, 12"', add
label define geo3_zw2012_lbl 001021005 `"Mutare Urban Wards 07, 13, 15, 19"', add
label define geo3_zw2012_lbl 001021006 `"Mutare Urban Wards 01, 02, 04"', add
label define geo3_zw2012_lbl 001022001 `"Rusape Urban Wards 01, 02, 03, 04, 05, 06, 07, 08, 09, 10"', add
label define geo3_zw2012_lbl 001023001 `"Chipinge Urban Wards 01, 02, 03, 04, 05, 06, 07, 08"', add
label define geo3_zw2012_lbl 002001001 `"Bindura Rural Wards 09, 10, 13, 14, 15, 16, 17, 18"', add
label define geo3_zw2012_lbl 002001002 `"Bindura Rural Wards 01, 02, 03, 04, 05"', add
label define geo3_zw2012_lbl 002001003 `"Bindura Rural Wards 07, 08, 19, 20"', add
label define geo3_zw2012_lbl 002001004 `"Bindura Rural Wards 06, 11, 12, 21"', add
label define geo3_zw2012_lbl 002002001 `"Muzarabani Wards 01, 04, 05, 06, 08, 23, 24"', add
label define geo3_zw2012_lbl 002002002 `"Muzarabani Wards 02, 03, 10, 17, 18, 19, 27"', add
label define geo3_zw2012_lbl 002002003 `"Muzarabani Wards 12, 14, 15, 16, 22, 28"', add
label define geo3_zw2012_lbl 002002004 `"Muzarabani Wards 07, 09, 11, 13, 20, 21, 25, 26, 29"', add
label define geo3_zw2012_lbl 002003001 `"Guruve Wards 05, 06, 07, 22"', add
label define geo3_zw2012_lbl 002003002 `"Guruve Wards 08, 09, 10, 11, 12"', add
label define geo3_zw2012_lbl 002003003 `"Guruve Wards 01, 03, 13, 14, 15"', add
label define geo3_zw2012_lbl 002003004 `"Guruve Wards 02, 17, 19, 20, 21"', add
label define geo3_zw2012_lbl 002003005 `"Guruve Wards 04, 16, 18, 23, 24"', add
label define geo3_zw2012_lbl 002004001 `"Mazowe, Mvurwi Wards 07, 08, 09, 10, 11, 12, 13"', add
label define geo3_zw2012_lbl 002004002 `"Mazowe, Mvurwi Wards 18, 19, 20, 22, 34, 35"', add
label define geo3_zw2012_lbl 002004003 `"Mazowe, Mvurwi Wards 01, 02, 03, 04, 05, 06, 25, 26, 27, 29, 30"', add
label define geo3_zw2012_lbl 002004004 `"Mazowe, Mvurwi Wards 14, 15, 16, 17, 32, 33"', add
label define geo3_zw2012_lbl 002004005 `"Mazowe, Mvurwi Wards 01, 02, 03, 04, 05, 06"', add
label define geo3_zw2012_lbl 002004006 `"Mazowe, Mvurwi Wards 21, 23, 24, 31"', add
label define geo3_zw2012_lbl 002005001 `"Mt Darwin Wards 01, 03, 05, 06, 10, 11, 13, 25, 27, 28, 29, 30, 31, 32, 33, 37, 38"', add
label define geo3_zw2012_lbl 002005002 `"Mt Darwin Wards 09, 12, 14, 15, 24, 36"', add
label define geo3_zw2012_lbl 002005003 `"Mt Darwin Wards 18, 19, 20, 21, 22, 39, 40"', add
label define geo3_zw2012_lbl 002005004 `"Mt Darwin Wards 07, 08, 17, 35"', add
label define geo3_zw2012_lbl 002005005 `"Mt Darwin Wards 02, 04, 34"', add
label define geo3_zw2012_lbl 002005006 `"Mt Darwin Wards 16, 23, 26"', add
label define geo3_zw2012_lbl 002006001 `"Rushinga Wards 12, 13, 14, 15, 16, 18, 24, 25"', add
label define geo3_zw2012_lbl 002006002 `"Rushinga Wards 04, 05, 06, 08, 09, 10, 11, 17, 19"', add
label define geo3_zw2012_lbl 002006003 `"Rushinga Wards 01, 02, 03, 07, 20, 21, 22, 23"', add
label define geo3_zw2012_lbl 002007001 `"Shamva Wards 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 27"', add
label define geo3_zw2012_lbl 002007002 `"Shamva Wards 01, 02, 03, 04, 05, 06, 07,  08, 09, 10, 25, 26"', add
label define geo3_zw2012_lbl 002007003 `"Shamva Wards 22, 23, 24, 28, 29"', add
label define geo3_zw2012_lbl 002008001 `"Mbire Wards 01, 02, 03, 04, 07, 11, 12, 16"', add
label define geo3_zw2012_lbl 002008002 `"Mbire Wards 08, 09, 10, 17"', add
label define geo3_zw2012_lbl 002008003 `"Mbire Wards 05, 06, 13, 14, 15"', add
label define geo3_zw2012_lbl 002021001 `"Bindura Urban Wards 01, 02, 03, 04, 05, 06, 07, 08, 09, 10, 11, 12"', add
label define geo3_zw2012_lbl 003001001 `"Chikomba Wards 08, 09, 10, 11, 12, 14, 15, 16, 17"', add
label define geo3_zw2012_lbl 003001002 `"Chikomba Wards 21, 22, 23, 24, 25, 28, 29, 30"', add
label define geo3_zw2012_lbl 003001003 `"Chikomba Wards 01, 02, 03, 04, 05, 06, 07, 13"', add
label define geo3_zw2012_lbl 003001004 `"Chikomba Wards 18, 19, 20, 26, 27"', add
label define geo3_zw2012_lbl 003002001 `"Goromonzi Wards 16, 25"', add
label define geo3_zw2012_lbl 003002002 `"Goromonzi Wards 03, 06, 07, 14, 15"', add
label define geo3_zw2012_lbl 003002003 `"Goromonzi Wards 13, 17, 19, 20, 21, 22, 23, 24"', add
label define geo3_zw2012_lbl 003002004 `"Goromonzi Wards 04, 05"', add
label define geo3_zw2012_lbl 003002005 `"Goromonzi Wards 08, 09, 10, 11"', add
label define geo3_zw2012_lbl 003002006 `"Goromonzi Wards 12,18"', add
label define geo3_zw2012_lbl 003002007 `"Goromonzi Ward 01, 02"', add
label define geo3_zw2012_lbl 003003001 `"Hwedza Wards 01, 02, 03, 04, 05, 06, 07, 08, 15"', add
label define geo3_zw2012_lbl 003003002 `"Hwedza Wards 09, 10, 11, 12, 13, 14"', add
label define geo3_zw2012_lbl 003004001 `"Marondera Rural Wards 10, 11, 12, 13, 14, 15, 16, 17, 18"', add
label define geo3_zw2012_lbl 003004002 `"Marondera Rural Wards 05, 07, 08, 09, 21, 22"', add
label define geo3_zw2012_lbl 003004003 `"Marondera Rural Wards 06, 19, 20, 23"', add
label define geo3_zw2012_lbl 003004004 `"Marondera Rural Wards 01, 02, 03, 04"', add
label define geo3_zw2012_lbl 003005001 `"Mudzi Wards 05, 06, 07, 08, 09"', add
label define geo3_zw2012_lbl 003005002 `"Mudzi Wards 02, 03, 04, 17"', add
label define geo3_zw2012_lbl 003005003 `"Mudzi Wards 12, 15, 16"', add
label define geo3_zw2012_lbl 003005004 `"Mudzi Wards 01, 10, 11"', add
label define geo3_zw2012_lbl 003005005 `"Mudzi Wards 13, 14, 18"', add
label define geo3_zw2012_lbl 003006001 `"Murehwa Wards 19, 22, 23, 24, 29"', add
label define geo3_zw2012_lbl 003006002 `"Murehwa Wards 14, 15, 25, 26, 27, 28"', add
label define geo3_zw2012_lbl 003006003 `"Murehwa Wards 01, 02, 03, 04, 05, 06, 07, 09, 10"', add
label define geo3_zw2012_lbl 003006004 `"Murehwa Wards 08, 17, 18, 20, 21"', add
label define geo3_zw2012_lbl 003006005 `"Murehwa Wards 11, 12, 13"', add
label define geo3_zw2012_lbl 003006006 `"Murehwa Wards 16, 30"', add
label define geo3_zw2012_lbl 003007001 `"Mutoko Wards 04, 08, 09, 12, 13, 14, 15, 16, 17"', add
label define geo3_zw2012_lbl 003007002 `"Mutoko Wards 18, 19, 21, 22, 23, 24, 27, 28"', add
label define geo3_zw2012_lbl 003007003 `"Mutoko Wards 01, 02, 03, 05, 06, 10"', add
label define geo3_zw2012_lbl 003007004 `"Mutoko Wards 25, 26, 29"', add
label define geo3_zw2012_lbl 003007005 `"Mutoko Wards 07, 11, 20"', add
label define geo3_zw2012_lbl 003008001 `"Seke Wards 01, 02, 08"', add
label define geo3_zw2012_lbl 003008002 `"Seke Wards 11, 12, 13, 14, 15, 18, 19, 20, 21"', add
label define geo3_zw2012_lbl 003008003 `"Seke Wards 03, 04, 05, 06, 07"', add
label define geo3_zw2012_lbl 003008004 `"Seke Wards 09, 10, 16, 17"', add
label define geo3_zw2012_lbl 003009001 `"Uzumba Maramba Pfungwe Wards 01, 02, 03, 04, 05, 06, 16, 17"', add
label define geo3_zw2012_lbl 003009002 `"Uzumba Maramba Pfungwe Wards 09, 12, 13, 14, 15"', add
label define geo3_zw2012_lbl 003009003 `"Uzumba Maramba Pfungwe Wards 07, 08, 10, 11"', add
label define geo3_zw2012_lbl 003021001 `"Marondera Urban Wards 03, 04, 08, 09, 10, 11, 12"', add
label define geo3_zw2012_lbl 003021002 `"Marondera Urban Wards 01, 02, 05, 06, 07"', add
label define geo3_zw2012_lbl 003023001 `"Ruwa Wards 05, 07, 08, 09"', add
label define geo3_zw2012_lbl 003023002 `"Ruwa Wards 01, 02, 03, 04, 06"', add
label define geo3_zw2012_lbl 004001001 `"Chegutu Wards 03, 04, 08, 09, 10, 27"', add
label define geo3_zw2012_lbl 004001002 `"Chegutu Wards 12, 13, 14, 15, 16, 17, 18, 19, 21, 26, 29"', add
label define geo3_zw2012_lbl 004001003 `"Chegutu Wards 11, 20, 22"', add
label define geo3_zw2012_lbl 004001004 `"Chegutu Wards 01, 02, 05, 06, 07"', add
label define geo3_zw2012_lbl 004001005 `"Chegutu Wards 23, 24, 25, 28"', add
label define geo3_zw2012_lbl 004002001 `"Hurungwe, Karoi Wards 10, 11, 23, 25"', add
label define geo3_zw2012_lbl 004002002 `"Hurungwe, Karoi Wards 09, 22"', add
label define geo3_zw2012_lbl 004002003 `"Hurungwe, Karoi Wards 01, 02, 03, 03, 04, 05, 05, 06, 06, 07, 08, 09, 10, 20"', add
label define geo3_zw2012_lbl 004002004 `"Hurungwe, Karoi Wards 12, 13"', add
label define geo3_zw2012_lbl 004002005 `"Hurungwe, Karoi Wards 01, 08"', add
label define geo3_zw2012_lbl 004002006 `"Hurungwe, Karoi Ward 14"', add
label define geo3_zw2012_lbl 004002007 `"Hurungwe, Karoi Wards 15, 17, 24"', add
label define geo3_zw2012_lbl 004002008 `"Hurungwe, Karoi Wards 16, 26"', add
label define geo3_zw2012_lbl 004002009 `"Hurungwe, Karoi Wards 18, 21"', add
label define geo3_zw2012_lbl 004002010 `"Hurungwe, Karoi Wards 04, 07"', add
label define geo3_zw2012_lbl 004002011 `"Hurungwe, Karoi Wards 02, 19"', add
label define geo3_zw2012_lbl 004003001 `"Mhondoro-Ngezi Wards 01, 02, 03, 04, 05, 06, 07, 08"', add
label define geo3_zw2012_lbl 004003002 `"Mhondoro-Ngezi Wards 13, 15, 16"', add
label define geo3_zw2012_lbl 004003003 `"Mhondoro-Ngezi Wards 10, 12, 14"', add
label define geo3_zw2012_lbl 004003004 `"Mhondoro-Ngezi Wards 09, 11"', add
label define geo3_zw2012_lbl 004004001 `"Kariba Rural Wards 01, 02, 03, 04, 05, 06, 07, 08, 09, 10, 11, 12"', add
label define geo3_zw2012_lbl 004005001 `"Makonde Wards 03, 05, 06, 11, 13"', add
label define geo3_zw2012_lbl 004005002 `"Makonde Wards 07, 08, 09, 10"', add
label define geo3_zw2012_lbl 004005003 `"Makonde Wards 12, 14, 15, 16, 17, 18, 19"', add
label define geo3_zw2012_lbl 004005004 `"Makonde Wards 01, 02, 04"', add
label define geo3_zw2012_lbl 004006001 `"Zvimba Wards 19, 20, 21, 30, 32"', add
label define geo3_zw2012_lbl 004006002 `"Zvimba Wards 13, 14, 15, 16"', add
label define geo3_zw2012_lbl 004006003 `"Zvimba Wards 01, 02, 07, 08, 09, 10, 11, 12, 27, 28"', add
label define geo3_zw2012_lbl 004006004 `"Zvimba Wards 24, 35"', add
label define geo3_zw2012_lbl 004006005 `"Zvimba Wards 25, 26"', add
label define geo3_zw2012_lbl 004006006 `"Zvimba Wards 17, 18, 31"', add
label define geo3_zw2012_lbl 004006007 `"Zvimba Wards 22, 23, 33, 34"', add
label define geo3_zw2012_lbl 004006008 `"Zvimba Wards 03, 04, 05, 06, 29"', add
label define geo3_zw2012_lbl 004007001 `"Sanyati Wards 02, 03"', add
label define geo3_zw2012_lbl 004007002 `"Sanyati Wards 04, 06, 07, 08, 09, 13, 14, 15, 16, 17"', add
label define geo3_zw2012_lbl 004007003 `"Sanyati Wards 10, 11, 12, 18"', add
label define geo3_zw2012_lbl 004007004 `"Sanyati Wards 01, 05"', add
label define geo3_zw2012_lbl 004021001 `"Chinhoyi Wards 01, 03, 05, 09, 10, 11, 13, 14, 15"', add
label define geo3_zw2012_lbl 004021002 `"Chinhoyi Wards 02, 04, 06, 07, 08, 12"', add
label define geo3_zw2012_lbl 004022001 `"Kadoma Wards 01, 03, 07, 08, 13, 14"', add
label define geo3_zw2012_lbl 004022002 `"Kadoma Wards 09, 10, 11, 12, 15, 16, 17"', add
label define geo3_zw2012_lbl 004022003 `"Kadoma Wards 02, 04, 05, 06"', add
label define geo3_zw2012_lbl 004023001 `"Chegutu Urban Wards 01, 02, 03, 07, 10, 12"', add
label define geo3_zw2012_lbl 004023002 `"Chegutu Urban Wards 04, 05, 06, 08, 09, 11"', add
label define geo3_zw2012_lbl 004024001 `"Kariba Urban Wards 01, 02, 03, 04, 05, 06, 07, 08, 09"', add
label define geo3_zw2012_lbl 004025001 `"Norton Wards 01, 02, 03, 04, 06, 12, 13"', add
label define geo3_zw2012_lbl 004025002 `"Norton Wards 05, 07, 08, 09, 10, 11"', add
label define geo3_zw2012_lbl 005001001 `"Binga Wards 13, 14, 16, 17, 18, 19, 20, 21, 25"', add
label define geo3_zw2012_lbl 005001002 `"Binga Wards 01, 02, 03, 04, 05, 06, 22, 23"', add
label define geo3_zw2012_lbl 005001003 `"Binga Wards 07, 08, 09, 10, 11, 12, 15, 24"', add
label define geo3_zw2012_lbl 005002001 `"Bubi Wards 01, 02, 03, 04, 05, 06, 07, 08, 09, 10, 14, 16, 17, 18, 20, 21, 22"', add
label define geo3_zw2012_lbl 005002002 `"Bubi Wards 11, 12, 13, 15, 19, 23"', add
label define geo3_zw2012_lbl 005003001 `"Hwange Rural Wards 01, 02, 03, 04, 05, 06, 07, 08, 09, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20"', add
label define geo3_zw2012_lbl 005004001 `"Lupane Wards 01, 02, 03, 04, 05, 06, 08, 09, 13, 24, 25, 26, 27"', add
label define geo3_zw2012_lbl 005004002 `"Lupane Wards 14, 15, 18, 19, 21, 22, 23, 28"', add
label define geo3_zw2012_lbl 005004003 `"Lupane Wards 07, 10, 11, 12, 16, 17, 20"', add
label define geo3_zw2012_lbl 005005001 `"Nkayi Wards 01, 02, 03, 04, 05, 16, 17, 28"', add
label define geo3_zw2012_lbl 005005002 `"Nkayi Wards 11, 13, 14, 23, 24, 25, 27, 30"', add
label define geo3_zw2012_lbl 005005003 `"Nkayi Wards 06, 07, 08, 09, 10, 12, 15, 26"', add
label define geo3_zw2012_lbl 005005004 `"Nkayi Wards 18, 19, 20, 21, 22, 29"', add
label define geo3_zw2012_lbl 005006001 `"Tsholotsho Wards 01, 02, 03, 04, 07, 08, 09, 21"', add
label define geo3_zw2012_lbl 005006002 `"Tsholotsho Wards 10, 11, 12, 13, 14, 15, 20"', add
label define geo3_zw2012_lbl 005006003 `"Tsholotsho Wards 16, 17, 18, 19"', add
label define geo3_zw2012_lbl 005006004 `"Tsholotsho Wards 05, 06, 22"', add
label define geo3_zw2012_lbl 005007001 `"Umguza Wards 01, 02, 03, 04, 05, 06, 07"', add
label define geo3_zw2012_lbl 005007002 `"Umguza Wards 08, 09, 15, 16"', add
label define geo3_zw2012_lbl 005007003 `"Umguza Wards 10, 11, 12, 13, 14, 17, 18, 19"', add
label define geo3_zw2012_lbl 005021001 `"Hwange Urban Wards 01, 02, 03, 04, 05, 06, 07, 08, 09, 10, 11, 12, 14, 15"', add
label define geo3_zw2012_lbl 005021002 `"Hwange Urban Ward 13"', add
label define geo3_zw2012_lbl 005022001 `"Victoria Falls Wards 01, 02, 03, 04, 05, 06, 07, 08, 09, 10, 11"', add
label define geo3_zw2012_lbl 006001001 `"Beitbridge Rural Wards 07, 08, 09, 10, 11, 12, 13, 14"', add
label define geo3_zw2012_lbl 006001002 `"Beitbridge Rural Wards 04, 05, 06"', add
label define geo3_zw2012_lbl 006001003 `"Beitbridge Rural Wards 01, 02, 03, 15"', add
label define geo3_zw2012_lbl 006002001 `"Bulilima Wards 01, 02, 03, 04, 05, 17, 18, 19, 20, 21"', add
label define geo3_zw2012_lbl 006002002 `"Bulilima Wards 09, 10, 11, 12, 14, 15, 16"', add
label define geo3_zw2012_lbl 006002003 `"Bulilima Wards 06, 07, 08, 13, 22"', add
label define geo3_zw2012_lbl 006003001 `"Mangwe Rural, Mangwe Urban (Plumtree) Wards 01, 02, 03, 04, 05, 05, 06, 06, 07, 08, 09, 10, 11, 12, 15, 16, 17"', add
label define geo3_zw2012_lbl 006003002 `"Mangwe Rural, Mangwe Urban (Plumtree) Wards 01, 02, 03, 04, 13, 14"', add
label define geo3_zw2012_lbl 006004001 `"Gwanda Rural Wards 06, 07, 08, 09, 13, 14, 15, 18"', add
label define geo3_zw2012_lbl 006004002 `"Gwanda Rural Wards 01, 02, 03, 04, 05, 21, 22, 23"', add
label define geo3_zw2012_lbl 006004003 `"Gwanda Rural Wards 10, 11, 12, 16, 17, 19, 20, 24"', add
label define geo3_zw2012_lbl 006005001 `"Insiza Wards 04, 08, 13, 14, 15, 16, 17, 18, 22, 23"', add
label define geo3_zw2012_lbl 006005002 `"Insiza Wards 01, 02, 03, 05, 06, 07, 11, 12"', add
label define geo3_zw2012_lbl 006005003 `"Insiza Wards09, 10, 19, 20, 21"', add
label define geo3_zw2012_lbl 006006001 `"Matobo Wards 15, 16, 17, 18, 24, 25"', add
label define geo3_zw2012_lbl 006006002 `"Matobo Wards 10, 11, 12, 13, 14, 19"', add
label define geo3_zw2012_lbl 006006003 `"Matobo Wards 05, 06, 07, 08, 09, 20"', add
label define geo3_zw2012_lbl 006006004 `"Matobo Wards 01, 02, 03, 04, 21, 22, 23"', add
label define geo3_zw2012_lbl 006007001 `"Umzingwane Wards 03, 04, 05, 06, 07, 08, 09, 10, 11, 12, 13, 14, 20"', add
label define geo3_zw2012_lbl 006007002 `"Umzingwane Wards 01, 02, 15, 16, 17, 18, 19"', add
label define geo3_zw2012_lbl 006021001 `"Gwanda Urban Wards 01, 02, 03, 04, 05, 06, 07, 08, 09, 10"', add
label define geo3_zw2012_lbl 006022001 `"Beitbridge Urban Wards 01, 02, 03, 04, 05, 06"', add
label define geo3_zw2012_lbl 007001001 `"Chirumanzu Wards 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 22, 24"', add
label define geo3_zw2012_lbl 007001002 `"Chirumanzu Wards 01, 02, 05, 06, 09, 21"', add
label define geo3_zw2012_lbl 007001003 `"Chirumanzu Wards 03, 04, 07, 08, 10, 23, 25"', add
label define geo3_zw2012_lbl 007002001 `"Gokwe North Wards 10, 11, 12, 13, 35, 36"', add
label define geo3_zw2012_lbl 007002002 `"Gokwe North Wards 01, 02, 03, 28, 29, 30, 31"', add
label define geo3_zw2012_lbl 007002003 `"Gokwe North Wards 17, 18, 19, 22, 24, 25, 26"', add
label define geo3_zw2012_lbl 007002004 `"Gokwe North Wards 08, 09, 14, 34"', add
label define geo3_zw2012_lbl 007002005 `"Gokwe North Wards 06, 07, 16, 27, 32"', add
label define geo3_zw2012_lbl 007002007 `"Gokwe North Wards 04, 05, 33"', add
label define geo3_zw2012_lbl 007002008 `"Gokwe North Wards 15, 20, 21, 23"', add
label define geo3_zw2012_lbl 007003001 `"Gokwe South Wards 14, 23, 24, 25, 33"', add
label define geo3_zw2012_lbl 007003002 `"Gokwe South Wards 02, 26, 27, 29"', add
label define geo3_zw2012_lbl 007003003 `"Gokwe South Wards 01, 19, 20"', add
label define geo3_zw2012_lbl 007003004 `"Gokwe South Wards 09, 10"', add
label define geo3_zw2012_lbl 007003005 `"Gokwe South Wards 13, 16, 17"', add
label define geo3_zw2012_lbl 007003006 `"Gokwe South Wards 03, 08, 21, 22"', add
label define geo3_zw2012_lbl 007003007 `"Gokwe South Wards 04, 05, 06"', add
label define geo3_zw2012_lbl 007003008 `"Gokwe South Wards 07, 18, 31, 32"', add
label define geo3_zw2012_lbl 007003009 `"Gokwe South Wards 11, 12"', add
label define geo3_zw2012_lbl 007003010 `"Gokwe South Wards 15, 28, 30"', add
label define geo3_zw2012_lbl 007004001 `"Gweru Rural  Wards 01, 02, 04, 05, 09, 15, 16, 17, 19"', add
label define geo3_zw2012_lbl 007004002 `"Gweru Rural  Wards 10, 11, 12, 13, 14, 18"', add
label define geo3_zw2012_lbl 007004003 `"Gweru Rural  Wards03, 06, 07, 08"', add
label define geo3_zw2012_lbl 007005001 `"Kwekwe Rural Wards 09, 12, 13, 16, 17, 18, 19, 29"', add
label define geo3_zw2012_lbl 007005002 `"Kwekwe Rural Wards 20, 21, 22"', add
label define geo3_zw2012_lbl 007005003 `"Kwekwe Rural Wards 01, 02, 03, 04, 30"', add
label define geo3_zw2012_lbl 007005004 `"Kwekwe Rural Wards 23, 24, 25, 27, 28, 33"', add
label define geo3_zw2012_lbl 007005005 `"Kwekwe Rural Wards 06, 07, 08"', add
label define geo3_zw2012_lbl 007005006 `"Kwekwe Rural Wards 10, 11, 14, 32"', add
label define geo3_zw2012_lbl 007005007 `"Kwekwe Rural Wards 05, 15, 26, 31"', add
label define geo3_zw2012_lbl 007006001 `"Mberengwa Wards 15, 16, 22, 24, 25, 26, 27, 28, 33"', add
label define geo3_zw2012_lbl 007006002 `"Mberengwa Wards 01, 02, 03, 35, 36"', add
label define geo3_zw2012_lbl 007006003 `"Mberengwa Wards 04, 05, 06, 07, 08"', add
label define geo3_zw2012_lbl 007006004 `"Mberengwa Wards 09, 10, 11, 12, 13, 14, 17, 37"', add
label define geo3_zw2012_lbl 007006005 `"Mberengwa Wards 18, 19, 20, 21, 23"', add
label define geo3_zw2012_lbl 007006006 `"Mberengwa Wards 29, 30, 31, 32, 34"', add
label define geo3_zw2012_lbl 007007001 `"Shurugwi Rural Wards 01, 02, 03, 04, 05, 06, 16, 17, 18, 19, 20, 21, 22, 23"', add
label define geo3_zw2012_lbl 007007002 `"Shurugwi Rural Wards 07, 08 ,09, 10, 11, 12, 13, 14, 15, 24"', add
label define geo3_zw2012_lbl 007008001 `"Zvishavane Rural Wards 01, 02, 03, 04, 06, 07, 08, 10, 11, 12, 13"', add
label define geo3_zw2012_lbl 007008002 `"Zvishavane Rural Wards 05, 09, 14, 15, 16, 17, 18, 19"', add
label define geo3_zw2012_lbl 007021001 `"Gweru Urban Wards 09, 10, 11, 12, 13, 14"', add
label define geo3_zw2012_lbl 007021002 `"Gweru Urban Wards 15, 16, 17"', add
label define geo3_zw2012_lbl 007021003 `"Gweru Urban Wards 06, 07, 08"', add
label define geo3_zw2012_lbl 007021004 `"Gweru Urban Wards 01, 02, 18"', add
label define geo3_zw2012_lbl 007021005 `"Gweru Urban Wards 03, 04, 05"', add
label define geo3_zw2012_lbl 007022001 `"Kwekwe Urban Wards 01, 02, 03"', add
label define geo3_zw2012_lbl 007022002 `"Kwekwe Urban Wards 04, 05, 11, 12"', add
label define geo3_zw2012_lbl 007022003 `"Kwekwe Urban Wards 06, 07, 08, 09, 10, 13, 14"', add
label define geo3_zw2012_lbl 007023001 `"Redcliff Wards 01, 02, 03, 04, 05, 06, 07, 08, 09"', add
label define geo3_zw2012_lbl 007024001 `"Zvishavane Urban Wards 01, 02, 03, 04, 05, 06, 07, 08, 09, 10"', add
label define geo3_zw2012_lbl 007025001 `"Gokwe Centre Wards 01, 02, 03, 04, 05, 06"', add
label define geo3_zw2012_lbl 007026001 `"Shurugwi Urban Wards 01, 02, 03, 04, 05, 06, 07, 08, 09, 10, 11, 12, 13"', add
label define geo3_zw2012_lbl 008001001 `"Bikita Wards 04, 05, 06, 07, 09, 13"', add
label define geo3_zw2012_lbl 008001002 `"Bikita Wards 01, 02, 03, 26, 28, 29"', add
label define geo3_zw2012_lbl 008001003 `"Bikita Wards 20, 21, 22, 24, 25, 27"', add
label define geo3_zw2012_lbl 008001004 `"Bikita Wards 10, 11, 12, 19, 23, 30, 32"', add
label define geo3_zw2012_lbl 008001005 `"Bikita Wards 15, 17, 18, 31"', add
label define geo3_zw2012_lbl 008001006 `"Bikita Wards 08, 14, 16"', add
label define geo3_zw2012_lbl 008002001 `"Chiredzi Rural Wards 20, 21, 23"', add
label define geo3_zw2012_lbl 008002002 `"Chiredzi Rural Wards 06, 07, 08, 31"', add
label define geo3_zw2012_lbl 008002003 `"Chiredzi Rural Wards 09, 12, 13, 14, 15, 22"', add
label define geo3_zw2012_lbl 008002004 `"Chiredzi Rural Wards 24, 32"', add
label define geo3_zw2012_lbl 008002005 `"Chiredzi Rural Wards 19, 27, 30"', add
label define geo3_zw2012_lbl 008002006 `"Chiredzi Rural Wards 17, 26, 29"', add
label define geo3_zw2012_lbl 008002007 `"Chiredzi Rural Wards 16, 18, 28"', add
label define geo3_zw2012_lbl 008002008 `"Chiredzi Rural Wards 01, 02, 03, 04, 05, 25"', add
label define geo3_zw2012_lbl 008002009 `"Chiredzi Rural Wards 10, 11"', add
label define geo3_zw2012_lbl 008003001 `"Chivi Wards 01, 02, 03, 04, 05, 06, 08, 09, 11"', add
label define geo3_zw2012_lbl 008003002 `"Chivi Wards 25, 26, 27, 28, 29, 31"', add
label define geo3_zw2012_lbl 008003003 `"Chivi Warda 07, 10, 13, 14, 15, 17, 18"', add
label define geo3_zw2012_lbl 008003004 `"Chivi Wards 12, 16, 20, 22, 30"', add
label define geo3_zw2012_lbl 008003005 `"Chivi Wards 19, 21, 23, 24, 32"', add
label define geo3_zw2012_lbl 008004001 `"Gutu Wards 01, 02, 03, 04, 32"', add
label define geo3_zw2012_lbl 008004002 `"Gutu Wards 18, 20, 21, 22, 23, 35, 38, 40"', add
label define geo3_zw2012_lbl 008004003 `"Gutu Wards 05, 06, 07, 28, 29, 31"', add
label define geo3_zw2012_lbl 008004004 `"Gutu Wards 24, 25, 26, 27, 30, 41"', add
label define geo3_zw2012_lbl 008004005 `"Gutu Wards 10, 12, 13, 14"', add
label define geo3_zw2012_lbl 008004006 `"Gutu Wards 08, 33, 34, 37"', add
label define geo3_zw2012_lbl 008004007 `"Gutu Wards 15, 16, 36"', add
label define geo3_zw2012_lbl 008004008 `"Gutu Wards 09, 11, 17, 19, 39"', add
label define geo3_zw2012_lbl 008005001 `"Masvingo Rural Wards 01, 02, 03, 04, 05, 06, 09, 33"', add
label define geo3_zw2012_lbl 008005002 `"Masvingo Rural Wards 13, 15, 16, 17, 18"', add
label define geo3_zw2012_lbl 008005003 `"Masvingo Rural Wards 23, 24, 25, 26, 27, 28, 35"', add
label define geo3_zw2012_lbl 008005004 `"Masvingo Rural Wards 10, 21, 22, 31, 34"', add
label define geo3_zw2012_lbl 008005005 `"Masvingo Rural Wards 07, 08, 14, 32"', add
label define geo3_zw2012_lbl 008005006 `"Masvingo Rural Wards 29, 30"', add
label define geo3_zw2012_lbl 008005007 `"Masvingo Rural Wards 11, 12, 19, 20"', add
label define geo3_zw2012_lbl 008006001 `"Mwenezi Wards 04, 06, 07, 08, 11, 12, 14, 18"', add
label define geo3_zw2012_lbl 008006002 `"Mwenezi Wards 09, 10, 17"', add
label define geo3_zw2012_lbl 008006003 `"Mwenezi Wards 01, 02, 03, 05"', add
label define geo3_zw2012_lbl 008006004 `"Mwenezi Ward 13"', add
label define geo3_zw2012_lbl 008006005 `"Mwenezi Wards 15, 16"', add
label define geo3_zw2012_lbl 008007001 `"Zaka Wards 01, 02, 03, 04, 05, 07, 08, 18, 19, 33, 34"', add
label define geo3_zw2012_lbl 008007002 `"Zaka Wards 06, 11, 12, 16, 17, 22"', add
label define geo3_zw2012_lbl 008007003 `"Zaka Wards 27, 28, 29, 30, 32"', add
label define geo3_zw2012_lbl 008007004 `"Zaka Wards 15, 21, 26, 31"', add
label define geo3_zw2012_lbl 008007005 `"Zaka Wards 20, 23, 24, 25"', add
label define geo3_zw2012_lbl 008007006 `"Zaka Wards 09, 10, 13, 14"', add
label define geo3_zw2012_lbl 008021001 `"Masvingo Urban Wards 02, 03, 04, 05"', add
label define geo3_zw2012_lbl 008021002 `"Masvingo Urban Wards 01, 07"', add
label define geo3_zw2012_lbl 008021003 `"Masvingo Urban Wards 06, 08, 09, 10"', add
label define geo3_zw2012_lbl 008022001 `"Chiredzi Town Wards 01, 02, 03, 04, 05, 06, 07, 08"', add
label define geo3_zw2012_lbl 009001001 `"Harare Rural Ward 01"', add
label define geo3_zw2012_lbl 009021001 `"Harare Urban Ward 37"', add
label define geo3_zw2012_lbl 009021002 `"Harare Urban Wards 29, 30"', add
label define geo3_zw2012_lbl 009021003 `"Harare Urban Ward 15"', add
label define geo3_zw2012_lbl 009021004 `"Harare Urban Ward 23"', add
label define geo3_zw2012_lbl 009021005 `"Harare Urban Ward 43"', add
label define geo3_zw2012_lbl 009021006 `"Harare Urban Ward 33"', add
label define geo3_zw2012_lbl 009021007 `"Harare Urban Ward 41"', add
label define geo3_zw2012_lbl 009021008 `"Harare Urban Ward 09"', add
label define geo3_zw2012_lbl 009021009 `"Harare Urban Ward 16"', add
label define geo3_zw2012_lbl 009021010 `"Harare Urban Ward 42"', add
label define geo3_zw2012_lbl 009021011 `"Harare Urban Ward 40"', add
label define geo3_zw2012_lbl 009021012 `"Harare Urban Ward 32"', add
label define geo3_zw2012_lbl 009021013 `"Harare Urban Ward 22"', add
label define geo3_zw2012_lbl 009021014 `"Harare Urban Wards 03, 04"', add
label define geo3_zw2012_lbl 009021015 `"Harare Urban Ward 26"', add
label define geo3_zw2012_lbl 009021016 `"Harare Urban Ward 02"', add
label define geo3_zw2012_lbl 009021017 `"Harare Urban Wards 20, 46"', add
label define geo3_zw2012_lbl 009021018 `"Harare Urban Ward 38"', add
label define geo3_zw2012_lbl 009021019 `"Harare Urban Ward 18"', add
label define geo3_zw2012_lbl 009021020 `"Harare Urban Ward 27"', add
label define geo3_zw2012_lbl 009021021 `"Harare Urban Wards 34, 36"', add
label define geo3_zw2012_lbl 009021022 `"Harare Urban Ward 14"', add
label define geo3_zw2012_lbl 009021023 `"Harare Urban Ward 25"', add
label define geo3_zw2012_lbl 009021024 `"Harare Urban Ward 24"', add
label define geo3_zw2012_lbl 009021025 `"Harare Urban Ward 07"', add
label define geo3_zw2012_lbl 009021026 `"Harare Urban Ward 45"', add
label define geo3_zw2012_lbl 009021027 `"Harare Urban Ward 28"', add
label define geo3_zw2012_lbl 009021028 `"Harare Urban Ward 44"', add
label define geo3_zw2012_lbl 009021029 `"Harare Urban Ward 17"', add
label define geo3_zw2012_lbl 009021030 `"Harare Urban Ward 39"', add
label define geo3_zw2012_lbl 009021031 `"Harare Urban Ward 35"', add
label define geo3_zw2012_lbl 009021032 `"Harare Urban Ward 13"', add
label define geo3_zw2012_lbl 009021033 `"Harare Urban Ward 21"', add
label define geo3_zw2012_lbl 009021034 `"Harare Urban Ward 31"', add
label define geo3_zw2012_lbl 009021035 `"Harare Urban Ward 12"', add
label define geo3_zw2012_lbl 009021036 `"Harare Urban Ward 10"', add
label define geo3_zw2012_lbl 009021037 `"Harare Urban Ward 05"', add
label define geo3_zw2012_lbl 009021038 `"Harare Urban Ward 06"', add
label define geo3_zw2012_lbl 009021039 `"Harare Urban Ward 19"', add
label define geo3_zw2012_lbl 009021040 `"Harare Urban Ward 11"', add
label define geo3_zw2012_lbl 009021041 `"Harare Urban Ward 08"', add
label define geo3_zw2012_lbl 009022001 `"Chitungwiza Wards 06, 12, 13, 18"', add
label define geo3_zw2012_lbl 009022002 `"Chitungwiza Wards 01, 02, 03, 07, 08"', add
label define geo3_zw2012_lbl 009022003 `"Chitungwiza Wards 17, 19, 20"', add
label define geo3_zw2012_lbl 009022004 `"Chitungwiza Wards 22, 23"', add
label define geo3_zw2012_lbl 009022005 `"Chitungwiza Wards 09, 10, 11, 14"', add
label define geo3_zw2012_lbl 009022006 `"Chitungwiza Wards 04, 05"', add
label define geo3_zw2012_lbl 009022007 `"Chitungwiza Wards 21, 25"', add
label define geo3_zw2012_lbl 009022008 `"Chitungwiza Wards 15, 16"', add
label define geo3_zw2012_lbl 009022009 `"Chitungwiza Ward 24"', add
label define geo3_zw2012_lbl 009023001 `"Epworth Ward 01, 02"', add
label define geo3_zw2012_lbl 009023002 `"Epworth Ward 07"', add
label define geo3_zw2012_lbl 009023003 `"Epworth Ward 03, 05"', add
label define geo3_zw2012_lbl 009023004 `"Epworth Ward 06"', add
label define geo3_zw2012_lbl 009023005 `"Epworth Ward 04"', add
label values geo3_zw2012 geo3_zw2012_lbl

label define dhs_ipumsi_zw_lbl 01 `"Manicaland"'
label define dhs_ipumsi_zw_lbl 02 `"Mashonaland Central"', add
label define dhs_ipumsi_zw_lbl 03 `"Mashonaland East"', add
label define dhs_ipumsi_zw_lbl 04 `"Mashonaland West"', add
label define dhs_ipumsi_zw_lbl 05 `"Matabeleland North"', add
label define dhs_ipumsi_zw_lbl 06 `"Matabeleland South"', add
label define dhs_ipumsi_zw_lbl 07 `"Midlands"', add
label define dhs_ipumsi_zw_lbl 08 `"Masvingo"', add
label define dhs_ipumsi_zw_lbl 09 `"Harare"', add
label define dhs_ipumsi_zw_lbl 10 `"Bulawayo"', add
label values dhs_ipumsi_zw dhs_ipumsi_zw_lbl

label define ownership_lbl 0 `"NIU (not in universe)"'
label define ownership_lbl 1 `"Owned"', add
label define ownership_lbl 2 `"Not owned"', add
label define ownership_lbl 9 `"Unknown"', add
label values ownership ownership_lbl

label define ownershipd_lbl 000 `"NIU (not in universe)"'
label define ownershipd_lbl 100 `"Owned"', add
label define ownershipd_lbl 110 `"Owned, already paid"', add
label define ownershipd_lbl 120 `"Owned, still paying"', add
label define ownershipd_lbl 130 `"Owned, constructed"', add
label define ownershipd_lbl 140 `"Owned, inherited"', add
label define ownershipd_lbl 190 `"Owned, other"', add
label define ownershipd_lbl 191 `"Owned, house"', add
label define ownershipd_lbl 192 `"Owned, condominium"', add
label define ownershipd_lbl 193 `"Apartment proprietor"', add
label define ownershipd_lbl 194 `"Shared ownership"', add
label define ownershipd_lbl 200 `"Not owned"', add
label define ownershipd_lbl 210 `"Renting, not specified"', add
label define ownershipd_lbl 211 `"Renting, government"', add
label define ownershipd_lbl 212 `"Renting, local authority"', add
label define ownershipd_lbl 213 `"Renting, parastatal"', add
label define ownershipd_lbl 214 `"Renting, private"', add
label define ownershipd_lbl 215 `"Renting, private company"', add
label define ownershipd_lbl 216 `"Renting, individual"', add
label define ownershipd_lbl 217 `"Renting, collective"', add
label define ownershipd_lbl 218 `"Renting, joint state and individual"', add
label define ownershipd_lbl 219 `"Renting, public subsidized"', add
label define ownershipd_lbl 220 `"Renting, private subsidized"', add
label define ownershipd_lbl 221 `"Renting, co-tenant"', add
label define ownershipd_lbl 222 `"Renting, relative of tenant"', add
label define ownershipd_lbl 223 `"Renting, cooperative"', add
label define ownershipd_lbl 224 `"Renting, with a job or business"', add
label define ownershipd_lbl 225 `"Renting, loan-backed habitation"', add
label define ownershipd_lbl 226 `"Renting, mixed contract"', add
label define ownershipd_lbl 227 `"Furnished dwelling"', add
label define ownershipd_lbl 228 `"Sharecropping"', add
label define ownershipd_lbl 230 `"Subletting"', add
label define ownershipd_lbl 231 `"Rent to own"', add
label define ownershipd_lbl 239 `"Renting, other"', add
label define ownershipd_lbl 240 `"Occupied de facto/squatting"', add
label define ownershipd_lbl 250 `"Free/usufruct (no cash rent)"', add
label define ownershipd_lbl 251 `"Free, provided by employer"', add
label define ownershipd_lbl 252 `"Free, without work or services"', add
label define ownershipd_lbl 253 `"Free, provided by family or friend"', add
label define ownershipd_lbl 254 `"Free, private"', add
label define ownershipd_lbl 255 `"Free, public"', add
label define ownershipd_lbl 256 `"Free, condemned"', add
label define ownershipd_lbl 257 `"Free, other"', add
label define ownershipd_lbl 260 `"Endowment, Waqf (Egypt historical)"', add
label define ownershipd_lbl 290 `"Not owned, other"', add
label define ownershipd_lbl 999 `"Unknown"', add
label values ownershipd ownershipd_lbl

label define electric_lbl 0 `"NIU (not in universe)"'
label define electric_lbl 1 `"Yes"', add
label define electric_lbl 2 `"No"', add
label define electric_lbl 9 `"Unknown"', add
label values electric electric_lbl

label define watsup_lbl 00 `"NIU (not in universe)"'
label define watsup_lbl 10 `"Yes, piped water"', add
label define watsup_lbl 11 `"Piped inside dwelling"', add
label define watsup_lbl 12 `"Piped, exclusively to this household"', add
label define watsup_lbl 13 `"Piped, shared with other households"', add
label define watsup_lbl 14 `"Piped outside the dwelling"', add
label define watsup_lbl 15 `"Piped outside dwelling, in building"', add
label define watsup_lbl 16 `"Piped within the building or plot of land"', add
label define watsup_lbl 17 `"Piped outside the building or lot"', add
label define watsup_lbl 18 `"Have access to public piped water"', add
label define watsup_lbl 20 `"No piped water"', add
label define watsup_lbl 99 `"Unknown"', add
label values watsup watsup_lbl

label define fuelcook_lbl 00 `"NIU (not in universe)"'
label define fuelcook_lbl 10 `"None"', add
label define fuelcook_lbl 20 `"Electricity"', add
label define fuelcook_lbl 30 `"Petroleum gas, unspecified"', add
label define fuelcook_lbl 31 `"Gas -- piped/utility"', add
label define fuelcook_lbl 32 `"Gas -- tanked or bottled"', add
label define fuelcook_lbl 33 `"Propane"', add
label define fuelcook_lbl 34 `"Liquefied petroleum gas"', add
label define fuelcook_lbl 40 `"Petroleum liquid"', add
label define fuelcook_lbl 41 `"Oil, kerosene, and other liquid fuels"', add
label define fuelcook_lbl 42 `"Kerosene/paraffin"', add
label define fuelcook_lbl 43 `"Kerosene or oil"', add
label define fuelcook_lbl 44 `"Kerosene or gasoline"', add
label define fuelcook_lbl 45 `"Gasoline"', add
label define fuelcook_lbl 46 `"Cocinol"', add
label define fuelcook_lbl 47 `"Diesel"', add
label define fuelcook_lbl 50 `"Wood, coal, and other solid fuels"', add
label define fuelcook_lbl 51 `"Wood and other plant fuels"', add
label define fuelcook_lbl 52 `"Non-wood plant materials"', add
label define fuelcook_lbl 53 `"Coal or charcoal"', add
label define fuelcook_lbl 54 `"Charcoal"', add
label define fuelcook_lbl 55 `"Coal"', add
label define fuelcook_lbl 56 `"Wood or charcoal"', add
label define fuelcook_lbl 60 `"Multiple fuels"', add
label define fuelcook_lbl 61 `"Bottled gas and wood"', add
label define fuelcook_lbl 62 `"Propane and electricity"', add
label define fuelcook_lbl 63 `"Propane, kerosene, and electricity"', add
label define fuelcook_lbl 64 `"Propane and kerosene"', add
label define fuelcook_lbl 65 `"Kerosene and electrictiy"', add
label define fuelcook_lbl 66 `"Other combinations"', add
label define fuelcook_lbl 70 `"Other"', add
label define fuelcook_lbl 71 `"Alcohol"', add
label define fuelcook_lbl 72 `"Biogas"', add
label define fuelcook_lbl 73 `"Discarded or waste material"', add
label define fuelcook_lbl 74 `"Dung/manure"', add
label define fuelcook_lbl 75 `"Other combined organic waste materials"', add
label define fuelcook_lbl 76 `"Solar energy"', add
label define fuelcook_lbl 77 `"Candle"', add
label define fuelcook_lbl 99 `"Unknown/missing"', add
label values fuelcook fuelcook_lbl

label define toilet_lbl 00 `"NIU (not in universe)"'
label define toilet_lbl 10 `"No toilet"', add
label define toilet_lbl 11 `"No flush toilet"', add
label define toilet_lbl 20 `"Have toilet, type not specified"', add
label define toilet_lbl 21 `"Flush toilet"', add
label define toilet_lbl 22 `"Non-flush, latrine"', add
label define toilet_lbl 23 `"Non-flush, other and unspecified"', add
label define toilet_lbl 99 `"Unknown"', add
label values toilet toilet_lbl

label define mortnum_lbl 0 `"None"'
label define mortnum_lbl 1 `"1 death"', add
label define mortnum_lbl 2 `"2 deaths"', add
label define mortnum_lbl 3 `"3 deaths"', add
label define mortnum_lbl 4 `"4 deaths"', add
label define mortnum_lbl 5 `"5 deaths"', add
label define mortnum_lbl 6 `"6 deaths"', add
label define mortnum_lbl 7 `"7 or more deaths"', add
label define mortnum_lbl 8 `"Unknown"', add
label define mortnum_lbl 9 `"NIU (not in universe)"', add
label values mortnum mortnum_lbl

label define anymort_lbl 1 `"Yes"'
label define anymort_lbl 2 `"No"', add
label define anymort_lbl 8 `"Unknown/missing"', add
label define anymort_lbl 9 `"NIU (not in universe)"', add
label values anymort anymort_lbl

label define polymal_lbl 0 `"No more than one wife linked via SPLOC"'
label define polymal_lbl 1 `"More than one wife linked via SPLOC"', add
label values polymal polymal_lbl

label define poly2nd_lbl 0 `"Person is not the 2nd or higher order wife linked via SPLOC"'
label define poly2nd_lbl 1 `"Person is the 2nd or higher order wife linked via SPLOC"', add
label values poly2nd poly2nd_lbl

label define relate_lbl 1 `"Head"'
label define relate_lbl 2 `"Spouse/partner"', add
label define relate_lbl 3 `"Child"', add
label define relate_lbl 4 `"Other relative"', add
label define relate_lbl 5 `"Non-relative"', add
label define relate_lbl 6 `"Other relative or non-relative"', add
label define relate_lbl 9 `"Unknown"', add
label values relate relate_lbl

label define related_lbl 1000 `"Head"'
label define related_lbl 2000 `"Spouse/partner"', add
label define related_lbl 2100 `"Spouse"', add
label define related_lbl 2200 `"Unmarried partner"', add
label define related_lbl 2210 `"Civil union"', add
label define related_lbl 2300 `"Same-sex spouse/partner"', add
label define related_lbl 3000 `"Child"', add
label define related_lbl 3100 `"Biological child"', add
label define related_lbl 3200 `"Adopted child"', add
label define related_lbl 3300 `"Stepchild"', add
label define related_lbl 3400 `"Child/child-in-law"', add
label define related_lbl 3500 `"Child/child-in-law/grandchild"', add
label define related_lbl 3600 `"Child of unmarried partner"', add
label define related_lbl 4000 `"Other relative"', add
label define related_lbl 4100 `"Grandchild"', add
label define related_lbl 4110 `"Grandchild or great grandchild"', add
label define related_lbl 4120 `"Great grandchild"', add
label define related_lbl 4130 `"Great-great grandchild"', add
label define related_lbl 4200 `"Parent/parent-in-law"', add
label define related_lbl 4210 `"Parent"', add
label define related_lbl 4211 `"Stepparent"', add
label define related_lbl 4220 `"Parent-in-law"', add
label define related_lbl 4300 `"Child-in-law"', add
label define related_lbl 4301 `"Daughter-in-law"', add
label define related_lbl 4302 `"Spouse/partner of child"', add
label define related_lbl 4310 `"Unmarried partner of child"', add
label define related_lbl 4400 `"Sibling/sibling-in-law"', add
label define related_lbl 4410 `"Sibling"', add
label define related_lbl 4420 `"Stepsibling"', add
label define related_lbl 4430 `"Sibling-in-law"', add
label define related_lbl 4431 `"Sibling of spouse/partner"', add
label define related_lbl 4432 `"Spouse/partner of sibling"', add
label define related_lbl 4500 `"Grandparent"', add
label define related_lbl 4510 `"Great grandparent"', add
label define related_lbl 4600 `"Parent/grandparent/ascendant"', add
label define related_lbl 4700 `"Aunt/uncle"', add
label define related_lbl 4800 `"Other specified relative"', add
label define related_lbl 4810 `"Nephew/niece"', add
label define related_lbl 4820 `"Cousin"', add
label define related_lbl 4830 `"Sibling's sibling-in-law"', add
label define related_lbl 4900 `"Other relative, not elsewhere classified"', add
label define related_lbl 4910 `"Other relative with same family name"', add
label define related_lbl 4920 `"Other relative with different family name"', add
label define related_lbl 4930 `"Other relative, not specified (secondary family)"', add
label define related_lbl 5000 `"Non-relative"', add
label define related_lbl 5100 `"Friend/guest/visitor/partner"', add
label define related_lbl 5110 `"Partner/friend"', add
label define related_lbl 5111 `"Friend"', add
label define related_lbl 5112 `"Partner/roommate"', add
label define related_lbl 5113 `"Housemate/roommate"', add
label define related_lbl 5120 `"Visitor"', add
label define related_lbl 5130 `"Ex-spouse"', add
label define related_lbl 5140 `"Godparent"', add
label define related_lbl 5150 `"Godchild"', add
label define related_lbl 5200 `"Employee"', add
label define related_lbl 5210 `"Domestic employee"', add
label define related_lbl 5220 `"Relative of employee, n.s."', add
label define related_lbl 5221 `"Spouse of servant"', add
label define related_lbl 5222 `"Child of servant"', add
label define related_lbl 5223 `"Other relative of servant"', add
label define related_lbl 5300 `"Roomer/boarder/lodger/foster child"', add
label define related_lbl 5310 `"Boarder"', add
label define related_lbl 5311 `"Boarder or guest"', add
label define related_lbl 5320 `"Lodger"', add
label define related_lbl 5330 `"Foster child"', add
label define related_lbl 5340 `"Tutored/foster child"', add
label define related_lbl 5350 `"Tutored child"', add
label define related_lbl 5400 `"Employee, boarder, or guest"', add
label define related_lbl 5500 `"Other specified non-relative"', add
label define related_lbl 5510 `"Agregado"', add
label define related_lbl 5520 `"Temporary resident, guest"', add
label define related_lbl 5600 `"Group quarters"', add
label define related_lbl 5610 `"Group quarters, non-inmates"', add
label define related_lbl 5620 `"Institutional inmates"', add
label define related_lbl 5900 `"Non-relative, n.e.c."', add
label define related_lbl 6000 `"Other relative or non-relative"', add
label define related_lbl 9999 `"Unknown"', add
label values related related_lbl

label define age_lbl 000 `"Less than 1 year"'
label define age_lbl 001 `"1 year"', add
label define age_lbl 002 `"2 years"', add
label define age_lbl 003 `"3"', add
label define age_lbl 004 `"4"', add
label define age_lbl 005 `"5"', add
label define age_lbl 006 `"6"', add
label define age_lbl 007 `"7"', add
label define age_lbl 008 `"8"', add
label define age_lbl 009 `"9"', add
label define age_lbl 010 `"10"', add
label define age_lbl 011 `"11"', add
label define age_lbl 012 `"12"', add
label define age_lbl 013 `"13"', add
label define age_lbl 014 `"14"', add
label define age_lbl 015 `"15"', add
label define age_lbl 016 `"16"', add
label define age_lbl 017 `"17"', add
label define age_lbl 018 `"18"', add
label define age_lbl 019 `"19"', add
label define age_lbl 020 `"20"', add
label define age_lbl 021 `"21"', add
label define age_lbl 022 `"22"', add
label define age_lbl 023 `"23"', add
label define age_lbl 024 `"24"', add
label define age_lbl 025 `"25"', add
label define age_lbl 026 `"26"', add
label define age_lbl 027 `"27"', add
label define age_lbl 028 `"28"', add
label define age_lbl 029 `"29"', add
label define age_lbl 030 `"30"', add
label define age_lbl 031 `"31"', add
label define age_lbl 032 `"32"', add
label define age_lbl 033 `"33"', add
label define age_lbl 034 `"34"', add
label define age_lbl 035 `"35"', add
label define age_lbl 036 `"36"', add
label define age_lbl 037 `"37"', add
label define age_lbl 038 `"38"', add
label define age_lbl 039 `"39"', add
label define age_lbl 040 `"40"', add
label define age_lbl 041 `"41"', add
label define age_lbl 042 `"42"', add
label define age_lbl 043 `"43"', add
label define age_lbl 044 `"44"', add
label define age_lbl 045 `"45"', add
label define age_lbl 046 `"46"', add
label define age_lbl 047 `"47"', add
label define age_lbl 048 `"48"', add
label define age_lbl 049 `"49"', add
label define age_lbl 050 `"50"', add
label define age_lbl 051 `"51"', add
label define age_lbl 052 `"52"', add
label define age_lbl 053 `"53"', add
label define age_lbl 054 `"54"', add
label define age_lbl 055 `"55"', add
label define age_lbl 056 `"56"', add
label define age_lbl 057 `"57"', add
label define age_lbl 058 `"58"', add
label define age_lbl 059 `"59"', add
label define age_lbl 060 `"60"', add
label define age_lbl 061 `"61"', add
label define age_lbl 062 `"62"', add
label define age_lbl 063 `"63"', add
label define age_lbl 064 `"64"', add
label define age_lbl 065 `"65"', add
label define age_lbl 066 `"66"', add
label define age_lbl 067 `"67"', add
label define age_lbl 068 `"68"', add
label define age_lbl 069 `"69"', add
label define age_lbl 070 `"70"', add
label define age_lbl 071 `"71"', add
label define age_lbl 072 `"72"', add
label define age_lbl 073 `"73"', add
label define age_lbl 074 `"74"', add
label define age_lbl 075 `"75"', add
label define age_lbl 076 `"76"', add
label define age_lbl 077 `"77"', add
label define age_lbl 078 `"78"', add
label define age_lbl 079 `"79"', add
label define age_lbl 080 `"80"', add
label define age_lbl 081 `"81"', add
label define age_lbl 082 `"82"', add
label define age_lbl 083 `"83"', add
label define age_lbl 084 `"84"', add
label define age_lbl 085 `"85"', add
label define age_lbl 086 `"86"', add
label define age_lbl 087 `"87"', add
label define age_lbl 088 `"88"', add
label define age_lbl 089 `"89"', add
label define age_lbl 090 `"90"', add
label define age_lbl 091 `"91"', add
label define age_lbl 092 `"92"', add
label define age_lbl 093 `"93"', add
label define age_lbl 094 `"94"', add
label define age_lbl 095 `"95"', add
label define age_lbl 096 `"96"', add
label define age_lbl 097 `"97"', add
label define age_lbl 098 `"98"', add
label define age_lbl 099 `"99"', add
label define age_lbl 100 `"100+"', add
label define age_lbl 999 `"Not reported/missing"', add
label values age age_lbl

label define age2_lbl 01 `"0 to 4"'
label define age2_lbl 02 `"5 to 9"', add
label define age2_lbl 03 `"10 to 14"', add
label define age2_lbl 04 `"15 to 19"', add
label define age2_lbl 05 `"0 to 5"', add
label define age2_lbl 06 `"6 to 10"', add
label define age2_lbl 07 `"10 to 15"', add
label define age2_lbl 08 `"11 to 14"', add
label define age2_lbl 09 `"15 to 17"', add
label define age2_lbl 10 `"16 to 19"', add
label define age2_lbl 11 `"18 to 24"', add
label define age2_lbl 12 `"20 to 24"', add
label define age2_lbl 13 `"25 to 29"', add
label define age2_lbl 14 `"30 to 34"', add
label define age2_lbl 15 `"35 to 39"', add
label define age2_lbl 16 `"40 to 44"', add
label define age2_lbl 17 `"45 to 49"', add
label define age2_lbl 18 `"50 to 54"', add
label define age2_lbl 19 `"55 to 59"', add
label define age2_lbl 20 `"60 to 64"', add
label define age2_lbl 21 `"65 to 69"', add
label define age2_lbl 22 `"70 to 74"', add
label define age2_lbl 23 `"75 to 79"', add
label define age2_lbl 24 `"80 to 84"', add
label define age2_lbl 25 `"85+"', add
label define age2_lbl 98 `"Unknown"', add
label values age2 age2_lbl

label define sex_lbl 1 `"Male"'
label define sex_lbl 2 `"Female"', add
label define sex_lbl 9 `"Unknown"', add
label values sex sex_lbl

label define race_lbl 10 `"White"'
label define race_lbl 20 `"Black"', add
label define race_lbl 21 `"Black African"', add
label define race_lbl 22 `"Black Caribbean"', add
label define race_lbl 23 `"Afro-Ecuadorian"', add
label define race_lbl 24 `"Other Black"', add
label define race_lbl 30 `"Indigenous"', add
label define race_lbl 31 `"American Indian"', add
label define race_lbl 32 `"Latin American Indian"', add
label define race_lbl 40 `"Asian"', add
label define race_lbl 41 `"Chinese"', add
label define race_lbl 42 `"Japanese"', add
label define race_lbl 43 `"Korean"', add
label define race_lbl 44 `"Vietnamese"', add
label define race_lbl 45 `"Filipino"', add
label define race_lbl 46 `"Indian"', add
label define race_lbl 47 `"Pakistani"', add
label define race_lbl 48 `"Bangladeshi"', add
label define race_lbl 49 `"Other Asian"', add
label define race_lbl 50 `"Mixed race"', add
label define race_lbl 51 `"Brown (Brazil)"', add
label define race_lbl 52 `"Mestizo (Indigenous and White)"', add
label define race_lbl 53 `"Mulatto (Black and White)"', add
label define race_lbl 54 `"Coloured (South Africa)"', add
label define race_lbl 55 `"Creole (Suriname)"', add
label define race_lbl 56 `"Two or more races"', add
label define race_lbl 60 `"Other"', add
label define race_lbl 61 `"Montubio (Ecuador)"', add
label define race_lbl 99 `"Unknown"', add
label values race race_lbl

label define edattain_lbl 0 `"NIU (not in universe)"'
label define edattain_lbl 1 `"Less than primary completed"', add
label define edattain_lbl 2 `"Primary completed"', add
label define edattain_lbl 3 `"Secondary completed"', add
label define edattain_lbl 4 `"University completed"', add
label define edattain_lbl 9 `"Unknown"', add
label values edattain edattain_lbl

label define edattaind_lbl 000 `"NIU (not in universe)"'
label define edattaind_lbl 100 `"Less than primary completed (n.s.)"', add
label define edattaind_lbl 110 `"No schooling"', add
label define edattaind_lbl 120 `"Some primary completed"', add
label define edattaind_lbl 130 `"Primary (4 yrs) completed"', add
label define edattaind_lbl 211 `"Primary (5 yrs) completed"', add
label define edattaind_lbl 212 `"Primary (6 yrs) completed"', add
label define edattaind_lbl 221 `"Lower secondary general completed"', add
label define edattaind_lbl 222 `"Lower secondary technical completed"', add
label define edattaind_lbl 311 `"Secondary, general track completed"', add
label define edattaind_lbl 312 `"Some college completed"', add
label define edattaind_lbl 320 `"Secondary or post-secondary technical completed"', add
label define edattaind_lbl 321 `"Secondary, technical track completed"', add
label define edattaind_lbl 322 `"Post-secondary technical education"', add
label define edattaind_lbl 400 `"University completed"', add
label define edattaind_lbl 999 `"Unknown/missing"', add
label values edattaind edattaind_lbl

label define yrschool_lbl 00 `"None or pre-school"'
label define yrschool_lbl 01 `"1 year"', add
label define yrschool_lbl 02 `"2 years"', add
label define yrschool_lbl 03 `"3 years"', add
label define yrschool_lbl 04 `"4 years"', add
label define yrschool_lbl 05 `"5 years"', add
label define yrschool_lbl 06 `"6 years"', add
label define yrschool_lbl 07 `"7 years"', add
label define yrschool_lbl 08 `"8 years"', add
label define yrschool_lbl 09 `"9 years"', add
label define yrschool_lbl 10 `"10 years"', add
label define yrschool_lbl 11 `"11 years"', add
label define yrschool_lbl 12 `"12 years"', add
label define yrschool_lbl 13 `"13 years"', add
label define yrschool_lbl 14 `"14 years"', add
label define yrschool_lbl 15 `"15 years"', add
label define yrschool_lbl 16 `"16 years"', add
label define yrschool_lbl 17 `"17 years"', add
label define yrschool_lbl 18 `"18 years or more"', add
label define yrschool_lbl 90 `"Not specified"', add
label define yrschool_lbl 91 `"Some primary"', add
label define yrschool_lbl 92 `"Some technical after primary"', add
label define yrschool_lbl 93 `"Some secondary"', add
label define yrschool_lbl 94 `"Some tertiary"', add
label define yrschool_lbl 95 `"Adult literacy"', add
label define yrschool_lbl 96 `"Special education"', add
label define yrschool_lbl 98 `"Unknown/missing"', add
label define yrschool_lbl 99 `"NIU (not in universe)"', add
label values yrschool yrschool_lbl

label define educzw_lbl 00 `"NIU (not in universe)"'
label define educzw_lbl 10 `"None"', add
label define educzw_lbl 21 `"Preschool, grade 1"', add
label define educzw_lbl 22 `"Preschool, grade 2"', add
label define educzw_lbl 23 `"Preschool, grade 3"', add
label define educzw_lbl 29 `"Preschool, grade unknown"', add
label define educzw_lbl 31 `"Primary, grade 1"', add
label define educzw_lbl 32 `"Primary, grade 2"', add
label define educzw_lbl 33 `"Primary, grade 3"', add
label define educzw_lbl 34 `"Primary, grade 4"', add
label define educzw_lbl 35 `"Primary, grade 5"', add
label define educzw_lbl 36 `"Primary, grade 6"', add
label define educzw_lbl 37 `"Primary, grade 7"', add
label define educzw_lbl 38 `"Primary, grade unknown"', add
label define educzw_lbl 41 `"Lower secondary, grade 1"', add
label define educzw_lbl 42 `"Lower secondary, grade 2"', add
label define educzw_lbl 43 `"Lower secondary, grade 3"', add
label define educzw_lbl 44 `"Lower secondary, grade 4"', add
label define educzw_lbl 45 `"Upper secondary, grade 5"', add
label define educzw_lbl 46 `"Upper secondary, grade 6"', add
label define educzw_lbl 49 `"Secondary, grade unknown"', add
label define educzw_lbl 51 `"Tertiary, certificate or diploma after primary"', add
label define educzw_lbl 52 `"Tertiary, certificate or diploma after secondary"', add
label define educzw_lbl 53 `"Tertiary, undergraduate or graduate studies"', add
label define educzw_lbl 59 `"Tertiary, unknown level"', add
label define educzw_lbl 98 `"Unknown"', add
label values educzw educzw_lbl

label define empstat_lbl 0 `"NIU (not in universe)"'
label define empstat_lbl 1 `"Employed"', add
label define empstat_lbl 2 `"Unemployed"', add
label define empstat_lbl 3 `"Inactive"', add
label define empstat_lbl 9 `"Unknown/missing"', add
label values empstat empstat_lbl

label define empstatd_lbl 000 `"NIU (not in universe)"'
label define empstatd_lbl 100 `"Employed, not specified"', add
label define empstatd_lbl 110 `"At work"', add
label define empstatd_lbl 111 `"At work, and 'student'"', add
label define empstatd_lbl 112 `"At work, and 'housework'"', add
label define empstatd_lbl 113 `"At work, and 'seeking work'"', add
label define empstatd_lbl 114 `"At work, and 'retired'"', add
label define empstatd_lbl 115 `"At work, and 'no work'"', add
label define empstatd_lbl 116 `"At work, and other situation"', add
label define empstatd_lbl 117 `"At work, family holding, not specified"', add
label define empstatd_lbl 118 `"At work, family holding, not agricultural"', add
label define empstatd_lbl 119 `"At work, family holding, agricultural"', add
label define empstatd_lbl 120 `"Have job, not at work in reference period"', add
label define empstatd_lbl 130 `"Armed forces"', add
label define empstatd_lbl 131 `"Armed forces, at work"', add
label define empstatd_lbl 132 `"Armed forces, not at work in reference period"', add
label define empstatd_lbl 133 `"Military trainee"', add
label define empstatd_lbl 140 `"Marginally employed"', add
label define empstatd_lbl 200 `"Unemployed, not specified"', add
label define empstatd_lbl 201 `"Unemployed 6 or more months"', add
label define empstatd_lbl 202 `"Worked fewer than 6 months, permanent job"', add
label define empstatd_lbl 203 `"Worked fewer than 6 months, temporary job"', add
label define empstatd_lbl 210 `"Unemployed, experienced worker"', add
label define empstatd_lbl 220 `"Unemployed, new worker"', add
label define empstatd_lbl 230 `"No work available"', add
label define empstatd_lbl 240 `"Inactive unemployed"', add
label define empstatd_lbl 300 `"Inactive (not in labor force)"', add
label define empstatd_lbl 310 `"Housework"', add
label define empstatd_lbl 320 `"Unable to work, disabled or health reasons"', add
label define empstatd_lbl 321 `"Permanent disability"', add
label define empstatd_lbl 322 `"Temporary illness"', add
label define empstatd_lbl 323 `"Disabled or imprisoned"', add
label define empstatd_lbl 330 `"In school"', add
label define empstatd_lbl 340 `"Retirees and living on rent"', add
label define empstatd_lbl 341 `"Living on rents"', add
label define empstatd_lbl 342 `"Living on rents or pension"', add
label define empstatd_lbl 343 `"Retirees/pensioners"', add
label define empstatd_lbl 344 `"Retired"', add
label define empstatd_lbl 345 `"Pensioner"', add
label define empstatd_lbl 346 `"Non-retirement pension"', add
label define empstatd_lbl 347 `"Disability pension"', add
label define empstatd_lbl 348 `"Retired without benefits"', add
label define empstatd_lbl 350 `"Elderly"', add
label define empstatd_lbl 351 `"Elderly or disabled"', add
label define empstatd_lbl 360 `"Institutionalized"', add
label define empstatd_lbl 361 `"Prisoner"', add
label define empstatd_lbl 370 `"Intermittent worker"', add
label define empstatd_lbl 371 `"Not working, seasonal worker"', add
label define empstatd_lbl 372 `"Not working, occasional worker"', add
label define empstatd_lbl 380 `"Other income recipient"', add
label define empstatd_lbl 390 `"Inactive, other reasons"', add
label define empstatd_lbl 391 `"Too young to work"', add
label define empstatd_lbl 392 `"Dependent"', add
label define empstatd_lbl 999 `"Unknown/missing"', add
label values empstatd empstatd_lbl

label define labforce_lbl 1 `"No, not in the labor force"'
label define labforce_lbl 2 `"Yes, in the labor force"', add
label define labforce_lbl 8 `"Unknown"', add
label define labforce_lbl 9 `"NIU (not in universe)"', add
label values labforce labforce_lbl

label define occisco_lbl 01 `"Legislators, senior officials and managers"'
label define occisco_lbl 02 `"Professionals"', add
label define occisco_lbl 03 `"Technicians and associate professionals"', add
label define occisco_lbl 04 `"Clerks"', add
label define occisco_lbl 05 `"Service workers and shop and market sales"', add
label define occisco_lbl 06 `"Skilled agricultural and fishery workers"', add
label define occisco_lbl 07 `"Crafts and related trades workers"', add
label define occisco_lbl 08 `"Plant and machine operators and assemblers"', add
label define occisco_lbl 09 `"Elementary occupations"', add
label define occisco_lbl 10 `"Armed forces"', add
label define occisco_lbl 11 `"Other occupations, unspecified or n.e.c."', add
label define occisco_lbl 97 `"Response suppressed"', add
label define occisco_lbl 98 `"Unknown"', add
label define occisco_lbl 99 `"NIU (not in universe)"', add
label values occisco occisco_lbl

label define isco88a_lbl 010 `"Armed forces"'
label define isco88a_lbl 111 `"Legislators"', add
label define isco88a_lbl 112 `"Senior government officials"', add
label define isco88a_lbl 113 `"Traditional chiefs and heads of villages"', add
label define isco88a_lbl 114 `"Senior officials of special-interest organizations"', add
label define isco88a_lbl 121 `"Directors and chief executives"', add
label define isco88a_lbl 122 `"Production and operations department managers"', add
label define isco88a_lbl 123 `"Other department managers"', add
label define isco88a_lbl 131 `"General managers"', add
label define isco88a_lbl 199 `"Legislators, senior officials and managers not elsewhere classified"', add
label define isco88a_lbl 211 `"Physicists, chemists and related professionals"', add
label define isco88a_lbl 212 `"Mathematicians, statisticians and related professionals"', add
label define isco88a_lbl 213 `"Computing professionals"', add
label define isco88a_lbl 214 `"Architects, engineers and related professionals"', add
label define isco88a_lbl 221 `"Life science professionals"', add
label define isco88a_lbl 222 `"Health professionals (except nursing)"', add
label define isco88a_lbl 223 `"Nursing and midwifery professionals"', add
label define isco88a_lbl 231 `"College, university and higher education teaching professionals"', add
label define isco88a_lbl 232 `"Secondary education teaching professionals"', add
label define isco88a_lbl 233 `"Primary and pre-primary education teaching professionals"', add
label define isco88a_lbl 234 `"Special education teaching professionals"', add
label define isco88a_lbl 235 `"Other teaching professionals"', add
label define isco88a_lbl 241 `"Business professionals"', add
label define isco88a_lbl 242 `"Legal professionals"', add
label define isco88a_lbl 243 `"Archivists, librarians and related information professionals"', add
label define isco88a_lbl 244 `"Social science and related professionals"', add
label define isco88a_lbl 245 `"Writers and creative or performing artists"', add
label define isco88a_lbl 246 `"Religious professionals"', add
label define isco88a_lbl 299 `"Professionals no elsewhere classified"', add
label define isco88a_lbl 311 `"Physical and engineering science technicians"', add
label define isco88a_lbl 312 `"Computer associate professionals"', add
label define isco88a_lbl 313 `"Optical and electronic equipment operators"', add
label define isco88a_lbl 314 `"Ship and aircraft controllers and technicians"', add
label define isco88a_lbl 315 `"Safety and quality inspectors"', add
label define isco88a_lbl 321 `"Life science technicians and related associate professionals"', add
label define isco88a_lbl 322 `"Modern health associate professionals (except nursing)"', add
label define isco88a_lbl 323 `"Nursing and midwifery associate professionals"', add
label define isco88a_lbl 324 `"Traditional medicine practitioners and faith healers"', add
label define isco88a_lbl 331 `"Primary education teaching associate professionals"', add
label define isco88a_lbl 332 `"Pre-primary education teaching associate professionals"', add
label define isco88a_lbl 333 `"Special education teaching associate professionals"', add
label define isco88a_lbl 334 `"Other teaching associate professionals"', add
label define isco88a_lbl 341 `"Finance and sales associate professionals"', add
label define isco88a_lbl 342 `"Business services agents and trade brokers"', add
label define isco88a_lbl 343 `"Administrative associate professionals"', add
label define isco88a_lbl 344 `"Customs, tax and related government associate professionals"', add
label define isco88a_lbl 345 `"Police inspectors and detectives"', add
label define isco88a_lbl 346 `"Social work associate professionals"', add
label define isco88a_lbl 347 `"Artistic, entertainment and sports associate professionals"', add
label define isco88a_lbl 348 `"Religious associate professionals"', add
label define isco88a_lbl 399 `"Technicians and associate professionals not elsewhere classified"', add
label define isco88a_lbl 411 `"Secretaries and keyboard-operating clerks"', add
label define isco88a_lbl 412 `"Numerical clerks"', add
label define isco88a_lbl 413 `"Material-recording and transport clerks"', add
label define isco88a_lbl 414 `"Library, mail and related clerks"', add
label define isco88a_lbl 419 `"Other office clerks"', add
label define isco88a_lbl 421 `"Cashiers, tellers and related clerks"', add
label define isco88a_lbl 422 `"Client information clerks"', add
label define isco88a_lbl 499 `"Clerks not elsewhere classified"', add
label define isco88a_lbl 511 `"Travel attendants and related workers"', add
label define isco88a_lbl 512 `"Housekeeping and restaurant services workers"', add
label define isco88a_lbl 513 `"Personal care and related workers"', add
label define isco88a_lbl 514 `"Other personal services workers"', add
label define isco88a_lbl 515 `"Astrologers, fortune-tellers and related workers"', add
label define isco88a_lbl 516 `"Protective services workers"', add
label define isco88a_lbl 521 `"Fashion and other models"', add
label define isco88a_lbl 522 `"Shop salespersons and demonstrators"', add
label define isco88a_lbl 523 `"Stall and market salespersons"', add
label define isco88a_lbl 599 `"Service workers and shop and market sales workers not elsewhere classified"', add
label define isco88a_lbl 611 `"Market gardeners and crop growers"', add
label define isco88a_lbl 612 `"Market-oriented animal producers and related workers"', add
label define isco88a_lbl 613 `"Market-oriented crop and animal producers"', add
label define isco88a_lbl 614 `"Forestry and related workers"', add
label define isco88a_lbl 615 `"Fishery workers, hunters and trappers"', add
label define isco88a_lbl 621 `"Subsistence agricultural and fishery workers"', add
label define isco88a_lbl 699 `"Skilled agricultural and fishery workers not elsewhere classified"', add
label define isco88a_lbl 711 `"Miners, shotfirers, stone cutters and carvers"', add
label define isco88a_lbl 712 `"Building frame and related trades workers"', add
label define isco88a_lbl 713 `"Building finishers and related trades workers"', add
label define isco88a_lbl 714 `"Painters, building structure cleaners and related trades workers"', add
label define isco88a_lbl 721 `"Metal moulders, welders, sheet-metal workers, structural- metal preparers, and related trades workers"', add
label define isco88a_lbl 722 `"Blacksmiths, tool-makers and related trades workers"', add
label define isco88a_lbl 723 `"Machinery mechanics and fitters"', add
label define isco88a_lbl 724 `"Electrical and electronic equipment mechanics and fitters"', add
label define isco88a_lbl 731 `"Precision workers in metal and related materials"', add
label define isco88a_lbl 732 `"Potters, glass-makers and related trades workers"', add
label define isco88a_lbl 733 `"Handicraft workers in wood, textile, leather and related materials"', add
label define isco88a_lbl 734 `"Printing and related trades workers"', add
label define isco88a_lbl 741 `"Food processing and related trades workers"', add
label define isco88a_lbl 742 `"Wood treaters, cabinet-makers and related trades workers"', add
label define isco88a_lbl 743 `"Textile, garment and related trades workers"', add
label define isco88a_lbl 744 `"Pelt, leather and shoemaking trades workers"', add
label define isco88a_lbl 799 `"Craft and related trade workers not elsewhere classified"', add
label define isco88a_lbl 811 `"Mining- and mineral-processing-plant operators"', add
label define isco88a_lbl 812 `"Metal-processing-plant operators"', add
label define isco88a_lbl 813 `"Glass, ceramics and related plant operators"', add
label define isco88a_lbl 814 `"Wood-processing- and papermaking-plant operators"', add
label define isco88a_lbl 815 `"Chemical-processing-plant operators"', add
label define isco88a_lbl 816 `"Power-production and related plant operators"', add
label define isco88a_lbl 817 `"Automated-assembly-line and industrial-robot operators"', add
label define isco88a_lbl 821 `"Metal- and mineral-products machine operators"', add
label define isco88a_lbl 822 `"Chemical-products machine operators"', add
label define isco88a_lbl 823 `"Rubber- and plastic-products machine operators"', add
label define isco88a_lbl 824 `"Wood-products machine operators"', add
label define isco88a_lbl 825 `"Printing-, binding- and paper-products machine operators"', add
label define isco88a_lbl 826 `"Textile-, fur- and leather-products machine operators"', add
label define isco88a_lbl 827 `"Food and related products machine operators"', add
label define isco88a_lbl 828 `"Assemblers"', add
label define isco88a_lbl 829 `"Other machine operators and assemblers"', add
label define isco88a_lbl 831 `"Locomotive-engine drivers and related workers"', add
label define isco88a_lbl 832 `"Motor-vehicle drivers"', add
label define isco88a_lbl 833 `"Agricultural and other mobile-plant operators"', add
label define isco88a_lbl 834 `"Ships' deck crews and related workers"', add
label define isco88a_lbl 899 `"Other plant and machine operaters and assemblers not elsewhere classified"', add
label define isco88a_lbl 911 `"Street vendors and related workers"', add
label define isco88a_lbl 912 `"Shoe cleaning and other street services elementary occupations"', add
label define isco88a_lbl 913 `"Domestic and related helpers, cleaners and launderers"', add
label define isco88a_lbl 914 `"Building caretakers, window and related cleaners"', add
label define isco88a_lbl 915 `"Messengers, porters, doorkeepers and related workers"', add
label define isco88a_lbl 916 `"Garbage collectors and related labourers"', add
label define isco88a_lbl 921 `"Agricultural, fishery and related labourers"', add
label define isco88a_lbl 931 `"Mining and construction labourers"', add
label define isco88a_lbl 932 `"Manufacturing labourers"', add
label define isco88a_lbl 933 `"Transport labourers and freight handlers"', add
label define isco88a_lbl 989 `"Elementary occupations not elsewhere classified"', add
label define isco88a_lbl 990 `"Other workers, not elsewhere classified"', add
label define isco88a_lbl 998 `"Unknown"', add
label define isco88a_lbl 999 `"NIU (not in universe)"', add
label values isco88a isco88a_lbl

label define migrate0_lbl 00 `"NIU (not in universe)"'
label define migrate0_lbl 10 `"Same major administrative unit"', add
label define migrate0_lbl 11 `"Same major, same minor administrative unit"', add
label define migrate0_lbl 12 `"Same minor administrative unit, same house"', add
label define migrate0_lbl 13 `"Same minor administrative unit, different house"', add
label define migrate0_lbl 14 `"Same major, different minor administrative unit"', add
label define migrate0_lbl 20 `"Different major administrative unit"', add
label define migrate0_lbl 30 `"Abroad"', add
label define migrate0_lbl 99 `"Unknown/missing"', add
label values migrate0 migrate0_lbl

label define geomig1_10_lbl 368001 `"In residential place of birth [Governorate: Iraq]"'
label define geomig1_10_lbl 368002 `"In current governate [Governorate: Iraq]"', add
label define geomig1_10_lbl 368011 `"Dahuk [Governorate: Iraq]"', add
label define geomig1_10_lbl 368012 `"Nineveh [Governorate: Iraq]"', add
label define geomig1_10_lbl 368013 `"Sulaymaniyah [Governorate: Iraq]"', add
label define geomig1_10_lbl 368014 `"Al-Tameem [Governorate: Iraq]"', add
label define geomig1_10_lbl 368015 `"Erbil [Governorate: Iraq]"', add
label define geomig1_10_lbl 368021 `"Diala [Governorate: Iraq]"', add
label define geomig1_10_lbl 368022 `"Al-Anbar [Governorate: Iraq]"', add
label define geomig1_10_lbl 368023 `"Baghdad [Governorate: Iraq]"', add
label define geomig1_10_lbl 368024 `"Babylon [Governorate: Iraq]"', add
label define geomig1_10_lbl 368025 `"Kerbela [Governorate: Iraq]"', add
label define geomig1_10_lbl 368026 `"Wasit [Governorate: Iraq]"', add
label define geomig1_10_lbl 368027 `"Salah Al-Deen [Governorate: Iraq]"', add
label define geomig1_10_lbl 368028 `"Al-Najaf [Governorate: Iraq]"', add
label define geomig1_10_lbl 368031 `"Al-Qadisiya [Governorate: Iraq]"', add
label define geomig1_10_lbl 368032 `"Al-Muthanna [Governorate: Iraq]"', add
label define geomig1_10_lbl 368033 `"Thi-Qar [Governorate: Iraq]"', add
label define geomig1_10_lbl 368034 `"Maysan [Governorate: Iraq]"', add
label define geomig1_10_lbl 368035 `"Al-Basrah [Governorate: Iraq]"', add
label define geomig1_10_lbl 368097 `"Other countries [Governorate: Iraq]"', add
label define geomig1_10_lbl 368098 `"Unknown [Governorate: Iraq]"', add
label define geomig1_10_lbl 368099 `"NIU (not in universe) [Governorate: Iraq]"', add
label define geomig1_10_lbl 418001 `"Vientiane Capital [Province: Laos]"', add
label define geomig1_10_lbl 418002 `"Phongsaly [Province: Laos]"', add
label define geomig1_10_lbl 418003 `"Luangnamtha [Province: Laos]"', add
label define geomig1_10_lbl 418004 `"Oudomxay [Province: Laos]"', add
label define geomig1_10_lbl 418005 `"Bokeo [Province: Laos]"', add
label define geomig1_10_lbl 418006 `"Luangprabang [Province: Laos]"', add
label define geomig1_10_lbl 418007 `"Huaphanh [Province: Laos]"', add
label define geomig1_10_lbl 418008 `"Xayabury [Province: Laos]"', add
label define geomig1_10_lbl 418009 `"Xiengkhuang [Province: Laos]"', add
label define geomig1_10_lbl 418010 `"Vientiane [Province: Laos]"', add
label define geomig1_10_lbl 418011 `"Borikhamxay [Province: Laos]"', add
label define geomig1_10_lbl 418012 `"Khammuane [Province: Laos]"', add
label define geomig1_10_lbl 418013 `"Savannakhet [Province: Laos]"', add
label define geomig1_10_lbl 418014 `"Saravane [Province: Laos]"', add
label define geomig1_10_lbl 418015 `"Sekong [Province: Laos]"', add
label define geomig1_10_lbl 418016 `"Champasack [Province: Laos]"', add
label define geomig1_10_lbl 418017 `"Attapeu [Province: Laos]"', add
label define geomig1_10_lbl 418018 `"Xaysomboun SR [Province: Laos]"', add
label define geomig1_10_lbl 418097 `"Foreign Country [Province: Laos]"', add
label define geomig1_10_lbl 418098 `"Unknown [Province: Laos]"', add
label define geomig1_10_lbl 418099 `"NIU [Province: Laos]"', add
label define geomig1_10_lbl 608001 `"Abra [Province: Philippines]"', add
label define geomig1_10_lbl 608002 `"Agusan del norte [Province: Philippines]"', add
label define geomig1_10_lbl 608003 `"Agusan del sur [Province: Philippines]"', add
label define geomig1_10_lbl 608004 `"Aklan [Province: Philippines]"', add
label define geomig1_10_lbl 608005 `"Albay [Province: Philippines]"', add
label define geomig1_10_lbl 608006 `"Antique [Province: Philippines]"', add
label define geomig1_10_lbl 608007 `"Basilan, City Of Isabela [Province: Philippines]"', add
label define geomig1_10_lbl 608008 `"Bataan [Province: Philippines]"', add
label define geomig1_10_lbl 608009 `"Batanes [Province: Philippines]"', add
label define geomig1_10_lbl 608010 `"Batangas [Province: Philippines]"', add
label define geomig1_10_lbl 608011 `"Benguet [Province: Philippines]"', add
label define geomig1_10_lbl 608012 `"Bohol [Province: Philippines]"', add
label define geomig1_10_lbl 608013 `"Bukidnon [Province: Philippines]"', add
label define geomig1_10_lbl 608014 `"Bulacan [Province: Philippines]"', add
label define geomig1_10_lbl 608015 `"Cagayan, Batanes [Province: Philippines]"', add
label define geomig1_10_lbl 608016 `"Camarines norte [Province: Philippines]"', add
label define geomig1_10_lbl 608017 `"Camarines Sur [Province: Philippines]"', add
label define geomig1_10_lbl 608018 `"Camiguin [Province: Philippines]"', add
label define geomig1_10_lbl 608019 `"Capiz [Province: Philippines]"', add
label define geomig1_10_lbl 608020 `"Catanduanes [Province: Philippines]"', add
label define geomig1_10_lbl 608021 `"Cavite [Province: Philippines]"', add
label define geomig1_10_lbl 608022 `"Cebu [Province: Philippines]"', add
label define geomig1_10_lbl 608023 `"Davao (Davao del Norte) [Province: Philippines]"', add
label define geomig1_10_lbl 608024 `"Davao del Sur [Province: Philippines]"', add
label define geomig1_10_lbl 608025 `"Davao Oriental [Province: Philippines]"', add
label define geomig1_10_lbl 608026 `"Eastern Samar [Province: Philippines]"', add
label define geomig1_10_lbl 608027 `"Ifugao [Province: Philippines]"', add
label define geomig1_10_lbl 608028 `"Ilocos Norte [Province: Philippines]"', add
label define geomig1_10_lbl 608029 `"Ilocos Sur [Province: Philippines]"', add
label define geomig1_10_lbl 608030 `"Iloilo, Guimaras [Province: Philippines]"', add
label define geomig1_10_lbl 608031 `"Isabela [Province: Philippines]"', add
label define geomig1_10_lbl 608032 `"Kalinga-Apayao, Apayo, Kalinga [Province: Philippines]"', add
label define geomig1_10_lbl 608033 `"La Union [Province: Philippines]"', add
label define geomig1_10_lbl 608034 `"Laguna [Province: Philippines]"', add
label define geomig1_10_lbl 608035 `"Lanao del Norte [Province: Philippines]"', add
label define geomig1_10_lbl 608036 `"Lanao del Sur [Province: Philippines]"', add
label define geomig1_10_lbl 608037 `"Leyte, Biliran [Province: Philippines]"', add
label define geomig1_10_lbl 608038 `"Maguindanao, Cotabato city [Province: Philippines]"', add
label define geomig1_10_lbl 608039 `"Manila [Province: Philippines]"', add
label define geomig1_10_lbl 608040 `"Marinduque [Province: Philippines]"', add
label define geomig1_10_lbl 608041 `"Masbate [Province: Philippines]"', add
label define geomig1_10_lbl 608042 `"Misamis Occidental [Province: Philippines]"', add
label define geomig1_10_lbl 608043 `"Misamis Oriental [Province: Philippines]"', add
label define geomig1_10_lbl 608044 `"Mountain Province [Province: Philippines]"', add
label define geomig1_10_lbl 608045 `"Negros Occidental [Province: Philippines]"', add
label define geomig1_10_lbl 608046 `"Negros Oriental [Province: Philippines]"', add
label define geomig1_10_lbl 608047 `"Cotabato (North Cotabato) [Province: Philippines]"', add
label define geomig1_10_lbl 608048 `"Northern Samar [Province: Philippines]"', add
label define geomig1_10_lbl 608049 `"Nueva Ecija [Province: Philippines]"', add
label define geomig1_10_lbl 608050 `"Nueva Vizcaya [Province: Philippines]"', add
label define geomig1_10_lbl 608051 `"Occidental Mindoro [Province: Philippines]"', add
label define geomig1_10_lbl 608052 `"Oriental Mindoro [Province: Philippines]"', add
label define geomig1_10_lbl 608053 `"Palawan [Province: Philippines]"', add
label define geomig1_10_lbl 608054 `"Pampanga [Province: Philippines]"', add
label define geomig1_10_lbl 608055 `"Pangasinan [Province: Philippines]"', add
label define geomig1_10_lbl 608056 `"Quezon [Province: Philippines]"', add
label define geomig1_10_lbl 608057 `"Quirino [Province: Philippines]"', add
label define geomig1_10_lbl 608058 `"Rizal [Province: Philippines]"', add
label define geomig1_10_lbl 608059 `"Romblon [Province: Philippines]"', add
label define geomig1_10_lbl 608060 `"Samar (Western Samar) [Province: Philippines]"', add
label define geomig1_10_lbl 608061 `"Siquijor [Province: Philippines]"', add
label define geomig1_10_lbl 608062 `"Sorsogon [Province: Philippines]"', add
label define geomig1_10_lbl 608063 `"South Cotabato, Sarangani [Province: Philippines]"', add
label define geomig1_10_lbl 608064 `"Southern Leyte [Province: Philippines]"', add
label define geomig1_10_lbl 608065 `"Sultan Kudarat [Province: Philippines]"', add
label define geomig1_10_lbl 608066 `"Sulu [Province: Philippines]"', add
label define geomig1_10_lbl 608067 `"Surigao Del Norte, Dinagat islands [Province: Philippines]"', add
label define geomig1_10_lbl 608068 `"Surigao del Sur [Province: Philippines]"', add
label define geomig1_10_lbl 608069 `"Tarlac [Province: Philippines]"', add
label define geomig1_10_lbl 608070 `"Tawi-Tawi [Province: Philippines]"', add
label define geomig1_10_lbl 608071 `"Zambales [Province: Philippines]"', add
label define geomig1_10_lbl 608072 `"Zamboanga Norte [Province: Philippines]"', add
label define geomig1_10_lbl 608073 `"Zamboanga del Sur, Zamboanga Sibugay [Province: Philippines]"', add
label define geomig1_10_lbl 608074 `"Manila Metro, 2nd District [Province: Philippines]"', add
label define geomig1_10_lbl 608075 `"Manila Metro, 3rd District [Province: Philippines]"', add
label define geomig1_10_lbl 608076 `"Manila Metro, 4th District [Province: Philippines]"', add
label define geomig1_10_lbl 608077 `"Aurora [Province: Philippines]"', add
label define geomig1_10_lbl 608097 `"Foreign country [Province: Philippines]"', add
label define geomig1_10_lbl 608098 `"Unknown [Province: Philippines]"', add
label define geomig1_10_lbl 608099 `"NIU (not in universe) [Province: Philippines]"', add
label define geomig1_10_lbl 686001 `"Dakar [Region: Senegal]"', add
label define geomig1_10_lbl 686002 `"Ziguinchor [Region: Senegal]"', add
label define geomig1_10_lbl 686003 `"Diourbel [Region: Senegal]"', add
label define geomig1_10_lbl 686004 `"Saint Louis, Louga, Matam [Region: Senegal]"', add
label define geomig1_10_lbl 686005 `"Tambacounda, Kedougou [Region: Senegal]"', add
label define geomig1_10_lbl 686006 `"Kaolack, Fatick, Kaffrine [Region: Senegal]"', add
label define geomig1_10_lbl 686007 `"Thiès [Region: Senegal]"', add
label define geomig1_10_lbl 686010 `"Kolda, Sedhiou [Region: Senegal]"', add
label define geomig1_10_lbl 686097 `"Abroad [Region: Senegal]"', add
label define geomig1_10_lbl 686099 `"NIU (not in universe) [Region: Senegal]"', add
label define geomig1_10_lbl 724011 `"Galicia [Communities & autonomous city: Spain]"', add
label define geomig1_10_lbl 724012 `"Principado de Asturias [Communities & autonomous city: Spain]"', add
label define geomig1_10_lbl 724013 `"Cantabria [Communities & autonomous city: Spain]"', add
label define geomig1_10_lbl 724021 `"País Vasco [Communities & autonomous city: Spain]"', add
label define geomig1_10_lbl 724022 `"Comunidad Foral de Navarra [Communities & autonomous city: Spain]"', add
label define geomig1_10_lbl 724023 `"La Rioja [Communities & autonomous city: Spain]"', add
label define geomig1_10_lbl 724024 `"Aragón [Communities & autonomous city: Spain]"', add
label define geomig1_10_lbl 724030 `"Comunidad de Madrid [Communities & autonomous city: Spain]"', add
label define geomig1_10_lbl 724041 `"Castilla y León [Communities & autonomous city: Spain]"', add
label define geomig1_10_lbl 724042 `"Castilla-La Mancha [Communities & autonomous city: Spain]"', add
label define geomig1_10_lbl 724043 `"Extremadura [Communities & autonomous city: Spain]"', add
label define geomig1_10_lbl 724051 `"Cataluña [Communities & autonomous city: Spain]"', add
label define geomig1_10_lbl 724052 `"Comunidad Valenciana [Communities & autonomous city: Spain]"', add
label define geomig1_10_lbl 724053 `"Illes Balears [Communities & autonomous city: Spain]"', add
label define geomig1_10_lbl 724061 `"Andalucía [Communities & autonomous city: Spain]"', add
label define geomig1_10_lbl 724062 `"Región de Murcia [Communities & autonomous city: Spain]"', add
label define geomig1_10_lbl 724063 `"Ciudad Autónoma de Ceuta [Communities & autonomous city: Spain]"', add
label define geomig1_10_lbl 724064 `"Ciudad Autónoma de Melilla [Communities & autonomous city: Spain]"', add
label define geomig1_10_lbl 724070 `"Canarias [Communities & autonomous city: Spain]"', add
label define geomig1_10_lbl 724090 `"Other Spanish territories in 1970 [Communities & autonomous city: Spain]"', add
label define geomig1_10_lbl 724097 `"Foreign country [Communities & autonomous city: Spain]"', add
label define geomig1_10_lbl 724098 `"Unknown [Communities & autonomous city: Spain]"', add
label define geomig1_10_lbl 724999 `"NIU (not in universe) [Communities & autonomous city: Spain]"', add
label define geomig1_10_lbl 780010 `"Port of Spain [Region: Trinidad and Tobago]"', add
label define geomig1_10_lbl 780020 `"San Fernando [Region: Trinidad and Tobago]"', add
label define geomig1_10_lbl 780080 `"Chaguanas, Sangre Grande, Couva/Tabaquite /Talparo, Rio Claro/Mayaro, Siparia, Penal/Debe, Princess Town, Port Fontin,  Caroni,  St. Andrew/St. David, Victoria, St. Patrick, Arima [Region: Trinidad and Tobago]"', add
label define geomig1_10_lbl 780094 `"St. Paul, St. Mary, St. David, St. George, St. Patrick, St. Andrew, St. John, Tobago [Region: Trinidad and Tobago]"', add
label define geomig1_10_lbl 780098 `"Unknown [Region: Trinidad and Tobago]"', add
label define geomig1_10_lbl 780099 `"NIU (not in universe) [Region: Trinidad and Tobago]"', add
label define geomig1_10_lbl 834001 `"Dodoma [Region: Tanzania]"', add
label define geomig1_10_lbl 834002 `"Arusha, Manyara [Region: Tanzania]"', add
label define geomig1_10_lbl 834003 `"Kilimanjaro [Region: Tanzania]"', add
label define geomig1_10_lbl 834004 `"Tanga [Region: Tanzania]"', add
label define geomig1_10_lbl 834005 `"Morogoro [Region: Tanzania]"', add
label define geomig1_10_lbl 834006 `"Pwani [Region: Tanzania]"', add
label define geomig1_10_lbl 834007 `"Dar es Salaam [Region: Tanzania]"', add
label define geomig1_10_lbl 834008 `"Lindi [Region: Tanzania]"', add
label define geomig1_10_lbl 834009 `"Mtwara [Region: Tanzania]"', add
label define geomig1_10_lbl 834010 `"Ruvuma [Region: Tanzania]"', add
label define geomig1_10_lbl 834011 `"Iringa, Njombe [Region: Tanzania]"', add
label define geomig1_10_lbl 834012 `"Mbeya [Region: Tanzania]"', add
label define geomig1_10_lbl 834013 `"Singida [Region: Tanzania]"', add
label define geomig1_10_lbl 834014 `"Tabora [Region: Tanzania]"', add
label define geomig1_10_lbl 834015 `"Katavi, Rukwa [Region: Tanzania]"', add
label define geomig1_10_lbl 834016 `"Kigoma [Region: Tanzania]"', add
label define geomig1_10_lbl 834019 `"Geita, Kagera, Mwanza, Shinyanga, Simiyu [Region: Tanzania]"', add
label define geomig1_10_lbl 834020 `"Mara [Region: Tanzania]"', add
label define geomig1_10_lbl 834051 `"Zanzibar North [Region: Tanzania]"', add
label define geomig1_10_lbl 834052 `"Zanzibar South [Region: Tanzania]"', add
label define geomig1_10_lbl 834053 `"Zanzibar Town/West [Region: Tanzania]"', add
label define geomig1_10_lbl 834054 `"Pemba North [Region: Tanzania]"', add
label define geomig1_10_lbl 834055 `"Pemba South [Region: Tanzania]"', add
label define geomig1_10_lbl 834097 `"Abroad [Region: Tanzania]"', add
label define geomig1_10_lbl 716000 `"Bulawayo [Province: Zimbabwe]"', add
label define geomig1_10_lbl 716001 `"Manicaland [Province: Zimbabwe]"', add
label define geomig1_10_lbl 716002 `"Mashonaland Central [Province: Zimbabwe]"', add
label define geomig1_10_lbl 716003 `"Mashonaland East [Province: Zimbabwe]"', add
label define geomig1_10_lbl 716004 `"Mashonaland West [Province: Zimbabwe]"', add
label define geomig1_10_lbl 716005 `"Matabeleland North [Province: Zimbabwe]"', add
label define geomig1_10_lbl 716006 `"Matabeleland South [Province: Zimbabwe]"', add
label define geomig1_10_lbl 716007 `"Midlands [Province: Zimbabwe]"', add
label define geomig1_10_lbl 716008 `"Masvingo [Province: Zimbabwe]"', add
label define geomig1_10_lbl 716009 `"Harare [Province: Zimbabwe]"', add
label define geomig1_10_lbl 716097 `"Abroad [Province: Zimbabwe]"', add
label define geomig1_10_lbl 716098 `"Unknown [Province: Zimbabwe]"', add
label define geomig1_10_lbl 716099 `"NIU (Not in universe) [Province: Zimbabwe]"', add
label values geomig1_10 geomig1_10_lbl

label define mig1_10_zw_lbl 716000 `"Bulawayo"'
label define mig1_10_zw_lbl 716001 `"Manicaland"', add
label define mig1_10_zw_lbl 716002 `"Mashonaland Central"', add
label define mig1_10_zw_lbl 716003 `"Mashonaland East"', add
label define mig1_10_zw_lbl 716004 `"Mashonaland West"', add
label define mig1_10_zw_lbl 716005 `"Matabeleland North"', add
label define mig1_10_zw_lbl 716006 `"Matabeleland South"', add
label define mig1_10_zw_lbl 716007 `"Midlands"', add
label define mig1_10_zw_lbl 716008 `"Masvingo"', add
label define mig1_10_zw_lbl 716009 `"Harare"', add
label define mig1_10_zw_lbl 716097 `"Abroad"', add
label define mig1_10_zw_lbl 716098 `"Unknown"', add
label define mig1_10_zw_lbl 716099 `"NIU (Not in universe)"', add
label values mig1_10_zw mig1_10_zw_lbl

label define mig2_10_zw_lbl 716000021 `"Bulawayo Urban"'
label define mig2_10_zw_lbl 716001001 `"Buhera"', add
label define mig2_10_zw_lbl 716001002 `"Chimanimani"', add
label define mig2_10_zw_lbl 716001003 `"Chipinge Rural"', add
label define mig2_10_zw_lbl 716001004 `"Makoni"', add
label define mig2_10_zw_lbl 716001005 `"Mutare Rural"', add
label define mig2_10_zw_lbl 716001006 `"Mutasa"', add
label define mig2_10_zw_lbl 716001007 `"Nyanga"', add
label define mig2_10_zw_lbl 716001021 `"Mutare Urban"', add
label define mig2_10_zw_lbl 716001022 `"Rusape Urban"', add
label define mig2_10_zw_lbl 716001023 `"Chipinge Urban"', add
label define mig2_10_zw_lbl 716002001 `"Bindura Rural"', add
label define mig2_10_zw_lbl 716002002 `"Muzarabani"', add
label define mig2_10_zw_lbl 716002003 `"Guruve"', add
label define mig2_10_zw_lbl 716002004 `"Mazowe, Mvurwi"', add
label define mig2_10_zw_lbl 716002005 `"Mt Darwin"', add
label define mig2_10_zw_lbl 716002006 `"Rushinga"', add
label define mig2_10_zw_lbl 716002007 `"Shamva"', add
label define mig2_10_zw_lbl 716002008 `"Mbire"', add
label define mig2_10_zw_lbl 716002021 `"Bindura Urban"', add
label define mig2_10_zw_lbl 716003001 `"Chikomba"', add
label define mig2_10_zw_lbl 716003002 `"Goromonzi"', add
label define mig2_10_zw_lbl 716003003 `"Hwedza"', add
label define mig2_10_zw_lbl 716003004 `"Marondera Rural"', add
label define mig2_10_zw_lbl 716003005 `"Mudzi"', add
label define mig2_10_zw_lbl 716003006 `"Murehwa"', add
label define mig2_10_zw_lbl 716003007 `"Mutoko"', add
label define mig2_10_zw_lbl 716003008 `"Seke"', add
label define mig2_10_zw_lbl 716003009 `"Uzumba Maramba Pfungwe"', add
label define mig2_10_zw_lbl 716003021 `"Marondera Urban"', add
label define mig2_10_zw_lbl 716004001 `"Chegutu"', add
label define mig2_10_zw_lbl 716004002 `"Hurungwe, Karoi"', add
label define mig2_10_zw_lbl 716004003 `"Mhondoro-Ngezi"', add
label define mig2_10_zw_lbl 716004004 `"Kariba Rural"', add
label define mig2_10_zw_lbl 716004005 `"Makonde"', add
label define mig2_10_zw_lbl 716004006 `"Zvimba"', add
label define mig2_10_zw_lbl 716004007 `"Sanyati"', add
label define mig2_10_zw_lbl 716004021 `"Chinhoyi"', add
label define mig2_10_zw_lbl 716004022 `"Kadoma"', add
label define mig2_10_zw_lbl 716004023 `"Chegutu Urban"', add
label define mig2_10_zw_lbl 716004024 `"Kariba Urban"', add
label define mig2_10_zw_lbl 716004025 `"Norton"', add
label define mig2_10_zw_lbl 716005001 `"Binga"', add
label define mig2_10_zw_lbl 716005002 `"Bubi"', add
label define mig2_10_zw_lbl 716005003 `"Hwange Rural"', add
label define mig2_10_zw_lbl 716005004 `"Lupane"', add
label define mig2_10_zw_lbl 716005005 `"Nkayi"', add
label define mig2_10_zw_lbl 716005006 `"Tsholotsho"', add
label define mig2_10_zw_lbl 716005007 `"Umguza"', add
label define mig2_10_zw_lbl 716005021 `"Hwange Urban"', add
label define mig2_10_zw_lbl 716005022 `"Victoria Falls"', add
label define mig2_10_zw_lbl 716006001 `"Beitbridge Rural"', add
label define mig2_10_zw_lbl 716006002 `"Bulilima"', add
label define mig2_10_zw_lbl 716006003 `"Mangwe Rural, Mangwe Urban (Plumtree)"', add
label define mig2_10_zw_lbl 716006004 `"Gwanda Rural"', add
label define mig2_10_zw_lbl 716006005 `"Insiza"', add
label define mig2_10_zw_lbl 716006006 `"Matobo"', add
label define mig2_10_zw_lbl 716006007 `"Umzingwane"', add
label define mig2_10_zw_lbl 716006021 `"Gwanda Urban"', add
label define mig2_10_zw_lbl 716006022 `"Beitbridge Urban"', add
label define mig2_10_zw_lbl 716007001 `"Chirumanzu"', add
label define mig2_10_zw_lbl 716007002 `"Gokwe North"', add
label define mig2_10_zw_lbl 716007003 `"Gokwe South"', add
label define mig2_10_zw_lbl 716007004 `"Gweru Rural"', add
label define mig2_10_zw_lbl 716007005 `"Kwekwe Rural"', add
label define mig2_10_zw_lbl 716007006 `"Mberengwa"', add
label define mig2_10_zw_lbl 716007007 `"Shurugwi Rural"', add
label define mig2_10_zw_lbl 716007008 `"Zvishavane Rural"', add
label define mig2_10_zw_lbl 716007021 `"Gweru Urban"', add
label define mig2_10_zw_lbl 716007022 `"Kwekwe Urban"', add
label define mig2_10_zw_lbl 716007023 `"Redcliff"', add
label define mig2_10_zw_lbl 716007024 `"Zvishavane Urban"', add
label define mig2_10_zw_lbl 716007026 `"Shurugwi Urban"', add
label define mig2_10_zw_lbl 716008001 `"Bikita"', add
label define mig2_10_zw_lbl 716008002 `"Chiredzi Rural"', add
label define mig2_10_zw_lbl 716008003 `"Chivi"', add
label define mig2_10_zw_lbl 716008004 `"Gutu"', add
label define mig2_10_zw_lbl 716008005 `"Masvingo Rural"', add
label define mig2_10_zw_lbl 716008006 `"Mwenezi"', add
label define mig2_10_zw_lbl 716008007 `"Zaka"', add
label define mig2_10_zw_lbl 716008021 `"Masvingo Urban"', add
label define mig2_10_zw_lbl 716008022 `"Chiredzi Town"', add
label define mig2_10_zw_lbl 716009001 `"Harare Rural"', add
label define mig2_10_zw_lbl 716009021 `"Harare Urban"', add
label define mig2_10_zw_lbl 716009022 `"Chitungwiza"', add
label define mig2_10_zw_lbl 716009023 `"Epworth"', add
label define mig2_10_zw_lbl 716097097 `"Foreign Country"', add
label define mig2_10_zw_lbl 716098098 `"Unknown"', add
label define mig2_10_zw_lbl 716099099 `"NIU (not in universe)"', add
label values mig2_10_zw mig2_10_zw_lbl


save "ipums_5p_raw_2012_sa.dta", replace
