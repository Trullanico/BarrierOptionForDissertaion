package it.univr.dissertation.analyticformulas;

import net.finmath.functions.AnalyticFormulas;

import net.finmath.functions.NormalDistribution;


//For analytic value it's more easy to use the class net.finmath.functions.BarrierOptions, but i have try to implement some formulas

public class MyAnalyticForumulas {

	//classic B&S for call or put
	public static double blackScholesOptionValue(double initialValue, double riskFreeRate, double sigma, double maturity, double strike,
		 double callOrPut) {
		
		if (callOrPut == 1) {
		return AnalyticFormulas.blackScholesOptionValue(initialValue, riskFreeRate, sigma, maturity, strike, true) ;
		}
		else {
			return AnalyticFormulas.blackScholesOptionValue(initialValue, riskFreeRate, sigma, maturity, strike, false) ;
		}
	}

	/*
	 *  B&S for Down and Out call/put 
	 *  For Call: Bjork,Arbitrage Theory in Continuous Time, Proposition 18.17, pag. 274
	 *  For Put: following Bjork,Arbitrage Theory in Continuous Time, Proposition 18.18, pag. 275
	 */
	public static double blackScholesDownAndOut(double initialValue, double riskFreeRate, double sigma, double maturity, double strike,
			double lowerBarrier, double callOrPut) {
		
		double rTilda = riskFreeRate - (0.5 *sigma*sigma);
		double exprTilda = (2*(riskFreeRate - (0.5 *sigma*sigma)))/(sigma*sigma);
		
		if (callOrPut == 1) {
		return AnalyticFormulas.blackScholesOptionValue(initialValue, riskFreeRate, sigma, maturity, strike) 
				- Math.pow(lowerBarrier/initialValue,exprTilda) 
				* AnalyticFormulas.blackScholesOptionValue(lowerBarrier*lowerBarrier/initialValue, riskFreeRate, sigma, maturity, strike);
		}
		else {
			double hContract = Math.exp(-riskFreeRate*(maturity))* NormalDistribution.cumulativeDistribution (((rTilda*maturity) + Math.log(initialValue/lowerBarrier))/(sigma*Math.sqrt(maturity))) ;
			double hContractSecond = Math.exp(-riskFreeRate*(maturity))* NormalDistribution.cumulativeDistribution (((rTilda*maturity) + Math.log((lowerBarrier*lowerBarrier/initialValue)/lowerBarrier))/(sigma*Math.sqrt(maturity))) ;
			
			double bond = hContract - Math.pow(lowerBarrier/initialValue,exprTilda) * hContractSecond ;
			double stock = (lowerBarrier * hContract) 
					- (lowerBarrier * Math.pow(lowerBarrier/initialValue,exprTilda) * hContractSecond) 
					+ AnalyticFormulas.blackScholesOptionValue(initialValue, riskFreeRate, sigma, maturity, lowerBarrier) 
					- Math.pow(lowerBarrier/initialValue,exprTilda) 
					* AnalyticFormulas.blackScholesOptionValue(lowerBarrier*lowerBarrier/initialValue, riskFreeRate, sigma, maturity, lowerBarrier);
									
			return (strike * bond) - stock + MyAnalyticForumulas.blackScholesDownAndOut(initialValue, riskFreeRate, sigma, maturity, strike, lowerBarrier, 1);
		}
		}
	
