package com.company;

import java.io.*;
import java.util.ArrayList;
import java.util.Random;

public class Main {
    // data:
    public static boolean normalize;
    public static int n;
    public static int f;
    public static int k;
    public static double[][] data;
    public static boolean[] categorical;
    public static boolean anyCategorical;
    public static int[] labels;
    public static int label_cnt;
    public static int[][] sorted;
    public static int[][] sortedInverse;

    // distances
    public static double minDis;
    public static double maxDis;
    public static double[] distances;
    public static double[] unsortedDistances;
    public static int[] sortedI1;
    public static int[] sortedI2;
    public static double distanceClassRange;
    public static ArrayList<Double> distanceThresholds;
    public static ArrayList<Double> distanceThresholdsEnds;
    public static ArrayList<Integer> distanceThresholdsMembers;

    // smart parameters
    public static boolean smartLinks;
    public static boolean timedout;
    public static int groupCount;
    public static int[] groups;

    // objective parameters
    public static String objective;
    public static int wcm_obj;
    public static int bcm_obj;
    //public static int range_obj;

    // solver
    public static int single_timeout;
    public static double solutionCost;
    public static int solutionLB;
    public static boolean unknownSolution;
    public static String output;
    public static int base_var_cnt;
    public static int obj_var_cnt;
    public static ArrayList<int[]> baseHardClauses;
    public static ArrayList<int[]> objHardClauses;
    public static ArrayList<int[]> objSoftClauses;
    public static ArrayList<Double> objSoftClausesWeights;
    public static int weights_sum_to_print = 0;

    // tree
    public static boolean useTree;
    public static Node tree;
    public static int[] notTree;
    public static int height;
    public static boolean mustSplit;

    // output and files
    public static String name;
    public static String postfix="";
    public static String labelFileCode;
    public static long startTime;

    public static int fileCnt=0;

    public static ArrayList<int[]> mustLinks = new ArrayList<>();
    public static double[] mlDis;
    public static ArrayList<int[]> cannotLinks = new ArrayList<>();
    public static double[] clDis;
    public static double ml_p = 0.0;
    public static double cl_p = 0.0;
    public static int ml_c = 0;
    public static int cl_c = 0;
    public static int mcl_c = 0;
    public static double ml_c_n = 0.0;
    public static double cl_c_n = 0.0;
    public static double mcl_c_n = 0.0;

    public static Random random = new Random();

    public static int randomSeed = 11111;

    public static String solver_path = "~/SAT_Project/loandra-master/loandra_static";



