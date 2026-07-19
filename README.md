# GridAnalysis

GridAnalysis is a 2D Java/JavaFX implementation and visual laboratory for the
[Hagrid irregular-grid ray-tracing accelerator](https://github.com/cg-saarland/hagrid).
It is intended to make the construction and traversal algorithms inspectable
before translating them to a 3D GPU implementation such as OpenCL or CUDA.

The 2D model preserves the structure of Hagrid while translating octrees to
quadtrees, eight children to four, XYZ coordinates to XY, and box faces to box
edges.

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

## Density model

The visual 2D implementation deliberately retains Hagrid's cube-root density
scaling. Although square-root scaling would be dimensionally natural in a pure
2D algorithm, the cube root keeps the parameters comparable with the original
3D implementation and with the values used by the GPU port.

Density is relative to the scene bounds. Changing the overall bounds, primitive
scale, or resolution therefore changes the apparent refinement even when the
numeric density values remain the same.

## Tests

Files under `src/test/java` are assertion-based programs with `main` methods,
not JUnit tests. Maven compiles them during the `test` phase. The expansion
harness can also be run directly after packaging:

```shell
java -ea -cp "target\classes;target\test-classes" gridanalysis.algorithm.ExpandTest
```

## Screenshots

Adaptive multi-level build:

![Adaptive multi-level grid](screenshot1.png?raw=true "Adaptive multi-level grid")

Merged irregular grid:

![Merged irregular grid](screenshot2.jpg?raw=true "Merged irregular grid")
