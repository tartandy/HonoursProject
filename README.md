# HonoursProject
Just a dump of my honours project from university. Uses OpenStreetMap data to pre-process and parse a road map to more easily calculate the shortest path using Dijkstra's algorithm.

It definitely gets messy in here at times, but the purpose of the project was to create an effective pre-processing algorithm, not make the project as a whole well polished.

Please note that I used a DOM XML parser to pre-process **because I'm an idiot**, and this project project could desprately do with being redone with a SAX parser that doesn't try and consume the whole file to memory before processing it. I didn't realise the error of my ways until quite late into development, and didn't have time to change it. At least I know now how bad DOM parsers can be of data at this scale...
