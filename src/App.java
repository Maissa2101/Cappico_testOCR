import org.opencv.core.*;
import org.opencv.core.Point;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import static org.opencv.core.Core.countNonZero;
import static org.opencv.core.CvType.CV_8UC1;

public class App extends JFrame {
    static final int precision = 20;

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    List<Point> listCenter = new ArrayList<>();
    List<Integer> listRadius = new ArrayList<>();
    JFrame frame = new JFrame("App");
    Hough transformHough;
    ImagePanel imagePanel;
    Vector<HoughLine> lines;
    double ecart;

    public static void main(String[] args) {
        /** p r o g r a m m e **/
        App app1 = new App();
        App app2 = new App();
        ValidationChar vc = new ValidationChar();
        List<HoughLine> lineEleve = app2.applyDetectionLine(1, "p2");
        System.out.println();
        System.out.println();
        List<HoughLine> lineRef = app1.applyDetectionLine(2, "p");
        //  double tauxReussite =  app1.compareLetter(lineEleve,lineRef);
        // System.out.println(ConsoleColor.RED+ " taux reussite "+ tauxReussite);
//        app1.applyDetectionCircle(1,"p");
//        app2.applyDetectionCircle(2,"p2");
//        app1.applyAll(1,lineRef);
//        app2.applyAll(2,lineEleve);

    }

