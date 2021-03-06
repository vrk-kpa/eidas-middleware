/*
 * Copyright (c) 2020 Governikus KG. Licensed under the EUPL, Version 1.2 or as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the "Licence"); You may not use this work except
 * in compliance with the Licence. You may obtain a copy of the Licence at:
 * http://joinup.ec.europa.eu/software/page/eupl Unless required by applicable law or agreed to in writing,
 * software distributed under the Licence is distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS
 * OF ANY KIND, either express or implied. See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 */

package de.governikus.eumw.utils.xml;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import lombok.extern.slf4j.Slf4j;


/**
 * this class should provide utility helper methods for XML
 */
@Slf4j
public final class XmlHelper
{

  /**
   * utility class constructor
   */
  private XmlHelper()
  {
    super();
  }

  /**
   * this method will marshal any element that is annotated with
   * {@link javax.xml.bind.annotation.XmlRootElement}
   *
   * @param object the annotated xml-object
   * @return the string representation of the xml-object
   */
  public static String marshalObject(Object object)
  {
    if (log.isTraceEnabled())
    {
      log.trace("translating java instance of type '{}' to a xml-string.", object.getClass());
    }
    try
    {
      JAXBContext jc = JAXBContext.newInstance(object.getClass());
      Marshaller marshaller = jc.createMarshaller();
      marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
      marshaller.setProperty(Marshaller.JAXB_ENCODING, StandardCharsets.UTF_8.name());
      final StringWriter w = new StringWriter();
      marshaller.marshal(object, w);
      return w.toString();
    }
    catch (JAXBException e)
    {
      throw new XmlException("error while marshalling class " + object.getClass().getName(), e);
    }
  }

  /**
   * this method will marshal any element that is annotated with
   * {@link javax.xml.bind.annotation.XmlRootElement} into a file
   *
   * @param object the annotated xml-object
   * @param pathname the file path to the created xml file
   */
  public static void marshalObjectToFile(Object object, String pathname)
  {
    if (log.isTraceEnabled())
    {
      log.trace("translating java instance of type '{}' to a xml-string.", object.getClass());
    }
    try
    {
      JAXBContext jc = JAXBContext.newInstance(object.getClass());
      Marshaller marshaller = jc.createMarshaller();
      marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
      marshaller.setProperty(Marshaller.JAXB_ENCODING, StandardCharsets.UTF_8.name());
      marshaller.marshal(object, new File(pathname));
    }
    catch (JAXBException e)
    {
      throw new XmlException("error while marshalling class " + object.getClass().getName(), e);
    }
  }

  /**
   * this method will be used to unmarshal a XML string into a POJO
   *
   * @param xml the xml string that should be unmarshalled
   * @param clazz the destination class that will be able to hold the data of the XML structure
   * @return the unmarshalled POJO
   */
  public static <T> T unmarshal(String xml, Class<T> clazz)
  {
    if (log.isTraceEnabled())
    {
      log.trace("unmarshalling xml '{}' to object of type '{}'.", xml, clazz);
    }
    try
    {
      JAXBContext jaxbContext = JAXBContext.newInstance(clazz);
      Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
      return (T)jaxbUnmarshaller.unmarshal(new StringReader(xml));
    }
    catch (Exception ex)
    {
      throw new XmlException("could not parse given XML \n'" + xml + "'\n", ex);
    }
  }

  /**
   * this method will be used to unmarshal a XML file into a POJO
   *
   * @param xmlFile the xml file that should be unmarshalled
   * @param clazz the destination class that will be able to hold the data of the XML structure
   * @return the unmarshalled POJO
   */
  public static <T> T unmarshal(File xmlFile, Class<T> clazz)
  {
    if (log.isTraceEnabled())
    {
      log.trace("unmarshalling xml file '{}' to object of type '{}'.", xmlFile, clazz);
    }
    try
    {
      JAXBContext jaxbContext = JAXBContext.newInstance(clazz);
      Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
      return (T)jaxbUnmarshaller.unmarshal(xmlFile);
    }
    catch (Exception ex)
    {
      throw new XmlException("could not parse given XML \n'" + xmlFile + "'\n", ex);
    }
  }

  /**
   * Will check if the given xml string is a valid xml representation
   *
   * @param xml string representation to check
   * @return true if the xml representation is not erroneous, false else
   */
  public static boolean isXmlWellFormed(String xml)
  {
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    dbf.setValidating(false);

    try (ByteArrayInputStream inputStream = new ByteArrayInputStream(xml.getBytes()))
    {
      dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
      dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
      dbf.setFeature("http://xml.org/sax/features/external-general-entities", false);
      dbf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
      dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
      dbf.setXIncludeAware(false);
      dbf.setExpandEntityReferences(false);
      dbf.setNamespaceAware(true);

      DocumentBuilder builder = null;
      builder = dbf.newDocumentBuilder();
      // the "parse" method also validates XML, will throw an exception if misformatted
      builder.parse(new InputSource(inputStream));
      return true;
    }
    catch (ParserConfigurationException | SAXException | IOException e)
    {
      return false;
    }
  }
}
