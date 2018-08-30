package hough;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.opencv.core.Point;

public class Lines {

    static class Line {
        double a,b;

        Line(Point p1, Point p2) {
            double deltaX = p2.x - p1.x;
            if(deltaX == 0.0) {
                deltaX = 1.0;//in pixel
            }
            this.a = (p2.y - p1.y) / deltaX;
            this.b = p1.y - a * p1.x;
        }
    }

    public List<Point> intersections;

    public Lines(List<HoughLine> listHoughLine, int width, int height) {
        List<Line> lines = new ArrayList<>();
        for(HoughLine hf : listHoughLine) {
            lines.add(new Line(
                    new Point(hf.getX1(), hf.getY1()),
                    new Point(hf.getX2(), hf.getY2())
            ));
        }

        this.intersections = findIntersections(lines);
        Iterator<Point> it = this.intersections.iterator();
        while(it.hasNext()) {
            Point point = it.next();
            if(!(point.x >= 0 && point.x <= width && point.y >= 0 && point.y <= height)) {
                it.remove();
            }
        }
    }

    public static Point intersect(Line line1, Line line2) {
        double x = (line2.b - line1.b) / (line1.a - line2.a);
        double y = line1.a * x + line1.b;

        return new Point((int) x, (int) y);
    }

    static List<Point> findIntersections(List<Line> lines) {
        List<Point> intersections = new ArrayList<>();
        for(int i=0; i < lines.size(); i++) {
            Line currentLine = lines.get(i);
            for(int j=i+1; j < lines.size(); j++){
                Line comparedLine = lines.get(j);
                if(currentLine.a != comparedLine.a){
                    intersections.add(intersect(currentLine, comparedLine));
                }
            }
        }
        return intersections;
    }
}