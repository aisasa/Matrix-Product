package matrmult;

import java.util.*;

public class MatrMultLists {
    // Matrices
    public static ArrayList<double[]> a;        // Matrix A
    public static ArrayList<double[]> b;        // Matrix B
    public static ArrayList<double[]> ab;       // Product matrix A·B
    // System
    public static int nProc;                    // # of cores in system
    private static Thread[] threads;            // Array of threads
    
    public static void main(String[] args) throws InterruptedException{
        // Get number of processing cores in system
        nProc = Runtime.getRuntime().availableProcessors();
        // Declare threads
        threads = new Thread[nProc];
        // Initialize matrices
        initMtrs();
        // Go product
        long startT = System.currentTimeMillis();
        ab = threadedProduct(a, b); //mtrxTranspose(bT));
        System.out.println("Exec time: " + (System.currentTimeMillis() - startT)/1000. + " secs");
        // Show some checks
        //System.out.println("a·b excerpt= " + Arrays.toString(ab.get(0)));
        //System.out.println("a·b = " + Arrays.deepToString(ab.toArray()));
    }
    
    public static ArrayList<double[]> threadedProduct(ArrayList<double[]> a, ArrayList<double[]> b) throws InterruptedException{
        // Check correct dimensions
        if(a.get(0).length != b.size()) throw new RuntimeException("No matching dimensiones in matrices");
        // Compute transpose of bT
        ArrayList<double[]> bT = mtrxTranspose(b);      // 0.11 secs
        // Get number of rows in each chunk (piece of work) of matrix A
        int rowsInChunksAvrg = a.size()/nProc;
        int remain = a.size()%nProc;           
        // Product matrix initialization (to zeros)
        ArrayList<double[]> prodMtrx = new ArrayList<>(a.size());
        for(int i=0; i<a.size(); i++)
            prodMtrx.add(new double[b.get(0).length]);
        // Go threading
        int index = 0;                      // Indexing the A matrix chunks
        for(int i=0; i<nProc; i++){         // For each core or chunk:
            // Obtain number of rows in every chunk
            int rowsInChunk = rowsInChunksAvrg + (i < remain ? 1 : 0);
            // Define new matrix for every chunk and init with its matrix A portion
            ArrayList<double[]> aChunkAL = new ArrayList<>(rowsInChunk);
            for(int j=index; j<index+rowsInChunk; j++)
                aChunkAL.add(a.get(j)); 
            // Launch thread
            (threads[i] = new Thread(new ChunkProduct(aChunkAL, bT, prodMtrx, index, rowsInChunk))).start(); 
            // Update index
            index = index + rowsInChunk; 
        }
        for(int i=0; i<nProc; i++)          // For each thread...
            threads[i].join();              // ...wait until it ends (all finished then)
        return prodMtrx;
    }
    private static class ChunkProduct implements Runnable{
        ArrayList<double[]> aChunk;                       
        ArrayList<double[]> bT;                  
        ArrayList<double[]> prodM;          // Result (product) matrix
        int idx;                            // 'From' matrix result index... 
        int length;                         // ...with this length (# of rows) 
        ChunkProduct(ArrayList<double[]> aChunk, ArrayList<double[]> bT, ArrayList<double[]> prodM, int idx, int length){
            this.aChunk = aChunk;
            this.bT = bT;
            this.prodM = prodM;
            this.idx = idx;
            this.length = length;
        }
        @Override
        public void run(){ 
            // Product of chunk (portion of A) by transpose of matrix B
            ArrayList<double[]> chunkProduct = transposeProduct(aChunk, bT); 
            // Building the product matrix: each thread cooperate with its portion
            for(int i=0; i<chunkProduct.size(); i++)
                prodM.set(idx+i, chunkProduct.get(i));
        }
    }
    
    private static ArrayList<double[]> transposeProduct(ArrayList<double[]> a, ArrayList<double[]> bT){          
        /*ArrayList<double[]> dp = new ArrayList<>(a.size());
        for(int i=0; i<a.size(); i++)
            dp.add(new double[bT.size()]); 
        for(int i=0; i<a.size(); i++)
            for(int j=0; j<bT.size(); j++)
                for(int k=0; k<a.get(0).length; k++)
                    dp.get(i)[j] += a.get(i)[k] * bT.get(j)[k];*/
        
        ArrayList<double[]> dp = new ArrayList<>(a.size());
        for(int i=0; i<a.size(); i++){
            double[] array = new double[bT.size()];
            for(int j=0; j<bT.size(); j++){ //j++){
                array[j] = 0;
                for(int k=0; k<a.get(0).length; k++){
                    array[j] += a.get(i)[k] * bT.get(j)[k];
                }
            }
            dp.add(array);
        }
        return dp;
    }
    
    private static ArrayList<double[]> mtrxTranspose(ArrayList<double[]> m){
        //long startT = System.currentTimeMillis();
        int mNCols = m.get(0).length;
        ArrayList<double[]> mT = new ArrayList<>(mNCols); 
        for(int i=0; i<mNCols; i++)
            mT.add(new double[m.size()]);
        double[] arrayM;
        
        for(int i=0; i<m.size(); i++){
            arrayM = m.get(i);
            for(int j=0; j<mNCols; j++){
                mT.get(j)[i] = arrayM[j];
            }
        }
        //System.out.println("Transp time2: " + (System.currentTimeMillis() - startT)/1000. + " secs");
        
        /*long startT = System.currentTimeMillis();
        ArrayList<double[]> mT = new ArrayList<>(m.get(0).length); 
        //mT = new ArrayList<>(m.get(0).length);
        double[] array = new double[m.size()];
        for(int i=0; i<m.get(0).length; i++){
            for(int j=0; j<m.size(); j++){
                    array[j] = m.get(j)[i];
            }
            mT.add(Arrays.copyOf(array, array.length));
        }
        System.out.println("Transp time3: " + (System.currentTimeMillis() - startT)/1000. + " secs"); */
        return mT;
    }
    
    private static void initMtrs(){
        /*double[][] aMtrx = new double[][]{{1, 2, 3}, {3, 2, 1}, {2, 1, 3}, {1, 3, 2}};
        double[][] bMtrx = new double[][]{{0, 1, 2, 1}, {1, 0, 1, 2}, {2, 0, 1, 0}}; 
        int aRows = aMtrx.length;
        int bRows = bMtrx.length;*/
        
        int aRows = 1000;
        int aCols = 1500;
        int bRows = 1500;
        int bCols = 3000;
        double[][] aMtrx = new double[aRows][aCols];  //[1000][1500];
        double[][] bMtrx = new double[bRows][bCols];  //[1500][3000];
        // Filling elements with random stuff
        for(int i=0; i<aRows; i++ )
            for(int j=0; j<aCols; j++)
                aMtrx[i][j] = Math.random();
        for(int i=0; i<bRows; i++ )
            for(int j=0; j<bCols; j++)
                bMtrx[i][j] = Math.random();
        
        a = new ArrayList<>(aMtrx.length);  
        b = new ArrayList<>(bMtrx.length); 
        for(int i=0; i<aRows; i++)
            a.add(aMtrx[i]);
        for(int i=0; i<bRows; i++)
            b.add(bMtrx[i]);
    }
}
