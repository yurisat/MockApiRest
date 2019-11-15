package com.yurisatiro.mockrest.application;

import static spark.Spark.after;
import static spark.Spark.before;
import static spark.Spark.get;
import static spark.Spark.options;
import static spark.Spark.post;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import spark.Filter;
import spark.Request;
import spark.Response;
import spark.Route;

/**
 * Hello world!
 *
 */
public class App {
	
	private static final String TYPE_APPLICATION_JSON = "application/json";
	private static final String DEFAULT_SEPARATOR_ROUTE = "_";
	private static final String DEFAULT_SEPARATOR_SERVICE = "/";
	private static final String DIRECTORY_OUTPUT = "output.files";
	private static final String METHOD_SERVICE_GET = "get";
	private static final String METHOD_SERVICE_POST = "post";
	private static final  Logger logger = LoggerFactory.getLogger(App.class);
	
	public static void main(String[] args) throws URISyntaxException {
		
		String pathJson = System.getProperty(DIRECTORY_OUTPUT);
		
		if (null == pathJson || pathJson.isEmpty()) {
			System.out.println("Por favor, informe o diretório com os json. Parametro: " + DIRECTORY_OUTPUT);
			return;
		}

		File folder = new File(pathJson);
		
		if (!folder.exists()) {
			System.out.println("Não existe o diretório: " + pathJson);
			return;
		}
		
		File[] listOfFiles = folder.listFiles();
	
		if (null == listOfFiles || listOfFiles.length == 0) {
			System.out.println("Não existe arquivos de configuração no diretório: " + pathJson);
			return;
		}
		
		after(new Filter() {

			@Override
			public void handle(Request request, Response response) {
				logger.debug(request.contextPath());
			}
			
		});
		
		
		options(new Route("/*") {

			@Override
			public Object handle(Request req, Response res) {
				String accessControlRequestHeaders = req.headers("Access-Control-Request-Headers");
				if (accessControlRequestHeaders != null) {
					res.header("Access-Control-Allow-Headers", accessControlRequestHeaders);
				}

				String accessControlRequestMethod = req.headers("Access-Control-Request-Method");
				if (accessControlRequestMethod != null) {
					res.header("Access-Control-Allow-Methods", accessControlRequestMethod);
				}

			    return "OK";
			}
			
		});
		
		before(new Filter() {
			
			@Override
			public void handle(Request req, Response res) {
				res.header("Access-Control-Allow-Origin", "*");
				res.header("Access-Control-Allow-Headers", "*");
				res.type(TYPE_APPLICATION_JSON);
				
			}
		});
		
		loadRoutesInFiles(listOfFiles);
		
	}

	/**
	 * @param listOfFiles
	 */
	private static void loadRoutesInFiles(File[] listOfFiles) {
		for (File file : listOfFiles) {
			
			String typeService = getExtensionFile(file.toString());
			String route = DEFAULT_SEPARATOR_SERVICE 
					+ getNameWithoutExtension(file.getName()).replace(DEFAULT_SEPARATOR_ROUTE, DEFAULT_SEPARATOR_SERVICE);
			
			final String fileJson = file.getPath();
			
			if (typeService.equalsIgnoreCase(METHOD_SERVICE_GET)) {
				
				get(new Route(route) {
				     public Object handle(Request request, Response response) {
				    	 response.type(TYPE_APPLICATION_JSON);
				        return getJsonFile(fileJson);
				     }
				 });
				
			} else if (typeService.equalsIgnoreCase(METHOD_SERVICE_POST)) {
				
				post(new Route(route) {
				     public Object handle(Request request, Response response) {
				    	 response.type(TYPE_APPLICATION_JSON);
				    	 return getJsonFile(fileJson);
				     }
				});
				
			}
		}
	}
	
    public static String getJsonFile(String file) {

        StringBuilder sb = new StringBuilder();

        try (BufferedReader br = Files.newBufferedReader(Paths.get(file), Charset.defaultCharset())) {

            // read line by line
            String line; 
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }

        } catch (IOException e) {
            System.out.format("IOException: %s%n", e);
        }

        return sb.toString();

    }
    
    public static String getExtensionFile(String fileName) {
    	
    	String extension = "";

    	int i = fileName.lastIndexOf('.');
    	if (i > 0) {
    	    extension = fileName.substring(i+1);
    	}
    	
    	return extension;
    	
    }
    
    public static String getNameWithoutExtension(String fileName) {
    	
    	String file = "";

    	int i = fileName.lastIndexOf('.');
    	if (i > 0) {
    	    file = fileName.substring(0, i);
    	}
    	
    	return file;
    	
    }
    
}
