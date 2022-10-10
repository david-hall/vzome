package com.vzome.core.algebra;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import com.vzome.core.algebra.AlgebraicStructures.RationalFieldExtension;

@SuppressWarnings("serial")
public class ContinuedFraction extends Number implements Comparable<ContinuedFraction>, Iterable<Integer> {

    private final RationalFieldExtension<?> value;
    // value is broken up into these parts:
    private final int signum;
    // everything after this point must be positive
    private final int wholePart;
    private final List<Integer> initialSeries = new ArrayList<>();
    private final List<Integer> periodicSeries = new ArrayList<>();
    private final RationalFieldExtension<?> remainder;

    public static void main(String[] args) {
        int max = 9;
        for (int d = 1; d < max; d++) {
            System.out.println();
            for (int n = 1; n < max; n++) {
                ContinuedFraction cf2 = new ContinuedFraction(n, d);
                System.out.println(cf2.getValue() + " = " + cf2);
            }
        }

        AlgebraicField field = new RootTwoField();
        max = 35;
        // for(int i5 = 0; i5 <= max; i5++) {
        // for(int i4 = 0; i4 <= max; i4++) {
        // for(int i3 = 0; i3 <= max; i3++) {
        // for(int i2 = 0; i2 <= max; i2++) {
        for (int i1 = 0; i1 <= max; i1++) {
            for (int u0 = 0; u0 <= 0; u0++) {
                AlgebraicNumber n = field.createAlgebraicNumber(new int[] { u0, i1 });// , i2, i3, i4, i5});
                ContinuedFraction cf2 = new ContinuedFraction(n);
                System.out.println(cf2.getValue() + "\t= " + cf2);
//                {
//                    // TODO: verify that the series iterator spits out the same integers as toString()
//                    // and that none but the first is 0
//                    // and that none are negative
//                    String delim = "; ";
//                    for(int i : cf2) {
//                        System.out.print(i + delim);
//                        delim = ", ";
//                    }
//                    System.out.println();
//                }
                // if(!n.isZero()) {
                // n = n.reciprocal();
                // cf2 = new ContinuedFraction(n);
                // System.out.println(cf2.getValue() + "\t= " + cf2);
                // }
            }
        }
        // }
        // }
        // }
        // }

        {
        System.out.println("\npowers");

        max = 25;
        {
            for (int u = -max; u <= max; u++) {
                AlgebraicNumber n = field.createPower(u);
                ContinuedFraction cf2 = new ContinuedFraction(n);
                System.out.println(cf2.getValue() + "\t= " + cf2);
            }
        }

        System.out.println();
        }

        {
        field = new HeptagonField();
        max = 3;
        Map<Double, ContinuedFraction> map = new HashMap<>();
        for (int irrats2 = 0; irrats2 <= max; irrats2++) {
            for (int irrats1 = 0; irrats1 <= max; irrats1++) {
                for (int u = 0; u <= 0; u++) {
                    AlgebraicNumber n = field.createAlgebraicNumber(new int[] { u, irrats2, irrats1 });
                    ContinuedFraction cf2 = new ContinuedFraction(n);
                    map.put(n.evaluate(), cf2);
                    System.out.println(cf2.getValue() + "\t= " + cf2);
                    if (!n.isZero()) {
                        n = n.reciprocal();
                        cf2 = new ContinuedFraction(n);
                        map.put(n.evaluate(), cf2);
                        System.out.println(cf2.getValue() + "\t= " + cf2);
                    }
                }
            }
        }

        System.out.println();

        map.keySet().stream().sorted().forEach(k -> {
            ContinuedFraction c = map.get(k);
            System.out.println(k + "\t" + c.getValue() + "\t= " + c);
        });

        System.out.println();
        }

        {
        ContinuedFraction cf = new ContinuedFraction(72, 19);
        System.out.println(cf.getValue() + " = " + cf);
        cf = new ContinuedFraction(89, 37);
        System.out.println(cf.getValue() + " = " + cf);
        cf = new ContinuedFraction(2, 3);
        System.out.println(cf.getValue() + " = " + cf);
        cf = new ContinuedFraction(13, 21);
        System.out.println(cf.getValue() + " = " + cf);

        System.out.println();
        }

        final java.util.Random random = new java.util.Random();
        
        {
        int bits = 31;
        int nn = BigInteger.probablePrime(bits, random).intValueExact();
        int dd = BigInteger.probablePrime(bits, random).intValueExact();
        ContinuedFraction cf1 = new ContinuedFraction(nn, dd);
        System.out.println(cf1.getValue() + " = " + cf1);
        ContinuedFraction cf2 = new ContinuedFraction(dd, nn);
        System.out.println(cf2.getValue() + " = " + cf2);

        System.out.println("n : 1/n cf1.compareTo(cf2) = " + cf1.compareTo(cf2));
        System.out.println("int.compare(0, 1)  = " + Integer.compare(0, 1));

        System.out.println();
        }

        {
        field = new RootThreeField();
        int bits = 31;
        int nn = BigInteger.probablePrime(bits, random).intValueExact();
        int dd = BigInteger.probablePrime(bits, random).intValueExact();
        ContinuedFraction cf1 = new ContinuedFraction(new BigRational(nn, dd));
        System.out.println(cf1.getValue() + " = " + cf1);
        ContinuedFraction cf2 = new ContinuedFraction(field.createRational(nn, dd));
        System.out.println(cf2.getValue() + " = " + cf2);

        System.out.println("br:an cf1.compareTo(cf2) = " + cf1.compareTo(cf2));

        System.out.println();
        }

        {
        int bits = 5;
        int nn = BigInteger.probablePrime(bits, random).intValueExact();
        int dd = BigInteger.probablePrime(bits, random).intValueExact();
        // reciprocal makes sure that both are less than 1.
        ContinuedFraction cf1 = new ContinuedFraction((new RootTwoField()).createAlgebraicNumber(new int[] {nn, dd}).reciprocal());
        System.out.println(cf1.getValue() + " = " + cf1);
        ContinuedFraction cf2 = new ContinuedFraction((new RootThreeField()).createAlgebraicNumber(new int[] {nn, dd}).reciprocal());
        System.out.println(cf2.getValue() + " = " + cf2);

        System.out.println("r2 : r3 cf1.compareTo(cf2) = " + cf1.compareTo(cf2));

        System.out.println();
        }

//        {
//        Double d0 = 1d / 3d;
//        Double d1 = 4d / 3d;
//        Double d2 = d1 - 1d;
//        System.out.println(d0);
//        System.out.println(d1 + " - 1.0 = " + d2); // same to 15 decimals
//        }
    }

