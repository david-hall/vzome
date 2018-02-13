package com.vzome.core.algebra;

/**
 * @author David Hall
 */
public class SqrtField extends ParameterizedField<Integer> {
    
    /**
     * 
     * @param radicand
     * @return the coefficients of a SqrtField given the same parameter. 
     * This can be used to determine when two fields have compatible coefficients 
     * without having to generate an instance of the class. 
     * Note that this method provides no validation of the parameter.
     */
    public static double[] getFieldCoefficients(int radicand) {
        return new double[] { 1.0d, Math.sqrt(radicand) };
    }

    @Override
    public double[] getCoefficients() {
        return getFieldCoefficients(radicand());
    }
    
    public static final String FIELD_PREFIX = "sqrt";

    protected int perfectSquare;

    public SqrtField(int radicand) {
        this(FIELD_PREFIX + radicand, radicand);
    }

    // this c'tor is only intended to allow RootTwoField and RootThreeField
    // to be derived from SqrtField and still maintain their original name
    protected SqrtField(String name, int radicand) {
        super(name, 2, radicand);
    }
    
    /**
     * MAX_VALUE is limited to a Short because multiplierMatrix is of type Short
     */
    public final static int MAX_VALUE = Short.MAX_VALUE;

    @Override
    protected void validate() {
        if (operand > MAX_VALUE) {
            String msg = "radicand cannot exceed " + MAX_VALUE;
            throw new IllegalArgumentException(msg);
        }
        if (radicand() <= 0) {
            String msg = "radicand " + radicand() + " is not positive.";
            throw new IllegalArgumentException(msg);
        }
    }
    
    @Override
        protected void initializeNormalizer() {
        Double squareRoot = Math.sqrt( radicand() );
        double floor = Math.floor(squareRoot);
        if(floor * floor == radicand()) {
                perfectSquare = squareRoot.intValue();
                normalizer = SqrtField::normalizePerfectSquare; 
        } else {
                perfectSquare = -1;
        }
    }
    
    private static void normalizePerfectSquare(AlgebraicField field, BigRational[] factors) {
        if(!factors[1].isZero() ) {
            factors[0] = factors[0].plus( factors[1].times( ((SqrtField)field).perfectSquare ) );
            factors[1] = BigRational.ZERO;
        }
    }

    @Override
    protected void initializeLabels() {
        String r = Integer.toString(radicand());
        irrationalLabels[1] = new String[] {"\u221A" + r, "sqrt(" + r + ")"};
    }

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
        short u = 0;
        short r = 1;
        if (isPerfectSquare()) {
            u = 1;
            r = 0;
        }
        short[][][] mm = {
            { // units
                {1, u},
                {u, radicand()},
            },
            { // rootN
                {0, r},
                {r, 0},
            }
        };
        multiplierMatrix = mm;
    }

    public Double sqrt() {
        return coefficients[1];
    }

    public boolean isPerfectSquare() {
        return perfectSquare >= 0;
    }

    public short radicand() {
        return operand.shortValue();
    }

    private AlgebraicNumber defaultStrutScaling;
    @Override
    public AlgebraicNumber getDefaultStrutScaling() {
        if(defaultStrutScaling == null) {
            // we start with this value just because we did in RootTwoField and RootThreeField
            defaultStrutScaling = createAlgebraicNumber(1, 0, 2, -3);
        }
        return defaultStrutScaling;
    }
}
