/**
 * Created by kkolli on 10/27/16.
 */
public class CallAPI {

    private static final String URI = "https://prod.practitest.com/api/automated_tests/upload_test_result.json";



    public static String getURI() {
        return URI;
    }

    public static void main(String[] args) {

        int fileCount;

        ReportGenerate report = new ReportGenerate();
        fileCount = report.generateJsonReport();
        PractiTestRestCalls restCall = new PractiTestRestCalls();
        restCall.generateReportFromPractiTest();
        FileCompare comp = new FileCompare();
        comp.compare(fileCount);
       // comp.generateTestNameAndTag();
        restCall.createTestInstance();
        restCall.moveTestsToTestSet();
        // restCall.uploadMultiTestresults();
       // restCall.createTestInstance();

    }


}
