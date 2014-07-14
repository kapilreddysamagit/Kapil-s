
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

import javax.xml.ws.soap.Addressing;

import com.sforce.rest.SearchResult;
import com.sforce.soap.enterprise.DeletedRecord;
import com.sforce.soap.enterprise.DescribeGlobalResult;
import com.sforce.soap.enterprise.DescribeSObjectResult;
import com.sforce.soap.enterprise.EmailFileAttachment;
import com.sforce.soap.enterprise.EmailPriority;
import com.sforce.soap.enterprise.EnterpriseConnection;
import com.sforce.soap.enterprise.Field;
import com.sforce.soap.enterprise.FieldType;
import com.sforce.soap.enterprise.GetUserInfoResult;
import com.sforce.soap.enterprise.LeadConvert;
import com.sforce.soap.enterprise.LeadConvertResult;
import com.sforce.soap.enterprise.MergeRequest;
import com.sforce.soap.enterprise.PicklistEntry;
import com.sforce.soap.enterprise.ProcessSubmitRequest;
import com.sforce.soap.enterprise.QueryResult;
import com.sforce.soap.enterprise.SaveResult;
import com.sforce.soap.enterprise.SingleEmailMessage;
import com.sforce.soap.enterprise.UndeleteResult;
import com.sforce.soap.enterprise.sobject.Account;
import com.sforce.soap.enterprise.sobject.Contact;
import com.sforce.soap.enterprise.sobject.Lead;
import com.sforce.soap.enterprise.sobject.Note;
import com.sforce.soap.enterprise.sobject.Opportunity;
import com.sforce.soap.enterprise.sobject.SObject;
import com.sforce.soap.enterprise.sobject.Task;
import com.sforce.soap.partner.DeleteResult;
import com.sforce.soap.partner.EmptyRecycleBinResult;
import com.sforce.soap.partner.GetDeletedResult;
import com.sforce.soap.partner.GetUpdatedResult;
import com.sforce.soap.partner.InvalidateSessionsResult;
import com.sforce.soap.partner.MergeResult;
import com.sforce.soap.partner.ProcessResult;
import com.sforce.soap.partner.SearchRecord;
import com.sforce.soap.partner.SendEmailResult;
import com.sforce.ws.ConnectorConfig;
import com.sforce.ws.ConnectionException;

public class QuickstartApiSample {

   private static BufferedReader reader = new BufferedReader(
         new InputStreamReader(System.in));

   EnterpriseConnection connection;
   String authEndPoint = "";

   public static void main(String[] args) {
      if (args.length < 1) {
         System.out.println("Usage: com.example.samples."
               + "QuickstartApiSamples <AuthEndPoint>");

         System.exit(-1);
      }

      QuickstartApiSample sample = new QuickstartApiSample(args[0]);
      sample.run();
   }

   public void run() {
      // Make a login call
      if (login()) {
         // Do a describe global
         describeGlobalSample();

         // Describe an object
         describeSObjectsSample();

         // Retrieve some data using a query
         querySample();

         // Log out
         logout();
      }
   }

   // Constructor
   public QuickstartApiSample(String authEndPoint) {
      this.authEndPoint = authEndPoint;
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

      try {
         ConnectorConfig config = new ConnectorConfig();
         config.setUsername(username);
         config.setPassword(password);

         System.out.println("AuthEndPoint: " + authEndPoint);
         config.setAuthEndpoint(authEndPoint);

         connection = new EnterpriseConnection(config);
         printUserInfo(config);

         success = true;
      } catch (ConnectionException ce) {
         ce.printStackTrace();
      } 

      return success;
   }

   private void printUserInfo(ConnectorConfig config) {
      try {
         GetUserInfoResult userInfo = connection.getUserInfo();

         System.out.println("\nLogging in ...\n");
         System.out.println("UserID: " + userInfo.getUserId());
         System.out.println("User Full Name: " + userInfo.getUserFullName());
         System.out.println("User Email: " + userInfo.getUserEmail());
         System.out.println();
         System.out.println("SessionID: " + config.getSessionId());
         System.out.println("Auth End Point: " + config.getAuthEndpoint());
         System.out
               .println("Service End Point: " + config.getServiceEndpoint());
         System.out.println();
      } catch (ConnectionException ce) {
         ce.printStackTrace();
      }
   }

