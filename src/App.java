import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Vector;

public class App extends JFrame {
    JFrame frame = new JFrame("App");
    Hough transformHough;
    ImagePanel imagePanel;
    Vector<HoughLine> lines;

    public App() {
    }

    public static void main(String[] args) {
        /** p r o g r a m m e **/
        App app1 = new App();
        App app2 = new App();
        ValidationChar vc = new ValidationChar();
        //   Vector<HoughLine> lineEleve = app2.applyDetection(1, "mMajtest");
        System.out.println();
        System.out.println();
        Vector<HoughLine> lineRef = app1.applyDetection(2, "mMaj");
    }

    public void setUrl() {
    }

    Vector<HoughLine> applyDetection(int index,
                                     String namefile) {
        ProcessImage processImage = new ProcessImage();
        BufferedImage im = loadImage(namefile);
        System.out.println("erosion ...");
        for (int i = 0; i < 10; i++) im = processImage.erosion(im);
        save(im, "testErosion" + index);
        im = processImage.bAndW(im);
        save(im, "testBW" + index);
        im = processImage.formatageIm(im);
        save(im, "redecoupage" + index);
        transformHough = new Hough();
        transformHough.initialiseHough(im.getWidth(), im.getHeight());
        transformHough.addPoints(im);
        Vector<HoughLine> lines = transformHough.getLines(6, 32);
        //lines = reductionLineSimilar(lines);
        save(im, "image" + index);
        System.out.println(" nb= lines " + lines.size());
        imagePanel = new ImagePanel(lines, index);
        initFrame();
        System.out.println("num points :" + transformHough.numPoints);
        return lines;
    }

    void initFrame() {
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        /**  M E N U    B A R   **/
        JMenuBar menuBar = new JMenuBar();
        JMenu file = new JMenu("file");
        JMenu edit = new JMenu("edit");
        JMenu help = new JMenu("help");
        JMenuItem newAction = new JMenuItem("new");
        JMenuItem loadAction = new JMenuItem("open");
        JMenuItem saveAction = new JMenuItem("save");
        file.add(newAction);
        file.add(loadAction);
        file.add(saveAction);
        menuBar.add(file);
        menuBar.add(edit);
        menuBar.add(help);
        frame.add(menuBar);
        frame.setJMenuBar(menuBar);
        /** P a n n e a u x **/
        JPanel panel1 = new JPanel();
        panel1.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.BLACK), "Panel 1"));

        JButton houghAction = new JButton("Hough transform");
        JButton ocrAction = new JButton("Detect");
        GridLayout gridLayout = new GridLayout(1, 2);
        gridLayout.setHgap(5);
        gridLayout.setVgap(5);
        panel1.setLayout(gridLayout);
        panel1.add(houghAction);
        panel1.add(ocrAction);

        /*** C A N V A S**/
        frame.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 0.5;
        gbc.gridy = 0;
        gbc.weighty = 0.05;
        frame.add(panel1, gbc);
        gbc.gridy = 1;
        gbc.weighty = 0.3;
        frame.add(imagePanel, gbc);
        gbc.gridy = 2;
        gbc.weighty = 0.2;
        frame.setSize(800, 600);
        frame.setMinimumSize(new Dimension(800, 600));
        frame.setResizable(true);
        frame.setVisible(true);
    }

    BufferedImage loadImage(String nameFile) {

        try {
            BufferedImage im;
            if (nameFile != null) {
                System.out.println("load " + nameFile + "");
                im = ImageIO.read(new File("/home/excilys/eclipse-workspace/OCR/picture/" + nameFile + ".png"));
            } else {
                im = ImageIO.read(new File("/home/excilys/eclipse-workspace/OCR/picture/mMaj.png"));
            }
            return im;
        } catch (IOException e) {
            System.out.println(" no file found");
            return null;
        }
    }

    void save(BufferedImage im, String name) {
        File outputfile;
        if (name != null) {
            outputfile = new File(name + ".png");
        } else {
            outputfile = new File("image.png");
        }

        try {
            ImageIO.write(im, "png", outputfile);
        } catch (IOException e) {
            System.out.println(" error save image");
        } finally {
            System.out.println(" save file ... ");
        }
    }

    @Override
    public void paintComponents(Graphics graphics) {
        super.paintComponents(graphics);
    }

    double diff(float valeur1, float valeur2) {
        return (Math.abs(Math.abs(valeur2) - Math.abs(valeur1)));
    }

    double distancePoints(float x1, float y1, float x2, float y2) {
        return Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
    }

    Vector<HoughLine> reductionLineSimilar(Vector<HoughLine> lines) {
        int i = 0, j = 0;
        int epsilon = 50;
        while (i < lines.size()) {
            j = i;
            double m = (lines.get(i).y2 - lines.get(i).y1) / (lines.get(i).x2 - lines.get(i).x1);
            while (j < lines.size()) {
                double m2 = (lines.get(j).y2 - lines.get(j).y1) / (lines.get(j).x2 - lines.get(j).x1);
                // droite sont horizontale

                if (lines.get(i) != lines.get(j)) {
                    System.out.println();
                    System.out.println(ConsoleColor.CYAN + " i =" + i + " j=" + j + ConsoleColor.RESET);
                    if (compareAngle(m, m2)) {
                        lines.remove(lines.get(j));
                        System.out.println(ConsoleColor.GREEN + " Reduction " + ConsoleColor.RESET);
                    }
                }

                j++;

            }
            i++;
        }

        for (HoughLine line : lines) {
            double m = (line.y2 - line.y1) / (line.x2 - line.x1);
            if (Double.isInfinite(m)) {
                m = 999;
            }
            double b = line.y1 - m * line.x1;
            //System.out.println(" Equation y= " + m + " x " + b);
        }
        return lines;
    }

    boolean compareB(HoughLine line1, HoughLine line2, double m1, double m2) {
        if (Double.isInfinite(m1)) {
            m1 = 999;
        }
        if (Double.isInfinite(m2)) {
            m2 = 999;
        }
        double b1 = line1.y1 - m1 * line1.x1;
        line1.b = b1;
        double b2 = line2.y1 - m1 * line2.x1;

        double diff = b2 - b1;
        //   System.out.println("");

        System.out.println(ConsoleColor.RED + " diff b " + diff + ConsoleColor.RESET);

        if (diff < 0) {
            diff = -1 * diff;
        }
        if (diff < 50) {
            return true;
        } else {
            return false;
        }
    }

    double foundPente(HoughLine line1, HoughLine line2) {
        return (line1.y2 - line2.y1) / (line1.x2 - line2.x1);
    }

    boolean compareAngle(double m1, double m2) {
        double expression1 = Math.abs((m2 - m1) / (1 + m1 * m2));
        double angle = Math.atan(expression1);
        System.out.println(" angle diff " + Math.toDegrees(angle));

        if (Math.toDegrees(angle) < 10) return true;
        else return false;
    }

    boolean comparePente(double m, double m2) {
        if (Double.isInfinite(m)) {
            m = 999;
        }
        if (Double.isInfinite(m2)) {
            m2 = 999;
        }
        double diff = m2 - m;
        if (diff < 15) {
            return true;
        } else {
            return false;
        }
    }

}

