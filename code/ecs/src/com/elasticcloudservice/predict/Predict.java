package com.elasticcloudservice.predict;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import com.algorithm.PutFlavors;
import com.algorithm.Score;
import com.algorithm.Tools;
import com.basic.data.Flavor;
import com.basic.data.Server;
import com.filetool.main.Main;

public class Predict {
	public static long startTime;
	private static Random rand;
	public static String[] predictVm(String[] ecsContent, String[] inputContent)  {

		/** =========do your work here========== **/
		startTime =  System.currentTimeMillis();
		rand = new Random();
		Tools.rand = rand;
		DataProcess solver = new DataProcess();
		solver.parseInput(inputContent);
		solver.parseTrain(ecsContent);
		//返回处理后的键值对 //
		Map<String, Integer> flavorNum = solver.dataPredictModel4();

		//保存对应虚拟机的型号
		LinkedHashMap<String, Flavor> VMMap = solver.getVMMap();
		LinkedList<Flavor> list = new LinkedList<Flavor>(VMMap.values());
		Collections.reverse(list);
		//服务器
		Server[] serverArray=solver.getServers();
		Server server = serverArray[0];
		//预测类型
		Boolean cpuOrMem=solver.cpuOrMem;
		
		//ArrayList<Server> servers=PutFlavors.putFlavorsToServersOld(flavorNum, VMMap, server.type, server.total_cpu,server.total_mem, cpuOrMem);
		Map<String, ArrayList<Server>> servers= PutFlavors.putFlavorsToServers(flavorNum, VMMap, serverArray);
		
		//动态调整
		/*double thresh = 0.0;
		Server tempServer = servers.get(servers.size() - 1);
		if((cpuOrMem == true && tempServer.cpuUsage() >= thresh)
				|| (cpuOrMem == false && tempServer.memUsage() >= thresh) ) {
			if(tempServer.left_cpu > 0 && tempServer.left_mem > 0) {
				for(Flavor tempFlavor : list) {
					while(tempServer.addFlavor(tempFlavor)){
						flavorNum.put(tempFlavor.name, flavorNum.get(tempFlavor.name) + 1);
					}
				}
			}
			if(servers.size() - 2 >= 0) {
				tempServer = servers.get(servers.size() - 2);
				if(tempServer.left_cpu > 0 && tempServer.left_mem>0) {
					for(Flavor tempFlavor : list) {
						while(tempServer.addFlavor(tempFlavor)){
							flavorNum.put(tempFlavor.name, flavorNum.get(tempFlavor.name) + 1);
						}
					}
				}
			}
		}else if((cpuOrMem == true && tempServer.cpuUsage() < thresh) 
				|| (cpuOrMem == false && tempServer.memUsage() < thresh)) {
			for(Entry<String, Integer> flavor : tempServer.flavorMap.entrySet()){
				flavorNum.put(flavor.getKey(), flavorNum.get(flavor.getKey()) - flavor.getValue());
			}
			servers.remove(servers.size() - 1);
		}
		System.out.println();
		for(Map.Entry<String, Integer> flavor : flavorNum.entrySet()) {
			System.out.println(flavor.getKey() + ":" + flavor.getValue());
		}*/
		
		//动态调整  new
		double thresh = 0.0;
		double cpuThresh = 0.0;
		double memThresh = 0.0;
		for(Map.Entry<String , ArrayList<Server>> entry: servers.entrySet()) {
			ArrayList<Server> arrayList = entry.getValue();
			Server tempServer = arrayList.get(entry.getValue().size() - 1);
			if(tempServer.cpuUsage() >= thresh) {
				if(tempServer.left_cpu > cpuThresh && tempServer.left_mem > memThresh) {
					for(Flavor tempFlavor : list) {
						while(tempServer.addFlavor(tempFlavor)) {
							flavorNum.put(tempFlavor.name, flavorNum.get(tempFlavor.name) + 1);
						}
					}
				}
			}
			else {
				for(Entry<String, Integer> flavor : tempServer.flavorMap.entrySet()){
					flavorNum.put(flavor.getKey(), flavorNum.get(flavor.getKey()) - flavor.getValue());
				}
				arrayList.remove(arrayList.size() - 1);
			}
		}
		
		/*Server tempServer = servers.get(servers.size() - 1);
		if((cpuOrMem == true && tempServer.cpuUsage() >= thresh)
				|| (cpuOrMem == false && tempServer.memUsage() >= thresh) ) {
			if(tempServer.left_cpu > 0 && tempServer.left_mem > 0) {
				for(Flavor tempFlavor : list) {
					while(tempServer.addFlavor(tempFlavor)){
						flavorNum.put(tempFlavor.name, flavorNum.get(tempFlavor.name) + 1);
					}
				}
			}
			if(servers.size() - 2 >= 0) {
				tempServer = servers.get(servers.size() - 2);
				if(tempServer.left_cpu > 0 && tempServer.left_mem>0) {
					for(Flavor tempFlavor : list) {
						while(tempServer.addFlavor(tempFlavor)){
							flavorNum.put(tempFlavor.name, flavorNum.get(tempFlavor.name) + 1);
						}
					}
				}
			}
		}else if((cpuOrMem == true && tempServer.cpuUsage() < thresh) 
				|| (cpuOrMem == false && tempServer.memUsage() < thresh)) {
			for(Entry<String, Integer> flavor : tempServer.flavorMap.entrySet()){
				flavorNum.put(flavor.getKey(), flavorNum.get(flavor.getKey()) - flavor.getValue());
			}
			servers.remove(servers.size() - 1);
		}
		System.out.println();*/
		for(Map.Entry<String, Integer> flavor : flavorNum.entrySet()) {
			System.out.println(flavor.getKey() + ":" + flavor.getValue());
		}
		
		
		

		//计算分数  李永彬
		if(Main.debug == true) {
			String filePath = "E:\\Desktop\\test\\test2\\trainDataIndexByData.txt";
			Map<String, Integer> testFlavors = TestDataProcess.read(filePath, solver.predictStartTime.substring(0,10), solver.predictEndTime.substring(0,10));
			ArrayList<String> test = Score.getScore(servers, flavorNum, VMMap, testFlavors);
			FileWriter fw;
			try {
				fw = new FileWriter("E:\\Desktop\\test\\test2\\analysis.txt");
				for(String s : test)
					fw.write(s + "\r\n");
				fw.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		
		
		
		
		/**
		 * 输出结果
		 */
		ArrayList<String> arrayList = new ArrayList<>();
		PutFlavors.getResult(arrayList, flavorNum, servers);
		//PutFlavors.getResultOld(arrayList, flavorNum, servers);
		String[] results = new String[arrayList.size()];
		for(int i = 0; i < results.length; i++)
			results[i] = arrayList.get(i);
		return results;

	}
}