   private void logout() {
      try {
         connection.logout();
         System.out.println("Logged out.");
      } catch (ConnectionException ce) {
         ce.printStackTrace();
      }
   }

   /**
    * To determine the objects that are available to the logged-in user, the
    * sample client application executes a describeGlobal call, which returns
    * all of the objects that are visible to the logged-in user. This call
    * should not be made more than once per session, as the data returned from
    * the call likely does not change frequently. The DescribeGlobalResult is
    * simply echoed to the console.
    */
   private void describeGlobalSample() {
      try {
         // describeGlobal() returns an array of object results that
         // includes the object names that are available to the logged-in user.
         DescribeGlobalResult dgr = connection.describeGlobal();

         System.out.println("\nDescribe Global Results:\n");
         // Loop through the array echoing the object names to the console
         for (int i = 0; i < dgr.getSobjects().length; i++) {
            System.out.println(dgr.getSobjects()[i].getName());
         }
      } catch (ConnectionException ce) {
         ce.printStackTrace();
      }
   }

   /**
    * The following method illustrates the type of metadata information that can
    * be obtained for each object available to the user. The sample client
    * application executes a describeSObject call on a given object and then
    * echoes the returned metadata information to the console. Object metadata
    * information includes permissions, field types and length and available
    * values for picklist fields and types for referenceTo fields.
    */
   private void describeSObjectsSample() {
      String objectToDescribe = getUserInput("\nType the name of the object to "
            + "describe (try Account): ");

      try {
         // Call describeSObjects() passing in an array with one object type
         // name
         DescribeSObjectResult[] dsrArray = connection
               .describeSObjects(new String[] { objectToDescribe });

         // Since we described only one sObject, we should have only
         // one element in the DescribeSObjectResult array.
         DescribeSObjectResult dsr = dsrArray[0];

         // First, get some object properties
         System.out.println("\n\nObject Name: " + dsr.getName());

         if (dsr.getCustom())
            System.out.println("Custom Object");
         if (dsr.getLabel() != null)
            System.out.println("Label: " + dsr.getLabel());

         // Get the permissions on the object

         if (dsr.getCreateable())
            System.out.println("Createable");
         if (dsr.getDeletable())
            System.out.println("Deleteable");
         if (dsr.getQueryable())
            System.out.println("Queryable");
         if (dsr.getReplicateable())
            System.out.println("Replicateable");
         if (dsr.getRetrieveable())
            System.out.println("Retrieveable");
         if (dsr.getSearchable())
            System.out.println("Searchable");
         if (dsr.getUndeletable())
            System.out.println("Undeleteable");
         if (dsr.getUpdateable())
            System.out.println("Updateable");

         System.out.println("Number of fields: " + dsr.getFields().length);

         // Now, retrieve metadata for each field
         for (int i = 0; i < dsr.getFields().length; i++) {
            // Get the field
            Field field = dsr.getFields()[i];

            // Write some field properties
            System.out.println("Field name: " + field.getName());
            System.out.println("\tField Label: " + field.getLabel());

            // This next property indicates that this
            // field is searched when using
            // the name search group in SOSL
            if (field.getNameField())
               System.out.println("\tThis is a name field.");

            if (field.getRestrictedPicklist())
               System.out.println("This is a RESTRICTED picklist field.");

            System.out.println("\tType is: " + field.getType());

            if (field.getLength() > 0)
               System.out.println("\tLength: " + field.getLength());

            if (field.getScale() > 0)
               System.out.println("\tScale: " + field.getScale());

            if (field.getPrecision() > 0)
               System.out.println("\tPrecision: " + field.getPrecision());

            if (field.getDigits() > 0)
               System.out.println("\tDigits: " + field.getDigits());

            if (field.getCustom())
               System.out.println("\tThis is a custom field.");

            // Write the permissions of this field
            if (field.getNillable())
               System.out.println("\tCan be nulled.");
            if (field.getCreateable())
               System.out.println("\tCreateable");
            if (field.getFilterable())
               System.out.println("\tFilterable");
            if (field.getUpdateable())
               System.out.println("\tUpdateable");

            // If this is a picklist field, show the picklist values
            if (field.getType().equals(FieldType.picklist)) {
               System.out.println("\t\tPicklist values: ");
               PicklistEntry[] picklistValues = field.getPicklistValues();

               for (int j = 0; j < field.getPicklistValues().length; j++) {
                  System.out.println("\t\tValue: "
                        + picklistValues[j].getValue());
               }
            }

            // If this is a foreign key field (reference),
            // show the values
            if (field.getType().equals(FieldType.reference)) {
               System.out.println("\tCan reference these objects:");
               for (int j = 0; j < field.getReferenceTo().length; j++) {
                  System.out.println("\t\t" + field.getReferenceTo()[j]);
               }
            }
            System.out.println("");
         }
      } catch (ConnectionException ce) {
         ce.printStackTrace();
      }
   } 

