package BackLigneDroite;

public class OptionsAnalyse {
    private boolean angle;
    private boolean tremblement;
    private boolean vitesse;
    private boolean taille; // 🌟 AJOUT : Pour activer/désactiver le calcul géométrique de taille

    public boolean getAngle() { return angle; }
    public void setAngle(boolean angle) { this.angle = angle;}

    public boolean getTremblement() { return tremblement; }
    public void setTremblement(boolean tremblement) { this.tremblement = tremblement; }

    public boolean getVitesse() { return vitesse; }
    public void setVitesse(boolean vitesse) { this.vitesse = vitesse; }

    // 🌟 AJOUT : Getter & Setter pour la Taille
    public boolean getTaille() { return taille; }
    public void setTaille(boolean taille) { this.taille = taille; }
}