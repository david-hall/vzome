package com.vzome.core.algebra;

/**
 * @author David Hall
 */
public class PlasticNumberField  extends ParameterizedField<Integer> {
    public static final String FIELD_NAME = "plasticNumber";
    
    /**
     * 
     * @param radicand
     * @return the coefficients of a PlasticNumberField. 
     * This can be used to determine when two fields have compatible coefficients 
     * without having to generate an instance of the class. 
     * Note that this method provides no validation of the parameter.
     */
    public static double[] getFieldCoefficients() {
        final double plasticNumber = 1.32471795724475d;
        return new double[] {
                1.0d,
                plasticNumber,
                plasticNumber * plasticNumber
        };
    }
    
    @Override
    public double[] getCoefficients() {
        return getFieldCoefficients();
    }
    
    public PlasticNumberField() {
        super(FIELD_NAME, 3, 0);
    }

    @Override
    protected void validate() {}
    
    @Override
    protected void initializeCoefficients() {
        double[] temp = getCoefficients();
        int i = 0;
        for(double coefficient : temp) {
            coefficients[i++] = coefficient;
        }
    }

    @Override
    protected void initializeMultiplierMatrix() {
        short[][][] mm = {
            { // 1
                {1, 0, 0,},
                {0, 0, 1,},
                {0, 1, 0,},
            },
            { // PHI
                {0, 1, 0,},
                {1, 0, 1,},
                {0, 1, 1,},
            },
            { // PHI^2
                {0, 0, 1,},
                {0, 1, 0,},
                {1, 0, 1,},
            },
        };

        multiplierMatrix = mm;
    }

    @Override
    protected void initializeLabels() {
        // according to http://archive.bridgesmathart.org/2000/bridges2000-87.pdf
        // Marting Gardner referred to the square of the plastic number as High-Phi. 
        // I'm going to use the capital PHI here.
        irrationalLabels[1] = new String[]{"\u03A6", "P"};
        irrationalLabels[2] = new String[]{"\u03A6\u00B2", "P^2"};
    }
}
