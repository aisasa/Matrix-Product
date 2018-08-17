package matrmult;

import org.jblas.*;

public class MatrMultBlas {
    public static DoubleMatrix a;
    public static DoubleMatrix b;
    public static DoubleMatrix ab;
    
    public static void main(String[] args) throws InterruptedException{
        a = DoubleMatrix.randn(1000, 1500);
        b = DoubleMatrix.randn(1500, 3000);
        long startT = System.currentTimeMillis();
        ab = a.mmul(b);
        System.out.println("jblas exec time: " + (System.currentTimeMillis() - startT)/1000. + " secs");
        
        //System.out.println(ab.getRow(0).toString());
    }
    
}
    
    