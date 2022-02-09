package browser;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;
import java.util.StringTokenizer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

// The tutorial can be found just here on the SSaurel's Blog : 
// https://www.ssaurel.com/blog/create-a-simple-http-web-server-in-java
// Each Client Connection will be managed in a dedicated Thread
public class Server implements Runnable {

	static final File WEB_ROOT = new File("./server-browser/");
	static final String DEFAULT_FILE = "/browser/src/main/java/browser/resources/index/index.html";
	static final String FILE_NOT_FOUND = "/browser/src/main/java/browser/resources/404/404.html";
	static final String METHOD_NOT_SUPPORTED = "/browser/src/main/java/browser/resources/not_supported/not_supported.html";
	// port to listen connection
	static final int PORT = 8080;

	// verbose mode
	static final boolean verbose = true;

	// Client Connection via Socket Class
	private Socket connect;

	public Server(Socket c) {
		connect = c;
	}

	public static void main(String[] args) {
		try {
			ServerSocket serverConnect = new ServerSocket(PORT);
			System.out.println("Server started.\nListening for connections on port : " + PORT + " ...\n");

			// we listen until user halts server execution
			while (true) {
				Server myServer = new Server(serverConnect.accept());

				if (verbose) {
					System.out.println("Connecton opened. (" + new Date() + ")");
				}

				// create dedicated thread to manage the client connection
				Thread thread = new Thread(myServer);
				thread.start();
			}

		} catch (IOException e) {
			System.err.println("Server Connection error : " + e.getMessage());
		}
	}

	@Override
	public void run() {
		// we manage our particular client connection
		BufferedReader in = null;
		PrintWriter out = null;
		BufferedOutputStream dataOut = null;
		String fileRequested = null;

		try {
			// we read characters from the client via input stream on the socket
			in = new BufferedReader(new InputStreamReader(connect.getInputStream()));
			// we get character output stream to client (for headers)
			out = new PrintWriter(connect.getOutputStream());
			// get binary output stream to client (for requested data)
			dataOut = new BufferedOutputStream(connect.getOutputStream());

			// get first line of the request from the client
			String input = in.readLine();
			// we parse the request with a string tokenizer
			StringTokenizer parse = new StringTokenizer(input);
			String method = parse.nextToken().toUpperCase(); // we get the HTTP method of the client
			// we get file requested
			fileRequested = parse.nextToken().toLowerCase();

			// we support only GET and HEAD methods, we check
			if (!method.equals("GET") && !method.equals("HEAD")) {
				if (verbose) {
					System.out.println("501 Not Implemented : " + method + " method.");
				}

				// we return the not supported file to the client
				File file = new File(WEB_ROOT, METHOD_NOT_SUPPORTED);
				int fileLength = (int) file.length();
				String contentMimeType = "text/html";
				// read content to return to client
				byte[] fileData = readFileData(file, fileLength);

				// we send HTTP Headers with data to client
				out.println("HTTP/1.1 501 Not Implemented");
				out.println("Server: Java HTTP Server from SSaurel : 1.0");
				out.println("Date: " + new Date());
				out.println("Content-type: " + contentMimeType);
				out.println("Content-length: " + fileLength);
				out.println(); // blank line between headers and content, very important !
				out.flush(); // flush character output stream buffer
				// file
				dataOut.write(fileData, 0, fileLength);
				dataOut.flush();

			} else {
				byte[] fileData = null;
				int fileLength = 0;
				// GET or HEAD method
				if (fileRequested.endsWith("/")) {
					fileRequested += DEFAULT_FILE;
				}
				if (fileRequested.endsWith("/classe.json")) { // quando richiedo il file /classe.json
					XmlMapper xmlMapper = new XmlMapper();

					// read file and put contents into the string
					String readContent = new String(
							Files.readAllBytes(Paths.get("server-browser/browser/src/main/java/browser/classe.xml")));

					// deserialize from the XML into a Root object
					root deserializedData = xmlMapper.readValue(readContent, root.class);

					// transformo la classe deserialized data in una stringa JSON classe.json
					ObjectMapper objectMapper = new ObjectMapper();
					String jsonString = objectMapper.writerWithDefaultPrettyPrinter()
							.writeValueAsString(deserializedData);

					// vado a leggere lunghezza e byte della nuova stringa JSON
					fileLength = jsonString.length();
					fileData = jsonString.getBytes();
				} else if (fileRequested.endsWith("/punti-vendita.xml")) { // quando richiedo il file /classe.json
					ObjectMapper objectMapper = new ObjectMapper();

					// leggo il file json e lo trasformo in una stringa
					String json = new String(
							Files.readAllBytes(Paths.get("server-browser/browser/src/main/java/browser/puntiVendita.json")));

					// deserialize from the JSON into a puntiRisultati object
					puntiVendita punti = objectMapper.readValue(json, puntiVendita.class);

					// trasformo la classe punti in una stringa XML
					XmlMapper xmlMapper = new XmlMapper();
					String xmlString = xmlMapper.writerWithDefaultPrettyPrinter().writeValueAsString(punti);

					// vado a leggere lunghezza e byte della nuova stringa XML
					fileLength = xmlString.length();
					fileData = xmlString.getBytes();
				} else {

					File file = new File(WEB_ROOT, fileRequested);
					fileLength = (int) file.length();
					fileData = readFileData(file, fileLength);
				}

				String content = getContentType(fileRequested);
				if (method.equals("GET")) { // GET method so we return content

					// send HTTP Headers
					out.println("HTTP/1.1 200 OK");
					out.println("Server: Java HTTP Server from SSaurel : 1.0");
					out.println("Date: " + new Date());
					out.println("Content-type: " + content);
					out.println("Content-length: " + fileLength);
					out.println(); // blank line between headers and content, very important !
					out.flush(); // flush character output stream buffer

					dataOut.write(fileData, 0, fileLength);
					dataOut.flush();
				}

				if (verbose) {
					System.out.println("File " + fileRequested + " of type " + content + " returned");
				}

			}

		} catch (FileNotFoundException fnfe) {
			try {
				fileNotFound(out, dataOut, fileRequested);
			} catch (IOException ioe) {
				System.err.println("Error with file not found exception : " + ioe.getMessage());
			}

		} catch (IOException ioe) {
			System.err.println("Server error : " + ioe);
		} finally {
			try {
				in.close();
				out.close();
				dataOut.close();
				connect.close(); // we close socket connection
			} catch (Exception e) {
				System.err.println("Error closing stream : " + e.getMessage());
			}

			if (verbose) {
				System.out.println("Connection closed.\n");
			}
		}

	}