   // Modified version of code in the SOAP API QuickStart
	private void querySample() {
	    String soqlQuery = "SELECT FirstName, LastName, MailingAddress FROM Contact";
	    try {
	        QueryResult qr = connection.query(soqlQuery);
	        boolean done = false;
	
	        if (qr.getSize() > 0) {
	            System.out.println("\nLogged-in user can see "
	              + qr.getRecords().length + " contact records.");
	
	            while (!done) {
	                System.out.println("");
	                SObject[] records = qr.getRecords();
	                for (int i = 0; i < records.length; ++i) {
	                    Contact con = (Contact) records[i];
	                    String fName = con.getFirstName();
	                    String lName = con.getLastName();
	                    
	                    // Access the compound address field MailingAddress
	                    String addr = (String) con.getMailingCity();
	                    String streetAddr = "";
	                    if (null != addr) streetAddr = addr;
	
	                    if (fName == null) {
	                        System.out.println("Contact " + (i + 1) + ": " + lName +
	                            " -- " + streetAddr);
	                    } else {
	                        System.out.println("Contact " + (i + 1) + ": " + fName +
	                            " " + lName +
	                            " -- " + streetAddr);
	                    }
	                }
	
	                if (qr.isDone()) {
	                    done = true;
	                } else {
	                    qr = connection.queryMore(qr.getQueryLocator());
	                }
	            }
	        } else {
	            System.out.println("No records found.");
	        }
	    } catch (ConnectionException ce) {
	        ce.printStackTrace();
	    }
	}
	public String[] convertLeadRecords() {
		   String[] result = new String[4];
		   try {

		      // Create two leads to convert
		      Lead[] leads = new Lead[2];
		      Lead lead = new Lead();
		      lead.setLastName("Mallard");
		      lead.setFirstName("Jay");
		      lead.setCompany("Wingo Ducks");
		      lead.setPhone("(707) 555-0328");
		      leads[0] = lead;
		      lead = new Lead();
		      lead.setLastName("Platypus");
		      lead.setFirstName("Ogden");
		      lead.setCompany("Denio Water Co.");
		      lead.setPhone("(775) 555-1245");
		      leads[1] = lead;
		      SaveResult[] saveResults = connection.create(leads);

		      // Create a LeadConvert array to be used
		      // in the convertLead() call
		      LeadConvert[] leadsToConvert = new LeadConvert[saveResults.length];

		      for (int i = 0; i < saveResults.length; ++i) {
		         if (saveResults[i].isSuccess()) {
		            System.out
		                  .println("Created new Lead: " + saveResults[i].getId());
		            leadsToConvert[i] = new LeadConvert();
		            leadsToConvert[i].setConvertedStatus("Closed - Converted");
		            leadsToConvert[i].setLeadId(saveResults[i].getId());
		            result[0] = saveResults[i].getId();
		         } else {
		            System.out.println("\nError creating new Lead: "
		                  + saveResults[i].getErrors()[0].getMessage());
		         }
		      }
		      // Convert the leads and iterate through the results
		      LeadConvertResult[] lcResults = connection.convertLead(leadsToConvert);
		      for (int j = 0; j < lcResults.length; ++j) {
		         if (lcResults[j].isSuccess()) {
		            System.out.println("Lead converted successfully!");
		            System.out.println("Account ID: " + lcResults[j].getAccountId());
		            System.out.println("Contact ID: " + lcResults[j].getContactId());
		            System.out.println("Opportunity ID: "
		                  + lcResults[j].getOpportunityId());
		         } else {
		            System.out.println("\nError converting new Lead: "
		                  + lcResults[j].getErrors()[0].getMessage());
		         }
		      }
		   } catch (ConnectionException ce) {
		      ce.printStackTrace();
		   }
		   return result;
		}
	
