import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Created by kkolli on 11/1/16.
 */
public class PractiTestRestCalls {

    public static String Project_ID = "4216";
    public static String TestSet_ID ="";
    public static String TestSet_Name = "Ultra Automation Tests";
    public static String custom_field_ID_Date ="22679";
    public static String custom_field_ID_Automated = "22701";
    public static String custom_field_ID_Browser = "22702";
    public static int PRETTY_PRINT_INDENT_FACTOR = 4;
    private static final String apiToken = "custom api_token=c2dd601adf6667751ba04c2143bef307e9b430a4";


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

    public void createTestSet() {
        DateFormat df = new SimpleDateFormat("dd/MMM/yy HH:mm:ss");
        Calendar calobj = Calendar.getInstance();
        String dateAndTime = df.format(calobj.getTime());
        String date = dateAndTime.substring(0, 9);
        String testSetBody = "{\"project_id\":\"" + Project_ID + "\",\"name\":\"" + TestSet_Name + " - " + dateAndTime + "\",\"___f_" + custom_field_ID_Automated + "\": {\"value\":\"yes\"},\"___f_" + custom_field_ID_Browser + "\": {\"value\":\"Chrome\"}} ";
        try {
            BufferedWriter writer= new BufferedWriter(new FileWriter(new File(System.getProperty("user.home") +"/Documents/RejectedTestCases.json")));
            HttpResponse<JsonNode> jsonResponse = Unirest.post("https://prod.practitest.com/api/sets.json")
                    .header("Content-Type", "application/json")
                    .header("Authorization", apiToken)
                    .body(testSetBody)
                    .asJson();
            if(jsonResponse.getStatus() != 200){
                writer.write(testSetBody.toString());
                writer.newLine();
                System.out.println("Problem creating new test set in practiTest -- "+testSetBody);
            }else {
                String response = jsonResponse.getBody().toString();
                Matcher matcher = Pattern.compile("\"id\":([0-9]+)").matcher(response);
                while (matcher.find()) {
                    TestSet_ID = (matcher.group(1));
                    System.out.println("Test set ID  is -- " + TestSet_ID);
                }
            }
        }catch (UnirestException e) {
            e.printStackTrace();
        }catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void moveTestsToTestSet(){
        try{
            FileCompare file = new FileCompare();
        String testsToBeMoved = "{\"project_id\":\"" + Project_ID + "\",\"test_ids\":"+file.getTestCaseID()+"}";

            HttpResponse<JsonNode> jsonResponse = Unirest.post("https://prod.practitest.com/api/sets/"+TestSet_ID+"/add_instances.json")
                    .header("Content-Type", "application/json")
                    .header("Authorization", apiToken)
                    .body(testsToBeMoved)
                    .asJson();
            if(jsonResponse.getStatus() != 200){
                System.out.println("Problem creating new testInstance in test set of practiTest for test ID --- "+file.getTestCaseID());
            }
        }catch (UnirestException e) {
            e.printStackTrace();
        }
    }

    public String getSystemId(String testID) {
        org.json.JSONObject responseObj = new org.json.JSONObject();
        String systemId = "";
        try {
            HttpResponse<JsonNode> jsonResponse = Unirest.get(" https://prod.practitest.com/api/tests/" + testID + ".json?project_id=" + Project_ID)
                    .header("Content-Type", "application/json")
                    .header("Authorization", apiToken)
                    .asJson();
            responseObj = jsonResponse.getBody().getObject();
            systemId = responseObj.get("system_id").toString();
            if (jsonResponse.getStatus() != 200) {
                System.out.println("Problem fetching system ID from practiTest");
            }
        } catch (UnirestException e) {
            e.printStackTrace();
        }
        return systemId;
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

