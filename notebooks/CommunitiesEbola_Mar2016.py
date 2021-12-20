'''
Created on 23 Oct 2014
This version, started 13 March 2016


@author: Guy Harling
'''

import networkx as nx
import numpy as np
import matplotlib.pyplot as plt
import math, random, operator, os, csv
import cProfile, pstats, StringIO
from collections import Counter
import time
import cPickle as pickle
import scipy.stats as ss

dateToday       = time.strftime("%d%b%Y")   
# dateToday       = "01Oct2015"


# dataDeposit     = 'C:\Users\Guy\Desktop\Datasets\EbolaTrial' 
# resultsDeposit  = 'C:\Users\Guy\Dropbox\My Research\PostDoc VdG\Adaptive Step Wedge\ResultsWorking'

dataDeposit     = 'C:\Users\GHARLING\Desktop\Datasets\EbolaTrial' 
resultsDeposit  = 'C:\Users\GHARLING\Dropbox\My Research\PostDoc VdG\Adaptive Step Wedge\ResultsWorking'

#===============================================================================
# Profiling code
#===============================================================================

clocks = {}                                        # a (global) dictionary of possible clocks you would like to start and stop
def clock_start(marker, info=False):                     # a profiler that can run any amount of code between "start" and "stop"
    clocks[marker] = [time.clock()]                 # marker is a (required) string that names the clock in your dictionary.
    if info:                                        # info is a logical that will print a file of detailed profile information\n
        clocks[marker].append(cProfile.Profile())    # to your designated os filepath.
        clocks[marker][1].enable()

def clock_stop(marker):                      # sound is a logical that may make a silly sound to let you know when the\n
    total = time.clock() - clocks[marker][0]        # code has finished running.  I find it useful for long runs, but feel free\n
    if len(clocks[marker]) > 1:                     # to turn it off.  In both cases, the total time is printed in the shell\n
        clocks[marker][1].disable()                  # or terminal.
        string = StringIO.StringIO()
        printer = pstats.Stats(clocks[marker][1], stream=string).sort_stats('cumulative')
        printer.print_stats()
        filer = open(marker+".txt", "w")
        filer.write(string.getvalue())
        filer.close()
    print marker + ":"+" " *(10-len(marker)) + str(int(total)/60) + " min, " + str(round(total%60, 2)) + " sec"



#===============================================================================
# Build a network with community structure using a Stochastic Block Model 
#===============================================================================

# Generate community 'k' with 'n' nodes and 'm' total ties, both based on normal distribution draws
def genCommunity(k, n, m, n_sd, m_sd):
    noOfNodes   = int( round( np.random.normal(n, n_sd + 0.00001) ) ) 
    noOfTies    = int( round( np.random.normal(m, m_sd + 0.00001) ) )
    g           = nx.Graph()
    nodes = [ k + "-" + str(x) for x in range(noOfNodes) ]
    g.add_nodes_from(nodes)
    while g.number_of_edges() < noOfTies:
        node_pair = tuple(random.sample(nodes, 2))
        if not node_pair in g.edges():
            g.add_edge(*node_pair)
    return g

# Generate community 'k' with 'n' nodes and 'm' total ties, ties now based on log-normal distribution draws
def genLogNormCommunity(k, n, m, n_sd, m_sd):
    noOfNodes   = int( round( np.random.normal(n, n_sd + 0.00001) ) ) 
    g           = nx.Graph()
    nodes = [ k + "-" + str(x) for x in range(noOfNodes) ]
    g.add_nodes_from(nodes)
    
    commFlag    = 0
    while commFlag     == 0:
        stubDict = { j : int( round( np.random.lognormal(math.log(m), 1))) for j in nodes }
        if np.sum(stubDict.itervalues()) % 2 == 0: commFlag = 1

    stubList = []
    for k, v in stubDict.iteritems():
        for i in range(v):
            stubList.append(k) 
    
    while len(stubList) > 0 or len(set(stubList)) > 1:
        node_pair = tuple(random.sample(stubList, 2))
        if not node_pair in g.edges() and not node_pair[0] == node_pair[1]:
            g.add_edge(*node_pair)
    return g
    
# Merge communities into a single graph
def mergeCommunities(comms):
    # comms: a dictionary of individual community graphs
    G = nx.Graph()
    for g in comms.values():
        G.add_nodes_from(g.nodes())
        G.add_edges_from(g.edges())
    return G

# Generate cross-community ties
def genCrossties(G, g1, g2, noOfTies):
    noOfTies = int(noOfTies)
    g1_nodes = nx.nodes(g1); g2_nodes = nx.nodes(g2)
    g1_picks = []; g2_picks = []
    for t in range(noOfTies):
        g1_picks.append(random.choice(g1_nodes))
        g2_picks.append(random.choice(g2_nodes))
    G.add_edges_from(zip(g1_picks, g2_picks))
    return(noOfTies)

# Generate community structured graph (combined):
def genCommStructure(K, D, n, m, xm, n_sd, m_sd, xm_sd, lognormal=False):
    # K: number of communities; D: number of community crossTie types
    commNames   = [ '0' + str(c+1) if c < 9 else str(c+1) for c in range(K)  ]    
    commGp      = { commNames[k]: int( np.floor( float(k) * D / K ) ) for k in range(K) }  
    inTies      = [ meanMIn[t] * commSize[t] / 2 for t in range(len(m)) ]
    if lognormal == False: 
        comms       = { commNames[k]: genCommunity(commNames[k], n[commGp[commNames[k]]], inTies[commGp[commNames[k]]], 
                                                   n_sd[commGp[commNames[k]]], m_sd[commGp[commNames[k]]]) 
                       for k in range(K) }
    else:
        comms       = { commNames[k]: genLogNormCommunity(commNames[k], n[commGp[commNames[k]]], inTies[commGp[commNames[k]]], 
                                                   n_sd[commGp[commNames[k]]], m_sd[commGp[commNames[k]]]) 
                       for k in range(K) }        
    commSizeEmp = { k: nx.number_of_nodes(comms[k]) for k in commNames }
    propXties   = [ float(xm[t]) / np.sum(xm) for t in range(len(xm)) ]
    commsPerGp  = K / len(m)
    
    commTotTies = {k: int( round( np.random.normal(xm[ commGp[k] ], xm_sd[ commGp[k] ]+0.00001) * commSizeEmp[k] ) ) for k in commNames }
    commTotTies = {k: 0 if v < 0 else v for k, v in commTotTies.iteritems() }
    xtiesOutMat = [ [     ( commTotTies[k] * propXties[ commGp[kk] ] / commsPerGp ) if commGp[k] != commGp[kk]
                     else ( commTotTies[k] * propXties[ commGp[kk] ] / (commsPerGp-1) ) for k in commNames ] for kk in commNames ]  
    G           = mergeCommunities(comms)
    crossTiesDict = {} 
    for i in range(K):
        for j in range(K):
            commNameI = commNames[i]
            commNameJ = commNames[j]
            if i > j: crossTiesDict[(commNameI, commNameJ)] = genCrossties( G, comms[commNameI], comms[commNameJ], xtiesOutMat[i][j] )
    return (G, crossTiesDict, commGp)


# Generate community vaccination times - one method, one round

def commVaccTimes(noOfComms, method, trialStart, trialEnd, gpsPerRound, timeSteps, crtLag=0, xtiesDict={}, outsideWorld=False):
    
    # Remove xties not in trial:
    if outsideWorld == True:
        xtiesDict2 = dict(xtiesDict)
        toDel = [ z for z in xtiesDict2 if int(z[0]) > noOfComms or int(z[1]) > noOfComms ]
        for z in toDel:
            del xtiesDict2[z] 
    else: xtiesDict2 = dict(xtiesDict)
    
    # noOfComms must be divisible by groups per round
    commNames   = [ '0' + str(c+1) if c < 9 else str(c+1) for c in range(noOfComms)  ]
    randomNo    = { c: random.random() for c in commNames }
    randomRnk   = sorted( randomNo.items(), key=operator.itemgetter(1) )
    randomOrder = [ c[0] for c in randomRnk ]

    noOfSteps   = noOfComms / gpsPerRound
    trialLength = trialEnd - trialStart
    stepSize    = trialLength / noOfSteps
    vaccTimes   = [ int(math.ceil(float(x/gpsPerRound))) * stepSize + trialStart for x in range(noOfComms) ]

    topHalf = [ c[0] for c in randomRnk[ : noOfComms/2 ] ]
    btmHalf = [ c[0] for c in randomRnk if c[0] not in topHalf] 

    if method[0] == 'S':
        # 0. One-off list setup
        trialTies = { c: 0 for c in commNames }
        for (i, j), v in xtiesDict2.iteritems():
            trialTies[i] += v; trialTies[j] += v 
        tiesRnk     = sorted( trialTies.items(), key=operator.itemgetter(1), reverse=True )
        tiesOrder   = [ t[0] for t in tiesRnk ] 
        if method == 'SHH':
            nextTwo = [ c for c in tiesOrder[:2] ]
            [ tiesOrder.remove(c) for c in nextTwo ]
            holdOne = [ ]
        
    # 'times' contains: time community becomes vaccinated; its control community; when its control community stops being a control; ranked order of community 
    times   = { c: ('inf', 'inf', 'inf', 'inf' ) for c in commNames }

    # A. Methods that can be fully determined based on ordering at start of trial:     
    if method == 'NON':
        times   = { c: ('inf', 'inf', 'inf', randomOrder.index(c) ) for c in commNames }

    if method == 'CRT':
        topHalf = [ c[0] for c in randomRnk[ : noOfComms/2 ] ]
        btmHalf = [ c[0] for c in randomRnk if c[0] not in topHalf]        
        times   = { c:  (vaccTimes[ randomOrder.index(c) ], 
                         btmHalf[ topHalf.index(c) ], 
                         vaccTimes[ randomOrder.index( btmHalf[ topHalf.index(c) ] ) ] + crtLag, 
                         randomOrder.index(c) ) if c in topHalf 
                   else (vaccTimes[ randomOrder.index(c) ] + crtLag, 'inf', 'inf', 
                         randomOrder.index(c) ) for c in commNames }

    if method == 'SPP':
        for e, t in enumerate(vaccTimes):
            if (noOfComms / 2) > e:    
                topTwo = [ c for c in tiesOrder[:2] ]
                if random.random() > 0.5:   chosen = topTwo[0]; notChosen = topTwo[1]
                else:                       chosen = topTwo[1]; notChosen = topTwo[0]
                tiesOrder.remove(chosen)
                tiesOrder.remove(notChosen)
                times[chosen]    = ( t, notChosen, t + ( int(noOfComms) / 2 * stepSize ) + crtLag, e )       # Can set treatment time for control comm, since already known
                times[notChosen] = ( t + ( int(noOfComms) / 2 * stepSize ) + crtLag, 'inf', 'inf', e + ( int(noOfComms) / 2) )                
            
    # B. Methods that adjust depending on who has been treated:
    if method == 'AFH' or method == 'ASH' or method == 'SSH' or method == 'SFH' or method == 'SHH' or method == 'CSW':
        waitList    = [ c for c in commNames ]
    
        for e, t in enumerate(vaccTimes):
            # 1. Each round list setup for adaptive methods:
            if method[0] == 'A':
                trialTies = { c: 0 for c in waitList }
                for (i, j), v in xtiesDict2.iteritems():
                    if (i in waitList and j in waitList): 
                        trialTies[i] += v; trialTies[j] += v
                tiesRnk     = sorted( trialTies.items(), key=operator.itemgetter(1), reverse=True )
                tiesOrder   = [ a[0] for a in tiesRnk ]
                
            # 2. Choose a treatment community:    
            if (method == 'ASH' or method == 'SSH') and len(waitList) > 1:
                chosen = tiesOrder[0]; notChosen = tiesOrder[1]
                tiesOrder.remove(chosen)
                
            if (method == 'AFH' or method == 'SFH') and len(waitList) > 1:
                topTwo = [ c for c in tiesOrder[:2] ]
                if random.random() > 0.5:   chosen = topTwo[0]; notChosen = topTwo[1]
                else:                       chosen = topTwo[1]; notChosen = topTwo[0]
                if method == 'SFH': tiesOrder.remove(chosen)
                
            if method == 'SHH' and len(waitList) > 2:
                if random.random() > 0.5:   chosen = nextTwo[0]; notChosen = nextTwo[1]
                else:                       chosen = nextTwo[1]; notChosen = nextTwo[0] 
                if len(holdOne) == 0:   
                    nextTwo = [ c for c in tiesOrder[:2] ]
                    [ tiesOrder.remove(c) for c in tiesOrder[:2] ]
                elif len(tiesOrder) > 0:
                    nextTwo = [ holdOne[0], tiesOrder[0] ]
                    tiesOrder.remove(tiesOrder[0])
                else: 
                    nextTwo = [ holdOne[0], holdOne[0] ]
                holdOne = [ notChosen ]

            if method == 'SHH' and len(waitList) == 2:
                chosen = nextTwo[0]; notChosen = 'inf'

            if method == 'CSW' and len(waitList) > 1:
                chosen = randomOrder[0]
                randomOrder.remove(chosen)
                ctrlRandomNo = { c: random.random() for c in randomOrder }
                notChosen = sorted( ctrlRandomNo.items(), key=operator.itemgetter(1) )[0][0]        # Pick a random remaining community as control
               
            # Final round for everyone:
            if len(waitList) == 1:  chosen = waitList[0]; notChosen = 'inf'       

            times[chosen] = ( t, notChosen, 'inf', e )
            times = { k: (v[0], v[1], t, v[3]) if v[1] == chosen else v for k, v in times.iteritems() }     # Update treatment time for control comm as appropriate

            waitList.remove(chosen)

    if method == 'APP':
        waitList    = [ c for c in commNames ]
        for e, t in enumerate(vaccTimes):
            if (noOfComms / 2) > e:
                # 1. Each round list setup for adaptive methods:
                trialTies = { c: 0 for c in waitList }
                for (i, j), v in xtiesDict2.iteritems():
                    if (i in waitList and j in waitList): 
                        trialTies[i] += v; trialTies[j] += v
                tiesRnk     = sorted( trialTies.items(), key=operator.itemgetter(1), reverse=True )
                tiesOrder   = [ a[0] for a in tiesRnk ]
        
                # 2. Choose a treatment community:    
                topTwo = [ c for c in tiesOrder[:2] ]
                if random.random() > 0.5:   chosen = topTwo[0]; notChosen = topTwo[1]
                else:                       chosen = topTwo[1]; notChosen = topTwo[0]  

                times[chosen]    = ( t, notChosen, t + ( int(noOfComms) / 2 * stepSize ) + crtLag, e )       # Can set treatment time for control comm, since already known
                times[notChosen] = ( t + ( int(noOfComms) / 2 * stepSize ) + crtLag, 'inf', 'inf', e + ( int(noOfComms) / 2) )                

                waitList.remove(chosen)
                waitList.remove(notChosen)                
    
    return(times)