    // public ContinuedFraction(Number number) {
    // // TODO:
    // }

    public ContinuedFraction(int n) {
        this(new BigRational(n));
    }

    public ContinuedFraction(int numerator, int denominator) {
        this(new BigRational(numerator, denominator));
    }

    public ContinuedFraction(BigRational n) {
        value = n;
        signum = n.signum();
        // everything internally is kept positive
        if (signum == -1) {
            n = n.negate();
        }
        if (signum == 0) {
            wholePart = 0;
            remainder = n.zero();
        } else {
            // TODO: Deal with the case when we have BigInteger terms
//            wholePart = Double.valueOf(n.evaluate()).intValue();
//            if(wholePart != 0) {
//                n = n.minus(n.create(wholePart));
//            }
            int maxReps = Integer.MAX_VALUE;
            int[] whole = { 0 };
            remainder = generateSeries(n, maxReps, whole);
            wholePart = whole[0];
        }
    }

    public ContinuedFraction(AlgebraicNumber n) {
        value = n;
        signum = n.signum();
        // everything internally is kept positive
        if (signum == -1) {
            n = n.negate();
        }
        if (signum == 0) {
            wholePart = 0;
            remainder = n.zero();
        } else {
            // TODO: Deal with the case when we have BigInteger terms
//            wholePart = Double.valueOf(n.evaluate()).intValue();
//            if(wholePart != 0) {
//                n = n.minus(n.create(wholePart));
//            }
            int maxReps = n.getField().getOrder() * 20; // TODO: seems like a reasonable number for now but I need a better plan
            int[] whole = { 0 };
            remainder = generateSeries(n, maxReps, whole);
            wholePart = whole[0];
        }
    }

    protected <T extends RationalFieldExtension<T>> T generateSeries(T n, int maxReps, int[] whole) {
        // According to https://crypto.stanford.edu/pbc/notes/contfrac/periodic.html
        // Any periodic continued fraction 
        //    represents a root of a quadratic equation with integer coefficients.
        // The converse is also true:
        //    Theorem: An irrational root of ax^2 + bx + c = 0 where a, b and c are integers 
        //    has a periodic continued fraction expansion.
        // That means that an AlgebraicField of order > 2 will not be periodic; 
        //    that is, it will not repeat, and any AlgebraicField of order 2 will repeat.
        List<RationalFieldExtension<?>> remainders = new ArrayList<>();
        while (!n.isZero() && (0 < maxReps--)) {
            // TODO: Deal with the case when we have BigInteger terms
            int intPart = Double.valueOf(n.evaluate()).intValue();
            initialSeries.add(intPart);
            n = n.minus(n.create(intPart)); // this is the remainder
            if (n.isZero()) {
                break; // no remainder
            }
            int rep = remainders.indexOf(n);
            if (rep >= 0) {
                List<Integer> repeats = initialSeries.subList(rep, initialSeries.size()); 
                periodicSeries.addAll(repeats);
                // though not obvious, this will actually truncate initialSeries since it backs repeats
                repeats.clear(); 
                break;
            }
            remainders.add(n);
            if (!n.isZero()) {
                n = n.reciprocal();
            }
        }
        if(!initialSeries.isEmpty()) {
            whole[0] = initialSeries.remove(0);
        } else if(!periodicSeries.isEmpty()) {
            whole[0] = periodicSeries.remove(0);
        } else {
            whole[0] = 0;
        }
        return n; // as remainder
    }
    
