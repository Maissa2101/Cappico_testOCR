package androidhough;

public class ConvolveMatrix
{
    public static final int SIZE = 3;


    public static ImageTable computeConvolution(ImageTable src) {
        int width = src.getWidth();
        int height = src.getHeight();
        ImageTable result = new ImageTable(width,height);

        int sum = 0;

        int turn = 0;
        mainLoop: while (turn < 20) {
            for (int y = 1; y < height - SIZE; ++y) {
                for (int x = 1; x < width - SIZE; ++x) {

                    // get pixel matrix
                    for (int i = 0; i < SIZE; ++i) {
                        for (int j = 0; j < SIZE; ++j) {
                            if (src.getImage()[x + (i - 1) + (y + (j - 1)) * width] == Color.BLACK)
                                sum += 1;
                        }
                    }

                    if (sum <= 1) {
                        result.getImage()[x + y * width] = Color.WHITE;
                    } else {
                        if (result.getImage()[x + y * width] == Color.WHITE
                                && result.getImage()[x + 1 + y * width] == Color.BLACK
                                && result.getImage()[x - 1 + y * width] == Color.BLACK
                                && result.getImage()[x + (y + 1) * width] == Color.BLACK
                                && result.getImage()[x + (y - 1) * width] == Color.BLACK
                                && result.getImage()[x - 1 + (y - 1) * width] == Color.BLACK
                                && result.getImage()[x + 1 + (y + 1) * width] == Color.BLACK
                                && result.getImage()[x - 1 + (y + 1) * width] == Color.BLACK
                                && result.getImage()[x + 1 + (y - 1) * width] == Color.BLACK) {
                            break mainLoop;
                        }
                        result.getImage()[x + y * src.getWidth()] = Color.BLACK;
                    }
                    sum = 0;
                }
            }
            turn++;
            src = new ImageTable(result);

        }
        // final image
        return src;
    }
}
