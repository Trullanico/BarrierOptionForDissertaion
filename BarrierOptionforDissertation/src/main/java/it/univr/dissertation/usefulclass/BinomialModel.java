package it.univr.dissertation.usefulclass;

import java.util.Arrays;
import java.util.function.DoubleUnaryOperator;



/**
 * This class is used in order to construct a binomial model. The binomial model is a discrete model
 * for a stochastic process S, such that every time n we have
 * S(n+1)=S(n)*M(n), 
 * where M(n)=u>1+r with probability q and M(n)=d<1 with probability 1-q, with r the risk free factor.
 * This is done under the risk neutral measure: it can be proved (see notes) that it must hold
 * q=(1 + r - d)/(u - d).
 * For any time index i, all possible value of the process are computed starting from the one with all
 * up movements, i.e., S(0)*u^i.
 */
public class BinomialModel implements TreeModelInterface {

	//the values of these fields will be directly given in the constructor 
	private double initialValue;
	private double upFactor;
	private double downFactor;
	private double riskFreeFactor;

	//these are instead determined from the last three values above, in order to have an arbitrage free market
	private double riskNeutralProbabilityUp;
	private double riskNeutralProbabilityDown;

	private int numberOfTimes;

	//these fields will be initialized and set in private methods. For now their values is "null".
	private double[][] valuesProbabilities;
	private double[][] values;

	/**
	 * It construct an object representing a Binomial model.
	 * 
	 * @param upFactor: the number u such that S(i+1)=S(i)*u with probability q
	 * @param downFactor: the number d such that S(i+1)=S(i)*d with probability d
	 * @param riskFreeFactor: the number rho such that the risk free bond B satisfies B(i+1)=B(i)*(1+rho).
	 * 						  In order to have an arbitrage free market, it must hold d<1+rho<u.
	 * @param initialValue: the initial value of the process, B(0)
	 * @param numberOfTimes: the number of times for which the process is simulated, starting from time 0.
	 * 						 Note that this is equal to the number of subintervals plus 1.
	 */
	public BinomialModel(double upFactor, double downFactor, double riskFreeFactor, double initialValue, int numberOfTimes) {
		//remember the use of this in order to solve the conflict between name of input variables and of fields
		this.upFactor = upFactor;
		this.downFactor = downFactor;
		this.riskFreeFactor = riskFreeFactor;
		this.numberOfTimes = numberOfTimes;	
		//this must be fixed in order to avoid arbitrages!
		riskNeutralProbabilityUp = (1 + riskFreeFactor - downFactor) / (upFactor - downFactor);
		riskNeutralProbabilityDown = 1 - riskNeutralProbabilityUp;
		this.initialValue = initialValue;
	}


	/*
	 * This method is private! This is our inner implementation, behind the scenes. We don't want an user of our
	 * class to access it. The method sets values to be a matrix whose row n represents the possible values of the
	 * binomial model at time index n. However, the last values of the row are zero, because at time n the binomial
	 * model can only take n+1 values. Note that this could cause confusion if somebody is directly aware of this
	 * method.
	 */
	
	/*
	 * [S_0 0 0 0 0 0 0 0]
	 * [S_0*u S_0*d 0 0 0 0 0 0]
	 * [S_0*u^2 S_0*u*d S_0*d^2 0 0 0 0 0 0]
	 * ....
	 */
	private void generateValues() {
		values = new double[numberOfTimes][numberOfTimes];
		values[0][0] = initialValue;
		int numberOfDowns;//it will be updated in the for loop
		for (int numberOfMovements = 1; numberOfMovements < numberOfTimes; numberOfMovements++) {
			for (int numberOfUps = 0; numberOfUps <= numberOfMovements; numberOfUps++) {
				numberOfDowns=numberOfMovements-numberOfUps;
				/*
				 * Value of the binomial model when it went numberOfUps times up and numberOfDowns times down.
				 * Note that this is stored in position numberOfDowns! So the first position has all ups and so on
				 */
				values[numberOfMovements][numberOfDowns] = values[0][0] * Math.pow(upFactor, numberOfUps)*
						Math.pow(downFactor, numberOfDowns);
			}
		}
	}

	/*
	 * This method is private! This is our inner implementation, behind the scenes. We don't want an user of our
	 * class to access it. The method sets valuesProbabilities to be a matrix whose row n represents the probabilities
	 * of the corresponding values of the binomial model at time index n. However, the last values of the row are
	 * zero, because at time n the binomial model can only take n+1 values. Note that this could cause confusion
	 * if somebody is directly aware of this method.
	 */
	
