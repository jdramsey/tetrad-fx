# tetrad-fx

We're doing some doodlings here to try to make an alternative, 
lightweight [Tetrad](https://github.com/cmu-phil/tetrad) UI using JavaFX. 
The idea is to use the Tetrad library (and continue to refine and 
develop it without giving up the Swing app) but present that material 
in a different interface with a new look and feel, taking advantage 
of what's available in JavaFX.

A first draft is done for this, a few days ahead of schedule, so as
tiem permits for the next few days I will be going through and making
things smoother and more robust. Then I will send it out for review
to the Tetrad team and we will decide whether to continue with this
approach or not. Comments are welcome.

The idea we have bene pursuiing is to shift the focus of
Tetrad away from simulation studies and more toward analysis of particular 
datasets,  so what we're doing at the moment is making an app that allows 
you to load a dataset, transform the data in various ways, do a search on
data yielding a graph, and transform that graph in various ways.

We are keeping a running overview of the work here:

https://github.com/cmu-phil/tetrad-fx/blob/main/src/main/java/io/github/cmuphil/tetradfx/ui/ReadMe.md


