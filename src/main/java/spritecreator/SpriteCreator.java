package spritecreator;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.imageio.ImageIO;

public class SpriteCreator {
	
	public static class ImageData {
		public String imageName;
		public BufferedImage bufferedImage;
	}
	
	private static final Integer MAX_COLUMN_COUNT = 10;
	private static final Integer TILE_WIDTH = 32;
	private static final Integer TILE_HEIGHT = 32;

	private String jarPath;
	private JarFile jarFile;

	private ImageData imageData;
	private List<ImageData> imageList;
	
	private Integer columnCount;
	private Integer rowCount;
	
	private BufferedImage spriteImage;
	
	public SpriteCreator() {
		
	}
	
	public void process(String path) {
		this.jarPath = path;
		work();	
	}
	
	private void work() {
		loadJar();
		
		if(jarFile == null) {
			return;
		}
		
		try {
			createImageList();
			createSprite();
			createCssFile();
			createSampleHtml();
		} catch (Exception ex) {
			System.out.println(ex.getMessage());
		}
	}
	
	private void loadJar() {
		
		try {
			jarFile = new JarFile(jarPath);
		} catch (Exception ex) {
			System.out.println("Error loading JAR file");
		}
	}
	
	private BufferedImage currentBufferedImage;
	
	private void createImageList() throws Exception {
		if(jarFile == null) {
			System.out.println("NULL jar file");
			endProgram();
		}
		
		imageList = new ArrayList<ImageData>();
		Enumeration<JarEntry> entries = jarFile.entries();
		
		while(entries.hasMoreElements()) {
			JarEntry entry = (JarEntry)entries.nextElement();
			if(entry.getName().endsWith(".png")) {
				System.out.println(entry.getName());
				imageData = new ImageData();
				imageData.imageName = getImageName(entry.getName());
				currentBufferedImage = ImageIO.read(jarFile.getInputStream(entry));
				imageData.bufferedImage = resizeImage(currentBufferedImage);
				imageList.add(imageData);
			}
		}
	}
	
	private void createSprite() throws Exception {
		Integer spriteWidth = (TILE_WIDTH * MAX_COLUMN_COUNT);
		Integer spriteHeight = ((imageList.size() / 10) * TILE_HEIGHT) + 1;
		resetRowAndColumnCount();
		
		spriteImage = new BufferedImage(spriteWidth, spriteHeight, BufferedImage.TYPE_INT_ARGB);
		
		Graphics2D g2 = spriteImage.createGraphics();
		
		for(ImageData data : imageList) {
			if(columnCount == 10) {
				rowCount++;
				columnCount = 0;
			}
			
			g2.drawImage(data.bufferedImage, (columnCount * TILE_WIDTH), (rowCount * TILE_HEIGHT), null);
			columnCount++;
		}
		
		g2.dispose();
		
		ImageIO.write(spriteImage, "PNG", new File("/tmp/sprite.png"));
	}
	
	private void createCssFile() throws Exception {
		String cssContent = buildCssContent();
		writeFile("sprite.css", cssContent);
	}
	
	private void createSampleHtml() throws Exception {
		String htmlContent = buildHtmlContent();
		writeFile("sample.html", htmlContent);
	}
	
	private String buildCssContent() {
		resetRowAndColumnCount();
		Integer xPos = 0;
		Integer yPos = 0;
		
		StringBuilder builder = new StringBuilder();
		
		builder.append(".sprite {\nvertical-align: middle;\ndisplay: inline-block;\nheight: " + TILE_HEIGHT.toString() + "px;\nwidth: " + TILE_WIDTH.toString() + "px;\n}\n\n.item-sprite {\nbackground: url(./sprite.png) no-repeat;\n}\n");
		
		for(ImageData imageData : imageList) {
			if(columnCount == 10) {
				columnCount = 0;
				rowCount++;
			}
			
			xPos = columnCount * TILE_WIDTH;
			yPos = rowCount * TILE_HEIGHT;
			
			builder.append("\n");
			builder.append("." + imageData.imageName);
			builder.append(" {\n");
			builder.append("background-position: -" + xPos.toString() + "px -" + yPos.toString() + "px;");
			builder.append("\n}");
			builder.append("\n");
			
			columnCount++;
		}
		
		return builder.toString();
	}
	
	private String buildHtmlContent() {
		StringBuilder builder = new StringBuilder();
		
		builder.append("<html><head>");
		builder.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"./sprite.css\"");
		builder.append("</head><body>");
		for(ImageData imageData : imageList) {
			builder.append("<div>");
			builder.append("<div class=\"sprite item-sprite " + imageData.imageName + "\" ></div>");
			
			builder.append("<span style=\"padding-left:0.5em\">" + imageData.imageName + "</span>");
			builder.append("</div>");
		}
		
		builder.append("</body></html>");
		
		return builder.toString();
	}
	
	private BufferedImage resizeImage(BufferedImage originalImage) {
		Image scaledImage = originalImage.getScaledInstance(TILE_WIDTH, TILE_HEIGHT, Image.SCALE_SMOOTH);
		BufferedImage scaledBufferedImage = new BufferedImage(scaledImage.getWidth(null), scaledImage.getHeight(null), BufferedImage.TYPE_INT_ARGB);
		scaledBufferedImage.getGraphics().drawImage(scaledImage, 0, 0, null);
		return scaledBufferedImage;
	}
	
	private String getImageName(String path) {
		String imageName = "";
		String[] stringParts = path.split("/");
		imageName = stringParts[stringParts.length - 1];
		return imageName.replace(".png", "");
	}
	
	private void writeFile(String fileName, String content) throws Exception {
		PrintWriter writer = new PrintWriter("/tmp/" + fileName);
		writer.write(content);
		writer.close();
		System.out.println("\nWrote File: " + fileName);
	}
	
	private void resetRowAndColumnCount() {
		rowCount = 0; 
		columnCount = 0;
	}
	
	private void endProgram() {
		System.exit(0);
	}
}
