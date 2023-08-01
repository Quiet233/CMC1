import lizard
import git
from pydriller import Repository, Git
import pandas as pd
import csv
def get_lastinfo(GitPath, ProjectPath, FileInfo, gr, LastcommitID, commitID, FileList,FileType, FirstFileLine, LastFileComplexityInfo, LastFileTokenCountInfo, LastFileMethodNumInfo):
    gr.checkout(LastcommitID)
    CommitComplexity = 0
    CommitToken = 0
    CommitMethodNum = 0

    repo = git.Repo(GitPath)
    # thiscommit = '751a179769ed29c686558cd618a7934754b6691f'
    # lastcommit = '86c74fb4b57813378fe2fd71d44dc1f8f7a28056'
    gitt = repo.git
    diff_st = gitt.diff(LastcommitID, commitID)
    #print(LastcommitID)
    # Name = 'src/java/org/apache/avro/reflect/ReflectDatumReader.java'

    i = 0
    for FileName in FileList:
        print("FileName:" + FileName)
        if FileType[i] == 2:#下一个提交增加的文件
            i = i + 1
            LastFileComplexityInfo.append(0)
            LastFileTokenCountInfo.append(0)
            LastFileMethodNumInfo.append(0)
            continue
        if FileType[i] == 1:#正常修改的文件
            n = diff_st.index(FileName)
            m = n - 3
            OldName = ''
            while (diff_st[m - 1: m] != " "):
                OldName = diff_st[m - 1: m] + OldName
                m = m - 1
            if OldName[0: 2] != '--':
                OldName = OldName[2:len(OldName)]
                if OldName != FileName:
                    info = lizard.analyze_file(ProjectPath + "\\" + OldName)
                #得到的OldName为--git 说明没有改名
            else:
                info = lizard.analyze_file(ProjectPath + "\\" + FileName)
        if FileType[i] == 3:#下一个提交删除的文件
            info = lizard.analyze_file(ProjectPath + "\\" + FileName)
        FileComplexity = 0
        FileToken = info.token_count
        FileMethodNum = len(info.function_list)

        for function in info.function_list:
            FileComplexity = FileComplexity + function.cyclomatic_complexity

        CommitComplexity = CommitComplexity + FileComplexity
        CommitToken = CommitToken + FileToken
        CommitMethodNum = CommitMethodNum + FileMethodNum

        LastFileComplexityInfo.append(FileComplexity)
        LastFileTokenCountInfo.append(FileToken)
        LastFileMethodNumInfo.append(FileMethodNum)
        i = i + 1

    FileInfo.loc[FirstFileLine, 'LastCommitComplexity'] = CommitComplexity
    FileInfo.loc[FirstFileLine, 'LastCommitTokenCount'] = CommitToken
    FileInfo.loc[FirstFileLine, 'LastCommitMethodNum'] = CommitMethodNum

