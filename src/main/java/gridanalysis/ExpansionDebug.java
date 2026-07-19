package gridanalysis;

import gridanalysis.algorithm.Expand;
import gridanalysis.algorithm.Build;
import gridanalysis.algorithm.Flatten;
import gridanalysis.algorithm.GridAbstracts;
import gridanalysis.algorithm.Hagrid;
import gridanalysis.algorithm.Merge;
import gridanalysis.algorithm.Traversal;
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
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
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
    private static final double PLOT_WIDTH = 576;
    private static final double PLOT_HEIGHT = 576;
    private static final double ORIGIN_X = (900 - PLOT_WIDTH) / 2;
    private static final double ORIGIN_Y = 55;

    private final Canvas canvas = new Canvas(900, 690);
    private final TextArea status = new TextArea();
    private final CheckBox partial = new CheckBox("Aggressive partial expansion");
    private final CheckBox buildStage = new CheckBox("Build");
    private final CheckBox mergeStage = new CheckBox("Merge");
    private final CheckBox flattenStage = new CheckBox("Flatten");
    private final CheckBox expandStage = new CheckBox("Expand");
    private final CheckBox cellLabels = new CheckBox("Cell labels");
    private final CheckBox rayLabels = new CheckBox("Ray annotations");
    private final TextField topDensity = new TextField("0.12");
    private final TextField secondDensity = new TextField("2.4");
    private final TextField alpha = new TextField("0.995");
    private final TextField expansionIterations = new TextField("3");

    private Grid grid;
    private Tri[] triangles;
    private Cell[] ownership;
    private Expand.Session expansion;
    private int selectedCell = 1;
    private int iteration;
    private String lastStep = "Ownership map";
    private Traversal traversal;
    private Vec2f rayOriginFraction = new Vec2f(0.0f, 0.38f);
    private Vec2f rayEndFraction = new Vec2f(1.0f, 0.63f);
    private int draggedRayHandle;
    private boolean rayWasDragged;

    @Override
    public void start(Stage stage) {
        partial.setSelected(true);
        cellLabels.setSelected(true);
        rayLabels.setSelected(true);
        cellLabels.setOnAction(event -> draw());
        rayLabels.setOnAction(event -> draw());
        status.setEditable(false);
        status.setFocusTraversable(false);
        status.setWrapText(true);
        status.setPrefRowCount(2);
        status.setMinHeight(54);
        status.setPrefHeight(54);
        status.setMaxHeight(54);
        status.setStyle("-fx-control-inner-background: #ffffff; -fx-background-color: #e2e8f0;"
                + " -fx-border-color: #cbd5e1; -fx-border-radius: 5; -fx-background-radius: 5;"
                + " -fx-font-family: 'Consolas'; -fx-font-size: 12px;");
        for (TextField field : new TextField[] {topDensity, secondDensity, alpha, expansionIterations}) {
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
            Vec2f rayOrigin = traversal.origin();
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

        FlowPane stages = new FlowPane(12, 6, new Label("Construction:"), buildStage,
                mergeStage, flattenStage, expandStage, partial,
                new Label("Display:"), cellLabels, rayLabels);
        FlowPane parameters = new FlowPane(8, 6,
                new Label("top_density"), topDensity,
                new Label("snd_density"), secondDensity,
                new Label("alpha"), alpha,
                new Label("exp_iters"), expansionIterations);
        Label applyHint = new Label("Press Enter in a value or reset construction to apply");
        applyHint.setStyle("-fx-text-fill: #64748b; -fx-font-size: 11px;");
        HBox controls = new HBox(10, reset, resetRay, traversalStep);
        VBox toolbar = new VBox(7, stages, parameters, applyHint, controls, status);
        toolbar.setPadding(new Insets(10, 14, 9, 14));
        toolbar.setStyle("-fx-background-color: #f8fafc; -fx-border-color: transparent transparent #e2e8f0 transparent;");
        BorderPane root = new BorderPane(canvas);
        root.setTop(toolbar);
        BorderPane.setMargin(canvas, new Insets(6, 0, 0, 0));
        root.setStyle("-fx-background-color: #f1f5f9;");

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
        traversal = createTraversal();
        lastStep = stageName();
        draw();
    }

    private void resetRay() {
        traversal = createTraversal();
        lastStep = "Ray located at grid entry";
        draw();
    }

    private void stepRay() {
        traversal.step();
        if (traversal.currentCell() >= 0) selectedCell = traversal.currentCell();
        lastStep = traversal.message();
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
            if (cellLabels.isSelected() && (id == selectedCell || (r.w >= 54 && r.h >= 22))) {
                g.save();
                g.beginPath();
                g.rect(r.x + 1, r.y + 1, Math.max(0, r.w - 2), Math.max(0, r.h - 2));
                g.closePath();
                g.clip();
                g.setFill(Color.web("#334155"));
                g.fillText("cell " + id + " " + references(id), r.x + 5, r.y + 15);
                g.restore();
            }
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
            g.setStroke(traversal.testedRefs().contains(i) ? Color.web("#f59e0b") : colors[i]);
            g.setLineWidth(traversal.testedRefs().contains(i) ? 5 : 2);
            g.fillPolygon(xs, ys, 3);
            g.strokePolygon(xs, ys, 3);
            if (rayLabels.isSelected()) {
                g.setFill(colors[i]);
                g.fillText("T" + i, xs[0] + 4, ys[0] - 4);
            }
        }
    }

    private void drawSampleRay(GraphicsContext g) {
        Vec2f start = traversal.origin();
        Vec2f end = traversal.origin().add(traversal.direction().mul((float) traversal.maxT()));
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
        for (Vec2f point : traversal.exits()) {
            g.fillOval(sxWorld(point.x) - 5, syWorld(point.y) - 5, 10, 10);
        }
        g.setFill(Color.web("#2563eb"));
        g.fillOval(sxWorld(start.x) - 8, syWorld(start.y) - 8, 16, 16);
        g.setStroke(Color.WHITE);
        g.setLineWidth(2);
        g.strokeOval(sxWorld(start.x) - 8, syWorld(start.y) - 8, 16, 16);
        g.setFill(traversal.hit() ? Color.web("#16a34a") : Color.web("#dc2626"));
        g.fillOval(sxWorld(current.x) - 7, syWorld(current.y) - 7, 14, 14);
        g.setFill(Color.web("#dc2626"));
        g.fillRect(sxWorld(end.x) - 7, syWorld(end.y) - 7, 14, 14);
        if (rayLabels.isSelected()) {
            g.setFill(Color.web("#dc2626"));
            g.fillText("sample ray", ORIGIN_X + 8, ORIGIN_Y + PLOT_HEIGHT * 0.62);
        }
    }

    private void drawLegend(GraphicsContext g) {
        double y = 18;
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
        // World scale is deliberately fixed: the 2D density heuristic is
        // invariant under uniform scaling, so it is not a construction input.
        float scale = 1.0f;
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

    private Traversal createTraversal() {
        Vec2f extents = grid.bbox.extents();
        Vec2f origin = new Vec2f(
                grid.bbox.min.x + extents.x * rayOriginFraction.x,
                grid.bbox.min.y + extents.y * rayOriginFraction.y);
        Vec2f end = new Vec2f(
                grid.bbox.min.x + extents.x * rayEndFraction.x,
                grid.bbox.min.y + extents.y * rayEndFraction.y);
        Cell[] exitBounds = ownership;
        if (expandStage.isSelected()) {
            exitBounds = new Cell[expansion.size()];
            for (int i = 0; i < exitBounds.length; i++) {
                exitBounds[i] = expansion.getCell(i).copy();
            }
        }
        return new Traversal(grid, triangles, exitBounds, origin, end);
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

    private double sxWorld(double x) {
        return ORIGIN_X + (x - grid.bbox.min.x) * PLOT_WIDTH / grid.bbox.extents().x;
    }

    private double syWorld(double y) {
        return ORIGIN_Y + (grid.bbox.max.y - y) * PLOT_HEIGHT / grid.bbox.extents().y;
    }

    private record Rect(double x, double y, double w, double h) {}

}
