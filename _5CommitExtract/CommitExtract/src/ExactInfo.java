
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
//求commit的文件指标：修改文件数量 增、减代码行总数 单个文件的增、减行数 单个文件代码修改行数、文件代码修改总行数
//FileNum	TotalAddLines	TotalDeleteLines	FileName	AddLines	DeleteLines	CodeLines	FileAdd	FileDelete
public class ExactInfo {
    public static void main(String[] args) throws IOException {
        String ProjectName = "avro";
        String CsvPath = "D:\\Postgraduate\\Metric-tool\\TestOutput\\" + ProjectName + "\\Metric\\commit\\FinalMetric\\12MetricResults.csv";
        String MapPath = "D:\\Postgraduate\\Metric-tool\\myproject\\" + ProjectName + "\\map\\12Map.txt";

        String Logpath = "D:\\Postgraduate\\Metric-tool\\myproject\\" + ProjectName + "\\log\\avro12_log.txt";
        String FileList = "D:\\Postgraduate\\Metric-tool\\myproject\\" + ProjectName + "\\FileList\\avro1_file.txt";
        String FileDepsPath = "D:\\Postgraduate\\Metric-tool\\TestOutput\\" + ProjectName + "\\commit12";
        String Outpath = "D:\\Postgraduate\\Metric-tool\\TestOutput\\CommitExtract";

        String ProjectPath = "D:\\Postgraduate\\Metric-tool\\myproject\\" + ProjectName;
        String Gitpath = "D:\\Postgraduate\\Metric-tool\\myproject\\" + ProjectName + "\\.git";


        List<Integer> AcdcCommit = new ArrayList<>();//存放ACDC聚类下架构相似度小于规定值 的commmit号码 以及hash码
        List<Integer> FcaCommit = new ArrayList<>();//存放FCA聚类下架构相似度小于规定值 的commmit号码

        LinkedHashMap<Integer, String> HashCode = new LinkedHashMap<>();//存放所有commit的hash码

        BufferedReader fl = new BufferedReader(new FileReader(FileList));//s为log地址
        LinkedHashMap<String, Integer> fc = new LinkedHashMap<>();//存放文件代码行数

        String outpath = Outpath + "\\FileInfo_FCA.csv";
        String outpath2 = Outpath + "\\FileDepsInfo_FCA.csv";
//        写文件
        FileOutputStream fileOutputStream = new FileOutputStream(outpath, true);
        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream, "GBK");
        BufferedWriter bw1 = new BufferedWriter(outputStreamWriter);

        FileOutputStream fileOutputStream2 = new FileOutputStream(outpath2, true);
        OutputStreamWriter outputStreamWriter2 = new OutputStreamWriter(fileOutputStream2, "GBK");
        BufferedWriter bw2 = new BufferedWriter(outputStreamWriter2);

        //获取第一个提交时的文件代码行数
        String s1;
        while((s1 = fl.readLine()) != null) {
            String[] s2;
            s2 = s1.trim().split(" ");
            fc.put(s2[1], Integer.parseInt(s2[0]));
        }

//        for (String fn : fc.keySet()) {
//            System.out.println(fn + " " + fc.get(fn));
//
//        }

        String FirstCommitID = GetHashCode(HashCode, MapPath);
        GetCommitNumber(AcdcCommit,FcaCommit,CsvPath);


