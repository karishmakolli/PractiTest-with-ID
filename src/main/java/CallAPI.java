/**
 * Created by kkolli on 10/27/16.
 */
public class CallAPI {

    public static void main(String[] args) {

       int fileCount;
        //String fileLocation = "/Users/kkolli/desktop/results";
        String fileLocation = args[0].toString();
        ReportGenerate report = new ReportGenerate();
        fileCount = report.generateJsonReport(fileLocation);
        FileCompare comp = new FileCompare();
        comp.getTestCases(fileCount);
        PractiTestRestCalls restCall = new PractiTestRestCalls();
        restCall.createTestSet();
        restCall.moveTestsToTestSet();
        restCall.generateReportFromPractiTest();
        comp.compareTestSetAndUpdateResult();

    }

    }
