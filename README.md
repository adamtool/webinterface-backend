The Back End of the Web Interface
=================================
A framework containing all necessary dependencies and the API for integrating Adam with its model checking and synthesizing algorithms into the web interface.
  
Integration:
------------
This module can be used as separate library and
- is integrated in: [webinterface](https://github.com/adamtool/webinterface),
- contains the packages: webinterface-backend,
- depends on the repos: [libs](https://github.com/adamtool/libs), [framework](https://github.com/adamtool/framework), [logics](https://github.com/adamtool/logics), [modelchecker](https://github.com/adamtool/modelchecker), [synthesizer](https://github.com/adamtool/synthesizer).

------------------------------------

How To Build
------------
A __Makefile__ is located in the main folder.
First, pull a local copy of the dependencies with
```
make pull_dependencies
```
then build the whole framework with all the dependencies with
```
make
```
To build a single dependencies separately, use, e.g,
```
make tools
```
To delete the build files and clean-up
```
make clean
```
To also delete the files generated by the test and all temporary files use
```
make clean-all
```

Tests
-----
You can run all tests for the module by just typing
```
ant test
```
For testing a specific class use for example
```
ant test-class -Dclass.name=uniolunisaar.adam.tests.webbackend.TestStepwiseGraphBuilder
```
and for testing a specific method use for example
```
ant test-method -Dclass.name=uniolunisaar.adam.tests.webbackend.TestingMCFormulaCreation -Dmethod.name=testMCFormulaCreation
```
