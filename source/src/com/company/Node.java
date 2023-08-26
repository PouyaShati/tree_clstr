package com.company;

import java.util.ArrayList;
import java.util.Random;


public class Node
{
    Node left;
    Node right;

    public static int leafCount = 0;
    public static int branchCount = 0;

    int id;

    int feature;
    boolean categorical;
    double threshold;
    ArrayList<Double> values;

    public static ArrayList<Node> idList = new ArrayList<>();

    int label=0;

    public void setIds()
    {
        if(left == null)
            id += branchCount;

        while(idList.size() < id+1)
            idList.add(null);
        idList.set(id, this);

        if(left != null)
        {
            left.setIds();
            right.setIds();
        }
    }


    public Node(Node left, Node right)
    {
        this.left = left;
        this.right = right;

        id = branchCount;

        branchCount++;
    }

    public Node()
    {
        this.id = id;

        id = leafCount;

        leafCount++;
    }

    public static Node balancedTree(int height)
    {
        return balancedTree(height, 1.01);
    }

    public static Node balancedTree(int height, double nodeProb)
    {
        if(height == 0 || (Main.random.nextDouble() > nodeProb && height < Main.height))
            return new Node();

        return new Node(Node.balancedTree(height-1, nodeProb), Node.balancedTree(height-1, nodeProb));
    }

    public int predict(double[] point)
    {
        if(left == null)
        {
            return label;
        }

        if(categorical)
        {
            if(values.indexOf(point[feature])>=0)
                return left.predict(point);
            else
                return right.predict(point);
        }else
        {
            if(point[feature] <= threshold)
                return left.predict(point);
            else
                return right.predict(point);
        }
    }

    public void print()
    {
        if(left != null)
        {
            System.out.println("" + id + ": " + feature);
            if(categorical)
            {
                System.out.print("{");
                for(int i=0; i<values.size(); i++)
                {
                    System.out.print(values.get(i));
                    if(i < values.size()-1)
                        System.out.print(", ");
                    else
                        System.out.println("}");
                }
            }else
                System.out.println(threshold);

        }else
        {
            System.out.println("leaf: " + id + " : " + label);
        }
    }
}

