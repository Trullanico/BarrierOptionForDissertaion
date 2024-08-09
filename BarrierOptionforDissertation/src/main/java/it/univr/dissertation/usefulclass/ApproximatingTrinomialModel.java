package it.univr.dissertation.usefulclass;



/**
 * This class implements a discrete trinomial model which approximates the continuous Black-Scholes model
 * for "small" length of the time discretization 0=t_0<t_1<..<t_n=T.
 * In particular, this is an abstract class which get extended by (say for now) one class that represents
 * the Boyle model. The only abstract method, which gets implemented in the derived classes, takes
 * care of computing the up factor and up probability with which we construct an object of type TrinomialModel.
 * 
 * 
 * @author Andrea Mazzon
 *
 */
public abstract class ApproximatingTrinomialModel extends ApproximatingTreeModelForBlackScholes {

	/**
	 * It constructs an object of type ApproximatingTrinomialModel.
	 * 
	 * @param initialPrice, the initial price of the asset modeled by the process
	 * @param riskFreeRate, the number r such that the value of a risk-free bond at time T is e^(rT)
	 * @param volatility, the log-volatility of the Black-Scholes model
	 * @param lastTime, the last time T in the time discretization 0=t_0<t_1<..<t_n=T
	 * @param timeStep, the length t_k-t_{k-1} of the equally spaced time steps that we take for the approximating
	 * time discretization 0=t_0<t_1<..<t_n=T
	 */
	public ApproximatingTrinomialModel(double initialPrice, double riskFreeRate, double volatility, 
			double lastTime, double timeStep) {
		super(initialPrice, riskFreeRate, volatility, lastTime, timeStep);
	}


	/**
	 * It constructs an object of type ApproximatingTrinomialModel.
	 * 
	 * @param initialPrice, the initial price of the asset modeled by the process
	 * @param riskFreeRate, the number r such that the value of a risk-free bond at time T is e^(rT)
	 * @param volatility, the log-volatility of the Black-Scholes model
	 * @param lastTime, the last time T in the time discretization 0=t_0<t_1<..<t_n=T
	 * @param numberOfTimes, the number of times in the equally spaced time steps that we take for the approximating
	 * time discretization 0=t_0<t_1<..<t_n=T
	 */
	public ApproximatingTrinomialModel(double initialPrice, double riskFreeRate, double volatility,
			double lastTime, int numberOfTimes) {
		super(initialPrice, riskFreeRate, volatility, lastTime, numberOfTimes);
	}


	//this is an abstract method which gets implemented in the derived class
	protected abstract double[] getUpFactorAndUpProbabilityOfTrinomialModel();

	
	// In this method we generate the TrinomialModel, once we know up factor and up probability
	@Override
	protected void generateTreeModel() {
		double probabilityUp = getUpFactorAndUpProbabilityOfTrinomialModel()[0];
		double upFactor = getUpFactorAndUpProbabilityOfTrinomialModel()[1];
		
		int numberOfTimes = getNumberOfTimes();
		double initialPrice = getInitialPrice();
		double riskFreeRate = getRiskFreeRate();
		double timeStep = getTimeStep();

		double riskFreeFactorForTrinomialModel = Math.exp(riskFreeRate * timeStep) - 1;

		ourTreeModel = new TrinomialModel(probabilityUp, upFactor,  riskFreeFactorForTrinomialModel, initialPrice, numberOfTimes);
	}

}
