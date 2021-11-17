package com.vzome.core.algebra;

public class SqrtGoldenNumber {

    /**
     * If the radicand is null, 0 or 1, the radicand itself is returned. 
     * If the radicand is negative then null is returned. 
     * If the gcd of all denominators is not a perfect square then null is returned. 
     * If the radicand can be expressed as a+bφ where a and b are rationals 
     * and if it is a perfect square, then its square root will be returned.
     * If the radicand can't be expressed as a+bφ, either because the field does not support the golden ratio,
     * or because additional terms are present, as is possible in higher order fields, 
     * then an exception will be thrown. This method will correctly handle fields of order > 2 (e.g. SnubDodecField) 
     * and those where φ is an AlgebraicNumber with more than one non-zero term (e.g. Polygon(10)).
     * @param n is the radicand
     * @return The square root of {@code n} if the specified criteria are met. Otherwise null.
     */
    public static AlgebraicNumber solve(final AlgebraicNumber n) {
        AlgebraicNumberImpl radicand = (AlgebraicNumberImpl) n;
        if(radicand == null || radicand.isOne() || radicand.isZero()) {
            return radicand;
        }
        if(radicand.signum() < 0 ) {
            // negative
            return null;
        }
        AlgebraicField field = radicand.getField();
        final AlgebraicNumberImpl phi = (AlgebraicNumberImpl)field.getGoldenRatio();
        if(phi == null)
            throw new IllegalArgumentException(field.getName() + " does not include the golden ratio.");
        // Scale up radicand so we can work with integers instead of rationals
        // TODO: toTrailingDivisor() fails on int overflows so use some sort of BigInteger or BigDecimal gcd method 
        // See BigDecimal BigRationalImpl.toBigDecimal(BigRationalImpl that)
        // or add the rational coefficients then use BigInteger gcd = BigRationalImpl.getDenominator()
        int[] radicandTd = radicand.toTrailingDivisor();
        int gcd = radicandTd[field.getOrder()];
        int sqrtGcd = Double.valueOf(Math.sqrt(gcd)).intValue();
        AlgebraicNumberImpl scalar = (AlgebraicNumberImpl)field.createRational(gcd);
        AlgebraicNumberImpl sqrtScalar = (AlgebraicNumberImpl)field.createRational(sqrtGcd);
        if(sqrtGcd * sqrtGcd != gcd) {
            // If gcd is a multiple of 5 then we may still have a perfect sqrt involving phi = (1+sqrt5)/2
            // so we can't return null yet, even if gcd is not a perfect square
            // TODO: This optimization does not generalize and it is only good for the golden field as-is
            if(gcd % 5 != 0) {
                // a quick test here is faster than letting the recursive call return null;
                // but the algorithm will work without this line.
                return null;
            }
            // recursively find sqrt of gcd. Since it is an integer, we can't recurse more than once.
            sqrtScalar = (AlgebraicNumberImpl)solve(field.createRational(gcd));
            if(sqrtScalar == null) {
                return null;
            }
        }
        radicand = (AlgebraicNumberImpl) radicand.times(scalar);
        if(radicand.isRational()) {
            // use simpler math for rationals
            long sqrt = Double.valueOf(Math.sqrt(radicand.evaluate())).longValue();
            if(sqrt * sqrt == radicand.evaluate())
                // radicand is a perfect square rational 
                return field.createRational(sqrt).dividedBy(sqrtScalar);
        }
        BigRational[] radTerms = radicand.getFactors();
        BigRational[] testTerms = phi.getFactors();
        BigRational[] phisTerms = ((AlgebraicNumberImpl)field.zero()).getFactors();
        int keyTerm = testTerms.length;
        for(int i=testTerms.length-1; i >=0; i--) {
            if(!testTerms[i].isZero()) {
                keyTerm = i;
                break;
            }
        }
        for(int i=0; i < testTerms.length; i++) {
            if(!testTerms[i].isZero()) {
                if(i == keyTerm) {
                    phisTerms[i] = radTerms[i];
                } else {
                    // This is OK for 5N-gon fields but not for generalizing to multiple irrats 
                    phisTerms[i] = radTerms[keyTerm].negate();
                }
            }
        }
        AlgebraicNumberImpl phiTerm = new AlgebraicNumberImpl(field, phisTerms);
        // See in PolygonField.convertGoldenNumberPairs() how the units and phis 
        // will collide in the case of PolygonField(10).
        // In most supported fields, radTerms[0] is all we need to generate units, but not so in Polygon(10).
        // Instead, by always subtracting phiTerm from radicand to derive units,
        // this algorithm now works when phis and units overlap as in Polygon(10).
        // However, this won't generalize to using two irrationals without some additional work
        // to determine which term(s) in the two irrats might collide.
        // In the case of phi, we know the high term never collides with units.
        AlgebraicNumber units = radicand.minus(phiTerm); 
        BigRational[] unitsTerms = ((AlgebraicNumberImpl)units).getFactors();
        
        if(! units.plus(phiTerm).equals(radicand)) 
            throw new IllegalArgumentException(radicand + " cannot be scaled to a+bφ where a and b are integers.");
        AlgebraicNumberImpl phis = (AlgebraicNumberImpl) phiTerm.dividedBy(phi);
        if(!phis.isRational()) {
            throw new IllegalStateException(field.getName() + ": " + radicand + ": Algorithm error. Unable to calculate a rational value for phis.");
        }
        
        // From here on, I'll use variable names mostly based on the discussion at
        // https://math.stackexchange.com/questions/4294165/is-a-b2-cd-solvable-for-a-b-given-c-d-when-a-b-c-d-are-all-int
        // At this point, we should have two valid values for c and d.
        
        BigRational c = unitsTerms[0];
        BigRational d = phis.getFactors()[0];
        
        if(c.isNegative())
            return null; // see example #2 in the discussion
        // d may be zero if c is not a perfect square handled earlier (e.g. c=1805).
        // discriminant is called D in the discussion
        // D=16(c²+cd-d²)
        BigRational discriminant = c.times(c).plus(c.times(d)).minus(d.times(d)).timesInt(16); 
//        System.out.println("D=" + discriminant);
        if(discriminant.isNegative())
            return null;
        long sqrtDiscriminant = Double.valueOf(Math.sqrt(discriminant.evaluate())).longValue();
        if(sqrtDiscriminant * sqrtDiscriminant != discriminant.evaluate())
            return null; // discriminant is not a perfect square
        // sqrtDiscriminant is called R in the discussion
        BigRational bigR = new BigRationalImpl(sqrtDiscriminant);
//        System.out.println("R=" + bigR);
        // B=(-(-2(d+2c)) ± R)/2(5)
        // B=(2(d+2c) ± R)/10
        // k=(2(d+2c)
        //   plusB=(k + R)/10
        // minusB(=(k - R)/10
        BigRational k = d.plus(c.timesInt(2)).timesInt(2);
        BigRational oneTenth=new BigRationalImpl(1,10);
        BigRational bigBs[] = new BigRational[] {
                k.plus(bigR).times(oneTenth),
                k.minus(bigR).times(oneTenth)
        };
        for(BigRational bigB : bigBs) {
            if(bigB.isZero())
                continue;
            long sqrtB = Double.valueOf(Math.sqrt(bigB.evaluate())).longValue();
            if(sqrtB * sqrtB == bigB.evaluate()) {
                BigRationalImpl b = new BigRationalImpl(sqrtB);
                if(!b.isWhole())
                    continue;
                // a=(d-b²)/2b
                BigRational a = d.minus(b.times(b)).times(b.timesInt(2).reciprocal());
                if(((BigRationalImpl)a).isWhole()) {
                    long nUnits = Double.valueOf(a.evaluate()).longValue();
                    int nPhis = Double.valueOf(b.evaluate()).intValue();
                    AlgebraicNumber result = field.createRational(nUnits)
                            .plus(phi.timesInt(nPhis))
                            .times(sqrtScalar.reciprocal());
                    if(result.signum() < 0) {
                        result = result.negate();
                    }
                    return result;
                }
            }
        }
        return null;
    }
}
