package gridanalysis;

import gridanalysis.algorithm.Expand;
import gridanalysis.algorithm.Build;
import gridanalysis.algorithm.Flatten;
import gridanalysis.algorithm.GridAbstracts;
import gridanalysis.algorithm.Hagrid;
import gridanalysis.algorithm.Merge;
import gridanalysis.coordinates.Vec2f;
import gridanalysis.coordinates.Vec2i;
import gridanalysis.gridclasses.BBox;
import gridanalysis.gridclasses.Cell;
import gridanalysis.gridclasses.Entry;
import gridanalysis.gridclasses.Grid;
import gridanalysis.gridclasses.Tri;
import gridanalysis.utilities.list.IntegerList;
import gridanalysis.utilities.list.ObjectList;
import gridanalysis.jfx.MEngine;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

/**
 * Small deterministic laboratory for inspecting Hagrid cell expansion.
 * Run this class instead of the normal launcher. Click an ownership cell to
 * inspect it, then advance expansion one axis at a time.
 */
public final class ExpansionDebug extends Application {
    private static final double ORIGIN_X = 70;
    private static final double ORIGIN_Y = 55;
    private static final double PLOT_WIDTH = 576;
    private static final double PLOT_HEIGHT = 576;

    private final Canvas canvas = new Canvas(900, 690);
    private final Label status = new Label();
    private final CheckBox partial = new CheckBox("Aggressive partial expansion");
    private final CheckBox buildStage = new CheckBox("Build");
    private final CheckBox mergeStage = new CheckBox("Merge");
    private final CheckBox flattenStage = new CheckBox("Flatten");
    private final CheckBox expandStage = new CheckBox("Expand");
    private final TextField topDensity = new TextField("0.12");
    private final TextField secondDensity = new TextField("2.4");
    private final TextField alpha = new TextField("0.995");
    private final TextField expansionIterations = new TextField("3");
    private final TextField sceneScale = new TextField("600");

    private Grid grid;
    private Tri[] triangles;
    private Cell[] ownership;
    private Expand.Session expansion;
    private int selectedCell = 1;
    private int iteration;
    private String lastStep = "Ownership map";
    private RayTraversal traversal;
    private Vec2f rayOriginFraction = new Vec2f(0.0f, 0.38f);
    private Vec2f rayEndFraction = new Vec2f(1.0f, 0.63f);
    private int draggedRayHandle;
    private boolean rayWasDragged;

