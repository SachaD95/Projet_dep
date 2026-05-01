package BackLigneDroite;

import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class ScoreController {

    private final ModeleController modeleController = new ModeleController();
    private final PathRotationOptimizer lineOptimizer = new PathRotationOptimizer();


    private static final int NB_POINTS_MODELE = 100;
    private static final double SEUIL_FRECHET_LIGNE = 50.0;
    private static final double SEUIL_FRECHET_ELLIPSE = 60.0; // À ajuster selon tes tests

    @PostMapping("/calculer-score")
    public ReponseScore calculerLeScore(@RequestBody RequeteDessin requete) {
        String type = requete.getTypeModele();


        return calculerScoreLigne(requete);
    }

    /**
     * Logique originale pour la ligne droite
     */
    private ReponseScore calculerScoreLigne(RequeteDessin requete) {
        List<Point> pointsJoueur = requete.getPoints();
        List<Point> pointsModele = modeleController.getModele(requete.getTypeModele()).getPoints();

        PathRotationOptimizer.RotationResult res = lineOptimizer.findOptimalRotation(pointsJoueur, pointsModele);

        double score = Math.max(0, 100 - (res.getBestFrechetDistance() / SEUIL_FRECHET_LIGNE) * 100);

        ReponseScore reponse = new ReponseScore();
        reponse.setScore(Math.round(score * 10.0) / 10.0);

        if (requete.getOptions().getAngle()) {
            reponse.setCommAngle("Angle : " + Math.round(res.getNormalizedAngle() * 10.0) / 10.0 + "°");
        }
        return reponse;
    }


}