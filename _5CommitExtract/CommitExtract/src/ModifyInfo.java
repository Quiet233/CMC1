import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.*;
import org.eclipse.jgit.errors.MissingObjectException;
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
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.util.io.DisabledOutputStream;

import java.io.*;
import java.util.*;
import java.math.*;
public class ModifyInfo {
    public static void main(String[] args) throws IOException {

        String ProjectName = "avro";
        String CsvPath = "E:\\Metric-tool\\TestOutput\\" + ProjectName + "\\Metric\\commit\\avroMetricResults_release1-2.csv";
        String MapPath = "E:\\Metric-tool\\myproject\\" + ProjectName + "\\map\\12Map.txt";

        String FileList = "E:\\Metric-tool\\myproject\\" + ProjectName + "\\FileList\\avro1_file.txt";//该文件是log中第一个提交的文件代码行数文件
        String FileDepsPath = "E:\\Metric-tool\\TestOutput\\" + ProjectName + "\\commit12";
        //暂时不算文件依赖数了 所以函数中相关部分注释掉了
        String Outpath = "E:\\Metric-tool\\TestOutput\\CommitExtract\\" + ProjectName;

        String Gitpath = "E:\\Metric-tool\\myproject\\" + ProjectName + "\\.git";
//        String ProjectName = args[0];
//        String CsvPath = args[1];
//        String MapPath = args[2];
//
//        String FileList = args[3];//该文件是log中第一个提交的文件代码行数文件
//        // String FileDepsPath = "E:\\Metric-tool\\TestOutput\\" + ProjectName + "\\commit12";
//        //暂时不算文件依赖数了 所以函数中相关部分注释掉了
//        String Outpath = args[5] + "\\" + ProjectName;
//
//        String Gitpath = args[4];
        //String FileDepsPath = "D:\\Metric-tool\\TestOutput\\" + ProjectName + "\\commit12";

        List<Integer> AcdcCommit = new ArrayList<>();//存放ACDC聚类下架构相似度小于规定值 的commmit号码 以及hash码
        List<Integer> FcaCommit = new ArrayList<>();//存放FCA聚类下架构相似度小于规定值 的commmit号码

        LinkedHashMap<Integer, String> HashCode = new LinkedHashMap<>();//存放所有commit的hash码

        BufferedReader fl = new BufferedReader(new FileReader(FileList));//s为log地址
        LinkedHashMap<String, Integer> fc = new LinkedHashMap<>();//存放文件代码行数

        //获取第一个提交时的文件代码行数
        String s1;
        while ((s1 = fl.readLine()) != null) {
            String[] s2;
            s2 = s1.trim().split(" ");
            if(s2[1].contains(".java"))//只存java文件
                fc.put(s2[1], Integer.parseInt(s2[0]));
        }
        //通过map得到所有commitID
        String FirstCommitID = GetHashCode(HashCode, MapPath);
        //根据划线得到需要提取的commit
        GetCommitNumber(AcdcCommit, FcaCommit, CsvPath);

        File file = new File(Outpath);
        if(!file.exists())
            file.mkdirs();
        String outpath1 = Outpath + "\\A2A_CommitFileInfo_FCA.csv";
        //String outpath2 = Outpath + "\\A2A_FileDepsInfoAfter_FCA.csv";
        //String outpath3 = Outpath + "\\A2A_FileDepsInfoBefore_FCA.csv";



        //CommitFileInfo(ProjectName, HashCode, fc, AcdcCommit, outpath1,outpath2, Gitpath,FileDepsPath, FirstCommitID);
        CommitFileInfo(ProjectName, HashCode, fc, FcaCommit, outpath1, Gitpath, FirstCommitID);

    }

