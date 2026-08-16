<img width="1280" height="400" alt="The Pokémon Politoed enjoying its time in the sun!" src="https://github.com/user-attachments/assets/82363f8f-7b68-42d6-b5c5-7671ee554a95" />
<h1 align="center">Hoppers</h1>

<div align="center">
<blockquote>
Survey the pond, then jump frogs until only<br>
ONE is left standing...Start simple and grow your<br>
skills with each level. In no time at all you’ll be<br> 
the smartest frog in the pond!<br>
&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;-- Official Hoppers Manual<br>
</blockquote>  
</div>
<h3 align="center">You stumble upon a pond chock-full of Politoeds... Can you catch them all?</h2>
<p>Hoppers is a logic puzzle—to be more specific, a single-player peg-solitaire game—produced by the popular toy and board game company ThinkFun but invented by Nob Yoshigahara. However, in this iteration, you'll notice that the frog leaping isn't your typical amphibian—it's Politoed, the Frog <i>Pokémon</i>!</p>

## Overview
The primary components that build the backbone of this Java and JavaFX project are threefold:
- The `solver`*, which intakes a configuration of the Hoppers game to find the shortest path to a given solution, implementing breadth-first search coupled with backtracking<br>
<sub>*To adopt this algorithm for your own use, visit its repository [here](https://github.com/TORITZA/solver).</sub>

- `HoppersPTUI`, encompassing the text-based user interface, allowing play directly from the developer console
  
- `HoppersGUI`, encapsulating the graphical user interface, which replicates a pond populated with the Pokémon Politoed in lieu of the usual frogs!
<div align="center">
  <img width="900" height="477" alt="Hoppers MVC" src="https://github.com/user-attachments/assets/02d40458-7ce8-4371-9185-b317eab17186"/>
</div>

Both of the aforementioned classes incorporate the model-view-controller architecture, relying on `HoppersModel` to store the internal logic of their respective games.

Default Hoppers puzzles are provided in the `data/default`. Included in this directory is also a subfolder ('data/custom`) that contains any custom puzzles the user may create. 


Further documentation is detailed in the source code for not only all the classes listed but also any providers or supplementary programs. 

## Features
This section aims to cover the core features included with either iteration of the Hoppers puzzle game: `HoppersPTUI` or `HoppersGUI`. 

- <b>Assist Mechanism</b>: Get stuck? The <i>Hint</i> command uses the `solver` algorithm to find the next best step forward given the current state of the Hoppers puzzle. Should the puzzle in its present configuration be insoluble, the app will output as such.
- <b>File Importing & Parsing</b>: Aptly named, the <i>Load</i> action constructs a puzzle representation of the Hoppers data file provided.
- <b>Issue Alert System</b>: Ran into a bug? Send in a trouble ticket straight from the menu!
- <b>Creation Mode</b>: The pond is your oyster in <i>Creation Mode</i>! Create Hoppers puzzles uninhibited, whether to share with others or to solve yourself.

While their implementations of these features are similar, both UIs interact and display the internal model in different ways.*



<h4>HoppersPTUI</h4>
For the interface to work properly, a Hoppers puzzle file must be inserted through the application's CLI arguments. The features enumerated above can then be accessed via the PTUI's starting menu:<br>
<div>
  <img width="493" height="196" alt="PTUI menu view in dev console" src="https://github.com/user-attachments/assets/0716b91b-34a8-484d-a296-0ff53e30f4a8" />
</div>
<sup>The PTUI menu view as shown in the developer console</sup>
<br>
Now, the user may navigate the app's primary features from the comfort of their command-line! 
<div>
  <img width="273" height="248" alt="user-inputted command" src="https://github.com/user-attachments/assets/fcc846cc-cdd1-4718-a1b6-3520b2a4975a" />
</div>
<sup>From the user's end, selecting the Red Frog located at (2,2) on the game board to then be moved</sup>


<h4>HoppersGUI</h4>
Similarly, the GUI also requires the insertion of a puzzle file upon start-up. 

<sub>*Specific instructions on how to play Hoppers, how to operate the app's controls, and how to navigate Creation Mode can be found in `HoppersGUI`'s <b>Help</b> menu.</sub>

## Version Changelog
- `1.0.0` - 
- `1.1.0` - 

## Credits & Licensing 
