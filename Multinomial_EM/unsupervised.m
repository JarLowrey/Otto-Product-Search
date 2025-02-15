function logProb = gaussmix(numClusters, dataFile, dataFileWithClusters, modelFile, numExamples, numFeatures)
    if isa(numClusters,'char')
        numClusters = str2int(numClusters);
    end
    
    [data, correctClusters] = scanIn(dataFile, numExamples, numFeatures, dataFileWithClusters);
    maxNumValuesPerFeat = max(data);
    [multinomials, priors,maxNumValues] = init(data,numClusters, maxNumValuesPerFeat);
    logProb = -realmax;
    
    
    repeat=true;
    iterationNo=0;
    maxIterations = 15;
    while repeat 
        [clusterExampleProbs] = eStep(data,numExamples,numFeatures,numClusters,priors, multinomials);
        [multinomials, priors] = mStep(data,numExamples,numFeatures,numClusters,multinomials,clusterExampleProbs,maxNumValues);
               
        %see if stopping condition has been met by finding total log Prob
%         probAfterIteration = totalLogLikelihoodOfData(numExamples,clusterLogDistDenominators);
%         repeat = (probAfterIteration-elogProb) > 0.001;
%         
        %total log probability is monotonically increasing. Otherwise there
        %is an error in the program
%         if ( probAfterIteration-logProb ) < 0
%            disp('ERROR : Total probability decreasing!!'); 
%            return
%         end
        
        % logProb = proffprintfbAfterIteration;
        iterationNo = iterationNo+1;
        % totalLogProbsVector(iterationNo) = probAfterIteration;
        repeat = iterationNo < maxIterations;
    end
    
    writeOutput(modelFile,multinomials,data);
    
    [correct, percentCorrect] = checkCorrectClustering(clusterExampleProbs, correctClusters, numClusters);
    
    disp(percentCorrect);
    
    % plotTotalLogProbData(totalLogProbsVector,dataFile);
end

function plotTotalLogProbData(totalLogProbVector,dataFile)
    figure(1);
    sizeOfVector = size(totalLogProbVector);
    plot((1:sizeOfVector(2)),totalLogProbVector(1,:) );
    title(dataFile);
    xlabel('Iteration');
    ylabel('Total Log Probability');

end

function [rawData, correctClusters] = scanIn( dataFile, numExamples, numFeatures, dataFileWithClusters)
    disp('Scanning in data');
    fid = fopen(dataFile,'r'); % Open text file
    
    r = [1 1 numExamples numFeatures];
    
    rawData = dlmread(dataFile, ',', r);
    fclose(fid);
    
    % read in header string for correct cluster file
    datafile2 = fopen(dataFileWithClusters, 'r');
    fgetl(datafile2);
    correctClusters = zeros(numExamples);
    for ex=1:numExamples
        str = fgetl(datafile2);
        cluster = sscanf(str,'Class_%d');
        correctClusters(ex) = cluster;
    end
                    
    fclose(datafile2);
end

function writeOutput(modelFile,multinomials,data)
    % fid = fopen(modelFile,'w'); % Open text file
    
    exAndFeat = size(data);
    numExamples = exAndFeat(1);
    numFeatures = exAndFeat(2);
    exAndClus = size(multinomials);
    numClusters = exAndClus(1);
    numValPerFeature = exAndClus(3);
%     
%     %find the max clusterLogDistribution of each example and save it as the
%     %cluster the example is assign to
%     assignedCluster = (1:numExamples).*0;
%     for ex=1:numExamples
%         max = -realmax;
%         
%         for c=1:numClusters
%             if(clusterLogDist(ex,c)>max)
%                 assignedCluster(ex) = c;
%                 max = clusterLogDist(ex,c);
%             end
%         end
%         
%         fprintf(fid,'%d ',assignedCluster(ex) );
%         fprintf(fid,repmat('%f ',[1,numFeatures]),data(ex,:) );
%         fprintf(fid,'\n');
%     end
%         
%     fclose(fid);

    for c=1:numClusters
        fid = fopen(strcat(modelFile, int2str(c), '.txt'), 'w');
        for f=1:numFeatures
            for v=1:numValPerFeature
                fprintf(fid, '%d ', multinomials(c, f, v));
            end
            fprintf(fid, '\n');
        end
        fclose(fid);
    end