	public void createForeignKeySample() {
		   try {
		      Opportunity newOpportunity = new Opportunity();
		      newOpportunity.setName("OpportunityWithFK");
		      newOpportunity.setStageName("Prospecting");
		      Calendar dt = connection.getServerTimestamp().getTimestamp();
		      dt.add(Calendar.DAY_OF_MONTH, 7);
		      newOpportunity.setCloseDate(dt);

		      Account parentAccountRef = new Account();
		     // parentAccountRef.setMyExtId__c("SAP1111111");
		      newOpportunity.setAccount(parentAccountRef);

		      SaveResult[] results = connection
		            .create(new SObject[] { newOpportunity });
		   } catch (ConnectionException ce) {
		      ce.printStackTrace();
		   }
		}
	
	public void deleteRecords(String[] ids) {
		   try {
		      com.sforce.soap.enterprise.DeleteResult[] deleteResults = connection.delete(ids);
		      for (int i = 0; i < deleteResults.length; i++) {
		         com.sforce.soap.enterprise.DeleteResult deleteResult = deleteResults[i];
		         if (deleteResult.isSuccess()) {
		            System.out
		                  .println("Deleted Record ID: " + deleteResult.getId());
		         } else {
		            // Handle the errors.
		            // We just print the first error out for sample purposes.
		            com.sforce.soap.enterprise.Error[] errors = deleteResult.getErrors();
		            if (errors.length > 0) {
		               System.out.println("Error: could not delete " + "Record ID "
		                     + deleteResult.getId() + ".");
		               System.out.println("   The error reported was: ("
		                     + errors[0].getStatusCode() + ") "
		                     + errors[0].getMessage() + "\n");
		            }
		         }
		      }
		   } catch (ConnectionException ce) {
		      ce.printStackTrace();
		   }
		}
	
	public void emptyRecycleBin(String[] ids) {
		   try {
		      com.sforce.soap.enterprise.EmptyRecycleBinResult[] emptyRecycleBinResults = connection
		            .emptyRecycleBin(ids);
		      for (int i = 0; i < emptyRecycleBinResults.length; i++) {
		         com.sforce.soap.enterprise.EmptyRecycleBinResult emptyRecycleBinResult = emptyRecycleBinResults[i];
		         if (emptyRecycleBinResult.isSuccess()) {
		            System.out.println("Recycled ID: "
		                  + emptyRecycleBinResult.getId());
		         } else {
		            com.sforce.soap.enterprise.Error[] errors = emptyRecycleBinResult.getErrors();
		            if (errors.length > 0) {
		               System.out
		                     .println("Error code: " + errors[0].getStatusCode());
		               System.out
		                     .println("Error message: " + errors[0].getMessage());
		            }
		         }
		      }
		   } catch (ConnectionException ce) {
		      ce.printStackTrace();
		   }
		}
	public void getDeletedRecords() {
		   try {
		      GregorianCalendar endTime = (GregorianCalendar)
		         connection.getServerTimestamp().getTimestamp();
		      GregorianCalendar startTime = (GregorianCalendar) endTime.clone();         
		      // Subtract 60 minutes from the server time so that we have
		      // a valid time frame.
		      startTime.add(GregorianCalendar.MINUTE, -60);
		      System.out.println("Checking deletes at or after: "
		            + startTime.getTime().toString());
		      
		      // Get records deleted during the specified time frame.
		      com.sforce.soap.enterprise.GetDeletedResult gdResult = connection.getDeleted("Account",
		            startTime, endTime);
		      
		      // Check the number of records contained in the results,
		      // to check if something was deleted in the 60 minute span.
		      DeletedRecord[] deletedRecords = gdResult.getDeletedRecords();
		      if (deletedRecords != null && deletedRecords.length > 0) {
		         for (int i = 0; i < deletedRecords.length; i++) {
		            DeletedRecord dr = deletedRecords[i];
		            System.out.println(dr.getId() + " was deleted on "
		                  + dr.getDeletedDate().getTime().toString());
		         }
		      } else {
		         System.out.println("No deletions of Account records in "
		               + "the last 60 minutes.");
		      }
		   } catch (ConnectionException ce) {
		      ce.printStackTrace();
		   }
		}
	
