# tetrad-fx

We're doing some doodlings here to try to make an alternative, lightweight [Tetrad](https://github.com/cmu-phil/tetrad) UI using JavaFX. The idea is to use the Tetrad library (and continue to refine and develop it without giving up the Swing app) but present that material in a perhaps different interface with a new look and feel, taking advantage of what's available in JavaFX (which is quite a lot and is strongly supported by the community).

One idea we had, which we're currently pursuing, is to shift the focus of Tetrad away from simulation and more toward analysis of particular datasets. We find that users are often perplexed by the full-blown modular interface of the Swing app and are often not particularly interested in doing simulation or don't see the point of it. But Tetrad is a very general tool; it's possible to make it dataset-oriented, with simulation as just one possible tool among many for understanding a particular dataset. It's mostly straightforward to make an interface like this in JavaFX, especially since most of the "guts" of the project are already implemented in the Tetrad library. We just need to implement the relevant components in JavaFX and organize them in a different way. So we're trying that.

Summary of what we have so far:
* A data viewer and a graph viewer.
* A sketch of one possible interface idea. Not committed to this by any stretch of the imagination, nor is it even complete, just playing around with what FX and Java's object orientation can do. Some underlying Tetrad library code is being adjusted to suit. This is very early. But a lot of the menu items do something at least, even if they're not all implemented. But honestly, we may do it a different way entirely, who knows?
* We had the idea of including some causal search "games" for people to play to familiarize themselves with the theory and demystify what's going on. We think this would be a contribution to the community. In the interface, we describe some such games we'd like to implement and include. If these go well, we may add more. The games we have in mind for starters are:
    1. A game to help the user practice making d-separation judgments.
    2. A game to let the user explore the PC algorithm and constraint-based search.
    3. A game to let the user explore permutation search.  
