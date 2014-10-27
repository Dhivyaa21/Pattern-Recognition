import MNIST.MNISTReader;
import org.apache.commons.math3.stat.descriptive.moment.Variance;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.statistics.SimpleHistogramBin;
import org.jfree.data.statistics.SimpleHistogramDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import java.awt.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by joao on 2014-09-11.
 */
public class ImageUtils {
    static final public int PLANE_8 = 8;
    static final public int PLANE_16 = 16;
    static final public int PLANE_32 = 32;
    static final public int STANDARD_PLANE = PLANE_32;
    static final public int PLANE_64 = 64;

    static final public int FRAME_COEFFICIENT_0 = 0;
    static final public int FRAME_COEFFICIENT_1 = 1;

    private static boolean printAfterOperation;
    private static JFrame crossingsPane = null;
    private static JFrame projPane;
    private static JFrame profilePane;
    private static ChartPanel crossingsPanel;
    private static ChartPanel projPanel;
    private static ChartPanel profilePanel;

    public static void smooth(MyMatrix matrix) {
        MyMatrix mask = new MyMatrix(3, 3, 1, "mask");

        mask.normalize();

        smooth(matrix, mask, 1);
    }

    public static MyMatrix smooth(MyMatrix matrix, MyMatrix mask, double frameCoefficient, int times) {

        for (int i = 0; i < times; i++) {
            smooth(matrix, mask, frameCoefficient);
        }

        return matrix;
    }

    public static void smooth(MyMatrix matrix, MyMatrix mask, double frameCoefficient) {

        MyMatrix baseMatrix = matrix.clone();

        int radius = mask.numCols();
//            baseMatrix.makeBorder(radius / 2,1);  //  borders = adjacent pixel
        baseMatrix.makeBorder(radius / 2, frameCoefficient);  //  borders = 0

//            baseMatrix.print("baseMatrix");

        for (int y = 0; y < matrix.numRows(); y++) {
            for (int x = 0; x < matrix.numCols(); x++) {

                MyMatrix subMatrix = baseMatrix.subMatrix(radius, radius, x, y);
//                    System.out.println("subMatrix("+y+","+x+"):\n"+subMatrix);

                matrix.set(y, x, MyMatrix.product(subMatrix, mask).sum());
//                    System.out.println("matrix("+y+","+x+"):\n"+matrix);
            }
        }

        matrix.setName("smooth." + matrix.getName());

        if (printAfterOperation) {
            matrix.print();
        }
    }

    public static Point rotate(Point p, double degrees) {

        double radians = degrees / 180 * Math.PI;

        return new Point(
                (int) (Math.cos(radians) * p.x + Math.sin(radians) * p.y),
                (int) (-Math.sin(radians) * p.x + Math.cos(radians) * p.y));
    }

    public static double detectSkew(MyMatrix matrix) {
        double angle = 0;

        Point init = new Point(matrix.numCols() / 2, matrix.numRows() / 2);
        double[][] m = matrix.getMatrix();

        final int SAMPLE_SIZE = 256;

        Variance variance = new Variance(false);
        double maxVariance = 0;
        for (double degrees = -90; degrees < 90; degrees += 5) {

            double[] skewVector = new double[128 / 16];
            int i = 0;

            for (int y = 0; y < 128; y += 16) {

                int total = 0;
                for (int x = 0; x < SAMPLE_SIZE; x++) {
                    Point pt = rotate(new Point(x, y), degrees);
                    pt.x += init.x;
                    pt.y += init.y;
                    total += (int) m[pt.x][pt.y];
                }

                skewVector[i++] = total;
            }

            double v = variance.evaluate(skewVector);

            if (v > maxVariance) {
                maxVariance = v;
                angle = degrees;
            }

            if (printAfterOperation) {
                System.out.format("%d degrees: %.1f%n radians", (int) degrees, v);
            }
        }

        return angle;
    }

    public static MyMatrix correctSkew(MyMatrix skew, double degrees) {

        int length = Math.max(skew.numRows(), skew.numRows());

        MyMatrix corrected = new MyMatrix(length, length, 255, skew.getName());
        double[][] correctedMatrix = corrected.getMatrix();
        double[][] skewMatrix = skew.getMatrix();

        int midX = skew.numCols() / 2;
        int midY = skew.numRows() / 2;

        for (int y = 0; y < skew.numRows(); y++) {

            for (int x = 0; x < skew.numCols(); x++) {

                Point pt = rotate(new Point(x - midX, midY - y), -degrees);
                pt.x += midX;
                pt.y += midY;
                if (0 <= pt.x && pt.x < corrected.numCols() &&
                        0 <= pt.y && pt.y < corrected.numRows()) {
                    correctedMatrix[pt.x][pt.y] = skewMatrix[y][x];
                }
            }
        }
        corrected.setName("correctSkew." + skew.getName());

        if (printAfterOperation) {
            corrected.print();
        }

        return corrected;
    }