	public void getUpdatedRecords() {
		   try {
		      GregorianCalendar endTime = (GregorianCalendar) connection
		            .getServerTimestamp().getTimestamp();
		      GregorianCalendar startTime = (GregorianCalendar) endTime.clone();
		      // Subtract 60 minutes from the server time so that we have
		      // a valid time frame.
		      startTime.add(GregorianCalendar.MINUTE, -60);
		      System.out.println("Checking updates as of: "
		            + startTime.getTime().toString());
		      
		      // Get the updated accounts within the specified time frame
		      com.sforce.soap.enterprise.GetUpdatedResult ur = connection.getUpdated("Account", startTime,
		            endTime);
		      System.out.println("GetUpdateResult: " + ur.getIds().length);
		      
		      // Write the results
		      if (ur.getIds() != null && ur.getIds().length > 0) {
		         for (int i = 0; i < ur.getIds().length; i++) {
		            System.out.println(ur.getIds()[i] + " was updated between "
		                  + startTime.getTime().toString() + " and "
		                  + endTime.getTime().toString());
		         }
		      } else {
		         System.out.println("No updates to accounts in "
		               + "the last 60 minutes.");
		      }
		   } catch (ConnectionException ce) {
		      ce.printStackTrace();
		   }
		}

	public void invalidateSessionsSample(String[] sessionIds) {
		   try {
		      com.sforce.soap.enterprise.InvalidateSessionsResult[] results;
		      results = connection.invalidateSessions(sessionIds);
		      for (com.sforce.soap.enterprise.InvalidateSessionsResult result : results) {
		         // Check results for errors
		         if (!result.isSuccess()) {
		            if (result.getErrors().length > 0) {
		               System.out.println("Status code: "
		                     + result.getErrors()[0].getStatusCode());
		               System.out.println("Error message: "
		                     + result.getErrors()[0].getMessage());
		            }
		         } else {
		            System.out.println("Success.");
		         }
		      }
		   } catch (ConnectionException ce) {
		      ce.printStackTrace();
		   }
		}
	
	public Boolean mergeRecords() {
		   Boolean success = false;
		   // Array to hold the results
		   String[] accountIds = new String[2];
		   try {
		      // Create two accounts to merge
		      Account[] accounts = new Account[2];
		      Account masterAccount = new Account();
		      masterAccount.setName("MasterAccount");
		      masterAccount.setDescription("The Account record to merge with.");
		      accounts[0] = masterAccount;
		      Account accountToMerge = new Account();
		      accountToMerge.setName("AccountToMerge");
		      accountToMerge
		            .setDescription("The Account record that will be merged.");
		      accounts[1] = accountToMerge;
		      SaveResult[] saveResults = connection.create(accounts);

		      if (saveResults.length > 0) {
		         for (int i = 0; i < saveResults.length; i++) {
		            if (saveResults[i].isSuccess()) {
		               accountIds[i] = saveResults[i].getId();
		               System.out.println("Created Account ID: "
		                     + accountIds[i]);                 
		            } else {
		               // If any account is not created,
		               // print the error returned and exit
		               System.out
		                     .println("An error occurred while creating account."
		                           + " Error message: "
		                           + saveResults[i].getErrors()[0].getMessage());
		               return success;
		            }
		         }
		      }

		      // Set the Ids of the accounts 
		      masterAccount.setId(accountIds[0]);
		      accountToMerge.setId(accountIds[1]);

		      // Attach a note to the account to be merged with the master,
		      // which will get re-parented after the merge
		      Note note = new Note();
		      System.out.println("Attaching note to record " +
		            accountIds[1]);
		      note.setParentId(accountIds[1]);
		      note.setTitle("Merged Notes");
		      note.setBody("This note will be moved to the "
		            + "MasterAccount during merge");
		      SaveResult[] sRes = connection.create(new SObject[] { note });
		      if (sRes[0].isSuccess()) {
		         System.out.println("Created Note record.");
		      } else {
		         com.sforce.soap.enterprise.Error[] errors = sRes[0].getErrors();
		         System.out.println("Could not create Note record: "
		               + errors[0].getMessage());
		      }

		      // Perform the merge
		      MergeRequest mReq = new MergeRequest();
		      masterAccount.setDescription("Was merged");
		      mReq.setMasterRecord(masterAccount);
		      mReq.setRecordToMergeIds(new String[] { saveResults[1].getId() });
		      com.sforce.soap.enterprise.MergeResult mRes = connection.merge(new MergeRequest[] { mReq })[0];
		      
		      if (mRes.isSuccess())
		      {
		         System.out.println("Merge successful.");            
		         // Write the IDs of merged records
		         for(String mergedId : mRes.getMergedRecordIds()) {
		            System.out.println("Merged Record ID: " + mergedId);                           
		         }
		         // Write the updated child records. (In this case the note.)
		         System.out.println(
		               "Child records updated: " + mRes.getUpdatedRelatedIds().length);   
		         success = true;
		      } else {
		         System.out.println("Failed to merge records. Error message: " +
		               mRes.getErrors()[0].getMessage());
		      }
		   } catch (ConnectionException ce) {
		      ce.printStackTrace();
		   }
		   return success;
		}
	
