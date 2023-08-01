import org.eclipse.jgit.api.Git;
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

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
public class CommitIDExtract {
    public static void main(String[] args) throws IOException {
        String ProjectName = "avro";
        String CsvPath = "D:\\Postgraduate\\Metric-tool\\TestOutput\\" + ProjectName + "\\Metric\\commit\\FinalMetric\\12MetricResults.csv";
        String MapPath = "D:\\Postgraduate\\Metric-tool\\myproject\\" + ProjectName + "\\map\\12Map.txt";
        String FileList = "D:\\Postgraduate\\Metric-tool\\myproject\\" + ProjectName + "\\FileList\\avro1_file.txt";
        String Outpath = "D:\\Postgraduate\\Metric-tool\\TestOutput\\CommitExtract\\A2A98\\" + ProjectName;

        File F = new File(Outpath);
        if (!F.exists()) {
            F.mkdirs();
        }



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
        int mode = 1;
        String FirstCommitID = GetHashCode(HashCode, MapPath);
        GetCommitNumber(AcdcCommit, FcaCommit, CsvPath,mode);

        String outpath1 = Outpath + "\\A2A_CommitID_FCA.csv";
        String outpath2 = Outpath + "\\A2A_LastCommitID_FCA.csv";

        FileOutputStream fileOutputStream = new FileOutputStream(outpath1, true);
        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream, "GBK");
        BufferedWriter bw1 = new BufferedWriter(outputStreamWriter);

        FileOutputStream fileOutputStream2 = new FileOutputStream(outpath2, true);
        OutputStreamWriter outputStreamWriter2 = new OutputStreamWriter(fileOutputStream2, "GBK");
        BufferedWriter bw2 = new BufferedWriter(outputStreamWriter2);

        for(int m = 0; m < FcaCommit.size(); m++){
            String CommitID = HashCode.get(FcaCommit.get(m));
            bw1.write(CommitID);
            bw1.newLine();
            bw1.flush();
        }

        FcaCommit.clear();
        mode = 2;
        GetCommitNumber(AcdcCommit, FcaCommit, CsvPath,mode);

        for(int m = 0; m < FcaCommit.size(); m++){
            String CommitID = HashCode.get(FcaCommit.get(m));
            bw2.write(CommitID);
            bw2.newLine();
            bw2.flush();
        }
    }
    public static void GetCommitNumber(List<Integer> A, List<Integer> F, String path, int mode) throws IOException {
        BufferedReader f = new BufferedReader(new FileReader(path));//s为tag地址
        String str;

        //mode = 1 得到thisCommit mode = 2 得到lastCommit
        while ((str = f.readLine()) != null)//逐行读文件
        {
            //剔除开头两行索引
            if (!str.contains("commit"))
                continue;
            String[] str2;
            str2 = str.split(",");//将每个单元格分隔开
            String[] CommitName;
            CommitName = str2[0].split("--");//得到commit号码
            if(mode == 1)
                CommitName[1] = CommitName[1].substring(6);//到字符串末尾可以不写endindex
            else
                CommitName[0] = CommitName[0].substring(6);//到字符串末尾可以不写endindex
            //System.out.print(CommitName[0] + " " + str2[1] + " " + str2[3] + ";");

//需要的数据范围
            if(mode == 1) {
                if (Float.parseFloat(str2[2]) > 0.0) {
                    A.add(Integer.parseInt(CommitName[1]));
                }
                if (Float.parseFloat(str2[4]) > 0.0) {
                    F.add(Integer.parseInt(CommitName[1]));
                }
            }
            else{
                if (Float.parseFloat(str2[2]) > 0.0) {
                    A.add(Integer.parseInt(CommitName[0]));
                }
                if (Float.parseFloat(str2[4]) > 0.0) {
                    F.add(Integer.parseInt(CommitName[0]));
                }
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
}