#===============================================================================
# Build an SIR model structure
#===============================================================================

def genInfections(Graph, k, n, noOfComms, outsideWorld=False):
    # Generate k initial infectious nodes for n epidemics 
    infected = []
    if outsideWorld == True:
        commNamesT  = [ '0' + str(c+1) if c < 9 else str(c+1) for c in range(noOfComms)  ]
        commNamesNT = [ '0' + str(c+1) if c < 9 else str(c+1) for c in range(noOfComms, noOfComms*2)  ]
        nodesT = [ x for x in Graph.nodes() if x[0:2] in commNamesT ]
        nodesNT = [ x for x in Graph.nodes() if x[0:2] in commNamesNT ]
        for i in range(0, n):
            infected.append(random.sample(nodesT, k))
        for i in range(0, n):
            infected[i].extend(random.sample(nodesNT, k))    
    else: 
        for i in range(0, n):
            infected.append(random.sample(Graph.nodes(), k))
            
    return(infected) 
  
def contagionSetup(Graph, timesteps, Ctype="SI"):
    # Baseline setup for a spreading process
    #     Ctype must contain 'S' and 'I', can contain anything else it likes

    # map node names to values for array use:
    nodeOrdering = { k: i for i, k in enumerate(Graph.nodes()) }
  
    # Set up counters for everything
    # a. Each state has a set of nodes
    statesCurrentNodes = {}
    # b. each state will have has an array of nodes for when the node entered the state 
    statesTimeNodesEnter = {}
    # c. each node has a state value
    nodesCurrentState = dict.fromkeys(Graph)
    nodesCurrentState = {c: 'S' for c in nodesCurrentState} 
    # c1. each node potentially has an infector
    infectorDict = {}
    # d. each state will have a set of epidemic size at each timepoint 
    epiSizeAtT = {}
    # e. each timepoint will have a number of new infection - array of epidemic curve
    epiCurve = np.zeros(timesteps, dtype=int)
    infectiousAtT   = [ set() for t in xrange(timesteps) ]
    removedAtT      = [ set() for t in xrange(timesteps) ]
    # f. Set of states: one value per model state - for iterating over
    epiStates = set()
    for c in range(len(Ctype)):
        state = Ctype[c]
        epiStates.add(state)
        epiSizeAtT[state] = np.zeros(timesteps, dtype=int)
        statesTimeNodesEnter[state] = np.zeros(nx.number_of_nodes(Graph))
        statesTimeNodesEnter[state][:] = np.inf
        statesCurrentNodes[state] = set()
    statesCurrentNodes['S'] = set(Graph.nodes()) 
    return(epiStates, statesCurrentNodes, nodesCurrentState, statesTimeNodesEnter, epiSizeAtT, epiCurve, nodeOrdering, infectorDict, infectiousAtT, removedAtT)

def contagionT0(Graph, epiStates, statesCurrentNodes, nodesCurrentState, statesTimeNodesEnter, epiSizeAtT, I_0, nodeOrdering, noOfComms, outsideWorld, postInfState='E'):
    # Introduce new infections into the population
    #     I(0) is an array of infected nodes
    for nm in I_0:
        m = nodeOrdering[nm]
        nodesCurrentState[nm] = postInfState
        statesCurrentNodes[postInfState].add(nm)
        statesCurrentNodes['S'].remove(nm)
        statesTimeNodesEnter[postInfState][m] = 0
    for state in epiStates:
        # Remove external clusters if present for summary stats:
        if outsideWorld == True:
            stateCurrentNodesT = { n for n in statesCurrentNodes[state] if int(n[0:2]) <= noOfComms }
        else:
            stateCurrentNodesT = statesCurrentNodes[state].copy()
        epiSizeAtT[state][0]    = int(len(stateCurrentNodesT))
        del stateCurrentNodesT
        
    return(statesCurrentNodes, nodesCurrentState, statesTimeNodesEnter, epiSizeAtT)

def moveLoop(indiv_name, indiv_no, from_state, to_state, change_p, fromList, toList, timeList, t, infector=None, infectDict=None):
    # In spreading process: the process of determining if a move occurs
    #    and if so placing the changed node in a 'leaving' pot and an 'arriving' pot  
    if (random.random() < change_p):
        if indiv_name not in fromList[from_state]:       # avoid moving to two different states in one period 
            timeList[to_state][indiv_no] = t 
            toList[to_state].add(indiv_name)       
        fromList[from_state].add(indiv_name) 
        if infector != None: infectDict[indiv_name] = infector

def listLoop(state, fromList, toList, nodeList, stateList):
    # In spreading process: updating nodes at the end of a loop, for one state
    for i in toList:
        nodeList[i] = state
        stateList[state].add(i)
    for i in fromList:
        stateList[state].remove(i) 

def contagionT1(Graph, timesteps, epiStates, transRates, statesCurrentNodes, nodesCurrentState, statesTimeNodesEnter, 
                nodesCommunity, vaccCommunities, epiSizeAtT, epiCurve, nodeOrdering, infectorDict, infectiousAtT, removedAtT, vaccE, noOfComms, outsideWorld):
    # Run the spreading process from t=1 to t=T
    for t in range(0, timesteps):
        # Which communities to vaccinate at this timestep:
        treatmentCommunities = [ c for c, tc in vaccCommunities.iteritems() if t == tc[0] ]
        treatmentNodes       = [ x for x, c in nodesCommunity.iteritems() if c in treatmentCommunities ]
        
        leavers     = {}
        arrivers    = {}
        infStates   = [ x for x in epiStates if x not in ['S', 'E', 'R', 'D', 'V'] ]
        progSteps   = [ x for x in transRates if x[2] not in ['S'] ] 
        if vaccE == False:  vaccStates  = ['S']
        else:               vaccStates  = ['S', 'E']
        infTotal    = len(statesCurrentNodes['E'])
        for s in infStates:
            infTotal += len(statesCurrentNodes[s])
        for state in epiStates:
            leavers[state]  = set()
            arrivers[state] = set()   
  
        # a. Give the infectious a chance to infect
        for s in infStates:
            for i in statesCurrentNodes[s]:
                for j in nx.neighbors(Graph, i):
                    if (nodesCurrentState[j] == 'S'):
                        moveLoop(j, nodeOrdering[j], 'S', 'E', transRates['pS'+s], leavers, arrivers, statesTimeNodesEnter, t, i, infectorDict)

        # b. Give all in infected states a chance to progress
        for p in progSteps:
            if p[1] is not 'S':
                for i in statesCurrentNodes[p[1]]:
                    moveLoop(i, nodeOrdering[i], p[1], p[2], transRates['p' + p[1] + p[2]], leavers, arrivers, statesTimeNodesEnter, t)

        # c. Give chance for vaccination:
        for s in vaccStates:
            for i in statesCurrentNodes[s]:
                if i in treatmentNodes:
                    moveLoop(i, nodeOrdering[i], s, 'V', transRates['pSV'], leavers, arrivers, statesTimeNodesEnter, t)

        # d. Update the lists of nodes in each state:
        for s in epiStates:
            listLoop(s, leavers[s], arrivers[s], nodeList=nodesCurrentState, stateList=statesCurrentNodes)
       
        # d. Update epidemic size at t
        for state in epiStates:
            # Remove external clusters if present for summary stats:
            if outsideWorld == True:
                stateCurrentNodesT = { n for n in statesCurrentNodes[state] if int(n[0:2]) <= noOfComms }
            else:
                stateCurrentNodesT = statesCurrentNodes[state].copy()
            epiSizeAtT[state][t]    = len(stateCurrentNodesT)
            
            if state == 'I':
                if outsideWorld == True:
                    IarriversT = { n for n in arrivers['I'] if int(n[0:2]) <= noOfComms }
                else:
                    IarriversT = arrivers['I'].copy()
                epiCurve[t] = len(IarriversT)
                del IarriversT
            del stateCurrentNodesT
  
        for q in ['I', 'H', 'F']:
            for y in statesCurrentNodes[q]:
                infectiousAtT[t].add(y)
        for q in ['D', 'R']:
            for y in arrivers[q]:
                removedAtT[t].add(y)
        t +=1

    return(nodesCurrentState, statesTimeNodesEnter, epiSizeAtT, epiCurve, epiStates, nodeOrdering, infectorDict, infectiousAtT, removedAtT, Graph) 

def contagionFullRun(Graph, initInf, timesteps, modelType, transRates, timeCommVacc, vaccE, noOfComms, outsideWorld=False):
    base    = contagionSetup(Graph, timesteps, modelType)
    step0   = contagionT0(Graph, epiStates=base[0], statesCurrentNodes=base[1], 
                         nodesCurrentState=base[2], statesTimeNodesEnter=base[3], epiSizeAtT=base[4], I_0=initInf, nodeOrdering=base[6],
                         noOfComms=noOfComms, outsideWorld=outsideWorld)
    commMembership = { k: k[0:2] for k in Graph.nodes() }
    step1 = contagionT1(Graph, timesteps, epiStates=base[0], transRates=transRates, statesCurrentNodes=step0[0], 
                         nodesCurrentState=step0[1], statesTimeNodesEnter=step0[2], nodesCommunity=commMembership, 
                         vaccCommunities=timeCommVacc, epiSizeAtT=step0[3], epiCurve=base[5], nodeOrdering=base[6],
                         infectorDict=base[7], infectiousAtT=base[8], removedAtT=base[9], vaccE=vaccE, noOfComms=noOfComms, outsideWorld=outsideWorld)
    return(step1)

