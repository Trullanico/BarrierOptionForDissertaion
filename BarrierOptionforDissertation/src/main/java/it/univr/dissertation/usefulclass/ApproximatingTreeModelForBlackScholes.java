package it.univr.dissertation.usefulclass;

import java.util.function.DoubleUnaryOperator;



/**
 * This is an abstract class which represents a general tree model approximating Black-Scholes.
 * The only abstract method, which gets implemented in the derived classes, takes care of
 * generating the underlying tree, giving it probabilities, movements sizes, ecc.
 * 
 * @author Andrea Mazzon
 *
 */
public abstract class ApproximatingTreeModelForBlackScholes implements ApproximatingTreeModelInterface {
	
	//parameters describing the model
	private double initialPrice;
	private double riskFreeRate;
	private double volatility;

	//parameters of the time discretization
	private double timeStep;
	private double lastTime;
	private int numberOfTimes;

	/*
	 * This is of fundamental importance in our implementation: we generate it in derived classes of
	 * this abstract one, and here we delegate to it the implementation of the methods where we want
	 * to get values, transformed values and conditional expectations.
	 * In order for this field to be defined here, we have to make it possible to construct as both a
	 * Binomial and Trinomial model. A solution is to create an interface TreeModelInterface which gets
	 * implemented by BinomialModel and TrinomialModel.
	 */
	protected TreeModelInterface ourTreeModel;
	
	/**
	 * It constructs an object of type ApproximatingTreeModelForBlackScholes.
	 * 
	 * @param initialPrice, the initial price of the asset modeled by the process
	 * @param riskFreeRate, the number r such that the value of a risk-free bond at time T is e^(rT)
	 * @param volatility, the log-volatility of the Black-Scholes model
	 * @param lastTime, the last time T in the time discretization 0=t_0<t_1<..<t_n=T
	 * @param timeStep, the length t_k-t_{k-1} of the equally spaced time steps that we take for the approximating
	 * 		  time discretization 0=t_0<t_1<..<t_n=T
	 */
	public ApproximatingTreeModelForBlackScholes(double initialPrice, double riskFreeRate, double volatility, 
			double lastTime, double timeStep) {
		this.initialPrice = initialPrice;
		this.riskFreeRate = riskFreeRate;
		this.volatility = volatility;
		this.lastTime = lastTime;
		this.timeStep = timeStep;
		numberOfTimes = (int) (Math.round(lastTime/timeStep) + 1);//the number of times comes from the number of times steps
	}


	/**
	 * It constructs an object of type ApproximatingTreeModelInterface.
	 * 
	 * @param initialPrice, the initial price of the asset modeled by the process
	 * @param riskFreeRate, the number r such that the value of a risk-free bond at time T is e^(rT)
	 * @param volatility, the log-volatility of the Black-Scholes model
	 * @param lastTime, the last time T in the time discretization 0=t_0<t_1<..<t_n=T
	 * @param numberOfTimes, the number of times in the equally spaced time steps that we take for the approximating
	 * 		  time discretization 0=t_0<t_1<..<t_n=T
	 */
	public ApproximatingTreeModelForBlackScholes(double initialPrice, double riskFreeRate, double volatility,
			double lastTime, int numberOfTimes) {
		this.initialPrice = initialPrice;
		this.riskFreeRate = riskFreeRate;
		this.volatility = volatility;
		this.lastTime = lastTime;
		this.numberOfTimes = numberOfTimes;
		timeStep = lastTime/(numberOfTimes-1);//the times step comes from the number of times
	}


	
	/*
	 * In this method we generate the object ourTreeModel. Note that, once we have it, the
	 * implementation of the next methods is the same for any approximation model.
	 */
	protected abstract void generateTreeModel();


	//all next methods are pure delegation to ourTreeModel
	/**
	 * It returns all the possible values of the approximating tree model at the given time index.
	 * The entries of the array are ordered from the biggest one to the smallest one.
	 * @param timeIndex, the given time index as an int
	 * @return an array of doubles representing all the possible values of the approximating tree
	 * 		  model at timeIndex. The entries of the array are ordered from the biggest one to the
	 * 		  smallest one.
	 */
	@Override
	public double[] getValuesAtGivenTimeIndex(int timeIndex) {
		//we want to generate ourTreeModel only once! So we check if it is null: if yes, we have to generate it
		if (ourTreeModel==null) {
			generateTreeModel();
		}
		//pure delegation
		return ourTreeModel.getValuesAtGivenTimeIndex(timeIndex);
	}

	
	
