package it.univr.dissertation.products;


import java.util.Random;
import java.util.function.DoubleBinaryOperator;
import java.util.function.DoubleUnaryOperator;


import net.finmath.exception.CalculationException;

import net.finmath.montecarlo.RandomVariableFromDoubleArray;
import net.finmath.montecarlo.assetderivativevaluation.AssetModelMonteCarloSimulationModel;

import net.finmath.montecarlo.assetderivativevaluation.products.AbstractAssetMonteCarloProduct;

import net.finmath.montecarlo.templatemethoddesign.assetderivativevaluation.MonteCarloBlackScholesModel2;

import net.finmath.stochastic.RandomVariable;
import net.finmath.time.TimeDiscretization;
import net.finmath.time.TimeDiscretizationFromArray;






/**
 * This class implements the valuation of a European option on a single asset.
 * In particular, the option has payoff
 * (X_T-K)1_{B_L <= X_t <= B_U} for a given underlying X_t, t >=0. 
 * Here K is the strike of the option, T its maturity, B_L and B_U the lower and upper barrier, respectively.
 * 
 * Note that this class extends AbstractAssetMonteCarloProduct. For this reason, we have to implement the method
 * getValue(double evaluationTime, AssetModelMonteCarloSimulationModel model).
 * 
 * We do that exploiting the methods of the interface AssetModelMonteCarloSimulationModel.
 *
 * @author Andrea Mazzon
 */
public class BarrierOptionModifiedbyNico extends AbstractAssetMonteCarloProduct {

	private double maturity;
	private double strike;
	private double lowerBarrier;
	private double upperBarrier;
	private int underlyingIndex;
	
	private Random seedGenerator = new Random();
	
	private int numberOfSubIntervals ;

	private double volatility ;
	private double riskFreeRate ;

	/**
	 * It constructs an object representing a barrier, European call option on an underlying X. The underlying is 
	 * @param maturity The maturity T in the option payoff (X_T-K)1_{B_L <= X_t <= B_U}
	 * @param strike The strike K in the option payoff (X_T-K)1_{B_L <= X_t <= B_U}
	 * @param lowerBarrier the lower barrier B_L in the option payoff (X_T-K)1_{B_L <= X_t <= B_U}
	 * @param upperBarrier the upper barrier B_U in the option payoff (X_T-K)1_{B_L <= X_t <= B_U}
	 * @param underlyingIndex it identifies the underlying if model in getValue is multi-dimensional
	 */
	public BarrierOptionModifiedbyNico(double maturity, double strike, double lowerBarrier, double upperBarrier, int underlyingIndex) {
		this.maturity = maturity;
		this.strike = strike;
		this.lowerBarrier = lowerBarrier;
		this.upperBarrier = upperBarrier;
		this.underlyingIndex = underlyingIndex;
	}



	/**
	 * It constructs an object representing a barrier, European call option on an underlying X. The underlying is 
	 * @param maturity The maturity T in the option payoff (X_T-K)1_{B_L <= X_t <= B_U}
	 * @param strike The strike K in the option payoff (X_T-K)1_{B_L <= X_t <= B_U}
	 * @param lowerBarrier the lower barrier B_L in the option payoff (X_T-K)1_{B_L <= X_t <= B_U}
	 * @param upperBarrier the upper barrier B_U in the option payoff (X_T-K)1_{B_L <= X_t <= B_U}
	 */
	public BarrierOptionModifiedbyNico(double maturity, double strike, double lowerBarrier, double upperBarrier) {
		this.maturity = maturity;
		this.strike = strike;
		this.lowerBarrier = lowerBarrier;
		this.upperBarrier = upperBarrier;
		this.underlyingIndex = 0;
	}

	//the only method we have to implement

