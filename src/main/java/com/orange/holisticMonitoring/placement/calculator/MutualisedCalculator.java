package com.orange.holisticMonitoring.placement.calculator;

import com.esotericsoftware.yamlbeans.YamlException;
import com.orange.holisticMonitoring.placement.constraint.PropXinSReif;
import com.orange.holisticMonitoring.placement.inputs.Function;
import com.orange.holisticMonitoring.placement.inputs.FunctionType;
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
import org.chocosolver.solver.search.strategy.assignments.DecisionOperatorFactory;
import org.chocosolver.solver.search.strategy.selectors.variables.FirstFail;
import org.chocosolver.solver.search.strategy.strategy.AbstractStrategy;
import org.chocosolver.solver.search.strategy.strategy.StrategiesSequencer;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.SetVar;
import org.chocosolver.util.criteria.Criterion;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class MutualisedCalculator {

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
    
    public static int factorial(int n) {
        
    	int fact = 1;        
        for (int i = 1; i <= n; i++) {
            fact = fact * i;
        }
        return fact;
    }
    
    public static int combination(int n, int k) {
        
        return (factorial(n) / (factorial(n - k) * factorial(k)));
    }

    private static int defaultFlow(IntVar var, Map<IntVar, Integer> map) {
        return map.get(var);
    }
    
    static int maxLinkCapability = 10000;

    public static void main(String[] args) throws IOException {
    	
    	
		MutualisedCalculator.calculate("C8","C8W4L5S49");
		
    }

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

        G_infrastructure g_infrastructure = new G_infrastructure(infrastructureId);
        G_usersRequirements g_usersRequirements = new G_usersRequirements(usersRequirementsId);

        int trashServerId = g_infrastructure.getServers().size();//!

        int[] S_ids = new int[g_infrastructure.getServers().size() + 1];//!
        for (int i = 0; i < S_ids.length - 1; i++)//!
            S_ids[i] = i;
        // The trash server
        S_ids[S_ids.length - 1] = trashServerId;//!

        System.out.println("infrastrctureId="+infrastructureId+" ("+g_infrastructure.getServers().size()+" servers) ; usersRequirementId="+usersRequirementsId);
        
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

        int[] FxF_ids = new int[g_usersRequirements.getFunctions().size() * g_usersRequirements.getFunctions().size()];
        for (int i = 0; i < FxF_ids.length; i++)
            FxF_ids[i] = i;


        //*******************************************************************************
        //Variables and their domains
        //*******************************************************************************

        //X_serviceFunctions
        //********************
        SetVar X_serviceFunctions = model.setVar("X_serviceFunctions", new int[]{}, F_ids);

        // X_serviceFlows
        //********************
        SetVar X_serviceFlows = model.setVar("X_serviceFlows", new int[]{}, FxF_ids);
        //Secondary variables which are equivalent to the previous
        BoolVar[] X_serviceFlows_containsFl = model.boolVarArray("X_serviceContainsFl", FxF_ids.length);
        model.setBoolsChanneling(X_serviceFlows_containsFl, X_serviceFlows).post();

        //X_flowEquivToRindex
        //********************
        IntVar[] X_flowEquivToRindex = model.intVarArray("X_flowEquivToRindex", R_ids.length, FxF_ids);
        //Secondary variable equivalent to the previous
        int[] domain_rIndicesEquivToFlow = new int[R_ids.length];
        for (int index = 0; index < R_ids.length; index++)
            domain_rIndicesEquivToFlow[index] = index;
        SetVar[] X_rIndicesEquivToFlow = model.setVarArray("X_rIndicesEquivToFlow", FxF_ids.length, new int[]{}, domain_rIndicesEquivToFlow);
        model.setsIntsChanneling(X_rIndicesEquivToFlow, X_flowEquivToRindex).post();
        /*for (int flowId = 0; flowId < FxF_ids.length; flowId++) {
            for (int rIndex = 0; rIndex < R_ids.length; rIndex++) {
                BoolVar bv = model.arithm(X_flowEquivToRindex[rIndex], "=", flowId).reify();
                new Constraint("iInS", new PropXinSReif(rIndex, X_rIndicesEquivToFlow[flowId], bv)).post();
//                model.member(rIndex, X_rIndicesEquivToFlow[flowId]).reifyWith(bv);
//                model.notMember(rIndex, X_rIndicesEquivToFlow[flowId]).reifyWith(bv.not());
//                model.ifThenElse(, , );
            }
        }*/

        // X_serversHostingF
        //********************
        IntVar[] X_serverHostingF = model.intVarArray("X_serverHostingF", F_ids.length, S_ids);
        //Secondary variable equivalent to the previous
        SetVar[] X_FunctionsOnServer = model.setVarArray("X_FunctionsOnServer", S_ids.length, new int[]{}, F_ids);
        model.setsIntsChanneling(X_FunctionsOnServer, X_serverHostingF).post();

        // X_linksHostingFl
        //********************
        SetVar[] X_linksHostingFl = model.setVarArray("X_linksHostingFl", FxF_ids.length, new int[]{}, L_ids);
        int[] domain_indices_links = new int[L_ids.length];
        for (int index = 0; index < L_ids.length; index++)
            domain_indices_links[index] = index;
        SetVar[] X_indices_linksHostingFl = model.setVarArray("X_indices_linksHostingFl", FxF_ids.length, new int[]{}, domain_indices_links);
        BoolVar[][] X_flowToLink = new BoolVar[FxF_ids.length][L_ids.length];

        //Secondary variable equivalent to the previous
        /*BoolVar[][] X_flowToLink = model.boolVarMatrix("X_flowToLink", FxF_ids.length, L_ids.length);
        for (int flowId = 0; flowId < FxF_ids.length; flowId++){
            model.setBoolsChanneling(X_flowToLink[flowId], X_indices_linksHostingFl[flowId]).post();
        }*/

        for (int flowId = 0; flowId < FxF_ids.length; flowId++) {
            for (int lIndex = 0; lIndex < L_ids.length; lIndex++) {
                X_flowToLink[flowId][lIndex] = model.member(L_ids[lIndex], X_linksHostingFl[flowId]).reify();
//                new Constraint("iInS", new PropXinSReif(lIndex, X_indices_linksHostingFl[flowId], X_flowToLink[flowId][lIndex])).post();
//                model.member(lIndex, X_indices_linksHostingFl[flowId]).reifyWith(X_flowToLink[flowId][lIndex]);
//                model.notMember(lIndex, X_indices_linksHostingFl[flowId]));
//                model.ifThenElse(model.member(L_ids[lIndex], X_linksHostingFl[flowId]), model.member(lIndex, X_indices_linksHostingFl[flowId]), model.notMember(lIndex, X_indices_linksHostingFl[flowId]));
            }
            model.setBoolsChanneling(X_flowToLink[flowId], X_indices_linksHostingFl[flowId]).post();
        }
        IntVar[] X_capabilityRequirementOfFlow = model.intVarArray("X_capabilityRequirementOfFlow", FxF_ids.length, 0, maxLinkCapability);
        IntVar[][] X_requiredCapacityFromLinkByFlow = model.intVarMatrix(L_ids.length, FxF_ids.length, 0, maxLinkCapability);

        SetVar[] X_flowsOnLink = model.setVarArray("X_flowsOnLink", L_ids.length, new int[]{}, FxF_ids);

        for (int link = 0; link < L_ids.length; link++) {
            BoolVar[] contains = new BoolVar[FxF_ids.length];
            for (int flow = 0; flow < FxF_ids.length; flow++) {
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
        
        // pathsHavingSameSourceAndDestination -> edgesIdsOfPath -> <edges, ....>
        
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
        IntVar[] X_index_linksHostingFl = model.intVarArray("X_indice_linksHostingFl", FxF_ids.length, 0, X_infrastrcturePaths.length - 1);
        for (int flowId = 0; flowId < FxF_ids.length; flowId++) {
            //X_infrastrcturePaths[X_index_linksHostingFl[flowId]]=X_linksHostingFl[flowId]
            model.element(X_index_linksHostingFl[flowId], X_infrastrcturePaths, X_linksHostingFl[flowId]).post();
        }

        //*******************************************************************************
        // Constraint 1 : if one of the incoming or the Outgoing flows of a function is in X_serviceFlows then the function is in X_serviceFunctions
        //*******************************************************************************

        SetVar[] X_incomingAndOutgoingFlows_Function = new SetVar[F_ids.length];
        int[][] flowsOfFunction = new int[F_ids.length][2 * F_ids.length];
        for (int functionId = 0; functionId < F_ids.length; functionId++) {
            for (int tailId = 0; tailId < F_ids.length; tailId++)
                flowsOfFunction[functionId][tailId] = F_ids.length * functionId + tailId;
            for (int headId = 0; headId < F_ids.length; headId++)
                flowsOfFunction[functionId][F_ids.length + headId] = F_ids.length * headId + functionId;
            X_incomingAndOutgoingFlows_Function[functionId] = model.setVar("X_incomingAndOutgoingFlows_Function[" + functionId + "]", flowsOfFunction[functionId]);
        }

        SetVar[][] X_intersectionParamaters = new SetVar[F_ids.length][2];

        SetVar[] X_intersectionOF_incomingAndOutgoingFlows_Function_And_X_serviceFlows = model.setVarArray("X_intersectionOF_incoming...", F_ids.length, new int[]{}, FxF_ids);
        for (int functionId = 0; functionId < F_ids.length; functionId++) {
            X_intersectionParamaters[functionId][0] = X_incomingAndOutgoingFlows_Function[functionId];
            X_intersectionParamaters[functionId][1] = X_serviceFlows;
            model.intersection(X_intersectionParamaters[functionId], X_intersectionOF_incomingAndOutgoingFlows_Function_And_X_serviceFlows[functionId]).post();

            BoolVar member = model.notEmpty(X_intersectionOF_incomingAndOutgoingFlows_Function_And_X_serviceFlows[functionId]).reify();

            new Constraint("iInS", new PropXinSReif(functionId, X_serviceFunctions, member)).post();
//            model.member(functionId, X_serviceFunctions).reifyWith(member);
            model.reification(member.not(), model.arithm(X_serverHostingF[functionId], "=", trashServerId));//!
//            model.ifThenElse(
//                    model.notEmpty(X_intersectionOF_incomingAndOutgoingFlows_Function_And_X_serviceFlows[functionId]),
//                    model.member(functionId, X_serviceFunctions),
//                    model.notMember(functionId, X_serviceFunctions));
        }

        //*******************************************************************************
        // Constraint 2
        //*******************************************************************************

        for (int functionId = 0; functionId < F_ids.length; functionId++) {
            //System.out.print("fun: "+functionId);
            for (int i = 0; i < F_ids.length; i++) {
                if (i != functionId) {
                    int incomingFlowId = i * F_ids.length + functionId;
                    //System.out.print(" in: "+incomingFlowId);
                    // pathDestination[X_index_linksHostingFl[incomingFlowId]]=X_serverHostingF[functionId]
                    model.ifThen(X_serviceFlows_containsFl[incomingFlowId], model.element(X_serverHostingF[functionId], pathDestination, X_index_linksHostingFl[incomingFlowId]));
                    int outgoingFlowId = functionId * F_ids.length + i;
                    //System.out.println(" out: "+outgoingFlowId);
                    model.ifThen(X_serviceFlows_containsFl[outgoingFlowId], model.element(X_serverHostingF[functionId], pathSource, X_index_linksHostingFl[outgoingFlowId]));
                }
            }
        }
        //*******************************************************************************
        // Constraint 3
        //*******************************************************************************

        model.union(X_flowEquivToRindex, X_serviceFlows).post();


        //*******************************************************************************
        // Test
        //*******************************************************************************
/*
		for (int rFlowIndex=0; rFlowIndex<R_ids.length; rFlowIndex++)
            model.arithm(X_flowEquivToRindex[rFlowIndex],"=",R_ids[rFlowIndex]).post();
*/

        //*******************************************************************************
        // Constraint 4
        //*******************************************************************************

        int[] typeOfF = new int[F_ids.length];
        int[] parameterOfF = new int[F_ids.length];
        for (int functionId = 0; functionId < F_ids.length; functionId++) {
            int j = 0;
            while (j < g_usersRequirements.getFunctions().size() && g_usersRequirements.getFunctions().get(j).id != functionId)
                j++;
            if (g_usersRequirements.getFunctions().get(j).id == functionId) {
                typeOfF[functionId] = g_usersRequirements.getFunctions().get(j).type.value;
                parameterOfF[functionId] = g_usersRequirements.getFunctions().get(j).parameter;
            }
        }

        IntVar[] X_tail_flowEquivToR = new IntVar[R_ids.length];//model.intVarArray("X_tail_flowEquivToR", R_ids.length, F_ids);
        for (int rFlowIndex = 0; rFlowIndex < R_ids.length; rFlowIndex++) {
            // The constraint 4
            int tailFunctionId = tailId(R_ids[rFlowIndex], F_ids.length);
            if (typeOfF[tailFunctionId] == FunctionType.probe.value)
                X_tail_flowEquivToR[rFlowIndex] = model.intVar("X_tail_flowEquivToR["+rFlowIndex+"]", tailFunctionId);
//                model.arithm(X_tail_flowEquivToR[rFlowIndex], "=", tailFunctionId).post();
            else {
                int[] values = IntStream
                        .range(0, F_ids.length)
                        .filter(i -> typeOfF[i] == typeOfF[tailFunctionId] && parameterOfF[i] == parameterOfF[tailFunctionId])
                        .toArray();
                X_tail_flowEquivToR[rFlowIndex] = model.intVar("X_tail_flowEquivToR["+rFlowIndex+"]", values);
//                model.element(model.intVar(typeOfF[tailFunctionId]), typeOfF, X_tail_flowEquivToR[rFlowIndex]).post();
//                model.member(X_tail_flowEquivToR[rFlowIndex], ).post();
//                model.element(model.intVar(parameterOfF[tailFunctionId]), parameterOfF, X_tail_flowEquivToR[rFlowIndex]).post();
//                model.member(X_tail_flowEquivToR[rFlowIndex], IntStream.range(0, F_ids.length).filter(i -> parameterOfF[i] == parameterOfF[tailFunctionId]).toArray()).post();
            }
        }

        IntVar X_cte_lengthOf_F_ids = model.intVar(F_ids.length);
        for (int rFlowIndex = 0; rFlowIndex < R_ids.length; rFlowIndex++)
            model.div(X_flowEquivToRindex[rFlowIndex], X_cte_lengthOf_F_ids, X_tail_flowEquivToR[rFlowIndex]).post(); // Remplir X_tail_flowEquivToR

        IntVar[] X_head_flowEquivToR = model.intVarArray("X_head_flowEquivToR", R_ids.length, F_ids);
        for (int rFlowIndex = 0; rFlowIndex < R_ids.length; rFlowIndex++)
            model.mod(X_flowEquivToRindex[rFlowIndex], X_cte_lengthOf_F_ids, X_head_flowEquivToR[rFlowIndex]).post();
/*
        //Variables equivalent to the previous
        BoolVar[][] X_head_to_flowEquivToR = model.boolVarMatrix("X_head_to_flowEquivToR", F_ids.length, R_ids.length);
        for (int function = 0; function < F_ids.length; function++) {
        	for (int rflow = 0; rflow < R_ids.length; rflow++) {
        		model.arithm(X_head_flowEquivToR[rflow],"=",function).reifyWith(X_head_to_flowEquivToR[function][rflow]);        		
        	}
        }
*/
        //*******************************************************************************
        // Constraint 5
        //*******************************************************************************
    
        for (int rFlowIndex = 0; rFlowIndex < R_ids.length; rFlowIndex++) {
            int tailFunctionId = tailId(R_ids[rFlowIndex], F_ids.length);
            if (typeOfF[tailFunctionId] != FunctionType.probe.value) {
                for (int incoming_to_rFlowIndex = 0; incoming_to_rFlowIndex < R_ids.length; incoming_to_rFlowIndex++) {
                    if (headId(R_ids[incoming_to_rFlowIndex], F_ids.length) == tailId(R_ids[rFlowIndex], F_ids.length))
                        model.arithm(X_head_flowEquivToR[incoming_to_rFlowIndex], "=", X_tail_flowEquivToR[rFlowIndex]).post();
                }
            }
        }
      
        //*******************************************************************************
        // Constraint 5'
        //*******************************************************************************
        
        
        //X_incommingRflowsToRflow
        SetVar[] X_incommingRflowsToRflow= new SetVar[R_ids.length];
        for (int rFlowIndex = 0; rFlowIndex < R_ids.length; rFlowIndex++) {
        	
        	List <Integer> incoming_to_rFlow = new ArrayList<Integer>();
        	int tailFunctionId = tailId(R_ids[rFlowIndex], F_ids.length);
            for (int incoming_to_rFlowIndex = 0; incoming_to_rFlowIndex < R_ids.length; incoming_to_rFlowIndex++) {
                if (headId(R_ids[incoming_to_rFlowIndex], F_ids.length) == tailFunctionId)
                	incoming_to_rFlow.add(incoming_to_rFlowIndex);
            }
            
            int[] table_incoming_to_rFlow = new int [incoming_to_rFlow.size()];
            for (int r = 0; r < table_incoming_to_rFlow.length; r++)
            	table_incoming_to_rFlow[r]=incoming_to_rFlow.get(r);
            
            X_incommingRflowsToRflow[rFlowIndex]=model.setVar("X_incommingRToR["+rFlowIndex+"]",table_incoming_to_rFlow);
        }
        
        
        //X_incommingFlowsTo_flowEquivToRflow
        BoolVar[][] X_incommingFlowTo_flowEquivToRflow = model.boolVarMatrix("X_incommingFlowsTo_flowEquivToR", R_ids.length, FxF_ids.length);
        for (int rFlowIndex = 0; rFlowIndex < R_ids.length; rFlowIndex++) {
        	for (int flow=0; flow<FxF_ids.length; flow++){        		
        		model.and(X_serviceFlows_containsFl[flow],
        						model.mod(model.intVar(flow),X_cte_lengthOf_F_ids,X_tail_flowEquivToR[rFlowIndex]).reify())
        						.reifyWith(X_incommingFlowTo_flowEquivToRflow[rFlowIndex][flow]);
        	}
        }
        
        //The constraint
        SetVar[][] intersectionParameters= new SetVar[R_ids.length*FxF_ids.length][2];
        SetVar[][] intersectionResult = model.setVarMatrix(R_ids.length, FxF_ids.length, new int[]{}, domain_rIndicesEquivToFlow);
        IntVar card = model.intVar(1, R_ids.length);
        for (int rFlowIndex = 0; rFlowIndex < R_ids.length; rFlowIndex++) {
        	
        	
    		for (int flow=0; flow<FxF_ids.length; flow++){
    			intersectionParameters[rFlowIndex+flow][0]=X_incommingRflowsToRflow[rFlowIndex];
    			intersectionParameters[rFlowIndex+flow][1]=X_rIndicesEquivToFlow[flow];
        		model.ifThen(X_incommingFlowTo_flowEquivToRflow[rFlowIndex][flow], model.intersection(intersectionParameters[rFlowIndex+flow],intersectionResult[rFlowIndex][flow]));
        		intersectionResult[rFlowIndex][flow].setCard(card);
        	}
    		
        }
        
        //model.member(7,intersectionResult[11][67]).post();
        //model.member(7,intersectionResult[11][4]).post();
        
        //*******************************************************************************
        // Constraint 6
        //*******************************************************************************

        for (int rFlowIndex = 0; rFlowIndex < R_ids.length; rFlowIndex++) {
            int headFunctionId = headId(R_ids[rFlowIndex], F_ids.length);
            if (typeOfF[headFunctionId] == FunctionType.user.value)
                model.arithm(X_head_flowEquivToR[rFlowIndex], "=", headFunctionId).post();
        }

        //*******************************************************************************
        // Constraint 7
        //*******************************************************************************

        for (int flowId = 0; flowId < FxF_ids.length; flowId++)
            model.arithm(X_linksHostingFl[flowId].getCard(), ">", 0).reifyWith(X_serviceFlows_containsFl[flowId]);
//            model.ifThenElse(
//                    X_serviceFlows_containsFl[flowId],
//                    model.notEmpty(X_linksHostingFl[flowId]),
//                    model.allEqual(X_linksHostingFl[flowId], emptySet));

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
        
        int[] usersFlowsRequirements = new int[g_usersRequirements.getFlows().size()];
        for (int flowIndex = 0; flowIndex < usersFlowsRequirements.length; flowIndex++) {
            int j = 0;
            while (j < g_usersRequirements.getFunctions().size() && g_usersRequirements.getFunctions().get(j).id != flowIndex)
                j++;
            if (j <= g_usersRequirements.getFunctions().size())
                usersFlowsRequirements[flowIndex] = g_usersRequirements.getFlows().get(flowIndex).capabilityRequ;
        }


        for (int flowId = 0; flowId < FxF_ids.length; flowId++)
            model.max(X_rIndicesEquivToFlow[flowId], usersFlowsRequirements, 0, X_capabilityRequirementOfFlow[flowId], false).post();


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

        //X_linksHostingRindex
        SetVar[] X_linksHostingRindex = model.setVarArray("X_linksHostingRindex", R_ids.length, new int[]{}, L_ids);
        for (int rIndex = 0; rIndex < R_ids.length; rIndex++) {
            //X_linksHostingR[rIndex]=X_linksHostingFl[X_flowEquivToR[rIndex]];
            model.element(X_flowEquivToRindex[rIndex], X_linksHostingFl, X_linksHostingRindex[rIndex]).post();
        }

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
                        model.allEqual(X_unionParameter[pathIndex][rIndex], X_linksHostingRindex[rIndex]).post();
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
        
        
        
        Map<IntVar, Integer> map =
        	    IntStream.range(0, X_flowEquivToRindex.length)
        	             .boxed()
        	             .collect(Collectors.toMap(i -> X_flowEquivToRindex[i], i -> R_ids[i]));
        
        /*
        Map<IntVar, Integer> map = new HashMap();
        for (int i=0; i<X_flowEquivToRindex.length; i++)
        	map.put(X_flowEquivToRindex[i], R_ids[i]);
        */
        
        Solver solver = model.getSolver();
        
        AbstractStrategy stra1 = Search.lastConflict(
                new StrategiesSequencer(
                        Search.minDomLBSearch(X_flowEquivToRindex),
                        Search.minDomLBSearch(X_index_linksHostingFl),
                        Search.minDomLBSearch(X_serverHostingF),
                        Search.minDomLBSearch(X_capabilityRequirementOfFlow)
                )
        );
        
        AbstractStrategy stra2 = Search.lastConflict(
                new StrategiesSequencer(
                   		
                		Search.intVarSearch(
                				
            				variables -> Arrays.stream(variables)
            		          .filter(v -> !v.isInstantiated())
            		          .findAny()
            		          .orElse(null),	
            					  
            			    var -> {
            			        int value = defaultFlow(var, map);
            			        if(var.contains(value)){
            			            return value;
            			        }else{
            			            return var.getLB();
            			        }
            			    },

            			    
            			    DecisionOperatorFactory.makeIntEq(),
            			    X_flowEquivToRindex
            			    ),
                		Search.minDomLBSearch(X_flowEquivToRindex),
                        Search.minDomLBSearch(X_index_linksHostingFl),
                        Search.minDomLBSearch(X_serverHostingF),
                        Search.minDomLBSearch(X_capabilityRequirementOfFlow)
                       
                )
        );
        
        
        
        
  /*      
        solver.setSearch(
                Search.lastConflict(
                        new StrategiesSequencer(
           		
                        		Search.intVarSearch(
                        				
	                				variables -> Arrays.stream(variables)
	                		          .filter(v -> !v.isInstantiated())
	                		          .findAny()
	                		          .orElse(null),	
                    					  
                    			    var -> defaultFlow(var, map),
                    			    DecisionOperatorFactory.makeIntEq(),
                    			    X_flowEquivToRindex
                    			    ),
     
                                Search.minDomLBSearch(X_flowEquivToRindex),
                                Search.minDomLBSearch(X_index_linksHostingFl),
                                Search.minDomLBSearch(X_serverHostingF),
                                Search.minDomLBSearch(X_capabilityRequirementOfFlow)
                                
//                                Search.minDomLBSearch(ArrayUtils.append(X_flowEquivToRindex,X_index_linksHostingFl,X_serverHostingF,X_capabilityRequirementOfFlow))
                        )
                )
        );*/
        
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
        System.out.println("Time & computeFootprint & networkFootprint & nbMutualisations");
        
       boolean firstSolution = true;
       CalculationResult result = new CalculationResult();
       
       model.getSolver().setSearch(stra1);
       
       
        
       while(solver.solve()) {
           //System.out.printf("#sol: %s (%.3fs)\n", solver.getSolutionCount(), solver.getTimeCount());
           //System.out.printf("computeFootprint: %d\n", X_computeFootprint.getValue());
           //System.out.printf("networkFootprint: %d\n", X_networkFootprint.getValue());
       	
       	/*
           for (int rFlowIndex = 0; rFlowIndex < R_ids.length; rFlowIndex++) {
           	System.out.println("X_incommingRflowsToRflow["+rFlowIndex+"] = "+X_incommingRflowsToRflow[rFlowIndex].getValue());
           }*/
       
       	/*
       	for (int rFlowIndex = 0; rFlowIndex < R_ids.length; rFlowIndex++) {
           	for (int flow = 0; flow < FxF_ids.length; flow++) {
           		if (X_incommingFlowTo_flowEquivToRflow[rFlowIndex][flow].getValue()!=0)
           			System.out.print("* "+X_flowEquivToRindex[rFlowIndex].getValue()+":"+flow+", ");
           	}
           }
           
           System.out.println("** X_incommingRflowsToRflow[11] = "+X_incommingRflowsToRflow[11].getValue());
           System.out.println("** X_flowEquivToRindex[11] = "+X_flowEquivToRindex[11].getValue());
           System.out.println("** X_incommingFlowTo_flowEquivToRflow[11][4]= "+X_incommingFlowTo_flowEquivToRflow[11][4].getValue());
           System.out.println("** X_rIndicesEquivToFlow[4]= "+X_rIndicesEquivToFlow[4].getValue());
           System.out.println("** intersectionResult[11][4]= "+intersectionResult[11][4].getValue());
           System.out.println("** X_incommingFlowTo_flowEquivToRflow[11][67]= "+X_incommingFlowTo_flowEquivToRflow[11][67].getValue());
           System.out.println("** X_rIndicesEquivToFlow[67]= "+X_rIndicesEquivToFlow[67].getValue());
           System.out.println("** intersectionResult[11][67]= "+intersectionResult[11][67].getValue());
           */
           //Number of mutualisations
           int nbMutualisations=0;
           for (int flowId=0; flowId<X_rIndicesEquivToFlow.length; flowId++){
           	if (X_rIndicesEquivToFlow[flowId].getCard().getValue()>=2)
           		nbMutualisations=nbMutualisations+combination(X_rIndicesEquivToFlow[flowId].getCard().getValue(),2);
           }
           
/*
           String X_str_serviceFunctions = X_serviceFunctions.getValue().toString();
         System.out.println("X_serviceFunctions : " + X_serviceFunctions.getValue().toString());
         for (int functionId = 0; functionId < F_ids.length; functionId++)
             if (X_str_serviceFunctions.contains(Integer.toString(functionId)))
                 System.out.println("X_serverHostingF[" + functionId + "] = " + X_serverHostingF[functionId].getValue());
    
         
         
         
         
           for (int flowId=0; flowId<X_rIndicesEquivToFlow.length; flowId++){
           	if(X_rIndicesEquivToFlow[flowId].getCard().getValue()!=0)
           		System.out.println(flowId+" : "+X_rIndicesEquivToFlow[flowId].getCard().getValue()+":"+X_rIndicesEquivToFlow[flowId].getValue());
           }
*/            
           //System.out.printf("Number of mutualisations: %d \n",nbMutualisations);
           
           time.add(solver.getTimeCount());
           networkFootprint.add(X_networkFootprint.getValue());
           computeFootprint.add(X_computeFootprint.getValue());
           mutualisation.add(nbMutualisations);
           
           System.out.println(solver.getTimeCount()+" & "+X_computeFootprint.getValue()+" & "+X_networkFootprint.getValue()+" & "+nbMutualisations);
           if (firstSolution){
           	result.firstSolution_time = solver.getTimeCount();
           	result.firstSolution_computeFootprint = X_computeFootprint.getValue();
           	result.firstSolution_networkFootprint = X_networkFootprint.getValue();
           	result.firstSolution_nbMutualisations = nbMutualisations;
           	result.lastSolution_time = solver.getTimeCount();
           	result.lastSolution_computeFootprint = X_computeFootprint.getValue();
           	result.lastSolution_networkFootprint = X_networkFootprint.getValue();
           	result.lastSolution_nbMutualisations = nbMutualisations;
           	firstSolution = false;
           }
           else{
           	result.lastSolution_time = solver.getTimeCount();
           	result.lastSolution_computeFootprint = X_computeFootprint.getValue();
           	result.lastSolution_networkFootprint = X_networkFootprint.getValue();
           	result.lastSolution_nbMutualisations = nbMutualisations;
           }
           	
           
           
           if (minFP > X_footprint.getValue()) minFP = X_footprint.getValue();
           if (maxFP < X_footprint.getValue()) maxFP = X_footprint.getValue();
           //System.out.printf("[%d, %d]\n", minFP, maxFP);
//           for (IntVar v : model.retrieveIntVars(false)) {
//               System.out.printf("%s\n", v);
//           }
//           for (SetVar v : model.retrieveSetVars()) {
//               System.out.printf("%s\n", v);
//           }
//           for(IntVar v : X_serverHostingF){
//               assert v.isInstantiated();
//           }
           //System.out.println(solution.toString().replaceAll(", X", ", \n X"));
//           String X_str_serviceFunctions = X_serviceFunctions.getValue().toString();
//           System.out.println("X_serviceFunctions : " + X_serviceFunctions.getValue().toString());
//           for (int functionId = 0; functionId < F_ids.length; functionId++)
//               if (X_str_serviceFunctions.contains(Integer.toString(functionId)))
//                   System.out.println("X_serverHostingF[" + functionId + "] = " + X_serverHostingF[functionId].getValue());
//           System.out.println();
//           System.out.println("X_serviceFlows : " + X_serviceFlows.getValue().toString());
//           for (int flowId = 0; flowId < FxF_ids.length; flowId++)
//               if ((X_linksHostingFl[flowId].getValue().toString().contentEquals("{}")) == false)
//                   System.out.println("X_linksHostingFl[" + flowId + "] = " + X_linksHostingFl[flowId].getValue());
//	    	/**/
//           System.out.println();
//	    	for (int flowId = 0; flowId < X_flowEquivToRindex.length; flowId++)
//               System.out.println("X_flowEquivToRindex[" + flowId + "] = " + X_flowEquivToRindex[flowId].getValue());
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
//        solver.printStatistics();
        //String str = "T"+usersRequirementsId+"(x)= ";
        String networkFootprintCurve = "M_networkFootrpint_"+infrastructureId+usersRequirementsId+"(x)= ";
        String computeFootprintCurve = "M_computeFootrpint_"+infrastructureId+usersRequirementsId+"(x)= ";
        //String computeMutualisationLabels = "";
        //String networkMutualisationLabels = "";
        
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