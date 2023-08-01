import pydriller
import git
from pydriller import Repository, Git
import pandas as pd
import numpy as np
import csv

def main():

    ProjectName = 'avro'
    CommitIDFilePath = r'D:\Postgraduate\Metric-tool\TestOutput\CommitExtract\A2A98\\' + ProjectName + '\A2A_CommitID_FCA.csv'
    CommitIDFile = pd.read_csv(CommitIDFilePath, header=None, encoding='gb18030')
    LastCommitIDFilePath = r'D:\Postgraduate\Metric-tool\TestOutput\CommitExtract\A2A98\\' + ProjectName + '\A2A_LastCommitID_FCA.csv'
    LastCommitIDFile = pd.read_csv(LastCommitIDFilePath, header=None, encoding='gb18030')

    OutPath1 = r"D:\Postgraduate\Metric-tool\TestOutput\CommitExtract\A2A_CommitInfo2.csv"
    OutPath2 = r"D:\Postgraduate\Metric-tool\TestOutput\CommitExtract\A2A_FileInfo2.csv"

    headers1 = [('CommitID','in_main_branch', 'merge', 'dmm_unit_size', 'dmm_unit_complexity', 'dmm_unit_interfacing')]
    headers2 = [('CommitID','Filename','complexity', 'token_count')]

    with open(OutPath1, 'a', encoding='utf8', newline='') as file:
        writer = csv.writer(file)
        writer.writerows(headers1)

    with open(OutPath2, 'a', encoding='utf8', newline='') as file:
        writer = csv.writer(file)
        writer.writerows(headers2)

    # CommitIDFile['CommitID'].replace(' ', np.nan, inplace=True)
    # CommitIDFile.dropna(subset=['CommitID'], inplace=True)
    # gr = Git(r'D:\Postgraduate\Metric-tool\myproject\avro\.git')
    # gr.checkout('86d3ba82a52c921bcf820f580405e06fd3d797cd')
    FilePath = r'D:\Metric-tool\myproject\avro'
    for index in CommitIDFile.iloc[:, 0].index:
        CommitID = CommitIDFile.iloc[index, 0]
        CommitID = CommitID.strip()
        LastCommitID = LastCommitIDFile.iloc[index, 0]
        LastCommitID = LastCommitID.strip()
        print(CommitID)
        #commit信息
        for commit in Repository(FilePath, single=CommitID).traverse_commits():
            int_main_branch = commit.in_main_branch
            merge = commit.merge
            dmm_unit_size = commit.dmm_unit_size
            dmm_unit_complexity = commit.dmm_unit_complexity
            dmm_unit_interfacing = commit.dmm_unit_interfacing
            Info = [CommitID, int_main_branch, merge, dmm_unit_size, dmm_unit_complexity, dmm_unit_interfacing]
            #存进csv文件中
            with open(OutPath1, 'a', encoding='utf8', newline='') as file:
                writer = csv.writer(file)
                writer.writerow(Info)
            #文件信息File
            HasFile = 0;
            i = 0
            for file in commit.modified_files:
                HasFile = 1;
                filename = file.filename
                if filename.find('.java') == -1:
                    continue
                complexity = file.complexity
                token_count = file.token_count
                # for lastcommit in Repository(FilePath, single=LastCommitID).traverse_commits():
                #     FS = lastcommit
                #     for fl in FS:
                #         if fl.filename == filename:
                #             lastcomplexity = fl.complexity
                if i == 0:
                    Info = [CommitID, filename, complexity, token_count]
                else:
                    Info = ["", filename, complexity,  token_count]
                with open(OutPath2, 'a', encoding='utf8', newline='') as file:
                    writer = csv.writer(file)
                    writer.writerow(Info)
                    i = i + 1
            if HasFile == 0:
                Info = [CommitID, '/', 0, 0]
                with open(OutPath2, 'a', encoding='utf8', newline='') as file:
                    writer = csv.writer(file)
                    writer.writerow(Info)




if __name__ == "__main__":
    main()