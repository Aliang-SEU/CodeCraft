package com.algorithm;

import java.util.Random;

public class Tools {
	/**
	 * 获取 a-b范围内的随机数
	 * @param a
	 * @param b
	 * @return
	 */
	public static Random rand;
	public static double rand(double a, double b) {
		return rand.nextDouble() * (b - a) + a;		
	} 

	/**
	 * sigmoid激活函数
	 * @param x
	 * @return
	 */
	public static double sigmoid(double x) {
	    return 0.5 / (1 + Math.exp(-x / (0.5)));
	}
	/**
	 * 反激活函数
	 * @param x
	 * @return
	 */
	public static double sigmodDerivate(double x) {
		return x * (1 - x);
	}
	/**
	 * 反正切函数
	 * @param value
	 * @return
	 */
	public static double tanhDerivative(double value) {
		return (1 - Math.pow(value, 2));
	}

}