    public static void CommitFileInfo(String ProjectName, LinkedHashMap<Integer, String > HashCode, LinkedHashMap<String, Integer> fc, List l, String OutPath1, String Gitpath, String FirstCommitID) throws IOException {

        /******文件增减 总代码行数等指标输出文件****/
        FileOutputStream fileOutputStream = new FileOutputStream(OutPath1, true);
        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream, "GBK");
        BufferedWriter bw1 = new BufferedWriter(outputStreamWriter);
        /**************文件依赖数输出文件*****/
//        //提交前文件依赖数
//        FileOutputStream fileOutputStream2 = new FileOutputStream(OutPath2, true);
//        OutputStreamWriter outputStreamWriter2 = new OutputStreamWriter(fileOutputStream2, "GBK");
//        BufferedWriter bw2 = new BufferedWriter(outputStreamWriter2);
//        //提交后文件依赖数
//        FileOutputStream fileOutputStream3 = new FileOutputStream(OutPath3, true);
//        OutputStreamWriter outputStreamWriter3 = new OutputStreamWriter(fileOutputStream3, "GBK");
//        BufferedWriter bw3 = new BufferedWriter(outputStreamWriter3);


        for (int i = 0; i < l.size(); i++) {
            int first = 0;
            String[] CommitInfo = new String[10000];
            String CommitID = HashCode.get(l.get(i)).trim();
            String OldCommitID = "";
            //得到该提交在需要提取的commit列表中的索引
            int num  = (int) l.get(i);
            //不是第一个提交 得到上一个提交 Map中从第一个开始
            if(num != 1)
                OldCommitID = HashCode.get(num - 1).trim();


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

            /***********文件代码总行数算法：firstCommit不变 因为已经得到了第一个提交的代码总行数 并存在了Map里 List里第一个commit但不是FirstCommitID diff FirstCommitID Hashcode里的上一个CommitID
            HashCode里上一个Commit提交后的文件代码总行数 作为初始值
            diff 上一个提交 本次提交
            遍历 最后修改文件代码总行数就行***********/


            if (HashCode.get(l.get(i)).trim() == FirstCommitID.trim()) {
                //diff 第一个commit与其上一个commit 减去其增加的文件数
                newcommit = rw.parseCommit(repo.resolve(FirstCommitID.trim()));
                oldcommit = rw.parseCommit(newcommit.getParent(0).getId());
                first = 1;

            } else if (HashCode.get(l.get(i)).trim() != FirstCommitID.trim() && i == 0) {//List第一个但不是本版本第一个commit diff 第一个commit和自己的上一个commit 加上增加的文件数
                //RevCommit thiscommit = rw.parseCommit(repo.resolve(OldCommitID));
                newcommit = rw.parseCommit(repo.resolve(OldCommitID));
                oldcommit = rw.parseCommit(repo.resolve(FirstCommitID.trim()));
            } else {//其余情况算文件代码总行数 diff 上一个提取的commit的parent 到自己的parent

                //RevCommit thiscommit = rw.parseCommit(repo.resolve(HashCode.get(l.get(i - 1))));
                newcommit = rw.parseCommit(repo.resolve(OldCommitID));
                //RevCommit lastCommit = rw.parseCommit(repo.resolve(OldCommitID));
                int k = (int)l.get(i - 1);
                oldcommit = rw.parseCommit(repo.resolve(HashCode.get(k - 1).trim()));
                //if()
            }
            System.out.println(HashCode.get(l.get(i)).trim());
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
                if(!diffs.isEmpty()) {
                    for (DiffEntry diff : diffs) {
                        //FileInfo FI = new FileInfo();
                        int fclinesAdded = 0;
                        int fclinesDeleted = 0;
//                    System.out.println(diff.getOldPath());
                    //System.out.println(diff.getNewPath());
                        if(!diff.getOldPath().contains(".java") && !diff.getNewPath().contains(".java"))
                            continue;
                        for (Edit edit : df.toFileHeader(diff).toEditList()) {

                            fclinesAdded += edit.getEndB() - edit.getBeginB();
                            fclinesDeleted += edit.getEndA() - edit.getBeginA();
                            //System.out.println(edit);
//                        System.out.println(edit.getEndB() - edit.getBeginB());
//                        System.out.println(edit.getEndA() - edit.getBeginA());
                        }
                        int changedline = fclinesAdded - fclinesDeleted;

                        //System.out.println(changedline);
                        if (first == 1) {
                            if (diff.getOldPath() != diff.getNewPath()) {
                                if (diff.getNewPath().contains("/dev/null")) {//删除文件
                                    fc.put(diff.getOldPath(), 0 - changedline);
                                } else if (diff.getOldPath().contains("/dev/null")) {//增加文件
                                    //fc.remove(diff.getOldPath());
                                    fc.put(diff.getNewPath(), 0);
                                } else {//文件改名
                                    int Newlines = fc.get(diff.getNewPath());
                                    //fc.remove(diff.getNewPath());
                                    fc.put(diff.getNewPath(), Newlines - changedline);
                                }
                            } else {
                                fc.put(diff.getPath(sd), fc.get(diff.getPath(sd)) + changedline);
                            }
                        } else {
                            if (diff.getOldPath() != diff.getNewPath()) {
                                if (diff.getNewPath().contains("/dev/null")) {//删除文件

//                                if(diff.getOldPath().contains("lang/java/maven-plugin/src/test/resources/unit/idl/pom-jsr310.xml"))
//                                    System.out.println("---" + diff.getOldPath() +  " " +fc.get(diff.getOldPath()) + " " + changedline);

                                    fc.remove(diff.getOldPath().trim());
                                    //FileName = diff.getOldPath();
                                } else if (diff.getOldPath().contains("/dev/null")) {//增加文件
                                    fc.put(diff.getNewPath().trim(), changedline);

//                                if(diff.getNewPath().contains("lang/java/maven-plugin/src/test/resources/unit/idl/pom-jsr310.xml"))
//                                    System.out.println("+++" + diff.getNewPath() +  " "+ fc.get(diff.getNewPath())+ " " + changedline);
                                    //System.out.print(changedline);
                                    //System.out.print(fc.get(diff.getOldPath()));
                                } else {//文件改名
                                    if (fc.get(diff.getOldPath()) != null) {//旧路径没被新路径替换  //关键语句！！
                                        int oldlines = fc.get(diff.getOldPath());

//                                    if(diff.getNewPath().contains("lang/java/maven-plugin/src/test/resources/unit/idl/pom-jsr310.xml"))
//                                        System.out.println("+-+" + diff.getOldPath() +  " " +fc.get(diff.getOldPath()) + " " + changedline);
                                        //不删除旧文件
                                        fc.put(diff.getNewPath(), oldlines + changedline);
                                    }
                                }
                            } else {
                                //if(fc.containsValue(diff.getPath(sd)))
                                //系统原因得不到的文件代码行数设为0 会导致实验结果比实际的差一点，但是不影响结果的真实性 因为得不到的文件数有限
                                int lines = 0;
                                if(fc.containsKey(diff.getPath(sd)))
                                    lines = fc.get(diff.getPath(sd));
                                fc.put(diff.getPath(sd), lines + changedline);
//                            if(diff.getPath(sd).contains("lang/java/maven-plugin/src/test/resources/unit/idl/pom-jsr310.xml"))
//                                System.out.println("+-+-" + diff.getPath(sd) + " " + lines + " " + changedline);
                                //else
                                // fc.put(diff.getPath(sd), changedline);
                            }
                        }
                    }
                }//求文件代码行数的第一步 求出List前一个commit提交后的文件代码行数
//                int m1 = fc.get("lang/perl/xt/schema.t");
//                System.out.print("*********************************" + m1 + "********************************");
//                for (String s : fc.keySet()) {
//                    System.out.println(s + " " + fc.get(s));
//                }

                int linesAdded = 0;
                int linesDeleted = 0;
                int filesChanged = 0;
                /*********求FileName,AddLines,DeleteLines,CodeLines,FileAdd,FileDelete五个指标 ********/
                if(first == 1) {
                    newcommit = rw.parseCommit(repo.resolve(CommitID));
                    oldcommit = rw.parseCommit(newcommit.getParent(0).getId());
                }
                else{
                    newcommit = rw.parseCommit(repo.resolve(CommitID));
                    oldcommit = rw.parseCommit(repo.resolve(OldCommitID));
                }
                List<DiffEntry> diffs2;
                diffs2 = df.scan(oldcommit.getTree(), newcommit.getTree());
                //filesChanged = diffs2.size();

                LinkedHashMap<String, FileInfo> SFinfo = new LinkedHashMap<>();//存放单个文件名以及代码增减行数信息
                for (DiffEntry diff : diffs2) {
                    FileInfo FI = new FileInfo();
                    if(!diff.getOldPath().contains(".java") && !diff.getNewPath().contains(".java"))
                        continue;
//                    System.out.println(diff.getOldPath());

                    //System.out.println(diff.getNewPath());
                    filesChanged++;
                    for (Edit edit : df.toFileHeader(diff).toEditList()) {
                        linesAdded += edit.getEndB() - edit.getBeginB();
                        linesDeleted += edit.getEndA() - edit.getBeginA();
                        //System.out.println(edit);
                        FI.addNum += edit.getEndB() - edit.getBeginB();
                        FI.deleteNum += edit.getEndA() - edit.getBeginA();
                    }
                    //System.out.println(FI.addNum - FI.deleteNum);
                    String FileName = diff.getPath(sd);//删除的文件名变为dev/null

                    if (diff.getOldPath() != diff.getNewPath()) {
                        if (diff.getNewPath().contains("/dev/null")) {//删除文件
                            FileName = diff.getOldPath();
                            FI.codelines = fc.get(FileName);
                            //fc.remove(diff.getOldPath().trim());

                        } else if (diff.getOldPath().contains("/dev/null")) {//增加文件
                            fc.put(diff.getNewPath(), 0);
                            FI.codelines = fc.get(FileName);
                            //fc.put(diff.getNewPath().trim(), FI.addNum - FI.deleteNum);
                        } else {//文件改名
                            //因为系统问题没有的文件 我们直接将其代码行数记成0
                            int oldlines = 0;
                            if(!fc.containsKey(diff.getOldPath())){
                                fc.put(diff.getOldPath(), 0);
                                oldlines = 0;
                            }
                            else
                                oldlines = fc.get(diff.getOldPath());
                            //fc.remove(diff.getOldPath());
                            fc.put(diff.getNewPath(), oldlines);
                            FI.codelines = fc.get(FileName);
                            //fc.put(diff.getNewPath(), oldlines + FI.deleteNum - FI.deleteNum);
                        }
                    }
                    else{//没有改名
                        //因为系统问题找不到的文件 代码行数设为0
                        if(!fc.containsKey(FileName))
                            FI.codelines = 0;
                        else
                            FI.codelines = fc.get(FileName);
                        //int lines = fc.get(diff.getPath(sd));
                        //fc.put(diff.getPath(sd), lines + FI.addNum - FI.deleteNum);
                    }
                    SFinfo.put(FileName, FI);//存放本次提交的所有修改文件名
                }
                /**********文件依赖数计算函数********/
//              先输出文件依赖数指标
//                String[] FileDepsInfo1 = FileDpes(ProjectName, CommitID, SFinfo, FileDepsPath, (int)l.get(i));
//                String[] FileDepsInfo2 = FileDpes(ProjectName, CommitID, SFinfo, FileDepsPath, (int)l.get(i) - 1);
//
//                for (int m = 0; FileDepsInfo1[m] != null; m++) {
//                    bw2.write(FileDepsInfo1[m]);
//                    bw2.newLine();
//                    bw2.flush();
//                }
//
//                for (int m = 0; FileDepsInfo2[m] != null; m++) {
//                    bw3.write(FileDepsInfo2[m]);
//                    bw3.newLine();
//                    bw3.flush();
//                }

                /**********输出剩余五个指标************/
                //增减文件数
                int FileAdd = 0;
                int FileDelete = 0;
                //int TotalCodeLines = 0;

                CommitInfo[0] = CommitID + "," + filesChanged + "," + linesAdded + "," + linesDeleted;
                int n = 0;
                FileRepositoryBuilder builder = new FileRepositoryBuilder();
                Repository repo1 = builder.setGitDir(new File(Gitpath)).setMustExist(true).build();
                int NUC = 0;
                double AGE = 0.0; //单位是天
                int NS = 0;
                int ND = 0;
                double Entropy = 0.0;
                int LT = 0;
                int NDEV = 0;

                ArrayList<String> Subsystem = new ArrayList<>();
                ArrayList<String> Directory = new ArrayList<>();
                //每个文件的具体信息
                for (String s : SFinfo.keySet()) {
                    //System.out.println("Filename:" + s);
//                    int codelines;
//                    codelines = fc.get(s);
                    /******计算NUC、AGE、NS、ND、Entropy、LT*****/
                    /***均为提交级别指标 不是文件级别***/

                    //ArrayList<RevCommit> commits =  new  LogFollowCommand(repo, CommitID, s).call();

                    Git git = new Git(repo);
                    git.checkout().setName(CommitID).call();
                    int CS = 0;
                    Iterable<RevCommit> commits  = git.log().addPath(s).call();
                    List<RevCommit> result = new ArrayList<RevCommit>();
                    commits.forEach(result::add);
                    /************NDEV**********/

                    Set<String> Authors = new HashSet<>();
//                    for(int j = 0; j < commits.size(); j++){
//                        String Author =  commits.get(j).getAuthorIdent().getName();
//                        Authors.add(Author);
//                    }
                    for(RevCommit commit : result){
                        String Author =  commit.getAuthorIdent().getName();
                        Authors.add(Author);
                    }
                    NDEV = Authors.size();

                    CS = result.size();
                    if(CS > NUC)
                        NUC = CS;
//                    if(commits.size() > NUC)
//                        NUC = commits.size();


                    if(CS > 1) {
                        int day = ((result.get(0).getCommitTime() - result.get(1).getCommitTime()) / (60 * 20 * 24));
                        day = day / filesChanged;
                        AGE += day;
                    }

                    int nsend = s.indexOf('/');
                    String sub = s.substring(0, nsend - 1);
                    if(!Subsystem.contains(sub))
                        Subsystem.add(sub);

                    int ndend = s.lastIndexOf('/');
                    String Dir = "";
                    if(nsend != ndend)
                        Dir = s.substring(nsend + 1, ndend - 1);
                    else
                        Dir = sub;
                    if(!Directory.contains(Dir))
                        Directory.add(Dir);

                    double Filechanged =  SFinfo.get(s).addNum + SFinfo.get(s).deleteNum;
                    double CommitChanged = linesAdded + linesDeleted;
                    //double en =  (double)(Filechanged/CommitChanged) * (Math.log(Filechanged/CommitChanged) / Math.log(2));
                    if(Filechanged != 0)
                        Entropy -=  (Filechanged/CommitChanged) * (Math.log((Filechanged/CommitChanged)) / Math.log(2));
//                    System.out.println(CommitChanged);
//                    System.out.println(Math.log(Filechanged/CommitChanged) / Math.log(2));
//                    BigDecimal entr = new BigDecimal(en);
//                    Entropy -= entr.setScale(2,BigDecimal.ROUND_HALF_UP).doubleValue();

                    LT += SFinfo.get(s).codelines;


                    if (s != null) {
                        if (CommitInfo[n] != null)
                            CommitInfo[n] = CommitInfo[n] + "," + s + "," + SFinfo.get(s).addNum + "," + SFinfo.get(s).deleteNum + "," +SFinfo.get(s).codelines;
                        else
                            CommitInfo[n] = ",,,," + s + "," + SFinfo.get(s).addNum + "," + SFinfo.get(s).deleteNum + "," + SFinfo.get(s).codelines;
                    }
                    //增减文件数计算
                    if((SFinfo.get(s).deleteNum == 0) && (SFinfo.get(s).deleteNum == SFinfo.get(s).codelines))
                        FileAdd++;
                    if((SFinfo.get(s).addNum == 0) && (SFinfo.get(s).deleteNum == SFinfo.get(s).codelines) && (SFinfo.get(s).deleteNum != 0))
                        FileDelete++;
                    //TotalCodeLines += SFinfo.get(s).codelines;
                    n++;
                }


                NS = Subsystem.size();
                ND = Directory.size();

                /**************获取作者经验EXP,REXP******************/
                int EXP = 0;
                int REXP = 0;
                Git git = getGitObj(repo);//获得git对象
                git.checkout().setName(CommitID).call();
                Iterable<RevCommit> log = git.log().call();

                String AuthorName = "";
                int CommitTime = 0;
                int ci = 0;

                for (RevCommit commit : log) {
                    if(ci == 0) {
                        AuthorName = commit.getAuthorIdent().getName();
                        CommitTime = commit.getCommitTime();
                    }
                    //System.out.println(commit.getId());
                    //System.out.println(commit.getAuthorIdent().getName());
                    if( ci != 0 && AuthorName.equals(commit.getAuthorIdent().getName())) {
                        EXP++;
                        if(CommitTime - commit.getCommitTime() <= 1200000)
                            REXP++;
                    }

                    ci++;

                }
                /*********************/

                //本提交中没有修改文件的情况  
                if(n == 0){
                    CommitInfo[n] = CommitInfo[n] + ",/," + 0 + "," + 0 + "," + 0;
                }
                CommitInfo[0] = CommitInfo[0] + "," + FileAdd + "," + FileDelete + "," + NDEV + "," + NUC + "," + AGE + "," + NS + "," + ND + "," + Entropy + "," + LT + "," + EXP + "," + REXP;
                //CommitInfo[0] = CommitInfo[0] + "," + FileAdd + "," + FileDelete + "," + NDEV + "," + NUC + "," + AGE + "," + NS + "," + ND + "," + Entropy + "," + LT;
                for (int m = 0; CommitInfo[m] != null; m++) {
                    bw1.write(CommitInfo[m]);
                    bw1.newLine();
                    bw1.flush();
                }
            } catch (IOException e1) {
                throw new RuntimeException(e1);
            } catch (GitAPIException e) {
                throw new RuntimeException(e);
            }

