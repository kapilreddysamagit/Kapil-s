import com.sforce.soap.enterprise.LeadConvert;
import com.sforce.soap.enterprise.LeadConvertResult;
import com.sforce.soap.enterprise.SaveResult;
import com.sforce.soap.enterprise.sobject.Account;
import com.sforce.soap.enterprise.sobject.Lead;
import com.sforce.soap.enterprise.sobject.Opportunity;
import com.sforce.soap.enterprise.sobject.SObject;
import com.sforce.soap.partner.PartnerConnection;
import com.sforce.soap.partner.sobject.*;
import com.sforce.soap.partner.*;
import com.sforce.ws.ConnectorConfig;
import com.sforce.ws.ConnectionException;
import com.sforce.soap.partner.Error;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.util.*;

public class PartnerSamples {
    PartnerConnection partnerConnection = null;
    private static BufferedReader reader =
        new BufferedReader(new InputStreamReader(System.in));
    
    public static void main(String[] args) {
        PartnerSamples samples = new PartnerSamples();
        if (samples.login()) {
            // Add calls to the methods in this class.
            // For example:
            // samples.querySample();
        }
    } 
    
    private String getUserInput(String prompt) {
        String result = "";
        try {
          System.out.print(prompt);
          result = reader.readLine();
        } catch (IOException ioe) {
          ioe.printStackTrace();
        }
        return result;
    }
    
    private boolean login() {
        boolean success = false;
        String username = getUserInput("Enter username: ");
        String password = getUserInput("Enter password: ");
        String authEndPoint = getUserInput("Enter auth end point: ");

        try {
          ConnectorConfig config = new ConnectorConfig();
          config.setUsername(username);
          config.setPassword(password);
          
          config.setAuthEndpoint(authEndPoint);
          config.setTraceFile("traceLogs.txt");
          config.setTraceMessage(true);
          config.setPrettyPrintXml(true);

          partnerConnection = new PartnerConnection(config);          

          success = true;
        } catch (ConnectionException ce) {
          ce.printStackTrace();
        } catch (FileNotFoundException fnfe) {
          fnfe.printStackTrace();
        }

        return success;
      }

    // 
    // Add your methods here.
    //
    
    public void querySample() {    
        try {
            // Set query batch size
            partnerConnection.setQueryOptions(250);
            
            // SOQL query to use 
            String soqlQuery = "SELECT FirstName, LastName FROM Contact";
            // Make the query call and get the query results
            QueryResult qr = partnerConnection.query(soqlQuery);
            
            boolean done = false;
            int loopCount = 0;
            // Loop through the batches of returned results
            while (!done) {
                System.out.println("Records in results set " + loopCount++
                        + " - ");
                SObject[] records = qr.getRecords();
                // Process the query results
                for (int i = 0; i < records.length; i++) {
                    SObject contact = records[i];
                    Object firstName = contact.getField("FirstName");
                    Object lastName = contact.getField("LastName");
                    if (firstName == null) {
                        System.out.println("Contact " + (i + 1) + 
                                ": " + lastName
                        );
                    } else {
                        System.out.println("Contact " + (i + 1) + ": " + 
                                firstName + " " + lastName);
                    }
                }
                if (qr.isDone()) {
                    done = true;
                } else {
                    qr = partnerConnection.queryMore(qr.getQueryLocator());
                }
            }
        } catch(ConnectionException ce) {
            ce.printStackTrace();
        }
        System.out.println("\nQuery execution completed.");         
    }
    
    
    public void searchSample(String phoneNumber) {
        try {    
            // Example of phoneNumber format: 4155551212
            String soslQuery = 
                "FIND {" + phoneNumber + "} IN Phone FIELDS " +
                "RETURNING " +
                "Contact(Id, Phone, FirstName, LastName), " +
                "Lead(Id, Phone, FirstName, LastName)," +
                "Account(Id, Phone, Name)";
            // Perform SOSL query
            SearchResult sResult = partnerConnection.search(soslQuery);
            // Get the records returned by the search result
            SearchRecord[] records = sResult.getSearchRecords();
            // Create lists of objects to hold search result records
            List<SObject> contacts = new ArrayList<SObject>();
            List<SObject> leads = new ArrayList<SObject>();
            List<SObject> accounts = new ArrayList<SObject>();
            
            // Iterate through the search result records
            // and store the records in their corresponding lists
            // based on record type.
            if (records != null && records.length > 0) {
              for (int i = 0; i < records.length; i++){
                SObject record = records[i].getRecord();
                if (record.getType().toLowerCase().equals("contact")) {
                  contacts.add(record);
                } else if (record.getType().toLowerCase().equals("lead")){
                  leads.add(record);
                } else if (record.getType().toLowerCase().equals("account")) {
                  accounts.add(record);
                }
              }
              // Display the contacts that the search returned
              if (contacts.size() > 0) {
                System.out.println("Found " + contacts.size() + 
                    " contact(s):");
                for (SObject contact : contacts) {
                  System.out.println(contact.getId() + " - " +
                      contact.getField("FirstName") + " " +
                      contact.getField("LastName") + " - " +
                      contact.getField("Phone")
                  );
                }
              }
              // Display the leads that the search returned
              if (leads.size() > 0) {
                System.out.println("Found " + leads.size() +
                    " lead(s):");
                for (SObject lead : leads) {
                  System.out.println(lead.getId() + " - " +
                      lead.getField("FirstName") + " " +
                      lead.getField("LastName") + " - " +
                      lead.getField("Phone")
                  );
                }
              }
              // Display the accounts that the search returned
              if (accounts.size() > 0) {
                System.out.println("Found " + 
                    accounts.size() + " account(s):");
                for (SObject account : accounts) {
                  System.out.println(account.getId() + " - " +
                      account.getField("Name") + " - " +                  
                      account.getField("Phone")
                  );
                }
              }
            } else {
              // The search returned no records 
              System.out.println("No records were found for the search.");
            }
          } catch (ConnectionException ce) {
            ce.printStackTrace();
        }      
    }
    
