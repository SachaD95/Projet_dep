package BackLigneDroite;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Vitesse {

    public static String analyserProfilVitesse(List<Point> points, int nbSegmentsVoulus) {
        if (points == null || points.size() < nbSegmentsVoulus * 2) return "~|Tracé trop court";

        List<Double> distances = new ArrayList<>();

        for (int i = 0; i < points.size() - 1; i++) {
            double d = calculerDistance(points.get(i), points.get(i + 1));
            if (d > 0.1) distances.add(d);
        }

        if (distances.isEmpty()) return "-|Tracé immobile";

        // Médiane sur les distances inter-points
        List<Double> triees = new ArrayList<>(distances);
        Collections.sort(triees);
        double vitesseMediane = triees.get(triees.size() / 2);
        if (vitesseMediane < 0.5) vitesseMediane = 0.5;

        // Analyse par segment — normalisé par nombre d'INTERVALLES (pas de points)
        StringBuilder profilVisuel = new StringBuilder();
        int pointsParSegment = points.size() / nbSegmentsVoulus;
        int segmentsReguliers = 0;

        for (int s = 0; s < nbSegmentsVoulus; s++) {
            int debut = s * pointsParSegment;
            int fin = (s == nbSegmentsVoulus - 1) ? points.size() - 1 : (s + 1) * pointsParSegment;

            int nbIntervalles = 0;
            double distSegment = 0;

            for (int i = debut; i < fin; i++) {
                double d = calculerDistance(points.get(i), points.get(i + 1));
                if (d > 0.1) {          // même filtre que pour la médiane
                    distSegment += d;
                    nbIntervalles++;
                }
            }

            // Segment vide ou sur-place → on le marque lent
            if (nbIntervalles == 0) {
                profilVisuel.append("- ");
                continue;
            }

            // Vitesse moyenne = distance totale / nombre d'intervalles valides
            double vMoyenneSegment = distSegment / nbIntervalles;
            double ratio = vMoyenneSegment / vitesseMediane;

            if (ratio < 0.9) {
                profilVisuel.append("- ");
            } else if (ratio > 1.1) {
                profilVisuel.append("+ ");
            } else {
                profilVisuel.append("~ ");
                segmentsReguliers++;
            }
        }

        int scoreRegularite = (int) Math.round(((double) segmentsReguliers / nbSegmentsVoulus) * 100);
        return profilVisuel.toString().trim();
    }

    private static double calculerDistance(Point p1, Point p2) {
        return Math.sqrt(Math.pow(p2.getX() - p1.getX(), 2) + Math.pow(p2.getY() - p1.getY(), 2));
    }
}