def contagionOneSet(noOfComms, noPerComm, noCommTypes, mWithin, mBtwn, modelType, noInitInf, noOfRuns, timeSteps, crtLag,
                      vaccTypes, vaccE, trialStart, trialEnd, followupEnd, gpsPerRound, 
                      transRates, noPerCommSD=0, mWithinSD=0, mBtwnSD=0, lognormal=False, outsideWorld=False, noisy=False):
    if outsideWorld == True:
        noOfCommsBuild = noOfComms * 2
    else:
        noOfCommsBuild = noOfComms 
    finished = 0
    while finished == 0:
        # Build a network graph for this loop:
        netBuild    = genCommStructure(noOfCommsBuild, noCommTypes, n=noPerComm, m=mWithin, xm=mBtwn, n_sd=noPerCommSD, m_sd=mWithinSD, xm_sd=mBtwnSD)
        G           = netBuild[0]
        xtiesD      = netBuild[1]
        commGp      = netBuild[2]
        initInfSet  = genInfections(G, noInitInf, 1, noOfComms, outsideWorld=outsideWorld)
            
        # Build a set of vaccination times for each method:
        timeCommVaccSet = {}
        if 'non' in vaccTypes: 
            timeCommVaccSet['non'] = commVaccTimes(noOfComms, 'NON', trialStart, trialEnd, gpsPerRound, timeSteps, outsideWorld=outsideWorld)
        if 'crt' in vaccTypes: 
            timeCommVaccSet['crt'] = commVaccTimes(noOfComms, 'CRT', trialStart, trialEnd, gpsPerRound, timeSteps, crtLag, outsideWorld=outsideWorld)
        if 'csw' in vaccTypes: 
            timeCommVaccSet['csw'] = commVaccTimes(noOfComms, 'CSW', trialStart, trialEnd, gpsPerRound, timeSteps, outsideWorld=outsideWorld)        
        if 'asw' in vaccTypes: 
            timeCommVaccSet['asw'] = commVaccTimes(noOfComms, 'ASW', trialStart, trialEnd, gpsPerRound, timeSteps, xtiesDict=xtiesD, outsideWorld=outsideWorld)
        if 'cah' in vaccTypes: 
            timeCommVaccSet['cah'] = commVaccTimes(noOfComms, 'CAH', trialStart, trialEnd, gpsPerRound, timeSteps, xtiesDict=xtiesD, outsideWorld=outsideWorld) 
        if 'spp' in vaccTypes: 
            timeCommVaccSet['spp'] = commVaccTimes(noOfComms, 'SPP', trialStart, trialEnd, gpsPerRound, timeSteps, crtLag, xtiesDict=xtiesD, outsideWorld=outsideWorld) 
        if 'sfh' in vaccTypes: 
            timeCommVaccSet['sfh'] = commVaccTimes(noOfComms, 'SFH', trialStart, trialEnd, gpsPerRound, timeSteps, xtiesDict=xtiesD, outsideWorld=outsideWorld) 
        if 'ssh' in vaccTypes: 
            timeCommVaccSet['ssh'] = commVaccTimes(noOfComms, 'SSH', trialStart, trialEnd, gpsPerRound, timeSteps, xtiesDict=xtiesD, outsideWorld=outsideWorld) 
        if 'afh' in vaccTypes: 
            timeCommVaccSet['afh'] = commVaccTimes(noOfComms, 'AFH', trialStart, trialEnd, gpsPerRound, timeSteps, xtiesDict=xtiesD, outsideWorld=outsideWorld) 
        if 'ash' in vaccTypes: 
            timeCommVaccSet['ash'] = commVaccTimes(noOfComms, 'ASH', trialStart, trialEnd, gpsPerRound, timeSteps, xtiesDict=xtiesD, outsideWorld=outsideWorld) 
        if 'shh' in vaccTypes:
            timeCommVaccSet['shh'] = commVaccTimes(noOfComms, 'SHH', trialStart, trialEnd, gpsPerRound, timeSteps, xtiesDict=xtiesD, outsideWorld=outsideWorld)
        if 'app' in vaccTypes:
            timeCommVaccSet['app'] = commVaccTimes(noOfComms, 'APP', trialStart, trialEnd, gpsPerRound, timeSteps, crtLag, xtiesDict=xtiesD, outsideWorld=outsideWorld)
        
        # Run all the methods for this loop
        resultsOneSet  = {}
    
        ok = 0; failcount = 0
        while ok == 0 and failcount < 5:
            # print ok, failcount
            finalSizes = []
            for v in vaccTypes:
                finalSize    = np.zeros( noOfRuns )
                newCases     = np.zeros([ noOfRuns, timeSteps ])
                stateSizeAtT = list()
                entryTimes   = list()
                
                results      = contagionFullRun(G, initInfSet[0], timeSteps, modelType, transRates, timeCommVacc=timeCommVaccSet[v], vaccE=vaccE, noOfComms=noOfComms, outsideWorld=outsideWorld)

                finalSize    = ( float(results[2]['R'][timeSteps-1]) + results[2]['D'][timeSteps-1] ) / nx.number_of_nodes(G) 
                finalSizes.append(round(finalSize,4))
                nodesCurrentState, entryTimes, stateSizeAtT, newCases, epiStates, nodeOrdering, infectorDict, infectiousAtT, removedAtT, Graph = results
                # Focus only on nodes in Trial
                if outsideWorld == True:
                    trialNetSize = noOfComms*noPerComm[0]
                    # Drop so ignore: 0: nodesCurrentState; 9: Graph
                    # Unchanged so ignore: 4: epiStates
                                        
                    # 1: EntryTimes - remove external nodes
                    intNodesPos = sorted([ (nodeOrdering[x], x) for x in nodeOrdering if int(x[0:2]) <= noOfComms ], key=lambda x: x[0])
                    # intNodesVals = np.zeros(trialNetSize)
                    entryTimes2 = entryTimes.copy()

                    for s in entryTimes:
                        intNodesVals = np.zeros(trialNetSize)
                        for i, t in enumerate(intNodesPos):
                            intNodesVals[i] = entryTimes[s][t[0]]
                        entryTimes2[s] = intNodesVals 
                    
                    # 5: Fix nodeOrdering - remove external nodes, reset ordering 
                    nodeOrdering2 = { t[1]: i for i, t in enumerate(intNodesPos) }

                    # 6: infectorDict (dict) - remove external nodes
                    infectorDict2 = { k: v for k, v in infectorDict.iteritems() if int(v[0:2]) <= noOfComms }
                    
                    # 7. infectiousAtT (set) - remove external nodes    
                    for i in infectiousAtT:
                        for j in i.copy():
                            if int(j[0:2]) > noOfComms:
                                i.remove(j)

                    # 8. removedAtT (set) - remove external nodes                    
                    for i in removedAtT:
                        for j in i.copy():
                            if int(j[0:2]) > noOfComms:
                                i.remove(j)
                                            
                    # Summary stats fixed at each timepoint: 2: stateSizeAtT; 3: newCases 
                    
                    # Fix initInfSet to remove external nodes:
                    initInfSet2 = [ initInfSet[:][i][ 0:len(initInfSet[:][i])/2 ] for i in range(len(initInfSet)) ]
                    
                else:
                    initInfSet2 = initInfSet[:]
                    nodeOrdering2 = nodeOrdering.copy()
                    infectorDict2 = infectorDict.copy()
                    entryTimes2 = entryTimes.copy()
                    
                #    We ignore [0], keep [1]-[3] and [5]-[8] for each loop, and add 4 characteristics of the loop (Graph, xtiesDict, initInfSet, timeCommVaccSet)
                resultsOneSet[v] = [finalSize, newCases, entryTimes2, stateSizeAtT, v, nodeOrdering2, 
                                    infectorDict2, infectiousAtT, removedAtT, [], xtiesD, initInfSet2[0], timeCommVaccSet[v] ]
                    # REF: I have removed the [9] items, previously "Graph"; is now included as part of overall set
                
            # Figure out if Re < 1 at beginning of trial in any vaccine strategy:
            tmpWhoInfectedByWhom    = { v: resultsOneSet[v][6] for v in vaccTypes }  
            infectionsCaused        = { v: Counter( tmpWhoInfectedByWhom[v].values() ) for v in vaccTypes } 
            effectiveR = []
            for v in vaccTypes:
                tmpInfectors = resultsOneSet[v][7] 
                periodTmpInfectors = set()
                for d in range(35, 42):                             # NOTE: This now hardwired to a 6 week burn-in
                    for x in tmpInfectors[d]:
                        periodTmpInfectors.add(x)
                tmpInfections = [ infectionsCaused[v][p] for p in periodTmpInfectors ]
                if len(periodTmpInfectors) == 0:    effectiveR.append( np.inf )
                else:                               effectiveR.append( float(sum(tmpInfections)) / len(periodTmpInfectors) )
            if min(effectiveR) >= 1.0 and np.isfinite(max(effectiveR)) == True : ok = 1; finished = 1         # Re >1, but not 'inf', in week before trial starts
            else: failcount += 1                                                                            # If a difficult network/initial nodes set, try again
#         print finished, ok, failcount
    if ok == 1: 
        # And at the end of all the runs, add descriptions of the type of contagion run and generalities about the graph
        output = [ resultsOneSet, modelType, timeSteps, noOfRuns, epiStates, transRates, commGp, Graph ]
    return( output )


#===============================================================================
# Generate and repackage data
#===============================================================================

def contagionManyRuns(noOfRuns, vaccE, runTitle, noOfComms, noPerComm, noCommTypes, 
                               mWithin, mBtwn, mWithinSD, mBtwnSD,
                               modelType, noInitInf, timeSteps, crtLag, 
                               vaccTypes, trialStart, trialEnd, followupEnd, gpsPerRound,
                               transRates, noPerCommSD, roundLength, lognormal=False, outsideWorld=False):
    for r in range(noOfRuns):
        # print 'starting run #', r
        test = contagionOneSet(noOfComms=noOfComms, noPerComm=noPerComm, noCommTypes=noCommTypes, 
                               mWithin=mWithin, mBtwn=mBtwn,
                               modelType=modelType, noInitInf=noInitInf, noOfRuns=noOfRuns, timeSteps=timeSteps, crtLag=crtLag, 
                               vaccTypes=vaccTypes, vaccE=vaccE, trialStart=trialStart, trialEnd=trialEnd, followupEnd=followupEnd, gpsPerRound=gpsPerRound,
                               transRates=transRates, noPerCommSD=noPerCommSD, mWithinSD=mWithinSD, mBtwnSD=mBtwnSD, lognormal=lognormal, outsideWorld=outsideWorld, 
                               noisy=False)
        os.chdir( dataDeposit)
        runSum = [noOfRuns, noOfComms, noPerComm, noPerCommSD, mWithin, mWithinSD, mBtwn, mBtwnSD, 
                  noInitInf, trialStart, roundLength, gpsPerRound, followupEnd] 
        pickle.dump([test, runSum], open("ebolaRun_" + runTitle + "_" + str(r), "wb"))
        del test, runSum

def resultsOneVaccType(noOfRuns, vaccMethods, runTitle):
    for v in vaccMethods:
        # print 'repackaging vacc ', v
        # Get all runs for one vaccType in a single pickle - 1.1 - 1.4 GB for 1000 runs
        os.chdir( dataDeposit )
        resultsOneVacc = []
        for r in range(noOfRuns):
            singleRun   = pickle.load( open( 'EbolaRun_'+ runTitle + "_"  + str(r), "rb") )[0]
            singleVacc  = singleRun[0][v]
            resultsOneVacc.append(singleVacc) 
            del singleRun, singleVacc
        pickle.dump( resultsOneVacc, open("SingleVacc_"+ runTitle + "_"  + str(v), "wb") )
        del resultsOneVacc
  
#===============================================================================
# clock_start("setup", info=False)
#                
# contagionManyRuns(runsNo, vaccE=False, runTitle='')
# resultsOneVaccType(runsNo, vaccMethods, runTitle='')
#        
# clock_stop("setup")
#===============================================================================


 
#===============================================================================
# Data summarization from after running epidemics
#===============================================================================
  
  
#===============================================================================
# A. Calculate, output and plot of effective reproductive number   
#===============================================================================
  
def infectionsCaused(allVaccTypes, noOfRuns, runTitle):
    # Returns number of infections caused by each person, and an average across the population, for each vaccination method
    infectionsCaused        = {}    
    os.chdir( dataDeposit )
  
    for v in allVaccTypes:
        # print 'step 1'
        tmpVacc                 = pickle.load( open("SingleVacc_" + runTitle + "_" + str(v), "rb") )
        tmpWhoInfectedByWhom    = [ tmpVacc[l][6] for l in range(noOfRuns) ]
        infectionsCaused[v]     = { l : Counter( tmpWhoInfectedByWhom[l].values() ) for l in range(noOfRuns) }
        del tmpVacc
          
    for l in range(noOfRuns):
        # print 'step 2, run ', l
        tmpInfectionsAll        = { v: [ infectionsCaused[v][l][p] for p in infectionsCaused[v][l] ] for v in allVaccTypes } 
        meanInfections          = { v: np.mean(tmpInfectionsAll[v]) for v in allVaccTypes }
  
    return infectionsCaused, meanInfections     
   
def rEffectiveTruth(allVaccTypes, lengthOfTrial, noOfRuns, runTitle='', reMethod='infectious', periodUnit='weeks'):
    # Average number of infections caused by currently infectious people, over their infection:
    #     Replace ReMethod with 'removed' to limit to only those recovering right now
    rEffectivePick = 7
    if reMethod == 'removed':   rEffectivePick = 8
  
    if periodUnit == 'days':    periodicity = 1
    if periodUnit == 'weeks':   periodicity = 7
    noOfPeriods     = int( math.ceil( float(lengthOfTrial) / periodicity ) )
    cutDays         = [ i * periodicity for i in range(noOfPeriods)  ]
     
    tmpInfectors    = []
    for v in sorted(allVaccTypes):
        # print 'step 3'
        os.chdir( dataDeposit )
        tmpVacc             = pickle.load( open("SingleVacc_" + runTitle + "_" + str(v), "rb") )
        tmpInfectors.append( [ tmpVacc[l][rEffectivePick] for l in range(noOfRuns) ] )
        del tmpVacc
     
    rawData = infectionsCaused(sorted(allVaccTypes), noOfRuns, runTitle)
      
    effectiveR = {}             # Dictionary of L loops, dictionary of V vacc methods, list of T timepoints
    for l in range(noOfRuns):
        # print 'Re for run ', l
        effectiveR[l] = {}
        for i, v in enumerate(sorted(allVaccTypes)):
            effectiveR[l][v]    = []
            for p in range(noOfPeriods):
                periodTmpInfectors = set()
                for d in range(cutDays[p], cutDays[p]+periodicity):
                    for x in tmpInfectors[i][l][d]:     
                        periodTmpInfectors.add(x)
                tmpInfections = [ rawData[0][v][l][p] for p in periodTmpInfectors ]
                if len(periodTmpInfectors) == 0:   
                    effectiveR[l][v].append( np.inf )
                else:                           
                    effectiveR[l][v].append( float(sum(tmpInfections)) / len(periodTmpInfectors) )
    effectiveRma = { l: { v: np.ma.masked_array( effectiveR[l][v], ~np.isfinite(effectiveR[l][v]) ) for v in sorted(allVaccTypes) } for  l in range(noOfRuns) }
         
    # Get an average for Re at different timepoints across many runs:
    aveEffectiveR   = { v: np.ma.mean( [ effectiveRma[l][v] for l in range(noOfRuns) ], axis=0) for v in sorted(allVaccTypes) }
    return (aveEffectiveR, effectiveRma)
  
def plotRe(inputData, noOfRuns, allVaccTypes, allVaccNames, saveLoc='', plotTitle=''):
    # Plot R Effective over time for each vaccine strategy
    plt.figure(figsize=(15,9), dpi=300)
    print allVaccTypes
