function logProb = gaussmix(numClusters, dataFile, modelFile, numExamples, numFeatures)
    if isa(numClusters,'char')
        numClusters = str2double(numClusters);
    end
    
    data = scanIn(dataFile, numExamples, numFeatures);
    maxNumValuesPerFeat = max(data);
    [multinomials, priors] = init(data,numClusters, maxNumValuesPerFeat);
    logProb = -realmax;
    
    repeat=true;
    iterationNo=0;
    while repeat 
        [clusterExampleProbs] = eStep(data,numExamples,numFeatures,numClusters,priors, multinomials);
        [means, variances, priors] = mStep(data,numExamples,numFeatures,numClusters,clusterExampleProbs);
        
        %see if stopping condition has been met by finding total log Prob
        probAfterIteration = totalLogLikelihoodOfData(numExamples,clusterLogDistDenominators);
        repeat = (probAfterIteration-logProb) > 0.001;
        
        %total log probability is monotonically increasing. Otherwise there
        %is an error in the program
        if ( probAfterIteration-logProb ) < 0
           disp('ERROR : Total probability decreasing!!'); 
           return
        end
        
        logProb = probAfterIteration;
        iterationNo = iterationNo+1;
        totalLogProbsVector(iterationNo) = probAfterIteration;
    end
    
    plotTotalLogProbData(totalLogProbsVector,dataFile);
    writeOutput(modelFile,clusterExampleProbs,data);
end

function plotTotalLogProbData(totalLogProbVector,dataFile)
    figure(1);
    sizeOfVector = size(totalLogProbVector);
    plot((1:sizeOfVector(2)),totalLogProbVector(1,:) );
    title(dataFile);
    xlabel('Iteration');
    ylabel('Total Log Probability');

end

function [rawData] = scanIn( dataFile, numExamples, numFeatures)
    fid = fopen(dataFile,'r'); % Open text file
    
    r = [1 1 numExamples numFeatures];
    
    rawData = dlmread(dataFile, ',', r);
                    
    fclose(fid);
end

function writeOutput(modelFile,clusterLogDist,data)
    fid = fopen(modelFile,'w'); % Open text file
    
    exAndFeat = size(data);
    numExamples = exAndFeat(1);
    numFeatures = exAndFeat(2);
    exAndClus = size(clusterLogDist);
    numClusters = exAndClus(2);
    
    %find the max clusterLogDistribution of each example and save it as the
    %cluster the example is assign to
    assignedCluster = (1:numExamples).*0;
    for ex=1:numExamples
        max = -realmax;
        
        for c=1:numClusters
            if(clusterLogDist(ex,c)>max)
                assignedCluster(ex) = c;
                max = clusterLogDist(ex,c);
            end
        end
        
        fprintf(fid,'%d ',assignedCluster(ex) );
        fprintf(fid,repmat('%f ',[1,numFeatures]),data(ex,:) );
        fprintf(fid,'\n');
    end
        
    fclose(fid);
end

% multinomials: (num clusters) x (num features) x (num values per fectures)
% array of integers representing counts of each value for each feature in a
% cluster
function [multinomials, priors] = init(data, numClusters, maxNumValuesPerFeat)
    %initialize cluster priors to a uniform distribution
    if numClusters==1
        priors = 1;
    else
        priors( 1:numClusters ) =  1.0 / double(numClusters) ;
    end
    
    %initialize priors for each cluster to a uniform
    %distribution
    numExAndFeat = size(data);
    numEx = numExAndFeat(1);
    numFeat = numExAndFeat(2);
    
    %iterate through datapoints; assign each datapoint to a cluster and
    %update the assigned cluster's multinomial distribution
    multinomials = zeros(numClusters, numFeat, (max(maxNumValuesPerFeat) + 1));
    
    
    % multinomials = zeros(numClusters,numFeat,maxNumValuesPerFeat);
    numDataInCluster = zeros(numClusters);
    for i=1:numEx
        % get the cluster assignment
        clusterAssignment = floor(numClusters * rand()) + 1;
        numDataInCluster(clusterAssignment) = numDataInCluster(clusterAssignment) + 1;
        
        % iterate through the number of features and add 1 to the feature
        % value which the data has
        for j=1:numFeat
            multinomials(clusterAssignment, j, (data(i, j) + 1)) = multinomials(clusterAssignment, j, (data(i, j) + 1)) + 1;
        end
    end
end

