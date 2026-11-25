"""Exercise the real JavaFX Maven plugin without opening a desktop window."""
from pathlib import Path
import re
import subprocess
import tempfile
import unittest
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parent.parent


class DemoStudioArgumentsTest(unittest.TestCase):
    def test_plugin_delivers_single_studio_argument(self):
        script = (ROOT / "scripts/run-demo-studio.sh").read_text()
        argument = re.search(r"'-Djavafx.args=([^']+)'", script).group(1)
        project = ET.parse(ROOT / "akropia-game/pom.xml")
        ns = {"m": "http://maven.apache.org/POM/4.0.0"}
        plugin = next(p for p in project.findall(".//m:plugin", ns)
                      if p.findtext("m:artifactId", namespaces=ns) == "javafx-maven-plugin")
        version = plugin.findtext("m:version", namespaces=ns)
        with tempfile.TemporaryDirectory(prefix="akropia argument probe ") as folder:
            root = Path(folder)
            classes = root / "target/classes"
            classes.mkdir(parents=True)
            source = root / "ArgumentProbe.java"
            source.write_text("""
public class ArgumentProbe {
    public static void main(String[] args) {
        System.out.println("AKROPIA_ARGUMENT_COUNT=" + args.length);
        for (String arg : args) System.out.println("AKROPIA_ARGUMENT=" + arg);
    }
}
""")
            subprocess.run(["javac", "-d", str(classes), str(source)],
                           check=True, capture_output=True, text=True)
            (root / "pom.xml").write_text("""
<project xmlns="http://maven.apache.org/POM/4.0.0">
  <modelVersion>4.0.0</modelVersion>
  <groupId>org.akropia.test</groupId><artifactId>argument-probe</artifactId>
  <version>1</version>
</project>
""")
            result = subprocess.run([
                "mvn", "-q", "-f", str(root / "pom.xml"),
                f"org.openjfx:javafx-maven-plugin:{version}:run",
                "-Djavafx.mainClass=ArgumentProbe", "-Djavafx.args=" + argument],
                text=True, capture_output=True, timeout=90)
            self.assertEqual(result.returncode, 0, result.stdout + result.stderr)
            self.assertIn("AKROPIA_ARGUMENT_COUNT=1", result.stdout)
            self.assertIn("AKROPIA_ARGUMENT=--demo-studio=src/main/resources/demos",
                          result.stdout)


if __name__ == "__main__":
    unittest.main()
