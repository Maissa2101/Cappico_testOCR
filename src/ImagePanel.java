import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Vector;

public class ImagePanel extends JPanel {
    protected Vector<HoughLine> lines;
    int w, h;
    private BufferedImage im = null;

    public ImagePanel(Vector<HoughLine> lines, int index) {
        {
            this.lines = lines;
            this.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.BLACK), " Preview"));

            try {
                im = ImageIO.read(new File("/home/excilys/capico/tests/Cappico_testOCR/image" + index + ".png"));
                JLabel label = new JLabel(new ImageIcon("/home/excilys/capico/tests/Cappico_testOCR/image" + index + ".png"));
                this.add(label);
            } catch (IOException e) {
                System.out.println(" erreur loading image ...");
            }
            repaint();
        }
    }

    @Override
    public void paint(Graphics graphics) {
        super.paint(graphics);
        int i = 0;
        if (lines.size() > 0) {
            for (HoughLine line : lines) {
                if (line != null) {
                    if (i < 2) {
                        graphics.setColor(Color.RED);
                    } else {
                        if ((i<4)) {
                            graphics.setColor(Color.GREEN);
                        } else {
                            graphics.setColor(Color.CYAN);
                        }
                    }
                    graphics.drawLine((int) line.x1 + (getWidth() / 2 - (im.getWidth() / 2)), (int) line.y1 + (22), (int) line.x2 + (getWidth() / 2 - (im.getWidth() / 2)), (int) line.y2 + 22);
                    i++;
                }
            }
        }
    }

}
