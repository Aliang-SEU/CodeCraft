package com.algorithm.arima;

public class ARMAMath
{
	public double avgData(double[] dataArray)
	{
		return this.sumData(dataArray)/dataArray.length;
	}
	
	public double sumData(double[] dataArray)
	{
		double sumData=0;
		for(int i=0;i<dataArray.length;i++)
		{
			sumData+=dataArray[i];
		}
		return sumData;
	}
	
	public double stderrData(double[] dataArray)
	{
		return Math.sqrt(this.varerrData(dataArray));
	}
	
	public double varerrData(double[] dataArray)
	{
		double variance=0;
		double avgsumData=this.avgData(dataArray);
		
		for(int i=0;i<dataArray.length;i++)
		{
			dataArray[i]-=avgsumData;
			variance+=dataArray[i]*dataArray[i];
		}
		return variance/dataArray.length;//variance error;
	}

	public double[] autocorData(double[] dataArray,int order)
	{
		double[] autoCor=new double[order+1];
		double varData=this.varerrData(dataArray);//��׼������ķ���
		
		for(int i=0;i<=order;i++)
		{
			autoCor[i]=0;
			for(int j=0;j<dataArray.length-i;j++)
			{
				autoCor[i]+=dataArray[j+i]*dataArray[j];
			}
			autoCor[i]/=dataArray.length;
			autoCor[i]/=varData;
		}
		return autoCor;
	}
	
	public double[] autocorGrma(double[] dataArray,int order)
	{
		double[] autoCor=new double[order+1];
		for(int i=0;i<=order;i++)
		{
			autoCor[i]=0;
			for(int j=0;j<dataArray.length-i;j++)
			{
				autoCor[i]+=dataArray[j+i]*dataArray[j];
			}
			autoCor[i]/=(dataArray.length-i);
			
		}
		return autoCor;
	}
	

	public double[] parautocorData(double[] dataArray,int order)
	{
		double parautocor[]=new double[order];
		
		for(int i=1;i<=order;i++)
	    {
			parautocor[i-1]=this.parcorrCompute(dataArray, i,0)[i-1];
	    }
		return parautocor;
	}

	public double[][] toplize(double[] dataArray,int order)
	{//����toplize��ά����
		double[][] toplizeMatrix=new double[order][order];
		double[] atuocorr=this.autocorData(dataArray,order);

		for(int i=1;i<=order;i++)
		{
			int k=1;
			for(int j=i-1;j>0;j--)
			{
				toplizeMatrix[i-1][j-1]=atuocorr[k++];
			}
			toplizeMatrix[i-1][i-1]=1;
			int kk=1;
			for(int j=i;j<order;j++)
			{
				toplizeMatrix[i-1][j]=atuocorr[kk++];
			}
		}
		return toplizeMatrix;
	}


	public double[] getMApara(double[] autocorData,int q)
	{
		double[] maPara=new double[q+1];
		double[] tempmaPara=maPara;
		double temp=0;
		boolean iterationFlag=true;

		maPara[0]=1;
		while(iterationFlag)
		{
			for(int i=1;i<maPara.length;i++)
			{
				temp+=maPara[i]*maPara[i];
			}
			tempmaPara[0]=autocorData[0]/(1+temp);
		
			for(int i=1;i<maPara.length;i++)
			{
				temp=0;
				for(int j=1;j<maPara.length-i;j++)
				{
					temp+=maPara[j]*maPara[j+i];
				}
				tempmaPara[i]=-(autocorData[i]/maPara[0]-temp);
			}
			iterationFlag=false;
			for(int i=0;i<maPara.length;i++)
			{
				if(maPara[i]!=tempmaPara[i])
				{
					iterationFlag=true;
					break;
				}
			}
			
			maPara=tempmaPara;
		}
		
		return maPara;
	}