        Repository RPS = getRepository(ProjectPath); //获得库对象
        Git git = getGitObj(RPS);//获得git对象
        for(int i = 0; i < FcaCommit.size(); i++) {
            int first = 0;
            String[] CommitInfo = new String[1000];
            String CommitID = HashCode.get(FcaCommit.get(i)).trim();

            FileRepository repo;
            RevWalk rw;
            try{
                repo = new FileRepository(new File(Gitpath));
                rw = new RevWalk(repo);
            }catch (IOException e1) {
                throw new RuntimeException(e1);
            }
            RevCommit newcommit;
            RevCommit oldcommit;



            if(HashCode.get(FcaCommit.get(i)).trim() == FirstCommitID.trim()){
                //diff 第一个commit与其上一个commit 减去其增加的文件数
                newcommit = rw.parseCommit(repo.resolve(FirstCommitID.trim()));
                oldcommit = rw.parseCommit(newcommit.getParent(0).getId());
                first = 1;

            }
            else if(HashCode.get(FcaCommit.get(i)).trim() != FirstCommitID.trim() && i == 0){//等于1但不是本版本第一个commit diff 第一个commit和自己的上一个commit 加上增加的文件数
                RevCommit thiscommit = rw.parseCommit(repo.resolve(CommitID));
                newcommit = rw.parseCommit(thiscommit.getParent(0).getId());
                oldcommit = rw.parseCommit(repo.resolve(FirstCommitID.trim()));
            }
            else{//其余情况算文件代码总行数 diff 上一个提取的commit的parent 到自己的parent
                RevCommit thiscommit = rw.parseCommit(repo.resolve(CommitID));
                newcommit = rw.parseCommit(thiscommit.getParent(0).getId());
                RevCommit lastCommit =  rw.parseCommit(repo.resolve(HashCode.get(FcaCommit.get(i - 1)).trim()));
                oldcommit = rw.parseCommit(lastCommit.getParent(0).getId());
            }
            System.out.println(HashCode.get(FcaCommit.get(i)).trim());
//diff操作
            try {
//                FileRepository repo = new FileRepository(new File(Gitpath));
//                RevWalk rw = new RevWalk(repo);
//                RevCommit thiscommit = rw.parseCommit(repo.resolve("4e7eafa0669dace70113cb5e92ac075a207f0856"));
//                RevCommit newcommit = rw.parseCommit(thiscommit.getParent(0).getId());
//                RevCommit lastcommit = rw.parseCommit(repo.resolve("21a4eb06f5be66acfe2eaedaa8d7988a44b37338"));
//                RevCommit oldcommit = rw.parseCommit(lastcommit.getParent(0).getId());

                DiffFormatter df = new DiffFormatter(DisabledOutputStream.INSTANCE);
                df.setRepository(repo);
                df.setDiffComparator(RawTextComparator.DEFAULT);
                df.setDetectRenames(true);
                List<DiffEntry> diffs;
                //求文件代码总行数
                diffs = df.scan(oldcommit.getTree(), newcommit.getTree());
               // filesChanged = diffs.size();
                DiffEntry.Side sd = null;
                for (DiffEntry diff : diffs) {
                    //FileInfo FI = new FileInfo();
                    int fclinesAdded = 0;
                    int fclinesDeleted = 0;
//                    System.out.println(diff.getOldPath());
//                    System.out.println(diff.getNewPath());

                    for (Edit edit : df.toFileHeader(diff).toEditList()) {

                        fclinesAdded += edit.getEndB() - edit.getBeginB();
                        fclinesDeleted += edit.getEndA() - edit.getBeginA();
                        //System.out.println(edit);
//                        System.out.println(edit.getEndB() - edit.getBeginB());
//                        System.out.println(edit.getEndA() - edit.getBeginA());
                    }
                    int changedline = fclinesAdded - fclinesDeleted;

                    //System.out.println(changedline);
                    if(first == 1){
                        if(diff.getOldPath() != diff.getNewPath()){
                            if(diff.getNewPath().contains("/dev/null")){//删除文件
                                fc.put(diff.getOldPath(),0 - changedline);
                            }
                            else if(diff.getOldPath().contains("/dev/null")){//增加文件
                                //fc.remove(diff.getOldPath());
                                fc.put(diff.getNewPath(),0);
                            }
                            else{//文件改名
                                int Newlines = fc.get(diff.getNewPath());
                                //fc.remove(diff.getNewPath());
                                fc.put(diff.getNewPath(),Newlines - changedline);
                            }
                        }
                        else{
                            fc.put(diff.getPath(sd), fc.get(diff.getPath(sd)) + changedline);
                        }
                    }
                    else {
                        if(diff.getOldPath() != diff.getNewPath()){
                            if(diff.getNewPath().contains("/dev/null")){//删除文件

//                                if(diff.getOldPath().contains("lang/java/maven-plugin/src/test/resources/unit/idl/pom-jsr310.xml"))
//                                    System.out.println("---" + diff.getOldPath() +  " " +fc.get(diff.getOldPath()) + " " + changedline);

                                fc.remove(diff.getOldPath().trim());
                                //FileName = diff.getOldPath();
                            }
                            else if(diff.getOldPath().contains("/dev/null")){//增加文件
                                fc.put(diff.getNewPath().trim(),changedline);

//                                if(diff.getNewPath().contains("lang/java/maven-plugin/src/test/resources/unit/idl/pom-jsr310.xml"))
//                                    System.out.println("+++" + diff.getNewPath() +  " "+ fc.get(diff.getNewPath())+ " " + changedline);
                                //System.out.print(changedline);
                                //System.out.print(fc.get(diff.getOldPath()));
                            }
                            else{//文件改名
                                if(fc.get(diff.getOldPath()) != null) {//旧路径没被新路径替换  //关键语句！！
                                    int oldlines = fc.get(diff.getOldPath());

//                                    if(diff.getNewPath().contains("lang/java/maven-plugin/src/test/resources/unit/idl/pom-jsr310.xml"))
//                                        System.out.println("+-+" + diff.getOldPath() +  " " +fc.get(diff.getOldPath()) + " " + changedline);
                                    //不删除旧文件
                                        fc.put(diff.getNewPath(), oldlines + changedline);
                                    }
                                }
                            }

                        else{
                            //if(fc.containsValue(diff.getPath(sd)))
                            int lines = fc.get(diff.getPath(sd));
                            fc.put(diff.getPath(sd), lines + changedline);
//                            if(diff.getPath(sd).contains("lang/java/maven-plugin/src/test/resources/unit/idl/pom-jsr310.xml"))
//                                System.out.println("+-+-" + diff.getPath(sd) + " " + lines + " " + changedline);
                            //else
                               // fc.put(diff.getPath(sd), changedline);
                        }
                    }
                }
//                int m1 = fc.get("lang/perl/xt/schema.t");
//                System.out.print("*********************************" + m1 + "********************************");
//                for (String s : fc.keySet()) {
//                    System.out.println(s + " " + fc.get(s));
//                }

                int linesAdded = 0;
                int linesDeleted = 0;
                int filesChanged = 0;
                //求剩余五个指标
                newcommit = rw.parseCommit(repo.resolve(CommitID));
                oldcommit = rw.parseCommit(newcommit.getParent(0).getId());
                List<DiffEntry> diffs2;
                diffs2 = df.scan(oldcommit.getTree(), newcommit.getTree());
                filesChanged = diffs2.size();

                LinkedHashMap<String, FileInfo> SFinfo = new LinkedHashMap<>();//存放单个文件代码增减行数信息
                for (DiffEntry diff : diffs2) {
                    FileInfo FI = new FileInfo();

//                    System.out.println(diff.getOldPath());
//                    System.out.println(diff.getNewPath());

                    for (Edit edit : df.toFileHeader(diff).toEditList()) {
                        linesAdded += edit.getEndB() - edit.getBeginB();
                        linesDeleted += edit.getEndA() - edit.getBeginA();

                        FI.addNum += edit.getEndB() - edit.getBeginB();
                        FI.deleteNum += edit.getEndA() - edit.getBeginA();
                    }
                    //System.out.println(FI.addNum - FI.deleteNum);
                    String FileName = diff.getPath(sd);//删除的文件名变为dev/null

                    if(diff.getOldPath() != diff.getNewPath()){
                        if(diff.getNewPath().contains("/dev/null")){//删除文件

                            FileName = diff.getOldPath();
                        }
                        else if(diff.getOldPath().contains("/dev/null")){//增加文件

                            fc.put(diff.getNewPath(), 0);
                        }
                        else{//文件改名
                            int oldlines = fc.get(diff.getOldPath());
                            //fc.remove(diff.getOldPath());
                            fc.put(diff.getNewPath(), oldlines);
                        }
                    }
                    SFinfo.put(FileName, FI);
                }
//              先输出文件依赖数指标
//                String[] FileDepsInfo = FileDpes(ProjectName, CommitID, SFinfo, FileDepsPath, FcaCommit.get(i));
//                for (int m = 0; FileDepsInfo[m] != null; m++) {
//                    bw2.write(FileDepsInfo[m]);
//                    bw2.newLine();
//                    bw2.flush();
//                }
//              输出剩余五个指标
                CommitInfo[0] = CommitID + "," + filesChanged + "," + linesAdded + "," + linesDeleted;
                int n = 0;
                for(String s: SFinfo.keySet()){
                    //System.out.println("Filename:" + s);
                    int codelines;
                    codelines = fc.get(s);
                    if(s != null){
                        if(CommitInfo[n] != null)
                            CommitInfo[n] = CommitInfo[n] + "," + s + "," + SFinfo.get(s).addNum + "," +SFinfo.get(s).deleteNum + "," + codelines;
                        else
                            CommitInfo[n] = ",,,," + s + "," + SFinfo.get(s).addNum + "," +SFinfo.get(s).deleteNum  + "," + codelines;
                    }
                    n++;
                }
//                for (int m = 0; CommitInfo[m] != null; m++) {
//                    bw1.write(CommitInfo[m]);
//                    bw1.newLine();
//                    bw1.flush();
//                }
            } catch (IOException e1) {
                throw new RuntimeException(e1);
            }

        System.out.println("**************************************");

        }


