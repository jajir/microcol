import java.awt.Image
import java.awt.image.BufferedImage
import javax.imageio.ImageIO
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption

/**
 * Script generate images from SVG filies with custom color scheme.
 */


def conf = new Conf(
		inkscape:"/Applications/Inkscape.app/Contents/MacOS/Inkscape",
		microcolBaseDir: "/Users/jan/Documents/repo/github/microcol/")

/**
 * Zacatek zadani operaci a transformaci.
 */

//Definice barev
def colors = readColors(conf.baseDir + "main/resources/colors.properties")

//sunset(conf, colors)
//colony(conf, colors)
//europe(conf, colors)
//market(conf, colors)
//background(conf, colors)
//cursors(conf, colors)
bullet(conf, colors)

//Sunset
def sunset(conf, colors) {
	Request sunset = new Request(
			sourceFileName:conf.tmp + "./sunset.svg",
			exportFileName:conf.destiny + "sunset.png",
			exportArea:"-446:-113:1422:1450",
			exportWidth:781,
			exportHeight:651)
	copyFile(conf.filePath("sunset2.svg"),conf.tmp + "sunset.svg")
	colorReplacer(conf.tmp + "sunset.svg", colors)
	export(conf, sunset)
	cut(conf.destiny + "sunset.png", conf.destiny + "sunset-top-line.png", 65, 90, 0, 65 )
	cut(conf.destiny + "sunset.png", conf.destiny + "sunset-bottom-line.png", 48, 75, 0, 555 )
}

//Colony
def colony(conf, colors) {
        copyFile(conf.filePath("colony-background.svg"),conf.tmp + "colony-background.svg")
        colorReplacer(conf.tmp + "colony-background.svg", colors)
        export(conf, new Request(
                sourceFileName:conf.tmp + "colony-background.svg",
                exportFileName:conf.destiny + "colony.png",
                exportArea:"-635:-292:1327:1611",
                exportWidth:780,
                exportHeight:758))
        cut(conf.destiny + "colony.png", conf.destiny + "colony-top1.png", 65, 203, 20, 74 )
        cut(conf.destiny + "colony.png", conf.destiny + "colony-top2.png", 85, 190, 685, 82 )
        cut(conf.destiny + "colony.png", conf.destiny + "colony-bottom.png", 71, 62, 2, 645 )
}

//Europe
def europe(conf, colors) {
	copyFile(conf.filePath("europe1.svg"),conf.tmp + "europe.svg")
	colorReplacer(conf.tmp + "europe.svg", colors)
	export(conf, new Request(
		sourceFileName:conf.tmp + "europe.svg",
		exportFileName:conf.destiny + "europe.png",
		exportArea:"-885:-879:1180:772",
		exportWidth:780,
		exportHeight:624))
	cut(conf.destiny + "europe.png", conf.destiny + "europe-left.png", 83, 624, 1, 0 )
	cut(conf.destiny + "europe.png", conf.destiny + "europe-right.png", 22, 624, 757, 0 )
}

//Market
def market(conf, colors) {
	copyFile(conf.filePath("market.svg"),conf.tmp + "market.svg")
	colorReplacer(conf.tmp + "market.svg", colors)
	export(conf, new Request(
		sourceFileName:conf.tmp + "market.svg",
		exportFileName:conf.destiny + "market.png",
		exportArea:"-257:-114:1674:1536",
		exportWidth:700,
		exportHeight:598))
	cut(conf.destiny + "market.png", conf.destiny + "market-top.png", 10, 598, 1, 0 )
	cut(conf.destiny + "market.png", conf.destiny + "market-bottom.png", 65, 598, 634, 0 )
}

//Game background
def background(conf, colors) {
	copyFile(conf.filePath("background.svg"),conf.tmp + "background.svg")
	colorReplacer(conf.tmp + "background.svg", colors)
	export(conf, new Request(
		sourceFileName:conf.tmp + "background.svg",
		exportFileName:conf.destiny + "background.png",
		exportArea:"0:0:720:480",
		exportWidth:648,
		exportHeight:432))
}

//Crates
def crates(conf, colors) {
        copyFile(conf.filePath("crates.svg"),conf.tmp + "crates.svg")
        colorReplacer(conf.tmp + "crates.svg", colors)
        export(conf, new Request(
                sourceFileName:conf.tmp + "crates.svg",
                exportFileName:conf.destiny + "crates.png",
                exportArea:"0:0:125:250",
                exportWidth:200,
                exportHeight:400))
        cut(conf.destiny + "crates.png", conf.destiny + "crate-closed.png", 200, 200, 0, 0 )
        cut(conf.destiny + "crates.png", conf.destiny + "crate-open.png", 200, 200, 0, 200 )
}