	public double[] parcorrCompute(double[] dataArray,int p,int q)
	{
		double[][] toplizeArray=new double[p][p];//p��toplize����
		
		double[] atuocorr=this.autocorData(dataArray,p+q);//����p+q�׵�����غ���
		double[] autocorrF=this.autocorGrma(dataArray, p+q);//����p+q�׵������ϵ����
		for(int i=1;i<=p;i++)
		{
			int k=1;
			for(int j=i-1;j>0;j--)
			{
				toplizeArray[i-1][j-1]=atuocorr[q+k++];
			}
			toplizeArray[i-1][i-1]=atuocorr[q];
			int kk=1;
			for(int j=i;j<p;j++)
			{
				toplizeArray[i-1][j]=atuocorr[q+kk++];
			}
		}
		
	    double[][] toplizeMatrixinverse = getN(toplizeArray);
		
	    double[] temp=new double[p];
	    for(int i=1;i<=p;i++)
	    {
	    	temp[i-1]=atuocorr[q+i];
	    }
	    double[][] autocorrMatrix = new double[temp.length][1];
	    for(int i=0;i<temp.length;i++)
	    {
	    	autocorrMatrix[i][0]=temp[i];
	    }
		double[][] parautocorDataMatrix=juzhenchen(toplizeMatrixinverse, autocorrMatrix, toplizeMatrixinverse[0].length, autocorrMatrix[0].length, toplizeMatrixinverse.length ); //  [Fi]=[toplize]x[autocorr]';
		
		double[] result=new double[parautocorDataMatrix.length+1];
		for(int i=0;i<parautocorDataMatrix.length;i++)
		{
			result[i]=parautocorDataMatrix[i][0];
		}
		
		//����sigmat2
		double sum2=0;
		for(int i=0;i<p;i++)
			for(int j=0;j<p;j++)
			{
				sum2+=result[i]*result[j]*autocorrF[Math.abs(i-j)];
			}
		result[result.length-1]=autocorrF[0]-sum2; //result�������һ���洢���Ź���ֵ
		
		
			return result;   //����0�е����һ������k�׵�ƫ�����ϵ�� pcorr[k]=����ֵ
	}
	/**
	 * 1  
	 * 求解代数余子式 输入：原始矩阵+行+列 现实中真正的行和列数目
	 */
	public double[][] getDY(double[][] data, int h, int v) {
		int H = data.length;
		int V = data[0].length;
		double[][] newData = new double[H - 1][V - 1];

		for (int i = 0; i < newData.length; i++) {

			if (i < h - 1) {
				for (int j = 0; j < newData[i].length; j++) {
					if (j < v - 1) {
						newData[i][j] = data[i][j];
					} else {
						newData[i][j] = data[i][j + 1];
					}
				}
			} else {
				for (int j = 0; j < newData[i].length; j++) {
					if (j < v - 1) {
						newData[i][j] = data[i + 1][j];
					} else {
						newData[i][j] = data[i + 1][j + 1];
					}
				}

			}
		}
		// System.out.println("---------------------代数余子式测试.---------------------------------");
		// for(int i=0;i<newData.length;i++){
		// for(int j=0;j<newData[i].length;j++){
		// System.out.print("newData["+i+"]"+"["+j+"]="+newData[i][j]+"   ");
		// }
		//
		// System.out.println();
		// }

		return newData;
	}
	/**
	 * 求解行列式的模----------->最终的总结归纳
	 * 
	 * @param data
	 * @return
	 */
	public double getHL(double[][] data) {

		// 终止条件
		if(data.length == 1)
			return data[0][0];
		if (data.length == 2) {
			return data[0][0] * data[1][1] - data[0][1] * data[1][0];
		}

		double total = 0;
		// 根据data 得到行列式的行数和列数
		int num = data.length;
		// 创建一个大小为num 的数组存放对应的展开行中元素求的的值
		double[] nums = new double[num];

		for (int i = 0; i < num; i++) {
			if (i % 2 == 0) {
				nums[i] = data[0][i] * getHL(getDY(data, 1, i + 1));
			} else {
				nums[i] = -data[0][i] * getHL(getDY(data, 1, i + 1));
			}
		}
		for (int i = 0; i < num; i++) {
			total += nums[i];
		}
		//System.out.println("total=" + total);
		return total;
	}
	/**
	 * 求解逆矩阵------>z最后的总结和归纳
	 * 
	 * @param toplizeArray
	 * @return
	 */
	public double[][] getN(double[][] toplizeArray) {
		// 先是求出行列式的模|data|
		double A = getHL(toplizeArray);
		// 创建一个等容量的逆矩阵
		double[][] newData = new double[toplizeArray.length][toplizeArray.length];
		if(toplizeArray.length == 1) {
			newData[0][0] = 1.0 / toplizeArray[0][0];
			return newData;
		}
		for (int i = 0; i < toplizeArray.length; i++) {
			for (int j = 0; j < toplizeArray.length; j++) {
				double num;
				if ((i + j) % 2 == 0) {
					num = getHL(getDY(toplizeArray, i + 1, j + 1));
				} else {
					num = -getHL(getDY(toplizeArray, i + 1, j + 1));
				}

				newData[i][j] = num / A;
			}
		}

		// 转置 代数余子式转制
		newData = getA_T(newData);
		// 打印
		/*for (int i = 0; i < toplizeArray.length; i++) {
			for (int j = 0; j < toplizeArray.length; j++) {
				System.out.print("newData[" + i + "][" + j + "]= "
						+ newData[i][j] + "   ");
			}

			System.out.println();
		}*/

		return newData;
	}
	/**
	 * 取得转置矩阵
	 * @param A
	 * @return
	 */
	public double[][] getA_T(double[][] A) {
		int h = A.length;
		int v = A[0].length;
		// 创建和A行和列相反的转置矩阵
		double[][] A_T = new double[v][h];
		// 根据A取得转置矩阵A_T
		for (int i = 0; i < v; i++) {
			for (int j = 0; j < h; j++) {
				A_T[j][i] = A[i][j];
			}
		}
		//System.out.println("取得转置矩阵  wanbi........");
		return A_T;
	}
	 //对A与B相乘的结果用C表示
    private double[][] juzhenchen(double[][] a, double[][] b, int m, int p, int n) {
        double[][] c = new double[m][p];
    	for(int i=0;i<m;i++){
            for(int j=0;j<p;j++){
                c[i][j]=add(a[i],b,j,n);
            }
        }
    	return c;
    }
    //利用累加,得到c[i][j]的值
    private double add(double[] a, double[][] b, int j, int n) {
        double sum=0;
        for(int k=0;k<n;k++)
        {
            sum+=a[k]*b[k][j];
        }
        return sum;
    }
}
