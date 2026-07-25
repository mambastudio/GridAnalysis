package gridanalysis.algorithm;

import gridanalysis.coordinates.Vec2f;
import gridanalysis.coordinates.Vec2i;
import gridanalysis.gridclasses.BBox;
import gridanalysis.gridclasses.Grid;
import java.util.Objects;

/**
 * Mutable state belonging to one irregular-grid construction run.
 *
 * <p>This is deliberately separate from both the immutable construction
 * configuration and the completed {@link Grid}.</p>
 */
public final class HagridBuildContext {
    private final HagridConfig config;
    private final Grid grid;

    // Temporary virtual-grid data shared by the build and merge passes.
    public Vec2i grid_dims;
    public BBox grid_bbox;
    public Vec2f cell_size;
    public int grid_shift;

    public HagridBuildContext() {
        this(HagridConfig.defaults());
    }

    public HagridBuildContext(HagridConfig config) {
        this.config = Objects.requireNonNull(config, "config");
        this.grid = new Grid();
        this.grid_dims = new Vec2i();
        this.grid_bbox = new BBox();
        this.cell_size = new Vec2f();
    }

    public HagridConfig config() {
        return config;
    }

    public Grid grid() {
        return grid;
    }
}
