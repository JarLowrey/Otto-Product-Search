Authors: James Lowery, Spencer Morris, Alane Suhr, David Wright
Class: CSE 5522
Assignment: Final Project Report
Due: 04/30/2015
Last Updated: 04/28/2015

This project compares various models of machine learning for a product 
classification task. Each model resides in its own directory with its
own copy of the data in an appropriate format. 

To run the Python script with the support vector machine implementation, we 
recommend loading the files into a PyCharm project and running from there. You 
should expect a success rate of 75% to 99%, depending on which implementation 
you choose. There are no arguments passed to this program, so it is important
that it is run from the directory as provided.

To run the MATLAB code, which is an implementation of the EM algorithm,
move MATLAB's working directory to the directory with the file unsupervised.m. 
Run the function "unsupervised" with the parameters
unsupervised(9, 'train.csv', 'trainWClassNames.csv', 'model', 61878, 93)
Note that the MATLAB model has the potential to run out of memory and crash.

To run the Java code, which is an implementation of Naive Bayes, we recommend
that you use Eclipse to run the program. You can create a new Project, then
Import as a File System the contents of the directory Multinomial_Naive_Bayes.
Then run the main method in Test.java with the command line arguments:
"train.txt train_with_class_name_removed.txt"
You should expect a success rate of 64%.