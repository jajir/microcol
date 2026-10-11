package org.microcol.build;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Element;

final class GameReportSupport {
    private GameReportSupport() {
    }

    static String verify(Path reports) throws Exception {
        require(Files.isDirectory(reports),
                "No game test reports; refusing untested native build");
        List<Path> files;
        try (var paths = Files.list(reports)) {
            files = paths
                    .filter(path -> path.getFileName().toString()
                            .matches("TEST-.*\\.xml"))
                    .sorted().collect(Collectors.toList());
        }
        require(!files.isEmpty(), "No game Surefire XML reports");
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature(
                "http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        long tests = 0;
        long skipped = 0;
        StringBuilder record = new StringBuilder();
        for (Path report : files) {
            Element suite = factory.newDocumentBuilder().parse(report.toFile())
                    .getDocumentElement();
            require("testsuite".equals(suite.getTagName()),
                    "Not a Surefire testsuite: " + report);
            long count = count(suite, "tests");
            long omissions = count(suite, "skipped");
            require(count(suite, "failures") == 0,
                    "Failing game suite: " + report);
            require(count(suite, "errors") == 0,
                    "Errored game suite: " + report);
            require(omissions <= count, "Invalid skipped count: " + report);
            tests = Math.addExact(tests, count);
            skipped = Math.addExact(skipped, omissions);
            record.append(report.getFileName()).append(": tests=").append(count)
                    .append(", skipped=").append(omissions).append('\n');
        }
        require(tests > skipped, "All game tests were absent or skipped");
        return record.append("Totals: tests=").append(tests)
                .append(", skipped=").append(skipped)
                .append(", failures=0, errors=0\n").toString();
    }

    private static long count(Element suite, String attribute)
            throws IOException {
        require(suite.hasAttribute(attribute),
                "Missing test count: " + attribute);
        long result = Long.parseLong(suite.getAttribute(attribute));
        require(result >= 0, "Negative test count: " + attribute);
        return result;
    }

    private static void require(boolean condition, String message)
            throws IOException {
        if (!condition) {
            throw new IOException(message);
        }
    }
}
