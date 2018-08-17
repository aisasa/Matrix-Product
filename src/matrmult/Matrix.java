package matrmult;

import java.util.*;             // Arrays and ArrayList

public class Matrix {
    // Constants
    public static enum ElemsType{INT, LONG, FLOAT, DOUBLE};
    public static enum InitType{NO, ZEROS};
    // Variables
    private ArrayList matrix;
    private int nRows, nCols;
    private ElemsType elemsType;
    
    Matrix(int rows, int cols, ElemsType eType, InitType iType){
        this(rows, cols, eType);
        if(iType == InitType.ZEROS){
            if(elemsType == ElemsType.DOUBLE)
                for(int i=0; i<nRows; i++)
                    matrix.add(new double[nCols]);  
        }
        // TODO: rest of primitives
    }
    
    Matrix(int rows, int cols, ElemsType eType){
        matrix = new ArrayList<>(rows);
        nRows = rows;
        nCols = cols;
        elemsType = eType;
    }
    
    Matrix(Object m){
        if(m.getClass().getCanonicalName().equals("double[][]"))
            initMatrix((double[][])m);   
        // TODO: rest of primitives 
    }
    private void initMatrix(double[][] m){
        matrix = new ArrayList<>(m.length);
        nRows = m.length;
        nCols = m[0].length;
        elemsType = ElemsType.DOUBLE;
        for(int i=0; i<nRows; i++)
            matrix.add(m[i]);
    }
    
    public Matrix transpose(){
        Matrix t = new Matrix(nCols, nRows, elemsType);
        for(int i=0; i<nCols; i++)
            t.add(this.getColV(i));
        return t;
    }
    
    public void add(double[] v){
        matrix.add(v);
    }
    
    public void set(int row, double[] v){
        matrix.set(row, v);
    }
    
    public double getElem(int row, int col){
        return ((double[])matrix.get(row))[col];
    }
    
    public double[] getRow(int row){
        return (double[])matrix.get(row);
    }
    
    public double[] getColV(int col){
        double[] c = new double[nRows];
        for(int i=0; i<nRows; i++)
            c[i] = ((double[])matrix.get(i))[col];
        return c;
    }
    
    public ArrayList getMatrix(){
        return matrix;
    }
    
    public int getNRows(){
        return nRows;
    }
    
    public int getNCols(){
        return nCols;
    }
    
    public int getNElems(){
        return nRows * nCols;
    }
    
    public ElemsType getElemsType(){
        return elemsType;
    }
    
    public double[][] toArray(){
        /*double[][] m = new double[nRows][nCols];
        for(int i=0; i<nRows; i++)
            m[i] = (double[])matrix.get(i);*/
        return (double[][])matrix.toArray();
    }
    
    @Override
    public String toString(){
        return Arrays.deepToString(matrix.toArray());
    }
    
}
