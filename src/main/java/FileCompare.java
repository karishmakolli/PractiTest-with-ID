import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
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
    String testName;
    List<String> tags;
    boolean createTest = false;

    public boolean compare(int fileCount) {

        JSONArray testCasesInNestedSuite = new JSONArray();
        JSONObject testSuite = new JSONObject();
        JSONArray frameWorkArray = new JSONArray();

        try {
            Object AutomationResultObj = parser.parse(new FileReader(System.getProperty("user.home") +"/Documents/GeneratedGroupJsonReport.Json"));
            JSONObject automationReportObject = (JSONObject) AutomationResultObj;

            Object PTobj = parser.parse(new FileReader(System.getProperty("user.home") +"/Documents/GeneratedJsonReportOfPT.Json"));
            JSONArray PTinstancesReportObject = (JSONArray) PTobj;
            for(Object dataSet : PTinstancesReportObject){
                JSONObject dataValues = (JSONObject) dataSet;
                PtTests.add(dataValues.get("data"));
            }
            file = new FileWriter(System.getProperty("user.home") +"/Documents/TestsToBeCreated.Json");

            if(fileCount > 1){
                frameWorkArray = (JSONArray) automationReportObject.get("testsuites");
            }else {
                JSONObject testsuites = (JSONObject) automationReportObject.get("testsuites");
                frameWorkArray = (JSONArray) testsuites.get("testsuite");
            }

            for (Object frameWorkObj : frameWorkArray) {
                currentFrameWorkObject = (JSONObject) frameWorkObj;
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
        return createTest;
        }

        public void readingTestCases() {
            boolean match = false;
            JSONArray testCases = new JSONArray();
            JSONObject testCaseObject = new JSONObject();
            JSONArray currentArray = new JSONArray();
            JSONObject PTObject = new JSONObject();
            String status = "skipped";
            String runTime = "00:00:00";
            String mins ="00";

            try {
                testCases = (JSONArray) convertJsonObjectToJsonArray(currentFrameWorkObject.get("testcase"));
                for (Object testcase : testCases) {
                    testCaseObject = (JSONObject) testcase;
                    testName = generateTestNameAndTag(testCaseObject.get("classname").toString(), testCaseObject.get("name").toString());
                    BigDecimal bd = new BigDecimal(testCaseObject.get("time").toString());
                    boolean Mins = false;
                    String time = (testCaseObject.get("time").toString());
                    int Seconds = Integer.parseInt(time.split("\\.")[0]);
                    if (Seconds > 59) {
                        int Min = Seconds / 60;
                        if (Min < 9) {
                            mins = 0 + Integer.toString(Min);
                        } else {
                            mins = Integer.toString(Min);
                        }
                        String totalTime = mins.concat("." + time.split("\\.")[1]);
                        bd = new BigDecimal(totalTime);
                        Mins = true;
                    }
                    BigDecimal roundOff = bd.setScale(1, BigDecimal.ROUND_HALF_EVEN);
                    String duration = roundOff.toString();
                    runTime = duration.replaceAll("\\.", ":0");
                    if (!Mins) {
                        runTime = "00:" + runTime;
                    } else {
                        runTime = runTime + ":00";
                    }
                    if (testCaseObject.containsKey("skipped")) {
                        status = "NO RUN";
                        runTime = "00:00:00";
                    } else if (testCaseObject.containsKey("failure")) {
                        status = "FAILED";
                    } else {
                        status = "PASSED";
                    }
                    label:
                    for (int i = 0; i < PtTests.size(); i++) {
                        JSONArray array = (JSONArray) PtTests.get(i);
                        for (Object PTO : array) {
                            PTObject = (JSONObject) PTO;
                            if (testName.equalsIgnoreCase(PTObject.get("name").toString())) {
                                match = true;
                                PractiTestRestCalls rest = new PractiTestRestCalls();
                                rest.uploadOneTestresults(PTObject.get("system_id").toString(), status, runTime);
                                break label;
                            }
                        }
                    }
                            if (!match) {
                                JSONObject writeObject = new JSONObject();
                                writeObject.put("name", testName);
                                writeObject.put("status", status);
                                writeObject.put("duration", runTime);
                                writeObject.put("tags", tags);
                                writeArray.add(writeObject);
                                createTest = true;
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
            Matcher matcher = Pattern.compile("#([A-Za-z-]+)").matcher(describe+it);
            while (matcher.find()) {
                tags.add(matcher.group(1));
            }
            return(fileName);
        }
    }