	/*
	 *  B&S for Down and In call/put 
	 *  For Call: In-Out parity
	 *  For Put: In-Out parity
	 */
	public static double blackScholesDownAndIn(double initialValue, double riskFreeRate, double sigma, double maturity, double strike,
			double lowerBarrier, double callOrPut ) {
		double exprTilda = (2*(riskFreeRate - (0.5 *sigma*sigma)))/(sigma*sigma);
		
		
		if (callOrPut == 1) {
			return Math.pow(lowerBarrier/initialValue,exprTilda) 
					* AnalyticFormulas.blackScholesOptionValue(lowerBarrier*lowerBarrier/initialValue, riskFreeRate, sigma, maturity, strike);
			}
			else {
				return AnalyticFormulas.blackScholesOptionValue(initialValue,riskFreeRate,sigma, maturity,strike,false) 
						- MyAnalyticForumulas.blackScholesDownAndOut(initialValue, riskFreeRate, sigma, maturity, strike, lowerBarrier, -1);
			}
	
	}
	
//	/*
//	 *  B&S for Up and Out call/put 
//	 *  For Call: In progress, attention, now there is a dummy value
//	 *  For Put: Bjork,Arbitrage Theory in Continuous Time, Proposition 18.19, pag. 275
//	 */
//	public static double blackScholesUpAndOut(double initialValue, double riskFreeRate, double sigma, double maturity, double strike,
//			double upperBarrier, double callOrPut ) {
//		double exprTilda = (2*(riskFreeRate - (0.5 *sigma*sigma)))/(sigma*sigma);
//		
//		if (callOrPut == 1) {
//		//pay attention, this is dummy value
//			return 1;
//			}
//			else {
//				return AnalyticFormulas.blackScholesOptionValue(initialValue, riskFreeRate, sigma, maturity, strike, false)
//						- Math.pow(upperBarrier/initialValue,exprTilda) 
//						* AnalyticFormulas.blackScholesOptionValue(upperBarrier*upperBarrier/initialValue, riskFreeRate, sigma, maturity, strike, false);
//			}
//		
//	}
//	
//	/*
//	 *  B&S for Up and In call/put 
//	 *  For Call: In progress, attention, now there is a dummy value
//	 *  For Put: In-Out parity
//	 */
//	public static double blackScholesUpAndIn(double initialValue, double riskFreeRate, double sigma, double maturity, double strike,
//			double upperBarrier, double callOrPut ) {
//		
//		double exprTilda = (2*(riskFreeRate - (0.5 *sigma*sigma)))/(sigma*sigma);
//		
//		if (callOrPut == 1) {
//			//pay attention, this is dummy value
//			return 1;
//			}
//			else {
//				return Math.pow(upperBarrier/initialValue,exprTilda) 
//						* AnalyticFormulas.blackScholesOptionValue(upperBarrier*upperBarrier/initialValue, riskFreeRate, sigma, maturity, strike, false) ;
//			}
//	}
//	
//
//	
//	/*
//	 * IN PROGRESS
//	 * In this way bond look OK, it's 1 when upperBarrier is high, and it decrease when the Barrier decrease
//	 * Also the stock look OK, it's 100 hen upperBarrier is high, and it decrease when the Barrier decrease
//	 * 
//	 * BUT the results isn't satisfying, when the Barrier is near, can be negative values.
//	 * 
//	 * COMMENTO NON MOLTO TECNICO: 
//	 * Ho rimaneggaito un po'alla carlona i pezzi per far quadrare, ma senza successo, forse però non è lontana la soluzione
//	 * 
//	 */
//	public static double blackScholesUpAndOutCall(double initialValue, double riskFreeRate, double sigma, double maturity, double strike,
//			double upperBarrier) {
//		
//		double rTilda = riskFreeRate - (0.5 *sigma*sigma);
//		double exprTilda = (2*(riskFreeRate - (0.5 *sigma*sigma)))/(sigma*sigma);
//		
//		double hContract = Math.exp(-riskFreeRate*(maturity))* NormalDistribution.cumulativeDistribution (((rTilda*maturity) + Math.log(initialValue/upperBarrier))/(sigma*Math.sqrt(maturity))) ;
//		double hContractSecond = Math.exp(-riskFreeRate*(maturity))* NormalDistribution.cumulativeDistribution (((rTilda*maturity) + Math.log((upperBarrier*upperBarrier/initialValue)/upperBarrier))/(sigma*Math.sqrt(maturity))) ;
//		
//		double bond = hContractSecond - Math.pow(upperBarrier/initialValue,exprTilda) * hContract ;
//		double stock = (upperBarrier * hContractSecond) 
//				- (upperBarrier * Math.pow(upperBarrier/initialValue,exprTilda) * hContract) 
//				-(AnalyticFormulas.blackScholesOptionValue(initialValue, riskFreeRate, sigma, maturity, upperBarrier, false)
//				- Math.pow(upperBarrier/initialValue,exprTilda) 
//			* AnalyticFormulas.blackScholesOptionValue(upperBarrier*upperBarrier/initialValue, riskFreeRate, sigma, maturity, upperBarrier, false))
//				;
//		
//		System.out.println("stock " + stock);
//		System.out.println("bond " +bond);
////				System.out.println("hContract " +hContractSecond+ "first " + hContract);
//				
//		return  stock -(strike * bond)+ MyAnalyticForumulas.blackScholesUpAndOut(initialValue, riskFreeRate, sigma, maturity, strike, upperBarrier, -1);
//	}
	
//	public static double blackScholesDownAndOutPut(double initialValue, double riskFreeRate, double sigma, double maturity, double strike,
//	double lowerBarrier) {
//
//double rTilda = riskFreeRate - (0.5 *sigma*sigma);
//double exprTilda = (2*(riskFreeRate - (0.5 *sigma*sigma)))/(sigma*sigma);
//
//double hContract = Math.exp(-riskFreeRate*(maturity))* NormalDistribution.cumulativeDistribution (((rTilda*maturity) + Math.log(initialValue/lowerBarrier))/(sigma*Math.sqrt(maturity))) ;
//double hContractSecond = Math.exp(-riskFreeRate*(maturity))* NormalDistribution.cumulativeDistribution (((rTilda*maturity) + Math.log((lowerBarrier*lowerBarrier/initialValue)/lowerBarrier))/(sigma*Math.sqrt(maturity))) ;
//
//double bond = hContract - Math.pow(lowerBarrier/initialValue,exprTilda) * hContractSecond ;
//double stock = (lowerBarrier * hContract) 
//		- (lowerBarrier * Math.pow(lowerBarrier/initialValue,exprTilda) * hContractSecond) 
//		+ AnalyticFormulas.blackScholesOptionValue(initialValue, riskFreeRate, sigma, maturity, lowerBarrier) 
//		- Math.pow(lowerBarrier/initialValue,exprTilda) 
//		* AnalyticFormulas.blackScholesOptionValue(lowerBarrier*lowerBarrier/initialValue, riskFreeRate, sigma, maturity, lowerBarrier);
////System.out.println("stock " + stock);
////		System.out.println("bond " +bond);
////				System.out.println("hContract " +hContractSecond+"second "+ hContract);
//						
//return (strike * bond) - stock + MyAnalyticForumulas.blackScholesDownAndOutCall(initialValue, riskFreeRate, sigma, maturity, strike, lowerBarrier);
//}

//public static double blackScholesDownAndInPut(double initialValue, double riskFreeRate, double sigma, double maturity, double strike,
//	double lowerBarrier) {
//
//return AnalyticFormulas.blackScholesOptionValue(initialValue,riskFreeRate,sigma, maturity,strike,false) 
//		- MyAnalyticForumulas.blackScholesDownAndOutPut(initialValue, riskFreeRate, sigma, maturity, strike, lowerBarrier);
//}
//	public static double blackScholesUpAndInCall(double initialValue, double riskFreeRate, double sigma, double maturity, double strike,
//			double upperBarrier) {
//		
//		return AnalyticFormulas.blackScholesOptionValue(initialValue,riskFreeRate,sigma, maturity,strike,false) 
//				- OurAnalyticFormulas.blackScholesDownAndOutPut(initialValue, riskFreeRate, sigma, maturity, strike, lowerBarrier);
//	}
	
//	public static double blackScholesUpAndIn(double initialValue, double riskFreeRate, double sigma, double maturity, double strike,
//			double upperBarrier) {
//		return Math.pow(initialValue/upperBarrier,-(2*riskFreeRate/sigma*sigma - 1)) 
//				* AnalyticFormulas.blackScholesOptionValue(initialValue, riskFreeRate, sigma, maturity, strike, true);
//	}
	
//	public static double blackScholesDownAndOutSecondTerm(double initialValue, double riskFreeRate, double sigma, double maturity, double strike,
//			double lowerBarrier) {
//		double rtilda = (2*(riskFreeRate - (0.5 *sigma*sigma)))/(sigma*sigma);
//		
//		return  Math.pow(lowerBarrier/initialValue,rtilda) 
//				* AnalyticFormulas.blackScholesOptionValue(lowerBarrier*lowerBarrier/initialValue, riskFreeRate, sigma, maturity, strike);
//
//}
//	public static double blackScholesDownAndOutFirstTerm(double initialValue, double riskFreeRate, double sigma, double maturity, double strike,
//			double lowerBarrier) {
//		return AnalyticFormulas.blackScholesOptionValue(initialValue, riskFreeRate, sigma, maturity, strike); 
//				
//	}
	
	
//	-(upperBarrier * hContract) 
//	+ (upperBarrier * Math.pow(upperBarrier/initialValue,exprTilda) * hContractSecond) 
//	+ AnalyticFormulas.blackScholesOptionValue(initialValue, riskFreeRate, sigma, maturity, upperBarrier, false)
//	- Math.pow(upperBarrier/initialValue,exprTilda) 
//	* AnalyticFormulas.blackScholesOptionValue(upperBarrier*upperBarrier/initialValue, riskFreeRate, sigma, maturity, upperBarrier, false)
//	

}