end

% multinomials: (num clusters) x (num features) x (num values per fectures)
% array of integers representing counts of each value for each feature in a
% cluster
function [multinomials, priors, maxNumValues] = init(data, numClusters, maxNumValuesPerFeat)
    disp('Initializing data');
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
    maxNumValues = max(maxNumValuesPerFeat);
    
    %iterate through datapoints; assign each datapoint to a cluster and
    %update the assigned cluster's multinomial distribution
    multinomials = zeros(numClusters, numFeat, (maxNumValues + 1));
    
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
    
    % normalize multinomials
    for c=1:numClusters
        for f=1:numFeat
            % divide each feature value by the total number of data in the
            % cluster
            for v=1:maxNumValues
                multinomials(c, f, v) = multinomials(c, f, v) / numDataInCluster(c);
            end
        end
    end
    
end

function [probClusterEx] = eStep(data,numExamples,numFeatures,numClusters,priors, multinomials)
    disp('Performing E step');
    probClusterEx = zeros(numExamples, numClusters);

    for ex=1:numExamples   
        for c=1:numClusters
            % Calculate P(c : ex) = log P(c) + log P(ex : c) - log P(ex)
            prior = log(priors(c));
            
            % log P(ex : c) = (sum over all features k) log P(ex_k : c_k)
            prob_ex_c = double(0);
            for f = 1:numFeatures
                prob_ex_c = prob_ex_c + log(multinomials(c, f, data(ex, f) + 1));
            end
                        
            % log P(ex) = log (sum over all clusters) P(c) * P(ex : C)
            % use log-sum-exp trick            
            cluster_comps = zeros(numClusters);
            
            max_cluster_comp = 0;
            
            for ci = 1:numClusters
                prob_ex_c = double(priors(ci));
                
                for f1 = 1:numFeatures
                    prob_ex_c = prob_ex_c * multinomials (ci, f1, data(ex, f1) + 1);
                end
                
                cluster_comps(ci) = prob_ex_c;
                
                if prob_ex_c > max_cluster_comp
                    max_cluster_comp = prob_ex_c;
                end
            end
            
            % get log (sum over all clusters) exp(comps(c) - max_comp)
            cluster_log_sum = double(0);
            
            for ci = 1:numClusters
                cluster_log_sum = cluster_log_sum + exp(cluster_comps(ci) - max_cluster_comp);
            end
                        
            prob_ex = double(max_cluster_comp) + double(log(cluster_log_sum)) + 1;
            
            probClusterEx(ex, c) = prior + prob_ex_c - prob_ex;
        end
    end
end

