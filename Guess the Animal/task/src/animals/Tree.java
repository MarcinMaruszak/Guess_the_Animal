package animals;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Tree {
    private Node root;

    public Tree() {
    }

    public Node getRoot() {
        return root;
    }

    public void setRoot(Node root) {
        this.root = root;
    }

    public List<String> getAllLeaves() {
        List<String> leaves = new ArrayList<>();
        findLeaf(root, leaves);
        Collections.sort(leaves);
        return leaves;
    }

    private void findLeaf(Node node, List<String> list) {
        if (node == null) {
            return;
        }
        if (node.getRight() == null && node.getLeft() == null) {
            list.add(node.getFact());
        }
        if (node.getRight() != null) {
            findLeaf(node.getRight(), list);
        }
        if (node.getLeft() != null) {
            findLeaf(node.getLeft(), list);
        }
    }

    public List<String> getAllAnimalFacts(String animal, ResourceBundle patternRes) {
        Node node = findNode(root, null, animal);
        List<String> facts = new ArrayList<>();
        if (node != null) {
            traversUp(node, facts, patternRes);
        }
        Collections.reverse(facts);
        return facts;
    }


    private void traversUp(Node node, List<String> facts, ResourceBundle patternRes) {//todo
        Node parent = node.getParent();
        if (parent != null) {
            if(node == parent.getRight()){
                facts.add(parent.getFact());
            }else {
                facts.add(makeNegative(parent.getFact(), patternRes));
            }

            traversUp(parent, facts, patternRes);
        }
    }

    private String makeNegative(String statement, ResourceBundle patternRes){
        Pattern pattern = Pattern.compile(patternRes.getString("negative.1.pattern"));
        Matcher matcher = pattern.matcher(statement);
        if(matcher.find()){
            return matcher.replaceAll(patternRes.getString("negative.1.replace"));
        }else {
            pattern = Pattern.compile(patternRes.getString("negative.2.pattern"));
            matcher = pattern.matcher(statement);
            if(matcher.find()){
                return matcher.replaceAll(patternRes.getString("negative.2.replace"));
            }else {
                pattern = Pattern.compile(patternRes.getString("negative.3.pattern"));
                matcher = pattern.matcher(statement);
                if(matcher.find()){
                    return matcher.replaceAll(patternRes.getString("negative.3.replace"));
                }
            }
        }
        return "";
    }

    public int getNumberOfNodes() {
        return getNodes(root, 0);
    }

    private int getNodes(Node node, int i) {
        if (node != null) {
            i++;
            i = getNodes(node.getRight(), i);
            i = getNodes(node.getLeft(), i);
        }
        return i;
    }

    private Node findNode(Node node, Node toReturn, String animal) {

        if (node.getFact().contains(animal)) {
            toReturn = node;
            return toReturn;
        }
        if (node.getLeft() != null) {
            toReturn = findNode(node.getLeft(), toReturn, animal);
        }
        if (node.getRight() != null) {
            toReturn = findNode(node.getRight(), toReturn, animal);
        }
        return toReturn;
    }

    public List<Integer> getDepths() {
        List<Integer> depths = new ArrayList<>();
        getDepths(root, 0, depths);
        return depths;
    }

    private int getDepths(Node node, int depth, List<Integer> depths) {
        if (node.getLeft() == null && node.getRight() == null) {
            depths.add(depth);
        }else {
            depth++;
            if (node.getRight() != null) {
                getDepths(node.getRight(), depth, depths);
            }
            if (node.getRight() != null) {
                getDepths(node.getLeft(), depth, depths);
            }
            return depth;
        }
        return depth;
    }

    public List<String> getStatements() {
        List<String> facts = new ArrayList<>();
        return findStatement(root, facts);
    }

    private List<String> findStatement(Node node, List<String> facts) {
        if (node.getRight() != null || node.getLeft() != null) {
            facts.add(node.getFact());
        }
        if (node.getRight() != null) {
            findStatement(node.getRight(), facts);
        }
        if (node.getLeft() != null) {
            findStatement(node.getLeft(), facts);
        }
        return facts;
    }


    boolean isALeaf(Node node){
        return node.getLeft()==null&&node.getRight()==null;
    }
}
