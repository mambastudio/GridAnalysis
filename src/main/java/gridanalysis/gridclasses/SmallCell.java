package gridanalysis.gridclasses;

import gridanalysis.coordinates.UShort2;

/** Compressed irregular-grid cell, matching Hagrid's CUDA SmallCell shape. */
public record SmallCell(UShort2 min, UShort2 max, int begin) {
}