    @Override
    public void start(Stage stage) {
        partial.setSelected(true);
        status.setWrapText(true);
        status.setPrefWidth(520);
        for (TextField field : new TextField[] {topDensity, secondDensity, alpha, expansionIterations, sceneScale}) {
            field.setPrefColumnCount(5);
            field.setOnAction(event -> reset());
        }
        buildStage.setSelected(true);
        buildStage.setDisable(true);
        mergeStage.setSelected(true);
        flattenStage.setSelected(true);
        expandStage.setSelected(false);
        Button reset = new Button("Reset construction");
        Button resetRay = new Button("Reset ray");
        Button traversalStep = new Button("Traversal step");

        reset.setOnAction(event -> reset());
        partial.setOnAction(event -> reset());
        mergeStage.setOnAction(event -> {
            if (!mergeStage.isSelected()) {
                flattenStage.setSelected(false);
                expandStage.setSelected(false);
            }
            reset();
        });
        flattenStage.setOnAction(event -> {
            if (flattenStage.isSelected()) mergeStage.setSelected(true);
            else expandStage.setSelected(false);
            reset();
        });
        expandStage.setOnAction(event -> {
            if (expandStage.isSelected()) {
                mergeStage.setSelected(true);
                flattenStage.setSelected(true);
            }
            reset();
        });
        resetRay.setOnAction(event -> resetRay());
        traversalStep.setOnAction(event -> stepRay());
        canvas.setOnMousePressed(event -> {
            Vec2f rayOrigin = traversal.origin;
            Vec2f endpoint = traversal.endPoint();
            double originDx = event.getX() - sxWorld(rayOrigin.x);
            double originDy = event.getY() - syWorld(rayOrigin.y);
            double endDx = event.getX() - sxWorld(endpoint.x);
            double endDy = event.getY() - syWorld(endpoint.y);
            if (originDx * originDx + originDy * originDy <= 18 * 18) draggedRayHandle = 1;
            else if (endDx * endDx + endDy * endDy <= 18 * 18) draggedRayHandle = 2;
            else draggedRayHandle = 0;
            rayWasDragged = false;
        });
        canvas.setOnMouseDragged(event -> {
            if (draggedRayHandle == 0) return;
            rayWasDragged = true;
            Vec2f fraction = new Vec2f(
                    clamp01((float) ((event.getX() - ORIGIN_X) / PLOT_WIDTH)),
                    clamp01(1.0f - (float) ((event.getY() - ORIGIN_Y) / PLOT_HEIGHT)));
            if (draggedRayHandle == 1) rayOriginFraction = snapToPerimeter(fraction);
            else rayEndFraction = fraction;
            resetRay();
        });
        canvas.setOnMouseReleased(event -> draggedRayHandle = 0);
        canvas.setOnMouseClicked(event -> {
            if (!rayWasDragged) selectCell(event.getX(), event.getY());
            rayWasDragged = false;
        });

        HBox stages = new HBox(12, new Label("Construction:"), buildStage,
                mergeStage, flattenStage, expandStage, partial);
        HBox parameters = new HBox(8,
                new Label("top_density"), topDensity,
                new Label("snd_density"), secondDensity,
                new Label("alpha"), alpha,
                new Label("exp_iters"), expansionIterations,
                new Label("scene_scale"), sceneScale,
                new Label("Press Enter or Reset construction to apply"));
        HBox controls = new HBox(10, reset, resetRay, traversalStep, status);
        VBox toolbar = new VBox(8, stages, parameters, controls);
        toolbar.setPadding(new Insets(10));
        BorderPane root = new BorderPane(canvas);
        root.setTop(toolbar);
        root.setStyle("-fx-background-color: #f8fafc;");

        reset();
        stage.setTitle("Hagrid Expansion Laboratory");
        stage.setScene(new Scene(root));
        stage.show();
    }

    private void reset() {
        grid = createGrid();
        ownership = copyCells(grid);
        expansion = new Expand.Session(grid, triangles, partial.isSelected());
        iteration = 0;
        if (expandStage.isSelected()) {
            for (int i = 0; i < readInt(expansionIterations, 3, 0); i++) {
                expansion.stepAxis(0);
                expansion.stepAxis(1);
            }
            iteration = 3;
        }
        selectedCell = Math.min(1, grid.num_cells - 1);
        traversal = new RayTraversal();
        lastStep = stageName();
        draw();
    }

    private void resetRay() {
        traversal = new RayTraversal();
        lastStep = "Ray located at grid entry";
        draw();
    }

    private void stepRay() {
        traversal.step();
        lastStep = traversal.message;
        draw();
    }

    private void selectCell(double screenX, double screenY) {
        Vec2i dimensions = grid.grid_dims();
        int x = (int) ((screenX - ORIGIN_X) * dimensions.x / PLOT_WIDTH);
        int y = dimensions.y - 1
                - (int) ((screenY - ORIGIN_Y) * dimensions.y / PLOT_HEIGHT);
        if (x < 0 || y < 0 || x >= dimensions.x || y >= dimensions.y) {
            return;
        }
        selectedCell = GridAbstracts.lookup_entry(
                grid.entries, grid.shift, grid.dims, new Vec2i(x, y));
        draw();
    }

    private void draw() {
        GraphicsContext g = canvas.getGraphicsContext2D();
        g.setFill(Color.web("#f8fafc"));
        g.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        drawBaseGrid(g);
        drawTriangles(g);
        drawOwnership(g);
        drawExpandedBounds(g);
        drawSampleRay(g);
        drawLegend(g);

        Cell selected = expansion.getCell(selectedCell);
        status.setText(lastStep + " | " + traversal.details() + " | selected " + selectedCell
                + " refs=" + references(selectedCell)
                + " exit=" + selected.min + " -> " + selected.max);
    }

