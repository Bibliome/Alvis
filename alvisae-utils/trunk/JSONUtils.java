package org.bibliome.util;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public abstract class JSONUtils {
	private JSONUtils() {}
	
	private static Element raw(Document doc, String s) {
		Element result = doc.createElement("raw");
		result.setTextContent(s);
		return result;
	}
	
	private static Element toXML(Document doc, JSONArray array) {
		Element result = doc.createElement("array");
		for (Object o : array)
			result.appendChild(toXML(doc, o));
		return result;
	}
	
	public static Element toXML(Document doc, JSONObject object) {
		Element result = doc.createElement("object");
		for (Object o : object.entrySet()) {
			@SuppressWarnings("unchecked")
			Map.Entry<String,Object> e = (Map.Entry<String,Object>) o;
			Element pair = doc.createElement("pair");
			pair.setAttribute("key", e.getKey());
			pair.appendChild(toXML(doc, e.getValue()));
			result.appendChild(pair);
		}
		return result;
	}
	
	private static Element toXML(Document doc, String s) {
		Element result = doc.createElement("quoted");
		result.setTextContent(s);
		return result;
	}
	
	public static Element toXML(Document doc, Object object) {
		if (object == null)
			return raw(doc, "null");
		Class<?> klass = object.getClass();
		if (klass.equals(JSONArray.class))
			return toXML(doc, (JSONArray) object);
		if (klass.equals(JSONObject.class))
			return toXML(doc, (JSONObject) object);
		if (klass.equals(String.class))
			return toXML(doc, (String) object);
		if (klass.equals(Boolean.class))
			return raw(doc, object.toString());
		if (Number.class.isAssignableFrom(klass))
			return raw(doc, object.toString());
		throw new RuntimeException("could not convert object of type " + klass + " (" + object + ")");
	}
	
	public static Document toXML(DocumentBuilder docBuilder, Object object) {
		Document result = docBuilder.newDocument();
		Element root = toXML(result, object);
		result.appendChild(root);
		return result;
	}
	
	public static void main(String args[]) throws ParserConfigurationException, IOException, TransformerException {
		Reader reader = new FileReader(args[0]);
		Object object = JSONValue.parse(reader);
		reader.close();
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = dbf.newDocumentBuilder();
		Document doc = toXML(docBuilder, object);
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		Source source = new DOMSource(doc);
		Result result = new StreamResult(new File(args[1]));
		transformer.transform(source, result);
	}
}
