
To build xercesMinimal.jar for this project, follow these steps:

1. Check out the xerces2-j project from
  https://svn.apache.org/repos/asf/xerces/java/tags/Xerces-J_2_9_1/
You may need to add tools/resolver.jar and tools/xml-apis.jar to project
classpath before JRE to make it compile;

2. Apply the patch in this folder to xerces;

3. Build xerces with target 'jar', to get xercesImpl.jar;

4. Copy xercesImpl.jar to nekohtml, under folder lib/xerces-2.9.1/, and rename
it to xercesImpl-2.9.1.jar;

5. Check out the nekohtml project from
  https://nekohtml.svn.sourceforge.net/svnroot/nekohtml/trunk/
We are using Revision 298 here. You may need to add project xerces to the
classpath before JRE to make it compile;

6. Build nekohtml with target 'x-minimal', to get xercesMinimal.jar; you may
need to add lib/bcel-5.2.jar to Ant classpath to make it work;

7. Copy build/lib/xercesMinimal.jar from project nekohtml to this project,
under folder lib/.

Now you should be able to run CybernekoXpathTest.


Yin Qu (yin@ecologylab.net)
Sept 26, 2011

