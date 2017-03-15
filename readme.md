# Waste Disposal Agent Simulation

This repository contains three iterations of an agent designed to maximise the amount of waste disposed by a tanker in a set time limit. Each iteration of the agent has two branches:

+ **branch-16px**: A version with a modified framework allowing for 16px tilesets and correcting linter warnings. Designed for use on 1080p monitors.
+ **branch**: A version with the original framework but otherwise identical functionality.

The iterations are as follows:

+ **nearest and nearest-16px**: The agent attempts to find an optimal route to gather waste while remaining refuelled. If no Tasks are within range, the Tanker will move randomly away from the nearest Pump.
+ **search and search-16px**: The agent acts similarly to **nearest**, but will attempt to move the Tanker to specific positions around previously discovered Pumps before wandering.
+ **map and map-16px**: The agent again acts similarly to **nearest**, but maintains a an undirected graph of Pumps. When the closest discovered Task is out of immediate range, the agent searches for a path between Pumps that will position the Tanker within range.

Copyright (c) 2005 Neil Madden, 2009-2011 Julian Zappala, 2017 Jayme Green
See the file "license.terms" for BSD-style license.
The images used in the demo are public domain, from
http://www.openclipart.org/