    public String createSample() {
        String result = null;
        try {
            // Create a new sObject of type Contact
               // and fill out its fields.
            SObject contact = new SObject();
            contact.setType("Contact");
            contact.setField("FirstName", "Otto");
            contact.setField("LastName", "Jespersen");
            contact.setField("Salutation", "Professor");
            contact.setField("Phone", "(999) 555-1234");
            contact.setField("Title", "Philologist");
        
            // Add this sObject to an array 
            SObject[] contacts = new SObject[1];
            contacts[0] = contact;
            // Make a create call and pass it the array of sObjects
            SaveResult[] results = partnerConnection.create(contacts);
        
            // Iterate through the results list
            // and write the ID of the new sObject
            // or the errors if the object creation failed.
            // In this case, we only have one result
            // since we created one contact.
            for (int j = 0; j < results.length; j++) {
                if (results[j].isSuccess()) {
                    result = results[j].getId();
                    System.out.println(
                        "\nA contact was created with an ID of: " + result
                    );
                 } else {
                    // There were errors during the create call,
                    // go through the errors array and write
                    // them to the console
                    for (int i = 0; i < results[j].getErrors().length; i++) {
                        Error err = results[j].getErrors()[i];
                        System.out.println("Errors were found on item " + j);
                        System.out.println("Error code: " + 
                            err.getStatusCode().toString());
                        System.out.println("Error message: " + err.getMessage());
                    }
                 }
            }
        } catch (ConnectionException ce) {
            ce.printStackTrace();
        }
        return result;
    }
    
    public void updateSample(String id) {
    	  try {
    	      // Create an sObject of type contact
    	      SObject updateContact = new SObject();
    	      updateContact.setType("Contact");
    	      
    	      // Set the ID of the contact to update
    	      updateContact.setId(id);
    	      // Set the Phone field with a new value
    	      updateContact.setField("Phone", "(415) 555-1212");

    	      // Create another contact that will cause an error
    	      // because it has an invalid ID.
    	      SObject errorContact = new SObject();
    	      errorContact.setType("Contact");
    	      // Set an invalid ID on purpose
    	      errorContact.setId("SLFKJLFKJ");      
    	      // Set the value of LastName to null
    	      errorContact.setFieldsToNull(new String[] {"LastName"});

    	      // Make the update call by passing an array containing
    	      // the two objects. 
    	      SaveResult[] saveResults = partnerConnection.update(
    	         new SObject[] {updateContact, errorContact}
    	      );
    	      // Iterate through the results and write the ID of 
    	      // the updated contacts to the console, in this case one contact.
    	      // If the result is not successful, write the errors
    	      // to the console. In this case, one item failed to update.
    	      for (int j = 0; j < saveResults.length; j++) {
    	          System.out.println("\nItem: " + j);
    	          if (saveResults[j].isSuccess()) {
    	              System.out.println("Contact with an ID of " +
    	                      saveResults[j].getId() + " was updated.");
    	          }
    	          else {                        
    	            // There were errors during the update call,
    	            // go through the errors array and write
    	            // them to the console.
    	            for (int i = 0; i < saveResults[j].getErrors().length; i++) {
    	              Error err = saveResults[j].getErrors()[i];
    	              System.out.println("Errors were found on item " + j);
    	              System.out.println("Error code: " + 
    	                  err.getStatusCode().toString());
    	              System.out.println("Error message: " + err.getMessage());
    	            }
    	          }      
    	      }      
    	  } catch (ConnectionException ce) {
    	      ce.printStackTrace();
    	  }
    	}
    

}