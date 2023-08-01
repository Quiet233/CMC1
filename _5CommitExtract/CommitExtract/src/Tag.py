import pydriller
import git
from pydriller import Repository, Git
import pandas as pd
import numpy as np
import csv
def main():
    MetricFilePath = r"D:\Postgraduate\Metric-tool\TestOutput\CommitExtract\A2A98\avro\AvroData.csv"
    OutPath = r"D:\Postgraduate\Metric-tool\TestOutput\CommitExtract\A2A98\avro\Tag.csv"
    Metric = pd.read_csv(MetricFilePath,encoding='gb18030')
    Tag = []
    for index in Metric['A2A'].index:
        if Metric['A2A'][index] < 98:
            Tag.append('ArchChanged')
        else:
            Tag.append('NoArchChanged')
    Tag = pd.DataFrame(Tag)
    Tag.to_csv(OutPath, index=False, header='Tag')



if __name__ == "__main__":
        main()