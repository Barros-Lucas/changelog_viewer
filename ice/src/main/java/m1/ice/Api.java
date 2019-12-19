package m1.ice;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
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

@RestController
public class Api {

	@GetMapping(value = "/getCommits")
	private String test(@RequestParam String url){
	
		try {
			url = URLDecoder.decode(url, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return "Url : " + url;
		
	}
	
	private StringBuffer create_json (URL _url) throws IOException
	{
		BufferedReader reader = new BufferedReader(new InputStreamReader(_url.openStream()));
		StringBuffer json = new StringBuffer();
		String line;

		while ((line = reader.readLine()) != null) {
		  json.append(line);
		}
		reader.close();
		return json;
	}
	
	private ArrayList<Hashtable> return_format (URL _url, String git) throws IOException
	{
		StringBuffer json = create_json(_url);
		int id;
		ArrayList<Hashtable> com = new ArrayList<Hashtable>();
		JSONArray jsonarray = new JSONArray((String)json.toString());
		for (int i = 0; i < jsonarray.length(); i++) {
		    JSONObject jsonobject = jsonarray.getJSONObject(i);
		    String author;
		    String date;
		    String message;
		    if (git.equals("github"))
		    {
		    	author = jsonobject.getJSONObject("commit").getJSONObject("author").getString("name");
			    date = jsonobject.getJSONObject("commit").getJSONObject("author").getString("date");
			    message = jsonobject.getJSONObject("commit").getString("message");
		    }else
		    {
		    	message = jsonobject.getString("message");
			    author = jsonobject.getString("author_name");
			    date = jsonobject.getString("authored_date");
		    }   
		    
		    Hashtable content = new Hashtable();
            content.put("message", message);
            content.put("author", author);
            content.put("date", date);
            com.add(content);
		}
		
		return com;
	}
	
	@CrossOrigin(origins = "*") 
	@ResponseBody
	@GetMapping(value = "/fromUrl")
	private ArrayList<Hashtable> fromUrl(@RequestParam String url) throws IOException{
		
		System.out.println(url);
		String mots[] = url.split("/");		
		URL _url = null;
		System.out.println(mots[2]);
		if (mots[2].equals("github.com"))
		{
			_url = new URL("https://api.github.com/repos/"+mots[3]+"/"+mots[4]+"/commits");
			return return_format (_url, "github");
	
		}else
		{
			_url = new URL ("https://gitlab.com/api/v4/projects?search="+mots[4]);
			BufferedReader reader = new BufferedReader(new InputStreamReader(_url.openStream()));
			StringBuffer json = new StringBuffer();
			String line;

			while ((line = reader.readLine()) != null) {
			  json.append(line);
			}
			reader.close();

			int id = 0;
			JSONArray jsonarray = new JSONArray((String)json.toString());
			for (int i = 0; i < jsonarray.length(); i++) {
			    JSONObject jsonobject = jsonarray.getJSONObject(i);
			    id = jsonobject.getInt("id");
			}

			_url = new URL("https://gitlab.com/api/v4/projects/"+id+"/repository/commits");
			return return_format (_url, "gitlab");
		}	
	}
	
	private ArrayList<Hashtable> viewer () throws IOException, NoHeadException, GitAPIException
	{
		File gitDir = new File("C:\\Users\\AdminEtu\\Documents\\Eclipse\\.git");

        RepositoryBuilder builder = new RepositoryBuilder();
        Repository repository;
        repository = builder.setGitDir(gitDir).readEnvironment()
                .findGitDir().build();

        Git git = new Git(repository);
        RevWalk walk = new RevWalk(repository);
        RevCommit commit = null;

        Iterable<RevCommit> logs = git.log().call();
        Iterator<RevCommit> i = logs.iterator();
        
        ArrayList<Hashtable> com = new ArrayList<Hashtable>();
        
        while (i.hasNext()) {
            commit = walk.parseCommit( i.next() );
            System.out.println(commit);
            Hashtable content = new Hashtable();
            content.put("id", commit.getAuthorIdent());
            content.put("author", commit.getAuthorIdent());
            content.put("message", commit.getFullMessage());
            com.add(content);
            System.out.println( commit.getFullMessage() );
        }
		return com;
		
	}
	@CrossOrigin(origins = "*") 
	 @PostMapping("/upload") // //new annotation since 4.3
	    public ArrayList<Hashtable> singleFileUpload(@RequestParam("file") MultipartFile file,
	                                   RedirectAttributes redirectAttributes) throws NoHeadException, IOException, GitAPIException  {

	        if (file.isEmpty()) {
	            redirectAttributes.addFlashAttribute("message", "Please select a file to upload");
	        }

	        try {

	            // Get the file and save it somewhere
	            byte[] bytes = file.getBytes();

	            Path path = Paths.get("C:\\Users\\AdminEtu\\Documents\\Eclipse\\Save.zip");
	            Files.write(path, bytes);

	            redirectAttributes.addFlashAttribute("message",
	                    "You successfully uploaded '" + file.getOriginalFilename() + "'");
	            
	            ZipFile zipFile = new ZipFile("C:/Users/AdminEtu/Documents/Eclipse/Save.zip");
	            zipFile.extractAll("C:/Users/AdminEtu/Documents/Eclipse/");
	            	            

	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	        
	        return  viewer();
	    }
	
	
	
}
