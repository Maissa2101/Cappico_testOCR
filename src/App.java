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
        App app = new App();
        BufferedImage im = app.loadImage();
        im = app.seuilImg(im);
        app.transformHough = new Hough();
        app.transformHough.initialiseHough(im.getWidth(), im.getHeight());
        app.transformHough.addPoints(im);
        Vector<HoughLine> lines = app.transformHough.getLines(5, 25);
        //  lines.get(i).
        app.save(im);
        app.imagePanel = new ImagePanel(lines);
        app.initFrame();
        System.out.println(" num points : " + app.transformHough.numPoints);


    }

    public void setUrl() {
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
        frame.setResizable(false);
        frame.setVisible(true);
    }

    BufferedImage loadImage() {
        try {
            BufferedImage im = ImageIO.read(new File("/home/excilys/eclipse-workspace/OCR/picture/amin.png"));
            return im;
        } catch (IOException e) {
            System.out.println(" no file found");
            return null;
        } finally {
            System.out.println(" loading file ... ");
        }
    }

    BufferedImage seuilImg(BufferedImage im) {
        System.out.println(" seuillage ...");

        for (int i = 0; i < im.getWidth(); i++) {
            for (int j = 0; j < im.getHeight(); j++) {
                im.setRGB(i, j, (-1 * im.getRGB(i, j)));
                if (im.getRGB(i, j) == 1) {
                    im.setRGB(i, j, Color.WHITE.getRGB());
                } else {
                    im.setRGB(i, j, Color.black.getRGB());
                }

            }
        }
        return im;
    }

    void save(BufferedImage im) {
        File outputfile = new File("image.png");
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
}
