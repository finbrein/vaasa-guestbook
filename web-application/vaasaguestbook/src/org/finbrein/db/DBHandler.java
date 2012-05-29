package org.finbrein.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.finbrein.util.GuestbookDAO;

public class DBHandler {

	Connection conn = null;
	Statement stmt = null;
	String tableName="guestbook";

	public String openConnection() {
		// For mySQL database the above code would look like this:
		try {
			Properties properties = new Properties();

			//properties.load(new FileInputStream("data.properties"));
			//InputStream in = this.getClass().getClassLoader().getResourceAsStream("/org/finbrein/resources/data.properties");
			//properties.load(in);
			String user = properties.getProperty("username");
			String pass = properties.getProperty("password");
			String url = properties.getProperty("url");

			Class.forName("com.mysql.jdbc.Driver");
			conn = DriverManager.getConnection("jdbc:mysql://mysql.cc.puv.fi:3306/e0700180_guestbook", "e0700180", "JeHD4w76Yj2C");

			// Here we create the statement object for executing SQL commands
			stmt = conn.createStatement();

		} catch (ClassNotFoundException e) {

			return e.getMessage();
		} catch (SQLException e) {

			return e.getMessage();
		}

		return "OK";
	}

	public String closeConnection() {

		try {

			if (conn != null)
				conn.close();

			if (stmt != null)
				stmt.close();
		} catch (SQLException e) {

			return e.getMessage();
		}

		return "OK";
	} // end closeConnection

	public String addData( String userImageName, String name, String message ) {

		if(name==null || message == null)
			return null;

		try {
			openConnection();
			stmt.executeUpdate("insert into " + tableName + "( image, name, message )" + " values('"	+ userImageName + "', '" + name + "', '" + message + "')");
			closeConnection();
		} catch (SQLException e) {

			return e.getMessage();
		}

		return "OK";

	}
	
	private String formatString(String timestamp){
		Date date = null;
		try {
			date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(timestamp);
		} catch (ParseException e) {
			e.getMessage();
		}
		String newFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm").format(date);
	    return newFormat;
	}

	public List displayAllData() 
	{

	

		try {

			openConnection();

			ResultSet resultSet= stmt.executeQuery("select * from " + tableName);


			List list = new ArrayList();
			
			SimpleDateFormat sd = new SimpleDateFormat();
			
			while(resultSet.next()){
				//queryResults+="<td><img src=" + "uploaded_files/" + resultSet.getString(2) +" width='90' height='90'/></td>";
	
				list.add(new GuestbookDAO(resultSet.getString(2), resultSet.getString(3), resultSet.getString(4), formatString(resultSet.getString(5)) ));
				//list.add(new GuestbookDAO(resultSet.getString(1), resultSet.getString(2), resultSet.getString(3), resultSet.getString(4), resultSet.getString(5) ));
				
			}		

			closeConnection();


			return list;


		} catch (SQLException e) {

			return null;
		}



	}

	public List<String> displayAllDataJson() 
	{
		try {

			openConnection();

			ResultSet resultSet= stmt.executeQuery("select * from " + tableName);

			List<String> data = new ArrayList<String>();
	
			while(resultSet.next()){
				
				data.add(resultSet.getString(1));
				data.add(resultSet.getString(2));
				data.add(resultSet.getString(3));				
				data.add(resultSet.getString(4));
				data.add(resultSet.getString(5));
			
			}

			closeConnection();

			return data;


		} catch (SQLException e) {

			return null;
		}



	}
}
