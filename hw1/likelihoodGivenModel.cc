// Alane Suhr
// CSE 5522
// Homework 1 due 5 Feb
#include "Model.hh"
#include <iostream>

using namespace std;

/**
 * Calculate the log-likelihood of the given dataset and the given model.
 * Takes two arguments.
 *     name of data file
 *     name of model file
 */
int main(int argc, char *argv[]) {
  char *dataFileS = argv[1];
  char *modelFileS = argv[2];

  // Create a new model given the data file and the model file.
  Model *model = new Model(dataFileS, modelFileS);

  cout << "Log Likelihood is " << model->logLikelihood() << endl;
}
