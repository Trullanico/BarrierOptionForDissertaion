package it.univr.dissertation.usefulclass;

/**
 * This class represents the approximation of a Black-Scholes model via the Cox Ross Rubinstein model.
 * It extends ApproximatingBinomialModel. The only method that is implemented here computes the values
 * of the up and down movements of the Binomial model.
 * 
 * @author Andrea Mazzon
 *
 */
public class CoxRossRubinsteinModel extends ApproximatingBinomialModel {
	
	/**
	 * It constructs an object which represents the approximation of a Black-Scholes model via the Cox Ross
	 * Rubinstein model.
	 * 
	 * @param initialPrice, the initial price of the asset modeled by the process
	 * @param riskFreeRate, the number r such that the value of a risk-free bond at time T is e^(rT)
	 * @param volatility, the log-volatility of the Black-Scholes model
	 * @param lastTime, the last time T in the time discretization 0=t_0<t_1<..<t_n=T
	 * @param timeStep, the length t_k-t_{k-1} of the equally spaced time steps that we take for the approximating
	 * time discretization 0=t_0<t_1<..<t_n=T
	 */
	public CoxRossRubinsteinModel(double initialPrice, double riskFreeRate, double volatility, 
			double lastTime, double timeStep) {
		super(initialPrice, riskFreeRate, volatility, lastTime, timeStep);
	}
	
	/**
	 * It constructs an object which represents the approximation of a Black-Scholes model via the Cox Ross
	 * Rubinstein model.
	 * 
	 * @param initialPrice, the initial price of the asset modeled by the process
	 * @param riskFreeRate, the number r such that the value of a risk-free bond at time T is e^(rT)
	 * @param volatility, the log-volatility of the Black-Scholes model
	 * @param lastTime, the last time T in the time discretization 0=t_0<t_1<..<t_n=T
	 * @param numberOfTimes, the number of times in the equally spaced time steps that we take for the approximating
	 * time discretization 0=t_0<t_1<..<t_n=T
	 */
	public CoxRossRubinsteinModel(double initialPrice, double riskFreeRate, double volatility, 
			double lastTime, int numberOfTimes) {
		super(initialPrice, riskFreeRate, volatility, lastTime, numberOfTimes);
	}
	
	/**
	 * It computes and returns the up and down movements of the Binomial model for the Cox-Ross-Rubinstein model
	 * @return an arrays of two elements: the first is the up movement for the Cox-Ross-Rubinstein model,
	 * 		   the second the down one.
	 */
	@Override
	protected double[] getUpAndDownFactorsOfBinomialModel() {
		/*
		 * We use the getters of the parent class here, because the fields are private: we prefer to let them
		 * be private and not protected because firts it is always a good practice, and also because in this way
		 * is is not possible to manipulate them.
		 */
		//see slides
		double upFactor = Math.exp(getVolatility() * Math.sqrt(getTimeStep()));
        double downFactor = 1.0 / upFactor;
        double[] uAndDownFactors = {upFactor, downFactor};
        return uAndDownFactors;
	}
}