    public RationalFieldExtension<?> getValue() {
        return value;
    }

    public int wholePart() {
        return wholePart;
    }

    public RationalFieldExtension<?> getRemainder() {
        return remainder;
    }

    public int initialSize() {
        return initialSeries.size();
    }

    public int periodicSize() {
        return periodicSeries.size();
    }

    public List<Integer> getSeries() {
        List<Integer> list = new ArrayList<>(initialSeries.size() + periodicSeries.size());
        list.addAll(initialSeries);
        list.addAll(periodicSeries);
        return list;
    }

    @Override
    public int intValue() {
        return wholePart;
    }

    @Override
    public long longValue() {
        return wholePart;
    }

    @Override
    public float floatValue() {
        return Double.valueOf(value.evaluate()).floatValue();
    }

    @Override
    public double doubleValue() {
        return value.evaluate();
    }

    public boolean isPeriodic() {
        return periodicSeries.size() > 0;
    }

    public boolean isInfinite() {
        return ! remainder.isZero();
    }

    @Override
    public String toString() {
        if (signum == 0) {}
        StringBuilder buf = new StringBuilder();
        buf.append("[");
        buf.append(signum == -1 ? "-" : " ");
        final String padding = " "; // one space
        if (wholePart < 10) {
            buf.append(padding);
        }
        buf.append(wholePart);
        String delim = appendSeries(buf, initialSeries, "; ", padding);
        if(!periodicSeries.isEmpty()) {
            delim = appendSeries(buf, periodicSeries, delim + "{ ", padding);
            buf.append(" }");
        }
        if(isInfinite()) {
            buf.append(" ...");
        }
        buf.append(" ]");
        return buf.toString();
    }
    
    private static String appendSeries(StringBuilder buf, List<Integer> series, String delim, String padding) {
        for (int i : series) {
            buf.append(delim);
            delim = ", ";
            if (i < 10) {
                buf.append(padding);
            }
            buf.append(i);
        }
        return delim;
    }

    /**
     * From https://crypto.stanford.edu/pbc/notes/contfrac/compute.html
     * 
     * Inverting a continued fraction is trivial: if a1 == 0, 
     * then remove this term, otherwise insert 0 at the beginning of the sequence.
     * 
     * Comparison of two continued fractions [a1; a2,...] and [b1; b2,...] 
     * is performed by comparing a[i] to b[i] where i is the smallest index 
     * with a[i] != b[i]. Flip the result if i is even. 
     * If one sequence ends before the other, then treat the missing term as infinity.
     */
    @Override
    public int compareTo(ContinuedFraction that) {
        Iterator<Integer> itThis = this.iterator();
        Iterator<Integer> itThat = that.iterator();
        boolean flip = true;
        int limit1 = this.initialSeries.size() + this.periodicSeries.size();
        int limit2 = that.initialSeries.size() + that.periodicSeries.size();
        int limit = Math.max(limit1, limit2) + 1; // add one for wholePart
        while (limit-- > 0 && itThis.hasNext() && itThat.hasNext()) {
            int comparison = itThis.next().compareTo(itThat.next());
            if (comparison != 0) {
                // I think we flip the result as described above
                // when index is even because the difference 
                // from the true converged value alternates polarity
                // but I need to verify this logic anyway
                return flip ? -comparison : comparison;
            }
            flip = ! flip;
        }
        if (!itThis.hasNext() && !itThat.hasNext()) {
            Double d1 = Double.valueOf(this.remainder.evaluate());
            Double d2 = Double.valueOf(that.remainder.evaluate());
            return d1.compareTo(d2);
        }
        return itThis.hasNext() ? 1 : -1;
    }

    @Override
    public Iterator<Integer> iterator() {
        Iterator<Integer> it = new Iterator<Integer>() {
            int source = 0;
            int next = 0;
            List<Integer> series = initialSeries;

            @Override
            public boolean hasNext() {
                return series != null;
            }

            @Override
            public Integer next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                int result = 0;
                switch(source) {
                case 0:
                    result = signum == -1 ? -wholePart : wholePart;
                    next = 0;
                    source++;
                    if(series.isEmpty()) {
                        source++;
                        series = periodicSeries.isEmpty() ? null : periodicSeries;
                    }
                    return result;

                case 1:
                    result = series.get(next);
                    next++;
                    if (next == series.size()) {
                        source++;
                        next = 0;
                        series = periodicSeries.isEmpty() ? null : periodicSeries;
                    }
                    return result;

                case 2:
                    result = series.get(next);
                    next++;
                    if (next == series.size()) {
                        source++;
                        next = 0;
                        series = null; // indicates that we have iterated over each series once.
                    }
                    return result;
                    
                default:
                    throw new IllegalStateException("Unexpected source: " + source);

                }
            }
        };
        return it;
    }
}
