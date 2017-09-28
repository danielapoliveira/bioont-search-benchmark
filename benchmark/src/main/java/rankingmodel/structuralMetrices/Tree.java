package rankingmodel.structuralMetrices;


import java.util.ArrayList;

/**
 * Created by Anila Sahar Butt.
 */
public class Tree {
    private Node root = new Node();

    public Tree(String node) {
        root.setNode(node);
        root.setNodeChildren(new ArrayList<Node>());
    }


    public void setRoot(Node node){
        this.root = node;
    }

    public Node getRoot(){
        return this.root;
    }

    public void addChildren(ArrayList<String> childlist){

        ArrayList<Node> childNodeList = new ArrayList<Node>();

        for(int i=0; i<childlist.size(); i++) {
            Node node = new Node();
            node.setNode(childlist.get(i));
            node.setNodeParent(root);
            node.setNodeChildren(new ArrayList<Node>());
            childNodeList.add(node);

        }

        root.setNodeChildren(childNodeList);

    }

    public void addChildrenForNode(Node nodeIRI, ArrayList<String> childlist){

        ArrayList<Node> childNodeList = new ArrayList<Node>();

        for(int i=0; i<childlist.size(); i++) {
            Node node = new Node();
            node.setNode(childlist.get(i));
            node.setNodeParent(nodeIRI);
            node.setNodeChildren(new ArrayList<Node>());
            childNodeList.add(node);

        }

        nodeIRI.setNodeChildren(childNodeList);

    }

    public boolean contains(String nodeIRI, Node node){

        boolean found = false;

        if (node.getNode().equalsIgnoreCase(nodeIRI)){
            found = true;
        } else {
            ArrayList<Node> children = node.getNodeChildren();
            for (int i=0; i< children.size(); i++){
                Node childNode = children.get(i);
                found = contains(nodeIRI, childNode);
                if(found==true){
                    break;
                }
            }
        }
        return found;
    }

    public Node getNode(String nodeIRI, Node node){

        Node resultNode = new Node();
        if (node.getNode().equalsIgnoreCase(nodeIRI)){
            resultNode = node;
        } else {
            ArrayList<Node> children = node.getNodeChildren();
            for (int i=0; i< children.size(); i++){
                Node childNode = children.get(i);
                resultNode = getNode(nodeIRI, childNode);
                if(!resultNode.getNode().equalsIgnoreCase("")){
                    break;
                }
            }
        }
        return resultNode ;
    }

    public boolean equals(){
        boolean flag=false;

        return flag;
    }
}