    public static MyMatrix correctSlant(MyMatrix slanted) {
        return correctSlant(slanted, 0);
    }

    public static MyMatrix correctSlant(MyMatrix slanted, double angle) {

        MyMatrix corrected = null;
        corrected = slanted.clone();
        corrected.product(0);

        double m_00 = slanted.moment(0, 0);
        double m_01 = slanted.moment(0, 1);
        double m_10 = slanted.moment(1, 0);

        final int xc = (int) Math.round(m_10 / m_00);
        final int yc = (int) Math.round(m_01 / m_00);

        double µ11 = slanted.moment(1, 1, xc, yc);
        double µ02 = slanted.moment(0, 2, xc, yc);

        final double tan_ø = angle == 0 ? µ11 / µ02 : Math.tan(angle);
        final double ø = Math.atan(tan_ø);

        if (printAfterOperation) {
            System.out.println("Slant correction:");
            DecimalFormat df = new DecimalFormat("#,###.####");
            System.out.println(" m_00: " + df.format(m_00));
            System.out.println(" m_01: " + df.format(m_01));
            System.out.println(" m_10: " + df.format(m_10));
            System.out.println("   xc: " + df.format(xc));
            System.out.println("   yc: " + df.format(yc));
            System.out.println("  µ11: " + df.format(µ11));
            System.out.println("  µ02: " + df.format(µ02));
            System.out.println("tan ø: " + df.format(tan_ø));
            System.out.println("    ø: " + df.format(ø) + " radians");
            System.out.println();
        }
        corrected.apply(new MatrixOperation(slanted) {
            @Override
            public int toX(int x, int y) {
                return (int) (x - (y - yc) * tan_ø);
            }

            @Override
            public int toY(int x, int y) {
                return y;
            }
        });
        corrected.setName("correctSlant." + slanted.getName());
        if (printAfterOperation) {
            corrected.print();
        }

        return corrected;
    }

    public static MyMatrix invert(MyMatrix matrix) {

        matrix.product(-1).add(1);
        if (printAfterOperation) {
            matrix.print();
        }

        return matrix;
    }

    public static MyMatrix normalize(MyMatrix matrix) {

        return normalize(matrix, STANDARD_PLANE);
    }

    public static MyMatrix normalize(MyMatrix matrix, int plane) {

        final double a = plane / (double) matrix.numCols();
        final double b = plane / (double) matrix.numRows();

        MyMatrix normalized = new MyMatrix(plane, plane, 0, "normalize." + matrix.getName());

        normalized.apply(new MatrixOperation(matrix) {
            @Override
            public int toX(int xp, int yp) {
                return (int) (a * xp);
            }

            @Override
            public int toY(int xp, int yp) {
                return (int) (b * yp);
            }
        });
        if (printAfterOperation) {
            normalized.print();
        }

        return normalized;
    }

    public static MyMatrix normalizeByMoment(MyMatrix matrix, boolean forward, int plane) {

        MyMatrix normalized = new MyMatrix(plane, plane, 0, "normalizedByMoment." + matrix.getName());

        int w1 = matrix.numCols();
        int h1 = matrix.numRows();
        int w2 = plane;
        int h2 = plane;

        final double a = w2 / (double) w1;
        final double b = h2 / (double) h1;

        double m_00 = matrix.moment(0, 0);
        double m_10 = matrix.moment(1, 0);
        double m_01 = matrix.moment(0, 1);

        final int xc = (int) Math.round(m_10 / m_00);
        final int yc = (int) Math.round(m_01 / m_00);
        final int xcp = Math.round(w2 / 2f);
        final int ycp = Math.round(h2 / 2f);

        if (forward) {
            normalized.apply(new MatrixOperation(matrix) {
                @Override
                public int toX(int x, int y) {
                    return (int) (a * (x - xc) + xcp);
                }

                @Override
                public int toY(int x, int y) {
                    return (int) (b * (y - yc) + ycp);
                }
            });
        } else {
            normalized.applyBackwards(new MatrixOperation(matrix) {
                @Override
                public int fromX(int x, int y) {
                    return (int) ((x - xcp) / a + xc);
                }

                @Override
                public int fromY(int x, int y) {
                    return (int) (b * (y - ycp) / b + yc);
                }
            });
        }

        if (printAfterOperation) {
            normalized.print();
        }

        return normalized;
    }

