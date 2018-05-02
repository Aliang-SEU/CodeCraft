package com.algorithm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.basic.data.Flavor;
import com.basic.data.Server;

public class Score {
	public static ArrayList<String> getScoreOld(ArrayList<Server> servers, Boolean isCpuOrMem,Map<String, Integer> expectFlavors,HashMap<String, Flavor> VMMap,Map<String, Integer> realityFlavors)
	{	
		if(servers.isEmpty())
		{
			throw new RuntimeException("服务器台数为0");
		}
		ArrayList<String> testResult=new ArrayList<>();
		double kindOfFlavors=expectFlavors.size();
		double sumOfFlavorsUsage=0;
		double varianceOfFlavors=0;
		double squareOfExcepctFlavors=0;
		double squareOfRealityFlavors=0;
		
		Map<String,Integer> testMap=new HashMap<>();
		
		for(Map.Entry<String, Integer> entry:expectFlavors.entrySet())
		{
			squareOfExcepctFlavors+=Math.abs(entry.getValue());
			squareOfRealityFlavors+=Math.abs(realityFlavors.get(entry.getKey()));
			varianceOfFlavors+=Math.abs(entry.getValue()-realityFlavors.get(entry.getKey()));
			testMap.put(entry.getKey(),entry.getValue()-realityFlavors.get(entry.getKey()));
			if(isCpuOrMem)
			    sumOfFlavorsUsage+=entry.getValue()*VMMap.get(entry.getKey()).cpu;
			else
				sumOfFlavorsUsage+=entry.getValue()*VMMap.get(entry.getKey()).mem;
		}
		double expectSorce=1-Math.sqrt(varianceOfFlavors/kindOfFlavors)/
				(Math.sqrt(squareOfExcepctFlavors/kindOfFlavors)+Math.sqrt(squareOfRealityFlavors/kindOfFlavors));
		double putFlavorsSorce=0;
		if(isCpuOrMem)
			putFlavorsSorce=sumOfFlavorsUsage/(servers.get(0).total_cpu*servers.size());
		else 
			putFlavorsSorce=sumOfFlavorsUsage/(servers.get(0).total_mem*servers.size());
		double sorce= expectSorce*putFlavorsSorce*100;
		testResult.add(expectFlavors.size()+"");
		for(int i=1;i<=15;i++)
		{
			if(testMap.containsKey("flavor"+i))
				testResult.add("flavor"+i+"   "+expectFlavors.get("flavor"+i)+"   "+realityFlavors.get("flavor"+i)+"  "+ testMap.get("flavor"+i));
		}
		testResult.add("");
		testResult.add(servers.size()+"");
		for(Server server:servers)
		{
			testResult.add("free_cpu="+server.left_cpu+"  "+"free_mem="+server.left_mem);
		}
		testResult.add("\n预测效率: "+expectSorce);
		testResult.add("放置效率: "+putFlavorsSorce);
		testResult.add("分数=: "+sorce);
		return testResult;
	}
	public static ArrayList<String> getScore(Map<String,ArrayList<Server>> servers, Map<String, Integer> expectFlavors,HashMap<String, Flavor> VMMap,Map<String, Integer> realityFlavors)
	{	
		if(servers.isEmpty())
		{
			throw new RuntimeException("服务器台数为0");
		}
		ArrayList<String> testResult=new ArrayList<>();
		double kindOfFlavors=expectFlavors.size();
		double sumOfFlavorsCPUUsage=0;
		double sumOfFlavorsMEMUsage=0;
		double varianceOfFlavors=0;
		double squareOfExcepctFlavors=0;
		double squareOfRealityFlavors=0;
		
		Map<String,Integer> testMap=new HashMap<>();
		
		for(Map.Entry<String, Integer> entry:expectFlavors.entrySet())
		{
			squareOfExcepctFlavors+=Math.abs(entry.getValue());
			squareOfRealityFlavors+=Math.abs(realityFlavors.get(entry.getKey()));
			varianceOfFlavors+=Math.abs(entry.getValue()-realityFlavors.get(entry.getKey()));
			testMap.put(entry.getKey(),entry.getValue()-realityFlavors.get(entry.getKey()));
			sumOfFlavorsCPUUsage+=entry.getValue()*VMMap.get(entry.getKey()).cpu;
		    sumOfFlavorsMEMUsage+=entry.getValue()*VMMap.get(entry.getKey()).mem;
		}
		double expectSorce=1-Math.sqrt(varianceOfFlavors/kindOfFlavors)/
				(Math.sqrt(squareOfExcepctFlavors/kindOfFlavors)+Math.sqrt(squareOfRealityFlavors/kindOfFlavors));
		double putFlavorsSorce=0;
		
		double sumOfServersCPUUsage = 0;
		double sumOfServersMEMUsage = 0;
		
		for(Map.Entry<String, ArrayList<Server>> map : servers.entrySet()) {
			ArrayList<Server> serverList = map.getValue();
			for(Server server : serverList) {
				sumOfServersCPUUsage += server.total_cpu;
				sumOfServersMEMUsage += server.total_mem;
			}
		}
		double cpuPutFlavorsSorce = sumOfFlavorsCPUUsage / sumOfServersCPUUsage;
		double memPutFlavorsSorce = sumOfFlavorsMEMUsage / sumOfServersMEMUsage;
		
		putFlavorsSorce=(cpuPutFlavorsSorce + memPutFlavorsSorce) / 2;
		double sorce= expectSorce*putFlavorsSorce*100;
		testResult.add(expectFlavors.size()+"");
		for(int i=1;i<=18;i++)
		{
			if(testMap.containsKey("flavor"+i))
				testResult.add("flavor"+i+"   "+expectFlavors.get("flavor"+i)+"   "+realityFlavors.get("flavor"+i)+"  "+ testMap.get("flavor"+i));
		}
		testResult.add("");
		
		int sum = 0;
		for(Map.Entry<String, ArrayList<Server>> map : servers.entrySet()) {
			ArrayList<Server> serverList = map.getValue();
			sum += serverList.size();
		}
		testResult.add(sum+"");
		
		for(Map.Entry<String, ArrayList<Server>> map : servers.entrySet()) {
			ArrayList<Server> serverList = map.getValue();
			int i=0;
			for(Server server : serverList) {
				i++;
				testResult.add(map.getKey() + "-" + i + " free_cpu="+server.left_cpu+"  "+"free_mem="+server.left_mem);
			}
		}
		testResult.add("");
		testResult.add("\n预测效率: " + expectSorce);
		testResult.add("CPU放置效率: " + cpuPutFlavorsSorce);
		testResult.add("MEM放置效率: " + memPutFlavorsSorce);
		testResult.add("放置效率: " + putFlavorsSorce);
		testResult.add("分数=: " + sorce);
		return testResult;
	}
}
