import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;


/**
 * Created by kkolli on 11/1/16.
 */
public class PractiTestRestCalls extends CallAPI {

    public static String Project_ID = "4051";
    public static String Author_ID = "8834";
    public static String TestSet_ID ="12";
    public static String custom_field_ID_Tag = "22353";
    public static String custom_field_ID_Automated = "22354";
    public static int PRETTY_PRINT_INDENT_FACTOR = 4;
    private static final String apiToken = "custom api_token=c2dd601adf6667751ba04c2143bef307e9b430a4";
    JSONArray displayId = new JSONArray();

    public void uploadOneTestresults(String instanceId, String status,String duration) {
        String jsonInString = "{\"project_id\":\"" + Project_ID + "\",\"instance_id\":\"" + instanceId + "\",\"run_duration\":\""+duration+"\",\"steps\":[{\"status\":\""+status+"\"}]}";
        try {
            HttpResponse<JsonNode> jsonResponse = Unirest.post("https://prod.practitest.com/api/automated_tests/upload_test_result.json")
                    .header("Content-Type", "application/json")
                    .header("Authorization", apiToken)
                    .body(jsonInString)
                    .asJson();
            if(jsonResponse.getStatus() != 200){
                System.out.println("Problem uploading result in practiTest for instance-ID --- "+instanceId+jsonResponse.getBody().toString());
            }
        } catch (UnirestException e) {
            e.printStackTrace();
        }
    }


    public void createTestInstance() {

        try {
            JSONParser parser = new JSONParser();
            JSONObject testCase;
            BufferedWriter writer= new BufferedWriter(new FileWriter(new File(System.getProperty("user.home") +"/Documents/RejectedTestCases.json")));
            Object newTests = parser.parse(new FileReader(System.getProperty("user.home") +"/Documents/TestsToBeCreated.Json"));
            FileCompare compare = new FileCompare();
            JSONArray newTestToBeCreated = compare.convertJsonObjectToJsonArray(newTests);
            for (Object newtestCase : newTestToBeCreated) {
                testCase = (JSONObject) newtestCase;
                String testCaseName = testCase.get("name").toString();
                if(testCaseName.contains("\"")){
                    testCaseName = testCaseName.replaceAll("\"","\\\\\"");
                }
                JSONArray tags = compare.convertJsonObjectToJsonArray(testCase.get("tags"));
                String testCaseBody = "{\"project_id\":\"" + Project_ID + "\",\"author_id\":\"" + Author_ID + "\",\"name\":\"" + testCaseName + "\",\"___f_" + custom_field_ID_Tag + "\": {\"value\":" + testCase.get("tags") + "},\"___f_" + custom_field_ID_Automated + "\": {\"value\":\"true\"},\"duration_estimate\":\""+testCase.get("duration")+"\",\"run_status\":{\"value\":\"" + testCase.get("status") + "\"}}";
                if (tags.size() >1 || !tags.contains("guardrails")&&tags.size()>0) {
                        HttpResponse<JsonNode> jsonResponse = Unirest.post("https://prod.practitest.com/api/tests.json")
                                .header("Content-Type", "application/json")
                                .header("Authorization", apiToken)
                                .body(testCaseBody)
                                .asJson();
                        displayId.add(jsonResponse.getBody().getObject());
                    System.out.println("Json response is "+jsonResponse.getStatus()+"test acse body is "+testCaseBody);
                    if(jsonResponse.getStatus() != 200){
                        writer.write(testCase.toString());
                        writer.newLine();
                        System.out.println("Problem creating new testCase in practiTest for testCase -- "+testCase.get("name"));
                    }
                } else{
                    writer.write(testCase.toJSONString());
                    writer.newLine();
                }
            }
            writer.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnirestException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }catch (ParseException e) {
            e.printStackTrace();
        }
        moveTestsToTestSet();

    }

    public void moveTestsToTestSet(){
        try{
        JSONArray testInstances = new JSONArray();
            org.json.JSONObject testResponse = new org.json.JSONObject();
        for (Object testCase : displayId) {
            testResponse = (org.json.JSONObject) testCase;
            if (testResponse.has("display_id")) {
                testInstances.add(testResponse.get("display_id"));
            }
        }
            System.out.println("Created test cases Id's --- "+testInstances);
        String testsToBeMoved = "{\"project_id\":\"" + Project_ID + "\",\"test_ids\":"+testInstances+"}";

            HttpResponse<JsonNode> jsonResponse = Unirest.post("https://prod.practitest.com/api/sets/"+TestSet_ID+"/add_instances.json")
                    .header("Content-Type", "application/json")
                    .header("Authorization", apiToken)
                    .body(testsToBeMoved)
                    .asJson();
            if(jsonResponse.getStatus() != 200){
                System.out.println("Problem creating new testInstance in test set of practiTest for test ID --- "+testInstances);
            }
        }catch (UnirestException e) {
            e.printStackTrace();
        }
    }

    public void generateReportFromPractiTest() {
        boolean instancesExist = true;
        int pageNum = 1;
        org.json.JSONObject responseObj = new org.json.JSONObject();
        org.json.JSONArray responseArray = new org.json.JSONArray();
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(new File(System.getProperty("user.home") + "/Documents/GeneratedJsonReportOfPT.Json")));
            while(instancesExist) {
                HttpResponse<JsonNode> jsonResponse = Unirest.get("https://prod.practitest.com/api/sets/" + TestSet_ID + "/instances.json?project_id=" + Project_ID + "/limit=250&page="+pageNum)
                        .header("Content-Type", "application/json")
                        .header("Authorization", apiToken)
                        .asJson();
                responseObj = jsonResponse.getBody().getObject();
                responseArray.put(responseObj);
                System.out.println("Lenght of practiTest results --- " +pageNum+ "-*--"+ responseObj.getJSONArray("data").length());
                if(responseObj.getJSONArray("data").length()==0){
                    instancesExist = false;
                }
                if (jsonResponse.getStatus() != 200) {
                    System.out.println("Problem fetching data from practiTest test set");
                }
                pageNum++;
            }
            for (int i = 0; i < responseArray.toString().split(",").length; i++) {
                writer.write(responseArray.toString(PRETTY_PRINT_INDENT_FACTOR).split(",")[i]);
            }
            writer.close();
        } catch (UnirestException e) {
            e.printStackTrace();
        } catch (java.io.IOException io) {
            io.printStackTrace();
        }

    }
}

