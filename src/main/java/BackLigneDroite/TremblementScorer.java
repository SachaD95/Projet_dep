package BackLigneDroite;

import java.util.List;
import java.text.DecimalFormat;

public class TremblementScorer {

    private static final DecimalFormat df = new DecimalFormat("0.00");

    // windowSize = nombre de voisins de chaque côté (ex: 5 → fenêtre de 11 points)
    // Plus windowSize est petit, plus on capture les micro-tremblements
    // Trop petit (< 3) → instable. Trop grand → on reintègre la courbure
    private static final int WINDOW_SIZE = 8;

    public String calculerScoreTremblement(List<Point> points) {
        if (points == null || points.size() < WINDOW_SIZE * 2 + 1) return "0.00";

        int n = points.size();
        double sommeCarres = 0;
        int count = 0;

        for (int i = WINDOW_SIZE; i < n - WINDOW_SIZE; i++) {
            // Fenêtre locale autour du point i
            List<Point> fenetre = points.subList(i - WINDOW_SIZE, i + WINDOW_SIZE + 1);

            // Droite idéale locale par régression sur la fenêtre
            double[] droite = regressionLocale(fenetre);
            if (droite == null) continue;

            // Distance perpendiculaire du point central à cette droite locale
            Point centre = points.get(i);
            double residu = distancePerpendiculaire(centre, droite);

            sommeCarres += residu * residu;
            count++;
        }

        if (count == 0) return "0.00";

        // Écart-type des résidus locaux
        double ecartType = Math.sqrt(sommeCarres / count);
        return df.format(ecartType);
    }

    /**
     * Régression linéaire sur une liste de points.
     * Retourne [moyX, moyY, dx, dy] : un point de la droite + vecteur directeur normalisé.
     */
    private double[] regressionLocale(List<Point> pts) {
        int n = pts.size();
        double sumX = 0, sumY = 0, sumXX = 0, sumXY = 0, sumYY = 0;

        for (Point p : pts) {
            sumX  += p.getX();
            sumY  += p.getY();
            sumXX += p.getX() * p.getX();
            sumXY += p.getX() * p.getY();
            sumYY += p.getY() * p.getY();
        }

        double moyX = sumX / n;
        double moyY = sumY / n;
        double varX = sumXX / n - moyX * moyX;
        double varY = sumYY / n - moyY * moyY;

        double dx, dy;
        if (varX >= varY) {
            double a = (sumXY / n - moyX * moyY) / (varX < 1e-9 ? 1e-9 : varX);
            double norm = Math.sqrt(1 + a * a);
            dx = 1.0 / norm;
            dy = a / norm;
        } else {
            double a = (sumXY / n - moyX * moyY) / (varY < 1e-9 ? 1e-9 : varY);
            double norm = Math.sqrt(1 + a * a);
            dy = 1.0 / norm;
            dx = a / norm;
        }

        return new double[]{moyX, moyY, dx, dy};
    }

    /**
     * Distance perpendiculaire d'un point P à la droite définie par [moyX, moyY, dx, dy].
     * Formule : |(P - origine) × direction|
     */
    private double distancePerpendiculaire(Point p, double[] droite) {
        double moyX = droite[0], moyY = droite[1];
        double dx   = droite[2], dy   = droite[3];
        // Produit vectoriel 2D = composante scalaire du vecteur perpendiculaire
        return Math.abs((p.getX() - moyX) * dy - (p.getY() - moyY) * dx);
    }
}