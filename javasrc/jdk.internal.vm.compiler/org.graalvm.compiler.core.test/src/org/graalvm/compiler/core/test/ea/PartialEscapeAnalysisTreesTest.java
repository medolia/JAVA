/*
 * Copyright (c) 2017, 2019, Oracle and/or its affiliates. All rights reserved.
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
 */


package org.graalvm.compiler.core.test.ea;

import java.util.HashSet;

import org.graalvm.compiler.api.directives.GraalDirectives;
import org.graalvm.compiler.core.test.GraalCompilerTest;
import org.graalvm.compiler.debug.DebugContext;
import org.graalvm.compiler.nodes.debug.BlackholeNode;
import org.graalvm.compiler.phases.common.DeadCodeEliminationPhase;
import org.junit.Assert;
import org.junit.Test;

import jdk.vm.ci.code.InstalledCode;
import jdk.vm.ci.meta.ResolvedJavaMethod;

/*
 * Test whether complex tree structures properly maintain identity.
 */
public class PartialEscapeAnalysisTreesTest extends EATestBase {

    static class TreeNode {
        TreeNode left;
        TreeNode right;

        TreeNode() {

        }

        TreeNode(TreeNode left, TreeNode right) {
            this.left = left;
            this.right = right;
        }

        public void visit(HashSet<TreeNode> instances) {
            instances.add(this);
            if (left != null) {
                left.visit(instances);
            }
            if (right != null) {
                right.visit(instances);
            }
        }

        int countInstances() {
            HashSet<TreeNode> instances = new HashSet<>();
            visit(instances);
            return instances.size();
        }
    }

    public static TreeNode buildTree(boolean a) {
        TreeNode leftChild;
        TreeNode rightChild;
        TreeNode taskToFork;
        TreeNode task;
        if (a) {
            GraalDirectives.blackhole(new TreeNode());
            leftChild = new TreeNode();
            rightChild = new TreeNode();
            task = new TreeNode(leftChild, rightChild);
            taskToFork = rightChild;
            GraalDirectives.blackhole(task);
        } else {
            leftChild = new TreeNode();
            rightChild = new TreeNode();
            task = new TreeNode(leftChild, rightChild);
            taskToFork = leftChild;
            GraalDirectives.blackhole(task);
        }
        if (taskToFork.left == null) {
            taskToFork.left = new TreeNode();
        }

        return new TreeNode(task, null);
    }

    @Test
    public void testBuildTree() {
        testGraph("buildTree");
    }

    /**
     * Prepare a graph that includes some blackholes and then remove the blackholes and compile
     * normally to create an unusual situation for PEA.
     */
    @SuppressWarnings("try")
    public void testGraph(String name) {
        ResolvedJavaMethod method = getResolvedJavaMethod(name);

        prepareGraph(name, true);
        try (DebugContext.Scope s = graph.getDebug().scope(getClass(), method, getCodeCache(), graph)) {
            for (BlackholeNode node : graph.getNodes().filter(BlackholeNode.class)) {
                graph.removeFixed(node);
            }
            new DeadCodeEliminationPhase().apply(graph);
            createCanonicalizerPhase().apply(graph, context);

            InstalledCode code = getCode(method, graph, true);

            GraalCompilerTest.Result r = executeExpected(method, null, true);
            int expectedInstances = ((TreeNode) r.returnValue).countInstances();
            TreeNode r2 = (TreeNode) code.executeVarargs(true);
            Assert.assertEquals("Wrong number of nodes in tree", expectedInstances, r2.countInstances());

            r = executeExpected(method, null, false);
            expectedInstances = ((TreeNode) r.returnValue).countInstances();
            r2 = (TreeNode) code.executeVarargs(false);
            Assert.assertEquals("Wrong number of nodes in tree", expectedInstances, r2.countInstances());
        } catch (Throwable e) {
            throw graph.getDebug().handle(e);
        }
    }
}
