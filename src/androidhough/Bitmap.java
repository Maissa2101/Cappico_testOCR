package androidhough;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.IOException;
import java.io.OutputStream;

public class Bitmap {
    private final BufferedImage bufferedImage;
    public BufferedImage getBufferedImage() {
        return bufferedImage;
    }
    public static final class Config {
        public static final int ARGB_8888 = BufferedImage.TYPE_INT_ARGB;
    };
    public static enum CompressFormat {
        JPEG, PNG, WEBP
    }

    public Bitmap(BufferedImage bufferedImage) {
        this.bufferedImage = bufferedImage;
    }

    public static Bitmap createBitmap(int[] data, int width, int height, int type) {
        // https://stackoverflow.com/questions/9396159/how-do-i-create-a-bufferedimage-from-array-containing-pixels
        BufferedImage bufferedImage = new BufferedImage( width, height, type );
        final int[] a = ( (DataBufferInt) bufferedImage.getRaster().getDataBuffer() ).getData();
        System.arraycopy(data, 0, a, 0, data.length);
        return  new Bitmap(bufferedImage);
    }

    /* File outputfile = new File("image.jpg");
    ImageIO.write(bufferedImage, "jpg", outputfile);*/
    //im.compress(Bitmap.CompressFormat.PNG, 100, outputfile);
    public void compress(CompressFormat format, int qualityRatio, OutputStream outputStream) {
        try {
            ImageIO.write(this.bufferedImage, format.toString().toLowerCase(), outputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //getPixels(formatedImageTable.getImage(),0,w,0,0,w,h);
    public void getPixels(int[] dst, int offset, int stride, int x, int y, int width, int height) {
        final int[] data = ( (DataBufferInt) bufferedImage.getRaster().getDataBuffer() ).getData();
        System.arraycopy(data,0, dst, 0, data.length);
    }

    public int getWidth() {
        return bufferedImage.getWidth();
    }

    public int getHeight() {
        return bufferedImage.getHeight();
    }
}
