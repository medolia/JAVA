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
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;

import jdk.javadoc.internal.doclets.formats.html.markup.ContentBuilder;
import jdk.javadoc.internal.doclets.formats.html.markup.Entity;
import jdk.javadoc.internal.doclets.formats.html.markup.HtmlStyle;
import jdk.javadoc.internal.doclets.formats.html.markup.HtmlTag;
import jdk.javadoc.internal.doclets.formats.html.markup.HtmlTree;
import jdk.javadoc.internal.doclets.formats.html.markup.Navigation;
import jdk.javadoc.internal.doclets.formats.html.markup.Navigation.PageMode;
import jdk.javadoc.internal.doclets.formats.html.markup.StringContent;
import jdk.javadoc.internal.doclets.formats.html.markup.Table;
import jdk.javadoc.internal.doclets.formats.html.markup.TableHeader;
import jdk.javadoc.internal.doclets.toolkit.Content;
import jdk.javadoc.internal.doclets.toolkit.util.ClassUseMapper;
import jdk.javadoc.internal.doclets.toolkit.util.DocFileIOException;
import jdk.javadoc.internal.doclets.toolkit.util.DocPath;
import jdk.javadoc.internal.doclets.toolkit.util.DocPaths;

/**
 * Generate package usage information.
 *
 *  <p><b>This is NOT part of any supported API.
 *  If you write code that depends on this, you do so at your own risk.
 *  This code and its internal interfaces are subject to change or
 *  deletion without notice.</b>
 */
public class PackageUseWriter extends SubWriterHolderWriter {

    final PackageElement packageElement;
    final SortedMap<String, Set<TypeElement>> usingPackageToUsedClasses = new TreeMap<>();
    final String packageUseTableSummary;
    private final Navigation navBar;

    /**
     * Constructor.
     *
     * @param configuration the configuration
     * @param mapper a mapper to provide details of where elements are used
     * @param filename the file to be generated
     * @param pkgElement the package element to be documented
     */
    public PackageUseWriter(HtmlConfiguration configuration,
                            ClassUseMapper mapper, DocPath filename,
                            PackageElement pkgElement) {
        super(configuration, configuration.docPaths.forPackage(pkgElement).resolve(filename));
        this.packageElement = pkgElement;

        // by examining all classes in this package, find what packages
        // use these classes - produce a map between using package and
        // used classes.
        for (TypeElement usedClass : utils.getEnclosedTypeElements(pkgElement)) {
            Set<TypeElement> usingClasses = mapper.classToClass.get(usedClass);
            if (usingClasses != null) {
                for (TypeElement usingClass : usingClasses) {
                    PackageElement usingPackage = utils.containingPackage(usingClass);
                    Set<TypeElement> usedClasses = usingPackageToUsedClasses
                            .get(utils.getPackageName(usingPackage));
                    if (usedClasses == null) {
                        usedClasses = new TreeSet<>(utils.makeGeneralPurposeComparator());
                        usingPackageToUsedClasses.put(utils.getPackageName(usingPackage),
                                                      usedClasses);
                    }
                    usedClasses.add(usedClass);
                }
            }
        }

        packageUseTableSummary = resources.getText("doclet.Use_Table_Summary",
                resources.getText("doclet.packages"));
        this.navBar = new Navigation(packageElement, configuration, PageMode.USE, path);
    }

    /**
     * Generate a class page.
     *
     * @param configuration the current configuration of the doclet.
     * @param mapper        the mapping of the class usage.
     * @param pkgElement    the package being documented.
     * @throws DocFileIOException if there is a problem generating the package use page
     */
    public static void generate(HtmlConfiguration configuration,
                                ClassUseMapper mapper, PackageElement pkgElement)
            throws DocFileIOException {
        DocPath filename = DocPaths.PACKAGE_USE;
        PackageUseWriter pkgusegen = new PackageUseWriter(configuration, mapper, filename, pkgElement);
        pkgusegen.generatePackageUseFile();
    }

    /**
     * Generate the package use list.
     * @throws DocFileIOException if there is a problem generating the package use page
     */
    protected void generatePackageUseFile() throws DocFileIOException {
        HtmlTree body = getPackageUseHeader();
        HtmlTree div = new HtmlTree(HtmlTag.DIV);
        div.setStyle(HtmlStyle.contentContainer);
        if (usingPackageToUsedClasses.isEmpty()) {
            div.add(contents.getContent("doclet.ClassUse_No.usage.of.0", utils.getPackageName(packageElement)));
        } else {
            addPackageUse(div);
        }
        bodyContents.addMainContent(div);
        HtmlTree footer = HtmlTree.FOOTER();
        navBar.setUserFooter(getUserHeaderFooter(false));
        footer.add(navBar.getContent(false));
        addBottom(footer);
        bodyContents.setFooter(footer);
        body.add(bodyContents.toContent());
        printHtmlDocument(null,
                getDescription("use", packageElement),
                body);
    }