    private void drawBaseGrid(GraphicsContext g) {
        Vec2i dimensions = grid.grid_dims();
        g.setStroke(Color.web("#cbd5e1"));
        g.setLineWidth(1);
        g.setLineDashes();
        for (int x = 0; x <= dimensions.x; x++) {
            double p = ORIGIN_X + x * PLOT_WIDTH / dimensions.x;
            g.strokeLine(p, ORIGIN_Y, p, ORIGIN_Y + PLOT_HEIGHT);
        }
        for (int y = 0; y <= dimensions.y; y++) {
            double p = ORIGIN_Y + y * PLOT_HEIGHT / dimensions.y;
            g.strokeLine(ORIGIN_X, p, ORIGIN_X + PLOT_WIDTH, p);
        }
    }

    private void drawOwnership(GraphicsContext g) {
        for (int id = 0; id < ownership.length; id++) {
            Cell cell = ownership[id];
            Rect r = rect(cell);
            g.setFill(id == selectedCell
                    ? Color.rgb(37, 99, 235, 0.18)
                    : Color.rgb(148, 163, 184, 0.07));
            g.fillRect(r.x, r.y, r.w, r.h);
            g.setStroke(id == selectedCell ? Color.web("#2563eb") : Color.web("#64748b"));
            g.setLineWidth(id == selectedCell ? 3 : 1.5);
            g.setLineDashes();
            g.strokeRect(r.x, r.y, r.w, r.h);
            g.setFill(Color.web("#334155"));
            g.fillText("cell " + id + " " + references(id), r.x + 5, r.y + 15);
        }
    }

    private void drawExpandedBounds(GraphicsContext g) {
        for (int id = 0; id < expansion.size(); id++) {
            Cell cell = expansion.getCell(id);
            Rect r = rect(cell);
            boolean selected = id == selectedCell;
            g.setFill(selected
                    ? Color.rgb(14, 165, 233, 0.20)
                    : Color.rgb(249, 115, 22, 0.045));
            g.fillRect(r.x, r.y, r.w, r.h);
            g.setStroke(selected ? Color.web("#0284c7") : Color.rgb(234, 88, 12, 0.34));
            g.setLineWidth(selected ? 3 : 1);
            g.setLineDashes(8, 6);
            g.strokeRect(r.x, r.y, r.w, r.h);
        }
        g.setLineDashes();
    }

    private void drawTriangles(GraphicsContext g) {
        Color[] colors = {Color.web("#22c55e"), Color.web("#a855f7"), Color.web("#ef4444")};
        for (int i = 0; i < triangles.length; i++) {
            Tri tri = triangles[i];
            double[] xs = {sxWorld(tri.p0().x), sxWorld(tri.p1().x), sxWorld(tri.p2().x)};
            double[] ys = {syWorld(tri.p0().y), syWorld(tri.p1().y), syWorld(tri.p2().y)};
            g.setFill(Color.color(colors[i].getRed(), colors[i].getGreen(), colors[i].getBlue(), 0.28));
            g.setStroke(traversal.testedRefs.contains(i) ? Color.web("#f59e0b") : colors[i]);
            g.setLineWidth(traversal.testedRefs.contains(i) ? 5 : 2);
            g.fillPolygon(xs, ys, 3);
            g.strokePolygon(xs, ys, 3);
            g.setFill(colors[i]);
            g.fillText("T" + i, xs[0] + 4, ys[0] - 4);
        }
    }

