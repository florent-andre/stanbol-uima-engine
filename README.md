
- sometimes, when install a new bundle, the engine complains about "Class not found". Source of this not well identified for now. A restart of the stanbol server solve this isssue.



- les problème de classloading et discovering sont du au fait que les bundles exportent les même packages et que le bundle qui fait l'import ne peux en sélectionner qu'un.
-- une des solutions serait de "netoyer" l'ensemble des bundles en commencant par l'uima.utils (clerezza) puis les autres...
-- il faut les sortir pour en faire des bundles indépendants.

- the uima-utils package is the main responsible for locate class and resource loading.


