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

package java.nio;

import java.util.Objects;

// ## If the sequence is a string, use reflection to share its array

class StringCharBuffer                                  // package-private
    extends CharBuffer
{
    CharSequence str;

    StringCharBuffer(CharSequence s, int start, int end) { // package-private
        super(-1, start, end, s.length(), null);
        int n = s.length();
        Objects.checkFromToIndex(start, end, n);
        str = s;
        this.isReadOnly = true;
    }

    public CharBuffer slice() {
        return new StringCharBuffer(str,
                                    -1,
                                    0,
                                    this.remaining(),
                                    this.remaining(),
                                    offset + this.position());
    }

    @Override
    public CharBuffer slice(int index, int length) {
        Objects.checkFromIndexSize(index, length, limit());
        return new StringCharBuffer(str,
                                    -1,
                                    0,
                                    length,
                                    length,
                                    offset + index);
    }

    private StringCharBuffer(CharSequence s,
                             int mark,
                             int pos,
                             int limit,
                             int cap,
                             int offset) {
        super(mark, pos, limit, cap, null, offset, null);
        str = s;
        this.isReadOnly = true;
    }

    public CharBuffer duplicate() {
        return new StringCharBuffer(str, markValue(),
                                    position(), limit(), capacity(), offset);
    }

    public CharBuffer asReadOnlyBuffer() {
        return duplicate();
    }

    public final char get() {
        return str.charAt(nextGetIndex() + offset);
    }

    public final char get(int index) {
        return str.charAt(checkIndex(index) + offset);
    }

    char getUnchecked(int index) {
        return str.charAt(index + offset);
    }

    // ## Override bulk get methods for better performance

    public final CharBuffer put(char c) {
        throw new ReadOnlyBufferException();
    }

    public final CharBuffer put(int index, char c) {
        throw new ReadOnlyBufferException();
    }

    public final CharBuffer compact() {
        throw new ReadOnlyBufferException();
    }

    public final boolean isReadOnly() {
        return true;
    }

    final String toString(int start, int end) {
        return str.subSequence(start + offset, end + offset).toString();
    }

    public final CharBuffer subSequence(int start, int end) {
        try {
            int pos = position();
            return new StringCharBuffer(str,
                                        -1,
                                        pos + checkIndex(start, pos),
                                        pos + checkIndex(end, pos),
                                        capacity(),
                                        offset);
        } catch (IllegalArgumentException x) {
            throw new IndexOutOfBoundsException();
        }
    }

    public boolean isDirect() {
        return false;
    }

    public ByteOrder order() {
        return ByteOrder.nativeOrder();
    }

    ByteOrder charRegionOrder() {
        return null;
    }

    public boolean equals(Object ob) {
        if (this == ob)
            return true;
        if (!(ob instanceof CharBuffer))
            return false;
        CharBuffer that = (CharBuffer)ob;
        if (this.remaining() != that.remaining())
            return false;
        return BufferMismatch.mismatch(this, this.position(),
                                       that, that.position(),
                                       this.remaining()) < 0;
    }

    public int compareTo(CharBuffer that) {
        int i = BufferMismatch.mismatch(this, this.position(),
                                        that, that.position(),
                                        Math.min(this.remaining(), that.remaining()));
        if (i >= 0) {
            return Character.compare(this.get(this.position() + i), that.get(that.position() + i));
        }
        return this.remaining() - that.remaining();
    }
}
