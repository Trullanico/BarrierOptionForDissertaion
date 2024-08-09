package it.univr.dissertation.usefulclass;



import net.finmath.randomnumbers.RandomNumberGenerator;

import org.apache.commons.math3.random.Well19937c;

public class LinearCongruentialGenrator implements RandomNumberGenerator {

	   /**
	 * 
	 */
	private static final long serialVersionUID = -7203498180754925124L;
	
	private final Well19937c well19937c;
	   private final int dimension  ;

	    public LinearCongruentialGenrator(int dimension) {
	        this.dimension = dimension;
	        this.well19937c = new Well19937c(); // Inizializzazione di well19937c nel costruttore
	    }

    @Override
    public double[] getNext() {
        double[] sample = new double[dimension];
        for (int i = 0; i < dimension; i++) {
            sample[i] = well19937c.nextDouble();
        }
        return sample;
    }

    @Override
    public int getDimension() {
        return dimension;
    }
}


