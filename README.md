# Equation Parsing

Code and data used for the paper

Subhro Roy, Shyam Upadhyay and Dan Roth.
Equation Parsing : Mapping Sentences to Grounded Equations.
EMNLP 2016.

If you use the code or data, please cite the above publication.

Data can be found in the folder data/equationparsebrat/.

Instructions to run the code :

You will need to have maven installed in your system. To download the 
dependencies, run

    mvn dependency:copy-dependencies
        
Next compile using : 
    
    mvn compile     

Finally here are the options:

1. sh run.sh Numoccur : Predicts quantity trigger list. Output file log/Numoccur.out

2. sh run.sh Var : Predicts variable trigger list. Output file log/Var.out

3. sh run.sh Tree : Predicts equation tree. Output file log/Tree.out

4. sh run.sh Pipeline : Runs the first three classifiers in a pipeline, and predicts complete equation parse. Output file log/Pipeline.out. Assumes steps 1-3 have been already run.

5. sh run.sh : Runs the pipeline method end to end, basically runs steps 1 to 4.

6. sh run.sh Joint : Runs the joint model to predict complete equation parse. Output file : log/Joint.out


Please send any suggestions, comments, issues to sroy9@illinois.edu.





