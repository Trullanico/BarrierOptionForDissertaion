package it.univr.dissertation.products;

import java.util.function.DoubleUnaryOperator;

import it.univr.dissertation.usefulclass.ApproximatingTreeModelInterface;
import it.univr.dissertation.usefulclass.UsefulMethodsForArrays;

/**
 * This class implements the valuation of an European option with double or single barrier. This is a path
 * dependent option which pays the payoff only if the value of the underlying stays in an interval
 * [lowerBarrier, upperBarrier] for the whole path. We have single barrier if we only have lowerBarrier
 * or only have upperBarrier. The value is computed via an approximation of a continuous time process
 * with a tree model, repesented by an object of type ApproximatingTreeModelInterface.
 * 
 * @author Andrea Mazzon
 *
 */
public class BarrierOptionTreeModels {

	private double maturity;
	private DoubleUnaryOperator payoffFunction;
	private DoubleUnaryOperator barrierFunction;//this is defined in the costructor.
	

	/**
	 * It constructs an object which represents the implementation of the European option with barriers.
	 * @param maturity, the maturity of the option
	 * @param optionStrike The strike K in the option payoff 
	 * @param lowerBarrier the lower barrier B_L in the option payoff 
	 * @param upperBarrier the upper barrier B_U in the option payoff
	 * @param isCall Set true for call, set false for put
	 * @param payoffFunction, the function which identifies the payoff. The payoff is f(S_T) for payoffFunction
	 * 			f and underlying value S_T at maturity. The payoffFunction is represented by a DoubleUnaryOperator
	 */
	public BarrierOptionTreeModels(double maturity,double strike, double lowerBarrier, double upperBarrier, boolean isCall ) {
		this.maturity = maturity;
		//ternary operator!
		barrierFunction = (x) -> (x>lowerBarrier & x < upperBarrier ? 1 : 0);
		
		if (isCall == true) {
			payoffFunction = (x) -> (x - strike > 0 ? x - strike : 0.0);
		}
		else  {
			payoffFunction = (x) -> (strike - x > 0 ? strike - x : 0.0);
		}
	}
	
	
	//method that give value without barrier in order to exploit it for in-out parity
	
	public double getValueWithoutBarrier(ApproximatingTreeModelInterface approximatingTreeModel) {
		//the vector representing all the possible values of the payoff at maturity
		double[] optionValues = approximatingTreeModel.getTransformedValuesAtGivenTime(maturity, payoffFunction);
		int numberOfTimes = (int) Math.round(maturity/approximatingTreeModel.getTimeStep());
		//we go backward and for any timeIndex we compute the conditional expectation of the value of the option at timeIndex + 1
		for (int timeIndex = numberOfTimes - 1; timeIndex >= 0; timeIndex--) {
			//delegation to approximatingBinomialModel!
        	double[] conditionalExpectation = approximatingTreeModel.getConditionalExpectation(optionValues, timeIndex);
            optionValues = conditionalExpectation;   
        }
		return optionValues[0];
	}
	/**
	 * It returns the discounted value of the option written on the continuous time model approximated by
	 * the object of type ApproximatingTreeModelInterface given in input. The value of the option is computed
	 * as the discounted expectation of the possible values at maturity. This expectation is computed by going backward
	 * from maturity to initial time and computing the iterative conditional expectation, see slides. The conditional
	 * expectations are multiplied at every time with a vector whose elements are 1 if the value of the underlying
	 * is within the interval [lowerBarrier, upperBarrier] and 0 otherwise.
	 * 
	 * 
	 * @param approximatingTreeModel, the underlying
	 * @return the value of the option written on the underlying
	 */
	public double getValue(ApproximatingTreeModelInterface approximatingTreeModel) {
		
		//the values of the option at maturity if this is not a barrier option
		//(f(S_0u^nd^0),f(S_0u^(n-1)d^1),..., f(S_0u^0d^n))
		double[] optionValuesWithoutBarrier = approximatingTreeModel.getTransformedValuesAtGivenTime(maturity, payoffFunction);
		
		//the values of the underlyings: we need them to check if they are inside the interval
		double[] underlyingValues = approximatingTreeModel.getValuesAtGivenTime(maturity);
		
		/*
		 * vector whose elements are 1 if the value of the underlying approximating Tree model is within the interval
		 * [lowerBarrier, upperBarrier] and 0 otherwise
		 */
		//(0,0,0,1,1,1...,0,0,0)
		double[] areTheUnderlyingValuesInsideInterval = UsefulMethodsForArrays.applyFunctionToArray(underlyingValues, barrierFunction);
		 
		//(f(S_0u^nd^0),f(S_0u^(n-1)d^1),..., f(S_0u^0d^n))
		//the values of the option at maturity, considering now the barrier
		double[] optionValues = UsefulMethodsForArrays.multArrays(optionValuesWithoutBarrier, areTheUnderlyingValuesInsideInterval);

		int numberOfTimes = (int) Math.round(maturity/approximatingTreeModel.getTimeStep());
		for (int timeIndex = numberOfTimes - 1; timeIndex >= 0; timeIndex--) {
			//now we repeat the same thing as above at any time.
			
			//the values of the option not considering the barrier
        	double[] conditionalExpectation = approximatingTreeModel.getConditionalExpectation(optionValues, timeIndex);
    		//the values of the underlyings: we need them to check if they are inside the interval
        	underlyingValues = approximatingTreeModel.getValuesAtGivenTimeIndex(timeIndex);
        	/*
    		 * vector whose elements are 1 if the value of the underlying approximating tree model is within the interval
    		 * [lowerBarrier, upperBarrier] and 0 otherwise
    		 */
        	areTheUnderlyingValuesInsideInterval = UsefulMethodsForArrays.applyFunctionToArray(underlyingValues, barrierFunction);
        	
    		//the values of the option, considering now the barrier
        	double[] transformedConditionalExpectation = UsefulMethodsForArrays.multArrays(conditionalExpectation, areTheUnderlyingValuesInsideInterval);
        	optionValues = transformedConditionalExpectation;  

        }
		return optionValues[0];
	}
}
