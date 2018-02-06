package com.vzome.core.algebra;

/**
 * @author David Hall
 */
public class SnubCubeField  extends ParameterizedField<Integer> {
    public static final String FIELD_NAME = "snubCube";
    
    /**
     * 
     * @param radicand
     * @return the coefficients of a SqrtPhiField. 
     * This can be used to determine when two fields have compatible coefficients 
     * without having to generate an instance of the class. 
     * Note that this method provides no validation of the parameter.
     */
    public static double[] getFieldCoefficients() {
        // Tribonacci constant is a root of x^3 − x^2 − x − 1 and satisfies x + x^(−3) = 2
        final double tribonacciConstant = (1.0d
                + Math.cbrt(19.0d - (3.0d * Math.sqrt(33))) // this term has a minus in the middle
                + Math.cbrt(19.0d + (3.0d * Math.sqrt(33))) // this term has a plus  in the middle
                ) / 3.0d;
        return new double[] {
                1.0d,
                tribonacciConstant,
                tribonacciConstant * tribonacciConstant
        };
    }
    
    @Override
    public double[] getCoefficients() {
        return getFieldCoefficients();
    }
    
    public SnubCubeField() {
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
                {0, 1, 1,},
            },
            { // psi
                {0, 1, 0,},
                {1, 0, 1,},
                {0, 1, 2,},
            },
            { // psi^2
                {0, 0, 1,},
                {0, 1, 1,},
                {1, 1, 2,},
            },
        };

        multiplierMatrix = mm;
    }

    @Override
    protected void initializeLabels() {
        irrationalLabels[1] = new String[]{"\u03C8", "psi"};
        irrationalLabels[2] = new String[]{"\u03C8\u00B2", "psi^2"};
    }
}
