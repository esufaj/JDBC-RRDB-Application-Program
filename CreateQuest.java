import java.util.*;
import java.sql.*;


import pgpass.*;

public class CreateQuest {
	private Connection conDB;
	private String url;
	private String user = "esufaj";
	
	//private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd");//day of quest
	private String day;
	private String realm; // realm of the quest
	private String theme; //theme of the quest
	private Integer amount; // amount of sql given
	private Float seed;
	


//Constructor

public CreateQuest(String[] args) {
	 try {
         // Register the driver with DriverManager.
         Class.forName("org.postgresql.Driver").newInstance();
     } catch (ClassNotFoundException e) {
         e.printStackTrace();
         System.exit(0);
     } catch (InstantiationException e) {
         e.printStackTrace();
         System.exit(0);
     } catch (IllegalAccessException e) {
         e.printStackTrace();
         System.exit(0);
     }
	
	 // URL: Which database?
     //url = "jdbc:postgresql://db:5432/<dbname>?currentSchema=yrb";
     url = "jdbc:postgresql://db:5432/";

     // set up acct info
     // fetch the PASSWD from <.pgpass>
     Properties props = new Properties();
     try {
         String passwd = PgPass.get("db", "*", user, user);
         props.setProperty("user",    "esufaj");
         props.setProperty("password", passwd);
         // props.setProperty("ssl","true"); // NOT SUPPORTED on DB
     } catch(PgPassException e) {
         System.out.print("\nCould not obtain PASSWD from <.pgpass>.\n");
         System.out.println(e.toString());
         System.exit(0);
     }
     
     // Initialize the connection.
     try {
         // Connect with a fall-thru id & password
         //conDB = DriverManager.getConnection(url,"<username>","<password>");
         conDB = DriverManager.getConnection(url, props);
     } catch(SQLException e) {
         System.out.print("\nSQL: database connection error.\n");
         System.out.println(e.toString());
         System.exit(0);
     }  
     
     // Let's have autocommit turned off.  No particular reason here.
     try {
         conDB.setAutoCommit(false);
     } catch(SQLException e) {
         System.out.print("\nFailed trying to turn autocommit off.\n");
         e.printStackTrace();
         System.exit(0);
     }    
     
     if(args.length < 4) {
    	 System.out.println("\nUsage:day, realm, theme, amount, (user), (seed)");
    	 System.exit(0);
     } else if (args.length == 4 || args.length == 5 || args.length == 6) {
    	 try {
    		 day = new String(args[0]);
    		 //java.sql.Date date = java.sql.Date.valueOf(day);
    		 realm = new String(args[1]);
    		 theme = new String(args[2]);
    		 amount = Integer.parseInt(args[3]);
    		 System.out.println("Default username used: esufaj.");
    		 if(args.length == 5) {
    		 user = new String(args[4]);
    		 System.out.println("Current username given: " + user);
    		 }
    		 if(args.length == 6) {
    		 seed = Float.parseFloat(args[5]);
    		 }
    	 }catch (NumberFormatException e) {
             System.out.println("\nUsage: day, realm, theme, amount, user, seed");
             
             System.out.println("Provide a Date for day with format yyyy-MM-dd");
             System.out.println("Provide a String for the realm");
             System.out.println("Provide a String for the theme");
             System.out.println("Provide an Integer for amount. EX: 20000");
             System.out.println("Provide a String for user. EX: john");
             System.out.println("Provide an Float for seed between -1 and 1. EX: 0.618");

             System.exit(0);
         }
     }else if(args.length > 6) {
    	 System.out.println("\nUsage:day, realm, theme, amount, (user), (seed)");
    	 System.exit(0);
     }
    	 

         
     
     // Is the day in the future?
     if (!dayCheck()) {
         System.out.print("Day: " + day);
         System.out.print(" is not in future. Day given should be after " + getcurDay());
         System.out.println(". Please provide a day at least 1 day in the future from " + getcurDay());
         System.out.println("Bye.");
         System.exit(0);
     }
     
     // Is this realm in the database?
     if (!realmCheck()) {
         System.out.print(realm + " is not a valid realm");
         System.out.println(" in the database, please try again.");
         System.out.println("Bye.");
         System.exit(0);
     }
     
     // Is the amount less than or greater than total sql?
     if (!amountCheck()) {
		 System.out.println("Amount is too large. You gave " + amount + ". Amount should be less then " + getsqlTotal());
		 System.out.println("Bye");
         System.exit(0);
     }
     
     // checking if seed is between -1 and 1
     if ( seed != null && (!seedCheck())) {
    	 System.out.println("Seed given is " + seed + ". Seed must be between -1 and 1.");
    	 System.out.println("Bye");
    	 System.exit(0);
     }
     
     
     // if checks are true then we will set seed insert quest and insert loot
     if(dayCheck() && realmCheck() && amountCheck() ) {
    	 if(seed != null) {
    	 setSeed();
    	 }
    	 insertQuest();
    	 insertLoot();
    	 System.out.println("All checks have passed, insertions have been set!.");
     }else {
    	 System.out.println("Insertions denied, check your arguments!");
    	 System.exit(0);
     }
     
     //inserting quest into db
    // insertQuest();
     //inserting loot into db
     //insertLoot();
    	 
     
     
     // Commit.  Okay, here nothing to commit really, but why not...
     try {
         conDB.commit();
     } catch(SQLException e) {
         System.out.print("\nFailed trying to commit.\n");
         e.printStackTrace();
         System.exit(0);
     }    
     // Close the connection.
     try {
         conDB.close();
     } catch(SQLException e) {
         System.out.print("\nFailed trying to close the connection.\n");
         e.printStackTrace();
         System.exit(0);
     }  
}

public java.sql.Date getcurDay() {
	String            queryText = "";     // The SQL text.
    PreparedStatement querySt   = null;   // The query handle.
    ResultSet         answers   = null;   // A cursor.
    java.sql.Date     curDay    = null;	  // return;Placeholder for the cur day in db

    
    queryText =
            "SELECT CURRENT_DATE     ";
    
 // Prepare the query.
    try {
        querySt = conDB.prepareStatement(queryText);
    } catch(SQLException e) {
        System.out.println("SQL#1 failed in prepare");
        System.out.println(e.toString());
        System.exit(0);
    }
    
    // Execute the query.
    try {
    	
        answers = querySt.executeQuery();
    } catch(SQLException e) {
        System.out.println("SQL#1 failed in execute");
        System.out.println(e.toString());
        System.exit(0);
    }
    
    // Any answer?
    try {
        if (answers.next()) {
            curDay = answers.getDate("CURRENT_DATE");
            
        } else {
            //do nothing

        }
    } catch(SQLException e) {
        System.out.println("SQL#1 failed in cursor.");
        System.out.println(e.toString());
        System.exit(0);
    }

    // Close the cursor.
    try {
        answers.close();
    } catch(SQLException e) {
        System.out.print("SQL#1 failed closing cursor.\n");
        System.out.println(e.toString());
        System.exit(0);
    }

    // We're done with the handle.
    try {
        querySt.close();
    } catch(SQLException e) {
        System.out.print("SQL#1 failed closing the handle.\n");
        System.out.println(e.toString());
        System.exit(0);
    }

    return curDay;
}

public boolean dayCheck() {
	boolean dayCheck = false;
	java.sql.Date d1 = java.sql.Date.valueOf(day);
	
	if(d1.after(getcurDay())) {
		dayCheck = true;
	} else { 
		dayCheck = false;
	} return dayCheck;
}



