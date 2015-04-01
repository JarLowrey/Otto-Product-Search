#ifndef CLUSTER_H_
#define CLUSTER_H_

#include <vector>

/**
 * Each cluster has a prior, a set of means (one per feature) and a set of variances (one per feature)
 */
class Cluster {
public:
  double prior;
  std::vector<double> means;
  std::vector<double> variances;
};

class Model {
  // Contains clusters and the dataset
  std::vector<Cluster *> clusters_;
  std::vector<std::vector<double> > examples_;

  // This is the same as examples_ except transformed so that it's sorted on features first.
  std::vector<std::vector<double> > featureSortedData_;

  // log P(cluster|example). Sorted on example first, then cluster. 
  // I.e. [i][j] is ith example, jth cluster.
  std::vector<std::vector<double> > logLikelihoodsForClustersAndExamples_;

  int numClusters_;
  int numFeaturesPerExample_;

public:
  // Constructors
  Model(char *filename, int numClusters);
  Model(char *dataFile, char *modelFile);

  // Prints cluster information to given filename
  void printClusters(char *filename);

  // Prints the clusters assigned to each datapoint
  void printDataClusters();

  // Initializes the clusters for before EM
  void initializeClusters();

  // Generates a random mean for each feature
  std::vector<double> randomMeansOfFeatures(int seed);

  // Generates a random variance for each feature
  std::vector<double> varianceOfFeatures();

  // Prints information about the given cluster (prior, means, variances)
  void printClusterInformation(Cluster *cluster);

  // Recomputes log P(cluster|example) for each cluster-example pair
  void expectationStep();

  // Maximizes log likelihood by recalculating prior, means, and variances per cluster
  void maximizationStep();

  // Returns log likelihood for model
  double logLikelihood();

  // Returns the log of the probability of the feature occurring in the distribution for that cluster's feature
  double logGaussianGivenCluster(int exampleIndex, int clusterIndex, int featureIndex);
};

#endif /* CLUSTER_H_ */
