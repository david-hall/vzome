package com.vzome.core.algebra;

/**
 * @author David Hall
 *
 * When the radicand is 3, this field is the basis for the vertices
 * of the 4D Triangular Hebesphenorotundaeic Rhombochoron
 * as described at http://eusebeia.dyndns.org/4d/J92_rhombochoron
 * See also http://eusebeia.dyndns.org/4d/bipara2gyrC1010
 * and http://eusebeia.dyndns.org/4d/smbr which both use a root2 field
 * and http://eusebeia.dyndns.org/4d/cubicpyramid which uses only integers.
 * The vertices of numerous other regular polychora are described at http://eusebeia.dyndns.org/4d/uniform
 */
public class PhiPlusSqrtField extends ParameterizedField<Integer> {

    public static final String FIELD_PREFIX = "phiPlusSqrt";

    private static final PolygonField PHI_SUBFIELD = new PolygonField(5);
    
    /**
     * 
     * @param radicand
     * @return the coefficients of a PhiPlusSqrtField given the same parameter. 
     * This can be used to determine when two fields have compatible coefficients 
     * without having to generate an instance of the class. 
     * Note that this method provides no validation of the parameter.
     */
    public static double[] getFieldCoefficients(int radicand) {
        // Note that these coefficients are not necessarily in increasing order, depending on the operand
        // but rather, the first two are in the same position as the golden subfield
        // then the remaining coefficients are in increasing order.
        // This is not necessary mathematically, but it means that AlgebraicFields.haveSameInitialCoefficients("golden") will return true. 
        // In addition, since createPower works on the first irrational, we want that to be phi whenever applicable.
        final double PHI_VALUE = PentagonField.PHI_VALUE; // ( 1.0 + Math.sqrt( 5.0 ) ) / 2.0;
        final double squareRoot = ( new SqrtField( radicand) ).sqrt();
        return new double[] {
                1.0d,
                PHI_VALUE,
                squareRoot,
                PHI_VALUE * squareRoot // Never used for a normalized perfect square
        };
    }
    
    @Override
    public double[] getCoefficients() {
        return getFieldCoefficients(operand);
    }
    
    private final SqrtField sqrtSubField;

    protected int perfectSquare;

    public PhiPlusSqrtField(int radicand) {
        super(FIELD_PREFIX + radicand, 4, radicand);
        sqrtSubField = new SqrtField(radicand);
        initialize();
    }

    @Override
    protected void initialize() {
        if(sqrtSubField != null) {  // sqrtSubField will be null during c'tor call to super()
            super.initialize();
        }
    }

    @Override
    protected void validate() {
        sqrtSubField.validate();
    }

    @Override
    protected void initializeCoefficients() {
        double[] temp = getCoefficients();
        int i = 0;
        for(double coefficient : temp) {
            coefficients[i++] = coefficient;
        }
    }

    /*

    Multiplication table:
    N = operand

    p = phi
    s =     sqrt(N)
    t = phi*sqrt(N) = s*p

    When N is a perfect square, s will be an integer.
      *  |  1  |  p    |  s   |  t
    -----+-----+-------+------+------
      1  |  1  |  p    |  s   |  t
      p  |  p  |  1+p  |  t   |  s+t
      s  |  s  |  t    |  N   |  Np
      t  |  t  |  s+t  |  Np  | 1N+Np

    *********************************************************************
    Perfect square example:
    N = 9
    s = 3
    t = 3p
      *  |  1  |  p    |  3   |  3p
    -----+-----+-------+------+------
      1  |  1  |  p    |  3   |  3p
      p  |  p  |  1+p  |  3p  |  3+3p
      3  |  3  |  3p   |  9   |  9p
      3p |  3p |  3+3p |  9p  |  9+9p

    *********************************************************************
    Non-perfect square example:
    N = 7
    s = sqrt(N)
    t = sp
      *  |  1  |  p    |  s   |  t
    -----+-----+-------+------+------
      1  |  1  |  p    |  s   |  t
      p  |  p  |  1+p  |  t   |  s+t
      s  |  s  |  t    |  7   |  7p
      t  |  t  |  s+t  |  7p  |  7+7p

    *********************************************************************
     */
    @Override
    protected void initializeMultiplierMatrix() {
        final short r = sqrtSubField.radicand();
        if(sqrtSubField.isPerfectSquare()) {
            short[][][] mm = {
                { // 1
                  { 1, 0, 0, 0, },
                  { 0, 1, 0, 0, },
                  { 0, 0, r, 0, },
                  { 0, 0, 0, r, },
                },
                { // p = phi
                  { 0, 1, 0, 0, },
                  { 1, 1, 0, 0, },
                  { 0, 0, 0, r, },
                  { 0, 0, r, r, },
                },
                { // s = sqrt(N)
                  { 0, 0, 1, 0, },
                  { 0, 0, 0, 1, },
                  { 1, 0, 0, 0, },
                  { 0, 1, 0, 0, },
                },
                { // t = phi*sqrt(N)
                  { 0, 0, 0, 1, },
                  { 0, 0, 1, 1, },
                  { 0, 1, 0, 0, },
                  { 1, 1, 0, 0, },
                },
            };
            multiplierMatrix = mm;
        } else {
            // NOT a Perfect Square
            short[][][] mm = {
                { // 1
                  { 1, 0, 0, 0, },
                  { 0, 1, 0, 0, },
                  { 0, 0, r, 0, },
                  { 0, 0, 0, r, },
                },
                { // p = phi
                  { 0, 1, 0, 0, },
                  { 1, 1, 0, 0, },
                  { 0, 0, 0, r, },
                  { 0, 0, r, r, },
                },
                { // s = sqrt(N)
                  { 0, 0, 1, 0, },
                  { 0, 0, 0, 1, },
                  { 1, 0, 0, 0, },
                  { 0, 1, 0, 0, },
                },
                { // t = phi*sqrt(N)
                  { 0, 0, 0, 1, },
                  { 0, 0, 1, 1, },
                  { 0, 1, 0, 0, },
                  { 1, 1, 0, 0, },
                },
            };
            multiplierMatrix = mm;
        }
    }

