package com.company;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;


public class Util
{
    public static int pow(int a, int b)
    {
        if(b == 0)
            return 1;
        else return pow(a, b-1) * a;
    }

    public static int[] copy(int[] array)
    {
        int[] result = new int[array.length];
        for(int i=0; i<array.length; i++)
            result[i] = array[i];
        return result;
    }

    public static boolean[][] copy(boolean[][] array)
    {
        boolean[][] result = new boolean[array.length][];
        for(int i=0; i<array.length; i++)
        {
            result[i] = new boolean[array[i].length];
            for(int j=0; j<array[i].length; j++)
                result[i][j] = array[i][j];
        }

        return result;
    }

    public static void sort(double[][] data, int feature, int[][] sorted, int start, int end)
    {
        if(start >= end)
            return;

        sort(data, feature, sorted, start, (start+end)/2);
        sort(data, feature, sorted, (start+end)/2+1, end);

        int[] temp = new int[end-start+1];

        int i1 = start;
        int i2 = (start+end)/2+1;
        for(int i=0; i<end-start+1; i++)
            if(i1 <= (start+end)/2 && i2 <= end)
            {
                if(data[sorted[i1][feature]][feature] <  data[sorted[i2][feature]][feature])
                {
                    temp[i] = sorted[i1][feature];
                    i1++;
                }else
                {
                    temp[i] = sorted[i2][feature];
                    i2++;
                }
            }else if(i1 <= (start+end)/2)
            {
                temp[i] = sorted[i1][feature];
                i1++;
            }
            else if(i2 <= end)
            {
                temp[i] = sorted[i2][feature];
                i2++;
            }

        for(int i=0; i<end-start+1; i++)
            sorted[i+start][feature] = temp[i];
    }

    public static void sortDistance(double[] distance, int[] sortedI1, int[] sortedI2)
    {
        sortDistance(distance, sortedI1, sortedI2, 0, distance.length-1);
    }

    public static void sortDistance(double[] distance, int[] sortedI1, int[] sortedI2, int start, int end)
    {
        if(start >= end)
            return;

        sortDistance(distance, sortedI1, sortedI2, start, (start + end) / 2);
        sortDistance(distance, sortedI1, sortedI2, (start+end)/2+1, end);


        double[] temp = new double[end-start+1];
        int[] tempI1 = new int[end-start+1];
        int[] tempI2 = new int[end-start+1];

        int c1 = start;
        int c2 = (start+end)/2+1;
        for(int c=0; c<end-start+1; c++)
            if(c1 <= (start+end)/2 && c2 <= end)
            {
                if(distance[c1] <  distance[c2])
                {
                    temp[c] = distance[c1];
                    tempI1[c] = sortedI1[c1];
                    tempI2[c] = sortedI2[c1];
                    c1++;
                }else
                {
                    temp[c] = distance[c2];
                    tempI1[c] = sortedI1[c2];
                    tempI2[c] = sortedI2[c2];
                    c2++;
                }
            }else if(c1 <= (start+end)/2)
            {
                temp[c] = distance[c1];
                tempI1[c] = sortedI1[c1];
                tempI2[c] = sortedI2[c1];
                c1++;
            }
            else if(c2 <= end)
            {
                temp[c] = distance[c2];
                tempI1[c] = sortedI1[c2];
                tempI2[c] = sortedI2[c2];
                c2++;
            }

        for(int c=0; c<end-start+1; c++)
        {
            distance[c+start] = temp[c];
            sortedI1[c+start] = tempI1[c];
            sortedI2[c+start] = tempI2[c];
        }
    }

    public static String getAssignment(String[] input)
    {
        String result = "";

        for(int i=0; i<input.length; i++)
            if(input[i].charAt(0) == '-')
                result = result + "0";
            else
                result = result + "1";

        return result;
    }