        bw1.close();
        bw2.close();

    }

    public static String[] FileDpes(String ProjectName, String CommitID, LinkedHashMap<String, FileInfo> SFinfo, String FileDepsPath, int num) throws IOException {
        String[] FileDepsInfo = new String[10000];
        FileDepsInfo[0] = CommitID;
        FileDepsPath = FileDepsPath + "\\commit" + num + "\\" + ProjectName + "-FileDeps.csv";

        BufferedReader fd = new BufferedReader(new FileReader(FileDepsPath));//读取依赖文件

        int n = 0;
        for(String s:SFinfo.keySet()){
            int HasNODeps = 0;
            int FileDepsNum = 0;
            String fi;
            while((fi = fd.readLine()) != null){
                String[] str = fi.split(",");
                if(str[0].contains(s)){
                    FileDepsNum += Integer.parseInt(str[2]);
                    HasNODeps = 1;
                }
            }
            if(HasNODeps == 1) {
                if (s != null) {
                    if (FileDepsInfo[n] != null)
                        FileDepsInfo[n] = FileDepsInfo[n] + "," + s + "," + FileDepsNum;
                    else
                        FileDepsInfo[n] = "," + s + "," + FileDepsNum;
                }
            }
            else{
                if (s != null) {
                    if (FileDepsInfo[n] != null)
                        FileDepsInfo[n] = FileDepsInfo[n] + "," + s + ",/";
                    else
                        FileDepsInfo[n] = "," + s + ",/";
                }
            }
            n++;
        }
        return FileDepsInfo;
    }
    public static void GetCommitNumber(List<Integer> A,List<Integer> F, String path) throws IOException {
        BufferedReader f = new BufferedReader(new FileReader(path));//s为tag地址
        String str;

        while ((str = f.readLine()) != null)//逐行读文件
        {
            //剔除开头两行索引
            if(!str.contains("commit"))
                continue;
            String[] str2;
            str2 = str.split(",");//将每个单元格分隔开
            String[] CommitName;

            CommitName = str2[0].split("--");//得到commit号码
            CommitName[0] = CommitName[0].substring(6);//到字符串末尾可以不写endindex
            //System.out.print(CommitName[0] + " " + str2[1] + " " + str2[3] + ";");

//需要的数据范围
            if (Float.parseFloat(str2[1]) < 90.0) {
                A.add(Integer.parseInt(CommitName[0]));
            }


            if (Float.parseFloat(str2[3]) < 90.0) {
                F.add(Integer.parseInt(CommitName[0]));
            }

        }

//        for (int i = 0; i < A.size(); i++) {
//            System.out.println(A.get(i));
//        }

        for (int i = 0; i < F.size(); i++) {
            System.out.print(F.get(i) + "   ");
        }


    }

    public static String GetHashCode(HashMap<Integer,String> h, String path){
        String FirstCommitID = null;
        try {
            BufferedReader f = new BufferedReader(new FileReader(path));//s为log地址
            String str;


            while ((str = f.readLine()) != null)//逐行读文件
            {

                //str = str.trim();//去掉空格
                String[] str2;

                str2 = str.split(" ");
                if(str2.length < 2)//最后一行不要
                    continue;

                str2[2] = str2[2].trim();
                int index = str2[2].indexOf("Value");
                str2[2] = str2[2].substring(0,index - 1);
                h.put(Integer.valueOf(str2[4]),str2[2]);
               // System.out.println(str2[2] + " " + str2[4]);
                //返回该版本间第一个提交的hash码
                if(Integer.valueOf(str2[4]) == 1)
                    FirstCommitID = str2[2];
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return FirstCommitID;
    }

    //获取Repository对象
    public static Repository getRepository(String dir) {
        try {
            Repository repository = new FileRepositoryBuilder()
                    .setGitDir(new File(dir + "\\.git"))
                    .readEnvironment()
                    .findGitDir()
                    .build();
            // System.out.println( repository.getBranch());
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

