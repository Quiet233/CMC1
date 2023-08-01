# -*- coding = utf-8 -*-
# @Time : 2023/4/25 10:49
# @Author : 詹文静
# @File : Sample.py
# @Software : PyCharm
import numpy as np
import pandas as pd
import csv


def main():
    ProjectName = 'avro'
    DataPath = r'D:\Postgraduate\Metric-tool\TestOutput\CommitExtract\A2A98\\' + ProjectName + '\AvroData.csv'
    OutPath = r'D:\Postgraduate\Metric-tool\TestOutput\CommitExtract\A2A98\\' + ProjectName + '\SampleData.csv'
    Data = pd.read_csv(DataPath, encoding='gb18030')
    Data1 = Data.loc[Data['Tag'] == 'NoArchChanged']
    Data2 = Data.loc[Data['Tag'] == 'ArchChanged']
    NewData1 = pd.DataFrame(Data1)
    m = len(Data1['Tag'])
    k = len(Data2['Tag'])
    Data1.reset_index(drop=True, inplace=True)
    Data2.reset_index(drop=True, inplace=True)
    n = np.random.randint(0, m, k)
    Sample = pd.DataFrame()

    for cl in Data1.columns:
        Sample[cl] =  None

    l = NewData1.values.tolist()
    for i in n:
        Sample.loc[len(Sample)] = l[i]
        #Sample = pd.concat([Sample, df])

    Sample = pd.concat([Sample, Data2])
    Sample.reset_index(drop=True, inplace=True)
    Sample.to_csv(OutPath, sep=',', index=None)

if __name__ == '__main__':
    main()