    private void drawSampleRay(GraphicsContext g) {
        Vec2f start = traversal.origin;
        Vec2f end = traversal.origin.add(traversal.direction.mul((float) traversal.maxT));
        g.setStroke(Color.web("#dc2626"));
        g.setLineWidth(2.5);
        g.setLineDashes(10, 5);
        g.strokeLine(sxWorld(start.x), syWorld(start.y), sxWorld(end.x), syWorld(end.y));
        g.setLineDashes();
        Vec2f current = traversal.point();
        g.setStroke(Color.web("#991b1b"));
        g.setLineWidth(4);
        g.strokeLine(sxWorld(start.x), syWorld(start.y), sxWorld(current.x), syWorld(current.y));
        g.setFill(Color.web("#dc2626"));
        for (Vec2f point : traversal.exits) {
            g.fillOval(sxWorld(point.x) - 5, syWorld(point.y) - 5, 10, 10);
        }
        g.setFill(Color.web("#2563eb"));
        g.fillOval(sxWorld(start.x) - 8, syWorld(start.y) - 8, 16, 16);
        g.setStroke(Color.WHITE);
        g.setLineWidth(2);
        g.strokeOval(sxWorld(start.x) - 8, syWorld(start.y) - 8, 16, 16);
        g.setFill(traversal.hit ? Color.web("#16a34a") : Color.web("#dc2626"));
        g.fillOval(sxWorld(current.x) - 7, syWorld(current.y) - 7, 14, 14);
        g.setFill(Color.web("#dc2626"));
        g.fillRect(sxWorld(end.x) - 7, syWorld(end.y) - 7, 14, 14);
        g.setFill(Color.web("#dc2626"));
        g.fillText("sample ray", ORIGIN_X + 8, ORIGIN_Y + PLOT_HEIGHT * 0.62);
    }

    private void drawLegend(GraphicsContext g) {
        double y = ORIGIN_Y + PLOT_HEIGHT + 28;
        g.setFill(Color.web("#334155"));
        g.fillText("Solid gray/blue: immutable voxel ownership", ORIGIN_X, y);
        g.setFill(Color.web("#ea580c"));
        g.fillText("Dashed orange/blue: expanded traversal exit bounds", ORIGIN_X + 260, y);
        g.setFill(Color.web("#64748b"));
        g.fillText("Drag the blue circle around the grid boundary; drag the red square to aim.", ORIGIN_X, y + 22);
    }

    private String references(int cellId) {
        Cell cell = ownership[cellId];
        StringBuilder out = new StringBuilder("{");
        for (int i = cell.begin; i < cell.end; i++) {
            if (i > cell.begin) out.append(',');
            out.append(grid.ref_ids.get(i));
        }
        return out.append('}').toString();
    }

    private Rect rect(Cell cell) {
        BBox bounds = grid.cellbound(cell);
        return new Rect(
                sxWorld(bounds.min.x), syWorld(bounds.max.y),
                bounds.extents().x * PLOT_WIDTH / grid.bbox.extents().x,
                bounds.extents().y * PLOT_HEIGHT / grid.bbox.extents().y);
    }

    private static Cell[] copyCells(Grid grid) {
        Cell[] cells = new Cell[grid.num_cells];
        for (int i = 0; i < cells.length; i++) cells[i] = grid.cells.get(i).copy();
        return cells;
    }

    private Grid createGrid() {
        float scale = readFloat(sceneScale, 600.0f, 0.001f);
        triangles = new Tri[] {
            triangle(scale, 0.14f, 0.20f, 0.33f, 0.28f, 0.20f, 0.52f),
            triangle(scale, 0.29f, 0.44f, 0.74f, 0.26f, 0.62f, 0.89f),
            triangle(scale, 0.90f, 0.15f, 0.99f, 0.46f, 0.93f, 0.76f)
        };

        Hagrid hagrid = new Hagrid();
        hagrid.top_density = readFloat(topDensity, 0.12f, 0.0001f);
        hagrid.snd_density = readFloat(secondDensity, 2.4f, 0.0001f);
        hagrid.alpha = readFloat(alpha, 0.995f, 0.0f);
        hagrid.exp_iters = readInt(expansionIterations, 3, 0);
        MEngine constructionSink = new MEngine();
        new Build(constructionSink, hagrid).build_grid(triangles, triangles.length);
        if (mergeStage.isSelected()) {
            new Merge(constructionSink, hagrid).merge_grid();
        }
        if (flattenStage.isSelected()) {
            new Flatten(constructionSink, hagrid).flatten_grid();
        }
        return hagrid.getIrregularGrid();
    }

    private static Tri triangle(float scale,
            float x0, float y0, float x1, float y1, float x2, float y2) {
        return new Tri(
                new Vec2f(x0 * scale, y0 * scale),
                new Vec2f(x1 * scale, y1 * scale),
                new Vec2f(x2 * scale, y2 * scale));
    }

