Code and data used for the paper

Equation Parsing : Mapping Sentences to Grounded Equations
Subhro Roy and Dan Roth
EMNLP 2016


Data can be found in the folder data/equationparsebrat/.

Instructions to run the code :

1. sh run.sh Numoccur 
- Predicts quantity trigger list. Output file log/Numoccur.out

2. sh run.sh Var 
- Predicts variable trigger list. Output file log/Var.out

3. sh run.sh Tree 
- Predicts equation tree. Output file log/Tree.out

4. sh run.sh Pipeline
- Runs the first three classifiers in a pipeline, and predicts complete equation parse. Output file log/Pipeline.out

5. sh run.sh 
- Runs the pipeline method end to end, basically runs steps 1 to 4.

6. sh run.sh Joint
- Runs the joint model to predict complete equation parse. Output file : log/Joint.out








