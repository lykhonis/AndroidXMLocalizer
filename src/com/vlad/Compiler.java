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

public class Compiler {
	
	public static void compile(String resourceFolderPath, String languageId) throws Exception {
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
				compileString(document, rootElement, file);
			} else if (file.isDirectory() && file.getName().startsWith("array.")) {
				compileStringArray(document, rootElement, file);
			} else if (file.isDirectory() && file.getName().startsWith("plurals.")) {
				compilePlurals(document, rootElement, file);
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

	private static String readAllText(File file) throws Exception {
		StringBuilder stringBuilder = new StringBuilder(512);
		BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
		String line;
		while ((line = bufferedReader.readLine()) != null) {
			stringBuilder.append(line);
		}
		return stringBuilder.toString();
	}

	private static void compileString(Document document, Element rootElement, File file) throws Exception {
		String name = file.getName();
		Element element = document.createElement("string");
		element.setAttribute("name", name.substring(0, name.length() - 4)); // endsWith ".txt"
		element.setTextContent(readAllText(file));
		rootElement.appendChild(element);
	}
	
	private static void compileStringArray(Document document, Element rootElement, File file) throws Exception {
		String name = file.getName();
		Element element = document.createElement("string-array");
		element.setAttribute("name", name.substring(6)); // startsWith "array."
		rootElement.appendChild(element);
		
		for (File fileItem : file.listFiles()) {
			Element item = document.createElement("item");
			item.setTextContent(readAllText(fileItem));
			element.appendChild(item);
		}
	}
	
	private static void compilePlurals(Document document, Element rootElement, File file) throws Exception {
		String name = file.getName();
		Element element = document.createElement("plurals");
		element.setAttribute("name", name.substring(8)); // startsWith "plurals."
		rootElement.appendChild(element);
		
		for (File fileItem : file.listFiles()) {
			name = fileItem.getName();
			Element item = document.createElement("item");
			item.setAttribute("quantity", name.substring(0, name.length() - 4)); // endsWith ".txt"
			item.setTextContent(readAllText(fileItem));
			element.appendChild(item);
		}
	}
}
