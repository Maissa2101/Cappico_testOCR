package androidhough;

import java.util.Arrays;

public class ImageTable {
    private int [] image;
    private int width;
    private int height;

    public ImageTable(ImageTable imageTable) {
        this.image = Arrays.copyOf(imageTable.image, imageTable.image.length);
        this.width = imageTable.width;
        this.height = imageTable.height;
    }


    public ImageTable(int width, int height) {
        this.width = width;
        this.height = height;
        this.image = new int[width*height];
    }

    public int[] getImage() {
        return image;
    }

    public void setImage(int[] image) {
        this.image = image;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }
}
