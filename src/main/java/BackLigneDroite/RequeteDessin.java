package BackLigneDroite;

import java.util.List;

public class RequeteDessin {
    private List<Point> points;
    private OptionsAnalyse options;
    private String typeModele;

    // --- NOUVEAUX CHAMPS ---
    private double taille;
    private double angle;
    private double largeurMax;
    private double hauteurMax;

    // --- GETTERS & SETTERS EXISTANTS ---
    public List<Point> getPoints() { return points; }
    public void setPoints(List<Point> points) { this.points = points; }

    public OptionsAnalyse getOptions() { return options; }
    public void setOptions(OptionsAnalyse options) { this.options = options; }

    public String getTypeModele() { return typeModele; }
    public void setTypeModele(String typeModele) { this.typeModele = typeModele; }

    // --- NOUVEAUX GETTERS & SETTERS ---
    public double getTaille() { return taille; }
    public void setTaille(double taille) { this.taille = taille; }

    public double getAngle() { return angle; }
    public void setAngle(double angle) { this.angle = angle; }

    public double getLargeurMax() { return largeurMax; }
    public void setLargeurMax(double largeurMax) { this.largeurMax = largeurMax; }

    public double getHauteurMax() { return hauteurMax; }
    public void setHauteurMax(double hauteurMax) { this.hauteurMax = hauteurMax; }
}