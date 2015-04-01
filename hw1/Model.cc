// Alane Suhr
// CSE 5522
// Homework 1 due 5 Feb
#include "Model.hh"
#include <fstream>
#include <iostream>
#include <algorithm>
#include <string>
#include <cmath>

#define VARIANCE_PROPORTION 0.5
#define CONST_E 2.718281828459
#define CONST_2PI 6.28318530718
#define CONST_EPSILON 0.000000000001

using namespace std;

/**
 * Constructor for the class without a preexisting model/parameters. 
 */
Model::Model(char *filename, int numClusters) : numClusters_(numClusters) {
  ifstream dataFile;
  dataFile.open(filename);

  cout << "Successfully opened training file " << filename << ".\n";

  // Get the number of examples and features
  int numExamples, numFeatures;
  dataFile >> numExamples >> numFeatures;

  numFeaturesPerExample_ = numFeatures;

  // Get the data.
  for (int i = 0; i < numExamples; i++) {
    vector<double> exampleData;
    for (int j = 0; j < numFeaturesPerExample_; j++) {
      double data;
      dataFile >> data;
      exampleData.push_back(data);
    }
    examples_.push_back(exampleData);
  }

  // Create (redundant) matrix sorted on feature, not example.
  for (int i = 0; i < numFeaturesPerExample_; i++) {
    vector<double> featureData;
    for (int j = 0; j < numExamples; j++) {
      featureData.push_back(examples_[j][i]);
    }
    featureSortedData_.push_back(featureData);
  }

  cout << "Successfully gathered existing data: " << numExamples << " examples with ";
  cout << numFeatures << " features each.\n";

  // Fill in log likelihoods with zero.
  for (int i = 0; i < numExamples; i++) {
    vector<double> likelihoodsForExample;

    for (int j = 0; j < numClusters; j++) {
      likelihoodsForExample.push_back(0);
    }
    logLikelihoodsForClustersAndExamples_.push_back(likelihoodsForExample);
  }

  dataFile.close();
}

/**
 * Constructor of class with preexisting model. 
 */
Model::Model(char *dataFileS, char *modelFileS) {
  ifstream dataFile;
  dataFile.open(dataFileS);

  cout << "Successfully opened testing file " << dataFileS << ".\n";

  // Get the number of examples and featurs
  int numExamples, numFeatures;
  dataFile >> numExamples >> numFeatures;

  numFeaturesPerExample_ = numFeatures;

  // Get the data.
  for (int i = 0; i < numExamples; i++) {
    vector<double> exampleData;
    for (int j = 0; j < numFeaturesPerExample_; j++) {
      double data;
      dataFile >> data;
      exampleData.push_back(data);
    }
    examples_.push_back(exampleData);
  }

  // Create feature-sorted data. 
  for (int i = 0; i < numFeaturesPerExample_; i++) {
    vector<double> featureData;
    for (int j = 0; j < numExamples; j++) {
      featureData.push_back(examples_[j][i]);
    }
    featureSortedData_.push_back(featureData);
  }

  dataFile.close();

  cout << "Successfully loaded testing data.\n";

  // Load the pre-existing model.
  ifstream modelFile;
  modelFile.open(modelFileS);

  modelFile >> numClusters_ >> numFeatures;

  for (int i = 0; i < numClusters_; i++) {
    Cluster *cluster = new Cluster();

    // Get the prior
    modelFile >> cluster->prior;

    // Get the means
    for (int j = 0; j < numFeatures; j++) {
      double mean;
      modelFile >> mean;
      cluster->means.push_back(mean); 
    }

    // Get the variances
    for (int j = 0; j < numFeatures; j++) {
      double variance;
      modelFile >> variance;
      cluster->variances.push_back(variance);
    }

    clusters_.push_back(cluster);
  }

  // Initialize log likelihoods for cluster-example pairs. 
  for (int i = 0; i < numExamples; i++) {
    vector<double> likelihoodsForExample;

    for (int j = 0; j < numClusters_; j++) {
      likelihoodsForExample.push_back(0);
    }
    logLikelihoodsForClustersAndExamples_.push_back(likelihoodsForExample);
  }
}

/**
 * Prints cluster informatino to the given filename.
 */
void Model::printClusters(char *filename) {
  ofstream dataFile;
  dataFile.open(filename);

  cout << "Successfully opened file for writing clusters (" << filename << ")\n";

  // Print numclusters and numfeatures.
  dataFile << numClusters_ << " " << numFeaturesPerExample_ << endl;

  // Print data for each cluster.
  for (int i = 0; i < numClusters_; i++) {
    dataFile << clusters_[i]->prior << " ";

    // Print means
    for (int j = 0; j < numFeaturesPerExample_; j++) {
      dataFile << clusters_[i]->means[j] << " ";
    }

    // Print variances
    for (int j = 0; j < numFeaturesPerExample_; j++) {
      dataFile << clusters_[i]->variances[j] << " ";
    }

    dataFile << endl;
  }

  dataFile.close();
}

