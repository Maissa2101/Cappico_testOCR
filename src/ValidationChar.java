import java.util.Vector;

public class ValidationChar {
    double distancePoints(float x1, float y1, float x2, float y2) {
        return Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
    }
    Boolean ValidateMaj(Vector<HoughLine> lineM, Vector<HoughLine> linesR) {
        int lineValidate = 0;
        int epsilon =10;
        Vector<HoughLine> linesRes = linesR;
        System.out.println(" Validation ::");
        for (HoughLine line : lineM) {
            System.out.println(" --------------------");
            int i = 0;
            while (i < linesRes.size()) {
                System.out.println(" dist = (" + linesRes.get(i).x1+ " | "+linesRes.get(i).y1 + ") - (" + linesRes.get(i).x2+ " | "+linesRes.get(i).y2+") ==== ("+line.x1+ " | "+line.y1 + ") - ( " + line.x2+ " | "+line.y2+")"  );
                if (distancePoints(line.x1, linesRes.get(i).y1, line.x1, linesRes.get(i).y1) < epsilon && distancePoints(line.x2, linesRes.get(i).y2, line.x2, linesRes.get(i).y2) < epsilon) {
                    lineValidate++;
                    linesRes.remove(i);
                    break;
                }
                i++;
            }
        }
        double validation = (lineValidate * 100 / lineM.size());
        System.out.println(" Validation a " + lineValidate);
        //if( validation > ) return true;
        return false;
    }
}
