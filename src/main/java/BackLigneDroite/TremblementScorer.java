package BackLigneDroite;

import java.util.List;
import java.text.DecimalFormat;

public class TremblementScorer {


    private static final DecimalFormat df = new DecimalFormat("0.00");

    public String calculerScoreTremblement(List<Point> points, double rayonPixels) {
        if (points == null || points.size() < 3) return "0.00";

        double sommeEcarts = 0;
        int count = 0;

        for (int i = 0; i < points.size(); i++) {
            Point reel = points.get(i);

            // On calcule la moyenne des points dans un rayon de X pixels autour du point i
            Point ideal = calculerMoyenneSpatiale(points, i, rayonPixels);

            if (ideal != null) {
                double distance = Math.sqrt(Math.pow(reel.getX() - ideal.getX(), 2) + Math.pow(reel.getY() - ideal.getY(), 2));
                sommeEcarts += distance;
                count++;
            }
        }

        // Score final : écart moyen en pixels, indépendant de la densité de points
        double scoreFinal = (count == 0) ? 0 : (sommeEcarts / count);
        return df.format(scoreFinal);
    }

    private Point calculerMoyenneSpatiale(List<Point> points, int indexCentre, double rayon) {
        double sumX = 0, sumY = 0;
        int pointsTrouves = 0;
        Point centre = points.get(indexCentre);

        // On regarde autour du point dans la liste
        // On limite la recherche aux voisins proches (index) pour la performance
        int scanRange = 20;
        int start = Math.max(0, indexCentre - scanRange);
        int end = Math.min(points.size() - 1, indexCentre + scanRange);

        for (int i = start; i <= end; i++) {
            Point voisin = points.get(i);
            double distPoints = Math.sqrt(Math.pow(centre.getX() - voisin.getX(), 2) + Math.pow(centre.getY() - voisin.getY(), 2));

            if (distPoints <= rayon) {
                sumX += voisin.getX();
                sumY += voisin.getY();
                pointsTrouves++;
            }
        }

        if (pointsTrouves == 0) return null;
        Point pt = new Point();
        pt.setX(sumX/pointsTrouves);
        pt.setY(sumY/pointsTrouves);
        return pt;
    }
}