#     colorList = [(0.66796875,0.66796875,0.66796875), (0.37109375,0.6171875,0.81640625), (0.99609375,0.5,0.0546875), (0.5,0.5,0.5), 
#                  (0.34765625,0.34765625,0.34765625), (0,0.41796875,0.640625), (0.99609375,0.734375,0.47265625), 
#                  (0.80859375,0.80859375,0.80859375), (0.78125,0.3203125,0), (0.6328125,0.78125,0.921875),
#                  (0.53515625,0.53515625,0.53515625)]
#     lineList  = [':', '-.', '-.', '-.', '--', '-', '-', '-', '-', '-', '-']
    colorList = [(0.66796875,0.66796875,0.66796875), 
                 (0.34765625,0.34765625,0.34765625), (0,0.41796875,0.640625), (0.99609375,0.734375,0.47265625), 
                 (0.80859375,0.80859375,0.80859375), (0.78125,0.3203125,0), (0.6328125,0.78125,0.921875),
                 (0.53515625,0.53515625,0.53515625)]
    lineList  = [':', '--', '-', '-', '-', '-', '-', '-']
    
    for i, v in enumerate(allVaccTypes):
        plt.plot(inputData[0][v], label=allVaccNames[i], linestyle=lineList[i], color=colorList[i])
        plt.legend()
    plt.xlim(0, 200)
    
    plt.plot([42,42],[0,plt.ylim()[1]], color='black', linestyle='--')
    plt.plot([0,plt.xlim()[1]],[1,1], color='red', linestyle='-')
    
    plt.xlabel('Days since start of outbreak')
    
    plt.rcParams.update({'font.size': 12})
        
    os.chdir( saveLoc )
    plt.savefig('Re_multirun_' + dateToday + '_' + plotTitle + '_' + str(noOfRuns) + '.png')
  
# clock_start("Re", info=False)
#   
# aveEffectiveR = rEffectiveTruth(allVaccTypes=vaccMethods, lengthOfTrial=followupEnd, noOfRuns=runsNo, reMethod='infectious')
# os.chdir( dataDeposit )
# pickle.dump( aveEffectiveR, open("aveRe_" + str(runsNo), "wb") )
# plotRe(inputData=aveEffectiveR, noOfRuns=runsNo, allVaccTypes=vaccMethods, allVaccNames=vaccNames, saveLoc=resultsDeposit)
#   
# clock_stop("Re")       

# # TODO: use this to pull out distribution of Re's at each time point. MAYBE
# os.chdir( dataDeposit )
# aveEffectiveR = pickle.load( open("aveRe_" + str(runsNo), "rb") )
# print aveEffectiveR[0]   

           
#===============================================================================
# B. Producing averages across many runs - epidemic size and entry times for nodes data
#===============================================================================
     
def epiSizeResults(allVaccTypes, allEpiStates, noOfRuns, runTitle):
    # Gives a dictionary of vaccination methods, each with a list of length noOfLoops of dictionaries
    #      entries are states: arrays of mean sizes of states at each timepoint  
    os.chdir( dataDeposit )
    epiSizeSet = {}
    epiSizeAve = {}
    for v in allVaccTypes:
        tmpVacc      = pickle.load( open("SingleVacc_" + runTitle + "_" + str(v), "rb") )
        epiSizeSet   = [ tmpVacc[l][3] for l in range(noOfRuns) ]
        del tmpVacc
        epiSizeAve[v] = { s: [] for s in allEpiStates }
        for s in allEpiStates:
            tmp = [ ( epiSizeSet[l][s].tolist() ) for l in range(noOfRuns) ]
            epiSizeAve[v][s] = [float(sum(col))/len(col) for col in zip(*tmp)]   
    return epiSizeAve                
                          
def entryTimeResults(allVaccTypes, allEpiStates, noOfRuns, noOfNodes, followupEnd):                                    
    # Gives a dictionary of vaccination methods, each with a list of length noOfLoops of dictionaries
    #     entries are states: lists of mean times nodes enter state   
    os.chdir( dataDeposit ) 
    entryTimesSet = {}
    entryTimesAve = {}
    for v in allVaccTypes:
        tmpVacc         = pickle.load( open("SingleVacc_" + str(v), "rb") )
        entryTimesSet   = [ tmpVacc[l][2] for l in range(noOfRuns) ]
        del tmpVacc    
        entryTimesAve[v] = { s: [] for s in allEpiStates }
        for s in allEpiStates:
            tmp     = [ ( entryTimesSet[l][s].tolist() ) for l in range(noOfRuns) ]
            tmpMa   = np.ma.masked_array( tmp, ~np.isfinite(tmp) )
            for n in range(noOfNodes): 
                trial = np.ma.mean( tmpMa[:, n])
                if trial < followupEnd: entryTimesAve[v][s].append( trial )
                else:                   entryTimesAve[v][s].append( np.inf )
        del entryTimesSet
    return entryTimesAve    
     
# clock_start("averages", info=False)
#              
# epiSizeAve      = epiSizeResults(allVaccTypes=vaccMethods, allEpiStates=stateCodes, noOfRuns=runsNo, runTitle=runTitle)
# entryTimesAve   = entryTimeResults(allVaccTypes=vaccMethods, allEpiStates=stateCodes, noOfRuns=runsNo, noOfNodes=nodesNo, runTitle=runTitle)
#     
# clock_stop("averages")
     
#===============================================================================
# C. Plots of state curves - by state
#===============================================================================
        
def contagFigStateNine(aveSizeOfEpi, namesOfStates, lengthOfTrial, vaccTypes, vaccNames, filename='placeholder', saving=True, reduced=False):
    pctsize = np.zeros([len(vaccTypes)+1, lengthOfTrial], dtype=float)
    pctsize[0, :] = aveSizeOfEpi[vaccTypes[0]]['S'][0] + aveSizeOfEpi[vaccTypes[0]]['E'][0] + aveSizeOfEpi[vaccTypes[0]]['I'][0] + aveSizeOfEpi[vaccTypes[0]]['V'][0]
#     colorList = [(0.66796875,0.66796875,0.66796875), (0.37109375,0.6171875,0.81640625), (0.99609375,0.5,0.0546875), (0.5,0.5,0.5),
#                  (0.34765625,0.34765625,0.34765625), (0,0.41796875,0.640625), (0.99609375,0.734375,0.47265625), 
#                  (0.80859375,0.80859375,0.80859375), (0.78125,0.3203125,0), (0.6328125,0.78125,0.921875),
#                  (0.53515625,0.53515625,0.53515625)]
#     lineList  = [':', '-.', '-.', '-.', '--', '-', '-', '-', '-', '-', '-']
    colorList = [(0.66796875,0.66796875,0.66796875), 
                 (0.34765625,0.34765625,0.34765625), (0,0.41796875,0.640625), (0.99609375,0.734375,0.47265625), 
                 (0.80859375,0.80859375,0.80859375), (0.78125,0.3203125,0), (0.6328125,0.78125,0.921875),
                 (0.53515625,0.53515625,0.53515625)]
    lineList  = [':', '--', '-', '-', '-', '-', '-', '-']    
    figOrder  = [331,332,333,334,335,336,337,338]
    if saving==True:    fig = plt.figure(figsize=(15,9), dpi=300)
    else:               fig = plt.figure() 
    fig.subplots_adjust(left=0.1, hspace=0.25)    
    stateCodes = ['S', 'E', 'I', 'H', 'R', 'F', 'D', 'V']
    for i in range(len(figOrder)):    
        state       = stateCodes[i]
        figTitle    = namesOfStates[i]
        ax = fig.add_subplot(figOrder[i])         
        for t in range(lengthOfTrial):   
            for i, v in enumerate(vaccTypes):
                pctsize[i+1, t] = float(aveSizeOfEpi[v][state][t]) / pctsize[0, t] 
        for i, v in enumerate(vaccTypes):
            if reduced == False or (reduced == True and v != 'non'):
                ax.plot(pctsize[i+1], label=vaccNames[i], linestyle=lineList[i], color=colorList[i])
        if state == 'S': ax.set_ylim(0,1)
        ax.set_xlim(0,310)
        ax.set_title(figTitle, y=1.05, size='medium')
        ax.set_xticks(np.arange(0, 310, 100))
        h, l = ax.get_legend_handles_labels()
    fig.legend(h, l, loc=(0.7,0.1), labelspacing=0.5, prop={'size':10})
    fig.text(0.5, 0.01, 'Days since start of outbreak', ha='center', va='center')
    fig.text(0.01, 0.5, 'Proportion of population', ha='center', va='center', rotation='vertical')
    fig.tick_params(axis='both', which='both', top='off', right='off') 
    os.chdir( resultsDeposit )
    if saving==True:    plt.savefig(filename + '.png')
    else:               plt.show()    

def contagFigStateFour(aveSizeOfEpi, namesOfStates, lengthOfTrial, vaccTypes, vaccNames, filename='placeholder', saving=True, reduced=False):
    pctsize = np.zeros([len(vaccTypes)+1, lengthOfTrial], dtype=float)
    pctsize[0, :] = aveSizeOfEpi[vaccTypes[0]]['S'][0] + aveSizeOfEpi[vaccTypes[0]]['E'][0] + aveSizeOfEpi[vaccTypes[0]]['I'][0] + aveSizeOfEpi[vaccTypes[0]]['V'][0]
#     colorList = [(0.66796875,0.66796875,0.66796875), (0.37109375,0.6171875,0.81640625), (0.99609375,0.5,0.0546875), (0.5,0.5,0.5),
#                  (0.34765625,0.34765625,0.34765625), (0,0.41796875,0.640625), (0.99609375,0.734375,0.47265625), 
#                  (0.80859375,0.80859375,0.80859375), (0.78125,0.3203125,0), (0.6328125,0.78125,0.921875),
#                  (0.53515625,0.53515625,0.53515625)]
#     lineList  = [':', '-.', '-.', '-.', '--', '-', '-', '-', '-', '-', '-']
    colorList = [(0.66796875,0.66796875,0.66796875), 
                 (0.34765625,0.34765625,0.34765625), (0,0.41796875,0.640625), (0.99609375,0.734375,0.47265625), 
                 (0.80859375,0.80859375,0.80859375), (0.78125,0.3203125,0), (0.6328125,0.78125,0.921875),
                 (0.53515625,0.53515625,0.53515625)]
    lineList  = [':', '--', '-', '-', '-', '-', '-', '-'] 
    figOrder  = [221,222,223,224]
    if saving==True:    fig = plt.figure(figsize=(15,9), dpi=300)
    else:               fig = plt.figure() 
    stateCodes = ['S', 'I', 'D', 'V'] 
    for i in range(len(figOrder)):    
        state       = stateCodes[i]
        figTitle    = namesOfStates[i]
        ax = fig.add_subplot(figOrder[i])         
        for t in range(lengthOfTrial):   
            for i, v in enumerate(vaccTypes):
                pctsize[i+1, t] = float(aveSizeOfEpi[v][state][t]) / pctsize[0, t] 
        if state == 'S': ax.set_ylim(0,1)
        ax.set_xlim(0,310)
        ax.set_title(figTitle, y=1.05, size='medium')
        ax.set_xticks(np.arange(0, 301, 100))
        for i, v in enumerate(vaccTypes):
            if reduced == False or (reduced == True and v != 'non'):
                ax.plot(pctsize[i+1], label=vaccNames[i], linestyle=lineList[i], color=colorList[i])
                if state == 'S': ax.legend(loc=0, prop={'size': 'x-small'})             
    fig.text(0.5, 0.05, 'Days since start of outbreak', ha='center', va='center')
    fig.text(0.08, 0.5, 'Proportion of population', ha='center', va='center', rotation='vertical')
    fig.tick_params(axis='both', which='both', top='off', right='off') 
    os.chdir( resultsDeposit )
    if saving==True:    plt.savefig(filename + '.png')
    else:               plt.show()  

def contagFigStateSingle(aveSizeOfEpi, namesOfStates, lengthOfTrial, vaccTypes, vaccNames, stateCode, filename='placeholder', saving=True, reduced=False):
    pctsize = np.zeros([len(vaccTypes)+1, lengthOfTrial], dtype=float)
    pctsize[0, :] = aveSizeOfEpi[vaccTypes[0]]['S'][0] + aveSizeOfEpi[vaccTypes[0]]['E'][0] + aveSizeOfEpi[vaccTypes[0]]['I'][0] + aveSizeOfEpi[vaccTypes[0]]['V'][0]
    
    colorList = { 'crt': (0.37109375,0.6171875,0.81640625), 'spp': (0.99609375,0.5,0.0546875), 
                 'app': (0.5,0.5,0.5), 
                 'csw': (0.34765625,0.34765625,0.34765625), 'ssh': (0.6328125,0.78125,0.921875), 'ash': (0.99609375,0.734375,0.47265625), 
                 'sfh': (0.80859375,0.80859375,0.80859375), 'afh': (0.78125,0.3203125,0), 'shh': (0,0.41796875,0.640625) }
    lineList  = { 'crt': '-.', 'spp': '-.', 'app': '-.', 
                 'csw': '--', 'ssh':'-', 'ash': '-', 'sfh': '-', 
                 'afh': '-', 'shh': ':' }
    #===========================================================================
    # colorList = { 'non': (0.66796875,0.66796875,0.66796875), 'crt': (0.37109375,0.6171875,0.81640625), 'spp': (0.99609375,0.5,0.0546875), 
    #              'app': (0.5,0.5,0.5), 
    #              'csw': (0.34765625,0.34765625,0.34765625), 'ssh': (0.6328125,0.78125,0.921875), 'ash': (0.99609375,0.734375,0.47265625), 
    #              'sfh': (0.80859375,0.80859375,0.80859375), 'afh': (0.78125,0.3203125,0), 'shh': (0,0.41796875,0.640625) }
    # lineList  = { 'non': ':', 'crt': '-.', 'spp': '-.', 'app': '-.', 
    #              'csw': '--', 'ssh':'-', 'ash': '-', 'sfh': '-', 
    #              'afh': '-', 'shh': ':' }
    #===========================================================================
    
    if saving==True:    plt.figure(figsize=(8,5), dpi=300)
    else:               plt.figure() 
   
    state       = stateCode[0]
    figTitle    = namesOfStates[0]
    for t in range(lengthOfTrial):   
        for i, v in enumerate(vaccTypes):
            pctsize[i+1, t] = float(aveSizeOfEpi[v][state][t]) / pctsize[0, t] 
    if state == 'S': plt.ylim(0,1)
    plt.xlim(0,310)
    plt.title(figTitle, y=1.05, size='medium')
    plt.xticks(np.arange(0, 301, 100))
    for i, v in enumerate(vaccTypes):
        plt.plot(pctsize[i+1], label=vaccNames[i], linestyle=lineList[v], color=colorList[v], lw=2)
        plt.legend(loc=0, prop={'size': 'x-small'})             
    plt.xlabel('Days since start of outbreak')
    plt.ylabel('Proportion of population')
    plt.tick_params(axis='both', which='both', top='off', right='off') 
    os.chdir( resultsDeposit )
    if saving==True:    plt.savefig(filename + '.png')
    else:               plt.show() 

         
