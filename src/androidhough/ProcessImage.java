package androidhough;

import java.util.ArrayList;

public class ProcessImage {
    private ProcessImage(){}

    private static int maxX, maxY, minY, minX;

    // on recupere le format ( delimite par une boite englobante ) on la rescale centre et voila
    static ImageTable formatageIm(ImageTable imageTable) {
        System.out.println("ProcessImage" + " fomatage ...");
        boiteEnglobante(imageTable);

        imageTable = decoupage(imageTable);
        Bitmap formatedImage = Bitmap.createBitmap(imageTable.getImage(),imageTable.getWidth(),
                imageTable.getHeight(), Bitmap.Config.ARGB_8888);
        formatedImage = BitmapUtils.resizeKeepRatio(formatedImage,300,300);

        int w = formatedImage.getWidth();
        int h = formatedImage.getHeight();
        ImageTable formatedImageTable = new ImageTable(w,h);
        formatedImage.getPixels(formatedImageTable.getImage(),0,w,0,0,w,h);

        ArrayList<Integer> list = new ArrayList<>();

        for (int i = 0; i < w; i++) {
            list.add(Color.BLACK);
        }

        for (int i = 0; i < formatedImageTable.getImage().length; i++) {
            list.add(formatedImageTable.getImage()[i]);
        }

        for (int i = 0; i < h + 1; i++) {
            list.add( i*w + i, Color.BLACK);
        }

        for (int i = 0; i < h + 1; i++) {
            list.add( i*(w + 1) + w + 1 + i, Color.BLACK);
        }

        for (int i = 0; i < w + 2; i++) {
            list.add( (h + 1)*(w + 2), Color.BLACK);
        }

        int [] res = new int[(w + 2)*(h + 2)];
        int x = 0;
        for (Integer i : list) {
            res[x++] = i;
        }

        formatedImageTable.setImage(res);
        formatedImageTable.setWidth(formatedImageTable.getWidth()+2);
        formatedImageTable.setHeight(formatedImageTable.getHeight()+2);
        return formatedImageTable;
    }

    private static ImageTable decoupage(ImageTable imageTable) {
        System.out.println("ProcessImage" + "  decoupage ...");
        ImageTable imchar = new ImageTable(maxX-minX+1,maxY-minY+1);
        for (int i = 0; i < imchar.getWidth(); i++) {
            for (int j = 0; j < imchar.getHeight(); j++) {
                imchar.getImage()[i + j * imchar.getWidth()] = imageTable.getImage()[i + minX + (j + minY) * imageTable.getWidth()];
            }
        }
        return imchar;
    }

    private static void boiteEnglobante(ImageTable imageTable) {
        System.out.println("ProcessImage" + "  englobage ...");
        minX = imageTable.getWidth()/2;
        minY = imageTable.getHeight()/2;
        maxX = imageTable.getWidth()/2;
        maxY = imageTable.getHeight()/2;
        for (int j = 0; j < imageTable.getHeight(); j++) {
            for (int i = 0; i < imageTable.getWidth(); i++) {
                if (imageTable.getImage()[i+j*imageTable.getWidth()] == Color.WHITE) {
                    if (maxX < i) {
                        maxX = i;
                    }
                    if (maxY < j) {
                        maxY = j;
                    }
                    if (minX > i) {
                        minX = i;
                    }
                    if (minY > j) {
                        minY = j;
                    }
                }
            }
        }
    }


    static ImageTable erosion(ImageTable imageTable) {
        System.out.println("ProcessImage" + " erosion ...");
        return ConvolveMatrix.computeConvolution(imageTable);
    }



    static ImageTable bAndW(ImageTable imageTable) {
        System.out.println("ProcessImage" + " seuillage ...");
        for (int i = 0; i < imageTable.getImage().length; i++) {
            if (imageTable.getImage()[i] < -1) {
                imageTable.getImage()[i] = Color.WHITE;
            } else {
                imageTable.getImage()[i] = Color.BLACK;
            }
        }
        return imageTable;
    }


}

