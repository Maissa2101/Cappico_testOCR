import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Vector;

public class ImagePanel extends JPanel {
    int w, h;
    private BufferedImage im = null;
    protected Vector<HoughLine> lines;
    public ImagePanel(Vector<HoughLine> lines) {
        {
            this.lines = lines;
            this.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.BLACK), " Preview"));

            try {
                im = ImageIO.read(new File("/home/excilys/eclipse-workspace/OCR/image.png"));
                JLabel label = new JLabel(new ImageIcon("/home/excilys/eclipse-workspace/OCR/image.png"));
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
        if (lines.size()>0) {
            for (HoughLine line:lines) {
                graphics.drawLine((int)line.x1, (int)line.x2, (int)line.y1, (int)line.y2);
               System.out.println(" ("+line.x1+","+line.y1+") -- ("+ line.x2+", "+line.y2);
            }
        }
    }

}
