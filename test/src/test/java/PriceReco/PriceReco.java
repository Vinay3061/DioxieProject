package PriceReco;

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


/*  The below code defines a test class named PriceRecoTest, which contains a test method named getcarprice().
 ** The test data for the test method is provided by a data provider method named getData().
 ** The test method sends an HTTP GET request to a PriceReco API with query parameters based on the test data,
 ** extracts the actual price from the response body, and compares it to the expected price.
 ** The test class uses TestNG annotations (@Test and @DataProvider) to define the test and data provider methods,
 ** and uses Apache POI to read test data from an Excel file. It also uses RestAssured to send HTTP requests and
 ** assert response status code and response body.
 **/


public class PriceReco {
	
	// Define the data provider that reads data from an Excel sheet
	@DataProvider(name="carsData")
	public Object[][] getData() throws IOException{
		File file = new File("./data/PriceTest.xlsx");// Path of the Excel file containing test data
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
	void getcarprice(  
					   String make, 
			           String model,
			           String grade,
			           String suffix,
			           double  mfgYear,
			           double odometerReading,
			           double noOfOwnership,
			           String color,
			           String insuranceType,
			           String sourceType,
			           String vehicleClassification,
			           String fuelType,
			           double qualityLevel,
			           String newCarTradeIn,
			           java.lang.Double ExpectedPrice
			         ) {
		// Send an HTTP GET request to a PriceReco API with query parameters based on the test data
		Response res= given()
			.pathParam("mypath","price-reco")
			.queryParam("Make",make)
			.queryParam("Model",model)
			.queryParam("Grade",grade)
			.queryParam("Suffix",suffix)
			.queryParam("MfgYear",mfgYear)
			.queryParam("Odometer.Reading",odometerReading)
			.queryParam("No.Of.Ownership",noOfOwnership)
			.queryParam("Color",color)
			.queryParam("Insurance.Type",insuranceType)
			.queryParam("Source.Type",sourceType)
			.queryParam("Vehicle.Classification",vehicleClassification)
			.queryParam("Fuel.Type",fuelType)
			.queryParam("Quality.Level",qualityLevel)
			.queryParam("New.Car.Trade.in",newCarTradeIn)
			
			// Make a GET request to the specified URL
			.when()
			.get(" https://ml.api.ls.dioxe.net/{mypath}");
		 
 		
        // Assert the response status code and car price
        
        Assert.assertEquals(res.getStatusCode(), 200,"Correct Status Code is Returned");
        
        // Extract the predicted car price from the response body
        
        String recocarPrice = res.jsonPath().get("Pred_LR_PP").toString();
       
        // Convert the recocarPrice from a String to a Float 
        Float recoprice=Float.parseFloat(recocarPrice);
         
        
     // Convert the expected price from a Double to a Float
        Float lexpectPrice = ExpectedPrice.floatValue();
        
       
     // Assert that the predicted price matches the expected price
        Assert.assertEquals(recoprice,lexpectPrice);
        
     // Print the response body for debugging purposes
       // System.out.println("Response Body for row :" + res.asString());
        		
    }
}