def get_info(GitPath, ProjectPath, FileInfo, FileNum, gr, commitID, LastCommitID, index,FileComplexityInfo, FileTokenCountInfo, FileMethodNumInfo, LastFileComplexityInfo, LastFileTokenCountInfo, LastFileMethodNumInfo):
    i = 0
    #本提交的圈复杂度等信息
    gr.checkout(commitID)
    FileList = []
    #记录文件类型 1--正常修改文件 2--增加的文件 3--删除的文件
    FileType = []
    #print(commitID)
    CommitComplexity = 0
    CommitToken = 0
    CommitMethodNum = 0
    FirstFileLine = -1
    HasFixFile = 0


    while(i < FileNum):
        #print(FileInfo['FileName'][index])
        #print('oooooooooooooooo')
        FileName = FileInfo['FileName'][index]
        FileList.append(FileName)
        HasType = 0
        #是增加或者删除的文件 信息填-1并跳过
        #删除文件 没有this 但是有last
        if FileInfo['DeleteLines'][index] == FileInfo['CodeLines'][index] != 0 and FileInfo['AddLines'][index] == 0:
            i = i + 1
            #FileInfo = FileInfo.drop(index,axis = 0)
            #print(FileInfo)
            FileComplexityInfo.append(0)
            FileTokenCountInfo.append(0)
            FileMethodNumInfo.append(0)
            index = index + 1
            FileType.append(3)
            continue

        # 增加文件 没有 last  但是有this last写0
        if FileInfo['DeleteLines'][index] == FileInfo['CodeLines'][index] == 0:
            FileType.append(2)
            HasType = 1

        if HasType == 0:
            FileType.append(1)

        if FirstFileLine == -1:
            FirstFileLine = index
            HasFixFile = 1
        info = lizard.analyze_file(ProjectPath + "\\" + FileName)

        FileComplexity = 0
        FileToken = info.token_count
        FileMethodNum = len(info.function_list)



        for function in info.function_list:
            FileComplexity = FileComplexity + function.cyclomatic_complexity

        CommitComplexity = CommitComplexity + FileComplexity
        CommitToken = CommitToken + FileToken
        CommitMethodNum = CommitMethodNum + FileMethodNum

        FileComplexityInfo.append(FileComplexity)
        FileTokenCountInfo.append(FileToken)
        FileMethodNumInfo.append(FileMethodNum)
        index = index + 1
        i = i + 1

    if i == 0:
        FirstFileLine = index
        FileComplexity = 0
        FileToken = 0
        FileMethodNum = 0
        FileComplexityInfo.append(FileComplexity)
        FileTokenCountInfo.append(FileToken)
        FileMethodNumInfo.append(FileMethodNum)
        index = index + 1
        HasFixFile = 1
        #print('kkkkkkkkkkkkkkkk')

    if HasFixFile == 0:
        FirstFileLine = index - 1

    print(FileInfo['FileName'].size)
    #print(len(FileComplexityInfo))


    FileInfo.loc[FirstFileLine,'CommitComplexity'] = CommitComplexity
    FileInfo.loc[FirstFileLine,'CommitTokenCount'] = CommitToken
    FileInfo.loc[FirstFileLine,'CommitMethodNum'] = CommitMethodNum
    #文件列表为空
    if len(FileList) == 0:
        m = index - FirstFileLine
        n = 0
        while(n < m):
            LastFileComplexityInfo.append(0)
            LastFileTokenCountInfo.append(0)
            LastFileMethodNumInfo.append(0)
            FileInfo.loc[FirstFileLine, 'LastCommitComplexity'] = 0
            FileInfo.loc[FirstFileLine, 'LastCommitTokenCount'] = 0
            FileInfo.loc[FirstFileLine, 'LastCommitMethodNum'] = 0
            n = n + 1
    else:
        get_lastinfo(GitPath, ProjectPath, FileInfo, gr, LastCommitID, commitID, FileList, FileType, FirstFileLine, LastFileComplexityInfo, LastFileTokenCountInfo, LastFileMethodNumInfo)
    print(len(LastFileComplexityInfo))
    print('***************************')
    return index