	public boolean realmCheck() {
		String            queryText = "";     // The SQL text.
        PreparedStatement querySt   = null;   // The query handle.
        ResultSet         answers   = null;   // A cursor.

        boolean           inDB      = false;  // Return.
        
        queryText =
                "SELECT *       "
              + "FROM Realm "
              + "WHERE realm = ?     ";
        
     // Prepare the query.
        try {
            querySt = conDB.prepareStatement(queryText);
        } catch(SQLException e) {
            System.out.println("SQL#2 failed in prepare");
            System.out.println(e.toString());
            System.exit(0);
        }
        
        // Execute the query.
        try {
        	querySt.setString(1, realm);
            answers = querySt.executeQuery();
        } catch(SQLException e) {
            System.out.println("SQL#2 failed in execute");
            System.out.println(e.toString());
            System.exit(0);
        }
        
        // Any answer?
        try {
            if (answers.next()) {
                inDB = true;
                //realm = answers.getString("realm");
            } else {
                inDB = false;
         
            }
        } catch(SQLException e) {
            System.out.println("SQL#2 failed in cursor.");
            System.out.println(e.toString());
            System.exit(0);
        }

        // Close the cursor.
        try {
            answers.close();
        } catch(SQLException e) {
            System.out.print("SQL#2 failed closing cursor.\n");
            System.out.println(e.toString());
            System.exit(0);
        }

        // We're done with the handle.
        try {
            querySt.close();
        } catch(SQLException e) {
            System.out.print("SQL#2 failed closing the handle.\n");
            System.out.println(e.toString());
            System.exit(0);
        }

        return inDB;
    }
	
 
	 public void insertQuest() {
	        String            queryText = "";     // The SQL text.
	        PreparedStatement querySt   = null;   // The query handle.
	       
	        int 			   num ;

	        queryText =
	            "INSERT into Quest     "
	          + " (theme, realm, day)"
	          + "values (?, ?, ?)   ";

	        // Prepare the query.
	        try {
	            querySt = conDB.prepareStatement(queryText);
	        } catch(SQLException e) {
	            System.out.println("SQL#3 failed in prepare");
	            System.out.println(e.toString());
	            System.exit(0);
	        }

	        // Execute the query.
	        try {
	            querySt.setString(1, theme);
	            querySt.setString(2, realm);
	            
	            java.sql.Date d1 = java.sql.Date.valueOf(day);
	            querySt.setDate(3, d1);

	             num = querySt.executeUpdate();
	        } catch(SQLException e) {
	            System.out.println("SQL#3 failed in execute");
	            System.out.println(e.toString());
	            System.exit(0);
	        }

	        // We're done with the handle.
	        try {
	            querySt.close();
	        } catch(SQLException e) {
	            System.out.print("SQL#3 failed closing the handle.\n");
	            System.out.println(e.toString());
	            System.exit(0);
	        }
	        	System.out.println("Quest has been inserted with the given Theme: " + theme + ", Realm: " + realm + ", and Day: " + day);
	    }
	
	 
	 
	 
	 public int getsqlTotal() {
	        String            queryText = "";     // The SQL text.
	        PreparedStatement querySt   = null;   // The query handle.
	        ResultSet         answers   = null;   // A cursor.

	        int                total    =  0;     // placeholder for the total sql

	        queryText =
	            "SELECT sum(sql) as total       "
	          + "FROM Treasure " ;
	          

	        // Prepare the query.
	        try {
	            querySt = conDB.prepareStatement(queryText);
	        } catch(SQLException e) {
	            System.out.println("SQL#4 failed in prepare");
	            System.out.println(e.toString());
	            System.exit(0);
	        }

	        // Execute the query.
	        try {
	            answers = querySt.executeQuery();
	        } catch(SQLException e) {
	            System.out.println("SQL#4 failed in execute");
	            System.out.println(e.toString());
	            System.exit(0);
	        }

	        // Any answer?
	        try {
	            if (answers.next()) {
	                total = answers.getInt("total");
	            } else {
	                // do nothing
	            }
	        } catch(SQLException e) {
	            System.out.println("SQL#4 failed in cursor.");
	            System.out.println(e.toString());
	            System.exit(0);
	        }

	        // Close the cursor.
	        try {
	            answers.close();
	        } catch(SQLException e) {
	            System.out.print("SQL#4 failed closing cursor.\n");
	            System.out.println(e.toString());
	            System.exit(0);
	        }

	        // We're done with the handle.
	        try {
	            querySt.close();
	        } catch(SQLException e) {
	            System.out.print("SQL#4 failed closing the handle.\n");
	            System.out.println(e.toString());
	            System.exit(0);
	        }

	        return total;
	    }
	
	 
	 