/**
 * Prints the clusters assigned to each data point to a file.
 */
void Model::printDataClusters() {
  ofstream clusteredDataFile;
  clusteredDataFile.open("clusteredData.csv");

  // Find the best cluster for each datapoint and print it. 
  for (unsigned int i = 0; i < examples_.size(); i++) {
    int bestCluster = 0;
    double maxLogLikelihood = logLikelihoodsForClustersAndExamples_[i][0];

    // Iterate through the clusters to find the max log likelihood
    for (int j = 1; j < numClusters_; j++) {
      double logLikelihood = logLikelihoodsForClustersAndExamples_[i][j];

      if (logLikelihood > maxLogLikelihood) {
        maxLogLikelihood= logLikelihood;
        bestCluster = j;
      }
    }
    clusteredDataFile << i << "," << bestCluster << endl;
  }

  clusteredDataFile.close();
}

/**
 * Initializes clusters for a model which doesn't contain preexisting clusters.
 */
void Model::initializeClusters() {
  // Get the means and variances of the features as a uniform distribution
  cout << "Initializing " << numClusters_ << " clusters.\n";

  vector<double> varianceOfFeatures = Model::varianceOfFeatures();

  // Initialize clusters
  for (int i = 0; i < numClusters_; i++) {
    // Calculate prior for cluster
    Cluster *cluster = new Cluster();
    cluster->prior = 1.0 / numClusters_;
    vector<double> meanOfFeatures = Model::randomMeansOfFeatures(i);
 
    // Calculate mean and variance for each feature
    for (int j = 0; j < numFeaturesPerExample_; j++) {
      cluster->means = meanOfFeatures;
      cluster->variances = varianceOfFeatures;
    }

    // Debugging
    cout << i << ": ";
    printClusterInformation(cluster);
    cout << endl;
    clusters_.push_back(cluster);
  }

  cout << "Successfully initialized clusters.\n";
}

/**
 * Prints information for a single cluster.
 */
void Model::printClusterInformation(Cluster *cluster) {
  cout << cluster->prior;

  // Means
  for (int i = 0; i < numFeaturesPerExample_; i++) {
    cout << " " << cluster->means[i];
  }

  // Variances
  for (int i = 0; i < numFeaturesPerExample_; i++) {
    cout << " " << cluster->variances[i];
  }
}

/**
 * Generates a vector of random means for each feature.
 */
vector<double> Model::randomMeansOfFeatures(int seed) {
  vector<double> means;

  // Get a mean for a feature
  for (int i = 0; i < numFeaturesPerExample_; i++) {
    // Need max and min if we want to pick a random value in the range.
    double min = *min_element(featureSortedData_[i].begin(), featureSortedData_[i].end());
    double max = *max_element(featureSortedData_[i].begin(), featureSortedData_[i].end());

    sleep(0.1);
    srand(time(NULL) + seed + i);

    // Add mean: average of max and min.
    // means.push_back(min + (double)rand() * (max - min) / RAND_MAX);

    // This method is actually better (explained in homework responses)
    means.push_back(featureSortedData_[i][(int)rand() * examples_.size() / RAND_MAX]);
  }

  return means;
}

/**
 * Generates a vector of variances for each feature.
 */ 
vector<double> Model::varianceOfFeatures() {
  vector<double> variances;

  // Get a variance for each feature
  for (int i = 0; i < numFeaturesPerExample_; i++) {
    double min = *min_element(featureSortedData_[i].begin(), featureSortedData_[i].end());
    double max = *max_element(featureSortedData_[i].begin(), featureSortedData_[i].end());

    // Variance is a constant fraction of the range of the feature data
    variances.push_back((max - min)*VARIANCE_PROPORTION);
  } 

  return variances;
}

/**
 * Updates the log-likelihood for each cluster-example pair.
 */
