package BackLigneDroite;

import java.util.ArrayList;
import java.util.List;

public class Simplification {


    public static List<Point> simplifierTrace(List<Point> points, double distMin) {
        if (points.size() < 2) return points;
        List<Point> res = new ArrayList<>();
        res.add(points.get(0));
        Point last = points.get(0);
        for (int i = 1; i < points.size(); i++) {
            double d = Math.sqrt(Math.pow(points.get(i).getX()-last.getX(),2)+Math.pow(points.get(i).getY()-last.getY(),2));
            if (d >= distMin) { res.add(points.get(i)); last = points.get(i); }
        }
        return res;
    }

}
