package com.basic.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * 服务器类
 * @author hzl
 *
 */
public class Server implements Comparable<Server>{
	public String type;
	public int total_cpu;
	public int total_mem;
	public int left_cpu;
	public int left_mem;
	public Map<String, Integer> flavorMap=new HashMap<>();
	
	
	
	@Override
	public int compareTo(Server o) {
		// TODO Auto-generated method stub
		if((double)total_cpu/(double)total_mem < (double)o.total_cpu/(double)o.total_mem)
			return -1;
		else if((double)total_cpu/(double)total_mem > (double)o.total_cpu/(double)o.total_mem)
			return 1;
		return 0;
	}



	public Server(String type, int total_cpu, int total_mem) {
		super();
		this.type = type;
		this.total_cpu = total_cpu;
		this.total_mem = total_mem;
		this.left_cpu = total_cpu;
		this.left_mem = total_mem;
	}
	
	

	public Server(String type, int total_cpu, int total_mem, Map<String, Integer> flavorMap) {
		super();
		this.type = type;
		this.total_cpu = total_cpu;
		this.total_mem = total_mem;
		this.left_cpu = total_cpu;
		this.left_mem = total_mem;
		this.flavorMap = flavorMap;
	}



	public Server(int total_cpu, int total_mem) {
		super();
		this.total_cpu = total_cpu;
		this.total_mem = total_mem;
		this.left_cpu = total_cpu;
		this.left_mem = total_mem;
	}
	
	public Server(int cpu, int mem, ArrayList<Flavor> flavor_list) {
		super();
		this.total_cpu = cpu;
		this.total_mem = mem;
		this.left_cpu = cpu;
		this.left_mem = mem;
	}
	
	public boolean addFlavor(Flavor flavor) {
		if(left_cpu>=flavor.cpu&&left_mem>=flavor.mem)
		{
			if(flavorMap.containsKey(flavor.name))
				flavorMap.put(flavor.name, flavorMap.get(flavor.name)+1);
			else
				this.flavorMap.put(flavor.name,1);
			this.left_cpu -= flavor.cpu;
			this.left_mem -= flavor.mem;
			return true;
		}
		else
			return false;
		
	}
	/**
	 * CPU的利用率
	 * @return
	 */
	public double cpuUsage() {
		return 1.0 - left_cpu / (double)total_cpu;
	}
	/**内存利用率
	 * 
	 * @return
	 */
	public double memUsage() {
		return 1.0 - left_mem / (double)total_mem;
	}



	@Override
	public String toString() {
		return "Server [type=" + type + ", total_cpu=" + total_cpu + ", total_mem=" + total_mem + ", left_cpu="
				+ left_cpu + ", left_mem=" + left_mem + ", flavorMap=" + flavorMap + "]";
	}
	

	
}
