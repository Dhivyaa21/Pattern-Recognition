import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.LUDecomposition;
import org.apache.commons.math3.linear.RealMatrix;

import java.util.Arrays;

/**
 * Created by joao on 2014-09-11.
 */
public class MyMatrix {

    double[][] matrix;
    private String name;
    private boolean printInt = true;

    public MyMatrix(int numRows, int numCols, double value, String name) {
        matrix = new double[numRows][numCols];

        for (int row = 0; row < numRows; row++) {
            for (int col = 0; col < numCols; col++) {
                matrix[row][col] = value;
            }
        }
        this.name = name;
    }

    public MyMatrix(int numRows, int numCols, byte[] values, String name) {

        matrix = new double[numRows][numCols];

        int i = 0;
        for (int row = 0; row < numRows; row++) {
            for (int col = 0; col < numCols; col++) {
                matrix[row][col] = values[i++] & 0xFF;
            }
        }
        this.name = name;
    }

    public MyMatrix(MyMatrix matrix) {
        this.matrix = matrix.subMatrix(matrix.numRows(), matrix.numCols(), 0, 0).getMatrix();
        this.name = matrix.name;
        this.printInt = matrix.printInt;
    }

    public MyMatrix(double[][] matrix, String name) {
        this.matrix = matrix;
        this.name = name;

    }

    public MyMatrix(double[][] matrix) {
        this.matrix = matrix;

    }

    public String getName() {
        return name;
    }

    @Override
    public MyMatrix clone() {
        return new MyMatrix(this);
    }

    public int numRows() {
        return matrix.length;
    }

    public int numCols() {
        return matrix[0].length;
    }

    public static MyMatrix product(MyMatrix aMatrix, MyMatrix bMatrix) {

        double[][] a = aMatrix.getMatrix();
        double[][] b = bMatrix.getMatrix();
        double[][] c = new double[aMatrix.numRows()][aMatrix.numCols()];

        for (int a_row = 0; a_row < aMatrix.numRows(); a_row++) {
            for (int a_col = 0; a_col < aMatrix.numCols(); a_col++) {
                c[a_row][a_col] = a[a_row][a_col] * b[a_row][a_col];
            }
        }

        return new MyMatrix(c);
    }

    public void makeBorder(int size, double coefficient) {

        double[][] tmp = new double[numRows() + 2 * size][numCols() + 2 * size];

        //  fill top border
        for (int j = 0; j < numCols(); j++) {
            tmp[0][j + 1] = coefficient * matrix[0][j];
        }

        //  copy matrix to tmp and fill corresponding left and right edges
        for (int i = 0; i < numRows(); i++) {
            System.arraycopy(matrix[i], 0, tmp[i + 1], 1, numCols());
            tmp[i + 1][0] = coefficient * matrix[i][0];
            tmp[i + 1][numCols() + 1] = coefficient * matrix[i][numCols() - 1];
        }

        //  fill bottom border
        for (int j = 0; j < numCols(); j++) {
            tmp[numRows() + 1][j + 1] = coefficient * matrix[numRows() - 1][j];
        }

        //  fill corners
        tmp[0][0] = coefficient * tmp[0][1];
        tmp[numRows() + 1][0] = coefficient * tmp[numRows() + 1][1];
        tmp[0][numCols() + 1] = coefficient * tmp[0][numCols()];
        tmp[numRows() + 1][numCols() + 1] = coefficient * tmp[numRows() + 1][numCols()];

        matrix = tmp;
    }

    public void removeBorder(int size) {

        double[][] tmp = new double[numRows() - 2 * size][numCols() - 2 * size];

        for (int j = size; j < numRows() - size; j++) {
            tmp[j - size] = Arrays.copyOfRange(matrix[j], size, numCols() - size);
        }

        matrix = tmp;
    }

    public void binarize() {
        binarize(1);
    }

    public MyMatrix binarize(double factor) {

        for (int j = 0; j < numRows(); j++) {
            for (int i = 0; i < numCols(); i++) {
                if (matrix[j][i] / factor < 0.5) {
                    matrix[j][i] = 0;
                } else {
                    matrix[j][i] = 1;
                }
            }
        }

        return this;
    }

    public MyMatrix subMatrix(int numRows, int numCols, int x, int y) {
        double[][] tmp = new double[numRows][numCols];

        for (int i = 0; i < numRows; i++) {
            for (int j = 0; j < numCols; j++) {
                tmp[i][j] = matrix[y + i][x + j];
            }
        }

        return new MyMatrix(tmp);
    }

