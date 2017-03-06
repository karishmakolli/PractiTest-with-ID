package com;

import org.json.JSONObject;
import org.json.XML;

import java.io.*;

public class ReportGenerate {
public static int PRETTY_PRINT_INDENT_FACTOR = 4;
public int Number_Of_Files = 1;

public int generateJsonReport(String fileLocation) {
  BufferedReader reader = null;
  String xmlLine = null;
  StringBuilder builder = new StringBuilder();
  try {
    File[] files = new File(fileLocation).listFiles();
    Number_Of_Files = ((files.length) - 1);
    System.out.println("length of files is " + Number_Of_Files);
    for (File file : files) {
      String filePath = file.getAbsolutePath();
      if (filePath.endsWith(".xml") || filePath.endsWith("XML")) {
        reader = new BufferedReader(new FileReader(new File(filePath)));
        while ((xmlLine = reader.readLine()) != null) {
          builder.append(xmlLine.trim());
        }
      }
    }
    reader.close();
    String xmlValue = builder.toString();
    JSONObject xmlToJsonObj = XML.toJSONObject(xmlValue);
    BufferedWriter writer = new BufferedWriter(new FileWriter(new File(System.getProperty("user.home") + "/Documents/GeneratedGroupJsonReport.Json")));
    for (int i = 0; i < xmlToJsonObj.toString().split(",").length; i++) {
      writer.write(xmlToJsonObj.toString(PRETTY_PRINT_INDENT_FACTOR).split(",")[i]);
    }
    writer.close();
  } catch (FileNotFoundException ex) {
    ex.printStackTrace();
  } catch (java.io.IOException io) {
    io.printStackTrace();
  }
  return Number_Of_Files;
}

}
