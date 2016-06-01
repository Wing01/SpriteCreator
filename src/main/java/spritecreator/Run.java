package spritecreator;

import java.util.Scanner;

public class Run {
	
	private static String path = "/tmp/forge-1.9-12.16.1.1891-srgBin.jar";

	public static void main (String[] args) {
		
		Scanner scanner = new Scanner(System.in);
		
		System.out.print("JAR Path: ");
		String jarPath = scanner.next();
		
		scanner.close();
		
		if(jarPath.endsWith(".jar")) {
			SpriteCreator loader = new SpriteCreator();
			loader.process(jarPath);
		} else {
			System.out.println("Invalid File Type. Jar Files Only");
		}
	}
}