	public void processRecords(String id, String[] approverIds) {
		   ProcessSubmitRequest request = new ProcessSubmitRequest();
		   request.setComments("A comment about this approval.");
		   request.setObjectId(id);
		   request.setNextApproverIds(approverIds);
		   try {
		      com.sforce.soap.enterprise.ProcessResult[] processResults = connection
		            .process(new ProcessSubmitRequest[] { request });
		      for (com.sforce.soap.enterprise.ProcessResult processResult : processResults) {
		         if (processResult.isSuccess()) {
		            System.out.println("Approval submitted for: " + id + ":");
		            for (int i = 0; i < approverIds.length; i++) {
		               System.out
		                     .println("\tBy: " + approverIds[i] + " successful.");
		            }
		            System.out.println("Process Instance Status: "
		                  + processResult.getInstanceStatus());
		         } else {
		            System.out.println("Approval submitted for: " + id
		                  + ", approverIds: " + approverIds.toString() + " FAILED.");
		            System.out.println("Error: "
		                  + processResult.getErrors().toString());
		         }
		      }
		   } catch (ConnectionException ce) {
		      ce.printStackTrace();
		   }
		}
	
	public void retrieveRecords(String[] ids) {
		   try {
		      SObject[] sObjects = connection.retrieve("ID, Name, Website",
		            "Account", ids);
		      // Verify that some objects were returned.
		      // Even though we began with valid object IDs,
		      // someone else might have deleted them in the meantime.
		      if (sObjects != null) {
		         for (int i = 0; i < sObjects.length; i++) {
		            // Cast the SObject into an Account object
		            Account retrievedAccount = (Account) sObjects[i];
		            if (retrievedAccount != null) {
		               System.out.println("Account ID: " + retrievedAccount.getId());
		               System.out.println("Account Name: " + retrievedAccount.getName());
		               System.out.println("Account Website: "
		                     + retrievedAccount.getWebsite());
		            }
		         }
		      }
		   } catch (ConnectionException ce) {
		      ce.printStackTrace();
		   }
		}
	
