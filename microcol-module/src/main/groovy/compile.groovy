// Compile the descriptor with the same JDK as Maven. Argument lists preserve
// spaces in checkout paths, and a failed compiler must stop packaging.
def targetDir = new File(project.build.directory)
def artifactFile = new File(targetDir,
        "microcol-game-${project.version}-jar-with-dependencies.jar")
def moduleFile = new File(basedir, 'src/main/java/module-info.java')
def compiler = new File(System.getProperty('java.home'), 'bin/javac')
def release = project.properties.getProperty('maven.compiler.release', '11')

if (!artifactFile.isFile()) {
    throw new IllegalStateException("Missing game artifact: ${artifactFile}")
}
if (!targetDir.isDirectory() && !targetDir.mkdirs()) {
    throw new IllegalStateException("Cannot create module output: ${targetDir}")
}

def command = [compiler.absolutePath, '--release', release,
        '-d', targetDir.absolutePath, '--patch-module',
        "microcol.game=${artifactFile.absolutePath}".toString(),
        moduleFile.absolutePath]

println "Compiling MicroCol module descriptor with ${compiler} (release ${release})"
def process = new ProcessBuilder(command).directory(basedir).inheritIO().start()
def exitCode = process.waitFor()
if (exitCode != 0) {
    throw new IllegalStateException("Module descriptor compilation failed (exit ${exitCode})")
}

