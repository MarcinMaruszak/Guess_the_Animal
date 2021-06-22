package animals;

import javax.swing.plaf.basic.BasicInternalFrameTitlePane;
import java.text.MessageFormat;
import java.time.LocalTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class UserInterface {
    private final Scanner scanner;
    private final Database database;
    private final Tree tree;
    private ResourceBundle messagesRes;
    private ResourceBundle patternRes;


    public UserInterface(Scanner scanner, String type, String language) {
        this.scanner = scanner;
        this.database = new Database(type);
        tree = new Tree();
        setResources();
    }

    private void setResources() {
        if (Locale.getDefault().getLanguage().equals(new Locale("eo").getLanguage())) {
            messagesRes = ResourceBundle.getBundle("messages_eo");
            patternRes = ResourceBundle.getBundle("patterns_eo");
        } else {
            messagesRes = ResourceBundle.getBundle("messages");
            patternRes = ResourceBundle.getBundle("patterns");
        }

    }

    public void start() {
        printHello();
        tree.setRoot(database.readFromFile());

        if (tree.getRoot() == null) {
            System.out.println(messagesRes.getString("animal.wantLearn"));
            System.out.println(messagesRes.getString("animal.askFavorite"));
            String animal = getAnimal();
            tree.setRoot(new Node(patternRes.getString("statement.0") + " " + animal, null));
        }

        System.out.println(messagesRes.getString("welcome"));

        label:
        while (true) {
            printMenu();
            String input = getInput();

            switch (input) {
                case "1":
                    do {
                        System.out.println(messagesRes.getString("game.think"));
                        System.out.println(messagesRes.getString("game.enter"));
                        getInput();
                        play(tree.getRoot());
                        System.out.println(messagesRes.getString("game.again"));
                    } while (getYesOrNo());
                    break;
                case "2":
                    printAllAnimals();
                    break;
                case "3":
                    searchForAnimalFacts();
                    break;
                case "4":
                    treeStats();
                    break;
                case "5":
                    printTree();
                    break;
                case "0":
                    break label;
                default:
                    System.out.println(MessageFormat.format(
                            messagesRes.getString("menu.property.error"), 5));
            }

        }
        database.saveToFile(tree.getRoot());
        System.out.println(messagesRes.getString("farewell"));
    }


    private void treeStats() {
        List<Integer> depths = tree.getDepths();
        System.out.println(messagesRes.getString("tree.stats.title"));
        System.out.println(MessageFormat.format(messagesRes.getString("tree.stats.root"),
                tree.getRoot().getFact()));
        System.out.println(MessageFormat.format(messagesRes.getString("tree.stats.nodes"),
                tree.getNumberOfNodes()));
        System.out.println(MessageFormat.format(messagesRes.getString("tree.stats.animals"),
                tree.getAllLeaves().size()));
        System.out.println(MessageFormat.format(messagesRes.getString("tree.stats.statements"),
                tree.getStatements().size()));
        System.out.println(MessageFormat.format(messagesRes.getString("tree.stats.height"),
                depths.stream().max(Comparator.naturalOrder()).get()));
        System.out.println(MessageFormat.format(messagesRes.getString("tree.stats.minimum"),
                depths.stream().min(Comparator.naturalOrder()).get()));
        System.out.println(MessageFormat.format(messagesRes.getString("tree.stats.average"),
                depths.stream().collect(Collectors.averagingInt(Integer::intValue))));
        System.out.println();
    }

    private void searchForAnimalFacts() {
        System.out.println(messagesRes.getString("animal.prompt"));
        String animal = getAnimal();
        List<String> facts = tree.getAllAnimalFacts(animal, patternRes);

        if (facts.isEmpty()) {
            System.out.println(MessageFormat.format(messagesRes.getString("tree.search.noFacts"), animal));
        } else {
            System.out.println(MessageFormat.format(messagesRes.getString("tree.search.facts"),
                    animal.replaceFirst("an ", "").replaceFirst("a ", "")));
            for (String fact : facts) {
                System.out.println(fact);
            }
        }
        System.out.println();
    }

    private void printAllAnimals() {
        List<String> animals = tree.getAllLeaves();
        System.out.println(messagesRes.getString("tree.list.animals"));
        for (String s : animals) {
            System.out.println("-" + s.replaceFirst(patternRes.getString("statement.0"), "")
                    .replaceFirst("(a |an )", ""));
        }
        System.out.println();
    }

    private void printMenu() {
        System.out.println(messagesRes.getString("menu.property.title") + "\n\n" +
                "1. " + messagesRes.getString("menu.entry.play") + "\n" +
                "2. " + messagesRes.getString("menu.entry.list") + "\n" +
                "3. " + messagesRes.getString("menu.entry.search") + "\n" +
                "4. " + messagesRes.getString("menu.entry.statistics") + "\n" +
                "5. " + messagesRes.getString("menu.entry.print") + "\n" +
                "0. " + messagesRes.getString("menu.property.exit") + "\n");
    }

    private void play(Node node) {

        if (node == null) {
            return;
        }
        if (node.getLeft() == null && node.getRight() == null) { //no more children, last question
            System.out.println(askQuestion(node.getFact()));
            if (getYesOrNo()) {  //yes
                System.out.println(messagesRes.getString("game.win"));
            } else {
                giveUpAndGetFact(node);
            }
        } else {  //more questions
            System.out.println(askQuestion(node.getFact()));
            if (getYesOrNo()) {  //yes
                play(node.getRight());
            } else {
                play(node.getLeft());
            }
        }
    }

    private void giveUpAndGetFact(Node node) {
        System.out.println(messagesRes.getString("game.giveUp"));
        String animal1 = node.getFact().replaceFirst(patternRes.getString("statement.0"), "").trim();
        String animal2 = getAnimal();
        System.out.println(MessageFormat.format(messagesRes.getString("statement.prompt"), animal1, animal2));
        String fact = getFact();
        System.out.println(MessageFormat.format(messagesRes.getString("game.isCorrect"), animal2));

        node.setFact(fact);

        if (getYesOrNo()) {
            node.setRight(new Node(patternRes.getString("statement.0") + " " + animal2, node));
            node.setLeft(new Node(patternRes.getString("statement.0") + " " + animal1, node));
            animal1 = replaceArticleWithDeterminer(
                    animal1.replaceAll(patternRes.getString("statement.0"), "").trim());
            animal2 = replaceArticleWithDeterminer(animal2);
            printNewLearnedFacts(node, animal2, animal1);
        } else {
            node.setLeft(new Node(patternRes.getString("statement.0") + " " + animal2, node));
            node.setRight(new Node(patternRes.getString("statement.0") + " " + animal1, node));
            animal1 = replaceArticleWithDeterminer(
                    animal1.replaceAll(patternRes.getString("statement.0"), "").trim());
            animal2 = replaceArticleWithDeterminer(animal2);
            printNewLearnedFacts(node, animal1, animal2);
        }
    }

    private String replaceArticleWithDeterminer(String animal) {
        Pattern pattern = Pattern.compile(patternRes.getString("definite.1.pattern"));
        Matcher matcher = pattern.matcher(animal);
        return matcher.replaceAll(patternRes.getString("definite.1.replace"));
    }

    private void printNewLearnedFacts(Node node, String animal1, String animal2) { //anim1 true anim2 false
        System.out.println(messagesRes.getString("game.distinguish"));

        Pattern pattern = Pattern.compile(patternRes.getString("animalFact.1.pattern"));
        Matcher matcher = pattern.matcher(node.getFact());
        if (matcher.find()) {
            System.out.printf(matcher.replaceAll(patternRes.getString("animalFact.1.replace")) + "\n",
                    animal1);
            matcher = pattern.matcher(makeNegative(node.getFact()));
            System.out.printf(matcher.replaceAll(patternRes.getString("animalFact.1.replace")) + "\n",
                    animal2);
        }
    }

    private String makeNegative(String statement) {
        Pattern pattern = Pattern.compile(patternRes.getString("negative.1.pattern"));
        Matcher matcher = pattern.matcher(statement);
        if (matcher.find()) {
            return matcher.replaceAll(patternRes.getString("negative.1.replace"));
        } else {
            pattern = Pattern.compile(patternRes.getString("negative.2.pattern"));
            matcher = pattern.matcher(statement);
            if (matcher.find()) {
                return matcher.replaceAll(patternRes.getString("negative.2.replace"));
            } else {
                pattern = Pattern.compile(patternRes.getString("negative.3.pattern"));
                matcher = pattern.matcher(statement);
                if (matcher.find()) {
                    return matcher.replaceAll(patternRes.getString("negative.3.replace"));
                }
            }
        }
        return "";
    }

    private String getFact() {
        String factString;
        while (true) {
            factString = getInput();
            Pattern pattern = Pattern.compile(patternRes.getString("statement.1.pattern"));
            Matcher matcher = pattern.matcher(factString);
            if (matcher.find()) {
                return matcher.group();
            } else {
                System.out.println(messagesRes.getString("statement.error"));
            }
        }
    }

    private boolean getYesOrNo() {
        String replay;
        Pattern yesPattern = Pattern.compile(patternRes.getString("positiveAnswer.isCorrect"));
        Pattern noPattern = Pattern.compile(patternRes.getString("negativeAnswer.isCorrect"));
        Matcher matcher;
        while (true) {
            replay = getInput().replaceFirst("\\W$", "").trim();
            matcher = yesPattern.matcher(replay);
            if (matcher.find()) {
                return true;
            } else {
                matcher = noPattern.matcher(replay);
                if (matcher.find()) {
                    return false;
                } else {
                    System.out.println(messagesRes.getString("ask.again"));
                }
            }
        }
    }

    private String getAnimal() {
        String animal = getInput();
        Pattern pattern = Pattern.compile(patternRes.getString("animal.1.pattern"));
        Matcher matcher = pattern.matcher(animal);
        if (matcher.find()) {
            if (messagesRes.getString("lng").equals("en")) {
                String article;
                String animal1 = animal.replaceFirst("a ", "")
                        .replaceFirst("an ", "")
                        .replaceFirst("the ", "");
                if (animal.startsWith("a ")) {
                    article = "a ";
                } else if (animal.startsWith("an ")) {
                    article = "an ";
                } else {
                    char c = animal1.toLowerCase(Locale.ROOT).charAt(0);
                    if (c == 'a' || c == 'e' || c == 'i' || c == 'o' || c == 'u') {
                        article = "an ";
                    } else {
                        article = "a ";
                    }
                }
                return article + animal1;

            } else {
                return matcher.group();
            }
        } else {
            System.out.println(messagesRes.getString("animal.error"));
            return getAnimal();
        }
    }

    private String getInput() {
        return scanner.nextLine().toLowerCase(Locale.ROOT);
    }

    private void printHello() {
        LocalTime timeNow = LocalTime.now();

        if (timeNow.isBefore(LocalTime.of(5, 0, 1))
                || timeNow.isAfter(LocalTime.of(18, 0))) {
            System.out.println(messagesRes.getString("greeting.evening"));
        } else if (timeNow.isBefore(LocalTime.of(12, 0, 1))) {
            System.out.println(messagesRes.getString("greeting.morning"));
        } else if (timeNow.isBefore(LocalTime.of(18, 0, 1))) {
            System.out.println(messagesRes.getString("greeting.afternoon"));
        }
        System.out.println();
    }

    public void printTree() {
        printNode(tree.getRoot(), new ArrayDeque<>());
    }


    private void printNode(Node node, Deque<Node> leftNodes) {
        if (node == null) {
            return;
        } else if (node.getParent() == null) {
            System.out.println("└ " + askQuestion(node.getFact())/*.getQuestion()*/);
        } else if (tree.isALeaf(node)) {
            System.out.println("└ " + getAnimalName(node.getFact())/*.getFact()*/);
        } else if (!tree.isALeaf(node)) {
            System.out.println("├ " + askQuestion(node.getFact())/*.getQuestion()*/);
        }
        if (node.getLeft() != null) {
            leftNodes.addLast(node.getLeft());
            System.out.print("│");
        }
        printNode(node.getRight(), leftNodes);
        if (!leftNodes.isEmpty()) {
            printNode(leftNodes.pollLast(), leftNodes);
        }
    }

    private String askQuestion(String statement) {
        Pattern pattern = Pattern.compile(patternRes.getString("question.1.pattern"));
        Matcher matcher = pattern.matcher(statement);
        if (matcher.find()) {
            return matcher.replaceAll(patternRes.getString("question.1.replace"));
        } else {
            pattern = Pattern.compile(patternRes.getString("question.2.pattern"));
            matcher = pattern.matcher(statement);
            if (matcher.find()) {
                return matcher.replaceAll(patternRes.getString("question.2.replace"));
            }
        }
        return "";
    }

    private String getAnimalName(String statement) {
        return statement.replaceFirst(patternRes.getString("statement.0"), "").trim();
    }
}
