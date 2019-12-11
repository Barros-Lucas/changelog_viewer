package changelog.viewer;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryBuilder;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;


public class App 
{
    public static void main( String[] args ) throws IOException, NoHeadException, GitAPIException
    {

        File gitDir = new File("/home/nicolas/Dev/apet/.git");

        RepositoryBuilder builder = new RepositoryBuilder();
        Repository repository;
        repository = builder.setGitDir(gitDir).readEnvironment()
                .findGitDir().build();

        Git git = new Git(repository);
        RevWalk walk = new RevWalk(repository);
        RevCommit commit = null;

        Iterable<RevCommit> logs = git.log().call();
        Iterator<RevCommit> i = logs.iterator();

        while (i.hasNext()) {
            commit = walk.parseCommit( i.next() );
            System.out.println( commit.getFullMessage() );
       }
        
        
    }
}
