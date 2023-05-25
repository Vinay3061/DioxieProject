package LeadScore;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;


import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.testng.annotations.DataProvider;
import io.restassured.response.Response;
import static io.restassured.RestAssured.*;


/*  The below code defines a test class named LeadScore(Sales)Test, which contains a test method named getsalesprobscore().
 ** The test data for the test method is provided by a data provider method named getData().
 ** The test method sends an HTTP GET request to a LeadScore(Sales) API with query parameters based on the test data,
 ** extracts the actual Probability Score  from the response body, and compares it to the Probability Score.
 ** The test class uses TestNG annotations (@Test and @DataProvider) to define the test and data provider methods,
 ** and uses Apache POI to read test data from an Excel file. It also uses RestAssured to send HTTP requests and
 ** assert response status code and response body.
 **/


public class LeadScoreSales {
	
	// Define the data provider that reads data from an Excel sheet
	@DataProvider(name="carsData")
	public Object[][] getData() throws IOException{
		File file = new File("./data/LeadScore(Sales).xlsx");// Path of the Excel file containing test data
	    FileInputStream inputStream = new FileInputStream(file);
	    Workbook workbook = WorkbookFactory.create(inputStream);

	 // Get the first sheet from the Excel file
	    Sheet sheet = workbook.getSheetAt(0); 
	    int rowCount = sheet.getLastRowNum();// Get the total number of rows in the sheet
	    int colCount = sheet.getRow(0).getLastCellNum();// Get the total number of columns in the sheet
	    
	    Object[][] data = new Object[rowCount][colCount];// Create a 2D array to store the test data
	 // Loop through each row in the sheet, starting from the second row (i.e. index 1)
	    for (int i = 1; i <= rowCount; i++) {
	    	Row row = sheet.getRow(i);
	    	// Loop through each column in the row
	    	for (int j = 0; j < colCount; j++) {
	    		Cell cell = row.getCell(j);
	    		// Check the data type of the cell and store the data accordingly
	    		switch (cell.getCellType()) {
	    			case STRING:
	    				data[i-1][j] = cell.getStringCellValue();
	    				break;
	    			case NUMERIC:
	    				data[i-1][j] = (int) cell.getNumericCellValue();
	    				data[i-1][j] = (double) cell.getNumericCellValue();
	    				break;
	    			default:
	    				System.out.println("Unsupported cell type.");
	    		}
	    	}
	    }
	    
	    workbook.close();// Close the workbook and the input stream
	    inputStream.close();
	    
	    return data;// Return the test data
	}
	// Define the actual test method
	@Test(dataProvider="carsData")
	void getsalesprobscore(  
					   String make, 
			           String model,
			           String grade,
			           String suffix,
			           double mfgYear,
			           String sourceType,
			           String newCarTradeIn,
			           java.lang.Double salesprobscore
			         ) {
		// Send an HTTP GET request to a LeadScore(Sales) API with query parameters based on the test data
		Response res= given()
			.pathParam("mypath","sales-prediction")
			.queryParam("Make",make)
			.queryParam("Model",model)
			.queryParam("Grade",grade)
			.queryParam("Suffix",suffix)
			.queryParam("MfgYear",mfgYear)			
			.queryParam("Source.Type",sourceType)			
			.queryParam("New.Car.Trade.in",newCarTradeIn)
			
			// Make a GET request to the specified URL
			.when()
			.get("https://ml.api.ls.dioxe.net/{mypath}");
		 
 		
        // Assert the response status code and Probability Score
        
        Assert.assertEquals(res.getStatusCode(), 200,"Correct Status Code is Returned");
        
        String leadscoresales = res.jsonPath().get("sales_prediction").toString();
        
        // Convert the probsalesscore from a String to a Float 
        Float probsalesscore=Float.parseFloat(leadscoresales);
        //System.out.println("Response Body for row :" + probsalesscore.getClass());
         
        
     
        
     // Convert the expectedprobscore from a Double to a Float
        Float expectedsalesprobscore = salesprobscore.floatValue();
       
     // Assert that the predicted Probability Score matches the expected Probability Score
        Assert.assertEquals(probsalesscore,expectedsalesprobscore);
        
     // Print the response body for debugging purposes
        //System.out.println("Response Body for row :" + res.asString());
        	 
    }
}


