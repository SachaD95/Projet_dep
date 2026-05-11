package BackLigneDroite;

import java.util.List;
import java.text.DecimalFormat;

public class TremblementScorer {


    private static final DecimalFormat df = new DecimalFormat("0.00");

    public String calculerScoreTremblement(List<Point> points, int windowSize) {
        if (points == null || points.size() < windowSize) {
            return "0.00";
        }

        double sommeEcarts = 0;
        int count = 0;

        for (int i = windowSize / 2; i < points.size() - windowSize / 2; i++) {
            double sumX = 0, sumY = 0;

            // Calcul du point moyen (lissage)
            for (int j = i - windowSize / 2; j <= i + windowSize / 2; j++) {
                sumX += points.get(j).getX();
                sumY += points.get(j).getY();
            }

            double idealX = sumX / windowSize;
            double idealY = sumY / windowSize;
            double reelX = points.get(i).getX();
            double reelY = points.get(i).getY();

            // Distance entre le point réel et sa version lissée
            double distance = Math.sqrt(Math.pow(reelX - idealX, 2) + Math.pow(reelY - idealY, 2));

            sommeEcarts += distance;
            count++;
        }

        double scoreFinal = (count == 0) ? 0 : (sommeEcarts / count);
        return df.format(scoreFinal); // Retourne le score formaté en String
    }
}