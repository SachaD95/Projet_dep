package BackLigneDroite;

import org.springframework.web.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class ModeleController {

    private static final int NB_POINTS = 100;

    @GetMapping("/modele/{type}")
    public ModeleDessin getModele(@PathVariable String type) {
        List<Point> points = switch (type) {
            case "ligne_horizontale" -> genererLigneDroite(50, 200, 450, 200, NB_POINTS);
            default                  -> genererLigneDroite(50, 200, 450, 200, NB_POINTS);
        };
        return new ModeleDessin(points, type);
    }

    // Ligne droite entre deux points
    private List<Point> genererLigneDroite(double x1, double y1, double x2, double y2, int n) {
        List<Point> points = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            double t = (double) i / (n - 1);
            Point p = new Point();
            p.setX(x1 + t * (x2 - x1));
            p.setY(y1 + t * (y2 - y1));
            points.add(p);
        }
        return points;
    }
}