def main():
    ProjectName = 'avro'
    MapPath = r'D:\Postgraduate\Metric-tool\myproject' + '\\' + ProjectName + r'\map\AllMap.txt'
    GitPath = r'D:\Postgraduate\Metric-tool\myproject' + '\\' + ProjectName + '\.git'
    ProjectPath = r'D:\Postgraduate\Metric-tool\myproject' + '\\' + ProjectName
    FileInfoPath = r'D:\Postgraduate\Metric-tool\TestOutput\CommitExtract\A2A98\avro\A2A_CommitFileInfo_FCA.csv'

    OutPath = r'D:\Postgraduate\Metric-tool\TestOutput\CommitExtract\A2A98'
    AllCommitID = []
    CommitOrder = []
    f = open(MapPath)
    line = f.readline()
    #读取所有CommitID以及序号
    while line:
        content = line.split(':')
        if len(content) > 1:
            commitID = content[1].split('\t')
            commitID = commitID[0]
            commitID = commitID.strip()
            AllCommitID.append(commitID)
            Order = content[2]
            Order = Order.strip()
            CommitOrder.append(Order)
        line = f.readline()
    print(CommitOrder)
    f.close()
    FileInfo = pd.read_csv(FileInfoPath, encoding='gb18030')
    gr = Git(GitPath)


    index = 0;

    FileComplexityInfo = []
    FileTokenCountInfo = []
    FileMethodNumInfo = []
    LastFileComplexityInfo = []
    LastFileTokenCountInfo = []
    LastFileMethodNumInfo = []

    Size = FileInfo['FileName'].size
    m = 1
    #开始循环
    while (index < Size):
        #print(FileInfo['FileName'].size)
        commitID = FileInfo['CommitID'][index]
        LastcommitID = AllCommitID[m - 1]
        #print(commitID)
        #print(LastcommitID)
        FileNum = FileInfo['FileNum'][index]
        index = get_info(GitPath, ProjectPath, FileInfo, FileNum, gr, commitID, LastcommitID, index, FileComplexityInfo, FileTokenCountInfo, FileMethodNumInfo, LastFileComplexityInfo, LastFileTokenCountInfo, LastFileMethodNumInfo)
        m = m + 1
        if m < CommitOrder.__len__() and CommitOrder[m] == '1':
            m = m + 1
    #将得到的数组赋值给Dataframe
    FileInfo['FileComplexity'] = FileComplexityInfo
    FileInfo['FileTokenCount'] = FileTokenCountInfo
    FileInfo['FileMethodNum'] = FileMethodNumInfo

    FileInfo['LastFileComplexity'] = LastFileComplexityInfo
    FileInfo['LastFileTokenCount'] = LastFileTokenCountInfo
    FileInfo['LastFileMethodNum'] = LastFileMethodNumInfo

    CommitInfo = FileInfo[['CommitID','FileName', 'FileComplexity', 'FileTokenCount', 'FileMethodNum', 'CommitComplexity', 'CommitTokenCount', 'CommitMethodNum', 'LastFileComplexity', 'LastFileTokenCount', 'LastFileMethodNum', 'LastCommitComplexity', 'LastCommitTokenCount', 'LastCommitMethodNum']]
    #CommitInfo = FileInfo[['CommitID', 'FileName', 'FileMethodNum',  'CommitMethodNum', 'LastFileMethodNum', 'LastCommitMethodNum']]
    CommitInfo.to_csv(OutPath + "\\" + ProjectName + "FileInfo2.csv", sep=',', index=None)

if __name__ == '__main__':
    main()
    # repo = git.Repo(r"D:\Postgraduate\Metric-tool\myproject\avro\.git")
    # thiscommit = '3e8840cd1454bb16d170c9dec2b1f2b40171bdc8'
    # lastcommit = '76bf17aaebe0ac8a6bd3d0c787a9e4616a00f5b1'
    # gitt = repo.git
    # diff_st = gitt.diff(lastcommit, thiscommit)
    # FileName = 'lang/java/src/java/org/apache/avro/mapred/tether/TetherPartitioner.java'
    # n = diff_st.index(FileName)
    # m = n - 3
    # OldName = ''
    # while(diff_st[m- 1: m] != " "):
    #      OldName = diff_st[m - 1: m] + OldName
    #      m = m - 1
    # if OldName[0: 2] == '--':
    #      print(OldName)
    # if OldName[0: 2] != '--':
    #     OldName = OldName[2:len(OldName)]
    #     print(OldName)
    #     if OldName is not FileName:
    #         FileName == OldName
    #         File2 = OldName
    #     得到的OldName为--git 说明没有改名
    # gr = Git(r'D:\Postgraduate\Metric-tool\myproject\avro\.git')
    # gr.checkout('76bf17aaebe0ac8a6bd3d0c787a9e4616a00f5b1')
    # info = lizard.analyze_file(r'D:\Postgraduate\Metric-tool\myproject\avro\lang/java/src/java/org/apache/avro/mapred/tether/TetherPartitoner.java')
    #                            #r'D:\Postgraduate\Metric-tool\myproject\avro\lang/java/src/java/org/apache/avro/mapred/tether/TetherPartitioner.java'

    #print(diff_st)





