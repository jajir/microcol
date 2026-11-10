"""Launch selection without starting a desktop application."""
import json
import os
from pathlib import Path
import subprocess
import tempfile
import unittest


SCRIPT = Path(__file__).resolve().parent / "run-game.sh"


class LaunchTest(unittest.TestCase):
    def launch(self, platform, java_home=None, brew=True):
        with tempfile.TemporaryDirectory(prefix="akropia launch ") as directory:
            root = Path(directory)
            scripts = {
                "uname": "echo " + platform,
                "brew": ('printf "%s\\n" "$TEST_JDK_PREFIX"' if brew else "exit 1"),
                "mvn": "exec /usr/bin/python3 -c 'import json,os,sys; "
                       "print(json.dumps([os.environ.get(\"JAVA_HOME\"), "
                       "os.environ[\"PATH\"], sys.argv[1:], os.getcwd()]))' \"$@\"",
            }
            for name, body in scripts.items():
                executable = root / name
                executable.write_text("#!/bin/bash\n" + body + "\n")
                executable.chmod(0o755)
            env = dict(os.environ, PATH=str(root) + ":/usr/bin:/bin",
                       TEST_JDK_PREFIX=str(root / "jdk 25"))
            env.pop("JAVA_HOME", None)
            if java_home:
                env["JAVA_HOME"] = java_home
            result = subprocess.run(["/bin/bash", str(SCRIPT), "-Dexample=two words"],
                                    env=env, text=True, capture_output=True, check=True)
            return json.loads(result.stdout), env["TEST_JDK_PREFIX"]

    def test_homebrew_selection_and_launch_arguments(self):
        result, prefix = self.launch("Darwin")
        home = prefix + "/libexec/openjdk.jdk/Contents/Home"
        self.assertEqual(result[0], home)
        self.assertTrue(result[1].startswith(home + "/bin:"))
        self.assertEqual(result[2], ["-pl", "akropia-game", "validate", "compile",
                                     "javafx:run", "-Dexample=two words"])
        self.assertEqual(result[3], str(SCRIPT.parent.parent))

    def test_explicit_home_is_preserved(self):
        result, _ = self.launch("Darwin", "/explicit jdk")
        self.assertEqual(result[0], "/explicit jdk")
        self.assertTrue(result[1].startswith("/explicit jdk/bin:"))

    def test_non_mac_uses_configured_environment(self):
        result, _ = self.launch("Linux")
        self.assertIsNone(result[0])


if __name__ == "__main__":
    unittest.main()
