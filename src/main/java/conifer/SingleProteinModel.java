package conifer;

import java.io.File;
import java.util.Random;

import org.apache.commons.io.FileUtils;

import com.sun.media.jai.codecimpl.util.ImagingException;

import bayonet.distributions.Normal.MeanVarianceParameterization;
import blang.ForwardSampler;
import blang.MCMCAlgorithm;
import blang.MCMCFactory;
import blang.MCMCRunner;
import blang.ProbabilityModel;
import blang.annotations.DefineFactor;
import blang.factors.IIDRealVectorGenerativeFactor;
import blang.processing.Processor;
import briefj.opt.Option;
import briefj.opt.OptionsParser;
import briefj.opt.OptionSet;
import conifer.ctmc.expfam.ExpFamMixture;
import conifer.ctmc.expfam.RateMtxNames;
import conifer.factors.UnrootedTreeLikelihood;
import conifer.models.MultiCategorySubstitutionModel;
import conifer.moves.PhyloHMCMove;
import briefj.run.Mains;
import briefj.run.Results;
import blang.processing.ProcessorContext;
import briefj.OutputManager;

import java.io.IOException;



public class SingleProteinModel implements Runnable, Processor
{
   @Option(gloss="File of provided alignment")
   public File inputFile;

   @Option(gloss="File of the tree topology")
   public File treeFile;
   
   @Option(gloss="Indicator of whether to exclude HMC move")
   public boolean isExcluded;
   
   @Option(gloss="band width of weight in MCMC")
   public double bandWidth;
   
   @Option()
   public static RateMtxNames selectedRateMtx = RateMtxNames.POLARITYSIZEGTR;

   @OptionSet(name = "factory")
   public final MCMCFactory factory = new MCMCFactory();
  
  
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
    factory.addProcessor(this);
    if(isExcluded)
    {
      factory.excludeNodeMove(PhyloHMCMove.class);
    }
    model = new Model();
    MCMCAlgorithm mcmc = factory.build(model, false);
    mcmc.options.nMCMCSweeps = 100; 
    mcmc.options.burnIn = (int) Math.round(.1 * factory.mcmcOptions.nMCMCSweeps);
    mcmc.run();
    File newDirectory = new File(Results.getResultFolder().getParent() + "isExcludedHMCMove" + isExcluded + bandWidth+selectedRateMtx+Results.getResultFolder().getName() + "." + System.currentTimeMillis());
    newDirectory.mkdir();
    try
    {
      FileUtils.copyDirectory(Results.getResultFolder(), newDirectory);
    } catch (IOException e)
    {
      // TODO Auto-generated catch block
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

}
