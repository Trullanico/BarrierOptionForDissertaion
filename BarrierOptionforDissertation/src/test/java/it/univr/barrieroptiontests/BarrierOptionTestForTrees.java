package it.univr.barrieroptiontests;

import java.util.Arrays;
import java.util.function.DoubleUnaryOperator;

import it.univr.dissertation.analyticformulas.MyAnalyticForumulas;
import it.univr.dissertation.products.BarrierOptionTreeModels;
import it.univr.dissertation.usefulclass.CoxRossRubinsteinModel;
import net.finmath.exception.CalculationException;
import net.finmath.functions.AnalyticFormulas;
import net.finmath.functions.BarrierOptions;
import net.finmath.functions.BarrierOptions.BarrierType;
import net.finmath.plots.Named;
import net.finmath.plots.Plot2D;


public class BarrierOptionTestForTrees {
	

	public static void main(String[] args) throws CalculationException {

		//option parameters
		double upperBarrier = Long.MAX_VALUE;
		double lowerBarrier = 90;
		double maturity = 2.0;		
		double strike = 100;
		int numberOfTime = 260;
		boolean iscall = true; 
		boolean isKnockOut = true; 
		double callOrPut; 
		if(iscall == true) {
			callOrPut = 1;
		}
		else {
			callOrPut = -1;
		}
	
	
		BarrierOptionTreeModels optionValueTreeCalculator = new BarrierOptionTreeModels(maturity, strike, lowerBarrier, upperBarrier, iscall);
		
	
		//model (i.e., underlying) parameters
		double initialValue = 100;
		double riskFreeRate = 0.0;
		double volatility = 0.3;
		
		CoxRossRubinsteinModel ourModelForFunction = new CoxRossRubinsteinModel(initialValue, riskFreeRate, volatility, maturity, numberOfTime);
		
		double treePrice = optionValueTreeCalculator.getValue(ourModelForFunction);
		
		double analyticPrice = MyAnalyticForumulas.blackScholesOptionValue(initialValue,riskFreeRate,volatility, maturity,strike,callOrPut);

		DoubleUnaryOperator numberOfTimesToPriceCoxRossModel = (numberOfTimesForFunction) -> {
			CoxRossRubinsteinModel ourModelForFunction2 = new CoxRossRubinsteinModel(initialValue, riskFreeRate,
					volatility, maturity, (int) numberOfTimesForFunction);		
			return optionValueTreeCalculator.getValue(ourModelForFunction2);
		};
		
		DoubleUnaryOperator dummyFunctionBlackScholesPrice = (numberOfTimesForFunction) -> {
			return MyAnalyticForumulas.blackScholesDownAndOut(initialValue,riskFreeRate,volatility, maturity,strike,lowerBarrier, callOrPut);
		//	return 	BarrierOptions.blackScholesBarrierOptionValue
		//			(initialValue, riskFreeRate, 0, volatility, maturity, strike, iscall, 0, upperBarrier, BarrierType.UP_OUT);
			//return AnalyticFormulas.blackScholesOptionValue(spotPrice,riskFreeRate,volatility, lastTime,strike,true);
		};
		
		//compute the optimal numbers of time steps (Boyle and Lau 1994)
		
		int numberOfConsecutiveDownsToReachBarrier = 4;

		double fofM = numberOfConsecutiveDownsToReachBarrier*numberOfConsecutiveDownsToReachBarrier*volatility*volatility*maturity

		/Math.pow(Math.log(lowerBarrier/initialValue), 2);
		System.out.println(fofM);

		//int idealNumberOfTimeSteps = (int) fofM;

		int idealNumberOfTimeSteps = (int) Math.floor(fofM);
		
		//plus 1 because we use the number of times, which are number of time steps plus one
		int idealNumberOfTimeStepsplus1 = idealNumberOfTimeSteps +1;

		//check of the values in the paper

		System.out.println("Ideal number of times " + idealNumberOfTimeStepsplus1);

		System.out.println("Value option for ideal number of times " +

		numberOfTimesToPriceCoxRossModel.applyAsDouble(idealNumberOfTimeStepsplus1));

		System.out.println("Value option for ideal number of times plus one " +

		numberOfTimesToPriceCoxRossModel.applyAsDouble(idealNumberOfTimeSteps+2));
		//we now plot the functions from a minimum number of points to a maximum number of points
				int maxNumberOfTimes = 400;
				int minNumberOfTimes = 100;

				final Plot2D plot = new Plot2D(minNumberOfTimes, maxNumberOfTimes, (maxNumberOfTimes-minNumberOfTimes+2)/2,
					    Arrays.asList(
					        new Named<DoubleUnaryOperator>("Cox-Ross-Rubinstein (initial=" + initialValue + ", vol=" + volatility + ", Ubarr=" + upperBarrier + ", Dbarr=" + lowerBarrier + ")", numberOfTimesToPriceCoxRossModel),
					        new Named<DoubleUnaryOperator>("Black-Scholes", dummyFunctionBlackScholesPrice)
					    )
					);

					plot.setXAxisLabel("Number of discretized times");
					plot.setYAxisLabel("Price");
					plot.setIsLegendVisible(true);
					plot.show();

	
		if (isKnockOut == true) {
		System.out.println("You set Knock-out Option");
		System.out.println();
		System.out.println("The price with tree model is: " + treePrice);
		System.out.println();
		System.out.println("Now the analytic price:");
		System.out.println();
		
		double analyticPriceDownandOut = MyAnalyticForumulas.blackScholesDownAndOut(initialValue, riskFreeRate, volatility, maturity, strike, lowerBarrier, callOrPut);
		double analyticPriceUpandOut =BarrierOptions.blackScholesBarrierOptionValue
				(initialValue, riskFreeRate, 0, volatility, maturity, strike, iscall, 0, upperBarrier, BarrierType.UP_OUT);
			if (callOrPut == 1) {
			System.out.println("You set Call Option");
			System.out.println();
			System.out.println("The analytic price without barrier: " + analyticPrice);
			System.out.println("The analytic price of Down and Out is: " + analyticPriceDownandOut);
			System.out.println("The analytic price of Up and Out is: " + analyticPriceUpandOut);
			}
			else {
			System.out.println("You set Put Option");
			System.out.println();
			System.out.println("The analytic price without barrier: " + analyticPrice);
			System.out.println("The analytic price of Down and Out is : " + analyticPriceDownandOut);
			System.out.println("The analytic price of Up and Out is : " + analyticPriceUpandOut);
			}
		}
		else {
			
			double treePriceWithoutBarrier = optionValueTreeCalculator.getValueWithoutBarrier(ourModelForFunction);
			
			double treePriceKnockIn = treePriceWithoutBarrier - treePrice;
			
			System.out.println("You set Knock-in Option");
			System.out.println();
			System.out.println("The price with tree model is: " + treePriceKnockIn);
			System.out.println();
			System.out.println("Now the analytic price:");
			System.out.println();
			
			
			double analyticPriceUpandIn = BarrierOptions.blackScholesBarrierOptionValue
					(initialValue, riskFreeRate, 0, volatility, maturity, strike, iscall, 0, upperBarrier, BarrierType.UP_IN);
			double analyticPriceDownandIn = MyAnalyticForumulas.blackScholesDownAndIn(initialValue, riskFreeRate, volatility, maturity, strike, lowerBarrier, callOrPut);
			
			if (callOrPut == 1) {
				System.out.println("You set Call Option");
				System.out.println();
				System.out.println("The analytic price without barrier: " + analyticPrice);
				System.out.println("The analytic price of Down and In is: " + analyticPriceDownandIn);
				System.out.println("The analytic price of Up and In is: " + analyticPriceUpandIn);
				}
				else {
				System.out.println("You set Put Option");
				System.out.println();
				System.out.println("The analytic price without barrier: " + analyticPrice);
				System.out.println("The analytic price of Down and In is : " + analyticPriceDownandIn);
				System.out.println("The analytic price of Up and In is : " + analyticPriceUpandIn);
				}
		
		}
		
		
		
}}
