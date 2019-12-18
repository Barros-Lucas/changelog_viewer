package m1.ice;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
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
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;

import org.springframework.web.bind.annotation.PostMapping;

import org.springframework.web.bind.annotation.RequestParam;
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
            Hashtable content = new Hashtable();
            content.put("id", commit.getName());
            content.put("author", commit.getCommitterIdent());
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