//Cursors
def cursors(conf, colors) {
        copyFile(conf.filePath("cursors.svg"),conf.tmp + "cursors.svg")
        colorReplacer(conf.tmp + "cursors.svg", colors)
        export(conf, new Request(
                sourceFileName:conf.tmp + "cursors.svg",
                exportFileName:conf.destiny + "cursors.png",
                exportArea:"0:0:100:300",
                exportWidth:30,
                exportHeight:90))
        cut(conf.destiny + "cursors.png", conf.destiny + "cursor-goto.png", 30, 30, 0, 0 )
        cut(conf.destiny + "cursors.png", conf.destiny + "icon-steps-25x25.png", 25, 25, 2, 32 )
        cut(conf.destiny + "cursors.png", conf.destiny + "icon-steps-turn-25x25.png", 25, 25, 2, 32 )
        cut(conf.destiny + "cursors.png", conf.destiny + "icon-steps-anchor-25x25.png", 25, 25, 2, 62 )
        cut(conf.destiny + "cursors.png", conf.destiny + "icon-steps-anchor-turn-25x25.png", 25, 25, 2, 62 )
}

//Bulet
def bullet(conf, colors) {
        copyFile(conf.filePath("bullet.svg"),conf.tmp + "bullet.svg")
        colorReplacer(conf.tmp + "bullet.svg", colors)
        export(conf, new Request(
                sourceFileName:conf.tmp + "bullet.svg",
                exportFileName:conf.destiny + "bullet.png",
                exportArea:"0:0:50:50",
                exportWidth:15,
                exportHeight:15))
}


/**
 * Konec zadani operaci a transformaci. 
 */
print "Done!\n"


/**
 * Configuration
 */

class Conf {
	String inkscape
	String microcolBaseDir
	
	def filePath(svgfileName) {
		return microcolBaseDir + "microcol-game/src/graphics/" + svgfileName;
	}
	
	def getBaseDir() {
		return microcolBaseDir + "microcol-game/src/"
	}
	
	def getDestiny() {
		return baseDir + "main/resources/images/"
	}
	
	def getTmp() {
            microcolBaseDir + "microcol-game/target/"
	}
	
}

/**
 * Definice funkci s svg 
 */

class Request {

	String sourceFileName
	String exportFileName
	String exportArea
	String exportWidth
	String exportHeight

}

def export(Conf conf, Request req) {
	def pmdCommand = conf.inkscape + " --without-gui --export-type=png "+
			" --export-file=" + req.exportFileName +
			" --export-area=" +  req.exportArea+
			" --export-width=" + req.exportWidth+
			" --export-height=" + req.exportHeight+
			" " + req.sourceFileName

	println "Command is: " + pmdCommand

	/**
	 * Following allows me to execure shell script and print out results.
	 */
	def sout = new StringBuffer()
	def serr = new StringBuffer()
	System.out << sout.toString()
	System.err << serr.toString()
	def process = pmdCommand.execute()
	process.consumeProcessOutput(sout, serr)
	process.waitForProcessOutput()
}


def cut(sourceFileName, targetFileName, width, height, positionX, positionY) {
	BufferedImage source = ImageIO.read(new File(sourceFileName));
	def result = new BufferedImage((int)width,(int)height,BufferedImage.TYPE_4BYTE_ABGR)
	def g = result.getGraphics();
	g.drawImage(source, -positionX, -positionY, null)
	g.dispose()
	ImageIO.write(result,'PNG',new File(targetFileName))
}

/**
 * 
 * @param fileName
 * @param replace map with color id and value that should be set.
 * @return
 */
def colorReplacer(String fileName, Map<String,String> replace) {
	def xml = new XmlParser().parse(fileName)
	def sun = xml.defs.'*'.findAll{node ->
		def String nodeId = node.@id
		def String val = replace.get(nodeId)
		if(val) {
			def List<String> styles = node.stop.@style
			if (styles.size()!=1) {
				println "Divny pocet attributy 'style' v " + node
			}else {
				def String style = styles[0]
				def List<String> out = new ArrayList()
				style.tokenize(";").each { part ->
					def (p1, p2) = part.tokenize(":")
					if(p1=="stop-color") {
						out.add("stop-color:" + val)
					}else {
						out.add(p1 + ":" + p2)
					}
				}
				def String value = ""
				out.each {
					value += it + ";"
				}
				node.stop.@style = value
			}
		}
	}
	def writer = new FileWriter(fileName)
	new XmlNodePrinter(new PrintWriter(writer)).print(xml)
}

def copyFile(String fileNameFrom, String fileNameTo){
	Files.copy(Paths.get(fileNameFrom), Paths.get(fileNameTo), StandardCopyOption.REPLACE_EXISTING)
}

def readColors(String fileName) {
	def out = [:]
	new File(fileName).eachLine { line ->
		if(line && !line.startsWith("#")) {
			def (key, value) = line.tokenize("=")
			out[key] = value
		}
	  }
	return out
}