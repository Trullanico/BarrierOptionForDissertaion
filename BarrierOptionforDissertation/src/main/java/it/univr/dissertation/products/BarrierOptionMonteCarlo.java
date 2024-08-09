package it.univr.dissertation.products;

import java.util.function.DoubleUnaryOperator;

import net.finmath.exception.CalculationException;
import net.finmath.montecarlo.RandomVariableFromDoubleArray;
import net.finmath.montecarlo.assetderivativevaluation.AssetModelMonteCarloSimulationModel;
import net.finmath.montecarlo.assetderivativevaluation.products.AbstractAssetMonteCarloProduct;
import net.finmath.stochastic.RandomVariable;
import net.finmath.time.TimeDiscretization;

public class BarrierOptionMonteCarlo extends AbstractAssetMonteCarloProduct {
	
	private double maturity;
	private double strike;
	private double lowerBarrier;
	private double upperBarrier;
	private int underlyingIndex;
	boolean IsKnockOut;
	private double callOrPutSign;
	
	/**
	 * It constructs an object representing a barrier, European option on an underlying X. The underlying is 
	 * @param maturity The maturity T in the option payoff 
	 * @param strike The strike K in the option payoff 
	 * @param lowerBarrier the lower barrier B_L in the option payoff 
	 * @param upperBarrier the upper barrier B_U in the option payoff 
	 * @param callOrPutSign Set 1 for call, set -1 for put
	 * @param underlyingIndex it identifies the underlying if model in getValue is multi-dimensional
	 * @param IsKnockOut Set true if it's an knock out option, set false for an knock in option
	 */
	public BarrierOptionMonteCarlo(double maturity, double strike, double lowerBarrier, double upperBarrier,double callOrPutSign, int underlyingIndex, boolean IsKnockOut) {
		this.maturity = maturity;
		this.strike = strike;
		this.lowerBarrier = lowerBarrier;
		this.upperBarrier = upperBarrier;
		this.underlyingIndex = underlyingIndex;
		this.IsKnockOut = IsKnockOut;
		this.callOrPutSign = callOrPutSign;
	}
	

	
	/**
	 * It constructs an object representing a barrier, call option on an underlying X. 
	 * With this constructors option is Call and Knock out.
	 * The underlying is 
	 * @param maturity The maturity T in the option payoff 
	 * @param strike The strike K in the option payoff 
	 * @param lowerBarrier the lower barrier B_L in the option payoff 
	 * @param upperBarrier the upper barrier B_U in the option payoff 
	 */
	public BarrierOptionMonteCarlo(double maturity, double strike, double lowerBarrier, double upperBarrier) {
		this.maturity = maturity;
		this.strike = strike;
		this.lowerBarrier = lowerBarrier;
		this.upperBarrier = upperBarrier;
		this.underlyingIndex = 0;
		this.IsKnockOut = true;
		this.callOrPutSign = 1;
	}

	@Override
	public RandomVariable getValue(double evaluationTime, AssetModelMonteCarloSimulationModel model)
			throws CalculationException {
		
		
		//we need it to check the path before maturity
		TimeDiscretization timeDiscretizationOfTheUnderlying = model.getTimeDiscretization();
		double[] discretizedTimes = timeDiscretizationOfTheUnderlying.getAsDoubleArray();
		
		/*
		 * At the beginning, it is 1 for all simulated trajectories. It will be 0 for those trajectories
		 * which exit the interval [B_L,B_U]
		 * We construct it like "insideBarrierAllTime", and if we have an Knock-In it will be like "touchBarrierAtLeastOnce"
		 */
		RandomVariable inOrOutBarrier = new RandomVariableFromDoubleArray(1.0);
		
		DoubleUnaryOperator indicatorFunction = x -> (x>=lowerBarrier & x<=upperBarrier ? 1.0 : 0.0);
		
		//we check all times
		for (double currentTime : discretizedTimes) {
			currentTime = Math.min(currentTime, maturity);
			
			//this will give an object of type RandomVariable representing the array 
			RandomVariable realizationsAtCurrentTime = model.getAssetValue(currentTime, underlyingIndex);
			//we check if the realizations of underlying exit the interval [B_L,B_U] 
			RandomVariable realizationsAtCurrentTimeInsideBarrier = realizationsAtCurrentTime.apply(indicatorFunction);
			
			/*
			 * Here we update inOrOutBarrier: its old value (that is, the one that is taken at the right).
			 * It gets updated by multiplication with realizationsAtCurrentTimeInsideBarrier, so when the loop finish 
			 * it represent if the path stay in interval for every time. 
			 * It will be like [0 , 0 , 1 , 1,....] "0" means that the path exit the interval [B_L,B_U].
			 * In this case "inOrOutBarrier" represents "insideBarrierAllTime"
			 */
			inOrOutBarrier = inOrOutBarrier.mult(realizationsAtCurrentTimeInsideBarrier);
		}

		/*
		 * From now on, this is the Finmath library implementation of European option apart from the point where
		 * we multiply values by insideBarriersAtAllTimes.
		 */

		// Get X(T)
		final RandomVariable underlyingAtMaturity	= model.getAssetValue(maturity, underlyingIndex);

		// The payoff: values = max(underlying - strike, 0) = V(T) = max(X(T)-K,0) for Call
		// The payoff: values = max(strike - underlying, 0) = V(T) = max(K -X(T),0) for Put
		RandomVariable values = underlyingAtMaturity.sub(strike).mult(callOrPutSign).floor(0);
		
		/* If the option is "Knock-In" we check if the trajectories touch the barrier,
		* we use the array inOrOutBarrier and we replace the 0 with 1 and vice versa
		* in this case "inOrOutBarrier" represents "touchBarrierAtLeastOnce"
		*/
		if ( IsKnockOut == false ) {
			
			System.out.println("You set an Knock-In Option ");
			
			inOrOutBarrier = inOrOutBarrier.sub(1).mult(-1);
			
		} 
		
		// we multiply payoff with inOrOutBarrier, pay attention
		// for Knock-In "inOrOutBarrier" represents "touchBarrierAtLeastOnce"
		values = values.mult(inOrOutBarrier);
		
		// Discounting...
		final RandomVariable numeraireAtMaturity	= model.getNumeraire(maturity);
		final RandomVariable monteCarloWeights		= model.getMonteCarloWeights(maturity);
		values = values.div(numeraireAtMaturity).mult(monteCarloWeights);

		// ...to evaluation time.
		final RandomVariable	numeraireAtEvalTime			= model.getNumeraire(evaluationTime);
		final RandomVariable	monteCarloWeightsAtEvalTime	= model.getMonteCarloWeights(evaluationTime);
		values = values.mult(numeraireAtEvalTime).div(monteCarloWeightsAtEvalTime);

		return values;

	}

}

