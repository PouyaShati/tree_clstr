package com.company;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Scanner;


public class AutoPlay
{
    public static void run() throws Exception
    {
        Scanner scanner = new Scanner(System.in);
        String base;
        System.out.println("Enter Base Command:");
        base = scanner.nextLine();

        int repeat = 1;
        int average = 1;

        if(base.split(" ")[0].equals("repeat"))
        {
            repeat = Integer.parseInt(base.split(" ")[1]);
            base = scanner.nextLine();
        }

        if(base.split(" ")[0].equals("average"))
        {
            average = Integer.parseInt(base.split(" ")[1]);
            base = scanner.nextLine();
        }

        ArrayList<ArrayList<String[]>> parameters = new ArrayList<>();

        for(String part: base.split(" "))
            if(part.charAt(0) == '!')
            {
                if(part.charAt(1) > 48 + parameters.size())
                    parameters.add(new ArrayList<>());
            }

        for(int i=0; i<parameters.size(); i++)
        {
            System.out.println("Enter Parameter " + (i+1) + " values");
            String values = scanner.nextLine();

            while(!values.equals("end"))
            {
                parameters.get(i).add(values.split(" "));
                values = scanner.nextLine();
            }
        }


        int[] counters = new int[parameters.size()];

        counters[0] = -1;

        ArrayList<String> commands = new ArrayList<>();

        OUTER: while(true)
        {
            int p=0;
            while(true)
            {
                counters[p] ++;
                if(counters[p] == parameters.get(p).size())
                {
                    counters[p] = 0;
                    p++;
                    if(p >= parameters.size())
                        break OUTER;
                }else
                    break;
            }

            String command = "";

            int[] position = new int[parameters.size()];

            for(String part: base.split(" "))
                if(part.charAt(0) == '$')
                {
                    command = command.substring(0, command.length()-1);
                }
                else if(part.charAt(0) == '!')
                {
                    command = command + parameters.get(part.charAt(1)-49).get(counters[part.charAt(1)-49])[position[part.charAt(1)-49]] + " ";
                    position[part.charAt(1)-49] ++;
                }else
                {
                    command = command + part + " ";
                }


            System.out.println(command);
            commands.add(command);
        }


        ArrayList<String> outputs = new ArrayList<>();

        System.out.println("Enter outputs to show: ");

        String output = scanner.next();
        while(!output.equals("end"))
        {
            outputs.add(output);
            output = scanner.next();
        }

        System.out.println("---------------------------------------------------");

        for(int j=0; j<outputs.size(); j++)
            System.out.print(outputs.get(j) + " ");
        System.out.println("Time");

        int avgCnt = 0;


        double[][] sumValues = new double[(commands.size() + average - 1)/average][];
        int[][] cntValues = new int[sumValues.length][];
        int[] statusUnknown = new int[cntValues.length];
        int[] statusTimedout = new int[cntValues.length];
        int[] statusInfeasible = new int[cntValues.length];
        int[] statusOptimum = new int[cntValues.length];
        for(int i=0; i<sumValues.length; i++)
        {
            sumValues[i] = new double[outputs.size()+1];
            cntValues[i] = new int[sumValues[i].length];
        }

        for(int i=0; i<commands.size(); i++)
        for(int r=0; r<repeat; r++)
        {
            long startTime = System.currentTimeMillis();

            String command = commands.get(i);

            ProcessBuilder processBuilder = new ProcessBuilder();
            processBuilder.command("bash", "-c", command);
            Process process = processBuilder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;

            String[] outputValues = new String[outputs.size()];

            while ((line = reader.readLine()) != null) {
                for(int j=0; j<outputs.size(); j++)
                {
                    if(line.startsWith(outputs.get(j)))
                    {
                        outputValues[j] = "";
                        for(int w=1; w< line.split(" ").length; w++)
                            outputValues[j] = outputValues[j] + " " + line.split(" ")[w];
                    }
                }
            }

            int exitVal = process.waitFor();


            for(int j=0; j<outputValues.length; j++)
            {
                try
                {
                    sumValues[i/average][j] += Double.parseDouble(outputValues[j]);
                    cntValues[i/average][j]++;
                }
                catch(Exception e)
                {

                    //not a double
                }
                System.out.print(outputValues[j] + ",");

                if(outputs.get(j).equals("Status:"))
                {
                    if(outputValues[j].equals(" Unknown"))
                        statusUnknown[i/average]++;
                    else if(outputValues[j].equals(" Timedout"))
                        statusTimedout[i/average]++;
                    else if(outputValues[j].equals(" Infeasible"))
                        statusInfeasible[i/average]++;
                    else if(outputValues[j].equals(" Optimum"))
                        statusOptimum[i/average]++;
                }
            }
            System.out.println(System.currentTimeMillis() - startTime);
            sumValues[i/average][outputValues.length] += (System.currentTimeMillis() - startTime);
            cntValues[i/average][outputValues.length]++;
        }

        if(average > 1)
        {
            System.out.print("-------------------------------------------------\nAverage Values:");

            for(int i=0; i<sumValues.length; i++)
            {
                System.out.println();
                for(int j=0; j<sumValues[i].length; j++)
                {
                    if(cntValues[i][j] > 0)
                        System.out.print((sumValues[i][j]/cntValues[i][j]) + ",");
                    else
                        System.out.print(sumValues[i][j] + ",");
                }
            }

            System.out.println("-------------------------------------------------\nStatus Counts:");

            System.out.println("Optimum:, Timedout:, Infeasible:, Unknown:");

            for(int i=0; i<statusUnknown.length; i++)
            {
                System.out.print("" + statusOptimum[i] + ",");
                System.out.print("" + statusTimedout[i] + ",");
                System.out.print("" + statusInfeasible[i] + ",");
                System.out.println("" + statusUnknown[i]);
            }
        }

    }
}