	public void searchSample() {
		   try {
		      // Perform the search using the SOSL query.
		      com.sforce.soap.enterprise.SearchResult sr = connection.search(
		            "FIND {4159017000} IN Phone FIELDS RETURNING "
		            + "Contact(Id, Phone, FirstName, LastName), "
		            + "Lead(Id, Phone, FirstName, LastName), "
		            + "Account(Id, Phone, Name)");

		      // Get the records from the search results.
		      com.sforce.soap.enterprise.SearchRecord[] records = sr.getSearchRecords();

		      ArrayList<Contact> contacts = new ArrayList<Contact>();
		      ArrayList<Lead> leads = new ArrayList<Lead>();
		      ArrayList<Account> accounts = new ArrayList<Account>();

		      // For each record returned, find out if it's a
		      // contact, lead, or account and add it to the
		      // appropriate array, then write the records
		      // to the console.
		      if (records.length > 0) {
		         for (int i = 0; i < records.length; i++) {
		            SObject record = records[i].getRecord();
		            if (record instanceof Contact) {
		               contacts.add((Contact) record);
		            } else if (record instanceof Lead) {
		               leads.add((Lead) record);
		            } else if (record instanceof Account) {
		               accounts.add((Account) record);
		            }
		         }

		         System.out.println("Found " + contacts.size() + " contacts.");
		         for (Contact c : contacts) {
		            System.out.println(c.getId() + ", " + c.getFirstName() + ", "
		                  + c.getLastName() + ", " + c.getPhone());
		         }
		         System.out.println("Found " + leads.size() + " leads.");
		         for (Lead d : leads) {
		            System.out.println(d.getId() + ", " + d.getFirstName() + ", "
		                  + d.getLastName() + ", " + d.getPhone());
		         }
		         System.out.println("Found " + accounts.size() + " accounts.");
		         for (Account a : accounts) {
		            System.out.println(a.getId() + ", " + a.getName() + ", "
		                  + a.getPhone());
		         }
		      } else {
		         System.out.println("No records were found for the search.");
		      }
		   } catch (Exception ce) {
		      ce.printStackTrace();
		   }
		}
	
	public void undeleteRecords() {
		   try {
		      // Get the accounts that were last deleted
		      // (up to 5 accounts)
		      QueryResult qResult = connection
		            .queryAll("SELECT Id, SystemModstamp FROM "
		                  + "Account WHERE IsDeleted=true "
		                  + "ORDER BY SystemModstamp DESC LIMIT 5");

		      String[] Ids = new String[qResult.getSize()];
		      // Get the IDs of the deleted records
		      for (int i = 0; i < qResult.getSize(); i++) {
		         Ids[i] = qResult.getRecords()[i].getId();
		      }

		      // Restore the records
		      UndeleteResult[] undelResults = connection.undelete(Ids);

		      // Check the results
		      for (UndeleteResult result : undelResults) {
		         if (result.isSuccess()) {
		            System.out.println("Undeleted Account ID: " + result.getId());
		         } else {
		            if (result.getErrors().length > 0) {
		               System.out.println("Error message: "
		                     + result.getErrors()[0].getMessage());
		            }
		         }
		      }
		   } catch (ConnectionException ce) {
		      ce.printStackTrace();
		   }
		}
	public void doSendEmail() {
		   try {
		      EmailFileAttachment efa = new EmailFileAttachment();
		      byte[] fileBody = new byte[1000000];
		      efa.setBody(fileBody);
		      efa.setFileName("attachment");
		      SingleEmailMessage message = new SingleEmailMessage();
		      message.setBccAddresses(new String[] {
		         "someone@salesforce.com"
		      });
		      message.setCcAddresses(new String[] {
		         "person1@salesforce.com", "person2@salesforce.com"
		      });
		      message.setBccSender(true);
		      message.setEmailPriority(EmailPriority.High);
		      message.setReplyTo("person1@salesforce.com");
		      message.setSaveAsActivity(false);
		      message.setSubject("This is how you use the " + "sendEmail method.");
		      // We can also just use an id for an implicit to address
		      GetUserInfoResult guir = connection.getUserInfo();
		      message.setTargetObjectId(guir.getUserId());
		      message.setUseSignature(true);
		      message.setPlainTextBody("This is the humongous body "
		            + "of the message.");
		      EmailFileAttachment[] efas = { efa };
		      message.setFileAttachments(efas);
		      message.setToAddresses(new String[] { "person3@salesforce.com" });
		      SingleEmailMessage[] messages = { message };
		      com.sforce.soap.enterprise.SendEmailResult[] results = connection.sendEmail(messages);
		      if (results[0].isSuccess()) {
		         System.out.println("The email was sent successfully.");
		      } else {
		         System.out.println("The email failed to send: "
		               + results[0].getErrors()[0].getMessage());
		      }
		   } catch (ConnectionException ce) {
		      ce.printStackTrace();
		   }
		}
	