	private byte[] readFileData(File file, int fileLength) throws IOException {
		FileInputStream fileIn = null;
		byte[] fileData = new byte[fileLength];

		try {
			fileIn = new FileInputStream(file);
			fileIn.read(fileData);
		} finally {
			if (fileIn != null)
				fileIn.close();
		}

		return fileData;
	}

	// return supported MIME Types
	private String getContentType(String fileRequested) {
		if (fileRequested.endsWith(".htm") || fileRequested.endsWith(".html"))
			return "text/html";
		if (fileRequested.endsWith(".css"))
			return "text/css";
		if (fileRequested.endsWith(".js"))
			return "text/javascript";
		if (fileRequested.endsWith(".png"))
			return "immage/png";
		if (fileRequested.endsWith(".jpg"))
			return "immage/jpg";
		else if (fileRequested.endsWith(".json"))
			return "application/json";
		else if (fileRequested.endsWith(".xml"))
			return "application/xml";
		else
			return "text/plain";
	}

	private void fileNotFound(PrintWriter out, OutputStream dataOut, String fileRequested) throws IOException {
		File file = new File(WEB_ROOT, FILE_NOT_FOUND);
		int fileLength = (int) file.length();
		String content = "text/html";
		byte[] fileData = readFileData(file, fileLength);

		out.println("HTTP/1.1 404 File Not Found");
		out.println("Server: Java HTTP Server from SSaurel : 1.0");
		out.println("Date: " + new Date());
		out.println("Content-type: " + content);
		out.println("Content-length: " + fileLength);
		out.println(); // blank line between headers and content, very important !
		out.flush(); // flush character output stream buffer

		dataOut.write(fileData, 0, fileLength);
		dataOut.flush();

		if (verbose) {
			System.out.println("File " + fileRequested + " not found");
		}
	}

}