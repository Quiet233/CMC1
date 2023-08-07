# ACMC
Architecture Change Metrics Calculator

# Metrics
The metrics calculated by this tool are as follows：
|     Type                 |     Feature        |     Definition                                                                                                |   |   |   |   |   |   |   |
|--------------------------|--------------------|---------------------------------------------------------------------------------------------------------------|---|---|---|---|---|---|---|
|     Volume-related       |     NCC            |     Number of characters   changed                                                                            |   |   |   |   |   |   |   |
|                          |     LC             |     Lines of code changes                                                                                     |   |   |   |   |   |   |   |
|                          |     NMC            |     Number of methods   changed                                                                               |   |   |   |   |   |   |   |
|     File-related         |     NMS            |     Number of modified subsystems                                                                             |   |   |   |   |   |   |   |
|                          |     NMD            |     Number of modified directories                                                                            |   |   |   |   |   |   |   |
|                          |     NMF            |     Number of modified files                                                                                  |   |   |   |   |   |   |   |
|                          |     Entropy        |     Distribution of modified code across each file                                                            |   |   |   |   |   |   |   |
|                          |     NAD            |     Number of added or deleted files                                                                          |   |   |   |   |   |   |   |
|                          |     MLMF           |     Max lines of code in a modified file before the change                                                    |   |   |   |   |   |   |   |
|                          |     CFC            |     Cyclomatic complexity   of file changes                                                                   |   |   |   |   |   |   |   |
|                          |     NUC            |     The max number of changes to the modified files                                                           |   |   |   |   |   |   |   |
|                          |     AGE            |     The average time interval between the last and   current change                                           |   |   |   |   |   |   |   |
|     Developer-related    |     NDVE           |     Number of developers                                                                                      |   |   |   |   |   |   |   |
|                          |     REXP           |     Recent developer experience                                                                               |   |   |   |   |   |   |   |
|                          |     EXP            |     Developer experience                                                                                      |   |   |   |   |   |   |   |
|     Issue-related        |     Issue-Type     |     Most of commits are assigned a type to indicate   its purpose (e.g., Bug, Improvement and New Feature)    |   |   |   |   |   |   |   |
|                          |     NIR            |     Number of issue reports                                                                                   |   |   |   |   |   |   |   |

# Usage
1) environment:  you should set up Java environment.(jdk1.8) you should set up Python3.10 environment.
2) All input files required：  
MetricResults.csv: A csv file that records the a2a values of adjacent commits between releases  
Map.txt: The IDs of all commits obtained in order of submission  
log.txt: A log files between releases that can be get by git  
SearchRequest.xml: A XML file that can be get on many version management platform like JIRA
releaseNum_file.txt: This file records the number of code lines of the first commit in the log, which can be obtained through the git command

4) run file：  
IssueType.java: calculate Issue-relate metrics   
ComplexityTest.py:calculate NCC NMC CFC  
ModifyInfo.java: remaining metrics  

Notice：  
1）The operation of the ComplexityTest.py file is based on two other java files  
2）Clone the required program before running the program 


# Other Files
1) _Predict：The main.java file under this folder is the program used for prediction when building the prediction model
2) TestOutput：This folder stores all the a2a metric obtained in our experiments
3) TestOutput\CommitExtract：This folder stores all the metrics we calculated through this tool
