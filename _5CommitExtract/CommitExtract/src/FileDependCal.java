import java.io.*;
import java.util.*;

public class FileDependCal {
    public static void main(String[] args) throws IOException {
        String projectname ="avro";

        String CsvPath = "D:\\Postgraduate\\Metric-tool\\TestOutput\\avro\\Metric\\commit\\FinalMetric\\12MetricResults.csv";
        String MapPath = "D:\\Postgraduate\\Metric-tool\\myproject\\avro\\map\\12Map.txt";
        String CommitFileFolder = "D:\\Postgraduate\\Metric-tool\\TestOutput\\" + projectname + "\\commit12";
        int CommitNum = 0;
        String FileDepsPath = CommitFileFolder + "\\commit" + CommitNum;
        String outpath = "D:\\Postgraduate\\Metric-tool\\TestOutput\\CommitExtract\\FileDepsCal.csv";

        String FileInfo = "D:\\Postgraduate\\Metric-tool\\TestOutput\\CommitExtract\\划线90\\" + projectname + "\\FileInfo_FCA.csv";

        List<Integer> FcaCommit = new ArrayList<>();//存放FCA聚类下架构相似度小于规定值的commmit号码

        LinkedHashMap<Integer, String> HashCode = new LinkedHashMap<>();//存放所有commit的hash码

        GetHashCode(HashCode, MapPath);
        GetCommitNumber(FcaCommit,CsvPath);

//      写文件
        FileOutputStream fileOutputStream = new FileOutputStream(outpath, true);
        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream, "GBK");
        BufferedWriter bw = new BufferedWriter(outputStreamWriter);

        for(int i = 0; i < FcaCommit.size(); i++){
            String CommitID = HashCode.get(FcaCommit.get(i));
            CommitNum = FcaCommit.get(i);
            FileDepsPath = CommitFileFolder + "\\commit" + CommitNum;

            BufferedReader FileDeps = new BufferedReader(new FileReader(FileDepsPath));//s为log地址
            String s;
            if((s = FileDeps.readLine()) != null){
                String[] str = s.split(",");
                //获取文件名

            }

        }



    }
    //读取CommitID
    //找到对应的Commit文件夹 然后找到Filemame-FIleDeps.csv文件
    //在第一列中找到对应文件名 统计依赖总数

    public static void GetCommitNumber(List<Integer> F, String path) throws IOException {
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

            if (Float.parseFloat(str2[3]) >= 90.0) {
                F.add(Integer.parseInt(CommitName[0]));
            }

        }


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

}