    public static MyMatrix thinning(MyMatrix matrix) {

        MyMatrix thinned = matrix.clone();
        double[][] to = thinned.getMatrix();
        double[][] from = copyOf(to);
        boolean modified;

        do {
            modified = false;
            //  step 1
            for (int y = 1; y < matrix.numRows() - 1; y++) {
                for (int x = 1; x < matrix.numCols() - 1; x++) {

                    if (from[y][x] == 1 && satisfiesStep1Conditions(from, x, y)) {
                        to[y][x] = 0;    //  delete pixel
                        modified = true;
                    }
                }
            }

            from = copyOf(to);

//            new MyMatrix(from).print();
            //  step 2
            for (int y = 1; y < matrix.numRows() - 1; y++) {
                for (int x = 1; x < matrix.numCols() - 1; x++) {

                    if (from[y][x] == 1 && satisfiesStep2Conditions(from, x, y)) {
                        to[y][x] = 0;    //  delete pixel
                        modified = true;
                    }
                }
            }

            from = copyOf(to);
//            new MyMatrix(from).print();

        } while (modified);

        thinned.matrix = to;

        thinned.setName("thinning." + matrix.getName());

        if (printAfterOperation) {
            thinned.print();
        }

        return thinned;
    }

    private static double[][] copyOf(double[][] to) {
        double[][] m = new double[to.length][to[0].length];

        for (int y = 0; y < to.length; y++) {
            System.arraycopy(to[y], 0, m[y], 0, to[y].length);
        }

        return m;
    }

    private static boolean satisfiesStep1Conditions(double[][] matrix, int x, int y) {
        int[] p = calculatePs(matrix, x, y);
        int a = calculateA(p);
        int b = calculateB(p);
//        System.out.println("(" + x + "," + y + ")s1:a,b=" + a + "," + b);
        return 2 <= b && b <= 6 &&
                a == 1 &&
                (p[2] == 0 || p[4] == 0 || p[6] == 0) &&
                (p[4] == 0 || p[6] == 0 || p[8] == 0);
    }

    private static boolean satisfiesStep2Conditions(double[][] matrix, int x, int y) {
        int[] p = calculatePs(matrix, x, y);
        int a = calculateA(p);
        int b = calculateB(p);
//        System.out.println("(" + x + "," + y + ")s2:a,b=" + a + "," + b);

        return 2 <= b && b <= 6 &&
                a == 1 &&
                (p[2] == 0 || p[4] == 0 || p[8] == 0) &&
                (p[2] == 0 || p[6] == 0 || p[8] == 0);
//                (p[2] * p[4] * p[8] == 0) && (p[2] * p[6] * p[8] == 0);
    }

    private static int calculateA(int[] p) {

        int a = 0;
        for (int i = 3; i < p.length; i++) {
            if (p[i - 1] == 0 && p[i] == 1) {
                a++;
            }
        }
        if (p[9] == 0 && p[2] == 1) {
            a++;
        }

        return a;
    }

    private static int calculateB(int[] p) {

        int total = 0;

        for (int i = 2; i < p.length; i++) {
            total += p[i];
        }

        return total;
    }

    private static int[] calculatePs(double[][] matrix, int x, int y) {
        int[] p = new int[9 + 1];

        p[1] = (int) matrix[y][x];
        p[2] = (int) matrix[y - 1][x];
        p[3] = (int) matrix[y - 1][x + 1];
        p[4] = (int) matrix[y][x + 1];
        p[5] = (int) matrix[y + 1][x + 1];
        p[6] = (int) matrix[y + 1][x];
        p[7] = (int) matrix[y + 1][x - 1];
        p[8] = (int) matrix[y][x - 1];
        p[9] = (int) matrix[y - 1][x - 1];

        return p;
    }

    public static MyMatrix binarySmooth(MyMatrix matrix) {

        MyMatrix smoothed = Denoise.binarySmooth(matrix);

        if (printAfterOperation) {
            smoothed.print();
        }

        return smoothed;
    }

    public static boolean isPrintAfterOperation() {
        return printAfterOperation;
    }

    public static void setPrintAfterOperation(boolean printAfterOperation) {
        ImageUtils.printAfterOperation = printAfterOperation;
    }