function [multinomials, priors] = mStep(data, numExamples, numFeat, numClusters, multinomials, probClusterEx, maxNumValuesPerFeat)
    disp('Performing M step');
    % Update the cluster priors
    % P(c) = (1/ num examples) (sum over all examples) p(c | ex) [in
    % probClusterEx]
    priors = zeros(numClusters);
    
    prior_sum = double(0);
    
    for c = 1:numClusters
        ex_sum = double(0);
        
        for ex = 1:numExamples
            ex_sum = ex_sum + probClusterEx(ex, c);
        end
        
        priors(c) = ex_sum / numExamples;

        prior_sum = prior_sum + priors(c);
    end
    
    for c = 1:numClusters
        priors(c) = priors(c) / prior_sum;
    end
    
    % Update the multinomials
    % Naive approach, simply iterate through the examples and choose the
    % most likely cluster, updating that cluster's multinomial that way
    numDataInCluster = zeros(numClusters);
    
    for i=1:numExamples
        % get the cluster assignment
        mostLikelyCluster = 1;
        mostLikelyProb = probClusterEx(i, 1);
        equiprobableClusters = zeros(numClusters);
        equiprobableClusters(1) = 1;
        numEquiprobable = 1;
        
        for c=2:numClusters
            if probClusterEx(i, c) > mostLikelyProb
                mostLikelyProb = probClusterEx(i, c);
                equiprobableClusters = zeros(numClusters);
                equiprobableClusters(c) = 1;
                numEquiprobable = 1;
                mostLikelyCluster = c;
            elseif probClusterEx(i, c) == mostLikelyProb
                % If they're equiprobably, add to list of equiprobable ones
                equiprobableClusters(c) = 1;
                numEquiprobable = numEquiprobable + 1;
            end
        end
        
        % If numEquiprobable greater than one, choose a random equiprobable
        % element.
        if numEquiprobable > 1
            randElem = floor(numEquiprobable * rand());
            index = 0;
            for elem=1:numClusters
                if equiprobableClusters(elem) == 1
                    if index == randElem
                        mostLikelyCluster = elem;
                    end
                    index = index + 1;
                end
            end
        end
            
        numDataInCluster(mostLikelyCluster) = numDataInCluster(mostLikelyCluster) + 1;
        
        % iterate through the number of features and add 1 to the feature
        % value which the data has
        for j=1:numFeat
            multinomials(mostLikelyCluster, j, (data(i, j) + 1)) = multinomials(mostLikelyCluster, j, (data(i, j) + 1)) + 1;
        end
    end
    
    % normalize multinomials
    for c=1:numClusters
        disp(numDataInCluster(c));
        for f=1:numFeat
            % divide each feature value by the total number of data in the
            % cluster
            for v=1:max(maxNumValuesPerFeat)
                multinomials(c, f, v) = multinomials(c, f, v) / numDataInCluster(c);
            end
        end
    end
end

function totalLogProb = totalLogLikelihoodOfData(numExamples,clusterLogDistDenominators)    
    %{
    total likelihood = P(x1,x2,�,xn) = \prod_i \sum_c P(x_i | cluster_c) P(cluster_c)
    log (P(x1,x2,�,xn)) = log( \prod_i \sum_c P(x_i | cluster_c) P(cluster_c) )
                           =\sum_i log( \sum_c P(x_i | cluster_c) P(cluster_c) ) )
    Given each denominator = log(\sum_c P(x_i | cluster_c) P(cluster_c) ) ) then 
    log (P(x1,x2,�,xn)) = \sum_i (denominator_i)
    %}

    totalLogProb=0;
    for ex=1:numExamples
        totalLogProb = totalLogProb + clusterLogDistDenominators(ex);
    end
end

function [correct, percentCorrect] = checkCorrectClustering(probClusterEx, correctClusters, numClusters)
    correct = zeros(size(correctClusters));
    numElements = size(correctClusters);

    for e=1:numElements
        % for each example, check to see which cluster it fits in best.
                % get the cluster assignment
        mostLikelyCluster = 1;
        mostLikelyProb = probClusterEx(e, 1);
        equiprobableClusters = zeros(numClusters);
        equiprobableClusters(1) = 1;
        numEquiprobable = 1;
        
        for c=2:numClusters
            if probClusterEx(e, c) > mostLikelyProb
                mostLikelyProb = probClusterEx(e, c);
                equiprobableClusters = zeros(numClusters);
                equiprobableClusters(c) = 1;
                numEquiprobable = 1;
                mostLikelyCluster = c;
            elseif probClusterEx(e, c) == mostLikelyProb
                % If they're equiprobably, add to list of equiprobable ones
                equiprobableClusters(c) = 1;
                numEquiprobable = numEquiprobable + 1;
            end
        end
        
        % If numEquiprobable greater than one, choose a random equiprobable
        % element.
        if numEquiprobable > 1
            randElem = floor(numEquiprobable * rand());
            index = 0;
            for elem=1:numClusters
                if equiprobableClusters(elem) == 1
                    if index == randElem
                        mostLikelyCluster = elem;
                    end
                    index = index + 1;
                end
            end
        end
        
        if correctClusters(e) == mostLikelyCluster
            correct(e) = 1;
        end
    end
    
    percentCorrect = sum(correct) / numElements;
end