    public static Mat BufferedImage2Mat(BufferedImage image) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ImageIO.write(image, "jpng", byteArrayOutputStream);
        byteArrayOutputStream.flush();
        return Imgcodecs.imdecode(new MatOfByte(byteArrayOutputStream.toByteArray()), Imgcodecs.CV_LOAD_IMAGE_UNCHANGED);
    }

    BufferedImage skeleton(int index, String name) {
        System.out.println(" name " + name);
        Mat src = loadImageMat(name);
        Imgproc.threshold(src, src, 127, 255, Imgproc.THRESH_BINARY_INV);
        Mat skel = new Mat(src.size(), CV_8UC1, new Scalar(0));
        Mat temp = new Mat(), eroded = new Mat();
        Mat element = Imgproc.getStructuringElement(Imgproc.MORPH_CROSS, new Size(3, 3));
        boolean done = false;
        Mat backup = new Mat();
        Mat gray = new Mat();
        double size = src.size().height*src.size().width;
        int i = 0;
        while (!done) {
            i++;
            backup = src;
            Imgproc.erode(src, eroded, element);
            Imgproc.dilate(eroded, temp, element);
            Core.subtract(src, temp, temp);
            Core.bitwise_or(skel, skel, temp, skel);
            eroded.copyTo(src);
            Imgproc.cvtColor(src, gray, Imgproc.COLOR_BGR2GRAY);
            BufferedImage skelImg = null;
            try {
                skelImg = Mat2BufferedImage(src);
                save(skelImg,"skeleton "+i);
            } catch (Exception e) {
                e.printStackTrace();
            }
            double zeros;
            zeros = size - countNonZero(gray) ;
            System.out.println("Size = " + size);
            System.out.println("zero = "+zeros);
            if(zeros==size){
                done=true;
            }

        }

        BufferedImage skelImg = null;
        try {
            skelImg = Mat2BufferedImage(backup);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return skelImg;
    }

    List<HoughLine> applyDetectionLine(int index,
                                       String namefile) {
        ProcessImage processImage = new ProcessImage();
        BufferedImage im = loadImage(namefile);
        System.out.println("erosion ...");
        System.out.println(" fin loading");
        im = skeleton(index, namefile);

//        for (int i = 0; i < 10; i++) im = processImage.erosion(im);
        save(im, "testErosion" + index);
//        im = processImage.bAndW(im);
//        save(im, "testBW" + index);
//        im = processImage.formatageIm(im);
//        save(im, "redecoupage" + index);
        transformHough = new Hough();
        transformHough.initialiseHough(im.getWidth(), im.getHeight());
        transformHough.addPoints(im);
        List<HoughLine> lines = new ArrayList(transformHough.getLines(10, 32));
        lines = reductionLineSimilar(lines);
        return lines;
    }

    void applyAll(int index, List<HoughLine> lines) {
        Mat src = loadImageMat("redecoupage" + index);
        BufferedImage imLine = Mix(src, lines);
        save(imLine, "imageAll " + index);
    }

    BufferedImage Mix(Mat src, List<HoughLine> lines) {
        HoughEllipse elli = new HoughEllipse();
        for (int x = 0; x < lines.size(); x++) {
            Point p1 = new Point();
            Point p2 = new Point();
            p1.set(new double[]{lines.get(x).x1, lines.get(x).y1});
            p2.set(new double[]{lines.get(x).x2, lines.get(x).y2});
            Imgproc.line(src, p1, p2, new Scalar(0, 100, 100), 3, 8, 0);
        }
        for (int x = 0; x < listRadius.size(); x++) {
            // circle center
            Imgproc.circle(src, listCenter.get(x), 1, new Scalar(0, 100, 100), 3, 8, 0);
            // circle outline
            Imgproc.circle(src, listCenter.get(x), listRadius.get(x), new Scalar(255, 0, 255), 3, 8, 0);
        }
        BufferedImage im = null;
        try {
            im = elli.Mat2BufferedImage(src);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return im;
    }

    void applyDetectionCircle(int index, String namefile) {
        ProcessImage processImage = new ProcessImage();
        BufferedImage im = loadImage(namefile);
        System.out.println("erosion ...");
        for (int i = 0; i < 8; i++) im = processImage.erosion(im);
        save(im, "testErosion" + index);
        im = processImage.formatageImCircle(im);
        save(im, "redecoupage" + index);
        HoughEllipse circleHough = new HoughEllipse();
        BufferedImage imCircle = circleHough.run("redecoupage" + index, precision);
        this.listRadius = circleHough.listRadius;
        this.listCenter = circleHough.listCenter;

        save(imCircle, "testCircle" + index);
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
                im = ImageIO.read(new File("/home/excilys/eclipse-workspace/OCR/picture/.png"));
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

    Mat loadImageMat(String name) {
        System.out.println(" loading MAT IMG");
        String file = "/home/excilys/eclipse-workspace/OCR/picture/" + name + ".png";

        Mat src = Imgcodecs.imread(file, Imgcodecs.IMREAD_COLOR);

        if (src.empty()) {
            System.out.println("Error opening image!");
            System.out.println("Program Arguments: [image_name -- default "
                    + file + "] \n");
            System.exit(-1);
        }

        return src;
    }

    double foundPente(HoughLine line1, HoughLine line2) {
        return (line1.y2 - line2.y1) / (line1.x2 - line2.x1);
    }

    boolean compareAngle(double angle, double angle2) {
        if (Math.abs(angle - angle2) < precision) {
            ecart = (Math.abs(angle - angle2));
            return true;
        } else {
            if (Math.abs(angle - angle2) > 90 && 180 - Math.abs(angle - angle2) < precision) {
                ecart = (180 - Math.abs(angle - angle2));
                return true;
            } else {
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
        System.out.println(" Validation ::");
        int i = 0, j;
        double nbValidateLine = 0;


        List<HoughLine> linesRef = new ArrayList<>();
        List<HoughLine> linesTest = new ArrayList<>();

        if (!(linesP.size() > linesM.size())) {
            linesRef.addAll(linesM);
            linesTest.addAll(linesP);
        } else {
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
                    System.out.println(ConsoleColor.BLUE + " ecart " + (1 - (ecart / 100)));
                    nbValidateLine = (nbValidateLine) + (1 - (ecart / 100));
                    break;
                } else {
                    j++;
                }
            }
            i++;
        }

        if ((linesP.size() > linesM.size())) {
            return ((nbValidateLine * 100) / linesP.size());
        } else {
            return ((nbValidateLine * 100) / linesM.size());

        }

    }

    BufferedImage Mat2BufferedImage(Mat matrix) throws Exception {
        MatOfByte mob = new MatOfByte();
        Imgcodecs.imencode(".jpg", matrix, mob);
        byte ba[] = mob.toArray();
        BufferedImage bi = ImageIO.read(new ByteArrayInputStream(ba));
        return bi;
    }

}

