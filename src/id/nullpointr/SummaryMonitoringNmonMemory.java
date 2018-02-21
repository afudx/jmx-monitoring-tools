package id.nullpointr;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SummaryMonitoringNmonMemory {
	
	int cpu=0;
	String normarPath;//=System.getProperty("user.dir")+"nmon"+File.separator;
	String fileName="";
	String configServer="";
	File fileOutput;
	boolean header=true;
	Long gigaByte=(long) 1;
	String serverName="";
	
	public SummaryMonitoringNmonMemory()
	{
		System.out.println("=========STARTING SUMMARY CPU /MEMORY USED ON TRANSACTION=======");
		BufferedReader br = null;
		
		try {
			 br = new BufferedReader(new InputStreamReader(System.in));

			normarPath=System.getProperty("user.dir")+File.separator+"nmon"+File.separator;
			System.out.println("BASE PATH :"+normarPath);
			
			
			System.out.print(" Summary Cpu(1) / Memory(2): ");
			String cpuString=br.readLine();
			System.out.print(" FileName source: ");
			fileName=br.readLine();
			System.out.print(" Config Transaction: ");
			configServer=br.readLine();
			System.out.print(" ServerName: ");
			serverName=br.readLine();
			
			System.out.println("##Starting Summary");
			if(cpuString.equals("1")) {
				fileOutput=new File(normarPath+serverName+"-cpu-summary.csv");
				fileOutput.createNewFile();
				System.out.println("## File "+fileOutput.getAbsolutePath()+"Is Created");
				summaryCPU(fileName,configServer);
			}
			
		}catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		
	}
	SimpleDateFormat sdf=new SimpleDateFormat("dd-MM-yyyyHH:mm:ss");
	public void summaryCPU(String fileName,String configServer) throws Exception{
		
		BufferedReader bufferredReadder = null;
		FileReader fileReader = null;

		
		try {
			System.out.println(""+normarPath+configServer);
			File file = new File(normarPath+configServer);
			fileReader = new FileReader(file.getAbsolutePath());
			bufferredReadder = new BufferedReader(fileReader);
			String sCurrentLine;
			

			while ((sCurrentLine = bufferredReadder.readLine()) != null) {
				String configData[] = sCurrentLine.split(",");
				String startTime=configData[1].trim().length()==8 ? configData[1]: "0"+configData[1];
				String endtime=configData[2].trim().length()==8 ? configData[2].trim(): "0"+configData[1].trim();
				Date dstartDate=sdf.parse(configData[0].trim()+ startTime);
				Date dendTime=sdf.parse(configData[0].trim()+ endtime);
				if(serverName.equalsIgnoreCase(configData[3])) {
					summaryCountCPU(configData[4],configData[0],dstartDate,dendTime,fileName);	
				}
			}
			
		}catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			if(bufferredReadder!=null) {
				try {
				bufferredReadder.close();}catch (Exception e) {
					// TODO: handle exception
				}
			}
		}
		
		
		}
		
	
	public void summaryCountCPU(String moduleName,String date,Date startDate, Date endTime, String fileSource) {
		
		BufferedReader bufferredReadder = null;
		FileReader fileReader = null;
		
		try {
			File file = new File(normarPath+fileSource);
			fileReader = new FileReader(file.getAbsolutePath());
			bufferredReadder = new BufferedReader(fileReader);
			String sCurrentLine;
			boolean firstLine=true;
			boolean exit=true;
			double avg=0L;
			double max=0L;
			double min=999999999999999L;
			Long count=0L;
			
			
			while ((sCurrentLine = bufferredReadder.readLine()) != null && exit) {
				String datas[] = sCurrentLine.split(",");
				if(firstLine) {
					firstLine=false;
				}else{
					Date dateData =sdf.parse(date+((datas[0]).length()==8 ? datas[0]:"0"+datas[0]));
					
					if(startDate.before(dateData)) {
						if(endTime.after(dateData)) {
							double used=Double.valueOf(datas[9]);
							count++;
							avg+=used;
							max=(max<used) ? used:max;
							min=(min>used) ? used:min;
						}else {
							exit=false;
						}
					}
					
				}
			}
			appendFile(moduleName,count,min,avg,max);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			if(bufferredReadder!=null) {
				try {
				bufferredReadder.close();}catch (Exception e) {
					// TODO: handle exception
				}
			}
		}
		
		
	}
	
	public void appendFile(String module,long count,double min,double avg,double max) {
		BufferedWriter writer = null;
	try {
		writer = new BufferedWriter(new FileWriter(fileOutput.getAbsolutePath(),true));
		if(header) {
			writer.append("Name Node,COUNT,MIN(%),AVG(%),MAX(%)");
			writer.newLine();
			header=false;
		}
		if(count>0) {
			BigDecimal b_count=new BigDecimal(""+count);
			BigDecimal b_gb=new BigDecimal(""+gigaByte);
			BigDecimal b_min=new BigDecimal(""+min).divide(b_gb,MathContext.DECIMAL128).setScale(4, RoundingMode.HALF_UP);
			BigDecimal b_avg=new BigDecimal(""+avg).divide(b_count,MathContext.DECIMAL128);
			b_avg=b_avg.divide(b_gb,MathContext.DECIMAL128).setScale(4, RoundingMode.HALF_UP);
			BigDecimal b_max=new BigDecimal(""+max).divide(b_gb,MathContext.DECIMAL128).setScale(4, RoundingMode.HALF_UP);
			StringBuffer summaryAvg = new StringBuffer();
			summaryAvg.append(module);
			summaryAvg.append(",");
			summaryAvg.append(count);
			summaryAvg.append(",");
			summaryAvg.append(b_min.toString());
			summaryAvg.append(",");
			summaryAvg.append(b_avg.toString());
			summaryAvg.append(",");
			summaryAvg.append(b_max.toString());
			writer.append(summaryAvg.toString());
			writer.newLine();
			writer.flush();
			System.out.println("Summary Transaction Modul "+ module+ " Is Finish");
		}
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