    private String stageName() {
        if (expandStage.isSelected()) return "Build + Merge + Flatten + Expand";
        if (flattenStage.isSelected()) return "Build + Merge + Flatten";
        if (mergeStage.isSelected()) return "Build + Merge";
        return "Build";
    }

    private static float readFloat(TextField field, float fallback, float minimum) {
        try {
            float value = Float.parseFloat(field.getText().trim());
            if (Float.isFinite(value) && value >= minimum) return value;
        } catch (NumberFormatException ignored) {
        }
        field.setText(Float.toString(fallback));
        return fallback;
    }

    private static int readInt(TextField field, int fallback, int minimum) {
        try {
            int value = Integer.parseInt(field.getText().trim());
            if (value >= minimum) return value;
        } catch (NumberFormatException ignored) {
        }
        field.setText(Integer.toString(fallback));
        return fallback;
    }

    private static float clamp01(float value) {
        return Math.max(0.001f, Math.min(0.999f, value));
    }

    private static Vec2f snapToPerimeter(Vec2f value) {
        float left = value.x;
        float right = 1.0f - value.x;
        float bottom = value.y;
        float top = 1.0f - value.y;
        float nearest = Math.min(Math.min(left, right), Math.min(bottom, top));
        if (nearest == left) return new Vec2f(0.0f, value.y);
        if (nearest == right) return new Vec2f(1.0f, value.y);
        if (nearest == bottom) return new Vec2f(value.x, 0.0f);
        return new Vec2f(value.x, 1.0f);
    }

    private final class RayTraversal {
        final Vec2f origin;
        final Vec2f direction;
        final double maxT;
        final java.util.ArrayList<Vec2f> exits = new java.util.ArrayList<>();
        double t;
        boolean done;
        boolean hit;
        int traversalIteration;
        int totalWork;
        int currentCell = -1;
        Vec2i voxel;
        double lastExitT = Double.NaN;
        double bestHitT = Double.POSITIVE_INFINITY;
        int bestHitRef = -1;
        final java.util.Set<Integer> testedRefs = new java.util.LinkedHashSet<>();
        String message = "Ray ready";

        RayTraversal() {
            Vec2f ext = grid.bbox.extents();
            origin = new Vec2f(
                    grid.bbox.min.x + ext.x * rayOriginFraction.x,
                    grid.bbox.min.y + ext.y * rayOriginFraction.y);
            Vec2f end = new Vec2f(
                    grid.bbox.min.x + ext.x * rayEndFraction.x,
                    grid.bbox.min.y + ext.y * rayEndFraction.y);
            direction = end.sub(origin);
            maxT = 1.0;
            Vec2f voxelPosition = origin.sub(grid.bbox.min).mul(grid.grid_inv());
            voxel = Vec2i.clamp(new Vec2i(voxelPosition), new Vec2i(), grid.grid_dims().sub(1));
        }

        Vec2f endPoint() {
            return origin.add(direction);
        }

        Vec2f point() {
            return origin.add(direction.mul((float) Math.min(t, maxT)));
        }

