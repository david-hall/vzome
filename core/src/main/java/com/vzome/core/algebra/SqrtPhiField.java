package com.vzome.core.algebra;

/**
 * @author David Hall
 */
/**
* @deprecated Use com.vzome.fields.sqrtphi.SqrtPhiField instead. Consider moving it back to this package.
*/
@Deprecated
public class SqrtPhiField  extends ParameterizedField<Integer> {
    public static final String FIELD_NAME = "sqrtPhi";
    
    /**
     * 
     * @param radicand
     * @return the coefficients of a SqrtPhiField. 
     * This can be used to determine when two fields have compatible coefficients 
     * without having to generate an instance of the class. 
     * Note that this method provides no validation of the parameter.
     */
    public static double[] getFieldCoefficients() {
        // Note that these coefficients are not in increasing order
        // but rather, the first two are in the same position as the golden subfield
        // then the remaining coefficients are in increasing order.
        // This is not necessary mathematically, but it means that AlgebraicFields.haveSameInitialCoefficients("golden") will return true. 
        // In addition, since createPower works on the first irrational, we want that to be phi whenever applicable.
        final double PHI_VALUE = PentagonField.PHI_VALUE; // ( 1.0 + Math.sqrt( 5.0 ) ) / 2.0;
        return new double[] {
                1.0d,
                PHI_VALUE,
                            Math.sqrt(PHI_VALUE),
                PHI_VALUE * Math.sqrt(PHI_VALUE)
        };
    }
    
    @Override
    public double[] getCoefficients() {
        return getFieldCoefficients();
    }
    
    public SqrtPhiField() {
        super(FIELD_NAME, 4, 0);
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

    /*

    Multiplication table:
    p = phi
    r = sqrt(phi)
    t = phi*sqrt(phi)

      *  |   1  | p   | r   | t
    -----+------+-----+-----+-----
      1  |   1  | p   | r   | t
      p  |   p  | 1+p | t   | r+t
      r  |   r  | t   | p   | 1+p
      t  |   t  | r+t | 1+p | 1+2p

     */
    @Override
    protected void initializeMultiplierMatrix() {
        short[][][] mm = {
            { // 1
              { 1, 0, 0, 0, },
              { 0, 1, 0, 0, },
              { 0, 0, 0, 1, },
              { 0, 0, 1, 1, },
            },
            { // p = phi
              { 0, 1, 0, 0, },
              { 1, 1, 0, 0, },
              { 0, 0, 1, 1, },
              { 0, 0, 1, 2, },
            },
            { // r = sqrt(phi)
              { 0, 0, 1, 0, },
              { 0, 0, 0, 1, },
              { 1, 0, 0, 0, },
              { 0, 1, 0, 0, },
            },
            { // t = phi*sqrt(phi)
              { 0, 0, 0, 1, },
              { 0, 0, 1, 1, },
              { 0, 1, 0, 0, },
              { 1, 1, 0, 0, },
            },
        };
        multiplierMatrix = mm;
    }

    @Override
    protected void initializeLabels() {
        irrationalLabels[1] = new String[]{"\u03C6", "phi"};
        irrationalLabels[2] = new String[]{"\u221A\u03C6", "sqrt(phi)"};
        irrationalLabels[3] = new String[]{"\u03C6\u221A\u03C6", "phi*sqrt(phi)"};
    }
}

/*
coefficientsMultiplied( sqrtPhi ) = 
{
  {     1.00000000000000,     1.61803398874990,     1.27201964951407,     2.05817102727149, },
  {     1.61803398874990,     2.61803398874990,     2.05817102727149,     3.33019067678556, },
  {     1.27201964951407,     2.05817102727149,     1.61803398874990,     2.61803398874990, },
  {     2.05817102727149,     3.33019067678556,     2.61803398874990,     4.23606797749979, },
}

multiplierMatrix( sqrtPhi ) = 
{
  {
    { 1, 0, 0, 0, },
    { 0, 1, 0, 0, },
    { 0, 0, 0, 1, },
    { 0, 0, 1, 1, },
  },
  {
    { 0, 1, 0, 0, },
    { 1, 1, 0, 0, },
    { 0, 0, 1, 1, },
    { 0, 0, 1, 2, },
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

factorsMultiplied( sqrtPhi ) = 
{
  { 1,  φ,  √φ, φ√φ,    },
  { φ,  1+φ,    φ√φ,    √φ+φ√φ, },
  { √φ, φ√φ,    φ,  1+φ,    },
  { φ√φ,    √φ+φ√φ, 1+φ,    1+2φ,   },
}

factorsDivided( sqrtPhi ) = 
{
  { 1,  -1+φ,   -√φ+φ√φ,    2√φ-φ√φ,    },
  { φ,  1,  √φ, -√φ+φ√φ,    },
  { √φ, -√φ+φ√φ,    1,  -1+φ,   },
  { φ√φ,    √φ, φ,  1,  },
}
*/
