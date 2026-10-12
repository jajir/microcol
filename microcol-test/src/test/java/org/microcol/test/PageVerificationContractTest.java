package org.microcol.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.tools.SimpleJavaFileObject;
import javax.tools.ToolProvider;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.microcol.page.PageComponentPanelDock;

import com.sun.source.tree.AssertTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.util.JavacTask;
import com.sun.source.util.TreeScanner;

@Tag("local")
class PageVerificationContractTest {

    @Test
    void dockVerificationRejectsWrongExpectationsAndMissingShips() {
        final PageComponentPanelDock dock = mock(PageComponentPanelDock.class,
                CALLS_REAL_METHODS);
        doReturn(2).when(dock).getNumberOfShipsInPort();
        dock.verifyNumberOfShipsInPort(2);
        assertThrows(AssertionError.class,
                () -> dock.verifyNumberOfShipsInPort(1));

        doReturn(false).when(dock).isUnitInPortSelected(0);
        dock.verifyUnitInPortSelected(0, false);
        assertThrows(AssertionError.class,
                () -> dock.verifyUnitInPortSelected(0, true));

        doReturn(List.of()).when(dock).getUnitsInPort();
        assertThrows(IndexOutOfBoundsException.class,
                () -> dock.verifyUnitInPortSelected(1, false));
    }

    @Test
    void pageAssertionsBelongToVerificationMethodsAndNeverToScenarios()
            throws Exception {
        final Path root = Path.of("src/test/java/org/microcol");
        final List<Path> sources;
        try (Stream<Path> files = Files.walk(root)) {
            sources = files.filter(path -> path.toString().endsWith(".java"))
                    .filter(path -> path.getParent()
                            .equals(root.resolve("page"))
                            || path.getFileName().toString().startsWith("TC_"))
                    .collect(Collectors.toList());
        }
        assertTrue(sources.size() > 19,
                "The page/scenario source set must exist");
        final List<String> violations = new ArrayList<>();
        for (Path source : sources) {
            violations.addAll(findViolations(source.getFileName().toString(),
                    Files.readString(source),
                    source.getFileName().toString().startsWith("TC_")));
        }
        assertEquals(List.of(), violations);
    }

    @Test
    void detectsHiddenAssertionsWithoutMistakingLocatorsOrComments()
            throws Exception {
        final String source = "class Example {"
                + "void action() { assertTrue(true); }"
                + "int getCount() { assert false; return 0; }"
                + "void verifyCount() { assertEquals(1, 1); }"
                + "void locate() { FxAssert.assertContext(); }"
                + "void comment() { /* assertTrue(false); */ }" + "}";
        assertEquals(2, findViolations("Example.java", source, false).size());
        assertEquals(3, findViolations("TC_Example.java", source, true).size());
    }

    private List<String> findViolations(final String fileName,
            final String source, final boolean scenario) throws Exception {
        final SimpleJavaFileObject input = new SimpleJavaFileObject(
                URI.create("string:///" + fileName),
                javax.tools.JavaFileObject.Kind.SOURCE) {
            @Override
            public CharSequence getCharContent(final boolean ignoreErrors) {
                return source;
            }
        };
        final List<String> violations = new ArrayList<>();
        try (var manager = ToolProvider.getSystemJavaCompiler()
                .getStandardFileManager(null, null, null)) {
            final JavacTask task = (JavacTask) ToolProvider
                    .getSystemJavaCompiler().getTask(null, manager, null,
                            List.of("-proc:none"), null, List.of(input));
            new TreeScanner<Void, String>() {
                @Override
                public Void visitMethod(final MethodTree method,
                        final String enclosingMethod) {
                    return super.visitMethod(method,
                            method.getName().toString());
                }

                @Override
                public Void visitMethodInvocation(
                        final MethodInvocationTree call, final String method) {
                    final String target = call.getMethodSelect().toString();
                    final String name = target
                            .substring(target.lastIndexOf('.') + 1);
                    if ((name.startsWith("assert")
                            && !name.equals("assertContext"))
                            || name.equals("fail")
                            || target.equals("FxAssert.verifyThat")) {
                        checkMethod(method);
                    }
                    return super.visitMethodInvocation(call, method);
                }

                @Override
                public Void visitAssert(final AssertTree assertion,
                        final String method) {
                    checkMethod(method);
                    return super.visitAssert(assertion, method);
                }

                private void checkMethod(final String method) {
                    if (scenario || method == null
                            || !method.startsWith("verify")) {
                        violations.add(fileName + ":" + method);
                    }
                }
            }.scan(task.parse(), null);
        }
        return violations;
    }
}
