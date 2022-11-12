package project1;

import java.util.ArrayList;
import java.util.Comparator;

public class Algorithm {
	static long start;
	static long end;
	// 储存拉格朗日内插函数或多项式拟合函数
	private static double[] p;
	
	// 储存三次样条插值函数
	private static double[] x;
	private static double[] a;
	private static double[] b;
	private static double[] c;
	private static double[] d;
	
	// 计算多项式拟合函数
	public static void fittingSet(int n, ArrayList<double[]> data) {
		start = System.nanoTime();
		
		int len = data.size();
		double[] x = new double[len];
		double[] y = new double[len];
		for (int i = 0; i < len; i++) {
			x[i] = data.get(i)[0];
			y[i] = data.get(i)[1];
		}
		
		// 构建线性方程组
		double[][] a = new double[n + 1][n + 2];
		for (int i = 0; i < n + 1; i++) {
			for (int j = 0; j < n + 1; j++) {
				for (int k = 0; k < len; k++) {
					a[i][j] += Math.pow(x[k], i + j);
				}
			}
		}
		for (int i = 0; i < n + 1; i++) {
			for (int j = 0; j < len; j++) {
				a[i][n + 1] += y[j] * Math.pow(x[j], i);
			}
		}
		
		// 求解线性方程组(Gaussian Elimination with Backward Substitution)
		for (int i = 0; i < n; i++) {
			int p = i - 1;
			while(a[++p][i] == 0 && p <= n);
			if (p > n) {
				//test
				throw new RuntimeException("no unique solution exists");
			}
			if (p != i) {
				double[] temp = a[p];
				a[p] = a[i];
				a[i] = temp;
			}
			for (int j = i + 1; j <= n; j++) {
				double m = a[j][i] / a[i][i];
				for (int k = 0; k < n + 2; k++) {
					a[j][k] -= m * a[i][k];
				}
			}
		}
		if (a[n][n] == 0) {
			//test
			throw new RuntimeException("no unique solution exists");
		}
		double[] f = new double[n + 1];
		f[n] = a[n][n + 1] / a[n][n];
		for (int i = n - 1; i>= 0; i--) {
			double sigma = 0;
			for (int j = i + 1; j <= n; j++) {
				sigma += a[i][j] * f[j];
			}
			f[i] = (a[i][n + 1] - sigma) / a[i][i];
		}
		
		// 保存结果
		Algorithm.p = f;

		end = System.nanoTime();  
		System.out.println(n + " times " + "fitting runtime:" + (end - start) + "(ns)");
	}
	
	// 计算三次样条插值结果
	public static double spline(double x0) {
		int j = 0;
		if (x0 < x[0]) {
			j = 0;
		} else if (x0 > x[x.length - 1]) {
			j = x.length - 2;
		} else {
			while (x0 > x[j++]);
			j -= 2;
		}
		double result = 0;
		result += d[j];
		result *= x0 - x[j];
		result += c[j];
		result *= x0 - x[j];
		result += b[j];
		result *= x0 - x[j];
		result += a[j];
		return result;
	}
	
