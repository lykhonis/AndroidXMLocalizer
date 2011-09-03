package com.vlad;

import java.io.File;
import java.io.PrintWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class Exploder {

	public static void explode(String resourceFolderPath, String languageId) throws Exception {
		File defaultLanguageFile = new File(resourceFolderPath + "values" + File.separator + "strings.xml");
		File translationFile = new File(resourceFolderPath + "translation-" + languageId + "/");

		translationFile.mkdir();

		DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		Document document = documentBuilder.parse(defaultLanguageFile);
		
		String path = translationFile.getPath();

		explodeString(path, document.getElementsByTagName("string"));
		explodeStringArray(path, document.getElementsByTagName("string-array"));
		explodePlurals(path, document.getElementsByTagName("plurals"));
	}
	
	private static void explodeString(String translationFolderPath, NodeList elements) throws Exception {
		for (int i = 0; i < elements.getLength(); i++) {
			Element element = (Element) elements.item(i);

			PrintWriter languageFile = new PrintWriter(translationFolderPath + File.separator
					+ element.getAttribute("name") + ".txt");
			languageFile.println(element.getTextContent());
			languageFile.close();
		}
	}

	private static void explodeStringArray(String translationFolderPath, NodeList elements) throws Exception {
		for (int i = 0; i < elements.getLength(); i++) {
			Element element = (Element) elements.item(i);
			NodeList items = element.getElementsByTagName("item");

			File arrayPathFile = new File(translationFolderPath + File.separator + "array."
					+ element.getAttribute("name"));
			arrayPathFile.mkdir();

			for (int j = 0; j < items.getLength(); j++) {
				Element item = (Element) items.item(j);

				PrintWriter languageFile = new PrintWriter(arrayPathFile.getPath() + File.separator + String.valueOf(j)
						+ ".txt");
				languageFile.println(item.getTextContent());
				languageFile.close();
			}
		}
	}

	private static void explodePlurals(String translationFolderPath, NodeList elements) throws Exception {
		for (int i = 0; i < elements.getLength(); i++) {
			Element element = (Element) elements.item(i);
			NodeList items = element.getElementsByTagName("item");

			File pluralsFile = new File(translationFolderPath + File.separator + "plurals."
					+ element.getAttribute("name"));
			pluralsFile.mkdir();

			for (int j = 0; j < items.getLength(); j++) {
				Element item = (Element) items.item(j);

				PrintWriter languageFile = new PrintWriter(pluralsFile.getPath() + File.separator
						+ item.getAttribute("quantity") + ".txt");
				languageFile.println(item.getTextContent());
				languageFile.close();
			}
		}
	}
}
