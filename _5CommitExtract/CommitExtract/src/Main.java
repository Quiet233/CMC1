import java.io.*;
import java.util.*;

//提取提交的issue信息
public class Main {
    public static void main(String[] args) throws IOException {
        /***************args[0] ProjectName, args[1] mode 1-- >=98 2-- <98, args[2] MetricResults csv文件, args[3] Map, args[4] log, args[5] searchrequest, args[6] outpath************/
//        String CsvPath = "E:\\Metric-tool\\TestOutput\\avro\\Metric\\commit\\avroMetricResults_release1-2.csv";
//        String MapPath = "E:\\Metric-tool\\myproject\\avro\\map\\12Map.txt";
//        String Logpath = "E:\\Metric-tool\\myproject\\avro\\log\\avro12_log.txt";
//        String Outpath = "E:\\Metric-tool\\TestOutput\\CommitExtract";
//        String ProjectName = "avro";
//        String XMLPath = "E:\\Metric-tool\\myproject\\SearchRequest\\avro_SearchRequest.xml";
        String CsvPath = args[2];
        String MapPath = args[3];
        String Logpath = args[4];
        String Outpath = args[6];
        String ProjectName = args[0];
        String XMLPath = args[5];

        List<Integer> AcdcCommit = new ArrayList<>();//存放ACDC聚类下架构相似度小于90 的commmit号码 以及hash码
        List<Integer> FcaCommit = new ArrayList<>();//存放FCA聚类下架构相似度小于90 的commmit号码

        LinkedHashMap<Integer, String> HashCode = new LinkedHashMap<>();//存放所有commit的hash码
        LinkedHashMap<String, Integer> fc = new LinkedHashMap<>();//存放文件代码行数

//        BufferedReader fl = new BufferedReader(new FileReader(FileList));//s为log地址
//        String s1;
//        while((s1 = fl.readLine()) != null) {
//            String[] s2;
//            s2 = s1.trim().split(" ");
//            fc.put(s2[1], Integer.parseInt(s2[0]));
//        }
//
//        for (String fn : fc.keySet()) {
//            System.out.println(fn + " " + fc.get(fn));
//
//        }


        GetHashCode(HashCode, MapPath);

        GetCommitNumber(AcdcCommit,FcaCommit,CsvPath,args[1]);

        //FindAndOut(AcdcCommit,FcaCommit,HashCode,Logpath, Outpath + "\\" + ProjectName);//输出到txt文件中 提取ACDC以及FCA和ACDC交集部分
        //FindAndOut1(FcaCommit,HashCode,Logpath, Outpath + "\\" + ProjectName);//输出到txt文件中
        ProjectName = ProjectName.toUpperCase();

        //FindAndOutcsv(ProjectName, AcdcCommit,FcaCommit,HashCode,Logpath, Outpath + "\\" + ProjectName, XMLPath, FileList);
        FindAndOutcsv1(ProjectName, AcdcCommit,FcaCommit,HashCode,Logpath, Outpath + "\\" + ProjectName, XMLPath, fc, args[1]);

    }
    public static void GetCommitNumber(List<Integer> A,List<Integer> F, String path,String mode) throws IOException {
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
            CommitName[1] = CommitName[1].substring(6);//到字符串末尾可以不写endindex
            //System.out.print(CommitName[0] + " " + str2[1] + " " + str2[3] + ";");

            /**********需要的数据范围*************/
            if(mode == "1") {
                if (Float.parseFloat(str2[1]) >= 98.0) {
                    F.add(Integer.parseInt(CommitName[1]));
                }
            }
            else {
                if (Float.parseFloat(str2[1]) < 98.0) {
                    F.add(Integer.parseInt(CommitName[1]));
                }
            }


        }

//            for (int i = 0; i < A.size(); i++) {
//                System.out.println(A.get(i));
//            }
//
            for (int i = 0; i < F.size(); i++) {
                System.out.println(F.get(i));
            }


    }

    public static void GetHashCode(HashMap<Integer,String> h, String path){
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
                //System.out.println(str2[2] + " " + str2[4]);

            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    public static void FindAndOutcsv1(String ProjectName, List<Integer> A, List<Integer> F, LinkedHashMap<Integer, String> h, String Logpath, String Outpath,String XMLpath, LinkedHashMap<String, Integer> fc, String mode) throws IOException {
        File file = new File(Outpath);
        if(!file.exists())
            file.mkdirs();
        String Outpath2 = "";
        /**********可以修改输出的文件名字*********/
        if(mode == "1") {
            Outpath2 = Outpath + "\\A2A_IssueInfo_noArch_Fca.csv";
        }
        else{
            Outpath2 = Outpath + "\\A2A_IssueInfo_Arch_Fca.csv";
        }
        //String Outpath3 = Outpath + "\\NIR.csv";

        BufferedReader f = new BufferedReader(new FileReader(Logpath));//s为log地址
        String str;

        FileOutputStream fileOutputStream = new FileOutputStream(Outpath2, true);
        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream, "GBK");
        BufferedWriter bw1 = new BufferedWriter(outputStreamWriter);

        //FileOutputStream fileOutputStream2 = new FileOutputStream(Outpath3, true);
        //OutputStreamWriter outputStreamWriter2 = new OutputStreamWriter(fileOutputStream2, "GBK");
        //BufferedWriter bw2 = new BufferedWriter(outputStreamWriter2);


        //FCA commit提取
        int i;
        i = F.size() - 1;
        String CommitID = "";
        if (i > -1)
            CommitID = h.get(F.get(i));
        else
            return;

        CommitID = CommitID.trim();
        String CommitInclude = "";

        bw1.flush();
        /**************开始读文件**************/
        while ((str = f.readLine()) != null)//逐行读文件
        {
            //System.out.print(CommitID);
            // System.out.println("********************");
            str = str.trim();
            if ((str.contains(CommitID) || CommitInclude.contains(CommitID)))//是否含有commitID
            {
                String[] CommitInfo = new String[10000];

                int n = 0;
                int issueNumber = 0;

                /********？？？？**********/
                if (CommitInfo[n] != null)
                    CommitInfo[n] = CommitInfo[n] + CommitID;
                else
                    CommitInfo[n] = CommitID;


                //找到Date
                while (!str.contains("Date")) {
                    str = f.readLine();
                }
                str = f.readLine();//读取空白的一行
                //下一行是issue
                int HasIssue = 0;
                String preNumber = "";

                str = f.readLine();//这一行读到issue了
                while ((str != null) && ((str.indexOf("commit")) != 0))//到下一个commit为止
                {
                    //SF.clear();
                    /********************改issue格式！！***********************/
                    String Issue = ProjectName + "-";
                    //String Issue = "AMQ-";
                    String Number = "";

                    while ((str != null) && str.contains(Issue)) {

                        int k = str.indexOf(Issue);
                        String s = "";
                        //从“-” 往后一个一个读
                        if (k + Issue.length() + 1 < str.length())
                            s = str.substring(k + Issue.length(), k + Issue.length() + 1);
                        else
                            s = str.substring(k + Issue.length());

                        if (!isNumeric(s)) {//不是数字 不是issue
                            if(HasIssue == 0) {
                                str = str.replace(",", " ");
                                if (CommitInfo[n] != null)
                                    CommitInfo[n] = CommitInfo[n] + ",/,/," + str + ",/,/";

                                    //CommitInfo[n].append(",/,/," + str + ",/,/," + Author);
                                else
                                    CommitInfo[n] = " ,/,/," + str + ",/,/";

                                n++;
                                HasIssue = 1;
                            }
                            str = f.readLine();
                            if (str.indexOf("commit") == 0)
                                break;
                        }

                        // break;

                        // }//没有数字的issue


                        //存issue的数据部分
                        int Num = 0;
                        while (isNumeric(s)) {
                            Issue = Issue + s;
                            Number = Number + s;
                            if ((k + Issue.length() + 1) < str.length())
                                s = str.substring(k + Issue.length(), k + Issue.length() + 1);
                            else
                                s = str.substring(k + Issue.length());
                            Num = 1;
                        }
                        //System.out.println(Number);
                        //与这个commit的存过的issue对比 存过的就不存了
                        if(Num == 1) {
                            if (preNumber == "" || ((preNumber != "") && (Integer.parseInt(Number) != Integer.parseInt(preNumber)))) {

                                //System.out.println(Issue);

                                if (CommitInfo[n] != null)
                                    CommitInfo[n] = CommitInfo[n] + "," + Issue;
                                    //CommitInfo[n].append("," + Issue);
                                else
                                    CommitInfo[n] = " ," + Issue;
                                issueNumber++;
//补足信息
                                StringBuilder Sb = new StringBuilder();
                                Sb.append(CommitInfo[n]);
                                Sb = CommitInfo(Issue, Sb, XMLpath);
                                System.out.println(Sb);
                                CommitInfo[n] = Sb.toString();

//看看这一行里有没有多个commit
                                n++;
                                preNumber = Number;
                                Issue = ProjectName + "-";
                                Number = "";
                                //读下一行或者这一行的剩下部分
                                if ((k + Issue.length() + 1) < str.length()) {
                                    str = str.substring(k + Issue.length());
                                    if (str.contains(Issue))
                                        continue;
                                }
                            }
                        }
                        //System.out.println(str);

                        //System.out.println(preNumber);

                        HasIssue = 1;//标记这个commit有issue
                        // }


                    }

                    if ((str != null) && !str.contains(Issue)) {//没有issue
                        if (str.indexOf("commit") == 0)
                            continue;
                        if (HasIssue == 1) {
                            str = f.readLine();
                            continue;
                        }//这个commit有issue 细节不用存 xml文件里有detail
                        //没有issue的话 就存到detail里 并把author填进去
                        if ((str != "\n")) {
                            str = str.replace(",", " ");
                            if (CommitInfo[n] != null)
                                CommitInfo[n] = CommitInfo[n] + ",/,/," + str + ",/,/";
                                //CommitInfo[n].append(",/,/," + str + ",/,/," + Author);
                            else
                                CommitInfo[n] = " ,/,/," + str + ",/,/";

                            //System.out.println(str);
                            n++;
                            HasIssue = 1;
                        }

                    }

                }
                //存入错误报告数

//                String IssueNum = Integer.toString(issueNumber);
//                bw2.write(IssueNum);
//                bw2.newLine();
//                bw2.flush();

                for (int m = 0; CommitInfo[m] != null; m++) {
                    bw1.write(CommitInfo[m]);
                    bw1.newLine();
                    bw1.flush();
                }
                CommitInclude = str; //记住最后读的这行 下一次循环需判断其是否含有需要的CommitID

                if (i != 0) {
                    i--;
                    CommitID = h.get(F.get(i));
                    CommitID = CommitID.trim();

                }
                else
                    break;

                }

            }

            bw1.close();
            //bw2.close();
            System.out.println("**************************表头**********************************");
            System.out.println("CommitID,IsuueID,IssueType,Detail,Component,Priority,issueNum");//输出表头

    }

    public static StringBuilder CommitInfo(String CommitID, StringBuilder CommitInfo,  String XMLPath) throws IOException {
//commitID = IssueID
        BufferedReader f = new BufferedReader(new FileReader(XMLPath));//s为log地址
        String str;
        String type = "";
        String Summary = "";
        String Priority = "";
        String Component = "";
        String[] Authors = new String[10];
        int HasItem = 0;
        int ah = 0;
        int bi = 0;
        int ei = 0;
        while((str = f.readLine()) != null){
            str = str.trim();
          if(str.contains("</key>")){
            bi = str.indexOf(">") + 1;
            ei = str.indexOf("</key>");
            String str2 = str.substring(bi, ei);
            str2 = str2.trim();
            CommitID = CommitID.trim();
            //System.out.println(str2);
            //System.out.println(CommitID);
            if(str2.equals(CommitID)){
                //System.out.println("find");
                while (((str = f.readLine()) != null) && (!str.contains("</item>"))){
                    HasItem = 1;
                    str = str.trim();
                    if(str.contains("</summary>")){
                        bi = str.indexOf(">") + 1;
                        ei = str.indexOf("</summary>");
                        if(bi >= str.length())
                            bi = 0;
                        Summary = str.substring(bi, ei).replace(",", " ");
                    }
                    else if(str.contains("</type>")){
                        bi = str.indexOf(">") + 1;
                        ei = str.indexOf("</type>");
                        type = str.substring(bi, ei);
                    }
                    else if(str.contains("</priority>")){
                        bi = str.indexOf(">") + 1;
                        ei = str.indexOf("</priority>");
                        Priority = str.substring(bi, ei);
                    }
                    else if(str.contains("</assignee>")){
                        bi = str.indexOf(">") + 1;
                        ei = str.indexOf("</assignee>");
                        Authors[ah] = str.substring(bi, ei);
                        ah++;
                    }

//                    else if(str.contains("</reporter>")){
//                        bi = str.indexOf(">") + 1;
//                        ei = str.indexOf("</reporter>");
//                        if(ah == 0 || !(str.substring(bi, ei).trim().equals(Authors[ah - 1])))
//                            Authors[ah] = str.substring(bi, ei);
//                        ah++;
//                    }
                    else if(str.contains("</component>")){
                        bi = str.indexOf(">") + 1;
                        ei = str.indexOf("</component>");
                        Component = str.substring(bi, ei);
                    }
                }
//                System.out.println(type);
//                System.out.println(Summary);
//                System.out.println(Priority);
//                System.out.println(Component);

                if (HasItem == 1) {
                    if (Component == "")
                        Component = "None";
                    CommitInfo.append("," + type + "," + Summary + "," + Component + "," + Priority);
                } else {
                    CommitInfo.append(",/,/,/,/");
                }


//                for(int k = 0; Authors[k] != null; k++)
//                {
//                    CommitInfo.append(Authors[k] + "、");
//                }

                break;


            }

          }

        }
       // System.out.println(CommitInfo);
        return CommitInfo;
    }

//    public static StringBuilder FileInfo()
//    {
//        return "123";
//    }

    public static boolean isNumeric(String string) {
        int intValue;

        System.out.println(String.format("Parsing string: \"%s\"", string));

        if(string == null || string.equals("")) {
            System.out.println("String cannot be parsed, it is null or empty.");
            return false;
        }

        try {
            intValue = Integer.parseInt(string);
            return true;
        } catch (NumberFormatException e) {
            System.out.println("Input String cannot be parsed to Integer.");
        }
        return false;
    }
}
class FileInfo{
    int addNum = 0;
    int deleteNum = 0;
    int codelines = 0;
}