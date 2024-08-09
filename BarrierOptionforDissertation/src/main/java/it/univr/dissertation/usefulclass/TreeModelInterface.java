package it.univr.dissertation.usefulclass;

import java.util.function.DoubleUnaryOperator;


/**
 * This is an interface which can be implemented by anzy class which represents a tree model.
 * 
 * @author Andrea Mazzon
 *
 */
public interface TreeModelInterface {
	
	double[] getValuesAtGivenTimeIndex(int timeIndex);
	
	double[] getTransformedValuesAtGivenTimeIndex(int timeIndex, DoubleUnaryOperator transformFunction);
	
	double[] getConditionalExpectation(double[] trinomialValues,int timeIndex);
}