# clock_start("curves", info=False)
#     
# contagFigStateNine(aveSizeOfEpi=epiSizeAve, namesOfStates=stateNames, 
#                   lengthOfTrial=followupEnd, vaccTypes=vaccMethods, vaccNames=vaccNames,
#                   filename='curvesVaccBoxes_' + dateToday + '_' + str(runsNo), saving=True)
#      
# contagFigVaccFour(aveSizeOfEpi=epiSizeAve, namesOfStates=stateNames, 
#                 lengthOfTrial=followupEnd, vaccTypes=vaccMethods, vaccNames=vaccNames,
#                 filename='curvesStateBoxes_' + dateToday + '_' + str(runsNo), saving=True)        
#     
# clock_stop("curves")
     
#===============================================================================
# D. Measure mean incidence rate at the cluster level over time
#===============================================================================
       
def clusterMeanIncidence(vaccType, trialLength, noOfRuns, runTitle, periodUnit='weeks'):
    os.chdir( dataDeposit )
    tmpRun          = pickle.load( open("ebolaRun_baseline_0", "rb") )[0]
    commList        = tmpRun[6].keys()
    del tmpRun     
        
    tmpVacc         = pickle.load( open("SingleVacc_" + runTitle + "_" + str(vaccType), "rb") )
    # list of length l of lists length n giving node order in each run
    nodeOrderList   = [ [ x[0] for x in sorted( tmpVacc[l][5].items(), key=operator.itemgetter(1) ) ] for l in range(noOfRuns) ]    
    # list of length l of dictionaries of length n, each entry containing node name: time of infection for each run
    allInfAll       = [ { nodeOrderList[l][i]: n for i, n in enumerate( tmpVacc[l][2]['I'] ) } for l in range(noOfRuns) ]
    comOrdListAll   = [ tmpVacc[l][12] for l in range(noOfRuns) ] 

    del tmpVacc       
    
    if periodUnit   == 'days': periodicity = 1
    if periodUnit   == 'weeks': periodicity = 7
    noOfPeriods     = int( math.ceil( float(trialLength) / periodicity ) )
    cutDays         = [ i * periodicity for i in range(noOfPeriods)  ]

    periodIncid_c   = [ {} for l in xrange(noOfRuns)]
    periodIncid_vo  = [ {} for l in xrange(noOfRuns)]
    periodSusc_c   = [ {} for l in xrange(noOfRuns)]
    periodInf_c   = [ {} for l in xrange(noOfRuns)]
    
    for l in range(noOfRuns):
        for c in commList:
            vo              = comOrdListAll[l][c][3]            # community order, rather than time
            allInfLoop      = allInfAll[l]
            allInfComm      = { k: v for k, v in allInfLoop.iteritems() if k[:2] == c }
            # Who is susceptible at start of period
            periodSusc      = [ { k: v for k, v in allInfComm.iteritems() if v > d } for d in cutDays ]
            periodSuscNo    = [ len( periodSusc[p] ) for p in range(len(periodSusc)) ]
            # Who infected during period
            periodInfs      = [ { k: v for k, v in allInfComm.iteritems() if (v >= d and v < (d + periodicity) ) } for d in cutDays ]
            periodInfNo     = [ len(periodInfs[p]) for p in range(len(cutDays)) ]
            # Incidence rate per 1000 person-periods at risk
            periodIncid     = [ 0 if periodSuscNo[i] == 0 else round( float(periodInfNo[i]) / periodSuscNo[i] * 1000, 2) for i in range(len(cutDays)) ]
            periodIncid_c[l][c]     = periodIncid               # List of l dictionaries, each containing c items, each with a list of length p periods
            periodIncid_vo[l][vo]   = periodIncid
            periodSusc_c[l][c]      = [ periodSuscNo[i] for i in range(len(cutDays)) ]
            periodInf_c[l][c]       = [ periodInfNo[i] for i in range(len(cutDays)) ]
    del periodIncid, periodInfs, periodInfNo, periodSusc, periodSuscNo, allInfLoop, allInfComm
       
    # Based on community _order_ rather than name, since randomized each round
    allIncidMeans_vo = {}
    for vo in range(len(commList)):
        commIncidMeans_vo = []
        for p in range(noOfPeriods):
            periodIncids_vo = []
            for l in range(noOfRuns):
                periodIncids_vo.append( periodIncid_vo[l][vo][p] )
            commIncidMeans_vo.append( round( np.mean(periodIncids_vo), 2 ) )
        allIncidMeans_vo[vo] = commIncidMeans_vo
    # Based on community name, should be meaningless and average out to all being equal
    allIncidMeans_c = {}
    for c in commList:
        commIncidMeans_c = []
        for p in range(noOfPeriods):
            periodIncids_c = []
            for i in range(noOfRuns):
                periodIncids_c.append( periodIncid_c[i][c][p] )
            commIncidMeans_c.append( round( np.mean(periodIncids_c), 2 ) )
        allIncidMeans_c[c] = commIncidMeans_c      
               
    return (allIncidMeans_vo, allIncidMeans_c, periodIncid_vo, periodIncid_c, periodSusc_c, periodInf_c) 
       
def clusterMeanIncidAll(allVaccTypes, trialLength, noOfRuns, runTitle, periodUnit='weeks'):
    meanIncidOut = { v: clusterMeanIncidence(v, trialLength, noOfRuns, runTitle, periodUnit) for v in allVaccTypes } 
    #===========================================================================
    # for i, x in enumerate(meanIncidOut['non']):
    #     print i, x
    #===========================================================================
    os.chdir( dataDeposit )
    pickle.dump(meanIncidOut, open("clustMeans_" + runTitle + "_" + str(noOfRuns), "wb"))
    del meanIncidOut
       
def clustMeanIncidFig(allIncidMeans, vaccType, vaccTypes, runTitle, fig='', loc='', 
                      periodUnit='weeks', multiFig=False, saving=False, filename='placeholder'):
    # NB allIncidMeans should only cover a single vaccType, only the "vo" version
    vacTitles   = { vaccMethods[i]: vaccNames[i] for i in range(len(vaccTypes)) }

    os.chdir( dataDeposit )
    tmpVacc         = pickle.load( open("SingleVacc_" + runTitle + "_" + str(vaccType), "rb") )
    allTimes        = sorted( [ tmpVacc[0][12][k][0] for k in tmpVacc[0][12] ] )
    uniqTimes       = sorted( list( set(allTimes) ) )
    del tmpVacc

    if vaccType != 'non':   vacColSch   = plt.get_cmap('copper')
    else:                   vacColSch   = plt.get_cmap('copper_r')
    
    vacColList  = [ vacColSch(i) for i in np.linspace(0, 0.9, len(uniqTimes)) ]
    vacColMap   = { t: vacColList[i] for i, t in enumerate(uniqTimes) }
           
    if multiFig == False or fig == '': fig = plt.figure(figsize=(15,9), dpi=300)
    vacTimeSet = set()
    for k, v in allIncidMeans.iteritems():
        vacTime = allTimes[k]  
        if multiFig == False or fig == '': ax = fig.add_subplot(111)
        else: ax = fig.add_subplot(loc[0],loc[1],loc[2])
        ax.plot( v, color=vacColMap[ vacTime ],
                  label = (vacTime if vacTime not in vacTimeSet else "") )
        vacTimeSet.add(vacTime)
    plt.xlim(0, float(310)/7)   
    plt.ylim(0, 120) 
    plt.yticks(np.arange(0,121,40))  
    plt.xticks(np.arange(0,44,10)) 
    plt.tick_params(axis='both', which='both', top='off', right='off') 
    if vaccType == ('csw' or 'sfh'):    plt.title('\n' + vacTitles[vaccType])
    else:                               plt.title(vacTitles[vaccType])
    # Re-order labels        
    handles, labels = ax.get_legend_handles_labels()
    labels = [ 9999 if i == 'inf' else i for i in labels ]
    labels, handles = zip(*sorted(zip(labels, handles), key = lambda t: int(t[0]) ))
    labels = [ 'Never' if i == 9999 else i for i in labels ]   
    if multiFig == False: 
        plt.legend(handles, labels, loc='upper right', title='Vaccination\nTime', prop={'size':8})
    if saving == True:      fig.savefig(filename + runTitle + "_" + '.png')
    elif multiFig == False: fig.show()    
           
def clustMeanIncidFigAll(vaccTypes, noOfRuns, runTitle, periodUnit='weeks', filename='placeholder', saving=True):
    os.chdir( dataDeposit )
    allIncidMeans    = pickle.load( open("clustMeans_" + runTitle + "_" + str(noOfRuns), "rb") )
    os.chdir( resultsDeposit )
    if saving==True:    figSup = plt.figure(figsize=(15,9), dpi=300)
    else:               figSup = plt.figure()
    figSup.subplots_adjust(hspace=0.25)
    for i in range(len(vaccTypes)):
        clustMeanIncidFig(allIncidMeans[vaccTypes[i]][0], vaccTypes[i], vaccTypes, runTitle=runTitle, fig=figSup, loc=(5,2,i+1),
                          periodUnit='weeks', multiFig=True)
    figSup.text(0.5, 0.05, 'Weeks since start of outbreak', ha='center', va='center')
    figSup.text(0.2, 0.5, 'Incidence rate per 1000 person-weeks', ha='center', va='center', rotation='vertical')
    if saving==True:    os.chdir( resultsDeposit ), plt.savefig(filename + '.png')
    else:               plt.show()
          
def clustMeanIncidFigComb(vaccTypes, noOfRuns, runTitle, periodUnit='weeks', filename='placeholder', saving=True):
    os.chdir( dataDeposit )
    allIncidMeans    = pickle.load( open("clustMeans_" + runTitle + "_" + str(noOfRuns), "rb") )
    os.chdir( resultsDeposit )
    if saving==True:    figSup = plt.figure(figsize=(15,9), dpi=300)
    else:               figSup = plt.figure()
    figSup.subplots_adjust(hspace=0.25)
    vacTitles   = { vaccMethods[i]: vaccNames[i] for i in range(len(vaccTypes)) }
    print vacTitles
    #===========================================================================
    # # setup for 
    # levels  = range(0, 55, 1)
    # tmpLoc  = [[0,0],[0,0]]
    # tmpCS   = plt.contourf(tmpLoc, levels, cmap='copper_r')
    # plt.clf()
    #===========================================================================

    levels  = range(0, 37, 1)
    tmpLoc  = [[0,0],[0,0]]
    tmpCS   = plt.contourf(tmpLoc, levels, cmap='copper_r')
    plt.clf()

    for i, vaccType in enumerate(vaccTypes):  
        print vaccType, 'comb'
        loc = (2,3,i+1)
        # loc = (3,3,i+1)
        os.chdir( dataDeposit )
        tmpVacc         = pickle.load( open("SingleVacc_" + runTitle + "_" + str(vaccType), "rb") )[0][12]
        allTimes        = sorted( [ tmpVacc[k][0] for k in tmpVacc ] )
        uniqTimes       = sorted( list( set(allTimes) ) )

        if allTimes[len(allTimes)-1] < 54*7:
            allTimes    = [ 54 if u == 'inf' else u/7 for u in allTimes ]
            uniqTimes   = [ 54 if u == 'inf' else u/7 for u in uniqTimes ]
            vacColSch   = plt.get_cmap('copper_r')
            vacColList  = [ vacColSch(j) for j in np.linspace(0, 1, 37) ]
            # vacColList  = [ vacColSch(j) for j in np.linspace(0, 1, 55) ]
        else:
            allTimes    = [ 84 if u == 'inf' else u/7 for u in allTimes ]
            uniqTimes   = [ 84 if u == 'inf' else u/7 for u in uniqTimes ]            
            vacColSch   = plt.get_cmap('copper_r')
            vacColList  = [ vacColSch(j) for j in np.linspace(0, 1, 85) ]
        vacColMap   = { t: vacColList[t] for t in uniqTimes }
        vacTimeSet  = set()
        
        for k, v in allIncidMeans[vaccTypes[i]][0].iteritems():
            vacTime = allTimes[k]  
            ax      = figSup.add_subplot(loc[0],loc[1],loc[2])
            ax.plot( v, color=vacColMap[ vacTime ],
                               label = (vacTime if vacTime not in vacTimeSet else "") )
            vacTimeSet.add(vacTime)
        plt.xlim(0, float(310)/7)   
        plt.ylim(0, 125) 
        plt.yticks(np.arange(0,126,25))
        plt.tick_params(axis='both', which='both', top='off', right='off') 
        if vaccType == 'shh':   plt.title('Static Rank Fuzzy Order\nHoldback-1')  
        else:                   plt.title(vacTitles[vaccType] + '\n')
        # plt.title(vacTitles[vaccType])
        del tmpVacc
        
    figSup.subplots_adjust(right=0.87)
    cbar_ax = figSup.add_axes([0.90, 0.15, 0.03, 0.7])
    cbar = plt.colorbar(tmpCS, cax=cbar_ax) # using the colorbar from contourf
    cbar.set_label('Week of vaccination', rotation=270, labelpad=10)
    # cbar.set_ticks([0, 6, 16, 26, 36, 54])
    # cbar.set_ticklabels([0, 6, 16, 26, 36, 'Never'])
    cbar.set_ticks([0, 6, 26, 36])
    cbar.set_ticklabels([0, 6, 26, 'Never'])
    figSup.text(0.5, 0.05, 'Weeks since start of outbreak', ha='center', va='center')
    figSup.text(0.07, 0.5, 'Incidence rate per 1000 person-weeks', ha='center', va='center', rotation='vertical')    
    
    if saving==True:    os.chdir( resultsDeposit ), plt.savefig(filename + '.png')
    else:               plt.show()    
    
