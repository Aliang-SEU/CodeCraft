package com.basic.data;

/**
 * 虚拟机类
 * @author hzl
 *
 */
public class Flavor implements Comparable<Flavor>, Cloneable{
	
	private static DimTobeOptimized dim;
	public String name;
	public int id;
	public int cpu;
	public int mem;

	/**
	 * 构造函数
	 * @param name
	 * @param cpu
	 * @param men
	 */
	public Flavor(String name, int cpu, int men) {
		super();
		this.name = name;
		this.id = Integer.parseInt(name.substring(6));
		this.cpu = cpu;
		this.mem = men;
	}

	/**
	 * 按CPU进行排序比较
	 */
	@Override
	public int compareTo(Flavor o) {
		// TODO Auto-generated method stub
		if(dim == DimTobeOptimized.CPU)
			return compCpu(o);
		else if(dim == DimTobeOptimized.MEM)
			return compMem(o);
		else
			return 0;
	}
	/**
	 * 按CPU大小进行排序
	 * @param o
	 * @return
	 */
	private int compCpu(Flavor o) {
		if(this.cpu < o.cpu)
			return 1;
		else if(this.cpu > o.cpu)
			return -1;
		else {
			if(this.mem < o.mem)
				return 1;
			else if(this.mem > o.mem)
				return -1;
			else
				return 0;
		}
	}
	/**
	 * 按mem进行排序
	 * @param o
	 * @return
	 */
	private int compMem(Flavor o) {
		if(this.mem < o.mem)
			return 1;
		else if(this.mem > o.mem)
			return -1;
		else {
			if(this.cpu < o.cpu)
				return 1;
			else if(this.cpu > o.cpu)
				return -1;
			else
				return 0;
		}
	}
	
	public static DimTobeOptimized getDim() {
		return dim;
	}

	public static void setDim(String name) {
		if(name.equals("CPU"))
			Flavor.dim = DimTobeOptimized.CPU;
		else if(name.equals("MEM"))
			Flavor.dim = DimTobeOptimized.MEM;
		else
			throw new RuntimeException("invalid parameter");
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		// TODO Auto-generated method stub
		return super.clone();
	}

	@Override
	public String toString() {
		return "Flavor [name=" + name + ", cpu=" + cpu + ", mem=" + mem + "]";
	}
	
} 

enum DimTobeOptimized{
	CPU(0, "CPU"),
	MEM(1, "MEM");
	
	private int id;
	private String name;
	private DimTobeOptimized(int id, String name) {
		this.id = id;
		this.name = name;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
}