            System.out.println("**************************************");

        }

        bw1.close();
        //bw2.close();

        /********文件增减行数表格的表头为 CommitID,FileNum,TotalAddLines,TotalDeleteLines,FileName,AddLines,DeleteLines,CodeLines,FileAdd,FileDelete,NDEV,NUC,AGE,NS,ND,Entropy,LT，EXP,REXP *****/

    }
    public static String[] FileDpes(String ProjectName, String CommitID, LinkedHashMap<String, FileInfo> SFinfo, String FileDepsPath, int num) throws IOException {
        String[] FileDepsInfo = new String[10000];
        FileDepsInfo[0] = CommitID;

        FileDepsPath = FileDepsPath + "\\commit" + num + "\\" + ProjectName + "-FileDeps.csv";



        //num = num - 1;

        int n = 0;
        int totalDeps = 0;
        int countALLDeps = 0;
        for (String s : SFinfo.keySet()) {
            s = s.trim();
            int k = 0;
            while(s.indexOf("/") != -1){
                k = s.indexOf("/");
                s = s.substring(k + 1);
            }
            //System.out.println(s);
           // int HasNODeps = 0;
            int FileDepsNum1 = 0;
            int FileDepsNum2 = 0;
            BufferedReader fd = new BufferedReader(new FileReader(FileDepsPath));//读取依赖文件
            String fi;
            fi = fd.readLine();//跳过第一行表头
            while ((fi = fd.readLine()) != null) {
                countALLDeps++;
                String[] str = fi.split(",");
                if (str[0].trim().contains(s) || str[1].trim().contains(s)) {
                    if(str[0].contains(s)) {
                        //FileDepsNum1 += Integer.parseInt(str[2]);
                        FileDepsNum1++;
                    }
                    else{
                        //FileDepsNum2 += Integer.parseInt(str[2]);
                        FileDepsNum2++;
                    }
                   // HasNODeps = 1;

                }
            }

            int FileDeps = FileDepsNum1 + FileDepsNum2;
            if (FileDepsInfo[n] != null)
                FileDepsInfo[n] = FileDepsInfo[n] + "," + s + "," + FileDepsNum1 + "," + FileDepsNum2 + "," + FileDeps;
            else
                FileDepsInfo[n] = "," + s + "," + FileDepsNum1 + "," + FileDepsNum2 + "," + FileDeps;

            totalDeps += (FileDepsNum1 + FileDepsNum2);
            n++;
        }
        if(n == 0) {
            BufferedReader fd2 = new BufferedReader(new FileReader(FileDepsPath));//读取依赖文件
            while(fd2.readLine() != null)
                countALLDeps++;
            FileDepsInfo[n] = FileDepsInfo[n] + ",/," + 0  + "," + 0 + "," + 0;
        }
        FileDepsInfo[0] = FileDepsInfo[0] + "," + totalDeps + "," + countALLDeps ;
        return FileDepsInfo;
    }


    public static void GetCommitNumber(List<Integer> A, List<Integer> F, String path) throws IOException {
        BufferedReader f = new BufferedReader(new FileReader(path));//s为tag地址
        String str;

        while ((str = f.readLine()) != null)//逐行读文件
        {
            //剔除开头两行索引
            if (!str.contains("commit"))
                continue;
            String[] str2;
            str2 = str.split(",");//将每个单元格分隔开
            String[] CommitName;
            CommitName = str2[0].split("--");//得到commit号码
            CommitName[1] = CommitName[1].substring(6);//到字符串末尾可以不写endindex
            //System.out.print(CommitName[0] + " " + str2[1] + " " + str2[3] + ";");

//需要的数据范围
            if (Float.parseFloat(str2[2]) > 0.0) {
                A.add(Integer.parseInt(CommitName[1]));
            }


            if (Float.parseFloat(str2[4]) > 0.0) {
                F.add(Integer.parseInt(CommitName[1]));
            }

        }

//        for (int i = 0; i < A.size(); i++) {
//            System.out.println(A.get(i));
//        }

        for (int i = 0; i < F.size(); i++) {
            System.out.print(F.get(i) + "   ");
        }


    }

    public static String GetHashCode(HashMap<Integer, String> h, String path) {
        String FirstCommitID = null;
        try {
            BufferedReader f = new BufferedReader(new FileReader(path));//s为log地址
            String str;


            while ((str = f.readLine()) != null)//逐行读文件
            {

                //str = str.trim();//去掉空格
                String[] str2;

                str2 = str.split(" ");
                if (str2.length < 2)//最后一行不要
                    continue;

                str2[2] = str2[2].trim();
                int index = str2[2].indexOf("Value");
                str2[2] = str2[2].substring(0, index - 1);
                h.put(Integer.valueOf(str2[4]), str2[2]);
                // System.out.println(str2[2] + " " + str2[4]);
                //返回该版本间第一个提交的hash码
                if (Integer.valueOf(str2[4]) == 1)
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
class LogFollowCommand {

    private final Repository repository;
    private String path;
    private Git git;

    private String CommitID;

    /**
     * Create a Log command that enables the follow option: git log --follow -- < path >
     * @param repository
     * @param path
     */
    public LogFollowCommand(Repository repository,String CommitID, String path){
        this.repository = repository;
        this.path = path;
        this.CommitID = CommitID;
    }

    /**
     * Returns the result of a git log --follow -- < path >
     * @return
     * @throws IOException
     * @throws MissingObjectException
     * @throws GitAPIException
     */
    public ArrayList<RevCommit> call() throws IOException, MissingObjectException, GitAPIException {
        ArrayList<RevCommit> commits = new ArrayList<RevCommit>();
        git = new Git(repository);
        git.checkout().setName(CommitID).call();
        RevCommit start = null;
        do {
            Iterable<RevCommit> log = git.log().addPath(path).call();
            for (RevCommit commit : log) {
                if (commits.contains(commit)) {
                    start = null;
                } else {
                    start = commit;
                    commits.add(commit);
                }
            }
            if (start == null) return commits;
        }
        while ((path = getRenamedPath( start)) != null);

        return commits;
    }

    /**
     * Checks for renames in history of a certain file. Returns null, if no rename was found.
     * Can take some seconds, especially if nothing is found... Here might be some tweaking necessary or the LogFollowCommand must be run in a thread.
     * @param start
     * @return String or null
     * @throws IOException
     * @throws MissingObjectException
     * @throws GitAPIException
     */
    private String getRenamedPath( RevCommit start) throws IOException, MissingObjectException, GitAPIException {
        Iterable<RevCommit> allCommitsLater = git.log().add(start).call();
        for (RevCommit commit : allCommitsLater) {

            TreeWalk tw = new TreeWalk(repository);
            tw.addTree(commit.getTree());
            tw.addTree(start.getTree());
            tw.setRecursive(true);
            RenameDetector rd = new RenameDetector(repository);
            rd.addAll(DiffEntry.scan(tw));
            List<DiffEntry> files = rd.compute();
            for (DiffEntry diffEntry : files) {
                if ((diffEntry.getChangeType() == DiffEntry.ChangeType.RENAME || diffEntry.getChangeType() == DiffEntry.ChangeType.COPY) && diffEntry.getNewPath().contains(path)) {
                    System.out.println("Found: " + diffEntry.toString() + " return " + diffEntry.getOldPath());
                    return diffEntry.getOldPath();
                }
            }
        }
        return null;
    }
}

