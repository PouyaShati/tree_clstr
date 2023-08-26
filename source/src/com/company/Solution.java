package com.company;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;


public class Solution
{

    public static void writeSolution(String name, String assignment, boolean writeFile) throws Exception
    {
        FileWriter fileWriter;
        PrintWriter printWriter = null;

        if(writeFile)
        {
            fileWriter = new FileWriter(name);
            printWriter = new PrintWriter(fileWriter);
        }

        int[] predicted = new int[Main.n];
        int pointer = 0;

        if(Main.useTree)
        {
            if(writeFile)
                printWriter.write("a:\n");


            for(int t=0; t<Main.tree.branchCount; t++)
            {
                Node toValue = Node.idList.get(t);

                for(int j=0; j<Main.f; j++)
                {
                    if(writeFile)
                        printWriter.write(" " + assignment.charAt(pointer));

                    if(assignment.charAt(pointer) == '1')
                    {
                        toValue.feature = j;
                        if(Main.anyCategorical && Main.categorical[j])
                        {
                            toValue.categorical = true;
                            toValue.values = new ArrayList<>();
                        }
                        else
                            toValue.categorical = false;
                    }

                    pointer++;
                }
                if(writeFile)
                    printWriter.write("\n");
            }
            if(writeFile)
                printWriter.write("\n");

            if(writeFile)
                printWriter.write("s:\n");
            for(int t=0; t<Main.tree.branchCount; t++)
            {
                Node toValue = Node.idList.get(t);

                int split=0;
                for(int i=0; i<Main.n; i++)
                {
                    if(writeFile)
                        printWriter.write(" " + assignment.charAt(pointer));

                    if(assignment.charAt(pointer) == '1')
                        if(toValue.categorical)
                        {
                            if(toValue.values.indexOf(Main.data[i][toValue.feature]) < 0)
                                toValue.values.add(Main.data[i][toValue.feature]);
                        }
                        else
                            split++;

                    pointer++;
                }

                if(writeFile)
                    printWriter.write("\n");

                if(!toValue.categorical)
                {
                    if(split > 0)
                        toValue.threshold = Main.data[Main.sorted[split-1][toValue.feature]][toValue.feature];
                    else
                        toValue.threshold = Main.data[Main.sorted[0][toValue.feature]][toValue.feature] - 1.0;
                }


            }
            if(writeFile)
                printWriter.write("\n");

            if(writeFile)
                printWriter.write("z:\n");
            for(int t=0; t<Main.tree.leafCount; t++)
            {
                for(int i=0; i<Main.n; i++)
                {
                    if(writeFile)
                        printWriter.write(" " + assignment.charAt(pointer));
                    if(assignment.charAt(pointer) == '1')
                        predicted[i] = t;
                    pointer++;
                }
                if(writeFile)
                    printWriter.write("\n");
            }
            if(writeFile)
                printWriter.write("\n");

            int treeP = Main.tree.branchCount;

            if(writeFile)
                printWriter.write("g:\n");
            for(int t=0; t<Main.tree.leafCount; t++)
            {
                int chosen = 1;

                for(int g=0; g<Main.k-1; g++)
                {
                    if(assignment.charAt(pointer) == '1')
                        chosen++;
                    if(writeFile)
                        printWriter.write(" " + assignment.charAt(pointer));
                    pointer++;
                }

                Node toValue = Node.idList.get(treeP);
                treeP++;
                toValue.label = chosen;

                if(writeFile)
                    printWriter.write("\n");
            }
            if(writeFile)
                printWriter.write("\n");
        }

        Main.notTree = new int[Main.n];

        if(writeFile)
            printWriter.write("x:\n");
        for(int i=0; i<Main.n; i++)
        {
            Main.notTree[i] = 1;
            for(int ind=0; ind<Main.k-1; ind++)
            {
                if(writeFile)
                    printWriter.write(" " + assignment.charAt(pointer));
                if(assignment.charAt(pointer) == '1')
                    Main.notTree[i]++;
                pointer++;
            }
            if(writeFile)
                printWriter.write("\n");
        }
        if(writeFile)
            printWriter.write("\n");

        if(Main.useTree)
            Main.notTree = null;

        Main.wcm_obj = -1;
        if(Main.objective.equals("wcm") || Main.objective.equals("wcm+bcm") || Main.objective.equals("wcm+bcm_p"))
        {
            if(writeFile)
                printWriter.write("DC:\n");

            for(int i=0; i<Main.groupCount; i++)
            {
                if(writeFile)
                    printWriter.write(" " +assignment.charAt(pointer));
                if(assignment.charAt(pointer) == '1')
                    Main.wcm_obj = i;
                pointer++;
            }
            if(writeFile)
                printWriter.write("\n");

            System.out.println("WCM OBJ: " + (Main.wcm_obj));
        }
        Main.bcm_obj = -1;
        if(Main.objective.equals("bcm") || Main.objective.equals("wcm+bcm") || Main.objective.equals("wcm+bcm_p"))
        {
            if(writeFile)
                printWriter.write("DM:\n");


            for(int i=0; i<Main.groupCount; i++)
            {
                if(writeFile)
                    printWriter.write(" " + assignment.charAt(pointer));

                if(assignment.charAt(pointer) == '1')
                    Main.bcm_obj = i;

                pointer++;
            }
            if(writeFile)
                printWriter.write("\n");

            System.out.println("BCM OBJ: " + (Main.bcm_obj));

        }

        if(writeFile)
            printWriter.close();

    }

