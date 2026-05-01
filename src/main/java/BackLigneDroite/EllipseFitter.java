package BackLigneDroite;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class EllipseFitter {

    public static class FitResult {
        private final double centerX, centerY, semiAxisA, semiAxisB, angleDeg, frechetDistance;
        private final boolean valid;

        public FitResult(double cx, double cy, double sa, double sb, double ang, double frechet, boolean v) {
            this.centerX = cx; this.centerY = cy;
            this.semiAxisA = sa; this.semiAxisB = sb;
            this.angleDeg = ang; this.frechetDistance = frechet; this.valid = v;
        }

        public double getCenterX() { return centerX; }
        public double getCenterY() { return centerY; }
        public double getSemiAxisA() { return semiAxisA; }
        public double getSemiAxisB() { return semiAxisB; }
        public double getAngleDeg() { return angleDeg; }
        public double getFrechetDistance() { return frechetDistance; }
        public boolean isValid() { return valid; }

        public static FitResult invalid() {
            return new FitResult(0, 0, 0, 0, 0, Double.MAX_VALUE, false);
        }
    }

    public FitResult fit(List<Point> userPoints, int nbModelPts) {
        // 1. Simplification : on élimine les points trop proches (bruit)
        List<Point> pts = simplifierTrace(userPoints, 5.0);
        if (pts.size() < 6) return FitResult.invalid();

        int n = pts.size();

        // 2. Matrice de Design D
        double[][] D = new double[n][6];
        for (int i = 0; i < n; i++) {
            double x = pts.get(i).getX();
            double y = pts.get(i).getY();
            D[i] = new double[]{ x*x, x*y, y*y, x, y, 1.0 };
        }

        // 3. Matrice de dispersion S
        double[][] S = matMul(transpose(D), D);

        // 4. Inversion de S (pivot partiel pour stabilité verticale/horizontale)
        double[][] Sinv = inv6(S);
        if (Sinv == null) return FitResult.invalid();

        // 5. Matrice de contrainte C (Fitzgibbon)
        double[][] C = new double[6][6];
        C[0][2] = 2; C[2][0] = 2; C[1][1] = -1;
        double[][] M = matMul(Sinv, C);

        // 6. Eigenproblème (Itération de puissance)
        double[][] vecs = new double[6][6];
        double[] vals = eig6Power(M, vecs);

        // 7. Recherche de l'unique vecteur propre solution (ellipse)
        double[] bestCoeffs = null;
        for (int i = 0; i < 6; i++) {
            double a = vecs[i][0], b = vecs[i][1], c = vecs[i][2];
            double disc = 4 * a * c - b * b;
            if (disc > 1e-12) { // C'est une ellipse
                bestCoeffs = vecs[i];
                break;
            }
        }

        if (bestCoeffs == null) return FitResult.invalid();

        // 8. Conversion en paramètres géométriques et ALIGNEMENT
        return conicToGeometricAligned(bestCoeffs, userPoints, nbModelPts);
    }

    private FitResult conicToGeometricAligned(double[] cf, List<Point> userPoints, int nbModelPts) {
        double a=cf[0], b=cf[1], c=cf[2], d=cf[3], e=cf[4], f=cf[5];
        double disc = 4*a*c - b*b;
        if (disc <= 0) return FitResult.invalid();

        // 1. Centre
        double cx = (b*e - 2*c*d) / disc;
        double cy = (b*d - 2*a*e) / disc;

        // 2. Déterminants
        double det3 = a*(c*f - (e/2)*(e/2)) - (b/2)*((b/2)*f - (e/2)*(d/2)) + (d/2)*((b/2)*(e/2) - c*(d/2));
        double det2 = a*c - (b/2)*(b/2);

        // 3. Valeurs propres (Quadratiques)
        double mid = (a + c) / 2.0;
        double delta = Math.sqrt(Math.max(0, Math.pow((a-c)/2.0, 2) + Math.pow(b/2.0, 2)));
        double l1 = mid - delta;
        double l2 = mid + delta;

        // 4. Calcul des axes initiaux
        double sA = Math.sqrt(Math.abs(-det3 / (det2 * l1)));
        double sB = Math.sqrt(Math.abs(-det3 / (det2 * l2)));
        double thetaRad = Math.atan2(l1 - a, b / 2.0);
        double angleDeg = Math.toDegrees(thetaRad);

        // --- SÉCURITÉ 90 DEGRÉS (Recalage sur Grand Axe) ---
        // On veut que semiA soit TOUJOURS le grand axe.
        // Si sB est plus grand, on inverse et on pivote de 90°.
        double finalSemiA, finalSemiB, finalAngle;
        if (sA >= sB) {
            finalSemiA = sA;
            finalSemiB = sB;
            finalAngle = angleDeg;
        } else {
            finalSemiA = sB;
            finalSemiB = sA;
            finalAngle = angleDeg + 90;
        }

        // 5. Alignement sur le premier point utilisateur
        Point startPoint = userPoints.get(0);
        List<Point> modelPts = EllipseModelGenerator.generateAligned(
                cx, cy, finalSemiA, finalSemiB, finalAngle, startPoint, nbModelPts
        );

        // 6. Calcul de Fréchet final
        double frechet = FrechetDistanceCalculator.calculateDiscreteFrechet(userPoints, modelPts);

        return new FitResult(cx, cy, finalSemiA, finalSemiB, finalAngle, frechet, true);
    }

    // --- Fonctions Algébriques (Inchangées, copies de la version stable) ---

    private double[][] inv6(double[][] A) {
        int n = 6;
        double[][] M = new double[n][n], I = new double[n][n];
        for (int i = 0; i < n; i++) {
            M[i] = Arrays.copyOf(A[i], n);
            I[i][i] = 1.0;
        }
        for (int col = 0; col < n; col++) {
            int pivot = col;
            for (int r = col + 1; r < n; r++) if (Math.abs(M[r][col]) > Math.abs(M[pivot][col])) pivot = r;
            double[] tM = M[col]; M[col] = M[pivot]; M[pivot] = tM;
            double[] tI = I[col]; I[col] = I[pivot]; I[pivot] = tI;
            double v = M[col][col];
            if (Math.abs(v) < 1e-18) return null;
            for (int j = 0; j < n; j++) { M[col][j] /= v; I[col][j] /= v; }
            for (int r = 0; r < n; r++) {
                if (r == col) continue;
                double f = M[r][col];
                for (int j = 0; j < n; j++) { M[r][j] -= f * M[col][j]; I[r][j] -= f * I[col][j]; }
            }
        }
        return I;
    }

    private double[] eig6Power(double[][] M, double[][] vecs) {
        int n = 6;
        double[] vals = new double[n];
        double[][] A = new double[n][n];
        for (int i = 0; i < n; i++) A[i] = Arrays.copyOf(M[i], n);
        Random rnd = new Random(42);
        for (int k = 0; k < n; k++) {
            double[] v = new double[n];
            for (int i = 0; i < n; i++) v[i] = rnd.nextDouble() - 0.5;
            for (int it = 0; it < 300; it++) {
                double[] Av = mulMV(A, v);
                double norm = 0; for (double x : Av) norm += x*x; norm = Math.sqrt(norm);
                if (norm < 1e-16) break;
                for (int i = 0; i < n; i++) v[i] = Av[i] / norm;
            }
            double lam = dot(v, mulMV(A, v));
            vals[k] = lam; vecs[k] = v;
            for (int i = 0; i < n; i++) for (int j = 0; j < n; j++) A[i][j] -= lam * v[i] * v[j];
        }
        return vals;
    }

    private List<Point> simplifierTrace(List<Point> points, double distMin) {
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

    private double[][] transpose(double[][] A) {
        double[][] T = new double[A[0].length][A.length];
        for (int i = 0; i < A.length; i++) for (int j = 0; j < A[0].length; j++) T[j][i] = A[i][j];
        return T;
    }

    private double[][] matMul(double[][] A, double[][] B) {
        double[][] C = new double[A.length][B[0].length];
        for (int i = 0; i < A.length; i++)
            for (int j = 0; j < B[0].length; j++)
                for (int k = 0; k < B.length; k++) C[i][j] += A[i][k] * B[k][j];
        return C;
    }

    private double[] mulMV(double[][] M, double[] v) {
        double[] r = new double[M.length];
        for (int i = 0; i < M.length; i++) for (int j = 0; j < v.length; j++) r[i] += M[i][j] * v[j];
        return r;
    }

    private double dot(double[] a, double[] b) {
        double s = 0; for (int i = 0; i < a.length; i++) s += a[i] * b[i]; return s;
    }
}