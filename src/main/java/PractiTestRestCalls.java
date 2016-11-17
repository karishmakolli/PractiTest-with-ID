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

    public static String Project_ID = "3878";
    public static String Author_ID = "8806";
    public static String TestSet_ID ="159892";
    public static int PRETTY_PRINT_INDENT_FACTOR = 4;
    private static final String apiToken = "custom api_token=c2dd601adf6667751ba04c2143bef307e9b430a4";
    JSONArray displayId = new JSONArray();

    public void uploadOneTestresults(String instanceId, String status, String message) {
        String jsonInString = "{\"project_id\":\"" + Project_ID + "\",\"instance_id\":\"" + instanceId + "\",\"instance_custom_fields\":{\"name\":\"browser\",\"value\":\"Chrome\"},\"steps\":[{\"status\":\"" + status + "\",\"actual_results\":\"" + message +"\"}]}";
        try {
            HttpResponse<JsonNode> jsonResponse = Unirest.post("https://prod.practitest.com/api/automated_tests/upload_test_result.json")
                    .header("Content-Type", "application/json")
                    .header("Authorization", apiToken)
                    .body(jsonInString)
                    .asJson();
            System.out.println("Entered the try block and respons eis :" + jsonResponse.getStatusText());
        } catch (UnirestException e) {
            e.printStackTrace();
        }
    }

    public void uploadMultiTestresults() {

        JSONParser parser = new JSONParser();
        JSONArray missingTestsArray = new JSONArray();
        JSONObject test = new JSONObject();
        String jsonTestInString;

        try {
            missingTestsArray = (JSONArray) parser.parse(new FileReader("/Users/kkolli/Desktop/Results.Json"));
            System.out.println(missingTestsArray.size());
            System.out.println("Object contains -- " + missingTestsArray);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        for (Object testCase : missingTestsArray) {
            test = (JSONObject) testCase;
            jsonTestInString = test.toJSONString();
            try {
                HttpResponse<JsonNode> jsonResponse = Unirest.post(getURI())
                        .header("Content-Type", "application/json")
                        .header("Authorization", apiToken)
                        .body(jsonTestInString)
                        .asJson();
            } catch (UnirestException e) {
                e.printStackTrace();
            }
        }
    }

    public void createTestInstance() {

        try {
            JSONParser parser = new JSONParser();
            JSONObject testCase;
            Object newTests = parser.parse(new FileReader("/Users/kkolli/Desktop/FileC.Json"));
            FileCompare compare = new FileCompare();
            JSONArray newTestToBeCreated = compare.convertJsonObjectToJsonArray(newTests);
            System.out.println("array is .. "+newTestToBeCreated);
            System.out.println("The size of array is .. "+newTestToBeCreated.size());
            for (Object newtestCase : newTestToBeCreated) {
                testCase = (JSONObject) newtestCase;
                String testCaseBody = "{\"project_id\":\"" + Project_ID + "\",\"author_id\":\"" + Author_ID +"\",\"name\":\""+testCase.get("name")+"\",\"run_status\":{\"value\":\""+testCase.get("status")+"\"}}";
                System.out.println("test case body is : -----"+testCaseBody);
            HttpResponse<JsonNode> jsonResponse = Unirest.post("https://prod.practitest.com/api/tests.json")
                    .header("Content-Type", "application/json")
                    .header("Authorization", apiToken)
                    .body(testCaseBody)
                    .asJson();
                System.out.println("Entered the try block and response is :" + jsonResponse.getBody().getObject());
                displayId.add(jsonResponse.getBody().getObject());
        }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnirestException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }catch (ParseException e) {
            e.printStackTrace();
        }
        System.out.println("the display Id contains : "+displayId);

    }

    public void moveTestsToTestSet(){
        JSONArray testInstances = new JSONArray();
        JSONObject testResponse = new JSONObject();
        for(Object testResponses: displayId){
            testResponse = (JSONObject) testResponses;
            System.out.println("Json object for testInstance is : "+testResponse);
            testInstances.add(testResponse.get("display_id"));
        }
        System.out.println("Json Array for testInstances is : ----- "+testInstances);
        String testsToBeMoved = "{\"test_ids\":"+testInstances+"}";
        try {
            HttpResponse<JsonNode> jsonResponse = Unirest.post("https://prod.practitest.com/sets/"+TestSet_ID+"/add_instances.json")
                    .header("Content-Type", "application/json")
                    .header("Authorization", apiToken)
                    .body(testsToBeMoved)
                    .asJson();
        }catch (UnirestException e) {
            e.printStackTrace();
        }

    }

    public void generateReportFromPractiTest() {
        try {
            HttpResponse<JsonNode> jsonResponse = Unirest.get("https://prod.practitest.com/api/sets/16/instances.json")
                    .header("Content-Type", "application/json")
                    .header("Authorization", apiToken)
                    .asJson();
           org.json.JSONObject responseObj = jsonResponse.getBody().getObject();
            BufferedWriter writer = new BufferedWriter(new FileWriter(new File("/Users/kkolli/Desktop/GeneratedJsonReportOfPT.Json")));
            for (int i = 0; i < responseObj.toString().split(",").length; i++) {
                writer.write(responseObj.toString(PRETTY_PRINT_INDENT_FACTOR).split(",")[i]);
            }
            writer.close();
        } catch (UnirestException e) {
            e.printStackTrace();
        } catch (java.io.IOException io) {
            io.printStackTrace();
        }

    }
}

