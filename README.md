# secretsanta

A simple program to help with "Secret Santa" gift-giving assignments.

## Usage

```sh
java -jar secretsanta.jar [names.txt] <exclusions.txt> <lastyear.txt>
```

Example names.txt:

```
Alice
Bob
Charles
Daisy
```

Example exclusions.txt where Alice cannot buy for Charles and vice-versa,
and Charles also cannot buy for Daisy:

```
Alice
1
Charles
Charles
2
Alice
Daisy
```

Example lastyear.txt, matching the output of last year's run, which is used as
additional exclusions:

```
Alice buys for Charles
Charles buys for Daisy
Daisy buys for Alice
```

Note that not all names need to be included in the latter two files.

Example output:

```
Alice buys for Daisy
Bob buys for Alice
Charles buys for Bob
Daisy buys for Charles
```