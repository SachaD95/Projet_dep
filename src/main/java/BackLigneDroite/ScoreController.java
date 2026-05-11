package BackLigneDroite;

import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class ScoreController {

    private final ModeleController modeleController = new ModeleController();
    private final PathRotationOptimizer lineOptimizer = new PathRotationOptimizer();
    private final EllipseFitter ellipseFitter = new EllipseFitter();

    private static final int NB_POINTS_MODELE = 100;
    private static final double SEUIL_FRECHET_LIGNE = 50.0;
    private static final double SEUIL_FRECHET_ELLIPSE = 60.0; // À ajuster selon tes tests

    @PostMapping("/calculer-score")
    public ReponseScore calculerLeScore(@RequestBody RequeteDessin requete) {
        String type = requete.getTypeModele();

        if ("ellipse".equalsIgnoreCase(type)) {
            return calculerScoreEllipse(requete);
        } else {
            return calculerScoreLigne(requete);
        }
    }


    /**
     * Logique originale pour la ligne droite
     */
    private ReponseScore calculerScoreLigne(RequeteDessin requete) {
        List<Point> pointsJoueur = requete.getPoints();
        List<Point> Original=pointsJoueur;
        if (pointsJoueur.size() > 1000) {
            List<Point> pointsReduits = new ArrayList<>();
            double step = (double) pointsJoueur.size() / 50;

            for (int i = 0; i < 50; i++) {
                int index = (int) (i * step);
                pointsReduits.add(pointsJoueur.get(index));
            }

            pointsJoueur = pointsReduits;
        }




        pointsJoueur=Simplification.simplifierTrace(pointsJoueur, 5.0);



        List<Point> pointsModele = modeleController.genererLigneDroite(50, 200, 450, 200, pointsJoueur.size());;

        PathRotationOptimizer.RotationResult res = lineOptimizer.findOptimalRotation(pointsJoueur, pointsModele);

        double score = Math.max(0, 100 - (res.getBestFrechetDistance() / SEUIL_FRECHET_LIGNE) * 100);

        ReponseScore reponse = new ReponseScore();
        reponse.setScore(Math.round(score * 10.0) / 10.0);



        if (requete.getOptions().getAngle()) {
            reponse.setCommAngle("Angle : " + Math.round(res.getNormalizedAngle() * 10.0) / 10.0 + "°");
        }

        if (requete.getOptions().getVitesse()){
            String Comv=Vitesse.analyserProfilVitesse(pointsJoueur,8);
            reponse.setCommVitesse("Vitesse:"+Comv);
        }

        if (requete.getOptions().getTremblement()){
            TremblementScorer scorer = new TremblementScorer();
            String ComTremblement=scorer.calculerScoreTremblement(pointsJoueur, 7);


            reponse.setCommTremblement("Tremblement:"+ComTremblement);
        }


        return reponse;
    }

    /**
     * Nouvelle logique pour l'ellipse intégrée
     */
    private ReponseScore calculerScoreEllipse(RequeteDessin requete) {
        List<Point> pointsJoueur = requete.getPoints();



        // 1. Fitting de l'ellipse
        EllipseFitter.FitResult fit = ellipseFitter.fit(pointsJoueur, NB_POINTS_MODELE);

        if (!fit.isValid()) {
            ReponseScore erreur = new ReponseScore();
            erreur.setScore(0);
            erreur.setCommTremblement("Impossible d'identifier une ellipse.");
            return erreur;
        }

        // 2. Calcul du score
        double dist = fit.getFrechetDistance();
        double score = Math.max(0, 100 - (dist / SEUIL_FRECHET_ELLIPSE) * 100);

        // 3. Construction de la réponse enrichie
        ReponseScore reponse = new ReponseScore();
        reponse.setScore(Math.round(score * 10.0) / 10.0);

        // Données géométriques pour le front
        reponse.setEllipseCx(fit.getCenterX());
        reponse.setEllipseCy(fit.getCenterY());
        reponse.setEllipseSemiA(fit.getSemiAxisA());
        reponse.setEllipseSemiB(fit.getSemiAxisB());
        reponse.setEllipseAngle(fit.getAngleDeg());

        // Génération des points du modèle pour le tracé front
        reponse.setEllipseModelPoints(EllipseModelGenerator.generate(
                fit.getCenterX(), fit.getCenterY(),
                fit.getSemiAxisA(), fit.getSemiAxisB(),
                fit.getAngleDeg(),
                NB_POINTS_MODELE
        ));

        // Commentaires
        if (requete.getOptions().getTremblement()) {
            reponse.setCommTremblement("Précision de la forme : " + Math.round(dist * 10.0) / 10.0);
        }

        return reponse;
    }
}