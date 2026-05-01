package BackLigneDroite;

import java.util.List;

public class RequeteDessin {
    private List<Point> points;
    private OptionsAnalyse options;
    private String typeModele; // "ligne_horizontale" ou "arc"

    public List<Point> getPoints() { return points; }
    public void setPoints(List<Point> points) { this.points = points; }

    public OptionsAnalyse getOptions() { return options; }
    public void setOptions(OptionsAnalyse options) { this.options = options; }

    public String getTypeModele() { return typeModele; }
    public void setTypeModele(String typeModele) { this.typeModele = typeModele; }
}