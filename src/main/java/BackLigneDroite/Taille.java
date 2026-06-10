package BackLigneDroite;

import java.util.List;

public class Taille {

    public static String analyserTaille(List<Point> pointsDessin, double longueurModeleAttendue) {
        if (pointsDessin == null || pointsDessin.size() < 2) return "0|Tracé inexistant";

        // 1. Calculer la longueur totale du tracé utilisateur
        double longueurUtilisateur = 0;
        for (int i = 0; i < pointsDessin.size() - 1; i++) {
            longueurUtilisateur += calculerDistance(pointsDessin.get(i), pointsDessin.get(i + 1));
        }

        if (longueurUtilisateur < 5) return "0|Tracé trop court";

        // 2. Comparer avec la longueur attendue du modèle
        // Exemple : Si attendu = 200px et utilisateur = 250px -> écart = 50px (25% d'erreur)
        double écartAbsolu = Math.abs(longueurUtilisateur - longueurModeleAttendue);
        double ratioErreur = écartAbsolu / longueurModeleAttendue;

        // On convertit en score sur 100 (0% si l'erreur dépasse 100% de la taille du modèle)
        int scoreTaille = (int) Math.max(0, Math.round((1.0 - ratioErreur) * 100));

        // Format de retour clair pour le JS
        return scoreTaille + "|Attendu : " + (int)longueurModeleAttendue + "px (Tu as fait : " + (int)longueurUtilisateur + "px)";
    }

    private static double calculerDistance(Point p1, Point p2) {
        return Math.sqrt(Math.pow(p2.getX() - p1.getX(), 2) + Math.pow(p2.getY() - p1.getY(), 2));
    }
}