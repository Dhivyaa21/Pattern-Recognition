/**
 * Created by joao on 2014-10-24.
 */
public class Profiles {

	Histogram upper = null;
	Histogram lower = null;
	Histogram left = null;
	Histogram right = null;
    private String title;

    public Profiles(MyMatrix matrix, String title) {
        this.title = title;

		upper = new Histogram(matrix.numCols(),"Upper");
		lower = new Histogram(matrix.numCols(),"Lower");
		left = new Histogram(matrix.numRows(),"Left");
		right = new Histogram(matrix.numRows(),"Right");

		double[][] m = matrix.getMatrix();

		//	upper
		for (int x = 0; x < matrix.numCols(); x++) {
			for (int y = 0; y < matrix.numRows(); y++) {
				if (m[y][x] == 1) {
					upper.set(x, y);
					break;
				}
			}
		}

		//	lower
		for (int x = 0; x < matrix.numCols(); x++) {
			for (int y = matrix.numRows() - 1; y >= 0; y--) {
				if (m[y][x] == 1) {
					lower.set(x, matrix.numRows() - 1 - y);
					break;
				}
			}
		}

		//	left
		for (int y = 0; y < matrix.numRows(); y++) {
			for (int x = 0; x < matrix.numCols(); x++) {
				if (m[y][x] == 1) {
					left.set(y, x);
					break;
				}
			}
		}

		//	right
		for (int y = 0; y < matrix.numRows(); y++) {
			for (int x = matrix.numCols() - 1; x >= 0; x--) {
				if (m[y][x] == 1) {
					right.set(y, matrix.numCols() - 1 - x);
				}
			}
		}
	}

	public Histogram getUpper() {
		return upper;
	}

	public Histogram getLower() {
		return lower;
	}

	public Histogram getLeft() {
		return left;
	}

	public Histogram getRight() {
		return right;
	}

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }


    public void print() {
        System.out.println(title);
        upper.print();
        lower.print();
        left.print();
        right.print();
    }
}
