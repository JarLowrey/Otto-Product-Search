// Alane Suhr
// CSE 5522
// Homework 1 due 5 February
#include <iostream>
#include <fstream>
#include <vector>
#include <stdlib.h>
#include "Model.hh"
#include <float.h>
#define IMPROVEMENT_THRESHOLD 0.0001
#define ITERATION_LIMIT 500

using namespace std;

/**
 * Main function takes 3 arguments:
 *   # clusters
 *   data file (input)
 *   model file (output)
 * Creates and refines clusters using the EM algorithm.
 */
int main(int argc, char *argv[]) {
  char *numClustersS = argv[1];
  char *dataFileS = argv[2];
  char *modelFileS = argv[3];

  int numClusters = atoi(numClustersS); 

  Model *model = new Model(dataFileS, numClusters);

  // Initialize the clusters. 
  model->initializeClusters();

  // Start log likelihood really low so that there will be a lot of improvement at the beginning?
  double prevLogLikelihood;
  double currentLogLikelihood = -DBL_MAX;
  double percentChange;

  int iter = 0;

  cout << "Beginning EM.\n";

  ofstream logLikelihoodFile;
  logLikelihoodFile.open("logLikelihoods.csv");

  // Continue EM while the percent change is larger than a certain percentage
  do {
    prevLogLikelihood = currentLogLikelihood;

    cout << "\nIteration #" << iter << endl;

    // EM
    model->expectationStep();
    model->maximizationStep();
    iter++;

    currentLogLikelihood = model->logLikelihood();
    logLikelihoodFile << iter << "," << currentLogLikelihood << endl;

    // Calculate percent change
    percentChange = (currentLogLikelihood - prevLogLikelihood) / currentLogLikelihood;
    if (percentChange < 0) {
      percentChange = -percentChange;
    }

    // Debugging
    cout << "New log likelihood is " << currentLogLikelihood << " (prev was " << prevLogLikelihood;
    cout << ", error is " << (percentChange * 100) << "%)\n";

    if (percentChange > IMPROVEMENT_THRESHOLD) {
      cout << "Hasn't converged. Continue running EM.\n";
    }
  } while (iter < ITERATION_LIMIT && percentChange > IMPROVEMENT_THRESHOLD);

  // Output the model file and the data cluster assignments
  model->printClusters(modelFileS);
  model->printDataClusters();

  logLikelihoodFile.close();

  return 0;
}


