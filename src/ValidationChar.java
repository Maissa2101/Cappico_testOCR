import java.util.Vector;

public class ValidationChar {
    double distance(float valeur1, float valeur2) {
        return (Math.sqrt(Math.pow(valeur2 - valeur1, 2)));
    }

    Boolean ValidateMaj(Vector<HoughLine> lineM, Vector<HoughLine> linesRes) {
        int lineValidate = 0;
        System.out.println(" Validation ::");
        for (HoughLine line : lineM) {
            System.out.println(" --------------------");
            int i = 0;
            while (i < linesRes.size()) {
                System.out.println(" dist = (" + linesRes.get(i).x1+ " | "+linesRes.get(i).y1 + ") - (" + linesRes.get(i).x2+ " | "+linesRes.get(i).y2+") ==== ("+line.x1+ " | "+line.y1 + ") - ( " + line.x2+ " | "+line.y2+")"  );
                if (distance(linesRes.get(i).x1, line.x1) < 30 && distance(linesRes.get(i).y1, line.y1) < 30 && distance(linesRes.get(i).y2, line.y2) < 30 && distance(linesRes.get(i).x2, line.x2) < 30) {
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