    public double[][] getMatrix() {
        return matrix;
    }

    public double sum() {
        double total = 0;
        for (double[] row : matrix) {
            for (double col : row) {
                total += col;
            }
        }
        return total;
    }

    public void set(int i, int j, double value) {
        matrix[i][j] = value;
    }

    public MyMatrix product(double v) {
        for (int i = 0; i < numRows(); i++) {
            for (int j = 0; j < numCols(); j++) {
                matrix[i][j] *= v;
            }
        }

        return this;
    }

    public void normalize() {
        product(1d / sum());
        printInt = false;
    }

    public MyMatrix print() {
        print(name);
        return this;
    }

    public void print(String label) {
        System.out.println(label + "\n" + this);
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();

        for (int j = 0; j < numRows(); j++) {
            for (int i = 0; i < numCols(); i++) {
                double cell = matrix[j][i];

                if (printInt) {
                    sb.append((int) cell).append(' ');
                } else {
                    if (cell - (int) cell < 0.00001) {
                        sb.append((int) cell).append(' ');
                    } else {
                        sb.append(cell).append(' ');
                    }
                }
            }
            sb.setLength(sb.length() - 1);
            sb.append("\n");
        }
        return sb.toString();
    }

    public MyMatrix add(double value) {

        for (int i = 0; i < numRows(); i++) {
            for (int j = 0; j < numCols(); j++) {
                matrix[i][j] += value;
            }
        }

        return this;
    }

    public MyMatrix add(MyMatrix mat) {

        double[][] m = mat.getMatrix();

        if (mat.numCols() == numCols() && mat.numRows() == numRows()) {
            for (int j = 0; j < numRows(); j++) {
                for (int i = 0; i < numCols(); i++) {
                    matrix[j][i] += m[j][i];
                }
            }
        }

        return this;
    }

    public MyMatrix fill(int value) {

        for (int j = 0; j < numRows(); j++) {
            for (int i = 0; i < numCols(); i++) {
                matrix[j][i] = value;
            }
        }

        return this;
    }

    public double moment(int p, int q) {
        return moment(p, q, 0, 0);
    }

    public double momentCentral(int p, int q) {
        double m_00 = moment(0, 0);
        double m_10 = moment(1, 0);
        double m_01 = moment(0, 1);

        final int xc = (int) Math.round(m_10 / m_00);
        final int yc = (int) Math.round(m_01 / m_00);

        return moment(p, q, xc, yc);
    }

    public double moment(int p, int q, double xc, double yc) {

        double total = 0;
        for (int y = 0; y < numRows(); y++) {
            for (int x = 0; x < numCols(); x++) {
                total += Math.pow(x - xc, p) * Math.pow(y - yc, q) * matrix[y][x];
            }
        }

        return total;
    }

    public MyMatrix apply(MatrixOperation o) {

        double[][] m = o.matrix.getMatrix();

        for (int y = 0; y < o.matrix.numRows(); y++) {
            for (int x = 0; x < o.matrix.numCols(); x++) {
                int yt = o.toY(x, y);
                int xt = o.toX(x, y);

                int yf = o.fromY(x, y);
                int xf = o.fromX(x, y);

                if (0 <= xt && xt < numCols() && 0 <= yt && yt < numRows() &&
                        0 <= xf && xf < o.matrix.numCols() && 0 <= yf && yf < o.matrix.numRows()) {
                    matrix[yt][xt] = m[yf][xf];
                }
            }
        }
        return this;
    }

    public MyMatrix applyBackwards(MatrixOperation o) {

        double[][] m = o.matrix.getMatrix();

        for (int y = 0; y < numRows(); y++) {
            for (int x = 0; x < numCols(); x++) {
                int yt = o.toY(x, y);
                int xt = o.toX(x, y);

                int yf = o.fromY(x, y);
                int xf = o.fromX(x, y);

                if (0 <= xt && xt < numCols() && 0 <= yt && yt < numRows() &&
                        0 <= xf && xf < o.matrix.numCols() && 0 <= yf && yf < o.matrix.numRows()) {
                    matrix[yt][xt] = m[yf][xf];
                }
            }
        }
        return this;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double determinant() {

        Array2DRowRealMatrix m = new Array2DRowRealMatrix(getMatrix());
        RealMatrix transpose = m.transpose();
        RealMatrix square = m.preMultiply(transpose);
        return new LUDecomposition(square).getDeterminant();

    }
}
