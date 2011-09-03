package com.vlad;

public class Main {

	public static void main(String[] args) {
		if (args.length == 3 && "-e".equals(args[0])) {
			try {
				Exploder.explode(args[1], args[2]);
			} catch (Exception e) {
				System.err.println("Fail: " + e.getMessage());
			}
		} else if (args.length == 3 && "-c".equals(args[0])) {
			try {
				Compiler.compile(args[1], args[2]);
			} catch (Exception e) {
				System.err.println("Fail: " + e.getMessage());
			}
		} else {
			System.out.println("Android XML Localizer 1.0");
			System.out.println("Usage: -e [res folder] [language id] - explode default language to translate");
			System.out.println("       -c [res folder] [language id] - compile to /res/values-[id]/string.xml");
		}
	}
}
