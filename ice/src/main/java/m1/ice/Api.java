package m1.ice;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryBuilder;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;

import org.springframework.web.bind.annotation.PostMapping;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import net.lingala.zip4j.ZipFile;

/**
 * Spring Boot Application
 * Back-end log-viewer application
 * We can upload directory or just an url to show log message
 */
@RestController
public class Api {
	
	/**
	 * Create a json from an InputStreamReader
	 * Read content and create json
	 * @param _url
	 * @return json
	 * @throws IOException
	 */
	public StringBuffer create_json (URL _url) throws IOException
	{
		BufferedReader reader = new BufferedReader(new InputStreamReader(_url.openStream()));
		StringBuffer json = new StringBuffer();
		String line;
		//For each line
		while ((line = reader.readLine()) != null) 
		{			
			json.append(line);
		}
		
		reader.close();
		return json;
	}
	
	/**
	 * Create an ArrayList<Hastable>.
	 * Return format : 3 informations (author, date, message).
	 * Uniform return for github and gitlab.
	 * @param _url
	 * @param git
	 * @return ArrayList<Hashtable> 
	 * @throws IOException
	 */
	
	public ArrayList<Hashtable> return_format (URL _url, String git) throws IOException
	{
		//Create json
		StringBuffer json = create_json(_url);
		//Initialize ArrayList
		ArrayList<Hashtable> com = new ArrayList<Hashtable>();
		//Convert to string
		JSONArray jsonarray = new JSONArray((String)json.toString());
		for (int i = 0; i < jsonarray.length(); i++) 
		{
		    JSONObject jsonobject = jsonarray.getJSONObject(i);
		    String author, date, message;
		    //If repository is github's repository
		    if (git.equals("github"))
		    {
		    	author = jsonobject.getJSONObject("commit").getJSONObject("author").getString("name");
			    date = jsonobject.getJSONObject("commit").getJSONObject("author").getString("date");
			    message = jsonobject.getJSONObject("commit").getString("message");
			  
			//else is gitlab's repository
		    }else
		    {
		    	message = jsonobject.getString("message");
			    author = jsonobject.getString("author_name");
			    date = jsonobject.getString("authored_date");
		    }   
		    
		    //Save on Hashtable
		    Hashtable content = new Hashtable();
            content.put("message", message);
            content.put("author", author);
            content.put("date", date);
            //Save on ArrayList
            com.add(content);
		}
		
		return com;
	}
	
	/**
	 * Create request for the well API (github or gitlab).
	 * If the RequestParam url contain github, API github is called else is the other.
	 * Automatic detection of type of repository.
	 * @param url
	 * @return ArrayList<Hashtable> 
	 * @throws IOException
	 */
	
	@CrossOrigin(origins = "*") 
	@ResponseBody
	@GetMapping(value = "/fromUrl")
	public ArrayList<Hashtable> fromUrl(@RequestParam String url) throws IOException
	{		
		//Split url to check if is a gitlab or github repository
		String mots[] = url.split("/");		
		URL _url = null;
		//If github
		if (mots[2].equals("github.com"))
		{
			//Create url
			_url = new URL("https://api.github.com/repos/"+mots[3]+"/"+mots[4]+"/commits");
			return return_format (_url, "github");
		//gitlab
		}else
		{
			//Create url to get the repository id
			_url = new URL ("https://gitlab.com/api/v4/projects?search="+mots[4]);
			//Convert to json
			BufferedReader reader = new BufferedReader(new InputStreamReader(_url.openStream()));
			StringBuffer json = new StringBuffer();
			String line;

			while ((line = reader.readLine()) != null) 
			{				
				json.append(line); 
			}
			reader.close();
			
			int id = 0;
			//Convert to string
			JSONArray jsonarray = new JSONArray((String)json.toString());
			for (int i = 0; i < jsonarray.length(); i++) 
			{	
				//Save id
			    JSONObject jsonobject = jsonarray.getJSONObject(i);
			    id = jsonobject.getInt("id");    
			}
			
			//Create url to get log message
			_url = new URL("https://gitlab.com/api/v4/projects/"+id+"/repository/commits");
			return return_format (_url, "gitlab");
		}	
	}
	
	/**
	 * Use to get some log messages from a .git directory.
	 * Take the directory save previously and get log message.
	 * @return ArrayList<Hashtable> 
	 * @throws IOException
	 * @throws NoHeadException
	 * @throws GitAPIException
	 */
	
	public ArrayList<Hashtable> viewer () throws IOException, NoHeadException, GitAPIException
	{
		//Url to the .git directory
		File gitDir = new File("C:\\Users\\AdminEtu\\Documents\\Eclipse\\.git");
        RepositoryBuilder builder = new RepositoryBuilder();
        Repository repository;
        repository = builder.setGitDir(gitDir).readEnvironment()
                .findGitDir().build();
        
        //Create Git Oject
        Git git = new Git(repository);
        RevWalk walk = new RevWalk(repository);
        RevCommit commit = null;

        Iterable<RevCommit> logs = git.log().call();
        Iterator<RevCommit> i = logs.iterator();
        
        ArrayList<Hashtable> com = new ArrayList<Hashtable>();
        //For each save informations
        while (i.hasNext()) 
        {        	
            commit = walk.parseCommit( i.next() );
            Hashtable content = new Hashtable();
            content.put("id", commit.getAuthorIdent());
            content.put("author", commit.getAuthorIdent());
            content.put("message", commit.getFullMessage());
            com.add(content);            
        }
        
		return com;
		
	}
	
	/**
	 * Used to save file sent by users
	 * Unzip this file and save it on a directory
	 * @param file
	 * @param redirectAttributes
	 * @return ArrayList<Hashtable> 
	 * @throws NoHeadException
	 * @throws IOException
	 * @throws GitAPIException
	 */
	@CrossOrigin(origins = "*") 
	 @PostMapping("/upload") // //new annotation since 4.3
	    public ArrayList<Hashtable> singleFileUpload(@RequestParam("file") MultipartFile file,
	                                   RedirectAttributes redirectAttributes) throws NoHeadException, IOException, GitAPIException  {
			
			//test if is empty
	        if (file.isEmpty()) 
	        {	        	
	            redirectAttributes.addFlashAttribute("message", "Please select a file to upload");
	        }
	        
	        //Save .zip somewhere
	        try 
	        {
	            // Get the file and save it somewhere
	            byte[] bytes = file.getBytes();
	            Path path = Paths.get("C:\\Users\\AdminEtu\\Documents\\Eclipse\\Save.zip");
	            Files.write(path, bytes);

	            redirectAttributes.addFlashAttribute("message",
	                    "You successfully uploaded '" + file.getOriginalFilename() + "'");
	            
	            //Unzip directory
	            ZipFile zipFile = new ZipFile("C:/Users/AdminEtu/Documents/Eclipse/Save.zip");
	            zipFile.extractAll("C:/Users/AdminEtu/Documents/Eclipse/"); 	            

	        } catch (IOException e) {
	        	
	            e.printStackTrace();
	            
	        }
	        
	        //log-Viwer
	        return  viewer();
	    }	
}
