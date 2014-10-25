/**
 * Created by joao on 2014-10-24.
 */
public class Crossings {

    Histogram horizontal = null;
    Histogram vertical = null;
    private String title;

    public Crossings(MyMatrix matrix, String title) {
        this.title = title;
        horizontal = new Histogram(matrix.numCols(), "Horizontal");
        vertical = new Histogram(matrix.numRows(), "Vertical");

        double[][] m = matrix.getMatrix();

        //	horizontal crossings
        for (int y = 0; y < matrix.numRows(); y++) {
            for (int x = 1; x < matrix.numCols(); x++) {
                if (m[y][x - 1] == 0 && m[y][x] == 1) {
                    horizontal.inc(y);
                }
            }
        }

        //	vertical crossings
        for (int x = 0; x < matrix.numCols(); x++) {
            for (int y = 1; y < matrix.numRows(); y++) {
                if (m[y - 1][x] == 0 && m[y][x] == 1) {
                    vertical.inc(x);
                }
            }
        }
    }

    public Histogram getHorizontal() {
        return horizontal;
    }

    public Histogram getVertical() {
        return vertical;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void print() {
        System.out.println(title);
        horizontal.print();
        horizontal.print();
    }
}
