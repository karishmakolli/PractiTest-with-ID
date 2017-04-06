package com;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileCompare {
JSONParser parser = new JSONParser();
org.json.JSONArray writeArray = new org.json.JSONArray();
public static JSONArray testCaseID = new JSONArray();
JSONObject currentFrameWorkObject = new JSONObject();
JSONArray PtTests = new JSONArray();
JSONArray results = new JSONArray();
FileWriter file;
String testName;
String PTID;
public static int PRETTY_PRINT_INDENT_FACTOR = 4;

public static JSONArray getTestCaseID() {
  return testCaseID;
}

public static void setTestCaseID(JSONArray testCaseID) {
  testCaseID = testCaseID;
}

public void getTestCases(int fileCount) {
  JSONArray frameWorkArray = new JSONArray();
  JSONObject testSuite = new JSONObject();
  JSONArray testCasesInNestedSuite = new JSONArray();
  try {
    Object AutomationResultObj = parser.parse(new FileReader(System.getProperty("user.home") + "/Documents/GeneratedGroupJsonReport.Json"));
    JSONObject automationReportObject = (JSONObject) AutomationResultObj;
    file = new FileWriter(System.getProperty("user.home") + "/Documents/TestsToBeCreated.Json");

    if (fileCount > 1) {
      frameWorkArray = (JSONArray) automationReportObject.get("testsuites");
    } else {
      JSONObject testsuites = (JSONObject) automationReportObject.get("testsuites");
      frameWorkArray = convertJsonObjectToJsonArray(testsuites.get("testsuite"));
    }
    for (Object frameWorkObj : frameWorkArray) {
      currentFrameWorkObject = (JSONObject) frameWorkObj;
      if (fileCount > 1) {
        if (currentFrameWorkObject.get("testsuite") instanceof JSONArray) {
          testCasesInNestedSuite = (JSONArray) currentFrameWorkObject.get("testsuite");
          for (Object innerTestSuite : testCasesInNestedSuite) {
            testSuite = (JSONObject) innerTestSuite;
            currentFrameWorkObject = testSuite;
            getPTID();
          }
        } else {
          currentFrameWorkObject = (JSONObject) currentFrameWorkObject.get("testsuite");
          getPTID();
        }
      } else {
        getPTID();
      }
    }
    setTestCaseID(testCaseID);
    for (int i = 0; i < writeArray.toString().split(",").length; i++) {
      file.write(writeArray.toString(PRETTY_PRINT_INDENT_FACTOR).split(",")[i]);
    }
    file.close();
  } catch (FileNotFoundException e) {
    e.printStackTrace();
  } catch (IOException e) {
    e.printStackTrace();
  } catch (ParseException e) {
    e.printStackTrace();
  }
}

public void getPTID() {
  JSONArray testCases = new JSONArray();
  JSONObject testCaseObject = new JSONObject();
  try {
    testCases = (JSONArray) convertJsonObjectToJsonArray(currentFrameWorkObject.get("testcase"));
    for (Object testcase : testCases) {
      testCaseObject = (JSONObject) testcase;
      if (testCaseObject.get("name").toString().contains("guardrails") || testCaseObject.get("classname").toString().contains("guardrails")) {
        testName = testCaseObject.get("classname").toString() + " " + testCaseObject.get("name").toString();
        PTID = generatePTID(testCaseObject.get("name").toString());
        if (PTID != null) {
          JSONObject storeResult = new JSONObject();
          getStatusAndTime(testCaseObject, storeResult);
          storeResult.put("name", testName);
          storeResult.put("PTID", PTID);
          results.add(storeResult);
        } else {
          System.out.println("No PTID for this test -- * --" + testCaseObject.get("name"));
          writeArray.put(testcase);
        }
      }
    }
  } catch (Exception e) {

  }
}

public void getStatusAndTime(JSONObject test, JSONObject result) {
  String status = "skipped";
  String runTime = "00:00:00";
  String mins = "00";
  String seconds = "00";
  int Seconds = 0;
  int Min = 0;

  String time = (test.get("time").toString());
  int milliSeconds = Integer.parseInt(time.split("\\.")[1]);
  Seconds = Integer.parseInt(time.split("\\.")[0]);
  if (milliSeconds >= 500) {
    Seconds = Seconds + 1;
  }
  if (Seconds > 59) {
    Min = Seconds / 60;
    Seconds = Seconds % 60;
    if (Min < 10) {
      mins = 0 + Integer.toString(Min);
    } else {
      mins = Integer.toString(Min);
    }
    if (Seconds < 10) {
      seconds = "0" + Integer.toString(Seconds);
    }
  } else if (Seconds < 10) {
    seconds = "0" + Integer.toString(Seconds);
  } else {
    seconds = Integer.toString(Seconds);
  }
  runTime = "00:" + mins + ":" + seconds;

  if (test.containsKey("skipped")) {
    status = "NO RUN";
    runTime = "00:00:00";
  } else if (test.containsKey("failure")) {
    status = "FAILED";
  } else {
    status = "PASSED";
  }
  result.put("duration", runTime);
  result.put("status", status);
}

public String generatePTID(String it) {
  String ID = "";
  Matcher matcher = Pattern.compile("PTID=([0-9]+)").matcher(it);
  while (matcher.find()) {
    ID = (matcher.group(1));
    testCaseID.add(ID);
    return ID;
  }
  return null;
}

public void compareTestSetAndUpdateResult() {
  JSONObject PTObject = new JSONObject();
  String testId = " ";
 // int loopNum = 0;
  try {
    Object PTobj = parser.parse(new FileReader(System.getProperty("user.home") + "/Documents/GeneratedJsonReportOfPT.Json"));
    JSONArray PTinstancesReportObject = (JSONArray) PTobj;
    for (Object dataSet : PTinstancesReportObject) {
      JSONObject dataValues = (JSONObject) dataSet;
      PtTests.add(dataValues.get("data"));
    }
    for (Object resultTestCase : results) {
      JSONObject testValues = (JSONObject) resultTestCase;
      PractiTestRestCalls restCall = new PractiTestRestCalls();
      String systemId = restCall.getSystemId(testValues.get("PTID").toString());
label:
      for (int i = 0; i < PtTests.size(); i++) {
        JSONArray array = (JSONArray) PtTests.get(i);
        for (Object PTO : array) {
          PTObject = (JSONObject) PTO;
          if (PTObject.get("test_system_id").toString().equals(systemId)) {
            PractiTestRestCalls rest = new PractiTestRestCalls();
            rest.uploadOneTestResult(PTObject.get("system_id").toString(), testValues.get("status").toString(), testValues.get("duration").toString());
            /*loopNum++;
           if(loopNum == 20){
              loopNum =i;
              try {
                System.out.println("Waiting to pass the next set of requests ... Will wait for 55 secs");
                Thread.sleep(55000);
              } catch(InterruptedException ex) {
                Thread.currentThread().interrupt();
              }
            }*/
            break label;
          }
        }
      }
    }
  } catch (FileNotFoundException e) {
    e.printStackTrace();
  } catch (IOException e) {
    e.printStackTrace();
  } catch (ParseException e) {
    e.printStackTrace();
  }

}

public JSONArray convertJsonObjectToJsonArray(Object tests) {
  JSONArray testsArray;
  if (tests instanceof JSONArray) {
    testsArray = (JSONArray) tests;
  } else {
    testsArray = new JSONArray();
    testsArray.add((JSONObject) tests);
  }
  return testsArray;
}

}

