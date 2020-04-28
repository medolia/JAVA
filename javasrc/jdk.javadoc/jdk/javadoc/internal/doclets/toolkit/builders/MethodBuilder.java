/*
 * Copyright (c) 2003, 2019, Oracle and/or its affiliates. All rights reserved.
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

package jdk.javadoc.internal.doclets.toolkit.builders;

import java.util.*;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

import jdk.javadoc.internal.doclets.toolkit.BaseConfiguration;
import jdk.javadoc.internal.doclets.toolkit.Content;
import jdk.javadoc.internal.doclets.toolkit.DocletException;
import jdk.javadoc.internal.doclets.toolkit.MethodWriter;
import jdk.javadoc.internal.doclets.toolkit.util.DocFinder;

import static jdk.javadoc.internal.doclets.toolkit.util.VisibleMemberTable.Kind.*;

/**
 * Builds documentation for a method.
 *
 *  <p><b>This is NOT part of any supported API.
 *  If you write code that depends on this, you do so at your own risk.
 *  This code and its internal interfaces are subject to change or
 *  deletion without notice.</b>
 */
public class MethodBuilder extends AbstractMemberBuilder {

    /**
     * The index of the current field that is being documented at this point
     * in time.
     */
    private ExecutableElement currentMethod;

    /**
     * The writer to output the method documentation.
     */
    private final MethodWriter writer;

    /**
     * The methods being documented.
     */
    private final List<? extends Element> methods;


    /**
     * Construct a new MethodBuilder.
     *
     * @param context       the build context.
     * @param typeElement the class whoses members are being documented.
     * @param writer the doclet specific writer.
     */
    private MethodBuilder(Context context,
            TypeElement typeElement,
            MethodWriter writer) {
        super(context, typeElement);
        this.writer = writer;
        methods = getVisibleMembers(METHODS);
    }

    /**
     * Construct a new MethodBuilder.
     *
     * @param context       the build context.
     * @param typeElement the class whoses members are being documented.
     * @param writer the doclet specific writer.
     *
     * @return an instance of a MethodBuilder.
     */
    public static MethodBuilder getInstance(Context context,
            TypeElement typeElement, MethodWriter writer) {
        return new MethodBuilder(context, typeElement, writer);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasMembersToDocument() {
        return !methods.isEmpty();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void build(Content contentTree) throws DocletException {
        buildMethodDoc(contentTree);
    }

    /**
     * Build the method documentation.
     *
     * @param memberDetailsTree the content tree to which the documentation will be added
     * @throws DocletException if there is a problem while building the documentation
     */
    protected void buildMethodDoc(Content memberDetailsTree) throws DocletException {
        if (writer == null) {
            return;
        }
        if (hasMembersToDocument()) {
            Content methodDetailsTreeHeader = writer.getMethodDetailsTreeHeader(typeElement,
                    memberDetailsTree);
            Content methodDetailsTree = writer.getMemberTreeHeader();

            for (Element method : methods) {
                currentMethod = (ExecutableElement)method;
                Content methodDocTree = writer.getMethodDocTreeHeader(currentMethod, methodDetailsTree);

                buildSignature(methodDocTree);
                buildDeprecationInfo(methodDocTree);
                buildMethodComments(methodDocTree);
                buildTagInfo(methodDocTree);

                methodDetailsTree.add(writer.getMethodDoc(methodDocTree));
            }
            memberDetailsTree.add(writer.getMethodDetails(methodDetailsTreeHeader, methodDetailsTree));
        }
    }

    /**
     * Build the signature.
     *
     * @param methodDocTree the content tree to which the documentation will be added
     */
    protected void buildSignature(Content methodDocTree) {
        methodDocTree.add(writer.getSignature(currentMethod));
    }

    /**
     * Build the deprecation information.
     *
     * @param methodDocTree the content tree to which the documentation will be added
     */
    protected void buildDeprecationInfo(Content methodDocTree) {
        writer.addDeprecated(currentMethod, methodDocTree);
    }

    /**
     * Build the comments for the method.  Do nothing if
     * {@link BaseConfiguration#nocomment} is set to true.
     *
     * @param methodDocTree the content tree to which the documentation will be added
     */
    protected void buildMethodComments(Content methodDocTree) {
        if (!configuration.nocomment) {
            ExecutableElement method = currentMethod;
            if (utils.getFullBody(currentMethod).isEmpty()) {
                DocFinder.Output docs = DocFinder.search(configuration,
                        new DocFinder.Input(utils, currentMethod));
                if (docs.inlineTags != null && !docs.inlineTags.isEmpty())
                        method = (ExecutableElement)docs.holder;
            }
            TypeMirror containingType = method.getEnclosingElement().asType();
            writer.addComments(containingType, method, methodDocTree);
        }
    }

    /**
     * Build the tag information.
     *
     * @param methodDocTree the content tree to which the documentation will be added
     */
    protected void buildTagInfo(Content methodDocTree) {
        writer.addTags(currentMethod, methodDocTree);
    }

    /**
     * Return the method writer for this builder.
     *
     * @return the method writer for this builder.
     */
    public MethodWriter getWriter() {
        return writer;
    }
}