    @Override
	protected void initializeNormalizer() {
    	perfectSquare = sqrtSubField.perfectSquare;
        if(sqrtSubField.isPerfectSquare()) {
            normalizer = PhiPlusSqrtField::normalizePerfectSquare; 
        }
    }
    
    private static void normalizePerfectSquare(AlgebraicField field, BigRational[] factors) {
    	int perfectSquare = ((PhiPlusSqrtField)field).perfectSquare;
        if(!factors[2].isZero()) {
            // move sqrt(N) factor to unit factor
            factors[0] = factors[0].plus(factors[2].times(perfectSquare));
            factors[2] = BigRational.ZERO;
        }
        if(!factors[3].isZero()) {
            // move phi*sqrt(N) factor to phi factor
            factors[1] = factors[1].plus(factors[3].times(perfectSquare));
            factors[3] = BigRational.ZERO;
        }
    }

    @Override
    protected void initializeLabels() {
        irrationalLabels[1] = new String[]{ PHI_SUBFIELD.getIrrational(1,0), PHI_SUBFIELD.getIrrational(1,1)};
        irrationalLabels[2] = new String[]{ sqrtSubField.getIrrational(1,0), sqrtSubField.getIrrational(1,1)};
        irrationalLabels[3] = new String[]{ irrationalLabels[1][0] +       irrationalLabels[2][0],
                                            irrationalLabels[1][1] + "*" + irrationalLabels[2][1] }; //"phi*sqrt(#)"
    }

    public SqrtField getSqrtSubField() {
        return sqrtSubField;
    }

}

/*
coefficientsMultiplied( phiPlusSqrt3 ) = 
{
  {     1.00000000000000,     1.61803398874990,     1.73205080756888,     2.80251707688815, },
  {     1.61803398874990,     2.61803398874990,     2.80251707688815,     4.53456788445702, },
  {     1.73205080756888,     2.80251707688815,     3.00000000000000,     4.85410196624968, },
  {     2.80251707688815,     4.53456788445702,     4.85410196624968,     7.85410196624968, },
}

multiplierMatrix( phiPlusSqrt3 ) = 
{
  {
    { 1, 0, 0, 0, },
    { 0, 1, 0, 0, },
    { 0, 0, 3, 0, },
    { 0, 0, 0, 3, },
  },
  {
    { 0, 1, 0, 0, },
    { 1, 1, 0, 0, },
    { 0, 0, 0, 3, },
    { 0, 0, 3, 3, },
  },
  {
    { 0, 0, 1, 0, },
    { 0, 0, 0, 1, },
    { 1, 0, 0, 0, },
    { 0, 1, 0, 0, },
  },
  {
    { 0, 0, 0, 1, },
    { 0, 0, 1, 1, },
    { 0, 1, 0, 0, },
    { 1, 1, 0, 0, },
  },
}

factorsMultiplied( phiPlusSqrt3 ) = 
{
  { 1,  φ,  √3, φ√3,    },
  { φ,  1+φ,    φ√3,    √3+φ√3, },
  { √3, φ√3,    3,  3φ, },
  { φ√3,    √3+φ√3, 3φ, 3+3φ,   },
}

factorsDivided( phiPlusSqrt3 ) = 
{
  { 1,  -1+φ,   1/3√3,  -1/3√3+1/3φ√3,  },
  { φ,  1,  1/3φ√3, 1/3√3,  },
  { √3, -√3+φ√3,    1,  -1+φ,   },
  { φ√3,    √3, φ,  1,  },
}
*/
