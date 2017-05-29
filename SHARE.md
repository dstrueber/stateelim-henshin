# SHARE image

## Availablility 
The [SHARE image](http://is.ieis.tue.nl/staff/pvgorp/share/?page=ConfigureNewSession&vdi=XP-TUe_TTC16_NMF_mrttc16_stateelim-henshin-ttc16.vdi) is available under the bundle name **XP-TUe_TTC16_NMF_mrttc16_stateelim-henshin-ttc16.vdi**.

By the way, sorry for the confusing name -- there doesn't see to be a function for changing it.

## Using the SHARE image

After opening the image, you will find an instructive readme file on the desktop. For your convenience, you can read the contents of this readme file below as well.

## Instructions 

How to work with this solution:
 
### 1. Running the evaluation framework
 
- If it's not open already, start ''Solution (Eclipse)'' from the desktop
- Use the Package Explorer (left-hand view in Eclipse) to go to  
    uko.rgse.ttc.stateelim.henshinsolution
     -> src
     -> test.henshinsolution
     -> HenshinRunner.java
- To see what this runner class does, follow the contents
  of the main method (hopefully self-explanatory)
- Trigger the execution by right-clicking on the class
  in the Package Explorer and selecting Run As -> Java Application
- Evaluation results are written to the following folder:
  uko.rgse.ttc.stateelim.henshinsolution/testresult
- The first result file should be created immediately. If it does not
  show up, press F5 in the package explorer to refresh.
 
### 2. Changing the configuration of the benchmark
 
- There is one configuration parameter: the timeout duration.
  To change it, use the Package Explorer go to  
    uko.rgse.ttc.stateelim.henshinsolution
     -> test
     -> TestFramework.java
- Here you can set the TIMEOUT_DURATION parameter to a different
  value.
- Afterwards, rerun the benchmark framework (-> step 1)
 
### 3. Inspecting the solution transformation
 
- Use the Package Explorer to go to  
    uko.rgse.ttc.stateelim.henshinsolution
     -> transformation
- Inspect the henshin_diagram files. There is one of them for each
  task.
