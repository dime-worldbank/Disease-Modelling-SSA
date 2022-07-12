/*Purpose: Examine the spatially disaggregated model outputs vis-a-vis the mobility patterns seen across Zimbabwe from the original OD matrix.

We want to see to what extent the infection and deaths that the model predicted according to a beta of (.3?) reflected where in the country saw the highest share of inbound and outbound movement

Note: it is i5 data that is used in the model
*/

cd "/Users/sophieayling/Documents/GitHub/Disease-Modelling-SSA/data"

tempfile temp1


import excel using "preprocessed/mobility/New Files/Most mobile districts i5.xlsx", firstrow clear
