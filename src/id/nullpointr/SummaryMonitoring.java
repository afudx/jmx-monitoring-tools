package id.nullpointr;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SummaryMonitoring {
	private File outputFileSummary = null;
	private DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
	private DateFormat dateFormat2 = new SimpleDateFormat("yyyyMMddHHmmss");
	private boolean header = true;
	private Long gigaByte = (long) (1024 * 1024 * 1024);
	private DecimalFormat df = new DecimalFormat("#.####");

	public void summary(String inputPath, String fileNameAdditional, String startTime, String endTime) throws Exception {
		String baseFileName = "SumMon_btw_pfrom_and_pend.csv";
		
		baseFileName = baseFileName.replace("pfrom", startTime);
		baseFileName = baseFileName.replace("pend", endTime);
		
		if(fileNameAdditional.equalsIgnoreCase(""))
			baseFileName = inputPath + File.separator + baseFileName;
		else
			baseFileName = inputPath + File.separator + fileNameAdditional + baseFileName;
			
		Date dfrom = dateFormat2.parse(startTime);
		Date dend = dateFormat2.parse(endTime);
		
		System.out.println(baseFileName);
		
		outputFileSummary = new File(baseFileName);
		outputFileSummary.createNewFile();
		
		File inputDirectory = new File(inputPath);
		
		if (!inputDirectory.isDirectory()) {
			System.out.println("Path is not Directory");
			return;
		}
		
		File[] files = inputDirectory.listFiles();
		
		for (File file : files) {
			if (!file.getName().contains("SumMon_btw"))
				summaryFromFile(file, dfrom, dend);
		}
	}

	public void summaryFromFile(File file, Date from, Date end) throws Exception {
		
		System.out.println("Starting Summary ...");
		
		FileReader fileReader = null;
		BufferedReader bufferredReadder = null;
		Long avg = 0L;
		Long max = 0L;
		Long min = 999999999999999L;
		Long count = 0L;
		
		try {
			fileReader = new FileReader(file);
			bufferredReadder = new BufferedReader(fileReader);
			String sCurrentLine;
			boolean firstLine = true;
			boolean exit = true;

			System.out.println("Summary File : " + file.getName());

			while ((sCurrentLine = bufferredReadder.readLine()) != null && exit) {
				if (firstLine) {
					firstLine = false;
				} else {
					String[] datas = sCurrentLine.split(",");
					Date dateData = dateFormat.parse(datas[0]);
					if (from.before(dateData)) {
						if (end.after(dateData)) {
							Long used = Long.valueOf(datas[3]);
							count++;
							avg += used;
							max = (max < used) ? used : max;
							min = (min > used) ? used : min;
						} else {
							System.out.println("exit file : " + file.getName());
							exit = false;
						}
					}
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (bufferredReadder != null) {
				bufferredReadder.close();
			}
		}

		appendFile(file.getName(), count, min, avg, max);

	}

	public void appendFile(String fileName, Long count, Long min, Long avg, Long max) {
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new FileWriter(outputFileSummary.getAbsolutePath(), true));
			if (header) {
				writer.append("Name Node,COUNT,MIN(GB),AVG(GB),MAX(GB)");
				writer.newLine();
				header = false;
			}
			if(count <=0 )
				count = 1L;
			
			BigDecimal b_count = new BigDecimal("" + count);
			BigDecimal b_gb = new BigDecimal("" + gigaByte);
			BigDecimal b_min = new BigDecimal("" + min).divide(b_gb, MathContext.DECIMAL128).setScale(4,
					RoundingMode.HALF_UP);
			BigDecimal b_avg = new BigDecimal("" + avg).divide(b_count, MathContext.DECIMAL128);
			b_avg = b_avg.divide(b_gb, MathContext.DECIMAL128).setScale(4, RoundingMode.HALF_UP);
			BigDecimal b_max = new BigDecimal("" + max).divide(b_gb, MathContext.DECIMAL128).setScale(4,
					RoundingMode.HALF_UP);
			StringBuffer summaryAvg = new StringBuffer();
			summaryAvg.append(fileName);
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

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {

			try {
				if (writer != null) {
					writer.close();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
