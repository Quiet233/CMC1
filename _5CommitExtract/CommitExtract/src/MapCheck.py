# -*- coding = utf-8 -*-
import lizard
import git
from pydriller import Repository, Git
import pandas as pd

def main():
    ProjectName = 'cxf'
    #MapPath = r'D:\Postgraduate\Metric-tool\myproject' + '\\' + ProjectName + r'\Map.txt'
    MapPath = r'D:\Metric-tool\myproject' + '\\' + ProjectName + r'\Map.txt'
    #GitPath = r'D:\Postgraduate\Metric-tool\myproject' + '\\' + ProjectName + '\.git'
    GitPath = r'D:\Metric-tool\myproject' + '\\' + ProjectName + '\.git'
    #OutPath = r'D:\Postgraduate\Metric-tool\myproject' + '\\' + ProjectName + r'\MapCheck.txt'
    OutPath = r'D:\Metric-tool\myproject' + '\\' + ProjectName + r'\MapCheck.txt'
    f = open(MapPath)
    line = f.readline()


    # try:
    #     gr.checkout('fdbdbbd2802b40d97cc64cce6eeecd9c5393887f')
    # except Exception as result:
    #     print(result)

    # 读取所有CommitID以及序号
    AllCommitID = []
    CommitOrder = []

    #将有问题的commit写到文件中 后续工作是手动在log中删除这些commit
    while line:
        content = line.split(':')
        print(line)
        if len(content) > 1:
            commitID = content[1].split('\t')
            commitID = commitID[0]
            commitID = commitID.strip()
            try:
                gr = Git(GitPath)
                gr.checkout(commitID)
            except Exception as result:
                print(result)
                AllCommitID.append(commitID)

        line = f.readline()
        print('***********************')

        # k = 0
        # while(k < len(AllCommitID)):
        #     print('Key:' + AllCommitID[k] + '\t' + 'Value:' + CommitOrder[k])
        #     k = k + 1
    f.close()
    if len(AllCommitID) > 0:
        f2 = open(OutPath, 'w+')
        f2.write('Key : ' + AllCommitID[0] + '\n')
        f2.close()
    if len(AllCommitID) > 1:
        f1 = open(MapPath, 'a+')
        i = 1
        while(i < len(AllCommitID)):
            f1.write('Key : ' + AllCommitID[i] + '\n')
            i = i + 1
        f1.close()


if __name__ == '__main__':
    main()

