package dungeonforge.generate;

import java.util.ArrayList;
import java.util.List;

/**
 * WEEK 10 (US-8.1) -- the one place that knows which level shapes exist. Modelled on Week 4's
 * ThemeRegistry: adding a shape costs one class and one line here, and GameWorld never names
 * a concrete generator.
 */
public final class GeneratorRegistry {

    private final List<LevelGenerator> shapes = new ArrayList<>();

    public GeneratorRegistry() {
        shapes.add(new CorridorGenerator());
        shapes.add(new CaveGenerator());
        shapes.add(new RingGenerator());
        shapes.add(new SpokeGenerator());
    }

    /** Levels cycle through the registered shapes; depth 1 is always the first. */
    public LevelGenerator forDepth(int depth) {
        return shapes.get((depth - 1) % shapes.size());
    }

    public int size() { return shapes.size(); }

    public List<String> shapeNames() {
        List<String> out = new ArrayList<>();
        for (LevelGenerator g : shapes) out.add(g.shapeName());
        return out;
    }
}
