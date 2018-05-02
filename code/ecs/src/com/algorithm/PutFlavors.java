package com.algorithm;

import com.basic.data.*;
import com.elasticcloudservice.predict.Predict;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * author by 李永彬
 * @author hzl
 *
 */

public class PutFlavors {
	private static Random rand1 = new Random();
	
	public static Map<String,ArrayList<Server>> putFlavorsToServers(Map<String,Integer>  mapPredictNumFlavors, Map<String, Flavor> mapFlavorCpuMem, Server[] servers)
	{	
		Arrays.sort(servers);
		int sumOfFlavors = 0;
		for(Map.Entry<String, Integer> entry : mapPredictNumFlavors.entrySet()) {		
			sumOfFlavors += entry.getValue();			
		}
		String[] flavorIds = new String[sumOfFlavors];
		ArrayList<Flavor> flavors=new ArrayList<>();
		for(Map.Entry<String, Integer> entry:mapPredictNumFlavors.entrySet())
		{		
			int numFlavor=entry.getValue();
			while(numFlavor--!=0)
				flavors.add(mapFlavorCpuMem.get(entry.getKey()));		
		}
		Collections.sort(flavors);
		int i = 0;
		for(Flavor flavor : flavors) {
			flavorIds[i++] = flavor.name+ "_" + rand1.nextInt(servers.length);
		}
		
		/*int i = 0;
		for(Map.Entry<String, Integer> entry : mapPredictNumFlavors.entrySet()) {
			int numFlavor = entry.getValue();
			while(numFlavor-- != 0) {
				int putType = rand1.nextInt(servers.length);
				//System.out.println(mapFlavorCpuMem.get(entry.getKey()).name+ "_" + putType);
				flavorIds[i++] = mapFlavorCpuMem.get(entry.getKey()).name+ "_" + putType;
			}
		}*/
		double evaluation = 0; //评价函数
		double cpuEvaluation =0;
		double memEvaluation =0;
		
		double cpuFlavors = 0;
		double memFlavors = 0;
		
		for(String type :flavorIds) {
			int end = type.indexOf("_");
			Flavor flavor = mapFlavorCpuMem.get(type.substring(0, end));
			cpuFlavors += flavor.cpu;
			memFlavors += flavor.mem;
		}
		
		//结果
		Map<String,ArrayList<Server>> resServerMap = new HashMap<>();
		
		final double initT = 1000000.0;
		double T = initT;
		double Tmin = 0.01;
		double r = 0.999999;
		long startTime = Predict.startTime;//限时
		String[] curFlavorIds = new String[sumOfFlavors];
		int varType = 0; //变异类型
		int iter = 0;
		while(T>Tmin)
		{
			System.arraycopy(flavorIds, 0, curFlavorIds, 0, flavorIds.length);
			varType = rand1.nextInt(2);
			
			if(varType < 2) {
				//随机交换顺序变异
				if(curFlavorIds.length >= 2)
				{
					int random1 = rand1.nextInt(curFlavorIds.length);
					int random2 = rand1.nextInt(curFlavorIds.length);
					while(random2 == random1 || (curFlavorIds[random1].equals(curFlavorIds[random2]))) {
						random2 = rand1.nextInt(curFlavorIds.length);
					}
					String temp = curFlavorIds[random1];
					curFlavorIds[random1] = curFlavorIds[random2];
					curFlavorIds[random2] = temp;
				}
			}
			else {
				//服务器类型变异
				//int varNum = rand1.nextInt(curFlavorIds.length);
				int varNum = 1;
				for(int k = 0; k < varNum; k++) {
					int varId = rand1.nextInt(curFlavorIds.length);
					int end = curFlavorIds[varId].indexOf("_");
					curFlavorIds[varId] = curFlavorIds[varId].substring(0, end) + "_" + rand1.nextInt(servers.length);		
						
				}
			}
			
			Map<String,ArrayList<Server>> serverMap = new HashMap<>();
			
			//可变类型放置
			
			if(serverMap.size() == 0) {
				int putServerId = (servers.length-1)/2;
				ArrayList<Server> list = new ArrayList<>();
				list.add(new Server(servers[putServerId].type, servers[putServerId].total_cpu, servers[putServerId].total_mem));
				serverMap.put(servers[putServerId].type, list);
			}
			int index = 0;	
			for(String type : curFlavorIds) {
				int putServerId = type.charAt(type.length() - 1) - '0';
				int end = type.indexOf("_");
				Flavor flavor = mapFlavorCpuMem.get(type.substring(0, end));
				boolean flag = false; //是否可放置
				for(Map.Entry<String, ArrayList<Server>> entry : serverMap.entrySet()) {
					if(flag)
						break;
					ArrayList<Server> list = entry.getValue();
					for(int j = 0; j < list.size(); j++) {
						Server curServer = list.get(j);
						if(curServer.addFlavor(flavor)) {
							int k = 0;
							for(; k < servers.length; k++) {
								if(servers[k].type.equals(entry.getKey())) 
									break;					
							}
							curFlavorIds[index] = flavor.name + "_" +  k;
							flag = true;
							break;
						}
					}		
				}
				if(!flag) {
					int id = 0;
					/*if(cpuEvaluation <= memEvaluation) {
						id = rand1.nextInt(servers.length - (servers.length -1) / 2 -1) + (servers.length -1) / 2 +1;
					}
					else {
						id = rand1.nextInt( (servers.length - 1) / 2 + 1);
					}*/
					id = rand1.nextInt(servers.length);	
					int n= rand1.nextInt(1000);
					if(n > 0)
					   id = putServerId;
					Server newServer = new Server(servers[id].type, servers[id].total_cpu, servers[id].total_mem);
					if(newServer.addFlavor(flavor)) {
						boolean f = false;//是否包含
						for(Map.Entry<String, ArrayList<Server>> entry : serverMap.entrySet()) {
							if(servers[id].type.equals(entry.getKey())) {
								ArrayList<Server> list = entry.getValue();
								list.add(newServer);
								f=true;
								break;
							}
						}
						if(!f) {
							ArrayList<Server> list =new ArrayList<>();
							list.add(newServer);
							serverMap.put(servers[id].type, list);
						}
						curFlavorIds[index] = flavor.name + "_" +  id;
					}			
				}
				index++;
				/*if(!serverMap.containsKey(servers[putServerId].type)) {
					ArrayList<Server> list = new ArrayList<>();
					list.add(new Server(servers[putServerId].type, servers[putServerId].total_cpu, servers[putServerId].total_mem));
					serverMap.put(servers[putServerId].type, list);
				}*/
				
				
			}
			
			/*for(String type : curFlavorIds) {
				int putServerId = type.charAt(type.length() - 1) - '0';
				int end = type.indexOf("_");
				Flavor flavor = mapFlavorCpuMem.get(type.substring(0, end));
				if(!serverMap.containsKey(servers[putServerId].type)) {
					ArrayList<Server> list = new ArrayList<>();
					list.add(new Server(servers[putServerId].type, servers[putServerId].total_cpu, servers[putServerId].total_mem));
					serverMap.put(servers[putServerId].type, list);
				}
				ArrayList<Server> list = serverMap.get(servers[putServerId].type);
				int j = 0;
				for(; j < list.size(); j++) {
					Server curServer = list.get(j);
					if(curServer.addFlavor(flavor)) {
						break;
					}
				}
				if(j == list.size()) {
					Server newServer = new Server(servers[putServerId].type, servers[putServerId].total_cpu, servers[putServerId].total_mem);
					if(newServer.addFlavor(flavor))
						list.add(newServer);
				}
			}*/
			
			double curEvaluation = 0;
			
			double cpuServers = 0;
			double memServers = 0;
			for(Map.Entry<String, ArrayList<Server>> entry: serverMap.entrySet()) {
				ArrayList<Server> serverList = entry.getValue();
				for(Server server : serverList) {
					cpuServers += server.total_cpu;
					memServers += server.total_mem;
				}
			}
			
			double curCpuEvaluation = cpuFlavors/cpuServers;
			double curMemEvaluation = memFlavors/memServers;
			curEvaluation = cpuFlavors/cpuServers + memFlavors/memServers;

			if(curEvaluation > evaluation ) {
				//System.out.println(curEvaluation/2+"---"+iter);
				//System.out.println(flavorIds.length+ " "+Arrays.toString(flavorIds));
				evaluation = curEvaluation;
				cpuEvaluation = curCpuEvaluation;
				memEvaluation = curMemEvaluation;
				resServerMap = serverMap;
				System.arraycopy(curFlavorIds, 0, flavorIds, 0, curFlavorIds.length);
			}
			else {
				if(Math.exp((curEvaluation - evaluation)/T) > Math.random()) {
					/*System.out.println(curEvaluation+"****"+iter);
					System.out.println(servers);*/
					System.arraycopy(curFlavorIds, 0, flavorIds, 0, curFlavorIds.length);
				}
			}
			T=r*T;
			iter++;
			if(System.currentTimeMillis()-startTime > 85000)
				break;
		}	
		//System.out.println(evaluation/2+"---"+iter);
		//System.out.println(flavorIds.length+ " "+Arrays.toString(flavorIds));
		return resServerMap;		
	}
	/**
	 * 
	 * @param mapPredictNumFlavors
	 * @param mapFlavorCpuMem
	 * @param type
	 * @param serverCpu
	 * @param serverMem
	 * @param cpuOrMem
	 * @return
	 */
	public static ArrayList<Server> putFlavorsToServersOld(Map<String,Integer>  mapPredictNumFlavors,Map<String, Flavor> mapFlavorCpuMem,String type, int serverCpu,int serverMem,boolean cpuOrMem)
	{
		ArrayList<Flavor> flavors=new ArrayList<>();
		for(Map.Entry<String, Integer> entry:mapPredictNumFlavors.entrySet())
		{		
			int numFlavor=entry.getValue();
			while(numFlavor--!=0)
				flavors.add(mapFlavorCpuMem.get(entry.getKey()));		
		}
		
        double evaluation = 0; //评价函数
		
		double cpuFlavors = 0;
		double memFlavors = 0;
		
		for(Flavor flavor :flavors) {
			cpuFlavors += flavor.cpu;
			memFlavors += flavor.mem;
		}
		Collections.sort(flavors);
		ArrayList<Server> resServers=new ArrayList<>();
		final double initT = 1000000.0;
		double T=initT;
		double Tmin=0.011;
		double r=0.99999;
		long startTime=Predict.startTime;//限时
		int iter = 0;
		while(T>Tmin)
		{
			@SuppressWarnings("unchecked")
			ArrayList<Flavor> newFlavors=(ArrayList<Flavor>)flavors.clone();
			if(newFlavors.size()>=2 && T!=initT)
			{
				int random1=rand1.nextInt(newFlavors.size());
				int random2=rand1.nextInt(newFlavors.size());
				while(random2==random1 || newFlavors.get(random1).name.equals(newFlavors.get(random2).name)) {
					random2=rand1.nextInt(newFlavors.size());
				}
				Collections.swap(newFlavors, random1, random2);				
			}
			ArrayList<Server> servers=new ArrayList<>();
			servers.add(new Server(type, serverCpu, serverMem));
			for(Flavor flavor:newFlavors)
			{
				int i=0;
				for(;i<servers.size();i++)
				{
					Server curServer=servers.get(i);
					if(curServer.addFlavor(flavor))
						break;
				}
				if(i==servers.size())
				{
					Server newServer=new Server(type, serverCpu, serverMem);
					if(newServer.addFlavor(flavor))
						servers.add(newServer);
				}
			}
			
            double curEvaluation = 0;
			
			double cpuServers = 0;
			double memServers = 0;
			for(Server server : servers) {
					cpuServers += server.total_cpu;
					memServers += server.total_mem;
			}
			
			curEvaluation = cpuFlavors/cpuServers + memFlavors/memServers;
			
			if(curEvaluation  > evaluation )
			{
				evaluation=curEvaluation;
				resServers=servers;	
				flavors=newFlavors;
			}
			else
			{
				if(Math.exp((curEvaluation - evaluation)/T)>Math.random())
				{
					evaluation=curEvaluation;
					flavors=newFlavors;
				}
			}
			T=r*T;
			System.out.println(evaluation);
			if(System.currentTimeMillis()-startTime > 10000)
				break;
		}	
		return resServers;		
	}
	public static void getResult(ArrayList<String> result,Map<String,Integer> mapPredictNumFlavor,Map<String,ArrayList<Server>> servers)
	{
		int num=0;
		for(Map.Entry<String, Integer> entry:mapPredictNumFlavor.entrySet())
		{
			result.add(entry.getKey()+" "+entry.getValue());
			num+=entry.getValue();
		}
		result.add(0, num+"");
		
		for(Map.Entry<String, ArrayList<Server>> entry : servers.entrySet()) {
			result.add("");
			String type = entry.getKey();
			ArrayList<Server> serverList = entry.getValue();
			result.add(type+" " + serverList.size());
			for(int i = 0; i < serverList.size(); i++)
			{
				String string = type+ "-" + (i + 1)+ "";
				Map<String, Integer> map = serverList.get(i).flavorMap;
				for(Map.Entry<String, Integer> entryFlavor : map.entrySet()) {
					string += " " + entryFlavor.getKey() + " " + entryFlavor.getValue();
				}
				result.add(string);
			}
		}	
	}
	public static void getResultOld(ArrayList<String> result,Map<String,Integer> mapPredictNumFlavor,ArrayList<Server> servers)
	{
		int num=0;
		for(Map.Entry<String, Integer> entry:mapPredictNumFlavor.entrySet())
		{
			result.add(entry.getKey()+" "+entry.getValue());
			num+=entry.getValue();
		}
		result.add(0, num+"");
		result.add("");
		String type = servers.get(0).type;
		result.add(type+" " + servers.size());
		for(int i = 0; i < servers.size(); i++) {
			String string = type+ "-" + (i + 1)+ "";
			Map<String, Integer> map = servers.get(i).flavorMap;
			for(Map.Entry<String, Integer> entryFlavor : map.entrySet()) {
				string += " " + entryFlavor.getKey() + " " + entryFlavor.getValue();
			}
			result.add(string);
		}
	}
}
