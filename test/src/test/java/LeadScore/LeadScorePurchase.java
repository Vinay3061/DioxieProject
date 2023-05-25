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


/*  The below code defines a test class named LeadScore(Purchase)Test, which contains a test method named getpurchaseprobscore().
 ** The test data for the test method is provided by a data provider method named getData().
 ** The test method sends an HTTP GET request to a LeadScore(Purchase) API with query parameters based on the test data,
 ** extracts the actual Probability Score  from the response body, and compares it to the Probability Score.
 ** The test class uses TestNG annotations (@Test and @DataProvider) to define the test and data provider methods,
 ** and uses Apache POI to read test data from an Excel file. It also uses RestAssured to send HTTP requests and
 ** assert response status code and response body.
 **/


public class LeadScorePurchase {
	
	// Define the data provider that reads data from an Excel sheet
	@DataProvider(name="carsData")
	public Object[][] getData() throws IOException{
		File file = new File("./data/LeadScore(Purchase).xlsx");// Path of the Excel file containing test data
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
	void getpurchaseprobscore(  
					   String make, 
			           String model,
			           double mfgYear,
			           String fuelType,
			           String sourceType,
			           java.lang.Double purchaseprobscore
			           
			         ) {
		
		// Send an HTTP GET request to a LeadScore(Sales) API with query parameters based on the test data
		Response res= given()
			.pathParam("mypath","purchase-prediction")
			.queryParam("Make",make)
			.queryParam("Model",model)			
			.queryParam("MfgYear",mfgYear)
			.queryParam("Fuel.Type",fuelType)
			.queryParam("Source.Type",sourceType)			
			
			
			// Make a GET request to the specified URL
			.when()
			.get("https://ml.api.ls.dioxe.net/{mypath}");
		 
 		
        // Assert the response status code and Probability Score
        
        Assert.assertEquals(res.getStatusCode(), 200,"Correct Status Code is Returned");
        
        // Extract the predicted Probability Score from the response body
        	String leadscorepurchase = res.jsonPath().get("purchase_prediction").toString();
        
        // Convert the probsalesscore from a String to a Float 
        Float probpurchasescore=Float.parseFloat(leadscorepurchase);
        
        
     // Convert the expectedprobscore from a Double to a Float
        Float expectedpurchaseprobscore = purchaseprobscore.floatValue();
       
     // Assert that the predicted Probability Score matches the expected Probability Score
       Assert.assertEquals(probpurchasescore,expectedpurchaseprobscore);
        
     // Print the response body for debugging purposes
        //System.out.println("Response Body for row :" + res.asString());
        	
    }
}