function [probClusterEx] = eStep(data,numExamples,numFeatures,numClusters,priors, multinomials)
    probClusterEx = zeros(numClusters, numExamples);

    for ex=1:numExamples        
        for c=1:numClusters
            % Calculate P(c : ex) = log P(c) + log P(ex : c) - log P(ex)
            prior = log(priors(c));
            
            % log P(ex : c) = (sum over all features k) log P(ex_k : c_k)
            double prob_ex_c = 0;
            for f = 1:numFeatures
                prob_ex_c = prob_ex_c + log(multinomials(c, f, data(ex, f) + 1));
            end
            
            % log P(ex) = log (sum over all clusters) P(c) * P(ex : C)
            % use log-sum-exp trick            
            cluster_comps = zeros(numClusters);
            
            max_cluster_comp = 0;
            
            for ci = 1:numClusters
                double prob_ex_c = priors(ci);
                
                for f1 = 1:numFeatures
                    prob_ex_c = prob_ex_c * multinomials (ci, f1, data(ex, f1) + 1);
                end
                
                cluster_comps(ci) = prob_ex_c;
                
                if prob_ex_c > max_cluster_comp
                    max_cluster_comp = prob_ex_c;
                end
            end
            
            % get log (sum over all clusters) exp(comps(c) - max_comp)
            double cluster_log_sum = 0;
            
            for ci = 1:numClusters
                cluster_log_sum = cluster_log_sum + exp(cluster_comps(ci) - max_cluster_comp);
            end
            
            double prob_ex = max_cluster_comp + log(cluster_log_sum);
            
            probClusterEx(c, ex) = prior + prob_ex_c + prob_ex;
        end
    end
end

function [means, variances, priors] = mStep(data,numExamples,numFeatures,numClusters,clusterLogDist)
    %NOTE :safe to convert from clusterLogDist back to probability as it is big
    probabilityDistribution = exp(clusterLogDist);
    weightsSummedOverDataExamples = sum(probabilityDistribution,1);
    
    %for each cluster prior, average the distribution over every examples
    %prior(cluster=c) = [ SUM OVER DATA EXAMPLES Prob(c|data) ]/numDataExamples
    priors = sum(probabilityDistribution,1)./double(numExamples);%prior = sum over column dimension/numData
    
    
    %for each mean, find weighted average of the data
    %MEAN(cluster=c) = [ SUM OVER DATA EXAMPLES data * Prob(c|data) ] / [SUM OVER DATA EXAMPLES Prob(c|data) ]
    means = zeros(numClusters,numFeatures);
    for c=1:numClusters
        for ex=1:numExamples
            %for each cluster mean, sum up the data features weighted by
            %probability
            means(c,:) = means(c,:) + data(ex,:) .* probabilityDistribution(ex,c);
        end
        
        means(c,:) = means(c,:) ./ weightsSummedOverDataExamples(c);
    end
    
    
    %for each variance, find distance from mean, square, and weight by dist
    %variance(cluster=c) = [SUM OVER DATA EXAMPLES (data-mean^2) * Prob(c|data)] / [SUM OVER DATA EXAMPLES Prob(c|data) ]
    variances = zeros(numClusters,numFeatures,numFeatures);
    for c=1:numClusters
        
        varianceDiagonal = zeros(1,numFeatures);
        for ex=1:numExamples
            %for every data feature, find sqaure distance from mean and
            %multiply by probability weight
            squareDistFromMean = ( data(ex,:) - means(c,:) ).^2;
            varianceDiagonal = varianceDiagonal + ( squareDistFromMean .* probabilityDistribution(ex,c) );
        end
        
        varianceDiagonal = varianceDiagonal ./ weightsSummedOverDataExamples(c);
        
        %assign the variances to the diagonal of the variance cluster matrix
        for f=1:numFeatures
            variances(c,f,f) = varianceDiagonal(f);
        end
    end
    
    %now we have cluster priors and the parameters that define a cluster,
    %means and variances of each data feature
end

function totalLogProb = totalLogLikelihoodOfData(numExamples,clusterLogDistDenominators)    
    %{
    total likelihood = P(x1,x2,…,xn) = \prod_i \sum_c P(x_i | cluster_c) P(cluster_c)
    log (P(x1,x2,…,xn)) = log( \prod_i \sum_c P(x_i | cluster_c) P(cluster_c) )
                           =\sum_i log( \sum_c P(x_i | cluster_c) P(cluster_c) ) )
    Given each denominator = log(\sum_c P(x_i | cluster_c) P(cluster_c) ) ) then 
    log (P(x1,x2,…,xn)) = \sum_i (denominator_i)
    %}

    totalLogProb=0;
    for ex=1:numExamples
        totalLogProb = totalLogProb + clusterLogDistDenominators(ex);
    end
end






