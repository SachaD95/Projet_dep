package BackLigneDroite;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Vitesse {

    public static String analyserProfilVitesse(List<Point> points, int nbSegmentsVoulus) {
        if (points == null || points.size() < 2) return "Tracé trop court";

        // 1. Calcul des vitesses locales ET des distances
        List<VitessePoint> data = new ArrayList<>();
        double distanceTotale = 0;

        for (int i = 0; i < points.size() - 1; i++) {
            double d = Math.sqrt(Math.pow(points.get(i+1).getX() - points.get(i).getX(), 2) +
                    Math.pow(points.get(i+1).getY() - points.get(i).getY(), 2));
            if (d > 0) { // On ignore les points strictement identiques pour ne pas fausser
                data.add(new VitessePoint(d));
                distanceTotale += d;
            }
        }

        if (distanceTotale == 0) return "Immobile";

        // 2. Calcul de la MÉDIANE SPATIALE (Pondérée)
        // On trie par vitesse
        data.sort(Comparator.comparingDouble(p -> p.vitesse));

        double medianeSpatiale = 0;
        double cumulDistance = 0;
        double seuilCible = distanceTotale / 2.0;

        for (VitessePoint vp : data) {
            cumulDistance += vp.vitesse;
            if (cumulDistance >= seuilCible) {
                medianeSpatiale = vp.vitesse;
                break;
            }
        }

        // 3. Analyse par segment (temporel)
        StringBuilder profil = new StringBuilder();
        int pointsParSegment = points.size() / nbSegmentsVoulus;

        for (int s = 0; s < nbSegmentsVoulus; s++) {
            int debut = s * pointsParSegment;
            int fin = (s == nbSegmentsVoulus - 1) ? points.size() - 1 : (s + 1) * pointsParSegment;

            double distSegment = 0;
            int nbLiens = 0;
            for (int i = debut; i < fin; i++) {
                distSegment += Math.sqrt(Math.pow(points.get(i+1).getX() - points.get(i).getX(), 2) +
                        Math.pow(points.get(i+1).getY() - points.get(i).getY(), 2));
                nbLiens++;
            }

            double vMoyenneSegment = distSegment / nbLiens;
            double ratio = vMoyenneSegment / medianeSpatiale;

            // Seuils ajustés car la médiane spatiale est plus robuste
            if (ratio < 0.90)      profil.append("- ");
            else if (ratio > 1.1) profil.append("+ ");
            else                   profil.append("~ ");
        }

        return profil.toString().trim();
    }

    // Petite classe interne pour stocker la vitesse
    private static class VitessePoint {
        double vitesse;
        VitessePoint(double v) { this.vitesse = v; }
    }
}