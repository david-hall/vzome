package com.vzome.core.generic;

import static junit.framework.TestCase.assertEquals;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class Utilities {
    private Utilities() {}

    /**
     * When this String is printed to System.out or System.err,
     * the Eclipse console window will generate a hyperlink 
     * to the line of code where this method is called. 
     * @return String containing the file name and line number of the source code 
     * from which this method is called.
     */
    public static String thisSourceCodeLine() {
        return getSourceCodeLine(2);
    }
	
    public static String getSourceCodeLine(int index) {
	// An index of 1 references the immediate calling method
	StackTraceElement ste = new Throwable().getStackTrace()[index]; 
	return "(" + ste.getFileName() + ":" + ste.getLineNumber() + ")";
    }

    public static void compareDoubles(double expected, double value, double delta) {
    	assertEquals(expected, value, delta);
    	if(expected != value) {
    	    System.out.println( value + " does not equal " + expected + ", but is within tolerance of " + delta + " " + getSourceCodeLine(2));
    	}
    }

    public static boolean isPowerOfTwo(int n) {
    	return (n != 0) && ( (n & -n) ==n );
    }
    
    public static boolean isPrime(int n) {
    	final int certainty = 100; // same value as nextProbablePrime()
    	return BigInteger.valueOf(n).isProbablePrime(certainty);
    }
    
    public static List<Integer> primeFactors(int n) {
    	List<Integer> factors = new ArrayList<>();
    	for(int prime = 2; prime <= n; prime = BigInteger.valueOf(prime).nextProbablePrime().intValue()) {
    		while(n % prime == 0) {
    			factors.add(prime);
    			n /= prime;
    		}
    	}
    	return factors;
    }

}
