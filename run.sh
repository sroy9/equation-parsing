## Script to run various configurations of equation parsing framework
if [ "$#" -eq 0 ]
then echo "Usage : sh run.sh (Numoccur/Var/Tree/Pipeline/Joint) 
Pipeline assumes component models to exist"
    exit 2
fi

## Numoccur model
if [ "$1" = "Numoccur" ]
then
    echo "Running Numoccur model"
    java -cp target/classes/:target/dependency/* numoccur.NumoccurDriver 1>log/Numoccur.out
fi


## Variable Prediction model
if [ "$1" = "Var" ]
then
    java -cp target/classes/:target/dependency/* var.VarDriver 1>log/Var.out 
fi


## Tree Prediction model
if [ "$1" = "Tree" ]
then
    echo "Running Tree model"
    java -cp target/classes/:target/dependency/* tree.TreeDriver 1>log/Tree.out 
fi

## LastTwo Prediction model
if [ "$1" = "Lasttwo" ]
then
    echo "Running Lasttwo model"
    java -cp target/classes/:target/dependency/* lasttwo.LasttwoDriver 1>log/Lasttwo.out 
fi

## Pipeline
if [ "$1" = "Pipeline" ]
then
    if [ "$2" = "A" ]
    then	
	echo "Running PipelineA"
	java -cp target/classes/:target/dependency/* pipeline.PipelineDriver A 1>log/PipelineA.out
    fi
    if [ "$2" = "B" ]
    then	
	echo "Running PipelineB"
	java -cp target/classes/:target/dependency/* pipeline.PipelineDriver B 1>log/PipelineB.out
    fi	
fi


## Joint
if [ "$1" == "Joint" ]
then
    echo "Running Joint"
    java -cp target/classes/:target/dependency/* joint.JointDriver 1>log/Joint.out
fi