	 public boolean amountCheck() {
		 boolean amountOK = false;
		 if(getsqlTotal() >= amount) {
			 amountOK = true;
		 }else {
			 amountOK = false;
		 }
		 return amountOK;
	 }

	 
	 public void setSeed() {
	        String            queryText = "";     // The SQL text.
	        PreparedStatement querySt   = null;   // The query handle.
	        ResultSet         answers   = null;   // A cursor.
	        //boolean           inDB      = false;  // Return.
	        
	        queryText =
		            "SELECT setseed(?)       ";
		              
	        
	     

	        // Prepare the query.
	        try {
	            querySt = conDB.prepareStatement(queryText);
	            
	        } catch(SQLException e) {
	            System.out.println("SQL#6 failed in prepare");
	            System.out.println(e.toString());
	            System.exit(0);
	        }

	        // Execute query1.
	        try {
	        	querySt.setFloat(1, seed);
	            answers = querySt.executeQuery();
	            
	        } catch(SQLException e) {
	            System.out.println("SQL#6 failed in execute");
	            System.out.println(e.toString());
	            System.exit(0);
	        }

	        // Close the cursor.
	        try {
	            answers.close();
	            
	        } catch(SQLException e) {
	            System.out.print("SQL#6 failed closing cursor.\n");
	            System.out.println(e.toString());
	            System.exit(0);
	        }

	        // We're done with the handle.
	        try {
	            querySt.close();
	        } catch(SQLException e) {
	            System.out.print("SQL#6 failed closing the handle.\n");
	            System.out.println(e.toString());
	            System.exit(0);
	        }

	        
	    }
	 
	 
	 public boolean seedCheck() {
		
		 boolean seedCheck = false;
		 if(seed > -1 && seed < 1) {
			 seedCheck = true;
		 }else {
			 seedCheck = false;
		 } return seedCheck;
	 }
	 
	 
	 