	// 计算三次样条插值函数
	public static void splineSet(ArrayList<double[]> data) {
		start = System.nanoTime();
		
		// 对点的坐标进行排序
		data.sort(new Comparator<double[]>() {
			@Override
			public int compare(double[] o1, double[] o2) {
				return Double.compare(o1[0], o2[0]);
			}
		});
		
		int len = data.size();
		double[] x = new double[len];
		double[] y = new double[len];
		for (int i = 0; i < len; i++) {
			x[i] = data.get(i)[0];
			y[i] = data.get(i)[1];
		}
		
		// 求解三对角线性方程组
		int n = len - 1;
		double[] a = y;
		double[] b = new double[n];
		double[] c = new double[n + 1];
		double[] d = new double[n];
		double[] h = new double[n];
		for (int i = 0; i < n; i++) {
			h[i] = x[i + 1] - x[i];
		}
		double[] alpha = new double[n];
		for (int i = 1; i < n; i++) {
			alpha[i] = 3 / h[i] * (a[i + 1] - a[i]) - 3 / h[i - 1] * (a[i] - a[i - 1]);
		}
		double[] l = new double[n + 1];
		double[] u = new double[n + 1];
		double[] z = new double[n + 1];
		l[0] = 1;
		u[0] = 0;
		z[0] = 0;
		for (int i = 1; i < n; i++) {
			l[i] = 2 * (x[i + 1] - x[i - 1]) - h[i - 1] * u[i - 1];
			u[i] = h[i] / l[i];
			z[i] = (alpha[i] - h[i - 1] * z[i - 1]) / l[i];
		}
		l[n] = 1;
		z[n] = 0;
		c[n] = 0;
		for (int j = n - 1; j >= 0; j--) {
			c[j] = z[j] - u[j] * c[j + 1];
			b[j] = (a[j + 1] - a[j]) / h[j] - h[j] * (c[j + 1] + 2 * c[j]) / 3;
			d[j] = (c[j + 1] - c[j]) / (3 * h[j]);
		}
		
		// 保存结果
		Algorithm.x = x;
		Algorithm.a = a;
		Algorithm.b = b;
		Algorithm.c = c;
		Algorithm.d = d;

		end = System.nanoTime();  
		System.out.println("spline runtime:" + (end - start) + "(ns)");
	}
	
//	// 计算拉格朗日插值结果
//	public static double lagrange(ArrayList<double[]> data, double x0) {
//		int len = data.size();
//		double[] x = new double[len];
//		double[] y = new double[len];
//		for (int i = 0; i < len; i++) {
//			x[i] = data.get(i)[0];
//			y[i] = data.get(i)[1];
//		}
//		double result = 0;
//		for (int i = 0; i < len; i++) {
//			double k = 1;
//			for (int j = 0; j < len; j++) {
//				if (i != j) {
//					k = k * (x0 - x[j]) / (x[i] - x[j]);
//				}
//			}
//			k = k * y[i];
//			result = result + k;
//		}
//		return result;
//	}
	
	// 计算拉格朗日插值或多项式拟合结果
	public static double calculate(double x0) {
		double result = 0;
		for (int i = 0; i < p.length; i++) {
			result += p[i] * Math.pow(x0, i);
		}
		return result;
	}
	
	// 计算拉格朗日内插函数
	public static void lagrangeSet(ArrayList<double[]> data) {
		start = System.nanoTime();
		
		// 将ArrayList中的数据提取到double数组中
		int len = data.size();
		double[] x = new double[len];
		double[] y = new double[len];
		for (int i = 0; i < len; i++) {
			x[i] = data.get(i)[0];
			y[i] = data.get(i)[1];
		}
		
		// 计算(x - x[i])累乘的结果
		double[] poly = new double[len + 1];
		poly[0] = 1;
		for (int i = 0; i < len; i++) {
			for (int j = i + 1; j > 0; j--) {
				poly[j] = poly[j - 1] - x[i] * poly[j];
			}
			poly[0] = - x[i] * poly[0];
		}
		
		// 计算拉格朗日内插多项式
		double[] p = new double[len];
		for (int i = 0; i < len; i++) {
			double frac = 1;	//分母
			for (int j = 0; j < len; j++) {
				if (j == i) continue;
				frac *= x[i] - x[j];
			}
			double[] temp = poly.clone();
			double[] l = new double[len];
			for (int j = len; j > 0; j--) {
				l[j - 1] = temp[j];
				temp[j - 1] = temp[j - 1] + x[i] * temp[j];
			}
			for (int j = 0; j < len; j++) {
				p[j] += y[i] * l[j] / frac;
			}
		}
		
		// 保存结果
		Algorithm.p = p;
		
		end = System.nanoTime();  
		System.out.println("lagrange runtime:" + (end - start) + "(ns)");
	}

	public static double[] getP() {
		return p;
	}
	
}