        void step() {
            if (done) {
                message = hit ? "Traversal already stopped at a primitive hit"
                        : "Traversal already left the grid";
                return;
            }

            Vec2i dimensions = grid.grid_dims();
            traversalIteration++;
            testedRefs.clear();
            int cellId = GridAbstracts.lookup_entry(grid.entries, grid.shift, grid.dims, voxel);
            currentCell = cellId;
            selectedCell = cellId;
            Cell cell = expandStage.isSelected() ? expansion.getCell(cellId) : ownership[cellId];
            BBox bounds = grid.cellbound(cell);
            double tx = boundaryT(bounds, 0);
            double ty = boundaryT(bounds, 1);
            double exitT = Math.min(tx, ty);
            lastExitT = exitT;

            testPrimitives(cell);
            totalWork += 1 + testedRefs.size();
            if (bestHitT <= exitT) {
                t = bestHitT;
                hit = true;
                done = true;
                message = "Iteration " + traversalIteration + ": HIT T" + bestHitRef
                        + " at t=" + format(bestHitT) + " before exit t=" + format(exitT);
                return;
            }

            Vec2f exitPoint = origin.add(direction.mul((float) exitT));
            exits.add(exitPoint);
            Vec2f exitVoxelPosition = exitPoint.sub(grid.bbox.min).mul(grid.grid_inv());
            Vec2i exitVoxel = new Vec2i(exitVoxelPosition);
            Vec2i cellPoint = new Vec2i(
                    direction.x >= 0 ? cell.max.x : cell.min.x,
                    direction.y >= 0 ? cell.max.y : cell.min.y);
            Vec2i nextVoxel = new Vec2i(
                    nearlyEqual(exitT, tx) ? cellPoint.x + (direction.x >= 0 ? 0 : -1) : exitVoxel.x,
                    nearlyEqual(exitT, ty) ? cellPoint.y + (direction.y >= 0 ? 0 : -1) : exitVoxel.y);
            voxel = new Vec2i(
                    direction.x >= 0 ? Math.max(nextVoxel.x, voxel.x) : Math.min(nextVoxel.x, voxel.x),
                    direction.y >= 0 ? Math.max(nextVoxel.y, voxel.y) : Math.min(nextVoxel.y, voxel.y));
            t = exitT;

            if (t >= maxT || voxel.x < 0 || voxel.y < 0
                    || voxel.x >= dimensions.x || voxel.y >= dimensions.y) {
                done = true;
                message = "Iteration " + traversalIteration + ": exited grid from cell " + cellId;
            } else {
                message = "Iteration " + traversalIteration + ": cell " + cellId
                        + " tested " + testedRefs + ", exit t=" + format(exitT)
                        + ", next voxel=" + voxel;
            }
        }

        private double boundaryT(BBox bounds, int axis) {
            double d = direction.get(axis);
            double boundary = d >= 0 ? bounds.max.get(axis) : bounds.min.get(axis);
            return (boundary - origin.get(axis)) / d;
        }

        private void testPrimitives(Cell cell) {
            for (int i = cell.begin; i < cell.end; i++) {
                int primitiveId = grid.ref_ids.get(i);
                testedRefs.add(primitiveId);
                Tri tri = triangles[primitiveId];
                Vec2f[] vertices = {tri.p0(), tri.p1(), tri.p2()};
                for (int edge = 0; edge < 3; edge++) {
                    double candidate = raySegmentT(vertices[edge], vertices[(edge + 1) % 3]);
                    if (candidate >= 0 && candidate < bestHitT) {
                        bestHitT = candidate;
                        bestHitRef = primitiveId;
                    }
                }
            }
        }

        private double raySegmentT(Vec2f a, Vec2f b) {
            Vec2f edge = b.sub(a);
            Vec2f delta = a.sub(origin);
            double denominator = cross(direction, edge);
            if (Math.abs(denominator) < 1e-9) return Double.POSITIVE_INFINITY;
            double rayT = cross(delta, edge) / denominator;
            double segmentT = cross(delta, direction) / denominator;
            return segmentT >= 0 && segmentT <= 1 ? rayT : Double.POSITIVE_INFINITY;
        }

        private double cross(Vec2f a, Vec2f b) {
            return a.x * b.y - a.y * b.x;
        }

        String details() {
            String hitText = bestHitRef < 0 ? "none"
                    : "T" + bestHitRef + "@" + format(bestHitT);
            return "iteration=" + traversalIteration + " voxel=" + voxel
                    + " cell=" + currentCell + " tested=" + testedRefs
                    + " nearestHit=" + hitText
                    + " exit=" + (Double.isNaN(lastExitT) ? "-" : format(lastExitT))
                    + " work=" + totalWork;
        }

        private boolean nearlyEqual(double a, double b) {
            return Math.abs(a - b) <= 1e-7 * Math.max(1.0, Math.max(Math.abs(a), Math.abs(b)));
        }

        private String format(double value) {
            return String.format("%.4f", value);
        }
    }

    private double sxWorld(double x) {
        return ORIGIN_X + (x - grid.bbox.min.x) * PLOT_WIDTH / grid.bbox.extents().x;
    }

    private double syWorld(double y) {
        return ORIGIN_Y + (grid.bbox.max.y - y) * PLOT_HEIGHT / grid.bbox.extents().y;
    }

    private record Rect(double x, double y, double w, double h) {}

}
