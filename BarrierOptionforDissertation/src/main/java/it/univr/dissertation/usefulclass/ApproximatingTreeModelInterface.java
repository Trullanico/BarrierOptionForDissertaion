package it.univr.dissertation.usefulclass;

import java.util.function.DoubleUnaryOperator;

/**
 * This is an interface for a Tree model approximating a continuous time model. It has methods
 * to return the possible values (possibly transformed by a function) at specific time and time
 * index as well as the initial price and the structure of the discretization: time step,
 * number of times and final time. It is also possible to return the conditional expectation
 * of values of an option at one time from the time before in the discretization.  
 * 
 * @author Andrea Mazzon
 *
 */
public interface ApproximatingTreeModelInterface {
	
	/**
	 * It returns all the possible values of the approximating tree model at the given time index.
	 * The entries of the array are ordered from the biggest one to the smallest one.
	 * @param timeIndex, the given time index as an int
	 * @return an array of doubles representing all the possible values of the approximating tree
	 * 		  model at timeIndex. The entries of the array are ordered from the biggest one to the smallest one.
	 */
	double[] getValuesAtGivenTimeIndex(int timeIndex);
	
	
	/**
	 * It returns all the possible values of the approximating tree model at the given time.
	 * The entries of the array are ordered from the biggest one to the smallest one.
	 * @param time, the given time as a double
	 * @return an array of doubles representing all the possible values of the approximating tree
	 * 		  model at time. The entries of the array are ordered from the biggest one to the smallest one.
	 */
	double[] getValuesAtGivenTime(double time);
	
	
	/**
	 * It returns an array whose elements are a function of all the possible values of the approximating
	 * tree model at the given time index. The entries of the array are placed in decreasing order
	 * of the underlying approximating tree model (not of its transformed value).

	 * @param timeIndex, the given time index as an int
	 * @param DoubleUnaryOperator transformFunction, the function
	 * @return an array of doubles representing a function all the possible values of the tree model at timeIndex.
	 *		  The entries of the array are placed in decreasing order of the underlying approximating tree model
	 *		  (not of its transformed value).
	 */
	double[] getTransformedValuesAtGivenTimeIndex(int timeIndex, DoubleUnaryOperator transformFunction);
	
	/**
	 * It returns an array whose elements are a function of all the possible values of the approximating
	 * tree model at the given time. The entries of the array are placed in decreasing order
	 * of the underlying approximating tree model (not of its transformed value).
	 * @param timeIndex, the given time as a double
	 * @param DoubleUnaryOperator transformFunction, the function
	 * @return an array of doubles representing a function all the possible values of the tree model at timeIndex.
	 *         The entries of the array are placed in decreasing order of the underlying approximating tree model
	 *		   (not of its transformed value).
	 */
	double[] getTransformedValuesAtGivenTime(double time, DoubleUnaryOperator transformFunction);
	
	/**
	 * It returns an array representing the discounted conditional expectations at given timeIndex of the
	 * values of (possibly a function of) an approximating model at time timeIndex+1.
	 * 
	 * @param optionValues, the values at timeIndex+1 whose conditional expectation is returned
	 * @param timeIndex, the time index
	 * @return the array of the discounted conditional expectations at timeIndex of optionValues. 
	 */
	double[] getConditionalExpectation(double[] optionValues,int timeIndex);
	
	/**
	 * It returns the initial price of the approximated model
	 * @return the initial price of the approximated model
	 */
	double getInitialPrice();

	/**
	 * It returns the time step of the time discretization with which we approximate the continuous time model
	 * @return the time step of the time discretization with which we approximate the continuous time  model
	 */
	double getTimeStep();
	
	/**
	 * It returns the last time of the time discretization with which we approximate the continuous time  model
	 * @return the last time of the time discretization with which we approximate the continuous time  model
	 */
	double getLastTime();

	/**
	 * It returns the number of times of the time discretization with which we approximate the continuous time  model
	 * @return the numbr of times of the time discretization with which we approximate the continuous time  model
	 */
	int getNumberOfTimes();
	

}
