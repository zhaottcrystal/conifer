<!-- File generated by tutorialj -->
# conifer [![Build Status](https://travis-ci.org/alexandrebouchard/conifer.png?branch=master)](https://travis-ci.org/alexandrebouchard/conifer)


-------

**conifer** contains utilities for doing phylogenetic inference.


Installation
------------

Prerequisite software:

- Java SDK 1.6+
- Gradle version 1.9+ (not tested on Gradle 2.0)

There are several options available to install the package:

### Use in eclipse

- Check out the source ``git clone git@github.com:zhaottcrystal/conifer.git``
- Type ``gradle eclipse`` from the root of the repository
- From eclipse:
- ``Import`` in ``File`` menu
- ``Import existing projects into workspace``
- Select the root
- Deselect ``Copy projects into workspace`` to avoid having duplicates


### Use in IntelliJ IDEA 14 CE
- Check out the source ``git clone git@github.com:zhaottcrystal/conifer.git``
- clone it to a local folder on the computer
- From IntelliJ:
  - "Import Project"
  - Select the folder where you clone the git repository
  - Select "Import project from external model" and select "Gradle"
  - Gradle will help build up the project automatically

Phylogenetic inference using conifer
-----------------------------

Let's first look at  **conifer**, a framework where you can do phylogenetic inference with amino acids sequences or DNA sequences.  This framework has incoporated several popular models such as K80 for DNA data or GTR models for both DNA and amino acid sequences. However, it is also flexible for you to customize your own selected features to define the rate matrix in the continuous time Markov chains (CTMCs) framework. Moreover, it is general to incorporate new data types where your data can take more than one character to denote a state in the CTMCs. This is particular useful when modelling condon evolution or the co-evolution of groups of interacting amino acid residues. 

### Modelling language
Our framework can incorporate as special cases most existing DNA, amino acid and codon evolution models. For simplicity, we take the HKY85 model (Hasegawa et al., 1985) as an example to show how to represent a previously constructed rate matrix using our framework. We start by showing mathematically how the classical HKY85 model is translated into features and weights in our language. We then show concretely how it is input into our software implementation using a small, JSON-based modelling language. 


***(This page will be updated soon to explain our model language)***

The classic representation of HKY85 model is 

