import java.util.zip.*

def version = project.version
def artifact = "microcol-game-" + version + "-jar-with-dependencies.jar"
def artifactFile = basedir.absolutePath + "/target/" + artifact
def moduleFile = basedir.absolutePath + "/src/main/java/module-info.java"
def targetDir = basedir.absolutePath + "/target/"

println "Base Dir: " + basedir
println "Version : " + version
println "Artifact: " + artifact
println "Artifact file: " + artifactFile
println "Module file  : " + moduleFile
println "Target dir   : " + targetDir

def command = "javac -verbose -d " + targetDir + " --patch-module" +
        " microcol.game=" + artifactFile + 
        " " + moduleFile
        
println command

Process process = command.execute()
def out = new StringBuffer()
def err = new StringBuffer()
process.consumeProcessOutput( out, err )
process.waitFor()
if( out.size() > 0 ) println out
if( err.size() > 0 ) println err


