import java.io.*;
import java.util.*;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.Edit;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.util.io.DisabledOutputStream;
public class Test {
    public static void main(String[] args) throws IOException, GitAPIException {

        String Gitpath = "D:\\Metric-tool\\myproject\\tika\\.git";
        String Outpath = "E:\\Metric-tool\\TestOutput\\CommitExtract";
        String ProjectPath = "D:\\Metric-tool\\myproject\\tika";
        List<Integer> AcdcCommit = new ArrayList<>();//存放ACDC聚类下架构相似度小于90 的commmit号码 以及hash码
        List<Integer> FcaCommit = new ArrayList<>();//存放FCA聚类下架构相似度小于90 的commmit号码

        String outpath = Outpath + "\\FileInfo_FCA.csv";

        //diffTest(Gitpath);
        /*****在这里填要checkout的hashcode****/
        String CommitID = "4ca79ab27c9fdf63a59a1bc14f494eff89919eaa";
        //checkoutTest(ProjectPath,CommitID);
        Repository RPS = getRepository(ProjectPath); //获得库对象
        Git git = getGitObj(RPS);//获得git对象
        git.checkout().setName(CommitID).call();
//        Iterable<RevCommit> commits = git.log().call();

        //String s = "src/java/org/apache/avro/specific/SpecificDatumWriter.java";
        //ArrayList<RevCommit> commits1 =  new  LogFollowCommand(RPS, CommitID, s).call();
//        String AuthorName = "";
//        int CommitTime = 0;
//        int i = 0;
//        int EXP = 0;
//        int REXP = 0;
//        for (RevCommit commit : commits) {
//            if(i == 0) {
//                AuthorName = commit.getAuthorIdent().getName();
//                CommitTime = commit.getCommitTime();
//            }
//            System.out.println(commit.getId());
//            System.out.println(commit.getAuthorIdent().getName());
//            if( i != 0 && AuthorName.equals(commit.getAuthorIdent().getName())) {
//                EXP++;
//                if(i != 0 && CommitTime - commit.getCommitTime() <= 1200000)
//                    REXP++;
//            }
//
//            i++;
//
//        }
//        System.out.println("EXP:" + EXP);
//        System.out.println("REXP:" + REXP);
////        /*********DiffTest********/
//        diffTest(Gitpath);
    }
    public static void checkoutTest(String ProjectPath, String CommitID) throws GitAPIException {
        Repository RPS = getRepository(ProjectPath); //获得库对象
        Git git = getGitObj(RPS);//获得git对象
        git.checkout().setName(CommitID).call();


    }
    public static void diffTest(String Gitpath) throws IOException {

        FileRepository repo;
        RevWalk rw;
        try {
            repo = new FileRepository(new File(Gitpath));
            rw = new RevWalk(repo);
        } catch (IOException e1) {
            throw new RuntimeException(e1);
        }
        RevCommit newcommit;
        RevCommit oldcommit;
        RevCommit thiscommit = rw.parseCommit(repo.resolve("62806bad9162f22afe69f01e97ea96321d83710a"));
        newcommit = rw.parseCommit(thiscommit.getParent(0).getId());
        RevCommit lastCommit = rw.parseCommit(repo.resolve("eeb1023f9630a576bf8ea2d5b0e485f565e90e42"));
        oldcommit = rw.parseCommit(lastCommit.getParent(0).getId());
//
        newcommit = rw.parseCommit(repo.resolve("710a001a1b41d465db7a300bfcc03040fcef03b3"));
        oldcommit = rw.parseCommit(repo.resolve("fe9330e56d2d6d356344d5152ca8b4ff3b52b89c"));
//
        DiffFormatter df = new DiffFormatter(DisabledOutputStream.INSTANCE);
        df.setRepository(repo);
        df.setDiffComparator(RawTextComparator.DEFAULT);
        df.setDetectRenames(true);
        List<DiffEntry> diffs;
//        求文件代码总行数
        diffs = df.scan(oldcommit.getTree(), newcommit.getTree());
        if(diffs.isEmpty())
            System.out.println("diffs is empty");
        //filesChanged = diffs.size();
        DiffEntry.Side sd = null;
        int k = 0;
        for (DiffEntry diff : diffs) {
            FileInfo FI = new FileInfo();
            System.out.println(k);
//            int fclinesAdded = 0;
//            int fclinesDeleted = 0;
            System.out.println(diff.getOldPath());
//            System.out.println(diff.getNewPath());

            for (Edit edit : df.toFileHeader(diff).toEditList()) {

                //fclinesAdded += edit.getEndB() - edit.getBeginB();
                //fclinesDeleted += edit.getEndA() - edit.getBeginA();
                System.out.println(edit);
//                        System.out.println(edit.getEndB() - edit.getBeginB());
                        System.out.println(edit.getEndA() - edit.getBeginA());
            }
//
        }
    }
    public static Repository getRepository(String dir) {
        try {
            Repository repository = new FileRepositoryBuilder()
                    .setGitDir(new File(dir + "\\.git"))
                    .readEnvironment()
                    .findGitDir()
                    .build();
//             System.out.println( repository.getBranch());
            return repository;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

    }

    //获取GIT对象
    public static Git getGitObj(Repository repository) {
        Git git = null;
        git = new Git(repository);
        return git;
    }

    public static AbstractTreeIterator prepareTreeParser(Repository repository, String objectId) throws IOException {
        try (RevWalk walk = new RevWalk(repository)) {
            RevCommit commit = walk.parseCommit(ObjectId.fromString(objectId));
            RevTree tree = walk.parseTree(commit.getTree().getId());
            CanonicalTreeParser treeParser = new CanonicalTreeParser();
            try (ObjectReader reader = repository.newObjectReader()) {
                treeParser.reset(reader, tree.getId());
            }
            walk.dispose();
            return treeParser;
        }
    }
}
