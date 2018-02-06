package com.vzome.core.algebra;

/**
 * @author David Hall
 */
public class SnubDodecahedronField extends ParameterizedField<Integer> {

    // TODO: Change this name "SnubDodec" for backward compatibility after we merge the two classes
    public static final String FIELD_NAME = "snubDodecahedron";
    
    /**
     * 
     * @return the coefficients of this AlgebraicField class. 
     * This can be used to determine when two fields have compatible coefficients 
     * without having to generate an instance of the class. 
     */
    public static double[] getFieldCoefficients() {
        return new double[] { 
            1.0d, 
            PHI_VALUE,
                        XI_VALUE,
            PHI_VALUE * XI_VALUE,
                        XI_VALUE * XI_VALUE,
            PHI_VALUE * XI_VALUE * XI_VALUE
        };
    }

    @Override
    public double[] getCoefficients() {
        return getFieldCoefficients();
    }
    
    public static final double PHI_VALUE = PentagonField.PHI_VALUE; // ( 1.0 + Math.sqrt( 5.0 ) ) / 2.0;

    // specified to more precision than a double can retain so that value is as exact as possible: within one ulp().
    public static final double XI_VALUE = 1.71556149969736783d; // root of x^3 -2x -PHI_VALUE 

    
    public SnubDodecahedronField() {
        super("snubDodecahedron", 6, 6);
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
                {1, 0, 0, 0, 0, 0 },
                {0, 1, 0, 0, 0, 0 },
                {0, 0, 0, 0, 0, 1 },
                {0, 0, 0, 0, 1, 1 },
                {0, 0, 0, 1, 0, 0 },
                {0, 0, 1, 1, 0, 0 },
            },
            { // phi
                {0, 1, 0, 0, 0, 0 },
                {1, 1, 0, 0, 0, 0 },
                {0, 0, 0, 0, 1, 1 },
                {0, 0, 0, 0, 1, 2 },
                {0, 0, 1, 1, 0, 0 },
                {0, 0, 1, 2, 0, 0 },
            },
            { // xi
                {0, 0, 1, 0, 0, 0 },
                {0, 0, 0, 1, 0, 0 },
                {1, 0, 0, 0, 2, 0 },
                {0, 1, 0, 0, 0, 2 },
                {0, 0, 2, 0, 0, 1 },
                {0, 0, 0, 2, 1, 1 },
            },
            { // phi * xi
                {0, 0, 0, 1, 0, 0 },
                {0, 0, 1, 1, 0, 0 },
                {0, 1, 0, 0, 0, 2 },
                {1, 1, 0, 0, 2, 2 },
                {0, 0, 0, 2, 1, 1 },
                {0, 0, 2, 2, 1, 2 },
            },
            { // xi^2
                {0, 0, 0, 0, 1, 0 },
                {0, 0, 0, 0, 0, 1 },
                {0, 0, 1, 0, 0, 0 },
                {0, 0, 0, 1, 0, 0 },
                {1, 0, 0, 0, 2, 0 },
                {0, 1, 0, 0, 0, 2 },
            },
            { // phi * xi^2
                {0, 0, 0, 0, 0, 1 },
                {0, 0, 0, 0, 1, 1 },
                {0, 0, 0, 1, 0, 0 },
                {0, 0, 1, 1, 0, 0 },
                {0, 1, 0, 0, 0, 2 },
                {1, 1, 0, 0, 2, 2 },
            },
        };

        multiplierMatrix = mm;
    }

    @Override
    protected void initializeLabels() {
        irrationalLabels[1] = new String[]{ "\u03C6",             "phi" };
        irrationalLabels[2] = new String[]{       "\u03BE",           "xi" };
        irrationalLabels[3] = new String[]{ "\u03C6\u03BE",       "phi*xi" };
        irrationalLabels[4] = new String[]{       "\u03BE\u00B2",     "xi^2" };
        irrationalLabels[5] = new String[]{ "\u03C6\u03BE\u00B2", "phi*xi^2" };
    }

}
