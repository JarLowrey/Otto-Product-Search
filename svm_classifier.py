import csv
from sklearn import svm

# Count the number of data points to be processed, and
# how many features there are per item
with open("train.csv", 'r') as csvfile:
    trainingReader = csv.reader(csvfile, dialect='excel')
    numDataPoints = 0
    numFeatures = 0
    for line in trainingReader:
        numDataPoints += 1
        numFeatures = len(line) - 1
    csvfile.close()

# Read the input data into the format needed for the SciKit-Learn library
with open("train.csv", 'r') as csvfile:
    trainingReader = csv.reader(csvfile, dialect='excel')
    index = 0

    inputData = [[0 for i in range(numFeatures)] for j in range(numDataPoints)]
    labels = [0 for i in range(numDataPoints)]
    for line in trainingReader:
        # Create a data point list holding the features' values
        dataPoint = [0 for i in range(len(line) - 1)]
        for i in range(len(line) - 1):
            dataPoint[i] = int(line[i])
        labels[index] = line[i + 1]

        # Then add that data point to our overall array
        inputData[index] = dataPoint
        index += 1
    csvfile.close()

print("Entering support vector learning section")
svc = svm.SVC()
svc.fit(inputData, labels)

# Testing the effectiveness of the classification against the training data
print("Training complete, entering prediction section")
predictions = svc.predict(inputData)
print("Prediction complete, tallying correct predictions")
if not(len(labels) == len(predictions)):
    print("Error: number of predictions is not the same as number of labels")
    exit()
correct = 0
incorrect = 0
for i in range(len(labels)):
    if labels[i] == predictions[i]:
        correct += 1
    else:
        incorrect += 1
print("Success ratio: ", correct / (correct + incorrect))
