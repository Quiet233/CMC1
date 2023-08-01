import weka.classifiers.Classifier;
import weka.core.WekaPackageManager;
import weka.classifiers.evaluation.Evaluation;
import weka.classifiers.evaluation.Prediction;
import weka.classifiers.evaluation.ThresholdCurve;
import weka.classifiers.trees.RandomForest;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;
import weka.core.converters.ArffLoader;
import weka.core.converters.CSVLoader;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Normalize;
//import weka.core.converters.ConverterUtils.DataSource;
import weka.classifiers.trees.RandomForest.*;
import weka.classifiers.meta.Bagging.*;
import weka.classifiers.Evaluation.*;
import weka.core.Instances.*;

import javax.activation.DataSource;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Random;

public class Main {
    public static void main(String[] args) throws Exception {
        WekaPackageManager.loadPackages(false);
        String ClassifyFile = "G:\\Metric-tool\\TestOutput\\CommitExtract\\avro\\AvroData_predict2.csv";
        String RegressionFile = "G:\\Metric-tool\\TestOutput\\CommitExtract\\avro\\AvroData_predict1.csv";

        File csvFile = new File(ClassifyFile);
        CSVLoader loader = new CSVLoader();
        loader.setSource(csvFile);
        Instances data = loader.getDataSet();

        File csvFile1 = new File(RegressionFile);
        CSVLoader loader1 = new CSVLoader();
        loader1.setSource(csvFile1);
        Instances data1 = loader1.getDataSet();

        ClassifyTask(data);
        RegressionTask(data1);

//        String[] options = new String[2];
//        options[0] = "-R";                                    // "range"
//        options[1] = "1";

        Remove remove = new Remove();// new instance of filter
        remove.setAttributeIndices("4,5,8");
        //remove.setInvertSelection(true);
        //remove.setOptions(options);                           // set options
        remove.setInputFormat(data);                          // inform filter about dataset **AFTER** setting options
        Instances newData = Filter.useFilter(data, remove);   // apply filter

        Remove remove1 = new Remove();// new instance of filter
        remove1.setAttributeIndices("4,5,8");
        remove1.setInputFormat(data1);
        Instances newData1 = Filter.useFilter(data1, remove1);

        ClassifyTask(newData);
        RegressionTask(newData1);
    }


    public static void ClassifyTask(Instances data) throws Exception {


        // setting class attribute if the data format does not provide this information
        // For example, the XRFF format saves the class attribute information as well
        data.setClassIndex(data.numAttributes() - 1);
        if (data.classIndex() == -1)
            data.setClassIndex(data.numAttributes() - 1);

        Classifier c1 = new RandomForest();
        Evaluation eval = new Evaluation(data);
        eval.crossValidateModel(c1, data, 10, new Random(129));
        ThresholdCurve tc = new ThresholdCurve();

        System.out.println(eval.toSummaryString());
        System.out.println(eval.toMatrixString());
        System.out.println(eval.toClassDetailsString());
        System.out.println("AUC:" + eval.weightedAreaUnderROC());
    }


    public static void RegressionTask(Instances data) throws Exception {

        // setting class attribute if the data format does not provide this information
        // For example, the XRFF format saves the class attribute information as well
        data.setClassIndex(data.numAttributes() - 1);
        if (data.classIndex() == -1)
            data.setClassIndex(data.numAttributes() - 1);

        Random rand = new Random(129);
        Instances randData = new Instances(data);
        randData.randomize(rand);
        if(randData.classAttribute().isNominal())
            randData.stratify(10);

        Classifier c1 = new RandomForest();
        Evaluation eval = new Evaluation(data);
        eval.crossValidateModel(c1, data, 10, new Random(129));
        System.out.println(eval.toSummaryString());
    }

    public static void FilterFuction() throws Exception {

    }

}