def clustMeanIncidFigSingle(vaccTypes, noOfRuns, runTitle, periodUnit='weeks', filename='placeholder', saving=True):
    os.chdir( dataDeposit )
    allIncidMeans    = pickle.load( open("clustMeans_" + runTitle + "_" + str(noOfRuns), "rb") )
    os.chdir( resultsDeposit )
    if saving==True:    figSup = plt.figure(figsize=(8,5), dpi=300)
    else:               figSup = plt.figure()
    
    vacTitles   = { vaccMethods[i]: vaccNames[i] for i in range(len(vaccMethods)) }
    
    # setup for 
    tmpLoc  = [[0,0],[0,0]]
    tmpCS   = plt.contourf(tmpLoc, levels, cmap='copper_r')
    plt.clf()

    for i, vaccType in enumerate(vaccTypes):  
        print vaccType, 'singles'
        loc = (1,1,i+1)
        os.chdir( dataDeposit )
        tmpVacc         = pickle.load( open("SingleVacc_" + runTitle + "_" + str(vaccType), "rb") )[0][12]
        allTimes        = sorted( [ tmpVacc[k][0] for k in tmpVacc ] )
        uniqTimes       = sorted( list( set(allTimes) ) )
        
        if allTimes[len(allTimes)-1] < 54*7:
            allTimes    = [ 54 if u == 'inf' else u/7 for u in allTimes ]
            uniqTimes   = [ 54 if u == 'inf' else u/7 for u in uniqTimes ]
            vacColSch   = plt.get_cmap('copper_r')
            vacColList  = [ vacColSch(j) for j in np.linspace(0, 1, 55) ]
        else:
            allTimes    = [ 84 if u == 'inf' else u/7 for u in allTimes ]
            uniqTimes   = [ 84 if u == 'inf' else u/7 for u in uniqTimes ]            
            vacColSch   = plt.get_cmap('copper_r')
            vacColList  = [ vacColSch(j) for j in np.linspace(0, 1, 85) ]
        
        vacColMap   = { t: vacColList[t] for t in uniqTimes }
        vacTimeSet  = set()
        for k, v in allIncidMeans[vaccTypes[i]][0].iteritems():
            vacTime = allTimes[k]  
            ax      = figSup.add_subplot(loc[0],loc[1],loc[2])
            ax.plot( v, color=vacColMap[ vacTime ],
                               label = (vacTime if vacTime not in vacTimeSet else "") )
            vacTimeSet.add(vacTime)
        plt.xlim(0, float(310)/7)   
        plt.ylim(0, 125) 
        plt.yticks(np.arange(0,126,25))        
        plt.title(vacTitles[vaccType])
        plt.tick_params(axis='both', which='both', top='off', right='off') 
        
        del tmpVacc
        
    figSup.subplots_adjust(right=0.87)
    cbar_ax = figSup.add_axes([0.90, 0.15, 0.03, 0.7])
    cbar = plt.colorbar(tmpCS, cax=cbar_ax) # using the colorbar from contourf
    cbar.set_label('Week of vaccination', rotation=270, labelpad=3)
    cbar.set_ticks([0, 6, 16, 26, 36, 54])
    cbar.set_ticklabels([0, 6, 16, 26, 36, 'Never'])
    figSup.text(0.5, 0.03, 'Weeks since start of outbreak', ha='center', va='center')
    figSup.text(0.07, 0.5, 'Incidence rate per 1000 person-weeks', ha='center', va='center', rotation='vertical')
    
    if saving==True:    os.chdir( resultsDeposit ), plt.savefig(filename + '.png')
    else:               plt.show()    

       
#===============================================================================
# clock_start("meanIncid", info=False)
#            
# clusterMeanIncidAll(allVaccTypes=vaccMethods, trialLength=followupEnd, noOfRuns=runsNo, periodUnit='weeks' )
#            
# # clustMeanIncidFigAll(vaccTypes=vaccMethods, noOfRuns=runsNo, periodUnit='weeks', 
# #                      filename='meanIncid_' + dateToday + '_' + str(runsNo), saving=True)
#                    
# clock_stop("meanIncid")
#===============================================================================
               
#===============================================================================
# Outputting results to file  
#===============================================================================

def incidTimesGen(runTitle, allVaccTypes, noOfRuns):
    vaccTypes   = sorted(allVaccTypes)
    incidTimes = {}
    for v in vaccTypes:
        os.chdir( dataDeposit )
        # print 'clustVals vacc type ', v        
        tmpVacc         = pickle.load( open("SingleVacc_" + runTitle + "_" + str(v), "rb") )
        incidTimes[v]   = [ tmpVacc[l][12] for l in range(noOfRuns) ]           # dict of length v, each entry a list of length l
        del tmpVacc
    pickle.dump(incidTimes, open("incidTimes_" + runTitle, "wb"))   
          
def writeOutClustVals(lengthOfTrial, periodicity, noOfRuns, allVaccTypes, path, runTitle, evalWk, cumIncid=False):
    os.chdir( dataDeposit )
    commIncid    = pickle.load( open("clustMeans_" + runTitle + "_" + str(noOfRuns), "rb") )
  
    os.chdir( dataDeposit )
    tmpRun          = pickle.load( open("ebolaRun_baseline_0", "rb") )[0]
    commList        = sorted( tmpRun[6].keys() ) 
    del tmpRun    
    noOfClust   = len(commList)
    noOfPeriods = lengthOfTrial / periodicity
    vaccTypes   = sorted(allVaccTypes)
           
    incidTimes  = pickle.load( open("incidTimes_" + runTitle, "rb") )
    os.chdir( resultsDeposit )      
    outData = [ [0] * (len(vaccTypes)*4 + 2) for i in range(noOfClust * noOfRuns) ]  
    for r in range(noOfRuns):
        baseNo = r * noOfClust 
        for i, c in enumerate(commList):
            outData[baseNo + i][0]  = r+1 
            outData[baseNo + i][1]  = c
            for m, v in enumerate(vaccTypes):
                if incidTimes[v][r][c][0] < 100000: 
                    vp = (incidTimes[v][r][c][0]) / periodicity + evalWk                # Evaluation week (final one for cumulative)
                else: vp = 9999
                # Incidence in treated cluster      
                if vp == 9999: 
                    outData[baseNo + i][2 + m*4]  = 0
                elif cumIncid ==False: 
                    outData[baseNo + i][2 + m*4] = commIncid[v][3][r][c][vp]
                elif evalWk > 0:
                    outData[baseNo + i][2 + m*4] = round(1000.0 * np.sum(commIncid[v][5][r][c][(vp-evalWk+1):(vp+1)]) / np.sum(commIncid[v][4][r][c][(vp-evalWk+1):(vp+1)] ), 2)                        
                else: outData[baseNo + i][2 + m*4] = 0
                # Time of treatment and control cluster ID                                          
                for j, u in enumerate(incidTimes[v][r][c][0:2]):
                    outData[baseNo + i][2 + m*4 + 1 + j] = u     
                # Incidence in control cluster              
                if incidTimes[v][r][c][1] == 'inf':
                    outData[baseNo + i][2 + m*4 + 3] = 9999
                elif cumIncid ==False: 
                    outData[baseNo + i][2 + m*4 + 3] =    commIncid[v][3][r][ incidTimes[v][r][c][1] ][vp]         # Incid from control community
                elif evalWk > 0:
                    outData[baseNo + i][2 + m*4 + 3] = round(1000.0 * np.sum(commIncid[v][5][r][ incidTimes[v][r][c][1] ][(vp-evalWk+1):(vp+1)]) / np.sum(commIncid[v][4][r][ incidTimes[v][r][c][1] ][(vp-evalWk+1):(vp+1)] ), 2)     
                else: outData[baseNo + i][2 + m*4 + 3] = 0
        # print 'writing out run # ' + str(r)
                                        
    os.chdir( resultsDeposit )          
    with open(path, "wb") as csv_file:
        writer = csv.writer(csv_file, delimiter=',')
        writer.writerow(["epidRunNo", "clustNo",   
                         "afh_incid", "afh_vacTime", "afh_ctrlComm", "afh_ctrlIncid", 
#                          "app_incid", "app_vacTime", "app_ctrlComm", "app_ctrlIncid", 
                         "ash_incid", "ash_vacTime", "ash_ctrlComm", "ash_ctrlIncid", 
#                          "crt_incid", "crt_vacTime", "crt_ctrlComm", "crt_ctrlIncid", 
                         "csw_incid", "csw_vacTime", "csw_ctrlComm", "csw_ctrlIncid", 
                         "non_incid", "non_vacTime", "non_ctrlComm", "non_ctrlIncid",
                         "sfh_incid", "sfh_vacTime", "sfh_ctrlComm", "sfh_ctrlIncid", 
                         "shh_incid", "shh_vacTime", "shh_ctrlComm", "shh_ctrlIncid",
#                          "spp_incid", "spp_vacTime", "spp_ctrlComm", "spp_ctrlIncid",
                         "ssh_incid", "ssh_vacTime", "ssh_ctrlComm", "ssh_ctrlIncid"
                         ])
        for line in outData:
            writer.writerow(line)
               
def writeOutPopVals(noOfRuns, allvaccTypes, startVac, aveEffectiveR, runTitle, periodUnit='weeks', path='placeholder'):
    outData     = [ [0] * 5 for i in range(noOfRuns * len(allvaccTypes)) ] 
    if periodUnit == 'weeks': trialStart = startVac / 7 
    else: trialStart = startVac
       
    finalSize   = {}
    removeTime  = {}
    for v in allvaccTypes:
        os.chdir( dataDeposit )
        # print 'popVals vacc type ', v
        tmpVacc         = pickle.load( open("SingleVacc_" + runTitle + "_" + str(v), "rb") )
        finalSize[v]   = [ tmpVacc[l][0] for l in range(noOfRuns) ]           # dict of length v, each entry a list of length l
        removeTime[v]  = [ tmpVacc[l][8] for l in range(noOfRuns) ]

        del tmpVacc  
                
    cumIncid    = { l: { v: finalSize[v][l] for v in allvaccTypes } for l in range(noOfRuns) }
           
    tLastInf    = { l: { v: len(removeTime[v][l]) - 1 - 
                        next(i for i, x in enumerate(reversed(removeTime[v][l])) if len(x) > 0 ) 
                        for v in allvaccTypes } for l in range(noOfRuns) }
           
    aveRe_wlag  = [ [ [ ( aveEffectiveR[1][l][v][i], 0 if i == 0 else aveEffectiveR[1][l][v][i-1] )
                       for i in range(len(aveEffectiveR[1][l][v])) ] for v in allvaccTypes ] for l in range(noOfRuns) ] 
    tToReUnder1 = { l: { v: next((i for i, (x, y) in enumerate(aveRe_wlag[l][vi][trialStart+1:]) if x < 1 and y >= 1), None) 
                        for vi, v in enumerate(allvaccTypes) } for l in range(noOfRuns) }
    tToReUnder1 = { l: { v: tToReUnder1[l][v] if tToReUnder1[l][v] == None else tToReUnder1[l][v] + trialStart
                        for v in allvaccTypes } for l in range(noOfRuns) }   
    
    for r in range(noOfRuns):
        # print 'writing out run ', r
        baseNo  = r * len(allvaccTypes)
        for t in range(len(allvaccTypes)):
            outData[baseNo + t][0]  = r + 1
            outData[baseNo + t][1]  = allvaccTypes[t]
            outData[baseNo + t][2]  = tToReUnder1[r][ allvaccTypes[t] ]
            outData[baseNo + t][3]  = cumIncid[r][ allvaccTypes[t] ]
            outData[baseNo + t][4]  = tLastInf[r][ allvaccTypes[t] ]
               
    os.chdir( resultsDeposit )          
    with open(path, "wb") as csv_file:
        writer = csv.writer(csv_file, delimiter=',')
        writer.writerow( ['Epid Run No', 'vacType', 'Re1Time', 'cumIncid', 'ttLastInf'] )
        for line in outData:
            writer.writerow(line)     
       
#===============================================================================
# clock_start("dataOut", info=False)
#    
# os.chdir( resultsDeposit )          
# for endWk in [0,1,2,3,4,5]:
#     writeOutClustVals(lengthOfTrial=followupEnd, periodicity=roundLength, noOfRuns=runsNo, allVaccTypes=vaccMethods, evalWk=endWk, 
#                   path=os.getcwd() + '/clustValue' + dateToday + '_' + str(runsNo) + '_hb' + str(evalWk) + '.csv')
# # writeOutPopVals(noOfRuns=runsNo, allvaccTypes=vaccMethods, startVac=trialStart, 
# #                 aveEffectiveR=aveEffectiveR, 
# #                 path= os.getcwd() + '/popValue_' + dateToday + '_' + str(runsNo) + '.csv')
#          
# clock_stop("dataOut")
#===============================================================================



#===============================================================================
# # Function for running main, and sensitivity analyses, to get key outputs
#===============================================================================

