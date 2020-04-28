/*
 * Copyright (c) 2000, 2019, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 */

// -- This file was mechanically generated: Do not edit! -- //

package java.nio;

import java.util.Objects;
import jdk.internal.access.foreign.MemorySegmentProxy;

/**

 * A read/write HeapDoubleBuffer.






 */

class HeapDoubleBuffer
    extends DoubleBuffer
{
    // Cached array base offset
    private static final long ARRAY_BASE_OFFSET = UNSAFE.arrayBaseOffset(double[].class);

    // Cached array base offset
    private static final long ARRAY_INDEX_SCALE = UNSAFE.arrayIndexScale(double[].class);

    // For speed these fields are actually declared in X-Buffer;
    // these declarations are here as documentation
    /*

    protected final double[] hb;
    protected final int offset;

    */

    HeapDoubleBuffer(int cap, int lim, MemorySegmentProxy segment) {            // package-private

        super(-1, 0, lim, cap, new double[cap], 0, segment);
        /*
        hb = new double[cap];
        offset = 0;
        */
        this.address = ARRAY_BASE_OFFSET;




    }

    HeapDoubleBuffer(double[] buf, int off, int len, MemorySegmentProxy segment) { // package-private

        super(-1, off, off + len, buf.length, buf, 0, segment);
        /*
        hb = buf;
        offset = 0;
        */
        this.address = ARRAY_BASE_OFFSET;




    }

    protected HeapDoubleBuffer(double[] buf,
                                   int mark, int pos, int lim, int cap,
                                   int off, MemorySegmentProxy segment)
    {

        super(mark, pos, lim, cap, buf, off, segment);
        /*
        hb = buf;
        offset = off;
        */
        this.address = ARRAY_BASE_OFFSET + off * ARRAY_INDEX_SCALE;




    }

    public DoubleBuffer slice() {
        int rem = this.remaining();
        return new HeapDoubleBuffer(hb,
                                        -1,
                                        0,
                                        rem,
                                        rem,
                                        this.position() + offset, segment);
    }

    @Override
    public DoubleBuffer slice(int index, int length) {
        Objects.checkFromIndexSize(index, length, limit());
        return new HeapDoubleBuffer(hb,
                                        -1,
                                        0,
                                        length,
                                        length,
                                        index + offset, segment);
    }

    public DoubleBuffer duplicate() {
        return new HeapDoubleBuffer(hb,
                                        this.markValue(),
                                        this.position(),
                                        this.limit(),
                                        this.capacity(),
                                        offset, segment);
    }

    public DoubleBuffer asReadOnlyBuffer() {

        return new HeapDoubleBufferR(hb,
                                     this.markValue(),
                                     this.position(),
                                     this.limit(),
                                     this.capacity(),
                                     offset, segment);



    }



    protected int ix(int i) {
        return i + offset;
    }







    public double get() {
        checkSegment();
        return hb[ix(nextGetIndex())];
    }

    public double get(int i) {
        checkSegment();
        return hb[ix(checkIndex(i))];
    }







    public DoubleBuffer get(double[] dst, int offset, int length) {
        checkSegment();
        Objects.checkFromIndexSize(offset, length, dst.length);
        int pos = position();
        if (length > limit() - pos)
            throw new BufferUnderflowException();
        System.arraycopy(hb, ix(pos), dst, offset, length);
        position(pos + length);
        return this;
    }

    public DoubleBuffer get(int index, double[] dst, int offset, int length) {
        checkSegment();
        Objects.checkFromIndexSize(index, length, limit());
        Objects.checkFromIndexSize(offset, length, dst.length);
        System.arraycopy(hb, ix(index), dst, offset, length);
        return this;
    }

    public boolean isDirect() {
        return false;
    }



    public boolean isReadOnly() {
        return false;
    }

    public DoubleBuffer put(double x) {

        checkSegment();
        hb[ix(nextPutIndex())] = x;
        return this;



    }

    public DoubleBuffer put(int i, double x) {

        checkSegment();
        hb[ix(checkIndex(i))] = x;
        return this;



    }

    public DoubleBuffer put(double[] src, int offset, int length) {

        checkSegment();
        Objects.checkFromIndexSize(offset, length, src.length);
        int pos = position();
        if (length > limit() - pos)
            throw new BufferOverflowException();
        System.arraycopy(src, offset, hb, ix(pos), length);
        position(pos + length);
        return this;



    }

    public DoubleBuffer put(DoubleBuffer src) {

        checkSegment();
        if (src instanceof HeapDoubleBuffer) {
            if (src == this)
                throw createSameBufferException();
            HeapDoubleBuffer sb = (HeapDoubleBuffer)src;
            int pos = position();
            int sbpos = sb.position();
            int n = sb.limit() - sbpos;
            if (n > limit() - pos)
                throw new BufferOverflowException();
            System.arraycopy(sb.hb, sb.ix(sbpos),
                             hb, ix(pos), n);
            sb.position(sbpos + n);
            position(pos + n);
        } else if (src.isDirect()) {
            int n = src.remaining();
            int pos = position();
            if (n > limit() - pos)
                throw new BufferOverflowException();
            src.get(hb, ix(pos), n);
            position(pos + n);
        } else {
            super.put(src);
        }
        return this;



    }

    public DoubleBuffer put(int index, double[] src, int offset, int length) {

        checkSegment();
        Objects.checkFromIndexSize(index, length, limit());
        Objects.checkFromIndexSize(offset, length, src.length);
        System.arraycopy(src, offset, hb, ix(index), length);
        return this;



    }





















    public DoubleBuffer compact() {

        int pos = position();
        int rem = limit() - pos;
        System.arraycopy(hb, ix(pos), hb, ix(0), rem);
        position(rem);
        limit(capacity());
        discardMark();
        return this;



    }








































































































































































































































































































































































































    public ByteOrder order() {
        return ByteOrder.nativeOrder();
    }







}