void Model::expectationStep() {
  for (unsigned int i = 0; i < examples_.size(); i++) {
    for (int j = 0; j < numClusters_; j++) {
      // Add the log of the cluster prior.
      double result = log(clusters_[j]->prior);

      // Add the Gaussian probability sums for each feature. We are assuming the features are independent.
      double gaussianProbSum = 0;
      for (int k = 0; k < numFeaturesPerExample_; k++) {
        gaussianProbSum += logGaussianGivenCluster(i, j, k);
      }
      result += gaussianProbSum;

      // Calculate the denominator, log P(example). Use log-sum-exp trick. 
      vector<double> logSumExpTermsForClusters;
      for (int l = 0; l < numClusters_; l++) {
        // Add the log of the cluster prior.
        double logSumExpForCluster = log(clusters_[l]->prior);

        // Add the Gaussian probability sums for each feature.
        double gaussianProbabilitySumForCluster = 0;
        for (int k = 0; k < numFeaturesPerExample_; k++) {
          gaussianProbabilitySumForCluster += logGaussianGivenCluster(i, l, k);
        }
        logSumExpForCluster += gaussianProbabilitySumForCluster;
        logSumExpTermsForClusters.push_back(logSumExpForCluster);
      }

      // Now actually do the log-sum-exp trick.
      double logSumExpMaxTerm = *max_element(logSumExpTermsForClusters.begin(), logSumExpTermsForClusters.end());
      double logSumExpSumTerm = 0;

      for (int l = 0; l < numClusters_; l++) {
        logSumExpSumTerm += pow(CONST_E, logSumExpTermsForClusters[l] - logSumExpMaxTerm);
      }

      double totalLogSumExpTerm = logSumExpMaxTerm + log(logSumExpSumTerm);

      result -= totalLogSumExpTerm;

      // Result =        log (P(cluster)P(data|cluster) / P(data))
      //        = (log of each) prior + Gaussian prob  +  P(data)
      logLikelihoodsForClustersAndExamples_[i][j] = result;
    }
  }

  cout << "Successfully completed expectation step.\n";
}

/** 
 * Generates the log of the probability that a feature has the given value
 * given that it's a Gaussian distribution of the cluster
 */
double Model::logGaussianGivenCluster(int exampleIndex, int clusterIndex, int featureIndex) {
  // This is a modified version of the gaussian PDF so that it returns the log of the gaussian,
  // not the straight probability. This avoids underflow.
  double value = examples_[exampleIndex][featureIndex];
  double mean = clusters_[clusterIndex]->means[featureIndex];
  double variance = clusters_[clusterIndex]->variances[featureIndex];

  return -((pow(value - mean, 2)/variance) + log(CONST_2PI * variance))/2;
} 

/**
 * Reevaluates the cluster's prior, means, and variances, maximizing the log likelihood of the model.
 */
void Model::maximizationStep() {
  for (int i = 0; i < numClusters_; i++) {
    // First get the new prior.
    double sumOfLogLikelihoods = 0;
    for (unsigned int j = 0; j < examples_.size(); j++) {
      sumOfLogLikelihoods += pow(CONST_E, logLikelihoodsForClustersAndExamples_[j][i]);
    }

    clusters_[i]->prior = sumOfLogLikelihoods / examples_.size();

    for (int j = 0; j < numFeaturesPerExample_; j++) {
      double sumOfMeans = 0;
      // First calculate the mean for feature j.
      for (unsigned int k = 0; k < examples_.size(); k++) {
        sumOfMeans += examples_[k][j] * pow(CONST_E, logLikelihoodsForClustersAndExamples_[k][i]);
      }

      clusters_[i]->means[j] = sumOfMeans / sumOfLogLikelihoods;

      // Next calculate the variance using the new mean.
      double sumOfVariances = 0;
      for (unsigned int k = 0; k < examples_.size(); k++) {
        sumOfVariances += pow(examples_[k][j] - clusters_[i]->means[j], 2) * pow(CONST_E, logLikelihoodsForClustersAndExamples_[k][i]);
      }
      clusters_[i]->variances[j] = sumOfVariances / sumOfLogLikelihoods;
    }
  }
  cout << "Successfully completed maximization step.\n";
}

/**
 * Generates the log likelihood of the model.
 */
double Model::logLikelihood() {
  double logLikelihood = 0;

  // Sum up over all the examples
  for (unsigned int i = 0; i < examples_.size(); i++) {
    vector<double> clusterLikelihoods;

    // Find P(cluster, example) = P(example|cluster)P(cluster)
    for (int j = 0; j < numClusters_; j++) {
      double clusterLikelihood = log(clusters_[j]->prior);

      double logGaussianSum = 0;
      for (int k = 0; k < numFeaturesPerExample_; k++) {
        logGaussianSum += logGaussianGivenCluster(i, j, k);
      }

      clusterLikelihood += logGaussianSum;
      clusterLikelihoods.push_back(clusterLikelihood);
    }

    double maxLikelihood = *max_element(clusterLikelihoods.begin(), clusterLikelihoods.end());

    // Use the log-sum-exp trick to avoid underflow for each P(cluster, example)
    double logSumExpSum = 0;
    for (int j = 0; j < numClusters_; j++) {
      logSumExpSum += pow(CONST_E, clusterLikelihoods[j] - maxLikelihood);
    }

    logLikelihood += maxLikelihood + log(logSumExpSum);
  }

  return logLikelihood;
}
