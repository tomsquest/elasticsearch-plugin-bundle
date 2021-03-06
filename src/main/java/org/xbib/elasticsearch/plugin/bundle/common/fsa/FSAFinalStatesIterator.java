package org.xbib.elasticsearch.plugin.bundle.common.fsa;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * An iterator that traverses the right language of a given node (all sequences
 * reachable from a given node).
 */
public final class FSAFinalStatesIterator implements Iterator<ByteBuffer> {
    /**
     * Default expected depth of the recursion stack (estimated longest sequence
     * in the automaton). Buffers expand by the same value if exceeded.
     */
    private static final int EXPECTED_MAX_STATES = 15;

    /**
     * The FSA to which this iterator belongs.
     */
    private final FSA fsa;

    /**
     * An internal cache for the next element in the FSA
     */
    private ByteBuffer nextElement;

    /**
     * A buffer for the current sequence of bytes from the current node to the
     * root.
     */
    private byte[] buffer = new byte[EXPECTED_MAX_STATES];

    /**
     * Reusable byte buffer wrapper around {@link #buffer}.
     */
    private ByteBuffer bufferWrapper = ByteBuffer.wrap(buffer);

    /**
     * An arc stack for DFS when processing the automaton.
     */
    private int[] arcs = new int[EXPECTED_MAX_STATES];

    /**
     * Current processing depth in {@link #arcs}.
     */
    private int position;

    /**
     * Create an instance of the iterator for a given node.
     *
     * @param fsa  fsa
     * @param node node
     */
    public FSAFinalStatesIterator(FSA fsa, int node) {
        this.fsa = fsa;

        if (fsa.getFirstArc(node) != 0) {
            restartFrom(node);
        }
    }

    /**
     * Restart walking from <code>node</code>. Allows iterator reuse.
     *
     * @param node node
     */
    public void restartFrom(int node) {
        position = 0;
        bufferWrapper.clear();
        nextElement = null;

        pushNode(node);
    }

    /**
     * Returns <code>true</code> if there are still elements in this iterator.
     */
    @Override
    public boolean hasNext() {
        if (nextElement == null) {
            nextElement = advance();
        }

        return nextElement != null;
    }

    /**
     * @return Returns a {@link java.nio.ByteBuffer} with the sequence corresponding to
     * the next final state in the automaton.
     */
    @Override
    public ByteBuffer next() {
        if (nextElement != null) {
            final ByteBuffer cache = nextElement;
            nextElement = null;
            return cache;
        } else {
            final ByteBuffer cache = advance();
            if (cache == null) {
                throw new NoSuchElementException();
            }
            return cache;
        }
    }

    /**
     * Advances to the next available final state.
     */
    private ByteBuffer advance() {
        if (position == 0) {
            return null;
        }
        while (position > 0) {
            final int lastIndex = position - 1;
            final int arc = arcs[lastIndex];
            if (arc == 0) {
                // Remove the current node from the queue.
                position--;
                continue;
            }
            // Go to the next arc, but leave it on the stack
            // so that we keep the recursion depth level accurate.
            arcs[lastIndex] = fsa.getNextArc(arc);
            // Expand buffer if needed.
            final int bufferLength = this.buffer.length;
            if (lastIndex >= bufferLength) {
                this.buffer = Arrays.copyOf(buffer, bufferLength
                        + EXPECTED_MAX_STATES);
                this.bufferWrapper = ByteBuffer.wrap(buffer);
            }
            buffer[lastIndex] = fsa.getArcLabel(arc);
            if (!fsa.isArcTerminal(arc)) {
                // Recursively descend into the arc's node.
                pushNode(fsa.getEndNode(arc));
            }
            if (fsa.isArcFinal(arc)) {
                bufferWrapper.clear();
                bufferWrapper.limit(lastIndex + 1);
                return bufferWrapper;
            }
        }
        return null;
    }

    /**
     * Not implemented in this iterator.
     */
    @Override
    public void remove() {
        throw new UnsupportedOperationException("Read-only iterator.");
    }

    /**
     * Descends to a given node, adds its arcs to the stack to be traversed.
     */
    private void pushNode(int node) {
        // Expand buffers if needed.
        if (position == arcs.length) {
            arcs = Arrays.copyOf(arcs, arcs.length + EXPECTED_MAX_STATES);
        }
        arcs[position++] = fsa.getFirstArc(node);
    }
}
