import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Created by kkolli on 11/3/16.
 */
public class FileCompare {
    JSONParser parser = new JSONParser();
    JSONArray  writeArray = new JSONArray();
    JSONObject currentFrameWorkObject = new JSONObject();
    JSONArray PtTests = new JSONArray();
    FileWriter file;
    String fileName;
    List<String> tags;

    public void compare(int fileCount) {

        JSONArray testCasesInNestedSuite = new JSONArray();
        JSONObject testSuite = new JSONObject();
        JSONArray frameWorkArray = new JSONArray();

        try {
            Object AutomationResultObj = parser.parse(new FileReader("/Users/kkolli/Desktop/GeneratedGroupJsonReport.Json"));
            JSONObject automationReportObject = (JSONObject) AutomationResultObj;

            Object PTobj = parser.parse(new FileReader("/Users/kkolli/Desktop/GeneratedJsonReportOfPT.Json"));
            JSONObject PTinstancesReportObject = (JSONObject) PTobj;
            PtTests = (JSONArray) PTinstancesReportObject.get("data");
            file = new FileWriter("/Users/kkolli/Desktop/FileC.Json");

            if(fileCount > 1){
                frameWorkArray = (JSONArray) automationReportObject.get("testsuites");
            }else {
                JSONObject testsuites = (JSONObject) automationReportObject.get("testsuites");
                frameWorkArray = (JSONArray) testsuites.get("testsuite");
            }

            for (Object frameWorkObj : frameWorkArray) {
                currentFrameWorkObject = (JSONObject) frameWorkObj;
                System.out.println("Current framework object is  :" + currentFrameWorkObject);
                if (fileCount > 1) {
                    if (currentFrameWorkObject.get("testsuite") instanceof JSONArray) {
                        testCasesInNestedSuite = (JSONArray) currentFrameWorkObject.get("testsuite");
                        for (Object innerTestSuite : testCasesInNestedSuite) {
                            testSuite = (JSONObject) innerTestSuite;
                            currentFrameWorkObject = testSuite;
                            readingTestCases();
                        }
                    } else {
                        currentFrameWorkObject = (JSONObject) currentFrameWorkObject.get("testsuite");
                        readingTestCases();
                    }
                }else {
                    readingTestCases();
                }
            }
            file.write(writeArray.toJSONString());
            file.flush();
            file.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        }

        public void readingTestCases() {
            boolean match = false;
            String message = "No message given";
            JSONArray testCases = new JSONArray();
            JSONObject testCaseObject = new JSONObject();
            JSONObject PTObject = new JSONObject();
            String status = "skipped";

            try {
                testCases = (JSONArray) convertJsonObjectToJsonArray(currentFrameWorkObject.get("testcase"));
                for (Object testcase : testCases) {
                    testCaseObject = (JSONObject) testcase;
                    fileName = generateTestNameAndTag(testCaseObject.get("classname").toString(),testCaseObject.get("name").toString());
                    System.out.println("file name is ----"+fileName+" and tag is---"+tags);
                    if (testCaseObject.containsKey("skipped")) {
                        status = "NO RUN";
                    } else if (testCaseObject.containsKey("failure")) {
                        status = "FAILED";
                        JSONObject failure = (JSONObject) testCaseObject.get("failure");
                        message = failure.get("message").toString();
                    } else {
                        status = "PASSED";
                    }
                    for (Object PTO : PtTests) {
                        PTObject = (JSONObject) PTO;
                        if (fileName.equalsIgnoreCase(PTObject.get("name").toString())) {
                            match = true;
                            PractiTestRestCalls rest = new PractiTestRestCalls();
                            rest.uploadOneTestresults(PTObject.get("system_id").toString(), status, message);
                            break;
                        }
                    }
                    if (!match) {
                        JSONObject writeObject = new JSONObject();
                        writeObject.put("name", fileName);
                        writeObject.put("status", status);
                        writeObject.put("message", message);
                        writeObject.put("tags",tags);
                        writeArray.add(writeObject);
                    }
                }
            }catch(Exception e){

            }
        }


        public JSONArray convertJsonObjectToJsonArray(Object tests) {
            JSONArray testsArray;
            if(tests instanceof JSONArray) {
              testsArray = (JSONArray) tests;
            } else {
                testsArray = new JSONArray();
                testsArray.add((JSONObject) tests);
            }
                return testsArray;
        }

        public String generateTestNameAndTag(String describe, String it){
            String describeWithoutTag = describe.replaceFirst("\\(.*","");
            String itWithoutTag = it.replaceFirst("\\(.*","");
            String fileName = describeWithoutTag.concat(" "+itWithoutTag);
            tags = new ArrayList<String>();
            Matcher matcher = Pattern.compile("#(\\w+-?(\\w+)?)").matcher(describe+it);
            while (matcher.find()) {
                tags.add(matcher.group(1));
            }
            return(fileName);
        }
    }

