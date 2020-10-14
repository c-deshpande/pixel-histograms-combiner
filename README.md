# pixel-histograms-combiner
Generate Histogram for each RGB color in the given dataset using a Combiner and in-mapper combining.

Project done as a part of CSE-6331 Cloud Computing Course at UTA.

<a href="https://lambda.uta.edu/cse6331/spring20/project2.html">Project Description</a>

<p>In this project, you are asked to improve the performance of your code developed in Project 1 in two different ways: 1) using a combiner and 2) using in-mapper combining.</p>

<p>Note: The hash table in your second Map-Reduce job must have at most 3*256=768 entries. If you get more, it means that either your Color hashCode() method is wrong (it should return the same int for the same Color) or your Color compareTo method is wrong (it should rteturn 0 for two equal Colors).</p>
