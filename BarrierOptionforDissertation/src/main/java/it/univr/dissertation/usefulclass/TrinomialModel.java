package it.univr.dissertation.usefulclass;

import java.util.Arrays;
import java.util.function.DoubleUnaryOperator;



/**
 * This class is used in order to construct a trinomial model. The trinomial model is a discrete model
 * for a stochastic process S, such that every time n we have
 * S(n+1)=S(n)*M(n), 
 * where M(n) can take values u, 1 or 1/u with probabilities q_u, q_m and q_d, respectively.
 * This is done under a martingale measure: it can be seen (see notes) that there exists infinitely many
 * martingale measures once one fixed q_u. 
 * For any time index i, all possible value of the process are computed starting from the biggets one.
 */
public class TrinomialModel  implements TreeModelInterface {

	//the values of these fields will be directly given in the constructor 
	private double initialValue;
	private double upFactor;
	private double riskNeutralProbabilityUp;
	private double riskFreeFactor;

	//this will be set to 1/u
	private double downFactor;

	
	//these are instead determined from the values above, in order to have an arbitrage free market
	private double riskNeutralProbabilityToStayTheSame;
	private double riskNeutralProbabilityDown;

	private int numberOfTimes;

	//these fields will be initialized and set in private methods. For now their values is "null".
	private double[][] values;

	/**
	 * It constructs an object representing a Binomial model.
	 * 
	 * @param riskNeutralProbabilityUp: the probability of an up movement
	 * @param upFactor: the number u such that S(i+1)=S(i)*u with probability q
	 * @param riskFreeFactor: the number rho such that the risk free bond B satisfies B(i+1)=B(i)*(1+rho).
	 * 						  In order to have an arbitrage free market, it must hold d<1+rho<u.
	 * @param initialValue: the initial value of the process, B(0)
	 * @param numberOfTimes: the number of times for which the process is simulated, starting from time 0.
	 * 						 Note that this is equal to the number of subintervals plus 1.
	 */
	public TrinomialModel(double riskNeutralProbabilityUp, double upFactor,  double riskFreeFactor, double initialValue, int numberOfTimes) {
		//remember the use of this in order to solve the conflict between name of input variables and of fields
		this.upFactor = upFactor;
		downFactor = 1/upFactor;
		this.riskFreeFactor = riskFreeFactor;
		this.numberOfTimes = numberOfTimes;	
		this.riskNeutralProbabilityUp = riskNeutralProbabilityUp;
		//this must be fixed in order to avoid arbitrages!
		riskNeutralProbabilityToStayTheSame = (1+riskFreeFactor-downFactor-riskNeutralProbabilityUp*(upFactor-downFactor))
				/(1-downFactor);
		riskNeutralProbabilityDown = 1 - riskNeutralProbabilityUp-riskNeutralProbabilityToStayTheSame;
		this.initialValue = initialValue;
	}


	/*
	 * This method is private! This is our inner implementation, behind the scenes. We don't want an user of our
	 * class to access it. The method sets values to be a matrix whose row n represents the possible values of the
	 * trinomial model at time index n. However, the last values of the row are zero, because at time n the trinomial
	 * model can only take 2*n+1 values. Note that this could cause confusion if somebody is directly aware of this
	 * method.
	 */
	
	/*
	 * [S_0]
	 * [S_0*u^2*d^1=S_0*u , S_0*u^1*d^1=S_0  ,S_0*u^0*d^1,0,...,0]    
	 * [S_0*u^4*d^2=S_0*u^2 , S_0*u^3*d^2=S_0*u  ,S_0*u^2*d^2=S_0, S_0*u^1*d^2=S_0*d, S_0*u^0*d^2,0,..,0] 
	 *	...
	 */
	private void generateValues() {
		values = new double[numberOfTimes][2*numberOfTimes+1];
		values[0][0] = initialValue;
		int positionToFill;//it will be updated in the for loop
		for (int numberOfTimeStepsAhead = 1; numberOfTimeStepsAhead < numberOfTimes; numberOfTimeStepsAhead++) {
			for (int exponentOfUp = 0; exponentOfUp <= 2*numberOfTimeStepsAhead; exponentOfUp++) {
				positionToFill=2*numberOfTimeStepsAhead-exponentOfUp;
				/*
				 * Note that this is stored in position ! So the first position has all ups.
				 */
				values[numberOfTimeStepsAhead][positionToFill] = values[0][0] * Math.pow(upFactor, exponentOfUp)*
						Math.pow(downFactor, numberOfTimeStepsAhead);
			}
		}
	}


	//the next methods are all public. Direct use from outside this class. 

	/**
	 * It returns all the possible values of the trinomial model at the given time index. The element in
	 * position i is the one where the underlying has gone down i times.
	 * @param timeIndex, the given time index
	 * @return an array of doubles representing all the possible values of the binomial model at timeIndex.
	 * 		   The value in position i is B(0)*u^(timeIndex-i)*d^i
	 */
	public double[] getValuesAtGivenTimeIndex(int timeIndex) {
		/*
		 * Pay attention: the method generateValues() initializes the array values and sets it. This is
		 * of course needed if we want to get those values. However, we want to do that only once!
		 * So we check if values is null (this means "not yet initialized") and call the method only
		 * in this case. 
		 */
		if (values == null) {
			generateValues();
		}	
		/*
		 * We only return the first timeIndex entries! The others are zero, because the process can take
		 * only timeIndex values at time index timeIndex.
		 */
		return Arrays.copyOfRange(values[timeIndex], 0, 2*timeIndex+1);
	}

	/**
	 * It returns an array whose elements are a function of all the possible values of the binomial model at
	 * the given time index. The element in position i is the function of the value of the underlying in the
	 * case when it has gone down i times.
	 * @param timeIndex, the given time index
	 * @param DoubleUnaryOperator transformFunction, the function
	 * @return an array of doubles representing a function all the possible values of the binomial model at timeIndex.
	 * 		   The value in position i is B(0)*transformFunction(u^(timeIndex-i)*d^i)
	 */
	public double[] getTransformedValuesAtGivenTimeIndex(int timeIndex, DoubleUnaryOperator transformFunction) {
		//the possible values of the binomial model
		double[] valuesAtGivenTimeIndex = getValuesAtGivenTimeIndex(timeIndex);
		//we return the function applied to this array
		return UsefulMethodsForArrays.applyFunctionToArray(valuesAtGivenTimeIndex, transformFunction);
	}


	
	/**
	 * It returns an array representing the discounted conditional expectations at given timeIndex of the
	 * values of (possibly a function of) a binomial model at time timeIndex+1. 
	 * 
	 * @param binomialValues, values of (possibly a function of) a binomial model at time timeIndex+1
	 * @param timeIndex, the time index
	 * @return the array of the discounted conditional expectations at timeIndex of binomialValues. 
	 * 			The i-th element is the conditional expectation computed in the case when the underlying
	 * 			has gone down i times.
	 */
	public double[] getConditionalExpectation(double[] trinomialValues,int timeIndex) {
		//at timeIndex we have 2*timeIndex + 1 values
		double[] conditionalExpectation = new double[2*timeIndex+1];

		for (int i = 0; i < 2*timeIndex+1; i++) {
			
			conditionalExpectation[i] = (trinomialValues[i]*riskNeutralProbabilityUp + trinomialValues[i + 1]*riskNeutralProbabilityToStayTheSame
					+trinomialValues[i + 2]*riskNeutralProbabilityDown)/(1+riskFreeFactor);
		}
		return conditionalExpectation;
	}
	
}

