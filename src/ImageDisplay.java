import MNIST.MNISTReader;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.Stack;


public class ImageDisplay extends JFrame {

    private final JButton invert;
    private final JButton normalize;
    private final JButton binarize;
    private final JButton denoise;
    private final JButton deslant;
    private final JButton smooth;
    private final JButton thin;
    private final JButton prev;
    private final JButton next;
    private final JButton undo;
    private final JButton gradient;
    JButton load;

    Stack<MyMatrix> stack = new Stack<>();

    JPanel upperPanel;

    MyMatrix matrix;
    MNISTReader reader;
    private int current = 0;

    public static void main(String[] args) {

        if ( args.length != 2 ) {
            System.err.println("Call with MNIST data and label files");
            System.exit(1);
        }

        ImageDisplay img = new ImageDisplay(args[0], args[1]);
        img.setSize(600, 400);
        img.setVisible(true);
        img.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        img.setResizable(true);
    }

    ImageDisplay(String mnistData, String mnistLabel) {
        super("Image Workbench");

        reader = new MNISTReader(mnistData, mnistLabel);

        load = new JButton("Load");
        prev = new JButton("Prev");
        next = new JButton("Next");
        invert = new JButton("Invert");
        normalize = new JButton("Normalize");
        binarize = new JButton("Binarize");
        denoise = new JButton("Denoise");
        deslant = new JButton("Deslant");
        smooth = new JButton("Smooth");
        thin = new JButton("Thin");
        gradient = new JButton("Gradient");
        undo = new JButton("Undo");

        DrawPanel panel = new DrawPanel();
        load.addActionListener(panel);
        binarize.addActionListener(panel);
        prev.addActionListener(panel);
        next.addActionListener(panel);
        normalize.addActionListener(panel);
        invert.addActionListener(panel);
        denoise.addActionListener(panel);
        deslant.addActionListener(panel);
        smooth.addActionListener(panel);
        thin.addActionListener(panel);
        gradient.addActionListener(panel);
        undo.addActionListener(panel);

        upperPanel = new JPanel();
        setLayout(new BorderLayout());

        upperPanel.add(load);
        upperPanel.add(prev);
        upperPanel.add(next);
        upperPanel.add(binarize);
        upperPanel.add(invert);
        upperPanel.add(normalize);
        upperPanel.add(denoise);
        upperPanel.add(deslant);
        upperPanel.add(smooth);
        upperPanel.add(thin);
        upperPanel.add(gradient);
        upperPanel.add(undo);

        add(upperPanel, BorderLayout.NORTH);
        add(panel, BorderLayout.CENTER);

        ImageUtils.setPrintAfterOperation(true);
    }

    private class DrawPanel extends JPanel implements ActionListener {

        public void actionPerformed(ActionEvent e) {

            if ( matrix != null && !e.getActionCommand().equals("Undo")) {
                stack.push(matrix.clone());
            }

            if (e.getActionCommand().equals("Load")) {
                try {
                    JFileChooser chooser = new JFileChooser();
                    FileNameExtensionFilter filter = new FileNameExtensionFilter(
                            "Text Files", "txt");
                    chooser.setFileFilter(filter);
                    int returnVal = chooser.showOpenDialog(upperPanel);
                    if (returnVal == JFileChooser.APPROVE_OPTION) {
                        System.out.println("You chose to open this file: " +
                                chooser.getSelectedFile().getAbsolutePath());
                    }
                    matrix = new MyMatrix(ImageOperations.readTextFile(chooser.getSelectedFile().getAbsolutePath()), "pattern1.txt");
                    matrix.print();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            } else if (e.getActionCommand().equals("Prev")) {
                if (--current < 0) {
                    current = 0;
                }
                matrix = ImageUtils.get_MNIST_data(current, reader);
                matrix.binarize();
                matrix.print();
            } else if (e.getActionCommand().equals("Next")) {
                if (current < reader.getNumOfImages() - 1) {
                    current++;
                }
                matrix = ImageUtils.get_MNIST_data(current, reader);
                matrix.binarize();
                matrix.print();
            } else if (e.getActionCommand().equals("Binarize")) {
                matrix.binarize();
                matrix.print();
            } else if (e.getActionCommand().equals("Invert")) {
                matrix = ImageUtils.invert(matrix);
            } else if (e.getActionCommand().equals("Invert")) {
                matrix = ImageUtils.invert(matrix);
            } else if (e.getActionCommand().equals("Normalize")) {
                matrix = ImageUtils.normalize(matrix);
            } else if (e.getActionCommand().equals("Denoise")) {
                matrix = ImageUtils.binarySmooth(matrix);
            } else if (e.getActionCommand().equals("Smooth")) {
                ImageUtils.smooth(matrix);
                matrix.binarize();
                matrix.print();
            } else if (e.getActionCommand().equals("Thin")) {
                matrix = ImageUtils.thinning(matrix);
            } else if (e.getActionCommand().equals("Deslant")) {
                matrix = ImageUtils.correctSlant(matrix);
            } else if (e.getActionCommand().equals("Gradient")) {
                matrix = ImageUtils.gradient(matrix);
            } else if (e.getActionCommand().equals("Undo")) {

                if ( !stack.empty()) {
                    matrix = stack.pop();
                    matrix.print();
                }
            }

            repaint();
        }

        public void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;

            setBackground(Color.WHITE);
            g.setColor(Color.WHITE);
            g2.fill(new Rectangle2D.Double(0, 0, 600, 400));
            g.setColor(Color.BLACK);

            int zoom = 4;
            if (matrix != null) {
                double[][] m = matrix.getMatrix();
                for (int y = 0; y < matrix.numRows(); y++) {
                    for (int x = 0; x < matrix.numCols(); x++) {
                        if (m[y][x] != 0) {
                            g2.fill(new Rectangle2D.Double(x * zoom, y * zoom, zoom, zoom));
                        }
                    }
                }
            }
        }
    }
}