	  public void allOrNoneHeaderSample() {
		    try {
		      // Create the first contact.
		      SObject[] sObjects = new SObject[2];
		      Contact contact1 = new Contact();
		      contact1.setFirstName("Robin");
		      contact1.setLastName("Van Persie");
		  
		      // Create the second contact. This contact doesn't 
		      // have a value for the required
		      // LastName field so the create will fail.
		      Contact contact2 = new Contact();
		      contact2.setFirstName("Ashley");
		      sObjects[0] = contact1;
		      sObjects[1] = contact2;
		      
		      // Set the SOAP header to roll back the create unless
		      // all contacts are successfully created.
		      connection.setAllOrNoneHeader(true);
		      // Attempt to create the two contacts.
		      SaveResult[] sr = connection.create(sObjects);
		      for (int i = 0; i < sr.length; i++) { 
		        if (sr[i].isSuccess()) {
		          System.out.println("Successfully created contact with id: " + 
		            sr[i].getId() + ".");
		        }
		        else {
		          // Note the error messages as the operation was rolled back 
		          // due to the all or none header.
		          System.out.println("Error creating contact: " + 
		            sr[i].getErrors()[0].getMessage());
		          System.out.println("Error status code: " + 
		            sr[i].getErrors()[0].getStatusCode());
		        }
		      }
		    } catch (ConnectionException ce) {
		      ce.printStackTrace();
		    }
		  }
	  
	  public void allowFieldTruncationSample() {
		  try {
		    Account account = new Account();
		    // Construct a string that is 256 characters long.
		    // Account.Name's limit is 255 characters.
		    String accName = "";
		    for (int i = 0; i < 256; i++) {
		      accName += "a";
		    }
		    account.setName(accName);
		    // Construct an array of SObjects to hold the accounts.
		    SObject[] sObjects = new SObject[1];
		    sObjects[0] = account;
		    // Attempt to create the account. It will fail in API version 15.0
		    // and above because the account name is too long.
		    SaveResult[] results = connection.create(sObjects);
		    System.out.println("The call failed because: "
		       + results[0].getErrors()[0].getMessage());
		    // Now set the SOAP header to allow field truncation.
		    connection.setAllowFieldTruncationHeader(true);
		    // Attempt to create the account now.
		    results = connection.create(sObjects);
		    System.out.println("The call: " + results[0].isSuccess());
		  } catch (ConnectionException ce) {
		    ce.printStackTrace();
		  }
		}
	  
	  public void ownerChangeOptionsHeaderSample() throws ConnectionException {
		     
		    // Create account. Accounts don't transfer activities, notes, or attachments by default
		    
		    Account account = new Account();
		    account.setName("Account");
		    com.sforce.soap.enterprise.SaveResult[] sr = connection.create(new com.sforce.soap.enterprise.sobject.SObject[] { account } );
		    String accountId = null;

		    if(sr[0].isSuccess()) {
		        System.out.println("Successfully saved the account");
		        accountId = sr[0].getId();
		        
		        // Create a note and a task for the account
		        
		        Note note = new Note();
		        note.setTitle("Note Title");
		        note.setBody("Note Body");
		        note.setParentId(accountId);
		        
		        Task task = new Task();
		        task.setWhatId(accountId);
		        
		        sr = connection.create(new com.sforce.soap.enterprise.sobject.SObject[] { note, task } );
		        
		        if(sr[0].isSuccess()) {
		            System.out.println("Successfully saved the note and task");
		            
		           com.sforce.soap.enterprise.QueryResult qr = connection.query("SELECT Id FROM User WHERE FirstName = 'Jane' AND LastName = 'Doe'");
		           String newOwnerId = qr.getRecords()[0].getId();
		           account.setId(accountId);
		           account.setOwnerId(newOwnerId);
		           
		           // Set owner change options so account's child note & task transfer to new owner
		           
		           connection.setOwnerChangeOptions(true, true);
		           connection.update(new com.sforce.soap.enterprise.sobject.SObject[] { account } );
		           
		           // The account, account's note, & task should be transferred to the new owner.
		        }
		       
		   } else {
		       System.out.println("Account save failed: " + sr[0].getErrors().toString());
		    }
		}
		
}