package tech.read_only.secretsanta;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toList;

public class SecretSanta {
    private static final String USAGE =
            "Usage: java -jar secretsanta.jar [names.txt] <exclusions.txt> <lastyear.txt>\n\n" +
            "Example names.txt:\n\n" +
            "Alice\n" +
            "Bob\n" +
            "Charles\n" +
            "Daisy\n\n" +
            "Example exclusions.txt where Alice cannot buy for Charles and vice-versa,\n" +
            "and Charles also cannot buy for Daisy:\n\n" +
            "2\n" +
            "Alice\n" +
            "1\n" +
            "Charles\n" +
            "Charles\n" +
            "2\n" +
            "Alice\n" +
            "Daisy\n\n" +
            "Example lastyear.txt, matching the output of last year's run, which is used as\n" +
            "additional exclusions:\n\n" +
            "Alice buys for Charles\n" +
            "Charles buys for Daisy\n" +
            "Daisy buys for Alice\n\n" +
            "Note that not all names need to be included in the latter two files.";

    public static void main(String[] args)
    {
        try {
            secretSanta(args);
        }
        catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    private static void secretSanta(String[] args)
    {
        if (args.length < 1 || args.length > 4) {
            throw new IllegalArgumentException(USAGE);
        }

        LinkedHashSet<String> names;
        try (Stream<String> input = Files.lines(Paths.get(args[0]))) {
            names = input.collect(toCollection(LinkedHashSet::new));
            if (names.size() < 3) {
                throw new IllegalArgumentException("Need at least 3 names in " + args[0]);
            }
        }
        catch (IOException e) {
            throw new IllegalStateException("Could not read " + args[0] + ".\n\n" + USAGE);
        }

        SetMultimap<String, String> excludedGifteesByGiver = HashMultimap.create();
        if (args.length > 1) {
            getExclusions(args[1], names, excludedGifteesByGiver);
        }
        if (args.length > 2) {
            getLastYear(args[2], names, excludedGifteesByGiver);
        }

        calculate(new ArrayList<>(names), excludedGifteesByGiver);
    }

    private static void getExclusions(String fileName, Set<String> names,
                                      SetMultimap<String, String> excludedGifteesByGiver)
    {
        try (Stream<String> streamInput = Files.lines(Paths.get(fileName))) {
            List<String> input = streamInput.collect(toList());
            if (input.size() < 4) {
                throw new IllegalArgumentException(fileName + " was empty or improperly formatted.\n\n" + USAGE);
            }

            int numEntries = Integer.valueOf(input.get(0));
            int lineNum = 1;
            for (int i = 0; i < numEntries; i++) {
                String name = input.get(lineNum++);
                if (!names.contains(name)) {
                    throw new IllegalArgumentException(fileName + " contains an unknown name.");
                }

                int numExclusions = Integer.valueOf(input.get(lineNum++));
                for (int j = 0; j < numExclusions; j++) {
                    String excluded = input.get(lineNum++);
                    if (!names.contains(name)) {
                        throw new IllegalArgumentException(fileName + " contains an unknown name.");
                    }
                    excludedGifteesByGiver.put(name, excluded);
                }
            }
        }
        catch (IOException e) {
            throw new IllegalStateException("Could not read " + fileName);
        }
        catch (NumberFormatException e) {
            throw new IllegalArgumentException(fileName + " was empty or improperly formatted.\n\n" + USAGE);
        }
    }

    private static void getLastYear(String fileName, Set<String> names,
                                    SetMultimap<String, String> excludedGifteesByGiver)
    {
        try (Stream<String> streamInput = Files.lines(Paths.get(fileName))) {
            List<String> input = streamInput.collect(toList());
            if (input.isEmpty()) {
                System.err.println(fileName + " was empty.\n\n" + USAGE);
                return;
            }

            for (String line: input) {
                String[] pieces = line.split(" buys for ");
                if (pieces.length != 2) {
                    throw new IllegalArgumentException(fileName + " was improperly formatted.\n\n" + USAGE);
                }
                if (!names.contains(pieces[0]) || !names.contains(pieces[1])) {
                    throw new IllegalArgumentException(fileName + " contains an unknown name.");
                }
                excludedGifteesByGiver.put(pieces[0], pieces[1]);
            }
        }
        catch (IOException e) {
            throw new IllegalStateException("Could not read " + fileName);
        }
    }

    private static void calculate(List<String> givers, SetMultimap<String, String> excludedGifteesByGiver)
    {
        List<String> giftees;

        int i = 0;
        while (true) {
            if (i++ >= 1000) {
                throw new IllegalStateException("Could not find a valid arrangement (too many exclusions)");
            }

            giftees = new ArrayList<>(givers);
            Collections.shuffle(giftees);

            boolean invalid = false;
            for (int j = 0; j < givers.size(); j++) {
                String giver = givers.get(j);
                String giftee = giftees.get(j);
                if (giver.equals(giftee) || excludedGifteesByGiver.get(giver).contains(giftee)) {
                    invalid = true;
                    break;
                }
            }

            if (!invalid) {
                break;
            }
        }

        for (i = 0; i < givers.size(); i++) {
            System.out.println(givers.get(i) + " buys for " + giftees.get(i));
        }
    }
}
