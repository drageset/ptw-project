package no.uib.info216.ptwp.parsers;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import org.apache.jena.datatypes.xsd.XSDDateTime;
import org.apache.jena.rdf.model.Model;

public class ParseUtils {

	/**
	 * Gjør dato på formatet dd.mm.yyyy (dmy) om til xsd:date format, som er
	 * yyyy-mm-dd
	 * 
	 * @param dmy
	 *            string
	 * @return xsd:date format string
	 */
	protected static String dmyToXSDate(String dmy) {
		try {
			return calToXSDate(dmyToCal(dmy));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return "ErrorFailedToParse";
	}

	/**
	 * Regner ut starttidspunkt fra slutttidspunkt ved å trekke fra en time
	 * 
	 * @param datetime
	 *            string
	 * 
	 * @return xsd:time format string
	 */

	protected static String calculateStartTime(String datetime) {
		DateTimeFormatter xsdFormat = DateTimeFormatter.ofPattern("HH:mm:ss", Locale.ENGLISH);
		LocalTime xsdDateTime = LocalTime.parse(datetime, xsdFormat);
		return xsdDateTime.minusHours(1).toString();
	}

	/**
	 * Regner ut starttidspunkt fra slutttidspunkt ved å trekke fra en time
	 * 
	 * @param datetime
	 *            string
	 * 
	 * @return xsd:time format string
	 */

	protected static String calculateStartDateTime(String datetime) {
		DateTimeFormatter xsdFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss", Locale.ENGLISH);
		LocalDateTime xsdDateTime = LocalDateTime.parse(datetime, xsdFormat);
		return xsdDateTime.minusHours(1).toString();
	}

	/**
	 * Gjør dato på formatet xsd:datetime om til et objekt av typen XSDDateTime
	 * @param xsdString
	 * @return XSDDateTime
	 */
	protected static XSDDateTime toXSDateTime(String xsdString) {
		Calendar cal;
		try {
			cal = Calendar.getInstance();
			SimpleDateFormat xsdFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ENGLISH);
			cal.setTime(xsdFormat.parse(xsdString));
			return new XSDDateTime(cal);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Gjør dato på formatet dd.mm.yyyy (dmy) om til Calendar
	 * @param dmy
	 * @return Calendar
	 * @throws ParseException
	 */
	protected static Calendar dmyToCal(String dmy) throws ParseException {
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.ENGLISH);
		cal.setTime(sdf.parse(dmy));
		return cal;
	}

	/**
	 * Gjør en calendar om til en streng med formatet til xsd:date
	 * 
	 * @param cal
	 * @return xsd:date format string
	 */
	protected static String calToXSDate(Calendar cal) {
		Date time = cal.getTime();
		SimpleDateFormat xsdFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
		String xsdDate = xsdFormat.format(time);
		return xsdDate;
	}

	/**
	 * Writes a model to a file in a chosen notation
	 * 
	 * @param model
	 *            The RDF model that you wish to write to a file
	 * @param filename
	 *            The filename of the file that you wish to write the RDF model
	 *            to
	 * @param notation
	 *            The notation (i.e. TURTLE, JSON-LD) that you wish the file to
	 *            be written in
	 */
	protected static void writeModelToFile(Model model, String filename, String notation) {
		try {
			PrintWriter writer = new PrintWriter(filename, "UTF-8");
			model.write(writer, notation);
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Skitten liten metode for å gjøre om mnd.år til XSD.gYearMonth
	 * 
	 * @param month
	 * @param year
	 * @return String med format som kan brukes til XSD.gYearMonth
	 */
	protected static String fixDateFormat(String month, String year) {
		String yyyymm = "";

		if (Integer.parseInt(year) > 17) {
			yyyymm += ("19" + year + "-");
		} else
			yyyymm += ("20" + year + "-");

		switch (month) {
		case "jan":
			yyyymm += "01";
			break;
		case "feb":
			yyyymm += "02";
			break;
		case "mar":
			yyyymm += "03";
			break;
		case "apr":
			yyyymm += "04";
			break;
		case "mai":
			yyyymm += "05";
			break;
		case "jun":
			yyyymm += "06";
			break;
		case "jul":
			yyyymm += "07";
			break;
		case "aug":
			yyyymm += "08";
			break;
		case "sep":
			yyyymm += "09";
			break;
		case "okt":
			yyyymm += "10";
			break;
		case "nov":
			yyyymm += "11";
			break;
		case "des":
			yyyymm += "12";
			break;
		}
		return yyyymm;
	}

	public static String airPolutionDate_to_XSDate(String string) {
		//return calToXSDate(airPolutionDate_ToCal(string)); 
		String[] dateTime = string.split(" ");
		String[] date = dateTime[0].split("\\.");
		
		if(date[0].length() < 4){ //test if the year comes first. If it does not, switch it to the right place.
			String temp = date[2];
			date[2] = date[0];
			date[0] = temp;
		}
		String xsddate = date[0]+ "-" + date [1] + "-" + date[2];
		return xsddate;
	}
	
	public static String airPolutionTime_to_XSDateTime(String string) {
		//return calToXSDateTime(airPolutionTime_ToCal(string));
		String[] array = string.replace(" ", ".").split("\\.");
		
		if(array[0].length() < 4){ //test if the year comes first. If it does not, switch it to the right place.
			String temp = array[2];
			array[2] = array[0];
			array[0] = temp;
		}
		String xsddate = array[0]+ "-" + array [1] + "-" + array[2] + "T" + array[3];
		return xsddate;
	}

//	private static String calToXSDateTime(Calendar cal) {
//		SimpleDateFormat xsdFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ENGLISH);
//		Date time = cal.getTime();
//		String xsdDateTime = xsdFormat.format(time);
//		return xsdDateTime;
//	}
//	

//	private static Calendar airPolutionDate_ToCal(String string) {
//		Calendar cal = Calendar.getInstance();
//		cal.clear();
//		SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd", Locale.ENGLISH);
//		try {
//			cal.setTime(sdf.parse(string));
//			return cal;
//		} catch (ParseException e) {
//			SimpleDateFormat sdfAlt = new SimpleDateFormat("dd.MM.yyyy", Locale.ENGLISH);
//			try {
//				cal.setTime(sdfAlt.parse(string));
//				return cal;
//			} catch (ParseException e1) {
//				System.out.println("Error: failed to recognize date format");
//				e1.printStackTrace();
//			}
//		}
//		return cal;
//	}
	
//	private static Calendar airPolutionTime_ToCal(String string) {
//		Calendar cal = Calendar.getInstance();
//		cal.clear();
//		SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.ENGLISH);
//		try {
//			cal.setTime(sdf.parse(string));
//			return cal;
//		} catch (ParseException e) {
//			SimpleDateFormat sdfAlt = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.ENGLISH);
//			try {
//				cal.setTime(sdfAlt.parse(string));
//				return cal;
//			} catch (ParseException e1) {
//				System.out.println("Error: failed to recognize date time format");
//				e1.printStackTrace();
//			}
//		}
//		return cal;
//	}
	
//	public static String airPolutionDate_to_XSDDate(String string) {
//
//		return calToXSDate(airPolutionDate_ToCal(string));
//	}


//	public static String airPolutionTime_to_XSDTime(String string) {
//
//		return calToXSDtime(airPolutionTime_ToCal(string));
//	}

	private static String calToXSDtime(Calendar cal) {
		SimpleDateFormat xsdTimeFormat = new SimpleDateFormat("HH:mm:ss", Locale.ENGLISH);
		Date time = cal.getTime();
		String xsdTime = xsdTimeFormat.format(time);
		return xsdTime;
	}
	
	

}