    public static void printSolution() throws Exception
    {
        printSolution(true, true);
    }

    public static void printSolution(boolean printTree, boolean printDistances) throws Exception
    {
        int clusterCnt;
        int[] predicted = new int[Main.n];

        if(Main.useTree)
        {
            if(printTree)
            {
                System.out.println("Tree:");
                System.out.println("Depth: " + Main.height);
                ArrayList<Node> queue = new ArrayList<>();
                queue.add(Main.tree);

                while(queue.size() > 0)
                {
                    Node toPrint = queue.remove(0);
                    toPrint.print();
                    if(toPrint.left != null)
                    {
                        queue.add(toPrint.left);
                        queue.add(toPrint.right);
                    }
                }
            }

            clusterCnt = Main.k;

            for(int i1=0; i1<Main.n; i1++)
            {
                predicted[i1] = Main.tree.predict(Main.data[i1])-1;
            }

        }else
        {
            clusterCnt = Main.k;
            for(int i1=0; i1<Main.n; i1++)
                predicted[i1] = Main.notTree[i1]-1;
        }

        Main.labelFileCode = "" + System.currentTimeMillis();
        FileWriter fileWriter = new FileWriter("labels/"+Main.name + Main.postfix);
        PrintWriter printWriter = new PrintWriter(fileWriter);

        for(int i=0; i<Main.n; i++)
        {
            printWriter.write("" + predicted[i]);
            if(i<Main.n-1)
                printWriter.write("\n");
        }
        printWriter.close();

        long startTimeMetrics = System.currentTimeMillis();

        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command("bash", "-c", "python3 metrics.py " + Main.name + " " + Main.name + Main.postfix);
        Process process = processBuilder.start();
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));


        String line;
        int exitVal;

        while ((line = reader.readLine()) != null)
            System.out.println(line);

        exitVal = process.waitFor();

        System.out.println("Metrics_Time: " + (System.currentTimeMillis() - startTimeMetrics));

        double maxWCDistance = 0.0;
        double minBCDistance = Double.MAX_VALUE;
        double sumWCDistance = 0.0;

        int[] clusterSize;

        clusterSize = new int[clusterCnt];
        for(int i1=0; i1<Main.n; i1++)
            clusterSize[predicted[i1]]++;

        System.out.print("Cluster_sizes:");
        for(int c=0; c<clusterCnt; c++)
            System.out.print(" " + clusterSize[c]);
        System.out.println();


        if(printDistances)
        {
            long startTimeDistances = System.currentTimeMillis();

            for(int i1=0; i1<Main.n; i1++)
                for(int i2=i1+1; i2<Main.n; i2++)
                {

                    if (predicted[i1] == predicted[i2])
                        sumWCDistance += Main.unsortedDistances[i1 * Main.n + i2];

                    if (predicted[i1] == predicted[i2])
                        if (Main.unsortedDistances[i1 * Main.n + i2] > maxWCDistance)
                        {
                            maxWCDistance = Main.unsortedDistances[i1 * Main.n + i2];
                        }

                    if (predicted[i1] != predicted[i2])
                        if (Main.unsortedDistances[i1 * Main.n + i2] < minBCDistance)
                        {
                            minBCDistance = Main.unsortedDistances[i1 * Main.n + i2];
                        }
                }

            System.out.println("Max Distance: " + Main.maxDis);

            System.out.println("Final_Max_Diameter: " + maxWCDistance);
            System.out.println("Final_Min_Split: " + minBCDistance);
            System.out.println("Sum WC Distance: " + sumWCDistance);

            System.out.println("Distances_Time: " + (System.currentTimeMillis() - startTimeDistances));
        }


    }

}
