Classification: Classification.cc
	g++ -Wall -std=c++0x -g -c Model.cc -o Model.o
	g++ -Wall -g -c Classification.cc -o Classification.o
	g++ -Wall Model.o Classification.o -lm -o EMAlgorithm
	rm Model.o
	rm Classification.o
Testing: likelihoodGivenModel.cc
	g++ -Wall -std=c++0x -g -c Model.cc -o Model.o
	g++ -Wall -g -c likelihoodGivenModel.cc -o likelihoodGivenModel.o
	g++ -Wall Model.o likelihoodGivenModel.o -lm -o testModel
	rm Model.o
	rm likelihoodGivenModel.o
