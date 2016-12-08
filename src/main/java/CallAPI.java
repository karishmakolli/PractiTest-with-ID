/**
 * Created by kkolli on 10/27/16.
 */
public class CallAPI {

    public static void main(String[] args) {

      int fileCount;
       String fileLocation = args[0].toString();
        //String fileLocation = "/Users/kkolli/desktop/results";
        ReportGenerate report = new ReportGenerate();
        fileCount = report.generateJsonReport(fileLocation);
        PractiTestRestCalls restCall = new PractiTestRestCalls();
        restCall.generateReportFromPractiTest();
        FileCompare comp = new FileCompare();
        boolean newTest = comp.compare(fileCount);
        if(newTest){
            restCall.createTestInstance();
        }
        System.out.println("just testing git");

    }

    }
