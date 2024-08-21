package it.univr.barrieroptiontests;

import java.util.function.DoubleUnaryOperator;

import it.univr.dissertation.analyticformulas.MyAnalyticForumulas;
import it.univr.dissertation.products.BarrierOptionFiniteDifferences;
import it.univr.dissertation.products.BarrierOptionMonteCarlo;
import it.univr.dissertation.products.BarrierOptionTreeModels;
import it.univr.dissertation.usefulclass.CoxRossRubinsteinModel;
import net.finmath.exception.CalculationException;
import net.finmath.finitedifference.models.FDMBlackScholesModel;
import net.finmath.finitedifference.models.FiniteDifference1DModel;
import net.finmath.finitedifference.products.FiniteDifference1DProduct;
import net.finmath.functions.BarrierOptions;
import net.finmath.functions.BarrierOptions.BarrierType;
import net.finmath.interpolation.RationalFunctionInterpolation;
import net.finmath.interpolation.RationalFunctionInterpolation.ExtrapolationMethod;
import net.finmath.interpolation.RationalFunctionInterpolation.InterpolationMethod;
import net.finmath.montecarlo.BrownianMotion;
import net.finmath.montecarlo.BrownianMotionFromMersenneRandomNumbers;
import net.finmath.montecarlo.assetderivativevaluation.MonteCarloBlackScholesModel;
import net.finmath.montecarlo.assetderivativevaluation.products.AbstractAssetMonteCarloProduct;
import net.finmath.time.TimeDiscretization;
import net.finmath.time.TimeDiscretizationFromArray;

public class BarrierOptionTests {