	/**
	 * It returns all the possible values of the approximating tree model at the given time.
	 * The entries of the array are ordered from the biggest one to the smallest one.
	 * @param time, the given time as a double
	 * @return an array of doubles representing all the possible values of the approximating tree
	 * 		   model at time. The entries of the array are ordered from the biggest one to the smallest one.
	 */
	@Override
	public double[] getValuesAtGivenTime(double time) {
		int timeIndex = (int) Math.round(time/timeStep);
		//we want to generate ourTreeModel only once! So we check if it is null: if yes, we have to generate it
		if (ourTreeModel==null) {
			generateTreeModel();
		}
		//pure delegation
		return ourTreeModel.getValuesAtGivenTimeIndex(timeIndex);
	}
	
	
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
	@Override
	public double[] getTransformedValuesAtGivenTimeIndex(int timeIndex, DoubleUnaryOperator transformFunction) {
		//we want to generate ourTreeModel only once! So we check if it is null: if yes, we have to generate it
		if (ourTreeModel==null) {
			generateTreeModel();
		}
		//pure delegation
		return ourTreeModel.getTransformedValuesAtGivenTimeIndex(timeIndex, transformFunction);
	}
	

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
	@Override
	public double[] getTransformedValuesAtGivenTime(double time, DoubleUnaryOperator transformFunction) {
		int timeIndex = (int) Math.round(time/timeStep);
		//we want to generate ourTreeModel only once! So we check if it is null: if yes, we have to generate it
		if (ourTreeModel==null) {
			generateTreeModel();
		}
		//pure delegation
		return ourTreeModel.getTransformedValuesAtGivenTimeIndex(timeIndex, transformFunction);
	}


	/**
	 * It returns an array representing the discounted conditional expectations at given timeIndex of the
	 * values of (possibly a function of) an approximating binomial model at time timeIndex+1.
	 * 
	 * @param optionValues, the values at timeIndex+1 whose conditional expectation is returned
	 * @param timeIndex, the time index
	 * @return the array of the discounted conditional expectations at timeIndex of optionValues. 
	 */
	@Override
	public double[] getConditionalExpectation(double[] optionValues,int timeIndex) {
		//we want to generate ourTreeModel only once! So we check if it is null: if yes, we have to generate it
		if (ourTreeModel==null) {
			generateTreeModel();
		}
		return ourTreeModel.getConditionalExpectation(optionValues, timeIndex);
	}

	/*
	 * Getters for the parameters of the Trinomial model. Some of them are used in the derived classes:
	 * in this way, we can set them private here (we prefer, because in this way they cannot be modified,
	 * note that there are no setters indeed)
	 */
	/**
	 * It returns the initial price of the approximated Black-Scholes model
	 * @return the initial price of the approximated Black-Scholes model
	 */
	@Override
	public double getInitialPrice() {
		return initialPrice;
	}

	/**
	 * It returns the time step of the time discretization with which we approximate Black-Scholes model
	 * @return the time step of the time discretization with which we approximate Black-Scholes model
	 */
	@Override
	public double getTimeStep() {
		return timeStep;
	}
	/**
	 * It returns the last time of the time discretization with which we approximate Black-Scholes model
	 * @return the last time of the time discretization with which we approximate Black-Scholes model
	 */
	@Override
	public double getLastTime() {
		return lastTime;
	}

	/**
	 * It returns the number of times of the time discretization with which we approximate Black-Scholes model
	 * @return the numbr of times of the time discretization with which we approximate Black-Scholes model
	 */
	@Override
	public int getNumberOfTimes() {
		return numberOfTimes;
	}	
	
	/**
	 * It returns the risk free rate of the approximated Black-Scholes model
	 * @return the risk free rate of the approximated Black-Scholes model
	 */
	public double getRiskFreeRate() {
		return riskFreeRate;
	}

	/**
	 * It returns the volatility of the approximated Black-Scholes model
	 * @return the volatility of the approximated Black-Scholes model
	 */
	public double getVolatility() {
		return volatility;
	}




}
