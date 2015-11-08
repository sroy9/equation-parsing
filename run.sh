## Script to run various configurations of equation parsing framework
if [ "$#" -eq 0 ]
then echo "Usage : sh run.sh (Numoccur/Var/LCA/LastTwo/Pipeline/Joint) 
If Pipeline, should have 2 more parameters : (SP/D) for NumOccur and (SP/D) for LCA predictor)
If Numoccur or LCA, should have one more parameter (SP/D)
SP : Structured Model D : Decomposed model
Pipeline assumes component models to exist"
    exit 2
fi

## Numoccur model
if [ "$1" = "Numoccur" ]
then
    if [ "$2" = "SP" ]
    then
	echo "Running Numoccur SP model"
	java -cp target/classes/:target/dependency/* struct.numoccur.NumoccurDriver 1>log/NumoccurSP.out 
    fi
    if [ "$2" = "D" ]
    then
	echo "Running Numoccur Decomposed model"
	java -cp target/classes/:target/dependency/* numoccur.NumoccurDriver 1>log/NumoccurD.out 
    fi
fi


## Variable Prediction model
if [ "$1" = "Var" ]
then
    java -cp target/classes/:target/dependency/* var.VarDriver 1>log/Var.out 
fi



## LCA model
if [ "$1" = "LCA" ]
then
    if [ "$2" = "SP" ]
    then
	echo "Running LCA SP model"
	java -cp target/classes/:target/dependency/* struct.lca.LcaDriver 1>log/LcaSP.out 
    fi
    if [ "$2" = "D" ]
    then
	echo "Running LCA Decomposed model"
	java -cp target/classes/:target/dependency/* lca.LcaDriver 1>log/LcaD.out 
    fi
fi


## Pipeline
if [ "$1" = "Pipeline" ]
then
    if [ "$2" = "SP" -a "$3" = "SP" ]
    then
	echo "Running Pipeline, with SP:Numoccur SP:Var SP:Tree"
	java -cp target/classes/:target/dependency/* inference.ConsDriver SP SP 1>log/PipelineSPSP.out
    fi
    if [ "$2" = "D" -a "$3" = "SP" ]
    then
	echo "Running Pipeline, with D:Numoccur SP:Var SP:Tree"
	java -cp target/classes/:target/dependency/* inference.ConsDriver D SP 1>log/PipelineDSP.out
    fi
    if [ "$2" = "SP" -a "$3" = "D" ]
    then
	echo "Running Pipeline, with SP:Numoccur SP:Var D:Tree"
	java -cp target/classes/:target/dependency/* inference.ConsDriver SP D 1>log/PipelineSPD.out
    fi
    if [ "$2" = "D" -a "$3" = "D" ]
    then
	echo "Running Pipeline, with D:Numoccur SP:Var D:Tree"
	java -cp target/classes/:target/dependency/* inference.ConsDriver D D 1>log/PipelineDD.out
    fi
fi

## Joint
if [ "$1" == "Joint" ]
then
    echo "Running Joint"
    java -cp target/classes/:target/dependency/* tree.TreeDriver 1>log/Joint.out 2>log/Joint.err &
fi
