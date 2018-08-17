package matrmult;

import java.util.*;

public class MatrMultArrs {
    static final int MIN_ELEMS = 160000;    // Threading appears to work better from this # of elements
    static double[][] a;                    // 'A' matrix      
    static double[][] b;                    // 'B' matrix\
    static double[][] ab;                   // A·B matrix product
    static int nProc;                       // Number of cores in system

    public static void main(String[] args) throws InterruptedException{
        // Get number of processing cores in system
        nProc = Runtime.getRuntime().availableProcessors();
        // Initialize matrices values
        initMatr();             
        // Check dimensions
        if(a[0].length != b.length) throw new RuntimeException("No matching dimensiones in matrices");
        // Choose efficient product according number of elements
        /*if((a.length * a[0].length) >= MIN_ELEMS)
            ab = threadedProduct(a, b); // Threaded dot product
        else
            ab = transpProduct(a, b);   // Transposed dot product */
        
        // Tests
        long startT;
        startT = System.currentTimeMillis();
        ab = threadedProduct(a, b);     // Threaded dot product
        System.out.println("Threaded exec time: " + (System.currentTimeMillis() - startT)/1000. + " secs");
        /*startT = System.currentTimeMillis();
        mtrxProduct = transpProduct(a, b);       // Transposed dot product
        System.out.println("Transposed exec time: " + (System.currentTimeMillis() - startT)/1000. + " secs");    */
            
        // Show results if wanted
        //System.out.println("a·b excerpt= " + Arrays.toString(ab[0]));    // Partial result test
        //System.out.println("a·b = " + Arrays.deepToString(ab));          // The complete result
    }   
    
    public static double[][] threadedProduct(double[][] aMtrx, double[][] bMtrx) throws InterruptedException{
        // Compute transpose of bMtrx
        double[][] bMtrxT = mtrxTranspose(bMtrx);
        // Get number of rows in each chunk (piece of work) of matrix A
        int rowsInChunksAvrg = aMtrx.length/nProc;
        int remain = aMtrx.length%nProc;           
        // Product matrix definition
        double[][] prodMtrx = new double[aMtrx.length][bMtrx[0].length]; 
        // Go threading
        Thread[] threads = new Thread[nProc];
        int index = 0;                      // Indexing the 'aMtrx' matrix chunks
        for(int i=0; i<nProc; i++){         // For each core:
            // Compute number of rows in this submatrix
            int rowsInChunk = rowsInChunksAvrg + (i < remain ? 1 : 0);
            // Initializing submatrix of A
            double[][] aChunk = new double[rowsInChunk][aMtrx[0].length];   
            System.arraycopy(aMtrx, index, aChunk, 0, rowsInChunk); 
            // Launch thread
            (threads[i] = new Thread(new ChunkDotProd(aChunk, bMtrxT, prodMtrx, index, rowsInChunk))).start(); 
            // Update index signaling submatrix rows in A
            index = index + rowsInChunk; 
        }
        for(int i=0; i<nProc; i++)          // For each thread...
            threads[i].join();              // ...wait until it ends (all then)
        return prodMtrx;
    }
    private static class ChunkDotProd implements Runnable{
        double[][] a;                       // Matrix 'A'
        double[][] b;                       // Matrix 'B'
        double[][] ab;                   // Result (product) matrix
        int idx;                              // 'From' matrix result index... 
        int length;                              // ...with this length (# of rows) 
        ChunkDotProd(double[][] a, double[][] b, double[][] ab, int idx, int length){
            this.a = a;
            this.b = b;
            this.ab = ab;
            this.idx = idx;
            this.length = length;
        }
        @Override
        public void run(){ 
            // Multiplying A submatrix by B
            double[][] chunkProduct = transpProduct(a, b); 
            // Building the final product matrix with each thread portion
            System.arraycopy(chunkProduct, 0, ab, idx, length);
        }
    }
    
    public static double[][] mtrxProduct(double[][] a, double[][] b){
        double[][] dp = new double[a.length][b[0].length];
        for(int i=0; i<a.length; i++)
            for(int j=0; j<b[0].length; j++)
                for(int k=0; k<a[0].length; k++)
                    dp[i][j] += a[i][k]*b[k][j];
        return dp;
    }
   
    public static double[][] transpProduct(double[][] a, double[][] bT){
        double[][] dp = new double[a.length][bT.length];
        for(int i=0; i<a.length; i++)
            for(int j=0; j<bT.length; j++)             
                for(int k=0; k<a[0].length; k++)
                    dp[i][j] += a[i][k]*bT[j][k];
        return dp;
    }
    
    private static double[][] mtrxTranspose(double[][] m){
        double[][] mT = new double[m[0].length][m.length];
        for(int i=0; i<m[0].length; i++)
            for(int j=0; j<m.length; j++)
                mT[i][j] = m[j][i]; 
        return mT;
    }
    
    public static void initMatr(){
        // A basic example to check results
        /*
            |1 2 3|                 |8 1 7 5|
            |3 2 1|   |0 1 2 1|     |4 3 9 7|
          a=|2 1 3| b=|1 0 1 2| a·b=|7 2 8 4|
            |1 3 2|   |2 0 1 0|     |7 1 7 7| 
            |0 1 1|                 |3 0 2 2| 
        */
        //a = new double[][]{{1, 2, 3}, {3, 2, 1}, {2, 1, 3}, {1, 3, 2}, {0, 1, 1}};
        //b = new double[][]{{0, 1, 2, 1}, {1, 0, 1, 2}, {2, 0, 1, 0}};  
        
        // The explanatory companion text basic example
        //a = new double[][]{{1, 0, 2}, {0, 1, 3}, {2, 1, 0}};
        //b = new double[][]{{2, 1}, {3, 0}, {1, 2}}; 
        
        // A more complex example to check times
        a = new double[1000][1500];  //[1000][1500];
        b = new double[1500][3000];  //[1500][3000];
        // Filling elements with random stuff
        for(int i=0; i<a.length; i++ )
            for(int j=0; j<a[0].length; j++)
                a[i][j] = Math.random();
        for(int i=0; i<b.length; i++ )
            for(int j=0; j<b[0].length; j++)
                b[i][j] = Math.random();   
    }
}