    public static MyMatrix gradient(MyMatrix matrix) {

        MyMatrix gradientMaxtrix = new MyMatrix(matrix.numRows(), matrix.numCols(), 0, matrix.getName());
        MyMatrix base = matrix.clone();
        base.makeBorder(1, 1);

        double[][] g = gradientMaxtrix.getMatrix();
        double[][] s = base.getMatrix();
        for (int y = 0; y < matrix.numRows(); y++) {
            for (int x = 0; x < matrix.numCols(); x++) {

                if (s[y + 1][x + 1] == 1) {
                    //  +1 to compensate for border
                    double alpha = Math.atan2(gy(s, x + 1, y + 1), gx(s, x + 1, y + 1));
                    if (alpha < 0) {
                        alpha += 2 * Math.PI;
                    }
//                double alpha = Math.atan(gy(s, x, y)/ gx(s, x, y));
//                double degrees = alpha * 180 / Math.PI;
                    if (alpha >= Math.PI) {
                        alpha -= Math.PI;
                    }

                    g[y][x] = (Math.round(alpha / Math.PI * 4) % 4) + 1; // range:[1,4]
                }
            }
        }

        if (printAfterOperation) {
            gradientMaxtrix.print();
        }

        return gradientMaxtrix;
    }

    //  Version from LAB 6 slides
    private static double gy(double[][] f, int x, int y) {
        return f[y + 1][x - 1] + 2 * f[y + 1][x] + f[y + 1][x + 1]
                - f[y - 1][x - 1] - 2 * f[y - 1][x] - f[y - 1][x + 1];
    }

    private static double gx(double[][] f, int x, int y) {
        return f[y - 1][x + 1] + 2 * f[y][x + 1] + f[y + 1][x + 1]
                - f[y - 1][x - 1] - 2 * f[y][x - 1] - f[y + 1][x - 1];
    }

    public static MyMatrix gradientSobel(MyMatrix matrix) {

        MyMatrix base = matrix.clone();
        base.makeBorder(1, 1);
        MyMatrix gradient = new MyMatrix(matrix.numRows(), matrix.numCols(), 0, matrix.getName());

        double[][] d = gradient.getMatrix();
        double[][] s = base.getMatrix();
        for (int y = 1; y <= matrix.numRows(); y++) {
            for (int x = 1; x <= matrix.numCols(); x++) {

                double alpha = Math.atan(sy(s, x, y) / sx(s, x, y));
                double degrees = alpha * 180 / Math.PI;
//				System.out.println(degrees);
//				d[y - 1][x - 1] = Math.round(alpha / (2 / (Math.PI * 32)));
                d[y - 1][x - 1] = Math.round(degrees);
            }
        }

        if (printAfterOperation) {
            gradient.print();
        }

        return gradient;
    }

    private static double sy(double[][] f, int j, int i) {
        return f[i - 1][j - 1] + 2 * f[i - 1][j] + f[i - 1][j + 1]
                - f[i + 1][j - 1] - 2 * f[i + 1][j] - f[i + 1][j + 1];
    }

    private static double sx(double[][] f, int j, int i) {
        return f[i - 1][j + 1] + 2 * f[i][j + 1] + f[i + 1][j + 1]
                - f[i - 1][j - 1] - 2 * f[i][j - 1] - f[i + 1][j - 1];
    }

    public static MyMatrix get_MNIST_data(int i, MNISTReader reader) {

        byte[] values = reader.getData(i);
        char label = reader.getLabel(i);

        return new MyMatrix(reader.getNumOfRows(), reader.getNumOfCols(), values, Character.toString(label));
    }

