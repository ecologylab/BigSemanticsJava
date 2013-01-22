git checkout -b importing

git remote add remote_ecologylabSemantics https://github.com/ecologylab/ecologylabSemantics.git
git fetch remote_ecologylabSemantics
git checkout -b branch_ecologylabSemantics remote_ecologylabSemantics/master
git checkout importing
git read-tree --prefix=bigSemanticsCore/ -u branch_ecologylabSemantics
git add bigSemanticsCore
git commit -m "Imported ecologylabSemantics as subproject bigSemanticsCore."

git remote add remote_cyberneko https://github.com/ecologylab/cyberneko.git
git fetch remote_cyberneko
git checkout -b branch_cyberneko remote_cyberneko/master
git checkout importing
git read-tree --prefix=cyberneko/ -u branch_cyberneko
git add cyberneko
git commit -m "Imported cyberneko as subproject cyberneko."

git remote add remote_ecologylabSemanticsCyberneko https://github.com/ecologylab/ecologylabSemanticsCyberneko.git
git fetch remote_ecologylabSemanticsCyberneko
git checkout -b branch_ecologylabSemanticsCyberneko remote_ecologylabSemanticsCyberneko/master
git checkout importing
git read-tree --prefix=bigSemanticsCybernekoWrapper/ -u branch_ecologylabSemanticsCyberneko
git add bigSemanticsCybernekoWrapper
git commit -m "Imported ecologylabSemanticsCyberneko as subproject bigSemanticsCybernekoWrapper."

git remote add remote_metadataExtractor https://github.com/ecologylab/metadataExtractor.git
git fetch remote_metadataExtractor
git checkout -b branch_metadataExtractor remote_metadataExtractor/master
git checkout importing
git read-tree --prefix=imageMetadataExtractor/ -u branch_metadataExtractor
git add imageMetadataExtractor
git commit -m "Imported metadataExtractor as subproject imageMetadataExtractor."

git remote add remote_ecologylabSemanticsSun https://github.com/ecologylab/ecologylabSemanticsSun.git
git fetch remote_ecologylabSemanticsSun
git checkout -b branch_ecologylabSemanticsSun remote_ecologylabSemanticsSun/master
git checkout importing
git read-tree --prefix=bigSemanticsSunSpecifics/ -u branch_ecologylabSemanticsSun
git add bigSemanticsSunSpecifics
git commit -m "Imported ecologylabSemanticsSun as subproject bigSemanticsSunSpecifics."

git remote add remote_ecologylabSemanticsAndroid https://github.com/ecologylab/ecologylabSemanticsAndroid.git
git fetch remote_ecologylabSemanticsAndroid
git checkout -b branch_ecologylabSemanticsAndroid remote_ecologylabSemanticsAndroid/master
git checkout importing
git read-tree --prefix=bigSemanticsAndroidSpecifics/ -u branch_ecologylabSemanticsAndroid
git add bigSemanticsAndroidSpecifics
git commit -m "Imported ecologylabSemanticsAndroid as subproject bigSemanticsAndroidSpecifics."

git remote add remote_ecologylabSemanticsExample https://github.com/ecologylab/ecologylabSemanticsExample.git
git fetch remote_ecologylabSemanticsExample
git checkout -b branch_ecologylabSemanticsExample remote_ecologylabSemanticsExample/master
git checkout importing
git read-tree --prefix=bigSemanticsSDK/ -u branch_ecologylabSemanticsExample
git add bigSemanticsSDK
git commit -m "Imported ecologylabSemanticsExample as subproject bigSemanticsSDK."

