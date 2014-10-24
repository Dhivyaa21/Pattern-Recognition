/**
 * Created by joao on 14.10.10.
 */

public class BinarySmooth {

    public static MyMatrix binarySmooth(MyMatrix matrix) {

        MyMatrix smoothed = matrix.clone();
        smoothed.makeBorder(1, 0);

        double[][] m = smoothed.getMatrix();
        int frame = 0;
        boolean modified;

        int pass = 0;
        do {
            modified = false;
            for (int y = matrix.numRows(); y > 0; y--) {
                for (int x = matrix.numCols(); x > 0; x--) {

                    if (smoothers[frame].smooth(m, x, y)) {
                        pass++;
                        modified = true;
                        break;
                    }
                }
                if (modified) {
                    modified = true;
                    break;
                }
            }
            smoothed.print(smoothed.getName() + ", binary "+smoothers[frame].getName()+" pass: " + pass);
            frame = (frame + 1) % 4;
        } while (modified);

        smoothed.removeBorder(1);

        System.out.println("total passes: " + pass);
        smoothed.setName("binarySmooth." + matrix.getName());

        return smoothed;
    }

    public static final Smoother smoothByFrameBottom = new Smoother("bottom") {
        @Override
        public boolean smooth(double[][] m, int x, int y) {

            /*
                |p9|p2|p3|  |=|=|=|
                |p8|p1|p4|  |=|t|=|
                |p7|p6|p5|  |x|x|x|
             */

            //	for easier reference
            double p1 = m[y][x];
            double p2 = m[y - 1][x];
            double p3 = m[y - 1][x + 1];
            double p4 = m[y][x + 1];
            double p5 = m[y + 1][x + 1];
            double p6 = m[y + 1][x];
            double p7 = m[y + 1][x - 1];
            double p8 = m[y][x - 1];
            double p9 = m[y - 1][x - 1];

            boolean modified = false;

            if (p8 == p9 && p9 == p2 && p2 == p3 && p3 == p4 && p4 != p1) {
                m[y][x] = p4;
                modified = true;
            }

            return modified;
        }
    };

    public static final Smoother smoothByFrameLeft = new Smoother("left") {
        @Override
        public boolean smooth(double[][] m, int x, int y) {

              /*
                |p9|p2|p3|  |x|=|=|
                |p8|p1|p4|  |x|t|=|
                |p7|p6|p5|  |x|=|=|
             */

            //	for easier reference
            double p1 = m[y][x];
            double p2 = m[y - 1][x];
            double p3 = m[y - 1][x + 1];
            double p4 = m[y][x + 1];
            double p5 = m[y + 1][x + 1];
            double p6 = m[y + 1][x];
            double p7 = m[y + 1][x - 1];
            double p8 = m[y][x - 1];
            double p9 = m[y - 1][x - 1];

            boolean modified = false;

            if (p2 == p3 && p3 == p4 && p4 == p5 && p5 == p6 && p6 != p1) {
                m[y][x] = p6;
                modified = true;
            }

            return modified;
        }
    };

    public static final Smoother smoothByFrameTop = new Smoother("top") {
        @Override
        public boolean smooth(double[][] m, int x, int y) {

              /*
                |p9|p2|p3|  |x|x|x|
                |p8|p1|p4|  |=|t|=|
                |p7|p6|p5|  |=|=|=|
             */

            //	for easier reference
            double p1 = m[y][x];
            double p2 = m[y - 1][x];
            double p3 = m[y - 1][x + 1];
            double p4 = m[y][x + 1];
            double p5 = m[y + 1][x + 1];
            double p6 = m[y + 1][x];
            double p7 = m[y + 1][x - 1];
            double p8 = m[y][x - 1];
            double p9 = m[y - 1][x - 1];

            boolean modified = false;

            if (p4 == p5 && p5 == p6 && p6 == p7 && p7 == p8 && p8 != p1) {
                m[y][x] = p8;
                modified = true;
            }

            return modified;
        }
    };

    public static final Smoother smoothByFrameRight = new Smoother("right") {
        @Override
        public boolean smooth(double[][] m, int x, int y) {

              /*
                |p9|p2|p3|  |=|=|x|
                |p8|p1|p4|  |=|t|x|
                |p7|p6|p5|  |=|=|x|
             */

            //	for easier reference
            double p1 = m[y][x];
            double p2 = m[y - 1][x];
            double p3 = m[y - 1][x + 1];
            double p4 = m[y][x + 1];
            double p5 = m[y + 1][x + 1];
            double p6 = m[y + 1][x];
            double p7 = m[y + 1][x - 1];
            double p8 = m[y][x - 1];
            double p9 = m[y - 1][x - 1];

            boolean modified = false;

            if (p6 == p7 && p7 == p8 && p8 == p9 && p9 == p2 && p2 != p1) {
                m[y][x] = p2;
                modified = true;
            }

            return modified;
        }
    };

    static final Smoother[] smoothers = {smoothByFrameBottom, smoothByFrameLeft, smoothByFrameTop, smoothByFrameRight};
}

abstract class Smoother {

    private String name;

    Smoother(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    abstract public boolean smooth(double[][] m, int x, int y);
}