    public static void calculateDistances()
    {
        Main.minDis = Double.MAX_VALUE;
        Main.maxDis = 0;

        Main.distances = new double[Main.n*Main.n];
        Main.unsortedDistances = new double[Main.n*Main.n];

        for(int i1=0; i1<Main.n; i1++)
            for(int i2=0; i2<Main.n; i2++)
            {
                if(i2 <= i1)
                {
                    Main.distances[i1*Main.n+i2] = -1.0;
                    continue;
                }

                Main.distances[i1*Main.n+i2] = 0;

                for(int j=0; j<Main.f; j++)
                    Main.distances[i1*Main.n+i2] += (Main.data[i1][j] - Main.data[i2][j]) * (Main.data[i1][j] - Main.data[i2][j]);

                Main.distances[i1*Main.n+i2] = Math.sqrt(Main.distances[i1*Main.n+i2]);

                if(Main.distances[i1*Main.n+i2] > Main.maxDis)
                    Main.maxDis = Main.distances[i1*Main.n+i2];
                if(Main.distances[i1*Main.n+i2] < Main.minDis)
                    Main.minDis = Main.distances[i1*Main.n+i2];
            }

        for(int i=0; i<Main.distances.length; i++)
            Main.unsortedDistances[i] = Main.distances[i];

        Main.sortedI1 = new int[Main.n*Main.n];
        Main.sortedI2 = new int[Main.n*Main.n];
        for(int i1=0; i1<Main.n; i1++)
            for(int i2=0; i2<Main.n; i2++)
            {
                if(i2 <= i1)
                {
                    Main.sortedI1[i1*Main.n+i2] = -1;
                    Main.sortedI2[i1*Main.n+i2] = -1;
                    continue;
                }
                Main.sortedI1[i1*Main.n+i2] = i1;
                Main.sortedI2[i1*Main.n+i2] = i2;
            }

        Util.sortDistance(Main.distances, Main.sortedI1, Main.sortedI2);

        Main.distanceThresholds = new ArrayList<>();

        for(int i=0; i<Main.distances.length; i++)
            if(Main.distances[i] > 0)
            {
                if(Main.distanceThresholds.size() == 0)
                    Main.distanceThresholds.add(Main.distances[i]);
                else if(Main.distances[i] > Main.distanceThresholds.get(Main.distanceThresholds.size()-1) + Main.distanceClassRange)
                    Main.distanceThresholds.add(Main.distances[i]);
            }


        Main.distanceThresholdsEnds = new ArrayList<>();
        for(int i=0; i<Main.distanceThresholds.size(); i++)
            Main.distanceThresholdsEnds.add(-1.0);

        int i=0;
        while(Main.distances[i] <= 0)
            i++;
        int p=0;
        while(i < Main.distances.length)
        {
            while(p+1 < Main.distanceThresholds.size() && Main.distances[i] >= Main.distanceThresholds.get(p+1))
                p++;
            Main.distanceThresholdsEnds.set(p, Main.distances[i]);
            i++;
        }

        Main.distanceThresholdsMembers = new ArrayList<>();
        for(int c=0; c<Main.distanceThresholds.size(); c++)
            Main.distanceThresholdsMembers.add(0);

        i=0;
        while(Main.distances[i] <= 0)
            i++;
        p=0;
        while(i < Main.distances.length)
        {
            while(p+1 < Main.distanceThresholds.size() && Main.distances[i] >= Main.distanceThresholds.get(p+1))
                p++;
            Main.distanceThresholdsMembers.set(p, Main.distanceThresholdsMembers.get(p)+1);
            i++;
        }

        System.out.println("Number of distance classes: " + Main.distanceThresholds.size());
    }


    public static void calculateSorts()
    {
        Main.sorted = new int[Main.data.length][];
        for(int i=0; i<Main.n; i++)
        {
            Main.sorted[i] = new int[Main.data[i].length];
            for(int j=0; j<Main.sorted[i].length; j++)
                Main.sorted[i][j] = i;
        }

        for(int j=0; j<Main.f; j++)
            Util.sort(Main.data, j, Main.sorted, 0, Main.n-1);

        Main.sortedInverse = new int[Main.data.length][];
        for(int i=0; i<Main.n; i++)
            Main.sortedInverse[i] = new int[Main.data[i].length];

        for(int j=0; j<Main.f; j++)
            for(int i=0; i<Main.n; i++)
                Main.sortedInverse[Main.sorted[i][j]][j] = i;
    }

    public static void readInstance(String name) throws Exception
    {

        File instance = new File("instance_" + name);
        Scanner scanner = new Scanner(instance);

        String flags = scanner.nextLine();

        Main.n = scanner.nextInt();
        Main.f = scanner.nextInt();

        Main.anyCategorical = false;

        if(flags.contains("c"))
        {
            Main.categorical = new boolean[Main.f];
            for(int j=0; j<Main.f; j++)
                if(scanner.nextInt() == 1)
                {
                    Main.categorical[j] = true;
                    Main.anyCategorical = true;
                }
                else
                    Main.categorical[j] = false;
        }else
        {
            Main.categorical = new boolean[Main.f];
        }

        double[] maxValues = new double[Main.f];
        double[] minValues = new double[Main.f];


        boolean labelsEnd = false;
        boolean labelsBeg = false;

        if(flags.contains(" le"))
            labelsEnd = true;
        else if(flags.contains(" lb"))
            labelsBeg = true;
        else if(flags.contains(" l"))
            labelsEnd = true;

        Main.label_cnt = 0;

        if(labelsBeg || labelsEnd)
        {
            Main.labels = new int[Main.n];
            Main.label_cnt = scanner.nextInt();
        }

        Main.data = new double[Main.n][];

        for(int i=0; i<Main.n; i++)
        {
            double[] new_entry = new double[Main.f];

            if(labelsBeg)
                Main.labels[i] = scanner.nextInt();

            for(int j=0; j<Main.f; j++)
            {
                new_entry[j] = scanner.nextDouble();
                if(i == 0)
                {
                    maxValues[j] = new_entry[j];
                    minValues[j] = new_entry[j];
                }else
                {
                    if(new_entry[j] > maxValues[j])
                        maxValues[j] = new_entry[j];
                    if(new_entry[j] < minValues[j])
                        minValues[j] = new_entry[j];
                }
            }

            if(labelsEnd)
                Main.labels[i] = scanner.nextInt();

            Main.data[i] = new_entry;
        }

        if(Main.normalize)
        {
            for(int i=0; i<Main.n; i++)
                for(int j=0; j<Main.f; j++)
                    if(maxValues[j] > minValues[j])
                        Main.data[i][j] = (Main.data[i][j] - minValues[j]) / (maxValues[j] - minValues[j]) * 100;
        }

    }


