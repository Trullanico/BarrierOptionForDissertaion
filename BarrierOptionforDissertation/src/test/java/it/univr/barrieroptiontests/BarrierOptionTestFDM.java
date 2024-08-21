package it.univr.barrieroptiontests;

import it.univr.dissertation.analyticformulas.MyAnalyticForumulas;
import it.univr.dissertation.products.BarrierOptionFiniteDifferences;

import net.finmath.exception.CalculationException;
import net.finmath.finitedifference.models.FDMBlackScholesModel;
import net.finmath.finitedifference.models.FiniteDifference1DModel;
import net.finmath.functions.BarrierOptions;
import net.finmath.functions.BarrierOptions.BarrierType;
import net.finmath.interpolation.RationalFunctionInterpolation;
import net.finmath.interpolation.RationalFunctionInterpolation.ExtrapolationMethod;
import net.finmath.interpolation.RationalFunctionInterpolation.InterpolationMethod;


public class BarrierOptionTestFDM {
	
	public static void main(String[] args) throws CalculationException {

		//option parameters
		double upperBarrier = 120;
		double lowerBarrier = 0;
		double maturity = 3.0;		
		double strike = 100;
		boolean iscall = false; 
		boolean isKnockOut = false; 
		double theta = 0.5;
	
		double callOrPut; 
		if(iscall == true) {
			callOrPut = 1;
		}
		else {
			callOrPut = -1;
		}
	
		BarrierOptionFiniteDifferences optionValueFDCalculator = new BarrierOptionFiniteDifferences(maturity, strike, lowerBarrier, upperBarrier, theta, callOrPut);
		
		
		//model (i.e., underlying) parameters
		double initialValue = 100;
		double riskFreeRate = 0.0;
		double volatility = 0.1;
		
		//Finite difference discretization parameters
		final int numTimesteps = 20;
		final int numSpacesteps = 100;
		final int numStandardDeviations = 15;

		final FiniteDifference1DModel model = new FDMBlackScholesModel(
				numTimesteps,//for the discretization of the time interval
				numSpacesteps,//for the discretization of the space domain
				numStandardDeviations,//this enters in the computation of the right and left end of the space domain
				strike,//in the finmath library, there is written that this is the center of the space discretization, but this is not true: it has no effect
				theta,
				strike,//the center of the discretization in the class FDMThetaMethod is initialValue*exp(r*timeHorizon)
				riskFreeRate,
				volatility);

		long start = System.currentTimeMillis();
		
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
		
		long end = System.currentTimeMillis();
		
		long executionTime = end - start;
		
		double analyticPrice = MyAnalyticForumulas.blackScholesOptionValue(initialValue,riskFreeRate,volatility, maturity,strike,callOrPut);
		double analyticPriceDownandOut = MyAnalyticForumulas.blackScholesDownAndOut(initialValue, riskFreeRate, volatility, maturity, strike, lowerBarrier, callOrPut);
		double analyticPriceUpandIn = BarrierOptions.blackScholesBarrierOptionValue
				(initialValue, riskFreeRate, 0, volatility, maturity, strike, iscall, 0, upperBarrier, BarrierType.UP_IN);
		double analyticPriceDownandIn = BarrierOptions.blackScholesBarrierOptionValue
				(initialValue, riskFreeRate, 0, volatility, maturity, strike, iscall, 0, lowerBarrier, BarrierType.DOWN_IN);
		
		double analyticPriceUpandOut = BarrierOptions.blackScholesBarrierOptionValue
				(initialValue, riskFreeRate, 0, volatility, maturity, strike, iscall, 0, upperBarrier, BarrierType.UP_OUT);
		
			
		if (isKnockOut == true) {
		System.out.println("You set Knock-out Option");
		System.out.println();
		System.out.println("The Finite difference price is: " + finiteDifferenceValueOfTheOption);
		System.out.println();
		System.out.println("Now the analytic price:");
		System.out.println();
		
		
				
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
			
			System.out.println("You set Knock-in Option");
			System.out.println();
			long start1 = System.currentTimeMillis();
			final double[][] returnedValuesWithoutBarrier = optionValueFDCalculator.getValueWithoutBarrier(0.0, model);
			
			final double[] initialValuesWithoutBarrier = returnedValuesWithoutBarrier[0];
			final double[] optionValuesWithoutBarrier = returnedValuesWithoutBarrier[1];
			
			final RationalFunctionInterpolation InterpolationWithoutBarrier = new RationalFunctionInterpolation(initialValuesWithoutBarrier, optionValuesWithoutBarrier, InterpolationMethod.CUBIC_SPLINE, ExtrapolationMethod.DEFAULT);

			
			double finiteDifferenceValueOfTheOptionWithoutBarrier = InterpolationWithoutBarrier.getValue(initialValue);	
			
			double finiteDifferenceValueKnockIn = finiteDifferenceValueOfTheOptionWithoutBarrier - finiteDifferenceValueOfTheOption;
			long end1 = System.currentTimeMillis();
			
			long executionTime1 = end1 - start1;
			
			System.out.println();
			System.out.println("The Finite difference price is: " + finiteDifferenceValueKnockIn);
			System.out.println();
			System.out.println("Now the analytic price:");
			System.out.println();
			
			
		
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
	
			
			//for test, remember to replace type of option when you change, now is set for a Up and In
			 double errorPerc = (finiteDifferenceValueKnockIn - analyticPriceUpandIn)/ analyticPriceUpandIn;
				
			 System.out.println("Time: " + executionTime+ " ms");
			 System.out.println("Time to use in out parity: " + executionTime1+ " ms");
			 System.out.println("Error perc: " + errorPerc);
		
		}
		
		
	}
}