	/*
	 * [S_0 0 0 0 0 0 0 0]
	 * [Q(S_0*u) Q(S_0*d) 0 0 0 0 0 0]
	 * [Q(S_0*u^2) Q(S_0*u*d) Q(S_0*d^2) 0 0 0 0 0 0]
	 * ....
	 */
	private void generateValuesProbabilities() {
		valuesProbabilities = new double[numberOfTimes][numberOfTimes];
		valuesProbabilities[0][0]=1;
		int numberOfDowns;//it will be updated in the for loop
		for (int numberOfMovements = 1; numberOfMovements < numberOfTimes; numberOfMovements++) {
			/*
			 * Here we have to take care of the computation of the binomial coefficient.  We are at
			 * time n and start the next for loop with the case when we have k=0 ups. So we have
			 * binomialCoefficient(n,k)=n!/(k!(n-k)!)=1 
			 */
			double binomialCoefficient = 1;
			for (int numberOfUps = 0; numberOfUps <= numberOfMovements; numberOfUps++) {
				numberOfDowns=numberOfMovements-numberOfUps;
				/*
				 * Probability of having B(numberOfMovements)=B(0)u^numberOfUps*d^numberOfDowns.
				 * Note that this is stored in position numberOfDowns! So the first position has all ups and so on
				 */
				valuesProbabilities[numberOfMovements][numberOfDowns]
						= binomialCoefficient*Math.pow(riskNeutralProbabilityUp, numberOfUps)
						* Math.pow(riskNeutralProbabilityDown, numberOfDowns);
				/*
				 * Here we update the value of the binomial coeffeicient computing the one
				 * that we will use next, i.e., when we will have one more up: so, calling k
				 * the actual number of ups, we have to compute 
				 * binomialCoefficient(n,k+1)=n!/((k+1)!(n-k-1)!)=n!/(k!(n-k)!)*(n-k)/(k+1).
				 * Since n!/(k!(n-k)!) is the last computed value, we multiply by (n-k) 
				 * (so, by numberOfDowns) and divide by k+1, so, by the current number of ups plus 1.
				 */
				binomialCoefficient=binomialCoefficient * (numberOfDowns)/(numberOfUps+1);
			}
		}
	}

	//the next methods are all public. Direct use from outside this class. 

	/**
	 * It returns all the possible values of the binomial model at the given time index. The element in
	 * position i is the one where the underlying has gone down i times.
	 * @param timeIndex, the given time index
	 * @return an array of doubles representing all the possible values of the binomial model at timeIndex.
	 * 		   The value in position i is B(0)*u^(timeIndex-i)*d^i
	 */
	@Override
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
		return Arrays.copyOfRange(values[timeIndex], 0, timeIndex+1);
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
	@Override
	public double[] getTransformedValuesAtGivenTimeIndex(int timeIndex, DoubleUnaryOperator transformFunction) {
		//the possible values of the binomial model
		double[] valuesAtGivenTimeIndex = getValuesAtGivenTimeIndex(timeIndex);
		//we return the function applied to this array
		return UsefulMethodsForArrays.applyFunctionToArray(valuesAtGivenTimeIndex, transformFunction);
	}


	/**
	 * It returns the probabilities of all the possible values of the binomial model at the given time index.
	 * The element in position i is the probability of the value where the underlying has gone down i times.
	 * @param timeIndex, the given time index
	 * @return an array of doubles representing the probabilities of all the possible values of the binomial
	 * 		   model at timeIndex. The value in position i is the probability of B(0)*u^(timeIndex-i)*d^i
	 */
	public double[] getValuesProbabilitiesAtGivenTimeIndex(int timeIndex) {
		/*
		 * Pay attention: the method generateValues() initializes the array valuesProbabilities and sets it.
		 * This is of course needed if we want to get those values. However, we want to do that only once!
		 * So we check if valuesProbabilities is null (this means "not yet initialized") and call the method only
		 * in this case. 
		 */
		if (valuesProbabilities == null) {
			generateValuesProbabilities();
		}
		/*
		 * We only return the first timeIndex entries! The others are zero, because the process can take
		 * only timeIndex values at time index timeIndex.
		 */
		return Arrays.copyOfRange(valuesProbabilities[timeIndex], 0, timeIndex + 1);
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
	@Override
	public double[] getConditionalExpectation(double[] binomialValues,int timeIndex) {
		//at timeIndex we have timeIndex + 1 values
		double[] conditionalExpectation = new double[timeIndex+1];
		for (int i = 0; i <= timeIndex; i++) {
			/*
			 * computation of the conditional probability at the state with i down. Note that the i-th element
			 * of binomialValues has gone up, because the number of down is still i. 
			 */
			conditionalExpectation[i] = (binomialValues[i]*riskNeutralProbabilityUp + binomialValues[i + 1]*riskNeutralProbabilityDown)/(1+riskFreeFactor);
		}
		return conditionalExpectation;
	}
	
	/**
	 * It returns the array whose two elements are the probability of an up movement and the probability
	 * of a down movement, respectively.
	 * @return the array whose two elements are the probability of an up movement and the probability
	 * of a down movement, respectively.
	 */
	public double[] getUpAndDownProbabilities() {
		double[] probabilities = {riskNeutralProbabilityUp,riskNeutralProbabilityDown};
		return probabilities;
	}


}
