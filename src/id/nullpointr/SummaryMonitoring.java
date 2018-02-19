package id.nullpointr;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.sun.org.apache.xml.internal.utils.URI;

public class SummaryMonitoring {
	String fileName="SumMon_btw_pfrom_and_pend.csv";
	File fileSummary =null;
	DateFormat dateFormat=new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
	DateFormat dateFormat2=new SimpleDateFormat("yyyyMMddHHmmss");
	boolean header=true;
	Long gigaByte=(long) (1024*1024*1024);
	void summary(String path,String from,String end) throws Exception{
		
		fileName=fileName.replace("pfrom", from);
		fileName=fileName.replace("pend", end);
		fileName=path+File.separator+fileName;
		Date dfrom=dateFormat2.parse(from);
		Date dend=dateFormat2.parse(end);
		fileSummary=new File(fileName);
		fileSummary.createNewFile();
		System.out.println(fileName);
		File directory=new File(path);
		if(!directory.isDirectory()) {
			System.out.println("Path Is not Directory");
			return;
		}
		File[] files=directory.listFiles();
		for(File file: files) {
			if(!file.getName().contains("SumMon_btw"))
				summaryFromFile(file,dfrom,dend);
		}
	}
	public void summaryFromFile(File file, Date from, Date end) throws Exception {
		System.out.println("Starting Summary....");
		FileReader fileReader = null;
		BufferedReader bufferredReadder = null;
		Long avg=0L;
		Long max=0L;
		Long min=999999999999999L;
		Long count=0L;
		try {
		fileReader = new FileReader(file);
		bufferredReadder = new BufferedReader(fileReader);
		String sCurrentLine;
		boolean firstLine=true;
		boolean exit=true;
		
		System.out.println("Summary File : "+file.getName());
		
		while ((sCurrentLine = bufferredReadder.readLine()) != null && exit) {
			if(firstLine) {
				firstLine=false;
			}else{
				String[] datas=sCurrentLine.split(",");
				Date dateData =dateFormat.parse(datas[0]);
				if(from.before(dateData)) {
					if(end.after(dateData)) {
						Long used=Long.valueOf(datas[3]);
						count++;
						avg+=used;
						max=(max<used) ? used:max;
						min=(min>used) ? used:min;
					}else {
						System.out.println("exit file : "+file.getName());
						exit=false;
					}
				}
			}
			
			
		}
		}catch (Exception e) {
			e.printStackTrace();
		}finally {
			if(bufferredReadder!=null) {
				bufferredReadder.close();
			}
		}
		
		appendFile(file.getName(),count,min,avg,max);
		
	}
	public void appendFile(String fileName,Long count,Long min,Long avg,Long max) {
			BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new FileWriter(fileSummary.getAbsolutePath(),true));
			if(header) {
				writer.append("Name Node,COUNT,MIN(GB),AVG(GB),MAX(GB)");
				writer.newLine();
				header=false;
			}
			
			
			StringBuffer summaryAvg = new StringBuffer();
			summaryAvg.append(fileName);
			summaryAvg.append(",");
			summaryAvg.append(count);
			summaryAvg.append(",");
			summaryAvg.append((min/gigaByte));
			summaryAvg.append(",");
			summaryAvg.append(((avg/count)/gigaByte));
			summaryAvg.append(",");
			summaryAvg.append((max/gigaByte));
			writer.append(summaryAvg.toString());
			writer.newLine();
			writer.flush();
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
			
			try {
				if(writer!=null) {
					writer.close();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