	@Override
	public RandomVariable getValue(double evaluationTime, AssetModelMonteCarloSimulationModel model)
			throws CalculationException {

		TimeDiscretization modelTimeDiscretization = model.getTimeDiscretization();
		
		RandomVariable initialValue = model.getAssetValue(0.0, 0);
		double doubleInitialValue = initialValue.getAverage();
		
		// maybe we can do better, but it isn't very relevant 
		numberOfSubIntervals = 20/ (int) maturity ;
		
		//instead of having the usual time step, we divide it in subintervals and we make it start from zero
		TimeDiscretization timeDiscretizationForSubInterval = new TimeDiscretizationFromArray(0.0, numberOfSubIntervals, modelTimeDiscretization.getTimeStep(0)/numberOfSubIntervals);
				
		double bufferLength=  (int) (volatility* (doubleInitialValue)*0.34);
			
		//a double binary operator defined inside the method: we can do that
		DoubleBinaryOperator checkCloseBarrier = (x, y) -> {//x is the old realization, y the new one
			
			//if the past realization is far from the lowerBarrier, or is already below the barrier we just return the new one
			if (x > lowerBarrier+ bufferLength || x < lowerBarrier ) {
				return y;
			}
			
			//and this is the simulation
			MonteCarloBlackScholesModel2 simulation = new MonteCarloBlackScholesModel2(
					timeDiscretizationForSubInterval,
					2,
					x,
					riskFreeRate,
					volatility,
					seedGenerator.nextInt());
			
			double[] timesForChecking = timeDiscretizationForSubInterval.getAsDoubleArray();
			
			double simulatedValue;
			
			for (double timeCheck : timesForChecking) {
				//we have only one trajectory, so it's a double
				simulatedValue = simulation.getAssetValue(timeCheck, underlyingIndex).getAverage();
			
				//if it goes below the barrier, we return it: so when we call this function we have something below the barrier 
				if (simulatedValue < lowerBarrier) {
			//		System.out.println("it's over" + simulatedValue);
					return simulatedValue;
				}	
			}
			//if it has not gone below the barrier, we return the new value we would have had from the beginning
			return y;
		};

		//time discretization: t_0=0<t_1<t_2<...<t_n=T
		//omega_j -> (X_{t_n}(omega_j)-K)^+ 1_{B_L <= X_{t_i}(omega_j)<= B_U for all i=0,1,...,n} 

		//we need it to check the path before maturity
		TimeDiscretization timeDiscretizationOfTheUnderlying = model.getTimeDiscretization();

		RandomVariable realizationsAtPreviousTime = model.getAssetValue(1, underlyingIndex);
		
	        
		/*
		 * At the beginning, it is 1 for all simulated trajectories. It will be 0 for those trajectories
		 * which exit the interval [B_L,B_U]
		 */
		RandomVariable insideBarriersAtAllTimes = new RandomVariableFromDoubleArray(1.0);

		/*
		 * 1_{B_L <= X_{t_i}(omega_j)<= B_U for all i=0,1,...,k+1} =
		 * 1_{B_L <= X_{t_{i}}(omega_j)<= B_U for all i=0,1,...,k}1_{B_L <= X_{t_{k+1}}(omega_j)<= B_U}
		 */
		DoubleUnaryOperator indicatorFunction = x -> (x>=lowerBarrier & x<=upperBarrier ? 1.0 : 0.0);
		double currentTime;
		//we check all times
		for (int timeIndex = 2; timeIndex < timeDiscretizationOfTheUnderlying.getNumberOfTimes(); timeIndex++) {
			currentTime = Math.min(timeDiscretizationOfTheUnderlying.getTime(timeIndex), maturity);
			
			RandomVariable possibleRealizationsAtCurrentTime = model.getAssetValue(currentTime, underlyingIndex);
			
			//we check if it is close to the barrier or not 
			RandomVariable realizationsAtCurrentTime =
					realizationsAtPreviousTime.apply(checkCloseBarrier, possibleRealizationsAtCurrentTime);
			
			//1_{B_L<=X_{t_{k+1}}(omega_j}<=B_U} for any simulation omega_j if t_{k+1} is the current time
			RandomVariable realizationsAtCurrentTimeInsideBarrier = realizationsAtCurrentTime.apply(indicatorFunction);

			/*
			 * Here we update insideBarriersAtAllTimes: its old value (that is, the one that is taken at the right) is
			 * 1_{B_L<=X_{t_i}(omega_j}<=B_U for any 0 <= i <= k} for any simulation omega_j if t_{k+1} is the current time.
			 * It gets updated by multiplication with realizationsAtCurrentTimeInsideBarrier, so it is now
			 * 1_{B_L<=X_{t_i}(omega_j}<=B_U for any 0 <= i <= k+1} for any simulation omega_j if t_{k+1} is the current time
			 */
			insideBarriersAtAllTimes = insideBarriersAtAllTimes.mult(realizationsAtCurrentTimeInsideBarrier);

			realizationsAtPreviousTime =  new RandomVariableFromDoubleArray(realizationsAtCurrentTime);
		}

		/*
		 * From now on, this is the Finmath library implementation of European option apart from the point where
		 * we multiply values by insideBarriersAtAllTimes.
		 */
		// Get underlying and numeraire

		// Get X(T)
		final RandomVariable underlyingAtMaturity	= model.getAssetValue(maturity, underlyingIndex);

		// The payoff: values = max(underlying - strike, 0) = V(T) = max(X(T)-K,0)
		RandomVariable values = underlyingAtMaturity.sub(strike).floor(0.0);

		values = values.mult(insideBarriersAtAllTimes);
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
    // Metodo setter per la volatilità
    public void setVolatility(double volatility) {
        this.volatility = volatility;
    }

    // Metodo setter per il tasso risk-free
    public void setRiskFreeRate(double riskFreeRate) {
        this.riskFreeRate = riskFreeRate;
    }


}
