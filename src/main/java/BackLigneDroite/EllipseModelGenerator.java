package BackLigneDroite;

import java.util.ArrayList;
import java.util.List;

/**
 * Génère les points d'une ellipse modèle à partir de ses paramètres géométriques.
 *
 * Appelé par EllipseFitter après le fitting pour :
 *   1. Créer l'ellipse "idéale" correspondant au tracé utilisateur
 *   2. Calculer la distance de Fréchet entre le tracé et cette ellipse
 *   3. Fournir les points à afficher côté front-end (l'ellipse modèle)
 *
 * Analogie avec le projet existant :
 *   EllipseModelGenerator.generate() ↔ ModeleController.genererLigneDroite()
 *   mais ici l'ellipse est calculée dynamiquement depuis le tracé utilisateur,
 *   pas chargée depuis un modèle fixe.
 */
public class EllipseModelGenerator {

    /**
     * Génère {@code nbPoints} points répartis uniformément sur une ellipse.
     *
     * @param cx        Coordonnée X du centre
     * @param cy        Coordonnée Y du centre
     * @param semiA     Grand demi-axe
     * @param semiB     Petit demi-axe
     * @param angleDeg  Angle de rotation de l'ellipse (en degrés, sens trigonométrique)
     * @param nbPoints  Nombre de points à générer (recommandé : 100-200)
     * @return          Liste de points décrivant l'ellipse modèle
     */
    public static List<Point> generate(double cx, double cy,
                                       double semiA, double semiB,
                                       double angleDeg, int nbPoints) {

        List<Point> points = new ArrayList<>(nbPoints);

        double angleRad = Math.toRadians(angleDeg);
        double cosA     = Math.cos(angleRad);
        double sinA     = Math.sin(angleRad);

        for (int i = 0; i < nbPoints; i++) {
            // Paramètre t ∈ [0, 2π[ – on évite de dupliquer le point de départ
            double t = 2 * Math.PI * i / nbPoints;

            // Point sur l'ellipse non-rotée
            double ex = semiA * Math.cos(t);
            double ey = semiB * Math.sin(t);

            // Application de la rotation puis translation vers le centre
            Point p = new Point();
            p.setX(cx + ex * cosA - ey * sinA);
            p.setY(cy + ex * sinA + ey * cosA);
            points.add(p);
        }

        return points;
    }

    /**
     * Variante qui génère les points en démarrant à l'angle paramétrique le
     * plus proche du premier point du tracé utilisateur.
     *
     * Utile pour améliorer le calcul de la distance de Fréchet quand le sens
     * de parcours ou le point de départ de l'utilisateur n'est pas standardisé.
     *
     * @param cx, cy, semiA, semiB, angleDeg  Paramètres géométriques (même signification)
     * @param startPoint    Premier point du tracé utilisateur
     * @param nbPoints      Nombre de points à générer
     * @return              Liste de points de l'ellipse modèle, réordonnée depuis startPoint
     */
    public static List<Point> generateAligned(double cx, double cy,
                                              double semiA, double semiB,
                                              double angleDeg,
                                              Point startPoint,
                                              int nbPoints) {

        // 1. Générer l'ellipse complète avec offset = 0
        List<Point> base = generate(cx, cy, semiA, semiB, angleDeg, nbPoints);

        // 2. Trouver l'index du point le plus proche de startPoint
        int bestIdx = 0;
        double bestDist = Double.MAX_VALUE;
        for (int i = 0; i < base.size(); i++) {
            double dx = base.get(i).getX() - startPoint.getX();
            double dy = base.get(i).getY() - startPoint.getY();
            double d  = Math.sqrt(dx * dx + dy * dy);
            if (d < bestDist) {
                bestDist = d;
                bestIdx  = i;
            }
        }

        // 3. Réordonner la liste pour commencer à bestIdx
        List<Point> aligned = new ArrayList<>(nbPoints);
        for (int i = 0; i < nbPoints; i++) {
            aligned.add(base.get((bestIdx + i) % nbPoints));
        }

        return aligned;
    }
}