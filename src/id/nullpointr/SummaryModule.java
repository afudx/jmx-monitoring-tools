package id.nullpointr;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SummaryModule {
	
	public static void main(String[] args) {
		SummaryModule sm = new SummaryModule();
		sm.proceedSummary("/data/tmp/jmx-data-test/input-module.csv","/data/tmp/jmx-data-test/result-file");
	}
	
	
	public void proceedSummary(String processData, String inputSummaryFile) {
		
		SummaryMonitoring summaryMonitoring = new SummaryMonitoring();
		DateFormat oldDateFormat = new SimpleDateFormat("dd-MM-yyyy");
		DateFormat newDateFormat = new SimpleDateFormat("yyyyMMdd");
		DateFormat finaleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
		
		File directory=new File(inputSummaryFile);
		
		if(!directory.isDirectory()) {
			throw new RuntimeException("Not a directory");
		}
		
		File[] files = directory.listFiles();
		
		try{
			BufferedReader br = new BufferedReader(new FileReader(processData));
			String sCurrentLine;

			while ((sCurrentLine = br.readLine()) != null) {
				String data[] = sCurrentLine.split(",");
				Date oldDate = null;
				
				try {
					oldDate = oldDateFormat.parse(data[0]);
				} catch (ParseException e) {
					System.err.println("Gagal parsing date");
					e.printStackTrace();
				}
				
				String newDate = newDateFormat.format(oldDate);
				String startTime = data[1].replaceAll(":", "");
				String endTime = data[2].replaceAll(":", "");
				String finalDateStart = newDate+startTime;
				String finalDateEnd = newDate+endTime;
				String hostname = data[3];
				String processName = data[4];
				Date dateStart = null;
				Date dateEnd = null;
				
				try {
					dateStart = finaleDateFormat.parse(finalDateStart);
					dateEnd = finaleDateFormat.parse(finalDateEnd);
				} catch (Exception e) {
					System.err.println("Failed parsing date"); 
				}
				
				String fileNameAdditional = hostname+processName.replace(" ", "");
				//System.out.println(inputSummaryFile+" "+fileNameAdditional+" "+finalDateStart+" "+finalDateEnd);
				try {
					summaryMonitoring.summary(inputSummaryFile, fileNameAdditional, finalDateStart, finalDateEnd);
				} catch (Exception e) {
					System.err.println("Failed creating file summary.");
					e.printStackTrace();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
