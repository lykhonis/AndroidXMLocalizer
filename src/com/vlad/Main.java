package com.vlad;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class Main {

	public static void main(String[] args) {
		if (args.length == 3 && "-e".equals(args[0])) {
			try {
				Explode(args[1], args[2]);
			} catch (Exception e) {
				System.err.println("Fail: " + e.getMessage());
			}
		} else if (args.length == 3 && "-c".equals(args[0])) {
			try {
				Compile(args[1], args[2]);
			} catch (Exception e) {
				System.err.println("Fail: " + e.getMessage());
			}
		} else {
			System.out.println("Android XML Localizer 1.0");
			System.out.println("Usage: -e [res folder] [language id] - explode default language to translate");
			System.out.println("       -c [res folder] [language id] - compile to /res/values-[id]/string.xml");
		}
	}

	private static void ExplodeString(String translationFolderPath, NodeList elements) throws Exception {
		for (int i = 0; i < elements.getLength(); i++) {
			Element element = (Element) elements.item(i);

			PrintWriter languageFile = new PrintWriter(translationFolderPath + File.separator
					+ element.getAttribute("name") + ".txt");
			languageFile.println(element.getTextContent());
			languageFile.close();
		}
	}

	private static void ExplodeStringArray(String translationFolderPath, NodeList elements) throws Exception {
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

	private static void ExplodePlurals(String translationFolderPath, NodeList elements) throws Exception {
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

	private static void Explode(String resourceFolderPath, String languageId) throws Exception {
		File defaultLanguageFile = new File(resourceFolderPath + "values" + File.separator + "strings.xml");
		File translationFile = new File(resourceFolderPath + "translation-" + languageId + "/");

		translationFile.mkdir();

		DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		Document document = documentBuilder.parse(defaultLanguageFile);

		ExplodeString(translationFile.getPath(), document.getElementsByTagName("string"));
		ExplodeStringArray(translationFile.getPath(), document.getElementsByTagName("string-array"));
		ExplodePlurals(translationFile.getPath(), document.getElementsByTagName("plurals"));
	}

	public static String ReadAllText(File file) throws Exception {
		StringBuilder stringBuilder = new StringBuilder(512);
		BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
		String line;
		while ((line = bufferedReader.readLine()) != null) {
			stringBuilder.append(line);
		}
		return stringBuilder.toString();
	}

	private static void CompileString(Document document, Element rootElement, File file) throws Exception {
		String name = file.getName();
		Element element = document.createElement("string");
		element.setAttribute("name", name.substring(0, name.length() - 4)); // endsWith ".txt"
		element.setTextContent(ReadAllText(file));
		rootElement.appendChild(element);
	}
	
	private static void CompileStringArray(Document document, Element rootElement, File file) throws Exception {
		String name = file.getName();
		Element element = document.createElement("string-array");
		element.setAttribute("name", name.substring(6)); // startsWith "array."
		rootElement.appendChild(element);
		
		for (File fileItem : file.listFiles()) {
			Element item = document.createElement("item");
			item.setTextContent(ReadAllText(fileItem));
			element.appendChild(item);
		}
	}
	
	private static void CompilePlurals(Document document, Element rootElement, File file) throws Exception {
		String name = file.getName();
		Element element = document.createElement("plurals");
		element.setAttribute("name", name.substring(8)); // startsWith "plurals."
		rootElement.appendChild(element);
		
		for (File fileItem : file.listFiles()) {
			name = fileItem.getName();
			Element item = document.createElement("item");
			item.setAttribute("quantity", name.substring(0, name.length() - 4)); // endsWith ".txt"
			item.setTextContent(ReadAllText(fileItem));
			element.appendChild(item);
		}
	}

	private static void Compile(String resourceFolderPath, String languageId) throws Exception {
		File languageFile = new File(resourceFolderPath + "values-" + languageId + "/strings.xml");
		languageFile.getParentFile().mkdir();

		DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		Document document = documentBuilder.newDocument();		
		document.setXmlVersion("1.0");
		document.setXmlStandalone(true);
		
		Element rootElement = document.createElement("resources");
		document.appendChild(rootElement);

		File translationFile = new File(resourceFolderPath + "translation-" + languageId + "/");
		for (File file : translationFile.listFiles()) {
			if (file.isFile() && file.getPath().endsWith(".txt")) {
				CompileString(document, rootElement, file);
			} else if (file.isDirectory() && file.getName().startsWith("array.")) {
				CompileStringArray(document, rootElement, file);
			} else if (file.isDirectory() && file.getName().startsWith("plurals.")) {
				CompilePlurals(document, rootElement, file);
			}
		}
		
		Transformer transformer = TransformerFactory.newInstance().newTransformer();
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");

		StreamResult result = new StreamResult(new StringWriter());
		DOMSource source = new DOMSource(document);
		transformer.transform(source, result);

		PrintWriter printWriter = new PrintWriter(languageFile);
		printWriter.print(result.getWriter().toString());
		printWriter.close();
	}
}
