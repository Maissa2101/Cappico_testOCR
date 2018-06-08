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
        Vector<HoughLine> lineEleve = app2.applyDetection(1, "iMajtest");
        System.out.println();
        System.out.println();
        Vector<HoughLine> lineRef = app1.applyDetection(2, "iMaj");
        boolean comparaison = vc.ValidateMaj(lineEleve,lineRef);
        if(comparaison) System.out.println(" Bon joueé c'est la bonne lettre !! ");
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
      // lines = reductionLineSimilar(lines);
        save(im, "image" + index);
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

    double distance(float valeur1, float valeur2) {
        return (Math.sqrt(Math.pow(valeur2 - valeur1, 2)));
    }

    Vector<HoughLine> reductionLineSimilar(Vector<HoughLine> lines) {
        int i= 0,j=0;
        while(i<lines.size()) {
            j=0;
            while(j<lines.size()) {
                if (lines.get(i) != lines.get(j)) {
                    if (distance(lines.get(i).x1, lines.get(j).x1) <= 20 && distance(lines.get(i).x2, lines.get(j).x2) <= 20 && distance(lines.get(i).y1, lines.get(j).y1) <= 20 && distance(lines.get(i).y2, lines.get(j).y2) <= 20) {
                        lines.remove(lines.get(i));
                    }
                }
                j++;
            }
            i++;
        }
        return lines;
    }
}
