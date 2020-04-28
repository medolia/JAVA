/*
 * Copyright (c) 2000, 2017, Oracle and/or its affiliates. All rights reserved.
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

package javax.print.attribute.standard;

import javax.print.attribute.Attribute;
import javax.print.attribute.SetOfIntegerSyntax;
import javax.print.attribute.SupportedValuesAttribute;

/**
 * Class {@code CopiesSupported} is a printing attribute class, a set of
 * integers, that gives the supported values for a {@link Copies Copies}
 * attribute. It is restricted to a single contiguous range of integers;
 * multiple non-overlapping ranges are not allowed.
 * <p>
 * <b>IPP Compatibility:</b> The CopiesSupported attribute's canonical array
 * form gives the lower and upper bound for the range of copies to be included
 * in an IPP "copies-supported" attribute. See class
 * {@link SetOfIntegerSyntax SetOfIntegerSyntax} for an explanation of canonical
 * array form. The category name returned by {@code getName()} gives the IPP
 * attribute name.
 *
 * @author Alan Kaminsky
 */
public final class CopiesSupported extends SetOfIntegerSyntax
        implements SupportedValuesAttribute {

    /**
     * Use serialVersionUID from JDK 1.4 for interoperability.
     */
    private static final long serialVersionUID = 6927711687034846001L;

    /**
     * Construct a new copies supported attribute containing a single integer.
     * That is, only the one value of {@code Copies} is supported.
     *
     * @param  member set member
     * @throws IllegalArgumentException if {@code member < 1}
     */
    public CopiesSupported(int member) {
        super (member);
        if (member < 1) {
            throw new IllegalArgumentException("Copies value < 1 specified");
        }
    }

    /**
     * Construct a new copies supported attribute containing a single range of
     * integers. That is, only those values of {@code Copies} in the one range
     * are supported.
     *
     * @param  lowerBound Lower bound of the range
     * @param  upperBound Upper bound of the range
     * @throws IllegalArgumentException if a {@code null} range is specified or
     *         if a {@code non-null} range is specified with {@code lowerBound}
     *         less than 1
     */
    public CopiesSupported(int lowerBound, int upperBound) {
        super(lowerBound, upperBound);

        if (lowerBound > upperBound) {
            throw new IllegalArgumentException("Null range specified");
        } else if (lowerBound < 1) {
            throw new IllegalArgumentException("Copies value < 1 specified");
        }
    }

    /**
     * Returns whether this copies supported attribute is equivalent to the
     * passed in object. To be equivalent, all of the following conditions must
     * be true:
     * <ol type=1>
     *   <li>{@code object} is not {@code null}.
     *   <li>{@code object} is an instance of class {@code CopiesSupported}.
     *   <li>This copies supported attribute's members and {@code object}'s
     *   members are the same.
     * </ol>
     *
     * @param  object {@code Object} to compare to
     * @return {@code true} if {@code object} is equivalent to this copies
     *         supported attribute, {@code false} otherwise
     */
    public boolean equals(Object object) {
        return super.equals (object) && object instanceof CopiesSupported;
    }

    /**
     * Get the printing attribute class which is to be used as the "category"
     * for this printing attribute value.
     * <p>
     * For class {@code CopiesSupported}, the category is class
     * {@code CopiesSupported} itself.
     *
     * @return printing attribute class (category), an instance of class
     *         {@link Class java.lang.Class}
     */
    public final Class<? extends Attribute> getCategory() {
        return CopiesSupported.class;
    }

    /**
     * Get the name of the category of which this attribute value is an
     * instance.
     * <p>
     * For class {@code CopiesSupported}, the category name is
     * {@code "copies-supported"}.
     *
     * @return attribute category name
     */
    public final String getName() {
        return "copies-supported";
    }
}
