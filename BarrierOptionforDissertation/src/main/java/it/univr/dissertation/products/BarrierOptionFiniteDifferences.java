package it.univr.dissertation.products;

import java.util.function.DoubleUnaryOperator;

import it.univr.dissertation.usefulclass.FDMThetaMethodForKnockOutOption;
import net.finmath.finitedifference.models.FiniteDifference1DBoundary;
import net.finmath.finitedifference.models.FiniteDifference1DModel;
import net.finmath.finitedifference.products.FiniteDifference1DProduct;

public class BarrierOptionFiniteDifferences implements FiniteDifference1DProduct, FiniteDifference1DBoundary {
	private final double maturity;
	private final double strike;
	private final double lowerBarrier;
	private final double upperBarrier;
	private final double theta;
	private final double callorPut; 

	/**
	 * It constructs an object representing a barrier, European option on an underlying X. The underlying is 
	 * @param optionMaturity The maturity T in the option payoff 
	 * @param optionStrike The strike K in the option payoff 
	 * @param lowerBarrier the lower barrier B_L in the option payoff 
	 * @param upperBarrier the upper barrier B_U in the option payoff 
	 * @param theta is a parameter that determines the weighting between the implicit and explicit parts of the scheme
	 * @param callorPutSign Set 1 for call, set -1 for put
	 */
	public BarrierOptionFiniteDifferences(final double optionMaturity, final double optionStrike,
			final double lowerBarrier, final double upperBarrier, final double theta, double callorPutSign) {
		maturity = optionMaturity;
		strike = optionStrike;
		this.lowerBarrier = lowerBarrier;
		this.upperBarrier = upperBarrier;
		this.theta = theta;
		this.callorPut = callorPutSign;
		
	}
	
	//method that give value without barrier in order to exploit it for in-out parity
	
	public double[][] getValueWithoutBarrier(final double evaluationTime, final FiniteDifference1DModel model) {/*
		 * The FDM algorithm requires the boundary conditions of the product.
		 * This product implements the boundary interface
		 */
		final FiniteDifference1DBoundary boundary = this;
		final FDMThetaMethodForKnockOutOption solver = new FDMThetaMethodForKnockOutOption(model, boundary, maturity, theta, 0, Long.MAX_VALUE);
		
		if (callorPut == 1) {
			
			return solver.getValue(evaluationTime, maturity, new DoubleUnaryOperator() {
				@Override
				public double applyAsDouble(final double assetValue) {
					return Math.max(assetValue - strike, 0);
				}
			});}
			else return solver.getValue(evaluationTime, maturity, new DoubleUnaryOperator() {
				@Override
				public double applyAsDouble(final double assetValue) {
					return Math.max(strike - assetValue, 0);
				}
		
		});
	}

	@Override
	public double[][] getValue(final double evaluationTime, final FiniteDifference1DModel model) {
		/*
		 * The FDM algorithm requires the boundary conditions of the product.
		 * This product implements the boundary interface
		 */
		final FiniteDifference1DBoundary boundary = this;
		final FDMThetaMethodForKnockOutOption solver = new FDMThetaMethodForKnockOutOption(model, boundary, maturity, theta, lowerBarrier, upperBarrier);
		
		if (callorPut == 1) {
		
		System.out.println( "You set Call Option ");
		
		return solver.getValue(evaluationTime, maturity, new DoubleUnaryOperator() {
			
		
			@Override
			public double applyAsDouble(final double assetValue) {
				return Math.max(assetValue - strike, 0);
			}
		});}
		else {
			System.out.println( "You set Put Option");
			return solver.getValue(evaluationTime, maturity, new DoubleUnaryOperator() {
		
			
			@Override
			public double applyAsDouble(final double assetValue) {
				return Math.max(strike - assetValue, 0);
			}
		});}}

			
	
	/*
	 * Implementation of the interface:
	 * @see net.finmath.finitedifference.products.FiniteDifference1DBoundary#getValueAtLowerBoundary(net.finmath.finitedifference.models.FDMBlackScholesModel, double, double)
	 */

	@Override
	public double getValueAtLowerBoundary(final FiniteDifference1DModel model, final double currentTime, final double stockPrice) {
		return 0;
	}

	@Override
	public double getValueAtUpperBoundary(final FiniteDifference1DModel model, final double currentTime, final double stockPrice) {
		return 0;
	}
}


