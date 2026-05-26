package BackLigneDroite;

import org.springframework.web.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class ModeleController {

    private static final int NB_POINTS = 100;

    @GetMapping("/modele/{type}")
    public ModeleDessin getModele(
            @PathVariable String type,
            @RequestParam(defaultValue = "300") double taille,
            @RequestParam(defaultValue = "0") double angle,
            @RequestParam(defaultValue = "500") double largeurMax,
            @RequestParam(defaultValue = "400") double hauteurMax
    ) {
        List<Point> points;

        // Calcul du centre dynamique du canvas reçu depuis le front-end
        double cx = largeurMax / 2;
        double cy = hauteurMax / 2;

        // Conversion de l'angle en radians pour les calculs trigonométriques
        double angleRadians = Math.toRadians(angle);

        if ("ellipse".equalsIgnoreCase(type)) {
            // Pour l'ellipse, la taille définit le grand axe (semiA)
            // On applique un ratio de 1.5 (divisé par 3) pour obtenir un petit axe (semiB) harmonieux
            double semiA = taille / 2;
            double semiB = taille / 3;
            points = genererEllipse(cx, cy, semiA, semiB, angleRadians, NB_POINTS);
        } else {
            // Modèle par défaut : Ligne droite centrée et orientée
            // Calcul du décalage (delta X, delta Y) par rapport au centre selon l'angle et la demi-longueur
            double dx = (taille / 2) * Math.cos(angleRadians);
            double dy = (taille / 2) * Math.sin(angleRadians);

            // Extrapolation des points de départ (x1, y1) et de fin (x2, y2)
            double x1 = cx - dx;
            double y1 = cy - dy;
            double x2 = cx + dx;
            double y2 = cy + dy;

            points = genererLigneDroite(x1, y1, x2, y2, NB_POINTS);
        }

        return new ModeleDessin(points, type);
    }

    // Ligne droite entre deux points
    public List<Point> genererLigneDroite(double x1, double y1, double x2, double y2, int n) {
        List<Point> points = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            double t = (double) i / (n - 1);
            Point p = new Point();
            p.setX(x1 + t * (x2 - x1));
            p.setY(y1 + t * (y2 - y1));
            points.add(p);
        }
        return points;
    }

    /**
     * Génère les points d'une ellipse inclinée et centrée sur l'écran
     */
    public List<Point> genererEllipse(double cx, double cy, double semiA, double semiB, double angleRadians, int n) {
        List<Point> points = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            // t parcourt l'intervalle complet [0, 2*PI] pour fermer la figure
            double t = 2 * Math.PI * i / (n - 1);

            // Coordonnées locales théoriques de l'ellipse centrée en (0,0) sans rotation
            double xLocal = semiA * Math.cos(t);
            double yLocal = semiB * Math.sin(t);

            // Application de la matrice de rotation et translation vers le centre (cx, cy)
            double xRot = cx + (xLocal * Math.cos(angleRadians) - yLocal * Math.sin(angleRadians));
            double yRot = cy + (xLocal * Math.sin(angleRadians) + yLocal * Math.cos(angleRadians));

            Point p = new Point();
            p.setX(xRot);
            p.setY(yRot);
            points.add(p);
        }
        return points;
    }
}