    public static void addConsts() throws Exception
    {

        Main.random.setSeed(Main.randomSeed);
        if(Main.ml_p > 0)
        {
            for(int i1 =0; i1<Main.n; i1++)
                for(int i2 =i1+1; i2<Main.n; i2++)
                    if(Main.labels[i1] == Main.labels[i2] && Main.random.nextDouble() < Main.ml_p)
                        Main.mustLinks.add(new int[]{i1, i2});
        }

        Main.random.setSeed(Main.randomSeed);
        if(Main.cl_p > 0)
        {
            for(int i1 =0; i1<Main.n; i1++)
                for(int i2 =i1+1; i2<Main.n; i2++)
                    if(Main.labels[i1] != Main.labels[i2] && Main.random.nextDouble() < Main.cl_p)
                        Main.cannotLinks.add(new int[]{i1, i2});
        }

        if(Main.ml_c_n > 0.0)
            Main.ml_c = (int) Math.round(Main.n * Main.ml_c_n);


        Main.random.setSeed(Main.randomSeed);
        if(Main.ml_c > 0)
        {
            ArrayList<Integer> pool_1 = new ArrayList<>();
            ArrayList<Integer> pool_2 = new ArrayList<>();

            for(int i1 =0; i1<Main.n; i1++)
                for(int i2 =i1+1; i2<Main.n; i2++)
                    if(Main.labels[i1] == Main.labels[i2])
                    {
                        pool_1.add(i1);
                        pool_2.add(i2);
                    }

            for(int i=0; i<Main.ml_c; i++)
            {
                int temp = Main.random.nextInt(pool_1.size());
                Main.mustLinks.add(new int[]{pool_1.remove(temp), pool_2.remove(temp)});
            }
        }

        if(Main.cl_c_n > 0.0)
            Main.cl_c = (int) Math.round(Main.n * Main.cl_c_n);

        Main.random.setSeed(Main.randomSeed);
        if(Main.cl_c > 0)
        {
            ArrayList<Integer> pool_1 = new ArrayList<>();
            ArrayList<Integer> pool_2 = new ArrayList<>();

            for(int i1 =0; i1<Main.n; i1++)
                for(int i2 =i1+1; i2<Main.n; i2++)
                    if(Main.labels[i1] != Main.labels[i2])
                    {
                        pool_1.add(i1);
                        pool_2.add(i2);
                    }

            for(int i=0; i<Main.cl_c; i++)
            {
                int temp = Main.random.nextInt(pool_1.size());
                Main.cannotLinks.add(new int[]{pool_1.remove(temp), pool_2.remove(temp)});
            }
        }


        if(Main.mcl_c_n > 0.0)
            Main.mcl_c = (int) Math.round(Main.n * Main.mcl_c_n);

        Main.random.setSeed(Main.randomSeed);
        if(Main.mcl_c > 0)
        {
            ArrayList<Integer> pool_1 = new ArrayList<>();
            ArrayList<Integer> pool_2 = new ArrayList<>();

            for(int i1 =0; i1<Main.n; i1++)
                for(int i2 =i1+1; i2<Main.n; i2++)
                {
                    pool_1.add(i1);
                    pool_2.add(i2);
                }

            for(int i=0; i<Main.mcl_c; i++)
            {
                int temp = Main.random.nextInt(pool_1.size());

                int i1 = pool_1.remove(temp);
                int i2 = pool_2.remove(temp);

                if(Main.labels[i1] != Main.labels[i2])
                    Main.cannotLinks.add(new int[]{i1, i2});
                else
                    Main.mustLinks.add(new int[]{i1, i2});
            }
        }


        FileWriter fileWriter = new FileWriter("consts/" + Main.name + Main.postfix);
        PrintWriter printWriter = new PrintWriter(fileWriter);

        for(int i=0; i<Main.mustLinks.size(); i++)
            printWriter.write("" + Main.mustLinks.get(i)[0] + " " + Main.mustLinks.get(i)[1] + "\n");
        printWriter.write("*\n");

        for(int i=0; i<Main.cannotLinks.size(); i++)
            printWriter.write("" + Main.cannotLinks.get(i)[0] + " " + Main.cannotLinks.get(i)[1] + "\n");
        printWriter.write("*");

        printWriter.close();
    }


}