    public static void showStats(MyMatrix matrix) {
        Crossings crossings = new Crossings(matrix, "Crossings");
//        crossings.print();
        ProjectionHistogram projectionHistogram = new ProjectionHistogram(matrix, "Projections");
//        projectionHistogram.print();
        Profiles profiles = new Profiles(matrix, "Profiles");
//        profiles.print();

        XYSeriesCollection dataset = getXySeriesCollection(getXySeries(crossings.getHorizontal()), getXySeries(crossings.getVertical()));
        JFreeChart chart = createChart(dataset, crossings.getTitle());
        if (crossingsPanel == null) {
            crossingsPanel = new ChartPanel(chart);
            crossingsPanel.setPreferredSize(new java.awt.Dimension(500, 270));
        } else {
            crossingsPanel.setChart(chart);
        }
        dataset = getXySeriesCollection(getXySeries(projectionHistogram.getHorizontal()), getXySeries(projectionHistogram.getVertical()));
        chart = createChart(dataset, projectionHistogram.getTitle());
        if (projPanel == null) {
            projPanel = new ChartPanel(chart);
            projPanel.setPreferredSize(new java.awt.Dimension(500, 270));
        } else {
            projPanel.setChart(chart);
        }

        dataset = getXySeriesCollection(getXySeries(profiles.getUpper()),
                getXySeries(profiles.getLower()),
                getXySeries(profiles.getLeft()),
                getXySeries(profiles.getRight()));
        chart = createChart(dataset, profiles.getTitle());
        if (profilePanel == null) {
            profilePanel = new ChartPanel(chart);
            profilePanel.setPreferredSize(new java.awt.Dimension(500, 270));
        } else {
            projPanel.setChart(chart);
        }

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                if (crossingsPane == null) {
                    crossingsPane = new JFrame("Crossings");
                    crossingsPane.setSize(600, 400);
                    crossingsPane.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
                    crossingsPane.getContentPane().add(crossingsPanel);
                }
                crossingsPane.setVisible(true);

                if (projPane == null) {
                    projPane = new JFrame("Projections");
                    projPane.setSize(600, 400);
                    projPane.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
                    projPane.getContentPane().add(projPanel);
                }
                projPane.setVisible(true);

                if (profilePane == null) {
                    profilePane = new JFrame("Profiles");
                    profilePane.setSize(600, 400);
                    profilePane.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
                    profilePane.getContentPane().add(profilePanel);
                }
                profilePane.setVisible(true);
            }
        });
    }

    private static XYSeriesCollection getXySeriesCollection(XYSeries... series) {
        final XYSeriesCollection dataset = new XYSeriesCollection();
        for (XYSeries s : series) {
            dataset.addSeries(s);
        }
        return dataset;
    }

    private static XYSeries getXySeries(Histogram histogram) {
        final XYSeries series = new XYSeries(histogram.getTitle());
        int[] values = histogram.getHistogram();
        for (int i = 0; i < values.length; i++) {
            series.add(i, values[i]);
        }
        return series;
    }

    /**
     * Creates a chart.
     *
     * @param dataset the data for the chart.
     * @return a chart.
     */
    private static JFreeChart createChart(final XYDataset dataset, String title) {

        // create the chart...
        final JFreeChart chart = ChartFactory.createXYLineChart(
                title,      // chart title
                "level",                      // x axis label
                "frequency",                      // y axis label
                dataset,                  // data
                PlotOrientation.VERTICAL,
                true,                     // include legend
                true,                     // tooltips
                false                     // urls
        );

        // NOW DO SOME OPTIONAL CUSTOMISATION OF THE CHART...
        chart.setBackgroundPaint(Color.white);


        // get a reference to the plot for further customisation...
        final XYPlot plot = chart.getXYPlot();
        plot.setBackgroundPaint(Color.lightGray);
        //    plot.setAxisOffset(new Spacer(Spacer.ABSOLUTE, 5.0, 5.0, 5.0, 5.0));
        plot.setDomainGridlinePaint(Color.white);
        plot.setRangeGridlinePaint(Color.white);

        final XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        plot.setRenderer(renderer);

        // change the auto tick unit selection to integer units only...
        final NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        // OPTIONAL CUSTOMISATION COMPLETED.

        return chart;

    }

    public static void showStat(String title, Histogram histogram) {

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                JFrame frame = new JFrame("Charts");

                frame.setSize(600, 400);
                frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
                frame.setVisible(true);

                SimpleHistogramDataset hist = new SimpleHistogramDataset(histogram.getTitle());

                int[] values = histogram.getHistogram();
                for (int i = 0; i < values.length; i++) {
                    hist.addBin(new SimpleHistogramBin(i, i + 1, true, false));

                    for (int j = 0; j < values[i]; j++) {
                        hist.addObservation(i);
                    }
                }
                JFreeChart chart = ChartFactory.createXYLineChart(histogram.getTitle(),
                        "level", "frequency", hist, PlotOrientation.VERTICAL, true, true,
                        false);

                ChartPanel cp = new ChartPanel(chart);

                frame.getContentPane().add(cp);
            }
        });
    }

    public static List<Point> findEndpoints(MyMatrix matrix) {

        ArrayList<Point> list = new ArrayList<Point>();

        double[][] m = matrix.getMatrix();
        for (int y = 0; y < matrix.numRows(); y++) {
            for (int x = 0; x < matrix.numCols(); x++) {
                if ( m[y][x] == 1 && calculateB(calculatePs(m, x, y)) == 1 ) {
                    list.add(new Point(x,y));
                }
            }
        }

        return list;
    }
}
