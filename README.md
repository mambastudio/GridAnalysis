# GridAnalysis

GridAnalysis is a 2D Java/JavaFX implementation and visual laboratory for the
[Hagrid irregular-grid ray-tracing accelerator](https://github.com/cg-saarland/hagrid).
It is intended to make the construction and traversal algorithms inspectable
before translating them to a 3D GPU implementation such as OpenCL or CUDA.

The 2D model preserves the structure of Hagrid while translating octrees to
quadtrees, eight children to four, XYZ coordinates to XY, and box faces to box
edges.

![Hagrid expansion and traversal laboratory](Screenshot.png?raw=true "Hagrid expansion and traversal laboratory")

## Construction pipeline

The implementation follows the original pipeline:

1. **Build** creates the initial adaptive grid from primitive density.
2. **Merge** combines compatible neighboring cells.
3. **Flatten** converts the hierarchy into traversal-ready cell ownership.
4. **Expand** enlarges cell exit bounds so a ray can safely skip neighboring
   cells whose primitive references have already been considered.

Build and Merge are the established foundation of this port. Flatten, basic
expansion, aggressive partial expansion, and an interactive traversal debugger
are available for analysis.

Expansion does not change voxel ownership. It creates a second set of bounds
used only to decide how far traversal may advance. These expanded bounds may
overlap: correctness depends on the expanded cell containing only regions whose
primitive-reference set is a subset of the selected cell's references. The
optimization is successful when it returns the same nearest primitive and hit
distance as unexpanded traversal while reducing traversal steps or primitive
tests.

## Observed traversal results

The interactive 2D scenario already demonstrates the purpose of the expansion
passes clearly. For the same ray placed along a primitive edge, the laboratory
observed:

| Construction mode | Traversal steps |
| --- | ---: |
| Build + Merge + Flatten | 52 |
| Build + Merge + Flatten + Expand + aggressive partial expansion | 10 |

That is a reduction of 42 cell transitions, or approximately **81% fewer
traversal steps**, for this particular ray and scene configuration. The expanded
exit bounds let traversal cross several compatible ownership cells at once
instead of stopping at every original cell boundary.

This is an illustrative result rather than a general performance benchmark.
Different rays, density parameters, primitive layouts, and scene bounds produce
different reductions. The important correctness check is that optimized and
unoptimized traversal report the same nearest primitive and hit distance; step
count and primitive-test work can then be compared safely.

## Build and run

The project uses Maven with JDK 25 and JavaFX 26 Early Access.

```shell
mvn clean package
mvn javafx:run
```

The normal executable entry point is `gridanalysis.Launcher`; the JavaFX
application class is `gridanalysis.GridAnalysis`.

To open the dedicated low-resolution expansion and traversal laboratory:

```shell
mvn javafx:run@expansion-debug
```

The laboratory can enable each construction stage independently, edit the
Hagrid density and expansion parameters, inspect cells and reference sets, and
step a ray through the resulting grid. Drag the ray origin around the outer grid
boundary and drag its target to change direction. Each traversal step reports
the current voxel/cell, tested primitives, nearest hit, exit distance, and work.
The reusable `gridanalysis.algorithm.Traversal` class contains the actual 2D
cell-walking algorithm; the JavaFX laboratory only controls and visualizes it.

## Density model

The Java implementation uses the dimensionally correct 2D analogue of Hagrid's
grid-resolution heuristic:

```text
Rx = dx * sqrt(lambda * N / A)
Ry = dy * sqrt(lambda * N / A)
```

Here `A` is the scene or cell area, `N` is its primitive count, and `lambda` is
the selected density. Uniformly scaling all scene coordinates therefore leaves
the grid topology unchanged. Aspect ratio, primitive count, primitive
distribution, and the density parameters still influence subdivision.

The future 3D GPU implementation should use the paper's original cube-root
volume formula. Its scene dimensions scale linearly while volume scales
cubically, so it has the same uniform-scale invariance in three dimensions.

## Tests

Files under `src/test/java` are assertion-based programs with `main` methods,
not JUnit tests. Maven compiles them during the `test` phase. The expansion
harness can also be run directly after packaging:

```shell
java -ea -cp "target\classes;target\test-classes" gridanalysis.algorithm.ExpandTest
```

## Interactive laboratory

The screenshot above shows the merged and flattened ownership grid, scene
primitives, draggable sample ray, selected traversal bounds, construction-stage
controls, and live traversal diagnostics in the dedicated JavaFX laboratory.
