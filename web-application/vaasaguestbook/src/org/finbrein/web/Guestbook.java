package org.finbrein.web;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.finbrein.db.DBHandler;
import org.finbrein.util.MyFileRenamePolicy;

import com.oreilly.servlet.multipart.FilePart;
import com.oreilly.servlet.multipart.MultipartParser;
import com.oreilly.servlet.multipart.ParamPart;
import com.oreilly.servlet.multipart.Part;

public class Guestbook extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5393824134441951476L;
	String userName = "";
	String message = "";
	String userImageName = "";

	MyFileRenamePolicy myFileRenamePolicy = null;

	private String fileRepositoryPath;

	DBHandler dbHandler = null;

	public void init() {

		/*
		 * Below we set the path for the directory where uploaded files are
		 * saved getServletContext().getRealPath(separator) returns the path to
		 * the root directory of the servlet. Variable separator indicates the
		 * directory separator on the system.
		 */

		String separator = System.getProperty("file.separator");

		fileRepositoryPath = getServletContext().getRealPath(separator)
				+ "uploaded_files" + separator;

		dbHandler = new DBHandler();
	}

	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, java.io.IOException {

		HttpSession httpSession = request.getSession();

		/*
		 * if(tableCreated.equals("true")) out.println("<p>" + tableName +
		 * " was created</p>"); else out.println("<p>" + tableName +
		 * " already exists!</p>");
		 */

		try {

			// Here we set the file limit size to 5 MB
			MultipartParser parser = new MultipartParser(request, 5 * 1024 * 1024);

			Part part = null;

			while ((part = parser.readNextPart()) != null) {

				if (part.isParam()) {

					// Here we define parameter, which is a ParamPart
					// by type casting part object.
					ParamPart parameter = (ParamPart) part;

					if (parameter.getName().equals("userName"))
						// Here we initialize userName variable with the
						// value of parameter object
						userName = parameter.getStringValue();

					if (parameter.getName().equals("message"))
						message = parameter.getStringValue();

					message.replace('\'', ' ');

					// Here we initialize the customFileRenamePolicy object with
					// given user name
					myFileRenamePolicy = new MyFileRenamePolicy(userName);

				} else if (part.isFile()) {
					// Here we get some info about the file
					FilePart filePart = (FilePart) part;

					userImageName = filePart.getFileName();

					if (userImageName != null) {

						// filePart.setRenamePolicy(customFileRenamePolicy);
						filePart.setRenamePolicy(myFileRenamePolicy);

						filePart.writeTo(new File(fileRepositoryPath));

						// Here we get the name of the file after applying
						// file renaming policy
						userImageName = filePart.getFileName();

					} else {

						System.out.println("No file was uploaded for this part!<br><br>");

					}

				}

			}
		} catch (IOException e) {
			System.out.println("<p>Some erro happened with file reading and writing!");
		} // handling post data
		

		if (userImageName != null && message != null && userName != null) 
			dbHandler.addData(userImageName, userName, message);
		
		List list = dbHandler.displayAllData();
		httpSession.setAttribute("guestbook", list);

		// RequestDispatcher dispatcher = request
		// .getRequestDispatcher("reportWithoutPagination.jsp");
		RequestDispatcher dispatcher = request.getRequestDispatcher("vaasaguestbook.jsp");
		dispatcher.forward(request, response);	

	}

	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, java.io.IOException {
		// Here we set the MIME type of the response, "text/html"
		doGet(request, response);
	}

}
