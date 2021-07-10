package org.w3c.dom;

public interface Document
{
  public Element createElement( String name );
  
  public Text createTextNode(String data);
}