def FullRun(trialStart, runsNo, vaccE, sdMOut, bSV, rSV, vaccMethods, vaccNames, roundLength, crtLag, lognormal=False, outsideWorld=False,
            build=True, effRe=True, plotEffRe=True, aveStats=True, plotAveStat=True, plotASreduced=True, plotASsingles=True,
            meanIncid=True, plotMeanIncid=True, plotMIsingles=True, dataOut=True, popDataOut=True, 
            runTitle=''):
    # A. Setup
    probVals    = [1, 1, 1, 1, bIH, bIF, bIR, bHF, bHR, 1, bSV]
    rateVals    = [rSI*3/4, rSH*3/4, rSF, rEI, rIH, rIF, rIR, rHF, rHR, rFD, rSV]
    tRatesBase  = { transPaths[i]: probVals[i] * rateVals[i] for i in range(len(transPaths)) } 

    trialEnd    = trialStart + ( roundLength * commNo / gpsPerRound ) 
    followupEnd = trialEnd + 7 * 44 - trialStart

    
    # B. Build networks, run epidemics
    if build == True:
        clock_start("setup", info=False)
        contagionManyRuns(noOfRuns=runsNo, vaccE=vaccE, runTitle=runTitle, noOfComms=commNo, noPerComm=commSize, noCommTypes=len(meanMOut), 
                               mWithin=meanMIn, mBtwn=meanMOut, mWithinSD=sdMIn, mBtwnSD=sdMOut,
                               modelType="SEIHRFDV", noInitInf=initInfects, timeSteps=followupEnd, crtLag=crtLag, 
                               vaccTypes=vaccMethods, trialStart=trialStart, trialEnd=trialEnd, followupEnd=followupEnd, gpsPerRound=gpsPerRound,
                               transRates=tRatesBase, noPerCommSD=sdCommSize, roundLength=roundLength, lognormal=lognormal, outsideWorld=outsideWorld)
        resultsOneVaccType(runsNo, vaccMethods, runTitle=runTitle)
        clock_stop("setup")
    
    # C. Effective Re calculation
    clock_start("Re", info=False)
    if effRe == True:
        aveEffR_wk  = rEffectiveTruth(allVaccTypes=vaccMethods, lengthOfTrial=followupEnd, noOfRuns=runsNo, runTitle=runTitle, reMethod='infectious')
        aveEffR_day = rEffectiveTruth(allVaccTypes=vaccMethods, lengthOfTrial=followupEnd, noOfRuns=runsNo, runTitle=runTitle, reMethod='infectious', periodUnit='days')
        os.chdir( dataDeposit )
        pickle.dump( aveEffR_day, open("aveReDay_" + runTitle + "_" + str(runsNo), "wb") )
        pickle.dump( aveEffR_wk, open("aveReWk_" + runTitle + "_" + str(runsNo), "wb") )
        aveEffR_day = pickle.load(open("aveReDay_" + runTitle + "_" + str(runsNo), "rb") )
        aveEffR_wk  = pickle.load(open("aveReWk_" + runTitle + "_" + str(runsNo), "rb") )
        
        #=======================================================================  TODO: Fix this to skip 'inf's and thus avoid all 'nan's
        # for v in ['crt']: 
        #     meanRePeriods = []
        #     for p in range(followupEnd):
        #         meanRePeriods.append( np.mean([ aveEffR_day[1][l][v][p] for l in aveEffR_day[1] ]) ) 
        #     maxDay          = meanRePeriods.index(np.ma.max([np.ma.masked_array(meanRePeriods, ~np.isfinite(meanRePeriods) )] ) )
        #     SEmaxDay        = np.std([ aveEffR_day[1][l][v][maxDay] for l in aveEffR_day[1] ]) / np.sqrt(float(runsNo))    
        #     print ('Highest Re (day, value, SE) for method' + v)
        #     print (v, maxDay, meanRePeriods[maxDay], SEmaxDay)        
        #=======================================================================
    if plotEffRe == True:
        os.chdir( dataDeposit )
        aveEffR_day = pickle.load( open("aveReDay_" + runTitle + "_" + str(runsNo), "rb") )
        plotRe(inputData=aveEffR_day, noOfRuns=runsNo, allVaccTypes=vaccMethods, allVaccNames=vaccNames, saveLoc=resultsDeposit, plotTitle=runTitle)
        plt.close('all')
    clock_stop("Re")       
    
    # D. Average epidemic sizes and times that nodes enter states
    if aveStats == True:
        clock_start("size and times", info=False)
        epiSizeAve      = epiSizeResults(allVaccTypes=vaccMethods, allEpiStates=stateCodes, noOfRuns=runsNo, runTitle=runTitle)
        # entryTimesAve   = entryTimeResults(allVaccTypes=vaccMethods, allEpiStates=stateCodes, noOfRuns=runsNo, noOfNodes=nodesNo, followupEnd=followupEnd)
        
        pickle.dump( epiSizeAve, open("epiSizeAve_" + runTitle + "_" + str(runsNo), "wb") )
        clock_stop("size and times")
    if plotAveStat == True:
        os.chdir( dataDeposit )
        epiSizeAve = pickle.load( open("epiSizeAve_" + runTitle + "_" + str(runsNo), "rb") )
        # E. Figures of epidemic curves
        clock_start("curves", info=False)
        contagFigStateNine(aveSizeOfEpi=epiSizeAve, namesOfStates=stateNames, 
                          lengthOfTrial=followupEnd, vaccTypes=vaccMethods, vaccNames=vaccNames,
                          filename='curvesVaccBoxes_' + dateToday + '_' + runTitle + '_' + str(runsNo), saving=True)
        if plotASreduced == True:
            contagFigStateFour(aveSizeOfEpi=epiSizeAve, 
                              namesOfStates = ['Susceptible', 'Infectious', 'Deceased', 'Vaccinated'], 
                              vaccTypes=vaccMethods, vaccNames=vaccNames, lengthOfTrial=followupEnd, 
                              filename='curvesVaccBoxesRed_' + dateToday + '_' + runTitle + '_' + str(runsNo), saving=True, reduced=True)  
            plt.close('all')
        if plotASsingles == True:
            for j in stateNames:
                contagFigStateSingle(aveSizeOfEpi=epiSizeAve, namesOfStates = [j], vaccTypes=vaccMethods, vaccNames=vaccNames, stateCode = j[0], lengthOfTrial=followupEnd, 
                                     filename='curvesVaccBoxesSing_' + j + '_' + dateToday + '_' + runTitle + '_' + str(runsNo), 
                                     saving=True, reduced=True)  
            plt.close('all')              
        clock_stop("curves")
    
    # F. Mean incidence rates in commmunities, and Figure
    clock_start("meanIncid", info=False)
    
    if meanIncid == True:
        clusterMeanIncidAll(allVaccTypes=vaccMethods, trialLength=followupEnd, noOfRuns=runsNo, runTitle=runTitle, periodUnit='weeks' )
        os.chdir(dataDeposit)
        allIncidMeans    = pickle.load( open('clustMeans_' + runTitle + '_' + str(runsNo),  "rb"))
        
        for v in vaccMethods:
            weeklyIncid = []; weeklySD = []
            for p in range(len(allIncidMeans['non'][0][0])):
                weeklyIncid.append(np.mean([ allIncidMeans[v][0][c][p] for c in allIncidMeans[v][0] ]))
                weeklySD.append(np.std([ allIncidMeans[v][0][c][p] for c in allIncidMeans[v][0] ]))
            maxIncidWk  = weeklyIncid.index(max(weeklyIncid))
            maxIncid    = weeklyIncid[maxIncidWk]
            maxIncidSE  = weeklySD[maxIncidWk] / np.sqrt(float(runsNo))
            # print ('(week, level, SE) of maximum incidence for method ' + v)
            # print (v, maxIncidWk+1, maxIncid, maxIncidSE)

    if plotMeanIncid == True:
        clustMeanIncidFigComb(vaccTypes=vaccMethods, noOfRuns=runsNo, periodUnit='weeks',
                             filename='meanIncid_' + dateToday + '_' + runTitle + '_' + str(runsNo), saving=True, runTitle=runTitle)
        plt.close('all')
    if plotMIsingles == True:
        for j in vaccMethods:
            clustMeanIncidFigSingle(vaccTypes=[j], noOfRuns=runsNo, periodUnit='weeks',
                                    filename='meanIncid_' + j + dateToday + '_' + runTitle + '_' + str(runsNo), saving=True, runTitle=runTitle)
        plt.close('all')

    clock_stop("meanIncid")
    
    # G. Data outputs: cluster-level and population-level
    
    clock_start("dataOut", info=False)
    if dataOut == True: 
        os.chdir( resultsDeposit )          
        incidTimesGen(runTitle, allVaccTypes=vaccMethods, noOfRuns=runsNo)
        if runTitle == 'baseline':
            for endWk in [-2,-1,0,1,2,3,4,5,6,7,8,9,10]:                
                writeOutClustVals(lengthOfTrial=followupEnd, periodicity=roundLength, noOfRuns=runsNo,            
                                  runTitle=runTitle, allVaccTypes=vaccMethods, evalWk=endWk, cumIncid=False,
                              path=os.getcwd() + '/clustValue' + dateToday + '_' + runTitle + '_' + str(runsNo) + '_wk' + str(endWk) + '.csv')
                if endWk > 0:
                    writeOutClustVals(lengthOfTrial=followupEnd, periodicity=roundLength, noOfRuns=runsNo,
                                      runTitle=runTitle, allVaccTypes=vaccMethods, evalWk=endWk, cumIncid=True,
                                      path=os.getcwd() + '/clustValue' + dateToday + '_' + runTitle + '_' + str(runsNo) + '_wk' + str(endWk) + '_cumul' + '.csv')
        else:
            writeOutClustVals(lengthOfTrial=followupEnd, periodicity=roundLength, noOfRuns=runsNo,            
                                  runTitle=runTitle, allVaccTypes=vaccMethods, evalWk=1, cumIncid=False,
                              path=os.getcwd() + '/clustValue' + dateToday + '_' + runTitle + '_' + str(runsNo) + '_wk' + str(1) + '.csv')                            
    if popDataOut == True:
        os.chdir( dataDeposit )
        aveEffR_day = pickle.load( open("aveReDay_" + runTitle + "_" + str(runsNo), "rb") )
        os.chdir( resultsDeposit )
        writeOutPopVals(noOfRuns=runsNo, allvaccTypes=vaccMethods, startVac=trialStart, 
                        aveEffectiveR=aveEffR_day, runTitle=runTitle, periodUnit='days',
                        path= os.getcwd() + '/popValue_' + dateToday + '_' + runTitle + '_' + str(runsNo) + '.csv')
    clock_stop("dataOut")

#===============================================================================
# Define parameter values for Ebola model: baseline
#===============================================================================

rSI = 0.1      # Daily risk of infection if not hospitalized          # beta_1 (Legrand terminology)   
rSH = 0.05      # Daily risk of infection if hospitalized              # beta_2                        
rSF = 0.1       # Likelihood of infection at funeral                   # beta_3

rEI = float(1) / 9    # Incubation period                                    # alpha - 9 days

rIH = float(1) / 5     # Time to hospitalization                              # gamma_h - 5 days
rIF = float(1) / 10     # Time to death, no hospitalization                    # gamma_d - 10 days
rIR = float(1) / 10    # Time to recovery, no hospitalization                 # gamma_i - 10 days
rHF = float(1) / 5     # Time to death, after hospitalization                 # gamma_dh - 5 days 
rHR = float(1) / 5    # Time to recovery, after hospitalization              # gamma_ih - 5 days

# Probability of death, no hospitalization 
bIF_e = 0.75                                                            # delta_1, raw
bIF = (bIF_e * rIR) / ( bIF_e * rIR + (1-bIF_e) * rIF )                  # delta_1, adjusted for competing risks 
# Probability of death, hospitalization
bHF_e = 0.65                                                           # delta_2, raw
bHF = (bHF_e * rHR) / ( bHF_e * rHR + (1-bHF_e) * rHF )                      # delta_2, adjusted for competing risks

# Probability of recovery, no hospitalization
bIR = (1 - rIH) * (1 - rIF) 
# Probability of recovery, hospitalization
bHR = ( 1 - bHF )

# Probability of being hospitalized
bIH_e = 0.5                                                            # theta, raw rate
bIH = bIH_e * ( rIR * (1-bIF) + rIF * bIF ) / ( bIH_e * ( rIR * (1-bIF) + rIF * bIF ) + (1 - bIH_e) * rIH )         # theta_1, adjusted for competing risks

rFD = 1
bFD = float(1) / 2                                                      # Time to funeral 

# Likelihood of removing someone from population
# bSV = 0.95                                                              # Effectiveness of vaccine on a Susceptible
# rSV = 0.8                                                               # Vaccine coverage level


#===============================================================================
# End of baseline parameter values
#===============================================================================

transPaths = ['pSI', 'pSH', 'pSF', 'pEI', 'pIH', 'pIF', 'pIR', 'pHF', 'pHR', 'pFD', 'pSV']

commNo      = 20
commSize    = [200,200]
sdCommSize  = [0,0]

meanMIn     = [4.5,5]
sdMIn       = [0,0]

meanMOut    = [1,0.5]
# sdMOut      = [0.5,0.5]

#===============================================================================
# vaccMethods = ['non', 'crt', 'spp', 'app', 'csw', 'ssh', 'ash', 'sfh', 'afh', 'shh']
# vaccNames   = ['No Vaccination', 'Standard Parallel', 'Static Rank Parallel', 'Adaptive Rank Parallel', 
#                'Standard Stepped Wedge', 'Static Rank Strict Order', 'Adaptive Rank Strict Order',
#                'Static Rank Fuzzy Order', 'Adaptive Rank Fuzzy Order',  'Static Rank Fuzzy Order Holdback-1']
#===============================================================================

