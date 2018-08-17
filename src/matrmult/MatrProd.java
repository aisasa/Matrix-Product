package matrmult;

import java.util.*;

public class MatrProd {
    public static Matrix a;
    public static Matrix b;
    public static Matrix ab;
    public static Matrix.ElemsType elemsType; 
    public static int nProc;
    
    public static void main(String[] args) throws InterruptedException{
        // Init matrix examples and number of cores in system
        initMtrs();
        nProc = Runtime.getRuntime().availableProcessors();
        // Go product
        long startT = System.currentTimeMillis();
        ab = threadedDotProduct(a, b); 
        System.out.println("Product exec time: " + (System.currentTimeMillis() - startT)/1000. + " secs");
        // Show some testings
        //System.out.println("a·b excerpt = " + Arrays.toString(ab.getRow(0)));
        //System.out.println("a·b = " + ab.toString());
        System.out.println("Last elem = " + ab.getElem(ab.getNRows()-1, ab.getNCols()-1));
    }
    
    public static Matrix threadedDotProduct(Matrix a, Matrix b) throws InterruptedException{
        Matrix bT = b.transpose();
        // Number of rows in each chunk (piece of work) of matrix A
        int rowsInChunksAvrg = a.getNRows()/nProc;
        int remain = a.getNRows()%nProc;           
        // Product matrix definition and initialization to zeros
        Matrix prod = new Matrix(a.getNRows(), bT.getNRows(), elemsType, Matrix.InitType.ZEROS);    
        // Go threading
        Thread[] threads = new Thread[nProc];
        int index = 0;                      // Indexing the A matrix chunks
        for(int i=0; i<nProc; i++){         // For each core
            int rowsInChunk = rowsInChunksAvrg + (i < remain ? 1 : 0); 
            Matrix aChunk = new Matrix(rowsInChunk, a.getNCols(), elemsType);   // Each piece of 'A' matrix in which it's divided
            for(int j=0; j<rowsInChunk; j++){
                aChunk.add(a.getRow(index + j)); 
            }
            (threads[i] = new Thread(new ChunkDotProd(aChunk, bT, prod, index, rowsInChunk))).start(); 
            index = index + rowsInChunk; 
        }
        for(int i=0; i<nProc; i++)          // For each thread...
            threads[i].join();              // ...wait until it ends (all then)
        return prod; 
    }
    private static class ChunkDotProd implements Runnable{
        Matrix a;                       
        Matrix bT;                  
        Matrix prod;                   // Result (product) matrix
        int idx;                              // 'From' matrix result index... 
        int length;                              // ...with this length (# of rows) 
        ChunkDotProd(Matrix a, Matrix b, Matrix prod, int idx, int length){
            this.a = a;
            this.bT = b;
            this.prod = prod;
            this.idx = idx;
            this.length = length;
        }
        @Override
        public void run(){ 
            Matrix chunkProduct = transpDotProduct(a, bT); //transpDotProduct(a, b); //noThrdDotProduct(a, b);
            for(int j=0; j<length; j++)
                prod.set(idx+j, chunkProduct.getRow(j));
        }
    }
   
    public static Matrix transpDotProduct(Matrix a, Matrix bT){
        int aRows = a.getNRows();
        int bTRows = bT.getNRows();
        int aCols = a.getNCols();
        double[] vProd, vA, vBT;
        Matrix dp = new Matrix(aRows, bTRows, elemsType);
        for(int i=0; i<aRows; i++){
            vProd = new double[bTRows];
            vA = a.getRow(i);
            for(int j=0; j<bTRows; j++){
                vBT = bT.getRow(j);
                for(int k=0; k<aCols; k++){
                    vProd[j] += vA[k] * vBT[k]; 
                }
            }
            dp.add(vProd); 
        }
        /*for(int i=0; i<a.getNRows(); i++){
            double[] v = new double[bT.getNRows()];
            for(int j=0; j<bT.getNRows(); j++) //get(0).length; j++)             
                for(int k=0; k<a.getNCols(); k++)
                    v[j] += a.getElem(i, k) * bT.getElem(j, k);  //a[i][k]*bT[j][k];
            dp.add(v); 
        }*/
        return dp;
    }
    
    public static Matrix dotProduct(Matrix a, Matrix b){   
        Matrix dp = new Matrix(a.getNRows(), b.getNCols(), elemsType);
        for(int i=0; i<a.getNRows(); i++){
            double[] vP = new double[b.getNCols()];
            double[] vA = a.getRow(i);
            for(int j=0; j<b.getNCols(); j++){
                double[] vB = b.getColV(j);
                for(int k=0; k<b.getNRows(); k++){
                    vP[j] += vA[k] * vB[k]; 
                }
            }
            dp.add(vP); //set(i, vP); //dp.add(vP);
        }
        return dp;
    }
    
    private static void initMtrs(){
        //double[][] aMtrx = new double[][]{{1, 2, 3}, {3, 2, 1}, {2, 1, 3}, {1, 3, 2}};
        //double[][] bMtrx = new double[][]{{0, 1, 2, 1}, {1, 0, 1, 2}, {2, 0, 1, 0}}; 
        
        int aRows = 1000;
        int aCols = 1500;
        int bRows = 1500;
        int bCols = 3000;
        double[][] aMtrx = new double[aRows][aCols];  //[1000][1500];
        double[][] bMtrx = new double[bRows][bCols];  //[1500][3000];
        // Filling elements with random stuff and compute transposed
        for(int i=0; i<aRows; i++ )
            for(int j=0; j<aCols; j++)
                aMtrx[i][j] = Math.random();
        for(int i=0; i<bRows; i++ )
            for(int j=0; j<bCols; j++)
                bMtrx[i][j] = Math.random();
        // New matrix objects
        a = new Matrix(aMtrx);  
        b = new Matrix(bMtrx); 
        elemsType = Matrix.ElemsType.DOUBLE;
    }
}