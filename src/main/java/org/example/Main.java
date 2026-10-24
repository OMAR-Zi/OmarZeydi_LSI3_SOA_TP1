package org.example;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;
import java.util.ArrayList;
import java.util.List;

public class Main {
    static final String XML_INVALIDE_PATH = "src/main/resources/restaurantnonvalide.xml";
    static final String XML_PATH = "src/main/resources/restaurant.xml";
    static final String XSD_PATH = "src/main/resources/restaurant.xsd";

    public static void main(String[] args) throws Exception {
        //Validation
        boolean valide = valider(XML_PATH, XSD_PATH);
        valider(XML_INVALIDE_PATH, XSD_PATH);

        if (!valide) {
            System.out.println("Arrêt : le document n'est pas valide.");
            return;
        }

        //Interrogation XPath
        Document doc = DocumentBuilderFactory.newInstance()
                .newDocumentBuilder().parse(new File(XML_PATH));
        XPath xpath = XPathFactory.newInstance().newXPath();

        afficher(doc, xpath, "Tous les noms de plats", "//plat/nom");
        afficher(doc, xpath, "Plats en promotion", "//plat[@promotion='true']/nom");
        afficher(doc, xpath, "Clients actifs", "//client[@actif='true']/nom");
        afficher(doc, xpath, "Plats de plus de 10", "//plat[prix > 10]/nom");

        Double nb = (Double) xpath.evaluate("count(//commande)", doc, XPathConstants.NUMBER);
        System.out.println("\nNombre de commandes : " + nb.intValue());
    }

    static boolean valider(String xmlPath, String xsdPath) {
        try {
            SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = factory.newSchema(new File(xsdPath));
            Validator validator = schema.newValidator();

            List<String> erreurs = new ArrayList<>();
            validator.setErrorHandler(new DefaultHandler() {
                @Override
                public void error(SAXParseException e) {
                    erreurs.add("ligne " + e.getLineNumber() + " : " + e.getMessage());
                }
            });

            validator.validate(new StreamSource(new File(xmlPath)));

            if (erreurs.isEmpty()) {
                System.out.println(xmlPath + " : document XML valide.");
                return true;
            }
            System.out.println(xmlPath + " : document invalide (" + erreurs.size() + " erreurs) :");
            erreurs.forEach(err -> System.out.println(" - " + err));
            return false;
        } catch (SAXException | java.io.IOException e) {
            System.out.println("Erreur : " + e.getMessage());
            return false;
        }
    }

    static void afficher(Document doc, XPath xpath, String titre, String requete) throws Exception {
        System.out.println("\n" + titre + " (" + requete + ") :");
        NodeList noeuds = (NodeList) xpath.evaluate(requete, doc, XPathConstants.NODESET);
        for (int i = 0; i < noeuds.getLength(); i++) {
            System.out.println(" - " + noeuds.item(i).getTextContent());
        }
    }
}