# Reduced set for R1 version of paper
vaccMethods = ['non', 'csw', 'ssh', 'ash', 'sfh', 'afh', 'shh']
vaccNames   = ['No Vaccination',
               'Standard Stepped Wedge', 'Static Rank Strict Order', 'Adaptive Rank Strict Order',
               'Static Rank Fuzzy Order', 'Adaptive Rank Fuzzy Order',  'Static Rank Fuzzy Order Holdback-1']

# Stuff for Victor's presentation: August 2016
vaccMethods = ['non', 'csw', 'ssh', 'sfh', 'afh', 'shh']
vaccNames   = ['No Vaccination',
               'Standard Stepped Wedge', 'Static Rank Strict Order', 
               'Static Rank Fuzzy Order', 'Adaptive Rank Fuzzy Order',  'Static Rank Fuzzy Order Holdback-1']


stateCodes  = ['S', 'E', 'I', 'H', 'R', 'F', 'D', 'V']
stateNames  = ['Susceptible', 'Exposed', 'Infectious', 'Hospitalized', 'Recovered', 'Funeral', 'Deceased', 'Vaccinated']

initInfects = 4
# trialStart  = 7 * 6               # how long running pre trial
# roundLength = 7                     # days per vaccination round
gpsPerRound = 1

nodesNo     = commNo * commSize[0] 

clock_start("Run_1", info=False)
FullRun(trialStart=42, runsNo=1000, vaccE=False, sdMOut=[0.5,0.5], bSV=0.95, rSV=0.8, vaccMethods=vaccMethods, vaccNames=vaccNames, 
        roundLength=7, crtLag=70,
            build=False, effRe=False, plotEffRe=False, aveStats=False, plotAveStat=False, plotASreduced=False, plotASsingles=False,
            meanIncid=False, plotMeanIncid=True, plotMIsingles=False, dataOut=False, popDataOut=False,
            runTitle='baseline')
clock_stop("Run_1")  

#===============================================================================
# clock_start("neg_ctrl", info=False)  
# FullRun(trialStart=42, runsNo=1000, vaccE=False, sdMOut=[0.5,0.5], bSV=0, rSV=0, vaccMethods=vaccMethods, vaccNames=vaccNames, 
#         roundLength=7, crtLag=70,
#             build=True, effRe=True, plotEffRe=True, aveStats=True, plotAveStat=True, plotASreduced=True, plotASsingles=False,
#             meanIncid=True, plotMeanIncid=True, plotMIsingles=False, dataOut=True, popDataOut=True,
#             runTitle='neg_ctrl')
# clock_stop("neg_ctrl")  
#     
# # A. Vaccine quality
#    
# clock_start("vacc_perf", info=False) 
# FullRun(trialStart=42, runsNo=1000, vaccE=False, sdMOut=[0.5,0.5], bSV=1, rSV=1, vaccMethods=vaccMethods, vaccNames=vaccNames, 
#         roundLength=7, crtLag=70,
#             build=True, effRe=True, plotEffRe=True, aveStats=True, plotAveStat=True, plotASreduced=True, plotASsingles=False,
#             meanIncid=True, plotMeanIncid=True, plotMIsingles=False, dataOut=True, popDataOut=True,
#             runTitle='vacc_perf')
# clock_stop("vacc_perf")  
# clock_start("vacc_poor", info=False)       
# FullRun(trialStart=42, runsNo=1000, vaccE=False, sdMOut=[0.5,0.5], bSV=0.7, rSV=0.7, vaccMethods=vaccMethods, vaccNames=vaccNames, 
#         roundLength=7, crtLag=70,
#             build=True, effRe=True, plotEffRe=True, aveStats=True, plotAveStat=True, plotASreduced=True, plotASsingles=False,
#             meanIncid=True, plotMeanIncid=True, plotMIsingles=False, dataOut=True, popDataOut=True,
#             runTitle='vacc_poor')
# clock_stop("vacc_poor")  
# clock_start("vaccE", info=False)       
# FullRun(trialStart=42, runsNo=1000, vaccE=True, sdMOut=[0.5,0.5], bSV=0.95, rSV=0.8, vaccMethods=vaccMethods, vaccNames=vaccNames, 
#         roundLength=7, crtLag=70,
#             build=True, effRe=True, plotEffRe=True, aveStats=True, plotAveStat=True, plotASreduced=True, plotASsingles=False,
#             meanIncid=True, plotMeanIncid=True, plotMIsingles=False, dataOut=True, popDataOut=True,
#             runTitle='vaccE')
# clock_stop("vaccE")  
#     
# # B. Vaccine timing
#    
# clock_start("week8", info=False) 
# FullRun(trialStart=56, runsNo=1000, vaccE=False, sdMOut=[0.5,0.5], bSV=0.95, rSV=0.8, vaccMethods=vaccMethods, vaccNames=vaccNames, 
#         roundLength=7, crtLag=70,
#             build=True, effRe=True, plotEffRe=True, aveStats=True, plotAveStat=True, plotASreduced=True, plotASsingles=False,
#             meanIncid=True, plotMeanIncid=True, plotMIsingles=False, dataOut=True, popDataOut=True,
#             runTitle='week8')
# clock_stop("week8")  
# clock_start("week10", info=False)          
# FullRun(trialStart=70, runsNo=1000, vaccE=False, sdMOut=[0.5,0.5], bSV=0.95, rSV=0.8, vaccMethods=vaccMethods, vaccNames=vaccNames,
#         roundLength=7, crtLag=70,
#             build=True, effRe=True, plotEffRe=True, aveStats=True, plotAveStat=True, plotASreduced=True, plotASsingles=False,
#             meanIncid=True, plotMeanIncid=True, plotMIsingles=False, dataOut=True, popDataOut=True,
#             runTitle='week10')
# clock_stop("week10")  
#              
# # C. Btwn-community heterogeneity 
#    
# clock_start("hetg_lo", info=False) 
# FullRun(trialStart=42, runsNo=1000, vaccE=False, sdMOut=[0.25,0.25], bSV=0.95, rSV=0.8, vaccMethods=vaccMethods, vaccNames=vaccNames, 
#         roundLength=7, crtLag=70,
#             build=True, effRe=True, plotEffRe=True, aveStats=True, plotAveStat=True, plotASreduced=True, plotASsingles=False,
#             meanIncid=True, plotMeanIncid=True, plotMIsingles=False, dataOut=True, popDataOut=True,
#             runTitle='hetg_lo')
# clock_stop("hetg_lo")  
# clock_start("hetg_hi", info=False)          
# FullRun(trialStart=42, runsNo=1000, vaccE=False, sdMOut=[0.75,0.75], bSV=0.95, rSV=0.8, vaccMethods=vaccMethods, vaccNames=vaccNames, 
#         roundLength=7, crtLag=70,
#             build=True, effRe=True, plotEffRe=True, aveStats=True, plotAveStat=True, plotASreduced=True, plotASsingles=False,
#             meanIncid=True, plotMeanIncid=True, plotMIsingles=False, dataOut=True, popDataOut=True,
#             runTitle='hetg_hi')
# clock_stop("hetg_hi")          
#    
# # D. Within-community heterogeneity 
#    
# clock_start("lognorm", info=False) 
#    
# FullRun(trialStart=42, runsNo=1000, vaccE=False, sdMOut=[0.5,0.5], bSV=0.95, rSV=0.8, lognormal=True, vaccMethods=vaccMethods, vaccNames=vaccNames, 
#         roundLength=7, crtLag=70,
#             build=True, effRe=True, plotEffRe=True, aveStats=True, plotAveStat=True, plotASreduced=True, plotASsingles=False,
#             meanIncid=True, plotMeanIncid=True, plotMIsingles=False, dataOut=True, popDataOut=True,
#             runTitle='lognorm')
# clock_stop("lognorm")        
# 
# ### Revision sensitivity analyses    ###
#  
# # E. Longer timesteps (reviewer commetn 2A6)
#     
# clock_start("Round14", info=False)
# FullRun(trialStart=42, runsNo=1000, vaccE=False, sdMOut=[0.5,0.5], bSV=0.95, rSV=0.8, vaccMethods=vaccMethods, vaccNames=vaccNames, 
#         roundLength=14, crtLag=70,
#             build=True, effRe=True, plotEffRe=True, aveStats=True, plotAveStat=True, plotASreduced=True, plotASsingles=False,
#             meanIncid=True, plotMeanIncid=True, plotMIsingles=False, dataOut=True, popDataOut=True,
#             runTitle='Round14')
# clock_stop("Round14") 
#      
# clock_start("Round21", info=False)
# FullRun(trialStart=42, runsNo=1000, vaccE=False, sdMOut=[0.5,0.5], bSV=0.95, rSV=0.8, vaccMethods=vaccMethods, vaccNames=vaccNames, 
#         roundLength=21, crtLag=70,
#             build=True, effRe=True, plotEffRe=True, aveStats=True, plotAveStat=True, plotASreduced=True, plotASsingles=False,
#             meanIncid=True, plotMeanIncid=True, plotMIsingles=False, dataOut=True, popDataOut=True,
#             runTitle='Round21')
# clock_stop("Round21") 
#   
# # F. Shorter CRT pause (reviewer comment 1A2)
#    
# clock_start("crtLag35", info=False)
# FullRun(trialStart=42, runsNo=1000, vaccE=False, sdMOut=[0.5,0.5], bSV=0.95, rSV=0.8, vaccMethods=vaccMethods, vaccNames=vaccNames, 
#         roundLength=7, crtLag=35,
#             build=True, effRe=True, plotEffRe=True, aveStats=True, plotAveStat=True, plotASreduced=True, plotASsingles=False,
#             meanIncid=True, plotMeanIncid=True, plotMIsingles=False, dataOut=True, popDataOut=True,
#             runTitle='crtLag35')
# clock_stop("crtLag35") 
#    
# clock_start("crtLag0", info=False)
# FullRun(trialStart=42, runsNo=1000, vaccE=False, sdMOut=[0.5,0.5], bSV=0.95, rSV=0.8, vaccMethods=vaccMethods, vaccNames=vaccNames, 
#         roundLength=7, crtLag=0,
#             build=True, effRe=True, plotEffRe=True, aveStats=True, plotAveStat=True, plotASreduced=True, plotASsingles=False,
#             meanIncid=True, plotMeanIncid=True, plotMIsingles=False, dataOut=True, popDataOut=True,
#             runTitle='crtLag0')
# clock_stop("crtLag0") 
#  
# #===============================================================================
# # # These values give CumIncid of ~10%; initial Re of ~1.4
# # rSI     = 0.05; rSH     = 0.025; rSF     = 0.05
# # # These values give CumIncid of ~63%; initial Re of ~1.7
# # rSI     = 0.075; rSH     = 0.0375; rSF     = 0.075
# #===============================================================================
#  
# # G. Lower infectiousnes (reviewer comment XX)
#   
# # These values give CumIncid of ~36%; initial Re of ~1.5
# rSI     = 0.06; rSH     = 0.03; rSF     = 0.06
#   
# clock_start("Run_lowRe", info=False)
# FullRun(trialStart=42, runsNo=1000, vaccE=False, sdMOut=[0.5,0.5], bSV=0.95, rSV=0.8, vaccMethods=vaccMethods, vaccNames=vaccNames, 
#         roundLength=7, crtLag=70,
#             build=True, effRe=True, plotEffRe=True, aveStats=True, plotAveStat=True, plotASreduced=True, plotASsingles=False,
#             meanIncid=True, plotMeanIncid=True, plotMIsingles=False, dataOut=True, popDataOut=True,
#             runTitle='Run_lowRe')   
# clock_stop("Run_lowRe") 
# rSI     = 0.1; rSH     = 0.05; rSF     = 0.1        # Reset values
# 
# # H. Allow for outside infections (reviewer comment XY)
#   
# clock_start("outWrld", info=False)
# FullRun(trialStart=42, runsNo=1000, vaccE=False, sdMOut=[0.5,0.5], bSV=0.95, rSV=0.8, vaccMethods=vaccMethods, vaccNames=vaccNames, 
#         roundLength=7, crtLag=70, outsideWorld=True,
#             build=False, effRe=False, plotEffRe=True, aveStats=False, plotAveStat=True, plotASreduced=True, plotASsingles=False,
#             meanIncid=False, plotMeanIncid=True, plotMIsingles=False, dataOut=False, popDataOut=False,
#             runTitle='outWrld')
# clock_stop("outWrld")    
#===============================================================================
             
            

            
            
            
            
            
            
            
            
            
            
            
            
            
            
#===============================================================================
# test object:     ( resultsOneSet, modelType, timeSteps, noOfRuns, epiStates, transRates, commGp, Graph)
# where resultsOneSet contains dictionaries with v entries of: 
#                 [ finalSize,    newCases, entryTimes, stateSizeAtT, v, nodeOrdering, 
#                                 infectorDict, infectiousAtT, removedAtT, [], xtiesDict, 
#                                 initInfSet[0], timeCommVaccSet[v] ]
# resultsOneSet object of form: [Wrapper: 0-7][VaccType: v1-v][Results: 0-12]
#===============================================================================
       
#===============================================================================
# - writeOutIndivVals last saved in Mar2015 file
# - Code for figures for Jan2015 poster last saved in the Mar2015 file
# - Additional unused code was last saved in the Mar2015 file
#===============================================================================
       
# #===============================================================================
# # Range of number of connections between communities
# #===============================================================================
#       
# x, y = ( [] for i in range(2) )
# for vt in test[0][0]:
#     for k, v in test[0][0][vt][10].iteritems():
#         for i in range(v):
#             x.append(int(float(k[0])))
#             y.append(int(float(k[1])))       
# plt.hist2d(x, y, bins=commNo, cmap='Greens' )
# plt.colorbar()
# plt.xlim(1,commNo+1)
# plt.ylim(1,commNo+1)
# plt.savefig('mixingMatrix_' + dateToday + '_' + str(test[3]) + '.png')  
