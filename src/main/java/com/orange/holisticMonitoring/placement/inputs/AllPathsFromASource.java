package com.orange.holisticMonitoring.placement.inputs;
/**
 * Print all paths from a given source to a destination - (http://www.geeksforgeeks.org/find-paths-given-source-destination/) 
 * Given a directed graph, a source vertex s and a destination vertex d, print all paths from given s to d.
 * */

import java.util.*;
public class AllPathsFromASource {
	static class Graph{
		public int V;
		Map<Integer, List<Integer>> adj; // Adjacency list
		
		Graph(int v){
			V = v;
			adj = new HashMap<Integer, List<Integer>>();
		}
		
		void addEdge(int u, int v){
			if(!adj.containsKey(u)){
				adj.put(u, new ArrayList<Integer>());
			}
			adj.get(u).add(v);
		}
		
		List<List<Integer>> getAllPaths(int u, int v){
			List<List<Integer>> result = new ArrayList<List<Integer>>();
			if(u==v){
				List<Integer> temp = new ArrayList<Integer>();
				temp.add(u);
				result.add(temp);
				return result;
			}
			boolean[] visited = new boolean[V];
			Deque<Integer> path = new ArrayDeque<Integer>();
			getAllPathsDFS(u, v, visited, path, result);
			return result;
		}
		
		void getAllPathsDFS(int u, int v, boolean[] visited, Deque<Integer> path, List<List<Integer>> result){
			visited[u] = true; // Mark visited
			path.add(u); // Add to the end
			if(u==v){
				result.add(new ArrayList<Integer>(path));
			}
			else{
				if(adj.containsKey(u)){
					for(Integer i : adj.get(u)){
						if(!visited[i]){
							getAllPathsDFS(i, v, visited, path, result);
						}
					}
				}
			}
			path.removeLast();
			visited[u] = false;
		}
	}
	
	public static void main(String[] args){
		Graph g = new Graph(4);
		g.addEdge(0,1);
		g.addEdge(0,2);
		g.addEdge(0,3);
		g.addEdge(2,0);
		g.addEdge(2,1);
		g.addEdge(1,3);
		
		List<List<Integer>> vertexPaths = g.getAllPaths(2,3);
		List<List<Integer>> edgePaths = new ArrayList<List<Integer>>();
		for(List<Integer> vpath : vertexPaths){
			List<Integer> lpath = new ArrayList<Integer>();
			int v1 = -1;
			for (int v2 : vpath)
			{
				 if (v1==-1)
					 v1=v2;
				 else
				 {
					 int l=v1*g.V+v2;
					 lpath.add(l);
					 v1=v2;
				 } 
			}
			edgePaths.add(lpath);
			System.out.println(vpath);
			System.out.println(lpath);
			System.out.println("--------------------------------------------");
		}
	}
	
}











