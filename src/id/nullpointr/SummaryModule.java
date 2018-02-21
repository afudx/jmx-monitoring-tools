package id.nullpointr;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.xml.stream.events.StartDocument;

public class SummaryModule {
	
	public static void main(String[] args) {
		SummaryModule sm = new SummaryModule();
		sm.proceedSummary("/data/tmp/jmx-data-test/input-module.csv","/data/WORK/monitoring-tools/data21FEB18sore");
	}
	
	
	public void proceedSummary(String processData, String inputSummaryFileDirectory) {
		
		SummaryMonitoring summaryMonitoring = new SummaryMonitoring();
		DateFormat oldDateFormat = new SimpleDateFormat("dd-MM-yyyy");
		DateFormat newDateFormat = new SimpleDateFormat("yyyyMMdd");
		DateFormat finaleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
		
		try{
			BufferedReader bufferedReader = new BufferedReader(new FileReader(processData));
			String sCurrentLine;

			while ((sCurrentLine = bufferedReader.readLine()) != null) {
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
				String nodeName = data[5];
				Date dateStart = null;
				Date dateEnd = null;
				
				try {
					dateStart = finaleDateFormat.parse(finalDateStart);
					dateEnd = finaleDateFormat.parse(finalDateEnd);
				} catch (Exception e) {
					System.err.println("Failed parsing date"); 
				}
				
				try {
					File sourceDirectoryFile = new File(inputSummaryFileDirectory);
					
					if(!sourceDirectoryFile.isDirectory()) {
						throw new RuntimeException("Not a directory");
					}
					
					File[] filesInDirectory = sourceDirectoryFile.listFiles();
					
					for (File file : filesInDirectory) {
						if(file.getName().toLowerCase().contains("SumData".toLowerCase())) {
							continue;
						}
						
						if (file.getName().toLowerCase().contains(hostname.toLowerCase()) && file.getName().toLowerCase().contains(nodeName.toLowerCase())) {
							String finalSummaryFileName = "SumData-" +hostname + "-" + processName.replaceAll("\\s", "") + "-" + newDate + "-"+ startTime + "-" + endTime +".csv";
							File finalSummaryFile = new File(inputSummaryFileDirectory+File.separator+finalSummaryFileName);
							finalSummaryFile.createNewFile();
							
							summaryMonitoring.summaryFromFile(file, finalSummaryFile, dateStart, dateEnd);
						}
						
					}
					
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
