import org.opencv.core.Point;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class App extends JFrame {

    List<Point> listCenter= new ArrayList<>();
    List<Integer> listRadius= new ArrayList<>();

    static {
        System.loadLibrary(org.opencv.core.Core.NATIVE_LIBRARY_NAME);
        System.loadLibrary("opencv_java342");
    }

    JFrame frame = new JFrame("App");
    Hough transformHough;
    ImagePanel imagePanel;
    Vector<HoughLine> lines;
    double ecart;
    int precision = 20;

    public static void main(String[] args) {
        /** p r o g r a m m e **/
        App app1 = new App();
        App app2 = new App();
        ValidationChar vc = new ValidationChar();
                List<HoughLine> lineEleve = app2.applyDetection(1, "p");
        System.out.println();
        System.out.println();
        List<HoughLine> lineRef = app1.applyDetection(2, "p2");
        double tauxReussite =  app1.compareLetter(lineEleve,lineRef);
        System.out.println(ConsoleColor.RED + " taux de réussite "+ tauxReussite);
        app1.applyDetectionCircle(1,"p");
        app2.applyDetectionCircle(2,"p2");
    }

    List<HoughLine> applyDetection(int index, String namefile) {
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
        List<HoughLine> lines = new ArrayList(transformHough.getLines(10, 32));
        lines = reductionLineSimilar(lines);
        save(im, "image" + index);
        System.out.println("nb of lines " + lines.size());
        imagePanel = new ImagePanel(lines, index);
        initFrame();
        System.out.println("nb of points :" + transformHough.numPoints);
        return lines;
    }

    void applyDetectionCircle(int index, String namefile) {
        ProcessImage processImage = new ProcessImage();
        BufferedImage im = loadImage(namefile);
        System.out.println("erosion ...");
        for (int i = 0; i < 8; i++) im = processImage.erosion(im);
        save(im, "testErosion" + index);
        im = processImage.formatageImCircle(im);
        save(im, "redecoupage" + index);
        HoughCircle circleHough = new HoughCircle();
        BufferedImage imCircle = circleHough.run("redecoupage"+index, precision);
        this.listRadius= circleHough.listRadius;
        this.listCenter = circleHough.listCenter;
        save(imCircle,"testCircle"+index);
    }

    void initFrame() {
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        /**  M E N U  B A R   **/
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

        /** C A N V A S **/
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
                im = ImageIO.read(new File("/home/excilys/capico-java/Cappico_testOCR/picture/" + nameFile + ".png"));
            } else {
                im = ImageIO.read(new File("/home/excilys/capico-java/Cappico_testOCR/picture/mMaj.png"));
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

    List<HoughLine> reductionLineSimilar(List<HoughLine> lines) {
        int i = 0, j;
        while (i < lines.size()) {
            j = i + 1;
            double m = (lines.get(i).y2 - lines.get(i).y1) / (lines.get(i).x2 - lines.get(i).x1);
            while (j < lines.size()) {
                double m2 = (lines.get(j).y2 - lines.get(j).y1) / (lines.get(j).x2 - lines.get(j).x1);
                // droite sont horizontale

                if (lines.get(i) != lines.get(j)) {

                    // angle par rapport à l'horizontale
                    double angle = Math.toDegrees(Math.atan((m)));
                    double angle2 = Math.toDegrees(Math.atan((m2)));

                    if (compareAngle(angle, angle2) && compareB(lines.get(i), lines.get(j), m, m2, Math.max(Math.abs(angle), Math.abs(angle2)))) {
                        lines.remove(lines.get(j));
                        j -= 1;
                    }
                }
                j++;
            }
            i++;
        }

        return lines;
    }

    boolean compareB(HoughLine line1, HoughLine line2, double m1, double m2, double angle) {
        if (Double.isInfinite(m1)) {
            m1 = 999;
        }
        if (Double.isInfinite(m2)) {
            m2 = 999;
        }
        double b1 = line1.y1 - m1 * line1.x1;
        double b2 = line2.y1 - m1 * line2.x1;
        double diff = b2 - b1;
        diff = Math.abs(diff);
        if (diff <= precision * (angle)) {
            return true;
        } else {
            return false;
        }
    }

    double foundPente(HoughLine line1, HoughLine line2) {
        return (line1.y2 - line2.y1) / (line1.x2 - line2.x1);
    }

    boolean compareAngle(double angle, double angle2) {
        if (Math.abs(angle - angle2) < precision){
            ecart=(Math.abs(angle-angle2));
            return true;
        }
        else{
            if (Math.abs(angle - angle2) > 90 && 180 - Math.abs(angle - angle2) < precision){
                ecart= (180-Math.abs(angle-angle2));
                return true;
            }
            else{
                return false;
            }
        }
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

   double compareLetter(List<HoughLine> linesP, List<HoughLine> linesM) {
        System.out.println(" Validation :");
        int i = 0, j;
        double nbValidateLine=0;


        List<HoughLine> linesRef = new ArrayList<>();
        List<HoughLine> linesTest = new ArrayList<>();

       if( !(linesP.size()>linesM.size())) {
           linesRef.addAll(linesM);
           linesTest.addAll(linesP);
       }else{
           linesRef.addAll(linesP);
           linesTest.addAll(linesM);

       }
        while (i < linesRef.size()) {
            j = 0;
            double m = (linesRef.get(i).y2 - linesRef.get(i).y1) / (linesRef.get(i).x2 - linesRef.get(i).x1);
            while (j < linesTest.size()) {
                double m2 = (linesTest.get(j).y2 - linesTest.get(j).y1) / (linesTest.get(j).x2 - linesTest.get(j).x1);
                double angle = Math.toDegrees(Math.atan((m)));
                double angle2 = Math.toDegrees(Math.atan((m2)));
                if (compareAngle(angle, angle2) && compareB(linesRef.get(i), linesTest.get(j), m, m2, Math.max(Math.abs(angle), Math.abs(angle2)))) {
                    linesRef.remove(linesRef.get(i));
                    linesTest.remove(linesTest.get(j));
                    i--;
                    System.out.println(ConsoleColor.BLUE+" écart "+ (1-(ecart/100)));
                    nbValidateLine=(nbValidateLine) + (1-(ecart/100));
                    break;
                }else{
                    j++;
                }
            }
            i++;
        }

       if((linesP.size()>linesM.size())) {
           return ((nbValidateLine*100)/linesP.size());
       }else{
           return ((nbValidateLine*100)/linesM.size());

       }

    }


}

