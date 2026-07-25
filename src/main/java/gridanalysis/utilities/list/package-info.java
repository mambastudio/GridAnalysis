/**
 * Primitive and object buffers used by the CPU reference implementation.
 *
 * <p>The public concrete types own storage or expose shared slices. Package-
 * private base classes contain implementation support only; they are not an
 * extensibility API. Scan, partition, paired-sort, and swap operations mirror
 * the construction primitives used by the upstream GPU implementation.</p>
 */
package gridanalysis.utilities.list;
