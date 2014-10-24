/**
 * Created by joao on 14.10.04.
 */
public class MatrixOperation {

    final MyMatrix matrix;

    protected MatrixOperation(MyMatrix matrix) {
        this.matrix = matrix;
    }

    public int fromX(int x, int y) { return x;}
    public int fromY(int x, int y) { return y;}

    public int toX(int x, int y) { return x;}
    public int toY(int x, int y) { return y;}
}
