/*
 * Copyright (c) 1998, 2019, Oracle and/or its affiliates. All rights reserved.
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

package jdk.javadoc.internal.doclets.formats.html;

import java.util.Set;
import java.util.TreeSet;

import jdk.javadoc.internal.doclets.formats.html.markup.BodyContents;
import jdk.javadoc.internal.doclets.formats.html.markup.ContentBuilder;
import jdk.javadoc.internal.doclets.formats.html.markup.Entity;
import jdk.javadoc.internal.doclets.formats.html.markup.HtmlStyle;
import jdk.javadoc.internal.doclets.formats.html.markup.HtmlTag;
import jdk.javadoc.internal.doclets.formats.html.markup.HtmlTree;
import jdk.javadoc.internal.doclets.formats.html.markup.StringContent;
import jdk.javadoc.internal.doclets.toolkit.Content;
import jdk.javadoc.internal.doclets.toolkit.util.DocFileIOException;
import jdk.javadoc.internal.doclets.toolkit.util.DocPath;
import jdk.javadoc.internal.doclets.toolkit.util.DocPaths;
import jdk.javadoc.internal.doclets.toolkit.util.IndexBuilder;


/**
 * Generate only one index file for all the Member Names with Indexing in
 * Unicode Order. The name of the generated file is "index-all.html" and it is
 * generated in current or the destination directory.
 *
 *  <p><b>This is NOT part of any supported API.
 *  If you write code that depends on this, you do so at your own risk.
 *  This code and its internal interfaces are subject to change or
 *  deletion without notice.</b>
 *
 * @see java.lang.Character
 */
public class SingleIndexWriter extends AbstractIndexWriter {

    private Set<Character> elements;

    /**
     * Construct the SingleIndexWriter with filename "index-all.html" and the
     * {@link IndexBuilder}
     *
     * @param configuration the configuration for this doclet
     * @param filename     Name of the index file to be generated.
     * @param indexbuilder Unicode based Index from {@link IndexBuilder}
     */
    public SingleIndexWriter(HtmlConfiguration configuration,
                             DocPath filename,
                             IndexBuilder indexbuilder) {
        super(configuration, filename, indexbuilder);
    }

    /**
     * Generate single index file, for all Unicode characters.
     *
     * @param configuration the configuration for this doclet
     * @param indexbuilder IndexBuilder built by {@link IndexBuilder}
     * @throws DocFileIOException if there is a problem generating the index
     */
    public static void generate(HtmlConfiguration configuration,
                                IndexBuilder indexbuilder) throws DocFileIOException {
        DocPath filename = DocPaths.INDEX_ALL;
        SingleIndexWriter indexgen = new SingleIndexWriter(configuration,
                                         filename, indexbuilder);
        indexgen.generateIndexFile();
    }

    /**
     * Generate the contents of each index file, with Header, Footer,
     * Member Field, Method and Constructor Description.
     * @throws DocFileIOException if there is a problem generating the index
     */
    protected void generateIndexFile() throws DocFileIOException {
        String title = resources.getText("doclet.Window_Single_Index");
        HtmlTree body = getBody(getWindowTitle(title));
        Content headerContent = new ContentBuilder();
        addTop(headerContent);
        navBar.setUserHeader(getUserHeaderFooter(true));
        headerContent.add(navBar.getContent(true));
        HtmlTree divTree = new HtmlTree(HtmlTag.DIV);
        divTree.setStyle(HtmlStyle.contentContainer);
        elements = new TreeSet<>(indexbuilder.getIndexMap().keySet());
        elements.addAll(configuration.tagSearchIndexKeys);
        addLinksForIndexes(divTree);
        for (Character unicode : elements) {
            if (configuration.tagSearchIndexMap.get(unicode) == null) {
                addContents(unicode, indexbuilder.getMemberList(unicode), divTree);
            } else if (indexbuilder.getMemberList(unicode) == null) {
                addSearchContents(unicode, configuration.tagSearchIndexMap.get(unicode), divTree);
            } else {
                addContents(unicode, indexbuilder.getMemberList(unicode),
                        configuration.tagSearchIndexMap.get(unicode), divTree);
            }
        }
        addLinksForIndexes(divTree);
        HtmlTree footer = HtmlTree.FOOTER();
        navBar.setUserFooter(getUserHeaderFooter(false));
        footer.add(navBar.getContent(false));
        addBottom(footer);
        body.add(new BodyContents()
                .setHeader(headerContent)
                .addMainContent(HtmlTree.DIV(HtmlStyle.header,
                        HtmlTree.HEADING(Headings.PAGE_TITLE_HEADING,
                                contents.getContent("doclet.Index"))))
                .addMainContent(divTree)
                .setFooter(footer)
                .toContent());
        createSearchIndexFiles();
        printHtmlDocument(null, "index", body);
    }

    /**
     * Add links for all the Index Files per unicode character.
     *
     * @param contentTree the content tree to which the links for indexes will be added
     */
    protected void addLinksForIndexes(Content contentTree) {
        for (Object ch : elements) {
            String unicode = ch.toString();
            contentTree.add(
                    links.createLink(getNameForIndex(unicode),
                            new StringContent(unicode)));
            contentTree.add(Entity.NO_BREAK_SPACE);
        }
        contentTree.add(new HtmlTree(HtmlTag.BR));
        contentTree.add(links.createLink(DocPaths.ALLCLASSES_INDEX,
                contents.allClassesLabel));
        if (!configuration.packages.isEmpty()) {
            contentTree.add(getVerticalSeparator());
            contentTree.add(links.createLink(DocPaths.ALLPACKAGES_INDEX,
                    contents.allPackagesLabel));
        }
        if (!configuration.tagSearchIndex.isEmpty()) {
            contentTree.add(getVerticalSeparator());
            contentTree.add(links.createLink(DocPaths.SYSTEM_PROPERTIES, contents.systemPropertiesLabel));
        }
    }
}
