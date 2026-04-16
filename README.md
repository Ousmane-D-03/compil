# SDM to MIPS Compiler

Ce projet implémente un compilateur pour un langage SDM simple. Il couvre l'analyse syntaxique, la construction d'un AST, la vérification sémantique, la génération d'un code intermédiaire, puis l'écriture d'un programme MIPS.

## Structure du projet

- `sdmips/sdm.g4` : grammaire ANTLR du langage SDM, incluant le support des tableaux
- `sdmips/main/Main.java` : point d'entrée principal du compilateur
- `sdmips/ast/` : construction de l'arbre syntaxique abstrait (AST)
- `sdmips/semantic/` : analyse sémantique, tables de symboles et vérification des types
- `sdmips/ir/` : génération du code intermédiaire
- `sdmips/mips/` : génération de code assembleur MIPS
- `sdmips/printers/` : affichage du code AST/IR
- `sdmips/tests/` : exemples de programmes SDM
- `rapport_modifications.tex` : rapport détaillé des modifications apportées

## Prérequis

- Java 8 ou version supérieure
- ANTLR 4.13.2 (`antlr-4.13.2-complete.jar`)

Le script `sdmips/run.sh` suppose que le jar ANTLR est disponible dans `/usr/local/lib/antlr-4.13.2-complete.jar`. Si votre installation est ailleurs, adaptez la variable `path` dans ce script ou précisez le classpath manuellement.

## Compilation

Depuis le dossier `sdmips` :

```bash
cd /home/ousmane/master/compilation/tp6_gen_mips/sdmips
java -jar /usr/local/lib/antlr-4.13.2-complete.jar -package parser -visitor sdm.g4
javac -cp .:/usr/local/lib/antlr-4.13.2-complete.jar -d build $(find . -name "*.java")
```

### Remarque

- Le premier appel ANTLR génère les fichiers du parser dans le package `parser`
- Le deuxième appel compile toutes les sources Java dans le dossier `build`

## Exécution

Le compilateur lit le programme SDM depuis l'entrée standard et écrit un fichier MIPS `.asm`.

```bash
cd /home/ousmane/master/compilation/tp6_gen_mips/sdmips
java -cp build:/usr/local/lib/antlr-4.13.2-complete.jar main.Main output.sdm < tests/test.sdm
```

Le fichier généré sera `output.asm`.

Vous pouvez aussi donner un nom de sortie différent :

```bash
java -cp build:/usr/local/lib/antlr-4.13.2-complete.jar main.Main monprogramme.sdm < tests/test.sdm
```

Cela produit `monprogramme.asm`.

## Nettoyage

Pour supprimer les classes compilées et les fichiers générés par ANTLR :

```bash
cd /home/ousmane/master/compilation/tp6_gen_mips/sdmips
./clean.sh
```

## Tests

Les exemples de tests se trouvent dans `sdmips/tests/` :

- `tests/test.sdm`
- `tests/unit.sdm`
- `tests/funs.sdm`
- `tests/array.sdm` (exemple de tableaux et d'indexation)

Testez-les en redirigeant leur contenu vers `main.Main`.

## Notes

- `main.Main` effectue les phases suivantes :
  1. Analyse syntaxique
  2. Construction de l'AST
  3. Vérification sémantique
  4. Traduction en IR
  5. Génération de code MIPS

- Le compilateur prend maintenant en charge les tableaux SDM :
  - types `int[]` et `boolean[]`
  - allocation avec `new type[expression]`
  - accès par `identificateur[index]`
  - affectation d'éléments de tableau

- Le projet contient aussi un rapport détaillé des modifications sous `rapport_modifications.tex`.
