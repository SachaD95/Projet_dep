package BackLigneDroite;


import java.util.List;

public class ModeleDessin {
    private List<Point> points;
    private OptionsAnalyse options;
    private String type; // "ligne_droite", extensible plus tard

    public ModeleDessin(List<Point> points, String type) {
        this.points = points;
        this.type = type;
    }

    public List<Point> getPoints() { return points; }
    public void setPoints(List<Point> points) { this.points = points; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public OptionsAnalyse getOptions() { return options; }
    public void setOptions(OptionsAnalyse options) { this.options = options; }
}