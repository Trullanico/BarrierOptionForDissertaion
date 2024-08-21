package it.univr.barrieroptiontests;

import java.util.Random;

import it.univr.dissertation.analyticformulas.MyAnalyticForumulas;
import it.univr.dissertation.products.BarrierOptionMonteCarlo;
import it.univr.dissertation.products.BarrierOptionDinamicTimeStepsV1;
import it.univr.dissertation.products.BarrierOptionDinamicTimeStepsV2;
import net.finmath.exception.CalculationException;
import net.finmath.functions.BarrierOptions;
import net.finmath.functions.BarrierOptions.BarrierType;
import net.finmath.montecarlo.BrownianMotion;
import net.finmath.montecarlo.BrownianMotionFromMersenneRandomNumbers;
import net.finmath.montecarlo.assetderivativevaluation.MonteCarloBlackScholesModel;
import net.finmath.montecarlo.assetderivativevaluation.products.AbstractAssetMonteCarloProduct;
import net.finmath.time.TimeDiscretization;
import net.finmath.time.TimeDiscretizationFromArray;

public class BarrierOptionTestMc {

	public static void main(String[] args) throws CalculationException {

		//option parameters
		double upperBarrier = Long.MAX_VALUE;
		double lowerBarrier = 90;
		double maturity = 1.0;		
		double strike = 100;
		boolean iscall = true; 
		boolean isKnockOut = true; 
	
		//model (i.e., underlying) parameters
		double initialValue = 100;
		double riskFreeRate = 0.0;
		double volatility = 0.3;
				
		double callOrPut; 
		if(iscall == true) {
			callOrPut = 1;
		}
		else {
			callOrPut = -1;
		}
		
		AbstractAssetMonteCarloProduct optionValueCalculator = new BarrierOptionMonteCarlo(maturity, strike, lowerBarrier, upperBarrier, callOrPut , 0,  isKnockOut);
		
		BarrierOptionDinamicTimeStepsV2 optionValueCalculatorModified = new BarrierOptionDinamicTimeStepsV2(maturity, strike, lowerBarrier, upperBarrier);
	
		//time discretization parameters
		double initialTime = 0.0;
		double timeStep = 0.05;
		int numberOfTimeSteps = (int) (maturity/timeStep);
		
		//time discretization parameters
		double initialTimeforModified = 0.0;
		double timeStepforModified = 0.05;
		int numberOfTimeStepsforModified = (int) (maturity/timeStepforModified);
				
		TimeDiscretization times = new TimeDiscretizationFromArray(initialTime, numberOfTimeSteps, timeStep);
		TimeDiscretization timesforModified = new TimeDiscretizationFromArray(initialTimeforModified, numberOfTimeStepsforModified, timeStepforModified);
		
		//simulation parameters
		int numberOfPaths = 50000;
	//	int seed = 1897;
		
		//simulation parameters
		int numberOfPathsforModified =5000;
	//	int seedforModified = 1897;
			
		optionValueCalculatorModified.setVolatility(volatility);
		optionValueCalculatorModified.setRiskFreeRate(riskFreeRate);
		
		//for test, remember to replace type of option when you change, now is set for a Down and Out
		double analyticPrice = MyAnalyticForumulas.blackScholesDownAndOut(initialValue,riskFreeRate,volatility, maturity,strike,lowerBarrier, callOrPut);
//				BarrierOptions.blackScholesBarrierOptionValue
//				(initialValue, riskFreeRate, 0, volatility, maturity, strike, iscall, 0, lowerBarrier, BarrierType.DOWN_IN);
				
		System.out.println("analyticPrice " + analyticPrice);
		
		 int numSimulations = 25; // Number of simulations to run
		 	double[] errorsforModified = new double[numSimulations]; // Array to store errors
		 	double[] errors = new double[numSimulations]; // Array to store errors
	        long[] executionTimesforModified = new long[numSimulations]; // Array to store execution times
	        long[] executionTimes = new long[numSimulations]; // Array to store execution times

	        for (int i = 0; i < numSimulations; i++) {
	            int seed = new Random().nextInt(); // Generate a random seed for each simulation
	            BrownianMotion ourDriver = new BrownianMotionFromMersenneRandomNumbers(times, 1 /* numberOfFactors */, numberOfPaths, seed);
	    		BrownianMotion ourDriverforModified = new BrownianMotionFromMersenneRandomNumbers(timesforModified, 1 /* numberOfFactors */, numberOfPathsforModified, seed);
	    		MonteCarloBlackScholesModel blackScholesProcess = new MonteCarloBlackScholesModel(initialValue, riskFreeRate, volatility, ourDriver);
	    		MonteCarloBlackScholesModel blackScholesProcessforModified = new MonteCarloBlackScholesModel(initialValue, riskFreeRate, volatility, ourDriverforModified);
	    		

	            // Measure execution time for the modified calculation
	            long startFM = System.currentTimeMillis();
	            double monteCarloPriceModified = optionValueCalculatorModified.getValue(blackScholesProcessforModified);
	            long endFM = System.currentTimeMillis();
	            long executionTimeModified = endFM - startFM;

	            // Measure execution time for the regular calculation
	            long start = System.currentTimeMillis();
	            double monteCarloPrice = optionValueCalculator.getValue(blackScholesProcess);
	            long end = System.currentTimeMillis();
	            long executionTime = end - start;

	            // Calculate errors
	            double errorforModified = (monteCarloPriceModified - analyticPrice);
	            double error = (monteCarloPrice - analyticPrice);
	            
	            // Store results
	            errorsforModified[i] = errorforModified;
	            errors[i] = error;
	            executionTimes[i] = executionTime ;
	            executionTimesforModified[i] =  executionTimeModified;
	            
	           System.out.println("MCmodified " + monteCarloPriceModified);
	            System.out.println("MC " + monteCarloPrice);
	            System.out.println();
	            // Output results for this iteration
	            System.out.println("Simulation " + (i+1) + ":");
	            System.out.println("Error: " + error);
	            System.out.println("ErrorMD: " + errorforModified);
	            System.out.println("Execution Time: " + executionTimes[i] + " ms\n");
	            System.out.println("Execution Time MD: " + executionTimesforModified[i] + " ms\n");
	        }
	        // Calculate average error and average execution time
	        double averageError = calculateAverage(errors);
	        double percError = averageError/analyticPrice;
	        long averageExecutionTime = calculateAverage(executionTimes);
	        
	        double averageErrorMD = calculateAverage(errorsforModified);
	        double percErrorMD = averageErrorMD/analyticPrice;
	        long averageExecutionTimeMD = calculateAverage(executionTimesforModified);

	        // Output average results
	        System.out.println("Average Error: " + averageError);
	        System.out.println("Percentage Error: " + percError);
	        System.out.println("Average Execution Time: " + averageExecutionTime + " ms");
	        
	     // Output average results
	        System.out.println("Average Error for modified: " + averageErrorMD);
	        System.out.println("Percentage Error for modified: " + percErrorMD);
	        System.out.println("Average Execution Time for modified: " + averageExecutionTimeMD + " ms");
	    }
	
	        // Method to calculate average of an array of longs
	        private static long calculateAverage(long[] array) {
	            long sum = 0;
	            for (long value : array) {
	                sum += value;
	            }
	            return sum / array.length;
	        }

	        // Method to calculate average of an array of doubles
	        private static double calculateAverage(double[] array) {
	            double sum = 0;
	            for (double value : array) {
	                sum += value;
	            }
	            return sum / array.length;
	        }
	        
	
	}

