package com;

public class UpdatePractiTestDashBoard {

public static void main(String[] args) {
  int fileCount;
  //String fileLocation = "/Users/kkolli/desktop/results";
  String fileLocation = args[0].toString();
  String testSetName;
  if(args.length > 1){
    testSetName = args[1].toString();
  }else{
    testSetName = "noName";
  }
  ReportGenerate report = new ReportGenerate();
  fileCount = report.generateJsonReport(fileLocation);
  FileCompare comp = new FileCompare();
  comp.getTestCases(fileCount);
  PractiTestRestCalls restCall = new PractiTestRestCalls();
    restCall.createTestSet(testSetName);
  restCall.moveTestsToTestSet();
  restCall.generateReportFromPractiTest();
  comp.compareTestSetAndUpdateResult();

}
}