![HKY85](https://github.com/zhaottcrystal/conifer/blob/master/EquationImages/HKY85.jpg)where ![beta](https://github.com/zhaottcrystal/conifer/blob/master/EquationImages/beta.jpg) to ensure  that the expected base change per unit time is one, and the diagonal elements, \``*", enforce that each row sums to zero. The A and G nucleotides contain bases that belong to a chemical group known as purines, while the C and T nucleotides contain bases that belong to a chemical group known as pyrimidines. Importantly, substitutions are more frequent between members of the same chemical group, motivating the addition of an extra parameter, ![kappa](https://github.com/zhaottcrystal/conifer/blob/master/EquationImages/kappa.jpg), to differentiate these intragroup substitutions (transitions) from intergroup substitutions (transversions).

To encode this under our Bayesian rate matrix GLMs, we set ![basetheta](https://github.com/zhaottcrystal/conifer/blob/master/EquationImages/basetheta.jpg) and ![basepi](https://github.com/zhaottcrystal/conifer/blob/master/EquationImages/basepi.jpg), allowing us to simplify our model into:

![model](https://github.com/zhaottcrystal/conifer/blob/master/EquationImages/model.jpg)

We have ![omega](https://github.com/zhaottcrystal/conifer/blob/master/EquationImages/omega.jpg), as there are four univariate features used to calculate the stationary distribution for the four states A, C, G, T and one bivariate feature to differentiate transitions from transversions. Univariate features are determined by only one state in the state space ![chi](https://github.com/zhaottcrystal/conifer/blob/master/EquationImages/chi.jpg), while bivariate features are characterized by an unordered pair of  states. We represent the weights as a vector ![omegaHKY](https://github.com/zhaottcrystal/conifer/blob/master/EquationImages/omegaHKY.jpg). The sufficient statistic of the ![pi](https://github.com/zhaottcrystal/conifer/blob/master/EquationImages/pi.jpg)-exponential family for x=A is ![psiA](https://github.com/zhaottcrystal/conifer/blob/master/EquationImages/psiA.jpg), while for x=C, it is ![psiC](https://github.com/zhaottcrystal/conifer/blob/master/EquationImages/psiC.jpg). The normalization used to calculate the stationary distribution is given by:


![fullPage](https://github.com/zhaottcrystal/conifer/blob/master/EquationImages/fullpage.jpg)

![secondPage](https://github.com/zhaottcrystal/conifer/blob/master/EquationImages/secondpage.jpg)

![lastPage](https://github.com/zhaottcrystal/conifer/blob/master/EquationImages/lastpage.jpg)


### Example: 
We provide an example ``SingleProteinModel.java`` using amino acid sequences to estimate the rate matrix and tree topology to demonstrate the use of our software.  
We paste this code at the end of this tutorial. 

First assume our tree topology is fixed and we target at obtaining a good estimate of the rate matrix.  We first explain the options provided to this program and then explain how to run this program in command line.  The ``inputFile`` and ``treeFile`` specifies the path of the alignment file and the tree topology file.  We can set ``isExcluded`` to false to perform an adaptive HMC within the MCMC techniques.  It means that within each MCMC iteration, we will run several leaf frog steps of HMC which aims at exploring the parameter space more efficiently.  The two following options of  ``epsilon`` and ``L`` are the step-size and number of leap frog jumps in HMC. For simplicity, the user can ignore setting up these two paramters since the program will automatically tune good values of  ``epsilon`` and  ``L`` by default to make the algorithm efficient.  ``nMCMCIterations`` defines the total number of MCMC iterations. The default burnin period is 10 percent of  ``nMCMCIterations``.  The important option is ``RateMatrixNames`` class. This is an enumeration class and it provides several rate matrix structures to perform phylogenetic inference. In the code, we select ``PROTEINSIMPLEGTR`` representing the general time reversible (GTR) model for amino acid sequences. Later in section 
   ``Extend conifer`` we will provide instructions on how to customize user's' features to define new rate matrices.  Right now we fix the tree topology to update the rate matrix. However, if the user would like to infer about the tree topology, just uncomment the block of code that we comment in ``SingleProteinModel.java`` right now. In that block of codes, we put a nonclock tree prior to the topology and the hyper prior for the branch length is an exponential distribution.  It is worth noting that the tree topology file must be an unrooted tree. 

To run it, you can do it either within eclipse or IntelliJ or in command line arguments.

You can find the classpath by first running it in Eclipse, and then debug it, right click ``SingleProteinModel.java`` and select ``Properties`` and find the classpath information 
from the ``Command Line`` option. One example command line arguments is given as below

`` /path/to/java -Xmx2g -classpath /path/to/jarfiles conifer.SingleProteinModel  
-inputFile /path/to/alignment.txt  -treeFile /path/to/tree.nwk.txt
-selectedRateMtx PROTEINSIMPLEGTR  -factory.mcmc.burnIn 10000
``
### Extend conifer

This part we mainly illustrate how to extend  ``conifer`` if the user would like to define their own data types where the number of characters to denote a state in CTMCs is not necessary to be one and their own rate matrices based on self-defined physicochemical properties. 

**User-customized Data Types**

We explain how to use user-customized new data types first. Assume we are interested in modelling the co-evolution of groups of interacting amino acid residues. In this case, one state in the CTMCs is denoted as a combination of two amino acid characters. To achieve this,  you can look at the class ``PhylogeneticObservationFactory.java`` and we implement this in the following block of codes.  We need to create an encoding file which is in ``proteinPair-iupac-encoding.txt``.

```java
public static PhylogeneticObservationFactory proteinPairFactory()
{
if(_proteinPairFactory == null)
_proteinPairFactory = fromResource("/conifer/io/proteinPair-iupac-encoding.txt");
return _proteinPairFactory;
}
```



**User-customized Rate Matrices**
In this section, we explain how to define own rate matrices based on user-customized physicochemical features. We illustrate how to define a ``POLARITYSIZE``  rate matrix based on the polarity and size features of amino acids. A detailed description the ``POLARITYSIZE`` model can be found in our paper "to add a link once the paper is submitted".   In ``RateMtxNames.java``, this enumeration class, we have created a ``POLOARITYSIZE``  case,  then in ``SerializedExpFamMixture`` class, we need to create a ``polaritySize()`` method.  To implement this method, all we need to do is to create a sample file ``/conifer/ctmc/expfam/polaritySizeGTR-expfam.txt`` which specifies the univarate feature and bivariate feature to define our rate matrix. Please refer to our paper about how to calculate the rate matrix within the generalized linear model framework based on univariate and bivariate features. One last thing is that we need to create a sample rate matrix defined under ``POLARITYSIZE`` model assumption under any chosen weights.  This serves as an initial input for the rate matrix. Please refer to ``RateMatrices.java`` to see how ``polairtySize()`` method is implemented. This method is called in ``UnrootedTreeLikelihood.java`` and the sample rate matrix serves as ``baseRateMatrix``. 




```java
public class SingleProteinModel implements Runnable, Processor
{
@Option(gloss="File of provided alignment")
public File inputFile;


@Option(gloss="File of the tree topology")
public File treeFile;

@Option(gloss="Indicator of whether to exclude HMC move")
public boolean isExcluded=false;

@Option(gloss="Number of MCMC iterations")
public int nMCMCIterations = 100000;

@Option(gloss="ESS Experiment Number")
public int rep = 1;

@Option(gloss="Rate Matrix Method")
public RateMtxNames selectedRateMtx = RateMtxNames.PROTEINSIMPLEGTR;

@OptionSet(name = "factory")
public final MCMCFactory factory = new MCMCFactory();

@Option(gloss="Bandwidth of proposal for vectors in MCMC")
public double bandwidth = 0.01;

@Option(gloss="Epsilon provided ")
public static Double epsilon = null;

@Option(gloss="L provided")
public static Integer L = null;

@Option(gloss="provided size of adaptation")
public static Integer sizeAdapt = 500;

public class Model
{
@DefineFactor(onObservations = true)
public final UnrootedTreeLikelihood<MultiCategorySubstitutionModel<ExpFamMixture>> likelihood1 = 
UnrootedTreeLikelihood
.fromFastaFile(inputFile, selectedRateMtx)
.withExpFamMixture(ExpFamMixture.rateMtxModel(selectedRateMtx))
.withTree(treeFile);

@DefineFactor
public final IIDRealVectorGenerativeFactor<MeanVarianceParameterization> prior =
IIDRealVectorGenerativeFactor
.iidNormalOn(likelihood1.evolutionaryModel.rateMatrixMixture.parameters);
}

private final PrintWriter detailWriter = BriefIO.output(Results.getFileInResultFolder("experiment.details.txt"));

// The topology of the tree is fixed so that I don't put a prior on the tree topology
// @DefineFactor
// NonClockTreePrior<RateParameterization> treePrior1 = 
//  NonClockTreePrior
// .on(likelihood1.tree);


//  @DefineFactor
//  Exponential<Exponential.MeanParameterization> branchLengthHyperPrior = 
//    Exponential
//    .on(treePrior.branchDistributionParameters.rate)
//    .withMean(10.0);

// private final PrintWriter treeWriter = BriefIO.output(Results.getFileInResultFolder("tree.nwk"));

// Note: only instantiate this in run to avoid problems with command line argument parsing
public Model model;

public void run()
{
RealVectorMHProposal.bandWidth = bandwidth;
PhyloHMCMove.epsilon=epsilon;
PhyloHMCMove.L = L;
PhyloHMCMove.sizeAdapt=sizeAdapt;
factory.addProcessor(this);
model = new Model();
long startTime = System.currentTimeMillis();
if(isExcluded)
{
factory.excludeNodeMove(PhyloHMCMove.class);
}
MCMCAlgorithm mcmc = factory.build(model, false);
mcmc.options.random = new Random(rep);
mcmc.options.nMCMCSweeps = nMCMCIterations; 
mcmc.options.burnIn = (int) Math.round(.1 * factory.mcmcOptions.nMCMCSweeps);
mcmc.run();
String fileName = inputFile.getName();
FileNameString fileNameString = new FileNameString(fileName);
String numberOfSites = fileNameString.subStringBetween(fileName, "numSites", "Seed");
String whichSeedUsed = fileNameString.subStringBetween(fileName, "Seed", ".txt");
logToFile("Total time in minutes: " + ((System.currentTimeMillis() - startTime)/60000.0));
File newDirectory = new File(Results.getResultFolder().getParent() + "rep"+ rep+ "isExcludedHMCMove" + isExcluded + bandwidth+selectedRateMtx++"epsilon"+PhyloHMCMove.epsilon+"L"+PhyloHMCMove.L+"Adapt"+ PhyloHMCMove.sizeAdapt);
newDirectory.mkdir();
try
{
FileUtils.copyDirectory(Results.getResultFolder(), newDirectory);
} catch (IOException e)
{
e.printStackTrace();
}

}

public static void main(String [] args) 
{
Mains.instrumentedRun(args, new SingleProteinModel());
}

@Override
public void process(ProcessorContext context)
{

}
public void logToFile(String someline) {
this.detailWriter.println(someline);
this.detailWriter.flush();
}


}


```