	public static void main(String[] args) throws CalculationException {

		//option parameters
		double upperBarrier = 120;
		double lowerBarrier = 0;
		double maturity = 3.0;		
		double strike = 100;
		boolean iscall = true; 
		boolean isKnockOut = false; 
		double theta = 0.5;
	
		double callOrPut; 
		if(iscall == true) {
			callOrPut = 1;
		}
		else {
			callOrPut = -1;
		}
		
		BarrierOptionTreeModels optionValueTreeCalculator = new BarrierOptionTreeModels(maturity, strike, lowerBarrier, upperBarrier, iscall);
		
		AbstractAssetMonteCarloProduct optionValueCalculator = new BarrierOptionMonteCarlo(maturity, strike, lowerBarrier, upperBarrier, callOrPut , 0,  isKnockOut);

		BarrierOptionFiniteDifferences optionValueFDCalculator = new BarrierOptionFiniteDifferences(maturity, strike, lowerBarrier, upperBarrier, theta, callOrPut);
		
		//time discretization parameters
		double initialTime = 0.0;
		double timeStep = 0.1;
		int numberOfTimeSteps = (int) (maturity/timeStep);
				
		TimeDiscretization times = new TimeDiscretizationFromArray(initialTime, numberOfTimeSteps, timeStep);
				
		//simulation parameters
		int numberOfPaths = 100000;
		int seed = 1897;
		
		BrownianMotion ourDriver = new BrownianMotionFromMersenneRandomNumbers(times, 1 /* numberOfFactors */, numberOfPaths, seed);
		
		//model (i.e., underlying) parameters
		double initialValue = 100;
		double riskFreeRate = 0.0;
		double volatility = 0.1;
		
		//Finite difference discretization parameters
		final int numTimesteps = 70;
		final int numSpacesteps = 300;
		final int numStandardDeviations = 15;
		
		
		CoxRossRubinsteinModel ourModelForFunction = new CoxRossRubinsteinModel(initialValue, riskFreeRate, volatility, maturity, timeStep);	
		
		//we construct an object of type MonteCarloBlackScholesModel: it represents the simulation of a Black-Scholes process
		MonteCarloBlackScholesModel blackScholesProcess = new MonteCarloBlackScholesModel(initialValue, riskFreeRate, volatility, ourDriver);
		
		final FiniteDifference1DModel model = new FDMBlackScholesModel(
				numTimesteps,//for the discretization of the time interval
				numSpacesteps,//for the discretization of the space domain
				numStandardDeviations,//this enters in the computation of the right and left end of the space domain
				strike,//in the finmath library, there is written that this is the center of the space discretization, but this is not true: it has no effect
				theta,
				strike,//the center of the discretization in the class FDMThetaMethod is initialValue*exp(r*timeHorizon)
				riskFreeRate,
				volatility);
		
		double treePrice = optionValueTreeCalculator.getValue(ourModelForFunction);
		
		double monteCarloPrice = optionValueCalculator.getValue(0.0, blackScholesProcess).getAverage();
		
		final double[][] returnedValues = optionValueFDCalculator.getValue(0.0, model);
		
		//these are the corresponding prices
		final double[] initialValues = returnedValues[0];
		final double[] optionValues = returnedValues[1];
		/*
		 * Since we want to plot a function, and not only the array with respect to the other array, we have to interpolate.
		 * That is, we want to find a continuous function f such that f(x_i)=y_i for all i, where x_i are the initial values
		 * and y_i the prices. We do that with the help of the Finmath library itself.
		 */
		final RationalFunctionInterpolation Interpolation = new RationalFunctionInterpolation(initialValues, optionValues, InterpolationMethod.CUBIC_SPLINE, ExtrapolationMethod.DEFAULT);

		double finiteDifferenceValueOfTheOption = Interpolation.getValue(initialValue);
		
		double analyticPrice = MyAnalyticForumulas.blackScholesOptionValue(initialValue,riskFreeRate,volatility, maturity,strike,callOrPut);
		
	
		if (isKnockOut == true) {
		System.out.println("You set Knock-out Option");
		System.out.println();
		System.out.println("The price with tree model is: " + treePrice);
		System.out.println("The Finite difference price is: " + finiteDifferenceValueOfTheOption);
		System.out.println("The Monte Carlo price is: " + monteCarloPrice);
		System.out.println();
		System.out.println("Now the analytic price:");
		System.out.println();
		
		double analyticPriceDownandOut = MyAnalyticForumulas.blackScholesDownAndOut(initialValue, riskFreeRate, volatility, maturity, strike, lowerBarrier, callOrPut);
		double analyticPriceUpandOut = BarrierOptions.blackScholesBarrierOptionValue
				(initialValue, riskFreeRate, 0, volatility, maturity, strike, iscall, 0, upperBarrier, BarrierType.UP_OUT);
		
			if (callOrPut == 1) {
		
			System.out.println("The analytic price without barrier: " + analyticPrice);
			System.out.println("The analytic price of Down and Out is: " + analyticPriceDownandOut);
			System.out.println("The analytic price of Up and Out is: " + analyticPriceUpandOut);
			}
			else {
			
			System.out.println("The analytic price without barrier: " + analyticPrice);
			System.out.println("The analytic price of Down and Out is : " + analyticPriceDownandOut);
			System.out.println("The analytic price of Up and Out is : " + analyticPriceUpandOut);
			}
		}
		else {
			final double[][] returnedValuesWithoutBarrier = optionValueFDCalculator.getValueWithoutBarrier(0.0, model);
			
			final double[] initialValuesWithoutBarrier = returnedValuesWithoutBarrier[0];
			final double[] optionValuesWithoutBarrier = returnedValuesWithoutBarrier[1];
			
			final RationalFunctionInterpolation InterpolationWithoutBarrier = new RationalFunctionInterpolation(initialValuesWithoutBarrier, optionValuesWithoutBarrier, InterpolationMethod.CUBIC_SPLINE, ExtrapolationMethod.DEFAULT);

			
			double finiteDifferenceValueOfTheOptionWithoutBarrier = InterpolationWithoutBarrier.getValue(initialValue);	
			
			double finiteDifferenceValueKnockIn = finiteDifferenceValueOfTheOptionWithoutBarrier - finiteDifferenceValueOfTheOption;
			
			double treePriceWithoutBarrier = optionValueTreeCalculator.getValueWithoutBarrier(ourModelForFunction);
			
			double treePriceKnockIn = treePriceWithoutBarrier - treePrice;
			
			
			System.out.println();
			System.out.println("The price with tree model is: " + treePriceKnockIn);
			System.out.println("The Finite difference price is: " + finiteDifferenceValueKnockIn);
			System.out.println("The Monte Carlo price is: " + monteCarloPrice);
			System.out.println();
			System.out.println("Now the analytic price:");
			System.out.println();
			
			
			double analyticPriceUpandIn = BarrierOptions.blackScholesBarrierOptionValue
					(initialValue, riskFreeRate, 0, volatility, maturity, strike, iscall, 0, upperBarrier, BarrierType.UP_IN);
			
			double analyticPriceDownandIn = MyAnalyticForumulas.blackScholesDownAndIn(initialValue, riskFreeRate, volatility, maturity, strike, lowerBarrier, callOrPut);
			
			if (callOrPut == 1) {
				
				System.out.println("The analytic price without barrier: " + analyticPrice);
				System.out.println("The analytic price of Down and In is: " + analyticPriceDownandIn);
				System.out.println("The analytic price of Up and In is: " + analyticPriceUpandIn);
				}
				else {
				
				System.out.println("The analytic price without barrier: " + analyticPrice);
				System.out.println("The analytic price of Down and In is : " + analyticPriceDownandIn);
				System.out.println("The analytic price of Up and In is : " + analyticPriceUpandIn);
				}
		
		}
		
		
	}
}