    public static void main(String[] args) throws Exception
    {
        if (args[0].equals("-autoplay")) // execute a sequence of auto-generated runs instead of one
        {
            AutoPlay.run();
            System.exit(0);
        }

        int arg_p = 0;
        height = 1;
        boolean printResult = false;
        normalize = false;
        useTree = false;
        mustSplit = false;
        single_timeout = 30;

        while (arg_p < args.length - 1)
        {
            if (args[arg_p].equals("-post")) // postfix to save intermediate results with
            {
                postfix = args[arg_p + 1];
                arg_p += 2;
            } else if (args[arg_p].equals("-d")) // use tree clustering and specify depth
            {
                height = Integer.parseInt(args[arg_p + 1]);
                useTree = true;
                arg_p += 2;
            } else if (args[arg_p].equals("-print")) // whether to print the results
            {
                printResult = true;
                arg_p += 1;
            } else if (args[arg_p].equals("-norm")) // normalize the values of each feature in the dataset
            {
                normalize = true;
                arg_p += 1;
            } else if (args[arg_p].equals("-obj")) // specify the objective (wcm=md, bcm=ms, wcm+bcm_p=[md,ms]), the epsilon value, and whether to use smart pairs technique
            {
                objective = args[arg_p + 1];
                if (objective.equals("wcm") || objective.equals("bcm") || objective.equals("wcm+bcm_p"))
                {
                    distanceClassRange = Double.parseDouble(args[arg_p + 2]);
                    arg_p += 3;
                } else
                    arg_p += 2;

                if (args[arg_p].equals("-smart"))
                {
                    smartLinks = true;
                    arg_p += 1;
                }
            } else if (args[arg_p].equals("-k")) // number of clusters
            {
                k = Integer.parseInt(args[arg_p + 1]);
                arg_p += 2;
            } else if (args[arg_p].equals("-sto")) // timeout
            {
                single_timeout = Integer.parseInt(args[arg_p + 1]);
                arg_p += 2;
            } else if (args[arg_p].equals("-ml_p")) // add ML by probability of each pair appearing
            {
                ml_p = Double.parseDouble(args[arg_p + 1]);
                arg_p += 2;
            }else if (args[arg_p].equals("-cl_p")) // add CL by probability of each pair appearing
            {
                cl_p = Double.parseDouble(args[arg_p + 1]);
                arg_p += 2;
            }else if (args[arg_p].equals("-ml_c"))  // add ML by number (fraction of |X| if specified by n)
            {
                if(args[arg_p + 1].startsWith("n"))
                    ml_c_n = Double.parseDouble(args[arg_p + 1].substring(1));
                else
                    ml_c = Integer.parseInt(args[arg_p + 1]);
                arg_p += 2;
            }else if (args[arg_p].equals("-cl_c"))  // add CL by number (fraction of |X| if specified by n)
            {
                if(args[arg_p + 1].startsWith("n"))
                    cl_c_n = Double.parseDouble(args[arg_p + 1].substring(1));
                else
                    cl_c = Integer.parseInt(args[arg_p + 1]);
                arg_p += 2;
            }else if (args[arg_p].equals("-mcl_c"))  // add ML/CL by number (fraction of |X| if specified by n)
            {
                if(args[arg_p + 1].startsWith("n"))
                    mcl_c_n = Double.parseDouble(args[arg_p + 1].substring(1));
                else
                    mcl_c = Integer.parseInt(args[arg_p + 1]);
                arg_p += 2;
            }else if (args[arg_p].equals("-ml")) // add ML in individual pairs
            {
                String[] temp = args[arg_p + 1].split("-");
                for(int i =0; i<temp.length/2; i++)
                {
                    int[] temp2 = new int[2];
                    temp2[0] = Integer.parseInt(temp[i*2]);
                    temp2[1] = Integer.parseInt(temp[i*2+1]);
                    mustLinks.add(temp2);
                }
                arg_p += 2;
            }else if (args[arg_p].equals("-cl")) // add CL in individual pairs
            {
                String[] temp = args[arg_p + 1].split("-");
                for(int i =0; i<temp.length/2; i++)
                {
                    int[] temp2 = new int[2];
                    temp2[0] = Integer.parseInt(temp[i*2]);
                    temp2[1] = Integer.parseInt(temp[i*2+1]);
                    cannotLinks.add(temp2);
                }
                arg_p += 2;
            }else if (args[arg_p].equals("-seed")) // set random seed
            {
                randomSeed = Integer.parseInt(args[arg_p + 1]);
                arg_p += 2;
            }else if (args[arg_p].equals("-path"))
            {
                solver_path = args[arg_p + 1];
                arg_p += 2;
            }

            System.out.println(arg_p);
        }

        name = args[args.length - 1]; // dataset name
        startTime = System.currentTimeMillis();

        if (useTree) // constructs a tree if one is needed
        {
            System.out.println("Constructing Tree");

            tree = Node.balancedTree(height);

            tree.setIds();

            System.out.println("Branch Count: " + tree.branchCount);
            System.out.println("Leaf Count: " + tree.leafCount);
        }

        Util.readInstance(name); // reads the dataset
        Util.addConsts(); // construct the ML and CL sets
        Util.calculateSorts(); // sort the dataset across different features

        Util.calculateDistances(); // calculate and sort the distances between all pairs and group them into distance classes

        base_var_cnt = 0;
        baseHardClauses = new ArrayList<>();

        if(useTree)
        {
            VarsAndCons.addConA(); // adding clauses from Eq. 7,8
            VarsAndCons.addConSplit(); // adding clauses from Eq. 9,10,14,15
            VarsAndCons.addConZ(); // adding clauses from Eq. 11,12,13
            VarsAndCons.addConGP(); // adding clauses from Eq. 16,17,18
        }else
        {
            VarsAndCons.addConX(); // adding clauses from Eq. 39
        }

        VarsAndCons.addXTieBreaker(); // adding clauses from Eq. 19,20
        VarsAndCons.addConForceClusters(k); // adding the clause from Eq. 21

        objHardClauses = new ArrayList<>();
        objSoftClauses = new ArrayList<>();
        objSoftClausesWeights = new ArrayList<>();
        obj_var_cnt = 0;

        groups = new int[distanceThresholds.size()];
        for(int i=0; i<groups.length; i++)
            groups[i] = i;

        groupCount = 0;
        for(int i=0; i<groups.length; i++)
        {
            if(groups[i]+1 > groupCount)
                groupCount = groups[i]+1;
        }

        System.out.println("Group Count: " + groupCount);

        VarsAndCons.addConC(mustLinks, cannotLinks); // adding clauses from Eq. 22-36

        solve(single_timeout); // solve the instance

        System.out.println("Time_Before_Print: " + (System.currentTimeMillis() - startTime));

        if(printResult)
            Solution.printSolution(); // prints the solution and the metrics

        System.out.println("------------------------------------------------");

        System.out.println("Time: " + (System.currentTimeMillis() - startTime));
    }

