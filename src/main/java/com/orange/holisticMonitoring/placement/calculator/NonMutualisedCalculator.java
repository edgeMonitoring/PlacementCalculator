package com.orange.holisticMonitoring.placement.calculator;

import com.esotericsoftware.yamlbeans.YamlException;
import com.orange.holisticMonitoring.placement.constraint.PropXinSReif;
import com.orange.holisticMonitoring.placement.inputs.Function;
import com.orange.holisticMonitoring.placement.inputs.G_infrastructure;
import com.orange.holisticMonitoring.placement.inputs.G_usersRequirements;
import com.orange.holisticMonitoring.placement.inputs.PathsBetweenTwoVertices;

import org.chocosolver.graphsolver.GraphModel;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.Settings;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.search.strategy.strategy.StrategiesSequencer;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.SetVar;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

public class NonMutualisedCalculator {

    static int headId(int edgeID, int vertexNumber) {
        if (edgeID != 0)
            return edgeID % vertexNumber;
        else
            return 0;
    }

    static int tailId(int edgeID, int vertexNumber) {
        if (edgeID != 0)
            return edgeID / vertexNumber;
        else
            return 0;
    }

    static int maxLinkCapability = 10000;

    public static CalculationResult calculate(String infrastructureId,String usersRequirementsId) throws YamlException, FileNotFoundException {
    	//Model model = new Model("Monitoring service placement");
        GraphModel model = new GraphModel();
        model.set(new Settings() {
            public boolean checkDeclaredConstraints() {
                return false;
            }

            @Override
            public boolean debugPropagation() {
                return false;
            }
        });
        //*******************************************************************************
        //Inputs
        //*******************************************************************************

        
        System.out.println("infrastrctureId="+infrastructureId+" ; usersRequirementId="+usersRequirementsId);
        
        G_infrastructure g_infrastructure = new G_infrastructure(infrastructureId);
        G_usersRequirements g_usersRequirements = new G_usersRequirements(usersRequirementsId);

        int trashServerId = g_infrastructure.getServers().size();//!

        int[] S_ids = new int[g_infrastructure.getServers().size() + 1];//!
        for (int i = 0; i < S_ids.length - 1; i++)//!
            S_ids[i] = i;
        // The trash server
        S_ids[S_ids.length - 1] = trashServerId;//!

        int[] L_ids = new int[g_infrastructure.getLinks().size()];//!
        for (int i = 0; i < L_ids.length; i++)
            L_ids[i] = g_infrastructure.getLinks().get(i).id;

        int[] F_ids = new int[g_usersRequirements.getFunctions().size()];
        for (int i = 0; i < F_ids.length; i++) {
            F_ids[i] = i;
        }
        //System.out.println(F_ids.length);

        int[] R_ids = new int[g_usersRequirements.getFlows().size()];
        for (int index = 0; index < R_ids.length; index++)
            R_ids[index] = g_usersRequirements.getFlows().get(index).id;

        

        //*******************************************************************************
        //Variables and their domains
        //*******************************************************************************


        // X_serverHostingF
        //********************
        IntVar[] X_serverHostingF = model.intVarArray("X_serverHostingF", F_ids.length, S_ids);
        //Secondary variable equivalent to the previous
        SetVar[] X_FunctionsOnServer = model.setVarArray("X_FunctionsOnServer", S_ids.length, new int[]{}, F_ids);
        model.setsIntsChanneling(X_FunctionsOnServer, X_serverHostingF).post();

        // X_linksHostingFl
        //********************
        SetVar[] X_linksHostingFl = model.setVarArray("X_linksHostingFl", R_ids.length, new int[]{}, L_ids);
        int[] domain_indices_links = new int[L_ids.length];
        for (int index = 0; index < L_ids.length; index++)
            domain_indices_links[index] = index;
        SetVar[] X_indices_linksHostingFl = model.setVarArray("X_indices_linksHostingFl", R_ids.length, new int[]{}, domain_indices_links);
        BoolVar[][] X_flowToLink = new BoolVar[R_ids.length][L_ids.length];

        //Secondary variable equivalent to the previous
        /*BoolVar[][] X_flowToLink = model.boolVarMatrix("X_flowToLink", FxF_ids.length, L_ids.length);
        for (int flowId = 0; flowId < FxF_ids.length; flowId++){
            model.setBoolsChanneling(X_flowToLink[flowId], X_indices_linksHostingFl[flowId]).post();
        }*/
        
        int[] domain_indices_flows = new int[R_ids.length];
        for (int index = 0; index < R_ids.length; index++)
            domain_indices_flows[index] = index;
        

        for (int flowId = 0; flowId < R_ids.length; flowId++) {
            for (int lIndex = 0; lIndex < L_ids.length; lIndex++) {
                X_flowToLink[flowId][lIndex] = model.member(L_ids[lIndex], X_linksHostingFl[flowId]).reify();
//                new Constraint("iInS", new PropXinSReif(lIndex, X_indices_linksHostingFl[flowId], X_flowToLink[flowId][lIndex])).post();
//                model.member(lIndex, X_indices_linksHostingFl[flowId]).reifyWith(X_flowToLink[flowId][lIndex]);
//                model.notMember(lIndex, X_indices_linksHostingFl[flowId]));
//                model.ifThenElse(model.member(L_ids[lIndex], X_linksHostingFl[flowId]), model.member(lIndex, X_indices_linksHostingFl[flowId]), model.notMember(lIndex, X_indices_linksHostingFl[flowId]));
            }
            model.setBoolsChanneling(X_flowToLink[flowId], X_indices_linksHostingFl[flowId]).post();
        }
        
        IntVar[] X_capabilityRequirementOfFlow = model.intVarArray("X_capabilityRequirementOfFlow", R_ids.length, 0, maxLinkCapability);
        IntVar[][] X_requiredCapacityFromLinkByFlow = model.intVarMatrix(L_ids.length, R_ids.length, 0, maxLinkCapability);

        SetVar[] X_flowsOnLink = model.setVarArray("X_flowsOnLink", L_ids.length, new int[]{}, domain_indices_flows);

        for (int link = 0; link < L_ids.length; link++) {
            BoolVar[] contains = new BoolVar[R_ids.length];
            for (int flow = 0; flow < R_ids.length; flow++) {
//                new Constraint("iInS", new PropXinSReif(flow, X_flowsOnLink[link], X_flowToLink[flow][link])).post();
                model.times(X_capabilityRequirementOfFlow[flow], X_flowToLink[flow][link], X_requiredCapacityFromLinkByFlow[link][flow]).post();
                contains[flow] = X_flowToLink[flow][link];
            }
            model.setBoolsChanneling(contains, X_flowsOnLink[link]).post();
        }
        /*for (int flow = 0; flow < FxF_ids.length; flow++) {
            for (int link = 0; link < L_ids.length; link++) {
                new Constraint("iInS", new PropXinSReif(flow, X_flowsOnLink[link], X_flowToLink[flow][link])).post();
//                model.member(flow, X_flowsOnLink[link]).reifyWith(X_flowToLink[flow][link]);
//                model.ifThenElse(X_flowToLink[flow][link], model.member(flow, X_flowsOnLink[link]), model.notMember(flow, X_flowsOnLink[link]));
                model.times(X_capabilityRequirementOfFlow[flow], X_flowToLink[flow][link], X_requiredCapacityFromLinkByFlow[link][flow]).post();
//                model.ifThenElse(X_flowToLink[flow][link],
//                        model.arithm(X_requiredCapacityFromLinkByFlow[link][flow], "=", X_capabilityRequirementOfFlow[flow]),
//                        model.arithm(X_requiredCapacityFromLinkByFlow[link][flow], "=", 0));
//            }
            }
        }*/

        //*******************************************************************************
        // Domain constraint on X_linksHostingFl: the links should form a path
        //*******************************************************************************

        // Let's define X_infrastrcturePaths
        int nbPaths = 0;
        for (PathsBetweenTwoVertices paths : g_infrastructure.getAllPaths())
            nbPaths = nbPaths + paths.edgesIdsOfPath.size();

        SetVar[] X_infrastrcturePaths = new SetVar[nbPaths + S_ids.length];//!
        int[] pathSource = new int[nbPaths + S_ids.length];//!
        int[] pathDestination = new int[nbPaths + S_ids.length];//!
                
        // Don't forget the loops...
        int[] loopIds = new int[S_ids.length - 1];//!
        for (int i = 0; i < S_ids.length - 1; i++)//!
            loopIds[i] = i * (S_ids.length - 1) + i;
        for (int i = 0; i < S_ids.length - 1; i++) {//!
            pathSource[i] = i;
            pathDestination[i] = i;
            X_infrastrcturePaths[i] = model.setVar("X_infrastrcturePaths[" + i + "]", loopIds[i]);
        }
        
        int offset = 0;
        offset=S_ids.length - 1;
        
        for (PathsBetweenTwoVertices pathsHavingSameSourceAndDestination : g_infrastructure.getAllPaths()) {
			
			while (pathsHavingSameSourceAndDestination.edgesIdsOfPath.size()!=0)
			{
				int nextPathIndex = 0;
				//tri
				for (int pathIndex=0; pathIndex<pathsHavingSameSourceAndDestination.edgesIdsOfPath.size(); pathIndex++){
					if(pathsHavingSameSourceAndDestination.edgesIdsOfPath.get(pathIndex).size()<pathsHavingSameSourceAndDestination.edgesIdsOfPath.get(nextPathIndex).size())
						nextPathIndex=pathIndex;
				}				
				//System.out.println(pathsHavingSameSourceAndDestination.sourceId+"::"+pathsHavingSameSourceAndDestination.edgesIdsOfPath.get(nextPathIndex).toString()+"::"+pathsHavingSameSourceAndDestination.destinationId);
				                
                int[] ub = new int[pathsHavingSameSourceAndDestination.edgesIdsOfPath.get(nextPathIndex).size()];
                for (int j = 0; j < pathsHavingSameSourceAndDestination.edgesIdsOfPath.get(nextPathIndex).size(); j++) {
                    ub[j] = pathsHavingSameSourceAndDestination.edgesIdsOfPath.get(nextPathIndex).get(j);
                }
                X_infrastrcturePaths[offset] = model.setVar("X_infrastrcturePaths[" + offset + "]", ub);
				pathSource[offset] = pathsHavingSameSourceAndDestination.sourceId;
                pathDestination[offset] = pathsHavingSameSourceAndDestination.destinationId;
                
                offset++;
                pathsHavingSameSourceAndDestination.edgesIdsOfPath.remove(nextPathIndex);
                
			}
        }

        

        //Don't forget the empty path
        //!X_infrastrcturePaths[X_infrastrcturePaths.length-1]=model.setVar("X_infrastrcturePaths["+(X_infrastrcturePaths.length-1)+"]", new int[] {});
        //!pathSource[pathSource.length-1]=-1;
        //!pathDestination[pathDestination.length-1]=-1;
        pathSource[offset] = trashServerId;//!
        pathDestination[offset] = trashServerId;//!
        X_infrastrcturePaths[offset] = model.setVar("X_infrastrcturePaths[" + offset + "]", new int[]{});//!
        //System.out.println(offset);
        //now the constraints
        IntVar[] X_index_linksHostingFl = model.intVarArray("X_indice_linksHostingFl", R_ids.length, 0, X_infrastrcturePaths.length - 1);
        for (int flowId = 0; flowId < R_ids.length; flowId++) {
            //X_infrastrcturePaths[X_index_linksHostingFl[flowId]]=X_linksHostingFl[flowId]
            model.element(X_index_linksHostingFl[flowId], X_infrastrcturePaths, X_linksHostingFl[flowId]).post();
        }


        //*******************************************************************************
        // Constraint 2
        //*******************************************************************************
        
        for (int functionId = 0; functionId < F_ids.length; functionId++) {
            //System.out.print("fun: "+functionId);
            for (int i = 0; i < F_ids.length; i++) {
                if (i != functionId) {
                    int incomingFlowId = i * F_ids.length + functionId;
                    int outgoingFlowId = functionId * F_ids.length + i;
                    for (int j = 0; j < R_ids.length; j++){
                    	if (R_ids[j]==incomingFlowId){
                    		model.element(X_serverHostingF[functionId], pathDestination, X_index_linksHostingFl[j]).post();
                    	}
                    	if (R_ids[j]==outgoingFlowId){
                    		model.element(X_serverHostingF[functionId], pathSource, X_index_linksHostingFl[j]).post();
                    	}
                    }
                    
                }
            }
        }

        //*******************************************************************************
        //Constraint 8
        //*******************************************************************************

        for (Function function : g_usersRequirements.getFunctions()) {
            if (function.predefinedHost != -1) {
                model.arithm(X_serverHostingF[function.id], "=", function.predefinedHost).post();
            }
        }
        
        //*******************************************************************************
        //Constraint 9
        //*******************************************************************************

        for (int flowIndex = 0; flowIndex < g_usersRequirements.getFlows().size(); flowIndex++) {
            int j = 0;
            while (j < g_usersRequirements.getFunctions().size() && g_usersRequirements.getFunctions().get(j).id != flowIndex)
                j++;
            if (j <= g_usersRequirements.getFunctions().size())
            	model.arithm(X_capabilityRequirementOfFlow[flowIndex], "=", g_usersRequirements.getFlows().get(flowIndex).capabilityRequ).post();
        }
        
        IntVar[] X_requiredCapacityFromLink = model.intVarArray("X_requiredCapacityFromLink", L_ids.length, 0, maxLinkCapability);
        for (int linkIndex = 0; linkIndex < L_ids.length; linkIndex++) {
            model.sum(X_requiredCapacityFromLinkByFlow[linkIndex], "=", X_requiredCapacityFromLink[linkIndex]).post();
            model.arithm(X_requiredCapacityFromLink[linkIndex], "<=", g_infrastructure.getLinks().get(linkIndex).capability).post();
        }

        //*******************************************************************************
        //Constraint 10
        //*******************************************************************************
        
        int[] usersFunctionsRequirements = new int[g_usersRequirements.getFunctions().size()];
        for (int functionId = 0; functionId < usersFunctionsRequirements.length; functionId++)
            usersFunctionsRequirements[functionId] = g_usersRequirements.getFunctions().get(functionId).capabilityRequ;

        IntVar[] X_hostableCapabilityByServer = new IntVar[S_ids.length - 1];//!
        for (int serverId = 0; serverId < S_ids.length - 1; serverId++)//!
        {
            X_hostableCapabilityByServer[serverId] = model.intVar("X_hostableCapabilityByServer[" + serverId + "]", 0, g_infrastructure.getServers().get(serverId).capability);
            //Constraint
            model.sumElements(X_FunctionsOnServer[serverId], usersFunctionsRequirements, X_hostableCapabilityByServer[serverId]).post();
        }

        //*******************************************************************************
        //Constraint 11
        //*******************************************************************************

        //X_linksHostingRPath
        int nbRPaths = 0;
        for (PathsBetweenTwoVertices paths : g_usersRequirements.getAllPaths())
            nbRPaths = nbRPaths + paths.edgesIdsOfPath.size();
        SetVar[] X_linksHostingRPath = model.setVarArray("X_linksHostingRPath", nbRPaths, new int[]{}, L_ids);
        int pathIndex = 0;
        SetVar[][] X_unionParameter = model.setVarMatrix("X_unionParameter", nbRPaths, R_ids.length, new int[]{}, L_ids);
        for (PathsBetweenTwoVertices paths : g_usersRequirements.getAllPaths()) {
            for (List<Integer> path : paths.edgesIdsOfPath) {
                //System.out.println(paths.sourceId+"::"+path.toString()+"::"+paths.destinationId);
                for (int rIndex = 0; rIndex < R_ids.length; rIndex++) {
                    if (path.contains(R_ids[rIndex]))
                        model.allEqual(X_unionParameter[pathIndex][rIndex], X_linksHostingFl[rIndex]).post();
                    else
//                        model.allEqual(X_unionParameter[pathIndex][rIndex], emptySet).post();
                        X_unionParameter[pathIndex][rIndex].getCard().eq(0).post();

                }
                model.union(X_unionParameter[pathIndex], X_linksHostingRPath[pathIndex]).post();
                pathIndex++;
            }
        }

        SetVar[] X_indices_linksHostingRPath = model.setVarArray("X_indices_linksHostingRPath", nbRPaths, new int[]{}, domain_indices_links);

        for (int pIndex = 0; pIndex < nbRPaths; pIndex++){
            for (int lIndex = 0; lIndex < L_ids.length; lIndex++) {
                BoolVar bv = model.boolVar();
                new Constraint("iInS", new PropXinSReif(L_ids[lIndex], X_linksHostingRPath[pIndex], bv)).post();
                new Constraint("iInS", new PropXinSReif(lIndex, X_indices_linksHostingRPath[pIndex], bv)).post();
//                model.member(lIndex, X_indices_linksHostingRPath[pIndex]).reifyWith(model.member(L_ids[lIndex], X_linksHostingRPath[pIndex]).reify());
//                model.ifThenElse(model.member(L_ids[lIndex], X_linksHostingRPath[pIndex]), model.member(lIndex, X_indices_linksHostingRPath[pIndex]), model.notMember(lIndex, X_indices_linksHostingRPath[pIndex]));
            }
        }

        //toleratedLatencyByFunction
        int toleratedLatencyByFunction[] = new int[F_ids.length];
        for (int functionId = 0; functionId < F_ids.length; functionId++) {
            int j = 0;
            while (j < g_usersRequirements.getFunctions().size() && g_usersRequirements.getFunctions().get(j).id != functionId)
                j++;
            if (g_usersRequirements.getFunctions().get(j).id == functionId) {
                toleratedLatencyByFunction[functionId] = g_usersRequirements.getFunctions().get(j).maxLatency;
            }
        }
        //X_latencyOfPath
        IntVar[] X_latencyOfPath = new IntVar[nbRPaths];
        int pathIndex2 = 0;
        for (PathsBetweenTwoVertices paths : g_usersRequirements.getAllPaths()) {
            for (@SuppressWarnings("unused") List<Integer> path : paths.edgesIdsOfPath) {
                X_latencyOfPath[pathIndex2] = model.intVar("X_latencyOfPath[" + pathIndex2 + "]", 0, toleratedLatencyByFunction[paths.destinationId]);
                pathIndex2++;
            }
        }

        //latencyOfLink
        int[] latencyOfLink = new int[g_infrastructure.getLinks().size()];
        for (int lIndex = 0; lIndex < latencyOfLink.length; lIndex++) {
            latencyOfLink[lIndex] = g_infrastructure.getLinks().get(lIndex).latency;
        }

        //Constraint
        for (int pIndex = 0; pIndex < nbRPaths; pIndex++)
            model.sumElements(X_indices_linksHostingRPath[pIndex], latencyOfLink, X_latencyOfPath[pIndex]).post();
        
        //*******************************************************************************
        //Objective function
        //*******************************************************************************
        
        /**/
        //X_computeFootprint
        int maxComputeFootprint = 0;
        for (int serverId = 0; serverId < S_ids.length - 1; serverId++) {//!
            maxComputeFootprint = maxComputeFootprint + g_infrastructure.getServers().get(serverId).capability;
        }
        IntVar X_computeFootprint = model.intVar("X_computeFootprint", 0, maxComputeFootprint);
        model.sum(X_hostableCapabilityByServer, "=", X_computeFootprint).post();

/**/
        //X_networkFootprint
        int maxNetworkFootprint = 0;
        for (int linkIndex = 0; linkIndex < L_ids.length; linkIndex++) {
            maxNetworkFootprint = maxNetworkFootprint + g_infrastructure.getLinks().get(linkIndex).capability;
        }
        IntVar X_networkFootprint = model.intVar("X_networkFootprint", 0, maxNetworkFootprint);
        model.sum(X_requiredCapacityFromLink, "=", X_networkFootprint).post();
        
/**/
        //X_footprint
        IntVar[] X_footprints = new IntVar[2];
        X_footprints[0] = X_networkFootprint;
        X_footprints[1] = X_computeFootprint;
        IntVar X_footprint = model.intVar("X_footprint", 0, maxComputeFootprint + maxNetworkFootprint);
        model.sum(X_footprints, "=", X_footprint).post();


        //Objective
        model.setObjective(Model.MINIMIZE, X_footprint);

        
        //*******************************************************************************
        //Resolution
        //*******************************************************************************
        

        Solver solver = model.getSolver();
        solver.setSearch(
                Search.lastConflict(
                        new StrategiesSequencer(
							Search.minDomLBSearch(X_index_linksHostingFl),
							Search.minDomLBSearch(X_serverHostingF),
							Search.minDomLBSearch(X_capabilityRequirementOfFlow)
                        )
                )
        );
        
//        Solution solution = solver.findSolution();
//        if (solution == null)
//            System.out.println("No solution");
//        int cstrs = 0;
//        int count = 0;
//        for (Constraint c : (Set<Constraint>) model.getHook("cinstances")) {
//            cstrs++;
//            count += c.isReified() ? 1 : 0;
//        }
        System.out.printf("#var: %d, #cstr: %d " +
//                        "(%d : %d), " +
                        "(%.3fs)\n",
                model.getNbVars(),
                model.getNbCstrs(),
//                cstrs,
//                count,
                (System.nanoTime() - model.getCreationTime()) * 1.e-9f);
        System.out.printf("#ivar: %d, #bvar: %d, #svar: %d\n",
                model.getNbIntVar(false),
                model.getNbBoolVar(),
                model.getNbSetVar());


        int minFP = Integer.MAX_VALUE;
        int maxFP = 0;
        solver.limitTime("10m");
//        solver.showSolutions();
        solver.setRestartOnSolutions();
//        solver.setNoGoodRecordingFromRestarts();
//        solver.showDashboard();
        
        ArrayList<Float> time=new ArrayList<Float>();
        ArrayList<Integer> networkFootprint=new ArrayList<Integer>();
        ArrayList<Integer> computeFootprint=new ArrayList<Integer>();
        ArrayList<Integer> mutualisation=new ArrayList<Integer>();
        
        networkFootprint.add(0);
        computeFootprint.add(0);
        System.out.println("Time & computeFootprint & networkFootprint");
        
        boolean firstSolution = true;
        CalculationResult result = new CalculationResult();
        
        while(solver.solve()) {
            //System.out.printf("#sol: %s (%.3fs)\n", solver.getSolutionCount(), solver.getTimeCount());
            //System.out.printf("computeFootprint: %d\n", X_computeFootprint.getValue());
            //System.out.printf("networkFootprint: %d\n", X_networkFootprint.getValue());
        	            
            time.add(solver.getTimeCount());
            networkFootprint.add(X_networkFootprint.getValue());
            computeFootprint.add(X_computeFootprint.getValue());
            
            System.out.println(solver.getTimeCount()+" & "+X_computeFootprint.getValue()+" & "+X_networkFootprint.getValue());
            if (firstSolution){
            	result.firstSolution_time = solver.getTimeCount();
            	result.firstSolution_computeFootprint = X_computeFootprint.getValue();
            	result.firstSolution_networkFootprint = X_networkFootprint.getValue();
            	result.lastSolution_time = solver.getTimeCount();
            	result.lastSolution_computeFootprint = X_computeFootprint.getValue();
            	result.lastSolution_networkFootprint = X_networkFootprint.getValue();
            	firstSolution = false;
            }
            else{
            	result.lastSolution_time = solver.getTimeCount();
            	result.lastSolution_computeFootprint = X_computeFootprint.getValue();
            	result.lastSolution_networkFootprint = X_networkFootprint.getValue();
            }
            
            
            if (minFP > X_footprint.getValue()) minFP = X_footprint.getValue();
            if (maxFP < X_footprint.getValue()) maxFP = X_footprint.getValue();
            //System.out.printf("[%d, %d]\n", minFP, maxFP);
//            for (IntVar v : model.retrieveIntVars(false)) {
//                System.out.printf("%s\n", v);
//            }
//            for (SetVar v : model.retrieveSetVars()) {
//                System.out.printf("%s\n", v);
//            }
//            for(IntVar v : X_serverHostingF){
//                assert v.isInstantiated();
//            }
            //System.out.println(solution.toString().replaceAll(", X", ", \n X"));
//            String X_str_serviceFunctions = X_serviceFunctions.getValue().toString();
//            System.out.println("X_serviceFunctions : " + X_serviceFunctions.getValue().toString());
//            for (int functionId = 0; functionId < F_ids.length; functionId++)
//                if (X_str_serviceFunctions.contains(Integer.toString(functionId)))
//                    System.out.println("X_serverHostingF[" + functionId + "] = " + X_serverHostingF[functionId].getValue());
//            System.out.println();
//            System.out.println("X_serviceFlows : " + X_serviceFlows.getValue().toString());
//            for (int flowId = 0; flowId < FxF_ids.length; flowId++)
//                if ((X_linksHostingFl[flowId].getValue().toString().contentEquals("{}")) == false)
//                    System.out.println("X_linksHostingFl[" + flowId + "] = " + X_linksHostingFl[flowId].getValue());
//	    	/**/
//            System.out.println();
//	    	for (int flowId = 0; flowId < X_flowEquivToRindex.length; flowId++)
//                System.out.println("X_flowEquivToRindex[" + flowId + "] = " + X_flowEquivToRindex[flowId].getValue());
            /*ClassLoader classLoader = Calculator.class.getClassLoader();
            String filesPath = classLoader.getResource("").getPath();
            try {
                PlacementVizualiser.visualize(g_infrastructure, F_ids.length, X_serviceFunctions, X_serviceFlows, X_serverHostingF,
                        filesPath+ "/servicePlacement"+solver.getSolutionCount()+".png");
            } catch (IOException e) {
                e.printStackTrace();
            }*/
        }
//        System.out.println();
        //solver.printStatistics();
        //String str = "T"+usersRequirementsId+"(x)= ";
        String networkFootprintCurve = "N_networkFootrpint_"+infrastructureId+usersRequirementsId+"(x)= ";
        String computeFootprintCurve = "N_computeFootrpint_"+infrastructureId+usersRequirementsId+"(x)= ";
        String computeMutualisationLabels = "";
        String networkMutualisationLabels = "";
        
        int i=0;
        String time2="";
        while (i<time.size()){
        	computeFootprintCurve = computeFootprintCurve + "x<" + time.get(i)+" ? "+computeFootprint.get(i)+" : ";
        	networkFootprintCurve = networkFootprintCurve + "x<" + time.get(i)+" ? "+networkFootprint.get(i)+" : ";
        	if (i==time.size()-1)
        		time2="maxTime";
        	else
        		time2=time.get(i+1).toString();
        	
        	/*
    		mutualisationLabels = mutualisationLabels + "set label '"
						        	+ mutualisation.get(i) 
						        	+"' textcolor lt 1 at "+time.get(i)
						        	+"+("+time2+"-"+time.get(i)
						        	+")/2, "+(computeFootprint.get(i+1)+1)+";";
			*/
        	i++;
        }
        computeFootprintCurve = computeFootprintCurve + computeFootprint.get(i);
        networkFootprintCurve = networkFootprintCurve + networkFootprint.get(i);
        
        //System.out.println(computeFootprintCurve);
        //System.out.println(networkFootprintCurve);
        //System.out.println();

        return result;
    
    
    }

}