	 public void insertLoot() {
	   
		  String queryText1 = "select * "
	    		+ "from Treasure "
	    		+ "order by random()";
	    
		  PreparedStatement querySt1 = null; // The query handle.
	    
		  ResultSet tanswers = null; // A cursor.

	    String queryText2 = "insert into Loot "
	    		+ "(loot_id, treasure, theme, realm, day) "
	    		+ "values (?, ?, ?, ?, ?)"; // The SQL text.
	    
	    PreparedStatement querySt2 = null; // The query handle.
	    
	    ResultSet answers = null; // A cursor.

	    String loot = "";
	    int sum = 0;
	    int counter = 0;

	   

	    // prepare query handle
	    try {
	      querySt1 = conDB.prepareStatement(queryText1);
	    } catch(SQLException e) {
	      System.out.println("SQL#7 failed to prepare the handle.\n");
	      System.out.println(e.toString());
	      System.exit(0);
	    } catch(NullPointerException e) {
	      System.out.println("SQL#7 failed to prepare the handle.\n");
	      System.exit(0);
	    }

	    // Execute the query
	    try {
	      tanswers = querySt1.executeQuery();
	    } catch(SQLException e) {
	      System.out.println("SQL#7 failed to execute the handle.\n");
	      System.out.println(e.toString());
	      System.exit(0);
	    }

	   
	    try {
	      
	      while(sum < amount)  {
	        if (tanswers.next()) {
	         
	        	loot = tanswers.getString("treasure");
	          sum = sum + tanswers.getInt("sql");
	          counter++;

	          // prepare the query handle
	          try {
	            querySt2 = conDB.prepareStatement(queryText2);
	          } catch(SQLException e) {
	            System.out.println("SQL#7 failed to prepare the handle.\n");
	            System.out.println(e.toString());
	            System.exit(0);
	          } catch(NullPointerException e) {
	            System.out.println("SQL#7 failed to prepare the handle.\n");
	            System.exit(0);
	          }

	          
	          try {
	            querySt2.setInt(1, counter);
	            querySt2.setString(2, loot);
	            querySt2.setString(3, theme);
	            querySt2.setString(4, realm);
	            
	            java.sql.Date d1 = java.sql.Date.valueOf(day);
	            querySt2.setDate(5, d1);

	            int num = querySt2.executeUpdate();
	            
	          } catch(SQLException e) {
	            System.out.println("SQL#7 failed to execute.\n");
	            System.out.println(e.toString());
	            System.exit(0);
	          } catch(NullPointerException e) {
	            System.out.println("SQL#7 failed to execute.\n");
	            System.exit(0);
	          }

	          // close query handle
	          try {
	            querySt2.close();
	          } catch(SQLException e) {
	            System.out.print("SQL#7 failed closing the handle.\n");
	            System.out.println(e.toString());
	            System.exit(0);
	          }
	          
	        } else {
	          // do nothing
	        }
		
	      }
	    } catch(SQLException e) {
	      System.out.println("SQL#7 failed to execute.\n");
	      System.out.println(e.toString());
	      System.exit(0);
	    }

	    // close query cursor
	    try {
	      tanswers.close();
	    } catch(SQLException e) {
	      System.out.print("SQL#7 failed closing the cursor.\n");
	      System.out.println(e.toString());
	      System.exit(0);
	    }
	    
	    // close query handle
	    try {
	      querySt1.close();
	    } catch(SQLException e) {
	      System.out.print("SQL#7 failed closing the handle.\n");
	      System.out.println(e.toString());
	      System.exit(0);
	    }
	    System.out.println("Insertion into Loot succeeded");
	  }
	 
	

     
     public static void main(String[] args) {
         CreateQuest cq = new CreateQuest(args);
     }

}