    public static void writeClauses(String name) throws Exception
    {
        FileWriter fileWriter = new FileWriter(name);
        PrintWriter printWriter = new PrintWriter(fileWriter);

        printWriter.write("c Height: " + height + "\n");
        printWriter.write("c must split: " + mustSplit + "\n");

        double weights_sum = 1.0;
        double leastWeight = Double.MAX_VALUE;

        for(int i=0; i<objSoftClauses.size(); i++)
        {
            objSoftClausesWeights.set(i, 0.0 + Math.round(objSoftClausesWeights.get(i)));

            weights_sum += objSoftClausesWeights.get(i);

            if(objSoftClausesWeights.get(i) < leastWeight)
                leastWeight = objSoftClausesWeights.get(i);

        }

        weights_sum_to_print = (int) Math.ceil(weights_sum);


        printWriter.write("p wcnf " + (base_var_cnt+obj_var_cnt) + " " + (baseHardClauses.size() + objHardClauses.size() + objSoftClauses.size()) + " " + weights_sum_to_print + "\n");

        int lenSum = 0;

        for(int i=0; i<objSoftClauses.size(); i++)
        {
            String to_write;

            to_write = "" + (int) Math.round(objSoftClausesWeights.get(i));

            for(int j=0; j<objSoftClauses.get(i).length; j++)
                to_write = to_write + " " + objSoftClauses.get(i)[j];
            to_write = to_write + " 0\n";
            printWriter.write(to_write);
            lenSum += objSoftClauses.get(i).length;
        }

        for(int i=0; i<baseHardClauses.size(); i++)
        {
            String to_write;

            to_write = "" + weights_sum_to_print;

            for(int j=0; j<baseHardClauses.get(i).length; j++)
                to_write = to_write + " " + baseHardClauses.get(i)[j];
            to_write = to_write + " 0\n";
            printWriter.write(to_write);
            lenSum += baseHardClauses.get(i).length;
        }

        for(int i=0; i<objHardClauses.size(); i++)
        {
            String to_write;

            to_write = "" + weights_sum_to_print;

            for(int j=0; j<objHardClauses.get(i).length; j++)
                to_write = to_write + " " + objHardClauses.get(i)[j];
            to_write = to_write + " 0\n";
            printWriter.write(to_write);
            lenSum += objHardClauses.get(i).length;
        }

        System.out.println("Number_of_variables: " + (base_var_cnt + obj_var_cnt));
        System.out.println("Number_of_clauses: " + (baseHardClauses.size() + objHardClauses.size() + objSoftClauses.size()));
        System.out.println("Clause_average_length: " + ((double) lenSum) / (baseHardClauses.size() + objHardClauses.size() + objSoftClauses.size()));

        printWriter.close();
    }


    public static void solve(int input_timeout) throws Exception
    {
        double timeout;

        timeout = input_timeout;

        long init_time = System.currentTimeMillis();

        String clauseName = "clauses/clauses_" + name + postfix + "_" + fileCnt;
        String solutionName = "solutions/solution_" + name + postfix + "_" + fileCnt;
        String logName = "logs/log_" + name + postfix + "_" + fileCnt;

        fileCnt++;

        writeClauses(clauseName);

        ProcessBuilder processBuilder = new ProcessBuilder();

        processBuilder.command("bash", "-c", "timeout " + timeout + "m " + solver_path + " -pmreslin-cglim=30 -weight-strategy=1 -print-model "+ clauseName);

        Process process = processBuilder.start();

        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

        FileWriter fileWriter = new FileWriter(logName);
        PrintWriter printWriter = new PrintWriter(fileWriter);

        timedout = true;
        solutionCost = -1;
        unknownSolution = false;
        output = "";

        String line;
        String allLines = "";
        while ((line = reader.readLine()) != null)
        {
            allLines = allLines + line + "\n";
            printWriter.write(line + "\n");

            if(line.charAt(0) == 'v')
            {
                output = Util.getAssignment(line.substring(2).split(" "));
            }

            if(line.charAt(0) == 's')
            {
                if(line.split(" ")[1].equals("UNKNOWN"))
                {
                    System.out.println("Unknown Solution");
                    unknownSolution = true;
                }
                if(line.split(" ")[1].equals("OPTIMUM") && line.split(" ")[2].equals("FOUND"))
                {
                        timedout = false;
                }
            }

            if(line.charAt(0) == 'o')
                solutionCost = Double.parseDouble(line.split(" ")[1]);

            if(line.charAt(0) == 'c' && line.split(" ").length > 4 && line.split(" ")[4].equals("LB:")) // does this only work for loandra?
                solutionLB = Integer.parseInt(line.split(" ")[5]);

        }
        printWriter.close();


        int exitVal = process.waitFor();
        if (exitVal == 0) {
            System.out.println("Solved!");
        }

        if(solutionCost < 0)
            timedout = false;

        System.out.println("Unknown: " + unknownSolution);
        System.out.println("Timedout: " + timedout);
        System.out.println("Solver_Cost: " + solutionCost);

        if(unknownSolution)
            System.out.println("Status: Unknown");
        else if(timedout)
            System.out.println("Status: Timedout");
        else if(solutionCost < 0)
            System.out.println("Status: Infeasible");
        else
            System.out.println("Status: Optimum");

        System.out.println("Timeout: " + timeout);
        System.out.println("Solver_LB: " + solutionLB);
        System.out.println("Solver_Time: " + (System.currentTimeMillis() - init_time));

        if(solutionCost < 0)
        {
            System.out.println("Time_Before_Print: " + (System.currentTimeMillis() - startTime));
            System.out.println(allLines);
            System.exit(1);
        }else
            Solution.writeSolution(solutionName, output, true);
    }
}
