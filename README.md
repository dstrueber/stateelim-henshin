# Henshin solution to the State Elimination Case of TTC2017

A solution for a [case](https://github.com/sinemgetir/state-elimination-mt) in the [Transformation Tool Contest 2017](http://www.transformation-tool-contest.eu/).

## How to use this repository? ##

### Run our solution ###

Set-up

* As a prerequisite, you will need to have the Java 8 SDK installed on your system.
* Download and install the [Eclipse Modeling Tools, Neon 3](https://www.eclipse.org/downloads/packages/eclipse-modeling-tools/neon3) distribution. We do not claim any support for earlier Eclipse versions.
* In Eclipse, install the Henshin plugin.
    * *Do Help -> Install New Software...*
    * Under *Work with...* enter the Nightly update site: http://download.eclipse.org/modeling/emft/henshin/updates/nightly
    * After the installation, restart Eclipse.
* Use the Git perspective to duplicate this repository to your local system, and to import the two contained projects into your Eclipse workspace. The projects should compile automatically without errors.

Usage

* To reproduce our experiments use the class *HenshinRunner.java*  in the  package *test.henshin*  of the project *uko.rgse.ttc.stateelim.henshinsolution*. Right-click on the class and select *Run as -> Java Application*.
* The test results are written to the folder *testresult* of the same project.
* The timeout duration can be set in the class *TestFramework.java* in the package *test*.

### View and edit the transformations ###

* To view and edit the transformations, please open the *.henshin* and *.henshin_diagram* files contained in the folder *transformations*  of the project *uko.rgse.ttc.stateelim.henshinsolution*.
