
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.StringTokenizer;

/**
 * Created by joao on 2014-09-11.
 */
public class ImageOperations {


    public static double[][] readTextFile(String filename) throws IOException {

        double[][] array;
        try {
            BufferedReader fis = new BufferedReader(new InputStreamReader(new FileInputStream(filename)));

            int numCols = fis.readLine().split("\t").length;
            int numRows = 1;
            while (fis.readLine() != null) {
                numRows++;
            }
            fis.close();

            array = new double[numRows][numCols];
            fis = new BufferedReader(new InputStreamReader(new FileInputStream(filename)));
            String line;
            for (int i = 0; (line = fis.readLine()) != null; i++) {
                StringTokenizer input = new StringTokenizer(line, "\t");
                for (int j = 0; input.hasMoreElements(); j++) {
                    array[i][j] = Integer.parseInt(input.nextToken());
                }
            }
        } catch (IOException e) {

            System.out.println("failed to open " + filename);
            throw e;
        }

        return array;
    }

    public static double[][] readJPEGFile(String filename) throws IOException {

        double[][] array;
        try {
            BufferedImage img = ImageIO.read(new File(filename));

            array = new double[img.getHeight()][img.getWidth()];
            for (int j = 0; j < img.getHeight(); j++) {
                for (int i = 0; i < img.getWidth(); i++) {

                    //  gray scale images will contain the same value for R, G, and B, so just pick one. Alpha will be 255.
                    Color c = new Color(img.getRGB(i, j));

                    int r = c.getRed();
                    int g = c.getBlue();
                    int b = c.getGreen();
                    int a = c.getAlpha();

                    array[j][i] = c.getRed();
                }
            }
        } catch (IOException e) {

            System.out.println("failed to open " + filename);
            throw e;
        }

        return array;
    }

    public static void saveImage(MyMatrix matrix, String filename) {
        try {

            BufferedImage img = new BufferedImage(matrix.numCols(), matrix.numRows(), BufferedImage.TYPE_INT_RGB);
            double[][] array = matrix.getMatrix();
            for (int j = 0; j < img.getHeight(); j++) {
                for (int i = 0; i < img.getWidth(); i++) {

                    int pixel = (int)array[j][i];
                    Color c = new Color(pixel,pixel,pixel);

                    img.setRGB(i, j, c.getRGB());
                }
            }

            ImageIO.write(img, "jpg", new File(filename));

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}