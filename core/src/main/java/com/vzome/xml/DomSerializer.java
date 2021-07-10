package com.vzome.xml;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;

public class DomSerializer
{   
    public static String toString( Element element )
    {
        DOMImplementation impl = element .getOwnerDocument() .getImplementation();
        if ( impl .hasFeature( "LS", "3.0" ) ){
            DOMImplementationLS lsImpl = (DOMImplementationLS) impl .getFeature("LS", "3.0");
            LSSerializer serializer = lsImpl .createLSSerializer();
            serializer .getDomConfig() .setParameter( "xml-declaration", false ); //by default its true, so set it to false to get String without xml-declaration
            return serializer .writeToString( element );
        }
        else {
            try {
                Transformer transformer = TransformerFactory.newInstance().newTransformer();
                StringWriter stringWriter = new StringWriter();
                transformer.transform(new DOMSource(element), new StreamResult(stringWriter));
                return stringWriter.toString();
            } catch (TransformerException e) {
                e.printStackTrace();
                return "<unableToSerialize/>";
            }
        }
    }
    
    public static void serialize( Document doc, Writer out ) throws UnsupportedEncodingException, TransformerException
    {       
        TransformerFactory tf = TransformerFactory .newInstance();
        Transformer transformer = tf .newTransformer();
        transformer .setOutputProperty( OutputKeys.OMIT_XML_DECLARATION, "no" );
        transformer .setOutputProperty( OutputKeys.METHOD, "xml" );
        transformer .setOutputProperty( OutputKeys.INDENT, "yes" );
        transformer .setOutputProperty( OutputKeys.STANDALONE, "no" );
        transformer .setOutputProperty( OutputKeys.ENCODING, "UTF-8" );
        transformer .setOutputProperty( "{http://xml.apache.org/xslt}indent-amount", "2" );
        
        transformer.transform( new DOMSource( doc ), new StreamResult( out ) );
    }
    
    public static void serialize( Document doc, OutputStream out ) throws UnsupportedEncodingException, TransformerException
    {
        serialize( doc, new OutputStreamWriter( out, "UTF-8" ) );
    }
    
    public static String getXmlString( Element node )
    {
        return toString( node );
    }
}