    /**
     * Add the package use information.
     *
     * @param contentTree the content tree to which the package use information will be added
     */
    protected void addPackageUse(Content contentTree) {
        Content content = new ContentBuilder();
        if (configuration.packages.size() > 1) {
            addPackageList(content);
        }
        addClassList(content);
        contentTree.add(content);
    }

    /**
     * Add the list of packages that use the given package.
     *
     * @param contentTree the content tree to which the package list will be added
     */
    protected void addPackageList(Content contentTree) {
        Content caption = contents.getContent(
                "doclet.ClassUse_Packages.that.use.0",
                getPackageLink(packageElement, utils.getPackageName(packageElement)));
        Table table = new Table(HtmlStyle.useSummary)
                .setCaption(caption)
                .setHeader(getPackageTableHeader())
                .setColumnStyles(HtmlStyle.colFirst, HtmlStyle.colLast);
        for (String pkgname: usingPackageToUsedClasses.keySet()) {
            PackageElement pkg = utils.elementUtils.getPackageElement(pkgname);
            Content packageLink = links.createLink(getPackageAnchorName(pkg),
                    new StringContent(utils.getPackageName(pkg)));
            Content summary = new ContentBuilder();
            if (pkg != null && !pkg.isUnnamed()) {
                addSummaryComment(pkg, summary);
            } else {
                summary.add(Entity.NO_BREAK_SPACE);
            }
            table.addRow(packageLink, summary);
        }
        contentTree.add(table.toContent());
    }

    /**
     * Add the list of classes that use the given package.
     *
     * @param contentTree the content tree to which the class list will be added
     */
    protected void addClassList(Content contentTree) {
        TableHeader classTableHeader = new TableHeader(
                contents.classLabel, contents.descriptionLabel);
        HtmlTree ul = new HtmlTree(HtmlTag.UL);
        ul.setStyle(HtmlStyle.blockList);
        for (String packageName : usingPackageToUsedClasses.keySet()) {
            PackageElement usingPackage = utils.elementUtils.getPackageElement(packageName);
            HtmlTree section = HtmlTree.SECTION(HtmlStyle.detail);
            section.add(links.createAnchor(getPackageAnchorName(usingPackage)));
            String tableSummary = resources.getText("doclet.Use_Table_Summary",
                                                        resources.getText("doclet.classes"));
            Content caption = contents.getContent(
                    "doclet.ClassUse_Classes.in.0.used.by.1",
                    getPackageLink(packageElement, utils.getPackageName(packageElement)),
                    getPackageLink(usingPackage, utils.getPackageName(usingPackage)));
            Table table = new Table(HtmlStyle.useSummary)
                    .setCaption(caption)
                    .setHeader(classTableHeader)
                    .setColumnStyles(HtmlStyle.colFirst, HtmlStyle.colLast);
            for (TypeElement te : usingPackageToUsedClasses.get(packageName)) {
                DocPath dp = pathString(te,
                        DocPaths.CLASS_USE.resolve(docPaths.forName(te)));
                Content stringContent = new StringContent(utils.getSimpleName(te));
                Content typeContent = links.createLink(dp.fragment(getPackageAnchorName(usingPackage)),
                        stringContent);
                Content summary = new ContentBuilder();
                addIndexComment(te, summary);

                table.addRow(typeContent, summary);
            }
            section.add(table.toContent());
            ul.add(HtmlTree.LI(HtmlStyle.blockList, section));
        }
        Content li = HtmlTree.SECTION(HtmlStyle.packageUses, ul);
        contentTree.add(li);
    }

    /**
     * Get the header for the package use listing.
     *
     * @return a content tree representing the package use header
     */
    private HtmlTree getPackageUseHeader() {
        String packageText = resources.getText("doclet.Package");
        String name = packageElement.isUnnamed() ? "" : utils.getPackageName(packageElement);
        String title = resources.getText("doclet.Window_ClassUse_Header", packageText, name);
        HtmlTree bodyTree = getBody(getWindowTitle(title));
        Content headerContent = new ContentBuilder();
        addTop(headerContent);
        Content linkContent = getModuleLink(utils.elementUtils.getModuleOf(packageElement),
                contents.moduleLabel);
        navBar.setNavLinkModule(linkContent);
        navBar.setUserHeader(getUserHeaderFooter(true));
        headerContent.add(navBar.getContent(true));
        ContentBuilder headingContent = new ContentBuilder();
        headingContent.add(contents.getContent("doclet.ClassUse_Title", packageText));
        headingContent.add(new HtmlTree(HtmlTag.BR));
        headingContent.add(name);
        Content heading = HtmlTree.HEADING(Headings.PAGE_TITLE_HEADING, true,
                HtmlStyle.title, headingContent);
        Content div = HtmlTree.DIV(HtmlStyle.header, heading);
        bodyContents.setHeader(headerContent)
                .addMainContent(div);
        return bodyTree;
    }
}
