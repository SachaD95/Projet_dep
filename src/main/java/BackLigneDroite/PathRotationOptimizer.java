package BackLigneDroite;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PathRotationOptimizer {

    private static final double ROTATION_STEP = 0.25;

    
    public static class RotationResult {
        private final double bestAngle;
        private final double bestFrechetDistance;
        private final double normalizedAngle;
        private final double scaleFactor;
        private final List<Point> rotatedPoints;

        public RotationResult(double bestAngle, double bestFrechetDistance,
                              double normalizedAngle, double scaleFactor,
                              List<Point> rotatedPoints) {
            this.bestAngle = bestAngle;
            this.bestFrechetDistance = bestFrechetDistance;
            this.normalizedAngle = normalizedAngle;
            this.scaleFactor = scaleFactor;
            this.rotatedPoints = rotatedPoints;
        }

        public double getBestAngle()       { return bestAngle; }
        public double getBestFrechetDistance() { return bestFrechetDistance; }
        public double getNormalizedAngle() { return normalizedAngle; }
        public double getScaleFactor()     { return scaleFactor; }
        public List<Point> getRotatedPoints() { return rotatedPoints; }
    }

    // État interne pendant la recherche du meilleur angle
    private static class OptimizationState {
        double bestAngle = 0;
        double bestFrechetDistance = Double.MAX_VALUE;
        List<Point> bestRotatedPoints = new ArrayList<>();
    }

    // -------------------------------------------------------
    // Méthode principale
    // -------------------------------------------------------
    public RotationResult findOptimalRotation(List<Point> userPoints, List<Point> modelPoints) {
        if (userPoints.isEmpty() || modelPoints.isEmpty()) {
            return new RotationResult(0, Double.MAX_VALUE, 0, 1, new ArrayList<>());
        }

        // 1. Facteur d'échelle basé sur la distance début-fin
        double userLength = euclideanDistance(
                userPoints.get(0), userPoints.get(userPoints.size() - 1));
        double modelLength = euclideanDistance(
                modelPoints.get(0), modelPoints.get(modelPoints.size() - 1));
        double scaleFactor = (userLength > 1e-6) ? (modelLength / userLength) : 1.0;

        // 2. Mise à l'échelle du tracé utilisateur
        List<Point> scaledUser = scalePoints(userPoints, scaleFactor);

        // 3. Centrage des deux tracés à l'origine (0, 0)
        Point userCenter  = getCenter(scaledUser);
        Point modelCenter = getCenter(modelPoints);

        List<Point> centeredUser  = translatePoints(scaledUser,    userCenter,  true);
        List<Point> centeredModel = translatePoints(modelPoints,   modelCenter, true);

        Point rotationCenter = makePoint(0, 0);

        // 4. Version inversée du tracé utilisateur (au cas où l'utilisateur dessine à l'envers)
        List<Point> reversedUser = new ArrayList<>(centeredUser);
        Collections.reverse(reversedUser);

        OptimizationState state = new OptimizationState();

        // 5. Tester toutes les rotations dans les deux orientations
        System.out.println("\n--- Test orientation ORIGINALE ---");
        testRotations(centeredUser, centeredModel, rotationCenter, state, false);

        System.out.println("\n--- Test orientation INVERSÉE ---");
        testRotations(reversedUser, centeredModel, rotationCenter, state, true);

        // 6. Re-translater le meilleur résultat au centre du modèle
        List<Point> finalPoints = translatePoints(state.bestRotatedPoints, modelCenter, false);

        // 7. Normalisation de l'angle entre -180 et 180
        double normalizedAngle = state.bestAngle > 180
                ? state.bestAngle - 360
                : state.bestAngle;

        System.out.println("Meilleur angle : " + state.bestAngle + "°");
        System.out.println("Meilleure distance Frechet : " + state.bestFrechetDistance);
        System.out.println("Angle normalisé : " + normalizedAngle + "°");

        return new RotationResult(
                normalizedAngle,
                state.bestFrechetDistance,
                Math.abs(normalizedAngle),
                scaleFactor,
                finalPoints
        );
    }

    // -------------------------------------------------------
    // Teste toutes les rotations de 0° à 360° avec un pas de ROTATION_STEP
    // -------------------------------------------------------
    private void testRotations(List<Point> pointsToRotate, List<Point> modelPoints,
                               Point center, OptimizationState state, boolean isReversed) {

        for (double angle = 0; angle < 360; angle += ROTATION_STEP) {
            List<Point> rotated = rotatePoints(pointsToRotate, center, angle);

            double frechetDistance = FrechetDistanceCalculator.calculateDiscreteFrechet(rotated, modelPoints);

            if (angle % 45 == 0) {
                String orientation = isReversed ? "INVERSÉE" : "ORIGINALE";
                System.out.println("Angle " + angle + "° (" + orientation + ") -> Frechet: " + frechetDistance);
            }

            if (frechetDistance < state.bestFrechetDistance) {
                state.bestFrechetDistance = frechetDistance;
                state.bestAngle = angle;
                state.bestRotatedPoints = rotated;
            }
        }
    }

    // -------------------------------------------------------
    // Géométrie
    // -------------------------------------------------------

    private List<Point> scalePoints(List<Point> points, double scaleFactor) {
        List<Point> scaled = new ArrayList<>(points.size());
        for (Point p : points) {
            scaled.add(makePoint(p.getX() * scaleFactor, p.getY() * scaleFactor));
        }
        return scaled;
    }

    private List<Point> rotatePoints(List<Point> points, Point center, double angleDegrees) {
        double rad = Math.toRadians(angleDegrees);
        double cos = Math.cos(rad);
        double sin = Math.sin(rad);

        List<Point> rotated = new ArrayList<>(points.size());
        for (Point p : points) {
            double dx = p.getX() - center.getX();
            double dy = p.getY() - center.getY();
            rotated.add(makePoint(
                    dx * cos - dy * sin + center.getX(),
                    dx * sin + dy * cos + center.getY()
            ));
        }
        return rotated;
    }

    /**
     * Translate tous les points en ajoutant ou soustrayant le centre.
     * subtract=true  → centrage à l'origine
     * subtract=false → re-translation vers le centre du modèle
     */
    private List<Point> translatePoints(List<Point> points, Point center, boolean subtract) {
        double sign = subtract ? -1.0 : 1.0;
        List<Point> translated = new ArrayList<>(points.size());
        for (Point p : points) {
            translated.add(makePoint(
                    p.getX() + sign * center.getX(),
                    p.getY() + sign * center.getY()
            ));
        }
        return translated;
    }

    private Point getCenter(List<Point> points) {
        double sumX = 0, sumY = 0;
        for (Point p : points) {
            sumX += p.getX();
            sumY += p.getY();
        }
        return makePoint(sumX / points.size(), sumY / points.size());
    }

    private double euclideanDistance(Point p1, Point p2) {
        double dx = p1.getX() - p2.getX();
        double dy = p1.getY() - p2.getY();
        return Math.sqrt(dx * dx + dy * dy);
    }

    // Crée un Point avec les setters (compatible avec ton Point.java actuel)
    private Point makePoint(double x, double y) {
        Point p = new Point();
        p.setX(x);
        p.setY(y);
        return p;
    }
}   