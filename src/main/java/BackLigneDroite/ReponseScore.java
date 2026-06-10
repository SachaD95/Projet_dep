package BackLigneDroite;

import java.util.List;

public class ReponseScore {

    private double score;
    private String commAngle;
    private String commTremblement;
    private String commVitesse;
    private String commTaille; // 🌟 AJOUT : Contient le score et les détails de longueur

    // --- Nouveaux champs pour l'ellipse ---
    private Double ellipseCx;
    private Double ellipseCy;
    private Double ellipseSemiA;
    private Double ellipseSemiB;
    private Double ellipseAngle;
    private List<Point> ellipseModelPoints;

    public ReponseScore() {}

    // --- GETTERS & SETTERS
    public double getScore() { return score; }
    public void setScore(double score) { this.score = score; }

    public String getCommAngle() { return commAngle; }
    public void setCommAngle(String commAngle) { this.commAngle = commAngle; }

    public String getCommTremblement() { return commTremblement; }
    public void setCommTremblement(String commTremblement) { this.commTremblement = commTremblement; }

    public String getCommVitesse() { return commVitesse; }
    public void setCommVitesse(String commVitesse) { this.commVitesse = commVitesse; }

    // 🌟 AJOUT : Getter & Setter pour la Taille
    public String getCommTaille() { return commTaille; }
    public void setCommTaille(String commTaille) { this.commTaille = commTaille; }

    // --- Nouveaux GETTERS & SETTERS ---
    public Double getEllipseCx() { return ellipseCx; }
    public void setEllipseCx(Double ellipseCx) { this.ellipseCx = ellipseCx; }

    public Double getEllipseCy() { return ellipseCy; }
    public void setEllipseCy(Double ellipseCy) { this.ellipseCy = ellipseCy; }

    public Double getEllipseSemiA() { return ellipseSemiA; }
    public void setEllipseSemiA(Double ellipseSemiA) { this.ellipseSemiA = ellipseSemiA; }

    public Double getEllipseSemiB() { return ellipseSemiB; }
    public void setEllipseSemiB(Double ellipseSemiB) { this.ellipseSemiB = ellipseSemiB; }

    public Double getEllipseAngle() { return ellipseAngle; }
    public void setEllipseAngle(Double ellipseAngle) { this.ellipseAngle = ellipseAngle; }

    public List<Point> getEllipseModelPoints() { return ellipseModelPoints; }
    public void setEllipseModelPoints(List<Point> ellipseModelPoints) { this.ellipseModelPoints = ellipseModelPoints; }
}